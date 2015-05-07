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

var closureCounter = 0;
var coroutineCounter = 0;

var interpret = function(asts, log, err) {
  // PA4: Bytecode interpreter.  Motivation: stackful interpreter
  // cannot implement coroutines.

  function compileToBytecode(ast) {
    // TODO step 2: Complete this function, which takes an AST as input
    // and produces bytecode as its output

    // This helper function generates a unique register name
    function uniquegen() {
      return '#btc-reg-' + uniquegen.counter++;
    }
    uniquegen.counter = 1;

    function btcnode(node, target, btc) {
      //TODO step 2: Handle every type of AST node you might receive!
      var instruction = {
        "type"  : node.type,
        "target": target
      };

      switch (node.type) {
        case "null":
          instruction.value = null;
          break;
        case "int-lit":
        case "string-lit":
          instruction.value = node.value;
          break;
        case "empty-dict-lit":
          break;
        case "+":
        case "-":
        case "*":
        case "/":
        case "==":
        case "!=":
        case "<":
        case ">":
        case "<=":
        case ">=":
          var regOp1 = uniquegen();
          var regOp2 = uniquegen();
          btcnode(node.operand1, regOp1, btc);
          btcnode(node.operand2, regOp2, btc);
          instruction.operand1 = regOp1;
          instruction.operand2 = regOp2;
          break;
        case "id":
          instruction.name = node.name;
          break;
        case "def":
        case "asgn":
          var regExpr = uniquegen();
          btcnode(node.value, regExpr, btc);
          instruction.name = node.name.name;
          instruction.value = regExpr;
          break;
        case "exp":
          btcnode(node.body, target, btc);
          break;
        case "print":
          var regExpr = uniquegen();
          btcnode(node.value, regExpr, btc);
          instruction.value = regExpr;
          break;
        case "error":
          var regExpr = uniquegen();
          btcnode(node.message, regExpr, btc);
          instruction.value = regExpr;
          break;
        case "get":
          var regDict = uniquegen();
          var regKey = uniquegen();
          btcnode(node.dict, regDict, btc);
          btcnode(node.field, regKey, btc);
          instruction.dict = regDict;
          instruction.key = regKey;
          break;
        case "put":
          var regDict = uniquegen();
          var regKey = uniquegen();
          var regVal = uniquegen();
          btcnode(node.dict, regDict, btc);
          btcnode(node.field, regKey, btc);
          btcnode(node.value, regVal, btc);
          instruction.dict = regDict;
          instruction.key = regKey;
          instruction.value = regVal;
          break;
        case 'in':
          var regKey = uniquegen();
          var regDict = uniquegen();
          btcnode(node.operand1, regKey, btc);
          btcnode(node.operand2, regDict, btc);
          instruction.dict = regDict;
          instruction.key = regKey;
          break;
        case "len":
          var regDict = uniquegen();
          btcnode(node.dict, regDict, btc);
          instruction.dict = regDict;
          break;
        case "ite":
          var regCond = uniquegen();
          var regTrue = uniquegen();
          var regFalse = uniquegen();
          btcnode(node.condition, regCond, btc);
          btcnode(node.true, regTrue, btc);
          btcnode(node.false, regFalse, btc);
          instruction.cond = regCond;
          instruction.true = regTrue;
          instruction.false = regFalse;
          break;
        case "lambda":
          var argNames = [];
          for (var i in node.arguments) {
            var arg = node.arguments[i];
            argNames.push({
              'type': 'id',
              'name': arg.name
            });
          }
          var bodyBtc = btcblock(node.body);
          instruction.arguments = argNames;
          instruction.body = bodyBtc;
          break;
        case "call":
          var regFunc = uniquegen();
          btcnode(node.function, regFunc, btc)
          var regArgs = [];
          for (var i in node.arguments) {
            var reg = uniquegen();
            var arg = node.arguments[i];
            btcnode(arg, reg, btc);
            regArgs.push(reg);
          }
          instruction.function = regFunc;
          instruction.arguments = regArgs;
          break;
        case "coroutine":
          var regBody = uniquegen();
          btcnode(node.body, regBody, btc)
          instruction.body = regBody;
          break;
        case "yield":
          var regArg = uniquegen();
          btcnode(node.arg, regArg, btc)
          instruction.arg = regArg;
          break;
        case "resume":
          var regCo = uniquegen();
          var regArg = uniquegen();
          btcnode(node.coroutine, regCo, btc)
          btcnode(node.arg, regArg, btc)
          instruction.coroutine = regCo;
          instruction.arg = regArg;
          break;
        default:
          log("Unknown node:");
          log(JSON.stringify(node, undefined, 2));
      }

      if (instruction.type !== "exp") {
        btc.push(instruction);
      }
    }

    function btcblock(statements) {
      // TODO step 2: Complete this function so that functions have
      // explicit return statements
      var btc = [];
      var target;
      statements.forEach(function(statement, index) {
        target = uniquegen();
        btcnode(statement, target, btc);
      });

      if (!target) {
        // If the body of the lambda is empty, return val is null
        target = uniquegen();
        btc.push({
          'type'  : 'null',
          'value' : null,
          'target': target
        });
      }

      btc.push({
        'type' : 'return',
        'value': target
      });
      return btc;
    }

    return btcblock(ast);
  }

  // Coroutine execution states
  var BORN   = 0,
      RUN    = 1,
      SUSP_Y = 2,
      SUSP_R = 3,
      DEAD   = 4;

  // Returns a closure, a data structure which stores the param names
  // (id objects), the body of the function, and the referencing
  // environment, in which it was initialized --- (for lexical scoping).
  function makeClosure(names, body, env) {
    // TODO step 1: use your own makeClosure here
    return {
      type: 'closure',
      num: closureCounter++,
      names: names,
      body: body,
      env: env,
      toString: function() {
        return "Lambda"
      }
    };
  }

  // Returns a stack frame, a data structure which stores
  // information about the state of a function
  function makeStackFrame(bytecode, env, retReg, parent) {
    // TODO step 3: decide what you need to store in a stack frame
    // based on what your bytecode interpreter needs.
    // Decide whether the arguments above are sufficient.
    return {
      body: bytecode,
      env: env,
      retReg: retReg,
      pc: 0, // program counter
      parent: parent
    };
  }

  // Returns a new program state object, a data structure which stores
  // information about a particular stack
  function makeProgramState(bytecode, env, argName) {
    // TODO step 3: decide what you need to store in a program state
    // object based on what your bytecode interpreter needs.
    // Decide whether the arguments above are sufficient.
    return {
      type: 'coroutine',
      num: coroutineCounter++,
      state: BORN,
      argName: argName,
      sp: makeStackFrame(bytecode, env, null, null), // stack pointer
      parent: null,
      yieldArg: null,
      resumeArg: null,
      yieldComplete: false,
      resumeComplete: false,
      toString: function() {
        switch (this.state) {
          case BORN:
          case SUSP_Y:
          case SUSP_R:
            return "Coroutine: Suspended";
          case RUN:
            return "Coroutine: Running";
          case DEAD:
            return "Coroutine: Dead";
        }
      }
    };
  }

  function resumeProgram(programState) {
    // TODO step 3: implement this function, which executes
    // bytecode.  See how it's called in the execBytecode function.
    while (programState.sp.pc < programState.sp.body.length) {
      var node = programState.sp.body[programState.sp.pc];
      switch (node.type) {
        case "null":
        case "int-lit":
        case "string-lit":
          programState.sp.env[node.target] = node.value;
          programState.sp.pc++;
          break;
        case "empty-dict-lit":
          programState.sp.env[node.target] = new Table();
          programState.sp.pc++;
          break;
        case '+':
          var first = programState.sp.env[node.operand1];
          var second = programState.sp.env[node.operand2];
          var t1 = typeof(first), t2 = typeof(second);
          if ((t1 != 'number' && t1 != 'string') || (t2 != 'number' && t2 != 'string')) {
            throw new ExecError("Operands to + must be numbers or strings.");
          }
          programState.sp.env[node.target] = first + second;
          programState.sp.pc++;
          break;
        case '-':
          var first = programState.sp.env[node.operand1];
          var second = programState.sp.env[node.operand2];
          if (typeof(first) != 'number' || typeof(second) != 'number') {
            throw new ExecError("Operands to - must be numbers.");
          }
          programState.sp.env[node.target] = first - second;
          programState.sp.pc++;
          break;
        case '*':
          var first = programState.sp.env[node.operand1];
          var second = programState.sp.env[node.operand2];
          if (typeof(first) != 'number' || typeof(second) != 'number') {
            throw new ExecError("Operands to * must be numbers.");
          }
          programState.sp.env[node.target] = first * second;
          programState.sp.pc++;
          break;
        case '/':
          var first = programState.sp.env[node.operand1];
          var second = programState.sp.env[node.operand2];
          if (typeof(first) != 'number' || typeof(second) != 'number') {
            throw new ExecError("Operands to / must be numbers.");
          }
          if (second == 0) {
            throw new ExecError("Division by zero");
          }
          programState.sp.env[node.target] = Math.floor(first / second);
          programState.sp.pc++;
          break;
        case '==':
          var first = programState.sp.env[node.operand1];
          var second = programState.sp.env[node.operand2];
          programState.sp.env[node.target] = first === second ? 1 : 0;
          programState.sp.pc++;
          break;
        case '!=':
          var first = programState.sp.env[node.operand1];
          var second = programState.sp.env[node.operand2];
          programState.sp.env[node.target] = first !== second ? 1 : 0;
          programState.sp.pc++;
          break;
        case '<=':
          var first = programState.sp.env[node.operand1];
          var second = programState.sp.env[node.operand2];
          if (typeof(first) != 'number' || typeof(second) != 'number') {
            throw new ExecError("Operands to <= must be numbers.");
          }
          programState.sp.env[node.target] = first <= second ? 1 : 0;
          programState.sp.pc++;
          break;
        case '>=':
          var first = programState.sp.env[node.operand1];
          var second = programState.sp.env[node.operand2];
          if (typeof(first) != 'number' || typeof(second) != 'number') {
            throw new ExecError("Operands to >= must be numbers.");
          }
          programState.sp.env[node.target] = first >= second ? 1 : 0;
          programState.sp.pc++;
          break;
        case '<':
          var first = programState.sp.env[node.operand1];
          var second = programState.sp.env[node.operand2];
          if (typeof(first) != 'number' || typeof(second) != 'number') {
            throw new ExecError("Operands to < must be numbers.");
          }
          programState.sp.env[node.target] = first < second ? 1 : 0;
          programState.sp.pc++;
          break;
        case '>':
          var first = programState.sp.env[node.operand1];
          var second = programState.sp.env[node.operand2];
          if (typeof(first) != 'number' || typeof(second) != 'number') {
            throw new ExecError("Operands to > must be numbers.");
          }
          programState.sp.env[node.target] = first > second ? 1 : 0;
          programState.sp.pc++;
          break;
        case "id":
          var val = envLookup(programState.sp.env, node.name);
          programState.sp.env[node.target] = val;
          programState.sp.pc++;
          break;
        case "def":
          var val = programState.sp.env[node.value];
          envBind(programState.sp.env, node.name, val)
          programState.sp.env[node.target] = null;
          programState.sp.pc++;
          break;
        case "asgn":
          var val = programState.sp.env[node.value];
          envUpdate(programState.sp.env, node.name, val)
          programState.sp.env[node.target] = null;
          programState.sp.pc++;
          break;
        case "exp":
          programState.sp.pc++;
          break;
        case "print":
          var e = programState.sp.env[node.value];
          if (e !== null) {
              log(e.toString());
          } else {
              log(e);
          }
          programState.sp.env[node.target] = null;
          programState.sp.pc++;
          break;
        case "error":
          var e = programState.sp.env[node.value];
          throw new ExecError(e);
          programState.sp.env[node.target] = null;
          programState.sp.pc++;
          break;
        case "get":
          var table = programState.sp.env[node.dict];
          if (table && table.type && table.type === "table") {
            var key = programState.sp.env[node.key];
            var val = table.get(key);
            programState.sp.env[node.target] = val;
            programState.sp.pc++;
          } else {
            throw new ExecError("Trying to index non-table.");
          }
          break;
        case "put":
          var table = programState.sp.env[node.dict];
          if (table && table.type && table.type === "table") {
            var key = programState.sp.env[node.key];
            var val = programState.sp.env[node.value];
            table.put(key, val);
            programState.sp.env[node.target] = null;
            programState.sp.pc++;
          } else {
            throw new ExecError("Trying to put into non-table.");
          }
          break;
        case "in":
          var table = programState.sp.env[node.dict];
          if (table && table.type && table.type == "table"){
            var key = programState.sp.env[node.key];
            var isKeyInTable = table.has_key(key);
            programState.sp.env[node.target] = isKeyInTable ? 1 : 0;
            programState.sp.pc++;
          }
          else {
            throw new ExecError('Trying to find key in non-table.');
          }
          break;
        case "len":
          var table = programState.sp.env[node.dict];
          if (table && table.type && table.type === "table") {
            var length = table.get_length();
            programState.sp.env[node.target] = length;
            programState.sp.pc++;
          } else {
            throw new ExecError("Trying to call len on non-table.");
          }
          break;
        case "ite":
          var cond = programState.sp.env[node.cond];
          var ct = programState.sp.env[node.true];
          var cf = programState.sp.env[node.false];
          if (cond == null || cond == 0) {
            cond = false;
          }
          if ((typeof cond != 'boolean') && (typeof cond != 'number')) {
            throw new ExecError('Condition not a boolean');
          }
          programState.sp.env[node.target] = cond ? ct : cf;
          programState.sp.pc++;
          break;
        case "lambda":
          var closure = makeClosure(node.arguments, node.body, programState.sp.env);
          programState.sp.env[node.target] = closure;
          programState.sp.pc++;
          break;
        case "call":
          var fn = programState.sp.env[node.function];
          if (fn !== null && fn.type && (fn.type === 'closure' || fn.type === 'type_construct')) {
            var newEnv = envExtend(fn.env);
            var argvals = [];
            for (var i in node.arguments) {
              var arg = programState.sp.env[node.arguments[i]];
              argvals.push(arg);
            }
            if (argvals.length !== fn.names.length) {
              throw new ExecError("Wrong number of arguments");
            }
            if (fn.type === 'closure') {
              for (var i in fn.names) {
                envBind(newEnv, fn.names[i].name, argvals[i]);
              }
              var newStackFrame = makeStackFrame(fn.body, newEnv, node.target, programState.sp);
              programState.sp.retReg = node.target;
              programState.sp.pc++;
              programState.sp = newStackFrame;
            } else {
              // type construct
              programState.sp.env[node.target] = argvals[0] && argvals[0].type === 'table' ? 'table' : 'other';
              programState.sp.pc++;
            }
          } else {
            throw new ExecError('Trying to call non-lambda');
          }
          break;
        case "tcall":
          var fn = programState.sp.env[node.function];
          if (fn !== null && fn.type && (fn.type === 'closure' || fn.type === 'type_construct')) {
            var newEnv = envExtend(fn.env);
            var argvals = [];
            for (var i in node.arguments) {
              var arg = programState.sp.env[node.arguments[i]];
              argvals.push(arg);
            }
            if (argvals.length !== fn.names.length) {
              throw new ExecError("Wrong number of arguments");
            }
            if (fn.type === 'closure') {
              for (var i in fn.names) {
                envBind(newEnv, fn.names[i].name, argvals[i]);
              }
              var newStackFrame = makeStackFrame(fn.body, newEnv, node.target, programState.sp.parent);
              programState.sp.retReg = node.target;
              programState.sp.pc++;
              programState.sp = newStackFrame;
            } else { // type construct
              programState.sp.env[node.target] = fn.body(argvals[0]);
              programState.sp.pc++;
            }
          } else {
            throw new ExecError('Trying to call non-lambda');
          }
          break;
        case "return":
          var retVal = programState.sp.env[node.value];
          if (programState.sp.parent !== null) {
            programState.sp = programState.sp.parent;
            programState.sp.env[programState.sp.retReg] = retVal;
          } else if (programState.parent !== null) { // must yield return value to resumer
            programState.parent.yieldArg = retVal;
            programState.parent.state = RUN
            programState.state = DEAD;
            resumeProgram(programState.parent);
            return; // IMPORTANT!
          } else { // end of program
            return;
          }
          break;
        case "coroutine":
          var fn = programState.sp.env[node.body];
          var coroutine;
          if (fn && fn.type && fn.type === 'closure') {
            if (fn.names.length !== 1) {
              throw new ExecError('Coroutine lambdas must accept exactly one argument.');
            }
            var newEnv = envExtend(fn.env);
            coroutine = makeProgramState(fn.body, newEnv, fn.names[0].name);
          } else {
            throw new ExecError('Tried to create coroutine with non-lambda.');
          }
          programState.sp.env[node.target] = coroutine;
          programState.sp.pc++;
          break;
        case "yield":
          var arg = programState.sp.env[node.arg];
          if (programState.parent !== null) {
            if (programState.yieldComplete) {
              programState.yieldComplete = false;
              programState.sp.env[node.target] = programState.resumeArg;
              programState.sp.pc++;
              break;
            } else {
              programState.parent.yieldArg = arg;
              programState.parent.state = RUN;
              programState.state = SUSP_Y;
              programState.yieldComplete = true;
              resumeProgram(programState.parent);
              return; // IMPORTANT!
            }
          } else {
            throw new ExecError('Tried to yield from non-coroutine.');
          }
        case "resume":
          var co = programState.sp.env[node.coroutine];
          var arg = programState.sp.env[node.arg];
          if (co && co.type && co.type === 'coroutine') {
            if (programState.resumeComplete) {
              programState.resumeComplete = false;
              programState.sp.env[node.target] = programState.yieldArg;
              programState.sp.pc++;
              break;
            } else {
              if (co.state == BORN) { // first call to resume, bind arg to coroutine's argName
                envBind(co.sp.env, co.argName, arg);
              } else if (co.state != SUSP_Y) { // not suspended on yield statement
                throw new ExecError('Coroutine not resumable.');
              }
              co.parent = programState;
              co.resumeArg = arg;
              co.state = RUN;
              programState.state = SUSP_R;
              programState.resumeComplete = true;
              resumeProgram(co);
              return; // IMPORTANT!
            }
          } else {
            throw new ExecError('Tried to call resume on non-coroutine.');
          }
        default:
          throw new Error(
            "What's " + node.type + "? " + JSON.stringify(node)
          );
      }
    }
  }

  function execBytecode(bytecode, env) {
    // TODO step 3: based on how you decide to implement
    // makeProgramState, make sure the makeProgramState call below
    // suits your purposes.
    var mainProgram = makeProgramState(bytecode, env)
    mainProgram.state = RUN;
    return resumeProgram(mainProgram);
  }

  function tailCallOptimization(bytecode) {
    // TODO step 5: implement this function, (which is called below).
    // It should take bytecode as input and transform call instructions
    // into tcall instructions if they can be optimized with the
    // tail call optimization.
    for (var i = 0; i < bytecode.length - 1; i++) {
      if (bytecode[i].type === "call") {
        if (bytecode[i+1].type === "return" && bytecode[i+1].value === bytecode[i].target) {
          bytecode[i].type = "tcall";
        }
      } else if (bytecode[i].type === "lambda") {
        tailCallOptimization(bytecode[i].body);
      }
    }
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
    // convert a JS object to a suitable 164 object
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

  var root = envRoot();
  root['*title'] = 'Root';
  /*
  envBind(root,"type", function(a){
    if (a && a.type==="table"){
      return "table";
    } else {
      return "other";
    }
  });
  */

  desugarAll(asts, [], function(desugaredAsts) {
    for (var i = 0, ii = desugaredAsts.length; i < ii; ++i) {
      try {
        var bytecode = compileToBytecode(desugaredAsts[i]);
        tailCallOptimization(bytecode);
        execBytecode(bytecode, root);
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
