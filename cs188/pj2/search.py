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
import logic
import game

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

    def getGhostStartStates(self):
        """
        Returns a list containing the start state for each ghost.
        Only used in problems that use ghosts (FoodGhostSearchProblem)
        """
        util.raiseNotDefined()

    def terminalTest(self, state):
        """
          state: Search state

        Returns True if and only if the state is a valid goal state.
        """
        util.raiseNotDefined()
        
    def getGoalState(self):
        """
        Returns goal state for problem. Note only defined for problems that have
        a unique goal state such as PositionSearchProblem
        """
        util.raiseNotDefined()

    def result(self, state, action):
        """
        Given a state and an action, returns resulting state and step cost, which is
        the incremental cost of moving to that successor.
        Returns (next_state, cost)
        """
        util.raiseNotDefined()

    def actions(self, state):
        """
        Given a state, returns available actions.
        Returns a list of actions
        """        
        util.raiseNotDefined()

    def getCostOfActions(self, actions):
        """
         actions: A list of actions to take

        This method returns the total cost of a particular sequence of actions.
        The sequence must be composed of legal moves.
        """
        util.raiseNotDefined()

    def getWidth(self):
        """
        Returns the width of the playable grid (does not include the external wall)
        Possible x positions for agents will be in range [1,width]
        """
        util.raiseNotDefined()

    def getHeight(self):
        """
        Returns the height of the playable grid (does not include the external wall)
        Possible y positions for agents will be in range [1,height]
        """
        util.raiseNotDefined()

    def isWall(self, position):
        """
        Return true if position (x,y) is a wall. Returns false otherwise.
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


def atLeastOne(expressions) :
    """
    Given a list of logic.Expr instances, return a single logic.Expr instance in CNF (conjunctive normal form)
    that represents the logic that at least one of the expressions in the list is true.
    >>> A = logic.PropSymbolExpr('A');
    >>> B = logic.PropSymbolExpr('B');
    >>> symbols = [A, B]
    >>> atleast1 = atLeastOne(symbols)
    >>> model1 = {A:False, B:False}
    >>> print logic.pl_true(atleast1,model1)
    False
    >>> model2 = {A:False, B:True}
    >>> print logic.pl_true(atleast1,model2)
    True
    >>> model3 = {A:True, B:True}
    >>> print logic.pl_true(atleast1,model2)
    True
    """
    "*** YOUR CODE HERE ***"
    if len(expressions) == 1:
        return expressions[0]
    return logic.Expr("|", *expressions)


def atLeastOneAsList(expressions):
    return [atLeastOne(expressions)]


def atMostOneAsList(expressions):
    return [~(expressions[i]) | ~(expressions[j]) for i in range(len(expressions)) for j in range(i+1, len(expressions))]


def atMostOne(expressions) :
    """
    Given a list of logic.Expr instances, return a single logic.Expr instance in CNF (conjunctive normal form)
    that represents the logic that at most one of the expressions in the list is true.
    """
    "*** YOUR CODE HERE ***"
    return logic.Expr("&", *(atMostOneAsList(expressions)))


def exactlyOneAsList(expressions):
    return atMostOneAsList(expressions) + atLeastOneAsList(expressions)


def exactlyOne(expressions) :
    """
    Given a list of logic.Expr instances, return a single logic.Expr instance in CNF (conjunctive normal form)
    that represents the logic that exactly one of the expressions in the list is true.
    """
    "*** YOUR CODE HERE ***"
    if len(expressions) == 1:
        return expressions[0]
    return logic.Expr("&", *(exactlyOneAsList(expressions)))


def extractActionSequence(model, actions):
    """
    Convert a model in to an ordered list of actions.
    model: Propositional logic model stored as a dictionary with keys being
    the symbol strings and values being Boolean: True or False
    Example:
    >>> model = {"North[2]":True, "P[3,4,1]":True, "P[3,3,1]":False, "West[0]":True, "GhostScary":True, "West[2]":False, "South[1]":True, "East[0]":False}
    >>> actions = ['North', 'South', 'East', 'West']
    >>> plan = extractActionSequence(model, actions)
    >>> print plan
    ['West', 'South', 'North']
    """
    "*** YOUR CODE HERE ***"
    plan = []
    for key in model:
        syminfo = logic.PropSymbolExpr.parseExpr(key)
        if model[key] and isinstance(syminfo, tuple) and isinstance(syminfo[1], str) and syminfo[0] in actions:
            plan.append(syminfo)
    return [action for action, time in sorted(plan, key=lambda pair: int(pair[1]))]


def isReachable(state, start, goal, time, limit):
    return (time >= util.manhattanDistance(start, state) and
            limit - time >= util.manhattanDistance(state, goal))


def positionLogicPlan(problem):
    """
    Given an instance of a PositionSearchProblem, return a list of actions that lead to the goal.
    Available actions are game.Directions.{NORTH,SOUTH,EAST,WEST}
    Note that STOP is not an available action.
    """
    "*** YOUR CODE HERE ***"
    DIRS = [game.Directions.NORTH, game.Directions.SOUTH, game.Directions.EAST, game.Directions.WEST]
    start, goal = problem.getStartState(), problem.getGoalState()
    minTime, maxTime = util.manhattanDistance(start, goal), problem.getWidth() * problem.getHeight()

    # we want a plan of length T, trying to minimize T
    for T in range(minTime, maxTime):

        # literals for start and goal states must be true
        clauses = [logic.PropSymbolExpr("Pos", start[0], start[1], 0)]
        clauses.append(logic.PropSymbolExpr("Pos", goal[0], goal[1], T))

        # this for-loop does the real work to check if there exists a plan of length T
        for t in range(T+1):

            # exactly one action per time step (i.e. one of North[5], South[5], East[5], West[5])
            if t < T:
                clauses.extend(exactlyOneAsList([logic.PropSymbolExpr(direc, t) for direc in DIRS]))

            # implications for every position at every time step
            for x in range(1, problem.getWidth()+1):
                for y in range(1, problem.getHeight()+1):
                    state = (x,y)

                    # if state is reachable, we calculate all forward and backward implications
                    if not problem.isWall(state) and isReachable(state, start, goal, t, T):
                        currPos = logic.PropSymbolExpr("Pos", x, y, t) # i.e. Pos[3,6,5]
                        forwardActions = problem.actions(state)
                        actionsToGetHere = []

                        if t < T:
                            for action in forwardActions:
                                ((nx, ny), cost) = problem.result(state, action)
                                rawAct = logic.PropSymbolExpr(action, t)
                                currAct = logic.PropSymbolExpr(action, x, y, t)
                                nextPos = logic.PropSymbolExpr("Pos", nx, ny, t+1)
                                axiom1 = currAct >> rawAct  # i.e. West[3,6,5] implies West[5]
                                axiom5 = currAct >> currPos # i.e. West[3,6,5] implies Pos[3,6,5]
                                axiom2 = currAct >> nextPos # i.e. West[3,6,5] implies Pos[2,6,6]
                                axiom1, axiom2, axiom5 = logic.to_cnf(axiom1), logic.to_cnf(axiom2), logic.to_cnf(axiom5)
                                clauses.extend([axiom1, axiom2, axiom5])
                            onlyOneForwardAction = exactlyOne([logic.PropSymbolExpr(direc, x, y, t) for direc in forwardActions])
                            axiom3 = currPos >> onlyOneForwardAction # i.e. Pos[3,6,5] implies North[3,6,5] or West[3,6,5] if South and East are walls
                            axiom3 = logic.to_cnf(axiom3)
                            clauses.append(axiom3)

                        # we have to consider t in [1,T] for backward implications, instead of t in [0,T-1]
                        if t > 0:
                            for action in forwardActions:
                                ((px, py), cost) = problem.result(state, action)
                                if isReachable((px, py), start, goal, t-1, T):
                                    actionsToGetHere.append(logic.PropSymbolExpr(game.Directions.REVERSE[action], px, py, t-1))
                            onlyOneActionToGetHere = exactlyOne(actionsToGetHere)
                            axiom4 = currPos >> onlyOneActionToGetHere # i.e. Pos[3,6,5] implies South[3,7,4] or East[2,6,4] if South and East are walls
                            axiom4 = logic.to_cnf(axiom4)
                            clauses.append(axiom4)

        model = logic.pycoSAT(clauses)
        if model:
            plan = extractActionSequence(model, DIRS)
            return plan

    print("NO PLAN, THIS SHOULD NOT BE HAPPENING")
    return [] # should never get here if maze has solution


def isReachableFromStart(state, start, time):
    return time >= util.manhattanDistance(start, state)


def isReachableFromGoal(state, goal, time, limit):
    return limit - time >= util.manhattanDistance(state, goal)


def foodLogicPlan(problem):
    """
    Given an instance of a FoodSearchProblem, return a list of actions that help Pacman
    eat all of the food.
    Available actions are game.Directions.{NORTH,SOUTH,EAST,WEST}
    Note that STOP is not an available action.
    """
    "*** YOUR CODE HERE ***"
    DIRS = [game.Directions.NORTH, game.Directions.SOUTH, game.Directions.EAST, game.Directions.WEST]
    startPos, startGrid = problem.getStartState()
    minTime, maxTime = 0, 51 # change these numbers for debugging
    for x in range(1, problem.getWidth()+1):
        for y in range(1, problem.getHeight()+1):
            if startGrid[x][y]: minTime += 1

    for T in range(minTime, maxTime):
        #print("Trying to find a solution of length %d" % T)

        clauses = []   # one action per time step
        clauses2 = []  # whether food is at (x,y) at start
        clauses3 = []  # whether food is at (x,y) at goal (should all be false)
        clauses4 = []  # action at specific time and place implies the same action at that time overall
        clauses5 = []  # action at specific time and place implies position at that time and place
        clauses6 = []  # action at specific time and place implies next position
        clauses7 = []  # only one forward action per position per time step
        clauses8 = []  # only one possible action to get to this position per time step
        clauses9 = []  # exactly one goal position
        clauses10 = [] # position at specific time and place implies no food at that time and place
        clauses12 = [] # food at specific time and place implies food prior in same place
        clauses13 = [] # food at specific time and place and no Pacman at that place in the next step implies food still there

        clauses9.append(logic.PropSymbolExpr("Pos", startPos[0], startPos[1], 0))

        for t in range(T+1):
            if t < T:
                clauses.extend(exactlyOneAsList([logic.PropSymbolExpr(direc, t) for direc in DIRS]))

            # Pacman must be in one of the initial food pellet positions in the end
            goalPositionAxioms = []
            goalPositions = []

            for x in range(1, problem.getWidth()+1):
                for y in range(1, problem.getHeight()+1):
                    state = (x,y)

                    # only add these axioms once
                    if t == 0:
                        if startGrid[x][y]:
                            clauses2.append(logic.PropSymbolExpr("Food", x, y, 0)) # food here initially
                            goalPositionAxioms.append(logic.PropSymbolExpr("Pos", x, y, T)) # Pacman could end here
                            goalPositions.append(state)
                        else:
                            clauses2.append(~logic.PropSymbolExpr("Food", x, y, 0)) # no food here initially
                        clauses3.append(~logic.PropSymbolExpr("Food", x, y, T)) # no food at every position in the end

                    # if state is not a wall, we calculate all forward and backward implications for position AND food
                    if not problem.isWall(state):
                        currPos = logic.PropSymbolExpr("Pos", x, y, t)           # i.e.  Pos[3,6,5]
                        currPosNextTime = logic.PropSymbolExpr("Pos", x, y, t+1) # i.e.  Pos[3,6,6]
                        currFood = logic.PropSymbolExpr("Food", x, y, t)         # i.e. Food[3,6,5]
                        nextFood = logic.PropSymbolExpr("Food", x, y, t+1)       # i.e. Food[3,6,6]
                        forwardActions = problem.actions((state, startGrid))
                        actionsToGetHere = []

                        if t < T:
                            for action in forwardActions:
                                (((nx, ny), nfood), cost) = problem.result((state, startGrid), action)
                                rawAct = logic.PropSymbolExpr(action, t)
                                currAct = logic.PropSymbolExpr(action, x, y, t)
                                nextPos = logic.PropSymbolExpr("Pos", nx, ny, t+1)
                                axiom1 = currAct >> rawAct            # i.e. West[3,6,5] implies West[5]
                                axiom5 = currAct >> currPos           # i.e. West[3,6,5] implies Pos[3,6,5]
                                axiom2 = currAct >> nextPos           # i.e. West[3,6,5] implies Pos[2,6,6]
                                axiom1, axiom2, axiom5 = logic.to_cnf(axiom1), logic.to_cnf(axiom2), logic.to_cnf(axiom5)
                                clauses4.append(axiom1)
                                clauses5.append(axiom5)
                                clauses6.append(axiom2)
                            onlyOneForwardAction = exactlyOne([logic.PropSymbolExpr(direc, x, y, t) for direc in forwardActions])
                            axiom3 = currPos >> onlyOneForwardAction           # i.e. Pos[3,6,5] implies North[3,6,5] or West[3,6,5] if South and East are walls
                            axiom8 = nextFood >> currFood                      # i.e. Food[3,6,5] implies Food[3,6,4]
                            axiom9 = (currFood & ~currPosNextTime) >> nextFood # i.e. Food[3,6,5] and not Pos[3,6,6] implies Food[3,6,6]
                            axiom3 = logic.to_cnf(axiom3)
                            axiom8 = logic.to_cnf(axiom8)
                            axiom9 = logic.to_cnf(axiom9)
                            clauses7.append(axiom3)
                            clauses12.append(axiom8)
                            clauses13.append(axiom9)

                        axiom6 = currPos >> ~currFood # i.e. Pos[3,6,5] implies not Food[3,6,5]
                        axiom6 = logic.to_cnf(axiom6)
                        clauses10.append(axiom6)

                        # we have to consider t in [1,T] for backward implications, instead of t in [0,T-1]
                        if t > 0:
                            for action in forwardActions:
                                (((px, py), pfood), cost) = problem.result((state, startGrid), action)
                                if not isReachableFromStart((px, py), startPos, t-1):
                                    continue
                                willAppend = True
                                for goalPos in goalPositions:
                                    if not isReachableFromGoal((px, py), goalPos, t-1, T):
                                        willAppend = False
                                if willAppend:
                                    actionsToGetHere.append(logic.PropSymbolExpr(game.Directions.REVERSE[action], px, py, t-1))
                            onlyOneActionToGetHere = exactlyOne(actionsToGetHere)
                            axiom4 = currPos >> onlyOneActionToGetHere # i.e. Pos[3,6,5] implies South[3,7,4] or East[2,6,4] if South and East are walls
                            axiom4 = logic.to_cnf(axiom4)
                            clauses8.append(axiom4)

            # Pacman must be in one of the initial food pellet positions in the end
            if t == 0:
                clauses9.extend(exactlyOneAsList(goalPositionAxioms))

        clauses += clauses2 + clauses3 + clauses4 + clauses5 + clauses6 + clauses7 + clauses8 + clauses9 + clauses10 + clauses12 + clauses13
        model = logic.pycoSAT(clauses)
        if model:
            plan = extractActionSequence(model, DIRS)
            #print("Found plan of length %d" % len(plan))
            #print plan
            return plan

    print("NO PLAN, THIS SHOULD NOT BE HAPPENING")
    return [] # should never get here if maze has solution

def foodGhostLogicPlan(problem):
    """
    Given an instance of a FoodGhostSearchProblem, return a list of actions that help Pacman
    eat all of the food and avoid patrolling ghosts.
    Ghosts only move east and west. They always start by moving East, unless they start next to
    and eastern wall. 
    Available actions are game.Directions.{NORTH,SOUTH,EAST,WEST}
    Note that STOP is not an available action.
    """
    "*** YOUR CODE HERE ***"
    DIRS = [game.Directions.NORTH, game.Directions.SOUTH, game.Directions.EAST, game.Directions.WEST]
    startPos, startGrid = problem.getStartState()
    minTime, maxTime = 0, 51 # change these numbers for debugging
    for x in range(1, problem.getWidth()+1):
        for y in range(1, problem.getHeight()+1):
            if startGrid[x][y]: minTime += 1

    for T in range(minTime, maxTime):
        #print("Trying to find a solution of length %d" % T)

        clauses = []   # one action per time step
        clauses2 = []  # whether food is at (x,y) at start
        clauses3 = []  # whether food is at (x,y) at goal (should all be false)
        clauses4 = []  # action at specific time and place implies the same action at that time overall
        clauses5 = []  # action at specific time and place implies position at that time and place
        clauses6 = []  # action at specific time and place implies next position
        clauses7 = []  # only one forward action per position per time step
        clauses8 = []  # only one possible action to get to this position per time step
        clauses9 = []  # exactly one goal position
        clauses10 = [] # position at specific time and place implies no food at that time and place
        clauses12 = [] # food at specific time and place implies food prior in same place
        clauses13 = [] # food at specific time and place and no Pacman at that place in the next step implies food still there
        clauses14 = [] # JESUS whether ghost/s are at (x,y) at start
        clauses15 = [] # JESUS turnAround ghosts
        clauses16 = [] # JESUS nextGhostW >> ~currAct
        clauses17 = [] # JESUS nextGhostE >> ~currAct
        clauses18 = [] # JESUS ghost go east
        clauses19 = [] # JESUS ghost go weast
        clauses20 = [] # JESUS ghost 
        clauses21 = [] # JESUS ghost 

        clauses9.append(logic.PropSymbolExpr("Pos", startPos[0], startPos[1], 0))

        #JESUS
        for ghost in problem.getGhostStartStates():
            clauses14.append(logic.PropSymbolExpr("Ge", ghost.getPosition()[0], ghost.getPosition()[1], 0))

        for t in range(T+1):
            if t < T:
                clauses.extend(exactlyOneAsList([logic.PropSymbolExpr(direc, t) for direc in DIRS]))

            # Pacman must be in one of the initial food pellet positions in the end
            goalPositionAxioms = []
            goalPositions = []

            for x in range(1, problem.getWidth()+1):
                for y in range(1, problem.getHeight()+1):
                    state = (x,y)

                    #JESUS; All the Way East
                    if (problem.isWall((x+1, y))):
                        borderGhostE = logic.PropSymbolExpr("Ge", x, y, t)
                        turnAroundE = logic.PropSymbolExpr("Gw", x - 1, y, t + 1)
                        clauses15.append(logic.to_cnf(borderGhostE >> turnAroundE))  # Ge[3, 2, 3] >> Gw[2, 2, 4] if at east wall
                    else:
                        eastG = logic.PropSymbolExpr("Ge", x, y, t)
                        easterG = logic.PropSymbolExpr("Ge", x + 1, y, t + 1)
                        clauses18.append(logic.to_cnf(eastG >> easterG))

                    #JESUS; All the Way West
                    if (problem.isWall((x-1, y))):
                        borderGhostW = logic.PropSymbolExpr("Gw", x, y, t)
                        turnAroundW = logic.PropSymbolExpr("Ge", x + 1, y, t + 1)
                        clauses15.append(logic.to_cnf(borderGhostW >> turnAroundW))  # Gw[1, 2, 3] >> Gw[2, 2, 4] if at west wall
                    else:
                        westG = logic.PropSymbolExpr("Gw", x, y, t)
                        westerG = logic.PropSymbolExpr("Gw", x - 1, y, t + 1)
                        clauses19.append(logic.to_cnf(westG >> westerG))

                    # only add these axioms once
                    if t == 0:
                        if startGrid[x][y]:
                            clauses2.append(logic.PropSymbolExpr("Food", x, y, 0)) # food here initially
                            goalPositionAxioms.append(logic.PropSymbolExpr("Pos", x, y, T)) # Pacman could end here
                            goalPositions.append(state)
                        else:
                            clauses2.append(~logic.PropSymbolExpr("Food", x, y, 0)) # no food here initially
                        clauses3.append(~logic.PropSymbolExpr("Food", x, y, T)) # no food at every position in the end

                    # if state is not a wall, we calculate all forward and backward implications for position AND food
                    if not problem.isWall(state):
                        currPos = logic.PropSymbolExpr("Pos", x, y, t)           # i.e.  Pos[3,6,5]
                        currPosNextTime = logic.PropSymbolExpr("Pos", x, y, t+1) # i.e.  Pos[3,6,6]
                        currFood = logic.PropSymbolExpr("Food", x, y, t)         # i.e.  Food[3,6,5]
                        nextFood = logic.PropSymbolExpr("Food", x, y, t+1)       # i.e.  Food[3,6,6]
                        forwardActions = problem.actions((state, startGrid))
                        actionsToGetHere = []

                        if t < T:
                            for action in forwardActions:
                                (((nx, ny), nfood), cost) = problem.result((state, startGrid), action)
                                rawAct = logic.PropSymbolExpr(action, t)
                                currAct = logic.PropSymbolExpr(action, x, y, t)
                                nextPos = logic.PropSymbolExpr("Pos", nx, ny, t+1)

                                nextGhostE = logic.PropSymbolExpr("Ge", nx, ny, t)       # JESUS Ge[3, 6, 5]
                                nextGhostW = logic.PropSymbolExpr("Gw", nx, ny, t)       # JESUS Gw[3, 6, 5]
                                waitingGhostNextE = logic.PropSymbolExpr("Gw", nx, ny, t + 1)
                                waitingGhostNextW = logic.PropSymbolExpr("Ge", nx, ny, t + 1)
                                clauses16.append(logic.to_cnf(nextGhostW >> ~currAct))   # JESUS Gw[3, 6, 5] >> ~rawAct
                                clauses17.append(logic.to_cnf(nextGhostE >> ~currAct))   # JESUS GE[3, 6 ,5] >> ~rawAct

                                clauses20.append(logic.to_cnf(waitingGhostNextW >> ~currAct))   # JESUS Gw[3, 6, 5] >> ~rawAct
                                clauses21.append(logic.to_cnf(waitingGhostNextE >> ~currAct))   # JESUS Gw[3, 6, 5] >> ~rawAct

                                axiom1 = currAct >> rawAct            # i.e. West[3,6,5] implies West[5]
                                axiom5 = currAct >> currPos           # i.e. West[3,6,5] implies Pos[3,6,5]
                                axiom2 = currAct >> nextPos           # i.e. West[3,6,5] implies Pos[2,6,6]
                                axiom1, axiom2, axiom5 = logic.to_cnf(axiom1), logic.to_cnf(axiom2), logic.to_cnf(axiom5)
                                clauses4.append(axiom1)
                                clauses5.append(axiom5)
                                clauses6.append(axiom2)
                            onlyOneForwardAction = exactlyOne([logic.PropSymbolExpr(direc, x, y, t) for direc in forwardActions])
                            axiom3 = currPos >> onlyOneForwardAction           # i.e. Pos[3,6,5] implies North[3,6,5] or West[3,6,5] if South and East are walls
                            axiom8 = nextFood >> currFood                      # i.e. Food[3,6,5] implies Food[3,6,4]
                            axiom9 = (currFood & ~currPosNextTime) >> nextFood # i.e. Food[3,6,5] and not Pos[3,6,6] implies Food[3,6,6]
                            axiom3 = logic.to_cnf(axiom3)
                            axiom8 = logic.to_cnf(axiom8)
                            axiom9 = logic.to_cnf(axiom9)
                            clauses7.append(axiom3)
                            clauses12.append(axiom8)
                            clauses13.append(axiom9)

                        axiom6 = currPos >> ~currFood # i.e. Pos[3,6,5] implies not Food[3,6,5]
                        axiom6 = logic.to_cnf(axiom6)
                        clauses10.append(axiom6)

                        # we have to consider t in [1,T] for backward implications, instead of t in [0,T-1]
                        if t > 0:
                            for action in forwardActions:
                                (((px, py), pfood), cost) = problem.result((state, startGrid), action)
                                if not isReachableFromStart((px, py), startPos, t-1):
                                    continue
                                willAppend = True
                                for goalPos in goalPositions:
                                    if not isReachableFromGoal((px, py), goalPos, t-1, T):
                                        willAppend = False
                                if willAppend:
                                    actionsToGetHere.append(logic.PropSymbolExpr(game.Directions.REVERSE[action], px, py, t-1))
                            onlyOneActionToGetHere = exactlyOne(actionsToGetHere)
                            axiom4 = currPos >> onlyOneActionToGetHere # i.e. Pos[3,6,5] implies South[3,7,4] or East[2,6,4] if South and East are walls
                            axiom4 = logic.to_cnf(axiom4)
                            clauses8.append(axiom4)

            # Pacman must be in one of the initial food pellet positions in the end
            if t == 0:
                clauses9.extend(exactlyOneAsList(goalPositionAxioms))

        clauses += clauses2 + clauses3 + clauses4 + clauses5 + clauses6 + clauses7 + clauses8 + clauses9 + clauses10 + clauses12 + clauses13 + clauses14 + clauses15 + clauses16 + clauses17 + clauses18 + clauses19 + clauses20 + clauses21
        model = logic.pycoSAT(clauses)
        if model:
            plan = extractActionSequence(model, DIRS)
            return plan

    print("NO PLAN, THIS SHOULD NOT BE HAPPENING")
    return [] # should never get here if maze has solution


# Abbreviations
plp = positionLogicPlan
flp = foodLogicPlan
fglp = foodGhostLogicPlan

# Some for the logic module uses pretty deep recursion on long expressions
sys.setrecursionlimit(100000)



