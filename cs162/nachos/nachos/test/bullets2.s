	.file	1 "bullets2.c"
gcc2_compiled.:
__gnu_compiled_c:
	.rdata
	.align	2
$LC0:
	.ascii	"bullets2.c\000"
	.align	2
$LC1:
	.byte	115
	.byte	104
	.byte	97
	.byte	107
	.byte	101
	.byte	32
	.byte	110
	.byte	32
	.byte	98
	.byte	97
	.byte	107
	.byte	101
	.byte	10
	.byte	0
	.align	2
$LC2:
	.ascii	"abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123"
	.ascii	"456789\n\000"
	.align	2
$LC3:
	.ascii	"wherediditgo.test\000"
	.align	2
$LC4:
	.ascii	"alphanum.txt\000"
	.align	2
$LC5:
	.ascii	"alphanum2.test\000"
	.align	2
$LC6:
	.ascii	"open() worked on nonexistent file\000"
	.align	2
$LC7:
	.ascii	"couldn't open() file alphanum\000"
	.align	2
$LC8:
	.ascii	"couldn't creat() file alphanum2\000"
	.align	2
$LC9:
	.ascii	"didn't write() 13 bytes to stdout\000"
	.align	2
$LC10:
	.ascii	"didn't read() 20 bytes from stdin\000"
	.align	2
$LC11:
	.ascii	"no error reading from stdout\000"
	.align	2
$LC12:
	.ascii	"no error writing to stdin\000"
	.align	2
$LC13:
	.ascii	"input isn't length 20\000"
	.align	2
$LC14:
	.ascii	"read() worked requesting -1 bytes\000"
	.align	2
$LC15:
	.ascii	"write() worked requesting -1 bytes\000"
	.align	2
$LC16:
	.ascii	"read() worked on nonexistent file\000"
	.align	2
$LC17:
	.ascii	"write() worked on nonexistent file\000"
	.align	2
$LC18:
	.ascii	"reading file alphanum erred\000"
	.align	2
$LC19:
	.ascii	"writing file alphanum2 erred\000"
	.align	2
$LC20:
	.ascii	"files aren't identical\000"
	.align	2
$LC21:
	.ascii	"read() worked on closed file\000"
	.align	2
$LC22:
	.ascii	"write() worked on closed file\000"
	.align	2
$LC23:
	.ascii	"opening file alphanum2 a second time failed\000"
	.align	2
$LC24:
	.ascii	"unlinking file alphanum2 didn't work\000"
	.align	2
$LC25:
	.ascii	"open() worked on unlinked file\000"
	.align	2
$LC26:
	.ascii	"creating file alphanum2 after unlink() failed\000"
	.align	2
$LC27:
	.ascii	"reading 2nd version of alphanum didn't work\000"
	.align	2
$LC28:
	.ascii	"reading alphanum with valid FD didn't work\000"
	.align	2
$LC29:
	.ascii	"reading worked on invalid FD\000"
	.align	2
$LC30:
	.ascii	"final2.test\000"
	.align	2
$LC31:
	.ascii	"Test %s passed\n\000"
	.align	2
$LC32:
	.ascii	"exit magically returned\000"
	.text
	.align	2
	.globl	main
	.ent	main
