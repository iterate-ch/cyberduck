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
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.PasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.VersionId;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.io.StatusOutputStream;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.sds.io.swagger.client.model.UserFileKeySetRequest;
import ch.cyberduck.core.sds.triplecrypt.CryptoWriteFeature;
import ch.cyberduck.core.shared.DefaultFindFeature;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferItem;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.vault.VaultCredentials;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.lang3.RandomUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.ByteArrayInputStream;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class SDSMissingFileKeysSchedulerFeatureTest extends AbstractSDSTest {

    @Test
    public void testMissingKeys() throws Exception {
        final Path room = new Path("CD-TEST-ENCRYPTED", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final byte[] content = RandomUtils.nextBytes(32769);
        final TransferStatus status = new TransferStatus();
        status.setLength(content.length);
        final Path test = new Path(room, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file, Path.Type.decrypted));
        final SDSNodeIdProvider nodeid = new SDSNodeIdProvider(session).withCache(cache);
        final SDSEncryptionBulkFeature bulk = new SDSEncryptionBulkFeature(session, nodeid);
        bulk.pre(Transfer.Type.upload, Collections.singletonMap(new TransferItem(test), status), new DisabledConnectionCallback());
        final CryptoWriteFeature writer = new CryptoWriteFeature(session, new SDSWriteFeature(session, nodeid));
        final StatusOutputStream<VersionId> out = writer.write(test, status, new DisabledConnectionCallback());
        assertNotNull(out);
        new StreamCopier(status, status).transfer(new ByteArrayInputStream(content), out);
        final VersionId version = out.getStatus();
        assertNotNull(version);
        assertTrue(new DefaultFindFeature(session).find(test));
        assertEquals(content.length, new SDSAttributesFinderFeature(session, nodeid).find(test).getSize());
        final SDSMissingFileKeysSchedulerFeature background = new SDSMissingFileKeysSchedulerFeature(session, nodeid);
        final List<UserFileKeySetRequest> processed = background.operate(new PasswordCallback() {
            @Override
            public Credentials prompt(final Host bookmark, final String title, final String reason, final LoginOptions options) throws LoginCanceledException {
                return new VaultCredentials("ahbic3Ae");
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

    @Test(expected = LoginCanceledException.class)
    public void testWrongPassword() throws Exception {
        final SDSNodeIdProvider nodeid = new SDSNodeIdProvider(session).withCache(cache);
        final SDSMissingFileKeysSchedulerFeature background = new SDSMissingFileKeysSchedulerFeature(session, nodeid);
        final AtomicBoolean prompt = new AtomicBoolean();
        final List<UserFileKeySetRequest> processed = background.operate(new PasswordCallback() {
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
