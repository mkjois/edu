/* MachinePlayer.java */

package player;

import java.util.Arrays;
import board.*;
import list.*;
import dict.*;

/**
 *  An implementation of an automatic Network player.  Keeps track of moves
 *  made by both players.  Can select a move for itself.
 */
public class MachinePlayer extends Player {

    private static final int DEFAULT_DEPTH = 3;
    private static final double MINSCORE = -1.0;
    private static final double MAXSCORE = 1.0;
    private static final double POSINFINITY = 2.0;
    private static final double NEGINFINITY = -2.0;

    private Board board;
    private HashTable fulls, partials;
    private int color, depth, turns;
    private boolean userDepth;
    private DList stepMoves;
  
    // Creates a machine player with the given color.  Color is either 0 (black)
    // or 1 (white).  (White has the first move.)
    public MachinePlayer(int color) {
        this(color, DEFAULT_DEPTH);
        this.userDepth = false;
    }
  
    // Creates a machine player with the given color and search depth.  Color is
    // either 0 (black) or 1 (white).  (White has the first move.)
    public MachinePlayer(int color, int searchDepth) {
        this.board = new Board();
        this.color = color;
        this.depth = searchDepth;
        this.userDepth = true;
        this.turns = 0;
        this.stepMoves = new DList();
        this.generatePartialTable();
    }

    private void generatePartialTable () {
        if (this.board.chipsNotOnBoard() > this.depth) {
            double possibleMoves = (double) (Board.SIZE * Board.SIZE - 4);
            int buckets = (int) (Math.pow(possibleMoves, this.depth));
            this.partials = new HashTable(buckets);
        } else {
            this.partials = new HashTable(1); // "add moves" will not be at evaluation depth
        }
    }

    private void adjustDepth () {
        if (this.userDepth) {
            return;
        }
        if (this.turns < 18) {
            this.depth = 3;
        } else {
            this.depth = 2;
        }
    }
  
    // Returns a new move by "this" player.  Internally records the move (updates
    // the internal game board) as a move by "this" player.
    public Move chooseMove() {
        Best bestMove = new Best();
        if (this.turns < 4) { // Places first 2 chips in goal areas
            double rand = Math.random();
            if (this.color == Board.WHITE) {
                int x = 7, y = Board.SIZE / 2;
                if (this.turns < 2) {
                    x = 0;
                }
                if (rand > 0.5) {
                    y -= 1;
                }
                bestMove = new Best(new Move(x, y));
            } else if (this.color == Board.BLACK) {
                int x = Board.SIZE / 2, y = 7;
                if (this.turns < 2) {
                    y = 0;
                }
                if (rand > 0.5) {
                    x -= 1;
                }
                bestMove = new Best(new Move(x, y));
            }
        } else {
            this.generatePartialTable();
            this.adjustDepth();
            bestMove = this.minimax(this.depth, MINSCORE, MAXSCORE, true);
        }

        try {
            this.board.makeMove(this.color, bestMove.move);
            this.turns++;
            if (bestMove.move.moveKind == Move.STEP) {
                this.stepMoves.add(bestMove.move);
            }
            if (this.fulls != null) {
            }
            return bestMove.move;
        } catch (InvalidMoveException e) {
            return new Move();
        }
    } 
  
    // If the Move m is legal, records the move as a move by the opponent
    // (updates the internal game board) and returns true.  If the move is
    // illegal, returns false without modifying the internal state of "this"
    // player.  This method allows your opponents to inform you of their moves.
    public boolean opponentMove(Move m) {
        try {
            this.board.makeMove(Board.otherPlayer(this.color), m);
            this.turns++;
            return true;
        } catch (InvalidMoveException e) {
            return false;
        }
    }
  
    // If the Move m is legal, records the move as a move by "this" player
    // (updates the internal game board) and returns true.  If the move is
    // illegal, returns false without modifying the internal state of "this"
    // player.  This method is used to help set up "Network problems" for your
    // player to solve.
    public boolean forceMove(Move m) {
        try {
            this.board.makeMove(this.color, m);
            this.turns++;
            return true;
        } catch (InvalidMoveException e) {
            return false;
        }
    }

