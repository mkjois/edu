"""This module implements the core Scheme interpreter functions, including the
eval/apply mutual recurrence, environment model, and read-eval-print loop.
"""

from buffer import Buffer
from scheme_primitives import *
from scheme_reader import *
from scheme_tokens import tokenize_lines, DELIMITERS
from ucb import main, trace

from doctest import run_docstring_examples
test = lambda fn: run_docstring_examples(fn, globals(), True)

##############
# Eval/Apply #
##############

def scheme_eval(expr, env):
    """Evaluate Scheme expression EXPR in environment ENV.

    >>> expr = read_line("(+ 2 2)")
    >>> expr
    Pair('+', Pair(2, Pair(2, nil)))
    >>> scheme_eval(expr, create_global_frame())
    4
    """
    if expr is None:
        raise SchemeError("Cannot evaluate an undefined expression.")

    # Evaluate Atoms
    if scheme_symbolp(expr):
        return env.lookup(expr)
    elif scheme_atomp(expr):
        return expr

    # All non-atomic expressions are lists.
    if not scheme_listp(expr):
        raise SchemeError("malformed list: {0}".format(str(expr)))
    first, rest = expr.first, expr.second

    # Evaluate Combinations
    if first in LOGIC_FORMS:
        return scheme_eval(LOGIC_FORMS[first](rest, env), env)
    elif first == "lambda":
        return do_lambda_form(rest, env)
    elif first == "mu":
        return do_mu_form(rest)
    elif first == "define":
        return do_define_form(rest, env)
    elif first == "quote":
        return do_quote_form(rest)
    elif first == "let":
        expr, env = do_let_form(rest, env)
        return scheme_eval(expr, env)
    else:
        procedure = scheme_eval(first, env)
        args = rest.map(lambda operand: scheme_eval(operand, env))
        return scheme_apply(procedure, args, env)

def scheme_apply(procedure, args, env):
    """Apply Scheme PROCEDURE to argument values ARGS in environment ENV.

    >>> env = create_global_frame()
    >>> scheme_eval(read_line("(define (f1 a b) (+ a b))"), env)
    >>> scheme_eval(read_line("(f1 1 2)"), env)
    3
    >>> scheme_eval(read_line("(define f2 (lambda () 5))"), env)
    >>> scheme_eval(read_line("(f2)"), env)
    5
    >>> scheme_eval(read_line("(f2 2)"), env) # doctest: +IGNORE_EXCEPTION_DETAIL
    Traceback (most recent call last):
    ...
    SchemeError:
    """
    if isinstance(procedure, PrimitiveProcedure):
        return apply_primitive(procedure, args, env)
    elif isinstance(procedure, LambdaProcedure):
        if len(procedure.formals) != len(args):
            raise (SchemeError("must have {0} arguments, {1} given".format(
                len(procedure.formals), len(args))))
        local_frame = procedure.env.make_call_frame(procedure.formals, args)
        return scheme_eval(procedure.body, local_frame)
    elif isinstance(procedure, MuProcedure):
        if len(procedure.formals) != len(args):
            raise (SchemeError("must have {0} arguments, {1} given".format(
                len(procedure.formals), len(args))))
        for i in range(len(procedure.formals)):
            env.define(procedure.formals[i], args[i])
        return scheme_eval(procedure.body, env)
    else:
        raise SchemeError("Cannot call {0}".format(str(procedure)))

def apply_primitive(procedure, args, env):
    """Apply PrimitiveProcedure PROCEDURE to a Scheme list of ARGS in ENV.

    >>> env = create_global_frame()
    >>> plus = env.bindings["+"]
    >>> twos = Pair(2, Pair(2, nil))
    >>> apply_primitive(plus, twos, env)
    4
    """
    py_args = []
    if args is not nil:
        while args is not nil:
            py_args.append(args.first)
            args = args.second
    if procedure.use_env:
        py_args.append(env)
    try:
        return procedure.fn(*py_args)
    except TypeError:
        raise SchemeError("Incorrent number of arguments")

