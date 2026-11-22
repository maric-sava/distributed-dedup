package rs.ac.uns.ftn.dedup.generator;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;


public class GeneratorConfigTest {

    @Test
    void acceptsValidConfig() {
        assertDoesNotThrow(() -> new GeneratorConfig(1000, 50, Distribution.UNIFORM, 42));
    }

    @Test
    void rejectsNegativeRows() {
        assertThrows(IllegalArgumentException.class, () -> new GeneratorConfig(-1, 50, Distribution.UNIFORM, 42));
    }

    @Test
    void rejectsZeroDistinct() {
        assertThrows(IllegalArgumentException.class, () -> new GeneratorConfig(1000, 0, Distribution.UNIFORM, 42));
    }

    @Test
    void rejectsNullDistribution() {
        assertThrows(IllegalArgumentException.class, () -> new GeneratorConfig(1000, 50, null, 42));
    }

}
