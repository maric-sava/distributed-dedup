package rs.ac.uns.ftn.dedup.benchmark;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Locale;
import rs.ac.uns.ftn.dedup.distributed.JobStats;

/** Appends one row per benchmark run to a CSV file. */
public class ResultsCsv {

    public static final String HEADER =
            "input,nodes,local,combiner,strategy,budgetBytes,buckets,run,timeMs,bytesSent,imbalance";

    private final Path file;

    public ResultsCsv(Path file) throws IOException {
        this.file = file;
        if (!Files.exists(file)) {
            Files.createDirectories(file.toAbsolutePath().getParent());
            Files.writeString(file, HEADER + "\n");
        }
    }

    /** The input is written as a file name only (a full path could contain commas). */
    public void append(BenchmarkCase benchmarkCase, int run, JobStats stats) throws IOException {
        String row = String.join(",",
                benchmarkCase.input().getFileName().toString(),
                String.valueOf(benchmarkCase.nodes()),
                benchmarkCase.local().name(),
                String.valueOf(benchmarkCase.combiner()),
                benchmarkCase.strategy().name(),
                String.valueOf(benchmarkCase.budgetBytes()),
                String.valueOf(benchmarkCase.buckets()),
                String.valueOf(run),
                String.valueOf(stats.timeMillis()),
                String.valueOf(stats.bytesSent()),
                String.format(Locale.ROOT, "%.3f", stats.imbalance())); // dot, not comma, as the decimal mark
        Files.writeString(file, row + "\n", StandardOpenOption.APPEND);
    }
}