package ch.cyberduck.core.transfer;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TransferProgressTest {

    @Test
    public void testGet() {
        final TransferProgress p = new TransferProgress(1L, 2L, "p", 3d);
        assertEquals(1L, p.getSize(), 0L);
        assertEquals(2L, p.getTransferred(), 0L);
        assertEquals("p", p.getProgress());
        assertEquals(3d, p.getSpeed(), 0d);
    }
}
