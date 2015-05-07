"""
Collection of functions for impurity, thresholds, stopping conditions
"""

import numpy as np

def iterpoints(data):
  for point in data:
    yield point

def iterfeatures(data):
  return iterpoints(data.T)

class Impurity:

  @staticmethod
  def entropy(labels):
    N = labels.size
    if N == 0: return 5.0
    freq = np.unique(labels, return_counts=True)[1] / float(N)
    indivs = np.multiply(freq, np.log2(freq))
    return -np.sum(indivs)

class Segmentor:

  @staticmethod
  def mean_of_means(data, labels, impurity, used):
    class_to_data = dict()
    for label in np.nditer(np.unique(labels)):
      class_to_data[int(label)] = data[np.where(labels == int(label))]
    base_impure = impurity(labels)
    best_feat, best_thresh, best_gain = None, 0.0, -5.0
    best_data = {
      "feature": None,
      "threshold": None,
      "left": {
        "data": None,
        "labels": None
      },
      "right": {
        "data": None,
        "labels": None
      },
      "gain": -5.0
    }
    for i in range(data.shape[1]):
      #if i in used: continue
      thresh = 0.0
      nclasses = len(class_to_data)
      for data_by_class in class_to_data.values():
        m = np.mean(data_by_class[:,i])
        #print("mean (f: %i) is %.4f" % (i, m))
        thresh += m
      thresh /= float(nclasses)
      mn, mx = np.min(data[:,i]), np.max(data[:,i])
      threshs = np.linspace(mn, mx, num=11)
      threshs[10] = thresh
      for threshold in threshs:
        #print("thresh (f: %i) is %.4f" % (i, threshold))
        lcond, rcond = np.where(data[:,i] < threshold), np.where(data[:,i] >= threshold)
        ldata, rdata = data[lcond], data[rcond]
        llabels, rlabels = labels[lcond], labels[rcond]
        nlabels = labels.size
        limpure, rimpure = impurity(llabels), impurity(rlabels)
        gain = base_impure - (llabels.size * limpure + rlabels.size * rimpure) / nlabels
        if gain > best_data["gain"]:
          best_data["feature"] = i
          best_data["threshold"] = threshold
          best_data["gain"] = gain
          best_data["left"]["data"] = ldata
          best_data["left"]["labels"] = llabels
          best_data["right"]["data"] = rdata
          best_data["right"]["labels"] = rlabels
    return best_data

class Stop:

  @staticmethod
  def depth(max_depth):
    def f(labels, depth):
      return depth > max_depth
    return f

  @staticmethod
  def proportion(min_proportion):
    def f(labels, depth):
      freq = np.unique(labels, return_counts=True)[1] / float(labels.size)
      return not np.amax(freq) < min_proportion
    return f

  @staticmethod
  def cardinality(min_elements):
    def f(labels, depth):
      return labels.size < min_elements
    return f
