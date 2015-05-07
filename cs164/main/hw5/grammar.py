# The Grammar:
#
#   S-> E | S,S
#   E-> E+E|E-E|E*E|(E)|-E|V
#   V-> a|b|c

# Original Grammar
G_org = {
    'S':('E','S,S'),                        \
    'E':('E+E','E-E','E*E','(E)','-E','V'), \
    'V':('a','b','c')                       \
   }

# Unambiguous Grammar
G_unamb={
  'S': ('E', 'E,S'), \
  'E': ('E+F', 'E-F', 'F'), \
  'F': ('F*G', 'G'), \
  'G': ('(E)', '-G', 'V'), \
  'V': ('a', 'b', 'c') \
}

# Fast Grammar
G_fast={
  'S': ('E', 'E,S'), \
  'E': ('E+F', 'E-F', 'F'), \
  'F': ('F*G', 'G'), \
  'G': ('(E)', '-G', 'V'), \
  'V': ('a', 'b', 'c') \
}
