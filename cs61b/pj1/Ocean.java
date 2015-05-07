/* Ocean.java */

/**
 *  The Ocean class defines an object that models an ocean full of sharks and
 *  fish.  Descriptions of the methods you must implement appear below.  They
 *  include a constructor of the form
 *
 *      public Ocean(int i, int j, int starveTime);
 *
 *  that creates an empty ocean having width i and height j, in which sharks
 *  starve after starveTime timesteps.
 *
 *  See the README file accompanying this project for additional details.
 */

public class Ocean {

  /**
   *  Do not rename these constants.  WARNING:  if you change the numbers, you
   *  will need to recompile Test4.java.  Failure to do so will give you a very
   *  hard-to-find bug.
   */

  public final static int EMPTY = 0;
  public final static int SHARK = 1;
  public final static int FISH = 2;

  /**
   *  Define any variables associated with an Ocean object here.  These
   *  variables MUST be private.
   */
  
  private final static int INDEXEMPTY = 0;
  private final static int INDEXSHARK = 1;
  private final static int INDEXFISH = 2;

  private int width, height, starveTime;
  private Cell[][] cells;

  /**
   *  The following methods are required for Part I.
   */

  /**
   *  Ocean() is a constructor that creates an empty ocean having width i and
   *  height j, in which sharks starve after starveTime timesteps.
   *  @param i is the width of the ocean.
   *  @param j is the height of the ocean.
   *  @param starveTime is the number of timesteps sharks survive without food.
   */

  public Ocean(int i, int j, int starveTime) {
      this.width = i;
      this.height = j;
      this.cells = new Cell[j][i];
      this.starveTime = starveTime;

      for (int y = 0; y < j; y++) {
          for (int x = 0; x < i; x++) {
              this.cells[y][x] = new Cell();
          }
      }
  }

  /**
   *  width() returns the width of an Ocean object.
   *  @return the width of the ocean.
   */

  public int width() {
      return this.width;
  }

  /**
   *  height() returns the height of an Ocean object.
   *  @return the height of the ocean.
   */

  public int height() {
      return this.height;
  }

  /**
   *  starveTime() returns the number of timesteps sharks survive without food.
   *  @return the number of timesteps sharks survive without food.
   */

  public int starveTime() {
      return this.starveTime;
  }

  /**
   *  addFish() places a fish in cell (x, y) if the cell is empty.  If the
   *  cell is already occupied, leave the cell as it is.
   *  @param x is the x-coordinate of the cell to place a fish in.
   *  @param y is the y-coordinate of the cell to place a fish in.
   */

  public void addFish(int x, int y) {
      x = Cell.modIndex(x, this.width);
      y = Cell.modIndex(y, this.height);
      Cell cell = this.cells[y][x];
      if (cell == null) {
          this.cells[y][x] = new Cell(Ocean.FISH);
      } else if (cell.isEmpty()) {
          cell.makeFish();
      }
  }

  /**
   *  addShark() (with two parameters) places a newborn shark in cell (x, y) if
   *  the cell is empty.  A "newborn" shark is equivalent to a shark that has
   *  just eaten.  If the cell is already occupied, leave the cell as it is.
   *  @param x is the x-coordinate of the cell to place a shark in.
   *  @param y is the y-coordinate of the cell to place a shark in.
   */

  public void addShark(int x, int y) {
      x = Cell.modIndex(x, this.width);
      y = Cell.modIndex(y, this.height);
      Cell cell = this.cells[y][x];
      if (cell == null) {
          this.cells[y][x] = new Cell(Ocean.SHARK, 0);
      } else if (cell.isEmpty()) {
          cell.makeShark();
      }
  }

  /**
   *  cellContents() returns EMPTY if cell (x, y) is empty, FISH if it contains
   *  a fish, and SHARK if it contains a shark.
   *  @param x is the x-coordinate of the cell whose contents are queried.
   *  @param y is the y-coordinate of the cell whose contents are queried.
   */

  public int cellContents(int x, int y) {
      x = Cell.modIndex(x, this.width);
      y = Cell.modIndex(y, this.height);
      Cell cell = this.cells[y][x];
      return cell.getValue();
  }

  private int[] numNeighbors (int x, int y) {
      int[] neighbors = new int[8];
      neighbors[0] = this.cellContents(x-1, y-1);
      neighbors[1] = this.cellContents(x  , y-1);
      neighbors[2] = this.cellContents(x+1, y-1);
      neighbors[3] = this.cellContents(x-1, y  );
      neighbors[4] = this.cellContents(x+1, y  );
      neighbors[5] = this.cellContents(x-1, y+1);
      neighbors[6] = this.cellContents(x  , y+1);
      neighbors[7] = this.cellContents(x+1, y+1);

      int[] count = {0, 0, 0};
      for (int entry : neighbors) {
          if (entry == Ocean.SHARK) {
              count[Ocean.INDEXSHARK] += 1;
          } else if (entry == Ocean.FISH) {
              count[Ocean.INDEXFISH] += 1;
          } else {
              count[Ocean.INDEXEMPTY] += 1;
          }
      }
      return count;
  }

