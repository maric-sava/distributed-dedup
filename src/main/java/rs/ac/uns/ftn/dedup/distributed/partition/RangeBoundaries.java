package rs.ac.uns.ftn.dedup.distributed.partition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Calculates and stores global range boundaries from the combined samples
 * of all nodes. For N nodes, N-1 cut points are selected from the sorted
 * sample to divide the data into approximately balanced ranges.
 *
 * All nodes use the same boundaries to determine the destination node
 * for each input line, ensuring that identical lines are routed to
 * the same node for subsequent deduplication.
 */
public class RangeBoundaries {

    private final List<String> boundaries; // sorted, nodeCount - 1 of them

    private RangeBoundaries(List<String> boundaries) {
        this.boundaries = List.copyOf(boundaries);
    }

    /**
     * Sorts the combined sample and selects nodeCount - 1 cut points
     * at evenly spaced positions to approximate balanced data ranges.
     * Duplicate cut points may occur when the sample contains frequent
     * identical values, resulting in empty or unevenly sized partitions.
     * This affects load balancing but not deduplication correctness.
     */
    public static RangeBoundaries fromSample(List<String> sample, int nodeCount) {
        if (nodeCount < 1) {
            throw new IllegalArgumentException("nodeCount must be at least 1: " + nodeCount);
        }
        List<String> sorted = new ArrayList<>(sample);
        Collections.sort(sorted);

        List<String> cuts = new ArrayList<>();
        if (!sorted.isEmpty()) {
            for (int i = 1; i < nodeCount; i++) {
                cuts.add(sorted.get(i * sorted.size() / nodeCount));
            }
        }
        return new RangeBoundaries(cuts);
    }

    /**
     * Determines the destination node by counting the cut points
     * smaller than or equal to the line. Each crossed boundary
     * increments the node ID, with equal values routed to the right.
     */
    public int ownerOf(String line) {
        int owner = 0;
        while (owner < boundaries.size() && line.compareTo(boundaries.get(owner)) >= 0) {
            owner++;
        }
        return owner;
    }

    public List<String> boundaries() {
        return List.copyOf(boundaries);
    }
}