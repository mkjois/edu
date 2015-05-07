function run(x, y) {
  /**
   * #contract: x is good and not odd and below(15)
   * #contract: if x is good then @pass else x is 2
   * #contract: y is not good; y is odd
   * #contract: y is below(2) or odd
   */
  return x + 5;
}

run(3, 1); //fail 1 and 2
run(7, 1); //fail 1
run(12, 1);
run(16, 1); // fail 1
run(2, 1); // fail 1
run(6, 2); // fail 3, 4
run(6, 7); // fail 3

function foo(input) {
  /**
   * #contract: if input is good then @output is odd else @pass
   * #contract: if input is good; @output is not odd then @fail else @pass
   */
  return input;
}

foo(8); // fail 1
foo(7);
