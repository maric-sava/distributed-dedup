package rs.ac.uns.ftn.dedup.distributed.partition;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import rs.ac.uns.ftn.dedup.distributed.Node;

/** Splits a node's file by ranges of lines: part i holds the lines that node i owns. */
public class RangePartitioner implements NodePartitioner {

    private final RangeBoundaries boundaries; // the same for every node, computed from the samples

    public RangePartitioner(RangeBoundaries boundaries) {
        this.boundaries = boundaries;
    }

    @Override
    public List<Path> split(Node source, Path input, int nodeCount) throws IOException {
        if (boundaries.boundaries().size() > nodeCount - 1) {
            throw new IllegalArgumentException("The boundaries were made for more than " + nodeCount + " nodes");
        }
        Path outgoing = source.folder().resolve("outgoing");
        Files.createDirectories(outgoing);

        List<Path> parts = new ArrayList<>();
        List<BufferedWriter> writers = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(input)) {
            for (int i = 0; i < nodeCount; i++) {
                Path part = outgoing.resolve("range-" + i + ".txt");
                parts.add(part);
                writers.add(Files.newBufferedWriter(part));
            }
            String line;
            while ((line = reader.readLine()) != null) {
                BufferedWriter writer = writers.get(boundaries.ownerOf(line));
                writer.write(line);
                writer.write('\n');
            }
        } finally {
            for (BufferedWriter writer : writers) {
                writer.close();
            }
        }
        return parts;
    }
}