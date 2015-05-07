package board;

import java.math.BigInteger;
import list.*;
import player.*;

/**
 * The internal implementation of the Network game board used by
 * MachinePlayer to keep track of the game.
 */
public class Board {

  public static final int WHITE = 1;
  public static final int BLACK = 0;
  public static final int EMPTY = -1;
  public static final int CORNER = -2;

  public static final int SIZE = 8;

  private static final BigInteger BIGZERO = BigInteger.ZERO;
  private static final BigInteger BIGONE = BigInteger.ONE;
  private static final BigInteger BIGTWO = BIGONE.add(BIGONE);

  protected int[][] board;
  private DList blackChips;
  private DList whiteChips;
  private DList oldMoves;

  /**
   * Board()
   * Constructs a Board object with empty squares and four corners.
   */
  public Board() {
    board = new int[SIZE][SIZE];
    oldMoves = new DList();
    blackChips = new DList();
    whiteChips = new DList();

    for (int y = 0; y < SIZE; y++){
      for (int x = 0; x < SIZE; x++){
        board[y][x] = EMPTY;
      }
    }

    board[0][0] = CORNER;
    board[0][SIZE-1] = CORNER;
    board[SIZE-1][0] = CORNER;
    board[SIZE-1][SIZE-1] = CORNER;
  }

  /**
   * getValue(int, int)
   * Gets the value of the square with position (x, y) on this Board.
   * If (x, y) is not a valid position, throws an exception.
   *
   * @param x The x-coordinate on this Board.
   * @param y The y-coordinate on this Board.
   * @return The value at (x, y) on this Board.
   * @exception OffBoardException Thrown if (x, y) is not a valid
   *     on this Board.
   */
  public int getValue(int x, int y) throws OffBoardException {
    if (x >= SIZE || y >= SIZE || x < 0 || y < 0) {
      throw new OffBoardException(x,y);
    }
    return board[y][x];
  }

  /**
   * isFull()
   * Determines whether or not both player have exhausted all 10 of their
   * "add moves" (a.k.a. There are 20 chips on this Board, 10 white and
   * 10 black).
   *
   * @return true if both players have 10 chips on this Board, false
   *     otherwise.
   */
  public boolean isFull (){
    return (oldMoves.length() >= 20);
  }

  /**
   * chipsNotOnBoard()
   * Returns the number of chips that have yet to be added to this Board.
   * Always between 0 and 20 (inclusive).
   *
   * @return The number of chips not yet placed on this Board.
   */
  public int chipsNotOnBoard () {
    return (20 - blackChips.length() - whiteChips.length());
  }

  private DList getAdjacentPieces(int x, int y, int color, int[] ignore) throws OffBoardException {
    DList neighbors = new DList();
    for (BoardCheck.Direction d : BoardCheck.Direction.values()) {
      int[] coordinates = BoardCheck.coordinateInDirection(x, y, d);
      try {
        if ((ignore != null) && 
            (ignore[0] == coordinates[0]) && 
            (ignore[1] == coordinates[1])) {
            continue;
        } else if (getValue(coordinates[0], coordinates[1]) == color) {
          neighbors.add(coordinates);
        }
      } catch (OffBoardException e) {
        continue;
      }
    }
    return neighbors;
  }

  private boolean formsCluster(int x, int y, int color, int[] ignore) throws OffBoardException {
    DList neighbors = getAdjacentPieces(x, y, color, ignore);
    if (neighbors.length() == 1) {
      int[] neighbor = (int[]) neighbors.pop();
      return (getAdjacentPieces(neighbor[0], neighbor[1], color, ignore).length() >= 1);
    } else if (neighbors.length() == 0) {
      return false;
    } else {
      return true;
    }
  }

  /**
   * otherPlayer(int) : STATIC
   * Returns the opposite player color of the one given.
   *
   * @param color The player color.
   * @return The opposite player's color.
   */
  public static int otherPlayer(int color) {
    if (color == BLACK) {
      return WHITE;
    } else {
      return BLACK;
    }
  }

