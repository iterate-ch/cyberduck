package ch.cyberduck.core.aquaticprime;

import ch.cyberduck.core.AbstractTestCase;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @version $Id$
 */
public class LicenseFactoryTest extends AbstractTestCase {

    @Test
    public void testFind() throws Exception {
        DonationKeyFactory.register();
        assertEquals(LicenseFactory.EMPTY_LICENSE, LicenseFactory.find());
//        ReceiptFactory.register();
        assertEquals(LicenseFactory.EMPTY_LICENSE, LicenseFactory.find());
    }
}