################
# Environments #
################

class Frame(object):
    """An environment frame binds Scheme symbols to Scheme values."""

    def __init__(self, parent):
        """An empty frame with a PARENT frame (that may be None)."""
        self.bindings = {}
        self.parent = parent

    def __repr__(self):
        if self.parent is None:
            return "<Global Frame>"
        else:
            s = sorted('{0}: {1}'.format(k,v) for k,v in self.bindings.items())
            return "<{{{0}}} -> {1}>".format(', '.join(s), repr(self.parent))

    def lookup(self, symbol):
        """Return the value bound to SYMBOL.  Errors if SYMBOL is not found.

        >>> global_frame = create_global_frame()
        >>> frame1 = Frame(global_frame)
        >>> global_frame.define('a', 1)
        >>> global_frame.define('b', 4)
        >>> frame1.define('a', 2)
        >>> frame1.define('c', 3)
        >>> frame1.lookup('a')
        2
        >>> frame1.lookup('b')
        4
        >>> frame1.lookup('c')
        3
        >>> global_frame.lookup('a')
        1
        >>> global_frame.lookup('c') # doctest: +IGNORE_EXCEPTION_DETAIL
        Traceback (most recent call last):
            ...
        SchemeError:
        """
        if symbol in self.bindings:
            return self.bindings[symbol]
        try:
            return self.parent.lookup(symbol)
        except:
            raise SchemeError("unknown identifier: {0}".format(str(symbol)))

    def global_frame(self):
        """The global environment at the root of the parent chain."""
        e = self
        while e.parent is not None:
            e = e.parent
        return e

    def make_call_frame(self, formals, vals):
        """Return a new local frame whose parent is SELF, in which the symbols
        in the Scheme formal parameter list FORMALS are bound to the Scheme
        values in the Scheme value list VALS.

        >>> env = create_global_frame()
        >>> formals, vals = read_line("(a b c)"), read_line("(1 2 3)")
        >>> env.make_call_frame(formals, vals)
        <{a: 1, b: 2, c: 3} -> <Global Frame>>
        >>> formals, vals = read_line("(a b c)"), read_line("(1 2)")
        >>> env.make_call_frame(formals, vals) # doctest: +IGNORE_EXCEPTION_DETAIL
        Traceback (most recent call last):
        ...
        SchemeError:
        """
        frame = Frame(self)
        lenf, lenv = len(formals), len(vals)
        if lenf != lenv:
            raise (SchemeError("Call must have {0} arguments, {1} given".format(
                lenf, lenv)))
        for i in range(lenf):
            frame.define(formals[i], vals[i])
        return frame

    def define(self, sym, val):
        """Define Scheme symbol SYM to have value VAL in SELF."""
        self.bindings[sym] = val

class LambdaProcedure(object):
    """A procedure defined by a lambda expression or the complex define form."""

    def __init__(self, formals, body, env):
        """A procedure whose formal parameter list is FORMALS (a Scheme list),
        whose body is the single Scheme expression BODY, and whose parent
        environment is the Frame ENV.  A lambda expression containing multiple
        expressions, such as (lambda (x) (display x) (+ x 1)) can be handled by
        using (begin (display x) (+ x 1)) as the body."""
        self.formals = formals
        self.body = body
        self.env = env

    def __str__(self):
        return "(lambda {0} {1})".format(str(self.formals), str(self.body))

    def __repr__(self):
        args = (self.formals, self.body, self.env)
        return "LambdaProcedure({0}, {1}, {2})".format(*(repr(a) for a in args))

