	.file	1 "joinfailtest.c"
gcc2_compiled.:
__gnu_compiled_c:
	.rdata
	.align	2
$LC0:
	.ascii	"Initiating join failure test... \000"
	.align	2
$LC1:
	.ascii	"joinfailtest.coff\000"
	.align	2
$LC2:
	.ascii	"passed.\n\000"
	.align	2
$LC3:
	.ascii	"failed.\n\000"
	.text
	.align	2
	.globl	main
	.ent	main
main:
	.frame	$sp,64,$31		# vars= 32, regs= 3/0, args= 16, extra= 0
	.mask	0x80030000,-8
	.fmask	0x00000000,0
	subu	$sp,$sp,64
	sw	$16,48($sp)
	move	$16,$4
	sw	$17,52($sp)
	sw	$31,56($sp)
	.set	noreorder
	.set	nomacro
	jal	__main
	move	$17,$5
	.set	macro
	.set	reorder

	.set	noreorder
	.set	nomacro
	bne	$16,$0,$L3
	li	$2,1			# 0x1
	.set	macro
	.set	reorder

	la	$4,$LC0
	jal	printf
	la	$16,$LC1
	move	$4,$16
	li	$5,1			# 0x1
	addu	$6,$sp,32
	addu	$2,$sp,16
	addu	$3,$sp,24
	sb	$0,17($sp)
	sb	$0,25($sp)
	sb	$0,16($sp)
	sw	$2,32($sp)
	.set	noreorder
	.set	nomacro
	jal	exec
	sw	$3,36($sp)
	.set	macro
	.set	reorder

	move	$4,$16
	li	$5,1			# 0x1
	addu	$6,$sp,36
	move	$16,$2
	.set	noreorder
	.set	nomacro
	jal	exec
	sb	$16,24($sp)
	.set	macro
	.set	reorder

	move	$4,$16
	addu	$17,$sp,40
	move	$5,$17
	.set	noreorder
	.set	nomacro
	jal	join
	move	$16,$2
	.set	macro
	.set	reorder

	move	$4,$16
	.set	noreorder
	.set	nomacro
	jal	join
	move	$5,$17
	.set	macro
	.set	reorder

	lw	$3,40($sp)
	li	$2,-1			# 0xffffffff
	bne	$3,$2,$L4
	la	$4,$LC2
	jal	printf
	j	$L6
$L4:
	la	$4,$LC3
	jal	printf
	j	$L6
$L3:
	bne	$16,$2,$L6
	lw	$2,0($17)
	#nop
	lb	$4,0($2)
	#nop
	bne	$4,$0,$L8
	.set	noreorder
	.set	nomacro
	jal	exit
	li	$4,1			# 0x1
	.set	macro
	.set	reorder

	j	$L6
$L8:
	.set	noreorder
	.set	nomacro
	jal	join
	addu	$5,$sp,44
	.set	macro
	.set	reorder

	.set	noreorder
	.set	nomacro
	jal	exit
	move	$4,$2
	.set	macro
	.set	reorder

$L6:
	lw	$31,56($sp)
	lw	$17,52($sp)
	lw	$16,48($sp)
	#nop
	.set	noreorder
	.set	nomacro
	j	$31
	addu	$sp,$sp,64
	.set	macro
	.set	reorder

	.end	main
