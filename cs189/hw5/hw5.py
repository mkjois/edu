import numpy as np
from scipy.io import loadmat
from math import sqrt
from tree import *
from util import *

if __name__ == "__main__":
  DATAFILE = "./spam-dataset/spam_data.mat"
  OUTFILE = "./results.csv"

  data = loadmat(DATAFILE)
  train_emails = data["training_data"]    # 5172 x 32
  train_labels = data["training_labels"]  # 1 x 5172
  train_labels = train_labels[0,:]
  test_emails = data["test_data"]         # 5857 x 32

  imp = Impurity.entropy
  seg = Segmentor.mean_of_means
  stops = [Stop.depth(9), Stop.proportion(0.95), Stop.cardinality(8)]

  N = int(0.9 * train_labels.size)
  accs, TRIALS = [], 1
  for _ in range(TRIALS):
    indices = np.random.permutation(train_labels.size)
    train_indices = indices[:N]
    valid_indices = indices[N:]

    tree = DecisionTree(imp, seg, stops)
    tree.train(train_emails[train_indices, :], train_labels[train_indices])
    validated = tree.predict(train_emails[valid_indices, :])
    acc = np.sum(np.equal(validated, train_labels[valid_indices])) / float(train_labels.size - N)
    accs.append(acc)
  print("Tree accuracy over %d trials:\t%.6f" % (TRIALS, sum(accs) / TRIALS))

  #J, K, M = 150, 1500, int(sqrt(train_emails.shape[1])) # number of trees, samples, features
  J, K, M = 100, 3000, 20 # number of trees, samples, features
  accs, TRIALS = [], 1
  forest = None
  for _ in range(TRIALS):
    indices = np.random.permutation(train_labels.size)
    train_indices = indices[:N]
    valid_indices = indices[N:]

    forest = RandomForest(J, K, M, imp, seg, stops)
    forest.train(train_emails[train_indices, :], train_labels[train_indices])
    validated = forest.predict(train_emails[valid_indices, :])
    acc = np.sum(np.equal(validated, train_labels[valid_indices])) / float(train_labels.size - N)
    accs.append(acc)
  print("Forest accuracy over %d trials:\t%.6f" % (TRIALS, sum(accs) / TRIALS))

  tree = DecisionTree(imp, seg, stops)
  tree.train(train_emails, train_labels)
  print("Done training!")
  predicted = tree.predict(test_emails)

  """
  forest.train(train_emails, train_labels)
  print("Done training!")
  predicted = forest.predict(test_emails)
  """

  with open(OUTFILE, "w") as f:
    f.write("Id,Category")
    i = 1
    for label in np.nditer(predicted):
      f.write("\n%d,%d" % (i, label))
      i += 1
