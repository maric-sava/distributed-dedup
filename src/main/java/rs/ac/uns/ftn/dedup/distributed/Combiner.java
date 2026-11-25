package rs.ac.uns.ftn.dedup.distributed;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import rs.ac.uns.ftn.dedup.core.Deduplicator;

/**
 * Performs optional local deduplication before the shuffle to reduce
 * data transfer volume. Duplicates across different nodes are
 * removed during destination-local deduplication.
 */
public class Combiner {

    private final Deduplicator local; // the same local algorithms: hash or external sort

    public Combiner(Deduplicator local) {
        this.local = local;
    }

    public void run(Node node) throws IOException {
        Path combined = node.folder().resolve("combined.txt"); // temporary, inside the node's own folder
        local.dedupe(node.inputFile(), combined);
        Files.move(combined, node.inputFile(), StandardCopyOption.REPLACE_EXISTING);
    }
}