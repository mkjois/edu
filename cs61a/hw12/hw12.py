# Name: Manohar Jois
# Login: cs61a-3u
# TA: Allen Nguyen
# Section: 203

from doctest import run_docstring_examples
test = lambda fn: run_docstring_examples(fn, globals(), True)

# Q1.

class Mobile(object):
    """A simple binary mobile that has branches of weights or other mobiles.

    >>> Mobile(1, 2)
    Traceback (most recent call last):
        ...
    TypeError: 1 is not a Branch
    >>> m = Mobile(Branch(1, Weight(2)), Branch(2, Weight(1)))
    >>> m.weight
    3
    >>> m.isbalanced
    True
    >>> m.left.contents = Mobile(Branch(1, Weight(1)), Branch(2, Weight(1)))
    >>> m.weight
    3
    >>> m.left.contents.isbalanced
    False
    >>> m.isbalanced # All submobiles must be balanced for m to be balanced
    False
    >>> m.left.contents.right.contents.weight = 0.5
    >>> m.left.contents.isbalanced
    True
    >>> m.isbalanced
    False
    >>> m.right.length = 1.5
    >>> m.isbalanced
    True
    >>> Mobile(Branch(1, Weight(5)), [1])
    Traceback (most recent call last):
        ...
    TypeError: [1] is not a Branch

    >>> check_positive([5])
    Traceback (most recent call last):
    ...
    TypeError: [5] is not a number

    #When sub-mobile weight is changed manually, mobile weight/balance should also change
    >>> w1 = Weight(1)
    >>> w2 = Weight(2)
    >>> m = Mobile(Branch(26, w1), Branch(13, w2))
    >>> m.weight
    3
    >>> m.isbalanced
    True
    >>> w1.weight = 5346
    >>> m.weight
    5348
    >>> m.isbalanced
    False

    #Rebalance tree by lengthening right branch
    >>> m.right.length = 2673*26
    >>> m.isbalanced
    True

    #Unbalanced submobile => unbalanced mobile
    >>> w1.isbalanced = False
    >>> m.isbalanced
    False

    #MOBILECEPTION
    >>> m = Mobile(Branch(1, Mobile(Branch(1, Weight(2)), Branch(2, Weight(1)))), Branch(2, Mobile(Branch(1, Weight(2)), Branch(2, Weight(1)))))
    >>> m.weight
    6
    >>> m.isbalanced
    False
    >>> m.left.contents.left.contents.weight = 4
    >>> m.left.contents.right.contents.weight = 2
    >>> m.isbalanced
    True
    >>> m.weight
    9
    """

    def __init__(self, left, right):
        if not type(left) == Branch:
            raise TypeError('{0} is not a Branch'.format(repr(left)))
        if not type(right) == Branch:
            raise TypeError('{0} is not a Branch'.format(repr(right)))
        self.left = left
        self.right = right

    @property
    def weight(self):
        """The total weight of the mobile."""
        return self.left.contents.weight + self.right.contents.weight

    @property
    def isbalanced(self):
        """True if and only if the mobile is balanced."""
        lcont, rcont = self.left.contents, self.right.contents
        eq_torque = self.left.torque == self.right.torque
        return eq_torque and lcont.isbalanced and rcont.isbalanced

def check_positive(x):
    """Check that x is a positive number, and raise an exception otherwise.

    >>> check_positive('hello')
    Traceback (most recent call last):
    ...
    TypeError: hello is not a number
    >>> check_positive('1')
    Traceback (most recent call last):
    ...
    TypeError: 1 is not a number
    >>> check_positive(-2)
    Traceback (most recent call last):
    ...
    ValueError: -2 <= 0
    """
    if type(x) == int or type(x) == float:
        if x > 0:
            return True
        else:
            raise ValueError('{0} <= 0'.format(str(x)))
    else:
        raise TypeError('{0} is not a number'.format(str(x)))


class Branch(object):
    """A branch of a simple binary mobile."""

    def __init__(self, length, contents):
        if type(contents) not in (Weight, Mobile):
            raise TypeError(str(contents) + ' is not a Weight or Mobile')
        check_positive(length)
        self.length = length
        self.contents = contents

    @property
    def torque(self):
        """The torque on the branch"""
        return self.length * self.contents.weight


class Weight(object):
    """A weight."""
    def __init__(self, weight):
        check_positive(weight)
        self.weight = weight
        self.isbalanced = True


# Q2.

