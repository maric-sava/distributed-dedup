package rs.ac.uns.ftn.dedup.local.sort;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class RunCursorTest {

    @TempDir
    Path dir;

    @Test
    public void walksThroughAllLines() throws IOException {
        Path file = dir.resolve("run.txt");
        Files.writeString(file, "a\nb\n");

        RunCursor cursor = new RunCursor(file);
        assertEquals("a", cursor.currentLine());
        cursor.advance();
        assertEquals("b", cursor.currentLine());
        cursor.advance();
        assertTrue(cursor.isFinished());
        assertNull(cursor.currentLine());
        cursor.close();
    }

    @Test
    public void emptyFileFinishedImmediately() throws IOException {
        Path file = dir.resolve("run.txt");
        Files.writeString(file, "");

        RunCursor cursor = new RunCursor(file);

        assertTrue(cursor.isFinished());
        cursor.close();
    }
}
