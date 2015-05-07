CS162 Group 23 Project Repo
===========================

"Basically everyone just fuck around with everything."

FYI
---

In general, please don't push to the **master** branch unless it is thoroughly unit-tested, though feel free to create your own branches as you code and test.

For Project 1 testing, just stick to having a static `selfTest()` method in either:

1. The class(es) you are primarily working on; or
2. An independent class file within the `threads` directory. Make sure you add this to the `Makefile` though.

Then simply call the method within the body of `ThreadedKernel.selfTest()`. I've included a sample test in `PriorityScheduler`. It's very similar to `PingTest` in `KThread`, but I've added some hopefully helpful comments.

2/28/14:

Added a simple shell script in `nachos/bin/nachos` that extends the provided one. Must be called from outside the outer `nachos` directory.

Simply call `nachos/bin/nachos <projX> <testFile> [other options...]`, where `<projX>` is "proj1" for project 1's configuration file, "proj2" for project 2's configuration file, etc.; `<testFile>` is a `.coff` file compiled from a C program located in `nachos/test`. Make `nachos/bin` part of your `PATH` to make this even more convenient.

Also commented the line in `UserKernel.java` that runs all of our project 1 tests, as we hopefully won't need them.

3/2/14:

Added a proj2 directory to design which contains a Python script `build.py`.
The script simply compiles LaTeX documents using `pdflatex`.
It inserts the appropriate header and footer part 2, then compiles all the parts together into one document. This means you can write your part into a file called `partX.tex`, where X is your part number, then run the script with the parts you want to compile as arguments.
For example: `python build.py 1 2 3` compiles the LaTeX document with parts 1 through 3. You need not have `\begin{document}` tags or anything like that in your LaTeX file, simply start with `\section{...}` and continue through to the end of your section.
I know this works on UNIX-like systems, including Mac OS X, as long as your LaTeX compile command is `pdflatex`. On Windows systems, I'm not sure if this will work. If it doesn't, then run the build script with the `--no-compile` flag, and it will just output `out.tex`, which you can compile yourself.
The output file is `out.pdf`.
