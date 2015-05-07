/* description: Parses end executes mathematical expressions. */

/* lexical grammar */
%lex

%x directive
%x contract
%x example
%x setup
%x colon

%%

"#" {this.begin('directive');}
.   /* do nothing */

<directive>\s+                   /* skip whitespace */
<contract>\s+                    /* skip whitespace */
<colon>\s+                       /* skip whitespace */

<directive>"contract"            {this.begin('contract'); this.begin('colon'); return 'CONTRACT';}
<directive>"example"             {this.begin('example'); this.begin('colon'); return 'EXAMPLE';}
<directive>"setup"               {this.begin('setup'); this.begin('colon'); return 'SETUP';}

<contract>[\+\-]?[0-9]*("."[0-9]+)("e"[\+\-]?[0-9]+)?\b  return 'FLOAT'
<contract>[\+\-]?[0-9]+("e"[\+\-]?[0-9]+)\b  return 'FLOAT'
<contract>[\+\-]?[0-9]+\b        return 'INTEGER'
<contract>\"((\\.)|[^"])*\"      return 'STRING'
<contract>\'((\\.)|[^'])*\'      return 'STRING'
<contract>"if"                   return 'IF'
<contract>"then"                 return 'THEN'
<contract>"else"                 return 'ELSE'
<contract>"and"                  return 'AND'
<contract>"or"                   return 'OR'
<contract>"not"                  return 'NOT'
<contract>"is"                   return 'VERB'
<contract>"are"                  return 'VERB'
<contract>"@pass"                 return 'PASS'
<contract>"@fail"                 return 'FAIL'
<contract>"@output"               return 'OUTPUT'
<contract>"null"                 return 'NULL'
<contract>[a-zA-Z_][a-zA-Z0-9_]* return 'ID'
<contract>","                    return ","
<contract>";"                    return ";"
<contract>"("                    return '('
<contract>")"                    return ')'
<contract><<EOF>>                return 'EOF'
<contract>.                      return 'INVALID'

<example>"=>"                    return 'ARROW'
<example>((?!\=\>).)+            return 'JS'

<setup>.+                        return 'JS'
<colon>":"                       {this.popState(); return ":";}

/lex

/* operator associations and precedence */

%left ';'
%left OR
%left AND
%left NOT

%start directives

%% /* language grammar */

directives
    : CONTRACT ":" clause EOF
        {return {type: "contract", clause: $3};}
    | CONTRACT ":" ite EOF
        {return {type: "contract", clause: $3};}
    | EXAMPLE ":" JS ARROW JS
        {return {type: "example", input: $3.trim(), output: $5.trim()};}
    | SETUP ":" JS
        {return {type: "setup", code: $3.trim()};}
    ;

ite
    : IF clause THEN clause ELSE clause
        {$$ = {type: "ite", cond: $2, true: $4, false: $6};}
    ;

clause
    : clause ";" clause
        {$$ = {type: "compound", operand1: $1, operand2: $3};}
    | subjects VERB descriptor
        {$$ = {type: "clause", subjects: $1, verb: {type: "verb", name: $2}, descriptor: $3};}
    | PASS
        {$$ = {type: "pass-lit"};}
    | FAIL
        {$$ = {type: "fail-lit"};}
    | '(' clause ')'
        {$$ = $2;}
    ;

subjects
    : subjects "," subject
        {$$ = $1.concat([$3]);}
    | subject
        {$$ = [$1];}
    ;

subject
    : ID ID
        {$$ = {type: "subject", qualifier: $1, name: $2};}
    | ID
        {$$ = {type: "subject", qualifier: null, name: $1};}
    | ID OUTPUT
        {$$ = {type: "subject", qualifier: $1, name: "@output"};}
    | OUTPUT
        {$$ = {type: "subject", qualifier: null, name: "@output"};}
    ;

descriptor
    : descriptor AND descriptor
        {$$ = {type: "and", operand1: $1, operand2: $3};}
    | descriptor OR descriptor
        {$$ = {type: "or", operand1: $1, operand2: $3};}
    | NOT descriptor
        {$$ = {type: "not", operand1: $2};}
    | ID
        {$$ = {type: "adjective", name: $1};}
    | ID '(' args ')'
        {$$ = {type: "adjective", name: $1 + $2 + $3 + $4};}
    | INTEGER
        {$$ = {type: "int-lit", value: parseInt($1)};}
    | FLOAT
        {$$ = {type: "float-lit", value: parseFloat($1)};}
    | STRING
        {$$ = {type: "string-lit", value: $1.slice(1,-1)};}
    | NULL
        {$$ = {type: "null", value: null};}
    | '(' descriptor ')'
        {$$ = $2;}
    ;

args
    : args "," arg
        {$$ = $1 + $2 + $3;}
    | arg
        {$$ = $1;}
    ;

arg
    : ID
        {$$ = $1;}
    | INTEGER
        {$$ = parseInt($1);}
    | FLOAT
        {$$ = parseFloat($1);}
    | STRING
        {$$ = $1;}
    | NULL
        {$$ = null;}
    ;
