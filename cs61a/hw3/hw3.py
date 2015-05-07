"""61A Homework 3
Name: Manohar Jois
Login: cs61a-3u
TA: Allen Nguyen
Section: 203
"""

from doctest import run_docstring_examples
test = run_docstring_examples

def str_interval(x):
    """Return a string representation of interval x.

    >>> str_interval(interval(-1, 2))
    '-1 to 2'
    """
    return '{0} to {1}'.format(lower_bound(x), upper_bound(x))

def add_interval(x, y):
    """Return an interval that contains the sum of any value in interval x and
    any value in interval y.

    >>> str_interval(add_interval(interval(-1, 2), interval(4, 8)))
    '3 to 10'
    """
    lower = lower_bound(x) + lower_bound(y)
    upper = upper_bound(x) + upper_bound(y)
    return interval(lower, upper)

def mul_interval(x, y):
    """Return the interval that contains the product of any value in x and any
    value in y.

    >>> str_interval(mul_interval(interval(-1, 2), interval(4, 8)))
    '-8 to 16'
    """
    p1 = lower_bound(x) * lower_bound(y)
    p2 = lower_bound(x) * upper_bound(y)
    p3 = upper_bound(x) * lower_bound(y)
    p4 = upper_bound(x) * upper_bound(y)
    return interval(min(p1, p2, p3, p4), max(p1, p2, p3, p4))


# Q1.

def interval(a, b):
    """Construct an interval from a to b."""
    return (a,b)

def lower_bound(x):
    """Return the lower bound of interval x."""
    return x[0]

def upper_bound(x):
    """Return the upper bound of interval x."""
    return x[1]

# Q2.

def div_interval(x, y):
    """Return the interval that contains the quotient of any value in x divided
    by any value in y.

    Division is implemented as the multiplication of x by the reciprocal of y.

    >>> str_interval(div_interval(interval(-1, 2), interval(4, 8)))
    '-0.25 to 0.5'
    """
    a,b = lower_bound(y), upper_bound(y)
    assert(a>0 or b<0), 'Cannot divide by interval containing 0'
    reciprocal_y = interval(1/upper_bound(y), 1/lower_bound(y))
    return mul_interval(x, reciprocal_y)

# Q3.

def sub_interval(x, y):
    """Return the interval that contains the difference between any value in x
    and any value in y.

    >>> str_interval(sub_interval(interval(-1, 2), interval(4, 8)))
    '-9 to -2'
    """
    a, b = lower_bound(x) - upper_bound(y), upper_bound(x) - lower_bound(y)
    return interval(a,b)


# Q4.

def mul_interval_fast(x, y):
    """Return the interval that contains the product of any value in x and any
    value in y, using as few multiplications as possible.

    >>> str_interval(mul_interval_fast(interval(-1, 2), interval(4, 8)))
    '-8 to 16'
    >>> str_interval(mul_interval_fast(interval(-2, -1), interval(4, 8)))
    '-16 to -4'
    >>> str_interval(mul_interval_fast(interval(-1, 3), interval(-4, 8)))
    '-12 to 24'
    >>> str_interval(mul_interval_fast(interval(-1, 2), interval(-8, 4)))
    '-16 to 8'
    """
    xa, xb = lower_bound(x), upper_bound(x)
    ya, yb = lower_bound(y), upper_bound(y)
    one = xa>=0 and xb>=0 and ya>=0 and yb>=0
    two = xa<0 and xb>=0 and ya>=0 and yb>=0
    three = xa<0 and xb<0 and ya>=0 and yb>=0
    four = xa>=0 and xb>=0 and ya<0 and yb>=0
    five = xa<0 and xb>=0 and ya<0 and yb>=0
    six = xa<0 and xb<0 and ya<0 and yb>=0
    seven = xa>=0 and xb>=0 and ya<0 and yb<0
    eight = xa<0 and xb>=0 and ya<0 and yb<0
    nine = xa<0 and xb<0 and ya<0 and yb<0
    if one: lower, upper = xa*ya, xb*yb
    if two: lower, upper = xa*yb, xb*yb
    if three: lower, upper = xa*yb, xb*ya
    if four: lower, upper = xb*ya, xb*yb
    if five: lower, upper = min(xa*yb,xb*ya), max(xa*ya,xb*yb)
    if six: lower, upper = xa*yb, xa*ya
    if seven: lower, upper = xb*ya, xa*yb
    if eight: lower, upper = xb*ya, xa*ya
    if nine: lower, upper = xb*yb, xa*ya
    return interval(lower, upper)


# Q5.

def make_center_width(c, w):
    """Construct an interval from center and width."""
    return interval(c - w, c + w)

def center(x):
    """Return the center of interval x."""
    return (upper_bound(x) + lower_bound(x)) / 2

def width(x):
    """Return the width of interval x."""
    return (upper_bound(x) - lower_bound(x)) / 2


