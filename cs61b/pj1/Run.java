public class Run {

    public int length, value, hunger;

    public Run (int length, int value, int hunger) {
        this.length = length;
        this.value = value;
        this.hunger = hunger;
    }

    public Run (int length, int value) {
        this(length, value, Cell.NOHUNGER);
    }

    public boolean equalParams (int value, int hunger) {
        return ((this.value == value) && (this.hunger == hunger));
    }

    public boolean equalParams (Run other) {
        return ((this.value == other.value) && (this.hunger == other.hunger));
    }

    public boolean isEmptyLength () {
        return (this.length < 1);
    }

    public boolean isEmptyValued () {
        return (this.value == Ocean.EMPTY);
    }

    public String toString () {
        String type;
        if (this.value == Ocean.SHARK) {
            type = " SHARK ";
        } else if (this.value == Ocean.FISH) {
            type = " FISH ";
        } else {
            type = " EMPTY ";
        }
        return "[ " + this.length + type + this.hunger + " ]";
    }

    public Cell[] toCellArray () {
        Cell[] result = new Cell[this.length];
        for (int i = 0; i < this.length; i++) {
            result[i] = new Cell(this.value, this.hunger);
        }
        return result;
    }
}
