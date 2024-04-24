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

import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordCallback;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.io.StatusOutputStream;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.sds.io.swagger.client.ApiException;
import ch.cyberduck.core.sds.io.swagger.client.api.NodesApi;
import ch.cyberduck.core.sds.io.swagger.client.api.UserApi;
import ch.cyberduck.core.sds.io.swagger.client.model.EncryptRoomRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.FileKey;
import ch.cyberduck.core.sds.io.swagger.client.model.Node;
import ch.cyberduck.core.sds.io.swagger.client.model.NodePermissions;
import ch.cyberduck.core.sds.io.swagger.client.model.RoomUsersAddBatchRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.RoomUsersAddBatchRequestItem;
import ch.cyberduck.core.sds.io.swagger.client.model.UserFileKeySetRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.UserKeyPairContainer;
import ch.cyberduck.core.sds.triplecrypt.TripleCryptConverter;
import ch.cyberduck.core.sds.triplecrypt.TripleCryptWriteFeature;
import ch.cyberduck.core.shared.DefaultFindFeature;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferItem;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.vault.VaultCredentials;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
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

import static ch.cyberduck.core.sds.SDSAttributesFinderFeature.KEY_ENCRYPTED;
import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class SDSMissingFileKeysSchedulerFeatureTest extends AbstractSDSTest {

    @Test
    public void testMissingKeys() throws Exception {
        final SDSNodeIdProvider nodeid = new SDSNodeIdProvider(session);
        final Path room = new SDSDirectoryFeature(session, nodeid).mkdir(new Path(
                new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), new TransferStatus());
        final EncryptRoomRequest encrypt = new EncryptRoomRequest().isEncrypted(true);
        final Node node = new NodesApi(session.getClient()).encryptRoom(encrypt, Long.parseLong(new SDSNodeIdProvider(session).getVersionId(room)), StringUtils.EMPTY, null);
        new NodesApi(session.getClient()).updateRoomUsers(new RoomUsersAddBatchRequest().
                addItemsItem(new RoomUsersAddBatchRequestItem().id(757L).permissions(new NodePermissions().read(true))), node.getId(), StringUtils.EMPTY);
        room.attributes().withCustom(KEY_ENCRYPTED, String.valueOf(true));
        final byte[] content = RandomUtils.nextBytes(32769);
        final TransferStatus status = new TransferStatus();
        status.setLength(content.length);
        final Path test = new Path(room, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        final SDSEncryptionBulkFeature bulk = new SDSEncryptionBulkFeature(session, nodeid);
        bulk.pre(Transfer.Type.upload, Collections.singletonMap(new TransferItem(test), status), new DisabledConnectionCallback());
        final TripleCryptWriteFeature writer = new TripleCryptWriteFeature(session, nodeid, new SDSDirectS3MultipartWriteFeature(session, nodeid));
        final StatusOutputStream<Node> out = writer.write(test, status, new DisabledConnectionCallback());
        new StreamCopier(status, status).transfer(new ByteArrayInputStream(content), out);
        assertTrue(new DefaultFindFeature(session).find(test));
        assertEquals(content.length, new SDSAttributesFinderFeature(session, nodeid).find(test).getSize());
        final SDSMissingFileKeysSchedulerFeature background = new SDSMissingFileKeysSchedulerFeature();
        final List<UserFileKeySetRequest> processed = background.operate(session, new DisabledPasswordCallback() {
            @Override
            public Credentials prompt(final Host bookmark, final String title, final String reason, final LoginOptions options) {
                return new VaultCredentials("eth[oh8uv4Eesij");
            }
        }, test);
        assertTrue(processed.stream().filter(userFileKeySetRequest -> userFileKeySetRequest.getFileId().equals(Long.parseLong(test.attributes().getVersionId()))).findAny().isPresent());
        new SDSDeleteFeature(session, nodeid).delete(Collections.singletonList(room), new DisabledLoginCallback(), new Delete.DisabledCallback());
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
        final UserApi userApi = new UserApi(session.getClient());
        this.removeKeyPairs(userApi);
        session.resetUserKeyPairs();
        // create legacy and new crypto key pair
        final UserKeyPair deprecated = Crypto.generateUserKeyPair(UserKeyPair.Version.RSA2048, "eth[oh8uv4Eesij".toCharArray());
        userApi.setUserKeyPair(TripleCryptConverter.toSwaggerUserKeyPairContainer(deprecated), null);
        List<UserKeyPairContainer> keyPairs = userApi.requestUserKeyPairs(null, null);
        assertEquals(1, keyPairs.size());
        final SDSNodeIdProvider nodeid = new SDSNodeIdProvider(session);
        final Path room = new SDSDirectoryFeature(session, nodeid).createRoom(
                new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), true);
        final byte[] content = RandomUtils.nextBytes(32769);
        final TransferStatus status = new TransferStatus();
        status.setLength(content.length);
        final Path test = new Path(room, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        final SDSEncryptionBulkFeature bulk = new SDSEncryptionBulkFeature(session, nodeid);
        bulk.pre(Transfer.Type.upload, Collections.singletonMap(new TransferItem(test), status), new DisabledConnectionCallback());
        final TripleCryptWriteFeature writer = new TripleCryptWriteFeature(session, nodeid, new SDSDirectS3MultipartWriteFeature(session, nodeid));
        final StatusOutputStream<Node> out = writer.write(test, status, new DisabledConnectionCallback());
        new StreamCopier(status, status).transfer(new ByteArrayInputStream(content), out);
        // Start migration
        session.unlockTripleCryptKeyPair(new DisabledLoginCallback() {
            @Override
            public Credentials prompt(final Host bookmark, final String title, final String reason, final LoginOptions options) throws LoginCanceledException {
                return new VaultCredentials("eth[oh8uv4Eesij");
            }
        }, session.userAccount(), UserKeyPair.Version.RSA4096);
        keyPairs = userApi.requestUserKeyPairs(null, null);
        assertEquals(2, keyPairs.size());
        final FileKey key = new NodesApi(session.getClient()).requestUserFileKey(Long.parseLong(test.attributes().getVersionId()), null, null);
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
            if(p.getFileId().equals(Long.parseLong(test.attributes().getVersionId()))) {
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
        assertEquals(2, userApi.requestUserKeyPairs(null, null).size());
        new SDSDeleteFeature(session, nodeid).delete(Collections.singletonList(room), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test(expected = LoginCanceledException.class)
    public void testWrongPassword() throws Exception {
        final SDSNodeIdProvider nodeid = new SDSNodeIdProvider(session);
        final Path room = new SDSDirectoryFeature(session, nodeid).mkdir(new Path(
                new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), new TransferStatus());
        final EncryptRoomRequest encrypt = new EncryptRoomRequest().isEncrypted(true);
        final Node node = new NodesApi(session.getClient()).encryptRoom(encrypt, Long.parseLong(new SDSNodeIdProvider(session).getVersionId(room)), StringUtils.EMPTY, null);
        new NodesApi(session.getClient()).updateRoomUsers(new RoomUsersAddBatchRequest().
                addItemsItem(new RoomUsersAddBatchRequestItem().id(757L).permissions(new NodePermissions().read(true))), node.getId(), StringUtils.EMPTY);
        room.attributes().withCustom(KEY_ENCRYPTED, String.valueOf(true));
        final byte[] content = RandomUtils.nextBytes(32769);
        final TransferStatus status = new TransferStatus();
        status.setLength(content.length);
        final Path test = new Path(room, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        final SDSEncryptionBulkFeature bulk = new SDSEncryptionBulkFeature(session, nodeid);
        bulk.pre(Transfer.Type.upload, Collections.singletonMap(new TransferItem(test), status), new DisabledConnectionCallback());
        final TripleCryptWriteFeature writer = new TripleCryptWriteFeature(session, nodeid, new SDSDirectS3MultipartWriteFeature(session, nodeid));
        final StatusOutputStream<Node> out = writer.write(test, status, new DisabledConnectionCallback());
        new StreamCopier(status, status).transfer(new ByteArrayInputStream(content), out);
        assertTrue(new DefaultFindFeature(session).find(test));
        assertEquals(content.length, new SDSAttributesFinderFeature(session, nodeid).find(test).getSize());
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
