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

/** Dedupes one bucket with a HashSet, if its distinct lines fit in the memory budget. */
public class BucketDeduper {

    private final long budgetBytes;

    public BucketDeduper(long budgetBytes) {
        this.budgetBytes = budgetBytes;
    }

    /**
     * Tries to dedupe the bucket in memory. On success writes the distinct lines to out
     * and returns true. If the distinct lines do not fit the budget, writes nothing
     * and returns false (the caller must split the bucket further).
     */
    public boolean tryDedupe(Path bucket, Writer out) throws IOException {
        Set<String> seen = new HashSet<>();
        long used = 0;

        try (BufferedReader reader = Files.newBufferedReader(bucket, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (seen.add(line)) {
                    used += MemoryModel.hashEntryBytes(line);
                    // Too big: give up. But with only one line we accept it, because
                    // splitting a bucket that holds a single line would never help.
                    if (used > budgetBytes && seen.size() >= 2) {
                        return false;
                    }
                }
            }
        }
        for (String line : seen) {
            out.write(line);
            out.write('\n');
        }
        return true;
    }
}
