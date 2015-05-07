	.file	1 "bullets.c"
gcc2_compiled.:
__gnu_compiled_c:
	.rdata
	.align	2
$LC0:
	.ascii	"bullets.c\000"
	.align	2
$LC1:
	.ascii	"a.test\000"
	.align	2
$LC2:
	.ascii	"b.test\000"
	.align	2
$LC3:
	.ascii	"c.test\000"
	.align	2
$LC4:
	.ascii	"d.test\000"
	.align	2
$LC5:
	.ascii	"e.test\000"
	.align	2
$LC6:
	.ascii	"f.test\000"
	.align	2
$LC7:
	.ascii	"g.test\000"
	.align	2
$LC8:
	.ascii	"h.test\000"
	.align	2
$LC9:
	.ascii	"i.test\000"
	.align	2
$LC10:
	.ascii	"j.test\000"
	.align	2
$LC11:
	.ascii	"k.test\000"
	.align	2
$LC12:
	.ascii	"l.test\000"
	.align	2
$LC13:
	.ascii	"m.test\000"
	.align	2
$LC14:
	.ascii	"n.test\000"
	.align	2
$LC15:
	.ascii	"o.test\000"
	.align	2
$LC16:
	.ascii	"p.test\000"
	.align	2
$LC17:
	.ascii	"q.test\000"
	.align	2
$LC18:
	.ascii	"r.test\000"
	.align	2
$LC19:
	.ascii	"s.test\000"
	.align	2
$LC20:
	.ascii	"t.test\000"
	.align	2
$LC21:
	.ascii	"u.test\000"
	.align	2
$LC22:
	.ascii	"v.test\000"
	.align	2
$LC23:
	.ascii	"w.test\000"
	.align	2
$LC24:
	.ascii	"x.test\000"
	.align	2
$LC25:
	.ascii	"y.test\000"
	.align	2
$LC26:
	.ascii	"z.test\000"
	.align	2
$LC27:
	.word	$LC1
	.word	$LC2
	.word	$LC3
	.word	$LC4
	.word	$LC5
	.word	$LC6
	.word	$LC7
	.word	$LC8
	.word	$LC9
	.word	$LC10
	.word	$LC11
	.word	$LC12
	.word	$LC13
	.word	$LC14
	.word	$LC15
	.word	$LC16
	.word	$LC17
	.word	$LC18
	.word	$LC19
	.word	$LC20
	.word	$LC21
	.word	$LC22
	.word	$LC23
	.word	$LC24
	.word	$LC25
	.word	$LC26
	.align	2
$LC28:
	.ascii	"zzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzz.test\000"
	.align	2
$LC29:
	.ascii	"zzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzz"
	.ascii	"zzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzz"
	.ascii	"zzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzz"
	.ascii	"zzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzz"
	.ascii	"zzzzzzzzzzzzzzzzzzzzzzzzzzzz.test\000"
	.align	2
$LC30:
	.ascii	"good creat() failed\000"
	.align	2
$LC31:
	.ascii	"creat() worked on a file name of 257 chars\000"
	.align	2
$LC32:
	.ascii	"creat() worked on a negative pointer\000"
	.align	2
$LC33:
	.ascii	"creat() incorrectly failed\000"
	.align	2
$LC34:
	.ascii	"creat() incorrectly succeeded\000"
	.align	2
$LC35:
	.ascii	"close() didn't error on negative FD\000"
	.align	2
$LC36:
	.ascii	"close() didn't work on valid FD\000"
	.align	2
$LC37:
	.ascii	"unlink() marked a non-existent file\000"
	.align	2
$LC38:
	.ascii	"unlink() didn't mark an existing file\000"
	.align	2
$LC39:
	.ascii	"close() didn't error on invalid FD\000"
	.align	2
$LC40:
	.ascii	"close() erred on valid FD after unlink()\000"
	.align	2
$LC41:
	.ascii	"good unlink() failed\000"
	.align	2
$LC42:
	.ascii	"unlink() worked on a file name of 257 chars\000"
	.align	2
$LC43:
	.ascii	"unlink() worked on a negative pointer\000"
	.align	2
$LC44:
	.ascii	"final.test\000"
	.align	2
$LC45:
	.ascii	"Test %s passed\n\000"
	.align	2
$LC46:
	.ascii	"exit magically returned\000"
	.text
	.align	2
	.globl	main
	.ent	main
