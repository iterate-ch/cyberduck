package ch.cyberduck.core.aquaticprime;

import ch.cyberduck.core.Local;

import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.*;

@Ignore
public class ReceiptVerifierTest {

    @Test
    public void testVerify() throws Exception {
        ReceiptVerifier r = new ReceiptVerifier(new Local("src/test/resources/receipt"), "ch.sudo.cyberduck", "4.7.3");
        assertTrue(r.verify(new DisabledLicenseVerifierCallback()));
        assertEquals("b8e85600dffe", r.getGuid());
    }

    @Test
    public void testVerifyFailure() throws Exception {
        ReceiptVerifier r = new ReceiptVerifier(new Local("src/test/resources/Info.plist"));
        assertFalse(r.verify(new DisabledLicenseVerifierCallback()));
        assertEquals(null, r.getGuid());
    }
}
