package rs.ac.uns.ftn.dedup.distributed.setup;

import rs.ac.uns.ftn.dedup.distributed.Node;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Simulates an initially distributed dataset by spreading
 * records across nodes in a round-robin manner.
 * Used for simulation setup, independently of deduplication.
 */
public class InputSpreader {

    /**
     * Streams the input file and distributes its records evenly
     * across nodes without loading the entire dataset into memory.
     */
    public void spread(Path input, List<Node> nodes) throws IOException {
        List<BufferedWriter> writers = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(input)) {
            for (Node node : nodes) {
                writers.add(Files.newBufferedWriter(node.inputFile()));
            }
            long count = 0;
            String line;
            while ((line = reader.readLine()) != null) {
                BufferedWriter writer = writers.get((int) (count % nodes.size()));
                writer.write(line);
                writer.write('\n');
                count++;
            }
        } finally {
            for (BufferedWriter writer : writers) {
                writer.close();
            }
        }
    }
}
