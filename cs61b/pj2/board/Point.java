package board;

/**
 * A data container to store an x-coordinate and a y-coordinate.
 */
public class Point {

    int x, y;
    
    /**
     * Point(int, int)
     * Constructs a new Point object with the given coordinates.
     *
     * @param x The x-coordinate.
     * @param y The y-coordinate.
     */
    public Point (int x, int y) {
        this.set(x, y);
    }
    
    /**
     * set(int, int)
     * Sets the given coordinates as the coordinates of this Point.
     *
     * @param x The x-coordinate.
     * @param y The y-coordinate.
     */
    void set (int x, int y) {
        this.x = x;
        this.y = y;
    }
    
    /**
     * equals(Object)
     * Determines whether or not this Point is equivalent to the given Object.
     *
     * @param other The Object to compare to.
     * @return true if this Point has the same coordinates as the other, false
     *     otherwise.
     */
    public boolean equals (Object other) {
        if (! (other instanceof Point)) {
            return false;
        }
        Point p = (Point) other;
        return (p.x == this.x) && (p.y == this.y);
    }

    /**
     * toString()
     * Returns a String representation of this Point.
     *
     * @return A String representing this Point.
     */
    public String toString () {
        return "(" + this.x + ", " + this.y + ")";
    }
}
