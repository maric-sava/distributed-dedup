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
 * Phase 2 of external sort: merges sorted runs into one sorted file without duplicates.
 * Example:  run 0: b c d,  run 1: a e f,  run 2: b   ->   a b c d e f
 */
public class KWayMerger {

    /**
     * Merges ALL the given runs in one go.
     * Idea: keep one cursor per run. Repeatedly take the cursor on the smallest line,
     * write that line, move that cursor forward.
     */
    public void merge(List<Path> runs, Path output) throws IOException {

        List<RunCursor> cursors = new ArrayList<>();

        // The heap is a box of cursors that always gives out the cursor on the SMALLEST line.
        // "Comparator.comparing(c -> c.currentLine())" tells it how to order cursors.
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

    /**
     * Merges many runs in PASSES, so that at most maxRunsPerMerge files are open at once.
     * Example with 5 runs and maxRunsPerMerge = 2:
     *   pass 0: (r0,r1) (r2,r3) (r4)  ->  3 bigger runs
     *   pass 1: (m0,m1) (m2)          ->  2 bigger runs
     *   final:  (m0,m1)               ->  the output
     * The input runs are deleted as they are merged.
     */
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
        mergeAndDelete(current, output);    // at most maxRunsPerMerge runs left: the final merge
    }

    // Merges a group of runs into target, then deletes the group's files.
    private void mergeAndDelete(List<Path> group, Path target) throws IOException {
        merge(group, target);
        for (Path path : group) {
            Files.delete(path);
        }
    }
}
