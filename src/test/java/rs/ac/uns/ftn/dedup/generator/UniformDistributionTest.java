package rs.ac.uns.ftn.dedup.generator;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UniformDistributionTest {

    @Test
    void sameSeedGivesSameSequence() {
        UniformDistribution a = new UniformDistribution(100, 42);
        UniformDistribution b = new UniformDistribution(100, 42);

        for (int i = 0; i < 1000; i++) {
            assertEquals(a.nextId(), b.nextId());
        }
    }

    @Test
    void idsStayInRange() {
        UniformDistribution d = new UniformDistribution(10, 1);
        for(int i=0; i < 1000; i++) {
            int id = d.nextId();
            assertTrue(id >= 0 && id < 10, "id out of range: " + id);
        }
    }
}
