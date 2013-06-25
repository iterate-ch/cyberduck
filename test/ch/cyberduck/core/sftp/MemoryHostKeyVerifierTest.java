package ch.cyberduck.core.sftp;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.ConnectionCanceledException;
import ch.cyberduck.core.local.LocalFactory;

import org.junit.Test;

import java.io.IOException;

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

/**
 * @version $Id$
 */
public class MemoryHostKeyVerifierTest extends AbstractTestCase {

    @Test
    public void testVerifyServerHostKey() throws Exception {
        final MemoryHostKeyVerifier v = new MemoryHostKeyVerifier(LocalFactory.createLocal("./knownhosts.test")) {

            @Override
            protected void save(final String hostname, final String serverHostKeyAlgorithm, final byte[] serverHostKey) throws IOException {
                //
            }

            @Override
            protected boolean isUnknownKeyAccepted(final String hostname, final int port, final String serverHostKeyAlgorithm, final byte[] serverHostKey) throws ConnectionCanceledException {
                return false;
            }

            @Override
            protected boolean isChangedKeyAccepted(final String hostname, final int port, final String serverHostKeyAlgorithm, final byte[] serverHostKey) throws ConnectionCanceledException {
                return false;
            }
        };
        assertFalse(v.isHostKeyDatabaseWritable());
        byte[] key = new byte[]{0, 0, 0, 7, 115, 115, 104, 45, 114, 115, 97, 0, 0, 0, 1, 35, 0, 0, 1, 1, 0, -26, 127, 71, 16, -7, 7, -48, 40, 2, -40, 64, 35, 46, 56, -100, -52, -118, -64, -4, 60, -128, 72, 114, -48, -107, 105, 82, 116, 30, -32, -86, -43, -11, 110, -89, 95, -76, 58, 34, -11, -100, -68, -45, 44, 84, 124, 53, -57, -63, 66, -94, -122, -16, -99, 71, 121, -37, 81, -21, 97, 70, 90, 87, -5, -116, 90, -77, -43, 18, 22, -49, -4, -33, -12, 11, 65, -9, 17, -98, 56, 78, 41, 125, -36, -83, 102, 75, -40, 34, 89, -83, -19, 84, -48, -47, 106, -14, -53, 114, -71, 28, -105, -117, 26, -77, 63, -119, -43, -58, -48, 33, -32, 71, 7, 105, 50, 104, -5, 65, 2, 29, -26, 57, -74, 42, 55, 69, 27, -43, 92, 20, -91, 27, -102, 7, 89, 55, -111, -31, 36, -1, -96, -96, -114, -35, -121, 82, -70, -87, -22, -80, -60, -19, 14, 88, -102, 52, -37, 16, -52, -93, -13, -123, 32, 100, -75, -7, 56, 105, 70, -58, -117, 7, -111, 114, 17, 113, -18, 62, -27, 74, -51, 26, 43, -89, -91, 98, -48, 124, 34, -94, -10, -3, 32, -51, -94, 101, -87, -109, -126, -119, 16, 117, 121, -110, 84, -25, -75, 5, 109, 68, 57, -62, 73, -5, -49, -96, -29, -71, 65, -73, 45, 88, -35, 21, 116, 4, -101, 29, 106, -64, -45, -69, 87, -39, 14, -19, -72, -64, -45, 115, 48, 80, -41, -88, 91, 1, -36, 110, 59, -11, 22, -55, 100, 81, 33};
        assertFalse(v.verify("ahostname", 9999, "ssh-rsa", key));
        v.allow("ahostname", "ssh-rsa", key, true);
        assertTrue(v.verify("ahostname", 9999, "ssh-rsa", key));
    }
}
