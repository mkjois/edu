#include <iostream>
using namespace std;

int main() {
    bool prevZero = false; // checks for consecutive zeroes
    bool finished = false;
    int subtotal = 0;
    int total = 0;
    int userInput;
    while (!finished) {
        cout << "Input: ";
        cin >> userInput;
        subtotal += userInput;
        if (userInput == 0 && prevZero == true) { // consecutive zeroes
            cout << "Total: " << total << endl;
            finished = true; // exits the while loop
        } else if (userInput == 0 && prevZero == false) {
            cout << "Subtotal: " << subtotal << endl;
            total += subtotal;
            subtotal = 0;
            prevZero = true;
        } else { // for non-zero inputs
            prevZero = false;
        }
    }
}
