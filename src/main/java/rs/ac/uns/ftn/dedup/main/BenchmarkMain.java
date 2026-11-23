package rs.ac.uns.ftn.dedup.main;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import rs.ac.uns.ftn.dedup.benchmark.BenchmarkCase;
import rs.ac.uns.ftn.dedup.benchmark.BenchmarkRunner;
import rs.ac.uns.ftn.dedup.benchmark.LocalAlgorithm;
import rs.ac.uns.ftn.dedup.benchmark.ResultsCsv;
import rs.ac.uns.ftn.dedup.distributed.partition.PartitionStrategy;

/** Usage: BenchmarkMain <dataFolder> <resultsCsv> [warmups] [repeats]. Defaults: 1 warm-up, 5 recorded runs. */
public class BenchmarkMain {

    private static final long BUDGET_BYTES = 1_000_000;
    private static final int BUCKETS = 16;

    public static void main(String[] args) throws IOException {
        if (args.length < 2 || args.length > 4) {
            System.err.println("Usage: BenchmarkMain <dataFolder> <resultsCsv> [warmups] [repeats]");
            System.exit(1);
        }
        Path dataFolder = Path.of(args[0]);
        Path csvFile = Path.of(args[1]);
        int warmups = args.length > 2 ? Integer.parseInt(args[2]) : 1;
        int repeats = args.length > 3 ? Integer.parseInt(args[3]) : 5;

        List<Path> inputs = List.of(
                dataFolder.resolve("users.txt"),
                dataFolder.resolve("users-zipf.txt"),
                dataFolder.resolve("users-unique.txt"));
        for (Path input : inputs) {
            if (!Files.exists(input)) {
                System.err.println("Missing input file: " + input);
                System.exit(1);
            }
        }

        List<BenchmarkCase> cases = buildGrid(inputs);
        ResultsCsv csv = new ResultsCsv(csvFile); // also creates the results folder
        writeEnvironment(csvFile.resolveSibling(csvFile.getFileName() + ".env.txt"), warmups, repeats, cases.size());

        new BenchmarkRunner(csv, dataFolder.resolve("benchmark-work"), warmups, repeats).run(cases);
        System.out.println("Done: " + cases.size() + " cases, results in " + csvFile);
    }

    private static List<BenchmarkCase> buildGrid(List<Path> inputs) {
        List<BenchmarkCase> cases = new ArrayList<>();
        for (Path input : inputs) {
            for (int nodes : new int[] {1, 2, 4, 8, 16}) {
                for (boolean combiner : new boolean[] {false, true}) {
                    for (PartitionStrategy strategy : PartitionStrategy.values()) {
                        if (nodes == 1 && strategy == PartitionStrategy.RANGE) {
                            continue; // with one node the strategy makes no difference
                        }
                        cases.add(new BenchmarkCase(input, nodes, LocalAlgorithm.HASH, combiner, strategy,
                                BUDGET_BYTES, BUCKETS));
                    }
                }
            }
        }
        // the sort algorithm as the local algorithm, to compare with hash in the distributed setting
        for (Path input : inputs) {
            for (boolean combiner : new boolean[] {false, true}) {
                cases.add(new BenchmarkCase(input, 4, LocalAlgorithm.SORT, combiner, PartitionStrategy.HASH,
                        BUDGET_BYTES, BUCKETS));
            }
        }
        return cases;
    }

    /** The conditions of the measurement, saved next to the results: they belong in the thesis. */
    private static void writeEnvironment(Path file, int warmups, int repeats, int caseCount) throws IOException {
        String text = String.join("\n",
                "date=" + Instant.now(),
                "java.version=" + System.getProperty("java.version"),
                "os=" + System.getProperty("os.name") + " " + System.getProperty("os.version")
                        + " " + System.getProperty("os.arch"),
                "cpus=" + Runtime.getRuntime().availableProcessors(),
                "maxHeapBytes=" + Runtime.getRuntime().maxMemory(),
                "budgetBytes=" + BUDGET_BYTES,
                "buckets=" + BUCKETS,
                "warmups=" + warmups,
                "repeats=" + repeats,
                "cases=" + caseCount) + "\n";
        Files.writeString(file, text);
    }
}