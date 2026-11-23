package rs.ac.uns.ftn.dedup.distributed.partition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Cut points that divide the lines into ranges, one range per node. */
public class RangeBoundaries {

    private final List<String> boundaries; // sorted, nodeCount - 1 of them

    private RangeBoundaries(List<String> boundaries) {
        this.boundaries = boundaries;
    }

    /** Cuts the sorted sample into nodeCount groups of about the same size. */
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

    /** The node that owns this line: how many cut points are smaller than or equal to it. */
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