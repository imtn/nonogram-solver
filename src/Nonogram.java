import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.stream.IntStream;

public class Nonogram {
    /**
     * A nonogram has a width, which is the number of columns,
     * And a height, which is the number of rows.
     * Each column and row has one or more numbers representing how many points are filled in for that row or column.
     */

    int width;
    int height;
    int[][] cols;
    int[][] rows;
    int largestColSize;
    int largestRowSize;
    static String filledIn = "O";
    static String crossedOut = "X";
    /**
     * A current state of the nonogram, where some points may be filled in or crossed out
     * Filled in is O, crossed out is X
     * outer (left) array is rows,
     * inner (right) array is cols
     */
    String[][] state; //

    public Nonogram(File f) {
        largestColSize = 0;
        largestRowSize = 0;
        try (Scanner scan = new Scanner(f)) {
            // When state is 0, parsing width/height
            // when state is 1, parsing columns
            // when state is 2, parsing rows
            int parseState = 0;
            int colIndex = 0;
            int rowIndex = 0;
            while (scan.hasNextLine()) {
                String line = scan.nextLine();

                if (line.isBlank()) {
                    parseState += 1;
                    continue;
                }

                String[] lineArr;
                switch (parseState) {
                    case 0:
                        lineArr = line.split(",");
                        width = Integer.parseInt(lineArr[0]);
                        height = Integer.parseInt(lineArr[1]);
                        cols = new int[width][];
                        rows = new int[height][];
                        break;
                    case 1:
                        lineArr = line.split(",");
                        cols[colIndex] = new int[lineArr.length];
                        for (int i = 0; i < lineArr.length; i++) {
                            cols[colIndex][i] = Integer.parseInt(lineArr[i]);
                        }
                        int thisColSize = getColOrRowSize(cols[colIndex], false);
                        if (thisColSize > largestColSize) {largestColSize = thisColSize;}
                        colIndex++;
                        break;
                    case 2:
                        lineArr = line.split(",");
                        rows[rowIndex] = new int[lineArr.length];
                        for (int i = 0; i < lineArr.length; i++) {
                            rows[rowIndex][i] = Integer.parseInt(lineArr[i]);
                        }
                        int thisRowSize = getColOrRowSize(rows[rowIndex], true);
                        if (thisRowSize > largestRowSize) {largestRowSize = thisRowSize;}
                        rowIndex++;
                        break;
                    default:
                        System.out.println("Weird switch state when parsing Nonogram: " + parseState);
                        break;
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("Didn't find file " + f + " while creating Nonogram.");
            e.printStackTrace();
        }
        state = new String[height][width];
        for (int i = 0; i < state.length; i++) {
            Arrays.fill(state[i], " ");
        }
    }

    public void print() {
        System.out.println("Width is " + width + " and Height is " + height);
        System.out.println("Largest Col has " + largestColSize + " and largest Row has " + largestRowSize);
        System.out.println("Columns:");
        for (int i = 0; i < cols.length; i++) {
            for (int j = 0; j < cols[i].length; j++) {
                System.out.print(cols[i][j] + " ");
            }
            System.out.println("");
        }
        System.out.println("Rows");
        for (int i = 0; i < rows.length; i++) {
            for (int j = 0; j < rows[i].length; j++) {
                System.out.print(rows[i][j] + " ");
            }
            System.out.println("");
        }
    }

    /**
     * Creates a string of the nonogram and its current state
     * Displayed as rows of O, X, and ' ' (whitespace)
     * Numbers for the rows and cols will be displayed along the top and left
     * Looks best with a monospaced font
     * @return the string representation of the nonogram as described above
     */
    public String toString(boolean printCrossedOut) {
        StringBuilder sb = new StringBuilder();

        //Add columns to stringBuilder
        for (int i = 0; i < largestColSize; i++) {
            sb.append(" ".repeat(largestRowSize + 1));
            for( int col = 0; col < cols.length; col++ ) {
                if ( cols[col].length - largestColSize + i >= 0 ) {
                    sb.append(cols[col][cols[col].length - largestColSize + i]);
                } else {
                    sb.append(" ");
                }
            }
            sb.append("\n");
        }

        //Add rows and state grid to stringBuilder
        for (int i = 0; i < height; i++) {
            //Add row
            sb.append(" ".repeat(largestRowSize - getColOrRowSize(rows[i], true)));
            for (int rowPos = 0; rowPos < rows[i].length; rowPos++) {
                sb.append(rows[i][rowPos] + " ");
            }

            //Add state grid
            for (String rowVal : state[i]) {
                if (printCrossedOut) {
                    sb.append(rowVal);
                } else {
                    if (rowVal.equals(crossedOut)) {
                        sb.append(" ");
                    } else {
                        sb.append(rowVal);
                    }
                }
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    /**
     * This method checks that an imported row or column can fit within the length specified.
     * It does this by verifying that the count of all filled in and crossed out blocks, defined by rc,
     * Is less than or equal to the length.
     * @param rc Integer array representing the row or column
     * @param length Integer representing the width or height to compare rc to
     * @return true if rc fits within length, false otherwise.
     */
    private static boolean isRowOrColValid(int[] rc, int length) {
        int count = rc.length - 1;
        for(Integer i : rc){
            count += i;
        }
        return count <= length;
    }

    private static int getColOrRowSize(int[] cr, boolean isRow) {
        // The size of a column or row, for the toString method
        // For columns, it is the length of the cr array
        // For rows, it is the sum of the number of digits across all ints in cr,
        //     Plus cr.length - 1, to add a space between each int in toString
        if (!isRow) {
            return cr.length;
        } else {
            int size = 0;
            for (Integer i : cr) {
                size += String.valueOf(i).length();
            }
            size += cr.length - 1;
            return size;
        }
    }

    public boolean isSolved() {
        // Check every column and row and compare to state
        // If all are filled correctly, then this nonogram is solved.

        // Check rows
        for (int row = 0; row < rows.length; row++) {
            if (!colOrRowIsFullySolved(rows[row], state[row])) {
                return false;
            }
        }

        // Check columns
        for (int col = 0; col < cols.length; col++) {
            String[] colState = new String[height];
            for (int row = 0; row < rows.length ; row++) { // build the column
                colState[row] = state[row][col];
            }
            if (!colOrRowIsFullySolved(cols[col], colState)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Takes in a column or row and its state, and returns a new state array with any new Os or Xs it identifies
     * @param cr int array that defines what points are in this col or row
     * @param crStateArr string array of currently known points
     * @return a new array with the values, or an empty array length 0 if no values were modified, crStateArr is already solved
     */
    public static String[] solveColOrRow(int[] cr, String[] crStateArr) {
        if (!arrContains(crStateArr, " ", 0, crStateArr.length)) { return new String[0]; }

        System.out.println("Solving crStateArr |" + String.join("", crStateArr) + "| using cr " + intArrString(cr, ","));

        String[] crState = Arrays.copyOf(crStateArr, crStateArr.length);

        if (cr.length == 1 && cr[0] == 0 && !allElemAreVal(crState, crossedOut, 0, crState.length)) { // Row of 0
            System.out.println("Row of 0");
            Arrays.fill(crState, crossedOut);
            return crState;
        } else if (cr.length == 1 && cr[0] * 2 > crState.length && !allElemAreVal(crState, filledIn, crState.length - cr[0], cr[0])) { // row of one big number
            System.out.println("Row of big num");
            System.out.println("cr is " + intArrString(cr, ",") + " and state length is " + crState.length + " condition is " + allElemAreVal(crState, filledIn, crState.length - cr[0], cr[0]));
            int num = cr[0];
            for (int i = crState.length - num; i < num; i++) {
                crState[i] = filledIn;
            }
            return crState;
        } else if (sumIntArr(cr) + cr.length - 1 == crState.length) { // fully filled out cr
            System.out.println("Full CR Row");
            int index = 0;
            for (Integer i : cr) {
                for (int j = index; j < index + i; j++) {
                    crState[j] = filledIn;
                }
                if (index + i < crState.length) {
                    crState[index + i] = crossedOut;
                }
                index += i + 1;
            }
            return crState;
        } else if (cr.length == 1 && countValInArr(crState, filledIn, 0, crState.length) > 0 && countValInArr(crState, filledIn, 0, crState.length) < cr[0]) { // row of one number partially filled in
            System.out.println("Row of one number partially filled in");
            // first add crossedOut before and after the filledIn part where appropriate,
            // then fill out the filledIn area where appropriate
            int leftFilledIndex = 0; // index of first filledIn square from left
            int rightFilledIndex = 0; // index of first filledIn square from right
            int leftCrossedIndex = -1; // index of first crossedOut square from left
            int rightCrossedIndex = crState.length; // index of first crossedOut square from right
            for (int i = 0; i < crState.length; i++) {
                if (crState[i].equals(filledIn)) {
                    leftFilledIndex = i;
                    break;
                }
            }
            for (int i = crState.length - 1; i >= 0; i--) {
                if (crState[i].equals(filledIn)) {
                    rightFilledIndex = i;
                    break;
                }
            }
            int existingWidth = rightFilledIndex - leftFilledIndex + 1;
            for (int i = leftFilledIndex - 1; i >= (Math.max(leftFilledIndex - existingWidth, 0)); i--) {
                if (crState[i].equals(crossedOut)) {
                    leftCrossedIndex = i;
                    break;
                }
            }
            for (int i = rightFilledIndex + 1; i < (Math.min(rightFilledIndex + existingWidth + 1, crState.length)); i++) {
                if (crState[i].equals(crossedOut)) {
                    rightCrossedIndex = i;
                    break;
                }
            }

            // fill in the crosses
            for (int i = 0; i <= leftCrossedIndex; i++) {
                crState[i] = crossedOut;
            }
            for (int i = rightCrossedIndex; i < crState.length; i++) {
                crState[i] = crossedOut;
            }

            // fill in the filledIn if necessary. check left, then right, to see if we need to fill in
            // check space on left
            if (leftFilledIndex - leftCrossedIndex + 1 < cr[0] - existingWidth) {
                for (int i = rightFilledIndex + 1; i <= leftCrossedIndex + cr[0]; i++) {
                    crState[i] = filledIn;
                }
            }
            // check space on right
            if (rightCrossedIndex - rightFilledIndex - 1 < cr[0] - existingWidth) {
                for (int i = leftFilledIndex - 1; i >= rightCrossedIndex - cr[0]; i--) {
                    crState[i] = filledIn;
                }
            }
            return crState;
        } else {
            System.out.println("Base case");
            /*
            Base case: Find leftmost/topmost possible config,
            Find rightmost/bottommost possible config,
            Identify what Os and Xs are shared between the two (that are within the same cr numbers (O) or between the same cr numbers (X))
            Populate the crState with those shared values
             */
            String[] firstMost = new String[crState.length];
            String[] lastMost = new String[crState.length];
            Arrays.fill(firstMost, crossedOut);
            Arrays.fill(lastMost, crossedOut);
            int fmIndex = 0; // firstMost Index
            int lmIndex = crState.length; // lastMost Index
            boolean filledInt;

            // for each num in cr, try adding to firstMost
            for (Integer i : cr) { // for each num in cr, try to add that to firstMost
                filledInt = false;

                // Find first index in crState where we can add i to firstMost, starting at fmIndex
                while (fmIndex < crState.length) {
//                    System.out.println("fmIndex is " + fmIndex + " and arrcontains returns " + arrContains(crState, crossedOut, fmIndex, fmIndex + i));
                    if (!arrContains(crState, crossedOut, fmIndex, fmIndex + i) && (fmIndex + i >= crState.length || !crState[fmIndex + i].equals(filledIn))) {
                        System.out.println("found place to add to firstMost");
                        // If the consecutive points starting at fmIndex does not have crossedOut and the point after the consecutive line is not filledIn
                        // Then add to firstMost
                        for (int j = fmIndex; j < fmIndex + i; j++) {
                            firstMost[j] = filledIn;
                        }
                        if (fmIndex + i < firstMost.length) { firstMost[fmIndex + i] = crossedOut; }
                        fmIndex += (i + 1); //increment fmIndex by length of consecutive line plus 1 for the crossed out block at end
                        filledInt = true;
                        break; // move to next Integer in cr
                    } else {
                        if ( arrContains(crState, crossedOut, fmIndex, fmIndex + i) ) {
                            System.out.println("found cross in middle");
                            // Found crossed out in middle
                            // move up so that fmIndex is one after the crossed out
                            int newIndex = findFirst(crState, crossedOut, fmIndex, fmIndex + i);
                            if (newIndex == -1) {
                                System.out.println("arr contains cross but couldn't find cross index, firstmost");
                                System.exit(1);
                            }
                            fmIndex = newIndex + 1;
                        } else {
                            System.out.println("move up one");
                            // move up one
                            // covers "filledin at end" and default case
//                            firstMost[fmIndex] = crossedOut;
                            fmIndex++;
                        }
                    }
                }
                if (fmIndex >= crState.length && !filledInt) {
                    // if fmIndex at end of array and we didn't fill anything in this loop,
                    // Could not create firstmost, which would mean that crState is unsolvable.
                    System.out.println("Could not create firstMost for Integer " + i);
                    printArr(firstMost);
                    System.exit(0);
                }
            }

            // for each num in cr backwards, try adding to lastMost
            for (int i = cr.length - 1; i >= 0; i--) { // for each num in cr from the end, try to add that to lastMost
                int num = cr[i];
                filledInt = false;
                // Unlike firstMost, lastMost represents one after the rightmost index of the consecutive points we fill in
                // We move through the array right to left, or back to front

                // Find last index in crState where we can add i to lastMost, starting at lmIndex
                while (lmIndex >= 0) {
                    if (!arrContains(crState, crossedOut, lmIndex - num, lmIndex) && (lmIndex - num <= 0 || !crState[lmIndex - num - 1].equals(filledIn))) {
                        // If no crossedOut in consecutive points starting at lmIndex and point before lmIndex is not filledIn
                        // Then add to lastMost
                        for (int j = lmIndex - 1; j > lmIndex - num - 1; j--) {
                            lastMost[j] = filledIn;
                        }
                        if ( lmIndex - num - 1 >= 0) {
                            lastMost[lmIndex - num - 1] = crossedOut;
                        }
                        lmIndex -= (num + 1);
                        filledInt = true;
                        break;
                    } else {
                        if ( arrContains(crState, crossedOut, lmIndex - num, lmIndex) ) {
                            //Found crossed out in middle
                            int newIndex = findFirst(crState, crossedOut, lmIndex - num, lmIndex);
                            if (newIndex == -1) {
                                System.out.println("arr contains cross but couldn't find cross index, lastmost");
                                System.exit(1);
                            }
                            lmIndex = newIndex;
                        } else {
                            // move down one
                            // covers "filledin at beginning" and default case
//                            lastMost[lmIndex] = crossedOut;
                            lmIndex--;
                        }
                    }
                }
                if (lmIndex <= 0 && !filledInt) {
                    // fmIndex reached start of array and we didn't fill anything in this loop
                    // Couldn't create lastmost, which would mean that crState is unsolvable
                    System.out.println("Could not create lastMost for Integer " + num);
                    printArr(lastMost);
                    System.exit(1);
                }
            }

            System.out.println("Firstmost and lastmost are ");
            printArr(firstMost);
            printArr(lastMost);

            fmIndex = 0; // Repurpose var to loop through firstMost from the beginning
            lmIndex = 0; // Repurpose var to loop through lastMost from the beginning
            if (!firstMost[fmIndex].equals(lastMost[lmIndex])) {
                // if they don't point to the same string, then fmIndex points to filledIn and lmIndex points to crossedOut
                // because I think it's impossible for the opposite to be true, as far as I can tell
                while (lmIndex < lastMost.length) {
                    if (lastMost[lmIndex].equals(filledIn)) {
                        break;
                    }
                    lmIndex++;
                }
            }

            /*
               loop through firstmost and lastmost, find consecutive groups with overlapping letters, and populate those in crState
               e.g. if firstmost and lastmost are OOOXX and XXOOO respectively, they overlap on index 2, so crState[2] = O
               but on the other hand, if firstmost and lastmost are OXOXX and XXOXO,
               they have no overlap, even though index 2 is O for both,
               because index 2 in firstmost is the second group of O,
               and index 2 in lastmost is the first group of O
             */

            while (fmIndex < firstMost.length && lmIndex < lastMost.length) {
                if (!firstMost[fmIndex].equals(lastMost[lmIndex])) {
                    System.out.println("fm and lm index point to different values when comparing overlaps");
                    System.exit(1);
                }

                // Indices of the first element after fm/lm index that is different from fm/lm index
                // Or end of firstmost/lastmost array
                int fmEndIndex = fmIndex;
                int lmEndIndex = lmIndex; // the index of the first
                while (fmEndIndex < firstMost.length) {
                    if (!firstMost[fmIndex].equals(firstMost[fmEndIndex])) {
                        break;
                    }
                    fmEndIndex++;
                }
                while (lmEndIndex < lastMost.length) {
                    if (!lastMost[lmIndex].equals(lastMost[lmEndIndex])) {
                        break;
                    }
                    lmEndIndex++;
                }

                // compare fmEndIndex to lmIndex
                if (lmIndex < fmEndIndex) {
                    for (int i = lmIndex; i < fmEndIndex; i++) {
                        crState[i] = firstMost[i];
                    }
                }
                fmIndex = fmEndIndex;
                lmIndex = lmEndIndex;
            }
        }


        return (Arrays.compare(crState, crStateArr) == 0) ? new String[0] : crState;
    }

    /**
     * Solves this nonogram.
     * Prints out the nonogram as it solves it, one step at a time
     */
    public void solveNonogram() {
        Set<Integer> unsolvedCols = new HashSet<Integer>(); // a set of columns that are yet unsolved
        Set<Integer> unsolvedRows = new HashSet<Integer>(); // a set of rows that are yet unsolved
        boolean colOrRowSolved;
        for (Integer i : IntStream.range(0, width).toArray()) {
            unsolvedCols.add(i);
        }
        for (Integer i : IntStream.range(0, height).toArray()) {
            unsolvedRows.add(i);
        }

        try (Scanner scan = new Scanner(System.in)) {
            // while there are elements in unsolvedCols or unsolvedRows
            while (!unsolvedCols.isEmpty() || !unsolvedRows.isEmpty()) {
                colOrRowSolved = false;
                Set<Integer> colsToRemove = new HashSet<Integer>();
                Set<Integer> rowsToRemove = new HashSet<Integer>();
                System.out.println("unsolved cols and rows are");
                printSet(unsolvedCols);
                printSet(unsolvedRows);


                // loop through all unsolved cols and try to solve them
                for (Integer i : unsolvedCols) {
                    String[] currCol = getCol(i);
                    if (colOrRowIsFullySolved(cols[i], currCol)) { // Break if this col is already solved
                        colsToRemove.add(i);
                        break;
                    }
                    String[] newCol = solveColOrRow(cols[i], currCol);

                    if (newCol.length > 0) { // solved the column!
                        colOrRowSolved = true;
                        setCol(i, newCol);
                        if (colOrRowIsFullySolved(cols[i], newCol)) { colsToRemove.add(i); }
                        System.out.println("\r\n\r\n\r\n~~~~~~~~~~~~\r\n\r\n\r\n");
                        System.out.println(toString(true));
                        scan.nextLine(); //TODO remove when I use time-based printing
                    }
                }

                // loop through all unsolved rows and try to solve them
                for (Integer i : unsolvedRows) {
                    String[] currRow = state[i];
                    if (colOrRowIsFullySolved(rows[i], currRow)) { // Break if this row is already solved
                        rowsToRemove.add(i);
                        break;
                    }
                    String[] newRow = solveColOrRow(rows[i], currRow);

                    if (newRow.length > 0) {
                        colOrRowSolved = true;
                        for (int j = 0; j < newRow.length; j++) { currRow[j] = newRow[j]; }
                        if(colOrRowIsFullySolved(rows[i], newRow)) { rowsToRemove.add(i); }
                        System.out.println("\r\n\r\n\r\n~~~~~~~~~~~~\r\n\r\n\r\n");
                        System.out.println(toString(true));
                        scan.nextLine(); //TODO remove when I use time-based printing
                    }
                }

                unsolvedCols.removeAll(colsToRemove);
                unsolvedRows.removeAll(rowsToRemove);

                if (!colOrRowSolved) {
                    System.out.println("Nothing solved in this loop.");
                    System.exit(1);
                }
            }
            System.out.println("\r\n\r\n\r\nNonogram fully solved!\r\n\r\n\r\n");
            System.out.println(toString(false));
        }
    }

    /**
     * Checks if the Column or Row state passed in is fully solved/complete as defined by the column or row
     * @param cr the numbers that define which elements in crState should be filled in
     * @param crState the string values that should be verified against cr
     * @return True if crState is solved as defined by cr, False otherwise
     */
    private static boolean colOrRowIsFullySolved(int[] cr, String[] crState) {
        // Loop over cr
        // For each number,
        // Check if there are that many consecutive filled in squares in crState
        // We don't care about crossed out squares being explicitly crossed or not

        int stateIndex = 0;
        int fisIndex = 0; //filled in square index
        for (int i = 0; i < cr.length; i++) { // for each cr number
            // find first filled in square
            while (fisIndex < crState.length && !crState[fisIndex].equals(filledIn)) { fisIndex++; }
            if (fisIndex >= crState.length) { return false; } // no filled in squares found

            // check for consecutive line of filled in squares, by returning false if any are not filled in
            for (int j = fisIndex; j < fisIndex + cr[i]; j++) {
                if (!crState[j].equals(filledIn)) { return false; }
            }
            fisIndex += cr[i];
            // check that after consecutive line, there is no filled in square, or it is end of crState
            if (fisIndex < crState.length && crState[fisIndex].equals(filledIn)) { return false; }
        }

        return true;
    }

    // Returns true if the arr array contains the val string between startIndex inclusive and endIndex exclusive, or else false
    private static boolean arrContains(String[] arr, String val, int startIndex, int endIndex) {
        if (startIndex < 0 || endIndex > arr.length) {
            return false;
        }
        for (int i = startIndex; i < endIndex; i++) {
            if(arr[i].equals(val)) {
                return true;
            }
        }
        return false;
    }

    // Returns the index of the first time we find val string in arr array between startIndex inclusive and endIndex exclusive, or else -1
    private static int findFirst(String[] arr, String val, int startIndex, int endIndex) {
        if (startIndex < 0 || endIndex > arr.length) {
            return -1;
        }
        for (int i = startIndex; i < endIndex; i++) {
            if(arr[i].equals(val)) {
                return i;
            }
        }
        return -1;
    }

    public static void printArr(Object[] arr) {
        for (Object o : arr) {
            System.out.print(o);
        }
        System.out.println("");
    }

    // TODO REMOVE BELOW 2 WHEN DONE TESTING
    private static String intArrString(int[] arr, String delim) {
        String s = "";
        for (int i = 0; i < arr.length; i++) {
            s += arr[i];
            if (i != arr.length - 1) {
                s += delim;
            }
        }
        return s;
    }

    private static void printSet(Set<Integer> s) {
        for (Integer o : s) {
            System.out.print(o + " ");
        }
        System.out.println("");
    }

    private static int sumIntArr(int[] arr) {
        int sum = 0;
        for (Integer i : arr) {
            sum += i;
        }
        return sum;
    }

    // Returns the colNum column of state as a string array
    public String[] getCol(int colNum) {
        int index = 0;
        String[] col = new String[height];
        for (int row = 0; row < state.length; row++) {
            col[index] = state[row][colNum];
            index++;
        }
        return col;
    }

    // Sets the colNum column with the values found in col
    public void setCol(int colNum, String[] col) {
        if (col.length != height) {
            System.out.println("col array doesn't match height");
            System.exit(1);
        }
        for (int row = 0; row < state.length; row++) {
            state[row][colNum] = col[row];
        }
    }

    /**
     * Verifies that all elements in arr between beginIndex inclusive and endIndex exclusive are val
     * @param arr Object array of elements
     * @param val An object that is the same type as arr
     * @param beginIndex Index to begin checking, inclusive
     * @param endIndex Index to finish checking, exclusive
     * @return true if all elements between beginIndex and endIndex are val, false otherwise
     */
    private static boolean allElemAreVal(Object[] arr, Object val, int beginIndex, int endIndex) {
        for (int i = beginIndex; i < endIndex; i++) {
            if (!arr[i].equals(val)) {
                return false;
            }
        }
        return true;
    }

    private static int countValInArr(Object[] arr, Object val, int beginIndex, int endIndex) {
        int sum = 0;
        for (int i = 0; i < arr.length; i++) {
            if (arr[i].equals(val)) {
                sum += 1;
            }
        }
        return sum;
    }
}
