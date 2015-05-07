#include <stdio.h>

/*
* Shifts array a "down" n places, with any indices greater than n left alone
* For example, shift([1, 2, 3], 2) would return [2, 3, 3]
*/
void shift(int a[], int n) {
    int i;
    for(i = 0; i != n; i++)
        a[i] = a[i+1];
}

int main() {
    int pos;
    int i[5] = {1, 2, 3, 4, 5};
    shift(i, 3);
    for(pos = 0; pos < 5; pos++) {
        printf("the %d-th position of the shifted array is: %d\n", pos, i[pos]);
    }
    return 0;
}
