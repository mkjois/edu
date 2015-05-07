MOD:
lui     $r0, 0x00
sw      $r3, 3($r0)
add     $r3, $r0, $r0
lw      $r1, 1($r3)
slt     $r2, $r1, $r0
beq     $r2, $r0, init

# negates $r1 to get absolute value
add     $r2, $r1, $r1
sub     $r1, $r1, $r2

init:
lw      $r0, 0($r3)
addi    $r2, $r0, 0x00

loop:
sw      $r2, 2($r3)
slt     $r2, $r2, $r1
sw      $r0, 0($r3)
lui     $r0, 0x00
ori     $r0, $r0, 0x01
beq     $r2, $r0, done
lw      $r0, 0($r3)
lw      $r2, 2($r3)
sub     $r2, $r2, $r1
j       loop

done:
lw      $r3, 3($r3)
jr      $r3
