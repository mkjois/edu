/* p1t3.c
 *  Tests for file system syscalls.
 */

#include "stdio.h"
#include "stdlib.h"
#include "syscall.h"

void error_if(int, char*, char*);

int main() {
    char* testnum = "p1t3.c";
    int readsize = 26;
    char readbuf[readsize+1];
    int bytesread;
    int byteswrit;
    int closed;
    int closed2;
    int unlinked;
    FILE alphanum = open("alphanum.txt");
    FILE temp = creat("temp.test");
    FILE temp2 = creat("temp2.test");

    error_if(alphanum < 0, testnum, "couldn't open file alphanum");
    error_if(temp < 0, testnum, "couldn't create/open file temp");
    error_if(temp2 < 0, testnum, "couldn't create/open file temp2");

    bytesread = read(alphanum, readbuf, readsize);
    error_if(bytesread < 0, testnum, "couldn't read from file alphanum");
    readbuf[bytesread] = '\n';
    byteswrit = write(temp, readbuf, bytesread+1);
    error_if(byteswrit < 0, testnum, "couldn't write to file temp");

    closed = close(temp);
    unlinked = unlink("temp2.test");
    closed2 = close(temp2);
    error_if(closed < 0, testnum, "error closing file temp");
    error_if(unlinked < 0, testnum, "error unlinking file temp2");
    error_if(closed2 < 0, testnum, "error closing file temp2 after unlinking");

    printf("Test %s passed\n", testnum);
    exit(0);
    printf("exit magically returned");
    return 0;
}

void error_if(int condition, char* testnum, char* message) {
    if (condition) {
        printf("Test %s failed: %s\n", testnum, message);
        halt();
        printf("you aint de root boi");
    }
}
