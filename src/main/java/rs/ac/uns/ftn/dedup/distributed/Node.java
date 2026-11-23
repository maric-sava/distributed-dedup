package rs.ac.uns.ftn.dedup.distributed;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Node {

    private final int id;
    private final Path folder;
    private final long budgetBytes;

    public Node(int id, Path baseFolder, long budgetBytes) throws IOException {
        this.id = id;
        this.folder = baseFolder.resolve("node-" + id);
        this.budgetBytes = budgetBytes;
        Files.createDirectories(incomingFolder());  // also creates the node folder itself
    }

    public int id() {
        return id;
    }

    public Path folder() {
        return folder;
    }

    public long budgetBytes() {
        return budgetBytes;
    }

    public Path incomingFolder() {
        return folder.resolve("incoming");
    }

    public Path inputFile() {
        return folder.resolve("input.txt");
    }

    /** The part of the final result produced by this node. */
    public Path outputFile() {
        return folder.resolve("output.txt");
    }
}
