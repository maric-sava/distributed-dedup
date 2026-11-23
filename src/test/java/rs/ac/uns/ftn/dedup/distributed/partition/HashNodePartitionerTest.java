package rs.ac.uns.ftn.dedup.distributed.partition;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import rs.ac.uns.ftn.dedup.distributed.Node;
import rs.ac.uns.ftn.dedup.local.hash.LineHash;

class HashNodePartitionerTest {

    @TempDir
    Path dir;

    private Path writeInput(Node node, List<String> lines) throws IOException {
        Path input = node.folder().resolve("input.txt");
        Files.writeString(input, String.join("\n", lines) + "\n");
        return input;
    }

    private int partOf(List<Path> parts, String line) throws IOException {
        for (int i = 0; i < parts.size(); i++) {
            if (Files.readAllLines(parts.get(i)).contains(line)) {
                return i;
            }
        }
        return -1;
    }

    @Test
    void givesOnePartPerNodeInsideSourceFolder() throws IOException {
        Node source = new Node(0, dir, 1000);
        Path input = writeInput(source, List.of("a"));

        List<Path> parts = new HashNodePartitioner().split(source, input, 4);

        assertEquals(4, parts.size());
        for (Path part : parts) {
            assertTrue(part.startsWith(source.folder()));
        }
    }

    @Test
    void everyLineIsInThePartOfItsHashNode() throws IOException {
        Node source = new Node(0, dir, 1000);
        List<String> lines = List.of("a", "b", "c", "a", "d", "b", "e", "a", "f", "g");
        Path input = writeInput(source, lines);

        List<Path> parts = new HashNodePartitioner().split(source, input, 3);

        int total = 0;
        for (int i = 0; i < parts.size(); i++) {
            for (String line : Files.readAllLines(parts.get(i))) {
                assertEquals(i, LineHash.bucket(line, HashNodePartitioner.SHUFFLE_SEED, 3));
                total++;
            }
        }
        assertEquals(lines.size(), total); // nothing lost, nothing added
    }

    @Test
    void sameLineGoesToSameNodeFromDifferentSources() throws IOException {
        Node first = new Node(0, dir, 1000);
        Node second = new Node(1, dir, 1000);
        HashNodePartitioner splitter = new HashNodePartitioner();

        List<Path> fromFirst = splitter.split(first, writeInput(first, List.of("a", "b", "c", "d")), 3);
        List<Path> fromSecond = splitter.split(second, writeInput(second, List.of("d", "c", "b", "a")), 3);

        for (String line : List.of("a", "b", "c", "d")) {
            assertEquals(partOf(fromFirst, line), partOf(fromSecond, line));
        }
    }
}