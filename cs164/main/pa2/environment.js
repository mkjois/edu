if (typeof(module) !== 'undefined') {
  var ExecError = require('./errors.js').ExecError;
}

/* Creates a root environment. */
function envRoot() {
  return {
    // The root doesn't have a parent. The `*parent` symbol is illegal in our
    // language, and thus safe to bind.
    '*parent': null
  };
}

/* Extends the environment. */
function envExtend(parent) {
    return {
        "*parent": parent
    };
}

/* Binds a new value to the top frame. */
function envBind(frame, name, value) {
  // TODO: Define "name", which must be bound to "value"
  if (frame.hasOwnProperty(name)) {
    throw new ExecError(name + " is already declared");
  } else {
    frame[name] = value;
  }
}

/* Updates the value binding of a variable. */
function envUpdate(frame, name, value) {
  // TODO: Update the environment; variable "name" must be bound to "value"
  if (frame.hasOwnProperty(name)) {
    frame[name] = value;
  } else {
    if (frame["*parent"] === null) {
      throw new ExecError(name + " is not declared");
    } else {
      envUpdate(frame["*parent"], name, value);
    }
  }
}

/* Looks up the value of a variable. */
function envLookup(frame, name) {
  // TODO: Lookup the value of "name" in "env" in the current and previous frames
  if (frame.hasOwnProperty(name)) {
    return frame[name];
  } else {
    if (frame["*parent"] === null) {
      throw new ExecError(name + " is not declared");
    } else {
      return envLookup(frame["*parent"], name);
    }
  } 
}

if (typeof(module) !== 'undefined') {
  module.exports = {
    root: envRoot,
    extend: envExtend,
    bind: envBind,
    update: envUpdate,
    lookup: envLookup
  };
}
