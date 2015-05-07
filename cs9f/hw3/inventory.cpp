#include <vector>
#include <string>
#include <cctype>
#include <iostream>
#include <fstream>
#include <sstream>
#include <cstdlib>
#include "inventory.h"

using namespace std;

void Quick (vector<ItemData>&, int, int, int);
int Sort (vector<ItemData>&, int, int , int);

//////////////////////
// ITEM_DATA STRUCT //
//////////////////////

ItemData::ItemData ()
    : item("NO_ITEM"),
      amount(0)
{}

ItemData::ItemData (string name, int n)
    : item(name),
      amount(n)
{}

/////////////////////
// INVENTORY CLASS //
/////////////////////

Inventory::Inventory ()
    : inv(vector<ItemData>(0))
{}

void Inventory::Update (string item, int amount) {
    for (int i = 0; i < inv.size(); i++) {
        if (inv[i].item == item) { // if item exists
            inv[i].amount += amount;
            return;
        }
    }
    ItemData info(item, amount);
    inv.push_back(info);
}

void Inventory::ListByName () {
    Quick(inv, 0, inv.size()-1, 0);
    for (int i = 0; i < inv.size(); i++) {
        cout << inv[i].item + ":\t" << inv[i].amount << endl;
    }
}

void Inventory::ListByQuantity () {
    Quick(inv, 0, inv.size()-1, 1);
    for (int i = 0; i < inv.size(); i++) {
        cout << inv[i].item + ":\t" << inv[i].amount << endl;
    }
}

////////////////////////////////////
// QUICKSORT AND HELPER FUNCTIONS //
////////////////////////////////////

// Returns whether c1 is alphabetically before c2
bool CharAlphaBefore (char c1, char c2) {
    if (islower(c1)) {
        c1 = toupper(c1);
    }
    if (islower(c2)) {
        c2 = toupper(c2);
    }
    return (c1 < c2);
}

// Returns whether s1 is alphabetically before s2
bool StrAlphaBefore (string s1, string s2) {
    if (s1.length() > s2.length()) {
        return (! StrAlphaBefore(s2, s1));
    }
    for (int i = 0; i < s1.length(); i++) {
        if (CharAlphaBefore(s1.at(i), s2.at(i))) {
            return true;
        } else if (CharAlphaBefore(s2.at(i), s1.at(i))) {
            return false;
        } else {
            continue;
        }
    }
    return (s1.length() < s2.length());
}

// Swaps items at the specified indices in the given vector<ItemData>.
void SwapIndex (vector<ItemData>& vect, int i1, int i2) {
    ItemData temp = vect[i1];
    vect[i1] = vect[i2];
    vect[i2] = temp;
}

// One step in the Quicksort process.
void SortStep (vector<ItemData>& vect, int& pivIndex, int i) {
    pivIndex++;; // increment the pivot's final destination index
    SwapIndex(vect, i, pivIndex);
}

// Moves the first value of a vector<ItemData> to a valid pivot position,
// while also moving other values to appropriate side of pivot.
int Sort (vector<ItemData>& vect, int first, int last, int attr) {
    int pivIndex = first;
    if (attr == 1) {
        int pivValue = vect[pivIndex].amount; // Assigns pivot to first value
        for (int i = first+1; i <= last; i++) {
            if (vect[i].amount < pivValue) {
                SortStep(vect, pivIndex, i);
            }
        }
    } else if (attr == 0) {
        string pivValue = vect[pivIndex].item;
        for (int i = first+1; i <= last; i++) {
            if (StrAlphaBefore(vect[i].item, pivValue)) {
                SortStep(vect, pivIndex, i);
            }
        }
    } else {
        cerr << "Last parameter for Sort() must be 0 or 1" << endl;
        exit(1);
    }
    SwapIndex(vect, first, pivIndex);
    return pivIndex;
}

// Recursive Quicksort to sort a vector<ItemData> in non-descending order.
// Sorts alphabetically if attr=0 and in increasing order if attr=1.
void Quick (vector<ItemData>& vect, int first, int last, int attr) {
    if (! (attr == 0 || attr == 1)) {
        cerr << "Last parameter for Quick() must be 0 or 1" << endl;
        exit(1);
    }
    int pivIndex;
    if (first < last) {
        pivIndex = Sort(vect, first, last, attr);
        Quick(vect, first, pivIndex - 1, attr);
        Quick(vect, pivIndex + 1, last, attr);
    }
}

///////////////////////////////////
// SUPPORT FOR TERMINAL COMMANDS //
///////////////////////////////////

void Inventory::InterpretCommands (istream& cmds) {
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

void Inventory::InterpretUpdate(istream& cmdLine) {
    string word, extra;
    int num;
    if (cmdLine >> word >> num) {
        if (! (cmdLine >> extra)) {
            Update(word, num);
        } else {
            cerr << "Error: update command only takes 2 arguments" << endl;
        }
    } else {
        cerr << "Error: update command not given proper arguments" << endl;
    }
}

void Inventory::InterpretList(istream& cmdLine) {
    string arg, extra;
    if (cmdLine >> arg) {
        if (! (cmdLine >> extra)) {
            if (arg == "names") {
                ListByName();
            } else if (arg == "quantities") {
                ListByQuantity();
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

void Inventory::InterpretBatch(istream& cmdLine) {
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

void Inventory::InterpretQuit(istream& cmdLine) {
    string extra;
    if (! (cmdLine >> extra)) {
        exit(0);
    } else {
        cerr << "Error: quit command does not take arguments" << endl;
    }
}

//////////
// MAIN //
//////////

int main () {
    Inventory i;
    i.InterpretCommands(cin);
    return 0;
}
