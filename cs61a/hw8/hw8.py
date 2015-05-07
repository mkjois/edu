"""61A Homework 8
Name: Manohar Jois
Login: cs61a-3u
TA: Allen Nguyen
Section: 203
"""

from doctest import run_docstring_examples
test = lambda fn: run_docstring_examples(fn, globals(), True)

def empty(s):
    return len(s) == 0

def set_contains2(s, v):
    """Return true if set s contains value v as an element.

    >>> set_contains2(s, 2)
    True
    >>> set_contains2(s, 5)
    False
    """
    if empty(s) or s.first > v:
        return False
    if s.first == v:
        return True
    return set_contains2(s.rest, v)

def intersect_set2(set1, set2):
    """Return a set containing all elements common to set1 and set2.

    >>> t = Rlist(2, Rlist(3, Rlist(4)))
    >>> intersect_set2(s, t)
    Rlist(2, Rlist(3))
    """
    if empty(set1) or empty(set2):
        return Rlist.empty
    e1, e2 = set1.first, set2.first
    if e1 == e2:
        return Rlist(e1, intersect_set2(set1.rest, set2.rest))
    if e1 < e2:
        return intersect_set2(set1.rest, set2)
    if e2 < e1:
        return intersect_set2(set1, set2.rest)

# Q1.

def adjoin_set2(s, v):
    """Return a set containing all elements of s and element v.

    >>> adjoin_set2(s, 2.5)
    Rlist(1, Rlist(2, Rlist(2.5, Rlist(3))))
    >>> x=Rlist(3, Rlist(6, Rlist(8, Rlist(16, Rlist(20)))))
    >>> adjoin_set2(x, -6)
    Rlist(-6, Rlist(3, Rlist(6, Rlist(8, Rlist(16, Rlist(20))))))
    >>> adjoin_set2(x, 3)
    Rlist(3, Rlist(6, Rlist(8, Rlist(16, Rlist(20)))))
    >>> adjoin_set2(x, 5)
    Rlist(3, Rlist(5, Rlist(6, Rlist(8, Rlist(16, Rlist(20))))))
    >>> adjoin_set2(x, 8)
    Rlist(3, Rlist(6, Rlist(8, Rlist(16, Rlist(20)))))
    >>> adjoin_set2(x, 19)
    Rlist(3, Rlist(6, Rlist(8, Rlist(16, Rlist(19, Rlist(20))))))
    >>> adjoin_set2(x, 20)
    Rlist(3, Rlist(6, Rlist(8, Rlist(16, Rlist(20)))))
    >>> adjoin_set2(x, 34)
    Rlist(3, Rlist(6, Rlist(8, Rlist(16, Rlist(20, Rlist(34))))))
    """
    if empty(s) or s.first > v:
        return Rlist(v, s)
    elif set_contains2(s,v):
        return s
    return Rlist(s.first, adjoin_set2(s.rest,v))

# Q2.

def union_set2(set1, set2):
    """Return a set containing all elements either in set1 or set2.

    >>> t = Rlist(1, Rlist(3, Rlist(5)))
    >>> union_set2(s, t)
    Rlist(1, Rlist(2, Rlist(3, Rlist(5))))
    >>> a=Rlist(8, Rlist(12, Rlist(20, Rlist(21))))
    >>> b=Rlist(2, Rlist(4, Rlist(23, Rlist(25))))
    >>> c=Rlist(14, Rlist(15, Rlist(16, Rlist(20.5))))
    >>> union_set2(a, a)
    Rlist(8, Rlist(12, Rlist(20, Rlist(21))))
    >>> union_set2(a, b)
    Rlist(2, Rlist(4, Rlist(8, Rlist(12, Rlist(20, Rlist(21, Rlist(23, Rlist(25))))))))
    >>> union_set2(c, a)
    Rlist(8, Rlist(12, Rlist(14, Rlist(15, Rlist(16, Rlist(20, Rlist(20.5, Rlist(21))))))))
    """
    if empty(set1): return set2
    if empty(set2): return set1
    if not set_contains2(set1, set2.first):
        set1 = adjoin_set2(set1, set2.first)
    return union_set2(set1, set2.rest)

def set_contains3(s, v):
    """Return true if set s contains value v as an element.

    >>> t = Tree(2, Tree(1), Tree(3))
    >>> set_contains3(t, 3)
    True
    >>> set_contains3(t, 0)
    False
    >>> set_contains3(big_tree(20, 60), 34)
    True
    """
    if s is None:
        return False
    if s.entry == v:
        return True
    if s.entry < v:
        return set_contains3(s.right, v)
    if s.entry > v:
        return set_contains3(s.left, v)

