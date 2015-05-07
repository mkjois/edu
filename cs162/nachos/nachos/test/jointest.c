#include "stdio.h"
#include "stdlib.h"

int main(int argc, char **argv) {
    char arr[2];
    int result[1];
    char *args[1];
    int pid;
    arr[1] = 0;
    if (argc == 0) {
        printf("Initiating join test... ");
        arr[0] = 'a';
        arr[1] = '\0';
    } else {
        arr[0] = **argv + 1;
        arr[1] = '\0';
    }
    if (arr[0] == 'd') {
        exit(1);
    }
    args[0] = arr;
    pid = exec("jointest.coff", 1, args);
    if (pid == -1) {
        printf("failed due to execution error.\n");
        exit(0);
    }
    join(pid, result);
    if (argc == 0) {
        if (*result == 3) {
            printf("passed.\n");
        } else {
            printf("failed with value %d.\n", *result);
        }
        exit(1);
    } else {
        exit(*result + 1);
    }
}
