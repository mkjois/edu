import numpy as np
import matplotlib.pyplot as plt
from scipy import io
from sklearn import svm
from sklearn.metrics import confusion_matrix

DATA_FILE = "data/spam-dataset/spam_data.mat"
TEST_RESULTS_FILE = "spam_results.csv"

K = 10
#C_RANGE = np.array([10**i for i in range(-5,6)])
C_RANGE = np.linspace(0.1, 100.0, 15)

def main():
  data = io.loadmat(DATA_FILE)
  train_emails = data["training_data"]
  train_labels = data["training_labels"]
  test_emails = data["test_data"]
  # images[:,:,k] gives image #k
  best_c, score = cross_validate(train_emails, train_labels, C_RANGE)
  print("\nBest C:\t%f" % best_c)
  print("Best Score:\t%f\n" % score)

  # train a new svm
  train_set_labels = [label for label in np.nditer(train_labels)]
  train_set = (train_emails, train_set_labels)

  classifier, accuracy = classify(train_set, train_set, best_c)

  predictions = classifier.predict(test_emails)
  with open(TEST_RESULTS_FILE, "w") as f:
    f.write("Id,Category\n")
    for i in range(len(predictions)):
      f.write(str(i+1) + "," + str(predictions[i]) + "\n")

def cross_validate(all_emails, all_labels, c_range):
  indices = np.random.permutation(all_labels.size)
  emails = []
  labels = []
  for i in indices[:all_labels.size / K * K]:
    emails.append(all_emails[i])
    labels.append(all_labels[0][i])
  fold_size = all_labels.size / K
  best_c, best_score = None, 0.0
  for c in np.nditer(c_range):
    scores = []
    for i in range(K):
      training_set_emails = []
      training_set_labels = []
      validation_set_emails = None
      validation_set_labels = None
      for j in range(K):
        if j == i:
          validation_set_emails = emails[fold_size*j : fold_size*(j+1)]
          validation_set_labels = labels[fold_size*j : fold_size*(j+1)]
        else:
          training_set_emails.extend(emails[fold_size*j : fold_size*(j+1)])
          training_set_labels.extend(labels[fold_size*j : fold_size*(j+1)])
      training_set = (training_set_emails, training_set_labels)
      validation_set = (validation_set_emails, validation_set_labels)
      #print(len(training_set_emails))
      classifier, accuracy = classify(training_set, validation_set, c)
      scores.append(accuracy)
    avg_score = sum(scores) / len(scores)
    print("C: %f\tScore: %f" % (c, avg_score))
    if avg_score > best_score:
      best_score = avg_score
      best_c = c
  return best_c, best_score

def classify(training_set, validation_set, c):
  v_set_emails = validation_set[0]
  v_set_labels = validation_set[1]

  t_set_emails = training_set[0]
  t_set_labels = training_set[1]

  classifier = svm.LinearSVC(C=c)
  classifier.fit(t_set_emails, t_set_labels)
  accuracy = classifier.score(v_set_emails, v_set_labels)
  return classifier, accuracy

if __name__ == "__main__":
    main()
