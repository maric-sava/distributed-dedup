package rs.ac.uns.ftn.dedup.generator;

import java.util.Arrays;
import java.util.Random;

/**
 * Generates IDs according to a Zipf distribution.
 * Uses cumulative probabilities and binary search
 * for inverse transform sampling.
 * Higher exponents produce greater data skew.
 */
public class ZipfDistribution implements IdSource {

    private final Random random;
    private final double[] cumulative; // cumulative[i] = probability that id <= i

    public ZipfDistribution(int distinct, long seed, double exponent) {
        this.random = new Random(seed);
        this.cumulative = new double[distinct];
        double sum = 0;
        for (int i = 0; i < distinct; i++) {
            sum += 1.0 / Math.pow(i + 1, exponent); // weight of id i
            cumulative[i] = sum;
        }
        for (int i = 0; i < distinct; i++) {
            cumulative[i] /= sum; // now the last value is 1.0
        }
    }

    @Override
    public int nextId() {
        double u = random.nextDouble(); // a point between 0 and 1
        int index = Arrays.binarySearch(cumulative, u);
        if (index < 0) {
            index = -index - 1; // not found: take the first id whose cumulative value is bigger
        }
        return Math.min(index, cumulative.length - 1);
    }
}