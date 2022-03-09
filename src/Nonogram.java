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

        // TODO add case for if there is a filledIn next to a crossedOut but it isn't fully solved.
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
            if (colOrRowIsFullySolved(cr, crState) && arrContains(crState, " ", 0, crState.length)) { setBlankToCrossedOut(crState); }
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
            leftCrossedIndex = rightFilledIndex - cr[0];
            rightCrossedIndex = leftFilledIndex + cr[0];
            for (int i = leftFilledIndex - 1; i >= (Math.max(rightFilledIndex - existingWidth, 0)); i--) {
                if (crState[i].equals(crossedOut)) {
                    leftCrossedIndex = i;
                    break;
                }
            }
            for (int i = rightFilledIndex + 1; i < (Math.min(leftFilledIndex + existingWidth + 1, crState.length)); i++) {
                if (crState[i].equals(crossedOut)) {
                    rightCrossedIndex = i;
                    break;
                }
            }

            System.out.println("Existing width is " + existingWidth);
            System.out.println("Left and right filled indices are " + leftFilledIndex + " and " + rightFilledIndex);
            System.out.println("Left and right crossed indices are " + leftCrossedIndex + " and " + rightCrossedIndex);

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
            if (colOrRowIsFullySolved(cr, crState) && arrContains(crState, " ", 0, crState.length)) { setBlankToCrossedOut(crState); }
            return crState;
        } else {
            System.out.println("Base case");
            /*
            Base case: Loop through each unsolved index in crState
            For each one, check if it must be X or it must be O
                It must be X if putting O makes it unsolvable
                It must be O if putting X makes it unsolvable
            For reference, X is crossedOut, O (capital letter O) is filledIn
             */
            for (int i = 0; i < crState.length; i++) {
                if (crState[i].equals(" ")) {
                    // Check must be X
                    crState[i] = filledIn;
                    if (!isSolvable(cr, crState)) {
                        crState[i] = crossedOut;
                        continue;
                    }
                    // Check must be O
                    crState[i] = crossedOut;
                    if (!isSolvable(cr, crState)) {
                        crState[i] = filledIn;
                        continue;
                    }
                    crState[i] = " ";
                }
            }
            // Return crState if it is different from crStateArr, otherwise return empty array
            if (colOrRowIsFullySolved(cr, crState) && arrContains(crState, " ", 0, crState.length)) { setBlankToCrossedOut(crState); }
            return (Arrays.compare(crState, crStateArr) == 0) ? new String[0] : crState;
        }
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

    private static int getArrValOrDefault (int[] arr, int ind, int def) {
        if (ind >= 0 && ind < arr.length) {
            return arr[ind];
        } else {
            return def;
        }
    }

    // Crosses out everything that's not filledIn or crossedOut. Useful for finishing a col or row after all the filledIn is added
    private static void setBlankToCrossedOut(String[] crState) {
        for (int i = 0; i < crState.length; i++) {
            if (!crState[i].equals(filledIn) && !crState[i].equals(crossedOut)) {
                crState[i] = crossedOut;
            }
        }
    }

    /**
     * Returns whether or not crState can be solved, meaning it has at least one solution, as defined by cr
     * Does not modify cr or crState
     * @param cr int array describing the row or col
     * @param crState string array describing the current known values
     * @return true if we can make a solution from crState, false otherwise.
     */
    public static boolean isSolvable(int[] cr, String[] crState) {
        int[] crPosition = new int[cr.length]; // each int in this array is the leftmost index of where the number in same index in cr starts in crState, meaning crPosition values is index for crState
        int firstOpenIndex = 0; // index of first place in crState that is open
        while (firstOpenIndex < crState.length && crState[firstOpenIndex].equals(crossedOut)) { firstOpenIndex++; }

        // Loop through crState from beginning, adding cr as soon as possible.
        //      If some cr cannot be added, unsolvable, return false
        // After adding cr, loop through cr backwards
        //      For each cr, if there is a filledIn after the end of last known cr,
        //      We move the last cr up to include that filled In (placing the cr as late as possible,
        //      And move all previous cr up as needed

        for (int i = 0; i < cr.length; i++) {
            if (firstOpenIndex >= crState.length || firstOpenIndex + cr[i] > crState.length) {
                System.out.println("Could not find space for current i, unsolvable");
                return false;
            }
            // for each cr, find the best place to add it to crState

            // if no crossedOut in current possible area and filledIn is not right after current possible area,
            // then set crPosition for this i to firstOpenIndex and move firstOpenIndex past possible area (including crossedOut at end if applicable)
            if (!arrContains(crState, crossedOut, firstOpenIndex, firstOpenIndex + cr[i]) && (firstOpenIndex + cr[i] >= crState.length || !crState[firstOpenIndex + cr[i]].equals(filledIn))) {
                crPosition[i] = firstOpenIndex;
                firstOpenIndex += (cr[i] + 1);
            } else if (arrContains(crState, crossedOut, firstOpenIndex, firstOpenIndex + i)) {
                // if there is crossedOut in current possible area, move firstOpenIndex past the crossedOut
                firstOpenIndex = findFirst(crState, crossedOut, firstOpenIndex, firstOpenIndex + i) + 1;
                i--; // decrement so we can try to place this cr again
            } else {
                // filledIn at end, most up one
                firstOpenIndex++;
                i--; // decrement so we can try to place this cr again
            }
        } // Finished making crPosition with earliest possible places

        System.out.println("crPosition are " + intArrString(crPosition, ","));

        while (arrContains(crState, filledIn, crPosition[crPosition.length - 1] + cr[crPosition.length - 1], crState.length)) { // While there is filledIn after last filledIn defined by crPosition,
            int filledInToMoveToIndex = findFirst(crState, filledIn, crPosition[crPosition.length - 1] + cr[crPosition.length - 1], crState.length); // get the filledIn from previous step to move to
            int currCRIndex = crPosition.length - 1; // crPosition[] index we're currently moving
            System.out.println("FilledIn at " + filledInToMoveToIndex + " when searching between " + (crPosition[crPosition.length - 1] + cr[crPosition.length - 1]) + " and " + crState.length);
            System.out.println("Currently moving cr at " + crPosition[currCRIndex] + " to cover filledIn at " + filledInToMoveToIndex);
            while (filledInToMoveToIndex >= 0 && filledInToMoveToIndex < crState.length) { // while the filledIn index is within crState,
                int newFilledInIndex = findFirst(crState, filledIn, crPosition[currCRIndex], crPosition[currCRIndex] + cr[currCRIndex]); // index of first filledIn already covered by currCRIndex, or -1 if none
                System.out.println("Current cr has filledIn at " + newFilledInIndex);

                // To find the new end index,
                // We get the firstFilledIn,
                // Move past the consecutive filledIn group if any,
                // Then move it to the latest index in crState where we think we can still put this cr
                // Once newEndIndex is calculated, the start index is just cr amount in front of it
                int newEndIndex = filledInToMoveToIndex;
                while (newEndIndex < crState.length && crState[newEndIndex].equals(filledIn)) { newEndIndex++; }
                while (
                        newEndIndex < crState.length // newEndIndex is not at end of array
                                && newEndIndex < filledInToMoveToIndex + cr[currCRIndex] // newEndIndex is within current consecutive points
                                && !crState[newEndIndex].equals(crossedOut) // newEndIndex is not crossedOut
                                && (newEndIndex + 1 >= crState.length || !crState[newEndIndex + 1].equals(filledIn)) // if newEndIndex + 1 within array, then newEndIndex + 1 is not filledIn
                ) {
                    newEndIndex++;
                } // finished getting the newEndIndex
                System.out.println("Checking newEndIndex conditions: not end of array? " + (newEndIndex-1 < crState.length) + " - within current consecutive points? " + (newEndIndex-1 < crPosition[currCRIndex] + cr[currCRIndex]) + " - is crossed out? " + !crState[newEndIndex-1].equals(crossedOut) + " - if next exists, it's not filledIn? " + (newEndIndex >= crState.length || !crState[newEndIndex].equals(filledIn)));
                System.out.println("New end index is " + newEndIndex);
                if (arrContains(crState, crossedOut, newEndIndex - cr[currCRIndex], newEndIndex)) {
                    // unable to add cr to this space, unsolvable
                    System.out.println("Unable to solve, crossedOut found at " + findFirst(crState, crossedOut, newEndIndex - cr[currCRIndex], newEndIndex));
                    return false;
                } else {
                    crPosition[currCRIndex] = newEndIndex - cr[currCRIndex]; // move up position
                    currCRIndex--; // move up currCRIndex
                    filledInToMoveToIndex = newFilledInIndex; // update new FilledIn that we want to move currCRIndex to, if this is not valid
                    if (currCRIndex < 0 && filledInToMoveToIndex > -1) {
                        // everything moved up but there was still something to cover, so unsolvable
                        return false;
                    }
                    if (currCRIndex >= 0) {
                        System.out.println("cr moved up to " + crPosition[currCRIndex + 1] + ", preparing to see if we can move cr " + cr[currCRIndex] + " at " + crPosition[currCRIndex] + " to include filledIn " + filledInToMoveToIndex);
                    } else {
                        System.out.println("currCRIndex is " + currCRIndex + ", already moved everything up, filledInIndex is " + filledInToMoveToIndex);
                    }
                }
            }
        }
        // Check that crPosition and crState define a valid crState
        Set<Integer> filledInSet = new HashSet<Integer>();
        for (int i = 0; i < cr.length; i++) {
            for (int j = crPosition[i]; j < crPosition[i] + cr[i]; j++) {
                filledInSet.add(j);
            }
        }
        for (int i = 0; i < crState.length; i++) {
            if (filledInSet.contains(i) && crState[i].equals(crossedOut)) {
                // if i should be filledIn but it is crossedOut
                return false;
            } else if (!filledInSet.contains(i) && crState[i].equals(filledIn)) {
                // if i should be not be filledIn but it is
                return false;
            }
        }
        return true;
    }
}
