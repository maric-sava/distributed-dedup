package rs.ac.uns.ftn.dedup.generator;

/**
 * Defines a common interface for generating IDs
 * according to a probability distribution.
 */
public interface IdSource {
    int nextId();
}
