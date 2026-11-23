package rs.ac.uns.ftn.dedup.distributed.partition;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import rs.ac.uns.ftn.dedup.distributed.Node;

/** Decides which node owns which line: splits a node's file into one part per destination node. */
public interface NodePartitioner {

    /** The part with index i is meant for node i. */
    List<Path> split(Node source, Path input, int nodeCount) throws IOException;
}