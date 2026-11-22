package rs.ac.uns.ftn.dedup.main;

import rs.ac.uns.ftn.dedup.generator.DataGenerator;
import rs.ac.uns.ftn.dedup.generator.Distribution;
import rs.ac.uns.ftn.dedup.generator.GeneratorConfig;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class GenerateMain {

    public static void main(String[] args) throws IOException {
        if (args.length != 4) {
            System.err.println("Usage: GenerateMain <rows> <distinct> <seed> <outputPath>");
            System.exit(1);
        }

        long rows = Long.parseLong(args[0]);
        int distinct = Integer.parseInt(args[1]);
        long seed = Long.parseLong(args[2]);
        Path output = Path.of(args[3]);

        Files.createDirectories(output.toAbsolutePath().getParent());

        GeneratorConfig config = new GeneratorConfig(rows, distinct, Distribution.UNIFORM, seed);
        new DataGenerator().generate(config, output);

        System.out.println("rows=" + rows + " distinct=" + distinct + " seed=" + seed + " file=" + output + " bytes=" + Files.size(output));
    }
}
