function square(x) {
  return x*x;
}

/*
 * An example of a passing test.
 */
print(square(-3))
// => 9

/*
 * An example of a failed test.
 */
print(square(0))
// => 1

print(square([]))
// => 0

/*
 * This segment will print false because the execution will not wait until
 * the test is already reached.
 */
var a = new Date().getTime();
wait(3000);
var b;
b = new Date();
b = b.getTime();
print(b - a >= 3000)
// => false

/*
 * This segment will print true since the test ``// => 1'' forces the code
 * to wait.
 */
var a = new Date().getTime();
wait(3000);
print(1)
// => 1
var b;
b = new Date();
b = b.getTime();
print(b - a >= 3000)
// => true

/*
 * Here's where the library raises an unhandled exception since tests not
 * within the global frame don't work.
 */
function hello() {
  print("hello");
  // => hello
}
