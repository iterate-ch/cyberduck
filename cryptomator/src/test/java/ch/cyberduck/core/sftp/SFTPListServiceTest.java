package ch.cyberduck.core.sftp;

/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.cryptomator.CryptoDeleteFeature;
import ch.cyberduck.core.cryptomator.CryptoListService;
import ch.cyberduck.core.cryptomator.CryptoTouchFeature;
import ch.cyberduck.core.cryptomator.CryptoVault;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.shared.DefaultTouchFeature;
import ch.cyberduck.core.shared.DefaultUploadFeature;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.vault.DefaultVaultRegistry;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@Category(IntegrationTest.class)
public class SFTPListServiceTest {

    @Test
    public void testListCryptomator() throws Exception {
        final Host host = new Host(new SFTPProtocol(), "test.cyberduck.ch", new Credentials(
                System.getProperties().getProperty("sftp.user"), System.getProperties().getProperty("sftp.password")
        ));
        final SFTPSession session = new SFTPSession(host);
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final Path home = new SFTPHomeDirectoryService(session).find();
        final Path vault = new Path(home, UUID.randomUUID().toString(), EnumSet.of(Path.Type.directory));
        final Path test = new Path(vault, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        final CryptoVault cryptomator = new CryptoVault(vault, new DisabledPasswordStore()).create(session, null, new DisabledPasswordCallback() {
            @Override
            public void prompt(final Credentials credentials, final String title, final String reason, final LoginOptions options) throws LoginCanceledException {
                credentials.setPassword("vault");
            }
        });
        session.withRegistry(new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator));
        assertTrue(new CryptoListService(session, new SFTPListService(session), cryptomator).list(vault, new DisabledListProgressListener()).isEmpty());
        new CryptoTouchFeature<Void>(session, new DefaultTouchFeature<Void>(new DefaultUploadFeature<Void>(new SFTPWriteFeature(session))), new SFTPWriteFeature(session), cryptomator).touch(test, new TransferStatus());
        assertEquals(test, new CryptoListService(session, new SFTPListService(session), cryptomator).list(vault, new DisabledListProgressListener()).get(0));
        new CryptoDeleteFeature(session, new SFTPDeleteFeature(session), cryptomator).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
        session.close();
    }
}
