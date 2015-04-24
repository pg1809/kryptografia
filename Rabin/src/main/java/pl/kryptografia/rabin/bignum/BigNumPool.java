package pl.kryptografia.rabin.bignum;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Pool of BigNums used to limit creation of new objects and allocating memory
 * for them multiple times.
 */
public class BigNumPool {

    private final static Logger logger = Logger.getLogger(BigNumPool.class.getName());

    private final static int INITIAL_POOL_SIZE = 1200;

    private final List<Integer> spacePointers = new ArrayList<>();

    private final List<BigNum> pool = new ArrayList<>(INITIAL_POOL_SIZE);

    private int next = 0;

    private BigNumPool() {
        for (int i = 0; i < INITIAL_POOL_SIZE; ++i) {
            pool.add(new BigNum());
        }
    }

    public BigNum get() {
        if (next == pool.size()) {
            System.out.println("Resizing pool to: " + 2 * pool.size());
            for (int i = 0; i < next; ++i) {
                pool.add(new BigNum());
            }
        }
        
        return pool.get(next++);
    }

    public void open() {
        spacePointers.add(next);
    }

    public void close() {
        if (spacePointers.isEmpty()) {
            logger.info("Trying to close an empty pool");
            return;
        }

        next = spacePointers.get(spacePointers.size() - 1);
        spacePointers.remove(spacePointers.size() - 1);
    }

    public static BigNumPool getInstance() {
        return BigNumPoolHolder.INSTANCE;
    }

    private static class BigNumPoolHolder {

        private static final BigNumPool INSTANCE = new BigNumPool();
    }
}
