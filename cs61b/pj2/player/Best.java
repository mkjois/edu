package player;

/**
 * A container for data. Stores a Move and the score associated with
 * applying that Move to a certain Board. Used in our minimax game
 * tree search algorithm.
 */
public class Best {

    Move move;
    double score;

    /**
     * Constructs a new Best with a quit move and a neutral score by default.
     */
    public Best () {
        this(new Move(), 0.0);
    }
    
    /**
     * Constructs a new Best with the given Move and a neutral score.
     *
     * @param m The Move to store.
     */
    public Best (Move m) {
        this(m, 0.0);
    }
    
    /**
     * Constructs a new Best with a quit move and the given score.
     *
     * @param score The score of the board in the process of being analyzed.
     */
    public Best (double score) {
        this(new Move(), score);
    }
    
    /**
     * Constructs a new Best with the given Move and score.
     *
     * @param m The Move to store.
     * @param score The score of the board the given Move is applied to.
     */
    public Best (Move m, double score) {
        this.move = m;
        this.score = score;
    }
}
