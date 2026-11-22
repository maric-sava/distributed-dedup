package rs.ac.uns.ftn.dedup.local.sort;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RunWriterTest {

    @TempDir
    Path dir;

    @Test
    void writeSortedRunsUntilInputEnds() throws IOException {
        BufferedReader reader = new BufferedReader(new StringReader("d\nb\nc\na\ne\n"));
        RunWriter writer = new RunWriter(150);

        Path run1 = dir.resolve("run1.txt");
        assertEquals(3, writer.writeRun(reader, run1));
        assertEquals(List.of("b", "c", "d"), Files.readAllLines(run1));

        Path run2 = dir.resolve("run2.txt");
        assertEquals(2, writer.writeRun(reader, run2));
        assertEquals(List.of("a", "e"), Files.readAllLines(run2));
    }

    @Test
    void splitsInputIntoSeveralSortedRuns() throws IOException {
        Path input = dir.resolve("input.txt");
        Files.writeString(input, "d\nb\nc\na\ne\n");

        List<Path> runs = new RunWriter(150).writeAllRuns(input, dir.resolve("runs"));

        assertEquals(2, runs.size());
        assertEquals(List.of("b", "c", "d"), Files.readAllLines(runs.get(0)));
        assertEquals(List.of("a", "e"), Files.readAllLines(runs.get(1)));
    }

    @Test
    void emptyInputGivesNoRuns() throws IOException {
        Path input = dir.resolve("input.txt");
        Files.writeString(input, "");

        assertEquals(0, new RunWriter(150).writeAllRuns(input, dir.resolve("runs")).size());
    }
}
