package rs.ac.uns.ftn.dedup.main;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import rs.ac.uns.ftn.dedup.distributed.DistributedDeduplicator;
import rs.ac.uns.ftn.dedup.local.hash.HashPartitionDeduplicator;

/** Usage: DistributedMain <input> <output> <nodes> <nodeBudgetBytes> <buckets> */
public class DistributedMain {

    public static void main(String[] args) throws IOException {
        if (args.length != 5) {
            System.err.println("Usage: DistributedMain <input> <output> <nodes> <nodeBudgetBytes> <buckets>");
            System.exit(1);
        }
        Path input = Path.of(args[0]);
        Path output = Path.of(args[1]);
        int nodes = Integer.parseInt(args[2]);
        long budget = Long.parseLong(args[3]);
        int buckets = Integer.parseInt(args[4]);

        DistributedDeduplicator job =
                new DistributedDeduplicator(nodes, budget, new HashPartitionDeduplicator(budget, buckets));

        long start = System.currentTimeMillis();
        job.dedupe(input, output);
        long time = System.currentTimeMillis() - start;

        List<Long> received = job.receivedBytesPerNode();
        double average = received.stream().mapToLong(Long::longValue).sum() / (double) nodes;
        double max = received.stream().mapToLong(Long::longValue).max().orElse(0);

        System.out.println("nodes=" + nodes + " budget=" + budget + " time=" + time + " ms");
        System.out.println("bytesSent=" + job.bytesSent());
        System.out.println("receivedPerNode=" + received);
        System.out.println(String.format(Locale.ROOT, "imbalance(max/average)=%.2f", max / average));
        System.out.println("output=" + output + " bytes=" + Files.size(output));
    }
}