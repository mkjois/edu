/* RunLengthEncoding.java */

/**
 *  The RunLengthEncoding class defines an object that run-length encodes an
 *  Ocean object.  Descriptions of the methods you must implement appear below.
 *  They include constructors of the form
 *
 *      public RunLengthEncoding(int i, int j, int starveTime);
 *      public RunLengthEncoding(int i, int j, int starveTime,
 *                               int[] runTypes, int[] runLengths) {
 *      public RunLengthEncoding(Ocean ocean) {
 *
 *  that create a run-length encoding of an Ocean having width i and height j,
 *  in which sharks starve after starveTime timesteps.
 *
 *  The first constructor creates a run-length encoding of an Ocean in which
 *  every cell is empty.  The second constructor creates a run-length encoding
 *  for which the runs are provided as parameters.  The third constructor
 *  converts an Ocean object into a run-length encoding of that object.
 *
 *  See the README file accompanying this project for additional details.
 */

public class RunLengthEncoding {

  /**
   *  Define any variables associated with a RunLengthEncoding object here.
   *  These variables MUST be private.
   */

  private int width, height, starveTime;
  private DList runs;
  private String init;

  /**
   *  The following methods are required for Part II.
   */

  /**
   *  RunLengthEncoding() (with three parameters) is a constructor that creates
   *  a run-length encoding of an empty ocean having width i and height j,
   *  in which sharks starve after starveTime timesteps.
   *  @param i is the width of the ocean.
   *  @param j is the height of the ocean.
   *  @param starveTime is the number of timesteps sharks survive without food.
   */

  public RunLengthEncoding(int i, int j, int starveTime) {
      this.init = "3 ARGS-constructor";
      this.width = i;
      this.height = j;
      this.starveTime = starveTime;
      Run emptyRun = new Run(i*j, Ocean.EMPTY, Cell.NOHUNGER);
      this.runs = new DList(emptyRun);
      this.check();
  }

  /**
   *  RunLengthEncoding() (with five parameters) is a constructor that creates
   *  a run-length encoding of an ocean having width i and height j, in which
   *  sharks starve after starveTime timesteps.  The runs of the run-length
   *  encoding are taken from two input arrays.  Run i has length runLengths[i]
   *  and species runTypes[i].
   *  @param i is the width of the ocean.
   *  @param j is the height of the ocean.
   *  @param starveTime is the number of timesteps sharks survive without food.
   *  @param runTypes is an array that represents the species represented by
   *         each run.  Each element of runTypes is Ocean.EMPTY, Ocean.FISH,
   *         or Ocean.SHARK.  Any run of sharks is treated as a run of newborn
   *         sharks (which are equivalent to sharks that have just eaten).
   *  @param runLengths is an array that represents the length of each run.
   *         The sum of all elements of the runLengths array should be i * j.
   */

  public RunLengthEncoding(int i, int j, int starveTime,
                           int[] runTypes, int[] runLengths) {
      this.init = "5 ARGS-constructor";
      this.width = i;
      this.height = j;
      this.starveTime = starveTime;
      this.runs = new DList();
      int hunger;

      for (int k = 0; k < runTypes.length; k++) {
          if (runTypes[k] == Ocean.SHARK) {
              hunger = 0;
          } else {
              hunger = Cell.NOHUNGER;
          }
          Run run = new Run(runLengths[k], runTypes[k], hunger);
          this.runs.insertEnd(run);
      }
      this.check();
  }

  /**
   *  restartRuns() and nextRun() are two methods that work together to return
   *  all the runs in the run-length encoding, one by one.  Each time
   *  nextRun() is invoked, it returns a different run (represented as an
   *  array of two ints), until every run has been returned.  The first time
   *  nextRun() is invoked, it returns the first run in the encoding, which
   *  contains cell (0, 0).  After every run has been returned, nextRun()
   *  returns null, which lets the calling program know that there are no more
   *  runs in the encoding.
   *
   *  The restartRuns() method resets the enumeration, so that nextRun() will
   *  once again enumerate all the runs as if nextRun() were being invoked for
   *  the first time.
   *
   *  (Note:  Don't worry about what might happen if nextRun() is interleaved
   *  with addFish() or addShark(); it won't happen.)
   */

  /**
   *  restartRuns() resets the enumeration as described above, so that
   *  nextRun() will enumerate all the runs from the beginning.
   */

  public void restartRuns() {
      this.runs.restartRuns();
  }

  /**
   *  nextRun() returns the next run in the enumeration, as described above.
   *  If the runs have been exhausted, it returns null.  The return value is
   *  an array of two ints (constructed here), representing the type and the
   *  size of the run, in that order.
   *  @return the next run in the enumeration, represented by an array of
   *          two ints.  The int at index zero indicates the run type
   *          (Ocean.EMPTY, Ocean.SHARK, or Ocean.FISH).  The int at index one
   *          indicates the run length (which must be at least 1).
   */

  public int[] nextRun() {
      Run run = this.runs.nextRun();
      if (run == null) {
          return null;
      } else {
          int[] result = {run.value, run.length};
          return result;
      }
  }

  /**
   *  toOcean() converts a run-length encoding of an ocean into an Ocean
   *  object.  You will need to implement the three-parameter addShark method
   *  in the Ocean class for this method's use.
   *  @return the Ocean represented by a run-length encoding.
   */

