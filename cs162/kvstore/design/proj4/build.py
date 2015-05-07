#!/usr/bin/python

"""
This should work on all UNIX-like systems.
Usage is as follows:
python build.py [parts] [--no-compile]
So if you want to compile with parts 1 and 3,
use 'python build.py 1 3' (without the quotes)
Make sure you name your file 'partX.tex', where
X is your part of the project.
You need not have any headers or footers on your file.
Simply write out your section. Headers and footers will
be appended to the top and bottom of the output file.
Output will be written to 'out.tex', and will be compiled
to 'out.pdf' unless the '--no-compile' flag is specified.
"""

from datetime import date
from sys import argv
import os

HEADER = """
\\documentclass{article}
\\usepackage[utf8]{inputenc}
\\usepackage{listings}
\\usepackage{authblk}
\\usepackage{enumerate}
\\usepackage[usenames,dvipsnames]{color}
\\usepackage[top=1in, bottom=1in, left=1in, right=1in]{geometry}

\\title{Project 4: Distributed Key-Value Store}
\\author{Romil Singapuri (ha), Alexander Wang (is), Manohar Jois (jp),
\\\Shrayus Gupta (jq), Junseok Lee (fy)}
"""
HEADER += "\\date{" + date.today().strftime("%B %d, %Y") + "}"

HEADER += """
\\begin{document}
\\lstset{language=Python}
\\maketitle
"""

FOOTER = "\\end{document}"

if __name__ == '__main__':
    files = []
    no_compile = False
    for arg in argv:
        if arg in ['1', '2', '3', '4', '5']:
            files.append('part' + arg + '.tex')
        elif arg == '--no-compile':
            no_compile = True
    files.sort()
    out = str(HEADER)
    for name in files:
        with open(name, 'r') as f:
            out += '\n'
            out += f.read()
    out += '\n'
    out += FOOTER
    with open('out.tex', 'w') as f:
        f.write(out)
    if not no_compile:
        os.system('pdflatex out.tex')
