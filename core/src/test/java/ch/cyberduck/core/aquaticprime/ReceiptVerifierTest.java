package ch.cyberduck.core.aquaticprime;

import ch.cyberduck.core.Local;

import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

@Ignore
public class ReceiptVerifierTest {

    @Test
    public void testVerify() {
        ReceiptVerifier r = new ReceiptVerifier(new Local("src/test/resources/receipt"), "ch.sudo.cyberduck", "4.7.3");
        assertFalse(r.verify(new DisabledLicenseVerifierCallback() {
            @Override
            public void failure(final InvalidLicenseException failure) {
                assertEquals("Not a valid registration key", failure.getMessage());
                assertEquals("Hash with GUID f02f4b09fb58 does not match hash in receipt.", failure.getDetail());
            }
        }));
    }
}
