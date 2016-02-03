#!/usr/bin/env python

import sys

DELTA = [
[  0,  32,  48,  51,  50,  48,  98, 148],
[ 32,   0,  26,  34,  29,  33,  84, 136],
[ 48,  26,   0,  42,  44,  44,  92, 152],
[ 51,  34,  42,   0,  44,  38,  86, 142],
[ 50,  29,  44,  44,   0,  24,  89, 142],
[ 48,  33,  44,  38,  24,   0,  90, 142],
[ 98,  84,  92,  86,  89,  90,   0, 148],
[148, 136, 152, 142, 142, 142, 148,   0],
]

for i in range(len(DELTA)):
    for j in range(len(DELTA[i])):
        DELTA[i][j] = float(DELTA[i][j])

def merge(delta):
    old_L, new_L = len(delta), len(delta)-1
    r = compute_r(delta)
    q = compute_q(delta, r)
    (a, b), minq = min_q(q) # a will be new node k
    d_ab = delta[a][b]
    new_delta = []
    for i in xrange(old_L):
        if i == b:
            continue
        row = []
        for j in xrange(old_L):
            if j == b:
                continue
            if i == a and j == a:
                row.append(0)
            elif i == a:
                row.append((delta[a][j] + delta[b][j] - d_ab) / 2)
            elif j == a:
                row.append((delta[a][i] + delta[b][i] - d_ab) / 2)
            else:
                row.append(delta[i][j])
        new_delta.append(row)
    wa = (d_ab + r[a] - r[b]) / 2
    wb = d_ab - wa
    print('merging %d and %d with weights %8f and %8f' % (a, b, wa, wb))
    return new_delta

def compute_r(delta):
    L = len(delta)
    return [sum(delta[i]) / (L-2) for i in xrange(L)]

def compute_q(delta, r):
    q = []
    for i in xrange(len(delta)):
        row = []
        for j in xrange(len(delta)):
            row.append(delta[i][j] - r[i] - r[j])
        q.append(row)
    return q

def min_q(q):
    best_indices = (-1, -1)
    best_q = float(1000000)
    for i in xrange(len(q)):
        for j in xrange(len(q)):
            if i == j:
                continue
            qval = q[i][j]
            if qval < best_q:
                best_indices = min(i, j), max(i, j)
                best_q = qval
    return best_indices, best_q

def pd(matrix):
    for row in matrix:
        print(row)

if __name__ == '__main__':
    pd(DELTA)
    d1 = merge(DELTA)
    pd(d1)
    d2 = merge(d1)
    pd(d2)
    d3 = merge(d2)
    pd(d3)
    d4 = merge(d3)
    pd(d4)
    d5 = merge(d4)
    pd(d5)
    d6 = merge(d5)
    pd(d6)
