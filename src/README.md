A nonogram is a puzzle with a 2-dimensional grid, where every column and row on that grid has one or more numbers that depict which points in that column or row are filled in. Each number depicts a consecutive group of filled-in points, while two or numbers will have at least one not-filled-in point between them.

This project allows users to import Nonograms and solve them, meaning to show the solution. It will also show the Nonogram being solved by the project in real-time.

----

This version of the Nonogram Solver is unable to solve some puzzles because when the Solver is attempting to solve a column or row where there appears to be space at the start to fill in, but there are actually filledIn points at the end, then the Solver will not recognize the ones at the end.
This is caused by a flaw in the design of the system that solves a column or row in the Nonogram. It finds the solution where all filled-in points are as far forward as possible, a solution where all filled-in points are as far back as possible, then finds matching points between the two to save to the solution. The interim solutions it creates are incorrect when all consecutive points can be placed in the beginning, even though there are points later at the end.

To fix this, I'm replacing the design with a new system that instead solves a column or row by checking if each unsolved point in that column or row must be filledIn or crossedOut. A point must be filledIn if crossing it out would make the column/row unsolvable, and vice versa for the must be crossedOut case. This new design will be present in the next commit, this current commit is mainly for archival sake.