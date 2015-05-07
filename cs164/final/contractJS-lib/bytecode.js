if (typeof(module) !== 'undefined') {
  module.exports = {
    'compile': compile
  };
}

// This helper function generates a unique register name
function uniquegen() {
  return '#btc-reg-' + uniquegen.counter++;
}
uniquegen.counter = 1;

function compile(ast) {
  var btc = [];
  var target;
  ast.forEach(function(func, index) {
    target = uniquegen();
    btcnode(func, target, btc);
  });
  return btc;
}

function btcnode(node, target, btc) {

  var instruction = {
    "type"  : node.type,
    "target": target
  };
  
  if ("line" in node) {
    instruction.line = node.line;
  }
  if ("column" in node) {
    instruction.column = node.column;
  }

  switch (node.type) {
    case "function":
      var params = [];
      node.params.forEach(function(param, index) {
        params.push(param);
      });
      var docsBtc = [];
      node.docs.forEach(function(doc, index) {
        var target = uniquegen();
        var docBtc = btcnode(doc, target, docsBtc);
      });
      instruction.name = node.name;
      instruction.params = params;
      instruction.docs = docsBtc;
      break;
    case "doc":
      var regDirective = uniquegen();
      btcnode(node.directive, regDirective, btc);
      instruction.directive = regDirective;
      break;
    case "contract":
      var regClause = uniquegen();
      btcnode(node.clause, regClause, btc);
      instruction.clause = regClause;
      break;
    case "example":
      instruction.input = node.input;
      instruction.output = node.output;
      break;
    case "setup":
      instruction.code = node.code;
      break;
    case "clause":
      var subjects = [];
      node.subjects.forEach(function(subject, index) {
        subjects.push(subject);
      });
      var regDescriptor = uniquegen();
      btcnode(node.descriptor, regDescriptor, btc);
      instruction.subjects = subjects;
      instruction.verb = node.verb;
      instruction.descriptor = regDescriptor;
      break;
    case "compound":
      var regClause1 = uniquegen();
      var regClause2 = uniquegen();
      btcnode(node.operand1, regClause1, btc);
      btcnode(node.operand2, regClause2, btc);
      instruction.operand1 = regClause1;
      instruction.operand2 = regClause2;
      break;
    case "ite":
      var regCond = uniquegen();
      var regTrue = uniquegen();
      var regFalse = uniquegen();
      btcnode(node.cond, regCond, btc);
      btcnode(node.true, regTrue, btc);
      btcnode(node.false, regFalse, btc);
      instruction.cond = regCond;
      instruction.true = regTrue;
      instruction.false = regFalse;
      break;
    case "pass-lit":
    case "fail-lit":
      break;
    case "and":
    case "or":
      var regOp1 = uniquegen();
      var regOp2 = uniquegen();
      btcnode(node.operand1, regOp1, btc);
      btcnode(node.operand2, regOp2, btc);
      instruction.operand1 = regOp1;
      instruction.operand2 = regOp2;
      break;
    case "not":
      var regOp1 = uniquegen();
      btcnode(node.operand1, regOp1, btc);
      instruction.operand1 = regOp1;
      break;
    case "adjective":
      instruction.name = node.name;
      break;
    case "null":
    case "int-lit":
    case "float-lit":
    case "string-lit":
      instruction.value = node.value;
      break;
    default:
      console.error("Unknown node:");
      console.error(JSON.stringify(node, undefined, 4));
  }

  btc.push(instruction);
}
