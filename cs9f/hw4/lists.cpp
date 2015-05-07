#include <iostream>
#include <cassert>
#include "lists.h"

using namespace std;

ListNode::ListNode (int k) {
    myValue = k;
    myNext = 0;
}

ListNode::ListNode (int k, ListNode* ptr) {
    myValue = k;
    myNext = ptr;
}

// Delete the node and all nodes accessible through it.
// Precondition: this != 0.
ListNode::~ListNode () {
    cout << "Deleting node with value " << myValue << endl;
    if (myNext) {
        delete myNext;
        myNext = 0;
    }
}

// Print the list.
void ListNode::Print () {
    ListNode* list = this;
    for (; list; list = list->Rest()) {
        cout << list->First() << " ";
    }
    cout << endl;
}

// Return the myValue stored in the node.
int ListNode::First () {
    return myValue;
}

// Return the list of the remaining elements.
ListNode* ListNode::Rest () {
    return myNext;
}
