package ch.cyberduck.core.aquaticprime;

import ch.cyberduck.core.Local;
import ch.cyberduck.core.local.LocalTouchFactory;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class DonationKeyTest {

    @Test
    public void testVerifyInvalidFile() throws Exception {
        final Local f = new Local(System.getProperty("java.io.tmpdir"), "f.cyberducklicense");
        DonationKey r = new DonationKey(f);
        assertFalse(r.verify(new DisabledLicenseVerifierCallback()));
        LocalTouchFactory.get().touch(f);
        assertFalse(r.verify(new DisabledLicenseVerifierCallback()));
        f.delete();
    }

    @Test
    public void testVerifyFailure() {
        final Local f = new Local("src/test/resources/test.cyberducklicense");
        DonationKey r = new DonationKey(f);
        assertEquals("3d4d06d0-27be-11e4-8a03-123143079e6c", r.getValue("Transaction"));
        assertFalse(r.verify(new DisabledLicenseVerifierCallback()));
    }

    @Test
    public void testGetValue() {
        final Local f = new Local("src/test/resources/test.cyberducklicense");
        DonationKey r = new DonationKey(f);
        assertEquals("test@cyberduck.io", r.getValue("Email"));
    }

    @Test
    public void testVerifyBinaryFile() throws Exception {
        final Local f = new Local("src/test/resources/binary.cyberducklicense");
        DonationKey r = new DonationKey(f);
        assertEquals("3d4d06d0-27be-11e4-8a03-123143079e6c", r.getValue("Transaction"));
        assertFalse(r.verify(new DisabledLicenseVerifierCallback()));
    }
}