import numpy as np
import matplotlib.pyplot as plt
from numpy.linalg import norm, det
from scipy import io
from scipy.stats import multivariate_normal
from math import log

TRAIN_DIGITS = True
TRAIN_SPAM = False

DIGIT_TRAIN_SET = "data/digit-dataset/train.mat"
DIGIT_TEST_SET = "data/digit-dataset/test.mat"
DIGIT_KAGGLE_SET = "data/digit-dataset/kaggle.mat"
DIGIT_RESULTS = "digit_results.csv"
SPAM_DATASET = "data/spam-dataset/spam_data.mat"
SPAM_RESULTS = "spam_results.csv"

def process_digits():
  FEATURED_LABEL = 5
  NUM_TRAINING = [100, 200, 500, 1000, 2000, 5000, 10000, 30000, 60000]
  OUTFILE = "p4out.txt"
  OUTSTR = ""
  shared_acc = []
  own_acc = []

  train_data = io.loadmat(DIGIT_TRAIN_SET)
  labels = train_data["train_label"] # N x 1
  images = train_data["train_image"] # 28 x 28 x N
  test_data = io.loadmat(DIGIT_TEST_SET)
  test_labels = test_data["test_label"] # N x 1
  test_images = test_data["test_image"] # 28 x 28 x N
  k_data = io.loadmat(DIGIT_KAGGLE_SET)
  k_images = k_data["kaggle_image"] # 28 x 28 x N
  L, W, N = images.shape
  images = images.reshape((L*W, N)) # 784 x N
  L_test, W_test, N_test = test_images.shape
  test_images = test_images.reshape((L_test * W_test, N_test))
  L_k, W_k, N_k = k_images.shape
  k_images = k_images.reshape((L_k * W_k, N_k))
  # images[:,k] gives image #k

  def normalize(image):
    l2 = norm(image)
    return image if l2 == 0 else image / l2
  #images = np.apply_along_axis(normalize, 1, images)
  #test_images = np.apply_along_axis(normalize, 1, test_images)
  #k_images = np.apply_along_axis(normalize, 1, k_images)

  for n in NUM_TRAINING:
    indices = np.random.permutation(labels.size)[:n]
    data = dict()
    for i in indices:
      label = labels[i][0]
      image = images[:,i]
      if label not in data:
        data[label] = dict([("images", []), ("prior", 0)])
      data[label]["images"].append(image)
      data[label]["prior"] += 1
    avg_cov = np.zeros(L*L*W*W).reshape((L*W, L*W))
    OUTSTR += "\nPrior probabilities:\n\n"
    for label in data:
      data[label]["prior"] = float(data[label]["prior"]) / n
      OUTSTR += "Label: %d\tP(Y=%d) = %.4f\n" % (label, label, data[label]["prior"])
      data[label]["images"] = np.array(data[label]["images"])
      data[label]["mu"] = np.mean(data[label]["images"], axis=0) # 784 x 1
      data[label]["cov"] = np.cov(data[label]["images"], rowvar=0) # 784 x 784
      #data[label]["cov"] = np.cov(data[label]["images"], rowvar=0) + .01 * np.diag(np.ones(L*W))# 784 x 784
      avg_cov += data[label]["prior"] * data[label]["cov"]
    for label in data:
      data[label]["shared"] = multivariate_normal(data[label]["mu"], avg_cov)
      data[label]["own"] = multivariate_normal(data[label]["mu"], data[label]["cov"])

    # classification
    score = 0.0
    score_own = 0.0
    digits = data.keys()
    for i in range(test_labels.size):
      true_label = int(test_labels[i][0])
      image = test_images[:,i]
      def mle(digit):
        return data[digit]["shared"].logpdf(image) + log(data[digit]["prior"])
      def mle_own(digit):
        return data[digit]["own"].logpdf(image) + log(data[digit]["prior"])
      prediction = max(digits, key=mle)
      prediction_own = max(digits, key=mle_own)
      if prediction == true_label:
        score += 1.0
      if prediction_own == true_label:
        score_own += 1.0
    acc = score / test_labels.size
    acc_own = score_own / test_labels.size
    shared_acc.append(acc)
    own_acc.append(acc_own)
    #print("Accuracy = %.4f" % acc)

    # kaggle
    """
    if n == NUM_TRAINING[-1]:
      with open(DIGIT_RESULTS, "w") as f:
        f.write("Id,Category")
        digits = data.keys()
        for i in range(k_images.shape[1]):
          image = k_images[:,i]
          def mle(digit):
            #return data[digit]["own"].logpdf(image) + log(data[digit]["prior"])
            return data[digit]["shared"].logpdf(image) + log(data[digit]["prior"])
          prediction = max(digits, key=mle)
          f.write("\n%d,%d" % (i+1, prediction))
    """

  fig = plt.figure()
  axes = fig.add_subplot(1,1,1)
  axes.plot(NUM_TRAINING, 1.0 - np.array(shared_acc), 'bo', clip_on=False)
  axes.set_xscale('log')
  axes.margins(0.1)
  plt.ylabel("Error rate")
  plt.xlabel("Size of training set")
  plt.title("Validation error vs. training set size: shared covariance")
  plt.savefig("images/p4d1.png", dpi=96)
  axes.cla()
  axes.plot(NUM_TRAINING, 1.0 - np.array(own_acc), 'ro', clip_on=False)
  axes.set_xscale('log')
  axes.margins(0.1)
  plt.ylabel("Error rate")
  plt.xlabel("Size of training set")
  plt.title("Validation error vs. training set size: own covariance")
  plt.savefig("images/p4d2.png", dpi=96)

  #with open(OUTFILE, "w") as f:
  #  f.write(OUTSTR)

