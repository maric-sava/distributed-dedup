package rs.ac.uns.ftn.dedup.generator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ZipfDistributionTest {

    @Test
    void sameSeedGivesSameSequence() {
        ZipfDistribution a = new ZipfDistribution(100, 42, 1.0);
        ZipfDistribution b = new ZipfDistribution(100, 42, 1.0);
        for (int i = 0; i < 1000; i++) {
            assertEquals(a.nextId(), b.nextId());
        }
    }

    @Test
    void idsStayInRange() {
        ZipfDistribution zipf = new ZipfDistribution(50, 1, 1.0);
        for (int i = 0; i < 10_000; i++) {
            int id = zipf.nextId();
            assertTrue(id >= 0 && id < 50);
        }
    }

    @Test
    void firstIdIsMuchMoreFrequentThanLast() {
        ZipfDistribution zipf = new ZipfDistribution(1000, 7, 1.0);
        int[] counts = new int[1000];
        for (int i = 0; i < 100_000; i++) {
            counts[zipf.nextId()]++;
        }
        // id 0 should get about 13% = 13,000 rows; the last id only about 13
        assertTrue(counts[0] > 10_000, "id 0 count: " + counts[0]);
        assertTrue(counts[0] > 100 * counts[999], "id 0: " + counts[0] + ", last: " + counts[999]);
    }
}