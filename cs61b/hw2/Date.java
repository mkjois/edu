/* Date.java */

import java.io.*;

class Date {

    private static int numDates = 0;
    private int month, day, year;

  /** Constructs a date with the given month, day and year.   If the date is
   *  not valid, the entire program will halt with an error message.
   *  @param month is a month, numbered in the range 1...12.
   *  @param day is between 1 and the number of days in the given month.
   *  @param year is the year in question, with no digits omitted.
   */
  public Date(int month, int day, int year) {
      if (!Date.isValidDate(month, day, year)) {
          ErrorExit("Given date is invalid.");
      } else {
          this.month = month;
          this.day = day;
          this.year = year;
          numDates++;
      }
  }

  /** Constructs a Date object corresponding to the given string.
   *  @param s should be a string of the form "month/day/year" where month must
   *  be one or two digits, day must be one or two digits, and year must be
   *  between 1 and 4 digits.  If s does not match these requirements or is not
   *  a valid date, the program halts with an error message.
   */
  public Date(String s) {
      int slash1 = s.indexOf('/');
      int slash2 = s.lastIndexOf('/');
      if (slash1 == -1 || slash1 == slash2) {
          ErrorExit("Invalid date format.");
      }

      int m = 0, d = 0, y = 0;
      try {
          m = Integer.parseInt(s.substring(0, slash1));
          d = Integer.parseInt(s.substring(slash1 + 1, slash2));
          y = Integer.parseInt(s.substring(slash2 + 1));
      } catch (NumberFormatException e) {
          ErrorExit("Invalid date format.");
      }

      if (isValidDate(m, d, y)) {
          this.month = m;
          this.day = d;
          this.year = y;
          numDates++;
      } else {
          ErrorExit("Given date is invalid.");
      }
  }

  private static void ErrorExit(String msg) {
      System.out.println("Error: " + msg);
      System.exit(0);
  }

  private static boolean is30Month(int month) {
      return (month == 4 || month == 6 || month == 9 || month == 11);
  }

  private boolean isBetween(Date d1, Date d2) {
      if (d1.isAfter(d2)) {
          return this.isBetween(d2, d1);
      }
      return (this.isBefore(d2) && this.isAfter(d1));
  }

  private int leapDaysRange(Date d) {
      if (this.isAfter(d)) {
          return d.leapDaysRange(this);
      }
      int numLeapDays = 0;
      Date leapDay;
      for (int y = this.year; y <= d.year; y++) {
          if (isLeapYear(y)) {
              leapDay = new Date(2, 29, y);
              if (leapDay.isBetween(this, d)) {
                  numLeapDays++;
              }
          }
      }
      return numLeapDays;
  }

  /** Checks whether the given year is a leap year.
   *  @return true if and only if the input year is a leap year.
   */
  public static boolean isLeapYear(int year) {
      if (year % 400 == 0) {
          return true;
      } else if (year % 100 == 0) {
          return false;
      } else if (year % 4 == 0) {
          return true;
      } else {
          return false;
      }
  }

  /** Returns the number of days in a given month.
   *  @param month is a month, numbered in the range 1...12.
   *  @param year is the year in question, with no digits omitted.
   *  @return the number of days in the given month.
   */
  public static int daysInMonth(int month, int year) {
      if (month == 2) {
          if (isLeapYear(year)) {
              return 29;
          } else {
              return 28;
          }
      } else if (is30Month(month)) {
          return 30;
      } else {
          return 31;
      }
  }

  /** Checks whether the given date is valid.
   *  @return true if and only if month/day/year constitute a valid date.
   *
   *  Years prior to A.D. 1 are NOT valid.
   */
  public static boolean isValidDate(int month, int day, int year) {
      if (year < 1) {
          return false;
      }
      if (month < 1 || month > 12) {
          return false;
      }
      int numDays = daysInMonth(month, year);
      if (day < 1 || day > numDays) {
          return false;
      } else {
          return true;
      }
  }

  /** Returns a string representation of this date in the form month/day/year.
   *  The month, day, and year are expressed in full as integers; for example,
   *  12/7/2006 or 3/21/407.
   *  @return a String representation of this date.
   */
  public String toString() {
      String result = this.month + "/" + this.day + "/" + this.year;
      return result;
  }

  /** Determines whether this Date is before the Date d.
   *  @return true if and only if this Date is before d. 
   */
  public boolean isBefore(Date d) {
      boolean sameYear, sameMonth;
      sameYear = (this.year == d.year);
      sameMonth = (this.month == d.month);

      if (this.year > d.year) {
          return false;
      } else if (sameYear && this.month > d.month) {
          return false;
      } else if (sameYear && sameMonth && this.day >= d.day) {
          return false;
      } else {
          return true;
      }
  }

  /** Determines whether this Date is after the Date d.
   *  @return true if and only if this Date is after d. 
   */
  public boolean isAfter(Date d) {
      return (!this.isBefore(d)) && (this.day != d.day);
  }

  /** Returns the number of this Date in the year.
   *  @return a number n in the range 1...366, inclusive, such that this Date
   *  is the nth day of its year.  (366 is used only for December 31 in a leap
   *  year.)
   */
  public int dayInYear() {
      int total = 0;
      for (int m = 1; m < this.month; m++) {
          total += daysInMonth(m, this.year);
      }
      total += this.day;
      return total;
  }

