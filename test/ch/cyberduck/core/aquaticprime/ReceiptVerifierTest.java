package ch.cyberduck.core.aquaticprime;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.LocalFactory;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @version $Id$
 */
public class ReceiptVerifierTest extends AbstractTestCase {

    @Test
    public void testVerify() throws Exception {
        ReceiptVerifier r = new ReceiptVerifier(LocalFactory.get("/Applications/Cyberduck.app/Contents/_MASReceipt/receipt"));
        assertTrue(r.verify());
        assertEquals("c42c030b8670", r.getGuid());
    }

    @Test
    public void testVerifyFailure() throws Exception {
        ReceiptVerifier r = new ReceiptVerifier(LocalFactory.get("test/ch/cyberduck/core/aquaticprime/Info.plist"));
        assertFalse(r.verify());
        assertEquals(null, r.getGuid());
    }
}
