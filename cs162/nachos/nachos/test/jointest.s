	.file	1 "jointest.c"
gcc2_compiled.:
__gnu_compiled_c:
	.rdata
	.align	2
$LC0:
	.ascii	"Initiating join test... \000"
	.align	2
$LC1:
	.ascii	"jointest.coff\000"
	.align	2
$LC2:
	.ascii	"failed due to execution error.\n\000"
	.align	2
$LC3:
	.ascii	"passed.\n\000"
	.align	2
$LC4:
	.ascii	"failed with value %d.\n\000"
	.text
	.align	2
	.globl	main
	.ent	main
main:
	.frame	$sp,48,$31		# vars= 16, regs= 3/0, args= 16, extra= 0
	.mask	0x80030000,-8
	.fmask	0x00000000,0
	subu	$sp,$sp,48
	sw	$17,36($sp)
	move	$17,$4
	sw	$16,32($sp)
	sw	$31,40($sp)
	.set	noreorder
	.set	nomacro
	jal	__main
	move	$16,$5
	.set	macro
	.set	reorder

	.set	noreorder
	.set	nomacro
	bne	$17,$0,$L3
	sb	$0,17($sp)
	.set	macro
	.set	reorder

	la	$4,$LC0
	jal	printf
	li	$2,97			# 0x61
	sb	$2,16($sp)
	.set	noreorder
	.set	nomacro
	j	$L4
	sb	$0,17($sp)
	.set	macro
	.set	reorder

$L3:
	lw	$3,0($16)
	#nop
	lbu	$2,0($3)
	sb	$0,17($sp)
	addu	$2,$2,1
	sb	$2,16($sp)
$L4:
	lb	$3,16($sp)
	li	$2,100			# 0x64
	.set	noreorder
	.set	nomacro
	bne	$3,$2,$L11
	addu	$2,$sp,16
	.set	macro
	.set	reorder

	.set	noreorder
	.set	nomacro
	jal	exit
	li	$4,1			# 0x1
	.set	macro
	.set	reorder

	addu	$2,$sp,16
$L11:
	sw	$2,24($sp)
	la	$4,$LC1
	li	$5,1			# 0x1
	.set	noreorder
	.set	nomacro
	jal	exec
	addu	$6,$sp,24
	.set	macro
	.set	reorder

	move	$16,$2
	li	$2,-1			# 0xffffffff
	bne	$16,$2,$L6
	la	$4,$LC2
	jal	printf
	.set	noreorder
	.set	nomacro
	jal	exit
	move	$4,$0
	.set	macro
	.set	reorder

$L6:
	move	$4,$16
	.set	noreorder
	.set	nomacro
	jal	join
	addu	$5,$sp,28
	.set	macro
	.set	reorder

	.set	noreorder
	.set	nomacro
	bne	$17,$0,$L7
	li	$2,3			# 0x3
	.set	macro
	.set	reorder

	lw	$5,28($sp)
	#nop
	bne	$5,$2,$L8
	la	$4,$LC3
	jal	printf
	j	$L9
$L8:
	la	$4,$LC4
	jal	printf
$L9:
	.set	noreorder
	.set	nomacro
	jal	exit
	li	$4,1			# 0x1
	.set	macro
	.set	reorder

	j	$L10
$L7:
	lw	$4,28($sp)
	.set	noreorder
	.set	nomacro
	jal	exit
	addu	$4,$4,1
	.set	macro
	.set	reorder

$L10:
	lw	$31,40($sp)
	lw	$17,36($sp)
	lw	$16,32($sp)
	#nop
	.set	noreorder
	.set	nomacro
	j	$31
	addu	$sp,$sp,48
	.set	macro
	.set	reorder

	.end	main