class MuProcedure(object):
    """A procedure defined by a mu expression, which has dynamic scope.
     _________________
    < Scheme is cool! >
     -----------------
            \   ^__^
             \  (oo)\_______
                (__)\       )\/\
                    ||----w |
                    ||     ||
    """

    def __init__(self, formals, body):
        """A procedure whose formal parameter list is FORMALS (a Scheme list),
        whose body is the single Scheme expression BODY.  A mu expression
        containing multiple expressions, such as (mu (x) (display x) (+ x 1))
        can be handled by using (begin (display x) (+ x 1)) as the body."""
        self.formals = formals
        self.body = body

    def __str__(self):
        return "(mu {0} {1})".format(str(self.formals), str(self.body))

    def __repr__(self):
        args = (self.formals, self.body)
        return "MuProcedure({0}, {1})".format(*(repr(a) for a in args))


#################
# Special forms #
#################

def consolidate(exprs):
    """Consolidates multiple Scheme expressions into a single BEGIN
    expression."""
    fullexpr = Pair('begin', Pair(exprs.first, nil))
    rest = fullexpr.second
    while exprs.second is not nil:
        rest.second = exprs.second
        rest = rest.second
        exprs = exprs.second
    return fullexpr

def do_lambda_form(vals, env):
    """Evaluate a lambda form with parameters VALS in environment ENV."""
    check_form(vals, 2)
    formals = vals.first
    check_formals(formals)
    body = vals.second
    if body.second is not nil:
        body = consolidate(body)
    else:
        body = body.first
    return LambdaProcedure(formals, body, env)

def do_mu_form(vals):
    """Evaluate a mu form with parameters VALS."""
    check_form(vals, 2)
    formals = vals[0]
    check_formals(formals)
    body = vals.second
    if body.second is not nil:
        body = consolidate(body)
    else:
        body = body.first
    return MuProcedure(formals, body)

def do_define_form(vals, env):
    """Evaluate a define form with parameters VALS in environment ENV.

    >>> env = create_global_frame()
    >>> vals = read_line('( (pow x y) (* x y) )')
    >>> do_define_form(vals, env)
    >>> print(env.lookup('pow'))
    (lambda (x y) (* x y))
    >>> vals = read_line('( (0 x y) (* x y) )')
    >>> do_define_form(vals, env) # doctest: +IGNORE_EXCEPTION_DETAIL
    Traceback (most recent call last):
    ...
    SchemeError:
    """
    check_form(vals, 2)
    target = vals[0]
    if scheme_symbolp(target):
        check_form(vals, 2, 2)
        env.define(target, scheme_eval(vals[1], env))
    elif isinstance(target, Pair):
        name, formals, body = target.first, target.second, vals.second
        if not scheme_symbolp(name):
            raise SchemeError("Given name {0} not a proper symbol".format(name))
        if body.second is not nil:
            body = consolidate(body)
        else:
            body = body.first
        vals = Pair(formals, Pair(body, nil))
        fn = do_lambda_form(vals, env)
        env.define(name, fn)
    else:
        raise SchemeError("bad argument to define")

def do_quote_form(vals):
    """Evaluate a quote form with parameters VALS."""
    check_form(vals, 1, 1)
    return vals[0]

def do_let_form(vals, env):
    """Evaluate a let form with parameters VALS in environment ENV.

    >>> env = create_global_frame()
    >>> scheme_eval(read_line("(let ((2 2)) 2)"), env) # doctest: +IGNORE_EXCEPTION_DETAIL
    Traceback (most recent call last):
    ...
    SchemeError:
    >>> scheme_eval(read_line("(let ((x 2 3)) 2)"), env) # doctest: +IGNORE_EXCEPTION_DETAIL
    Traceback (most recent call last):
    ...
    SchemeError:
    """
    check_form(vals, 2)
    bindings = vals[0]
    exprs = vals.second
    if not scheme_listp(bindings):
        raise SchemeError("bad bindings list in let form")

    # Add a frame containing bindings
    names, vals = nil, nil
    while bindings is not nil:
        pair = bindings.first
        if len(pair) != 2:
            raise (SchemeError("{0} must only have a name and value".format(
                pair)))
        param, value = pair[0], scheme_eval(pair[1], env)
        if not scheme_symbolp(param):
            raise SchemeError("parameter {0} is not a symbol".format(param))
        if names is nil and vals is nil:
            names, vals = Pair(param, nil), Pair(value, nil)
        else:
            add_to_pair(param, names)
            add_to_pair(value, vals)
        bindings = bindings.second
    new_env = env.make_call_frame(names, vals)

    # Evaluate all but the last expression after bindings, and return the last
    last = len(exprs)-1
    for i in range(0, last):
        scheme_eval(exprs[i], new_env)
    return exprs[last], new_env

