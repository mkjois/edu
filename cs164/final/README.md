# Contract
A lightweight JavaScript contract and doctest utility.

By Manny Jois, Romil Singapuri, Khoa Tran

* [Proposal](https://www.google.com/url?q=https://docs.google.com/a/berkeley.edu/document/d/1gkfXjkVFW5d0HlQBsnYlgo0GeRG0_fByIVRPQAJ36q4)
* [PA6 Report](https://www.google.com/url?q=https://docs.google.com/a/berkeley.edu/document/d/1kD8CHTRA9Bjjmh1f5EW-6SySvNp6P-HBatGZFX2Cmcs)
* [Design Doc](https://docs.google.com/a/berkeley.edu/document/d/1ZsAlnEoVs3ebRWCmEQbhH8pzp6zzwEm_PzvlwWl7vqg)
* [Proposal Slides]
(https://docs.google.com/a/berkeley.edu/presentation/d/1LpnB77gtrrGRYEeDFUt_qk5_eHn_3SLsmBglT-1J0us)
* [Poster Slides]
(https://docs.google.com/a/berkeley.edu/presentation/d/1sYMKlNCjrbfEThptECQly2mSlHAi85HNWwF2vCIdM9U)

## Usage

`node contract.js src dest`
* `src` is the directory of JS files with contracts and doctest directives
* `dest` is the directory for compiled JS files. It will have the same structure as `src`.

## Installation

*(instructions forthcoming...)*

## Constructs

A trivial example:
```
function sumOfSquares(x, y) {
  #contract: x, y are number
  #contract: @output is number and non_negative
  #example: sumOfSquares(4, -3) => 25
  #example: sumOfSquares(4, "word") => Error
  return x*x + y*y;
}
```

### Contract Directives

Syntax 1: `#contract: SUBJECTS VERB DESCRIPTOR`. This is called a *clause*.

Example: (see trivial example)

Syntax 2: `#contract: if CLAUSE then CLAUSE else CLAUSE` where CLAUSE is anything you can write with Syntax 1.

Example: `if data is sorted then @pass else arg1 is positive`

Clauses evaluate to True or False, so Syntax 2 is a good way to selectively test inputs and outputs. As shown above, you can use `@pass` and `@fail` as trivial true and false clauses, respectively.

#### Subjects

A comma separated list of sole nouns or qualifier-noun pairs. The nouns must be your function arguments or `@output`, which represents the return value of your function.

Examples:
* `x`
* `arg1, arg2, arg3`
* `all keys, @output, even_indexed data`

#### Verbs

The only two verbs are `is` and `are`, both of which simply evaluate the descriptor with every subject.

#### Descriptors

A combination of adjectives or primitive literals (null, integer, float, string) using boolean operators. Adjectives can even take arguments (once again, arguments must be adjectives or primitive literals).
For precedence, `not` is the highest, followed by `and`, then `or`.

Examples:
* `sorted`
* `positive and odd`
* `"special" or between(2, 8) and not 4.5e-2` (remember `or` has lowest precedence)

Subjects are evaluated one at a time against the whole descriptor. The evaluation is *short-circuiting*, meaning as soon as one part of the descriptor doesn't hold for one subject, the whole clause evaluates to False. If everything passes, the clause evaluates to True.

### Setup Directives

Syntax: `#setup: JS` where JS is arbitrary Javascript code.

Example: `#setup: my_array = ["one", {two: 3}]`

You can use this directive to do any sort of quick initialization relavant to any of your example directives.

### Example Directives

Syntax: `#example: JS => JS` where JS is arbitrary Javascript code.

Example: (see trivial example)

This directive tests, upon the first call to the function, whether the left side evaluates to the same value as the right side. If you use `Error` for the right side, it will test whether or not the left side throws any error.

## User-defined constructs

In the same directory as `contract.js` you can define your own library of qualifiers and adjectives in `userlib.js`. Simply assign `module.exports` to be an object where keys are the names of your constructs. The values you need are explained below.

### Qualifiers

A qualifier is a function that takes in one argument `arg` (the subject) and returns an object with a key `each`. The value of `each` should be a function that takes in a callback and calls it on `arg` however you see fit.

Example (even-indexed values of an array):
```
even_indexed: function(arr) {
  return {
    each: function(fn) {
      if (arr && arr.constructor === Array) {
        for (var i = 0; i < arr.length; i = i + 2) {
          fn(arr[i]);
        }
      }
    }
  };
}
```

### Adjectives

An adjective with no arguments is a function that takes in an argument `arg` (the subject) and returns `true` or `false`. An adjective that takes arguments instead returns a function that takes those arguments and returns `true` or `false`.

Examples:
```
positive: function(subject) {
  if (subject && subject.constructor === Number) {
    return subject > 0;
  }
  return false;
},
between: function(subject) {
  return function(lower, upper) {
    // return false if any are not numbers
    return lower <= subject && subject <= upper;
  };
}
```

## Current Limitations (i.e. improvement goals)

* Any contract directive that uses `@output` will be tested after the original function is called.
* If-then-else contracts cannot be nested. However, each of the three branches can contain compound clauses.
* The only arguments you may pass in to adjectives are null, integer, float and string literals. Anything else is not guaranteed to work as expected.
* All directives must come at the beginning of the function definition, right after the opening curly brace. You can, however, use any combination of single- and multi-line comments.
* You can only write contracts and doctests for *named* functions in the global frame.
* Different Error types cannot be tested for in example directives.