main:
	.frame	$sp,240,$31		# vars= 184, regs= 10/0, args= 16, extra= 0
	.mask	0xc0ff0000,-4
	.fmask	0x00000000,0
	subu	$sp,$sp,240
	sw	$31,236($sp)
	sw	$fp,232($sp)
	sw	$23,228($sp)
	sw	$22,224($sp)
	sw	$21,220($sp)
	sw	$20,216($sp)
	sw	$19,212($sp)
	sw	$18,208($sp)
	sw	$17,204($sp)
	.set	noreorder
	.set	nomacro
	jal	__main
	sw	$16,200($sp)
	.set	macro
	.set	reorder

	la	$20,$LC0
	addu	$2,$sp,16
	la	$3,$LC27
	addu	$4,$3,96
	move	$22,$2
	la	$23,$LC28
	la	$fp,$LC29
	addu	$21,$sp,120
$L3:
	lw	$5,0($3)
	lw	$6,4($3)
	lw	$7,8($3)
	lw	$8,12($3)
	sw	$5,0($2)
	sw	$6,4($2)
	sw	$7,8($2)
	sw	$8,12($2)
	addu	$3,$3,16
	.set	noreorder
	.set	nomacro
	bne	$3,$4,$L3
	addu	$2,$2,16
	.set	macro
	.set	reorder

	move	$4,$23
	lw	$5,0($3)
	lw	$6,4($3)
	sw	$5,0($2)
	.set	noreorder
	.set	nomacro
	jal	creat
	sw	$6,4($2)
	.set	macro
	.set	reorder

	move	$4,$fp
	.set	noreorder
	.set	nomacro
	jal	creat
	move	$18,$2
	.set	macro
	.set	reorder

	li	$4,-1			# 0xffffffff
	.set	noreorder
	.set	nomacro
	jal	creat
	move	$17,$2
	.set	macro
	.set	reorder

	xori	$4,$18,0x2
	sltu	$4,$0,$4
	move	$5,$20
	la	$6,$LC30
	.set	noreorder
	.set	nomacro
	jal	error_if
	move	$16,$2
	.set	macro
	.set	reorder

	nor	$17,$0,$17
	sltu	$4,$0,$17
	la	$6,$LC31
	.set	noreorder
	.set	nomacro
	jal	error_if
	move	$5,$20
	.set	macro
	.set	reorder

	nor	$16,$0,$16
	sltu	$4,$0,$16
	la	$6,$LC32
	.set	noreorder
	.set	nomacro
	jal	error_if
	move	$5,$20
	.set	macro
	.set	reorder

	addu	$19,$21,12
	addu	$16,$22,12
	li	$17,16			# 0x10
	li	$3,1			# 0x1
	sll	$2,$18,2
	addu	$2,$21,$2
	sw	$0,120($sp)
	sw	$3,124($sp)
	sw	$18,0($2)
$L7:
	lw	$4,0($16)
	addu	$16,$16,4
	.set	noreorder
	.set	nomacro
	jal	creat
	addu	$17,$17,-1
	.set	macro
	.set	reorder

	sw	$2,0($19)
	.set	noreorder
	.set	nomacro
	bgez	$17,$L7
	addu	$19,$19,4
	.set	macro
	.set	reorder

	move	$17,$0
	move	$16,$21
	slt	$2,$17,16
$L41:
	.set	noreorder
	.set	nomacro
	beq	$2,$0,$L13
	move	$5,$20
	.set	macro
	.set	reorder

	lw	$4,0($16)
	la	$6,$LC33
	xor	$4,$4,$17
	.set	noreorder
	.set	nomacro
	jal	error_if
	sltu	$4,$0,$4
	.set	macro
	.set	reorder

	.set	noreorder
	.set	nomacro
	j	$L37
	addu	$16,$16,4
	.set	macro
	.set	reorder

$L13:
	lw	$4,0($16)
	la	$6,$LC34
	nor	$4,$0,$4
	.set	noreorder
	.set	nomacro
	jal	error_if
	sltu	$4,$0,$4
	.set	macro
	.set	reorder

	addu	$16,$16,4
$L37:
	addu	$17,$17,1
	slt	$2,$17,20
	.set	noreorder
	.set	nomacro
	bne	$2,$0,$L41
	slt	$2,$17,16
	.set	macro
	.set	reorder

	li	$17,-2			# 0xfffffffe
$L19:
	.set	noreorder
	.set	nomacro
	jal	close
	move	$4,$17
	.set	macro
	.set	reorder

	.set	noreorder
	.set	nomacro
	bgez	$17,$L20
	move	$4,$2
	.set	macro
	.set	reorder

	nor	$4,$0,$4
	sltu	$4,$0,$4
	la	$6,$LC35
	.set	noreorder
	.set	nomacro
	jal	error_if
	move	$5,$20
	.set	macro
	.set	reorder

	.set	noreorder
	.set	nomacro
	j	$L38
	addu	$17,$17,1
	.set	macro
	.set	reorder

