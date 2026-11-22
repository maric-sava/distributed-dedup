package rs.ac.uns.ftn.dedup.core;

import java.io.IOException;
import java.nio.file.Path;

public interface Deduplicator {

    void dedupe(Path input, Path output) throws IOException;
}
