package rs.ac.uns.ftn.dedup.local.hash;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import rs.ac.uns.ftn.dedup.core.InMemoryDeduplicator;
import rs.ac.uns.ftn.dedup.generator.DataGenerator;
import rs.ac.uns.ftn.dedup.generator.Distribution;
import rs.ac.uns.ftn.dedup.generator.GeneratorConfig;

class HashPartitionDeduplicatorTest {

    @TempDir
    Path dir;

    private List<String> sortedLines(Path file) throws IOException {
        return Files.readAllLines(file).stream().sorted().toList();
    }

    private List<String> run(String content) throws IOException {
        Path in = dir.resolve("in.txt");
        Path out = dir.resolve("out.txt");
        Files.writeString(in, content);
        new HashPartitionDeduplicator(1000, 4).dedupe(in, out);
        return sortedLines(out);
    }

    @Test
    void matchesBaselineOnRandomFiles() throws IOException {
        for (long seed = 1; seed <= 10; seed++) {
            Path in = dir.resolve("in-" + seed + ".txt");
            Path expected = dir.resolve("expected-" + seed + ".txt");
            Path actual = dir.resolve("actual-" + seed + ".txt");
            new DataGenerator().generate(new GeneratorConfig(2000, 150, Distribution.UNIFORM, seed), in);

            new InMemoryDeduplicator().dedupe(in, expected);
            new HashPartitionDeduplicator(1000, 4).dedupe(in, actual);

            assertEquals(sortedLines(expected), sortedLines(actual));
        }
    }

    @Test
    void emptyFile() throws IOException {
        assertEquals(List.of(), run(""));
    }

    @Test
    void oneLine() throws IOException {
        assertEquals(List.of("a"), run("a\n"));
    }

    @Test
    void allLinesEqual() throws IOException {
        assertEquals(List.of("a"), run("a\na\na\na\n"));
    }

    @Test
    void noDuplicates() throws IOException {
        assertEquals(List.of("a", "b", "c"), run("c\na\nb\n"));
    }

    @Test
    void lineLongerThanBudgetIsStillWritten() throws IOException {
        String longLine = "x".repeat(5000);
        assertEquals(List.of(longLine), run(longLine + "\n" + longLine + "\n"));
    }
}