    private double eval () {
        if (this.board.isFull()) {
            return this.evalFullBoard();
        } else {
            return this.evalPartialBoard();
        }
    }

    private double evalFullBoard () {
        if (this.fulls == null) {
            this.fulls = new HashTable(100000);
        }
        Entry pair = this.fulls.find(this.board);
        if (pair != null) {
            double score = (double) pair.value();
            return score;
        }
        double score = this.evalBoard();
        this.fulls.insert(this.board, score);
        return score;
    }

    private double evalPartialBoard () {
        Entry pair = this.partials.find(this.board);
        if (pair != null) {
            return (double) pair.value();
        }
        double score = this.evalBoard();
        this.partials.insert(this.board, score);
        return score;
    }

    /**
     * eval()
     * Assigns a score between -1.0 and +1.0 for this board and player.
     * A more positive score means that this player has a higher chance
     * of winning the game on the current board, while a more negative score
     * indicates the opponent has a higher chance of winning. A score with
     * a magnitude of 1.0 indicates someone has won the game. Used by
     * minimax() and assumes that neither player has won the game.
     *
     * @return A double between -1.0 and +1.0 (exclusive) that effectively
     *     assesses how likely it will be for this player to win the
     *     game, assuming infinite intelligence on both sides.
     */
    private double evalBoard () {
        int[] blackScores = this.board.propertyScores(Board.BLACK);
        int[] whiteScores = this.board.propertyScores(Board.WHITE);
        double[] scores = new double[3];
        scores[0] = this.connectionScore(blackScores[0], whiteScores[0]);
        scores[1] = this.triangleScore(blackScores[1], whiteScores[1]);
        scores[2] = this.bothGoalScore(blackScores[2],
                                       blackScores[3],
                                       whiteScores[2],
                                       whiteScores[3]);
        return avgArray(scores);
    }

    private double connectionScore (int blackCon, int whiteCon) {
        int totalCon = blackCon + whiteCon;
        double blackProp = (double) blackCon / totalCon;
        double whiteProp = (double) whiteCon / totalCon;
        if (this.color == Board.BLACK) {
            return blackProp - whiteProp;
        } else if (this.color == Board.WHITE) {
            return whiteProp - blackProp;
        } else {
            return 0.0;
        }
    }

    private double triangleScore (int blackTri, int whiteTri) {
        int diff = 0;
        if (this.color == Board.BLACK) {
            diff = blackTri - whiteTri;
        } else if (this.color == Board.WHITE) {
            diff = whiteTri - blackTri;
        }
        return (double) diff / (Math.abs(diff) + 1);
    }

    private double bothGoalScore (int bs, int be, int ws, int we) {
        double blackGoal = goalScore(bs, be);
        double whiteGoal = goalScore(ws, we);
        if (this.color == Board.BLACK) {
            whiteGoal = -1.0 * whiteGoal;
        } else if (this.color == Board.WHITE) {
            blackGoal = -1.0 * blackGoal;
        }
        return (blackGoal + whiteGoal) / 2;
    }

    private static double goalScore (int start, int end) {
        double max = MAXSCORE, min = MINSCORE;
        if (start==0) {
            if (end==0) { return min; }
            else if (end==1) { return 0.0; }
            else if (end==2) { return min/4; }
            else if (end==3) { return min/2; }
            else if (end==4) { return 3*min/4; }
            else { return min; }
        } else if (start==1) {
            if (end==0) { return 0.0; }
            else if (end==1) { return max; }
            else if (end==2) { return max/2; }
            else if (end==3) { return min/4; }
            else if (end==4) { return 3*min/4; }
            else { return min; }
        } else if (start==2) {
            if (end==0) { return min/4; }
            else if (end==1) { return max/2; }
            else if (end==2) { return 0.0; }
            else if (end==3) { return min/2; }
            else if (end==4) { return min; }
            else { return min; }
        } else if (start==3) {
            if (end==0) { return min/2; }
            else if (end==1) { return min/4; }
            else if (end==2) { return min/2; }
            else if (end==3) { return min; }
            else if (end==4) { return min; }
            else { return min; }
        } else if (start==4) {
            if (end==0) { return 3*min/4; }
            else if (end==1) { return 3*min/4; }
            else if (end==2) { return min; }
            else if (end==3) { return min; }
            else if (end==4) { return min; }
            else { return min; }
        } else { return min; }
    }

