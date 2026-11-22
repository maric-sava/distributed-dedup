package rs.ac.uns.ftn.dedup.local.sort;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * A bookmark in one sorted run file. It remembers the line we are currently
 * looking at, and can move on to the next one.
 * The merge keeps one cursor per run and always asks: "which cursor is on the smallest line?"
 */
public class RunCursor {

    public final BufferedReader reader;
    private String currentLine;

    public RunCursor(Path file) throws IOException {
        this.reader = Files.newBufferedReader(file, StandardCharsets.UTF_8);
        this.currentLine = reader.readLine();
    }

    public String currentLine() {
        return currentLine;
    }

    public boolean isFinished() {
        return currentLine == null;
    }

    public void advance() throws IOException {
        currentLine = reader.readLine();
    }

    public void close() throws IOException {
        reader.close();
    }
}
