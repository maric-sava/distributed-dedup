package rs.ac.uns.ftn.dedup.distributed.partition;

import rs.ac.uns.ftn.dedup.distributed.Node;
import rs.ac.uns.ftn.dedup.local.hash.Partitioner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class HashNodePartitioner implements NodePartitioner {

    public static final int SHUFFLE_SEED = 1000;

    private final Partitioner partitioner = new Partitioner();

    @Override
    public List<Path> split(Node source, Path input, int nodeCount) throws IOException {
        Path outgoing = source.folder().resolve("outgoing");
        Files.createDirectories(outgoing);
        return partitioner.partition(input, outgoing, nodeCount, SHUFFLE_SEED);
    }
}
