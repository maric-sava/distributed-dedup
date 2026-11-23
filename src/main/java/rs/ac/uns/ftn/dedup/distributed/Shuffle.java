package rs.ac.uns.ftn.dedup.distributed;

import rs.ac.uns.ftn.dedup.distributed.partition.HashNodePartitioner;
import rs.ac.uns.ftn.dedup.distributed.partition.NodePartitioner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/** Every node splits its input and sends part i to node i. */
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

    private void keepLocally(Path part, Node node) throws IOException {
        Files.move(part, node.incomingFolder().resolve("from-" + node.id() + part.getFileName()));
    }
}
