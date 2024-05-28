package ch.cyberduck.core.sds;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import ch.cyberduck.core.*;
import ch.cyberduck.core.exception.ConnectionRefusedException;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.exception.ProxyLoginFailureException;
import ch.cyberduck.core.features.Upload;
import ch.cyberduck.core.proxy.DisabledProxyFinder;
import ch.cyberduck.core.proxy.Proxy;
import ch.cyberduck.core.proxy.ProxyFinder;
import ch.cyberduck.core.sds.io.swagger.client.ApiException;
import ch.cyberduck.core.sds.io.swagger.client.api.UserApi;
import ch.cyberduck.core.sds.io.swagger.client.model.ClassificationPoliciesConfig;
import ch.cyberduck.core.sds.io.swagger.client.model.UserKeyPairContainer;
import ch.cyberduck.core.sds.triplecrypt.TripleCryptConverter;
import ch.cyberduck.core.serializer.impl.dd.ProfilePlistReader;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DefaultX509TrustManager;
import ch.cyberduck.core.ssl.DisabledX509TrustManager;
import ch.cyberduck.core.ssl.KeychainX509KeyManager;
import ch.cyberduck.core.vault.VaultCredentials;
import ch.cyberduck.test.IntegrationTest;

import org.apache.http.HttpStatus;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;

