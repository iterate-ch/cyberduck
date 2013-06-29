package ch.cyberduck.core.aquaticprime;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.local.Local;
import ch.cyberduck.core.local.LocalFactory;

import org.junit.Test;

import static org.junit.Assert.assertFalse;

/**
 * @version $Id$
 */
public class DonationTest extends AbstractTestCase {

    @Test
    public void testVerify() throws Exception {
        final Local f = LocalFactory.createLocal(System.getProperty("java.io.tmpdir"), "f.cyberducklicense");
        Donation r = new Donation(f);
        assertFalse(r.verify());
        f.touch();
        assertFalse(r.verify());
        f.delete();
    }
}