import java.io.File;

public class NonogramSolver {
    public static void main(String[] args) {
        Nonogram n = new Nonogram(new File("nonograms/poochyena.nngm"));
        n.solveNonogram();
    }
}
