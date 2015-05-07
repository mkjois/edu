"""61A Homework 4
Name: Manohar Jois
Login: cs61a-3u
TA: Allen Nguyen
Section: 203
"""

from doctest import run_docstring_examples
test = run_docstring_examples

# Q1.

def reverse_list(s):
    """Reverse the contents of list s and return None.

    >>> digits = [6, 2, 9, 5, 1, 4, 1, 3]
    >>> reverse_list(digits)
    >>> digits
    [3, 1, 4, 1, 5, 9, 2, 6]
    >>> d = digits
    >>> reverse_list(d)
    >>> digits
    [6, 2, 9, 5, 1, 4, 1, 3]
    """
    reverse, inc, length = [], 0, len(s)
    while inc < length:
        inc += 1
        reverse += [s[length - inc]]
    for i in range(length):
        s[i] = reverse[i]


# Q2.

def make_accumulator():
    """Return an accumulator function that takes a single numeric argument and
    accumulates that argument into total, then returns total.

    >>> acc = make_accumulator()
    >>> acc(15)
    15
    >>> acc(10)
    25
    >>> acc2 = make_accumulator()
    >>> acc2(7)
    7
    >>> acc3 = acc2
    >>> acc3(6)
    13
    >>> acc2(5)
    18
    >>> acc(4)
    29
    """
    total = []
    def accumulator(num):
        total.append(num)
        return sum(total)
    return accumulator


# Q3.

def make_accumulator_nonlocal():
    """Return an accumulator function that takes a single numeric argument and
    accumulates that argument into total, then returns total.

    >>> acc = make_accumulator_nonlocal()
    >>> acc(15)
    15
    >>> acc(10)
    25
    >>> acc2 = make_accumulator_nonlocal()
    >>> acc2(7)
    7
    >>> acc3 = acc2
    >>> acc3(6)
    13
    >>> acc2(5)
    18
    >>> acc(4)
    29
    """
    total = 0
    def accumulator(num):
        nonlocal total
        total += num
        return total
    return accumulator


# Q4.

def make_counter():
    """Return a counter function.

    >>> c = make_counter()
    >>> c('a', 3)
    3
    >>> c('a', 5)
    8
    >>> c('b', 7)
    7
    >>> c('a', 9)
    17
    >>> c2 = make_counter()
    >>> c2(1, 2)
    2
    >>> c2(3, 4)
    4
    >>> c2(3, c('b', 6))
    17
    """
    s = {}
    def counter(key, value):
        nonlocal s
        total = s.get(key, 0)
        s[key] = total + value
        return s[key]
    return counter


# Q5.

def square(x):
    return x*x

def compose1(f, g):
    """Return a function of x that computes f(g(x))."""
    return lambda x: f(g(x))

from functools import reduce

def repeated(f, n):
    """Return the function that computes the nth application of f, for n>=1.

    f -- a function that takes one argument
    n -- a positive integer

    >>> repeated(square, 2)(5)
    625
    >>> repeated(square, 4)(5)
    152587890625
    """
    assert type(n) == int and n > 0, "Bad n"
    return reduce(compose1, [f]*n )


# Q6.

def card(n):
    """Return the playing card numeral for a positive n <= 13."""
    assert type(n) == int and n > 0 and n <= 13, "Bad card n"
    specials = {1: 'A', 11: 'J', 12: 'Q', 13: 'K'}
    return specials.get(n, str(n))

