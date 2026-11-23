package rs.ac.uns.ftn.dedup.distributed.partition;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import rs.ac.uns.ftn.dedup.distributed.Node;

class RangePartitionerTest {

    @TempDir
    Path dir;

    // cut points c, e, g: four ranges
    private final RangeBoundaries boundaries =
            RangeBoundaries.fromSample(List.of("a", "b", "c", "d", "e", "f", "g", "h"), 4);

    private Path writeInput(Node node, List<String> lines) throws IOException {
        Path input = node.folder().resolve("input.txt");
        Files.writeString(input, String.join("\n", lines) + "\n");
        return input;
    }

    @Test
    void splitsLinesByRange() throws IOException {
        Node source = new Node(0, dir, 1000);
        Path input = writeInput(source, List.of("h", "a", "d", "b", "g", "e", "c", "f"));

        List<Path> parts = new RangePartitioner(boundaries).split(source, input, 4);

        assertEquals(4, parts.size());
        assertEquals(List.of("a", "b"), Files.readAllLines(parts.get(0)));
        assertEquals(List.of("d", "c"), Files.readAllLines(parts.get(1)));
        assertEquals(List.of("e", "f"), Files.readAllLines(parts.get(2)));
        assertEquals(List.of("h", "g"), Files.readAllLines(parts.get(3)));
    }

    @Test
    void givesOnePartPerNodeEvenWhenSomeAreEmpty() throws IOException {
        Node source = new Node(0, dir, 1000);
        Path input = writeInput(source, List.of("a", "a"));

        List<Path> parts = new RangePartitioner(boundaries).split(source, input, 4);

        assertEquals(4, parts.size());
        assertEquals(List.of("a", "a"), Files.readAllLines(parts.get(0)));
        assertEquals(List.of(), Files.readAllLines(parts.get(3)));
        for (Path part : parts) {
            assertTrue(part.startsWith(source.folder())); // so Transport accepts them
        }
    }

    @Test
    void sameLineGoesToSameNodeFromDifferentSources() throws IOException {
        Node first = new Node(0, dir, 1000);
        Node second = new Node(1, dir, 1000);
        RangePartitioner partitioner = new RangePartitioner(boundaries);

        List<Path> fromFirst = partitioner.split(first, writeInput(first, List.of("a", "d", "f", "h")), 4);
        List<Path> fromSecond = partitioner.split(second, writeInput(second, List.of("h", "f", "d", "a")), 4);

        for (int i = 0; i < 4; i++) {
            assertEquals(Files.readAllLines(fromFirst.get(i)).stream().sorted().toList(),
                    Files.readAllLines(fromSecond.get(i)).stream().sorted().toList());
        }
    }

    @Test
    void rejectsBoundariesMadeForMoreNodes() throws IOException {
        Node source = new Node(0, dir, 1000);
        Path input = writeInput(source, List.of("a"));

        assertThrows(IllegalArgumentException.class,
                () -> new RangePartitioner(boundaries).split(source, input, 2));
    }
}