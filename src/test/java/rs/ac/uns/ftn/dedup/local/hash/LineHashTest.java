package rs.ac.uns.ftn.dedup.local.hash;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LineHashTest {

    @Test
    public void sameLineAndSeedGiveSameBucket() {
        assertEquals(LineHash.bucket("user_42", 1, 10), LineHash.bucket("user_42", 1, 10));
    }

    @Test
    public void bucketIsAlwaysInRange() {
        for (int i = 0; i < 10000; i++) {
            int bucket = LineHash.bucket("user_" + i, 3, 7);
            assertTrue(bucket >= 0 && bucket < 7, "out of range " + bucket);
        }
    }

    @Test
    public void spreadLinesEvenly() {
        int[] counts = new int[10];
        for (int i = 0; i < 10000; i++) {
            counts[LineHash.bucket("user_" + i, 7, 10)]++;
        }
        for (int count : counts) {
            assertTrue(count > 800 && count < 1200, "uneven bucket: " + count);
        }
    }

}
