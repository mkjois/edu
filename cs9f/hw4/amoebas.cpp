#include <iostream>
#include <string>
#include "amoebas.h"

using namespace std;

Amoeba::Amoeba (string s) {	// an amoeba is born, named s
	myName = s;
	myParent = 0;
	myOlderSibling = 0;
	myOldestChild = 0;
	myYoungestChild = 0;
}

// Access functions
string Amoeba::Name () {
	return myName;
}

Amoeba* Amoeba::Parent () {
	return myParent;
}

void Amoeba::AddChild (Amoeba* newChild) {
	// make child point to parent
	newChild->myParent = this;

	// check for parent having other children
	Amoeba* otherSibling = myYoungestChild;
	if (!otherSibling) {	// the new amoeba is an only child
		myYoungestChild = newChild;	// make the parent's child
		myOldestChild = newChild;		// ptrs point to the new one
		newChild->myOlderSibling = 0;	// no older sibling
	} else {	// there are other kids; make this one the youngest
		myYoungestChild = newChild;	// no younger siblings,
		newChild->myOlderSibling = otherSibling;	// but new kid now
	}					// has older siblings.
}

void Amoeba::PrintChildren () {
    Amoeba* curr = myYoungestChild;
    while (curr) {
        cout << curr->Name() << endl;
        curr = curr->myOlderSibling;
    }
}

void Amoeba::PrintGrandchildren () {
    Amoeba* curr = myYoungestChild;
    while (curr) {
        curr->PrintChildren();
        curr = curr->myOlderSibling;
    }
}

void Amoeba::PrintDescendants () {
    PrintDescWithTabs(0);
}

void Amoeba::PrintDescWithTabs (int tabs) {
    Amoeba* curr = myYoungestChild;
    while (curr) {
        for (int i = 0; i < tabs; i++) {
            cout << "    ";
        }
        cout << curr->Name() << endl;
        curr->PrintDescWithTabs(tabs+1);
        curr = curr->myOlderSibling;
    }
}