def adjoin_set3(s, v):
    """Return a set containing all elements of s and element v.

    >>> b = big_tree(0, 9)
    >>> b
    Tree(4, Tree(1), Tree(7, None, Tree(9)))
    >>> adjoin_set3(b, 5)
    Tree(4, Tree(1), Tree(7, Tree(5), Tree(9)))
    """
    if s is None:
        return Tree(v)
    if s.entry == v:
        return s
    if s.entry < v:
        return Tree(s.entry, s.left, adjoin_set3(s.right, v))
    if s.entry > v:
        return Tree(s.entry, adjoin_set3(s.left, v), s.right)

# Q3.

def depth(s, v):
    """Return the depth of the value v in tree set s.

    The depth of a value is the number of branches followed to reach the value.
    The depth of the root of a tree is always 0.

    >>> b = big_tree(0, 9)
    >>> depth(b, 4)
    0
    >>> depth(b, 7)
    1
    >>> depth(b, 9)
    2
    >>> b = big_tree(0, 50)
    >>> depth(b, 9)
    4
    >>> depth(b, 11)
    1
    >>> depth(b, 23)
    4
    >>> depth(b, 25)
    0
    >>> depth(b, 28)
    3
    """
    if not set_contains3(s,v):
        return False
    if s is None or s.entry == v:
        return 0
    if s.entry < v:
        return 1 + depth(s.right, v)
    if s.entry > v:
        return 1 + depth(s.left, v)

# Q4.

def tree_to_list(s):
    values = []
    if s is None:
        values += []
    elif not s.left and not s.right:
        values += [s.entry]
    elif s.left and not s.right:
        values += tree_to_list(s.left) + [s.entry]
    elif not s.left and s.right:
        values += [s.entry] + tree_to_list(s.right)
    else:
        values += tree_to_list(s.left) + [s.entry] + tree_to_list(s.right)
    return values

def list_to_rlist(l):
    if not l:
        return Rlist.empty
    return Rlist(l[0], list_to_rlist(l[1:]))

def tree_to_ordered_sequence(s):
    """Return an ordered sequence containing the elements of tree set s.

    >>> b = big_tree(0, 9)
    >>> tree_to_ordered_sequence(b)
    Rlist(1, Rlist(4, Rlist(7, Rlist(9))))
    >>> b = big_tree(0, 50)
    >>> tree_to_ordered_sequence(b)
    Rlist(1, Rlist(4, Rlist(7, Rlist(9, Rlist(11, Rlist(14, Rlist(16, Rlist(18, Rlist(21, Rlist(23, Rlist(25, Rlist(28, Rlist(31, Rlist(34, Rlist(36, Rlist(38, Rlist(41, Rlist(43, Rlist(45, Rlist(48, Rlist(50)))))))))))))))))))))
    >>> b = Tree(1, None, Tree(2, None, Tree(3, None, Tree(4, None, Tree(5)))))
    >>> tree_to_ordered_sequence(b)
    Rlist(1, Rlist(2, Rlist(3, Rlist(4, Rlist(5)))))
    >>> b = Tree(1, Tree(0, Tree(-4, Tree(-7, Tree(-9)))))
    >>> tree_to_ordered_sequence(b)
    Rlist(-9, Rlist(-7, Rlist(-4, Rlist(0, Rlist(1)))))
    >>> b = Tree(1, Tree(0, Tree(-4, Tree(-7, Tree(-9)))), Tree(2, None, Tree(3, None, Tree(4, None, Tree(5)))))
    >>> tree_to_ordered_sequence(b)
    Rlist(-9, Rlist(-7, Rlist(-4, Rlist(0, Rlist(1, Rlist(2, Rlist(3, Rlist(4, Rlist(5)))))))))
    """
    if s is None:
        return Rlist.empty
    if not s.left and not s.right:
        return Rlist(s.entry)
    if not s.left and s.right:
        return Rlist(s.entry, tree_to_ordered_sequence(s.right))
    if s.left and not s.right:
        return extend_rlist(tree_to_ordered_sequence(s.left), Rlist(s.entry))
    else:
        right = Tree(s.entry, None, s.right)
        left_seq = tree_to_ordered_sequence(s.left)
        right_seq = tree_to_ordered_sequence(right)
        return extend_rlist(left_seq, right_seq)

# Q5.

def rlist_to_list(r):
    if r is Rlist.empty:
        return []
    return [r.first] + rlist_to_list(r.rest)

