import numpy as np
from scipy.io import loadmat
import matplotlib.pyplot as plt
import sys
from numbers import Number
import random
from multiprocessing import Pool
import itertools

from util import TanhActivation, SigmoidActivation
from util import MeanSquaredLoss, CrossEntropyLoss

CROSS_VALIDATION = False
TRAIN_FULL = True
PLOT_THINGS = True # TRAIN_FULL must also be True
TEST_SET = False

kwargs = {'loss': 'cross',
          'init': 'gaussian',
          'epsilon': 0.1,
          'batch': 1,
          'eta': 0.0005,
          'iter': 100000,
          'plot': PLOT_THINGS}

class NeuralNet:
    """A neural network with one hidden layer, input bias and hidden bias"""
    Activ = {'tanh': TanhActivation, 'sigmoid': SigmoidActivation}
    Loss = {'mse': MeanSquaredLoss, 'cross': CrossEntropyLoss}

    def __init__(self, i, h, o, **kwargs):
        self.i, self.h, self.o = i, h, o
        self.hypers(**kwargs)

    def hypers(self, **kwargs):
        epsilon = kwargs['epsilon'] if 'epsilon' in kwargs else 1.0
        epsilon = float(epsilon)
        # don't forget input bias and hidden bias nodes
        init =  kwargs['init'] if 'init' in kwargs else 'uniform'
        if init == 'gaussian':
            self.w1 = np.random.normal(0.0, epsilon, (self.i+1, self.h))
            self.w2 = np.random.normal(0.0, epsilon, (self.h+1, self.o))
        else: # default is uniform on [-epsilon, epsilon]
            self.w1 = np.random.uniform(-epsilon, epsilon, (self.i+1, self.h))
            self.w2 = np.random.uniform(-epsilon, epsilon, (self.h+1, self.o))
        self.hactiv = NeuralNet.Activ[kwargs['hactiv']]() \
            if 'hactiv' in kwargs and kwargs['hactiv'] in NeuralNet.Activ \
            else TanhActivation()
        self.oactiv = NeuralNet.Activ[kwargs['oactiv']]() \
            if 'oactiv' in kwargs and kwargs['oactiv'] in NeuralNet.Activ \
            else SigmoidActivation()
        # note that this is not instantiated until training
        self.loss = NeuralNet.Loss[kwargs['loss']] \
            if 'loss' in kwargs and kwargs['loss'] in NeuralNet.Loss \
            else MeanSquaredLoss
        if 'eta' in kwargs:
            self.eta = kwargs['eta']
            if isinstance(self.eta, Number):
                self.eta = lambda t: kwargs['eta']
        else:
            self.eta = lambda t: 0.001
        self.batch = kwargs['batch'] if 'batch' in kwargs else 1
        self.debug = kwargs['debug'] if 'debug' in kwargs else False
        self.iterations = kwargs['iter'] if 'iter' in kwargs else 10000
        self.plot = kwargs['plot'] if 'plot' in kwargs else False

    def train(self, images, labels, vimg=None, vlab=None):
        t, N = 0.0, labels.size
        indices = np.arange(N)
        losses, accs, iters = [], [], []
        while t < self.iterations:
            t += 1.0
            # used for mini-batch grad descent
            if self.batch != 'full':
                b = np.random.choice(indices, self.batch) # Let b = self.batch
            else:
                b = indices
            y = self.vectorize(labels[b]) # b x o
            if self.debug: print(str(y.shape) + "\t: y")
            loss = self.loss(y)

            # forward pass
            # input bias feature
            x0 = np.c_[images[b], np.ones(b.size)] # b x (i+1)
            if self.debug: print(str(x0.shape) + "\t: x0")
            s1 = x0.dot(self.w1) # b x (i+1) . (i+1) x h = b x h
            if self.debug: print(str(s1.shape) + "\t: s1")
            # hidden bias
            x1 = np.c_[self.hactiv(s1), np.ones(b.size)] # b x (h+1)
            if self.debug: print(str(x1.shape) + "\t: x1")
            s2 = x1.dot(self.w2) # b x (h+1) . (h+1) x o = b x o
            if self.debug: print(str(s2.shape) + "\t: s2")
            x2 = self.oactiv(s2) # b x o
            if self.debug: print(str(x2.shape) + "\t: x2")

            # backward pass
            if isinstance(loss, CrossEntropyLoss) and isinstance(self.oactiv, SigmoidActivation):
                d2 = x2 - y # numerical optimization from cancelling out
            else:
                d2 = loss.grad(x2) * self.oactiv.grad(s2) # b x o * b x o = b x o
            if self.debug: print(str(d2.shape) + "\t: d2")
            grad_w2 = x1.T.dot(d2) # (h+1) x b . b x o = (h+1) x o

            # gradient descent
            self.w2 -= self.eta(t) * grad_w2

            if self.debug: print(str(grad_w2.shape) + "\t: g2")
            # d1 should be b x h
            d1 = d2.dot(self.w2[:-1,:].T) * self.hactiv.grad(s1)
            if self.debug: print(str(d1.shape) + "\t: d1")
            # grad_w1 should be (i+1) x h
            grad_w1 = x0.T.dot(d1) # (i+1) x b . b x h = (i+1) x h
            if self.debug: print(str(grad_w1.shape) + "\t: g1")

            # gradient descent
            self.w1 -= self.eta(t) * grad_w1

            if self.plot and vimg is not None and vlab is not None and (t+1) % 500 == 0 and self.batch == 1:
                lloss = self.loss(self.vectorize(labels))
                lx0 = np.c_[images, np.ones(images.shape[0])] # b x (i+1)
                ls1 = lx0.dot(self.w1) # b x (i+1) . (i+1) x h = b x h
                lx1 = np.c_[self.hactiv(ls1), np.ones(images.shape[0])] # b x (h+1)
                ls2 = lx1.dot(self.w2) # b x (h+1) . (h+1) x o = b x o
                lx2 = self.oactiv(ls2) # b x o
                iters.append(t+1)
                losses.append(lloss(lx2))
                p = self.predict(vimg)
                acc = float(np.sum(np.equal(p, vlab))) / vlab.size
                accs.append(acc)

        # plot shit
        if self.plot and vimg is not None and vlab is not None and self.batch == 1:
            plt.plot(iters, accs, 'bo')
            plt.ylabel("Accuracy")
            plt.xlabel("Iterations")
            plt.title("Validation accuracy vs. number of iterations")
            plt.savefig("images/accuracy_%s.png" % loss.name, dpi=96)
            plt.cla()
            plt.plot(iters, losses, 'bo')
            plt.ylabel("Loss")
            plt.xlabel("Iterations")
            plt.title("Training Loss vs. number of iterations")
            plt.savefig("images/loss_%s.png" % loss.name, dpi=96)

    def predict(self, images):
        images = np.c_[images, np.ones(images.shape[0])] # input bias feature
        # (h x i+1) x (i+1 x n) = (h x n)
        # column i is hidden layer values for image i
        hidden = self.w1.T.dot(images.T)
        hidden = self.hactiv(hidden)
        hidden = np.r_[hidden, [np.ones(hidden.shape[1])]] # hidden bias
        # (o x h+1) x (h+1 x n) = (o x n)
        # column j is output layer values for image j
        output = self.w2.T.dot(hidden)
        output = self.oactiv(output)
        predicted = np.argmax(output, axis=0)
        return predicted

    def vectorize(self, predicted):
        """
        Returns n x d matrix where row i is an image and the only nonzero
        column j is the predicted class.
        """
        vectors = np.zeros((predicted.size, self.o))
        vectors[np.arange(predicted.size), predicted] = 1.0
        return vectors

