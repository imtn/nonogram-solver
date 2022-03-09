import java.io.File;

public class NonogramSolver {
    public static void main(String[] args) {
        Nonogram n = new Nonogram(new File("nonograms/pokeball.nngm"));
        n.solveNonogram();

//        int[] cr = {2, 2};
//        String[] crState = {" ", " ", " ", " ", " ", " ", " ", " ", " ", " "};
//        System.out.println(Nonogram.isSolvable(cr, crState));
    }
}
