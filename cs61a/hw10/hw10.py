# Name: Manohar Jois
# Login: cs61a-3u
# TA: Allen Nguyen
# Section: 203

from doctest import run_docstring_examples
test = lambda fn: run_docstring_examples(fn, globals(), True)

BRACKETS = {('[', ']'): '+',
            ('(', ')'): '-',
            ('<', '>'): '*',
            ('{', '}'): '/'}
LEFT_RIGHT = {left:right for left, right in BRACKETS.keys()}
ALL_BRACKETS = set(b for bs in BRACKETS for b in bs)

# Q1.

a = '<[2{12.5 6.0}](3 -4 5)>'

def tokenize(line):
    """Convert a string into a list of tokens.

    >>> tokenize('<[2{12.5 6.0}](3 -4 5)>')
    ['<', '[', 2, '{', 12.5, 6.0, '}', ']', '(', 3, -4, 5, ')', '>']

    >>> tokenize('2.3.4')
    Traceback (most recent call last):
        ...
    ValueError: invalid token 2.3.4

    >>> tokenize('?')
    Traceback (most recent call last):
        ...
    ValueError: invalid token ?

    >>> tokenize('hello')
    Traceback (most recent call last):
        ...
    ValueError: invalid token hello

    >>> tokenize('<(GO BEARS)>')
    Traceback (most recent call last):
        ...
    ValueError: invalid token GO

    >>> tokenize('()')
    ['(', ')']
    >>> tokenize('<()>')
    ['<', '(', ')', '>']
    >>> tokenize('<()()>')
    ['<', '(', ')', '(', ')', '>']
    >>> tokenize('<5>')
    ['<', 5, '>']
    >>> tokenize('<(16)(47)[93]{24}>')
    ['<', '(', 16, ')', '(', 47, ')', '[', 93, ']', '{', 24, '}', '>']
    >>> tokenize('<10 (16)(47) 78 [93]{24} 82 36>')
    ['<', 10, '(', 16, ')', '(', 47, ')', 78, '[', 93, ']', '{', 24, '}', 82, 36, '>']
    >>> tokenize('<10 (16)(47) 78 <34 [45]> [93]{24} 82 36>')
    ['<', 10, '(', 16, ')', '(', 47, ')', 78, '<', 34, '[', 45, ']', '>', '[', 93, ']', '{', 24, '}', 82, 36, '>']
    """
    for bracket in ALL_BRACKETS:
        if bracket in line:
            line = line.replace(bracket, ' ' + bracket + ' ')
    char_list, token_list = line.split(), []
    for char in char_list:
        num = coerce_to_number(char)
        if num is not None:
            token_list.append(num)
        elif char in ALL_BRACKETS:
            token_list.append(char)
        else:
            raise ValueError('invalid token {0}'.format(char))
    return token_list

def coerce_to_number(token):
    """Coerce a string to a number or return None.

    >>> coerce_to_number('-2.3')
    -2.3
    >>> print(coerce_to_number('('))
    None
    """
    try:
        return int(token)
    except (TypeError, ValueError):
        try:
            return float(token)
        except (TypeError, ValueError):
            return None

# Q2.

RIGHT_LEFT = {right:left for left, right in BRACKETS.keys()}

def is_num(num):
    return isinstance(num, int) or isinstance(num, float)

