import numpy as np
import matplotlib.pyplot as plt
from numpy.random import normal
from numpy.linalg import eig
from math import sqrt

outfile = "p1out.txt"
outstr = ""

mu1, var1 = 3, 9
mu2, var2 = 4, 4

def gen_samples(mean, variance, num_samples):
  return normal(loc=mean, scale=sqrt(variance), size=num_samples)

samp1 = gen_samples(mu1, var1, 100)
samp2 = 0.5 * samp1 + gen_samples(mu2, var2, 100)
samp = np.array([samp1, samp2])

# 1a
mean1 = np.mean(samp1)
mean2 = np.mean(samp2)
mean = np.array([mean1, mean2])
outstr += "\nSample mean of X1: %f" % mean1
outstr += "\nSample mean of X2: %f" % mean2

# 1b
covariance = np.cov(samp)
outstr += "\n\nThe covariance matrix:\n\n"
outstr += str(covariance)

# 1c
eigenvals, eigenvecs = eig(covariance)
outstr += "\n\nThe eigenvalues and eigenvectors:\n\n"
for i in range(eigenvals.size):
  outstr += "Value: %.3f\tVector: %s\n" % (eigenvals[i], str(eigenvecs[:,i]))

# 1d
plt.plot(samp1, samp2, 'bo')
for i in range(eigenvals.size):
  eigenval = eigenvals[i]
  eigen_x, eigen_y = eigenvecs[:,i] * eigenval
  plt.arrow(mean1, mean2, eigen_x, eigen_y, length_includes_head=True, head_width=1)
plt.axis([-15, 15, -15, 15])
plt.xlabel("X1")
plt.ylabel("X2")
plt.title("Two-dimensional Gaussian")
plt.savefig("images/p1d.png", dpi=96)

# 1e
samp1 -= mean1
samp2 -= mean2
samp = np.array([samp1, samp2])
samp_rotated = eigenvecs.T.dot(samp)
plt.cla()
plt.plot(samp_rotated[0], samp_rotated[1], 'ro')
plt.axis([-15, 15, -15, 15])
plt.ylabel("X2_rotated")
plt.xlabel("X1_rotated")
plt.title("Rotated and centered two-dimensional Gaussian")
plt.savefig("images/p1e.png", dpi=96)

with open(outfile, "w") as f:
  f.write(outstr)
