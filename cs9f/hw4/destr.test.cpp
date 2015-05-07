#include <iostream>
#include "lists.h"

using namespace std;

ListNode* FromInput (istream &is) {
    int k;
    ListNode* head = 0;
    while (is >> k) {
        head = new ListNode (k, head);
    }
    return head;
}

int main () {
    ListNode* list = FromInput (cin);
    list->Print ();
    if (list) {
        delete list;
    }
    if (list) {
        list->Print ();
    } else {
        cout << "no list" << endl;
    }
    return 0;
}
