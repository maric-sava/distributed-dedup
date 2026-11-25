package rs.ac.uns.ftn.dedup.distributed;

import rs.ac.uns.ftn.dedup.core.Deduplicator;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

/**
 * Deduplicates all partitions assigned to a destination node,
 * independently of the partitioning strategy and local deduplication algorithm.
 */
public class DestinationDeduper {

    private final Deduplicator local;

    public DestinationDeduper(Deduplicator local) {
        this.local = local;
    }

    /**
     * Combines incoming partitions into a temporary file,
     * performs local deduplication and removes the intermediate file.
     */
    public Path run(Node node) throws IOException {
        Path merged = node.folder().resolve("merged.txt");
        concatenate(node.incomingFolder(), merged);
        local.dedupe(merged, node.outputFile());
        Files.delete(merged);
        return node.outputFile();
    }

    /**
     * Concatenates incoming partition files in deterministic filename order
     * without sorting or deduplicating their contents.
     */
    private void concatenate(Path folder, Path target) throws IOException {
        List<Path> files;
        try (Stream<Path> listing = Files.list(folder)) {
            files = listing.sorted().toList();
        }
        try (OutputStream out = Files.newOutputStream(target)) {
            for (Path file : files) {
                Files.copy(file, out);  // copies bytes, never loads the lines into memory
            }
        }
    }
}
