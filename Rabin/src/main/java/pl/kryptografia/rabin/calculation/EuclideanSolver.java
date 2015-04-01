package pl.kryptografia.rabin.calculation;

import java.util.Stack;
import pl.kryptografia.rabin.bignum.BigNum;

public class EuclideanSolver {

    /**
     * Private constructor for singleton pattern purpose.
     */
    private EuclideanSolver() {
    }

    /**
     * For given relatively prime big numbers a and b returns a pair (s, t) such
     * that sa + tb = 1.
     *
     * @param a A big number relatively prime to b.
     * @param b A big number relatively prime to a.
     * @return The solution (s, t) of diofantic equation sa + tb = 1.
     */
    public Pair solve(BigNum a, BigNum b) {
        // if a or b is equal to 1 the solution is trivial
        if (a.equals(BigNum.ONE)) {
            return new Pair(new BigNum(BigNum.ONE), new BigNum(BigNum.ZERO));
        } else if (b.equals(BigNum.ONE)) {
            return new Pair(new BigNum(BigNum.ZERO), new BigNum(BigNum.ONE));
        }

        // we put consecutive partial results of Euclidean algorithm onto the 
        // stack
        Stack<Pair> stack = new Stack<>();

        // quotient
        BigNum x;
        // reminder
        BigNum r;

//        do {
//            
//        } while (r.absGreaterThan(BigNum.ONE));
        return new Pair(a, b);
    }

    public static EuclideanSolver getInstance() {
        return EuclideanSolverHolder.INSTANCE;
    }

    private static class EuclideanSolverHolder {

        private static final EuclideanSolver INSTANCE = new EuclideanSolver();
    }
}
