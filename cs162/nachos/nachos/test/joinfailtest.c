#include "stdio.h"
#include "stdlib.h"

int main(int argc, char **argv) {
    char *a[1];
    char *b[1];
    char c[2];
    char d[2];
    int result[1];
    int result2[1];
    int pid1;
    int pid2;
    if (argc == 0) {
        printf("Initiating join failure test... ");
        c[1] = '\0';
        d[1] = '\0';
        c[0] = 0;
        a[0] = c;
        b[0] = d;
        pid1 = exec("joinfailtest.coff", 1, a);
        d[0] = pid1;
        pid2 = exec("joinfailtest.coff", 1, b);
        join(pid1, result);
        join(pid2, result);
        if (*result == -1) {
            printf("passed.\n");
        } else {
            printf("failed.\n");
        }
    } else if (argc == 1) {
        if (**argv == 0) {
            exit(1);
        } else {
            int s = join(**argv, result2);
            exit(s);
        }
    }
}
