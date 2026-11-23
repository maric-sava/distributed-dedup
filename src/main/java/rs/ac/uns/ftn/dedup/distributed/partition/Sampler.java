package rs.ac.uns.ftn.dedup.distributed.partition;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Takes a random sample of a file's lines in one pass, using only sampleSize lines of memory. */
public class Sampler {

    private final int sampleSize;
    private final long seed; // the same seed gives the same sample, so experiments can be repeated

    public Sampler(int sampleSize, long seed) {
        this.sampleSize = sampleSize;
        this.seed = seed;
    }

    public List<String> sample(Path file) throws IOException {
        Random random = new Random(seed);
        List<String> reservoir = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(file)) {
            String line;
            long seen = 0; // how many lines were read before this one
            while ((line = reader.readLine()) != null) {
                if (reservoir.size() < sampleSize) {
                    reservoir.add(line);
                } else {
                    long j = random.nextLong(seen + 1); // a random number from 0 to seen
                    if (j < sampleSize) {
                        reservoir.set((int) j, line);
                    }
                }
                seen++;
            }
        }
        return reservoir;
    }
}