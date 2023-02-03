package ch.cyberduck.core.eue;

/*
 * Copyright (c) 2002-2021 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import ch.cyberduck.core.*;
import ch.cyberduck.core.cryptomator.CryptoVault;
import ch.cyberduck.core.eue.io.swagger.client.ApiException;
import ch.cyberduck.core.eue.io.swagger.client.api.CreateShareApi;
import ch.cyberduck.core.eue.io.swagger.client.model.ShareCreationRequestModel;
import ch.cyberduck.core.eue.io.swagger.client.model.ShareCreationResponseEntry;
import ch.cyberduck.core.eue.io.swagger.client.model.ShareCreationResponseModel;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.http.HttpResponseOutputStream;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.serializer.impl.dd.ProfilePlistReader;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DisabledX509TrustManager;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.VaultTest;

import org.junit.After;
import org.junit.Before;
import org.junit.runners.Parameterized;

import java.io.ByteArrayInputStream;
import java.util.Collections;
import java.util.HashSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class AbstractEueSessionTest extends VaultTest {
    protected EueSession session;
    protected Profile profile;

    @Parameterized.Parameters(name = "vaultVersion = {0}")
    public static Object[] data() {
        return new Object[]{CryptoVault.VAULT_VERSION_DEPRECATED, CryptoVault.VAULT_VERSION};
    }

    @Parameterized.Parameter
    public int vaultVersion;

    @Before
    public void setup() throws Exception {
        final ProtocolFactory factory = new ProtocolFactory(new HashSet<>(Collections.singleton(new EueProtocol())));
        profile = new ProfilePlistReader(factory).read(
                this.getClass().getResourceAsStream(String.format("/%s/GMX Cloud.cyberduckprofile", this.getSupportedPlatform().name())));
        final Host host = new Host(profile, profile.getDefaultHostname(), new Credentials(
                PROPERTIES.get("eue.user"), PROPERTIES.get("eue.password")
        )) {
            @Override
            public String getProperty(final String key) {
                if("eue.share.writable".equals(key)) {
                    return String.valueOf(true);
                }
                return super.getProperty(key);
            }
        };
        session = new EueSession(host, new DisabledX509TrustManager(), new DefaultX509KeyManager());
        final LoginConnectionService login = new LoginConnectionService(new DisabledLoginCallback() {
            @Override
            public Credentials prompt(final Host bookmark, final String title, final String reason, final LoginOptions options) {
                fail(reason);
                return null;
            }
        }, new DisabledHostKeyCallback(), new TestPasswordStore(), new DisabledProgressListener());
        login.check(session, new DisabledCancelCallback());
    }

    private Factory.Platform.Name getSupportedPlatform() {
        switch(Factory.Platform.getDefault()) {
            case windows:
                return Factory.Platform.Name.windows;
            default:
                return Factory.Platform.Name.mac;
        }
    }

    @After
    public void disconnect() throws Exception {
        session.close();
    }

    public static class TestPasswordStore extends DisabledPasswordStore {
        @Override
        public String getPassword(final String serviceName, final String accountName) {
            if(accountName.equals("GMX Cloud (1015156902205593160) OAuth2 Token Expiry")) {
                return PROPERTIES.get("eue.tokenexpiry");
            }
            return null;
        }

        @Override
        public String getPassword(Scheme scheme, int port, String hostname, String user) {
            if(user.equals("GMX Cloud (1015156902205593160) OAuth2 Access Token")) {
                return PROPERTIES.get("eue.accesstoken");
            }
            if(user.equals("GMX Cloud (1015156902205593160) OAuth2 Refresh Token")) {
                return PROPERTIES.get("eue.refreshtoken");
            }
            return null;
        }

        @Override
        public void addPassword(final String serviceName, final String accountName, final String password) {
            if(accountName.equals("GMX Cloud (1015156902205593160) OAuth2 Token Expiry")) {
                VaultTest.add("eue.tokenexpiry", password);
            }
        }

        @Override
        public void addPassword(final Scheme scheme, final int port, final String hostname, final String user, final String password) {
            if(user.equals("GMX Cloud (1015156902205593160) OAuth2 Access Token")) {
                VaultTest.add("eue.accesstoken", password);
            }
            if(user.equals("GMX Cloud (1015156902205593160) OAuth2 Refresh Token")) {
                VaultTest.add("eue.refreshtoken", password);
            }
        }
    }

    protected Path createFile(final EueResourceIdProvider fileid, Path file, final byte[] content) throws Exception {
        final EueWriteFeature feature = new EueWriteFeature(session, fileid);
        final TransferStatus status = new TransferStatus()
                .withChecksum(feature.checksum(file, new TransferStatus().withLength(content.length)).compute(new ByteArrayInputStream(content), new TransferStatus().withLength(content.length)))
                .withLength(content.length);
        final HttpResponseOutputStream<EueWriteFeature.Chunk> out = feature.write(file, status, new DisabledConnectionCallback());
        final ByteArrayInputStream in = new ByteArrayInputStream(content);
        final TransferStatus progress = new TransferStatus();
        final BytecountStreamListener count = new BytecountStreamListener();
        new StreamCopier(new TransferStatus(), progress).withListener(count).transfer(in, out);
        assertEquals(content.length, count.getSent());
        in.close();
        out.close();
        return file;
    }

    protected ShareCreationResponseEntry createShare(final EueResourceIdProvider fileid, final Path file) throws BackgroundException, ApiException {
        final EueShareFeature shareFeature = new EueShareFeature(session, fileid);
        final DisabledPasswordCallback disabledPasswordCallback = new DisabledPasswordCallback() {
            @Override
            public Credentials prompt(final Host bookmark, final String title, final String reason, final LoginOptions options) {
                return new Credentials(null, new AlphanumericRandomStringService().random());
            }
        };
        final ShareCreationRequestModel shareCreationRequestModel = shareFeature.createShareCreationRequestModel(file, disabledPasswordCallback);
        final EueApiClient client = new EueApiClient(session);
        final CreateShareApi createShareApi = new CreateShareApi(client);
        final ShareCreationResponseModel shareCreationResponseModel = createShareApi.resourceResourceIdSharePost(fileid.getFileId(file), shareCreationRequestModel, null, null);
        return shareCreationResponseModel.get("!ano");
    }
}