  public Ocean toOcean() {
      Cell cell;
      Cell[] allCells = this.runs.toCellArray();
      Ocean ocean = new Ocean(this.width, this.height, this.starveTime);

      for (int y = 0; y < this.height; y++) {
          for (int x = 0; x < this.width; x++) {
              cell = allCells[this.width * y + x];
              if (cell.isShark()) {
                  ocean.addShark(x, y, cell.getHunger());
              } else if (cell.isFish()) {
                  ocean.addFish(x, y);
              }
          }
      }
      return ocean;
  }

  // FOR TESTING
  public static void main (String[] args) {
      int[] v = { 0, 0, 0, 2, 2, 1, 2, 2, 2, 2, 0, 1, 1, 0, 0, 2, 0, 0, 0, 0, 0};
      int[] h = {-1,-1,-1,-1,-1, 1,-1,-1,-1,-1,-1, 2, 0,-1,-1,-1,-1,-1,-1,-1,-1};
      RunLengthEncoding rle = new RunLengthEncoding(7, 3, 3);
      rle.runs = new DList(v, h);
      for (Run r : rle.runs) {
          System.out.println(r);
      }
      System.out.println(rle.runs.size);
  }

  /**
   *  The following method is required for Part III.
   */

  /**
   *  RunLengthEncoding() (with one parameter) is a constructor that creates
   *  a run-length encoding of an input Ocean.  You will need to implement
   *  the sharkFeeding method in the Ocean class for this constructor's use.
   *  @param sea is the ocean to encode.
   */

  public RunLengthEncoding(Ocean sea) {
      this.init = "1 ARG-constructor";
      this.width = sea.width();
      this.height = sea.height();
      this.starveTime = sea.starveTime();

      int[] values = new int[this.width * this.height];
      int[] hungers = new int[this.width * this.height];
      for (int y = 0; y < this.height; y++) {
          for (int x = 0; x < this.width; x++) {
              values[this.width * y + x] = sea.cellContents(x, y);
              hungers[this.width * y + x] = sea.sharkFeeding(x, y);
          }
      }

      this.runs = new DList(values, hungers);
      this.check();
  }

  /**
   *  The following methods are required for Part IV.
   */

  /**
   *  addFish() places a fish in cell (x, y) if the cell is empty.  If the
   *  cell is already occupied, leave the cell as it is.  The final run-length
   *  encoding should be compressed as much as possible; there should not be
   *  two consecutive runs of sharks with the same degree of hunger.
   *  @param x is the x-coordinate of the cell to place a fish in.
   *  @param y is the y-coordinate of the cell to place a fish in.
   */

  public void addFish(int x, int y) {
      x = Cell.modIndex(x, this.width);
      y = Cell.modIndex(y, this.height);
      int[] indices = this.runs.indicesOf(this.width * y + x);
      int dIndex = indices[DList.DLISTINDEX], rIndex = indices[DList.RUNINDEX];
      Run run = this.runs.getRun(dIndex);
      if (run.isEmptyValued()) {
          run = new Run(1, Ocean.FISH, Cell.NOHUNGER);
          this.runs.deepReplace(dIndex, rIndex, run);
      }
      this.check();
  }

  /**
   *  addShark() (with two parameters) places a newborn shark in cell (x, y) if
   *  the cell is empty.  A "newborn" shark is equivalent to a shark that has
   *  just eaten.  If the cell is already occupied, leave the cell as it is.
   *  The final run-length encoding should be compressed as much as possible;
   *  there should not be two consecutive runs of sharks with the same degree
   *  of hunger.
   *  @param x is the x-coordinate of the cell to place a shark in.
   *  @param y is the y-coordinate of the cell to place a shark in.
   */

  public void addShark(int x, int y) {
      x = Cell.modIndex(x, this.width);
      y = Cell.modIndex(y, this.height);
      int[] indices = this.runs.indicesOf(this.width * y + x);
      int dIndex = indices[DList.DLISTINDEX], rIndex = indices[DList.RUNINDEX];
      Run run = this.runs.getRun(dIndex);
      if (run.isEmptyValued()) {
          run = new Run(1, Ocean.SHARK, 0);
          this.runs.deepReplace(dIndex, rIndex, run);
      }
      this.check();
  }

  /**
   *  check() walks through the run-length encoding and prints an error message
   *  if two consecutive runs have the same contents, or if the sum of all run
   *  lengths does not equal the number of cells in the ocean.
   */

  private void check() {
      if (this.runs == null) {
          System.out.println("Your RLE has no runs!");
      }

      final int DUMMYVALUE = -999999, DUMMYHUNGER = -999999;
      int prevValue = DUMMYVALUE, prevHunger = DUMMYHUNGER;
      int totalLength = 0;

      for (Run run : this.runs) {
          if (run.length < 1) {
              System.out.println("Run " + run + " has an invalid length.");
          }
          if (run.equalParams(prevValue, prevHunger)) {
              System.out.println("Two consecutive runs of same type " +
                                 run + " : " + this.init);
          }
          prevValue = run.value;
          prevHunger = run.hunger;
          totalLength += run.length;
      }

      if (totalLength != this.width * this.height) {
          System.out.println("Your Ocean size " + this.width * this.height +
                             " != total run length " + totalLength);
      }
  }
}
