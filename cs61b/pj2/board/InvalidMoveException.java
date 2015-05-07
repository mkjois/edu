package board;

import player.Move;

/**
 * An exception designed to be thrown when invalid Network moves are encountered.
 */
public class InvalidMoveException extends Exception {

    /**
     * InvalidMoveException(Move)
     * Constructs an new InvalidMoveException based on the given invalid Move.
     *
     * @param m The invalid Move to showcase.
     */
    InvalidMoveException (Move m) {
        super("Invalid Move: " + m.toString());
    }
}
