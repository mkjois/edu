'''
Generates the test dataset

Randomizes ham and spam messages and rewrites them to a directory
Each time you run this file, it will produce a differing ordering

*** This file should not go out to students ***
'''

import glob
import os
import random
import re

BASE_DIR = './'
SPAM_DIR = 'spam_test/'
HAM_DIR = 'ham_test/'
OUTPUT_DIR = 'test/'

X = []
Y = []
newX = []
newY = []

# Get all filenames in spam directory
filenames = glob.glob(BASE_DIR + SPAM_DIR + '*.txt')
for filename in filenames:
    with open(filename) as f:
        text = f.read() # Read in text from file
        X.append(text)
        Y.append(1)

# Get all filenames in ham directory
filenames = glob.glob(BASE_DIR + HAM_DIR + '*.txt')
for filename in filenames:
    with open(filename) as f:
        text = f.read() # Read in text from file
        X.append(text)
        Y.append(0)

indices = [x for x in range(len(Y))]
random.shuffle(indices) # Shuffle

for idx in indices:
    newX.append(X[idx])
    newY.append(Y[idx])

X = newX
Y = newY

# Write results to file
os.makedirs(BASE_DIR + OUTPUT_DIR)

for i,x in enumerate(X):
    f = open(BASE_DIR + OUTPUT_DIR + str(i) + '.txt', 'w')
    f.write(x)
    f.close()

f = open(BASE_DIR + OUTPUT_DIR + 'spam_test_labels.txt', 'w')
for i,y in enumerate(Y):
    f.write(str(i+1) + ',' + str(y) + '\n')
f.close()

