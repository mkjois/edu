import numpy as np
import matplotlib.pyplot as plt

def linear_regression(csvfile, ntrain, ntest, use_offset):
  data = np.loadtxt(csvfile, delimiter=",")
  train_set, test_set = None, None

  if use_offset: # WITH an offset coefficient B_0
    train_set = np.c_[data[:NTRAIN,:], np.ones(NTRAIN)]
    test_set = np.c_[data[NTRAIN:,:], np.ones(NTEST)]
  else: # WITHOUT an offset coefficient B_0
    train_set = data[:NTRAIN,:]
    test_set = data[NTRAIN:,:]

  X = train_set[:, 1:] # design matrix
  Y = train_set[:, 0] # target vector
  A = X.T.dot(X)
  B = X.T.dot(Y)
  beta = np.linalg.solve(A, B) # solve X'XB = X'y

  Xtest = test_set[:, 1:]
  Ytest = test_set[:, 0]
  Ypredict = Xtest.dot(beta)
  residuals = 0.5 * np.square(Ypredict - Ytest)
  rss = sum(residuals)
  minyear = np.amin(Ypredict)
  maxyear = np.amax(Ypredict)
  return beta, rss, minyear, maxyear

if __name__ == "__main__":
  DATAFILE = "songs.csv"
  NTRAIN, NTEST = 463715, 51630
  OFFSET = True
  PLOTFILE = "images/p2.png"
  beta, rss, minyear, maxyear = linear_regression(DATAFILE, NTRAIN, NTEST, OFFSET)
  print("RSS:\t%f" % rss)
  print("Min:\t%f" % minyear)
  print("Max:\t%f" % maxyear)
  print("Offset:\t%f" % beta[beta.size-1])

  plt.stem(np.arange(1, beta.size), beta[:beta.size-1], linefmt="b-", markerfmt="bo", basefmt="r-")
  plt.xlabel("Coefficient b_i of Beta")
  plt.ylabel("Value of coefficient")
  plt.title("Coefficients of Beta")
  plt.savefig(PLOTFILE, dpi=96)
