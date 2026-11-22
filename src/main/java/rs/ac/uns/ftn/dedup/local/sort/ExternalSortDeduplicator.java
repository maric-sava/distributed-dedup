package rs.ac.uns.ftn.dedup.local.sort;

import rs.ac.uns.ftn.dedup.core.Deduplicator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

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
            List<Path> runs = new RunWriter(budgetBytes).writeAllRuns(input, runDir);
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
