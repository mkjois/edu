# multiAgents.py
# --------------
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


from util import manhattanDistance
from game import Directions
from searchAgents import mazeDistance
import random, util, sys, math

from game import Agent

class ReflexAgent(Agent):
    """
      A reflex agent chooses an action at each choice point by examining
      its alternatives via a state evaluation function.

      The code below is provided as a guide.  You are welcome to change
      it in any way you see fit, so long as you don't touch our method
      headers.
    """


    def getAction(self, gameState):
        """
        You do not need to change this method, but you're welcome to.

        getAction chooses among the best options according to the evaluation function.

        Just like in the previous project, getAction takes a GameState and returns
        some Directions.X for some X in the set {North, South, West, East, Stop}
        """
        # Collect legal moves and successor states
        legalMoves = gameState.getLegalActions()

        # Choose one of the best actions
        scores = [self.evaluationFunction(gameState, action) for action in legalMoves]
        bestScore = max(scores)
        bestIndices = [index for index in range(len(scores)) if scores[index] == bestScore]
        chosenIndex = random.choice(bestIndices) # Pick randomly among the best

        "Add more of your code here if you want to"

        return legalMoves[chosenIndex]

    def evaluationFunction(self, currentGameState, action):
        """
        Design a better evaluation function here.

        The evaluation function takes in the current and proposed successor
        GameStates (pacman.py) and returns a number, where higher numbers are better.

        The code below extracts some useful information from the state, like the
        remaining food (newFood) and Pacman position after moving (newPos).
        newScaredTimes holds the number of moves that each ghost will remain
        scared because of Pacman having eaten a power pellet.

        Print out these variables to see what you're getting, then combine them
        to create a masterful evaluation function.

        DESCRIPTION: This algorithm heavily favors eating all the pellets as
        quickly as possible and almost ignores the power pellet completely. If
        there is a potential win state then it is favored the very most and death
        is the worst consequence. pacman wants to get close to pellets and the
        pellet score will allow him to eat the pellet.
        """
        # Useful information you can extract from a GameState (pacman.py)
        successorGameState = currentGameState.generatePacmanSuccessor(action)
        newPos = successorGameState.getPacmanPosition()
        newFood = successorGameState.getFood()
        newGhostStates = successorGameState.getGhostStates()
        newScaredTimes = [ghostState.scaredTimer for ghostState in newGhostStates]

        "*** YOUR CODE HERE ***"
        #util.raiseNotDefined()

        currentGameState = successorGameState
        sum = 0
        foodConstant = -300

        ghostDistanceFromPacman = 0

        scaredTime = 0
        for ghost in newGhostStates:
            scaredTime = ghost.scaredTimer
            manhattan = manhattanDistance(newPos, ghost.getPosition())
            if manhattan < 1 and scaredTime <= 0:
                sum -= 3000
            if manhattan < 2 and scaredTime <= 0:
                sum -= 1000
            #if manhattan > 3 and scaredTime <= 0:
            #    sum -= 25 * manhattan
            if manhattan > 10 and scaredTime > 16:
                sum -= 400
            if manhattan > 3 and scaredTime > 4:
                sum -= 400
            if manhattan < 1 and scaredTime > 0:
                sum += 600

            closestFood = 20
            for foodPos in newFood.asList():
                closestFood = min(closestFood, manhattanDistance(newPos, foodPos))
            if (currentGameState.isWin()):
                return sys.maxint
            if currentGameState.getNumFood() != 1:
                if (closestFood > 2):
                    sum -= 20 * closestFood
            if (closestFood == 0):
                sum += 1000

        sum += foodConstant * currentGameState.getNumFood()
        sum += -1000 * (len(currentGameState.getCapsules()))

        return sum
        # return successorGameState.getScore()

def scoreEvaluationFunction(currentGameState):
    """
      This default evaluation function just returns the score of the state.
      The score is the same one displayed in the Pacman GUI.

      This evaluation function is meant for use with adversarial search agents
      (not reflex agents).
    """
    return currentGameState.getScore()

class MultiAgentSearchAgent(Agent):
    """
      This class provides some common elements to all of your
      multi-agent searchers.  Any methods defined here will be available
      to the MinimaxPacmanAgent & AlphaBetaPacmanAgent.

      You *do not* need to make any changes here, but you can if you want to
      add functionality to all your adversarial search agents.  Please do not
      remove anything, however.

      Note: this is an abstract class: one that should not be instantiated.  It's
      only partially specified, and designed to be extended.  Agent (game.py)
      is another abstract class.
    """

    def __init__(self, evalFn = 'scoreEvaluationFunction', depth = '2'):
        self.index = 0 # Pacman is always agent index 0
        self.evaluationFunction = util.lookup(evalFn, globals())
        self.depth = int(depth)


