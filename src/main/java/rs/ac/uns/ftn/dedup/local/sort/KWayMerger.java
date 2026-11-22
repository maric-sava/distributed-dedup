package rs.ac.uns.ftn.dedup.local.sort;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public class KWayMerger {

    public void merge(List<Path> runs, Path output) throws IOException {

        List<RunCursor> cursors = new ArrayList<>();
        PriorityQueue<RunCursor> heap = new PriorityQueue<>(Comparator.comparing(RunCursor::currentLine));

        try (BufferedWriter out = Files.newBufferedWriter(output, StandardCharsets.UTF_8)) {
            for (Path run : runs) {
                RunCursor cursor = new RunCursor(run);
                cursors.add(cursor);
                if (!cursor.isFinished()) { // only non-empty files
                    heap.add(cursor);
                }
            }

            String last = null;
            while (!heap.isEmpty()) {
                RunCursor smallest = heap.poll(); // take the currsor with the smallest line
                String line = smallest.currentLine();
                if (!line.equals(last)) {
                    out.write(smallest.currentLine());
                    out.write('\n');
                    last = line;
                }
                smallest.advance();
                if (!smallest.isFinished()) {
                    heap.add(smallest);
                }
            }
        } finally {
            for (RunCursor cursor : cursors) {
                cursor.close();
            }
        }
    }

    public void mergeInPasses(List<Path> runs, Path output, int maxRunsPerMerge) throws IOException {
        if (maxRunsPerMerge < 2) {
            throw new IllegalArgumentException("maxRunsPerMerge must be >= 2");
        }
        Path folder = output.toAbsolutePath().getParent();
        List<Path> current = new ArrayList<>(runs);
        int fileCounter = 0;

        while (current.size() > maxRunsPerMerge) {
            List<Path> next = new ArrayList<>();
            for (int start = 0; start < current.size(); start += maxRunsPerMerge) {
                int end = Math.min(start + maxRunsPerMerge, current.size());
                Path merged = folder.resolve("pass-" + fileCounter + "-" + start + "-" + end + ".txt");
                mergeAndDelete(current.subList(start, end), merged);
                next.add(merged);
            }
            current = next;
            fileCounter++;
        }
        mergeAndDelete(current, output);
    }

    private void mergeAndDelete(List<Path> group, Path target) throws IOException {
        merge(group, target);
        for (Path path : group) {
            Files.delete(path);
        }
    }
}
