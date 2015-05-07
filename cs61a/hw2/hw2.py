"""61A Homework 2
Name: Manohar Jois
Login: cs61a-3u
TA: Allen Nguyen
Section: 203
"""

def square(x):
    """Return x squared."""
    return x * x

# Q1.

def product(n, term):
    """Return the product of the first n terms in a sequence.

    term -- a function that takes one argument

    >>> product(4, square)
    576
    """
    total, k = 1, 1
    while k <= n:
        total, k = total * term(k), k+1
    return total

def factorial(n):
    """Return n factorial by calling product.

    >>> factorial(4)
    24
    """
    return product(n, int)

# Q2.

def accumulate(combiner, start, n, term):
    """Return the result of combining the first n terms in a sequence."""
    total, k = start, 1
    while k <= n:
        total, k = combiner(total, term(k)), k+1
    return total

def summation_using_accumulate(n, term):
    """An implementation of summation using accumulate.

    >>> summation_using_accumulate(4, square)
    30
    """
    from operator import add
    return accumulate(add, 0, n, term)

def product_using_accumulate(n, term):
    """An implementation of product using accumulate.

    >>> product_using_accumulate(4, square)
    576
    """
    from operator import mul
    return accumulate(mul, 1, n, term)

# Q3.

def double(f):
    """Return a function that applies f twice.

    f -- a function that takes one argument

    >>> double(square)(2)
    16
    """
    def g(x):
        return f(f(x))
    return g

# Q4.

def repeated(f, n):
    """Return the function that computes the nth application of f.

    f -- a function that takes one argument
    n -- a positive integer

    >>> repeated(square, 2)(5)
    625
    >>> repeated(square, 4)(5)
    152587890625
    """
    g, k = f, 2
    while k <= n:
        g, k = compose1(f, g), k+1
    return g

def compose1(f, g):
    """Return a function h, such that h(x) = f(g(x))."""
    def h(x):
        return f(g(x))
    return h

# Q5.

def zero(f):
    return lambda x: x

def successor(n):
    return lambda f: lambda x: f(n(f)(x))

def one(f):
    """Church numeral 1."""
    return successor(zero)(f)

def two(f):
    """Church numeral 2."""
    return successor(one)(f)

def add_church(m, n):
    """Return the Church numeral for m + n, for Church numerals m and n."""
    return lambda f: lambda x: m(f)(n(f)(x))

def church_to_int(n):
    """Convert the Church numeral n to a Python integer.

    >>> church_to_int(zero)
    0
    >>> church_to_int(one)
    1
    >>> church_to_int(two)
    2
    >>> three = successor(two)
    >>> church_to_int(add_church(two, three))
    5
    """
    def add_one(y):
        return y+1
    return n(add_one)(0)