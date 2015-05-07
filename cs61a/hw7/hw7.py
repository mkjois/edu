"""61A Homework 7
Name: Manohar Jois
Login: cs61a-3u
TA: Allen Nguyen
Section: 203
"""

from doctest import run_docstring_examples
test = run_docstring_examples

# Q1.

class Square(object):
    def __init__(self, side):
        self.side = side

class Rect(object):
    def __init__(self, width, height):
        self.width = width
        self.height = height

def type_tag(s):
    return type_tag.tags[type(s)]

type_tag.tags = {Square: 's', Rect: 'r'}

def area_square(square):
    return square.side ** 2

def area_rect(rect):
    return rect.width * rect.height

def perim_square(square):
    return square.side * 4

def perim_rect(rect):
    return (rect.width + rect.height) * 2

operations = {}
operations[('area', 's')] = area_square
operations[('area', 'r')] = area_rect
operations[('perimeter', 's')] = perim_square
operations[('perimeter', 'r')] = perim_rect

def apply(operator_name, shape):
    """Apply operator to shape.

    >>> apply('area', Square(10))
    100
    >>> apply('perimeter', Square(5))
    20
    >>> apply('area', Rect(5, 10))
    50
    >>> apply('perimeter', Rect(2, 4))
    12
    """
    key = (operator_name, type_tag(shape))
    return operations[key](shape)

# Q2.

def g(n):
    """Return the value of G(n), computed recursively.

    >>> g(1)
    1
    >>> g(2)
    2
    >>> g(3)
    3
    >>> g(4)
    10
    >>> g(5)
    22
    """
    if n <= 3:
        return n
    return g(n-1) + 2*g(n-2) + 3*g(n-3)

def g_iter(n):
    """Return the value of G(n), computed iteratively.
    >>> g_iter(1)
    1
    >>> g_iter(2)
    2
    >>> g_iter(3)
    3
    >>> g_iter(4)
    10
    >>> g_iter(5)
    22
    """
    if n <= 3: return n
    count, term1, term2, term3 = 4, 3, 4, 3
    while count < n:
        term1, term2, term3 = term2 * 3 // 2, term3 * 2, term1 + term2 + term3
        count += 1
    return term1 + term2 + term3

# Q3.

def part(n):
    """Return the number of partitions of positive integer n.

    >>> part(5)
    7
    >>> part(10)
    42
    >>> part(15)
    176
    >>> part(20)
    627
    """
    int_range = [i+1 for i in range(n)][::-1]
    def parts(x, int_list):
        if x==0: return 1
        if x<0 or len(int_list)==0: return 0
        largest = int_list[0]
        return parts(x-largest, int_list) + parts(x, int_list[1:])
    return parts(n, int_range)

# Q4.

from operator import sub, mul

def make_anonymous_factorial():
    """Return the value of an expression that computes factorial.

    >>> make_anonymous_factorial()(5)
    120
    """
    return YOUR_EXPRESSION_HERE

# Q5.

def has_cycle(s):
    """Return whether Rlist s contains a cycle.

    >>> s = Rlist(1, Rlist(2, Rlist(3)))
    >>> s.rest.rest.rest = s
    >>> has_cycle(s)
    True
    >>> t = Rlist(1, Rlist(2, Rlist(3)))
    >>> has_cycle(t)
    False
    """
    "*** YOUR CODE HERE ***"

def has_cycle_constant(s):
    """Return whether Rlist s contains a cycle.

    >>> s = Rlist(1, Rlist(2, Rlist(3)))
    >>> s.rest.rest.rest = s
    >>> has_cycle_constant(s)
    True
    >>> t = Rlist(1, Rlist(2, Rlist(3)))
    >>> has_cycle_constant(t)
    False
    """
    "*** YOUR CODE HERE ***"

class Rlist(object):
    """A recursive list consisting of a first element and the rest."""
    class EmptyList(object):
        def __len__(self):
            return 0

    empty = EmptyList()

    def __init__(self, first, rest=empty):
        self.first = first
        self.rest = rest

    def __repr__(self):
        args = repr(self.first)
        if self.rest is not Rlist.empty:
            args += ', {0}'.format(repr(self.rest))
        return 'Rlist({0})'.format(args)

    def __len__(self):
        return 1 + len(self.rest)

    def __getitem__(self, i):
        if i == 0:
            return self.first
        return self.rest[i-1]
