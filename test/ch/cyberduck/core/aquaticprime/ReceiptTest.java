package ch.cyberduck.core.aquaticprime;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.LocalFactory;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @version $Id:$
 */
public class ReceiptTest extends AbstractTestCase {

    @Test
    public void testVerify() throws Exception {
        Receipt r = new Receipt(LocalFactory.createLocal("/Applications/Cyberduck.app/Contents/_MASReceipt/receipt"));
        assertTrue(r.verify());
    }

    @Test
    public void testVerifyFailure() throws Exception {
        Receipt r = new Receipt(LocalFactory.createLocal("/Applications/Cyberduck.app/Contents/Info.plist"));
        assertFalse(r.verify());
    }
}
