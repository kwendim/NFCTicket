###### CS-E4300 Network security, 2018

### Getting started

1. Clone the project repository
2. Create a __private__ repository for your group
3. Add all group members, as well as the following users (as reporters):
    * Tuomas Aura (@aura)
    * Aleksi Peltonen (@peltona4)
4. Replace this document with your project report. The structure of the report is provided below.


***


# Project Report: Ticket Application by Group 25

Kidus Mammo, Mohammad Elhariry, Sonika Ujwal
## Overview

The ticket is designed to address the main security issues with the Ultralight-C NFC card. The information that is stored on the card is: Version number, tag, available rides, expiry date and the HMAC of the sensitive data. In addition, the counter is utilized to implement multiple memory page usage for read/write operations.

The user can buy tickets and use them or give them as a gift. The expiry date is set on first use (validation) to be the next midnight. The tickets are sold in fives and the maximum number of available rides a user can have on the card is limited to 150 (a reasonable  number of merry go round rides). The user can also top-up their card with more rides.

The authentication process uses a diversified key (based on the user id) to authenticate each card separately so if the key of one card is compromised it does not affect the whole system. Another key is used for the HMAC process which is saved only on the reader. The system also provides tearing protection by utilizing multiple memory page usage for read/write operations. We implement roll-back protection using the counter.

## Ticket application structure

