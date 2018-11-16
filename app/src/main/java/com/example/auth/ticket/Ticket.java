package com.example.auth.ticket;

import android.util.Log;

import com.example.auth.app.ulctools.Commands;
import com.example.auth.app.ulctools.Utilities;

import java.security.GeneralSecurityException;

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

    /**
     * Issue new tickets
     *
     * TODO: IMPLEMENT
     */
    public boolean issue(int daysValid, int uses) throws GeneralSecurityException {
        boolean res;

        // Authenticate
        res = utils.authenticate(authenticationKey);
        if (!res) {
            Utilities.log("Authentication failed in issue()", true);
            infoToShow = "Authentication failed";
            return false;
        }

        byte[] UID = new byte[192];
        boolean uid_read = utils.readPages(0,44,UID,0);

        if(uid_read){
            Log.i("UID", new String(UID));
            infoToShow = new String(UID);
        }

        byte[] counter = utils.readMemory();
        boolean counter_read = utils.readPages(44,4,counter,0);

        if(counter_read){
            infoToShow = new String(counter);
        }
        else{
            Log.i("counter", new String(counter));
        }



        // Example of writing:
//        byte[] message = "info".getBytes();
//        res = utils.writePages(message, 0, 6, 1);
//
//        // Set information to show for the user
//        if (res) {
//            infoToShow = "Wrote: " + new String(message);
//        } else {
//            infoToShow = "Failed to write";
//        }

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
            Utilities.log("Authentication failed in issue()", true);
            infoToShow = "Authentication failed";
            return false;
        }

        // Example of reading:
        byte[] message = new byte[4];
        res = utils.readPages(6, 1, message, 0);

        // Set information to show for the user
        if (res) {
            infoToShow = "Read: " + new String(message);
        } else {
            infoToShow = "Failed to read";
        }

        return true;
    }
}