  private void timeStepShark (Cell currCell, Cell nextCell, int[] neighbors) {
      int numFish = neighbors[Ocean.INDEXFISH];
      if (numFish > 0) { // case 1
          nextCell.makeShark();
      } else { // case 2
          int nextHunger = 1 + currCell.getHunger();
          nextCell.makeShark(nextHunger);
          if (nextCell.getHunger() > this.starveTime) {
              nextCell.makeEmpty();
          }
      }
  }

  private void timeStepFish (Cell nextCell, int[] neighbors) {
      int numSharks = neighbors[Ocean.INDEXSHARK];
      if (numSharks == 0) { // case 3
          nextCell.makeFish();
      } else if (numSharks == 1) { // case 4
          nextCell.makeEmpty();
      } else { // case 5
          nextCell.makeShark();
      }
  }

  private void timeStepEmpty (Cell nextCell, int[] neighbors) {
      int numFish = neighbors[Ocean.INDEXFISH];
      int numSharks = neighbors[Ocean.INDEXSHARK];
      if (numFish < 2) { // case 6
          nextCell.makeEmpty();
      } else if (numSharks < 2) { // case 7
          nextCell.makeFish();
      } else { // case 8
          nextCell.makeShark();
      }
  }

  /**
   *  timeStep() performs a simulation timestep as described in README.
   *  @return an ocean representing the elapse of one timestep.
   */

  public Ocean timeStep() {
      Cell currCell, nextCell;
      Ocean next = new Ocean(this.width, this.height, this.starveTime);

      for (int y = 0; y < this.height; y++) {
          for (int x = 0; x < this.width; x++) {
              currCell = this.cells[y][x];
              nextCell = next.cells[y][x];
              int[] neighbors = this.numNeighbors(x, y);

              if (currCell.isShark()) {
                  this.timeStepShark(currCell, nextCell, neighbors);
              } else if (currCell.isFish()) {
                  this.timeStepFish(nextCell, neighbors);
              } else {
                  this.timeStepEmpty(nextCell, neighbors);
              }
          }
      }
      return next;
  }

  /**
   *  The following method is required for Part II.
   */

  /**
   *  addShark() (with three parameters) places a shark in cell (x, y) if the
   *  cell is empty.  The shark's hunger is represented by the third parameter.
   *  If the cell is already occupied, leave the cell as it is.  You will need
   *  this method to help convert run-length encodings to Oceans.
   *  @param x is the x-coordinate of the cell to place a shark in.
   *  @param y is the y-coordinate of the cell to place a shark in.
   *  @param feeding is an integer that indicates the shark's hunger.  You may
   *         encode it any way you want; for instance, "feeding" may be the
   *         last timestep the shark was fed, or the amount of time that has
   *         passed since the shark was last fed, or the amount of time left
   *         before the shark will starve.  It's up to you, but be consistent.
   */

  public void addShark(int x, int y, int feeding) {
      x = Cell.modIndex(x, this.width);
      y = Cell.modIndex(y, this.height);
      Cell cell = this.cells[y][x];
      if (cell == null) {
          this.cells[y][x] = new Cell(Ocean.SHARK, feeding);
      } else if (cell.isEmpty()) {
          cell.makeShark(feeding);
      }   
  }

  private Cell getCell (int x, int y) {
      x = Cell.modIndex(x, this.width);
      y = Cell.modIndex(y, this.height);
      return this.cells[y][x];
  }

  /**
   *  The following method is required for Part III.
   */

  /**
   *  sharkFeeding() returns an integer that indicates the hunger of the shark
   *  in cell (x, y), using the same "feeding" representation as the parameter
   *  to addShark() described above.  If cell (x, y) does not contain a shark,
   *  then its return value is undefined--that is, anything you want.
   *  Normally, this method should not be called if cell (x, y) does not
   *  contain a shark.  You will need this method to help convert Oceans to
   *  run-length encodings.
   *
   *  EDIT: returns the shark's hunger if given cell position contains a shark,
   *  otherwise return the final static constant Cell.NOHUNGER
   *
   *  @param x is the x-coordinate of the cell whose contents are queried.
   *  @param y is the y-coordinate of the cell whose contents are queried.
   */

  public int sharkFeeding(int x, int y) {
      Cell cell = this.getCell(x, y);
      if (cell.isShark()) {
          return cell.getHunger();
      } else {
          return Cell.NOHUNGER;
      }
  }
}
