#!/usr/bin/env python

import string
from collections import defaultdict 

# Homework: Make this parser run in linear time on the string
#                 'a+a*a-a(a*a+a), ... ,a+a*a-a(a*a+a)'
#           from the language
#                   S->E|S,S
#                   E->E+E|E-E|E*E|(E)|-E|V
#                   V->a|b|c

# Grammars are represented in a simple fashion, no encapsulation in a class:
#   - representation is efficient but not general, as tokens are single
#     characters:
#   - the grammar is a hash table mapping nonterminals to a tuple containing
#     productions
#   - each symbol (non-terminal and terminal) is a single ASCII symbol
#   - non-terminals are uppercase; the rest are terminals
#   - the start nonterminal is always called S
#
# Example: grammar S->S+S|(S)|a is represented with
# 
#   G={'S':('S+S','(S)','a')}
#

# Suggested edge representation: 
#  edge is a tuple (u:int,v:int,state:tuple),
#      u, v: source and destiantion of the edge, given as indices into input
#            string.
#      state: production with a dot; see lecture notes for description of Earley
#             algorithm
#  state is a tuple (LHS:string,RHS,:string,position-of-dot:int)
#      LHS: nonterminal on left-hand side of a production
#      RHS: right-hand side of production
#      position-of-dot: where the dot is in the RHS, ranges from 0 to len[RHS],
#                       inclusive

# Set to True if you want to generate graphviz file.
visualize = False

# The Earley algorithm

def parse(grammar, inp, incr):

    if visualize:
        # write a graph that can be visualized with graphviz.
        # to view the graph, use the command $ dotty graph.dot
        gviz = open('graph.dot','w')
        gviz.write('digraph G {\nrankdir="LR";')
        for i in range(0,len(inp)):
            gviz.write('    %s -> %s [label = "%s",width=2];\n' % (i, i+1, inp[i]))

        def drawEdge(e):
            dot = e[2][2]
            rhs = e[2][1][0:dot]+'.'+e[2][1][dot:]
            gviz.write('    %s -> %s [label="%s --> %s", constraint=false];\n' % \
                           (e[0],e[1],e[2][0],rhs))


    # The graph is partioned by (destination,completenessStatus) of edges.
    # The graph is thus a dictionary with the key (dst,complete).
    # Each key maps to a pair consisting of a list of edges as well as a set of
    # edges. Both the list and the set keep the same set of edges.  We use both
    # so that we can modify the set of edges while iterating over it.  While a
    # list data structure can be modified during iteration, it cannot be tested
    # for membership as fast as the set. Hence we use it together with a set.
    # See the pattern below for illustration of how we use the set and list in
    # tandem.
    #
    # List+Set pattern:
    #
    # iterate over a list but use a set to determine
    # constant-time membership in the (changing) list
    # 
    # lst = [1,2]
    # s = set(lst)
    # for v in lst:
    #     print v
    #     if (3 not in s):
    #         lst.append(3)
    #         s.add(3)
    
    # defaultdict is like a dictionary that provides a default value when key
    # is not present
    graph = defaultdict(lambda: ([],set()))

    # status of edge is either complete or inProgress
    complete = 0
    inProgress = 1

    # return (list,set) of edges 
    def edgesIncomingTo(dst,status):
        key = (dst,status)
        return graph[key]
    def addEdge(e):
        """Add edge to graph and worklist if not present in graph already.
        Return True iff the edge was actually inserted
        """
        incr(0)
        # edge to key
        (src,dst,(N,RHS,pos)) = e
        status = complete if len(RHS)==pos else inProgress
        (edgeList,edgeSet) = edgesIncomingTo(dst,status)
        if e not in edgeSet:
            edgeList.append(e)
            edgeSet.add(e)
            # add the edge to the Dotty graph
            if visualize:
                drawEdge(e)
            return True
        return False
            
    # Add edge (0,0,(S -> . alpha)) to worklist, for all S -> alpha
    for P in grammar['S']:
        addEdge((0,0,('S',P,0)))
    
    ambiguous = False

    # for all tokens on the input
    for j in xrange(0,len(inp)+1):

        # skip in first iteration; we need to complete and predict the
        # start nonterminal S before we start advancing over the input
        if j > 0:
            # ADVANCE across the next token:
            # for each edge (i,j-1,N -> alpha . inp[j] beta)
            #     add edge (i,j,N -> alpha inp[j] . beta)
            for (i,_j,(N,RHS,pos)) in edgesIncomingTo(j-1,inProgress)[0]:
                incr(1)
                assert _j==j-1
                if pos < len(RHS) and RHS[pos]==inp[j-1]:
                    addEdge((i,j,(N,RHS,pos+1)))

        # Repeat COMPLETE and PREDICT until no more dges can be added
        edgeWasInserted = True
        while edgeWasInserted:
            edgeWasInserted = False
            # COMPLETE productions
            # for each edge (i,j,N -> alpha .)
            #    for each edge (k,i,M -> beta . N gamma)
            #        add edge (k,j,M -> beta N . gamma)
            eSet = set()
            for (i,_j,(N,RHS,pos)) in edgesIncomingTo(j,complete)[0]:
                assert _j==j and pos == len(RHS)
                for (k,_i,(M,RHS2,pos2)) in edgesIncomingTo(i,inProgress)[0]:
                    incr(2)
                    if RHS2[pos2]==N:
                        uniqueEdge = addEdge((k,j,(M,RHS2,pos2+1)))
                        if len(RHS2) == pos2+1:
                          #print((k,j,(M,RHS2,pos2+1)))
                          if (k,j,M) in eSet:
                            ambiguous = True
                          eSet.add((k,j,M))
                        edgeWasInserted = uniqueEdge or edgeWasInserted

            # PREDICT what the parser is to see on input (move dots in edges 
            # that are in progress)
            #
            # for each edge (i,j,N -> alpha . M beta)
            #     for each production M -> gamma
            #          add edge (j,j,M -> . gamma)
            for (i,_j,(N,RHS,pos)) in edgesIncomingTo(j,inProgress)[0]:
                incr(3)
                # non-terminals are upper case
                if RHS[pos] in string.ascii_uppercase:
                    M = RHS[pos]
                    # prediction: for all rules D->alpha add edge (j,j,.alpha)
                    for RHS in grammar[M]:
                        incr(4)
                        edgeWasInserted =  addEdge((j,j,(M,RHS,0))) or \
                                           edgeWasInserted
        
    if ambiguous:
        print("Ambiguous")

    if visualize:
        gviz.write('}')
        gviz.close()
    
    # input has been parsed OK if and only if an edge (0,n,S -> alpha .) exists
    for RHS in grammar['S']:
        if (0,len(inp),('S',RHS,len(RHS))) in \
           edgesIncomingTo(len(inp),complete)[1]:
            return True
    return False
