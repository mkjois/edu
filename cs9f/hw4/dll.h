#ifndef DLL_H
#define DLL_H

#include <iostream>
#include <cassert>

class DLLnode {
public:
	DLLnode (int k);
	DLLnode *Previous ();  // Return a ptr to the predecessor
	                       // of the given node.
	DLLnode *Next ();      // Return a ptr to the successor
	                       // of the given node.
	DLLnode *Insert (DLLnode *ptr);  // Insert ptr node at head 
	                                 // of list and return pointer 
	                                 // to new element.
	DLLnode *Delete ();    // Delete first node from the list;
	                       // return pointer to its successor, or 0.
	                       // Precondition: there is a node to delete.
	void Print ();
	bool LengthIs1 ();  // Return true if the list contains exactly 1 element.
private:
	int myValue;
	DLLnode* myPrevious;
	DLLnode* myNext;
};

#endif
