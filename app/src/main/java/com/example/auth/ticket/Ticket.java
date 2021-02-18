package com.example.auth.ticket;


import android.util.Log;

import com.example.auth.app.ulctools.Commands;
import com.example.auth.app.ulctools.Utilities;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;


/**
 * TODO: Complete the implementation of this class. Most of the code are already implemented. You
 * will need to change the keys, design and implement functions to issue and validate tickets.
 */
public class Ticket {

    private static byte[] defaultAuthenticationKey = "BREAKMEIFYOUCAN!".getBytes();// 16-byte key
    private static byte[] custom_auth_key="netsec12".getBytes();

    /** TODO: Change these according to your design. Diversify the keys. */
    private static byte[] authenticationKey = defaultAuthenticationKey;// 16-byte key
    private static byte[] hmacKey = "0123456789ABCDEF".getBytes(); // min 16-byte key


    public static byte[] data = new byte[192];

    private static TicketMac macAlgorithm; // For computing HMAC over ticket data, as needed
    private static Utilities utils;
    private static Commands ul;

    private Boolean isValid = false;
    private int remainingUses = 0;
    private int expiryTime = 0;

    private static String infoToShow; // Use this to show messages in Normal Mode

    /** Create a new ticket */
    public Ticket() throws GeneralSecurityException {
        // Set HMAC key for the ticket
        macAlgorithm = new TicketMac();
        macAlgorithm.setKey(hmacKey);

        ul = new Commands();
        utils = new Utilities(ul);
    }

    /** After validation, get ticket status: was it valid or not? */
    public boolean isValid() {
        return isValid;
    }

    /** After validation, get the number of remaining uses */
    public int getRemainingUses() {
        return remainingUses;
    }

    /** After validation, get the expiry time */
    public int getExpiryTime() {
        return expiryTime;
    }

    /** After validation/issuing, get information */
    public static String getInfoToShow() {
        String tmp = infoToShow;
        infoToShow = "";
        return tmp;
    }


    private byte[] diversifyMacKey(byte[] uid) {
        try {
            ByteArrayOutputStream diversifiedKeyStream = new ByteArrayOutputStream();
            diversifiedKeyStream.write(hmacKey);
            diversifiedKeyStream.write(Arrays.copyOfRange(uid, 0, 3));
            diversifiedKeyStream.write(Arrays.copyOfRange(uid, 4, 8));
            return diversifiedKeyStream.toByteArray();
        }
        catch(IOException ex) {
            return null;
        }
    }

    private byte[] generateDate(Calendar date) {
        // reset hour, minutes, seconds and millis
        date.set(Calendar.HOUR_OF_DAY, 0);
        date.set(Calendar.MINUTE, 0);
        date.set(Calendar.SECOND, 0);
        date.set(Calendar.MILLISECOND, 0);

        // next day
        date.add(Calendar.DAY_OF_MONTH, 1);
        byte[] dateArray = {(byte)(date.get(Calendar.YEAR) - 2000),(byte)date.get(Calendar.MONTH)
                ,(byte)date.get(Calendar.DAY_OF_MONTH)};

        return dateArray;
    }

    private byte[] generateNewTicket(int counterVal) {
        byte[] freshTicket = {0x05, 0x00, 0x00, 0x00};
        byte[] ticketInfo = new byte[8];
        if (counterVal % 2 == 0)
            utils.readPages(7, 2, ticketInfo, 0);
        else
            utils.readPages(5, 2, ticketInfo, 0);

        int currentNumOfRides = ticketInfo[0];
        byte[] expiryDate = Arrays.copyOfRange(ticketInfo, 1, 4);
        ByteBuffer wrapped = ByteBuffer.wrap(expiryDate);
        int currentExpiryDate = wrapped.getShort();

        if (currentNumOfRides > 95) {
            return null;
        }

        else if (currentNumOfRides == 0)
            return freshTicket;

        else if (currentExpiryDate == 0) {
            freshTicket[0] = (byte)(ticketInfo[0] + 5);
            return freshTicket;
        }
        else {
            Calendar dateNow = new GregorianCalendar();
            Calendar savedDate = new GregorianCalendar();
            savedDate.set(expiryDate[0] + 2000, expiryDate[1], expiryDate[2], 0, 0);

            if (dateNow.compareTo(savedDate) > 0) {
                return freshTicket;
            }
            else {
                freshTicket[0] = (byte)(ticketInfo[0] + 5);
                System.arraycopy(ticketInfo, 1, freshTicket, 1, 3);
                return freshTicket;
            }
        }
    }

    private void writeAuthValues() {
        byte[] authValues = new byte[8];
        authValues[0] = 0x05;
        utils.writePages(authValues, 0, 42, 2);
    }

