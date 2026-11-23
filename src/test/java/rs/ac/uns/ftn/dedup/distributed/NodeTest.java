package rs.ac.uns.ftn.dedup.distributed;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class NodeTest {

    @TempDir
    Path dir;

    @Test
    public void createsItsOwnFolder() throws IOException {
        Node node = new Node(0, dir, 1000);

        assertTrue(Files.isDirectory(node.folder()));
        assertTrue(Files.isDirectory(node.incomingFolder()));
        assertEquals(dir.resolve("node-0"), node.folder());
    }

    @Test
    void twoNodesHaveDifferentFolders() throws IOException {
        Node a = new Node(0, dir, 1000);
        Node b = new Node(1, dir, 1000);

        assertNotEquals(a.folder(), b.folder());
        assertNotEquals(a.incomingFolder(), b.incomingFolder());
    }

    @Test
    void remembersIdAndBudget() throws IOException {
        Node node = new Node(3, dir, 5000);

        assertEquals(3, node.id());
        assertEquals(5000, node.budgetBytes());
    }
}
