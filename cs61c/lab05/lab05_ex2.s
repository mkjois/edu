main:
	move $t0, $s0
	move $t1, $s1
	xor $t2, $t0, $t1
	xor $t3, $t1, $t2
	xor $t4, $t2, $t3
	xor $t5, $t3, $t4
	xor $t6, $t4, $t5
	xor $t7, $t5, $t6
	li $v0, 10
	syscall