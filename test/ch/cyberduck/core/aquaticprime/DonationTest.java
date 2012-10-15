package ch.cyberduck.core.aquaticprime;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.local.Local;
import ch.cyberduck.core.local.LocalFactory;

import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertFalse;

/**
 * @version $Id:$
 */
public class DonationTest extends AbstractTestCase {

    @BeforeClass
    public static void register() {
        Donation.register();
    }

    @Test
    public void testVerify() throws Exception {
        final Local f = LocalFactory.createLocal(System.getProperty("java.io.tmpdir"), "f");
        Donation r = new Donation(f);
        assertFalse(r.verify());
        f.touch();
        assertFalse(r.verify());
    }
}