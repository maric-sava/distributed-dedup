package rs.ac.uns.ftn.dedup.local.hash;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import rs.ac.uns.ftn.dedup.generator.DataGenerator;
import rs.ac.uns.ftn.dedup.generator.Distribution;
import rs.ac.uns.ftn.dedup.generator.GeneratorConfig;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PartitionerTest {

    @TempDir
    Path dir;

    @Test
    public void everyLineIsInTheBucketItsHashSays() throws Exception {
        Path input = dir.resolve("input.txt");
        new DataGenerator().generate(new GeneratorConfig(1000, 100, Distribution.UNIFORM, 5), input);

        List<Path> buckets = new Partitioner().partition(input, dir.resolve("buckets"), 4, 1);

        assertEquals(4,buckets.size());
        List<String> all = new ArrayList<>();
        for (int i = 0; i < buckets.size(); i++) {
            for (String line : Files.readAllLines(buckets.get(i))) {
                assertEquals(i, LineHash.bucket(line, 1, 4));
                all.add(line);
            }
        }

        List<String> expected = Files.readAllLines(input);
        Collections.sort(expected);
        Collections.sort(all);
        assertEquals(expected, all);
    }

    @Test
    public void everyInputGivesEmptyBuckets() throws Exception {
        Path input = dir.resolve("input.txt");
        Files.writeString(input, "");

        List<Path> buckets = new Partitioner().partition(input, dir.resolve("buckets"), 3, 1);

        assertEquals(3, buckets.size());
        for (Path bucket : buckets) {
            assertEquals(0, Files.size(bucket));
        }
    }
}
