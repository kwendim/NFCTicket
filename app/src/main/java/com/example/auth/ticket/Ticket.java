package com.example.auth.ticket;

import android.util.Log;

import com.example.auth.app.ulctools.Commands;
import com.example.auth.app.ulctools.Utilities;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.util.Arrays;


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
        byte[] dataToMac = utils.concatArrays(ticket, counterMem);

        macAlgorithm.setKey(hmacDiversifiedKey);
        byte[] ticketHmac = macAlgorithm.generateMac(dataToMac);


        ByteBuffer wrapped = ByteBuffer.wrap(counterMem); // big-endian by default
        int counterVal = wrapped.getShort();
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
        /*byte[] ticketToCheck = new byte[8];
        utils.readPages(5, 2, ticketToCheck, 0);
        if (Arrays.equals(Arrays.copyOfRange(dataToWrite,0,8), ticketToCheck))*/

        infoToShow = "Ticket issued successfully! You now have five rides";
        remainingUses = 5;
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

        // Example of reading:
        byte[] ticket = new byte[4];
        res = utils.readPages(5, 1, ticket, 0);

        // Set information to show for the user
        if (!res) {
            Utilities.log("Read  ticket in use()", true);
            infoToShow = "Failed to read";
        }

        return true;
    }
}