    private void writeVersionTagValues() {
        byte[] version_tag = new byte[4];
        version_tag[1]=0x01;
        version_tag[3]=0x01;
        utils.writePages(version_tag, 0, 4, 1);

    }
    private byte[] diversifyAuthKey(byte[] uid, boolean write) {

        byte[] trim_uid = Arrays.copyOfRange(uid,0,8);
        byte[] reversed_trim_uid = reverse_byte_array(trim_uid);
        byte[] reversed_auth_key = reverse_byte_array(custom_auth_key);
        byte[] authentication_key = utils.concatArrays(reversed_auth_key,reversed_trim_uid);
        if (write) {
            utils.writePages(authentication_key, 0, 44, 4);
        }
        return authentication_key;

    }


    private byte[] reverse_byte_array(byte[] toBeReversed){
        byte[] reversedArray = new byte[toBeReversed.length];
        System.arraycopy(toBeReversed, 0, reversedArray, 0, reversedArray.length);
        for(int i = 0; i < reversedArray.length / 2; i++)
        {
            byte temp = reversedArray[i];
            reversedArray[i] = reversedArray[reversedArray.length - i - 1];
            reversedArray[reversedArray.length - i - 1] = temp;
        }
        return reversedArray;
    }


    private String getDate(Calendar cal) {
        DateFormat fmt = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
        return fmt.format(cal.getTime());
    }
    /**
     * Issue new tickets
     *
     * TODO: IMPLEMENT
     */
    public boolean issue(int daysValid, int uses) throws GeneralSecurityException {
        boolean res;

        byte[] uid = new byte[12];
        boolean uidRead = utils.readPages(0,3, uid,0);

        if(!uidRead){
            Utilities.log("Reading uid failed in issue()", true);
            infoToShow = "Reading uid failed";
            return false;
        }




        byte[] version_tag = new byte[4];
        res = utils.readPages(4,1,version_tag,0);

        if(!res){
            Utilities.log("Reading Version and Tag failed()",true);
            infoToShow = "Reading Version and Tag failed";
            return false;
        }
        byte[] untouched_card = new byte[]{0x00,0x00,0x00,0x00};


        if (Arrays.equals(version_tag,untouched_card)){
            // Authenticate
            Log.d("Version_tag", "version_tag value is equal to 0");
            res = utils.authenticate(authenticationKey);
            if (!res) {
                Utilities.log("Authentication failed in issue()", true);
                infoToShow = "Authentication failed";
                return false;
            }

            writeAuthValues();
            writeVersionTagValues();
            diversifyAuthKey(uid, true);
            byte[] initCounter = {0, 0, 0, 0};
            utils.writePages(initCounter, 0, 41, 1);

        }
        else if (Arrays.equals(version_tag,new byte[]{0,0x01,0,0x01})){

            infoToShow="Version and tag matches our values. Trying diversified key";

            byte[] authDiversifiedKey = diversifyAuthKey(uid, false);
            res = utils.authenticate(authDiversifiedKey);
            if (!res) {
                Utilities.log("Authentication failed in issue()", true);
                infoToShow = "Authentication failed";
                return false;
            }

        }
        else{
            Utilities.log("Unknown Version and Tag",true);
            infoToShow="Unknown Version and Tag";
            return false;
        }


        byte[] hmacDiversifiedKey = diversifyMacKey(uid);

        byte[] counterPage = new byte[4];
        boolean counterRead = utils.readPages(41,1,counterPage,0);

        if(!counterRead){
            Utilities.log("Reading uid failed in issue()", true);
            infoToShow = "Failed to read counter";
            return false;
        }

        byte[] counterMem = Arrays.copyOfRange(counterPage, 0, 2);
        byte[] counterFlipped = {counterMem[1], counterMem[0]};
        ByteBuffer wrapped = ByteBuffer.wrap(counterFlipped);
        int counterVal = wrapped.getShort();
        ++counterFlipped[1];

        byte[] ticket = generateNewTicket(counterVal);
        if (ticket == null) {
            infoToShow = "Sorry you cannot purchase more than 100 tickets at a time.";
            return false;
        }
        byte[] dataToMac = utils.concatArrays(ticket, counterFlipped);
        macAlgorithm.setKey(hmacDiversifiedKey);
        byte[] ticketHmac = macAlgorithm.generateMac(dataToMac);



        byte[] dataToWrite = utils.concatArrays(ticket,ticketHmac);
        if (counterVal % 2 == 0) {
            res = utils.writePages(dataToWrite, 0, 5, 2);
        }
        else {
            res = utils.writePages(dataToWrite, 0, 7, 2);
        }

        if (!res) {
            Utilities.log("Ticket write failed in issue()", true);
            infoToShow = "Ticket write failed";
            return false;
        }
        byte[] incCounter = {0x01, 0, 0, 0};
        res = utils.writePages(incCounter, 0, 41, 1);
        if (!res) {
            infoToShow = "You removed the card too fast, please re-tap your card.";
            return false;
        }
        infoToShow = "Ticket issued successfully! You now have " + ticket[0] + " rides";
        return true;
    }



