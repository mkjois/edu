beq  $r0, $r0, branches0

addi $r0, $r0, 0x05
addi $r1, $r1, 0x09
add  $r0, $r0, $r1      # $r0 = 0x000e
lui  $r3, 0x7f
ori  $r3, $r3, 0xfd
addi $r3, $r3, 0x05     # overflow
disp $r3, 0x0           # disp[0] = 0x8002
sub  $r0, $r0, $r0
lui  $r0, 0x80
addi $r0, $r0, 0x04
sub  $r3, $r0, $r1      # overflow
disp $r3, 0x1           # disp[1] = 0x7ffb
sub  $r0, $r1, $r1
lui  $r1, 0x80
ori  $r1, $r1, 0x01
lui  $r0, 0xff
ori  $r0, $r0, 0xfd
add  $r3, $r1, $r0      # overflow
disp $r3, 0x0           # disp[0] = 0x7ffe
sub  $r1, $r0, $r0
lui  $r2, 0xff
ori  $r2, $r2, 0xff
andi $r0, $r2, 0x55
disp $r0, 0x0           # disp[0] = 0x0055
ori  $r0, $r0, 0x8a
andi $r0, $r0, 0xfd
disp $r0, 0x1           # disp[1] = 0x00dd
lui  $r0, 0x7f
ori  $r0, $r0, 0xff     # $r0 = 0x7fff
sub  $r3, $r0, $r2      # overflow
disp $r3, 0x1           # disp[1] = 0x8000
sub  $r0, $r1, $r1
sub  $r1, $r0, $r0

slt  $r2, $r2, $r0      # $r2 = 0x0001
slt  $r2, $r2, $r1      # $r2 = 0x0000
lui  $r2, 0x3c
ori  $r2, $r2, 0x3c
lui  $r1, 0x78
ori  $r1, $r1, 0x78
or   $r3, $r1, $r2
disp $r3, 0x1           # disp[1] = 0x7c7c
and  $r3, $r2, $r1
disp $r3, 0x0           # disp[0] = 0x3838
lui  $r3, 0x80
or   $r0, $r1, $r3
slt  $r3, $r2, $r1
disp $r3, 0x0           # disp[0] = 0x0001
slt  $r3, $r2, $r0
disp $r3, 0x1           # disp[1] = 0x0000

lui  $r0, 0x03
ori  $r0, $r0, 0x82
lui  $r1, 0x7c
ori  $r1, $r1, 0xfe
addp8 $r3, $r0, $r1
lui  $r1, 0x7e
ori  $r1, $r1, 0xfe
addp8 $r3, $r1, $r0     # overflow
lui  $r1, 0x7c
ori  $r1, $r1, 0xfc
addp8 $r3, $r0, $r1     # overflow
lui  $r1, 0x7e
ori  $r1, $r1, 0xfc
addp8 $r3, $r1, $r0     # overflow

lui  $r0, 0x81
ori  $r0, $r0, 0x7e
lui  $r1, 0x01
ori  $r1, $r1, 0xff
subp8 $r3, $r0, $r1
lui  $r1, 0x01
ori  $r1, $r1, 0xfe
subp8 $r3, $r0, $r1     # overflow
lui  $r1, 0x02
ori  $r1, $r1, 0xff
subp8 $r3, $r0, $r1     # overflow
lui  $r1, 0x02
ori  $r1, $r1, 0xfe
subp8 $r3, $r0, $r1     # overflow

lui  $r0, 0x00
ori  $r0, $r0, 0x06
lui  $r2, 0x00
ori  $r2, $r2, 0x01
sllv $r0, $r0, $r2      # $r0 = 0x000c
ori  $r2, $r2, 0x06
sllv $r0, $r0, $r2      # $r0 = 0x0600
addi $r1, $r2, -4
andi $r2, $r2, 0xfd
sllv $r0, $r0, $r2      # $r0 = 0xc000
srlv $r0, $r0, $r1      # $r0 = 0x1800
sllv $r0, $r0, $r1
srav $r0, $r0, $r2      # $r0 = 0xfe00
sub  $r0, $r0, $r0
addp8 $r1, $r0, $r0
subp8 $r2, $r1, $r0

branches0:
addi $r0, $r1, 0x0f
beq  $r0, $r1, error        # don't branch
andi $r1, $r0, 0xcc
addi $r1, $r1, 0x03
beq  $r1, $r0, branches1    # branch
disp $r0, 0x0               # won't disp

branches3:
bne  $r0, $r1, error
lui  $r0, 0x00
ori  $r0, $r0, 0x03
lui  $r1, 0x00
jal  jump0
sub  $r0, $r0, $r0
add  $r1, $r0, $r0
ori  $r0, $r0, 0x03
jal  jump3
j    finito

branches1:
bne  $r1, $r0, error        # don't branch
addi $r0, $r1, 0x01
bne  $r0, $r1, branches2    # branch
disp $r0, 0x1               # won't disp

branches2:
sub  $r1, $r1, $r1
or   $r1, $r1, $r0
j    branches3
disp $r0, 0x0               # won't disp

jump0:
beq  $r0, $r1, jump2
addi $r0, $r0, -1
j jump0

jump2:
jr $r3

jump3:
beq  $r0, $r1, jump4
sw   $r3, 0($r2)
addi $r2, $r2, 0x01
addi $r0, $r0, -1
jal  jump3
addi $r2, $r2, -1
lw   $r3, 0($r2)

jump4:
jr   $r3

error:
lui  $r3, 0xff
ori  $r3, $r3, 0xff
disp $r3, 0x0
disp $r3, 0x1

finito:
lui  $r1, 0xf0
ori  $r1, $r1, 0x0f
disp $r1, 0x0
disp $r1, 0x1
