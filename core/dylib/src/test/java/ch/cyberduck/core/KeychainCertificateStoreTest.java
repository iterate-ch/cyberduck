package ch.cyberduck.core;

import ch.cyberduck.binding.WindowController;

import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertFalse;

public class KeychainCertificateStoreTest {

    @Test
    public void testTrustedEmptyCertificates() throws Exception {
        final KeychainCertificateStore k = new KeychainCertificateStore(new WindowController() {
            @Override
            protected String getBundleName() {
                return null;
            }
        });
        assertFalse(k.verify("cyberduck.ch", Collections.emptyList()));
    }
}
