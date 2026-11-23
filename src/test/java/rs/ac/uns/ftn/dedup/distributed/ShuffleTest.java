package rs.ac.uns.ftn.dedup.distributed;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import rs.ac.uns.ftn.dedup.local.hash.LineHash;

class ShuffleTest {

    @TempDir
    Path dir;

    List<Node> nodes = new ArrayList<>();
    Transport transport = new Transport();

    @BeforeEach
    void createNodesWithInput() throws IOException {
        for (int id = 0; id < 3; id++) {
            Node node = new Node(id, dir, 1000);
            nodes.add(node);
            List<String> lines = new ArrayList<>();
            for (int j = 0; j < 30; j++) {
                lines.add("line-" + (j % 20)); // 30 lines, some repeated
            }
            Files.writeString(node.inputFile(), String.join("\n", lines) + "\n");
        }
    }

    private List<String> receivedBy(Node node) throws IOException {
        List<String> all = new ArrayList<>();
        try (Stream<Path> files = Files.list(node.incomingFolder())) {
            for (Path file : files.toList()) {
                all.addAll(Files.readAllLines(file));
            }
        }
        return all;
    }

    @Test
    void everyLineArrivesAtItsHashNode() throws IOException {
        new Shuffle(transport).run(nodes);

        int total = 0;
        for (Node node : nodes) {
            for (String line : receivedBy(node)) {
                assertEquals(node.id(), LineHash.bucket(line, ShuffleSplitter.SHUFFLE_SEED, 3));
                total++;
            }
        }
        assertEquals(90, total); // 3 nodes x 30 lines, nothing lost
    }

    @Test
    void ownPartDoesNotUseTheNetwork() throws IOException {
        long totalInputBytes = 0;
        for (Node node : nodes) {
            totalInputBytes += Files.size(node.inputFile());
        }

        new Shuffle(transport).run(nodes);

        assertEquals(6, transport.filesSent()); // each of 3 nodes sends to the 2 others
        assertTrue(transport.bytesSent() < totalInputBytes);
    }
}