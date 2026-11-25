package rs.ac.uns.ftn.dedup.core;

/**
 * Provides heuristic estimates of the memory footprint of records
 * stored in list-based and hash-based collections.
 * These estimates do not represent exact JVM memory consumption.
 */
public final class MemoryModel {

    // Estimated per-record overhead for list-based sorting.
    private static final long LIST_OVERHEAD = 50;
    // Estimated per-record overhead for hash-based deduplication.
    private static final long SET_OVERHEAD = 84;

    private MemoryModel() {}

    public static long sortEntryBytes(String line) {
        return line.length() + LIST_OVERHEAD;
    }

    public static long hashEntryBytes(String line) {
        return line.length() + SET_OVERHEAD;
    }
}
