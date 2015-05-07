#include <iostream>
#include "dll.h"

using namespace std;

int main () {
    const int N = 8;
    const int K = 2;
    cout << "For N=" << N << " and K=" << K << " :" << endl;
    DLLnode *list = 0;
    for (int j=N; j>0; j--) {
        DLLnode *newGuy = new DLLnode (j);
        if (list != 0) {
            list = list->Insert(newGuy);
        } else {
            list = newGuy;
        }
    }
    list->Print ();
    while (!list->LengthIs1 ()) {
        for (int j=0; j<K; j++) {
            list = list->Next ();
        }
        list = list->Delete();
    }
    cout << "Only one person remains: ";
    list->Print ();
    return 0;
}

