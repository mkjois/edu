if (typeof(module) !== 'undefined') {
    var ExecError = require('./errors.js').ExecError;
}

var count = 0;

function isTypeTable(obj) {
    return obj !== null && typeof(obj) === "object" && obj.type === "table";
}

// TODO: Use Table to implement dictionaries, lists, and objects
// Complete all the requisite methods below
function Table() {
    this.type = "table";
    this._keys = [];
    this._data = {};
    this.num = count;
    count++;
}

Table.prototype.put = function(key, value) {
    if (!this._data.hasOwnProperty(hash(key))) {
        this._keys.push(key);
    }
    this._data[hash(key)] = value;
};

Table.prototype.has_key = function(key) {
    if (this._data.hasOwnProperty(hash(key))) {
        return true;
    } else if (this._data.hasOwnProperty(hash("__mt"))) {
        var metatable = this._data[hash("__mt")];
        if (! isTypeTable(metatable)) {
            return false;
        } else if (metatable.has_key("__index")) {
            var index = metatable.get("__index");
            if (! isTypeTable(index)) {
                return false;
            } else {
                return index === null || this.num === index.num ? false : index.has_key(key);
            }
        } else {
            return false;
        }
    } else {
        return false;
    }
};

Table.prototype.get = function(key) {
    if (this._data.hasOwnProperty(hash(key))) {
        return this._data[hash(key)];
    } else if (this._data.hasOwnProperty(hash("__mt"))) {
        var metatable = this._data[hash("__mt")];
        if (! isTypeTable(metatable)) {
            var stringval = key === null ? "null" : key.toString();
            throw new ExecError("Tried to get nonexistent key: "
                                + stringval
                                + ".  Non-table used as metatable.");
        } else if (metatable.has_key("__index")) {
            var index = metatable.get("__index");
            if (! isTypeTable(index)) {
                var stringval = key === null ? "null" : key.toString();
                throw new ExecError("Tried to get nonexistent key: "
                                    + stringval
                                    + ".  Non-table used as __index.");
            } else if (index === null || this.num === index.num) {
                // NOTE: The following doesn't match the reference interpreter,
                // but feels more right
                var stringval = key === null ? "null" : key.toString();
                throw new ExecError("Tried to get nonexistent key: " + stringval);
            } else {
                return index.get(key);
            }
        } else {
            var stringval = key === null ? "null" : key.toString();
            throw new ExecError("Tried to get nonexistent key: "
                                + stringval
                                + ".  No __index in metatable.");
        }
    } else {
        var stringval = key === null ? "null" : key.toString();
        throw new ExecError("Tried to get nonexistent key: " + stringval);
    }
};

Table.prototype.toString = function() {
    return this.toStringHelper([]);
};

Table.prototype.toStringHelper = function(prevTableIds) {
    var result = [];
    for (var i in this._keys) {
        var key = this._keys[i];
        var k = key === null ? "null" : key.toString();
        var v = this.get(key);
        if (isTypeTable(v)) {
            if (this.num === v.num) { // self-referential tables
                v = "self";
            } else if (prevTableIds.indexOf(v.num) >= 0) { // mutually-referential tables
                v = "Table";
            } else {
                prevTableIds.push(v.num);
                v = v.toStringHelper(prevTableIds);
            }
        } else {
            v = v === null ? "null" : v.toString();
        }
        result.push(k + ": " + v)
    }
    return "{" + result.join(", ") + "}";
}

Table.prototype.get_length = function() {
    var i = 0;
    while (this.has_key(i)) {
        i++;
    }
    return i;
};

function hash(key) {
    switch (typeof(key)) {
        case "number":
            key = "n" + key;
            break;
        case "string":
            key = "s" + key;
            break;
        case "object":
            if (key === null) {}
            else if (key.type === "closure") {
                key = "c" + key.num;
            }
            else if (key.type === "table") {
                key = "t" + key.num;
            }
            break;
    }
    return key;
}

if (typeof(module) !== 'undefined') {
    module.exports = {
        Table: Table
    };
}
