	.data
# do not modify
LIST:	
	.word -44 -4 0 1 2 4 7 9 23 126
line:   
	.asciiz " was found at index "
newline: 
	.asciiz "\n"
expect:
	.asciiz ", expected "
	
	.text 
	
	li $s0 10 # length of the LIST
	li $s1 0
	
	la $a1 LIST # get address of the array
printLoop:
	li $a2 0 # search from index = 0
	li $a3 10 # to index = 10
	
	sll $t0 $s1 2
	addu $t0 $t0 $a1
	lw $s2 0($t0)
		
	move $a0 $s2
	li $v0 1
	syscall # PRINT value
	
	la $a0 line
	li $v0 4
	syscall # PRINT " was found at index "
	
	move $a0 $s2
	jal binSearch
	move $s2 $v0
	move $a0 $v0
	li $v0 1
	syscall # PRINT index
	
	beq $s2 $s1 skipError
	la $a0 expect
	li $v0 4
	syscall # PRINT ", expected "
	
	move $a0 $s1
	li $v0 1
	syscall # PRINT expected index
		
skipError:
	la $a0 newline
	li $v0 4
	syscall # PRINT "\n"
	
	addiu $s1 $s1 1 # s1++
	blt $s1 $s0 printLoop # repeat
	
	li $v0 10
	syscall # EXIT
	
binSearch: # binSearch($a0,$a1,$a2,$a3)
	# ****** YOUR CODE STARTS HERE ******
	# Labels:
	# $t0 = int index of nmin
	# $t1 = int index of nmax
	# $t2 = int index of nmid
	# $t3 = memory byte address of list[nmid]
	# $t4 = contents of list[nmid]
	add $t0, $a2, $zero
	add $t1, $a3, $zero
while:
        blt $t1, $t0, notFound
        add $t2, $t1, $t0
        srl $t2, $t2, 1 # floordiv by 2
        sll $t2, $t2, 2 # $t2 becomes byte index of nmid
        add $t3, $t2, $a1
        lw $t4, 0($t3)
        bgt $t4, $a0, if
        blt $t4, $a0, elseif
        j else
if:
	sub $t1, $t2, 4 # update byte index of nmax
	srl $t1, $t1, 2 # $t1 becomes int index of nmax
	j while
elseif:
	add $t0, $t2, 4 # update byte index of nmin
	srl $t0, $t0, 2 # $t0 becomes int index of nmin
	j while
else:
	srl $v0, $t2, 2 # $v0 becomes int index of nmid
	j return
notFound:
	li $v0 -1 # return value = -1
	j return
return:
	jr $ra # return
