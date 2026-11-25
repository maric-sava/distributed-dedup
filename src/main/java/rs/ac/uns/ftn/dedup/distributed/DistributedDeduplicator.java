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
import rs.ac.uns.ftn.dedup.distributed.partition.*;
import rs.ac.uns.ftn.dedup.distributed.setup.InputSpreader;

/**
 * Coordinates distributed deduplication across simulated nodes
 * using configurable partitioning and local deduplication strategies.
 */
public class DistributedDeduplicator implements Deduplicator {

    private final int nodeCount;
    private final long nodeBudgetBytes;
    private final Deduplicator local; // the algorithm every node uses (sort or hash)
    private Transport transport = new Transport();
    private List<Long> receivedBytes = List.of();
    private final boolean useCombiner;
    private long timeMillis;

    // Maximum samples per node; balances sampling accuracy and resource overhead.
    private static final int SAMPLE_SIZE_PER_NODE = 1000;
    // Fixed base seed for reproducibility, offset by node ID to vary sampling across nodes.
    private static final long SAMPLE_SEED = 42;

    private final PartitionStrategy strategy;

    public DistributedDeduplicator(int nodeCount, long nodeBudgetBytes, Deduplicator local) {
        this(nodeCount, nodeBudgetBytes, local, false);
    }

    public DistributedDeduplicator(int nodeCount, long nodeBudgetBytes, Deduplicator local, boolean useCombiner) {
        this(nodeCount, nodeBudgetBytes, local, useCombiner, PartitionStrategy.HASH);
    }

    public DistributedDeduplicator(int nodeCount, long nodeBudgetBytes, Deduplicator local,
            boolean useCombiner, PartitionStrategy strategy) {
        this.nodeCount = nodeCount;
        this.nodeBudgetBytes = nodeBudgetBytes;
        this.local = local;
        this.useCombiner = useCombiner;
        this.strategy = strategy;
    }

    /**
     * Orchestrates distributed deduplication across simulated nodes
     * using the configured partitioning strategy and local
     * deduplication algorithm.
     */
    @Override
    public void dedupe(Path input, Path output) throws IOException {
        long start = System.nanoTime();
        transport = new Transport();
        Path cluster = Files.createTempDirectory(output.toAbsolutePath().getParent(), "cluster-");
        try {
            // 1. Initialize the simulated cluster nodes.
            List<Node> nodes = new ArrayList<>();
            for (int id = 0; id < nodeCount; id++) {
                nodes.add(new Node(id, cluster, nodeBudgetBytes));
            }
            // 2. Simulate the initial distribution of input data.
            new InputSpreader().spread(input, nodes);

            // 3. Optionally remove local duplicates to reduce shuffle volume.
            if (useCombiner) {
                Combiner combiner = new Combiner(local);
                for (Node node : nodes) {
                    combiner.run(node);
                }
            }

            // 4. Select the partitioning strategy and redistribute records
            //    so that identical records reach the same destination node.
            NodePartitioner partitioner = choosePartitioner(nodes);
            new Shuffle(transport, partitioner).run(nodes);

            // Measure the data volume assigned to each destination node.
            receivedBytes = measureReceived(nodes);

            // 5. Deduplicate the records assigned to each destination node.
            DestinationDeduper destination = new DestinationDeduper(local);
            for (Node node : nodes) {
                destination.run(node);
            }

            // 6. Concatenate the deduplicated outputs into the final result.
            combine(nodes, output);
        } finally {
            // 7. Clean up the temporary cluster workspace.
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

    /**
     * Concatenates the already globally deduplicated node outputs
     * into a single result file. This is an output assembly step,
     * not a further deduplication stage.
     */
    private void combine(List<Node> nodes, Path output) throws IOException {
        try (OutputStream out = Files.newOutputStream(output)) {
            for (Node node : nodes) {
                Files.copy(node.outputFile(), out);
            }
        }
    }

    /**
     * Removes the temporary cluster workspace and its contents.
     */
    private void deleteRecursively(Path folder) throws IOException {
        try (Stream<Path> paths = Files.walk(folder)) {
            for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) {
                Files.delete(path); // reverse order: files before the folders that contain them
            }
        }
    }

    /**
     * Selects the configured partitioning strategy.
     * For range partitioning, collects samples from all nodes
     * to calculate shared global range boundaries.
     */
    private NodePartitioner choosePartitioner(List<Node> nodes) throws IOException {
        if (strategy == PartitionStrategy.HASH) {
            return new HashNodePartitioner();
        }
        List<String> samples = new ArrayList<>();
        for (Node node : nodes) {
            // Collect a reproducible, node-specific sample for global range boundary calculation.
            samples.addAll(new Sampler(SAMPLE_SIZE_PER_NODE, SAMPLE_SEED + node.id()).sample(node.inputFile()));
        }
        return new RangePartitioner(RangeBoundaries.fromSample(samples, nodes.size()));
    }
}