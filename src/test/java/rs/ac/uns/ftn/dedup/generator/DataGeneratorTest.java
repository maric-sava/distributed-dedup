package rs.ac.uns.ftn.dedup.generator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class DataGeneratorTest {

    @TempDir
    Path dir;

    private final DataGenerator generator = new DataGenerator();

    @Test
    void writesRequestedNumberOfLines() throws IOException {
        Path out = dir.resolve("out.txt");
        generator.generate(new GeneratorConfig(500, 20, Distribution.UNIFORM, 42), out);
        assertEquals(500, Files.readAllLines(out).size());
    }

    @Test
    void zeroRowsGivesEmtyFile() throws IOException {
        Path out = dir.resolve("out.txt");
        generator.generate(new GeneratorConfig(0, 20, Distribution.UNIFORM, 42), out);
        assertEquals(0, Files.size(out));
    }

    @Test
    void sameConfigIdenticalFiles() throws IOException {
        Path out1 = dir.resolve("out1.txt");
        Path out2 = dir.resolve("out2.txt");
        GeneratorConfig config = new GeneratorConfig(500, 20, Distribution.UNIFORM, 42);
        generator.generate(config, out1);
        generator.generate(config, out2);
        assertArrayEquals(Files.readAllBytes(out1), Files.readAllBytes(out2));
    }

    @Test
    void zipfMakesFirstRecordVeryFrequent() throws IOException {
        Path out = dir.resolve("zipf.txt");
        new DataGenerator().generate(new GeneratorConfig(10_000, 100, Distribution.ZIPF, 1), out);

        long firstCount = Files.readAllLines(out).stream().filter(line -> line.equals("user_0")).count();

        // uniform would give about 100 of 10,000; Zipf gives about 1,900
        assertTrue(firstCount > 1000, "user_0 count: " + firstCount);
    }
}
