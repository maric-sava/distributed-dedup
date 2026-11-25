package rs.ac.uns.ftn.dedup.generator;

import java.util.Random;

/**
 * Generates random IDs with equal probability
 * across the configured range of possible values.
 */
public class UniformDistribution implements IdSource {

    private final int distinct;
    private final Random random;

    public UniformDistribution(int distinct, long seed) {
        this.distinct = distinct;
        this.random = new Random(seed); // same seed -> same sequence
    }

    @Override
    public int nextId() {
        return random.nextInt(distinct);
    }

}
