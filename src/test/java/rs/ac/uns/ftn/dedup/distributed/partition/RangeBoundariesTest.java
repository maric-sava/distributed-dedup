package rs.ac.uns.ftn.dedup.distributed.partition;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.Test;

class RangeBoundariesTest {

    @Test
    void cutsTheSampleIntoEqualParts() {
        RangeBoundaries ranges = RangeBoundaries.fromSample(List.of("a", "b", "c", "d", "e", "f", "g", "h"), 4);

        assertEquals(List.of("c", "e", "g"), ranges.boundaries());
        assertEquals(0, ranges.ownerOf("a"));
        assertEquals(0, ranges.ownerOf("b"));
        assertEquals(1, ranges.ownerOf("c")); // equal to a cut point: goes to the higher node
        assertEquals(1, ranges.ownerOf("d"));
        assertEquals(2, ranges.ownerOf("e"));
        assertEquals(2, ranges.ownerOf("f"));
        assertEquals(3, ranges.ownerOf("g"));
        assertEquals(3, ranges.ownerOf("h"));
        assertEquals(3, ranges.ownerOf("zzz")); // bigger than everything in the sample
    }

    @Test
    void orderOfTheSampleDoesNotMatter() {
        RangeBoundaries sorted = RangeBoundaries.fromSample(List.of("a", "b", "c", "d"), 2);
        RangeBoundaries shuffled = RangeBoundaries.fromSample(List.of("c", "a", "d", "b"), 2);

        assertEquals(sorted.boundaries(), shuffled.boundaries());
    }

    @Test
    void oneNodeOwnsEverything() {
        RangeBoundaries ranges = RangeBoundaries.fromSample(List.of("a", "b", "c"), 1);

        assertEquals(List.of(), ranges.boundaries());
        assertEquals(0, ranges.ownerOf("anything"));
    }

    @Test
    void emptySampleSendsEverythingToNodeZero() {
        RangeBoundaries ranges = RangeBoundaries.fromSample(List.of(), 3);

        assertEquals(0, ranges.ownerOf("a"));
        assertEquals(0, ranges.ownerOf("z"));
    }

    @Test
    void aVeryFrequentLineTakesSeveralCutPointsAndStaysTogether() {
        // "x" is 6 of 8 sample lines: it cannot be split between nodes
        RangeBoundaries ranges = RangeBoundaries.fromSample(
                List.of("a", "x", "x", "x", "x", "x", "x", "z"), 4);

        assertEquals(List.of("x", "x", "x"), ranges.boundaries());
        assertEquals(0, ranges.ownerOf("a"));
        assertEquals(3, ranges.ownerOf("x")); // always the same node
        assertEquals(3, ranges.ownerOf("z"));
    }

    @Test
    void rejectsZeroNodes() {
        assertThrows(IllegalArgumentException.class, () -> RangeBoundaries.fromSample(List.of("a"), 0));
    }
}