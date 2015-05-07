'use strict';

var _nextNum = 0
function genUID(){
    /*Return an identifier that has never before been returned by
    genUID()*/
    _nextNum += 1;
    return 'id'+_nextNum;
}

var NONE = "",
    STAR = "aqua",
    OR = "yellow",
    PAREN = "lightgreen";

function makeSpan(uid, content) {
  return '<span class="' + uid + '">' + content + '</span>';
}

function makeScript(op_uid, content_uid, op) {
  var op_selector = '$(".' + op_uid + '")';
  var content_selector = '$(".' + content_uid + '")';
  var js = op_selector
         + '.hover(function() { '
         + content_selector
         + '.css("background", "'
         + getColor(op)
         + '"); }, function() { '
         + content_selector +
         '.css("background", "'
         + NONE
         + '"); });';
  return '<script>' + js + '</script>';
}

function getColor(op) {
  switch (op) {
    case "star" : return STAR;
    case "or"   : return OR;
    case "paren": return PAREN;
    default     : return NONE;
  }
}

if (typeof(module) !== 'undefined') {
  module.exports = {
    'genUID'    : genUID,
    'makeSpan'  : makeSpan,
    'makeScript': makeScript
  };
}
