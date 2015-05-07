import numpy as np
import matplotlib.pyplot as plt
import os
from scipy.io import loadmat
from numpy.linalg import norm

KRANGE = [10]
ITERS = 20

def init(k, d):
    return np.random.uniform(0, 255, (k, d))

def vis(image, dims, savefile):
    image = 255 - image.reshape(dims)
    plt.imshow(image, cmap='gray')
    plt.savefig(savefile, dpi=96)
    plt.cla()

def loss(images, means, clusters):
    J = 0
    for i in range(images.shape[0]):
        img = images[i]
        cluster = clusters[i]
        mean = means[cluster]
        J += norm(img - mean)
    return J

def kmeans(k, iters, images, imagedims, savedir):
    means = init(k, images.shape[1])
    clusters = np.zeros(images.shape[0])
    for i in range(iters):
        # assign images to clusters
        for i in range(images.shape[0]):
            img = images[i]
            distances = norm(img - means, axis=1)
            clusters[i] = np.argmin(distances)

        # recompute means of each cluster
        for j in range(k):
            indices = np.where(clusters == j)
            cluster = images[indices]
            means[j] = np.mean(cluster, axis=0)

        print('Loss = %.4f' % loss(images, means, clusters))

    for i in range(k):
        if not os.path.exists(savedir):
            os.makedirs(savedir)
        savefile = "%s/%d.png" % (savedir, i)
        vis(means[i], imagedims, savefile)

if __name__ == "__main__":
    TRAINFILE = 'mnist_data/images.mat'
    trainset = loadmat(TRAINFILE)
    images = trainset['images']               # 28 x 28 x 60000
    dim, _, num = images.shape
    images = images.reshape((dim * dim, num)).T   # 60000 x 784
    for k in KRANGE:
        print('\nRunning %d-means with %d iterations...\n' % (k, ITERS))
        kmeans(k, ITERS, images, (dim, dim), 'images/%d-means' % k)
