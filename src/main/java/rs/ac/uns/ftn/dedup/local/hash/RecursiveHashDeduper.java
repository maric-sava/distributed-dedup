package rs.ac.uns.ftn.dedup.local.hash;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

/**
 * Dedupes a file with a HashSet. If the distinct lines do not fit the memory budget,
 * splits the file into buckets with a new seed and handles each bucket the same way.
 */
public class RecursiveHashDeduper {

    private static final int MAX_DEPTH = 10; // maximum number of splits one line can go through

    private final BucketDeduper bucketDeduper;
    private final Partitioner partitioner = new Partitioner();
    private final int buckets; // how many pieces one split makes

    public RecursiveHashDeduper(long budgetBytes, int buckets) {
        this.bucketDeduper = new BucketDeduper(budgetBytes);
        this.buckets = buckets;
    }

    /** Writes the distinct lines of file to out. */
    public void dedupe(Path file, Writer out) throws IOException {
        dedupe(file, out, 0);
    }

    private void dedupe(Path file, Writer out, int depth) throws IOException {
        if (bucketDeduper.tryDedupe(file, out)) {
            return; // it fit in memory, nothing more to do
        }
        if (depth >= MAX_DEPTH) {
            throw new IllegalStateException("Still too big after " + MAX_DEPTH
                    + " splits. The lines probably share the same hash.");
        }
        // Too big: split with a NEW seed (depth + 1), so the lines spread out differently.
        Path folder = Files.createTempDirectory(file.toAbsolutePath().getParent(), "split-");
        try {
            List<Path> parts = partitioner.partition(file, folder, buckets, depth + 1);
            for (Path part : parts) {
                dedupe(part, out, depth + 1); // same procedure on every part
            }
        } finally {
            deleteFolder(folder);
        }
    }

    private void deleteFolder(Path folder) throws IOException {
        try (Stream<Path> leftovers = Files.list(folder)) {
            for (Path leftover : leftovers.toList()) {
                Files.delete(leftover);
            }
        }
        Files.delete(folder);
    }
}