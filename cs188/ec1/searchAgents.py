# searchAgents.py
# ---------------
# Licensing Information:  You are free to use or extend these projects for
# educational purposes provided that (1) you do not distribute or publish
# solutions, (2) you retain this notice, and (3) you provide clear
# attribution to UC Berkeley, including a link to http://ai.berkeley.edu.
# 
# Attribution Information: The Pacman AI projects were developed at UC Berkeley.
# The core projects and autograders were primarily created by John DeNero
# (denero@cs.berkeley.edu) and Dan Klein (klein@cs.berkeley.edu).
# Student side autograding was added by Brad Miller, Nick Hay, and
# Pieter Abbeel (pabbeel@cs.berkeley.edu).


"""
This file contains all of the agents that can be selected to control Pacman.  To
select an agent, use the '-p' option when running pacman.py.  Arguments can be
passed to your agent using '-a'.  For example, to load a SearchAgent that uses
depth first search (dfs), run the following command:

> python pacman.py -p SearchAgent -a fn=depthFirstSearch

Commands to invoke other search strategies can be found in the project
description.

Please only change the parts of the file you are asked to.  Look for the lines
that say

"*** YOUR CODE HERE ***"

The parts you fill in start about 3/4 of the way down.  Follow the project
description for details.

Good luck and happy searching!
"""

from game import Directions
from game import Agent
from game import Actions
from game import Grid
import util
import time
import search

#######################################################
# This portion is written for you, but will only work #
#       after you fill in parts of search.py          #
#######################################################

class SearchAgent(Agent):
    """
    This very general search agent finds a path using a supplied search
    algorithm for a supplied search problem, then returns actions to follow that
    path.

    As a default, this agent runs DFS on a PositionSearchProblem to find
    location (1,1)

    Options for fn include:
      depthFirstSearch or dfs
      breadthFirstSearch or bfs


    Note: You should NOT change any code in SearchAgent
    """

    def __init__(self, fn='depthFirstSearch', prob='PositionSearchProblem', heuristic='nullHeuristic'):
        # Warning: some advanced Python magic is employed below to find the right functions and problems

        # Get the search function from the name and heuristic
        if fn not in dir(search):
            raise AttributeError, fn + ' is not a search function in search.py.'
        func = getattr(search, fn)
        if 'heuristic' not in func.func_code.co_varnames:
            print('[SearchAgent] using function ' + fn)
            self.searchFunction = func
        else:
            if heuristic in globals().keys():
                heur = globals()[heuristic]
            elif heuristic in dir(search):
                heur = getattr(search, heuristic)
            else:
                raise AttributeError, heuristic + ' is not a function in searchAgents.py or search.py.'
            print('[SearchAgent] using function %s and heuristic %s' % (fn, heuristic))
            # Note: this bit of Python trickery combines the search algorithm and the heuristic
            self.searchFunction = lambda x: func(x, heuristic=heur)

        # Get the search problem type from the name
        if prob not in globals().keys() or not prob.endswith('Problem'):
            raise AttributeError, prob + ' is not a search problem type in SearchAgents.py.'
        self.searchType = globals()[prob]
        print('[SearchAgent] using problem type ' + prob)

    def registerInitialState(self, state):
        """
        This is the first time that the agent sees the layout of the game
        board. Here, we choose a path to the goal. In this phase, the agent
        should compute the path to the goal and store it in a local variable.
        All of the work is done in this method!

        state: a GameState object (pacman.py)
        """
        if self.searchFunction == None: raise Exception, "No search function provided for SearchAgent"
        starttime = time.time()
        problem = self.searchType(state) # Makes a new search problem
        self.actions  = self.searchFunction(problem) # Find a path
        totalCost = problem.getCostOfActions(self.actions)
        print('Path found with total cost of %d in %.1f seconds' % (totalCost, time.time() - starttime))
        if '_expanded' in dir(problem): print('Search nodes expanded: %d' % problem._expanded)

    def getAction(self, state):
        """
        Returns the next action in the path chosen earlier (in
        registerInitialState).  Return Directions.STOP if there is no further
        action to take.

        state: a GameState object (pacman.py)
        """
        if 'actionIndex' not in dir(self): self.actionIndex = 0
        i = self.actionIndex
        self.actionIndex += 1
        if i < len(self.actions):
            return self.actions[i]
        else:
            return Directions.STOP