import com.dracoon.sdk.crypto.Crypto;
import com.dracoon.sdk.crypto.model.UserKeyPair;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class SDSSessionTest extends AbstractSDSTest {

    @Test
    public void testClassificationConfiguration() throws Exception {
        final ClassificationPoliciesConfig policies = session.shareClassificationsPolicies();
        assertNotNull(policies);
        assertNotNull(policies.getShareClassificationPolicies());
        assertNotNull(policies.getShareClassificationPolicies().getClassificationRequiresSharePassword());
        assertEquals(0, policies.getShareClassificationPolicies().getClassificationRequiresSharePassword().getValue().intValue());
    }

    @Test(expected = LoginCanceledException.class)
    public void testLoginOAuthExpiredRefreshToken() throws Exception {
        final ProtocolFactory factory = new ProtocolFactory(new HashSet<>(Collections.singleton(new SDSProtocol())));
        final Profile profile = new ProfilePlistReader(factory).read(
                this.getClass().getResourceAsStream("/DRACOON (OAuth).cyberduckprofile"));
        final Host host = new Host(profile, "duck.dracoon.com", new Credentials(
                System.getProperties().getProperty("dracoon.user"), System.getProperties().getProperty("dracoon.key")
        ));
        final SDSSession session = new SDSSession(host, new DisabledX509TrustManager(), new DefaultX509KeyManager());
        assertNotNull(session.open(new DisabledProxyFinder(), new DisabledHostKeyCallback(), new DisabledLoginCallback(), new DisabledCancelCallback()));
        assertTrue(session.isConnected());
        assertNotNull(session.getClient());
        session.login(new DisabledLoginCallback(), new DisabledCancelCallback());
        assertFalse(new SDSListService(session, new SDSNodeIdProvider(session)).list(new Path("/", EnumSet.of(Path.Type.directory)), new DisabledListProgressListener()).isEmpty());
    }

    @Test(expected = ConnectionRefusedException.class)
    public void testProxyNoConnect() throws Exception {
        final ProtocolFactory factory = new ProtocolFactory(new HashSet<>(Collections.singleton(new SDSProtocol())));
        final Profile profile = new ProfilePlistReader(factory).read(
                this.getClass().getResourceAsStream("/DRACOON (CLI).cyberduckprofile"));
        final Host host = new Host(profile, "duck.dracoon.com", new Credentials(
                System.getProperties().getProperty("dracoon.user"), System.getProperties().getProperty("dracoon.key")
        ));
        final SDSSession session = new SDSSession(host, new DisabledX509TrustManager(), new DefaultX509KeyManager());
        final LoginConnectionService c = new LoginConnectionService(
                new DisabledLoginCallback(),
                new DisabledHostKeyCallback(),
                new DisabledPasswordStore(),
                new DisabledProgressListener(),
                new ProxyFinder() {
                    @Override
                    public Proxy find(final String target) {
                        return new Proxy(Proxy.Type.HTTP, "localhost", 3128);
                    }
                }
        );
        c.connect(session, new DisabledCancelCallback());
    }

    @Ignore
    @Test(expected = ProxyLoginFailureException.class)
    public void testConnectProxyInvalidCredentials() throws Exception {
        final ProtocolFactory factory = new ProtocolFactory(new HashSet<>(Collections.singleton(new SDSProtocol())));
        final Profile profile = new ProfilePlistReader(factory).read(
                this.getClass().getResourceAsStream("/DRACOON (CLI).cyberduckprofile"));
        final Host host = new Host(profile, "duck.dracoon.com", new Credentials(
                System.getProperties().getProperty("dracoon.user"), System.getProperties().getProperty("dracoon.key")
        ));
        final SDSSession session = new SDSSession(host, new DefaultX509TrustManager(),
                new KeychainX509KeyManager(new DisabledCertificateIdentityCallback(), host, new DisabledCertificateStore())) {
        };
        final LoginConnectionService c = new LoginConnectionService(
                new DisabledLoginCallback() {
                    @Override
                    public Credentials prompt(final Host bookmark, final String username, final String title, final String reason, final LoginOptions options) {
                        return new Credentials("test", "n");
                    }
                },
                new DisabledHostKeyCallback(),
                new DisabledPasswordStore(),
                new DisabledProgressListener(),
                new ProxyFinder() {
                    @Override
                    public Proxy find(final String target) {
                        return new Proxy(Proxy.Type.HTTP, "localhost", 3128);
                    }
                }
        );
        c.connect(session, new DisabledCancelCallback());
    }

    @Test
    public void testKeyPairMigration() throws Exception {
        final UserApi userApi = new UserApi(session.getClient());
        try {
            userApi.removeUserKeyPair(UserKeyPair.Version.RSA2048.getValue(), null);
        }
        catch(ApiException e) {
            if(e.getCode() == HttpStatus.SC_NOT_FOUND) {
                //ignore
            }
            else {
                throw e;
            }
        }
        try {
            userApi.removeUserKeyPair(UserKeyPair.Version.RSA4096.getValue(), null);
        }
        catch(ApiException e) {
            if(e.getCode() == HttpStatus.SC_NOT_FOUND) {
                //ignore
            }
            else {
                throw e;
            }
        }
        // create legacy key pair
        final UserKeyPair userKeyPair = Crypto.generateUserKeyPair(UserKeyPair.Version.RSA2048, "eth[oh8uv4Eesij".toCharArray());
        userApi.setUserKeyPair(TripleCryptConverter.toSwaggerUserKeyPairContainer(userKeyPair), null);
        List<UserKeyPairContainer> keyPairs = userApi.requestUserKeyPairs(null, null);
        assertEquals(1, keyPairs.size());
        // Start migration
        session.unlockTripleCryptKeyPair(new DisabledLoginCallback() {
            @Override
            public Credentials prompt(final Host bookmark, final String title, final String reason, final LoginOptions options) throws LoginCanceledException {
                return new VaultCredentials("eth[oh8uv4Eesij");
            }
        }, session.userAccount(), UserKeyPair.Version.RSA4096);
        keyPairs = userApi.requestUserKeyPairs(null, null);
        assertEquals(2, keyPairs.size());
        assertEquals(UserKeyPair.Version.RSA4096.getValue(), session.keyPair().getPublicKeyContainer().getVersion());
        assertEquals(UserKeyPair.Version.RSA2048.getValue(), session.keyPairDeprecated().getPublicKeyContainer().getVersion());
    }

    @Test
    public void testUploadFeature() throws Exception {
        final Upload feature = session.getFeature(Upload.class);
        assertSame(feature.getClass(), SDSDirectS3UploadFeature.class);
    }
}