    private static double sumArray (double[] array) {
        double result = 0.0;
        for (int i = 0; i < array.length; i++) {
            result += array[i];
        }
        return result;
    }

    private static double avgArray (double[] array) {
        return sumArray(array) / array.length;
    }

    /**
     * minimax(int, double, double, boolean)
     * Returns the Best that gives this player the highest chance of
     * winning this Board using a minimax algorithm for searching
     * game trees. The algorithm will generate a game tree down to the
     * given search depth and evaluate each possible outcome to determine
     * what Move will put the given player closest to winning. Note that
     * the method is guaranteed to return a Best with a valid Move.
     *
     * @param depth The search depth of the game tree generated and
     *     evaluated by the minimax algorithm. When the search depth is
     *     recursively decreased to 0, the current board is evaluated
     *     heuristically and given a score.
     * @param alpha The lowest score this player is guaranteed to achieve. 
     *     This will update if a move is found that guarantees a score
     *     higher than the current alpha.
     * @param beta The highest score the opponent is guaranteed to achieve.
     *     This will update if a move is found that guarantees a score
     *     lower than the current beta.
     * @param orig The original player to call the minimax algorithm. True
     *     if the algorithm is in the process of evaluating moves for this
     *     player's turn, false if in the process of evaluating moves for
     *     the opponent's turn.
     * @return The Best with the best Move to bring the given player closest to
     *     winning. Guaranteed to return a Best with a valid Move.
     */
    private Best minimax (int depth, double alpha, double beta, boolean orig) {
        double evalScore = 0.0;
        boolean baseCase = false;
        if (BoardCheck.winner(this.board, Board.otherPlayer(this.color))) {
            baseCase = true;
            evalScore = MINSCORE;
        } else if (BoardCheck.winner(this.board, this.color)) {
            baseCase = true;
            evalScore = MAXSCORE;
        } else if (depth < 1) {
            baseCase = true;
            evalScore = this.eval();
        }
        if (baseCase) {
            return new Best(evalScore); // junk move
        }

        int player;
        if (orig) {
            player = this.color;
        } else {
            player = Board.otherPlayer(this.color);
        }
        DList allMoves = this.board.allValidMoves(player);
        if (allMoves.isEmpty()) {
            return new Best();
        }
        Move move;
        Best reply, bestMove;
        if (orig) {
            bestMove = new Best((Move) allMoves.get(0), NEGINFINITY);
        } else {
            bestMove = new Best((Move) allMoves.get(0), POSINFINITY);
        }

        try {
            for (Object o : allMoves) {
                move = (Move) o;
                if (move.moveKind == Move.STEP) {
                    boolean repeat = false;
                    for (Object obj : this.stepMoves) {
                        if (moveEquals(move, (Move) obj)) {
                            repeat = true; // STEP moves will never be repeated
                            break;
                        }
                    }
                    if (repeat) {
                        continue;
                    }
                }
                this.board.makeMove(player, move);
                reply = this.minimax(depth-1, alpha, beta, ! orig);
                this.board.undoLastMove();
                if (orig && (reply.score >= bestMove.score)) {
                    bestMove.move = move;
                    bestMove.score = reply.score;
                    alpha = reply.score;
                } else if (!orig && (reply.score <= bestMove.score)) {
                    bestMove.move = move;
                    bestMove.score = reply.score;
                    beta = reply.score;
                }
                if (alpha >= beta) {
                    return bestMove;
                }
            }
        } catch (InvalidMoveException e) {
        }
        return bestMove;
    }

    private static boolean moveEquals(Move m1, Move m2) {
        return ((m1.moveKind == m2.moveKind) &&
               (m1.x1 == m2.x1) && (m1.y1 == m2.y1) &&
               (m1.x2 == m2.x2) && (m1.y2 == m2.y2));
    }
}