def add_to_pair(val, p):
    if p.second is nil:
        p.second = Pair(val, nil)
    else:
        add_to_pair(val, p.second)

#########################
# Logical Special Forms #
#########################

def do_if_form(vals, env):
    """Evaluate if form with parameters VALS in environment ENV."""
    check_form(vals, 3, 3)
    predicate, consequent, alternative = vals[0], vals[1], vals[2]
    if scheme_true(scheme_eval(predicate, env)):
        return consequent
    else:
        return alternative

def do_and_form(vals, env):
    """Evaluate short-circuited and with parameters VALS in environment ENV."""
    if vals is nil:
        return True
    while vals.second is not nil:
        check = scheme_eval(vals.first, env)
        if scheme_false(check):
            return False
        vals = vals.second
    return vals.first

def do_or_form(vals, env):
    """Evaluate short-circuited or with parameters VALS in environment ENV."""
    if vals is nil:
        return False
    while vals.second is not nil:
        check = scheme_eval(vals.first, env)
        if scheme_true(check):
            return vals.first
        vals = vals.second
    return vals.first

def do_cond_form(vals, env):
    """Evaluate cond form with parameters VALS in environment ENV.

    >>> env = create_global_frame()
    >>> scheme_eval(read_line("(cond ((= 1 2)))"), env) # doctest: +IGNORE_EXCEPTION_DETAIL
    Traceback (most recent call last):
        ...
    SchemeError:
    """
    num_clauses = len(vals)
    for i, clause in enumerate(vals):
        check_form(clause, 1)
        if clause.first == "else":
            if i < num_clauses-1:
                raise SchemeError("else must be last")
            test = True
            if clause.second is nil:
                raise SchemeError("badly formed else clause")
        else:
            test = scheme_eval(clause.first, env)
        if scheme_true(test):
            expr = clause.second
            if expr is nil:
                return test
            if expr.second is not nil:
                expr = consolidate(expr)
                return expr
            return expr.first
    return None

def do_begin_form(vals, env):
    """Evaluate begin form with parameters VALS in environment ENV."""
    check_form(vals, 1)
    while vals.second is not nil:
        scheme_eval(vals.first, env)
        vals = vals.second
    return vals.first

LOGIC_FORMS = {
        "and": do_and_form,
        "or": do_or_form,
        "if": do_if_form,
        "cond": do_cond_form,
        "begin": do_begin_form,
        }

# Utility methods for checking the structure of Scheme programs

def check_form(expr, min, max = None):
    """Check EXPR (default SELF.expr) is a proper list whose length is
    at least MIN and no more than MAX (default: no maximum). Raises
    a SchemeError if this is not the case."""
    if not scheme_listp(expr):
        raise SchemeError("badly formed expression: %s", expr)
    length = len(expr)
    if length < min:
        raise SchemeError("too few operands in form")
    elif max is not None and length > max:
        raise SchemeError("too many operands in form")

