/* bullets1.c
 *  Tests for file system syscalls.
 *  Gotta make the kernel "bulletproof" ;D
 */

#include "stdio.h"
#include "stdlib.h"
#include "syscall.h"

void error_if(int, char*, char*);

int main() {
    char* testnum = "bullets1.c";
    int i;
    char* alphanames[] = {"a.test","b.test","c.test","d.test","e.test",
                          "f.test","g.test","h.test","i.test","j.test",
                          "k.test","l.test","m.test","n.test","o.test",
                          "p.test","q.test","r.test","s.test","t.test",
                          "u.test","v.test","w.test","x.test","y.test","z.test"};
    char* goodname = "zzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzz.test";
    char* badname = "zzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzz.test";
    FILE files[20];
    FILE goodcreat1;
    FILE badcreat1;
    FILE badcreat2;
    FILE goodunlink1;
    FILE badunlink1;
    FILE badunlink2;
    int closed;
    int unlinked;
    FILE final;

    goodcreat1 = creat(goodname);
    badcreat1 = creat(badname);
    badcreat2 = creat((char*)(-1));
    error_if(goodcreat1 != 2, testnum, "good creat() failed");
    error_if(badcreat1 != -1, testnum, "creat() worked on a file name of 257 chars");
    error_if(badcreat2 != -1, testnum, "creat() worked on a negative pointer");
    files[0] = stdin;
    files[1] = stdout;
    files[goodcreat1] = goodcreat1;
    for (i=3; i<20; i++) {
        files[i] = creat(alphanames[i]);
    }
    for (i=0; i<20; i++) {
        if (i<16) {
            error_if(files[i] != i, testnum, "creat() incorrectly failed");
        } else {
            error_if(files[i] != -1, testnum, "creat() incorrectly succeeded");
        }
    }

    for (i=-2; i<8; i++) {
        closed = close(i);
        if (i<0) {
            error_if(closed != -1, testnum, "close() didn't error on negative FD");
        } else {
            error_if(closed != 0, testnum, "close() didn't work on valid FD");
        }
    }
    for (i=0; i<26; i++) {
        unlinked = unlink(alphanames[i]);
        if (i<3 || i>15) {
            error_if(unlinked != -1, testnum, "unlink() marked a non-existent file");
        } else {
            error_if(unlinked != 0, testnum, "unlink() didn't mark an existing file");
        }
    }
    for (i=4; i<20; i++) {
        closed = close(i);
        if (i<8 || i>15) {
            error_if(closed != -1, testnum, "close() didn't error on invalid FD");
        } else {
            error_if(closed != 0, testnum, "close() erred on valid FD after unlink()");
        }
    }
    goodunlink1 = unlink(goodname);
    badunlink1 = unlink(badname);
    badunlink2 = unlink((char*)(-1));
    error_if(goodunlink1 != 0, testnum, "good unlink() failed");
    error_if(badunlink1 != -1, testnum, "unlink() worked on a file name of 257 chars");
    error_if(badunlink2 != -1, testnum, "unlink() worked on a negative pointer");

    final = creat("final1.test");
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