  /**
   * propertyScores(int)
   * Returns an int[] of critical values pertaining to this Board for the
   * given player. The four values in the returned array are:
   *   1) The total number of connections for all chips of the given player.
   *   2) The total number of triangular connections for the given player.
   *   3) The number of chips in the given player's starting goal.
   *   4) The number of chips in the given player's ending goal.
   *
   * @param color The player color.
   * @return An int[4] holding the above critical values.
   */
  public int[] propertyScores (int color) {
      DList chipList;
      if (color == WHITE) {
          chipList = this.whiteChips;
      } else {
          chipList = this.blackChips;
      }
      int[] properties = new int[4];
      int numConnections = 0, numTriangles = 0, numStart = 0, numEnd = 0;
      for (Object o : chipList) {
          Point p = (Point) o;
          try {
              DList canSee = BoardCheck.links(this, p.x, p.y);
              numConnections += canSee.length();
              DList pairings = canSee.allPairs();
              for (Object ob : pairings) {
                  Object[] pair = (Object[]) ob;
                  int[] p1 = (int[]) pair[0], p2 = (int[]) pair[1];
                  BoardCheck.Direction d = BoardCheck.getDirection(p1[0], p1[1], p2[0], p2[1]);
                  int[] piece = BoardCheck.findPieceInDirection(this, p1[0], p1[1], color, d);
                  if ((piece != null) && (piece[0] == p2[0]) && (piece[1] == p2[1])) {
                      numTriangles++;
                  }
              }
          } catch (OffBoardException e) {
              return null;
          }
      }
      if (color == WHITE) {
          for (int i = 1; i < SIZE-1; i++) {
              if (this.board[i][0] == color) {
                  numStart++;
              } else if (this.board[i][SIZE-1] == color) {
                  numEnd++;
              }
          }
      } else if (color == BLACK) {
          for (int i = 1; i < SIZE-1; i++) {
              if (this.board[0][i] == color) {
                  numStart++;
              } else if (this.board[SIZE-1][i] == color) {
                  numEnd++;
              }
          }
      }
      properties[0] = numConnections / 2;
      properties[1] = numTriangles / 3;
      properties[2] = numStart;
      properties[3] = numEnd;
      return properties;
  }

  /**
   * isValidMove(int, Move)
   * Determines whether or not the given Move is allowed by the rules of
   * the Network game for the given player color on this Board.
   *
   * @param color The player color, white or black, as determined by
   *     final constant integers.
   * @param m The move to be judged as valid or not.
   * @return true if given Move is valid for given player, false otherwise.
   */
  public boolean isValidMove (int color, Move m){
    if (m.moveKind == Move.QUIT){ //QUIT moves are always valid
      return true;
    }
    else if (m.moveKind == Move.ADD) {  //TYPE ADD
      try {
        int position = getValue(m.x1, m.y1);
        if (position != EMPTY) {
          return false;
        } else if (color == BLACK && blackChips.length() == 10) {
          return false;
        } else if (color == WHITE && whiteChips.length() == 10) {
          return false;
        } else if (BoardCheck.isInEndArea(m.x1, m.y1, otherPlayer(color))) {
          return false;
        } else if (BoardCheck.isInStartArea(m.x1, m.y1, otherPlayer(color))) {
          return false;
        } else if (formsCluster(m.x1, m.y1, color, null)) {
          return false;
        } else {
          return true;
        }
      } catch (OffBoardException e) {
        return false;
      }
    }
    else if (m.moveKind == Move.STEP) { //TYPE STEP
      try {
        int position1 = getValue(m.x2, m.y2);
        int position2 = getValue(m.x1, m.y1);
        int[] prev = { m.x2, m.y2 };
        if (position1 != color) {
          return false;
        } else if (position2 != EMPTY) {
          return false;
        } else if (!isFull()){
          return false;
        } else if (BoardCheck.isInEndArea(m.x1, m.y1, otherPlayer(color))) {
          return false;
        } else if (BoardCheck.isInStartArea(m.x1, m.y1, otherPlayer(color))) {
          return false;
        } else if (formsCluster(m.x1, m.y1, color, prev)) {
          return false;
        } else {
          return true;
        }
      } catch (OffBoardException e) {
        return false;
      }
    }
    return true;
  }

  /**
   * allValidMoves(int)
   * Returns a DList of all valid Moves for the given player based only on
   * the configuration of this Board.
   *
   * @param color The player color, white or black, as determined by
   *     final constant integers.
   * @return A DList, possibly empty, of all valid Moves for given player
   *     on this Board.
   */
  public DList allValidMoves (int color){
    DList moves = new DList();
    for (int x = 0; x < SIZE; x++){
      for (int y = 0; y < SIZE; y++){
        try {
          if (getValue(x, y) == EMPTY){
            if (isFull()) {
              DList chips = null;
              if (color == WHITE) {
                chips = whiteChips;
              } else {
                chips = blackChips;
              }
              for (Object item : chips) {
                Point chip = (Point) item;
                Move m = new Move(x, y, chip.x, chip.y);
                if (isValidMove(color, m)){
                  moves.add(m);
                }
              }
            } else {
              Move m = new Move(x, y);
              if (isValidMove(color, m)){
                moves.add(m);
              }
            }
          }
        } catch (OffBoardException e) {
          continue;
        }
      }
    }
    return moves;
  }

