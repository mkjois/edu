package board;

import list.DList;
import player.Move;

/**
 * A collection of static methods to help analyze Board objects, particularly
 * to help determine if Boards have winning networks.
 */
public class BoardCheck {

  public static final int WIN_LENGTH = 5;

  /**
   * An enum to make it convenient to look around in various directions
   */
  public enum Direction {
    NORTH(0, -1), NORTHEAST(1, -1), EAST(1, 0), SOUTHEAST(1, 1), 
    SOUTH(0, 1), SOUTHWEST(-1, 1), WEST(-1, 0), NORTHWEST(-1, -1);

    private int xmod, ymod;

    private Direction(int _xmod, int _ymod) {
      xmod = _xmod;
      ymod = _ymod;
    }

    public int[] getModifier() {
      return new int[] {xmod, ymod};
    }
  }

  private BoardCheck() {
  }

  /**
   * links(Board, int, int) : STATIC
   * Returns a DList of int[2]'s corresponding to the coordinates of the 
   * pieces directly "visible" to and of the same color as the input piece.
   * 
   * @param   b   the board on which to look for links
   * @param   x   the x-coordinate of the input piece
   * @param   y   the y-coordinate of the input piece
   * @return      DList of links
   * @exception OffBoardException Thrown if (x,y) is not on the given Board.
   */
  public static DList links (Board b, int x, int y) throws OffBoardException {
    int pieceType = b.getValue(x, y);
    if (pieceType != Board.WHITE && pieceType != Board.BLACK) {
      return null;
    }
    
    DList result = new DList();

    for (Direction d : Direction.values()) {
      int[] coord = findPieceInDirection(b, x, y, pieceType, d);
      if (coord != null) {
        result.add(coord);
      }
    }
    return result;
  }

  /**
   * winner(Board, int) : STATIC
   * Determines if the player of the input color has won given the input
   * board.
   *
   * @param   b       the board to check for a winner
   * @param   color   the color to check
   * @return          boolean, whether or not color player has won
   */
  public static boolean winner (Board b, int color) {
    DList roots = new DList();
    try {
      if (color == Board.BLACK) {
        for (int i = 0; i < Board.SIZE; i++) {
          if (b.getValue(i, 0) == color) {
            int[] root = {i, 0};
            roots.add(root);
          }
        }
      } else if (color == Board.WHITE) {
        for (int i = 0; i < Board.SIZE; i++) {
          if (b.getValue(0, i) == color) {
            int[] root = {0, i};
            roots.add(root);
          }
        }
      }

      for (Object item : roots ) {
        int[] root = (int[]) item;
        if (formsNetwork(b, root[0], root[1], color, new DList())) {
          return true;
        }
      }
    } catch (OffBoardException e) {
      System.err.println(e);
    }
    return false;
  }

  /**
   * Determines whether or not there is a winning network from this piece.
   * Takes into account previously traversed links in past.
   *
   * @param   b     the board to examine
   * @param   x     the x coordinate of the piece
   * @param   y     the y coordinate of the piece
   * @param   color the color of the player to check
   * @param   past  a list of links that have already been examined
   * @return        whether or not this piece leads to a winning network
   */
  private static boolean formsNetwork (Board b, int x, int y, int color, DList past)
    throws OffBoardException {

    past.add(new int[] {x, y});

    int[] firstPoint = null;
    int[] v1 = null;
    if (past.length() > 1) {
      firstPoint = (int[]) past.get(past.length()-2);
      v1 = new int[] {x-firstPoint[0], y-firstPoint[1]};
    }

    DList links = links(b, x, y);

    for (Object item : links) {
      int[] link = (int[]) item;
      int[] v2 = { link[0] - x, link[1] - y };
      if (isInStartArea(link[0], link[1], color)) {
        continue;
      }
      else if (listContainsCoordinates(past, link)) {
        continue;
      }
      else if (past.length() > 1 && areParallel(v1, v2)) {
        continue;
      }
      else if (isInEndArea(link[0], link[1], color)) {
        if (past.length() >= WIN_LENGTH) {
          return true;
        } else {
          continue;
        }
      }
      else if (formsNetwork(b, link[0], link[1], color, past)){
        return true;
      }
    }
    past.pop();

    return false;
  }

