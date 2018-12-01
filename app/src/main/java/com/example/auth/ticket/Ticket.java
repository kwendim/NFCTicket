package com.example.auth.ticket;


import com.example.auth.app.ulctools.Commands;
import com.example.auth.app.ulctools.Utilities;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;


/**
 * TODO: Complete the implementation of this class. Most of the code are already implemented. You
 * will need to change the keys, design and implement functions to issue and validate tickets.
 */
public class Ticket {

    private static byte[] defaultAuthenticationKey = "BREAKMEIFYOUCAN!".getBytes();// 16-byte key

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


    private byte[] generateDiversifiedKey(byte[] uid, boolean useAuthKey) {
        try {
            ByteArrayOutputStream diversifiedKeyStream = new ByteArrayOutputStream();
            if (useAuthKey) {
                diversifiedKeyStream.write(authenticationKey);
            }
            else {
                diversifiedKeyStream.write(hmacKey);
            }
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

    /**
     * Issue new tickets
     *
     * TODO: IMPLEMENT
     */
    public boolean issue(int daysValid, int uses) throws GeneralSecurityException {
        boolean res;
        byte[] ticket = {0x05, 0x00, 0x00, 0x00};
        byte[] uid = new byte[12];
        boolean uidRead = utils.readPages(0,3, uid,0);

        //TODO: HANDLE AUTHENTICATION
        byte[] authDiversifiedKey = generateDiversifiedKey(uid, true);
        byte[] hmacDiversifiedKey = generateDiversifiedKey(uid, false);

        if(!uidRead){
            Utilities.log("Reading uid failed in issue()", true);
            infoToShow = "Reading uid failed";
            return false;
        }

        // Authenticate
        res = utils.authenticate(authenticationKey);
        if (!res) {
            Utilities.log("Authentication failed in issue()", true);
            infoToShow = "Authentication failed";
            return false;
        }

        byte[] counterPage = new byte[4];
        boolean counterRead = utils.readPages(41,1,counterPage,0);

        if(!counterRead){
            Utilities.log("Reading uid failed in issue()", true);
            infoToShow = "Failed to read counter";
            return false;
        }

        byte[] counterMem = Arrays.copyOfRange(counterPage, 0, 2);
        byte[] counterFlipped = {counterMem[1], counterMem[0]};
        ByteBuffer wrapped = ByteBuffer.wrap(counterMem);
        int counterVal = wrapped.getShort();
        ++counterFlipped[1];


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
        utils.writePages(incCounter, 0, 41, 1);
        infoToShow = "Ticket issued successfully! You now have five more rides";
        return true;
    }

    /**
     * Use ticket once
     *
     * TODO: IMPLEMENT
     */
    public boolean use() throws GeneralSecurityException {
        boolean res;
        // Authenticate
        res = utils.authenticate(authenticationKey);
        if (!res) {
            Utilities.log("Authentication failed in use()", true);
            infoToShow = "Authentication failed";
            return false;
        }


        byte[] uid = new byte[12];
        boolean uidRead = utils.readPages(0,3, uid,0);
        byte[] hmacDiversifiedKey = generateDiversifiedKey(uid, false);
        // Example of reading:

        byte[] counterPage = new byte[4];
        boolean counterRead = utils.readPages(41,1,counterPage,0);
        byte[] counterMem = Arrays.copyOfRange(counterPage, 0, 2);
        byte[] counterFlipped = {counterMem[1], counterMem[0]};
        ByteBuffer wrapped = ByteBuffer.wrap(counterFlipped);
        int counterVal = wrapped.getShort();


        byte[] ticketInfo = new byte[8];
        if (counterVal % 2 == 0)
            res = utils.readPages(7, 2, ticketInfo, 0);
        else
            res = utils.readPages(5, 2, ticketInfo, 0);


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

        byte[] expiryDate = Arrays.copyOfRange(ticketInfo, 1, 4);
        wrapped = ByteBuffer.wrap(expiryDate);
        int currentExpiryDate = wrapped.getShort();
        Calendar dateNow = new GregorianCalendar();

        if (currentExpiryDate == 0) {
            expiryDate = generateDate(dateNow);
        }
        else {
            Calendar savedDate = new GregorianCalendar();
            savedDate.set(expiryDate[0] + 2000, expiryDate[1], expiryDate[2], 0, 0);

            if (dateNow.compareTo(savedDate) == 1) {
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
        utils.writePages(incCounter, 0, 41, 1);
        infoToShow = "Ticket valid! You now have " + Integer.toString(currentNumOfRides) + " ride(s) left";
        return true;
    }
}