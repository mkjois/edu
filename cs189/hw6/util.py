import numpy as np
from math import tanh, exp

class OneArgumentFunction:
    def __init__(self):
        pass
    def func(self, z):
        raise NameError('Method %s not implemented for class %s' %
                        ('func', self.__class__.__name__))
    def grad(self, z):
        raise NameError('Method %s not implemented for class %s' %
                        ('grad', self.__class__.__name__))
    def hess(self, z):
        raise NameError('Method %s not implemented for class %s' %
                        ('hess', self.__class__.__name__))
    def __call__(self, *args):
        return self.func(*args)

class Activation(OneArgumentFunction):
    pass

class TanhActivation(Activation):
    def func(self, z):
        return np.tanh(z)
    def grad(self, z):
        f = self.func(z)
        return 1.0 - f * f

class SigmoidActivation(Activation):
    def func(self, z):
        return 1.0 / (1.0 + np.exp(-z))
    def grad(self, z):
        f = self.func(z)
        return f * (1.0 - f)

class Loss(OneArgumentFunction):
    def __init__(self, y):
        self.y = y

class MeanSquaredLoss(Loss):
    def __init__(self, y):
        self.y = y
        self.name = 'mse'
    def func(self, z):
        return 0.5 * np.sum(np.square(self.y - z))
    def grad(self, z):
        return z - self.y

class CrossEntropyLoss(Loss):
    def __init__(self, y):
        self.y = y
        self.name = 'cross'
    def func(self, z):
        term1 = self.y * np.log(z)
        term2 = (1.0 - self.y) * np.log(1.0 - z)
        return -1.0 * np.sum(term1 + term2)
    def grad(self, z):
        denom = z * (1.0 - z)
        return (z - self.y) / denom
