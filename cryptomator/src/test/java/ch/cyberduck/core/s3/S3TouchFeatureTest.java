package ch.cyberduck.core.s3;

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
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.cryptomator.CryptoTouchFeature;
import ch.cyberduck.core.cryptomator.CryptoVault;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.features.Home;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.shared.DefaultFindFeature;
import ch.cyberduck.core.shared.DefaultTouchFeature;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.vault.DisabledVaultLookupListener;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;
import java.util.UUID;

import static org.junit.Assert.assertTrue;

@Category(IntegrationTest.class)
public class S3TouchFeatureTest {

    @Test
    public void testTouchEncrypted() throws Exception {
        final Host host = new Host(new S3Protocol(), new S3Protocol().getDefaultHostname(),
                new Credentials(
                        System.getProperties().getProperty("s3.key"), System.getProperties().getProperty("s3.secret")
                ));
        final S3Session session = new S3Session(host);
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final Path home = session.getFeature(Home.class).find();
        final Path vault = new Path(home, UUID.randomUUID().toString(), EnumSet.of(Path.Type.directory));
        final Path test = new Path(vault, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        final CryptoVault cryptomator = new CryptoVault(vault, new DisabledPasswordStore(), new DisabledPasswordCallback() {
            @Override
            public void prompt(final Credentials credentials, final String title, final String reason, final LoginOptions options) throws LoginCanceledException {
                credentials.setPassword("vault");
            }
        }, new DisabledVaultLookupListener()).create(session, null);
        session.withVault(cryptomator);
        new CryptoTouchFeature(session, new S3TouchFeature(session, session.getFeature(Write.class, new S3WriteFeature(session))), cryptomator).touch(test, new TransferStatus());
        assertTrue(session.getFeature(Find.class).find(test));
        session.getFeature(Delete.class).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
        session.close();
    }

    @Test
    public void testTouchLongFilenameEncrypted() throws Exception {
        final Host host = new Host(new S3Protocol(), new S3Protocol().getDefaultHostname(),
                new Credentials(
                        System.getProperties().getProperty("s3.key"), System.getProperties().getProperty("s3.secret")
                ));
        final S3Session session = new S3Session(host);
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final Path home = session.getFeature(Home.class).find();
        final Path vault = new Path(home, UUID.randomUUID().toString(), EnumSet.of(Path.Type.directory));
        final Path test = new Path(vault, RandomStringUtils.random(130), EnumSet.of(Path.Type.file));
        final CryptoVault cryptomator = new CryptoVault(vault, new DisabledPasswordStore(), new DisabledPasswordCallback() {
            @Override
            public void prompt(final Credentials credentials, final String title, final String reason, final LoginOptions options) throws LoginCanceledException {
                credentials.setPassword("vault");
            }
        }, new DisabledVaultLookupListener()).create(session, null);
        session.withVault(cryptomator);
        new CryptoTouchFeature(session, new S3TouchFeature(session, session.getFeature(Write.class, new S3WriteFeature(session))), cryptomator).touch(test, new TransferStatus());
        assertTrue(new S3FindFeature(session).find(test));
        session.getFeature(Delete.class).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
        session.close();
    }

    @Test
    public void testTouchEncryptedDefaultFeature() throws Exception {
        final Host host = new Host(new S3Protocol(), new S3Protocol().getDefaultHostname(),
                new Credentials(
                        System.getProperties().getProperty("s3.key"), System.getProperties().getProperty("s3.secret")
                ));
        final S3Session session = new S3Session(host);
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final Path home = session.getFeature(Home.class).find();
        final Path vault = new Path(home, UUID.randomUUID().toString(), EnumSet.of(Path.Type.directory));
        final Path test = new Path(vault, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        final CryptoVault cryptomator = new CryptoVault(vault, new DisabledPasswordStore(), new DisabledPasswordCallback() {
            @Override
            public void prompt(final Credentials credentials, final String title, final String reason, final LoginOptions options) throws LoginCanceledException {
                credentials.setPassword("vault");
            }
        }, new DisabledVaultLookupListener()).create(session, null);
        session.withVault(cryptomator);
        new CryptoTouchFeature(session, new DefaultTouchFeature(session), cryptomator).touch(test, new TransferStatus());
        assertTrue(new DefaultFindFeature(session).find(test));
        session.getFeature(Delete.class).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
        session.close();
    }
}
