# inference.py
# ------------
# Licensing Information:  You are free to use or extend these projects for
# educational purposes provided that (1) you do not distribute or publish
# solutions, (2) you retain this notice, and (3) you provide clear
# attribution to UC Berkeley, including a link to
# http://inst.eecs.berkeley.edu/~cs188/pacman/pacman.html
# 
# Attribution Information: The Pacman AI projects were developed at UC Berkeley.
# The core projects and autograders were primarily created by John DeNero
# (denero@cs.berkeley.edu) and Dan Klein (klein@cs.berkeley.edu).
# Student side autograding was added by Brad Miller, Nick Hay, and
# Pieter Abbeel (pabbeel@cs.berkeley.edu).


import sys
import itertools
import util
import random
import game
import slam

class InferenceModule:
    """
    An inference module tracks a belief distribution over walls and
    Pacman's location.
    This is an abstract class, which you should not modify.
    """

    ############################################
    # Useful methods for all inference modules #
    ############################################

    def __init__(self):
        pass

    ######################################
    # Methods that need to be overridden #
    ######################################

    def initialize(self):
        "Sets the belief state to some initial configuration."
        pass

    def observe(self, observation):
        """
        Updates beliefs based on the given distance observation and gameState.
        This combines together observe and elapseTime from the previous particle
        filtering project. You may, of course, make your own elapseTime helper
        function if you wish.
        """
        pass

    def getWallBeliefDistribution(self):
        """
        Returns the agent's current belief state about the distribution
        of walls conditioned on all evidence so far.
        """
        pass
    
    def getPositionDistribution(self):
        """
        Returns the agent's current belief state about the distribution
        of its own position conditioned on all evidence so far.
        """
        pass
    
UNKNOWN = 99
YES = 1.0
NO = 0.0
LIKELY = 0.99

K = 20

