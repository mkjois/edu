	.file	1 "p1t3.c"
gcc2_compiled.:
__gnu_compiled_c:
	.rdata
	.align	2
$LC0:
	.ascii	"p1t3.c\000"
	.align	2
$LC1:
	.ascii	"alphanum.txt\000"
	.align	2
$LC2:
	.ascii	"temp.test\000"
	.align	2
$LC3:
	.ascii	"temp2.test\000"
	.align	2
$LC4:
	.ascii	"couldn't open file alphanum\000"
	.align	2
$LC5:
	.ascii	"couldn't create/open file temp\000"
	.align	2
$LC6:
	.ascii	"couldn't create/open file temp2\000"
	.align	2
$LC7:
	.ascii	"couldn't read from file alphanum\000"
	.align	2
$LC8:
	.ascii	"couldn't write to file temp\000"
	.align	2
$LC9:
	.ascii	"error closing file temp\000"
	.align	2
$LC10:
	.ascii	"error unlinking file temp2\000"
	.align	2
$LC11:
	.ascii	"error closing file temp2 after unlinking\000"
	.align	2
$LC12:
	.ascii	"Test %s passed\n\000"
	.align	2
$LC13:
	.ascii	"exit magically returned\000"
	.text
	.align	2
	.globl	main
	.ent	main
main:
	.frame	$fp,48,$31		# vars= 0, regs= 8/0, args= 16, extra= 0
	.mask	0xc03f0000,-4
	.fmask	0x00000000,0
	subu	$sp,$sp,48
	sw	$fp,40($sp)
	move	$fp,$sp
	sw	$31,44($sp)
	sw	$21,36($sp)
	sw	$20,32($sp)
	sw	$19,28($sp)
	sw	$18,24($sp)
	sw	$17,20($sp)
	.set	noreorder
	.set	nomacro
	jal	__main
	sw	$16,16($sp)
	.set	macro
	.set	reorder

	addu	$sp,$sp,-32
	la	$4,$LC1
	.set	noreorder
	.set	nomacro
	jal	open
	addu	$18,$sp,16
	.set	macro
	.set	reorder

	la	$4,$LC2
	.set	noreorder
	.set	nomacro
	jal	creat
	move	$16,$2
	.set	macro
	.set	reorder

	la	$21,$LC3
	move	$4,$21
	.set	noreorder
	.set	nomacro
	jal	creat
	move	$19,$2
	.set	macro
	.set	reorder

	srl	$4,$16,31
	la	$17,$LC0
	move	$5,$17
	la	$6,$LC4
	.set	noreorder
	.set	nomacro
	jal	error_if
	move	$20,$2
	.set	macro
	.set	reorder

	srl	$4,$19,31
	la	$6,$LC5
	.set	noreorder
	.set	nomacro
	jal	error_if
	move	$5,$17
	.set	macro
	.set	reorder

	srl	$4,$20,31
	la	$6,$LC6
	.set	noreorder
	.set	nomacro
	jal	error_if
	move	$5,$17
	.set	macro
	.set	reorder

	move	$4,$16
	move	$5,$18
	.set	noreorder
	.set	nomacro
	jal	read
	li	$6,26			# 0x1a
	.set	macro
	.set	reorder

	move	$16,$2
	srl	$4,$16,31
	la	$6,$LC7
	.set	noreorder
	.set	nomacro
	jal	error_if
	move	$5,$17
	.set	macro
	.set	reorder

	addu	$3,$18,$16
	li	$2,10			# 0xa
	move	$4,$19
	move	$5,$18
	addu	$6,$16,1
	.set	noreorder
	.set	nomacro
	jal	write
	sb	$2,0($3)
	.set	macro
	.set	reorder

	srl	$4,$2,31
	la	$6,$LC8
	.set	noreorder
	.set	nomacro
	jal	error_if
	move	$5,$17
	.set	macro
	.set	reorder

	.set	noreorder
	.set	nomacro
	jal	close
	move	$4,$19
	.set	macro
	.set	reorder

	move	$4,$21
	.set	noreorder
	.set	nomacro
	jal	unlink
	move	$16,$2
	.set	macro
	.set	reorder

	move	$4,$20
	.set	noreorder
	.set	nomacro
	jal	close
	move	$18,$2
	.set	macro
	.set	reorder

	srl	$4,$16,31
	move	$5,$17
	la	$6,$LC9
	.set	noreorder
	.set	nomacro
	jal	error_if
	move	$16,$2
	.set	macro
	.set	reorder

	srl	$4,$18,31
	la	$6,$LC10
	.set	noreorder
	.set	nomacro
	jal	error_if
	move	$5,$17
	.set	macro
	.set	reorder

	srl	$4,$16,31
	la	$6,$LC11
	.set	noreorder
	.set	nomacro
	jal	error_if
	move	$5,$17
	.set	macro
	.set	reorder

	la	$4,$LC12
	.set	noreorder
	.set	nomacro
	jal	printf
	move	$5,$17
	.set	macro
	.set	reorder

	.set	noreorder
	.set	nomacro
	jal	exit
	move	$4,$0
	.set	macro
	.set	reorder

	la	$4,$LC13
	jal	printf
	move	$2,$0
	move	$sp,$fp
	lw	$31,44($sp)
	lw	$fp,40($sp)
	lw	$21,36($sp)
	lw	$20,32($sp)
	lw	$19,28($sp)
	lw	$18,24($sp)
	lw	$17,20($sp)
	lw	$16,16($sp)
	#nop
	.set	noreorder
	.set	nomacro
	j	$31
	addu	$sp,$sp,48
	.set	macro
	.set	reorder

	.end	main
	.rdata
	.align	2
$LC14:
	.ascii	"Test %s failed: %s\n\000"
	.align	2
$LC15:
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
	beq	$4,$0,$L4
	sw	$31,16($sp)
	.set	macro
	.set	reorder

	la	$4,$LC14
	jal	printf
	jal	halt
	la	$4,$LC15
	jal	printf
$L4:
	lw	$31,16($sp)
	#nop
	.set	noreorder
	.set	nomacro
	j	$31
	addu	$sp,$sp,24
	.set	macro
	.set	reorder

	.end	error_if
