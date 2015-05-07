if (typeof(module) !== 'undefined') {
  var fs = require("fs"),
      fparse = require("./contractJS-lib/function_grammar"),
      cparse = require("./contractJS-lib/contract_grammar").parser,
      bytecode = require("./contractJS-lib/bytecode");
      compiler = require("./contractJS-lib/compiler");
}

/**
 * Builds the file with tests from the previous file which included
 * the tests as documentation.
 * @param data is the text of the file.
 * @param depth is the depth of the file in the directory structure.
 * @param name is the name of the target file.
 * @return string of the generated file. This is not saved to the filesystem.
 */
function buildFile(data, depth, name) {
  var ast = fparse.parse(data);
  var newAst = [];
  for (var i in ast) {
    var func = ast[i];
    if (func.name !== null) {
      var newDocs = []
      for (var j in func.docs) {
        var doc = func.docs[j];
        var indexOfHash = doc.text.indexOf("#");
        if (indexOfHash >= 0) {
          var directive = doc.text.slice(indexOfHash);
          try {
            directive = cparse.parse(directive);
          } catch (e) {
            if (e.message.slice(0, 11) == "Parse error") {
              e.message = e.message.replace("1", doc.line);
            }
            console.error(e.message);
            process.exit(1);
          }
          doc.directive = directive;
          newDocs.push(doc);
        }
      }
      func.docs = newDocs;
      newAst.push(func);
    } else {
      console.error("Warning: function at line " + func.line +
                  ", column " + func.column +
                  " has no name and will not be tested");
    }
  }
  var btc = bytecode.compile(newAst);
  var jscode = compiler.processFile(btc, data, depth);
  return jscode;
}

/**
 * Simple function to swap a prefix of a string with another prefix. Behavior
 * if the prefix param is not actually a prefix of s is undefined.
 * @param s is the original string.
 * @param prefix is the prefex to replace.
 * @param replacement is the new prefix.
 * @return string with replaced prefix.
 */
function swapPrefix(s, prefix, replacement) {
  var chopped = s.substring(prefix.length, s.length);
  return replacement + chopped;
}

/**
 * Build a tree-like map of a directory listing files and subdirectories
 * recursively.
 * @param start is the starting directory.
 * @return object containing the directory structure. Has fields files
 * containing a list of files, path containing the path name, and dirs
 * containing recursively found subdirectories.
 */
function fileTree(start) {
  if (fs.lstatSync(start).isDirectory()) {
    var rv = {'path': start, 'files': [], 'dirs': []};
    var contents = fs.readdirSync(start);
    var i;
    for (i = 0; i < contents.length; i++) {
      var info = fileTree(start + '/' + contents[i]);
      if (typeof(info) === 'string') {
        rv.files.push(info);
      } else {
        rv.dirs.push(info);
      }
    }
    return rv;
  } else {
    return start;
  }
}

/**
 * Copes the structure of the source directory to the destination
 * location recursively. Ignores files.
 * @param source is the source directory.
 * @param dest is the destination location.
 */
function copyDirStructure(source, dest) {
  var tree = fileTree(source);
  function helper(branch) {
    var name = swapPrefix(branch.path, source, dest);
    fs.mkdirSync(name);
    var i;
    for (i = 0; i < branch.dirs.length; i++) {
      helper(branch.dirs[i]);
    }
  }
  helper(tree);
  return tree;
}

/**
 * Decides whether to process a file and in either case writes the
 * relevant data to where the new file should be.
 * @param source is the file path.
 * @param dest is the location to write the result to.
 * @param depth is the depth of the file in the directory structure.
 */
function handleFile(source, dest, depth) {
  var data = fs.readFileSync(source, {'encoding': 'utf8'});
  if (source.substring(source.length - 3, source.length) === '.js') {
    var output = buildFile(data, depth, source);
  } else {
  }
  fs.writeFileSync(dest, output);
}

/**
 * Copies the directory structure and then the files (modified or otherwise)
 * from the source directory to the destination, which must not exist.
 * @param source is the source directory.
 * @param dest is the destination directory.
 */
function makeAdaptedVersion(source, dest) {
  var tree = copyDirStructure(source, dest);
  function helper(branch, depth) {
    var i;
    for (i = 0; i < branch.files.length; i++) {
      var name = swapPrefix(branch.files[i], source, dest);
      handleFile(branch.files[i], name, depth);
    }
    for (i = 0; i < branch.dirs.length; i++) {
      helper(branch.dirs[i], depth + 1);
    }
  }
  helper(tree, 0);
}

// THE FOLLOWING CODE IS FOR COMMAND LINE INTERACTION

var args = process.argv.slice(2).filter(function(arg) {
  return arg.charAt(0) !== "-";
});

if (args.length !== 2) {
  console.error("Usage: node contract.js [input dir] [output dir]");
  process.exit(1);
} else {
  var indir = args[0];
  var outdir = args[1];
}

if (!fs.existsSync(indir)) {
  console.error('Error: ' + indir + ' does not exist.');
  process.exit(1);
}
if (!fs.lstatSync(indir).isDirectory()) {
  console.error('Error: ' + indir + ' is not a directory.');
  process.exit(1);
}
if (fs.existsSync(outdir)) {
  console.error('Error: ' + outdir + ' already exists.');
  process.exit(1);
}

makeAdaptedVersion(indir, outdir);
fs.writeFileSync(outdir + '/.contract.js',
                 fs.readFileSync('./contractJS-lib/lib.js'));
