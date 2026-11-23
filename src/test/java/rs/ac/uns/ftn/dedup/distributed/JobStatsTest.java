package rs.ac.uns.ftn.dedup.distributed;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;

class JobStatsTest {

    @Test
    void equalLoadsGiveImbalanceOne() {
        JobStats stats = new JobStats(0, List.of(100L, 100L, 100L, 100L), 0);

        assertEquals(1.0, stats.imbalance(), 1e-9);
    }

    @Test
    void unevenLoadsGiveBusiestOverAverage() {
        JobStats stats = new JobStats(0, List.of(1L, 3L), 0); // max 3, average 2

        assertEquals(1.5, stats.imbalance(), 1e-9);
    }

    @Test
    void nothingReceivedDoesNotDivideByZero() {
        JobStats stats = new JobStats(0, List.of(0L, 0L, 0L), 0);

        assertEquals(1.0, stats.imbalance(), 1e-9);
    }

    @Test
    void totalAddsUp() {
        JobStats stats = new JobStats(0, List.of(1L, 2L, 3L), 0);

        assertEquals(6, stats.totalReceivedBytes());
    }
}