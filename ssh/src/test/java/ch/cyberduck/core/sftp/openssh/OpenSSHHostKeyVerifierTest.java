package ch.cyberduck.core.sftp.openssh;

import ch.cyberduck.core.Host;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.sftp.SFTPProtocol;

import org.junit.Test;

import java.io.InputStream;
import java.math.BigInteger;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.Collections;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;

import com.hierynomus.sshj.userauth.certificate.Certificate;
import net.schmizz.sshj.common.KeyType;
import net.schmizz.sshj.common.SecurityUtils;
import net.schmizz.sshj.transport.verification.OpenSSHKnownHosts;

import static org.junit.Assert.*;

public class OpenSSHHostKeyVerifierTest {

    @Test
    public void testVerifyServerHostKey() throws Exception {
        final Local l = new Local("./knownhosts.test");
        try {
            final OpenSSHHostKeyVerifier v = new OpenSSHHostKeyVerifier(l) {
                @Override
                protected boolean isUnknownKeyAccepted(final Host hostname, final PublicKey key) {
                    return false;
                }

                @Override
                protected boolean isChangedKeyAccepted(final Host hostname, final PublicKey key) {
                    return false;
                }
            };
            assertNotNull(v.database);
            final PublicKey key = SecurityUtils.getKeyFactory("RSA").generatePublic(new RSAPublicKeySpec(new BigInteger("a19f65e93926d9a2f5b52072db2c38c54e6cf0113d31fa92ff827b0f3bec609c45ea84264c88e64adba11ff093ed48ee0ed297757654b0884ab5a7e28b3c463bc9074b32837a2b69b61d914abf1d74ccd92b20fa44db3b31fb208c0dd44edaeb4ab097118e8ee374b6727b89ad6ce43f1b70c5a437ccebc36d2dad8ae973caad15cd89ae840fdae02cae42d241baef8fda8aa6bbaa54fd507a23338da6f06f61b34fb07d560e63fbce4a39c073e28573c2962cedb292b14b80d1b4e67b0465f2be0e38526232d0a7f88ce91a055fde082038a87ed91f3ef5ff971e30ea6cccf70d38498b186621c08f8fdceb8632992b480bf57fc218e91f2ca5936770fe9469", 16),
                    new BigInteger("23", 16)));
            assertFalse(v.verify(new Host(new SFTPProtocol(), "ahostname", 22), key));
            v.allow(new Host(new SFTPProtocol(), "ahostname", 22), key, true);
            assertTrue(v.verify(new Host(new SFTPProtocol(), "ahostname", 22), key));
        }
        finally {
            l.delete();
        }
    }

    @Test
    public void testVerifyServerHostKeyCustomPort() throws Exception {
        final Local l = new Local("./knownhosts.test");
        try {
            final OpenSSHHostKeyVerifier v = new OpenSSHHostKeyVerifier(l) {
                @Override
                protected boolean isUnknownKeyAccepted(final Host hostname, final PublicKey key) {
                    return false;
                }

                @Override
                protected boolean isChangedKeyAccepted(final Host hostname, final PublicKey key) {
                    return false;
                }
            };
            assertNotNull(v.database);
            final PublicKey key = SecurityUtils.getKeyFactory("RSA").generatePublic(new RSAPublicKeySpec(new BigInteger("a19f65e93926d9a2f5b52072db2c38c54e6cf0113d31fa92ff827b0f3bec609c45ea84264c88e64adba11ff093ed48ee0ed297757654b0884ab5a7e28b3c463bc9074b32837a2b69b61d914abf1d74ccd92b20fa44db3b31fb208c0dd44edaeb4ab097118e8ee374b6727b89ad6ce43f1b70c5a437ccebc36d2dad8ae973caad15cd89ae840fdae02cae42d241baef8fda8aa6bbaa54fd507a23338da6f06f61b34fb07d560e63fbce4a39c073e28573c2962cedb292b14b80d1b4e67b0465f2be0e38526232d0a7f88ce91a055fde082038a87ed91f3ef5ff971e30ea6cccf70d38498b186621c08f8fdceb8632992b480bf57fc218e91f2ca5936770fe9469", 16),
                    new BigInteger("23", 16)));
            assertFalse(v.verify(new Host(new SFTPProtocol(), "ahostname", 2211), key));
            v.allow(new Host(new SFTPProtocol(), "ahostname", 2211), key, true);
            assertTrue(v.verify(new Host(new SFTPProtocol(), "ahostname", 2211), key));
        }
        finally {
            l.delete();
        }
    }

