package rs.ac.uns.ftn.dedup.local.sort;

import rs.ac.uns.ftn.dedup.core.Deduplicator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

/**
 * Performs external merge sort-based deduplication in two phases:
 * 1. Run generation: sorts input chunks in memory according to
 *    an estimated memory budget and writes them to disk.
 * 2. Merge: combines sorted runs using bounded multi-pass merging
 *    while eliminating duplicate records.
 *
 * Produces a globally sorted, deduplicated output file.
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
