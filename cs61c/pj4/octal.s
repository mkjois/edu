OCTAL:
lui     $r0, 0x00
lui     $r1, 0x00
lui     $r2, 0x00
sw      $r3, 3($r0)
lui     $r3, 0x00
lw      $r0, 0($r3)
andi    $r1, $r0, 0x07
or      $r2, $r2, $r1
lui     $r1, 0x00
ori     $r1, $r1, 0x03
srlv    $r0, $r0, $r1
andi    $r1, $r0, 0x07
sw      $r2, 2($r3)
lui     $r2, 0x00
ori     $r2, $r2, 0x04
sllv    $r1, $r1, $r2
lw      $r2, 2($r3)
or      $r2, $r2, $r1
lui     $r1, 0x00
ori     $r1, $r1, 0x03
srlv    $r0, $r0, $r1
andi    $r1, $r0, 0x07
sw      $r2, 2($r3)
lui     $r2, 0x00
ori     $r2, $r2, 0x08
sllv    $r1, $r1, $r2
lw      $r2, 2($r3)
or      $r2, $r2, $r1
disp    $r2, 0x0
lw      $r3, 3($r3)
jr      $r3