$L20:
	sltu	$4,$0,$4
	la	$6,$LC36
	.set	noreorder
	.set	nomacro
	jal	error_if
	move	$5,$20
	.set	macro
	.set	reorder

	addu	$17,$17,1
$L38:
	slt	$2,$17,8
	bne	$2,$0,$L19
	move	$17,$0
	move	$16,$22
$L26:
	lw	$4,0($16)
	jal	unlink
	move	$4,$2
	addu	$2,$17,-3
	sltu	$2,$2,13
	bne	$2,$0,$L27
	nor	$4,$0,$4
	sltu	$4,$0,$4
	la	$6,$LC37
	.set	noreorder
	.set	nomacro
	jal	error_if
	move	$5,$20
	.set	macro
	.set	reorder

	.set	noreorder
	.set	nomacro
	j	$L39
	addu	$16,$16,4
	.set	macro
	.set	reorder

$L27:
	sltu	$4,$0,$4
	la	$6,$LC38
	.set	noreorder
	.set	nomacro
	jal	error_if
	move	$5,$20
	.set	macro
	.set	reorder

	addu	$16,$16,4
$L39:
	addu	$17,$17,1
	slt	$2,$17,26
	bne	$2,$0,$L26
	li	$17,4			# 0x4
$L33:
	.set	noreorder
	.set	nomacro
	jal	close
	move	$4,$17
	.set	macro
	.set	reorder

	move	$4,$2
	addu	$2,$17,-8
	sltu	$2,$2,8
	bne	$2,$0,$L34
	nor	$4,$0,$4
	sltu	$4,$0,$4
	la	$6,$LC39
	.set	noreorder
	.set	nomacro
	jal	error_if
	move	$5,$20
	.set	macro
	.set	reorder

	.set	noreorder
	.set	nomacro
	j	$L40
	addu	$17,$17,1
	.set	macro
	.set	reorder

$L34:
	sltu	$4,$0,$4
	la	$6,$LC40
	.set	noreorder
	.set	nomacro
	jal	error_if
	move	$5,$20
	.set	macro
	.set	reorder

	addu	$17,$17,1
$L40:
	slt	$2,$17,20
	bne	$2,$0,$L33
	.set	noreorder
	.set	nomacro
	jal	unlink
	move	$4,$23
	.set	macro
	.set	reorder

	move	$4,$fp
	.set	noreorder
	.set	nomacro
	jal	unlink
	move	$16,$2
	.set	macro
	.set	reorder

	li	$4,-1			# 0xffffffff
	.set	noreorder
	.set	nomacro
	jal	unlink
	move	$17,$2
	.set	macro
	.set	reorder

	sltu	$4,$0,$16
	move	$5,$20
	la	$6,$LC41
	.set	noreorder
	.set	nomacro
	jal	error_if
	move	$16,$2
	.set	macro
	.set	reorder

	nor	$17,$0,$17
	sltu	$4,$0,$17
	la	$6,$LC42
	.set	noreorder
	.set	nomacro
	jal	error_if
	move	$5,$20
	.set	macro
	.set	reorder

	nor	$16,$0,$16
	sltu	$4,$0,$16
	la	$6,$LC43
	.set	noreorder
	.set	nomacro
	jal	error_if
	move	$5,$20
	.set	macro
	.set	reorder

	la	$4,$LC44
	jal	creat
	la	$4,$LC45
	.set	noreorder
	.set	nomacro
	jal	printf
	move	$5,$20
	.set	macro
	.set	reorder

	.set	noreorder
	.set	nomacro
	jal	exit
	move	$4,$0
	.set	macro
	.set	reorder

	la	$4,$LC46
	jal	printf
	lw	$31,236($sp)
	lw	$fp,232($sp)
	lw	$23,228($sp)
	lw	$22,224($sp)
	lw	$21,220($sp)
	lw	$20,216($sp)
	lw	$19,212($sp)
	lw	$18,208($sp)
	lw	$17,204($sp)
	lw	$16,200($sp)
	move	$2,$0
	.set	noreorder
	.set	nomacro
	j	$31
	addu	$sp,$sp,240
	.set	macro
	.set	reorder

	.end	main
	.rdata
	.align	2
$LC47:
	.ascii	"Test %s failed: %s\n\000"
	.align	2
$LC48:
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
	beq	$4,$0,$L43
	sw	$31,16($sp)
	.set	macro
	.set	reorder

	la	$4,$LC47
	jal	printf
	jal	halt
	la	$4,$LC48
	jal	printf
$L43:
	lw	$31,16($sp)
	#nop
	.set	noreorder
	.set	nomacro
	j	$31
	addu	$sp,$sp,24
	.set	macro
	.set	reorder

	.end	error_if
