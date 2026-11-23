package rs.ac.uns.ftn.dedup.benchmark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import rs.ac.uns.ftn.dedup.distributed.JobStats;
import rs.ac.uns.ftn.dedup.distributed.partition.PartitionStrategy;

class ResultsCsvTest {

    @TempDir
    Path dir;

    private final BenchmarkCase benchmarkCase = new BenchmarkCase(
            Path.of("data/users.txt"), 2, LocalAlgorithm.HASH, true, PartitionStrategy.RANGE, 1_000_000, 16);

    @Test
    void newFileStartsWithTheHeader() throws IOException {
        Path file = dir.resolve("results.csv");

        new ResultsCsv(file);

        assertEquals(List.of(ResultsCsv.HEADER), Files.readAllLines(file));
    }

    @Test
    void appendsOneRowPerRun() throws IOException {
        Path file = dir.resolve("results.csv");
        ResultsCsv csv = new ResultsCsv(file);
        JobStats stats = new JobStats(1234, List.of(10L, 30L), 56); // imbalance: max 30, average 20 = 1.5

        csv.append(benchmarkCase, 1, stats);
        csv.append(benchmarkCase, 2, stats);

        List<String> lines = Files.readAllLines(file);
        assertEquals(3, lines.size());
        assertEquals("users.txt,2,HASH,true,RANGE,1000000,16,1,56,1234,1.500", lines.get(1));
        assertTrue(lines.get(2).contains(",16,2,56,"));
    }

    @Test
    void existingFileKeepsItsRowsAndGetsNoSecondHeader() throws IOException {
        Path file = dir.resolve("results.csv");
        new ResultsCsv(file).append(benchmarkCase, 1, new JobStats(1, List.of(1L), 1));

        new ResultsCsv(file); // opened again, like a second benchmark session

        assertEquals(2, Files.readAllLines(file).size()); // header + one row
    }

    @Test
    void createsMissingFolders() throws IOException {
        Path file = dir.resolve("deep").resolve("er").resolve("results.csv");

        new ResultsCsv(file);

        assertTrue(Files.exists(file));
    }
}