def make_center_percent(c, p):
    """Construct an interval from center and percentage tolerance.

    >>> str_interval(make_center_percent(2, 50))
    '1.0 to 3.0'
    """
    return interval(c - c*p/100, c + c*p/100)

def percent(x):
    """Return the percentage tolerance of interval x.

    >>> percent(interval(1, 3))
    50.0
    """
    c, w = center(x), width(x)
    return 100*w/c

# Q6.

def par1(r1, r2):
    return div_interval(mul_interval(r1, r2), add_interval(r1, r2))

def par2(r1, r2):
    one = interval(1, 1)
    rep_r1 = div_interval(one, r1)
    rep_r2 = div_interval(one, r2)
    return div_interval(one, add_interval(rep_r1, rep_r2))


# These two intervals give different results for parallel resistors:
a = make_center_percent(40,2)
b = make_center_percent(30,1)

def show_problem():
    print("Let 'a' be the interval", a, 'with center 40 and 2% tolerance')
    print("Let 'b' be the interval", b, 'with center 30 and 1% tolerance')
    print('')
    print('par1(a,b) =', par1(a,b))
    print('par2(a,b) =', par2(a,b))
    print('')
    print('par1(a,b) == par2(a,b) ?')
    return par1(a,b) == par2(a,b)

# Q7.

def multiple_references_explanation():
    return """In the par1 function, both r1 and r2 are referred to twice, once
each as arguments of calls to mul_interval and add_interval. These intervals
are supposed to represent uncertainties in measurements, so operating on the
same uncertainties around a value more than once will unnecessarily augment
the uncertainties in the output interval."""

# Q8.


def quadratic(x, a, b, c):
    """Return the interval that is the range the quadratic defined by a, b, and
    c, for domain interval x.

    >>> str_interval(quadratic(interval(0, 2), -2, 3, -1))
    '-3 to 0.125'
    >>> str_interval(quadratic(interval(1, 3), 2, -3, 1))
    '0 to 10'
    """
    x_vert, xa, xb = -b / (2*a), lower_bound(x), upper_bound(x)
    equation = lambda n: a*n*n + b*n + c
    ya, yb, y_vert = equation(xa), equation(xb), equation(x_vert)
    if xa > x_vert or xb < x_vert:
        lower, upper = min(ya,yb), max(ya,yb)
    else:
        lower, upper = min(ya, yb, y_vert), max(ya, yb, y_vert)
    return interval(lower, upper)


# Q9.

def non_zero(x):
    """Return whether x contains 0."""
    return lower_bound(x) > 0 or upper_bound(x) < 0

def square_interval(x):
    """Return the interval that contains all squares of values in x, where x
    does not contain 0.
    """
    assert non_zero(x), 'square_interval is incorrect for x containing 0'
    return mul_interval(x, x)

# The first two of these intervals contain 0, but the third does not.
seq = (interval(-1, 2), make_center_width(-1, 2), make_center_percent(-1, 50))

zero = interval(0, 0)

def sum_nonzero_with_for(seq):
    """Returns an interval that is the sum of the squares of the non-zero
    intervals in seq, using a for statement.

    >>> str_interval(sum_nonzero_with_for(seq))
    '0.25 to 2.25'
    """
    total = zero
    for elem in seq:
        if non_zero(elem):
            total = add_interval(total, square_interval(elem))
    return total

from functools import reduce
def sum_nonzero_with_map_filter_reduce(seq):
    """Returns an interval that is the sum of the squares of the non-zero
    intervals in seq, using using map, filter, and reduce.

    >>> str_interval(sum_nonzero_with_map_filter_reduce(seq))
    '0.25 to 2.25'
    """
    filtered = filter(non_zero, seq)
    mapped = map(square_interval, filtered)
    return reduce(add_interval, mapped)

def sum_nonzero_with_generator_reduce(seq):
    """Returns an interval that is the sum of the squares of the non-zero
    intervals in seq, using using reduce and a generator expression.

    >>> str_interval(sum_nonzero_with_generator_reduce(seq))
    '0.25 to 2.25'
    """
    generated = (square_interval(elem) for elem in seq if non_zero(elem))
    return reduce(add_interval, generated)

# Q10.

def polynomial(x, c):
    """Return the interval that is the range the polynomial defined by
    coefficients c, for domain interval x.

    >>> str_interval(polynomial(interval(0, 2), (-1, 3, -2)))
    '-3 to 0.125'
    >>> str_interval(polynomial(interval(1, 3), (1, -3, 2)))
    '0 to 10'
    >>> r = polynomial(interval(0.5, 2.25), (10, 24, -6, -8, 3))
    >>> round(lower_bound(r), 5)
    18.0
    >>> round(upper_bound(r), 5)
    23.0
    """
    "*** YOUR CODE HERE ***"

