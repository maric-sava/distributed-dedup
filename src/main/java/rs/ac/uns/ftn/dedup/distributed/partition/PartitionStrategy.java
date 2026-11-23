package rs.ac.uns.ftn.dedup.distributed.partition;

/** How lines are assigned to nodes. */
public enum PartitionStrategy {
    HASH,
    RANGE
}