def isvalid(tokens):
    """Return whether some prefix of tokens represent a valid Brackulator
    expression. Tokens in that expression are removed from tokens as a side
    effect.

    >>> isvalid(tokenize('([])'))
    True
    >>> isvalid(tokenize('([]')) # Missing right bracket
    False
    >>> isvalid(tokenize('[)]')) # Extra right bracket
    False
    >>> isvalid(tokenize('([)]')) # Improper nesting
    False
    >>> isvalid(tokenize('')) # No expression
    False
    >>> isvalid(tokenize('100'))
    True
    >>> isvalid(tokenize('<(( [{}] [{}] ))>'))
    True
    >>> isvalid(tokenize('<[2{12 6}](3 4 5)>'))
    True
    >>> isvalid(tokenize('()()')) # More than one expression is ok
    True
    >>> isvalid(tokenize('[])')) # Junk after a valid expression is ok
    True
    >>> isvalid(tokenize('([] [] [] [])'))
    True
    >>> isvalid(tokenize('()'))
    True
    >>> isvalid(tokenize('([3] [4] [6])'))
    True
    >>> isvalid(tokenize('([3] [4] [6] 18 24)'))
    True
    >>> isvalid(tokenize('<(( [{}] [{} ))>')) # Missing second right ]
    False
    >>> isvalid(tokenize('<(( [{}] [{}>] ))>')) # Extra right >
    False
    >>> isvalid(tokenize('<(( [{}] [{} 3 () ()] ))>'))
    True
    >>> isvalid(tokenize('<(( [{25} 5 [] 16] [{} 3 () (2 5)] ))>'))
    True
    >>> isvalid(tokenize('[(4 6) 2 <35.2 [3] ] 1 28.5>]')) #Extra right ]
    False
    >>> isvalid(tokenize('[(4 6) 2 <35.2 [3] [ 1 28.5>]')) #Missing right ]
    False
    >>> isvalid(tokenize('[]<23<[[<<<>{}{9.3{{{>')) #"[]" is a valid expression
    True
    >>> isvalid(tokenize('5<[]][{{5.3}{{'))
    True
    >>> tokenize('[] @^#8N.{ HA}VE YOU SE]EN THE M{{ARV>ELOUS BRE]ADFI()SH') #But this garbage can't be tokenized so should return an error.
    Traceback (most recent call last):
        ...
    ValueError: invalid token @^#8N.
    """
    copy = tokens[:] # is_valid shouldn't mutate tokens
    if not copy:
        return False
    if is_num(copy[0]):
        return True
    for char in copy[:]:
        if is_num(char):
            copy.remove(char)
    buffer, i, l = [], 0, len(copy)
    while i < l:
        if not buffer and i>0:
            return True
        if copy[i] in LEFT_RIGHT.keys():
            buffer.insert(0, copy[i])
        elif copy[i] in RIGHT_LEFT.keys():
            lbrack = RIGHT_LEFT[copy[i]]
            try:
                if buffer.index(lbrack) != 0:
                    return False
                else:
                    buffer.remove(lbrack)
            except ValueError:
                return False
        i += 1
    if not buffer:
            return True
    return False

# Q3.

def tail_read(tokens):
    first = 4
    rest = 5
    return Pair(first, rest)

def brack_read(tokens):
    """Return an expression tree for the first well-formed Brackulator
    expression in tokens. Tokens in that expression are removed from tokens as
    a side effect.

    >>> brack_read(tokenize('100'))
    100
    >>> brack_read(tokenize('([])'))
    Pair('-', Pair(Pair('+', nil), nil))
    >>> print(brack_read(tokenize('<[2{12 6}](3 4 5)>')))
    (* (+ 2 (/ 12 6)) (- 3 4 5))
    >>> brack_read(tokenize('(1)(1)')) # More than one expression is ok
    Pair('-', Pair(1, nil))
    >>> brack_read(tokenize('[])')) # Junk after a valid expression is ok
    Pair('+', nil)

    >>> brack_read(tokenize('([]')) # Missing right bracket
    Traceback (most recent call last):
        ...
    SyntaxError: unexpected end of line

    >>> brack_read(tokenize('[)]')) # Extra right bracket
    Traceback (most recent call last):
        ...
    SyntaxError: unexpected )

    >>> brack_read(tokenize('([)]')) # Improper nesting
    Traceback (most recent call last):
        ...
    SyntaxError: unexpected )

    >>> brack_read(tokenize('')) # No expression
    Traceback (most recent call last):
        ...
    SyntaxError: unexpected end of line

    >>> brack_read(tokenize('[]')) #Basic functionality
    Pair('+', nil)
    >>> brack_read(tokenize('()')) #Different brackets
    Pair('-', nil)
    >>> brack_read(tokenize('<()>')) #Nested brackets
    Pair('*', Pair(Pair('-', nil), nil))
    >>> brack_read(tokenize('<()()>')) #More than one expression
    Pair('*', Pair(Pair('-', nil), Pair(Pair('-', nil), nil)))
    >>> print(brack_read(tokenize('<5>'))) #Numbers
    (* 5)
    >>> print(brack_read(tokenize('<(16)(47)[93]{24}>'))) #Numbers, nested brackets, >1 exp
    (* (- 16) (- 47) (+ 93) (/ 24))
    >>> print(brack_read(tokenize('<10 (16)(47) 78 [93]{24} 82 36>')))
    (* 10 (- 16) (- 47) 78 (+ 93) (/ 24) 82 36)
    >>> print(brack_read(tokenize('<10 (16)(47) 78 <34 [45]> [93]{24} 82 36>')))
    (* 10 (- 16) (- 47) 78 (* 34 (+ 45)) (+ 93) (/ 24) 82 36)
    >>> print(brack_read(tokenize('([3 6]<2 (37) 1>[(9 3) 2.50 <0.2>])')))
    (- (+ 3 6) (* 2 (- 37) 1) (+ (- 9 3) 2.5 (* 0.2)))
    >>> brack_read(tokenize('[])<<{ [}}><{{{')) #Junk is fine
    Pair('+', nil)
    """
    expr, left_bracks = [], []
    while not isvalid(expr):
        try:
            char = tokens.pop(0)
            if char in LEFT_RIGHT.keys():
                left_bracks.append(char)
            elif char in RIGHT_LEFT.keys():
                if not left_bracks or left_bracks[-1] != RIGHT_LEFT[char]:
                    raise SyntaxError('unexpected {0}'.format(char))
                left_bracks.pop()
            expr.append(char)
        except IndexError:
            raise SyntaxError('unexpected {0}'.format('end of line'))
    if is_num(expr[0]):
        return expr[0]
    expr.pop(0)

