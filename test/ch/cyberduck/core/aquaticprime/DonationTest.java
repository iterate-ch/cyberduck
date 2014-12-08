package ch.cyberduck.core.aquaticprime;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Factory;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.local.FinderLocal;
import ch.cyberduck.core.local.LocalTouchFactory;
import ch.cyberduck.core.test.Depends;

import org.junit.Test;

import static org.junit.Assert.assertFalse;

/**
 * @version $Id$
 */
@Depends(platform = Factory.Platform.Name.mac)
public class DonationTest extends AbstractTestCase {

    @Test
    public void testVerify() throws Exception {
        final Local f = new FinderLocal(System.getProperty("java.io.tmpdir"), "f.cyberducklicense");
        Donation r = new Donation(f);
        assertFalse(r.verify());
        LocalTouchFactory.get().touch(f);
        assertFalse(r.verify());
        f.delete();
    }
}