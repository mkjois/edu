	.file	1 "printer.c"
gcc2_compiled.:
__gnu_compiled_c:
	.rdata
	.align	2
$LC0:
	.ascii	"Hello world!\n\000"
	.align	2
$LC1:
	.ascii	"Does this execute?\n\000"
	.text
	.align	2
	.globl	main
	.ent	main
main:
	.frame	$sp,24,$31		# vars= 0, regs= 1/0, args= 16, extra= 0
	.mask	0x80000000,-8
	.fmask	0x00000000,0
	subu	$sp,$sp,24
	sw	$31,16($sp)
	jal	__main
	la	$4,$LC0
	jal	printf
	.set	noreorder
	.set	nomacro
	jal	exit
	li	$4,1			# 0x1
	.set	macro
	.set	reorder

	la	$4,$LC1
	jal	printf
	lw	$31,16($sp)
	#nop
	.set	noreorder
	.set	nomacro
	j	$31
	addu	$sp,$sp,24
	.set	macro
	.set	reorder

	.end	main