class PositionSearchProblem(search.SearchProblem):
    """
    A search problem defines the state space, start state, goal test, successor
    function and cost function.  This search problem can be used to find paths
    to a particular point on the pacman board.

    The state space consists of (x,y) positions in a pacman game.

    Note: this search problem is fully specified; you should NOT change it.
    """

    def __init__(self, gameState, costFn = lambda x: 1, goal=(1,1), start=None, warn=True, visualize=True):
        """
        Stores the start and goal.

        gameState: A GameState object (pacman.py)
        costFn: A function from a search state (tuple) to a non-negative number
        goal: A position in the gameState
        """
        self.walls = gameState.getWalls()
        self.startState = gameState.getPacmanPosition()
        if start != None: self.startState = start
        self.goal = goal
        self.costFn = costFn
        self.visualize = visualize
        if warn and (gameState.getNumFood() != 1 or not gameState.hasFood(*goal)):
            print 'Warning: this does not look like a regular search maze'

        # For display purposes
        self._visited, self._visitedlist, self._expanded = {}, [], 0 # DO NOT CHANGE

    def getStartState(self):
        return self.startState

    def isGoalState(self, state):
        isGoal = state == self.goal

        # For display purposes only
        if isGoal and self.visualize:
            self._visitedlist.append(state)
            import __main__
            if '_display' in dir(__main__):
                if 'drawExpandedCells' in dir(__main__._display): #@UndefinedVariable
                    __main__._display.drawExpandedCells(self._visitedlist) #@UndefinedVariable

        return isGoal

    def getSuccessors(self, state):
        """
        Returns successor states, the actions they require, and a cost of 1.

         As noted in search.py:
             For a given state, this should return a list of triples,
         (successor, action, stepCost), where 'successor' is a
         successor to the current state, 'action' is the action
         required to get there, and 'stepCost' is the incremental
         cost of expanding to that successor
        """

        successors = []
        for action in [Directions.NORTH, Directions.SOUTH, Directions.EAST, Directions.WEST]:
            x,y = state
            dx, dy = Actions.directionToVector(action)
            nextx, nexty = int(x + dx), int(y + dy)
            if not self.walls[nextx][nexty]:
                nextState = (nextx, nexty)
                cost = self.costFn(nextState)
                successors.append( ( nextState, action, cost) )

        # Bookkeeping for display purposes
        self._expanded += 1 # DO NOT CHANGE
        if state not in self._visited:
            self._visited[state] = True
            self._visitedlist.append(state)

        return successors

    def getCostOfActions(self, actions):
        """
        Returns the cost of a particular sequence of actions. If those actions
        include an illegal move, return 999999.
        """
        if actions == None: return 999999
        x,y= self.getStartState()
        cost = 0
        for action in actions:
            # Check figure out the next state and see whether its' legal
            dx, dy = Actions.directionToVector(action)
            x, y = int(x + dx), int(y + dy)
            if self.walls[x][y]: return 999999
            cost += self.costFn((x,y))
        return cost

def manhattanHeuristic(position, problem, info={}):
    "The Manhattan distance heuristic for a PositionSearchProblem"
    xy1 = position
    xy2 = problem.goal
    return abs(xy1[0] - xy2[0]) + abs(xy1[1] - xy2[1])

def euclideanHeuristic(position, problem, info={}):
    "The Euclidean distance heuristic for a PositionSearchProblem"
    xy1 = position
    xy2 = problem.goal
    return ( (xy1[0] - xy2[0]) ** 2 + (xy1[1] - xy2[1]) ** 2 ) ** 0.5

#####################################################
# This portion is incomplete.  Time to write code!  #
#####################################################

