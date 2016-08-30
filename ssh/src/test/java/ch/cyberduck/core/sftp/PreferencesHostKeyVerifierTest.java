package ch.cyberduck.core.sftp;

import ch.cyberduck.core.exception.ChecksumException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.preferences.PreferencesFactory;

import org.junit.Test;

import java.math.BigInteger;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;

import net.schmizz.sshj.common.SecurityUtils;

import static org.junit.Assert.*;

public class PreferencesHostKeyVerifierTest {

    @Test
    public void testVerifyAcceptServerHostKey() throws Exception {
        PreferencesHostKeyVerifier v = new PreferencesHostKeyVerifier() {
            @Override
            public boolean isChangedKeyAccepted(String hostname, PublicKey key) throws ConnectionCanceledException, ChecksumException {
                return false;
            }

            @Override
            public boolean isUnknownKeyAccepted(String hostname, final PublicKey key) throws ConnectionCanceledException {
                return true;
            }
        };
        final PublicKey key = SecurityUtils.getKeyFactory("RSA").generatePublic(new RSAPublicKeySpec(new BigInteger("a19f65e93926d9a2f5b52072db2c38c54e6cf0113d31fa92ff827b0f3bec609c45ea84264c88e64adba11ff093ed48ee0ed297757654b0884ab5a7e28b3c463bc9074b32837a2b69b61d914abf1d74ccd92b20fa44db3b31fb208c0dd44edaeb4ab097118e8ee374b6727b89ad6ce43f1b70c5a437ccebc36d2dad8ae973caad15cd89ae840fdae02cae42d241baef8fda8aa6bbaa54fd507a23338da6f06f61b34fb07d560e63fbce4a39c073e28573c2962cedb292b14b80d1b4e67b0465f2be0e38526232d0a7f88ce91a055fde082038a87ed91f3ef5ff971e30ea6cccf70d38498b186621c08f8fdceb8632992b480bf57fc218e91f2ca5936770fe9469", 16),
                new BigInteger("23", 16)));
        assertTrue(v.verify("ahostname", 22, key));
        assertNull(PreferencesFactory.get().getProperty("ssh.hostkey.ssh-rsa.ahostname"));
        v.allow("ahostname", key, false);
        assertNull(PreferencesFactory.get().getProperty("ssh.hostkey.ssh-rsa.ahostname"));
        v.allow("ahostname", key, true);
        assertNotNull(PreferencesFactory.get().getProperty("ssh.hostkey.ssh-rsa.ahostname"));
        assertEquals("MIIBIDANBgkqhkiG9w0BAQEFAAOCAQ0AMIIBCAKCAQEAoZ9l6Tkm2aL1tSBy2yw4xU5s8BE9MfqS/4J7DzvsYJxF6oQmTIjmStuhH/CT7UjuDtKXdXZUsIhKtafiizxGO8kHSzKDeitpth2RSr8ddMzZKyD6RNs7MfsgjA3UTtrrSrCXEY6O43S2cnuJrWzkPxtwxaQ3zOvDbS2tiulzyq0VzYmuhA/a4CyuQtJBuu+P2oqmu6pU/VB6IzONpvBvYbNPsH1WDmP7zko5wHPihXPCliztspKxS4DRtOZ7BGXyvg44UmIy0Kf4jOkaBV/eCCA4qH7ZHz71/5ceMOpszPcNOEmLGGYhwI+P3OuGMpkrSAv1f8IY6R8spZNncP6UaQIBIw==",
                PreferencesFactory.get().getProperty("ssh.hostkey.ssh-rsa.ahostname"));
    }

    @Test
    public void testVerifyDenyServerHostKey() throws Exception {
        PreferencesHostKeyVerifier v = new PreferencesHostKeyVerifier() {
            @Override
            public boolean isChangedKeyAccepted(String hostname, PublicKey key) throws ConnectionCanceledException, ChecksumException {
                return false;
            }

            @Override
            public boolean isUnknownKeyAccepted(String hostname, final PublicKey key) throws ConnectionCanceledException {
                return false;
            }
        };
        final PublicKey key = SecurityUtils.getKeyFactory("RSA").generatePublic(new RSAPublicKeySpec(new BigInteger("a19f65e93926d9a2f5b52072db2c38c54e6cf0113d31fa92ff827b0f3bec609c45ea84264c88e64adba11ff093ed48ee0ed297757654b0884ab5a7e28b3c463bc9074b32837a2b69b61d914abf1d74ccd92b20fa44db3b31fb208c0dd44edaeb4ab097118e8ee374b6727b89ad6ce43f1b70c5a437ccebc36d2dad8ae973caad15cd89ae840fdae02cae42d241baef8fda8aa6bbaa54fd507a23338da6f06f61b34fb07d560e63fbce4a39c073e28573c2962cedb292b14b80d1b4e67b0465f2be0e38526232d0a7f88ce91a055fde082038a87ed91f3ef5ff971e30ea6cccf70d38498b186621c08f8fdceb8632992b480bf57fc218e91f2ca5936770fe9469", 16),
                new BigInteger("23", 16)));
        assertFalse(v.verify("bhostname", 22, key));
        assertNull(PreferencesFactory.get().getProperty("ssh.hostkey.ssh-rsa.bhostname"));
    }
}