class SLAMParticleFilter(InferenceModule):
    """
    Particle filtering inference module for use in SLAM.
    """
    
    "*** YOU MAY ADD WHATEVER HELPER METHODS YOU WANT TO THIS CLASS ***"
    
    def __init__(self, startPos, layoutWidth, layoutHeight, wallPrior, legalPositions, numParticles=500):
        "*** YOU OVERWRITE THIS METHOD HOWEVER YOU WANT ***"
        self.startPos = startPos
        self.W = layoutWidth  # including outer walls
        self.H = layoutHeight # including outer walls
        self.priorProb = wallPrior
        self.legalPositions = legalPositions
        self.numParticles = numParticles
        self.initialize()
    
    def initialize(self):
        "*** YOU OVERWRITE THIS METHOD HOWEVER YOU WANT ***"
        wallDist = self.makeWallDistribution()
        self.particles = []
        for i in range(self.numParticles):
          self.particles.append([[self.startPos], self.makeRandomMap(wallDist, False), wallDist])

    def makeWallDistribution(self):
        wallDist = util.Counter()
        for x in range(self.W):
          for y in range(self.H):
            if x == 0 or y == 0 or x == self.W - 1 or y == self.H - 1:
              wallDist[(x,y)] = sys.maxint
            else:
              wallDist[(x,y)] = self.priorProb / (1 - self.priorProb)
        return wallDist

    def makeRandomMap(self, wallDist, trueProb=True): # use False if using p/(1-p)
        randomMap = game.Grid(self.W, self.H, True)
        for x in range(1, self.W - 1):
          for y in range(1, self.H - 1):
            wallProb = wallDist[(x,y)]
            if not trueProb:
              wallProb /= wallProb + 1.0
            if not util.flipCoin(wallProb) or (x,y) == self.startPos:
              randomMap[x][y] = False
        return randomMap

    def getAssumedRanges(self, pos, grid):
        north, east, south, west = 0, 0, 0, 0
        x, y = pos[0], pos[1]+1
        while not grid[x][y]:
          north += 1
          y += 1
        x, y = pos[0]+1, pos[1]
        while not grid[x][y]:
          east += 1
          x += 1
        x, y = pos[0], pos[1]-1
        while not grid[x][y]:
          south += 1
          y -= 1
        x, y = pos[0]-1, pos[1]
        while not grid[x][y]:
          west += 1
          x -= 1
        return north, east, south, west

    def compareRanges(self, assumed, given):
        na, ea, sa, wa = assumed
        ng, eg, sg, wg = given
        if ng > na: return 0.0
        if eg > ea: return 0.0
        if sg > sa: return 0.0
        if wg > wa: return 0.0
        return 1.0
    
    def getMovementModel(self, pos, action, walls):
        model = util.Counter()
        for direc in game.Directions.LEFT.keys():
          model[self.translate(pos, direc, True, walls)] += 0.02
        model[self.translate(pos, action, True, walls)] += 0.9
        return model

    def translate(self, pos, action, useWalls=False, walls=None):
        x, y = pos
        if action == game.Directions.NORTH:
          if useWalls and walls[x][y+1]: return pos
          return (x,y+1)
        elif action == game.Directions.SOUTH:
          if useWalls and walls[x][y-1]: return pos
          return (x,y-1)
        elif action == game.Directions.EAST:
          if useWalls and walls[x+1][y]: return pos
          return (x+1,y)
        elif action == game.Directions.WEST:
          if useWalls and walls[x-1][y]: return pos
          return (x-1,y)
        else:
          return pos

    def inverseRSM(self, pos, ranges):
        x0, y0 = pos
        north, east, south, west = ranges
        model = util.Counter()
        for x in range(self.W):
          for y in range(self.H):
            if x == 0 or y == 0 or x == self.W - 1 or y == self.H - 1:
              model[(x,y)] = YES
            elif x < x0:
              if y == y0:
                if x0 - x < west: model[(x,y)] = NO
                elif x0 - x == west: model[(x,y)] = LIKELY
                else: model[(x,y)] = UNKNOWN
              else: model[(x,y)] = UNKNOWN
            elif x == x0:
              if y == y0: model[(x,y)] = NO
              elif y < y0:
                if y0 - y < south: model[(x,y)] = NO
                elif y0 - y == south: model[(x,y)] = LIKELY
                else: model[(x,y)] = UNKNOWN
              else: # y > y0
                if y - y0 < north: model[(x,y)] = NO
                elif y - y0 == north: model[(x,y)] = LIKELY
                else: model[(x,y)] = UNKNOWN
            else: # x > x0
              if y == y0:
                if x - x0 < east: model[(x,y)] = NO
                elif x - x0 == east: model[(x,y)] = LIKELY
                else: model[(x,y)] = UNKNOWN
              else: model[(x,y)] = UNKNOWN
        return model

    def findParticleWithPosition(self, pos):
        for particle in self.particles:
          if particle[0][-1] == pos:
            return particle
               
    def observe(self, prevAction, ranges): # prevAction is the ATTEMPTED action
        "*** YOU OVERWRITE THIS METHOD HOWEVER YOU WANT ***"
        north, east, south, west = ranges
        # Part 1: reweighting and sampling each particle based on Pacman's new
        #         position and the particle's map
        newParticles = []
        if prevAction is not None:
          for trajectory, walls, wallDist in self.particles:
            pacmanPos = trajectory[-1]
            assumed = self.getAssumedRanges(pacmanPos, walls)
            movementModel = self.getMovementModel(pacmanPos, prevAction, walls)
            nextPos = util.sample(movementModel)
            trajectory.append(nextPos)
            newParticles.append([trajectory, walls, wallDist])
          self.particles = newParticles

        newParticles = []
        for trajectory, walls, wallDist in self.particles:
          pacmanPos = trajectory[-1]
          assumed = self.getAssumedRanges(pacmanPos, walls)
          inverseModel = self.inverseRSM(pacmanPos, ranges)
          newWallDist = util.Counter()
          for x in range(self.W):
            for y in range(self.H):
              evidence = inverseModel[(x,y)]
              if evidence == UNKNOWN or evidence == YES:
                newWallDist[(x,y)] = wallDist[(x,y)]
              else:
                newWallDist[(x,y)] = wallDist[(x,y)] * (1-self.priorProb)/(self.priorProb) * evidence/(1-evidence)
          newParticles.append([trajectory, self.makeRandomMap(newWallDist, False), newWallDist])
        self.particles = newParticles
    
    def getWallBeliefDistribution(self):
        "*** YOU OVERWRITE THIS METHOD HOWEVER YOU WANT ***"
        # p = x/(x+1)
        beliefs = util.Counter()
        for pos in self.legalPositions:
          x, y = pos
          for particle in self.particles:
            if particle[1][x][y]:
              beliefs[pos] += 1.0
          beliefs[pos] /= self.numParticles
        return beliefs
    
    def getPositionBeliefDistribution(self):
        "*** YOU OVERWRITE THIS METHOD HOWEVER YOU WANT ***"
        beliefs = util.Counter()
        for particle in self.particles:
          beliefs[particle[0][-1]] += 1
        beliefs.normalize()
        return beliefs
