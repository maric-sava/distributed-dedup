package rs.ac.uns.ftn.dedup.distributed;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Transport {

    private long bytesSent = 0;
    private int filesSent = 0;

    public Path send(Path file, Node from, Node to) throws IOException {
        Path senderFolder = from.folder().toAbsolutePath().normalize();
        if (!file.toAbsolutePath().normalize().startsWith(senderFolder)) {
            throw new IllegalArgumentException("A node can only send files from its own folder: " + file);
        }
        Path target = to.incomingFolder().resolve("from-" + from.id() + "-" + file.getFileName());
        Files.copy(file, target); // fails if target already exists, which would be a bug
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
