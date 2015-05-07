        .data
n:      .word 6
        .text
main: 	addu    $t0, $0, $zero
	addiu   $t1, $zero, 1
	la      $t3, n
	lw      $t3, 0($t3)
	
fun: 	beq     $t3, $0, finish
	add     $t2, $t1, $t0
	move    $t0, $t1
	move    $t1, $t2
	subi    $t3, $t3, 1
	j       fun
	
finish: addiu   $a0, $t0, 0
	li      $v0, 1
	syscall			
	li      $v0, 10		
	syscall
