/*
 * CS 61C Fall 2013 Project 1
 *
 * DoublePair.java is a class which stores two doubles and 
 * implements the Writable interface. It can be used as a 
 * custom value for Hadoop. To use this as a key, you can
 * choose to implement the WritableComparable interface,
 * although that is not necessary for credit.
 */

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.Writable;

public class DoublePair implements Writable {
    // Declare any variables here
    private double double1, double2;

    /**
     * Constructs a DoublePair with both doubles set to zero.
     */
    public DoublePair() {
        this(0.0, 0.0);
    }

    /**
     * Constructs a DoublePair containing double1 and double2.
     */ 
    public DoublePair(double double1, double double2) {
        this.double1 = double1;
        this.double2 = double2;
    }

    /**
     * Returns the value of the first double.
     */
    public double getDouble1() {
        return this.double1;
    }

    /**
     * Returns the value of the second double.
     */
    public double getDouble2() {
        return this.double2;
    }

    /**
     * Sets the first double to val.
     */
    public void setDouble1(double val) {
        this.double1 = val;
    }

    /**
     * Sets the second double to val.
     */
    public void setDouble2(double val) {
        this.double2 = val;
    }

    /**
     * write() is required for implementing Writable.
     */
    public void write(DataOutput out) throws IOException {
        out.writeDouble(this.double1);
        out.writeDouble(this.double2);
    }

    /**
     * readFields() is required for implementing Writable.
     */
    public void readFields(DataInput in) throws IOException {
        this.double1 = in.readDouble();
        this.double2 = in.readDouble();
    }
}
