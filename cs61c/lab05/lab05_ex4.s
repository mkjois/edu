main:
    lw $t0, 0($a0)
    ori $t1, $zero, 0
    add $t0, $t0, $t1
    sw $t0, 0($a1)
    addiu $v0, $0, 10
    syscall
