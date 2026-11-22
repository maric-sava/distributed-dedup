package rs.ac.uns.ftn.dedup.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MemoryModelTest {

    @Test
    void knownValuesForTenChartLine() {
        assertEquals(60, MemoryModel.sortEntryBytes("user_48213"));     // 10 + 50
        assertEquals(94, MemoryModel.hashEntryBytes("user_48213"));   // 10 + 84
    }

    @Test
    void longerLineCostsMore() {
        assertTrue(MemoryModel.sortEntryBytes("a".repeat(100)) > MemoryModel.sortEntryBytes("a"));
    }

    @Test
    void hashEntryCostMoreThanSortEntry() {
        assertTrue(MemoryModel.hashEntryBytes("user_1") > MemoryModel.sortEntryBytes("user_1"));
    }
}
