#!/usr/bin/env python

from earley import parse
from grammar import G_org, G_unamb, G_fast

def parseAndPrintResult(G, i, incr):
    if parse(G, i, incr): print 'Parsed OK'
    else: print 'Syntax error'

# Profiling

import math, time

def run(G, inputs):
    # profiling counters
    points = 7
    counters = [0]*points  # Points is the number of instrumentation points
    def incr(i,k=1): 
        counters[i] = counters[i] + k
    # table to keep coutners from all runs
    inputLengths = [len(i) for i in inputs]
    table = dict([(k,[]) for k in inputLengths])
    counterTotals = []
    timeTotals = []
    #
    for i in inputs:
        # reset counters
        counters = [0]*len(counters)
        # run parser
        starttime = time.time()
        # should run it more than once
        parseAndPrintResult(G, i, incr)
        endtime = time.time()
        t = endtime - starttime
        # record time and counters
        table[len(i)] = [sum(counters),t]+counters
        counterTotals.append(sum(counters))
        timeTotals.append(t)
        
    print "\nin-size\tsum(cntrs)\ttime\tindividual-counters"
    for l in inputLengths:
        print l, "\t",
        for v in table[l]:
            print v, "\t",
        print

    steps = ((math.log(max(counterTotals)) - math.log(min(counterTotals))) / \
             (math.log(max(inputLengths)) - math.log(min(inputLengths))),)
    print "\nSteps (approx trend) = N^%.3f" % steps

    timefactor = ((math.log(max(timeTotals)) - math.log(min(timeTotals))) / \
            (math.log(max(inputLengths)) - math.log(min(inputLengths))),)
    print "Time  (approx trend) = N^%.3f" % timefactor

# inputs

inputs = (\
      # these first two are good for testing visualization
      'a*a',\
      'a*(a)',\
      'a+a-a',\
      'a*a-a--(a*a)+-b--c,'*6 +'a*a-a--(a*a)+-b--c',\
      'a*a-a--(a*a)+-b--c,'*12+'a*a-a--(a*a)+-b--c',\
      'a*a-a--(a*a)+-b--c,'*18+'a*a-a--(a*a)+-b--c',\
      'a*a-a--(a*a)+-b--c,'*24+'a*a-a--(a*a)+-b--c',\
      # you probably won't want to run the one below until you have a faster grammar
      'a*a-a--(a*a)+-b--c,'*2000+'a*a-a--(a*a)+-b--c'
    )

failing_inputs = (\
      # you can use these to see how failing parses look
      'a*a*',\
      'a*(*-a)',
    )

G_to_run = G_fast

if __name__ == '__main__':
    run(G_to_run,inputs)
    run(G_to_run,failing_inputs)
