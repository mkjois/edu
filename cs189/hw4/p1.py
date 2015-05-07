import numpy as np
from numpy.linalg import inv

def mu(beta, X):
  return 1.0 / (1.0 + np.exp(-X.dot(beta)))

def diag(mu):
  return np.diag(np.ndarray.flatten(np.multiply(mu, 1.0 - mu)))

def update(beta, mu_B, X, Y, lamb):
  I = np.eye(beta.size)
  hessian_inv = inv(2*lamb*I + X.T.dot(diag(mu_B)).dot(X))
  gradient = 2*lamb * I.dot(beta) - X.T.dot(Y - mu_B)
  return beta - hessian_inv.dot(gradient)

if __name__ == "__main__":
  X = np.array([[0, 3, 1], [1, 3, 1], [0, 1, 1], [1, 1, 1]]) # 4 x 3
  Y = np.array([[1], [1], [0], [0]]) # 4 x 1
  B0 = np.array([[-2], [1], [0]]) # 3 x 1
  LAMBDA = 0.07
  mu0 = mu(B0, X)
  B1 = update(B0, mu0, X, Y, LAMBDA)
  mu1 = mu(B1, X)
  B2 = update(B1, mu1, X, Y, LAMBDA)
  print("\nmu_0:\n")
  print(mu0)
  print("\nbeta_1:\n")
  print(B1)
  print("\nmu_1:\n")
  print(mu1)
  print("\nbeta_2:\n")
  print(B2)