    @Test
    public void testVerifyIndexError() {
        final OpenSSHHostKeyVerifier v = new OpenSSHHostKeyVerifier(
                new Local("src/test/resources", "known_hosts.invalidline")) {
            @Override
            protected boolean isUnknownKeyAccepted(final Host hostname, final PublicKey key) {
                return false;
            }

            @Override
            protected boolean isChangedKeyAccepted(final Host hostname, final PublicKey key) {
                return false;
            }
        };
        assertNotNull(v.database);
    }

    @Test
    public void testEcdsaNist() {
        // |1|Gf2LppqPUrz9Tfl4QyS/bDqX0yw=|EWSG6Gl45mO6ZX1ENbmQUGCndF8= ecdsa-sha2-nistp256 AAAAE2VjZHNhLXNoYTItbmlzdHAyNTYAAAAIbmlzdHAyNTYAAABBBLcNI58jw4+R7St2mDugzg46mEexty3p8AjWmc7OCy5vHoJRXzJwiKdUlbgE0YglnCz8MNvwQwKK0dnQDI3uJZ8=

        final OpenSSHHostKeyVerifier v = new OpenSSHHostKeyVerifier(
                new Local("src/test/resources", "known_hosts.ecdsa")) {
            @Override
            protected boolean isUnknownKeyAccepted(final Host hostname, final PublicKey key) {
                return false;
            }

            @Override
            protected boolean isChangedKeyAccepted(final Host hostname, final PublicKey key) {
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
            protected boolean isUnknownKeyAccepted(final Host hostname, final PublicKey key) {
                unknown.set(true);
                return true;
            }

            @Override
            protected boolean isChangedKeyAccepted(final Host hostname, final PublicKey key) {
                return false;
            }
        };
        final PublicKey key = SecurityUtils.getKeyFactory("RSA").generatePublic(new RSAPublicKeySpec(new BigInteger("a19f65e93926d9a2f5b52072db2c38c54e6cf0113d31fa92ff827b0f3bec609c45ea84264c88e64adba11ff093ed48ee0ed297757654b0884ab5a7e28b3c463bc9074b32837a2b69b61d914abf1d74ccd92b20fa44db3b31fb208c0dd44edaeb4ab097118e8ee374b6727b89ad6ce43f1b70c5a437ccebc36d2dad8ae973caad15cd89ae840fdae02cae42d241baef8fda8aa6bbaa54fd507a23338da6f06f61b34fb07d560e63fbce4a39c073e28573c2962cedb292b14b80d1b4e67b0465f2be0e38526232d0a7f88ce91a055fde082038a87ed91f3ef5ff971e30ea6cccf70d38498b186621c08f8fdceb8632992b480bf57fc218e91f2ca5936770fe9469", 16),
                new BigInteger("23", 16)));
        assertTrue(v.verify(new Host(new SFTPProtocol(), "h", 22), key));
        assertTrue(unknown.get());
    }

    @Test
    public void testVerifyServerHostKeyCertificate() throws Exception {
        final Local l = new Local("./knownhosts.test");
        try {
            final OpenSSHHostKeyVerifier v = new OpenSSHHostKeyVerifier(l) {
                @Override
                protected boolean isUnknownKeyAccepted(final Host hostname, final PublicKey key) {
                    return false;
                }

                @Override
                protected boolean isChangedKeyAccepted(final Host hostname, final PublicKey key) {
                    return false;
                }
            };
            assertNotNull(v.database);
            final PublicKey key = SecurityUtils.getKeyFactory("RSA").generatePublic(new RSAPublicKeySpec(new BigInteger("a19f65e93926d9a2f5b52072db2c38c54e6cf0113d31fa92ff827b0f3bec609c45ea84264c88e64adba11ff093ed48ee0ed297757654b0884ab5a7e28b3c463bc9074b32837a2b69b61d914abf1d74ccd92b20fa44db3b31fb208c0dd44edaeb4ab097118e8ee374b6727b89ad6ce43f1b70c5a437ccebc36d2dad8ae973caad15cd89ae840fdae02cae42d241baef8fda8aa6bbaa54fd507a23338da6f06f61b34fb07d560e63fbce4a39c073e28573c2962cedb292b14b80d1b4e67b0465f2be0e38526232d0a7f88ce91a055fde082038a87ed91f3ef5ff971e30ea6cccf70d38498b186621c08f8fdceb8632992b480bf57fc218e91f2ca5936770fe9469", 16),
                    new BigInteger("23", 16)));
            // Host presents an OpenSSH certificate wrapping the host key rather than the plain key itself
            final PublicKey certificate = Certificate.<PublicKey>getBuilder().publicKey(key).build();
            final Host host = new Host(new SFTPProtocol(), "ahostname", 22);
            assertFalse(v.verify(host, certificate));
            v.allow(host, certificate, true);
            // Verify matches by the embedded public key regardless of whether the certificate wrapper is presented again
            assertTrue(v.verify(host, certificate));
            assertTrue(v.verify(host, key));
        }
        finally {
            l.delete();
        }
    }

    @Test
    public void testVerifyServerHostKeyCertificateTypeEntry() throws Exception {
        final Local l = new Local("./knownhosts.test");
        try {
            final OpenSSHHostKeyVerifier v = new OpenSSHHostKeyVerifier(l) {
                @Override
                protected boolean isUnknownKeyAccepted(final Host hostname, final PublicKey key) {
                    return false;
                }

                @Override
                protected boolean isChangedKeyAccepted(final Host hostname, final PublicKey key) {
                    return false;
                }
            };
            assertNotNull(v.database);
            final PublicKey key = SecurityUtils.getKeyFactory("RSA").generatePublic(new RSAPublicKeySpec(new BigInteger("a19f65e93926d9a2f5b52072db2c38c54e6cf0113d31fa92ff827b0f3bec609c45ea84264c88e64adba11ff093ed48ee0ed297757654b0884ab5a7e28b3c463bc9074b32837a2b69b61d914abf1d74ccd92b20fa44db3b31fb208c0dd44edaeb4ab097118e8ee374b6727b89ad6ce43f1b70c5a437ccebc36d2dad8ae973caad15cd89ae840fdae02cae42d241baef8fda8aa6bbaa54fd507a23338da6f06f61b34fb07d560e63fbce4a39c073e28573c2962cedb292b14b80d1b4e67b0465f2be0e38526232d0a7f88ce91a055fde082038a87ed91f3ef5ff971e30ea6cccf70d38498b186621c08f8fdceb8632992b480bf57fc218e91f2ca5936770fe9469", 16),
                    new BigInteger("23", 16)));
            // Fully populated so the certificate can be serialized to a known_hosts line rather than just held in memory
            final Certificate<PublicKey> certificate = Certificate.<PublicKey>getBuilder()
                    .publicKey(key)
                    .nonce(new byte[]{1, 2, 3, 4})
                    .serial(BigInteger.ONE)
                    .type(2L)
                    .id("test")
                    .validPrincipals(Collections.singletonList("ahostname"))
                    .validAfter(new Date(0))
                    .validBefore(new Date(32503680000000L))
                    .critOptions(Collections.emptyMap())
                    .extensions(Collections.emptyMap())
                    .signatureKey(new byte[]{5, 6, 7, 8})
                    .signature(new byte[]{9, 10, 11, 12})
                    .build();
            final Host host = new Host(new SFTPProtocol(), "ahostname", 22);
            assertFalse(v.verify(host, certificate));
            // Write a certificate-type entry to known_hosts, i.e. the raw certificate rather than the unwrapped public key
            final OpenSSHKnownHosts.HostEntry entry = new OpenSSHKnownHosts.HostEntry(null, "ahostname", KeyType.fromKey(certificate), certificate);
            v.database.entries().add(entry);
            v.database.write(entry);
            // Matches through the certificate-type entry branch, not the unwrap-to-public-key path
            assertTrue(v.verify(host, certificate));
        }
        finally {
            l.delete();
        }
    }
}