def minValue(state, depth, agentIndex, searchType): #returns a value
    if terminalTest(state, depth):
        return utility(state)
    v = sys.maxint
    vlist = []
    numPlayers = state.getNumAgents()
    nextAgentIndex = (agentIndex + 1) % numPlayers
    for a in actions(state, agentIndex):
        if nextAgentIndex == 0:
            maxPotentialV = maxValue(result(agentIndex, state, a), depth - 1, nextAgentIndex, searchType)
            v = min(v, maxPotentialV)
            vlist.append(maxPotentialV)
        else:
            minPotentialV = minValue(result(agentIndex, state, a), depth, nextAgentIndex, searchType)
            v = min(v, minPotentialV)
            vlist.append(minPotentialV)
    if searchType == 0:
        return v
    average = 0
    for element in vlist:
        average += element

    return average/(len(vlist))

def maxValue(state, depth, agentIndex, searchType): # returns a value
    # print("depth = " + str(depth))
    if terminalTest(state, depth):
        return utility(state)
    v = -sys.maxint - 1
    numPlayers = state.getNumAgents()
    nextAgentIndex = (agentIndex + 1) % numPlayers
    for a in actions(state, agentIndex):
        v = max(v, minValue(result(agentIndex, state, a), depth, nextAgentIndex, searchType))
    return v

def result(agentIndex, currentGameState, action): #returns a state
    successorGameState = currentGameState.generateSuccessor(agentIndex, action)
    return successorGameState

def utility(state):
    return evaluationFunction(state)
    #return scoreEvaluationFunction(state)
    """ how can i use betterEvaluationFunction with only a state from pseudo?"""
    #return betterEvaluationFunction(state, action)

def actions(gameState, agentIndex): #return all possible actions
    return gameState.getLegalActions(agentIndex)

def terminalTest(state, depth): # returns a boolean
    #if (state.)
    if depth == -1 or state.isWin() or state.isLose():
        return True
    return False

evaluationFunction = None

class MinimaxAgent(MultiAgentSearchAgent):
    """
      Your minimax agent (question 7)
    """

    def getAction(self, gameState):
        """
          Returns the minimax action from the current gameState using self.depth
          and self.evaluationFunction.

          Here are some method calls that might be useful when implementing minimax.

          gameState.getLegalActions(agentIndex):
            Returns a list of legal actions for an agent
            agentIndex=0 means Pacman, ghosts are >= 1

          gameState.generateSuccessor(agentIndex, action):
            Returns the successor game state after an agent takes an action

          gameState.getNumAgents():
            Returns the total number of agents in the game
        """
        "*** YOUR CODE HERE ***"

        #return the action a in actions(s) with the highest minValue(Result(s, a))
        global evaluationFunction
        depth = self.depth
        evaluationFunction = self.evaluationFunction
        v = -sys.maxint - 1
        keepAction = None
        for action in gameState.getLegalActions(0):
            copy = v
            v = max(v, minValue(result(0, gameState, action), depth - 1, 1, 0))
            if v != copy:
                keepAction = action
        return keepAction
        #util.raiseNotDefined()

class ExpectimaxAgent(MultiAgentSearchAgent):
    """
      Your expectimax agent (question 8)
    """

    def getAction(self, gameState):
        """
          Returns the expectimax action using self.depth and self.evaluationFunction

          All ghosts should be modeled as choosing uniformly at random from their
          legal moves.
        """
        "*** YOUR CODE HERE ***"
        global evaluationFunction
        depth = self.depth
        evaluationFunction = self.evaluationFunction
        v = -sys.maxint - 1
        keepAction = None
        for action in gameState.getLegalActions(0):
            copy = v
            v = max(v, minValue(result(0, gameState, action), depth - 1, 1, 1))
            if v != copy:
                keepAction = action
        return keepAction
        # util.raiseNotDefined()

def betterEvaluationFunction(currentGameState):
    """
      Your extreme ghost-hunting, pellet-nabbing, food-gobbling, unstoppable
      evaluation function (question 9).

      DESCRIPTION: I made an algorithm that is inclined to follow the ghost 
      a bit and heavily favors eating the ghost. I decreased the desire to 
      eat pellets and relied on eating ghosts for points.
    """
    "*** YOUR CODE HERE ***"
    newPos = currentGameState.getPacmanPosition()
    newFood = currentGameState.getFood()
    newGhostStates = currentGameState.getGhostStates()
    newScaredTimes = [ghostState.scaredTimer for ghostState in newGhostStates]

    #util.raiseNotDefined()

    sum = 0
    foodConstant = -200

    ghostDistanceFromPacman = 0

    scaredTime = 0
    for ghost in newGhostStates:
        scaredTime = ghost.scaredTimer
        manhattan = manhattanDistance(newPos, ghost.getPosition())
        if manhattan < 1 and scaredTime <= 0:
            sum -= 2000
        if manhattan > 3 and scaredTime <= 0:
            sum -= 100
        if manhattan > 10 and scaredTime > 16:
            sum -= 400
        if manhattan > 3 and scaredTime > 4:
            sum -= 400
        if manhattan < 1 and scaredTime > 0:
            sum += 600

    #make him get closer to his closest food pellet
    # closestFood = sys.maxint
    # for foodPos in newFood:
    #     closestFood = min(closestFood, manhattanDistance(newPos, foodPos))
    # sum -= closestFood * 400

    sum += foodConstant * currentGameState.getNumFood()
    sum += -1000 * (len(currentGameState.getCapsules()))

    return sum
# Abbreviation
better = betterEvaluationFunction