if __name__ == "__main__":
    TRAINFILE = 'digit-dataset/train.mat'
    TESTFILE = 'digit-dataset/test.mat'
    RESULTSFILE = 'results.csv'
    trainset = loadmat(TRAINFILE)
    testset = loadmat(TESTFILE)
    train_images = trainset['train_images']               # 28 x 28 x 60000
    dim, _, num = train_images.shape
    train_images = train_images.reshape(dim * dim, num).T # 60000 x 784
    train_labels = trainset['train_labels'][:,0]          # 60000 (numpy.uint8)
    test_images = testset['test_images']                  # 28 x 28 x 10000
    dim, _, num = test_images.shape
    test_images = test_images.reshape(dim * dim, num).T   # 10000 x 784

    N = train_labels.size
    I, H, O = train_images.shape[1], 200, 10

    print("\n" + str(kwargs))

    if CROSS_VALIDATION:
        def f(tup):
            i, timg, tlab, vimg, vlab = tup
            nn = NeuralNet(I, H, O, **kwargs)
            nn.train(timg, tlab)
            p = nn.predict(vimg)
            acc = float(np.sum(np.equal(p, vlab))) / vlab.size
            return "Set %d:\t%.4f" % (i+1, acc)
        pool = Pool()
        K = 10
        indices = range(N)
        random.shuffle(indices)
        print("\nCross validation accuracies with K = %d\n" % K)
        args = []
        for i in range(K):
            tind = indices[: i*N/K] + indices[(i+1)*N/K :]
            timg = train_images[tind]
            tlab = train_labels[tind]
            vind = indices[i*N/K : (i+1)*N/K]
            vimg = train_images[vind]
            vlab = train_labels[vind]
            args.append((i, timg, tlab, vimg, vlab))
        results = pool.map(f, args)
        for result in results:
            print(result)

    nn = NeuralNet(I, H, O, **kwargs)

    if TRAIN_FULL:
        NT = N * 7 / 8
        indices = range(N)
        random.shuffle(indices)
        tind, vind = indices[:NT], indices[NT:]
        timg, vimg = train_images[tind], train_images[vind]
        tlab, vlab = train_labels[tind], train_labels[vind]
        print("\nTraining on full training set...")
        if PLOT_THINGS:
            nn.train(timg, tlab, vimg, vlab)
        else:
            nn.train(timg, tlab)
        print("...finished training")
        p = nn.predict(vimg)
        acc = float(np.sum(np.equal(p, vlab))) / vlab.size
        print("Validation accuracy: %.4f\n" % acc)


    if TEST_SET:
        print("\nRunning predictions on test set...")
        p = nn.predict(test_images)
        with open(RESULTSFILE, 'w') as f:
            f.write('Id,Category')
            for i in range(p.size):
                f.write('\n%d,%d' % (i+1, p[i]))
        print("...finished writing results to %s\n" % RESULTSFILE)
