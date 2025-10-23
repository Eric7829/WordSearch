# ICS4U – Word Search Assignment	

# Overview

Word Search puzzles are fun little puzzles found in the daily newspapers. Practicing word search puzzles helps you develop cognitive skills in tasks such as finding information and learning new words.

In this assignment, you will be working in teams of 2 to 3(max). You will work as a team to come up with the algorithms and create two programs to complete the assigned task. The first program will generate a word search puzzle from a given file with words and specified grid size. The second program will solve a given word search puzzle by finding all the words that would be provided on a text file. The design documentation will explain the algorithms you will use in the two programs.

# Part A: Planning Phase

During the planning phase of the project you must include a detailed plan and work breakdown structure and schedule in the form of a GANTT chart.   \- Oct. 30, 2025

Algorithm 1: Word Search Puzzle Generator

In this section, you will provide the algorithm to generate word search puzzles. You will also include test cases and the testing process of how you will ensure that this program will meet the requirements specified for this Project.  Please state the variables, methods, and classes you will be using and how you will be representing the puzzle in your program.

Algorithm 2: Word Search Puzzle Solver

In this section, you will provide the algorithm to solve word search puzzles. You will also include test cases and the testing process of how you will ensure that this program will meet the requirements  specified for this Project. Please state the variables, methods, and classes you will be using and how you will be representing the puzzle in your program.

**The above two phases will be due at the end of class:  Nov. 4, 2025**

# Part B: Word Search Generator

The word search generator will generate word search puzzles by placing words from a given input file into a word search puzzle. The word search generator must be able to place words vertically, horizontally, and diagonally (from top left to bottom right and top right to bottom left) into the word search puzzle. The word search generator must also be able to place the given words spelt backwards into the puzzle as well.

| HORIZONTAL FORWARD SDRAWKCAB | V E R T I C A L | D  I   A    G     O      N       A        L |        D       I      A     G    O   N  A L |
| :---- | :---- | :---- | :---- |

A good user interface (graphical)  will be created for the Word Search Generator to display user prompts. An input file containing the words to be placed into the puzzle will be specified by the user with file name and extension (ex. input.txt). It will be a plain text file that contains one word per line. All the text will be lower case and will have a maximum of 10 words. The words in the input file will be 4 to 8 letters long, inclusive.

Example Input File

| this test file contains seven words inside |
| :---- |

The input file will be in the same directory as the Word Search Generator program. You can assume the user will always give the input file name correctly.

Once the user has given the input file, the user is prompted for the grid dimensions with row first and then column. The minimum value for row and column is 10 and the maximum value is 20\. Row and columns do not have to be the same value. You must re-prompt the user if the grid size is invalid and have the user enter a valid value.

User will then be prompted to specify the solution file name and extension (ex. solution.txt) for the solution file. The solution file will be saved in the same directory as the program. This file will contain the placement of the placed words from the input file. Where there are no letters, a space will be used. All letters will be capitals.

The solution file is used to help you test your word placement. Instead of searching through a word puzzle to find your words, it will be much easier to look at the solution file to see where the words are placed in the puzzle file. The location of the placed words in the solution file MUST be in the same location as the puzzle file.

Solution File Example (15x15)

|         NFT            E I   E          V  L   S        E C E   T      S O              N   T          T   H          A   I S        I   S   D      N         R    S           O                W                                         EDISNI   |
| :---- |

The user will also be prompted to specify the puzzle file name and extension for the puzzle file (ie. puzzle.txt). The puzzle file will contain the generated word search puzzle. There will be no spaces between the letters. All the letters in the word search puzzle are capitals. All other letters in the word search puzzle must be random\!

Puzzle File Example (15x15)

| GGNNFKNFNFTIVLF VNUECRKEFIYERQA QFXTZJVEILLASQM BLUCPEACPEDNVTG ZECFSNOUJSIPEWS FNOPSNDACTASHJC AAHUTZASHIJCTNN KYNATFVIKSPFZNN NBINBNSTYWDBDXI VNKCJQBGKLDRAIZ SHVLOFEJULJCOEI TIBWKFGADBIFLWB PKXZSYVCFJCVBCI LLDXPTCMIXAOGGG MTBEGWNEDISNIAZ |
| :---- |

After a puzzle has been generated, the user will be prompted to restart the program or quit.

# 

# Part C: Word Search Solver

The word search solver will find the list of given words in a given word search puzzle that is generated from the word search generator. You may assume that the given words will always be in the given word search puzzle. The word search solver must be able to find words that are placed horizontally, vertically, and diagonally. The word search solver must also be able to find words that are spelled backwards.

