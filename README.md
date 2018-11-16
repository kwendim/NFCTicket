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

The authentication process uses a diversified key (based on the user id) to authenticate each card separately so if the key of one card is compromised it does not affect the whole system. Another key is used for the HMAC process, saved only on the reader; which protects against Mitm attacks. The system also provides tearing protection by utilizing multiple memory page usage for read/write operations. We implement roll-back protection using the counter.

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
      </tr>
  
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

Describe here the contents of each data field if it is not obvious from the diagram.

## Key management

Overview of key management on both cards and readers.

## Implementation

Brief overview of the implementation: where is your code and what does it do?

## Evaluation

### Security evaluation

What kind of security is achieved and any known weakesses.

### Reliability and deployablity

How reliability and deployability were considered in the ticket design.

## Final notes

Give here feedback to the teachers, such as open questions that you could not solve and what was difficult.
