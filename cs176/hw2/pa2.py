#!/usr/bin/env python

# Name: Manny Jois
# Email: m.k.jois@berkeley.edu
# SID: 23808180
# Login:

""" Bowtie programming assignment
    Implement each of the functions below using the algorithms covered in class.
    You can construct additional functions and data structures but you should not
    change the functions' APIs.
"""

import sys # DO NOT EDIT THIS


def get_suffix_array(s):
    """
    Naive implementation of suffix array generation
    """
    return sorted(xrange(len(s)), key=lambda i: s[i:])  # Naive, 0-indexed


# Returns the ranks of the in-order suffixes
def karkkainen_sanders(s, ltype=str):
    output = [0] * len(s)

    # group the k-suffixes
    group_a, group_b = [], []
    for i in xrange(0, len(s), 3):
        group_a.append((s[i:i+3], i))
    for i in xrange(1, len(s), 3):
        group_a.append((s[i:i+3], i))
    for i in xrange(2, len(s), 3):
        group_b.append((s[i:i+3], i))

    # sort group A and get order (with possible duplicates)
    order = sorted(xrange(len(group_a)), key=lambda i: group_a[i][0])
    for i in xrange(1, len(order)):
        if group_a[order[i]][0] == group_a[order[i-1]][0]:
            order[i] = order[i-1]

    # get ranks of in-string-order k-suffixes (not i%3=0, i%3=1 order)
    ranks = [-1 for _ in group_a]
    mid = (len(order) + 1) / 2
    for i in xrange(len(order)):
        ri = order[i]
        ranks[ri * 2 if ri < mid else 2 * (ri - mid) + 1] = i

    # recursive KS on group A if ranks are not unique
    if -1 in ranks:
        ranks = karkkainen_sanders(order, ltype=int)

    def ks_group_a_map(index):
        kfix, si = group_a[index]
        next_ai = si_to_ai(si) + 1
        char = kfix[0]
        offset = 0
        rank = ranks[next_ai] if next_ai < len(ranks) else -1
        if si % 3 == 1:
            char = kfix[1] if len(kfix) > 1 else '$'
            offset = 1
        return (char, rank, offset, si)

    def ks_group_b_map(pair):
        kfix, si = pair
        ai1 = si_to_ai(si+1)
        ai2 = si_to_ai(si+2)
        rank1 = ranks[ai1] if ai1 < len(ranks) else -1
        rank2 = ranks[ai2] if ai2 < len(ranks) else -2
        char1 = kfix[0]
        char2 = kfix[1] if len(kfix) > 1 else '$'
        return (char1, rank1, char2, rank2, si)

    print(s)
    print(group_a)

    # map the the groups to <char, rank, index> tuples, then sort group B
    group_a = map(ks_group_a_map, order)
    group_b = sorted(map(ks_group_b_map, group_b))

    print(group_a)
    print(group_b)
    print(order)

    # merge groups A and B
    r = 0
    while len(group_a) > 0 and len(group_b) > 0:
        a_char, a_rank, offset, a_si = group_a[0]
        b_char1, b_rank1, b_char2, b_rank2, b_si = group_b[0]
        b_char, b_rank = b_char1, b_rank1
        if offset > 0:
            b_char, b_rank = b_char2, b_rank2
        print((a_char, a_rank, b_char, b_rank))
        if (a_char, a_rank) < (b_char, b_rank):
            group_a.pop(0)
            output[a_si] = r
        else:
            group_b.pop(0)
            output[b_si] = r
        r += 1
    for _, _, _, si in group_a:
        output[si] = r
        r += 1
    for _, _, _, _, si in group_b:
        output[si] = r
        r += 1

    return output


def si_to_ai(i):
    return i - i/3
def si_to_bi(i):
    return i/3
def ai_to_si(i):
    return i + i/2
def bi_to_si(i):
    return 3*i + 2


def getM(F):
    """
    Returns the helper data structure M (using the notation from class)
    """
    m = dict()
    for i in xrange(len(F)):
        char = F[i]
        if char not in m:
            m[char] = i
    return m



