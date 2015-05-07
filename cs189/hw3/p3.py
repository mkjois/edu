import numpy as np
import matplotlib.pyplot as plt
from scipy.stats import multivariate_normal

# generate space to plot contours over
def gen_space(xmin, xmax, ymin, ymax, delta):
  x, y = np.mgrid[xmin:xmax:delta, ymin:ymax:delta]
  space = np.empty(x.shape + (2,))
  space[:,:,0] = x
  space[:,:,1] = y
  return x, y, space

# 3a
x, y, space = gen_space(-6, 6, -6, 6, 0.1)
mu1 = np.array([1, 1])
cov1 = np.array([[2, 0], [0, 1]])
z1 = multivariate_normal(mu1, cov1)
isocontours = plt.contour(x, y, z1.pdf(space))
plt.clabel(isocontours, inline=1, fontsize=10)
plt.xlabel("x1")
plt.ylabel("x2")
plt.title("2D Gaussian pdf isocontours: 3a")
plt.savefig("images/p3a.png", dpi=96)

# 3b
x, y, space = gen_space(-6, 6, -6, 6, 0.1)
mu1 = np.array([-1, 2])
cov1 = np.array([[3, 1], [1, 2]])
z1 = multivariate_normal(mu1, cov1)
plt.cla()
isocontours = plt.contour(x, y, z1.pdf(space))
plt.clabel(isocontours, inline=1, fontsize=10)
plt.xlabel("x1")
plt.ylabel("x2")
plt.title("2D Gaussian pdf isocontours: 3b")
plt.savefig("images/p3b.png", dpi=96)

# 3c
x, y, space = gen_space(-6, 6, -6, 6, 0.1)
mu1 = np.array([0, 2])
mu2 = np.array([2, 0])
cov1 = np.array([[1, 1], [1, 2]])
cov2 = np.array([[1, 1], [1, 2]])
z1 = multivariate_normal(mu1, cov1).pdf(space)
z2 = multivariate_normal(mu2, cov2).pdf(space)
plt.cla()
isocontours = plt.contour(x, y, z1 - z2)
plt.clabel(isocontours, inline=1, fontsize=10)
plt.xlabel("x1")
plt.ylabel("x2")
plt.title("2D Gaussian pdf isocontours: 3c")
plt.savefig("images/p3c.png", dpi=96)

# 3d
x, y, space = gen_space(-6, 6, -6, 6, 0.1)
mu1 = np.array([0, 2])
mu2 = np.array([2, 0])
cov1 = np.array([[1, 1], [1, 2]])
cov2 = np.array([[3, 1], [1, 2]])
z1 = multivariate_normal(mu1, cov1).pdf(space)
z2 = multivariate_normal(mu2, cov2).pdf(space)
plt.cla()
isocontours = plt.contour(x, y, z1 - z2)
plt.clabel(isocontours, inline=1, fontsize=10)
plt.xlabel("x1")
plt.ylabel("x2")
plt.title("2D Gaussian pdf isocontours: 3d")
plt.savefig("images/p3d.png", dpi=96)

# 3e
x, y, space = gen_space(-6, 6, -6, 6, 0.1)
mu1 = np.array([1, 1])
mu2 = np.array([-1, -1])
cov1 = np.array([[1, 0], [0, 2]])
cov2 = np.array([[2, 1], [1, 2]])
z1 = multivariate_normal(mu1, cov1).pdf(space)
z2 = multivariate_normal(mu2, cov2).pdf(space)
plt.cla()
isocontours = plt.contour(x, y, z1 - z2)
plt.clabel(isocontours, inline=1, fontsize=10)
plt.xlabel("x1")
plt.ylabel("x2")
plt.title("2D Gaussian pdf isocontours: 3e")
plt.savefig("images/p3e.png", dpi=96)
