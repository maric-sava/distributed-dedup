package rs.ac.uns.ftn.dedup.distributed.setup;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import rs.ac.uns.ftn.dedup.distributed.Node;

class InputSpreaderTest {

    @TempDir
    Path dir;

    private List<Node> createNodes(int count) throws IOException {
        List<Node> nodes = new ArrayList<>();
        for (int id = 0; id < count; id++) {
            nodes.add(new Node(id, dir, 1000));
        }
        return nodes;
    }

    @Test
    void dealsLinesOneByOne() throws IOException {
        Path input = dir.resolve("in.txt");
        Files.writeString(input, "a\nb\nc\nd\ne\n");
        List<Node> nodes = createNodes(2);

        new InputSpreader().spread(input, nodes);

        assertEquals(List.of("a", "c", "e"), Files.readAllLines(nodes.get(0).inputFile()));
        assertEquals(List.of("b", "d"), Files.readAllLines(nodes.get(1).inputFile()));
    }

    @Test
    void nothingIsLostOrAdded() throws IOException {
        List<String> lines = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            lines.add("line-" + (i % 70));
        }
        Path input = dir.resolve("in.txt");
        Files.writeString(input, String.join("\n", lines) + "\n");
        List<Node> nodes = createNodes(3);

        new InputSpreader().spread(input, nodes);

        List<String> all = new ArrayList<>();
        for (Node node : nodes) {
            all.addAll(Files.readAllLines(node.inputFile()));
        }
        assertEquals(lines.stream().sorted().toList(), all.stream().sorted().toList());
    }

    @Test
    void emptyInputGivesEmptyFilesOnEveryNode() throws IOException {
        Path input = dir.resolve("in.txt");
        Files.writeString(input, "");
        List<Node> nodes = createNodes(3);

        new InputSpreader().spread(input, nodes);

        for (Node node : nodes) {
            assertEquals(List.of(), Files.readAllLines(node.inputFile()));
        }
    }
}