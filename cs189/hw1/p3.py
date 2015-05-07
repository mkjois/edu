import numpy as np
import matplotlib.pyplot as plt
from scipy import io
from sklearn import svm
from sklearn.metrics import confusion_matrix

TRAIN_SET = "data/digit-dataset/train.mat"
TEST_SET = "data/digit-dataset/test.mat"
TEST_RESULTS_FILE = "digits_results.csv"

NUM_TRAINING = 10000
NUM_VALIDATION = 10000
K = 10
#C_RANGE = np.array([10**i for i in range(-5,6)])
#C_RANGE = np.array([10**i for i in range(-8,-2)])
C_RANGE = np.linspace(10**-7, 10**-6, 10)

def main():
  train_data = io.loadmat(TRAIN_SET)
  labels = train_data["train_labels"]
  images = train_data["train_images"]
  # images[:,:,k] gives image #k
  best_c, score = cross_validate(images, labels, C_RANGE)
  print("\nBest C:\t%f" % best_c)
  print("Best Score:\t%f\n" % score)

  # train a new svm
  indices = np.random.permutation(labels.size)
  validation_indices = indices[:NUM_VALIDATION]
  validation_set_images = [np.ndarray.flatten(images[:,:,i]) for i in validation_indices]
  validation_set_labels = [labels[i][0] for i in validation_indices]
  validation_set = (validation_set_images, validation_set_labels)
  train_indices = indices[NUM_VALIDATION:]
  train_set_images = [np.ndarray.flatten(images[:,:,i]) for i in train_indices]
  train_set_labels = [labels[i][0] for i in train_indices]
  train_set = (train_set_images, train_set_labels)

  classifier, accuracy = classify(train_set, validation_set, best_c)
  print("Final validation error rate:\t%f" % (1.0 - accuracy))

  test_images = io.loadmat(TEST_SET)["test_images"]
  test_images = [np.ndarray.flatten(test_images[:,:,i]) for i in range(test_images.shape[2])]
  predictions = classifier.predict(test_images)
  with open(TEST_RESULTS_FILE, "w") as f:
    f.write("Id,Category\n")
    for i in range(len(predictions)):
      f.write(str(i+1) + "," + str(predictions[i]) + "\n")

def cross_validate(images, labels, c_range):
  def get_data(fold):
    imgs = []
    lbls = []
    for i in np.nditer(fold):
      imgs.append(np.ndarray.flatten(images[:,:,i]))
      lbls.append(labels[i][0])
    return (imgs, lbls)
  best_c, best_score = None, 0.0
  indices = np.random.permutation(labels.size)[:NUM_TRAINING]
  folds = np.split(indices, K)
  folds = map(get_data, folds)
  for c in np.nditer(c_range):
    scores = []
    for i in range(K):
      training_set_images = []
      training_set_labels = []
      validation_set = None
      for j in range(K):
        if j == i:
          validation_set = folds[j]
        else:
          training_set_images.extend(folds[j][0])
          training_set_labels.extend(folds[j][1])
      training_set = (training_set_images, training_set_labels)
      classifier, accuracy = classify(training_set, validation_set, c)
      scores.append(accuracy)
    avg_score = sum(scores) / len(scores)
    print("C: %f\tScore: %f" % (c, avg_score))
    if avg_score > best_score:
      best_score = avg_score
      best_c = c
  return best_c, best_score

def classify(training_set, validation_set, c):
  v_set_images = validation_set[0]
  v_set_labels = validation_set[1]

  t_set_images = training_set[0]
  t_set_labels = training_set[1]

  classifier = svm.LinearSVC(C=c)
  classifier.fit(t_set_images, t_set_labels)
  accuracy = classifier.score(v_set_images, v_set_labels)
  return classifier, accuracy

if __name__ == "__main__":
    main()
