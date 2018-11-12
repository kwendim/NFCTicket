###### CS-E4300 Network security, 2018

### Getting started

1. Clone the project repository
2. Create a __private__ repository for your group
3. Add all group members, as well as the following users (as reporters):
    * Tuomas Aura (@aura)
    * Aleksi Peltonen (@peltona4)
4. Replace this document with your project report. The structure of the report is provided below.


***


# Project Report: Ticket Application by Group _N_

_Alice, Bob (list names of the group members here)_

## Overview

Brief summary of your ticket design, user experience, and intended security properties.


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
    <td> 4 </td>
    <td> 04h </td>
    <td> user memory </td>
    <td> user memory </td>
    <td> user memory </td>
    <td> user memory </td>
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