A graphical user interface will be created for the Word Search Solver to display user prompts. An input file containing the words that will be in the following puzzle file will be specified by the user with file name and extension (ex. input.txt). The input file follows the same specification as the input file in Part B.

The user is then prompted for the puzzle file. The puzzle file is the same type of puzzle file generated from the word puzzle generator.

The user will then be prompted for an html file where the puzzle will be placed into a table and the words will be highlighted. The letters in the highlighted cells will also be bolded. An example html file will be available to you.

Example HTML File

Once all the input has been entered, a timer will begin. Once the html file is generated, the timer will stop. The program will display the time it took to read the input files, solve the puzzle, and generate the html file. The run time for my example program will be used as the baseline time. The time will be in milliseconds.

**Parts: B & C:**

* **Initial submission to show progress todate: Due TBD**

* **Final Due date: TBD**

# Part D: Presentation

Each group will do a 5 minute presentation just to explain the algorithms they used to generate the word search puzzles as well as how they found the words in a given word search puzzle. The group will then do a live demonstration of the Word Search Solver so we can see how efficient their algorithm is. I will be providing the test files on demonstration day. The files will be a 20x20 puzzle with 10 words that are each 8 letters long. Part D will be marked based on the presentation rubric.

**Part D presentation will begin on:**

**Part A: Design Documentation Evaluation**  
**Project Management Phases:**

| A  GANTT chart  is used to show: tasks, task allocation and timeline (refer rubric) | /10 |
| :---- | :---: |

**Word Search Generator Algorithm Design:**

| Criteria | Mark |
| :---- | :---: |
| Does the algorithm place words forwards and backwards? | /2 |
| Does the algorithm place words horizontally & vertically? | /2 |
| Does the algorithm place words diagonally from left to right? | /2 |
| Does the algorithm place words diagonally from right to left? | /2 |
| Can the words in the puzzle intersect & have letters on the border? (ie. Share same letters) | /2 |
| Is the algorithm presented clearly in adequate details as per given rubric | /4 |
| Total | /14 |

**Word Search Solver Algorithm Design:**

| Criteria | Mark |
| :---- | :---: |
| Does the algorithm find words placed forwards and backwards? | /2 |
| Does the algorithm find words placed horizontally & vertically? | /2 |
| Does the algorithm find words placed diagonally from left to right? | /2 |
| Does the algorithm find words placed diagonally from right to left? | /2 |
| Does the algorithm test border cases (words on borders, overlapping letters between words, other) ? | /2 |
| Is the algorithm presented clearly in adequate details as per given rubric | /4 |
| Total | /14 |

**Test Cases**

| Criteria | Mark |
| :---- | :---: |
| Test Cases for input files | /4 |
| Test Cases for word placement | /4 |
| Test Cases for finding words | /4 |
| Total | /12 |

**Part B: Word Search Puzzle Generator**

| Criteria | Mark |
| :---- | :---: |
| Reads input file correctly based on specifications | /2 |
| Can create grids of size 10x10, 20x20 and grids in between | /1 |
| Outputs a solution file as stated in specifications | /2 |
| Outputs a puzzle file as stated in specifications | /3 |
| Words can be placed into puzzle horizontally | /1 |
| Words can be placed into puzzle vertically | /1 |
| Words can be placed into puzzle diagonally left to right | /2 |
| Words can be placed into puzzle diagonally right to left | /2 |
| Words can be placed forwards or backwards | /1 |
| Puzzle is filled with random characters | /1 |
| Provides a good graphical user interface: includes: choose option (G/S), enter file name, show words, process, repeat, quit | /4 |
| Code formatted and commented properly | /4 |
| **Total** | /24 |

**Part C: Word Search Puzzle Solver**

| Criteria | Mark |
| :---- | :---: |
| Reads input file correctly based on specifications | /1 |
| Reads in puzzle file correctly based on specifications | /2 |
| Outputs a solution file as stated in specifications | /2 |
| Outputs an html file as stated in specifications | /3 |
| Horizontally & Vertically placed words can be found | /2 |
| Diagonally left to right words can be found | /2 |
| Diagonally right to left words can be found | /2 |
| Forward and backward words can be found | /1 |
| Method is efficient (4/4 means faster than given base time) | /4 |
| User has option to start program again instead of just quitting | /1 |
| Code formatted and commented properly | /4 |
|  | /24 |

