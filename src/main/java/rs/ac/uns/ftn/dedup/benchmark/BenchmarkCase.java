package rs.ac.uns.ftn.dedup.benchmark;

import java.nio.file.Path;
import rs.ac.uns.ftn.dedup.core.Deduplicator;
import rs.ac.uns.ftn.dedup.distributed.DistributedDeduplicator;
import rs.ac.uns.ftn.dedup.distributed.partition.PartitionStrategy;
import rs.ac.uns.ftn.dedup.local.hash.HashPartitionDeduplicator;
import rs.ac.uns.ftn.dedup.local.sort.ExternalSortDeduplicator;

/** One configuration to measure. */
public record BenchmarkCase(Path input, int nodes, LocalAlgorithm local, boolean combiner,
                            PartitionStrategy strategy, long budgetBytes, int buckets) {

    private static final int MAX_RUNS_PER_MERGE = 10;

    /** Creates a fresh job. A job keeps the statistics of its last run, so every run gets its own. */
    public DistributedDeduplicator createJob() {
        Deduplicator algorithm = switch (local) {
            case HASH -> new HashPartitionDeduplicator(budgetBytes, buckets);
            case SORT -> new ExternalSortDeduplicator(budgetBytes, MAX_RUNS_PER_MERGE);
        };
        return new DistributedDeduplicator(nodes, budgetBytes, algorithm, combiner, strategy);
    }
}