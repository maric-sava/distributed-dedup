package rs.ac.uns.ftn.dedup.distributed;

import java.util.List;

/**
 * Captures distributed job metrics, including simulated
 * data transfer volume, per-node received data volume
 * and execution time.
 */
public record JobStats(long bytesSent, List<Long> receivedBytesPerNode, long timeMillis) {

    public JobStats {
        receivedBytesPerNode = List.copyOf(receivedBytesPerNode); // keeps the record read-only
    }

    public long totalReceivedBytes() {
        return receivedBytesPerNode.stream().mapToLong(Long::longValue).sum();
    }

    /**
     * Calculates the ratio of the maximum to the average
     * received data volume per node.
     * A value of 1.0 indicates perfect partition balance.
     */
    public double imbalance() {
        long total = totalReceivedBytes();
        if (total == 0) {
            return 1.0; // nothing was received, so nothing is uneven
        }
        long max = receivedBytesPerNode.stream().mapToLong(Long::longValue).max().orElse(0);
        double average = total / (double) receivedBytesPerNode.size();
        return max / average;
    }
}