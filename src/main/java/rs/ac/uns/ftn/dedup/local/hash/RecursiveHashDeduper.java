package rs.ac.uns.ftn.dedup.local.hash;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

/**
 * Performs recursive hash-based deduplication.
 * Buckets that exceed the estimated memory budget are
 * repartitioned using a different seed until they can
 * be deduplicated in memory or the recursion limit is reached.
 */
public class RecursiveHashDeduper {

    // Maximum repartitioning depth to prevent unbounded recursion.
    private static final int MAX_DEPTH = 10;

    private final BucketDeduper bucketDeduper;
    private final Partitioner partitioner = new Partitioner();
    // Number of child buckets created during each split
    private final int buckets;

    public RecursiveHashDeduper(long budgetBytes, int buckets) {
        this.bucketDeduper = new BucketDeduper(budgetBytes);
        this.buckets = buckets;
    }

    /**
     * Writes all distinct records to the supplied writer,
     * starting recursive processing at depth zero.
     */
    public void dedupe(Path file, Writer out) throws IOException {
        dedupe(file, out, 0);
    }

    private void dedupe(Path file, Writer out, int depth) throws IOException {
        if (bucketDeduper.tryDedupe(file, out)) {
            // Deduplication succeeded without further partitioning.
            return;
        }
        // Stop recursion if repeated partitioning fails to produce
        // buckets that fit within the estimated memory budget.
        if (depth >= MAX_DEPTH) {
            throw new IllegalStateException("Still too big after " + MAX_DEPTH
                    + " splits. The lines probably share the same hash.");
        }
        // Repartition the oversized bucket using a depth-dependent seed
        // to attempt a different distribution of records.
        Path folder = Files.createTempDirectory(file.toAbsolutePath().getParent(), "split-");
        try {
            List<Path> parts = partitioner.partition(file, folder, buckets, depth + 1);
            // Recursively process each child bucket using the shared output writer.
            for (Path part : parts) {
                dedupe(part, out, depth + 1);
            }
        } finally {
            // Remove temporary partitions even if recursive processing fails.
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