  /**
   * makeMove(int, Move)
   * Applies the given Move to this Board for the given player if the move
   * is valid, otherwise throws an exception.
   *
   * @param color The player color, white or black, as determined by
   *     final constant integers.
   * @param m The Move to applied to this Board.
   * @exception InvalidMoveException Thrown if the given Move is invalid
   *     for the given player.
   */
  public void makeMove (int color, Move m) throws InvalidMoveException{
    if (!isValidMove(color, m)){
      throw new InvalidMoveException(m);
    }
    if (m.moveKind == Move.QUIT) {
      resetBoard();
    } else if (m.moveKind == Move.ADD) {
      board[m.y1][m.x1] = color;
      Point move = new Point(m.x1, m.y1);
      if (color == BLACK) {
        blackChips.add(move);
      } else if (color == WHITE) {
        whiteChips.add(move);
      }
    } else if (m.moveKind == Move.STEP){
      board[m.y2][m.x2] = EMPTY;
      board[m.y1][m.x1] = color;
      Point next = new Point(m.x1, m.y1);
      Point prev = new Point(m.x2, m.y2);
      if (color == BLACK) {
        blackChips.remove(prev);
        blackChips.add(next);
      } else if (color == WHITE) {
        whiteChips.remove(prev);
        whiteChips.add(next);
      }
    }
    oldMoves.add(m);
  }

  /**
   * undoLastMove()
   * Returns this Board to a state as if the last applied Move never happened.
   * If no Moves have been applied, this method returns immediately.
   */
  public void undoLastMove () {
    if (this.oldMoves.length() == 0) {
      return;
    }
    Move m = (Move) oldMoves.pop();
    try {
      if (m.moveKind == Move.ADD){
        int color = getValue(m.x1, m.y1);
        Point chip = new Point(m.x1, m.y1);
        if (color == BLACK) {
          blackChips.remove(chip);
        } else if (color == WHITE) {
          whiteChips.remove(chip);
        }
        board[m.y1][m.x1] = EMPTY;
      }
      else if (m.moveKind == Move.STEP){
        int color = getValue(m.x1, m.y1);
        board[m.y1][m.x1] = EMPTY;
        board[m.y2][m.x2] = color;
        Point chip = new Point(m.x2, m.y2);
        Point prev = new Point(m.x1, m.y1);
        if (color == BLACK) {
          blackChips.remove(prev);
          blackChips.add(chip);
        } else if (color == WHITE) {
          whiteChips.remove(prev);
          whiteChips.add(chip);
        }
      }
    } catch (OffBoardException e) {
      System.err.println("oldMoves stack corrupted");
    }
  }
 
  private void resetBoard(){
    for (int x = 0; x < SIZE; x++){
      for (int y = 0; y < SIZE; y++){
        if (board[y][x] != CORNER){
          board[y][x] = EMPTY;
        }
      }
    }
    oldMoves = new DList();
    whiteChips = new DList();
    blackChips = new DList();
  }

  /**
   * equals(Object)
   * Determines whether or not this Board is structurally equivalent to the
   * given Object.
   *
   * @param board The Object to compare this Board to.
   * @return true if this Board and the given Board are identical, false
   *     otherwise.
   */
  public boolean equals (Object board){
    if (! (board instanceof Board)) {
      return false;
    }
    try{
      Board other = (Board) board;
      for (int y = 0; y < Board.SIZE; y++) {
        for (int x = 0; x < Board.SIZE; x++) {
          if (this.getValue(x,y) != other.getValue(x,y)) {
            return false;
          }
        }
      }
    }
    catch (OffBoardException e){
      return false;
    }
    return true;
  }
  
  /**
   * hashCode()
   * Returns a hash code for this Board, used for storage in HashTables.
   * Every Board has a unique hash code.
   *
   * @return This Board's hash code, a numerical representation of this object.
   */
  public int hashCode (){
    try{
      BigInteger base = BIGZERO;
      BigInteger value = BIGZERO;
      BigInteger result = BIGZERO;
      int cellValue, exp = 0;
      for (int i = 0; i < 3; i++) {
        base = base.add(BIGONE); // base is 3 (number of colors, excluding corners)
      }
      for (int y = 0; y < this.board.length; y++) {
        for (int x = 0; x < this.board[y].length; x++) {
          cellValue = this.getValue(x,y);
          if (cellValue == WHITE) {
            value = BIGTWO;
          } else if (cellValue == BLACK) {
            value = BIGONE;
          } else {
            value = BIGZERO;
          }
          result = result.add(value.multiply(base.pow(exp)));
          value = BIGZERO;
          exp++;
        }
      }
      return result.intValue();
    }
    catch (OffBoardException e){
      return 0;
    }
  }
  
  /**
   * toString()
   * Returns a String representation of this Board, used mainly for testing
   * purposes.
   *
   * @return A String that clearly represents this Board and its configuration.
   */
  public String toString() {
    String output = "";
    for (int i = 0; i < SIZE; i++) {
      output += "| ";
      for (int j = 0; j < SIZE; j++) {
        try {
          switch(getValue(j, i)){
            case BLACK:
              output += "B ";
              break;
            case WHITE:
              output += "W ";
              break;
            case CORNER:
              output += "X ";
              break;
            default:
              output += "  ";
              break;
          }
        } catch (Exception e) {}
      }
      output += "|\n";
    }
    return output;
  }
}
