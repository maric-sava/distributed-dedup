package rs.ac.uns.ftn.dedup.distributed;

import java.util.List;

/** The measurements of one distributed job. */
public record JobStats(long bytesSent, List<Long> receivedBytesPerNode, long timeMillis) {

    public JobStats {
        receivedBytesPerNode = List.copyOf(receivedBytesPerNode); // keeps the record read-only
    }

    public long totalReceivedBytes() {
        return receivedBytesPerNode.stream().mapToLong(Long::longValue).sum();
    }

    /** Busiest node divided by the average node. 1.0 means perfectly even. */
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