package rs.ac.uns.ftn.dedup.distributed;

import rs.ac.uns.ftn.dedup.distributed.partition.HashNodePartitioner;
import rs.ac.uns.ftn.dedup.distributed.partition.NodePartitioner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Redistributes data across nodes using the selected partitioning strategy,
 * ensuring that identical records are routed to the same destination.
 */
public class Shuffle {

    private final NodePartitioner partitioner;
    private final Transport transport;

    public Shuffle(Transport transport) {
        this(transport, new HashNodePartitioner()); // default strategy
    }

    public Shuffle(Transport transport, NodePartitioner partitioner) {
        this.transport = transport;
        this.partitioner = partitioner;
    }

    /**
     * Splits each source node's input into destination-specific partitions
     * and delivers each partition to its corresponding node.
     */
    public void run(List<Node> nodes) throws IOException {
        for (Node source : nodes) {
            List<Path> parts = partitioner.split(source, source.inputFile(), nodes.size());
            for (int i = 0; i < parts.size(); i++) {
                Node destination = nodes.get(i);
                if (destination == source) {
                    keepLocally(parts.get(i), source);
                } else {
                    transport.send(parts.get(i), source, destination);
                }
            }
        }
    }

    /**
     * Retains a partition on its source node without simulated
     * network transfer or associated transfer costs.
     */
    private void keepLocally(Path part, Node node) throws IOException {
        Files.move(part, node.incomingFolder().resolve("from-" + node.id() + "-" + part.getFileName()));
    }
}
