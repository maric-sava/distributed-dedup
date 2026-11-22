package rs.ac.uns.ftn.dedup.generator;

public final class RecordFormat {

    private RecordFormat() { } // only static methods, no instances

    public static String toRecord(int id) {
        return "user_" + id;
    }
}
