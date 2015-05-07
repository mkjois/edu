	.file	1 "p1t1.c"
gcc2_compiled.:
__gnu_compiled_c:
	.rdata
	.align	2
$LC0:
	.ascii	"p1t1.c\000"
	.align	2
$LC1:
	.ascii	"nosuchfile\000"
	.align	2
$LC2:
	.ascii	"foo.test\000"
	.align	2
$LC3:
	.ascii	"open('nosuchfile') didn't return -1\000"
	.align	2
$LC4:
	.ascii	"couldn't create/open file foo\000"
	.align	2
$LC5:
	.ascii	"couldn't read from stdin\000"
	.align	2
$LC6:
	.ascii	"couldn't write to file foo\000"
	.align	2
$LC7:
	.ascii	"Test %s passed: %d bytes requested, %d bytes read, %d by"
	.ascii	"tes written\n\000"
	.align	2
$LC8:
	.ascii	"exit magically returned\000"
	.text
	.align	2
	.globl	main
	.ent	main
main:
	.frame	$fp,48,$31		# vars= 0, regs= 6/0, args= 24, extra= 0
	.mask	0xc00f0000,-4
	.fmask	0x00000000,0
	subu	$sp,$sp,48
	sw	$fp,40($sp)
	move	$fp,$sp
	sw	$31,44($sp)
	sw	$19,36($sp)
	sw	$18,32($sp)
	sw	$17,28($sp)
	.set	noreorder
	.set	nomacro
	jal	__main
	sw	$16,24($sp)
	.set	macro
	.set	reorder

	addu	$sp,$sp,-16
	la	$4,$LC1
	.set	noreorder
	.set	nomacro
	jal	open
	addu	$18,$sp,24
	.set	macro
	.set	reorder

	la	$4,$LC2
	.set	noreorder
	.set	nomacro
	jal	creat
	move	$16,$2
	.set	macro
	.set	reorder

	nor	$16,$0,$16
	sltu	$4,$0,$16
	la	$19,$LC0
	move	$5,$19
	la	$6,$LC3
	.set	noreorder
	.set	nomacro
	jal	error_if
	move	$16,$2
	.set	macro
	.set	reorder

	srl	$4,$16,31
	la	$6,$LC4
	.set	noreorder
	.set	nomacro
	jal	error_if
	move	$5,$19
	.set	macro
	.set	reorder

	move	$4,$0
	move	$5,$18
	.set	noreorder
	.set	nomacro
	jal	read
	li	$6,10			# 0xa
	.set	macro
	.set	reorder

	move	$17,$2
	srl	$4,$17,31
	la	$6,$LC5
	.set	noreorder
	.set	nomacro
	jal	error_if
	move	$5,$19
	.set	macro
	.set	reorder

	move	$4,$16
	move	$5,$18
	addu	$6,$17,1
	li	$2,10			# 0xa
	addu	$18,$18,$17
	.set	noreorder
	.set	nomacro
	jal	write
	sb	$2,0($18)
	.set	macro
	.set	reorder

	move	$16,$2
	srl	$4,$16,31
	la	$6,$LC6
	.set	noreorder
	.set	nomacro
	jal	error_if
	move	$5,$19
	.set	macro
	.set	reorder

	la	$4,$LC7
	move	$5,$19
	li	$6,10			# 0xa
	addu	$16,$16,-1
	move	$7,$17
	.set	noreorder
	.set	nomacro
	jal	printf
	sw	$16,16($sp)
	.set	macro
	.set	reorder

	.set	noreorder
	.set	nomacro
	jal	exit
	move	$4,$0
	.set	macro
	.set	reorder

	la	$4,$LC8
	jal	printf
	move	$2,$0
	move	$sp,$fp
	lw	$31,44($sp)
	lw	$fp,40($sp)
	lw	$19,36($sp)
	lw	$18,32($sp)
	lw	$17,28($sp)
	lw	$16,24($sp)
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
$LC9:
	.ascii	"Test %s failed: %s\n\000"
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

	la	$4,$LC9
	jal	printf
	la	$4,$LC0
	.set	noreorder
	.set	nomacro
	jal	__assert
	li	$5,37			# 0x25
	.set	macro
	.set	reorder

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
