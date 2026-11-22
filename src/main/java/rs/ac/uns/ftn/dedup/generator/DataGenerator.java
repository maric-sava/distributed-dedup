package rs.ac.uns.ftn.dedup.generator;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class DataGenerator {

    public void generate(GeneratorConfig config, Path output) throws IOException {
        if (config.distribution() != Distribution.UNIFORM) {
            throw new UnsupportedOperationException("ZIPF comes in later step");
        }
        UniformDistribution ids = new UniformDistribution(config.distinct(), config.seed());
        try (BufferedWriter writer = Files.newBufferedWriter(output, StandardCharsets.UTF_8)) {
            for (long i = 0; i < config.rows(); i++) {
                writer.write(RecordFormat.toRecord(ids.nextId()));
                writer.write('\n');
            }
        }
    }
}
