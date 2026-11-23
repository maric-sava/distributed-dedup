package rs.ac.uns.ftn.dedup.distributed;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import rs.ac.uns.ftn.dedup.core.Deduplicator;
import rs.ac.uns.ftn.dedup.distributed.setup.InputSpreader;

/** Distributed deduplication on simulated nodes: spread, shuffle by hash, dedupe on every node. */
public class DistributedDeduplicator implements Deduplicator {

    private final int nodeCount;
    private final long nodeBudgetBytes;
    private final Deduplicator local; // the algorithm every node uses (sort or hash)
    private Transport transport = new Transport();
    private List<Long> receivedBytes = List.of();
    private final boolean useCombiner;
    private long timeMillis;

    public DistributedDeduplicator(int nodeCount, long nodeBudgetBytes, Deduplicator local) {
        this(nodeCount, nodeBudgetBytes, local, false);
    }

    public DistributedDeduplicator(int nodeCount, long nodeBudgetBytes, Deduplicator local, boolean useCombiner) {
        this.nodeCount = nodeCount;
        this.nodeBudgetBytes = nodeBudgetBytes;
        this.local = local;
        this.useCombiner = useCombiner;
    }

    @Override
    public void dedupe(Path input, Path output) throws IOException {
        long start = System.nanoTime();
        transport = new Transport();
        Path cluster = Files.createTempDirectory(output.toAbsolutePath().getParent(), "cluster-");
        try {
            // 1. create the computers
            List<Node> nodes = new ArrayList<>();
            for (int id = 0; id < nodeCount; id++) {
                nodes.add(new Node(id, cluster, nodeBudgetBytes));
            }
            // 2. put the data on them
            new InputSpreader().spread(input, nodes);

            // 2a. deduplicate each locally if using combiner
            if (useCombiner) {
                Combiner combiner = new Combiner(local);
                for (Node node : nodes) {
                    combiner.run(node); // each node removes its own duplicates before sending
                }
            }

            // 3. move records to their owner
            new Shuffle(transport).run(nodes);

            receivedBytes = measureReceived(nodes);

            // 4. each computer dedupes
            DestinationDeduper destination = new DestinationDeduper(local);
            for (Node node : nodes) {
                destination.run(node);
            }

            // 5. join the results
            combine(nodes, output);
        } finally {
            deleteRecursively(cluster);
            timeMillis = (System.nanoTime() - start) / 1_000_000;
        }
    }

    /** How many bytes travelled between nodes in the last run. */
    public long bytesSent() {
        return transport.bytesSent();
    }

    /** How many bytes each node received in the shuffle (including the part it kept itself). */
    public List<Long> receivedBytesPerNode() {
        return receivedBytes;
    }

    /** The measurements of the last job. */
    public JobStats stats() {
        return new JobStats(transport.bytesSent(), receivedBytes, timeMillis);
    }

    private List<Long> measureReceived(List<Node> nodes) throws IOException {
        List<Long> sizes = new ArrayList<>();
        for (Node node : nodes) {
            long total = 0;
            try (Stream<Path> files = Files.list(node.incomingFolder())) {
                for (Path file : files.toList()) {
                    total += Files.size(file);
                }
            }
            sizes.add(total);
        }
        return sizes;
    }

    private void combine(List<Node> nodes, Path output) throws IOException {
        try (OutputStream out = Files.newOutputStream(output)) {
            for (Node node : nodes) {
                Files.copy(node.outputFile(), out);
            }
        }
    }

    private void deleteRecursively(Path folder) throws IOException {
        try (Stream<Path> paths = Files.walk(folder)) {
            for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) {
                Files.delete(path); // reverse order: files before the folders that contain them
            }
        }
    }
}