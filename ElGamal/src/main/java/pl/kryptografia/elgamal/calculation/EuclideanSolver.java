package pl.kryptografia.elgamal.calculation;

import java.util.Stack;
import pl.kryptografia.elgamal.bignum.BigNum;

public class EuclideanSolver {

    /**
     * Private constructor for singleton pattern purpose.
     */
    private EuclideanSolver() {
    }

    /**
     * Calculates inverse of x modulo given number.
     * 
     * Numbers should be coprime.
     * 
     * @param x Number to find the inverse of.
     * @param modulus Modulus.
     * @return x^(-1) mod modulus
     */
    public BigNum inverseModulo(BigNum x, BigNum modulus) {
        BigNum result = solve(x, modulus).first;
        result.modulo(modulus);
        return result;
    }
    
    /**
     * For given relatively prime big numbers a and b returns a pair (s, t) such
     * that sa + tb = 1.
     *
     * @param A A big number relatively prime to b.
     * @param B A big number relatively prime to a.
     * @return The solution (s, t) of diofantic equation sa + tb = 1.
     */
    public Pair solve(BigNum A, BigNum B) {
        // get copies not to modify original parameters
        BigNum a = new BigNum(A);
        BigNum b = new BigNum(B);

        // if a or b is equal to 1 the solution is trivial
        if (A.equals(BigNum.ONE)) {
            return new Pair(new BigNum(BigNum.ONE), new BigNum(BigNum.ZERO));
        } else if (B.equals(BigNum.ONE)) {
            return new Pair(new BigNum(BigNum.ZERO), new BigNum(BigNum.ONE));
        }

        // we put consecutive partial results of Euclidean algorithm onto the 
        // stack
        Stack<Pair> stack = new Stack<>();

        // quotient
        BigNum x;
        // remainder
        BigNum r;

        do {
            x = new BigNum(a);
            x.divide(b);

            r = new BigNum(a);
            r.modulo(b);

            if (r.absGreaterThan(BigNum.ONE)) {
                // a and x are going to be modified so it is crucial to put
                // their copies onto the stack
                stack.push(new Pair(new BigNum(a), new BigNum(x)));

                a = new BigNum(b);
                b = new BigNum(r);
            }
        } while (r.absGreaterThan(BigNum.ONE));

        BigNum w = new BigNum(BigNum.ONE);

        while (!stack.isEmpty()) {
            BigNum s = stack.peek().second;
            stack.pop();

            BigNum temp = new BigNum(w);

            x.setSign(-x.getSign());

            w = new BigNum(x);

            x.multiply(s);
            x.subtract(temp);
        }

        x.setSign(-x.getSign());
        return new Pair(w, x);
    }

    public static EuclideanSolver getInstance() {
        return EuclideanSolverHolder.INSTANCE;
    }

    private static class EuclideanSolverHolder {

        private static final EuclideanSolver INSTANCE = new EuclideanSolver();
    }
}