def shuffle(cards):
    """Return a shuffled list that interleaves the two halves of cards.

    >>> suits = ['ÃƒÂ¢Ã¢â€žÂ¢Ã‚Â¡', 'ÃƒÂ¢Ã¢â€žÂ¢Ã‚Â¢', 'ÃƒÂ¢Ã¢â€žÂ¢Ã‚Â¤', 'ÃƒÂ¢Ã¢â€žÂ¢Ã‚Â§']
    >>> cards = [card(n) + suit for n in range(1,14) for suit in suits]
    >>> cards[:12]
    ['AÃƒÂ¢Ã¢â€žÂ¢Ã‚Â¡', 'AÃƒÂ¢Ã¢â€žÂ¢Ã‚Â¢', 'AÃƒÂ¢Ã¢â€žÂ¢Ã‚Â¤', 'AÃƒÂ¢Ã¢â€žÂ¢Ã‚Â§', '2ÃƒÂ¢Ã¢â€žÂ¢Ã‚Â¡', '2ÃƒÂ¢Ã¢â€žÂ¢Ã‚Â¢', '2ÃƒÂ¢Ã¢â€žÂ¢Ã‚Â¤', '2ÃƒÂ¢Ã¢â€žÂ¢Ã‚Â§', '3ÃƒÂ¢Ã¢â€žÂ¢Ã‚Â¡', '3ÃƒÂ¢Ã¢â€žÂ¢Ã‚Â¢', '3ÃƒÂ¢Ã¢â€žÂ¢Ã‚Â¤', '3ÃƒÂ¢Ã¢â€žÂ¢Ã‚Â§']
    >>> cards[26:30]
    ['7ÃƒÂ¢Ã¢â€žÂ¢Ã‚Â¤', '7ÃƒÂ¢Ã¢â€žÂ¢Ã‚Â§', '8ÃƒÂ¢Ã¢â€žÂ¢Ã‚Â¡', '8ÃƒÂ¢Ã¢â€žÂ¢Ã‚Â¢']
    >>> shuffle(cards)[:12]
    ['AÃƒÂ¢Ã¢â€žÂ¢Ã‚Â¡', '7ÃƒÂ¢Ã¢â€žÂ¢Ã‚Â¤', 'AÃƒÂ¢Ã¢â€žÂ¢Ã‚Â¢', '7ÃƒÂ¢Ã¢â€žÂ¢Ã‚Â§', 'AÃƒÂ¢Ã¢â€žÂ¢Ã‚Â¤', '8ÃƒÂ¢Ã¢â€žÂ¢Ã‚Â¡', 'AÃƒÂ¢Ã¢â€žÂ¢Ã‚Â§', '8ÃƒÂ¢Ã¢â€žÂ¢Ã‚Â¢', '2ÃƒÂ¢Ã¢â€žÂ¢Ã‚Â¡', '8ÃƒÂ¢Ã¢â€žÂ¢Ã‚Â¤', '2ÃƒÂ¢Ã¢â€žÂ¢Ã‚Â¢', '8ÃƒÂ¢Ã¢â€žÂ¢Ã‚Â§']
    >>> shuffle(shuffle(cards))[:12]
    ['AÃƒÂ¢Ã¢â€žÂ¢Ã‚Â¡', '4ÃƒÂ¢Ã¢â€žÂ¢Ã‚Â¢', '7ÃƒÂ¢Ã¢â€žÂ¢Ã‚Â¤', '10ÃƒÂ¢Ã¢â€žÂ¢Ã‚Â§', 'AÃƒÂ¢Ã¢â€žÂ¢Ã‚Â¢', '4ÃƒÂ¢Ã¢â€žÂ¢Ã‚Â¤', '7ÃƒÂ¢Ã¢â€žÂ¢Ã‚Â§', 'JÃƒÂ¢Ã¢â€žÂ¢Ã‚Â¡', 'AÃƒÂ¢Ã¢â€žÂ¢Ã‚Â¤', '4ÃƒÂ¢Ã¢â€žÂ¢Ã‚Â§', '8ÃƒÂ¢Ã¢â€žÂ¢Ã‚Â¡', 'JÃƒÂ¢Ã¢â€žÂ¢Ã‚Â¢']
    >>> cards[:12]  # Should not be changed
    ['AÃƒÂ¢Ã¢â€žÂ¢Ã‚Â¡', 'AÃƒÂ¢Ã¢â€žÂ¢Ã‚Â¢', 'AÃƒÂ¢Ã¢â€žÂ¢Ã‚Â¤', 'AÃƒÂ¢Ã¢â€žÂ¢Ã‚Â§', '2ÃƒÂ¢Ã¢â€žÂ¢Ã‚Â¡', '2ÃƒÂ¢Ã¢â€žÂ¢Ã‚Â¢', '2ÃƒÂ¢Ã¢â€žÂ¢Ã‚Â¤', '2ÃƒÂ¢Ã¢â€žÂ¢Ã‚Â§', '3ÃƒÂ¢Ã¢â€žÂ¢Ã‚Â¡', '3ÃƒÂ¢Ã¢â€žÂ¢Ã‚Â¢', '3ÃƒÂ¢Ã¢â€žÂ¢Ã‚Â¤', '3ÃƒÂ¢Ã¢â€žÂ¢Ã‚Â§']
    >>> repeated(shuffle, 8)(cards) == cards
    True
    """
    assert len(cards) % 2 == 0, 'len(cards) must be even'
    length, midpoint, inc = len(cards), len(cards) // 2, 0
    first, second = cards[0 : midpoint], cards[midpoint : length]
    for item in second:
        inc += 1
        first.insert(2*inc - 1, item)
    return first

