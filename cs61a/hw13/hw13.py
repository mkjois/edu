"""Homework 13 -- Vote for your favorite Project 4 Contest entry."""

def featherweight():
    """Return the integer number of your favorite featherweight entry.

    >>> isinstance(featherweight(), int)
    True
    >>> 0 <= featherweight() <= 32
    True
    """
    return 6  # Change to a featherweight entry number

def heavyweight():
    """Return the integer number of your favorite heavyweight entry.

    >>> isinstance(heavyweight(), int)
    True
    >>> 33 <= heavyweight() <= 42
    True
    """
    return 38  # Change to a heavyweight entry number

# These lines will be used to tally your vote.  Please do not change them.
print(featherweight())
print(heavyweight())