package rs.ac.uns.ftn.dedup.local.sort;

import rs.ac.uns.ftn.dedup.core.Deduplicator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

/**
 * External merge sort, in two phases:
 *   1. Make runs: read as many lines as fit in the memory budget, sort them,
 *      write them to a file (a "run"). Repeat until the input ends.
 *   2. Merge the runs into one sorted file, writing each line only once.
 * Duplicates end up next to each other in sorted order, so the merge can drop them.
 */
public class ExternalSortDeduplicator implements Deduplicator {

    private final long budgetBytes;
    private final int maxRunsPerMerge;

    public ExternalSortDeduplicator(long budgetBytes, int maxRunsPerMerge) {
        this.budgetBytes = budgetBytes;
        this.maxRunsPerMerge = maxRunsPerMerge;
    }

    @Override
    public void dedupe(Path input, Path output) throws IOException {
        Path runDir = Files.createTempDirectory(output.toAbsolutePath().getParent(), "runs-");
        try {
            // Phase 1: split the input into sorted runs.
            List<Path> runs = new RunWriter(budgetBytes).writeAllRuns(input, runDir);
            // Phase 2: merge the runs into the output (in several passes if there are many).
            new KWayMerger().mergeInPasses(runs, output, maxRunsPerMerge);
        } finally {
            deleteFolder(runDir);
        }
    }

    private void deleteFolder(Path folder) throws IOException {
        try (Stream<Path> leftovers = Files.list(folder)) {
            for (Path leftover : leftovers.toList()) {
                Files.delete(leftover);
            }
        }
        Files.delete(folder);
    }
}
