if (typeof(module) !== 'undefined') {
  module.exports = {
    'processBytecode': processBytecode,
    'processFile': processFile
  };
}

/**
 * The registers.
 */
var reg = {};

/**
 * Define operators.
 */
var op = {
  'or':
    function(o1, o2) {
      return '(' + o1.toString() + ' || ' + o2.toString() + ')';
    },
  'and':
    function(o1, o2) {
      return '(' + o1.toString() + ' && ' + o2.toString() + ')';
    },
  'not':
    function(o1) {
      return '(!(' + o1.toString() + '))';
    }
};

/**
 * Builds the function for an example.
 * @param input is the input JavaScript.
 * @param output is the output JavaScript.
 * @return string representation of the function testing the example.
 */
function makeExample(input, output) {
  if (output === "Error") {
    return "function() {\n      var verdict = false;\n      try{\n        " +
      input + ";\n      } catch (e) {\n        verdict = true;\n      }\n" +
      "      return verdict;\n    }";
  } else {
    return "function() {\n      var verdict = " +
      "_______contract.equality(" + input + ", " + output + ");\n" +
      "      return verdict;\n    }";
  }
}

/**
 * Handles the bytecode for a function.
 * @param btc is the bytecode.
 * @param lines is an array of lines of the original file.
 */
function handleFunc(btc, lines) {
  var preLines = [],
      postLines = [],
      testLines = [];
  var i;
  for (i = 0; i < btc.docs.length; i++) {
    var node = btc.docs[i];
    switch (node.type) {
      case 'adjective':
        reg[node.target] = '_______contract.' + node.name + '(_______arg)';
        break;
      case 'and':
        reg[node.target] = op.and(reg[node.operand1], reg[node.operand2]);
        break;
      case 'or':
        reg[node.target] = op.or(reg[node.operand1], reg[node.operand2]);
        break;
      case 'not':
        reg[node.target] = op.not(reg[node.operand1]);
        break;
      case 'null':
        reg[node.target] = '(_______arg === null)';
        break;
      case 'int-lit':
        reg[node.target] = '(_______arg === ' + node.value + ')';
        break;
      case 'float-lit':
        reg[node.target] = '(_______arg === ' + node.value + ')';
        break;
      case 'string-lit':
        reg[node.target] = '(_______arg === "' +
                           node.value.replace('"', '\\"') + '")';
        break;
      case 'pass-lit':
        reg[node.target] = {after: false, value: 'function() { return true; }'};
        break;
      case 'fail-lit':
        reg[node.target] = {after: false, value: 'function() { return false; }'};
        break;
      case 'ite':
        var after = false;
        if (reg[node.cond].after || reg[node.true].after || reg[node.false].after) {
          after = true;
        }
        reg[node.target] = {after: after,
                            value: 'function() {\n  if (' + reg[node.cond].value +
                                   '()) {\n' + '    return ' + reg[node.true].value +
                                   '();\n  } else {\n' + '    return ' +
                                   reg[node.false].value + '();\n  }\n}'};
        break;
      case 'clause':
        var nouns = [];
        var j;
        for (j = 0; j < node.subjects.length; j++) {
          var q = node.subjects[j].qualifier;
          if (q === null) {
            q = 'single'
          }
          nouns.push({'qualifier': q, 'name': node.subjects[j].name});
        }
        var subjectArray = ['function() {'];
        var after = false;
        for (j = 0; j < nouns.length; j++) {
          if (nouns[j].name !== '@output') {
            subjectArray.push('    if (!(_______contract.' + nouns[j].qualifier +
                              '(' + nouns[j].name +
                              ').each(function(_______arg) {');
            subjectArray.push('      return ' + reg[node.descriptor] + ';');
            subjectArray.push('    }))) {');
            subjectArray.push('      return false;');
            subjectArray.push('    }');
          } else {
            after = true;
            subjectArray.push('    if (!(_______contract.' + nouns[j].qualifier +
                              '(_______out).each(function(_______arg) {');
            subjectArray.push('      return ' + reg[node.descriptor] + ';');
            subjectArray.push('    }))) {');
            subjectArray.push('      return false;');
            subjectArray.push('    }');
          }
        }
        subjectArray.push('    return true;');
        subjectArray.push('  }');
        reg[node.target] = {after: after, value: subjectArray.join("\n")};
        break;
      case 'compound':
        var after = false;
        if (reg[node.operand1].after || reg[node.operand2].after) {
          after = true;
        }
        reg[node.target] = {after: after,
                            value: 'function() {\n  if (!(' + reg[node.operand1].value +
                                   '())) {\n    return false;\n  } else {\n    return ' +
                                   reg[node.operand2].value + '()\n  }\n}'};
        break;
      case 'setup':
        testLines.push('if (_______first_' + btc.name + ') {\n    ' + node.code +
                      '\n  }');
        break;
      case 'example':
        var msg = 'Failure in function ' + btc.name + ' for input ' +
                  node.input + ' and output ' + node.output;
        msg = '"' + msg.replace(new RegExp('\\"', 'g'), '\\"') + '"';
        var newInput = node.input.replace(
                         new RegExp(' *' + btc.name + ' *\\(', 'g'),
                         ' _______' + btc.name + '(');
        var newOutput = node.output.replace(
                         new RegExp(' *' + btc.name + ' *\\('),
                         ' _______' + btc.name + '(', 'g');
        testLines.push('if (_______first_' + btc.name + ') {\n' +
                      '    _______enforce(' + makeExample(newInput, newOutput) +
                      ', ' + msg + ');\n  }');
        break;
      case 'contract':
        reg[node.target] = {'type': 'contract',
                            'after': reg[node.clause].after,
                            'clause': reg[node.clause].value};
        break;
      case 'doc':
        if (reg[node.directive] !== undefined &&
            reg[node.directive].type === 'contract') {
          var msg = 'Violation in function ' + btc.name + ' for ' +
                    lines[node.line-1].replace(new RegExp(' *\\* *#', 'g'),
                                               '#');
          msg = '"' + msg.replace(new RegExp('\\"', 'g'), '\\"') + '"';
          if (reg[node.directive].after) {
            postLines.push('_______enforce(' + reg[node.directive].clause +
                           ', ' + msg + ');');
          } else {
            preLines.push('_______enforce(' + reg[node.directive].clause +
                           ', ' + msg + ');');
          }
        }
        break;
    }
  }
  var params = [];
  for (i = 0; i < btc.params.length; i++) {
    params.push(btc.params[i].name);
  }
  var paramString = params.join(', ');
  var outArray = ['function ' + btc.name + '(' + paramString + ') {'];
  for (i = 0; i < testLines.length; i++) {
    outArray.push('  ' + testLines[i]);
  }
  for (i = 0; i < preLines.length; i++) {
    outArray.push('  ' + preLines[i]);
  }
  outArray.push('  _______first_' + btc.name + ' = false;')
  outArray.push('  var _______out = _______' + btc.name + '(' + paramString + ');')
  for (i = 0; i < postLines.length; i++) {
    outArray.push('  ' + postLines[i]);
  }
  outArray.push('  return _______out;');
  outArray.push('}');
  if (outArray.length > 0) {
    outArray.push('');
  }
  return outArray.join("\n");
}