The Ultralight memory is organised as [below](https://www.nxp.com/docs/en/data-sheet/MF0ICU2_SDS.pdf). Update the table below to reflect the data structure of your card application.

<table>
  <tr>
    <td colspan="2"><b><center> Page address </center></b></td>
    <td colspan="4"><b><center> Byte number </center></b></td>
  </tr>
  <tr>
    <td><b><center> Decimal </center></b></td>
    <td><b><center> Hex </center></b></td>
    <td><b><center> 0 </center></b></td>
    <td><b><center> 1 </center></b></td>
    <td><b><center> 2 </center></b></td>
    <td><b><center> 3 </center></b></td>
  </tr>

  <tr>
    <td> 0 </td>
    <td> 00h </td>
    <td colspan="4"> serial number </td>
  </tr>

  <tr>
    <td> 1 </td>
    <td> 01h </td>
    <td colspan="4"> serial number </td>
  </tr>

  <tr>
    <td> 2 </td>
    <td> 02h </td>
    <td> serial number </td>
    <td> internal </td>
    <td> lock bytes </td>
    <td> lock bytes </td>
  </tr>

  <tr>
    <td> 3 </td>
    <td> 03h </td>
    <td> OTP </td>
    <td> OTP </td>
    <td> OTP </td>
    <td> OTP </td>



<tr>
  <td> 4 </td>
  <td> 04h </td>
  <td> version </td>
  <td> version </td>
  <td> tag </td>
  <td> tag </td>
</tr>

<tr>
  <td> 5 </td>
  <td> 05h </td>
  <td> Number of rides </td>
  <td> Expiry date </td>
  <td> Expiry date </td>
  <td> Expiry date </td>
</tr>

<tr>
  <td> 6 </td>
  <td> 06h </td>
  <td> MAC </td>
  <td> MAC </td>
  <td> MAC </td>
  <td> MAC </td>
</tr>

<tr>
    <td> 7 </td>
    <td> 07h </td>
    <td> Number of rides </td>
    <td> Expiry date </td>
    <td> Expiry date </td>
    <td> Expiry date </td>
  </tr>

  <tr>
    <td> 8 </td>
    <td> 08h </td>
    <td> MAC </td>
    <td> MAC </td>
    <td> MAC </td>
    <td> MAC </td>
  </tr>

  
  <tr>
    <td> ... </td>
    <td> ... </td>
    <td> </td>
    <td> </td>
    <td> </td>
    <td> </td>
  </tr>

  <tr>
    <td> ... </td>
    <td> ... </td>
    <td> </td>
    <td> </td>
    <td> </td>
    <td> </td>
  </tr>

  <tr>
    <td> ... </td>
    <td> ... </td>
    <td> </td>
    <td> </td>
    <td> </td>
    <td> </td>
  </tr>

  <tr>
    <td> 40 </td>
    <td> 28h </td>
    <td> lock bytes </td>
    <td> lock bytes </td>
    <td> - </td>
    <td> - </td>
  </tr>

  <tr>
    <td> 41 </td>
    <td> 29h </td>
    <td> 16-bit counter </td>
    <td> 16-bit counter </td>
    <td> - </td>
    <td> - </td>
  </tr>

  <tr>
    <td> 42 </td>
    <td> 2Ah </td>
    <td colspan="4"> authentication configuration </td>
  </tr>

  <tr>
    <td> 43 </td>
    <td> 2Bh </td>
<td colspan="4"> authentication configuration </td>
  </tr>

  <tr>
    <td> 44 to 47 </td>
    <td> 2Ch to 2Fh </td>
<td colspan="4"> authentication key </td>
  </tr>
</table>

The validation algorithm needs the data to be duplicated (two pages each) to solve the tearing issue, hence the repition of pages 5 and 6 on pages 7 and 8. This wil be explained thoroughly in the implementation section below.

## Key management

The authentication key is stored on the reader using the Android [keystore](https://developer.android.com/training/articles/keystore) system. The reader sets the key of the card at issue time. The key is formed of a generated key and the UID of the NFC card.

The HMAC key is stored only on the reader using the same method as the authentication key and the same format (a different generated key and the UID of the NFC card).

## Implementation

The implementation is done in the Ticket.java file, formed of two main operations: issuing new tickets/rides and validation.

### Issuing

The system will start by reading the value of the counter memory then it will generate a new ticket whose data is formed of the number of tickets and the expiry date (but expiry date will be set during validation). The HMAC of the ticket and the counter memory value will be generated as well. The system then check if the counter value is even, then it writes to memory pages 5 and 6 or to memory pages 7 and 8 if the counter value is odd.

### Validation

The system will start also by reading the value of the counter memory, if the value is even then it will read from memory pages 5 and 6 and write to memory pages 7 and 8 (and vice versa if the value is odd). The system will then read the ticket value, recalculate the MAC of the ticket and the counter memory value to ensure it matches the MAC saved on the card during issuing. If the HMAC is correct and the number or rides is greater than  zero the system will add the expiry date (if it is not already written), decrement the number of rides, recalculate the HMAC for the ticket number, counter + 1 and the expiry date and the write the new data to the specified memory pages. The last step will be incrementing the counter and writing it to the counter memory.

## Evaluation

### Security evaluation

The system protects against the following issues:

1. Mitm attacks: By using an HMAC for the ticket (number of rides, expiry date), the system is protected against an attacker who tries to modify the ticket details. The attacker will not be able to increment ride counts or rollback the date on expired tickets. 

2. Rollback prevention: The system uses an irreversible counter whose data is also included in the HMAC. If a rollback attack is performed, the HMAC will be different; hence no authentication will be performed.

3. Remaining replay vulnerability: This attack will be made more difficult by setting the AUTH1 parameter to 0.

Known weaknesses:

1. Passback: Users of the card can use the same card by passing it back for another round of ticket purchase. It is possible to deal with this issue by adding a timestamp in a different page, and making sure a ticket can not be used again within a certain time frame(Let's say 10 minutes). Though this will increase our security feature, it will require us to do more read and writes which might increase a user's ticket validation time.

2. Although the system has the AUTH 1 parameter configured to prevent read/write acccess without authentication, there is still the remaining replay vulnerability which we can not deal with without session integrity.

3. The diversified key uses the UID of the NFC card as a part of it. If an attacker were to find out this format, it would reduce the entropy of they key (The UID could be read from the card using any NFC reader).

### Reliability and deployablity

The following aspects are taken into consideration:

1. Tearing protection: Utilizing multiple memory pages for read/write operations protects against tearing. According to the implementation, the validation process checks the counter value; if the number is even the reader will read from pages 5 and 6 and write to pages 7 and 8 (and vice versa if the number is odd). The last step is to update the counter so in case the user removes the card faster than the write process, the counter will not be updated and hence the data will not be corrupted and no tickets are wrongly issued. 

2. Version upgrade: The multiple pages implementation of the data structure allows us to have a tearing proof method to implement more features in the long run. Version updates can be made much more smoothly.

## Final notes

The decision of using Android [keystore](https://developer.android.com/training/articles/keystore) system is not final as we have not yet tested this functionality before.It might be altered later on during the implementation if we find a better alternative. In addition, based on the time we have, we will try to implement other features that enhance security or improve user experience when diving deeper into the implementation. The documentation will be updated accordingly. 


In case the user wants to add rides while he still has valid tickets, we have two approaches but have not decided which would be best. The first approach is to prevent the user from buying new rides while he still has valid rides on his card; but this is not the best user experience. The second approach is to add more logic to the issuing part which will be as follows: Read the ticket data from the user memory to check if he has any valid rides, if not proceed with the logic mentioned in the implementation. If yes, we will add the number of new rides to the number of currently present rides then write the updated ticket to the user memory.