#include <iostream>
#include <fstream>
#include <sstream>
#include <string>

#include <cstdlib>

using namespace std;

void InterpretCommands (istream&);
void InterpretUpdate (istream&);
void InterpretList (istream&);
void InterpretBatch (istream&);
void InterpretQuit (istream&);

void InterpretCommands (istream& cmds) {
  string line, lineInWord;
  while (getline(cmds, line)) {
    istringstream lineIn (line);
    if (! (lineIn >> lineInWord)) {
      cerr << "No commands given." << endl;
    } else if (lineInWord=="update") {
      InterpretUpdate (lineIn);
    } else if (lineInWord=="list") {
      InterpretList (lineIn);
    } else if (lineInWord=="batch") {
      InterpretBatch (lineIn);
    } else if (lineInWord=="quit") {
      InterpretQuit (lineIn);
    } else { 
      cerr << "Unrecognizable command word." << endl; 
    }
  }
}

void InterpretUpdate(istream& cmdLine) {
    string word, extra;
    int num;
    if (cmdLine >> word >> num) {
        if (! (cmdLine >> extra)) {
            cout << "update " + word + " " << num << endl;
        } else {
            cerr << "Error: update command only takes 2 arguments" << endl;
        }
    } else {
        cerr << "Error: update command not given proper arguments" << endl;
    }
}

void InterpretList(istream& cmdLine) {
    string arg, extra;
    if (cmdLine >> arg) {
        if (! (cmdLine >> extra)) {
            if (arg == "names" || arg == "quantities") {
                cout << "list " + arg << endl;
            } else {
                cerr << "Error: list command must take \"names\" or "
                     << "\"quantities\" as argument" << endl;
            }
        } else {
            cerr << "Error: list command only takes 1 argument" << endl;
        }
    } else {
        cerr << "Error: list command not given arguments" << endl;
    }
}

void InterpretBatch(istream& cmdLine) {
    string file, extra;
    if (cmdLine >> file) {
        if (! (cmdLine >> extra)) {
            ifstream lines;
            lines.open(file.c_str());
            if (lines.fail()) {
                cerr << "Error: batch command could not open " << file << endl;
            }
            string line;
            while (getline(lines, line)) {
                istringstream lineStream(line);
                InterpretCommands(lineStream);
            }
        } else {
            cerr << "Error: batch command only takes 1 argument" << endl;
        }
    } else {
        cerr << "Error: batch command not given arguments" << endl;
    }
}

void InterpretQuit(istream& cmdLine) {
    string extra;
    if (! (cmdLine >> extra)) {
        exit(0);
    } else {
        cerr << "Error: quit command does not take arguments" << endl;
    }
}

int main ( ) {
  InterpretCommands (cin);
  return 0;
}
