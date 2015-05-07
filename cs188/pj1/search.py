# search.py
# ---------
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


"""
In search.py, you will implement generic search algorithms which are called by
Pacman agents (in searchAgents.py).
"""

import util
import sys
import copy

class SearchProblem:
    """
    This class outlines the structure of a search problem, but doesn't implement
    any of the methods (in object-oriented terminology: an abstract class).

    You do not need to change anything in this class, ever.
    """

    def getStartState(self):
        """
        Returns the start state for the search problem.
        """
        util.raiseNotDefined()

    def isGoalState(self, state):
        """
          state: Search state

        Returns True if and only if the state is a valid goal state.
        """
        util.raiseNotDefined()

    def getSuccessors(self, state):
        """
          state: Search state

        For a given state, this should return a list of triples, (successor,
        action, stepCost), where 'successor' is a successor to the current
        state, 'action' is the action required to get there, and 'stepCost' is
        the incremental cost of expanding to that successor.
        """
        util.raiseNotDefined()

    def getCostOfActions(self, actions):
        """
         actions: A list of actions to take

        This method returns the total cost of a particular sequence of actions.
        The sequence must be composed of legal moves.
        """
        util.raiseNotDefined()


def tinyMazeSearch(problem):
    """
    Returns a sequence of moves that solves tinyMaze.  For any other maze, the
    sequence of moves will be incorrect, so only use this for tinyMaze.
    """
    from game import Directions
    s = Directions.SOUTH
    w = Directions.WEST
    return  [s, s, w, s, w, w, s, w]

def breadthFirstSearch(problem):
    """
    Search the shallowest nodes in the search tree first.

    You are not required to implement this, but you may find it useful for Q5.
    """
    "*** YOUR CODE HERE ***"
    startState = problem.getStartState()
    explored = set()
    frontier = [startState]
    previous = dict()
    g = {startState: 0}
    while len(frontier) != 0:
        currState = frontier.pop(0)
        if problem.isGoalState(currState):
            return getActions(previous, currState)
        explored.add(currState)
        for state, action, cost in problem.getSuccessors(currState):
            if state in explored:
                continue
            gScore = g[currState] + cost
            if state not in frontier or gScore < g[state]:
                previous[state] = (currState, action)
                g[state] = gScore
                if state not in frontier:
                    frontier.append(state)
    return [] # should never get here

def nullHeuristic(state, problem=None):
    """
    A heuristic function estimates the cost from the current state to the nearest
    goal in the provided SearchProblem.  This heuristic is trivial.
    """
    return 0

visited = []
moreChildren = False
oneTruePath = []
oneTrueDepth = 0

def iterativeDeepeningSearch(problem):
    """
    Perform DFS with increasingly larger depth.

    Begin with a depth of 1 and increment depth by 1 at every step.
    """
    "*** YOUR CODE HERE ***"
    global visited
    global moreChildren
    global oneTrueDepth
    global oneTruePath

    visited = []
    moreChildren = False
    oneTruePath = []
    oneTrueDepth = 0

    root = problem.getStartState()
    moreChildren = True

    while moreChildren:
        moreChildren = False
        idHelper(problem, oneTrueDepth, root, [])
        if oneTruePath != []:
            return oneTruePath
        oneTrueDepth += 1
        visited = []
    return oneTruePath

def idHelper(problem, depth, node, path):
    global visited
    global oneTruePath
    global moreChildren

    justVisited = []

    if (oneTruePath != []):
        return

    if depth == 0:
        mohicans = problem.getSuccessors(node)
        if mohicans != []:
            moreChildren = True
            for successor in mohicans:
                if problem.isGoalState(successor[0]):
                    path.append(successor[1])
                    oneTruePath = path
        if problem.isGoalState(node):
            oneTruePath = path
        return

    successors = problem.getSuccessors(node)
    for successor in reversed(successors):
        if successor[0] not in visited:
            visited.append(successor[0])
            justVisited.append(successor[0])
    for successor in reversed(successors):
        savePath = path[:]
        if (successor[0] in visited):
            if successor[0] not in justVisited:
                continue
        visited.append(successor[0])
        savePath.append(successor[1])
        idHelper(problem, depth - 1, successor[0], savePath)
        if (oneTruePath != []):
            return


def aStarSearch(problem, heuristic=nullHeuristic):
    """Search the node that has the lowest combined cost and heuristic first."""
    "*** YOUR CODE HERE ***"
    startState = problem.getStartState()
    explored = set()
    frontier = set()
    frontier.add(startState)
    previous = dict()
    g = {startState: 0}
    def fScore(state):
        gScore = g[state]
        hScore = heuristic(state, problem)
        return gScore + hScore
    f = {startState: fScore(startState)}
    while len(frontier) != 0:
        currState = min(frontier, key=fScore)
        if problem.isGoalState(currState):
            return getActions(previous, currState)
        frontier.remove(currState)
        explored.add(currState)
        for state, action, cost in problem.getSuccessors(currState):
            if state in explored:
                continue
            gScore = g[currState] + cost
            if state not in frontier or gScore < g[state]:
                previous[state] = (currState, action)
                g[state] = gScore
                f[state] = fScore(state)
                if state not in frontier:
                    frontier.add(state)
    return [] # should never get here

def getActions(mapping, state):
    if state in mapping:
        prevState, action = mapping[state]
        actions = getActions(mapping, prevState)
        return actions + [action]
    else:
        return []

# Abbreviations
bfs = breadthFirstSearch
astar = aStarSearch
ids = iterativeDeepeningSearch
