package rs.ac.uns.ftn.dedup.local.hash;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class BucketDeduperTest {

    @TempDir
    Path dir;

    private Path bucketWith(String content) throws IOException {
        Path bucket = dir.resolve("bucket.txt");
        Files.writeString(bucket, content);
        return bucket;
    }

    private List<String> sortedLines(StringWriter out) {
        return out.toString().lines().sorted().toList();
    }

    @Test
    public void dedupesWhenItFits() throws IOException {
        StringWriter out = new StringWriter();
        assertTrue(new BucketDeduper(1000).tryDedupe(bucketWith("a\nb\na\n"), out));
        assertEquals(List.of("a", "b"), sortedLines(out));
    }

    @Test
    public void givesUpAndWritesNothingWhenTooBig() throws IOException {
        StringWriter out = new StringWriter();
        // each one-letter line costs 85 bytes (1 + 84); two distinct lines are 170 > 100
        assertFalse(new BucketDeduper(100).tryDedupe(bucketWith("a\nb\nc\n"), out));
        assertEquals("", out.toString());
    }

    @Test
    public void duplicatesDoNotUseBudget() throws IOException {
        StringWriter out = new StringWriter();
        assertTrue(new BucketDeduper(100).tryDedupe(bucketWith("a\n".repeat(1000)), out));
        assertEquals(List.of("a"), sortedLines(out));
    }

    @Test
    public void singleLineLargerThanBudgetIsAccepted() throws IOException {
        StringWriter out = new StringWriter();
        String longLine = "x".repeat(1000);
        assertTrue(new BucketDeduper(100).tryDedupe(bucketWith(longLine + "\n"), out));
        assertEquals(List.of(longLine), sortedLines(out));
    }
}
