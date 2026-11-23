package rs.ac.uns.ftn.dedup.benchmark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import rs.ac.uns.ftn.dedup.core.InMemoryDeduplicator;
import rs.ac.uns.ftn.dedup.distributed.partition.PartitionStrategy;
import rs.ac.uns.ftn.dedup.generator.DataGenerator;
import rs.ac.uns.ftn.dedup.generator.Distribution;
import rs.ac.uns.ftn.dedup.generator.GeneratorConfig;

class BenchmarkCaseTest {

    @TempDir
    Path dir;

    private List<String> sortedLines(Path file) throws IOException {
        return Files.readAllLines(file).stream().sorted().toList();
    }

    @Test
    void createdJobMatchesBaselineForEveryLocalAlgorithm() throws IOException {
        Path in = dir.resolve("in.txt");
        Path expected = dir.resolve("expected.txt");
        new DataGenerator().generate(new GeneratorConfig(2000, 150, Distribution.ZIPF, 1), in);
        new InMemoryDeduplicator().dedupe(in, expected);

        for (LocalAlgorithm local : LocalAlgorithm.values()) {
            Path actual = dir.resolve("actual-" + local + ".txt");
            BenchmarkCase benchmarkCase = new BenchmarkCase(in, 3, local, true, PartitionStrategy.RANGE, 1000, 4);

            benchmarkCase.createJob().dedupe(in, actual);

            assertEquals(sortedLines(expected), sortedLines(actual), "local=" + local);
        }
    }

    @Test
    void everyCallCreatesANewJob() {
        BenchmarkCase benchmarkCase =
                new BenchmarkCase(Path.of("x"), 2, LocalAlgorithm.HASH, false, PartitionStrategy.HASH, 1000, 4);

        assertNotSame(benchmarkCase.createJob(), benchmarkCase.createJob());
    }
}