# Q4.

from urllib.request import urlopen

def puzzle_4():
    """Return the soluton to puzzle 4."""
    base = 'http://www.pythonchallenge.com/pc/def/'
    codeword = 'linkedlist.php'
    nothing = base + codeword + '?nothing='
    number = str(12345)
    address = nothing + number
    while True:
        try:
            src = urlopen(address).read().decode()
            number = str(src[len(a)-5:])
            address = nothing + number
        except:
            break
    print(number)

class Pair(object):
    """A pair has two instance attributes: first and second.  For a Pair to be
    a well-formed list, second is either a well-formed list or nil.  Some
    methods only apply to well-formed lists.

    >>> s = Pair(1, Pair(2, nil))
    >>> s
    Pair(1, Pair(2, nil))
    >>> print(s)
    (1 2)
    >>> len(s)
    2
    >>> s[1]
    2
    >>> print(s.map(lambda x: x+4))
    (5 6)
    """
    def __init__(self, first, second):
        self.first = first
        self.second = second

    def __repr__(self):
        return "Pair({0}, {1})".format(repr(self.first), repr(self.second))

    def __str__(self):
        s = "(" + str(self.first)
        second = self.second
        while isinstance(second, Pair):
            s += " " + str(second.first)
            second = second.second
        if second is not nil:
            s += " . " + str(second)
        return s + ")"

    def __len__(self):
        n, second = 1, self.second
        while isinstance(second, Pair):
            n += 1
            second = second.second
        if second is not nil:
            raise TypeError("length attempted on improper list")
        return n

    def __getitem__(self, k):
        if k < 0:
            raise IndexError("negative index into list")
        y = self
        for _ in range(k):
            if y.second is nil:
                raise IndexError("list index out of bounds")
            elif not isinstance(y.second, Pair):
                raise TypeError("ill-formed list")
            y = y.second
        return y.first

    def map(self, fn):
        """Return a Scheme list after mapping Python function FN to SELF."""
        mapped = fn(self.first)
        if self.second is nil or isinstance(self.second, Pair):
            return Pair(mapped, self.second.map(fn))
        else:
            raise TypeError("ill-formed list")

class nil(object):
    """The empty list"""

    def __repr__(self):
        return "nil"

    def __str__(self):
        return "()"

    def __len__(self):
        return 0

    def __getitem__(self, k):
        if k < 0:
            raise IndexError("negative index into list")
        raise IndexError("list index out of bounds")

    def map(self, fn):
        return self

nil = nil() # Assignment hides the nil class; there is only one instance


def read_eval_print_loop():
    """Run a read-eval-print loop for the Brackulator language."""
    global Pair, nil
    from scheme_reader import Pair, nil
    from scalc import calc_eval

    while True:
        try:
            src = tokenize(input('> '))
            while len(src) > 0:
              expression = brack_read(src)
              print(calc_eval(expression))
        except (SyntaxError, ValueError, TypeError, ZeroDivisionError) as err:
            print(type(err).__name__ + ':', err)
        except (KeyboardInterrupt, EOFError):  # <Control>-D, etc.
            return