def check_formals(formals):
    """Check that FORMALS is a valid parameter list, a Scheme list of symbols
    in which each symbol is distinct.

    >>> check_formals(read_line("(a b c)"))
    >>> check_formals(read_line("(a . b)")) # doctest: +IGNORE_EXCEPTION_DETAIL
    Traceback (most recent call last):
    ...
    SchemeError:
    >>> check_formals(read_line("(a b c a)")) # doctest: +IGNORE_EXCEPTION_DETAIL
    Traceback (most recent call last):
    ...
    SchemeError:
    >>> check_formals(read_line("(a b 0 c)")) # doctest: +IGNORE_EXCEPTION_DETAIL
    Traceback (most recent call last):
    ...
    SchemeError:
    >>> check_formals(read_line("()"))
    """
    if not scheme_listp(formals):
        raise SchemeError("formals is ill-formed Scheme list")
    symbols = []
    for param in formals:
        if not scheme_symbolp(param):
            raise SchemeError("formals has non-symbol {0}".format(param))
        if param in symbols:
            raise SchemeError("formals has repeated symbol {0}".format(param))
        symbols.append(param)

##################
# Tail Recursion #
##################

def scheme_optimized_eval(expr, env):
    """Evaluate Scheme expression EXPR in environment ENV."""
    while True:
        if expr is None:
            raise SchemeError("Cannot evaluate an undefined expression.")

        # Evaluate Atoms
        if scheme_symbolp(expr):
            return env.lookup(expr)
        elif scheme_atomp(expr):
            return expr

        # All non-atomic expressions are lists.
        if not scheme_listp(expr):
            raise SchemeError("malformed list: {0}".format(str(expr)))
        first, rest = expr.first, expr.second

        # Evaluate Combinations
        if first in LOGIC_FORMS:
            "*** YOUR CODE HERE ***"
        elif first == "lambda":
            return do_lambda_form(rest, env)
        elif first == "mu":
            return do_mu_form(rest)
        elif first == "define":
            return do_define_form(rest, env)
        elif first == "quote":
            return do_quote_form(rest)
        elif first == "let":
            "*** YOUR CODE HERE ***"
        else:
            "*** YOUR CODE HERE ***"

################################################################
# Uncomment the following line to apply tail call optimization #
################################################################
# scheme_eval = scheme_optimized_eval


################
# Input/Output #
################

def read_eval_print_loop(next_line, env):
    """Read and evaluate input until an end of file or keyboard interrupt."""
    while True:
        try:
            src = next_line()
            while src.more_on_line:
                expression = scheme_read(src)
                result = scheme_eval(expression, env)
                if result is not None:
                    print(result)
        except (SchemeError, SyntaxError, ValueError) as err:
            print("Error:", err)
        except (KeyboardInterrupt, EOFError):  # <Control>-D, etc.
            return

def scheme_load(sym, env):
    """Load Scheme source file named SYM in environment ENV."""
    check_type(sym, scheme_symbolp, 0, "load")
    with scheme_open(sym) as infile:
        lines = infile.readlines()
    def next_line():
        return buffer_lines(lines)
    read_eval_print_loop(next_line, env.global_frame())

def scheme_open(filename):
    """If either FILENAME or FILENAME.scm is the name of a valid file,
    return a Python file opened to it. Otherwise, raise an error."""
    try:
        return open(filename)
    except IOError as exc:
        if filename.endswith('.scm'):
            raise SchemeError(str(exc))
    try:
        return open(filename + '.scm')
    except IOError as exc:
        raise SchemeError(str(exc))

def create_global_frame():
    """Initialize and return a single-frame environment with built-in names."""
    env = Frame(None)
    env.define("eval", PrimitiveProcedure(scheme_eval, True))
    env.define("apply", PrimitiveProcedure(scheme_apply, True))
    env.define("load", PrimitiveProcedure(scheme_load, True))
    add_primitives(env)
    return env

@main
def run(*argv):
    next_line = buffer_input
    if argv:
        try:
            filename = argv[0]
            input_file = open(argv[0])
            lines = input_file.readlines()
            def next_line():
                return buffer_lines(lines)
        except IOError as err:
            print(err)
            sys.exit(1)
    read_eval_print_loop(next_line, create_global_frame())
