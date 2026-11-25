package rs.ac.uns.ftn.dedup.local.hash;

import rs.ac.uns.ftn.dedup.core.MemoryModel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

/**
 * Performs in-memory deduplication of a single hash bucket
 * using a HashSet and an estimated memory budget.
 */
public class BucketDeduper {

    private final long budgetBytes;

    public BucketDeduper(long budgetBytes) {
        this.budgetBytes = budgetBytes;
    }

    /**
     * Attempts to deduplicate a bucket within the estimated memory budget.
     * On success, writes all distinct records and returns true.
     * If further partitioning is required, returns false without
     * writing any records to the output.
     */
    public boolean tryDedupe(Path bucket, Writer out) throws IOException {
        Set<String> seen = new HashSet<>();
        long used = 0;

        try (BufferedReader reader = Files.newBufferedReader(bucket, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (seen.add(line)) {
                    // Account for memory only when a new distinct record is inserted.
                    used += MemoryModel.hashEntryBytes(line);
                    // Allow a single distinct record to exceed the estimated budget,
                    // since further partitioning cannot reduce its memory footprint.
                    // Assumes that an individual record fits in available JVM memory.
                    if (used > budgetBytes && seen.size() >= 2) {
                        return false;
                    }
                }
            }
        }
        // Write results only after successful in-memory deduplication.
        for (String line : seen) {
            out.write(line);
            out.write('\n');
        }
        return true;
    }
}
