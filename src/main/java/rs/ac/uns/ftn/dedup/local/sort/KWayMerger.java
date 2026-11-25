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

/**
 * Merges sorted runs using a min-heap while eliminating duplicate
 * records. Supports multi-pass merging to limit the number of
 * simultaneously open run files.
 */
public class KWayMerger {

    /**
     * Merges the given sorted runs in a single pass using a min-heap.
     * Maintains one cursor per run and eliminates consecutive duplicates
     * while producing a globally sorted output.
     */
    public void merge(List<Path> runs, Path output) throws IOException {

        List<RunCursor> cursors = new ArrayList<>();

        // Order cursors by the smallest unprocessed record in each run.
        PriorityQueue<RunCursor> heap = new PriorityQueue<>(Comparator.comparing(RunCursor::currentLine));

        try (BufferedWriter out = Files.newBufferedWriter(output, StandardCharsets.UTF_8)) {
            for (Path run : runs) {
                RunCursor cursor = new RunCursor(run);
                // Track all cursors for resource cleanup.
                cursors.add(cursor);
                // Only include non-exhausted runs in the heap.
                if (!cursor.isFinished()) {
                    heap.add(cursor);
                }
            }

            String last = null;
            while (!heap.isEmpty()) {
                // Retrieve the cursor with the smallest remaining record.
                RunCursor smallest = heap.poll();
                String line = smallest.currentLine();
                // Identical records are consecutive in the sorted stream,
                // so only the last written record needs to be retained.
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

    /**
     * Merges sorted runs in multiple passes using a bounded merge fan-in.
     * Each intermediate pass reduces the number of remaining runs.
     * Input runs are deleted after their successful merge.
     */
    public void mergeInPasses(List<Path> runs, Path output, int maxRunsPerMerge) throws IOException {
        if (maxRunsPerMerge < 2) {
            throw new IllegalArgumentException("maxRunsPerMerge must be >= 2");
        }
        Path folder = output.toAbsolutePath().getParent();
        List<Path> current = new ArrayList<>(runs);
        int passIndex = 0;

        // Perform intermediate merge passes until the remaining runs
        // can be merged without exceeding the configured merge fan-in.
        while (current.size() > maxRunsPerMerge) {
            List<Path> next = new ArrayList<>();
            for (int start = 0; start < current.size(); start += maxRunsPerMerge) {
                int end = Math.min(start + maxRunsPerMerge, current.size());
                Path merged = folder.resolve("pass-" + passIndex + "-" + start + "-" + end + ".txt");
                // Merge a bounded group of runs and delete the consumed files.
                mergeAndDelete(current.subList(start, end), merged);
                next.add(merged);
            }
            // Use the newly generated runs as input for the next pass.
            current = next;
            passIndex++;
        }
        //  at most maxRunsPerMerge runs left: the final merge
        mergeAndDelete(current, output);
    }

    // Merges a group of runs into target, then deletes the group's files.
    private void mergeAndDelete(List<Path> group, Path target) throws IOException {
        merge(group, target);
        for (Path path : group) {
            Files.delete(path);
        }
    }
}