def process_spam():
  s_data = io.loadmat(SPAM_DATASET)
  labels = s_data["training_labels"] # 1 x N
  emails = s_data["training_data"] # N x 32
  k_emails = s_data["test_data"] # M x 32
  N, P = emails.shape
  NUM_TRAINING = [N/2, N]

  def normalize(email):
    l2 = norm(email)
    return email if l2 == 0 else email / l2
  #emails = np.apply_along_axis(normalize, 0, emails)
  #k_emails = np.apply_along_axis(normalize, 0, k_emails)

  for n in NUM_TRAINING:
    indices = np.random.permutation(labels.size)
    train_indices = indices[:n]
    test_indices = indices[n:]
    data = dict()
    for i in train_indices:
      label = labels[0][i]
      email = emails[i,:]
      if label not in data:
        data[label] = dict([("emails", []), ("prior", 0)])
      data[label]["emails"].append(email)
      data[label]["prior"] += 1
    avg_cov = np.zeros(P*P).reshape((P, P))
    for label in data:
      data[label]["prior"] = float(data[label]["prior"]) / n
      data[label]["emails"] = np.array(data[label]["emails"]) # 2500-ish x 32
      data[label]["mu"] = np.mean(data[label]["emails"], axis=0) # 32 x 1
      data[label]["cov"] = np.cov(data[label]["emails"], rowvar=0) # 32 x 32
      #data[label]["cov"] = np.cov(data[label]["emails"], rowvar=0) + .01 * np.diag(np.ones(P))# 32 x 32
      avg_cov += data[label]["prior"] * data[label]["cov"]
    for label in data:
      data[label]["shared"] = multivariate_normal(data[label]["mu"], avg_cov)
      data[label]["own"] = multivariate_normal(data[label]["mu"], data[label]["cov"])

    # classification
    score = 0.0
    digits = data.keys()
    for i in test_indices:
      true_label = int(labels[0][i])
      email = emails[i,:]
      def mle(digit):
        #return data[digit]["own"].logpdf(email) + log(data[digit]["prior"])
        return data[digit]["shared"].logpdf(email) + log(data[digit]["prior"])
      prediction = max(digits, key=mle)
      if prediction == true_label:
        score += 1.0
    print("Accuracy = %.4f" % (score / n))

    # kaggle
    if n == NUM_TRAINING[-1]:
      with open(SPAM_RESULTS, "w") as f:
        f.write("Id,Category")
        digits = data.keys()
        for i in range(k_emails.shape[0]):
          email = k_emails[i,:]
          def mle(digit):
            #return data[digit]["own"].logpdf(email) + log(data[digit]["prior"])
            return data[digit]["shared"].logpdf(email) + log(data[digit]["prior"])
          prediction = max(digits, key=mle)
          f.write("\n%d,%d" % (i+1, prediction))


if __name__ == "__main__":
  if TRAIN_DIGITS: process_digits()
  if TRAIN_SPAM: process_spam()
