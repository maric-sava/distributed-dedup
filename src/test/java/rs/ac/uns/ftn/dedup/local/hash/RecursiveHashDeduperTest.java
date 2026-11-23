package rs.ac.uns.ftn.dedup.local.hash;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import rs.ac.uns.ftn.dedup.core.InMemoryDeduplicator;
import rs.ac.uns.ftn.dedup.generator.DataGenerator;
import rs.ac.uns.ftn.dedup.generator.Distribution;
import rs.ac.uns.ftn.dedup.generator.GeneratorConfig;

class RecursiveHashDeduperTest {

    @TempDir
    Path dir;

    private List<String> sortedLines(StringWriter out) {
        return out.toString().lines().sorted().toList();
    }

    @Test
    void noSplitWhenItFits() throws IOException {
        Path in = dir.resolve("in.txt");
        Files.writeString(in, "a\nb\na\n");
        StringWriter out = new StringWriter();

        new RecursiveHashDeduper(10_000, 4).dedupe(in, out);

        assertEquals(List.of("a", "b"), sortedLines(out));
    }

    @Test
    void splitsAndMatchesBaselineOnRandomFiles() throws IOException {
        for (long seed = 1; seed <= 5; seed++) {
            Path in = dir.resolve("in-" + seed + ".txt");
            Path expected = dir.resolve("expected-" + seed + ".txt");
            new DataGenerator().generate(new GeneratorConfig(1000, 100, Distribution.UNIFORM, seed), in);
            new InMemoryDeduplicator().dedupe(in, expected);

            StringWriter out = new StringWriter();
            // about 30 distinct lines fit in memory, there are 100: it has to split
            new RecursiveHashDeduper(3000, 4).dedupe(in, out);

            assertEquals(Files.readAllLines(expected).stream().sorted().toList(), sortedLines(out));
        }
    }

    @Test
    void failsClearlyWhenLinesCannotBeSeparated() throws IOException {
        // These four different lines have the same Java hashCode, so no seed can ever separate them.
        Path in = dir.resolve("in.txt");
        Files.writeString(in, "AaAa\nAaBB\nBBAa\nBBBB\n");

        assertThrows(IllegalStateException.class,
                () -> new RecursiveHashDeduper(100, 4).dedupe(in, new StringWriter()));
    }
}