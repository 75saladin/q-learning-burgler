11/21/2016:
- Submission for part 1 of the assignment. Implements the environment of a 
  Q-Learning Burgler as described in assignment.pdf.

Usage:
Compile: javac QLearn.java
Run: javac QLearn <input>

<input> ::= The relative path of the input file

Input file format: (All lines are plain space-separated integer values)
Line 1: Width of board, number of trolls, number of ponies
Line 2: escape location as two integers representing an x,y pair (bottom left 
        is 0 0)
Line 3: A series of x y pairs to define pony locations
Line 4: A series of x y pairs to define obstruction locations (if none, this 
        line is a single pair: -1 -1)
Line 5: A series of x y pairs to define troll locations

Example input file can be found in input.txt