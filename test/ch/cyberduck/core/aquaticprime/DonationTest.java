package ch.cyberduck.core.aquaticprime;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.LocalFactory;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @version $Id$
 */
public class DonationTest extends AbstractTestCase {

    @Test
    public void testVerify() throws Exception {
        final Local f = LocalFactory.createLocal(System.getProperty("java.io.tmpdir"), "f.cyberducklicense");
        Donation r = new Donation(f);
        assertFalse(r.verify());
        assertTrue(f.touch());
        assertFalse(r.verify());
        f.delete();
    }
}