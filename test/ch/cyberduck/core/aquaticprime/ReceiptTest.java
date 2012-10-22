package ch.cyberduck.core.aquaticprime;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.local.LocalFactory;

import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @version $Id$
 */
public class ReceiptTest extends AbstractTestCase {

    @BeforeClass
    public static void register() {
        Receipt.register();
    }

    @Test
    public void testVerify() throws Exception {
        Receipt r = new Receipt(LocalFactory.createLocal("/Applications/Cyberduck.app/Contents/_MASReceipt/receipt"));
        assertTrue(r.verify());
        assertEquals("c42c030b8670", r.getName());
    }

    @Test
    public void testVerifyFailure() throws Exception {
        Receipt r = new Receipt(LocalFactory.createLocal("/Applications/Cyberduck.app/Contents/Info.plist"));
        assertFalse(r.verify());
        assertEquals("Unknown", r.getName());
    }
}