main:
	.frame	$sp,304,$31		# vars= 248, regs= 10/0, args= 16, extra= 0
	.mask	0xc0ff0000,-4
	.fmask	0x00000000,0
	subu	$sp,$sp,304
	sw	$31,300($sp)
	sw	$fp,296($sp)
	sw	$23,292($sp)
	sw	$22,288($sp)
	sw	$21,284($sp)
	sw	$20,280($sp)
	sw	$19,276($sp)
	sw	$18,272($sp)
	sw	$17,268($sp)
	.set	noreorder
	.set	nomacro
	jal	__main
	sw	$16,264($sp)
	.set	macro
	.set	reorder

	la	$4,$LC3
	la	$6,$LC1
	lwl	$2,3($6)
	lwr	$2,0($6)
	lwl	$3,7($6)
	lwr	$3,4($6)
	lwl	$5,11($6)
	lwr	$5,8($6)
	swl	$2,99($sp)
	swr	$2,96($sp)
	swl	$3,103($sp)
	swr	$3,100($sp)
	swl	$5,107($sp)
	swr	$5,104($sp)
	lb	$2,12($6)
	lb	$3,13($6)
	sb	$2,108($sp)
	sb	$3,109($sp)
	.set	noreorder
	.set	nomacro
	jal	open
	addu	$17,$sp,96
	.set	macro
	.set	reorder

	la	$4,$LC4
	.set	noreorder
	.set	nomacro
	jal	open
	move	$18,$2
	.set	macro
	.set	reorder

	la	$19,$LC5
	move	$4,$19
	.set	noreorder
	.set	nomacro
	jal	creat
	move	$22,$2
	.set	macro
	.set	reorder

	nor	$4,$0,$18
	sltu	$4,$0,$4
	la	$21,$LC0
	move	$5,$21
	la	$6,$LC6
	.set	noreorder
	.set	nomacro
	jal	error_if
	move	$23,$2
	.set	macro
	.set	reorder

	xori	$4,$22,0x2
	sltu	$4,$0,$4
	la	$6,$LC7
	.set	noreorder
	.set	nomacro
	jal	error_if
	move	$5,$21
	.set	macro
	.set	reorder

	xori	$4,$23,0x3
	sltu	$4,$0,$4
	la	$6,$LC8
	.set	noreorder
	.set	nomacro
	jal	error_if
	move	$5,$21
	.set	macro
	.set	reorder

	move	$4,$0
	addu	$16,$sp,112
	move	$5,$16
	li	$6,20			# 0x14
	.set	noreorder
	.set	nomacro
	jal	read
	sb	$0,132($sp)
	.set	macro
	.set	reorder

	move	$20,$2
	.set	noreorder
	.set	nomacro
	jal	strlen
	move	$4,$17
	.set	macro
	.set	reorder

	li	$4,1			# 0x1
	move	$5,$17
	.set	noreorder
	.set	nomacro
	jal	write
	move	$6,$2
	.set	macro
	.set	reorder

	xori	$2,$2,0xd
	sltu	$4,$0,$2
	la	$6,$LC9
	.set	noreorder
	.set	nomacro
	jal	error_if
	move	$5,$21
	.set	macro
	.set	reorder

	xori	$4,$20,0x14
	sltu	$4,$0,$4
	la	$6,$LC10
	.set	noreorder
	.set	nomacro
	jal	error_if
	move	$5,$21
	.set	macro
	.set	reorder

	li	$4,1			# 0x1
	move	$5,$16
	.set	noreorder
	.set	nomacro
	jal	read
	li	$6,20			# 0x14
	.set	macro
	.set	reorder

	move	$20,$2
	.set	noreorder
	.set	nomacro
	jal	strlen
	move	$4,$17
	.set	macro
	.set	reorder

	move	$4,$0
	move	$5,$17
	.set	noreorder
	.set	nomacro
	jal	write
	move	$6,$2
	.set	macro
	.set	reorder

	move	$17,$2
	sltu	$4,$0,$20
	la	$6,$LC11
	.set	noreorder
	.set	nomacro
	jal	error_if
	move	$5,$21
	.set	macro
	.set	reorder

	sltu	$4,$0,$17
	la	$6,$LC12
	.set	noreorder
	.set	nomacro
	jal	error_if
	move	$5,$21
	.set	macro
	.set	reorder

	.set	noreorder
	.set	nomacro
	jal	strlen
	move	$4,$16
	.set	macro
	.set	reorder

	xori	$2,$2,0x14
	sltu	$4,$0,$2
	la	$6,$LC13
	.set	noreorder
	.set	nomacro
	jal	error_if
	move	$5,$21
	.set	macro
	.set	reorder

	move	$4,$22
	addu	$16,$sp,136
	move	$5,$16
	.set	noreorder
	.set	nomacro
	jal	read
	li	$6,-1			# 0xffffffff
	.set	macro
	.set	reorder

	move	$20,$2
	move	$4,$22
	move	$5,$16
	.set	noreorder
	.set	nomacro
	jal	write
	li	$6,-1			# 0xffffffff
	.set	macro
	.set	reorder

	move	$17,$2
	nor	$4,$0,$20
	sltu	$4,$0,$4
	la	$6,$LC14
	.set	noreorder
	.set	nomacro
	jal	error_if
	move	$5,$21
	.set	macro
	.set	reorder

	nor	$4,$0,$17
	sltu	$4,$0,$4
	la	$6,$LC15
	.set	noreorder
	.set	nomacro
	jal	error_if
	move	$5,$21
	.set	macro
	.set	reorder

	move	$4,$18
	move	$5,$16
	.set	noreorder
	.set	nomacro
	jal	read
	li	$6,20			# 0x14
	.set	macro
	.set	reorder

	move	$20,$2
	move	$4,$18
	move	$5,$16
	.set	noreorder
	.set	nomacro
	jal	write
	li	$6,20			# 0x14
	.set	macro
	.set	reorder

	move	$17,$2
	nor	$4,$0,$20
	sltu	$4,$0,$4
	la	$6,$LC16
	.set	noreorder
	.set	nomacro
	jal	error_if
	move	$5,$21
	.set	macro
	.set	reorder

	nor	$4,$0,$17
	sltu	$4,$0,$4
	la	$6,$LC17
	.set	noreorder
	.set	nomacro
	jal	error_if
	move	$5,$21
	.set	macro
	.set	reorder

	la	$fp,$LC19
	addu	$17,$sp,160
	move	$18,$16
