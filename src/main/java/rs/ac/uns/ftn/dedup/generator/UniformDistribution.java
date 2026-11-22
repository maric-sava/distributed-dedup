package rs.ac.uns.ftn.dedup.generator;

import java.util.Random;

public class UniformDistribution {

    private final int distinct;
    private final Random random;

    public UniformDistribution(int distinct, long seed) {
        this.distinct = distinct;
        this.random = new Random(seed); // same seed -> same sequence
    }

    public int nextId() {
        return random.nextInt(distinct);
    }

}
