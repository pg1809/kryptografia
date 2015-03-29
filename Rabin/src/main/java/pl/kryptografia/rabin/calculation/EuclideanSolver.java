package pl.kryptografia.rabin.calculation;

public class EuclideanSolver {

    private EuclideanSolver() {
    }

    public static EuclideanSolver getInstance() {
        return EuclideanSolverHolder.INSTANCE;
    }

    private static class EuclideanSolverHolder {

        private static final EuclideanSolver INSTANCE = new EuclideanSolver();
    }
}