/**
 * Processes bytecode in full.
 * @param btc is the bytecode.
 * @param text is the original file text.
 * @return object containing the result and a list of function names.
 */
function processBytecode(btc, text) {
  var results = [];
  var names = [];
  var i;
  for (i = 0; i < btc.length; i++) {
    if (btc[i].type == 'function') {
      results.push(handleFunc(btc[i], text.split("\n")));
      names.push(btc[i].name);
    }
  }
  return {'result': results.join("\n"),
          'names': names};
}

/**
 * Processes a file in full.
 * @param btc is the bytecode for the file.
 * @param text is the text of the file.
 * @param depth is the depth of the file in the directory structure.
 * @return string of the result file.
 */
function processFile(btc, text, depth) {
  var outJS = processBytecode(btc, text);
  var outString = "var _______contract = require('" +
                  new Array(depth + 1).join('../') +
                  "./.contract.js');\n" +
                  "var _______enforce = _______contract.enforce;\n\n";
  var cleanedText = text;
  var i;
  var declarations = [];
  for (i = 0; i < outJS.names.length; i++) {
    cleanedText = cleanedText.replace(new RegExp('function ' +
                                      outJS.names[i] + ' *\\('),
                                      'function _______' + outJS.names[i] +
                                      '(', 'g');
    declarations.push('var _______first_' + outJS.names[i] + ' = true;');
  }
  outString += declarations.join("\n") + "\n\n" + cleanedText +
               "\n\n" + outJS.result;
  return outString;
}
