import java.math.BigInteger;

/* SimpleBoard.java */

/**
 *  Simple class that implements an 8x8 game board with three possible values
 *  for each cell:  0, 1 or 2.
 *
 *  DO NOT CHANGE ANY PROTOTYPES IN THIS FILE.
 **/

public class SimpleBoard {
  private final static int DIMENSION = 8;
  private int[][] grid;

  /**
   *  Invariants:  
   *  (1) grid.length == DIMENSION.
   *  (2) for all 0 <= i < DIMENSION, grid[i].length == DIMENSION.
   *  (3) for all 0 <= i, j < DIMENSION, grid[i][j] >= 0 and grid[i][j] <= 2.
   **/

  /**
   *  Construct a new board in which all cells are zero.
   */

  public SimpleBoard() {
    grid = new int[DIMENSION][DIMENSION];
  }

  /**
   *  Set the cell (x, y) in the board to the given value mod 3.
   *  @param value to which the element should be set (normally 0, 1, or 2).
   *  @param x is the x-index.
   *  @param y is the y-index.
   *  @exception ArrayIndexOutOfBoundsException is thrown if an invalid index
   *  is given.
   **/

  public void setElementAt(int x, int y, int value) {
    grid[x][y] = value % 3;
    if (grid[x][y] < 0) {
      grid[x][y] = grid[x][y] + 3;
    }
  }

  /**
   *  Get the valued stored in cell (x, y).
   *  @param x is the x-index.
   *  @param y is the y-index.
   *  @return the stored value (between 0 and 2).
   *  @exception ArrayIndexOutOfBoundsException is thrown if an invalid index
   *  is given.
   */

  public int elementAt(int x, int y) {
    return grid[x][y];
  }

  /**
   *  Returns true if "this" SimpleBoard and "board" have identical values in
   *    every cell.
   *  @param board is the second SimpleBoard.
   *  @return true if the boards are equal, false otherwise.
   */

  public boolean equals(Object board) {
    // Replace the following line with your solution.  Be sure to return false
    //   (rather than throwing a ClassCastException) if "board" is not
    //   a SimpleBoard.
    if (! (board instanceof SimpleBoard)) {
        return false;
    }
    SimpleBoard other = (SimpleBoard) board;
    for (int y = 0; y < this.grid.length; y++) {
        for (int x = 0; x < this.grid[y].length; x++) {
            if (this.elementAt(x,y) != other.elementAt(x,y)) {
                return false;
            }
        }
    }
    return true;
  }

  /**
   *  Returns a hash code for this SimpleBoard.
   *  @return a number between Integer.MIN_VALUE and Integer.MAX_VALUE.
   */

  public int hashCode() {
      BigInteger zero = BigInteger.ZERO;
      BigInteger one = BigInteger.ONE;
      BigInteger two = one.add(one);
      BigInteger base = BigInteger.ZERO;
      BigInteger value = BigInteger.ZERO;
      BigInteger result = BigInteger.ZERO;
      int cell, exp = 0;
      for (int i = 0; i < 3; i++) {
          base = base.add(BigInteger.ONE); // base is 3
      }
      for (int y = 0; y < this.grid.length; y++) {
          for (int x = 0; x < this.grid[y].length; x++) {
              cell = this.elementAt(x, y);
              for (int j = 0; j < cell; j++) {
                  value = value.add(BigInteger.ONE);
              }
              result = result.add(value.multiply(base.pow(exp)));
              value = BigInteger.ZERO;
              exp++;
          }
      }
      return result.intValue();
  }

}
