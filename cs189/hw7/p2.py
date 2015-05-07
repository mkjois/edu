import numpy as np
from math import isnan
from scipy.io import loadmat
from sklearn.neighbors import NearestNeighbors

TRAINFILE = 'joke_data/joke_train.mat'
VALIDFILE = 'joke_data/validation.txt'
QUERYFILE = 'joke_data/query.txt'
RESULTFILE = 'kaggle_submission.txt'
KRANGE = [1000]

def warmup1(R, valid):
    correct = 0
    for user, joke, rating in valid:
        user -= 1; joke -= 1
        avg = np.nanmean(R[:,joke])
        if int(avg > 0) == rating:
            correct += 1
    acc = float(correct) / valid.shape[0]
    print('Naive rating accuracy: %.6f' % acc)

def warmup2(R, valid, k, query, kaggle):
    nbrs = NearestNeighbors(n_neighbors=k+1, algorithm='ball_tree').fit(R)
    distances, indices = nbrs.kneighbors(R)
    indices = indices[:, 1:]
    correct = 0
    for user, joke, rating in valid:
        user -= 1; joke -= 1
        nn = R[indices[user]]
        avg = np.mean(nn[:,joke])
        if int(avg > 0) == rating:
            correct += 1
    acc = float(correct) / valid.shape[0]
    print('%d-NN rating accuracy: %.6f' % (k, acc))

    if kaggle:
        with open(RESULTFILE, 'w') as f:
            f.write('Id,Category')
            for uid, user, joke in query:
                user -= 1; joke -= 1
                nn = R[indices[user]]
                avg = np.mean(nn[:,joke])
                f.write('\n%d,%d' % (uid, int(avg > 0)))

if __name__ == "__main__":
    trainset = loadmat(TRAINFILE)
    R = trainset['train'] # 24983 x 100
    valid, query = [], []
    with open(VALIDFILE) as f:
        for line in f:
            valid.append(map(int, line.split(',')))
    with open(QUERYFILE) as f:
        for line in f:
            query.append(map(int, line.split(',')))
    valid, query = np.array(valid), np.array(query)
    warmup1(R, valid)
    R[np.isnan(R)] = 0.0
    for k in KRANGE:
        warmup2(R, valid, k, query, k == KRANGE[-1])