def interpret_mobile(s):
    """Return a Mobile described by string s by substituting one of the classes
    Branch, Weight, or Mobile for each occurrenct of the letter T.

    >>> simple = 'Mobile(T(2,T(1)), T(1,T(2)))'
    >>> interpret_mobile(simple).weight
    3
    >>> interpret_mobile(simple).isbalanced
    True
    >>> s = 'T(T(4,T(T(4,T(1)),T(1,T(4)))),T(2,T(10)))'
    >>> m = interpret_mobile(s)
    >>> m.weight
    15
    >>> m.isbalanced
    True
    """
    next_T = s.find('T')        # The index of the first 'T' in s.
    if next_T == -1:            # The string 'T' was not found in s
        try:
            return eval(s)      # Interpret s
        except TypeError as e:
            return None         # Return None if s is not a valid mobile
    for t in ('Branch', 'Weight', 'Mobile'):
        copy = s[:next_T] + t + s[next_T + 1:]
        result = interpret_mobile(copy)
        if result:
            return result
        else:
            pass
    return None


# Q3.

class Stream(object):
    """A lazily computed recursive list."""

    class empty(object):
        def __repr__(self):
            return 'Stream.empty'

    empty = empty()

    def __init__(self, first, compute_rest=lambda: Stream.empty):
        assert callable(compute_rest), 'compute_rest must be callable.'
        self.first = first
        self._compute_rest = compute_rest
        self._rest = None

    @property
    def rest(self):
        """Return the rest of the stream, computing it if necessary."""
        if self._compute_rest is not None:
            self._rest = self._compute_rest()
            self._compute_rest = None
        return self._rest

    def __repr__(self):
        return 'Stream({0}, <...>)'.format(repr(self.first))

    def __iter__(self):
        """Return an iterator over the elements in the stream.

        >>> s = make_integer_stream(1) # [1, 2, 3, 4, 5, ...]
        >>> list(zip(range(6), s))
        [(0, 1), (1, 2), (2, 3), (3, 4), (4, 5), (5, 6)]
        """
        rest = self
        while True:
            yield rest.first
            rest = rest.rest

    def __getitem__(self, k):
        """Return the k-th element of the stream.

        >>> s = make_integer_stream(5)
        >>> s[0]
        5
        >>> s[1]
        6
        >>> [s[i] for i in range(7,10)]
        [12, 13, 14]
        """
        if k > 0:
            return self.rest[k-1]
        return self.first

def make_integer_stream(first=1):
    """Return an infinite stream of increasing integers."""
    def compute_rest():
        return make_integer_stream(first+1)
    return Stream(first, compute_rest)


# Q4.

def scale_stream(s, k):
    """Return a stream over the elements of s scaled by a number k.

    >>> s = scale_stream(make_integer_stream(3), 5)
    >>> s.first
    15
    >>> s.rest
    Stream(20, <...>)
    >>> scale_stream(s.rest, 10)[2]
    300
    """
    return Stream(k * s.first, lambda: scale_stream(s.rest, k))


# Q5.

def merge(s0, s1):
    """Return a stream over the elements of increasing s0 and s1, removing
    repeats.

    >>> ints = make_integer_stream(1)
    >>> twos = scale_stream(ints, 2)
    >>> threes = scale_stream(ints, 3)
    >>> m = merge(twos, threes)
    >>> [m[i] for i in range(10)]
    [2, 3, 4, 6, 8, 9, 10, 12, 14, 15]
    """
    if s0 is Stream.empty:
        return s1
    if s1 is Stream.empty:
        return s0
    e0, e1 = s0.first, s1.first
    if e0 < e1:
        return Stream(e0, lambda: merge(s0.rest, s1))
    elif e1 < e0:
        return Stream(e1, lambda: merge(s0, s1.rest))
    else:
        return Stream(e0, lambda: merge(s0.rest, s1.rest))

def make_s():
    """Return a stream over all positive integers with only factors 2, 3, & 5.

    >>> s = make_s()
    >>> [s[i] for i in range(20)]
    [1, 2, 3, 4, 5, 6, 8, 9, 10, 12, 15, 16, 18, 20, 24, 25, 27, 30, 32, 36]
    """
    def rest():
        s = make_s()
        twofold = scale_stream(s, 2)
        threefold = scale_stream(s, 3)
        fivefold = scale_stream(s, 5)
        return merge(twofold, merge(threefold, fivefold))
    s = Stream(1, rest)
    return s


# Q6.

class Tree:
    """An n-ary tree with internal values."""
    def __init__(self, value, branches=[]):
        self.value = value
        self.branches = branches

def values(t):
    """Yield values of a tree by interleaving iterators of the branches.

    >>> T = Tree
    >>> t = T(1, [T(2, [T(4), T(6, [T(8)])]), T(3, [T(5), T(7)])])
    >>> tuple(values(t))
    (1, 2, 3, 4, 5, 6, 7, 8)
    """
    "*** YOUR CODE HERE ***"

def interleave(*iterables):
    """Interleave elements of iterables.

    >>> tuple(interleave([1, 4], [2, 5, 7, 8], [3, 6]))
    (1, 2, 3, 4, 5, 6, 7, 8)
    """
    "*** YOUR CODE HERE ***"
