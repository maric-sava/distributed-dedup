package rs.ac.uns.ftn.dedup.local.sort;

import rs.ac.uns.ftn.dedup.core.MemoryModel;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Implements the run generation phase of external merge sort.
 * Reads the input in memory-bounded chunks, sorts each chunk
 * in memory and writes it to a separate run file.
 * Duplicate records are preserved during this phase.
 */
public class RunWriter {

    private final long budgetBytes;

    public RunWriter(long budgetBytes) {
        this.budgetBytes = budgetBytes;
    }

    /**
     * Reads records until the estimated memory usage reaches or
     * exceeds the configured budget, or the input is exhausted.
     * Sorts the records and writes them to a single run file.
     * Returns the number of records written, or zero if none remain.
     * The reader retains its position between calls.
     */
    public int writeRun(BufferedReader reader, Path runFile) throws IOException {
        List<String> lines = new ArrayList<>();
        long used = 0;
        String line;
        while (used < budgetBytes && (line = reader.readLine()) != null) {
            lines.add(line);
            // Estimate the memory footprint of the retained record.
            used += MemoryModel.sortEntryBytes(line);
        }
        if(lines.isEmpty()) {
            return 0;
        }
        Collections.sort(lines);
        try (BufferedWriter writer = Files.newBufferedWriter(runFile, StandardCharsets.UTF_8)) {
            for (String l : lines) {
                writer.write(l);
                writer.write("\n");
            }
        }
        return lines.size();
    }

    /**
     * Generates sorted runs until the input is exhausted.
     * Names the files sequentially (run-0.txt, run-1.txt, ...)
     * and returns their paths in creation order.
     */
    public List<Path> writeAllRuns(Path input, Path runDir) throws IOException {
        Files.createDirectories(runDir);
        List<Path> runs = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(input, StandardCharsets.UTF_8)) {
            while (true) {
                Path runFile = runDir.resolve("run-" + runs.size() + ".txt");
                if (writeRun(reader, runFile) == 0) {
                    break;
                }
                runs.add(runFile);
            }
        }
        return runs;
    }
}
