package rs.ac.uns.ftn.dedup.local.hash;

/** Decides which bucket a line goes to. The seed lets each recursion level split differently. */
public final class LineHash {

    private LineHash() {}

    /**
     * Returns a bucket number from 0 to buckets-1.
     * The same line with the same seed always gives the same bucket.
     */
    public static int bucket(String line, int seed, int buckets) {
        // Step 1: start from Java's own hash of the line, and mix the seed in.
        // 0x9E3779B9 is the "golden ratio" constant, commonly used to mix in a seed.
        int h = line.hashCode() ^ (seed * 0x9E3779B9); // mix the seed in

        // Step 2: scramble the bits so that similar lines (user_1, user_2, ...) spread out evenly.
        // These 5 lines are the standard final mixing step of MurmurHash3 (called "fmix32").
        h ^= h >>> 16;
        h *= 0x85EBCA6B;
        h ^= h >>> 13;
        h *= 0xC2B2AE35;
        h ^= h >>> 16;

        // Step 3: turn the number into a bucket number.
        // floorMod instead of %, because h can be negative and % would give a negative result.
        return Math.floorMod(h, buckets); // floorMod, because h can be negative
    }
}
