package ch.cyberduck.core.sds;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.DisabledProgressListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LoginConnectionService;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.VersionId;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.io.StatusOutputStream;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.proxy.Proxy;
import ch.cyberduck.core.sds.io.swagger.client.ApiException;
import ch.cyberduck.core.sds.io.swagger.client.api.NodesApi;
import ch.cyberduck.core.sds.io.swagger.client.api.UserApi;
import ch.cyberduck.core.sds.io.swagger.client.model.FileKey;
import ch.cyberduck.core.sds.io.swagger.client.model.UserFileKeySetRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.UserKeyPairContainer;
import ch.cyberduck.core.sds.triplecrypt.TripleCryptConverter;
import ch.cyberduck.core.sds.triplecrypt.TripleCryptWriteFeature;
import ch.cyberduck.core.shared.DefaultFindFeature;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DisabledX509TrustManager;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferItem;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.vault.VaultCredentials;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.lang3.RandomUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import com.dracoon.sdk.crypto.Crypto;
import com.dracoon.sdk.crypto.model.EncryptedFileKey;
import com.dracoon.sdk.crypto.model.UserKeyPair;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class SDSMissingFileKeysSchedulerFeatureTest extends AbstractSDSTest {

    @Test
    public void testMissingKeys() throws Exception {
        final Path room = new Path("test", EnumSet.of(Path.Type.directory, Path.Type.volume, Path.Type.triplecrypt));
        final byte[] content = RandomUtils.nextBytes(32769);
        final TransferStatus status = new TransferStatus();
        status.setLength(content.length);
        final Path test = new Path(room, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file, Path.Type.triplecrypt));
        final SDSNodeIdProvider nodeid = new SDSNodeIdProvider(session).withCache(cache);
        final SDSEncryptionBulkFeature bulk = new SDSEncryptionBulkFeature(session, nodeid);
        bulk.pre(Transfer.Type.upload, Collections.singletonMap(new TransferItem(test), status), new DisabledConnectionCallback());
        final TripleCryptWriteFeature writer = new TripleCryptWriteFeature(session, nodeid, new SDSMultipartWriteFeature(session, nodeid));
        final StatusOutputStream<VersionId> out = writer.write(test, status, new DisabledConnectionCallback());
        assertNotNull(out);
        new StreamCopier(status, status).transfer(new ByteArrayInputStream(content), out);
        final VersionId version = out.getStatus();
        assertNotNull(version);
        assertTrue(new DefaultFindFeature(session).find(test));
        assertEquals(content.length, new SDSAttributesFinderFeature(session, nodeid).find(test).getSize());
        final SDSMissingFileKeysSchedulerFeature background = new SDSMissingFileKeysSchedulerFeature();
        final List<UserFileKeySetRequest> processed = background.operate(session, new DisabledPasswordCallback() {
            @Override
            public Credentials prompt(final Host bookmark, final String title, final String reason, final LoginOptions options) {
                return new VaultCredentials("eth[oh8uv4Eesij");
            }
        }, test);
        assertFalse(processed.isEmpty());
        boolean found = false;
        for(UserFileKeySetRequest p : processed) {
            if(p.getFileId().equals(Long.parseLong(version.id))) {
                found = true;
                break;
            }
        }
        assertTrue(found);
        new SDSDeleteFeature(session, nodeid).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    private void removeKeyPairs(UserApi userApi) throws ApiException {
        for(UserKeyPair.Version version : UserKeyPair.Version.values()) {
            try {
                userApi.removeUserKeyPair(version.getValue(), null);
            }
            catch(ApiException e) {
                final JsonObject json = JsonParser.parseReader(new StringReader(e.getResponseBody())).getAsJsonObject();
                if(json.has("errorCode")) {
                    if(json.get("errorCode").isJsonPrimitive()) {
                        final int errorCode = json.getAsJsonPrimitive("errorCode").getAsInt();
                        if(errorCode == -70020) {
                            // Ignore - User has no keypair with this algorithm version
                        }
                        else {
                            throw e;
                        }
                    }
                }
            }
        }
    }

    @Test
    public void testFileKeyMigration() throws Exception {
        final Host host = new Host(new SDSProtocol(), "cryptoiterate.dracoon.dev",
            new Credentials(System.getProperties().getProperty("sds.crypto.user"), System.getProperties().getProperty("sds.crypto.key")));
        final SDSSession session = new SDSSession(host, new DisabledX509TrustManager(), new DefaultX509KeyManager());
        final LoginConnectionService connect = new LoginConnectionService(new DisabledLoginCallback() {
            @Override
            public Credentials prompt(final Host bookmark, final String title, final String reason, final LoginOptions options) throws LoginCanceledException {
                throw new LoginCanceledException();
            }
        }, new DisabledHostKeyCallback(),
            new DisabledPasswordStore(), new DisabledProgressListener());
        connect.check(session, PathCache.empty(), new DisabledCancelCallback());
        final UserApi userApi = new UserApi(session.getClient());
        this.removeKeyPairs(userApi);
        session.resetUserKeyPairs();
        // create legacy and new crypto key pair
        final UserKeyPair deprecated = Crypto.generateUserKeyPair(UserKeyPair.Version.RSA2048, "eth[oh8uv4Eesij");
        userApi.setUserKeyPair(TripleCryptConverter.toSwaggerUserKeyPairContainer(deprecated), null);
        List<UserKeyPairContainer> keyPairs = userApi.requestUserKeyPairs(null, null);
        assertEquals(1, keyPairs.size());
        final Path room = new Path("cryptotest", EnumSet.of(Path.Type.directory, Path.Type.volume, Path.Type.triplecrypt));
        final byte[] content = RandomUtils.nextBytes(32769);
        final TransferStatus status = new TransferStatus();
        status.setLength(content.length);
        final Path test = new Path(room, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file, Path.Type.triplecrypt));
        final SDSNodeIdProvider nodeid = new SDSNodeIdProvider(session).withCache(cache);
        final SDSEncryptionBulkFeature bulk = new SDSEncryptionBulkFeature(session, nodeid);
        bulk.pre(Transfer.Type.upload, Collections.singletonMap(new TransferItem(test), status), new DisabledConnectionCallback());
        final TripleCryptWriteFeature writer = new TripleCryptWriteFeature(session, nodeid, new SDSMultipartWriteFeature(session, nodeid));
        final StatusOutputStream<VersionId> out = writer.write(test, status, new DisabledConnectionCallback());
        assertNotNull(out);
        new StreamCopier(status, status).transfer(new ByteArrayInputStream(content), out);
        final VersionId version = out.getStatus();
        // login to start migration
        session.getHost().setCredentials(
            new Credentials(System.getProperties().getProperty("sds.crypto.user"), System.getProperties().getProperty("sds.crypto.key")));
        session.login(Proxy.DIRECT, new DisabledLoginCallback() {
            @Override
            public Credentials prompt(final Host bookmark, final String title, final String reason, final LoginOptions options) throws LoginCanceledException {
                return new VaultCredentials("eth[oh8uv4Eesij");
            }
        }, new DisabledCancelCallback());
        keyPairs = userApi.requestUserKeyPairs(null, null);
        assertEquals(2, keyPairs.size());
        final FileKey key = new NodesApi(session.getClient()).requestUserFileKey(Long.parseLong(version.id), null, null);
        final EncryptedFileKey encFileKey = TripleCryptConverter.toCryptoEncryptedFileKey(key);
        assertEquals(EncryptedFileKey.Version.RSA2048_AES256GCM, encFileKey.getVersion());
        final SDSMissingFileKeysSchedulerFeature background = new SDSMissingFileKeysSchedulerFeature();
        final List<UserFileKeySetRequest> processed = background.operate(session, new DisabledPasswordCallback() {
            @Override
            public Credentials prompt(final Host bookmark, final String title, final String reason, final LoginOptions options) {
                return new VaultCredentials("eth[oh8uv4Eesij");
            }
        }, null);
        assertFalse(processed.isEmpty());
        boolean found = false;
        for(UserFileKeySetRequest p : processed) {
            if(p.getFileId().equals(Long.parseLong(version.id))) {
                found = true;
                break;
            }
        }
        assertTrue(found);
        final List<UserFileKeySetRequest> empty = new SDSMissingFileKeysSchedulerFeature().operate(session, new DisabledPasswordCallback() {
            @Override
            public Credentials prompt(final Host bookmark, final String title, final String reason, final LoginOptions options) {
                return new VaultCredentials("eth[oh8uv4Eesij");
            }
        }, null);
        assertTrue(empty.isEmpty());
        assertEquals(1, userApi.requestUserKeyPairs(null, null).size());
        new SDSDeleteFeature(session, nodeid).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test(expected = LoginCanceledException.class)
    public void testWrongPassword() throws Exception {
        final SDSNodeIdProvider nodeid = new SDSNodeIdProvider(session).withCache(cache);
        final SDSMissingFileKeysSchedulerFeature background = new SDSMissingFileKeysSchedulerFeature();
        final AtomicBoolean prompt = new AtomicBoolean();
        final List<UserFileKeySetRequest> processed = background.operate(session, new DisabledPasswordCallback() {
            @Override
            public Credentials prompt(final Host bookmark, final String title, final String reason, final LoginOptions options) throws LoginCanceledException {
                if(prompt.get()) {
                    throw new LoginCanceledException();
                }
                prompt.set(true);
                return new VaultCredentials("n");
            }
        }, null);
        assertTrue(prompt.get());
    }
}
