import numpy as np
import matplotlib.pyplot as plt
from scipy import io
from sklearn import svm
from sklearn.metrics import confusion_matrix

NUM_VALIDATION = 10000
TRAIN_SET_SIZES = [100, 200, 500, 1000, 2000, 5000, 10000]

def main():
  train_data = io.loadmat("data/digit-dataset/train.mat")
  labels = train_data["train_labels"]
  images = train_data["train_images"]
  # images[:,:,k] gives image #k
  scores = []
  print("Size\tAccuracy")
  for size in TRAIN_SET_SIZES:
    accuracy = classify(images, labels, size, False)
    scores.append(accuracy)
    print(str(size) + "\t" + str(accuracy))
  fig = plt.figure()
  axes = fig.add_subplot(1,1,1)
  axes.plot(TRAIN_SET_SIZES, 1.0 - np.array(scores), 'bo', clip_on=False)
  axes.set_xscale('log')
  axes.margins(0.1)
  plt.ylabel("Error rate")
  plt.xlabel("Size of training set")
  plt.title("Validation error vs. training set size")
  plt.savefig("images/error_rates.png", dpi=96)
  print("Error rate vs. training set size plot is saved as 'images/error_rates.png'")
  print("Confusion matrices are saved as 'images/confusion_matrix_N.png' for a training set of size N")

def classify(images, labels, train_size, showCM=False):
  indices = np.random.permutation(labels.size)
  validation_indices = indices[:NUM_VALIDATION]
  validation_set_images = [np.ndarray.flatten(images[:,:,i]) for i in validation_indices]
  validation_set_labels = [labels[i][0] for i in validation_indices]
  train_indices = indices[NUM_VALIDATION : NUM_VALIDATION + train_size]
  train_set_images = [np.ndarray.flatten(images[:,:,i]) for i in train_indices]
  train_set_labels = [labels[i][0] for i in train_indices]

  classifier = svm.LinearSVC()
  classifier.fit(train_set_images, train_set_labels)
  predicted_labels = classifier.predict(validation_set_images)
  num_correct = 0.0
  for i in range(len(predicted_labels)):
    if predicted_labels[i] == validation_set_labels[i]:
      num_correct += 1.0
  #accuracy = classifier.score(validation_set_images, validation_set_labels)
  accuracy = num_correct / len(validation_set_labels)

  # generate confusion matrix
  if showCM:
    cm = confusion_matrix(validation_set_labels, predicted_labels)
    plt.matshow(cm)
    plt.title("Confusion matrix for size-%d training set" % train_size)
    plt.colorbar()
    plt.ylabel("True digit")
    plt.xlabel("Predicted digit")
    plt.savefig("images/confusion_matrix_%d.png" % train_size, dpi=96)

  return accuracy

if __name__ == "__main__":
    main()
