package rs.ac.uns.ftn.dedup.main;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import rs.ac.uns.ftn.dedup.core.Deduplicator;
import rs.ac.uns.ftn.dedup.local.hash.HashPartitionDeduplicator;
import rs.ac.uns.ftn.dedup.local.sort.ExternalSortDeduplicator;

/** Usage: CompareMain <input> <repeats>. Prints median time of both algorithms for several budgets. */
public class CompareMain {

    public static void main(String[] args) throws IOException {
        Path input = Path.of(args[0]);
        int repeats = Integer.parseInt(args[1]);
        long[] budgets = {100_000, 300_000, 1_000_000, 3_000_000, 10_000_000};

        System.out.println("budget,sort_ms,hash_ms");
        for (long budget : budgets) {
            long sortMs = median(new ExternalSortDeduplicator(budget, 10), input, repeats);
            long hashMs = median(new HashPartitionDeduplicator(budget, 16), input, repeats);
            System.out.println(budget + "," + sortMs + "," + hashMs);
        }
    }

    private static long median(Deduplicator algorithm, Path input, int repeats) throws IOException {
        Path out = Path.of(input + ".bench-out");
        long[] times = new long[repeats];
        for (int i = 0; i < repeats; i++) {
            long start = System.nanoTime();
            algorithm.dedupe(input, out);
            times[i] = (System.nanoTime() - start) / 1_000_000;
        }
        Files.deleteIfExists(out);
        Arrays.sort(times);
        return times[repeats / 2];
    }
}