package rs.ac.uns.ftn.dedup.distributed;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import rs.ac.uns.ftn.dedup.core.InMemoryDeduplicator;
import rs.ac.uns.ftn.dedup.generator.DataGenerator;
import rs.ac.uns.ftn.dedup.generator.Distribution;
import rs.ac.uns.ftn.dedup.generator.GeneratorConfig;
import rs.ac.uns.ftn.dedup.local.hash.HashPartitionDeduplicator;
import rs.ac.uns.ftn.dedup.local.sort.ExternalSortDeduplicator;

class DistributedDeduplicatorTest {

    @TempDir
    Path dir;

    private List<String> sortedLines(Path file) throws IOException {
        return Files.readAllLines(file).stream().sorted().toList();
    }

    @Test
    void matchesBaselineForOneTwoAndFourNodes() throws IOException {
        for (Distribution distribution : Distribution.values()) {
            for (int nodes : new int[] {1, 2, 4}) {
                for (long seed = 1; seed <= 3; seed++) {
                    Path in = dir.resolve("in.txt");
                    Path expected = dir.resolve("expected.txt");
                    Path actual = dir.resolve("actual.txt");
                    new DataGenerator().generate(new GeneratorConfig(2000, 150, distribution, seed), in);
                    new InMemoryDeduplicator().dedupe(in, expected);

                    new DistributedDeduplicator(nodes, 1000, new HashPartitionDeduplicator(1000, 4))
                            .dedupe(in, actual);

                    assertEquals(sortedLines(expected), sortedLines(actual),
                            "nodes=" + nodes + " " + distribution + " seed=" + seed);
                }
            }
        }
    }

    @Test
    void worksWithExternalSortOnEveryNode() throws IOException {
        Path in = dir.resolve("in.txt");
        Path expected = dir.resolve("expected.txt");
        Path actual = dir.resolve("actual.txt");
        new DataGenerator().generate(new GeneratorConfig(2000, 150, Distribution.UNIFORM, 1), in);
        new InMemoryDeduplicator().dedupe(in, expected);

        new DistributedDeduplicator(3, 600, new ExternalSortDeduplicator(600, 3)).dedupe(in, actual);

        assertEquals(sortedLines(expected), sortedLines(actual));
    }

    @Test
    void emptyInputGivesEmptyOutput() throws IOException {
        Path in = dir.resolve("in.txt");
        Path out = dir.resolve("out.txt");
        Files.writeString(in, "");

        new DistributedDeduplicator(3, 1000, new HashPartitionDeduplicator(1000, 4)).dedupe(in, out);

        assertEquals(List.of(), sortedLines(out));
    }

    @Test
    void oneNodeSendsNothingOverTheNetwork() throws IOException {
        Path in = dir.resolve("in.txt");
        Path out = dir.resolve("out.txt");
        Files.writeString(in, "a\nb\na\n");
        DistributedDeduplicator job = new DistributedDeduplicator(1, 1000, new HashPartitionDeduplicator(1000, 4));

        job.dedupe(in, out);

        assertEquals(0, job.bytesSent());
    }

    @Test
    void severalNodesSendSomething() throws IOException {
        Path in = dir.resolve("in.txt");
        Path out = dir.resolve("out.txt");
        new DataGenerator().generate(new GeneratorConfig(500, 100, Distribution.UNIFORM, 1), in);
        DistributedDeduplicator job = new DistributedDeduplicator(4, 1000, new HashPartitionDeduplicator(1000, 4));

        job.dedupe(in, out);

        assertTrue(job.bytesSent() > 0);
    }

    @Test
    void clusterFolderIsDeletedAfterTheJob() throws IOException {
        Path in = dir.resolve("in.txt");
        Path out = dir.resolve("out.txt");
        Files.writeString(in, "a\nb\na\n");

        new DistributedDeduplicator(2, 1000, new HashPartitionDeduplicator(1000, 4)).dedupe(in, out);

        try (Stream<Path> entries = Files.list(dir)) {
            assertFalse(entries.anyMatch(p -> p.getFileName().toString().startsWith("cluster-")));
        }
    }

    @Test
    void receivedBytesAddUpToInputSize() throws IOException {
        Path in = dir.resolve("in.txt");
        Path out = dir.resolve("out.txt");
        new DataGenerator().generate(new GeneratorConfig(500, 100, Distribution.UNIFORM, 1), in);
        DistributedDeduplicator job = new DistributedDeduplicator(3, 1000, new HashPartitionDeduplicator(1000, 4));

        job.dedupe(in, out);

        assertEquals(3, job.receivedBytesPerNode().size());
        assertEquals(Files.size(in), job.receivedBytesPerNode().stream().mapToLong(Long::longValue).sum());
    }
}