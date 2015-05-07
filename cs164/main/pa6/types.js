/*
triple(x) is a dynamic function that returns what a programmer might expect given
the type of the input argument

We cannot use any functionality of doctest.js within the function to check types
on the fly.
*/
function triple(x) {
  if (x === undefined || x === null) {
    throw "Cannot triple null or undefined";
  }
  switch (x.constructor) {
    case Number: return x*3;
    case String: return x+x+x;
    case Array: return x.concat(x.concat(x));
    case Boolean: throw "Cannot triple a boolean";
    case Object: throw "Cannot triple an object";
    case Function: return function(y) { return x(x(x(y))); };
    default: throw "Unknown argument to triple(x)";
  }
}

/*
In order to test input and output types, we still have to include boilerplate
type-checking code, making the contract a part of the function logic.

doctest.js only checks the result of a single execution against an expected
output. It does well in testing for thrown exceptions and continuing the execution
of tests, but we cannot use it to separate the function's contract from its logic
and still do checking every time the function is called.
*/

print(triple(4));
// => 12

print(triple("abc"));
// => abcabcabc

print(triple(""));
// =>

print(triple([1,2,3]));
// => [1, 2, 3, 1, 2, 3, 1, 2, 3]

print(triple([]));
// => []

print(triple({a: 1, b: 2, c: 3}));
// => Error: Cannot triple an object

print(triple(null));
// => Error: Cannot triple null or undefined

print(triple(undefined));
// => Error: Cannot triple null or undefined

print(triple(true));
// => Error: Cannot triple a boolean

print(triple(new Date()));
// => Error: Unknown argument to triple(x)

var squareThreeTimes = triple(function(x) { return x*x; });
print(squareThreeTimes(2));
// => 256