class FoodSearchProblem:
    """
    A search problem associated with finding the a path that collects all of the
    food (dots) in a Pacman game.

    A search state in this problem is a tuple ( pacmanPosition, foodGrid ) where
      pacmanPosition: a tuple (x,y) of integers specifying Pacman's position
      foodGrid:       a Grid (see game.py) of either True or False, specifying remaining food
    """
    def __init__(self, startingGameState):
        self.start = (startingGameState.getPacmanPosition(), startingGameState.getFood())
        self.walls = startingGameState.getWalls()
        self.startingGameState = startingGameState
        self._expanded = 0 # DO NOT CHANGE
        self.heuristicInfo = {} # A dictionary for the heuristic to store information

    def getStartState(self):
        return self.start

    def isGoalState(self, state):
        return state[1].count() == 0

    def getSuccessors(self, state):
        "Returns successor states, the actions they require, and a cost of 1."
        successors = []
        self._expanded += 1 # DO NOT CHANGE
        for direction in [Directions.NORTH, Directions.SOUTH, Directions.EAST, Directions.WEST]:
            x,y = state[0]
            dx, dy = Actions.directionToVector(direction)
            nextx, nexty = int(x + dx), int(y + dy)
            if not self.walls[nextx][nexty]:
                nextFood = state[1].copy()
                nextFood[nextx][nexty] = False
                successors.append( ( ((nextx, nexty), nextFood), direction, 1) )
        return successors

    def getCostOfActions(self, actions):
        """Returns the cost of a particular sequence of actions.  If those actions
        include an illegal move, return 999999"""
        x,y= self.getStartState()[0]
        cost = 0
        for action in actions:
            # figure out the next state and see whether it's legal
            dx, dy = Actions.directionToVector(action)
            x, y = int(x + dx), int(y + dy)
            if self.walls[x][y]:
                return 999999
            cost += 1
        return cost

class AStarFoodSearchAgent(SearchAgent):
    "A SearchAgent for FoodSearchProblem using A* and your foodHeuristic"
    def __init__(self):
        self.searchFunction = lambda prob: search.aStarSearch(prob, foodHeuristic)
        self.searchType = FoodSearchProblem

def foodHeuristic(state, problem):
    """
    Your heuristic for the FoodSearchProblem goes here.

    This heuristic must be consistent to ensure correctness.  First, try to come
    up with an admissible heuristic; almost all admissible heuristics will be
    consistent as well.

    If using A* ever finds a solution that is worse uniform cost search finds,
    your heuristic is *not* consistent, and probably not admissible!  On the
    other hand, inadmissible or inconsistent heuristics may find optimal
    solutions, so be careful.

    The state is a tuple ( pacmanPosition, foodGrid ) where foodGrid is a Grid
    (see game.py) of either True or False. You can call foodGrid.asList() to get
    a list of food coordinates instead.

    If you want access to info like walls, capsules, etc., you can query the
    problem.  For example, problem.walls gives you a Grid of where the walls
    are.

    If you want to *store* information to be reused in other calls to the
    heuristic, there is a dictionary called problem.heuristicInfo that you can
    use. For example, if you only want to count the walls once and store that
    value, try: problem.heuristicInfo['wallCount'] = problem.walls.count()
    Subsequent calls to this heuristic can access
    problem.heuristicInfo['wallCount']
    """
    position, foodGrid = state
    "*** YOUR CODE HERE ***"
    if "distances" not in problem.heuristicInfo:
        problem.heuristicInfo["distances"] = dict()
    furthestFoodDistance = 0
    for pellet in foodGrid.asList():
        pair = (position, pellet)
        if pair in problem.heuristicInfo["distances"]:
            distance = problem.heuristicInfo["distances"][pair]
        else:
            distance = mazeDistance(position, pellet, problem.startingGameState)
            problem.heuristicInfo["distances"][pair] = distance
        furthestFoodDistance = max(furthestFoodDistance, distance)
    return furthestFoodDistance

