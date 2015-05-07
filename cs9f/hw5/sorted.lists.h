#ifndef SORTEDLIST_H
#define SORTEDLIST_H

#include <iostream>
#include <cassert>

using namespace std;

template <class NODETYPE> class SortedList;
template <class NODETYPE> class SortedListIterator;

////////////////////////
// ListNode INTERFACE //
////////////////////////

template <class NODETYPE>
class ListNode {
    friend class SortedListIterator<NODETYPE>;
	friend class SortedList<NODETYPE>;
public:
	ListNode (const NODETYPE &);
	NODETYPE Info ();
private:
	NODETYPE myInfo;
	ListNode* myNext;
};

/////////////////////////////
// ListNode IMPLEMENTATION //
/////////////////////////////

template <class NODETYPE>
ListNode<NODETYPE>::ListNode (const NODETYPE &value) {
	myInfo = value;
	myNext = 0;
}

template <class NODETYPE>
NODETYPE ListNode<NODETYPE>::Info () {
	return myInfo;
}

//////////////////////////
// SortedList INTERFACE //
//////////////////////////

template <class NODETYPE>
class SortedList {
    friend class SortedListIterator<NODETYPE>;
public:
	SortedList ();
	~SortedList ();
	SortedList (const SortedList<NODETYPE> &);
	void Insert (const NODETYPE &);
	bool IsEmpty ();
    SortedList& operator= (const SortedList<NODETYPE> &);
private:
	ListNode<NODETYPE>* myFirst;
};

///////////////////////////////
// SortedList IMPLEMENTATION //
///////////////////////////////

template <class NODETYPE>
SortedList<NODETYPE>::SortedList () {	// constructor
	myFirst = 0;
}

template <class NODETYPE>
SortedList<NODETYPE>::~SortedList () {	// destructor
	if (!IsEmpty ()) {
		cerr << "*** in destructor, destroying: ";
		ListNode<NODETYPE>* current = myFirst;
		ListNode<NODETYPE>* temp;
		while (current != 0) {
			cerr << " " << current->myInfo;
			temp = current;
			current = current->myNext;
			delete temp;
		}
		cerr << endl;
	}
}

template <class NODETYPE>
SortedList<NODETYPE>::SortedList (const SortedList<NODETYPE> &list) {	// copy constructor
	cerr << "*** in copy constructor" << endl;
	ListNode<NODETYPE>* listCurrent = list.myFirst;
	ListNode<NODETYPE>* newCurrent = 0;
	while (listCurrent != 0) {
		ListNode<NODETYPE> *temp 
		  = new ListNode<NODETYPE> (listCurrent->Info ());
		if (newCurrent == 0) {
			myFirst = temp;
			newCurrent = myFirst;
		} else {
			newCurrent->myNext = temp;
			newCurrent = temp;
		}
		listCurrent = listCurrent->myNext;
	}
}

template <class NODETYPE>
void SortedList<NODETYPE>::Insert (const NODETYPE &value) {	// Insert
	ListNode<NODETYPE> *toInsert 
	  = new ListNode<NODETYPE> (value);
	if (IsEmpty ()) {
		myFirst = toInsert;
	} else if (value < myFirst->Info ()) {
		toInsert->myNext = myFirst;
		myFirst = toInsert;
	} else {
		ListNode<NODETYPE> *temp = myFirst;
		for (temp = myFirst; 
			  temp->myNext != 0 && temp->myNext->Info () < value; 
			  temp = temp->myNext) {
		}
		toInsert->myNext = temp->myNext;
		temp->myNext = toInsert;
	}
}

template <class NODETYPE>
bool SortedList<NODETYPE>::IsEmpty () {	// IsEmpty
	return myFirst == 0;
}

template <class NODETYPE>
SortedList<NODETYPE>& SortedList<NODETYPE>::operator= (const SortedList<NODETYPE> &list) {
    if (myFirst == list.myFirst) {
        cerr << "Cannot assign list to itself." << endl;
        return *this;
    }
    ListNode<NODETYPE> *addCurr = 0;
    ListNode<NODETYPE> *delCurr = myFirst;
    ListNode<NODETYPE> *listCurr = list.myFirst;
    ListNode<NODETYPE> *temp, *newNode;
    cerr << "*** in operator=, destroying: ";
    while (delCurr || listCurr) {
        if (listCurr) {
            newNode = new ListNode<NODETYPE>(listCurr->myInfo);
            if (addCurr == 0) {
                myFirst = newNode;
                addCurr = myFirst;
            } else {
                addCurr->myNext = newNode;
                addCurr = addCurr->myNext;
            }
            listCurr = listCurr->myNext;
        }
        if (delCurr) {
            temp = delCurr;
            delCurr = delCurr->myNext;
            cerr << " " << temp->myInfo;
            delete temp;
        }
    }
    cerr << endl;
    return *this;
}

//////////////////////////////////
// SortedListIterator INTERFACE //
//////////////////////////////////

template <class NODETYPE>
class SortedListIterator {
public:
    SortedListIterator (const SortedList<NODETYPE> &);
    bool MoreRemain ();
    NODETYPE Next ();
private:
    ListNode<NODETYPE>* current;
};

///////////////////////////////////////
// SortedListIterator IMPLEMENTATION //
///////////////////////////////////////

template <class NODETYPE>
SortedListIterator<NODETYPE>::SortedListIterator (const SortedList<NODETYPE> &list) {
    current = list.myFirst;
}

template <class NODETYPE>
bool SortedListIterator<NODETYPE>::MoreRemain () {
    return (current != 0);
}

template <class NODETYPE>
NODETYPE SortedListIterator<NODETYPE>::Next () {
    NODETYPE temp = current->myInfo;
    current = current->myNext;
    return temp;
}

#endif
