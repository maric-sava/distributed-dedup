package rs.ac.uns.ftn.dedup.local.sort;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import rs.ac.uns.ftn.dedup.core.Deduplicator;
import rs.ac.uns.ftn.dedup.core.InMemoryDeduplicator;
import rs.ac.uns.ftn.dedup.generator.DataGenerator;
import rs.ac.uns.ftn.dedup.generator.Distribution;
import rs.ac.uns.ftn.dedup.generator.GeneratorConfig;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ExternalSortDeduplicatorTest {

    @TempDir
    Path dir;

    private List<String> sortedLines(Path file) throws IOException {
        List<String> lines = Files.readAllLines(file);
        Collections.sort(lines);
        return lines;
    }

    private List<String> externalSort(String content, long budget, int maxRunsPerMerge) throws IOException {
        Path in = dir.resolve("in");
        Path out = dir.resolve("out");
        Files.writeString(in, content);
        new ExternalSortDeduplicator(budget, maxRunsPerMerge).dedupe(in, out);
        return sortedLines(out);
    }

    @Test
    void matchesBaselineOnRandomFiles() throws IOException {
        for (long seed = 1; seed <= 10; seed++) {
            Path in = dir.resolve("in-" + seed + ".txt");
            Path expected = dir.resolve("expected-" + seed + ".txt");
            Path actual = dir.resolve("actual-" + seed + ".txt");
            new DataGenerator().generate(new GeneratorConfig(1000, 100, Distribution.UNIFORM, seed), in);

            new InMemoryDeduplicator().dedupe(in, expected);
            new ExternalSortDeduplicator(600, 3).dedupe(in, actual);

            assertEquals(sortedLines(expected), sortedLines(actual));
        }
    }

    @Test
    void emptyFile() throws IOException {
        assertEquals(List.of(), externalSort("", 600, 3));
    }

    @Test
    void oneLine() throws IOException {
        assertEquals(List.of("a"), externalSort("a\n", 600, 3));
    }

    @Test
    void allLinesEqual() throws IOException {
        assertEquals(List.of("x"), externalSort("x\n".repeat(100), 100, 2));
    }

    @Test
    void noDuplicates() throws IOException {
        assertEquals(List.of("a", "b", "c"), externalSort("c\na\nb\n", 60, 2));
    }

    @Test
    void lineLongerThanBudget() throws IOException {
        String longLine = "x".repeat(1000);
        assertEquals(List.of("b", longLine), externalSort(longLine + "\nb\n", 100, 2));
    }
}
