if (typeof(module) !== 'undefined') {
  var ExecError = require('./errors.js').ExecError;
  var desugarAST = require('./desugar.js').desugarAST;
  var env = require('./environment.js');
  var envRoot = env.root;
  var envExtend = env.extend;
  var envBind = env.bind;
  var envUpdate = env.update;
  var envLookup = env.lookup;
}

var interpret = function(asts, log, err) {

  var root = envRoot();
  root['*title'] = 'Root';

  // TODO: Complete the closure implementation. What's missing?
  function makeClosure(names, body, env) {
    return {
      names: names,
        body: body,
        type: 'closure',
        env: env
    };
  }

  function evalBlock(t, env) {
    var last = null;
    t.forEach(function(n) {
      last = evalStatement(n, env);
    });
    return last;
  }

  function evalExpression(node, env) {
    //TODO: Implement the expressions that aren't yet handled.
    switch (node.type){
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
        if (first === second) {
          return 1;
        } else {
          return 0;
        }
      case '!=':
        var first = evalExpression(node.operand1, env);
        var second = evalExpression(node.operand2, env);
        if (first !== second) {
          return 1;
        } else {
          return 0;
        }
      case '<=':
        var first = evalExpression(node.operand1, env);
        var second = evalExpression(node.operand2, env);
        if (typeof(first) != 'number' || typeof(second) != 'number') {
          throw new ExecError("Operands to <= must be numbers.");
        }
        if (first <= second) {
          return 1;
        } else {
          return 0;
        }
      case '>=':
        var first = evalExpression(node.operand1, env);
        var second = evalExpression(node.operand2, env);
        if (typeof(first) != 'number' || typeof(second) != 'number') {
          throw new ExecError("Operands to >= must be numbers.");
        }
        if (first >= second) {
          return 1;
        } else {
          return 0;
        }
      case '<':
        var first = evalExpression(node.operand1, env);
        var second = evalExpression(node.operand2, env);
        if (typeof(first) != 'number' || typeof(second) != 'number') {
          throw new ExecError("Operands to < must be numbers.");
        }
        if (first < second) {
          return 1;
        } else {
          return 0;
        }
      case '>':
        var first = evalExpression(node.operand1, env);
        var second = evalExpression(node.operand2, env);
        if (typeof(first) != 'number' || typeof(second) != 'number') {
          throw new ExecError("Operands to > must be numbers.");
        }
        if (first > second) {
          return 1;
        } else {
          return 0;
        }
      case "id":
        return envLookup(env, node.name);
      case "int-lit":
        return node.value;
      case "string-lit":
        return node.value;
      case "null":
        return null;
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
      case "call":
        var fn = evalExpression(node.function, env);
        if (fn !== null && fn.type && fn.type === 'closure') {
          // TODO: Perform a call. The code below will only work if there are
          // no arguments, so you'll have to fix it.  The crucial steps are:
          // 1. Extend the environment with a new frame --- see environment.js.
          // 2. Add argument bindings to the new frame.
          var newEnv = envExtend(fn.env);
          if (fn.names.length != node.arguments.length) {
            throw new ExecError("Wrong number of arguments");
          }
          for (var i = 0; i < fn.names.length; i++) {
            argval = evalExpression(node.arguments[i], env);
            envBind(newEnv, fn.names[i].name, argval);
          }
          return evalBlock(fn.body, newEnv);
        } else {
          throw new ExecError('Trying to call non-lambda');
        }
        break;
    }
  }

  function evalStatement(node, env) {
    switch (node.type) {
      // TODO: Complete for statements that aren't handled
      case "def":
        envBind(env, node.name.name, evalExpression(node.value, env));
        return null;
      case "asgn":
        envUpdate(env, node.name.name, evalExpression(node.value, env));
        return null;
      case "print":
        var eval = evalExpression(node.value, env);
        if (eval !== null && typeof(eval) === "object" && eval.type === "closure") {
            log("Lambda");
        } else {
            log(eval);
        }
        return null;
      case "error":
        var eval = evalExpression(node.message, env);
        if (eval !== null && typeof(eval) === "object" && eval.type === "closure") {
            throw new ExecError("Lambda");
        } else {
            throw new ExecError(eval);
        }
      case "exp":
        return evalExpression(node.body, env);
      default:
        throw new Error(
          "What's " + node.type + "? " + JSON.stringify(node)
      );
    }
  }

  var desugarOne = function(asts){
    var ast, remaining_asts;
    if (asts.length > 0){
      ast = asts.shift(); // pops first item
      remaining_asts = asts; // first item already popped
    }
    else{ return; } // no more ASTs to eval
    desugarAST(ast, function(ast) {
      try {
        evalBlock(ast, root);
        desugarOne(remaining_asts);
      } catch (e) {
        if (e.constructor === ExecError) {
          log('Error: ' + e.message);
        } else {
          throw e;
        }
      }
    });
  };
  desugarOne(asts);
};

// Makes the interpreter importable in Node.js
if (typeof(module) !== 'undefined') {
  module.exports = {
    'interpret': interpret
  };
}
