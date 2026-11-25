package rs.ac.uns.ftn.dedup.distributed;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Simulates data transfer between nodes through filesystem operations
 * and tracks the volume of transferred data.
 */
public class Transport {

    private long bytesSent = 0;
    private int filesSent = 0;

    /**
     * Copies a partition file to the destination node's incoming directory,
     * preserves its source identity and records transfer statistics.
     */
    public Path send(Path file, Node from, Node to) throws IOException {
        // Ensure that the file belongs to the source node's working directory.
        Path senderFolder = from.folder().toAbsolutePath().normalize();
        if (!file.toAbsolutePath().normalize().startsWith(senderFolder)) {
            throw new IllegalArgumentException("A node can only send files from its own folder: " + file);
        }
        Path target = to.incomingFolder().resolve("from-" + from.id() + "-" + file.getFileName());
        // Fail if the target already exists to prevent silent overwriting.
        Files.copy(file, target);
        bytesSent += Files.size(file);
        filesSent++;

        return target;
    }

    public long bytesSent() {
        return bytesSent;
    }

    public int filesSent() {
        return filesSent;
    }
}
