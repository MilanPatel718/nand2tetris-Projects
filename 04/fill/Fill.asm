// This file is part of www.nand2tetris.org
// and the book "The Elements of Computing Systems"
// by Nisan and Schocken, MIT Press.
// File name: projects/04/Fill.asm

// Runs an infinite loop that listens to the keyboard input.
// When a key is pressed (any key), the program blackens the screen,
// i.e. writes "black" in every pixel;
// the screen should remain fully black as long as the key is pressed. 
// When no key is pressed, the program clears the screen, i.e. writes
// "white" in every pixel;
// the screen should remain fully clear as long as no key is pressed.

//Immediately listen for keyboard input
@LISTEN_LOOP_INIT
0;JMP

(LISTEN_LOOP_INIT)
	@i
	M = 0
	@LISTEN_LOOP
	0;JMP

(LISTEN_LOOP)
	@SCREEN
	D = A
	@i
	A = D + M
	M = 0
	@i
	M = M + 1
	D = M
	
	@8192
	D = D - A
	@LISTEN_LOOP_INIT
	D;JGT
	@KBD
	D = M
	@DARK_INIT
	D;JGT
	@LISTEN_LOOP
	0;JMP

(DARK_INIT)
	@i
	M = 0
	@DARKEN_LOOP
	0;JMP

(DARKEN_LOOP)
	@SCREEN
	D = A
	@i
	A = D + M
	M = -1
	@i
	M = M + 1
	D=M
	@8192
	D = D - A
	@DARK_INIT
	D;JGT
	@KBD
	D = M
	@LISTEN_LOOP_INIT
	D;JEQ
	@DARKEN_LOOP
	0;JMP

	



