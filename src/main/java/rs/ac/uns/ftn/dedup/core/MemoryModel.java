package rs.ac.uns.ftn.dedup.core;

public final class MemoryModel {

    private static final long LIST_OVERHEAD = 50;
    private static final long SET_OVERHEAD = 84;

    private MemoryModel() {}

    public static long sortEntryBytes(String line) {
        return line.length() + LIST_OVERHEAD;
    }

    public static long hashEntryBytes(String line) {
        return line.length() + SET_OVERHEAD;
    }
}
