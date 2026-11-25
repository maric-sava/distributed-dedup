package rs.ac.uns.ftn.dedup.generator;

/**
 * Defines dataset generation parameters, including the total
 * number of records, the number of possible distinct values,
 * the value distribution and the seed for reproducibility.
 */
public record GeneratorConfig(long rows, int distinct, Distribution distribution, long seed) {

    public GeneratorConfig {
        if (rows < 0) {
            throw new IllegalArgumentException("rows must be  >= 0, got " + rows);
        }
        if (distinct < 1) {
            throw new IllegalArgumentException("distinct must be >= 1, got " + distinct);
        }
        if (distribution == null) {
            throw new IllegalArgumentException("distribution must not be null");
        }
    }
}
