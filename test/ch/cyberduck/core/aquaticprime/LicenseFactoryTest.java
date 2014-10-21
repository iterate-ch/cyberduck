package ch.cyberduck.core.aquaticprime;

import ch.cyberduck.core.AbstractTestCase;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @version $Id$
 */
public class LicenseFactoryTest extends AbstractTestCase {

    @Test
    public void testFindReceipt() throws Exception {
        DonationKeyFactory.register();
        assertEquals(new Receipt(null, "c42c030b8670"), LicenseFactory.find());
    }
}
