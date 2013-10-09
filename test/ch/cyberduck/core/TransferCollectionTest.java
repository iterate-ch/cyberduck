package ch.cyberduck.core;

import ch.cyberduck.core.transfer.CopyTransfer;
import ch.cyberduck.core.transfer.Transfer;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @version $Id$
 */
public class TransferCollectionTest extends AbstractTestCase {

    @Test
    public void testEmpty() throws Exception {
        TransferCollection c = new TransferCollection(LocalFactory.createLocal("test/ch/cyberduck/core/transfer/TransferCollectionEmpty.plist"));
        assertEquals(0, c.size());
        c.load();
        assertEquals(0, c.size());
    }

    @Test
    public void testLoadCopyDeprecated() throws Exception {
        TransferCollection c = new TransferCollection(LocalFactory.createLocal("test/ch/cyberduck/core/transfer/TransferCollectionCopyFormatDeprecated.plist"));
        assertEquals(0, c.size());
        c.load();
        assertEquals(1, c.size());
        assertEquals(0, c.numberOfRunningTransfers());
        assertNotNull(c.get(0));
        assertEquals(1, (c.get(0).getRoots().size()));
        assertTrue(c.get(0) instanceof CopyTransfer);
        assertEquals("/pub/hacks/listings/1301-046.zip", (c.get(0).getRoot()).getAbsolute());
        assertEquals("ftp.heise.de", c.get(0).getHost().getHostname());
    }

    @Test
    public void testLoadCopyInvalidCopyTransfer() throws Exception {
        TransferCollection c = new TransferCollection(LocalFactory.createLocal("test/ch/cyberduck/core/transfer/TransferCollectionCopyFormatInvalid.plist"));
        assertEquals(0, c.size());
        c.load();
        assertEquals(1, c.size());
        c.save();
        assertEquals(1, c.size());
    }

    @Test
    public void testSaveDeprecated() throws Exception {
        TransferCollection c = new TransferCollection(LocalFactory.createLocal("test/ch/cyberduck/core/transfer/TransferCollectionCopyFormatDeprecated.plist"));
        assertEquals(0, c.size());
        c.load();
        assertEquals(1, c.size());
        c.save();
        assertEquals(1, c.size());
    }

    @Test
    public void testLoadCopyWithDestination() throws Exception {
        TransferCollection c = new TransferCollection(LocalFactory.createLocal("test/ch/cyberduck/core/transfer/TransferCollectionCopyFormat.plist"));
        assertEquals(0, c.size());
        c.load();
        assertEquals(1, c.size());
        assertNotNull(c.get(0));
        assertEquals(1, (c.get(0).getRoots().size()));
        final Transfer transfer = c.get(0);
        assertEquals("/pub/hacks/listings/1301-130.zip", transfer.getRoot().getAbsolute());
        assertNull(transfer.getRoot().getLocal());
//        assertEquals("/sandbox/1301-130.zip", (c.get(0).getLocal()));
        assertEquals("ftp://ftp.heise.de/pub/hacks/listings/1301-130.zip", transfer.getRemote());
        assertEquals(109648L, transfer.getSize());
        assertEquals(109648L, transfer.getTransferred());
        assertTrue(transfer instanceof CopyTransfer);
        assertEquals("ftp.heise.de", transfer.getHost().getHostname());
        assertEquals("sudo.ch", ((CopyTransfer) transfer).getDestination().getHost().getHostname());
        assertEquals("1301-130.zip", transfer.getName());
    }
}
