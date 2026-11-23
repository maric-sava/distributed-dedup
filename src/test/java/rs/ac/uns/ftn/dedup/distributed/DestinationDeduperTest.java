package rs.ac.uns.ftn.dedup.distributed;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import rs.ac.uns.ftn.dedup.local.hash.HashPartitionDeduplicator;

class DestinationDeduperTest {

    @TempDir
    Path dir;

    private List<String> sortedLines(Path file) throws IOException {
        return Files.readAllLines(file).stream().sorted().toList();
    }

    @Test
    void mergesFilesFromDifferentSendersAndRemovesDuplicates() throws IOException {
        Node node = new Node(1, dir, 1000);
        Files.writeString(node.incomingFolder().resolve("from-0-bucket-1.txt"), "a\nb\na\n");
        Files.writeString(node.incomingFolder().resolve("from-1-bucket-1.txt"), "b\nc\n");

        new DestinationDeduper(new HashPartitionDeduplicator(1000, 4)).run(node);

        assertEquals(List.of("a", "b", "c"), sortedLines(node.outputFile()));
        assertFalse(Files.exists(node.folder().resolve("merged.txt"))); // temporary file is gone
    }

    @Test
    void nothingReceivedGivesEmptyOutput() throws IOException {
        Node node = new Node(0, dir, 1000);

        new DestinationDeduper(new HashPartitionDeduplicator(1000, 4)).run(node);

        assertEquals(List.of(), sortedLines(node.outputFile()));
    }

    @Test
    void worksWhenLocalAlgorithmHasToSplit() throws IOException {
        Node node = new Node(0, dir, 300);
        TreeSet<String> expected = new TreeSet<>();
        for (int file = 0; file < 3; file++) {
            List<String> lines = new ArrayList<>();
            for (int j = 0; j < 40; j++) {
                lines.add("k" + (j % 25));
                expected.add("k" + (j % 25));
            }
            Files.writeString(node.incomingFolder().resolve("from-" + file + "-part.txt"),
                    String.join("\n", lines) + "\n");
        }

        // budget 300 holds about 3 distinct lines, there are 25: the local algorithm must split
        new DestinationDeduper(new HashPartitionDeduplicator(300, 4)).run(node);

        assertEquals(List.copyOf(expected), sortedLines(node.outputFile()));
    }
}