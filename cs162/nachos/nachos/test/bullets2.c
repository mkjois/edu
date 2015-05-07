/* bullets2.c
 *  Tests for file system syscalls.
 *  Gotta make the kernel "bulletproof" ;D
 */

#include "stdio.h"
#include "stdlib.h"
#include "syscall.h"

void error_if(int, char*, char*);

int main() {
    char* testnum = "bullets2.c";
    int i;
    FILE files[20];
    FILE nofile;
    FILE alphanum;
    FILE alphanum2;
    char forstdout[] = {'s','h','a','k','e',' ','n',' ','b','a','k','e','\n','\0'};
    char forstdin[21];
    char buffer[20];
    char checker[100];
    char* alpha = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789\n";
    int goodread;
    int goodwrite;
    int badread;
    int badwrite;
    int closed;
    int unlinked;
    FILE goodopen;
    FILE badopen;
    FILE goodcreat;
    FILE final;

    nofile = open("wherediditgo.test");
    alphanum = open("alphanum.txt");
    alphanum2 = creat("alphanum2.test");
    error_if(nofile != -1, testnum, "open() worked on nonexistent file");
    error_if(alphanum != 2, testnum, "couldn't open() file alphanum");
    error_if(alphanum2 != 3, testnum, "couldn't creat() file alphanum2");

    forstdin[20] = '\0';
    goodread = read(stdin, forstdin, 20);
    goodwrite = write(stdout, forstdout, strlen(forstdout));
    error_if(goodwrite != 13, testnum, "didn't write() 13 bytes to stdout");
    error_if(goodread != 20, testnum, "didn't read() 20 bytes from stdin");
    badread = read(stdout, forstdin, 20);
    badwrite = write(stdin, forstdout, strlen(forstdout));
    error_if(badread != 0, testnum, "no error reading from stdout");
    error_if(badwrite != 0, testnum, "no error writing to stdin");
    error_if(strlen(forstdin) != 20, testnum, "input isn't length 20");

    badread = read(alphanum, buffer, -1);
    badwrite = write(alphanum, buffer, -1);
    error_if(badread != -1, testnum, "read() worked requesting -1 bytes");
    error_if(badwrite != -1, testnum, "write() worked requesting -1 bytes");
    badread = read(nofile, buffer, 20);
    badwrite = write(nofile, buffer, 20);
    error_if(badread != -1, testnum, "read() worked on nonexistent file");
    error_if(badwrite != -1, testnum, "write() worked on nonexistent file");
    
    goodread = 1;
    while (goodread > 0) {
        goodread = read(alphanum, buffer, 20);
        error_if(goodread < 0, testnum, "reading file alphanum erred");
        goodwrite = write(alphanum2, buffer, goodread);
        error_if(goodwrite < 0, testnum, "writing file alphanum2 erred");
    }
    goodread = read(open("alphanum2.test"), checker, 100);
    error_if(strcmp(checker, alpha) != 0, testnum, "files aren't identical");

    closed = close(alphanum);
    badread = read(alphanum, buffer, 20);
    badwrite = write(alphanum, buffer, 20);
    error_if(badread != -1, testnum, "read() worked on closed file");
    error_if(badwrite != -1, testnum, "write() worked on closed file");

    goodopen = open("alphanum2.test");
    error_if(goodopen != 5, testnum, "opening file alphanum2 a second time failed");
    unlinked = unlink("alphanum2.test");
    error_if(unlinked != 0, testnum, "unlinking file alphanum2 didn't work");
    badopen = open("alphanum2.test");
    error_if(badopen != -1, testnum, "open() worked on unlinked file");
    goodcreat = creat("alphanum2.test");
    error_if(goodcreat != 6, testnum, "creating file alphanum2 after unlink() failed");
    goodread = read(goodcreat, buffer, 20);
    error_if(goodread < 0, testnum, "reading 2nd version of alphanum didn't work");
    goodread = read(goodopen, buffer, 20);
    error_if(goodread < 0, testnum, "reading alphanum with valid FD didn't work");
    closed = close(goodcreat);
    badread = read(goodcreat, buffer, 20);
    error_if(badread != -1, testnum, "reading worked on invalid FD");
    unlinked = unlink("alphanum2.test");

    final = creat("final2.test");
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
