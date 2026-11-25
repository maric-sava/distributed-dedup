package rs.ac.uns.ftn.dedup.core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

/**
 * Performs in-memory deduplication using a HashSet.
 * Serves as a reference implementation for correctness testing.
 * Assumes that all distinct records fit in available memory.
 */
public class InMemoryDeduplicator implements Deduplicator {
    @Override
    public void dedupe(Path input, Path output) throws IOException {
        Set<String> seen = new HashSet<>();
        try (BufferedReader reader = Files.newBufferedReader(input, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                seen.add(line);
            }
        }
        try (BufferedWriter writer = Files.newBufferedWriter(output, StandardCharsets.UTF_8)) {
            for (String line : seen) {
                writer.write(line);
                writer.write('\n');
            }
        }
    }
}
