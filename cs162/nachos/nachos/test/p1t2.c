/* p1t2.c
 *  Tests for file system syscalls.
 */

#include "stdio.h"
#include "stdlib.h"
#include "syscall.h"

void error_if(int, char*, char*);

int main() {
    char* testnum = "p1t2.c";
    int readsize = 100;
    char readbuf[readsize+1];
    int bytesread;
    int byteswrit;
    FILE nofile = open("happyhappyjoyjoy");
    FILE alphanum = open("alphanum.txt");

    error_if(nofile != -1, testnum, "open() on a nonexistent file didn't return -1");
    error_if(alphanum < 0, testnum, "couldn't open file alphanum");

    bytesread = read(alphanum, readbuf, readsize);
    error_if(bytesread < 0, testnum, "couldn't read from file alphanum");
    readbuf[bytesread] = '\n';
    byteswrit = write(stdout, readbuf, bytesread+1);
    error_if(byteswrit < 0, testnum, "couldn't write to file foo");
    printf("Test %s passed: %d bytes requested, %d bytes read, %d bytes written\n",
           testnum, readsize, bytesread, byteswrit-1);
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
