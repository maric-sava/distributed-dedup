package rs.ac.uns.ftn.dedup.main;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import rs.ac.uns.ftn.dedup.generator.DataGenerator;
import rs.ac.uns.ftn.dedup.generator.Distribution;
import rs.ac.uns.ftn.dedup.generator.GeneratorConfig;

/** Usage: GenerateMain <rows> <distinct> <seed> <outputPath> [UNIFORM|ZIPF] */
public class GenerateMain {

    public static void main(String[] args) throws IOException {
        if (args.length < 4 || args.length > 5) {
            System.err.println("Usage: GenerateMain <rows> <distinct> <seed> <outputPath> [UNIFORM|ZIPF]");
            System.exit(1);
        }
        long rows = Long.parseLong(args[0]);
        int distinct = Integer.parseInt(args[1]);
        long seed = Long.parseLong(args[2]);
        Path output = Path.of(args[3]);
        Distribution distribution = args.length == 5
                ? Distribution.valueOf(args[4].toUpperCase())
                : Distribution.UNIFORM;

        if (output.toAbsolutePath().getParent() != null) {
            Files.createDirectories(output.toAbsolutePath().getParent());
        }
        new DataGenerator().generate(new GeneratorConfig(rows, distinct, distribution, seed), output);

        System.out.println("rows=" + rows + " distinct=" + distinct + " seed=" + seed
                + " distribution=" + distribution + " output=" + output + " bytes=" + Files.size(output));
    }
}