class ClosestDotSearchAgent(SearchAgent):
    "Search for all food using a sequence of searches"
    def registerInitialState(self, state):
        self.actions = []
        currentState = state
        while(currentState.getFood().count() > 0):
            nextPathSegment = self.findPathToClosestDot(currentState) # The missing piece
            self.actions += nextPathSegment
            for action in nextPathSegment:
                legal = currentState.getLegalActions()
                if action not in legal:
                    t = (str(action), str(currentState))
                    raise Exception, 'findPathToClosestDot returned an illegal move: %s!\n%s' % t
                currentState = currentState.generateSuccessor(0, action)
        self.actionIndex = 0
        print 'Path found with cost %d.' % len(self.actions)

    def findPathToClosestDot(self, gameState):
        """
        Returns a path (a list of actions) to the closest dot, starting from
        gameState.
        """
        # Here are some useful elements of the startState
        startPosition = gameState.getPacmanPosition()
        food = gameState.getFood()
        walls = gameState.getWalls()
        problem = AnyFoodSearchProblem(gameState)

        "*** YOUR CODE HERE ***"
        pathToClosestDot = search.bfs(problem)
        return pathToClosestDot

class AnyFoodSearchProblem(PositionSearchProblem):
    """
    A search problem for finding a path to any food.

    This search problem is just like the PositionSearchProblem, but has a
    different goal test, which you need to fill in below.  The state space and
    successor function do not need to be changed.

    The class definition above, AnyFoodSearchProblem(PositionSearchProblem),
    inherits the methods of the PositionSearchProblem.

    You can use this search problem to help you fill in the findPathToClosestDot
    method.
    """

    def __init__(self, gameState, costFn=lambda x: 1):
        "Stores information from the gameState.  You don't need to change this."
        # Store the food for later reference
        self.food = gameState.getFood()

        # Store info for the PositionSearchProblem (no need to change this)
        self.walls = gameState.getWalls()
        self.startState = gameState.getPacmanPosition()
        self.costFn = costFn
        self._visited, self._visitedlist, self._expanded = {}, [], 0 # DO NOT CHANGE

    def isGoalState(self, state):
        """
        The state is Pacman's position. Fill this in with a goal test that will
        complete the problem definition.
        """
        x,y = state

        "*** YOUR CODE HERE ***"
        return self.food[x][y]

##################
# Mini-contest 1 #
##################

class FoodSearchProblem2(FoodSearchProblem):
  def __init__(self, startingGameState, startPosition, endPosition, food):
    FoodSearchProblem.__init__(self, startingGameState)
    self.start = (startPosition, food)
    self.end = endPosition
  def isGoalState(self, state):
    return state[1].count() == 0

