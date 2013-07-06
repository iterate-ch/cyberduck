package ch.cyberduck.core.aquaticprime;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.local.LocalFactory;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @version $Id$
 */
public class ReceiptTest extends AbstractTestCase {

    @Test
    public void testVerify() throws Exception {
        Receipt r = new Receipt(LocalFactory.createLocal("test/ch/cyberduck/core/aquaticprime/receipt"));
        assertTrue(r.verify());
        assertEquals("040ccee30d02", r.getName());
    }

    @Test
    public void testVerifyFailure() throws Exception {
        Receipt r = new Receipt(LocalFactory.createLocal("test/ch/cyberduck/core/aquaticprime/Info.plist"));
        assertFalse(r.verify());
        assertEquals("Unknown", r.getName());
    }
}