  /** Determines the difference in days between d and this Date.  For example,
   *  if this Date is 12/15/2012 and d is 12/14/2012, the difference is 1.
   *  If this Date occurs before d, the result is negative.
   *  @return the difference in days between d and this date.
   */
  public int difference(Date d) {
      if (this.isBefore(d)) {
          return d.difference(this);
      }
      int diff = 0;
      if (this.isAfter(d)) {
          for (int y = d.year; y < this.year; y++) {
              diff += 365;
          }
          diff += this.dayInYear() - d.dayInYear();
          diff += this.leapDaysRange(d);
      } else {
          diff = 0;
      }
      return diff;
  }

  public static void main(String[] argv) {
    System.out.println("\nTesting constructors.");
    Date d1 = new Date(1, 1, 1);
    System.out.println("Date should be 1/1/1: " + d1);
    d1 = new Date("2/4/2");
    System.out.println("Date should be 2/4/2: " + d1);
    d1 = new Date("2/29/2000");
    System.out.println("Date should be 2/29/2000: " + d1);
    d1 = new Date("2/29/1904");
    System.out.println("Date should be 2/29/1904: " + d1);

    d1 = new Date(12, 31, 1975);
    System.out.println("Date should be 12/31/1975: " + d1);
    Date d2 = new Date("1/1/1976");
    System.out.println("Date should be 1/1/1976: " + d2);
    Date d3 = new Date("1/2/1976");
    System.out.println("Date should be 1/2/1976: " + d3);

    Date d4 = new Date("2/27/1977");
    Date d5 = new Date("8/31/2110");

    /** Testing isLeapYear method. */
    System.out.println("\nIs 2394 a leap year? " + Date.isLeapYear(2394));
    System.out.println("Is 3500 a leap year? " + Date.isLeapYear(3500));
    System.out.println("Is 24 a leap year? " + Date.isLeapYear(24));
    System.out.println("Is 4000 a leap year? " + Date.isLeapYear(4000));

    /** Testing daysInMonth method. */
    System.out.println("\nDays in March, 2348? " + Date.daysInMonth(3, 2348));
    System.out.println("Days in November, 1463? " + Date.daysInMonth(11, 1463));
    System.out.println("Days in August, 1927? " + Date.daysInMonth(8, 1927));
    System.out.println("Days in June, 8265? " + Date.daysInMonth(6, 8265));
    System.out.println("Days in February, 1995? " + Date.daysInMonth(2, 1995));
    System.out.println("Days in February, 1996? " + Date.daysInMonth(2, 1996));

    /** Testing isValidDate method. */
    System.out.println("\n2/15/1849 valid? " + Date.isValidDate(2, 15, 1849));
    System.out.println("2/29/1849 valid? " + Date.isValidDate(2, 29, 1849));
    System.out.println("2/29/2004 valid? " + Date.isValidDate(2, 29, 2004));
    System.out.println("5/0/1849 valid? " + Date.isValidDate(5, 0, 1849));
    System.out.println("5/31/1849 valid? " + Date.isValidDate(5, 31, 1849));
    System.out.println("5/32/1849 valid? " + Date.isValidDate(5, 32, 1849));
    System.out.println("9/0/1849 valid? " + Date.isValidDate(9, 0, 1849));
    System.out.println("9/30/1849 valid? " + Date.isValidDate(9, 30, 1849));
    System.out.println("9/31/1849 valid? " + Date.isValidDate(9, 31, 1849));

    /** Testing dayInYear method. */
    System.out.println("");
    Date d = new Date("1/1/1997");
    System.out.println("1/1/1997 is day #1? " + d.dayInYear());
    d = new Date("12/31/1997");
    System.out.println("12/31/1997 is day #365? " + d.dayInYear());
    d = new Date("12/31/1996");
    System.out.println("12/31/1996 is day #366? " + d.dayInYear());
    d = new Date("7/17/1997");
    System.out.println("7/17/1997 is day #198? " + d.dayInYear());
    d = new Date("7/17/1996");
    System.out.println("7/17/1996 is day #199? " + d.dayInYear());

    System.out.println("\nTesting before and after.");
    System.out.println(d2 + " after " + d1 + " should be true: " + 
                       d2.isAfter(d1));
    System.out.println(d3 + " after " + d2 + " should be true: " + 
                       d3.isAfter(d2));
    System.out.println(d1 + " after " + d1 + " should be false: " + 
                       d1.isAfter(d1));
    System.out.println(d1 + " after " + d2 + " should be false: " + 
                       d1.isAfter(d2));
    System.out.println(d2 + " after " + d3 + " should be false: " + 
                       d2.isAfter(d3));

    System.out.println(d1 + " before " + d2 + " should be true: " + 
                       d1.isBefore(d2));
    System.out.println(d2 + " before " + d3 + " should be true: " + 
                       d2.isBefore(d3));
    System.out.println(d1 + " before " + d1 + " should be false: " + 
                       d1.isBefore(d1));
    System.out.println(d2 + " before " + d1 + " should be false: " + 
                       d2.isBefore(d1));
    System.out.println(d3 + " before " + d2 + " should be false: " + 
                       d3.isBefore(d2));

    System.out.println("\nTesting difference.");
    System.out.println(d1 + " - " + d1  + " should be 0: " + 
                       d1.difference(d1));
    System.out.println(d2 + " - " + d1  + " should be 1: " + 
                       d2.difference(d1));
    System.out.println(d3 + " - " + d1  + " should be 2: " + 
                       d3.difference(d1));
    System.out.println(d3 + " - " + d4  + " should be -422: " + 
                       d3.difference(d4));
    System.out.println(d5 + " - " + d4  + " should be 48762: " + 
                       d5.difference(d4));
  }
}
