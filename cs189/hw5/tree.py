"""
Generic decision tree logic.
"""

import numpy as np
from scipy.stats import mode
from util import iterpoints

RANDOM_TEST_POINT = 0

class DecisionTree:
  def __init__(self, impurity, segmentor, stopping_conditions):
    self.impurity = impurity
    self.segmentor = segmentor
    self.stops = stopping_conditions
    self.root = None

  def train(self, data, labels):
    self.root = DecisionNode(None, None, None, None, 0)
    #print(labels)
    self.root.train(data, labels, self.impurity, self.segmentor, self.stops, set())

  def predict(self, data, debug=True):
    if self.root is None: raise Exception("Must train your tree first!")
    labels = []
    for i in range(data.shape[0]):
      if i == RANDOM_TEST_POINT:
        if debug:
          print("\nClassifying test point %d..." % i)
      point = data[i]
      node = self.root
      while not node.is_leaf():
        if i == RANDOM_TEST_POINT:
          feat = node.feature
          thresh = node.threshold
          lr = "<" if point[feat] < thresh else ">="
          if debug:
            print("feature %d %s %.4f" % (feat, lr, thresh))
        node = node.decide(point)
      decision = node.decide(point)
      labels.append(decision)
      if i == RANDOM_TEST_POINT:
        if debug:
          print("Classified as %d\n" % decision)
    return np.array(labels)

class DecisionNode:
  def __init__(self, feature, threshold, left, right, depth):
    self.feature = feature
    self.threshold = threshold
    self.left = left
    self.right = right
    self.depth = depth
    self.leaf = False
    self.label = None

  def is_leaf(self):
    return self.leaf

  def train(self, data, labels, impurity, segmentor, stops, used):
    #print("impurity of this node: %.4f" % impurity(labels))
    if impurity(labels) == 0:
      self.leaf = True
      self.label = labels[0]
      return

    for cond in stops:
      if cond(labels, self.depth):
        self.leaf = True
        self.label = mode(labels)[0][0]
        return

    optimal = segmentor(data, labels, impurity, used)
    self.feature = optimal["feature"]
    self.threshold = optimal["threshold"]
    ldata = optimal["left"]["data"]
    rdata = optimal["right"]["data"]
    llabels = optimal["left"]["labels"]
    rlabels = optimal["right"]["labels"]
    lleaf, rleaf = False, False
    #print("llabels.size = %d\tnum_ones = %d" % (llabels.size, np.sum(llabels)))
    #print("rlabels.size = %d\tnum_ones = %d" % (rlabels.size, np.sum(rlabels)))
    #print("feature: %d, threshold: %.4f" % (self.feature, self.threshold))

    if len(llabels) == 0 or len(rlabels) == 0:
      self.leaf = True
      if len(llabels) == 0:
        self.label = mode(rlabels)[0][0]
      else:
        self.label = mode(llabels)[0][0]
    else:
      self.left = DecisionNode(None, None, None, None, self.depth + 1)
      #print("Training left")
      self.left.train(ldata, llabels, impurity, segmentor, stops, used.union(set([self.feature])))

      self.right = DecisionNode(None, None, None, None, self.depth + 1)
      #print("Training right")
      self.right.train(rdata, rlabels, impurity, segmentor, stops, used.union(set([self.feature])))

  def decide(self, datum):
    if self.is_leaf():
      return self.label
    elif datum[self.feature] < self.threshold:
      return self.left
    return self.right

class RandomForest:
  def __init__(self, j, k, m, impurity, segmentor, stops):
    self.j = j # number of trees
    self.k = k # number of samples
    self.m = m # number of features
    self.impurity = impurity
    self.segmentor = segmentor
    self.stops = stops
    self.trees = []
    self.feat_subsets = dict()
    self.debug = dict()

  def train(self, data, labels):
    self.trees = []
    self.feat_subsets = dict()
    self.debug = dict(zip([i for i in range(data.shape[1])], [[0, []] for _ in range(data.shape[1])]))
    n, d = data.shape # number of points, features
    for i in range(self.j):
      tree = DecisionTree(self.impurity, self.segmentor, self.stops)
      point_indices = np.random.choice(n, self.k, replace=True)
      data_for_tree = data[point_indices]
      labels_for_tree = labels[point_indices]
      feat_indices = np.random.permutation(d)[:self.m]
      self.feat_subsets[i] = feat_indices
      data_for_tree = data_for_tree[:, feat_indices]
      tree.train(data_for_tree, labels_for_tree)
      top_feature = tree.root.feature
      top_thresh = tree.root.threshold
      self.debug[top_feature][0] += 1
      self.debug[top_feature][1].append(top_thresh)
      self.trees.append(tree)
      if i % 10 == 9: print("Trained %d trees" % (i+1))
    print("\n")
    for (feature, (num_trees, thresh_list)) in self.debug.items():
      avg_thresh = 0 if num_trees == 0 else sum(thresh_list) / float(num_trees)
      print("Feature %d split on %d roots at threshold %.4f" % (feature, num_trees, avg_thresh))
    print("\n")

  def predict(self, data):
    if len(self.trees) == 0: raise Exception("Must train your tree first!")
    all_labels = []
    for i in range(len(self.trees)):
      tree = self.trees[i]
      feat_indices = self.feat_subsets[i]
      according = tree.predict(data[:, feat_indices], False)
      all_labels.append(according)
    all_labels = np.array(all_labels)
    return mode(all_labels)[0]
