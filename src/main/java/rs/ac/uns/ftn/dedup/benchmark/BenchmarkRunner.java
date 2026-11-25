package rs.ac.uns.ftn.dedup.benchmark;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import rs.ac.uns.ftn.dedup.distributed.DistributedDeduplicator;
import rs.ac.uns.ftn.dedup.distributed.JobStats;

/**
 * Executes benchmark configurations with configurable
 * warmup and measurement runs.
 * Records measured job statistics in a CSV file.
 */
public class BenchmarkRunner {

    private final ResultsCsv csv;
    private final Path workFolder; // where the job output goes; it is deleted after every run
    private final int warmups;     // runs that are executed but not recorded
    private final int repeats;     // runs that are recorded

    public BenchmarkRunner(ResultsCsv csv, Path workFolder, int warmups, int repeats) {
        this.csv = csv;
        this.workFolder = workFolder;
        this.warmups = warmups;
        this.repeats = repeats;
    }

    public void run(List<BenchmarkCase> cases) throws IOException {
        Files.createDirectories(workFolder);
        for (int i = 0; i < cases.size(); i++) {
            BenchmarkCase benchmarkCase = cases.get(i);
            System.out.println("case " + (i + 1) + "/" + cases.size() + ": " + benchmarkCase);
            for (int run = 0; run < warmups + repeats; run++) {
                JobStats stats = runOnce(benchmarkCase);
                if (run >= warmups) {
                    csv.append(benchmarkCase, run - warmups + 1, stats); // recorded runs are numbered from 1
                }
            }
        }
    }

    private JobStats runOnce(BenchmarkCase benchmarkCase) throws IOException {
        Path output = workFolder.resolve("benchmark-output.txt");
        DistributedDeduplicator job = benchmarkCase.createJob();
        try {
            job.dedupe(benchmarkCase.input(), output);
            return job.stats();
        } finally {
            Files.deleteIfExists(output);
        }
    }
}