class ApproximateSearchAgent(Agent):
    "Implement your contest entry here.  Change anything but the class name."

    def registerInitialState(self, state):
        "This method is called before any moves are made."
        "*** YOUR CODE HERE ***"
        self.walls = state.getWalls()
        self.actions = []
        currentState = state
        statesInOrder = [currentState.getPacmanPosition()]
        while(currentState.getFood().count() > 0):
            nextPathSegment = self.findPathToClosestDot(currentState) # The missing piece
            self.actions += nextPathSegment
            for action in nextPathSegment:
                legal = currentState.getLegalActions()
                if action not in legal:
                    t = (str(action), str(currentState))
                    raise Exception, 'findPathToClosestDot returned an illegal move: %s!\n%s' % t
                currentState = currentState.generateSuccessor(0, action)
                statesInOrder.append(currentState.getPacmanPosition())

        def getResultingState(start, actions):
          currentState = start
          for action in actions:
            legal = currentState.getLegalActions()
            if action not in legal:
              t = (str(action), str(currentState))
              raise Exception, 'findPathToClosestDot returned an illegal move: %s!\n%s' % t
            currentState = currentState.generateSuccessor(0, action)
          return currentState

        def sortComp(x, y):
          if len(x[1]) < len(y[1]): return -1
          elif len(x[1]) > len(y[1]): return 1
          return (len(self.actions) - x[1][-1]) - (len(self.actions) - y[1][-1])

        def backtrack(actions):
          reverse = []
          for i in range(len(actions)-1, -1, -1):
            action = actions[i]
            if action == Directions.NORTH: reverse.append(Directions.SOUTH)
            elif action == Directions.SOUTH: reverse.append(Directions.NORTH)
            elif action == Directions.EAST: reverse.append(Directions.WEST)
            elif action == Directions.WEST: reverse.append(Directions.EAST)
            else: reverse.append(Directions.STOP)
          return reverse

        def isForwardAndBack(actions):
          if len(actions) % 2 != 0: return False
          l = len(actions)
          for i in range(l // 2):
            if actions[l-1-i] != Directions.REVERSE[actions[i]]: return False
          return True

        def startsWith(whole, starter):
          if len(starter) > len(whole): return False
          for i in range(len(starter)):
            if whole[i] != starter[i]: return False
          return True

        def foodAround(pos, food):
          x,y = pos
          return food[x-1][y-1] and food[x-1][y] and food[x-1][y+1] and food[x][y-1] and \
                 food[x][y+1] and food[x+1][y-1] and food[x+1][y] and food[x+1][y+1]

        def growRegion(region, pos, food):
          x,y = pos
          if foodAround(pos, food):
            region.add(pos)
            neighbors = [(x-1,y-1),(x-1,y),(x-1,y+1),(x,y-1),(x,y+1),(x+1,y-1),(x+1,y),(x+1,y+1)]
            for p in [n for n in neighbors if n not in region]:
              region.add(p)
              growRegion(region, p, food)

        def findRegions():
          legalPositions = set()
          food = state.getFood()
          for i in range(1, self.walls.width):
            for j in range(1, self.walls.height):
              if not self.walls[i][j]:
                legalPositions.add((i,j))
          regions = []
          for pos in legalPositions:
            cont = False
            for r in regions:
              if pos in r:
                cont = True
                break
            if cont:
              continue  
            if foodAround(pos, food):
              region = set()
              growRegion(region, pos, food)
              regions.append(region)
          return [list(r) for r in regions]

        def findGates(region):
          gates = set()
          for x,y in region:
            neighbors = [(x-1,y),(x+1,y),(x,y-1),(x,y+1)]
            for n in neighbors:
              if n not in region and not self.walls[n[0]][n[1]]:
                gates.add(n)
          return list(gates)

        def findCorners(region):
          corners = set()
          for x,y in region:
            walls = 0
            neighbors = [(x-1,y),(x+1,y),(x,y-1),(x,y+1)]
            for i,j in neighbors:
              if self.walls[i][j]:
                walls += 1
            if walls == 2:
              corners.add((x,y))
          return list(corners)

        regions = [{"region": r, "gates": findGates(r), "corners": findCorners(r), "fixed": False} for r in findRegions()]

        fixes = 13
        while fixes > 0:
          uniqueStates = dict()
          for i in range(len(statesInOrder)):
            s = statesInOrder[i]
            if s in uniqueStates:
              uniqueStates[s].append(i)
            else:
              uniqueStates[s] = [i]

          fixed = False
          sortedStates = uniqueStates.items()
          sortedStates.sort(sortComp)
          for s, i in sortedStates:
            if len(i) == 2:
              a0, a1, a2 = self.actions[:i[0]], self.actions[i[0]:i[1]], self.actions[i[1]:]
              a1a = Directions.STOP
              if s != state.getPacmanPosition():
                a1a = a0[-1]
              a1b, a1c, a1d = a1[0], a1[-1], a2[0]
              if len(self.actions) - i[1] < (i[1] - i[0]) / 2 and len(self.actions)-i[1] < 10 and a1c == Directions.REVERSE[a1b]:
                problem = PositionSearchProblem(state, goal=s, start=statesInOrder[-1], warn=False)
                backpath = search.bfs(problem)
                self.actions = a0 + a2 + backpath + a1
                fixed = True
                break
              if a1a + a1b == a1c + a1d and fixes == 1: # major fix, so do it last
                a1reverse = backtrack(a1)
                self.actions = a0 + a1reverse[:-1] + a2[1:]
                fixed = True
                break
            if len(i) == 3:
              a0, a1, a2, a3 = self.actions[:i[0]], self.actions[i[0]:i[1]], self.actions[i[1]:i[2]], self.actions[i[2]:]
              if isForwardAndBack(a1) and startsWith(a3, a1[:len(a1)//2]):
                self.actions = a0 + a2 + a3
                fixed = True
                break

          if not fixed:
            for region in regions:
              r, g, c, f = region["region"], region["gates"], region["corners"], region["fixed"]
              if f: continue
              if len(g) == 1:
                ig1 = uniqueStates[g[0]]
                a0, a1, a2 = self.actions[:ig1[0]], self.actions[ig1[0]:ig1[1]], self.actions[ig1[1]:]
                food = state.getFood()
                newFood = Grid(food.width, food.height)
                for x,y in c:
                  newFood[x][y] = True
                problem = FoodSearchProblem2(state, g[0], g[0], newFood)
                newPath = search.bfs(problem)
                def costFn1(pos):
                  return 10000 - util.manhattanDistance(pos, g[0])
                intermediate = getResultingState(state, a0 + newPath)
                while intermediate.getPacmanPosition() in r:
                  nextPathSegment = self.findPathToClosestDot(intermediate, costFn1) # The missing piece
                  newPath += nextPathSegment
                  intermediate = getResultingState(intermediate, nextPathSegment)
                while intermediate.getPacmanPosition() != g[0]:
                  reverseLast = Directions.REVERSE[newPath.pop()]
                  intermediate = getResultingState(intermediate, [reverseLast])
                self.actions = a0 + newPath + a2
                region["fixed"] = True
                break
              elif len(g) == 2:
                ig1 = uniqueStates[g[0]]
                ig2 = uniqueStates[g[1]]
                startGate, endGate = g[1], g[0]
                if ig1[0] < ig2[0]: 
                  startGate, endGate = endGate, startGate
                a0, a1, a2 = self.actions[:ig1[0]], self.actions[ig1[0]:ig2[0]], self.actions[ig2[0]:]
                food = state.getFood()
                newFood = Grid(food.width, food.height)
                for x,y in c:
                  newFood[x][y] = True
                problem = FoodSearchProblem2(state, startGate, endGate, newFood)
                newPath = search.bfs(problem)
                def costFn2(pos):
                  return 10000 - util.manhattanDistance(pos, endGate)
                intermediate = getResultingState(state, a0 + newPath)
                while intermediate.getPacmanPosition() in r:
                  nextPathSegment = self.findPathToClosestDot(intermediate, costFn2) # The missing piece
                  newPath += nextPathSegment
                  intermediate = getResultingState(intermediate, nextPathSegment)
                while intermediate.getPacmanPosition() != endGate:
                  reverseLast = Directions.REVERSE[newPath.pop()]
                  intermediate = getResultingState(intermediate, [reverseLast])
                self.actions = a0 + newPath + a2
                region["fixed"] = True
                break

          fixes -= 1
          currentState = state
          statesInOrder = [currentState.getPacmanPosition()]
          finalActions = []
          for action in self.actions:
            if currentState.getFood().count() == 0:
              break
            finalActions.append(action)
            currentState = currentState.generateSuccessor(0, action)
            statesInOrder.append(currentState.getPacmanPosition())
          self.actions = finalActions

        print("Found solution with %d actions" % len(self.actions))
        self.actionIndex = 0

    def getAction(self, state):
        """
        From game.py:
        The Agent will receive a GameState and must return an action from
        Directions.{North, South, East, West, Stop}
        """
        "*** YOUR CODE HERE ***"
        if 'actionIndex' not in dir(self): self.actionIndex = 0
        i = self.actionIndex
        self.actionIndex += 1
        if i < len(self.actions):
            return self.actions[i]
        else:
            return Directions.STOP

    def findPathToClosestDot(self, gameState, costFunc=lambda x: 1):
        problem = AnyFoodSearchProblem(gameState, costFunc)
        pathToClosestDot = search.astar(problem)
        return pathToClosestDot

def mazeDistance(point1, point2, gameState):
    """
    Returns the maze distance between any two points, using the search functions
    you have already built.  The gameState can be any game state -- Pacman's position
    in that state is ignored.

    Example usage: mazeDistance( (2,4), (5,6), gameState)

    This might be a useful helper function for your ApproximateSearchAgent.
    """
    x1, y1 = point1
    x2, y2 = point2
    walls = gameState.getWalls()
    assert not walls[x1][y1], 'point1 is a wall: ' + str(point1)
    assert not walls[x2][y2], 'point2 is a wall: ' + str(point2)
    prob = PositionSearchProblem(gameState, start=point1, goal=point2, warn=False)
    return len(search.bfs(prob))

