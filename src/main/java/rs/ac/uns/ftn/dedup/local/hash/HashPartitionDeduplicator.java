package rs.ac.uns.ftn.dedup.local.hash;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import rs.ac.uns.ftn.dedup.core.Deduplicator;

/**
 * Performs external hash-based deduplication by recursively
 * partitioning data that exceeds the estimated memory budget.
 * Delegates recursive processing to RecursiveHashDeduper
 * and produces a deduplicated output file without guaranteeing
 * record ordering.
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