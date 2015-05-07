#include <stdio.h>

int reverseInt(int number);

int main(void) {
    printf (" The reverse of the number is %i -> %i, should be 54321\n", 12345, reverseInt(12345));
    printf (" The reverse of the number is %i -> %i, should be 7331\n", 1337, reverseInt(1337));
    printf (" The reverse of the number is %i -> %i, should be 51\n", 15, reverseInt(15));
    printf (" The reverse of the number is %i -> %i, should be 12321\n", 12321, reverseInt(12321));
    printf (" The reverse of the number is %i -> %i, should be 2013\n", 3102, reverseInt(3102));
}

int reverseInt(int number) {
    int mod = 10,
        order = 1,
        result = 0,
        digit;

    while (number / mod > 0) {
        mod *= 10;
    }
    while (number > 0) {
        mod /= 10;
        digit = number / mod;
        number -= digit * mod;
        result += digit * order;
        order *= 10;
    }
    return result;
}



