#ifndef INVENTORY_H
#define INVENTORY_H

#include <vector>
#include <string>
#include <iostream>
#include <fstream>
#include <sstream>
#include <cstdlib>

using namespace std;

struct ItemData {
    public:
        string item;
        int amount;
        ItemData ();
        ItemData (string name, int n);
};

class Inventory {
    public:
        Inventory ();
        void Update (string item, int amount);
        void ListByName ();
        void ListByQuantity ();
        void InterpretCommands (istream& cmds);
    private:
        vector<ItemData> inv;
        void InterpretUpdate (istream& cmdLine);
        void InterpretList (istream& cmdLine);
        void InterpretBatch (istream& cmdLine);
        void InterpretQuit (istream& cmdLine);
};

#endif
