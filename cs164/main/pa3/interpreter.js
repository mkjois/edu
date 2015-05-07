'use strict';

if (typeof(module) !== 'undefined') {
  var ExecError = require('./errors.js').ExecError;
  var desugarAST = require('./desugar.js').desugarAST;
  var env = require('./environment.js');
  var Table = require('./table.js').Table;
  var envRoot = env.root;
  var envExtend = env.extend;
  var envBind = env.bind;
  var envUpdate = env.update;
  var envLookup = env.lookup;
}

var cCounter = 0;
var interpret = function(asts, log, err) {

  var root = envRoot();
  root['*title'] = 'Root';

  // Returns a closure, a data structure which stores the param names
  // (id objects), the body of the function, and the referencing
  // environment, in which it was initialized --- (for lexical scoping).
  function makeClosure(names, body, env) {
    return {
      names: names,
        body: body,
        type: 'closure',
        env: env,
        num: cCounter++,
        toString: function() {
            return "Lambda"
        }
    };
  }

  function to164Object(o) {
    // convert a Python object to a suitable 164 object
    var type = typeof o;
    if (type === 'number') {
      return o;
    } else if (type === 'string') {
      return o;
    } else {
      // throw new ExecError('converting unknown type')
      console.log('converting unknown type');
      return null;
    }
  }

  function toJSObject(o) {
    // convert a Python object to a suitable JS object
    var type = typeof o;
    if (type === 'number') {
      return o;
    } else if (type === 'string') {
      return o;
    } else {
      // throw new ExecError('converting unknown type')
      console.log('converting unknown type');
      return null;
    }
  }

  function evalBlock(t, env) {
    var last = null;
    t.forEach(function(n) {
      last = evalStatement(n, env);
    });
    return last;
  }

  function evalExpression(node, env) {
    // TODO: Use your own evalExpression here
    // keep the 'native' case below for using native JavaScript
    // introduce new cases as needed to implement dictionaries,
    // lists, and objects
    switch (node.type) {
      case 'native':
        var func = node.function.name;
        var args = node.arguments;

        var jsArgs = args.map(function(n) {
          return toJSObject(evalExpression(n, env));
        });
        var jsFunc = runtime[func];

        var ret = jsFunc.apply(null, jsArgs);
        return to164Object(ret);
      case '+':
        var first = evalExpression(node.operand1, env);
        var second = evalExpression(node.operand2, env);
        var t1 = typeof(first), t2 = typeof(second);
        if ((t1 != 'number' && t1 != 'string') || (t2 != 'number' && t2 != 'string')) {
          throw new ExecError("Operands to + must be numbers or strings.");
        }
        return first + second;
      case '-':
        var first = evalExpression(node.operand1, env);
        var second = evalExpression(node.operand2, env);
        if (typeof(first) != 'number' || typeof(second) != 'number') {
          throw new ExecError("Operands to - must be numbers.");
        }
        return first - second;
      case '/':
        var first = evalExpression(node.operand1, env)
        var second = evalExpression(node.operand2, env);
        if (typeof(first) != 'number' || typeof(second) != 'number') {
          throw new ExecError("Operands to / must be numbers.");
        }
        if (second == 0) {
          throw new ExecError("Division by zero");
        }
        return Math.floor(first / second);
      case '*':
        var first = evalExpression(node.operand1, env);
        var second = evalExpression(node.operand2, env);
        if (typeof(first) != 'number' || typeof(second) != 'number') {
          throw new ExecError("Operands to * must be numbers.");
        }
        return first * second;
      case '==':
        var first = evalExpression(node.operand1, env);
        var second = evalExpression(node.operand2, env);
        return first === second ? 1 : 0;
      case '!=':
        var first = evalExpression(node.operand1, env);
        var second = evalExpression(node.operand2, env);
        return first !== second ? 1 : 0;
      case '<=':
        var first = evalExpression(node.operand1, env);
        var second = evalExpression(node.operand2, env);
        if (typeof(first) != 'number' || typeof(second) != 'number') {
          throw new ExecError("Operands to <= must be numbers.");
        }
        return first <= second ? 1 : 0;
      case '>=':
        var first = evalExpression(node.operand1, env);
        var second = evalExpression(node.operand2, env);
        if (typeof(first) != 'number' || typeof(second) != 'number') {
          throw new ExecError("Operands to >= must be numbers.");
        }
        return first >= second ? 1 : 0;
      case '<':
        var first = evalExpression(node.operand1, env);
        var second = evalExpression(node.operand2, env);
        if (typeof(first) != 'number' || typeof(second) != 'number') {
          throw new ExecError("Operands to < must be numbers.");
        }
        return first < second ? 1 : 0;
      case '>':
        var first = evalExpression(node.operand1, env);
        var second = evalExpression(node.operand2, env);
        if (typeof(first) != 'number' || typeof(second) != 'number') {
          throw new ExecError("Operands to > must be numbers.");
        }
        return first > second ? 1 : 0;
      case "empty-dict-lit":
        return new Table();
      case "get":
        var table = evalExpression(node.dict, env);
        if (table && table.type && table.type === "table") {
            return table.get(evalExpression(node.field, env));
        } else {
            throw new ExecError("Trying to index non-table.");
        }
      case "id":
        return envLookup(env, node.name);
      case "int-lit":
        return node.value;
      case "string-lit":
        return node.value;
      case "null":
        return null;
      case "len":
        var table = evalExpression(node.dict, env);
        if (table && table.type && table.type === "table") {
            return table.get_length();
        } else {
            throw new ExecError("Trying to call len on non-table.");
        }
      case "in":
        var key = evalExpression(node.operand1, env);
        var table = evalExpression(node.operand2, env);
        if (table && table.type && table.type === "table") {
            return table.has_key(key) ? 1 : 0;
        } else {
            throw new ExecError("Trying to find key in non-table.");
        }
      case "ite":
        var cond = evalExpression(node.condition, env);
        var ct = evalExpression(node.true,  env);
        var cf = evalExpression(node.false, env);
        if (cond == null || cond == 0) {
          cond = false;
        }
        if ((typeof cond != 'boolean') && (typeof cond != 'number')) {
          throw new ExecError('Condition not a boolean');
        }
        return cond ? ct : cf;
      case "lambda":
        return makeClosure(node.arguments, node.body, env);
      case "exp":
        return evalExpression(node.body, env);
      case "call":
        var fn = evalExpression(node.function, env);
        if (fn !== null && fn.type && (fn.type === 'closure' || fn.type === 'type_construct')) {
          // TODO: Perform a call. The code below will only work if there are
          // no arguments, so you'll have to fix it.  The crucial steps are:
          // 1. Extend the environment with a new frame --- see environment.js.
          // 2. Add argument bindings to the new frame.
          var retval, argvals = [];
          var newEnv = envExtend(fn.env);
          for (var i in node.arguments) {
            var argval = evalExpression(node.arguments[i], env);
            argvals.push(argval)
          }
          if (fn.type === 'closure') {
              if (argvals.length !== fn.names.length) {
                throw new ExecError("Wrong number of arguments");
              }
              for (var i in fn.names) {
                envBind(newEnv, fn.names[i].name, argvals[i]);
              }
              retval = evalBlock(fn.body, newEnv);
          } else {
              if (argvals.length !== 1) {
                throw new ExecError("Wrong number of arguments");
              }
              retval = (argvals[0] && argvals[0].type === "table") ? 'table' : 'other';
          }
          return retval
        } else {
          throw new ExecError('Trying to call non-lambda');
        }
        break;
    }
  }

  function evalStatement(node, env) {
    // TODO: Use your own evalStatement here
    // introduce new cases as needed to implement dictionaries,
    // lists, and objects
    switch (node.type) {
      // TODO: Complete for statements that aren't handled
      case "put":
        var table = evalExpression(node.dict, env);
        var key = evalExpression(node.field, env);
        var value = evalExpression(node.value, env);
        if (table && table.type && table.type === "table") {
            table.put(key, value);
            return null;
        } else {
            throw new ExecError("Trying to put into non-table.");
        }
      case "def":
        envBind(env, node.name.name, evalExpression(node.value, env));
        return null;
      case "asgn":
        envUpdate(env, node.name.name, evalExpression(node.value, env));
        return null;
      case "print":
        var e = evalExpression(node.value, env);
        if (e !== null) {
            log(e.toString());
        } else {
            log(e);
        }
        return null;
      case "error":
        var e = evalExpression(node.message, env);
        throw new ExecError(e);
      case "exp":
        return evalExpression(node.body, env);
      default:
        throw new Error(
          "What's " + node.type + "? " + JSON.stringify(node)
      );
    }
  }

  function desugarAll(remaining, desugared, callback) {
    if (remaining.length == 0) {
      setTimeout(function () {
        callback(desugared);
      }, 0);
      return;
    }

    var head = remaining.shift();
    desugarAST(head, function(desugaredAst) {
      desugared.push(desugaredAst);
      desugarAll(remaining, desugared, callback);
    });
  }

  desugarAll(asts, [], function(desugaredAsts) {
    for (var i = 0, ii = desugaredAsts.length; i < ii; ++i) {
      try {
        evalBlock(desugaredAsts[i], root);
      } catch (e) {
        if (e instanceof ExecError) {
          log('Error: ' + e.message);
        } else {
          throw e;
        }
      }
    }
  });
};

if (typeof(module) !== 'undefined') {
  module.exports = {
    'interpret': interpret
  };
}
