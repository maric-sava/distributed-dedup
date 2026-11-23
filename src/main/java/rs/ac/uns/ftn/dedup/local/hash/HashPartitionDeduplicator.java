package rs.ac.uns.ftn.dedup.local.hash;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import rs.ac.uns.ftn.dedup.core.Deduplicator;

/**
 * Removes duplicate lines using hash partitioning:
 * try a HashSet, and if it does not fit the budget, split by hash and repeat on each part.
 */
public class HashPartitionDeduplicator implements Deduplicator {

    private final RecursiveHashDeduper deduper;

    public HashPartitionDeduplicator(long budgetBytes, int buckets) {
        this.deduper = new RecursiveHashDeduper(budgetBytes, buckets);
    }

    @Override
    public void dedupe(Path input, Path output) throws IOException {
        try (BufferedWriter out = Files.newBufferedWriter(output)) {
            deduper.dedupe(input, out);
        }
    }
}