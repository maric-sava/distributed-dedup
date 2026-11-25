package rs.ac.uns.ftn.dedup.local.hash;

/**
 * Maps records to hash-based buckets using a configurable seed.
 * Different seeds allow repartitioning at successive recursion levels.
 */
public final class LineHash {

    private LineHash() {}

    /**
     * Returns a bucket index between 0 and buckets - 1.
     * Identical records with the same seed and bucket count
     * are always assigned to the same bucket.
     */
    public static int bucket(String line, int seed, int buckets) {
        // Step 1: Combine Java's string hash with the seed.
        // The golden ratio constant helps mix different seed values.
        int h = line.hashCode() ^ (seed * 0x9E3779B9); // mix the seed in

        // Step 2: Improve hash distribution using the MurmurHash3
        // final mixing function (fmix32).
        h ^= h >>> 16;
        h *= 0x85EBCA6B;
        h ^= h >>> 13;
        h *= 0xC2B2AE35;
        h ^= h >>> 16;

        // Step 3: Map the hash to a valid, non-negative bucket index.
        return Math.floorMod(h, buckets); // floorMod, because h can be negative
    }
}
