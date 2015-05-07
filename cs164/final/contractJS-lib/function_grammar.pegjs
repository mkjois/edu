{
  var functions = {};
  var buffer = '';
  var buffer_array = [];
  var initialize = true;
  var newline = true;
  var global_line, global_col;
  var UUID = 0;
  var uniquename = function() {
    return "__" + line() + "__" + column() + "__";
  };
  var addtounit = function(id, properties) {
    if (!(id in functions)) {
      functions[id] = {}
    }
    for (var key in properties) {
      functions[id][key] = properties[key];
    }
  };
  var addmetadata = function(type, obj, c, l) {
    if (l === undefined) { l = line(); }
    if (c === undefined) { c = column(); }
    obj.type = type;
    obj.line = l;
    obj.column = c;
    return obj;
  }
}

start
  = unit* {return Object.keys(functions).map(function(k){return functions[k]});}

unit
  = func
  / string
  / comment_set
  / any_char

multi_line_comment
  = "/*" 
    ( !{return buffer.match(/\*\//)} c:. {
      if (initialize) {
        global_line = line();
        global_col = column();
      }
      if (c.match(/[\r\n]/)) {
        if (buffer != "") {
          buffer_array.push(addmetadata("doc", {text: buffer}, global_col, global_line));
        }
        buffer = "";
        global_line++;
        global_col = 1;
        newline = true;
      } else if (newline && c.match(/[ \t]/)) {
        global_col++;
      } else {
        buffer += c;
        newline = false;
      }
      initialize = false;
    })*               
    {
      buffer = buffer.replace(/\*\//, "");
      if (buffer != "") {
        buffer_array.push(addmetadata("doc", {text: buffer}, global_col, global_line));
      }
      buffer = "";
      initialize = true;
      newline = true;
      var temp = buffer_array.slice(); 
      buffer_array = [];
      return temp;
    }

single_line_comment
  = "//" w:[ \t]* s:[^\r\n]*
    { return [addmetadata("doc", {text: s.join("").trim()}, column() + w.length + 2)]; }

comment_set
  = s:single_line_comment spaces? c:comment_set {return s.concat(c);}
  / m:multi_line_comment spaces? c:comment_set {return m.concat(c);}
  / s:single_line_comment {return s;}
  / m:multi_line_comment {return m;}

func
  = "function" spaces id:identifier spaces? p:paramlist spaces? b:funcbody
    {
      addtounit(id, addmetadata("function", {name: id, params: p, docs: b}));
    }
  / "function" spaces? p:paramlist spaces? b:funcbody
    {
      var id = uniquename();
      addtounit(id, addmetadata("function", {name: null, params: p, docs: b}));
    }

funcbody
  = "{" spaces? c:comment_set spaces? [^}]* "}"
    { return c; }
  / "{" spaces? [^}]* spaces? "}"
    { return null; }

paramlist
  = "(" spaces? v:varlist spaces? ")"
    { return v; }

varlist
  = id:identifier spaces? "," spaces? v:varlist
    { return [addmetadata("id", {name: id})].concat(v); }
  / id:identifier
    { return [addmetadata("id", {name: id})]; }

identifier
  = a:([a-z] / [A-Z] / "_") b:([a-z] / [A-Z] / [0-9] /"_")*
    { return a + b.join(""); }

spaces
  = [ \t\r\n]+ {return "";}

string
  = "\"" ("\\" . / [^"])* "\""
  / "'" ("\\" . / [^'])* "'"

any_char
  = .
