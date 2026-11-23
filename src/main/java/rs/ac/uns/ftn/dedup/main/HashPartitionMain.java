package rs.ac.uns.ftn.dedup.main;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import rs.ac.uns.ftn.dedup.local.hash.HashPartitionDeduplicator;

/** Usage: HashPartitionMain <input> <output> <budgetBytes> <buckets> */
public class HashPartitionMain {

    public static void main(String[] args) throws IOException {
        if (args.length != 4) {
            System.err.println("Usage: HashPartitionMain <input> <output> <budgetBytes> <buckets>");
            System.exit(1);
        }
        Path input = Path.of(args[0]);
        Path output = Path.of(args[1]);
        long budget = Long.parseLong(args[2]);
        int buckets = Integer.parseInt(args[3]);

        long start = System.currentTimeMillis();
        new HashPartitionDeduplicator(budget, buckets).dedupe(input, output);
        long time = System.currentTimeMillis() - start;

        System.out.println("input=" + input + " budget=" + budget + " buckets=" + buckets
                + " time=" + time + " ms output=" + output + " bytes=" + Files.size(output));
    }
}