package rs.ac.uns.ftn.dedup.generator;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Generates reproducible datasets according to a configured
 * probability distribution.
 * Writes records sequentially without retaining the entire
 * dataset in memory.
 */
public class DataGenerator {

    // Controls the degree of skew in the Zipf distribution.
    // Higher values increase the probability of selecting lower-ranked IDs.
    private static final double ZIPF_EXPONENT = 1.0;

    public void generate(GeneratorConfig config, Path output) throws IOException {
        IdSource ids = switch (config.distribution()) {
            case UNIFORM -> new UniformDistribution(config.distinct(), config.seed());
            case ZIPF -> new ZipfDistribution(config.distinct(), config.seed(), ZIPF_EXPONENT);
        };
        try (BufferedWriter writer = Files.newBufferedWriter(output)) {
            for (long i = 0; i < config.rows(); i++) {
                writer.write(RecordFormat.toRecord(ids.nextId()));
                writer.write('\n');
            }
        }
    }
}