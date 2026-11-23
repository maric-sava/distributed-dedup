package rs.ac.uns.ftn.dedup.distributed.partition;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SamplerTest {

    @TempDir
    Path dir;

    private Path writeLines(List<String> lines) throws IOException {
        Path file = dir.resolve("in.txt");
        Files.writeString(file, lines.isEmpty() ? "" : String.join("\n", lines) + "\n");
        return file;
    }

    private List<String> numberedLines(int count) {
        List<String> lines = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            lines.add("line-" + i);
        }
        return lines;
    }

    @Test
    void smallFileGivesAllLines() throws IOException {
        Path file = writeLines(List.of("a", "b", "c"));

        assertEquals(List.of("a", "b", "c"), new Sampler(10, 1).sample(file));
    }

    @Test
    void emptyFileGivesEmptySample() throws IOException {
        assertEquals(List.of(), new Sampler(10, 1).sample(writeLines(List.of())));
    }

    @Test
    void neverReturnsMoreThanSampleSizeAndOnlyRealLines() throws IOException {
        List<String> lines = numberedLines(1000);
        Path file = writeLines(lines);

        List<String> sample = new Sampler(50, 1).sample(file);

        assertEquals(50, sample.size());
        assertTrue(lines.containsAll(sample));
    }

    @Test
    void sameSeedGivesSameSampleAndDifferentSeedDoesNot() throws IOException {
        Path file = writeLines(numberedLines(1000));

        assertEquals(new Sampler(50, 7).sample(file), new Sampler(50, 7).sample(file));
        assertNotEquals(new Sampler(50, 7).sample(file), new Sampler(50, 8).sample(file));
    }

    @Test
    void frequentLinesAppearOftenInTheSample() throws IOException {
        List<String> lines = new ArrayList<>();
        for (int i = 0; i < 10_000; i++) {
            lines.add(i % 2 == 0 ? "x" : "other-" + i); // "x" is half of the file
        }
        Path file = writeLines(lines);

        List<String> sample = new Sampler(1000, 1).sample(file);

        long x = sample.stream().filter(line -> line.equals("x")).count();
        assertTrue(x > 400 && x < 600, "x count in sample: " + x); // about 500 expected
    }
}