    /**
     * Use ticket once
     *
     * TODO: IMPLEMENT
     */
    public boolean use() throws GeneralSecurityException {
        boolean res;
        byte[] version_tag = new byte[4];
        res = utils.readPages(4,1,version_tag,0);

        if(!res){
            Utilities.log("Reading Version and Tag failed()",true);
            infoToShow = "Reading Version and Tag failed";
            return false;
        }
        byte[] untouched_card = new byte[]{0x00,0x00,0x00,0x00};


        if (Arrays.equals(version_tag,untouched_card)) {
            infoToShow = "This card has no tickets, please issue tickets first";
            return false;
        }

        byte[] uid = new byte[12];
        utils.readPages(0,3, uid,0);
        byte[] diversifiedAuthKey = diversifyAuthKey(uid, false);
        res = utils.authenticate(diversifiedAuthKey);
        if (!res) {
            Utilities.log("Authentication failed in use()", true);
            infoToShow = "Authentication failed";
            return false;
        }


        byte[] hmacDiversifiedKey = diversifyMacKey(uid);


        byte[] counterPage = new byte[4];
        utils.readPages(41,1,counterPage,0);
        byte[] counterMem = Arrays.copyOfRange(counterPage, 0, 2);
        byte[] counterFlipped = {counterMem[1], counterMem[0]};
        ByteBuffer wrapped = ByteBuffer.wrap(counterFlipped);
        int counterVal = wrapped.getShort();


        byte[] ticketInfo = new byte[8];
        if (counterVal % 2 == 0)
            utils.readPages(7, 2, ticketInfo, 0);
        else
            utils.readPages(5, 2, ticketInfo, 0);


        byte[] ticket = Arrays.copyOfRange(ticketInfo,0, 4);
        byte[] dataToMac = utils.concatArrays(ticket, counterFlipped);

        macAlgorithm.setKey(hmacDiversifiedKey);
        byte[] ticketHmac = macAlgorithm.generateMac(dataToMac);
        if (!Arrays.equals(Arrays.copyOfRange(ticketHmac, 0, 4), Arrays.copyOfRange(ticketInfo, 4, 8))) {
            infoToShow = "Invalid ticket";
            return false;
        }

        int currentNumOfRides = ticketInfo[0];
        if (currentNumOfRides == 0) {
            infoToShow = "No rides available, please purchase new ones";
            return false;
        }
        else if (currentNumOfRides > 100) {
            infoToShow = "Suspicious number of tickets detected";
            return false;
        }
        byte[] expiryDate = Arrays.copyOfRange(ticketInfo, 1, 4);
        wrapped = ByteBuffer.wrap(expiryDate);
        int currentExpiryDate = wrapped.getShort();
        Calendar dateNow = new GregorianCalendar();
        Calendar savedDate = new GregorianCalendar();

        if (currentExpiryDate == 0) {
            expiryDate = generateDate(dateNow);
            savedDate.set(expiryDate[0] + 2000, expiryDate[1], expiryDate[2], 0, 0, 0);
        }
        else {

            savedDate.set(expiryDate[0] + 2000, expiryDate[1], expiryDate[2], 0, 0, 0);
            if (dateNow.compareTo(savedDate) > 0) {
                infoToShow = "Ticket has expired, please purchase a new ticket";
                return false;
            }
        }

        ticket[0] = (byte)--currentNumOfRides;

        for (int i = 0; i < expiryDate.length; ++i) {
            ticket[i + 1] = expiryDate[i];
        }

        ++counterFlipped[1];
        dataToMac = utils.concatArrays(ticket, counterFlipped);
        macAlgorithm.setKey(hmacDiversifiedKey);
        ticketHmac = macAlgorithm.generateMac(dataToMac);

        byte[] dataToWrite = utils.concatArrays(ticket,ticketHmac);

        if (counterVal % 2 == 0) {
            res = utils.writePages(dataToWrite, 0, 5, 2);
        }
        else {
            res = utils.writePages(dataToWrite, 0, 7, 2);
        }

        if (!res) {
            Utilities.log("Ticket write failed in validate()", true);
            infoToShow = "Ticket write failed";
            return false;
        }

        byte[] incCounter = {0x01, 0, 0, 0};
        res = utils.writePages(incCounter, 0, 41, 1);
        if (!res) {
            infoToShow = "You removed the card too fast, please re-tap your card.";
            return false;
        }
        infoToShow =  "Remaining ride(s): " + Integer.toString(currentNumOfRides) + "\nValid until: " + getDate(savedDate) + " 23:59";
        return true;
    }
}