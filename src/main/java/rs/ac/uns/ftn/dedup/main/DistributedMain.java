package rs.ac.uns.ftn.dedup.main;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import rs.ac.uns.ftn.dedup.distributed.DistributedDeduplicator;
import rs.ac.uns.ftn.dedup.distributed.JobStats;
import rs.ac.uns.ftn.dedup.local.hash.HashPartitionDeduplicator;

/** Usage: DistributedMain <input> <output> <nodes> <nodeBudgetBytes> <buckets> */
public class DistributedMain {

    public static void main(String[] args) throws IOException {
        if (args.length < 5 || args.length > 6) {
            System.err.println("Usage: DistributedMain <input> <output> <nodes> <nodeBudgetBytes> <buckets> [combiner]");
            System.exit(1);
        }
        Path input = Path.of(args[0]);
        Path output = Path.of(args[1]);
        int nodes = Integer.parseInt(args[2]);
        long budget = Long.parseLong(args[3]);
        int buckets = Integer.parseInt(args[4]);
        boolean combiner = args.length == 6 && args[5].equals("combiner");

        DistributedDeduplicator job =
                new DistributedDeduplicator(nodes, budget, new HashPartitionDeduplicator(budget, buckets), combiner);

        job.dedupe(input, output);

        JobStats stats = job.stats();

        System.out.println("nodes=" + nodes + " combiner=" + combiner + " budget=" + budget + " time=" + stats.timeMillis() + " ms");
        System.out.println("bytesSent=" + job.bytesSent());
        System.out.println("receivedPerNode=" + stats.receivedBytesPerNode());
        System.out.println(String.format(Locale.ROOT, "imbalance(max/average)=%.2f", stats.imbalance()));
        System.out.println("output=" + output + " bytes=" + Files.size(output));
    }
}