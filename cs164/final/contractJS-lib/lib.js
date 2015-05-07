var assert = require('assert');

module.exports = {
  'enforce': function(f, msg) {
    if (!f()) {
      console.error("\u001b[31m" + msg + "\u001b[0m");
    }
  },

  /**
   *  Checks if a number is odd
   */
  'odd': function(x) {
    try {
      if ((x % 2) == 1) {
        return true;
      }
      return false;
    } catch (e) {
      return false;
    }
  },

  /**
   *  Returns a function f which returns f(x)
   */
  'single': function(x) {
    return {'each': function(f) { 
      try {
        return f(x); 
      } catch(e) {
        return false;
      }
    }};
  },

  /**
   *  Returns a function that takes a parameter y and
   *  checks if y is less than x
   */
  'below': function(x) {
    return function(y) { 
      try {
        return y < x; 
      }
      catch (e) {
        return false;
      }
    };
  },

  /**
   *  Returns a function that takes a parameter y and
   *  checks if y is more than x
   */
  'above': function(x) {
    return function(y) { 
      try {
        return y > x; 
      }
      catch (e) {
        return false;
      }
    };
  },

  /**
   *  Returns a function that takes a parameter y and
   *  checks if y is equal to x
   */
  'equal': function(x) {
    return function(y) { 
      try {
        return y === x; 
      } catch (e) {
        return false;
      }
    };
  },

  /**
   *  Checks if an array is sorted in ascending order
   */
  'sorted': function(arr) {
    try {
      if (arr.length == 1 || (arr.length == 2 && arr[0] < arr[1])) {
        return true;
      }
      var i = 0;
      for (i = 0; i < arr.length - 1; i++) {
        if (arr[i] > arr[i+1]) {
          return false;
        }
      }
      return true;
    } catch (e) { return false; }
  },

  /**
   *  Returns true if every element in the array satisfies a predicate f
   */
  'all': function(arr) {
    return {'each': function(f) {
      try {
    	 return arr.every(f);
      } catch (e) {
        return false;
      }
    }};
  },

  /**
   *  Returns true if the parameter x is a number
   */
  'number': function(x) {
    try {
  	  return typeof x === 'number';
    } catch (e) {
      return false;
    }
  },

  /**
   *  Returns true if the parameter x is a string
   */
  'string': function(x) {
    try {
  	  return typeof x === 'string';
    } catch (e) {
      return false;
    }
  },

  /**
   *  Returns true if the parameter x is an array
   */
  'array': function(x) {
    try {
  	  return x.constructor === Array;
    } catch (e) {
      return false;
    }
  },

  /**
   *  Returns true if the parameter x is an object
   */
  'object': function(x) {
    try {
  	  return typeof x === 'object';
    } catch (e) {
      return false;
    }
  },

  /**
   *  Returns true if the parameter x is a function
   */
  'function': function(x) {
    try {
  	  return typeof x === 'function';
    } catch (e) {
      return false;
    }
  },

  /**
   *  Takes an array or object arr and returns a function that takes a single argument,
   *  which returns true if that argument is in arr, or if it's a property of an object arr
   */
  'in': function(arr) {
    return function(x) {
      try {
        if (arr.constructor === Array) {
    	    return arr.indexOf(x) !== -1;
        } else if (typeof arr === 'object') {
          return arr.hasOwnProperty(x);
        }
      } catch (e) {
        return false;
      }
    };
  },

  'equality': function(a, b) {
    try {
      assert.deepEqual(a, b);
      return true;
    } catch(e) {
      return false;
    }
  }
}
