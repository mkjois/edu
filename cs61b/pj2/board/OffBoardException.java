package board;

/**
 * An exception designed to be thrown when coordinates are encountered that are
 * off the Network board.
 */
public class OffBoardException extends Exception {
    
    /**
     * OffBoardException(int, int)
     * Constructs a new OffBoardException based on the given invalid
     * coordinates.
     *
     * @param x The x-coordinate, possibly invalid.
     * @param y The y-coordinate, possibly invalid.
     */
    OffBoardException (int x, int y) {
        super("Invalid position: (" + x + ", " + y + ")");
    }
}
