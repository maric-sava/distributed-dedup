package rs.ac.uns.ftn.dedup.distributed;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import rs.ac.uns.ftn.dedup.core.Deduplicator;

/**
 * Runs on one node before the shuffle: replaces the node's input with its distinct lines,
 * so less data has to be sent. It only sees this node's data, so duplicates that live on
 * different nodes are still removed later, by the final dedupe.
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