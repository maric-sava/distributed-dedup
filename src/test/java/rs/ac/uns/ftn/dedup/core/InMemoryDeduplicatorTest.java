package rs.ac.uns.ftn.dedup.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class InMemoryDeduplicatorTest {

    @TempDir
    Path dir;

    private List<String> run(String ...lines) throws IOException {
        Path in = dir.resolve("input.txt");
        Path out = dir.resolve("output.txt");
        Files.writeString(in, lines.length == 0 ? "" : String.join("\n", lines) + "\n");
        new InMemoryDeduplicator().dedupe(in, out);
        List<String> result = Files.readAllLines(out);
        Collections.sort(result);
        return result;
    }

    @Test
    void removeDuplicates() throws IOException {
        assertEquals(List.of("a", "b", "c"), run("a", "b", "a", "c", "b"));
    }

    @Test
    void emptyFileGivesEmptyOutput() throws IOException {
        assertEquals(List.of(), run());
    }
}