def getOCC(L):
    """
    Returns the OCC data structure such that OCC[c][k] is the number of times char c appeared in L[1], ..., L[k]
    """
    occ = dict()
    for i in xrange(len(L)):
        char = L[i]
        for c in occ:
            counts = occ[c]
            next_count = counts[i-1]
            if c == char:
                next_count += 1
            counts.append(next_count)
        if char not in occ:
            occ[char] = [0 for _ in xrange(i)] + [1]
    return occ



def bwt(s, SA):
    """
    Input:
        s = a string text
        SA = the suffix array of s

    Output:
        BWT of s as a string

    """
    transformed = ''
    for index in SA:
        if index == 0:
            index = len(s)
        char = s[index - 1]
        transformed += char
    return transformed



def getF(L):
    """
    Input:
        L = bwt(s)

    Output:
        F column of bwt (sorted string of L)
    """
    return ''.join(sorted(L))



def exact_match(p, SA, L, F, M, occ):
    """
    Input:
        p = the pattern string
        SA = suffix array of some reference string s
        L = bwt(s)
        F = sorted(bwt(s))
        M, occ = buckets and repeats information used by sp, ep

    Output:
        The first aligned starting position of p in s (0-indexed)
    """
    alphabet = sorted(M.keys())
    last_char = p[len(p) - 1]
    index_of_next = alphabet.index(last_char) + 1
    sp = M[last_char]
    ep = len(F) - 1
    if index_of_next < len(alphabet):
        ep = M[alphabet[index_of_next]] - 1
    for i in xrange(2, len(p) + 1):
        next_char = p[len(p) - i]
        sp = M[next_char] + occ[next_char][sp - 1]
        ep = M[next_char] + occ[next_char][ep] - 1
        if sp > ep:
            break
    return False if sp > ep else min(map(lambda i: SA[i], xrange(sp, ep + 1)))



SUBS = {'a': 't', 't': 'a', 'c': 'g', 'g': 'c', 'A': 'T', 'T': 'A', 'C': 'G', 'G': 'C'}

def bowtie(SA, L, F, M, occ, p, q, k):
    """
    Input:
        SA = suffix array of some reference string s
        L = bwt(s)
        F = sorted(bwt(s))
        M, occ = buckets and repeats information used by sp, ep
        p = a string pattern
        q = a quality score array for p
        k = maximum number of backtracks

    Output:
        The first aligned starting position of p in s

    Notes:
        Only allow A<->T and G<->C mismatches
        Output should be 0-indexed
        If multiple matches are found, return the first

    Example:
        > S = 'GATTACA'
        > SA = get_suffix_array(S)
        > L = bwt(S)
        > F = getF(L)
        > M = getM(F)
        > occ = getOCC(L)
        > bowtie(SA, L, F, M, occ, 'AGA', [40, 15, 35], 2)
        4

    """
    alphabet = sorted(M.keys())
    last_char = p[len(p) - 1]
    if last_char not in F and SUBS[last_char] not in F:
        return False
    index_of_next = alphabet.index(last_char) + 1
    sp = M[last_char]
    ep = len(F) - 1
    if index_of_next < len(alphabet):
        ep = M[alphabet[index_of_next]] - 1

    bprefix = last_char
    bscore = q[len(q) - 1]
    bsp, bep = sp, ep

    while k >= 0:
        for i in xrange(2, len(p) + 1):
            next_char = p[len(p) - i]
            score = q[len(q) - i]
            sp = M[next_char] + occ[next_char][sp - 1]
            ep = M[next_char] + occ[next_char][ep] - 1
            if sp > ep:
                sp, ep = bsp, bep
                break
        k -= 1

    return (sp, ep)



if __name__ == '__main__':
    #print(karkkainen_sanders('aieubnoyctx$'))
    #print(karkkainen_sanders('abrakadabra$'))
    #print(karkkainen_sanders('ggggggggggg$'))

    s = 'gattaca$'
    sa = get_suffix_array(s)
    l = bwt(s, sa)
    f = getF(l)
    m = getM(f)
    occ = getOCC(l)
    print()
    print(s)
    print(sa)
    print(l)
    print(f)
    print(m)
    print(occ)
    print(bowtie(sa, l, f, m, occ, 'aga', [40, 15, 35], 2))