def partial_tree(s, n):
    """Return a balanced tree of the first n elements of Rlist s, along with
    the rest of s. A tree is balanced if the length of the path to any two
    leaves are at most one apart.

    >>> s = Rlist(1, Rlist(2, Rlist(3, Rlist(4, Rlist(5)))))
    >>> partial_tree(s, 3)
    (Tree(2, Tree(1), Tree(3)), Rlist(4, Rlist(5)))
    >>> x=Rlist(9, Rlist(14, Rlist(16, Rlist(27, Rlist(35, Rlist(42, Rlist(44, Rlist(48, Rlist(52, Rlist(57, Rlist(60, Rlist(65))))))))))))
    >>> partial_tree(x, 2)
    (Tree(9, None, Tree(14)), Rlist(16, Rlist(27, Rlist(35, Rlist(42, Rlist(44, Rlist(48, Rlist(52, Rlist(57, Rlist(60, Rlist(65)))))))))))
    >>> partial_tree(x, 12)[0]
    Tree(42, Tree(16, Tree(9, None, Tree(14)), Tree(27, None, Tree(35))), Tree(52, Tree(44, None, Tree(48)), Tree(60, Tree(57), Tree(65))))
    """
    if n == 0:
        return None, s
    left_size = (n-1)//2
    right_size = n - left_size - 1
    left, rest = partial_tree(s, left_size), s
    for _ in range(n):
        rest = rest.rest
    entry, right = left[1].first, partial_tree(left[1].rest, right_size)
    return Tree(entry, left[0], right[0]), rest

def ordered_sequence_to_tree(s):
    """Return a balanced tree containing the elements of ordered Rlist s.

    A tree is balanced if the lengths of the paths from the root to any two
    leaves are at most one apart.

    Note: this implementation is complete, but the definition of partial_tree
    below is not complete.

    >>> ordered_sequence_to_tree(Rlist(1, Rlist(2, Rlist(3))))
    Tree(2, Tree(1), Tree(3))
    >>> b = big_tree(0, 20)
    >>> elements = tree_to_ordered_sequence(b)
    >>> elements
    Rlist(1, Rlist(4, Rlist(7, Rlist(10, Rlist(13, Rlist(16, Rlist(19)))))))
    >>> ordered_sequence_to_tree(elements)
    Tree(10, Tree(4, Tree(1), Tree(7)), Tree(16, Tree(13), Tree(19)))
    """
    return partial_tree(s, len(s))[0]


def intersect_set3(set1, set2):
    """Return a set containing all elements common to set1 and set2.


    >>> s, t = big_tree(0, 12), big_tree(6, 18)
    >>> intersect_set3(s, t)
    Tree(8, Tree(6), Tree(10, None, Tree(12)))
    """
    s1, s2 = map(tree_to_ordered_sequence, (set1, set2))
    return ordered_sequence_to_tree(intersect_set2(s1, s2))

def union_set3(set1, set2):
    """Return a set containing all elements either in set1 or set2.

    >>> s, t = big_tree(6, 12), big_tree(10, 16)
    >>> union_set3(s, t)
    Tree(10, Tree(6, None, Tree(9)), Tree(13, Tree(11), Tree(15)))
    """
    s1, s2 = map(tree_to_ordered_sequence, (set1, set2))
    return ordered_sequence_to_tree(union_set2(s1, s2))


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

def extend_rlist(s1, s2):
    """Return a list containing the elements of s1 followed by those of s2."""
    if s1 is Rlist.empty:
        return s2
    return Rlist(s1.first, extend_rlist(s1.rest, s2))

def map_rlist(s, fn):
    """Return a list resulting from mapping fn over the elements of s."""
    if s is Rlist.empty:
        return s
    return Rlist(fn(s.first), map_rlist(s.rest, fn))

def filter_rlist(s, fn):
    """Filter the elemenst of s by predicate fn."""
    if s is Rlist.empty:
        return s
    rest = filter_rlist(s.rest, fn)
    if fn(s.first):
        return Rlist(s.first, rest)
    return rest

s = Rlist(1, Rlist(2, Rlist(3))) # A set is an Rlist with no duplicates
t = Rlist(1, Rlist(2, Rlist(2, Rlist(3))))
u = Rlist.empty

class Tree(object):
    """A tree with internal values."""

    def __init__(self, entry, left=None, right=None):
        self.entry = entry
        self.left = left
        self.right = right

    def __repr__(self):
        args = repr(self.entry)
        if self.left or self.right:
            args += ', {0}, {1}'.format(repr(self.left), repr(self.right))
        return 'Tree({0})'.format(args)

def big_tree(left, right):
    """Return a tree of elements between left and right.

    >>> big_tree(0, 12)
    Tree(6, Tree(2, Tree(0), Tree(4)), Tree(10, Tree(8), Tree(12)))
    """
    if left > right:
        return None
    split = left + (right - left)//2
    return Tree(split, big_tree(left, split-2), big_tree(split+2, right))