$L5:
	move	$4,$22
	move	$5,$16
	.set	noreorder
	.set	nomacro
	jal	read
	li	$6,20			# 0x14
	.set	macro
	.set	reorder

	move	$20,$2
	srl	$4,$20,31
	la	$6,$LC18
	.set	noreorder
	.set	nomacro
	jal	error_if
	move	$5,$21
	.set	macro
	.set	reorder

	move	$4,$23
	move	$5,$16
	.set	noreorder
	.set	nomacro
	jal	write
	move	$6,$20
	.set	macro
	.set	reorder

	srl	$4,$2,31
	move	$5,$21
	.set	noreorder
	.set	nomacro
	jal	error_if
	move	$6,$fp
	.set	macro
	.set	reorder

	bgtz	$20,$L5
	.set	noreorder
	.set	nomacro
	jal	open
	move	$4,$19
	.set	macro
	.set	reorder

	move	$4,$2
	move	$5,$17
	.set	noreorder
	.set	nomacro
	jal	read
	li	$6,100			# 0x64
	.set	macro
	.set	reorder

	la	$5,$LC2
	.set	noreorder
	.set	nomacro
	jal	strcmp
	move	$4,$17
	.set	macro
	.set	reorder

	sltu	$4,$0,$2
	la	$6,$LC20
	.set	noreorder
	.set	nomacro
	jal	error_if
	move	$5,$21
	.set	macro
	.set	reorder

	.set	noreorder
	.set	nomacro
	jal	close
	move	$4,$22
	.set	macro
	.set	reorder

	move	$4,$22
	move	$5,$18
	.set	noreorder
	.set	nomacro
	jal	read
	li	$6,20			# 0x14
	.set	macro
	.set	reorder

	move	$20,$2
	move	$4,$22
	move	$5,$18
	.set	noreorder
	.set	nomacro
	jal	write
	li	$6,20			# 0x14
	.set	macro
	.set	reorder

	move	$17,$2
	nor	$4,$0,$20
	sltu	$4,$0,$4
	la	$6,$LC21
	.set	noreorder
	.set	nomacro
	jal	error_if
	move	$5,$21
	.set	macro
	.set	reorder

	nor	$4,$0,$17
	sltu	$4,$0,$4
	la	$6,$LC22
	.set	noreorder
	.set	nomacro
	jal	error_if
	move	$5,$21
	.set	macro
	.set	reorder

	.set	noreorder
	.set	nomacro
	jal	open
	move	$4,$19
	.set	macro
	.set	reorder

	move	$17,$2
	xori	$4,$17,0x5
	sltu	$4,$0,$4
	la	$6,$LC23
	.set	noreorder
	.set	nomacro
	jal	error_if
	move	$5,$21
	.set	macro
	.set	reorder

	.set	noreorder
	.set	nomacro
	jal	unlink
	move	$4,$19
	.set	macro
	.set	reorder

	sltu	$4,$0,$2
	la	$6,$LC24
	.set	noreorder
	.set	nomacro
	jal	error_if
	move	$5,$21
	.set	macro
	.set	reorder

	.set	noreorder
	.set	nomacro
	jal	open
	move	$4,$19
	.set	macro
	.set	reorder

	nor	$2,$0,$2
	sltu	$4,$0,$2
	la	$6,$LC25
	.set	noreorder
	.set	nomacro
	jal	error_if
	move	$5,$21
	.set	macro
	.set	reorder

	.set	noreorder
	.set	nomacro
	jal	creat
	move	$4,$19
	.set	macro
	.set	reorder

	move	$16,$2
	xori	$4,$16,0x6
	sltu	$4,$0,$4
	la	$6,$LC26
	.set	noreorder
	.set	nomacro
	jal	error_if
	move	$5,$21
	.set	macro
	.set	reorder

	move	$4,$16
	move	$5,$18
	.set	noreorder
	.set	nomacro
	jal	read
	li	$6,20			# 0x14
	.set	macro
	.set	reorder

	srl	$4,$2,31
	la	$6,$LC27
	.set	noreorder
	.set	nomacro
	jal	error_if
	move	$5,$21
	.set	macro
	.set	reorder

	move	$4,$17
	move	$5,$18
	.set	noreorder
	.set	nomacro
	jal	read
	li	$6,20			# 0x14
	.set	macro
	.set	reorder

	srl	$4,$2,31
	la	$6,$LC28
	.set	noreorder
	.set	nomacro
	jal	error_if
	move	$5,$21
	.set	macro
	.set	reorder

	.set	noreorder
	.set	nomacro
	jal	close
	move	$4,$16
	.set	macro
	.set	reorder

	move	$4,$16
	move	$5,$18
	.set	noreorder
	.set	nomacro
	jal	read
	li	$6,20			# 0x14
	.set	macro
	.set	reorder

	nor	$2,$0,$2
	sltu	$4,$0,$2
	la	$6,$LC29
	.set	noreorder
	.set	nomacro
	jal	error_if
	move	$5,$21
	.set	macro
	.set	reorder

	.set	noreorder
	.set	nomacro
	jal	unlink
	move	$4,$19
	.set	macro
	.set	reorder

	la	$4,$LC30
	jal	creat
	la	$4,$LC31
	.set	noreorder
	.set	nomacro
	jal	printf
	move	$5,$21
	.set	macro
	.set	reorder

	.set	noreorder
	.set	nomacro
	jal	exit
	move	$4,$0
	.set	macro
	.set	reorder

	la	$4,$LC32
	jal	printf
	lw	$31,300($sp)
	lw	$fp,296($sp)
	lw	$23,292($sp)
	lw	$22,288($sp)
	lw	$21,284($sp)
	lw	$20,280($sp)
	lw	$19,276($sp)
	lw	$18,272($sp)
	lw	$17,268($sp)
	lw	$16,264($sp)
	move	$2,$0
	.set	noreorder
	.set	nomacro
	j	$31
	addu	$sp,$sp,304
	.set	macro
	.set	reorder

	.end	main
	.rdata
	.align	2
$LC33:
	.ascii	"Test %s failed: %s\n\000"
	.align	2
$LC34:
	.ascii	"you aint de root boi\000"
	.text
	.align	2
	.globl	error_if
	.ent	error_if
error_if:
	.frame	$sp,24,$31		# vars= 0, regs= 1/0, args= 16, extra= 0
	.mask	0x80000000,-8
	.fmask	0x00000000,0
	subu	$sp,$sp,24
	.set	noreorder
	.set	nomacro
	beq	$4,$0,$L8
	sw	$31,16($sp)
	.set	macro
	.set	reorder

	la	$4,$LC33
	jal	printf
	jal	halt
	la	$4,$LC34
	jal	printf
$L8:
	lw	$31,16($sp)
	#nop
	.set	noreorder
	.set	nomacro
	j	$31
	addu	$sp,$sp,24
	.set	macro
	.set	reorder

	.end	error_if
