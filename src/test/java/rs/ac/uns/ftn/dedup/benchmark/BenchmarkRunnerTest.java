package rs.ac.uns.ftn.dedup.benchmark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import rs.ac.uns.ftn.dedup.distributed.partition.PartitionStrategy;
import rs.ac.uns.ftn.dedup.generator.DataGenerator;
import rs.ac.uns.ftn.dedup.generator.Distribution;
import rs.ac.uns.ftn.dedup.generator.GeneratorConfig;

class BenchmarkRunnerTest {

    @TempDir
    Path dir;

    private List<BenchmarkCase> twoCases() throws IOException {
        Path in = dir.resolve("in.txt");
        new DataGenerator().generate(new GeneratorConfig(500, 100, Distribution.UNIFORM, 1), in);
        return List.of(
                new BenchmarkCase(in, 3, LocalAlgorithm.HASH, false, PartitionStrategy.HASH, 1000, 4),
                new BenchmarkCase(in, 3, LocalAlgorithm.SORT, false, PartitionStrategy.HASH, 1000, 4));
    }

    private List<String[]> rows(Path csvFile) throws IOException {
        List<String[]> rows = new ArrayList<>();
        List<String> lines = Files.readAllLines(csvFile);
        for (String line : lines.subList(1, lines.size())) { // skip the header
            rows.add(line.split(","));
        }
        return rows;
    }

    @Test
    void warmupRunsAreNotRecordedAndRecordedRunsAreNumbered() throws IOException {
        Path csvFile = dir.resolve("results.csv");
        BenchmarkRunner runner = new BenchmarkRunner(new ResultsCsv(csvFile), dir.resolve("work"), 1, 3);

        runner.run(twoCases());

        List<String[]> rows = rows(csvFile);
        assertEquals(6, rows.size()); // 2 cases x 3 recorded runs
        assertEquals("HASH", rows.get(0)[2]);
        assertEquals("1", rows.get(0)[7]);
        assertEquals("2", rows.get(1)[7]);
        assertEquals("3", rows.get(2)[7]);
        assertEquals("SORT", rows.get(3)[2]);
        assertEquals("1", rows.get(3)[7]);
    }

    @Test
    void recordedStatsAreReal() throws IOException {
        Path csvFile = dir.resolve("results.csv");

        new BenchmarkRunner(new ResultsCsv(csvFile), dir.resolve("work"), 0, 2).run(twoCases());

        for (String[] row : rows(csvFile)) {
            assertTrue(Long.parseLong(row[9]) > 0, "bytesSent should be positive with 3 nodes");
            assertTrue(Double.parseDouble(row[10]) >= 1.0, "imbalance is at least 1");
        }
    }

    @Test
    void leavesNothingBehindInTheWorkFolder() throws IOException {
        Path work = dir.resolve("work");

        new BenchmarkRunner(new ResultsCsv(dir.resolve("results.csv")), work, 1, 1).run(twoCases());

        try (Stream<Path> leftovers = Files.list(work)) {
            assertEquals(0, leftovers.count());
        }
    }
}