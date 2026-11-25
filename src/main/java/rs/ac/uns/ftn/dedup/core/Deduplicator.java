package rs.ac.uns.ftn.dedup.core;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Defines a common contract for deduplication algorithms.
 * Each implementation reads an input file and produces an output
 * file containing every distinct record exactly once.
 * Output ordering depends on the implementation.
 */
public interface Deduplicator {

    void dedupe(Path input, Path output) throws IOException;
}
