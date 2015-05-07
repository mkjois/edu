#include <iostream>
#include <string>
#include "amoebas.h"

using namespace std;

int main () {
	Amoeba* me = new Amoeba (string ("me"));
	Amoeba* parent = new Amoeba (string ("mom/dad"));
	Amoeba* grandparent = new Amoeba (string ("Amos McCoy"));
	Amoeba* brother = new Amoeba (string ("Fred"));
	Amoeba* sister = new Amoeba (string ("Wilma"));
	Amoeba* child1 = new Amoeba (string ("Mike"));
	Amoeba* child2 = new Amoeba (string ("Homer"));
	Amoeba* child3 = new Amoeba (string ("Marge"));
	Amoeba* grandchild11 = new Amoeba (string ("Bart"));
	Amoeba* grandchild12 = new Amoeba (string ("Lisa"));
	Amoeba* grandchild31 = new Amoeba (string ("Bill"));
	Amoeba* grandchild32 = new Amoeba (string ("Hilary"));

	// This will seem a bit backward, since we have an "add child"
	// instead of an "add parent".

	// First do Mike's kids.
	child1->AddChild (grandchild11);
	child1->AddChild (grandchild12);

	// Next do Marge's kids.
	child3->AddChild (grandchild31);
	child3->AddChild (grandchild32);

	// Now add Mike, Homer, and Marge to me.
	me->AddChild (child1);
	me->AddChild (child2);
	me->AddChild (child3);

	// Now add me to my parent.
	parent->AddChild (me);

	// Now add my brother and sister.
	parent->AddChild (brother);
	parent->AddChild (sister);

    // Add parent to grandparent.
    grandparent->AddChild(parent);

	// Print my name and my parent's name.
	cout << "Hi, my name is " << me->Name () << endl;
	cout << "Meet my parent " << me->Parent ()->Name () << endl;

    // Print grandparent.
    cout << "My grandparent is " << me->Parent()->Parent()->Name() << endl;

    // Testing PrintChildren()
    cout << endl << "Testing PrintChildren()" << endl << endl;
    cout << "Amos McCoy children: " << endl; grandparent->PrintChildren();
    cout << "mom/dad children: " << endl; parent->PrintChildren();
    cout << "my children: " << endl; me->PrintChildren();
    cout << "Mike children: " << endl; child1->PrintChildren();
    cout << "Marge children: " << endl; child3->PrintChildren();
    cout << "Fred children: " << endl; brother->PrintChildren();
    cout << "Lisa children: " << endl; grandchild12->PrintChildren();

    // Testing PrintGrandchildren()
    cout << endl << "Testing PrintGrandchildren()" << endl << endl;
    cout << "Amos McCoy gc: " << endl; grandparent->PrintGrandchildren();
    cout << "mom/dad gc: " << endl; parent->PrintGrandchildren();
    cout << "my gc: " << endl; me->PrintGrandchildren();
    cout << "Marge gc: " << endl; child3->PrintGrandchildren();
    cout << "Fred gc: " << endl; brother->PrintGrandchildren();
    cout << "Bart gc: " << endl; grandchild11->PrintGrandchildren();

    // Testing PrintDescendants()
    cout << endl << "Testing PrintDescendants()" << endl << endl;
    cout << "Amos McCoy tree: " << endl; grandparent->PrintDescendants();
    cout << endl << "mom/dad tree: " << endl; parent->PrintDescendants();
    cout << endl << "my tree: " << endl; me->PrintDescendants();
    cout << endl << "Mike tree: " << endl; child1->PrintDescendants();
    cout << endl << "Marge tree: " << endl; child3->PrintDescendants();
    cout << endl << "Wilma tree: " << endl; sister->PrintDescendants();
    cout << endl << "Homer tree: " << endl; child2->PrintDescendants();
    cout << endl << "Lisa tree: " << endl; grandchild12->PrintDescendants();
    cout << endl << "Hilary tree: " << endl; grandchild32->PrintDescendants();
}
