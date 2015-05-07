public class Cell {

    public final static int NOHUNGER = -1;

    private int value, hunger;

    public Cell (int value, int hunger) {
        this.value = value;
        this.hunger = hunger;
    }

    public Cell (int value) {
        this(value, Cell.NOHUNGER);
    }

    public Cell () {
        this.value = Ocean.EMPTY;
        this.hunger = Cell.NOHUNGER;
    }

    public static int modIndex(int index, int dimension) {
        double realQuotient = ((double) index) / ((double) dimension);
        int quotient = (int) Math.floor(realQuotient);
        if (dimension * quotient == index) {
            return 0;
        } else {
            return 1 + Cell.modIndex(index - 1, dimension);
        }
    }

    public int getValue () {
        return this.value;
    }

    public void setValue (int value) {
        this.value = value;
    }

    public int getHunger () {
        return this.hunger;
    }

    public void setHunger (int hunger) {
        this.hunger = hunger;
    }

    public boolean isEmpty () {
        return this.value == Ocean.EMPTY;
    }

    public boolean isShark () {
        return this.value == Ocean.SHARK;
    }

    public boolean isFish () {
        return this.value == Ocean.FISH;
    }

    public void makeEmpty () {
        this.value = Ocean.EMPTY;
        this.hunger = Cell.NOHUNGER;
    }

    public void makeShark () {
        this.value = Ocean.SHARK;
        this.hunger = 0;
    }

    public void makeShark (int hunger) {
        this.value = Ocean.SHARK;
        this.hunger = hunger;
    }

    public void makeFish () {
        this.value = Ocean.FISH;
        this.hunger = Cell.NOHUNGER;
    }

    public boolean equals (Cell other) {
        boolean equalValue = (this.value == other.value);
        boolean equalHunger = (this.hunger == other.hunger);
        return (equalValue && equalHunger);
    }

    public String toString () {
        if (this.isShark()) {
            return "S";
        } else if (this.isFish()) {
            return "~";
        } else {
            return " ";
        }
    }
}
