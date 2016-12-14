package ch.cyberduck.core;

import ch.cyberduck.core.transfer.CopyTransfer;
import ch.cyberduck.core.transfer.Transfer;

import org.junit.Test;

import static org.junit.Assert.*;

public class TransferCollectionTest {

    @Test
    public void testEmpty() throws Exception {
        TransferCollection c = new TransferCollection(new Local("src/test/resources/TransferCollectionEmpty.plist")) {
            @Override
            public void trash() {
                //
            }
        };
        c.clear();
        assertEquals(0, c.size());
        c.load();
        assertEquals(0, c.size());
    }

    @Test
    public void testLoadCopyDeprecated() throws Exception {
        TransferCollection c = new TransferCollection(new Local("src/test/resources/TransferCollectionCopyFormatDeprecated.plist")) {
            @Override
            public void trash() {
                //
            }
        };
        c.clear();
        assertEquals(0, c.size());
        c.load();
        assertEquals(0, c.size());
    }

    @Test
    public void testLoadCopyInvalidCopyTransfer() throws Exception {
        TransferCollection c = new TransferCollection(new Local("src/test/resources/TransferCollectionCopyFormatInvalid.plist")) {
            @Override
            public void trash() {
                //
            }
        };
        c.clear();
        assertEquals(0, c.size());
        c.load();
        assertEquals(0, c.size());
    }

    @Test
    public void testSaveDeprecated() throws Exception {
        TransferCollection c = new TransferCollection(new Local("src/test/resources/TransferCollectionCopyFormatDeprecated.plist")) {
            @Override
            public void trash() {
                //
            }
        };
        c.clear();
        assertEquals(0, c.size());
        c.load();
        assertEquals(0, c.size());
    }

    @Test
    public void testLoadCopyWithDestination() throws Exception {
        ProtocolFactory.register(new TestProtocol());
        TransferCollection c = new TransferCollection(new Local("src/test/resources/TransferCollectionCopyFormat.plist")) {
            @Override
            public void trash() {
                //
            }
        };
        c.clear();
        assertEquals(0, c.size());
        c.load();
        assertEquals(1, c.size());
        assertNotNull(c.get(0));
        assertEquals(1, (c.get(0).getRoots().size()));
        final Transfer transfer = c.get(0);
        assertTrue(transfer instanceof CopyTransfer);
        assertEquals("/pub/hacks/listings/1301-130.zip", transfer.getRoot().remote.getAbsolute());
        assertNull(transfer.getLocal());
        assertNotNull(transfer.getRoot().remote);
        assertNull(transfer.getRoot().local);
        assertEquals("http://ftp.heise.de:21/pub/hacks/listings/1301-130.zip", transfer.getRemote().getUrl());
        assertEquals(109648L, transfer.getSize(), 0L);
        assertEquals(109648L, transfer.getTransferred(), 0L);
        assertEquals("ftp.heise.de", transfer.getSource().getHostname());
//        assertEquals("sudo.ch", ((CopyTransfer) transfer).getDestination().getHost().getHostname());
        assertEquals("1301-130.zip", transfer.getName());
    }
}
