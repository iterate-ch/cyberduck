package ch.cyberduck.core.sftp.openssh;

import ch.cyberduck.core.Local;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.ConnectionCanceledException;

import org.junit.Test;

import java.io.InputStream;
import java.math.BigInteger;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.concurrent.atomic.AtomicBoolean;

import net.schmizz.sshj.common.SecurityUtils;

import static org.junit.Assert.*;

public class OpenSSHHostKeyVerifierTest {

    @Test
    public void testVerifyServerHostKey() throws Exception {
        final Local l = new Local("./knownhosts.test");
        try {
            final OpenSSHHostKeyVerifier v = new OpenSSHHostKeyVerifier(l) {
                @Override
                protected boolean isUnknownKeyAccepted(final String hostname, final PublicKey key) throws ConnectionCanceledException {
                    return false;
                }

                @Override
                protected boolean isChangedKeyAccepted(final String hostname, final PublicKey key) throws ConnectionCanceledException {
                    return false;
                }
            };
            assertNotNull(v.database);
            final PublicKey key = SecurityUtils.getKeyFactory("RSA").generatePublic(new RSAPublicKeySpec(new BigInteger("a19f65e93926d9a2f5b52072db2c38c54e6cf0113d31fa92ff827b0f3bec609c45ea84264c88e64adba11ff093ed48ee0ed297757654b0884ab5a7e28b3c463bc9074b32837a2b69b61d914abf1d74ccd92b20fa44db3b31fb208c0dd44edaeb4ab097118e8ee374b6727b89ad6ce43f1b70c5a437ccebc36d2dad8ae973caad15cd89ae840fdae02cae42d241baef8fda8aa6bbaa54fd507a23338da6f06f61b34fb07d560e63fbce4a39c073e28573c2962cedb292b14b80d1b4e67b0465f2be0e38526232d0a7f88ce91a055fde082038a87ed91f3ef5ff971e30ea6cccf70d38498b186621c08f8fdceb8632992b480bf57fc218e91f2ca5936770fe9469", 16),
                    new BigInteger("23", 16)));
            assertFalse(v.verify("ahostname", 22, key));
            v.allow("ahostname", key, true);
            assertTrue(v.verify("ahostname", 22, key));
        }
        finally {
            l.delete();
        }
    }

    @Test
    public void testVerifyIndexError() throws Exception {
        final OpenSSHHostKeyVerifier v = new OpenSSHHostKeyVerifier(
                new Local("test/resources", "known_hosts.invalidline")) {
            @Override
            protected boolean isUnknownKeyAccepted(final String hostname, final PublicKey key) throws ConnectionCanceledException {
                return false;
            }

            @Override
            protected boolean isChangedKeyAccepted(final String hostname, final PublicKey key) throws ConnectionCanceledException {
                return false;
            }
        };
        assertNotNull(v.database);
    }

    @Test
    public void testEcdsaNist() throws Exception {
        // |1|Gf2LppqPUrz9Tfl4QyS/bDqX0yw=|EWSG6Gl45mO6ZX1ENbmQUGCndF8= ecdsa-sha2-nistp256 AAAAE2VjZHNhLXNoYTItbmlzdHAyNTYAAAAIbmlzdHAyNTYAAABBBLcNI58jw4+R7St2mDugzg46mEexty3p8AjWmc7OCy5vHoJRXzJwiKdUlbgE0YglnCz8MNvwQwKK0dnQDI3uJZ8=

        final OpenSSHHostKeyVerifier v = new OpenSSHHostKeyVerifier(
                new Local("test/resources", "known_hosts.ecdsa")) {
            @Override
            protected boolean isUnknownKeyAccepted(final String hostname, final PublicKey key) throws ConnectionCanceledException {
                return false;
            }

            @Override
            protected boolean isChangedKeyAccepted(final String hostname, final PublicKey key) throws ConnectionCanceledException {
                return false;
            }
        };
        assertNotNull(v.database);
    }

    @Test
    public void testReadFailure() throws Exception {
        final AtomicBoolean unknown = new AtomicBoolean();
        final OpenSSHHostKeyVerifier v = new OpenSSHHostKeyVerifier(new Local("/t") {
            @Override
            public InputStream getInputStream() throws AccessDeniedException {
                throw new AccessDeniedException("t");
            }
        }) {
            @Override
            protected boolean isUnknownKeyAccepted(final String hostname, final PublicKey key) throws ConnectionCanceledException {
                unknown.set(true);
                return true;
            }

            @Override
            protected boolean isChangedKeyAccepted(final String hostname, final PublicKey key) throws ConnectionCanceledException {
                return false;
            }
        };
        final PublicKey key = SecurityUtils.getKeyFactory("RSA").generatePublic(new RSAPublicKeySpec(new BigInteger("a19f65e93926d9a2f5b52072db2c38c54e6cf0113d31fa92ff827b0f3bec609c45ea84264c88e64adba11ff093ed48ee0ed297757654b0884ab5a7e28b3c463bc9074b32837a2b69b61d914abf1d74ccd92b20fa44db3b31fb208c0dd44edaeb4ab097118e8ee374b6727b89ad6ce43f1b70c5a437ccebc36d2dad8ae973caad15cd89ae840fdae02cae42d241baef8fda8aa6bbaa54fd507a23338da6f06f61b34fb07d560e63fbce4a39c073e28573c2962cedb292b14b80d1b4e67b0465f2be0e38526232d0a7f88ce91a055fde082038a87ed91f3ef5ff971e30ea6cccf70d38498b186621c08f8fdceb8632992b480bf57fc218e91f2ca5936770fe9469", 16),
                new BigInteger("23", 16)));
        assertTrue(v.verify("h", 22, key));
        assertTrue(unknown.get());
    }

}
