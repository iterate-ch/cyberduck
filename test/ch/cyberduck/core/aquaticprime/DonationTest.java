package ch.cyberduck.core.aquaticprime;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.LocalFactory;
import ch.cyberduck.core.local.LocalTouchFactory;

import org.junit.Test;

import static org.junit.Assert.assertFalse;

/**
 * @version $Id$
 */
public class DonationTest extends AbstractTestCase {

    @Test
    public void testVerify() throws Exception {
        final Local f = LocalFactory.get(System.getProperty("java.io.tmpdir"), "f.cyberducklicense");
        Donation r = new Donation(f);
        assertFalse(r.verify());
        LocalTouchFactory.get().touch(f);
        assertFalse(r.verify());
        f.delete();
    }
}