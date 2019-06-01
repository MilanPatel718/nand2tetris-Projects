// This file is part of www.nand2tetris.org
// and the book "The Elements of Computing Systems"
// by Nisan and Schocken, MIT Press.
// File name: projects/04/Mult.asm

// Multiplies R0 and R1 and stores the result in R2.
// (R0, R1, R2 refer to RAM[0], RAM[1], and RAM[2], respectively.)


//Initalize R2 to 0
@R2
M = 0

//If R0 > R1, Sum R0 with itself R1 times
//else, Sum R1 with itself R2 times

@R0
D = M

@R1
D = D - M

@R0_GREATER
D;JGT

@R1_GREATER
0;JMP

//Case when R0 > R1
(R0_GREATER)
@i
M = 1
@LOOP_R0
0;JMP

(LOOP_R0)
@i
D = M
@R1
D = D - M 
@END
D;JGT
@R0
D = M
@R2
M = D + M
@i
M = M + 1
@LOOP_R0
0;JMP

//Case when R1 > R0
(R1_GREATER)
@i
M = 1
@LOOP_R1
0;JMP

(LOOP_R1)
@i
D = M
@R0
D = D - M
@END
D;JGT
@R1
D = M
@R2
M = D + M
@i
M = M + 1
@LOOP_R1
0;JMP

//Signals end of program
(END)
	@END
	0;JMP 