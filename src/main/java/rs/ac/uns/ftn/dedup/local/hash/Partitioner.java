package rs.ac.uns.ftn.dedup.local.hash;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Splits a file into bucket files. Equal lines always end up in the same bucket. */
public class Partitioner {

    public List<Path> partition(Path input, Path folder, int buckets, int seed) throws IOException {
        Files.createDirectories(folder);
        List<Path> files = new ArrayList<>();
        List<BufferedWriter> writers = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(input, StandardCharsets.UTF_8)) {
            for (int i = 0; i < buckets; i++) {
                Path file = folder.resolve("bucket-" + i + ".txt");
                files.add(file);
                writers.add(Files.newBufferedWriter(file, StandardCharsets.UTF_8));
            }
            String line;
            while ((line = reader.readLine()) != null) {
                BufferedWriter writer = writers.get(LineHash.bucket(line, seed, buckets));
                writer.write(line);
                writer.write('\n');
            }
        } finally {
            for (BufferedWriter writer : writers) {
                writer.close();
            }
        }
        return files;
    }
}