  /**
   * Determines whether or not two vectors are parallel. Method taken from
   * my linear algebra textbook.
   *
   * @param   v1    an int[2] representing the first vector
   * @param   v2    an int[2] representing the second vector
   * @return        whether or not the to vectors are parallel
   */
  private static boolean areParallel (int[] v1, int[] v2) {
    return v1[0] * v2[1] - v1[1] * v2[0] == 0;
  }

  /**
   * Determines whether a coordinate is in the end area. Depends on the color
   * of the player.
   *
   * @param   x     the x coordinate to check
   * @param   y     the y coordinate to check
   * @param   color the color of the player
   * @return        whether or not it is the color player's end area
   */
  static boolean isInEndArea (int x, int y, int color) {
    if (color == Board.BLACK) {
      return y == Board.SIZE - 1;
    } else if (color == Board.WHITE) {
      return x == Board.SIZE - 1;
    }
    return false;
  }

  /**
   * Determines whether a coordinate is in the start area. Depends on the color
   * of the player.
   *
   * @param   x     the x coordinate to check
   * @param   y     the y coordinate to check
   * @param   color the color of the player
   * @return        whether or not it is the color player's start area
   */
  static boolean isInStartArea (int x, int y, int color) {
    if (color == Board.BLACK) {
      return y == 0;
    } else if (color == Board.WHITE) {
      return x == 0;
    }
    return false;
  }
  
  /**
   * getDirection(int, int, int, int) : STATIC
   * Returns one of the 8 cardinal or intermediate Directions corresponding to
   * moving from (x1,y1) to (x2,y2).
   * 
   * @param x1 The initial x-coordinate.
   * @param y1 The initial y-coordinate.
   * @param x2 The final x-coordinate.
   * @param y2 The final y-coordinate.
   * @return A Direction instance: NORTH, NORTHEAST, NORTHWEST, EAST, WEST,
   *     SOUTH, SOUTHEAST, SOUTHWEST.
   */
  static Direction getDirection (int x1, int y1, int x2, int y2) {
      int x = 0, y = 0;
      if (x1 < x2) {
          x = 1;
      } else if (x1 > x2) {
          x = -1;
      }
      if (y1 < y2) {
          y = 1;
      } else if (y1 > y2) {
          y = -1;
      }
      for (Direction d : Direction.values()) {
          if (d.xmod == x && d.ymod == y) {
              return d;
          }
      }
      return null;
  }

  /** 
   * findPieceInDirection(Board, int, int, int, Direction) : STATIC
   * Finds the nearest piece of the same color in a particular direction.
   *
   * @param   b             the board to examine
   * @param   x             the x coordinate from which to start looking
   * @param   y             the y coordinate from which to start looking
   * @param   color         the color of the piece to look for
   * @param   direction     the direction to look for
   * @return                an int[2] with the coordinates of the nearst piece
   * @exception OffBoardException Thrown if (x,y) is not on the given Board.
   */
  static int[] findPieceInDirection (Board b, int x, int y, int color, Direction d) 
    throws OffBoardException
  {
    int[] p = coordinateInDirection(x, y, d);
    try {
      int pieceType = b.getValue(p[0], p[1]);
      while (pieceType != color) {
        if (pieceType != Board.EMPTY) {
          return null;
        }
        p = coordinateInDirection(p[0], p[1], d);
        pieceType = b.getValue(p[0], p[1]);
      }
      return p;
    } catch (OffBoardException e) {
      return null;
    }
  }

  /**
   * coordinateInDirection(int, int, Direction) : STATIC
   * Returns the coordinates of the space adjacent to the given space
   * in the given direction.
   *
   * @param   x   the original x-coordinate
   * @param   y   the original y-coordinate
   * @param   d   the direction to look
   * @return      an int[2] with the new coordinates
   */
  static int[] coordinateInDirection (int x, int y, Direction d) {
    int[] mod = d.getModifier();
    return new int[] { x+mod[0], y+mod[1] };
  }

  /**
   * Determines whether or not the list of coordinates contains the given
   * coordinate.
   *
   * @param   lst   the list of coordinates
   * @param   coord the coordinate to look for
   * @return        whether or not coord is in lst
   */
  private static boolean listContainsCoordinates(DList lst, int[] coord) {
    for (Object item : lst) {
      boolean found = true;
      int[] intItem = (int[]) item;
      for (int i = 0; i < intItem.length; i++){
        if (intItem[i] != coord[i]) {
          found = false;
          break;
        }
      }
      if (found) {
        return true;
      }
    }
    return false;
  }
}
