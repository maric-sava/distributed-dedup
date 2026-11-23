package rs.ac.uns.ftn.dedup.distributed;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import rs.ac.uns.ftn.dedup.local.hash.HashPartitionDeduplicator;

class CombinerTest {

    @TempDir
    Path dir;

    private Combiner combiner() {
        return new Combiner(new HashPartitionDeduplicator(1000, 4));
    }

    private List<String> sortedLines(Path file) throws IOException {
        return Files.readAllLines(file).stream().sorted().toList();
    }

    @Test
    void removesDuplicatesInsideOneNode() throws IOException {
        Node node = new Node(0, dir, 1000);
        Files.writeString(node.inputFile(), "a\nb\na\nc\nb\na\n");

        combiner().run(node);

        assertEquals(List.of("a", "b", "c"), sortedLines(node.inputFile()));
    }

    @Test
    void doesNotTouchOtherNodes() throws IOException {
        Node first = new Node(0, dir, 1000);
        Node second = new Node(1, dir, 1000);
        Files.writeString(first.inputFile(), "a\na\n");
        Files.writeString(second.inputFile(), "a\na\n");

        combiner().run(first);

        assertEquals(List.of("a"), sortedLines(first.inputFile()));
        assertEquals(List.of("a", "a"), sortedLines(second.inputFile()));
    }

    @Test
    void emptyInputStaysEmpty() throws IOException {
        Node node = new Node(0, dir, 1000);
        Files.writeString(node.inputFile(), "");

        combiner().run(node);

        assertEquals(List.of(), sortedLines(node.inputFile()));
    }

    @Test
    void leavesNoTemporaryFileBehind() throws IOException {
        Node node = new Node(0, dir, 1000);
        Files.writeString(node.inputFile(), "a\nb\na\n");

        combiner().run(node);

        assertFalse(Files.exists(node.folder().resolve("combined.txt")));
    }
}