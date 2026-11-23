package rs.ac.uns.ftn.dedup.distributed;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TransportTest {

    @TempDir
    Path dir;

    Node a;
    Node b;
    Node c;
    Transport transport = new Transport();

    @BeforeEach
    void createNodes() throws IOException {
        a = new Node(0, dir, 1000);
        b = new Node(1, dir, 1000);
        c = new Node(2, dir, 1000);
    }

    @Test
    void copiesFileToReceiverAndCountsBytes() throws IOException {
        Path file = a.folder().resolve("part.txt");
        Files.writeString(file, "x\ny\n"); // 4 bytes

        Path received = transport.send(file, a, b);

        assertEquals(b.incomingFolder(), received.getParent());
        assertEquals("x\ny\n", Files.readString(received));
        assertTrue(Files.exists(file)); // the sender still has its file
        assertEquals(4, transport.bytesSent());
        assertEquals(1, transport.filesSent());
    }

    @Test
    void countsAddUpOverSeveralSends() throws IOException {
        Path first = a.folder().resolve("one.txt");
        Path second = a.folder().resolve("two.txt");
        Files.writeString(first, "abc\n");    // 4 bytes
        Files.writeString(second, "hello\n"); // 6 bytes

        transport.send(first, a, b);
        transport.send(second, a, c);

        assertEquals(10, transport.bytesSent());
        assertEquals(2, transport.filesSent());
    }

    @Test
    void sameFileNameFromTwoSendersDoesNotClash() throws IOException {
        Path fromA = a.folder().resolve("part.txt");
        Path fromB = b.folder().resolve("part.txt");
        Files.writeString(fromA, "a\n");
        Files.writeString(fromB, "b\n");

        transport.send(fromA, a, c);
        transport.send(fromB, b, c);

        assertEquals("a\n", Files.readString(c.incomingFolder().resolve("from-0-part.txt")));
        assertEquals("b\n", Files.readString(c.incomingFolder().resolve("from-1-part.txt")));
    }

    @Test
    void rejectsFileThatIsNotInSenderFolder() throws IOException {
        Path foreign = b.folder().resolve("secret.txt");
        Files.writeString(foreign, "x\n");

        assertThrows(IllegalArgumentException.class, () -> transport.send(foreign, a, c));
    }
}