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

public class RunWriter {

    private final long budgetBytes;

    public RunWriter(long budgetBytes) {
        this.budgetBytes = budgetBytes;
    }

    public int writeRun(BufferedReader reader, Path runFile) throws IOException {
        List<String> lines = new ArrayList<>();
        long used = 0;
        String line;
        while (used < budgetBytes && (line = reader.readLine()) != null) {
            lines.add(line);
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
