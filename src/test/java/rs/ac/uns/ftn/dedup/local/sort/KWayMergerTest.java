package rs.ac.uns.ftn.dedup.local.sort;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class KWayMergerTest {

    @TempDir
    Path dir;

    private List<String> merge(String... contents) throws IOException {
        List<Path> runs = new ArrayList<>();
        for (int i = 0; i < contents.length; i++) {
            Path run = dir.resolve("run-" + i + ".txt");
            Files.writeString(run, contents[i]);
            runs.add(run);
        }
        Path out = dir.resolve("out.txt");
        new KWayMerger().merge(runs, out);
        return Files.readAllLines(out);
    }

    @Test
    void mergesThreeRunsIntoSortedOrder() throws IOException {
        assertEquals(List.of("a", "b", "c", "d", "e"), merge("a\nd\n", "b\nd\ne\n", "c\n"));
    }

    @Test
    void dropsDuplicatesInsideOneRun() throws IOException {
        assertEquals(List.of("a", "b"), merge("a\na\nb\n"));
    }

    @Test
    void dropsLineThatIsInEveryRun() throws IOException {
        assertEquals(List.of("x"), merge("x\n", "x\n", "x\n"));
    }

    @Test
    void mergesInSeveralPasses() throws IOException {
        List<Path> runs = new ArrayList<>();
        String[] contents = {"a\nf\n", "b\nf\n", "c\n", "d\ng\n", "e\n"};
        for (int i = 0; i < contents.length; i++) {
            Path run = dir.resolve("run-" + i + ".txt");
            Files.writeString(run, contents[i]);
            runs.add(run);
        }
        Path out = dir.resolve("out.txt");

        new KWayMerger().mergeInPasses(runs, out, 2); // 5 runs, at most 2 at a time

        assertEquals(List.of("a", "b", "c", "d", "e", "f", "g"), Files.readAllLines(out));
    }

}
