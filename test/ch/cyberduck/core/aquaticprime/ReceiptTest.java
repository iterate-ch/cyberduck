package ch.cyberduck.core.aquaticprime;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.LocalFactory;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @version $Id$
 */
public class ReceiptTest extends AbstractTestCase {

    @Test
    public void testVerify() throws Exception {
        Receipt r = new Receipt(LocalFactory.createLocal("/Applications/Cyberduck.app/Contents/_MASReceipt/receipt"));
        assertTrue(r.verify());
        assertEquals("c42c030b8670", r.getName());
    }

    @Test
    public void testVerifyFailure() throws Exception {
        Receipt r = new Receipt(LocalFactory.createLocal("test/ch/cyberduck/core/aquaticprime/Info.plist"));
        assertFalse(r.verify());
        assertEquals("Unknown", r.getName());
    }
}
