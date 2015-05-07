\NeedsTeXFormat{LaTeX2e}
\ProvidesClass{cs188}

\DeclareOption*{%
  \PassOptionsToClass{\CurrentOption}{problem-set}%
}

\ProcessOptions

\LoadClass{problem-set}

\def\coursenumber{CS 188}
\def\coursename{Introduction to \mbox{Artificial Intelligence}}
\def\coursesemester{Fall 2013}
