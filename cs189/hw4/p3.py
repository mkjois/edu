import numpy as np
import matplotlib.pyplot as plt
from scipy.io import loadmat
from math import exp
from random import randint

def load(filename):
  data = loadmat(filename)
  Xtrain = data["Xtrain"] # 3450 x 57
  Ytrain = data["ytrain"] # 3450 x 1
  Xtest = data["Xtest"] # 1151 x 57
  return Xtrain, Ytrain, Xtest

# Standardized mean and variance
def preprocess1(data):
  mean = np.mean(data, axis=0)
  stdev = np.std(data, axis=0)
  return (data - mean) / stdev

# Logarithmic transform
def preprocess2(data):
  return np.log(data + 0.1)

# Binarize features
def preprocess3(data):
  return (data > 0).astype(int)

def mu(beta, X):
  return 1.0 / (1.0 + np.exp(-X.dot(beta)))

def mu_i(beta, x_i):
  return 1.0 / (1.0 + exp(-x_i.T.dot(beta)))

def grad_batch(beta, X, Y, lamb):
  gradient = 2*lamb * np.eye(beta.size).dot(beta) - X.T.dot(Y - mu(beta, X))
  return gradient

def grad_stoch(beta, X, Y, lamb):
  i = randint(0, Y.size-1)
  x_i, y_i = X[i,:], Y[i]
  gradient = 2*lamb * np.eye(beta.size).dot(beta) - (y_i - mu_i(beta, x_i)) * x_i
  return gradient

def loss(beta, X, Y, lamb):
  return lamb * beta.dot(beta) - (Y.dot(np.log(mu(beta, X))) + (1 - Y).dot(np.log(1 - mu(beta, X))))

step1 = lambda t: 0.000001
step2 = lambda t: 0.001
step3 = lambda t: 0.2 / t

if __name__ == "__main__":
  FILE = "spam.mat"
  Xtrain, Ytrain, Xtest = load(FILE)
  Ytrain = np.ndarray.flatten(Ytrain)
  Xtrain1 = preprocess1(Xtrain)
  Xtrain2 = preprocess2(Xtrain)
  Xtrain3 = preprocess3(Xtrain)
  Xtrain = Xtrain3 # change for different preprocessing

  LAMBDA = 1.0
  EPSILON = 0.1
  ITER = 5000
  delta = 10
  irange = np.arange(ITER, step=delta)

  preprocesses = [("Standardized", Xtrain1),
                  ("Logarithmic", Xtrain2),
                  ("Binary", Xtrain3)]
  func_pairs = [("Batch, constant", step1, grad_batch, 1),
                ("Stochastic, constant", step2, grad_stoch, 2),
                ("Stochastic, decreasing", step3, grad_stoch, 3)]
  data = dict(map(lambda strategy: (strategy[0], []), preprocesses))
  for name, step, grad, qnum in func_pairs:
    for pproc, X in preprocesses:
      beta = np.zeros(Xtrain.shape[1]) # initialize Beta to zero vector
      for t in range(ITER):
        if t % delta == 0:
          data[pproc].append(loss(beta, X, Ytrain, LAMBDA))
        eta = step(t+1)
        nabla = grad(beta, X, Ytrain, LAMBDA)
        beta -= eta * nabla
      # plot shit
      plt.plot(irange, data[pproc], "bo")
      plt.xlabel("GD Iterations")
      plt.ylabel("Loss (negative log-likelihood)")
      plt.title("%s preprocessing: %s step size" % (pproc, name))
      plt.savefig("images/p3_%d_%s.png" % (qnum, pproc), dpi=96)
      plt.cla()
      data[pproc] = []
