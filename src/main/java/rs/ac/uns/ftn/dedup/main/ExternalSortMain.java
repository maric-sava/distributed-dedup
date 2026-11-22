package rs.ac.uns.ftn.dedup.main;

import rs.ac.uns.ftn.dedup.local.sort.ExternalSortDeduplicator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ExternalSortMain {

    public static void main(String[] args) throws IOException {
        if (args.length != 4) {
            System.err.println("Usage: ExternalSortMain <input> <output> <budgetBytes> <maxRunsPerMerge>");
            System.exit(1);
        }
        Path input = Path.of(args[0]);
        Path output = Path.of(args[1]);
        long budget = Long.parseLong(args[2]);
        int maxRunsPerMerge = Integer.parseInt(args[3]);

        long start = System.nanoTime();
        new ExternalSortDeduplicator(budget, maxRunsPerMerge).dedupe(input, output);
        long millis = (System.nanoTime() - start) / 1_000_000;

        System.out.println("input=" + input + " budget=" + budget + " maxRunsPerMerge=" + maxRunsPerMerge
                + " time=" + millis + " ms output=" + output + " bytes=" + Files.size(output));
    }
}
