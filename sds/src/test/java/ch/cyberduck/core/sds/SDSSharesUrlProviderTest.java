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

import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DescriptiveUrl;
import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.PasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.sds.io.swagger.client.model.CreateDownloadShareRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.ObjectExpiration;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DisabledX509TrustManager;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.vault.VaultCredentials;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class SDSSharesUrlProviderTest {

    @Test
    public void testShareFile() throws Exception {
        final Host host = new Host(new SDSProtocol(), "duck.ssp-europe.eu", new Credentials(
            System.getProperties().getProperty("sds.user"), System.getProperties().getProperty("sds.key")
        ));
        final SDSSession session = new SDSSession(host, new DisabledX509TrustManager(), new DefaultX509KeyManager());
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        final Path room = new SDSDirectoryFeature(session).mkdir(new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), null, new TransferStatus());
        final Path test = new SDSTouchFeature(session).touch(new Path(room, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        final DescriptiveUrl url = new SDSSharesUrlProvider(session).toDownloadUrl(test,
            new CreateDownloadShareRequest()
                .expiration(new ObjectExpiration().enableExpiration(false))
                .notifyCreator(false)
                .sendMail(false)
                .sendSms(false)
                .password(null)
                .mailRecipients(null)
                .mailSubject(null)
                .mailBody(null)
                .maxDownloads(null), new DisabledPasswordCallback());
        assertNotEquals(DescriptiveUrl.EMPTY, url);
        assertEquals(DescriptiveUrl.Type.signed, url.getType());
        assertTrue(url.getUrl().startsWith("https://duck.ssp-europe.eu/#/public/shares-downloads/"));
        new SDSDeleteFeature(session).delete(Collections.singletonList(room), new DisabledLoginCallback(), new Delete.DisabledCallback());
        session.close();
    }

    @Test
    public void testShareTopLevelRoom() throws Exception {
        final Host host = new Host(new SDSProtocol(), "duck.ssp-europe.eu", new Credentials(
            System.getProperties().getProperty("sds.user"), System.getProperties().getProperty("sds.key")
        ));
        final SDSSession session = new SDSSession(host, new DisabledX509TrustManager(), new DefaultX509KeyManager());
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        final Path room = new SDSDirectoryFeature(session).mkdir(new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), null, new TransferStatus());
        final DescriptiveUrl url = new SDSSharesUrlProvider(session).toDownloadUrl(room,
            new CreateDownloadShareRequest()
                .expiration(new ObjectExpiration().enableExpiration(false))
                .notifyCreator(false)
                .sendMail(false)
                .sendSms(false)
                .password(null)
                .mailRecipients(null)
                .mailSubject(null)
                .mailBody(null)
                .maxDownloads(null), new DisabledPasswordCallback());
        assertNotEquals(DescriptiveUrl.EMPTY, url);
        assertEquals(DescriptiveUrl.Type.signed, url.getType());
        assertTrue(url.getUrl().startsWith("https://duck.ssp-europe.eu/#/public/shares-downloads/"));
        new SDSDeleteFeature(session).delete(Collections.singletonList(room), new DisabledLoginCallback(), new Delete.DisabledCallback());
        session.close();
    }

    @Test
    public void testShareSubRoom() throws Exception {
        final Host host = new Host(new SDSProtocol(), "duck.ssp-europe.eu", new Credentials(
            System.getProperties().getProperty("sds.user"), System.getProperties().getProperty("sds.key")
        ));
        final SDSSession session = new SDSSession(host, new DisabledX509TrustManager(), new DefaultX509KeyManager());
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        final Path room = new SDSDirectoryFeature(session).mkdir(new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), null, new TransferStatus());
        final Path test = new SDSDirectoryFeature(session).mkdir(new Path(room, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), null, new TransferStatus());
        final DescriptiveUrl url = new SDSSharesUrlProvider(session).toDownloadUrl(test,
            new CreateDownloadShareRequest()
                .expiration(new ObjectExpiration().enableExpiration(false))
                .notifyCreator(false)
                .sendMail(false)
                .sendSms(false)
                .password(null)
                .mailRecipients(null)
                .mailSubject(null)
                .mailBody(null)
                .maxDownloads(null), new DisabledPasswordCallback());
        assertNotEquals(DescriptiveUrl.EMPTY, url);
        assertEquals(DescriptiveUrl.Type.signed, url.getType());
        assertTrue(url.getUrl().startsWith("https://duck.ssp-europe.eu/#/public/shares-downloads/"));
        new SDSDeleteFeature(session).delete(Collections.singletonList(room), new DisabledLoginCallback(), new Delete.DisabledCallback());
        session.close();
    }

    @Test(expected = InteroperabilityException.class)
    public void testToUrlMissingEmailRecipients() throws Exception {
        final Host host = new Host(new SDSProtocol(), "duck.ssp-europe.eu", new Credentials(
            System.getProperties().getProperty("sds.user"), System.getProperties().getProperty("sds.key")
        ));
        final SDSSession session = new SDSSession(host, new DisabledX509TrustManager(), new DefaultX509KeyManager());
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        final Path room = new SDSDirectoryFeature(session).mkdir(new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), null, new TransferStatus());
        final Path test = new SDSTouchFeature(session).touch(new Path(room, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        try {
            final DescriptiveUrl url = new SDSSharesUrlProvider(session).toDownloadUrl(test,
                new CreateDownloadShareRequest()
                    .expiration(new ObjectExpiration().enableExpiration(false))
                    .notifyCreator(false)
                    .sendMail(true)
                    .mailRecipients(null)
                    .sendSms(false)
                    .password(null)
                    .mailSubject(null)
                    .mailBody(null)
                    .maxDownloads(null), new DisabledPasswordCallback());
        }
        finally {
            new SDSDeleteFeature(session).delete(Collections.singletonList(room), new DisabledLoginCallback(), new Delete.DisabledCallback());
            session.close();
        }
    }

    @Test(expected = InteroperabilityException.class)
    public void testToUrlInvalidSMSRecipients() throws Exception {
        final Host host = new Host(new SDSProtocol(), "duck.ssp-europe.eu", new Credentials(
            System.getProperties().getProperty("sds.user"), System.getProperties().getProperty("sds.key")
        ));
        final SDSSession session = new SDSSession(host, new DisabledX509TrustManager(), new DefaultX509KeyManager());
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        final Path room = new SDSDirectoryFeature(session).mkdir(new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), null, new TransferStatus());
        final Path test = new SDSTouchFeature(session).touch(new Path(room, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        try {
            final DescriptiveUrl url = new SDSSharesUrlProvider(session).toDownloadUrl(test,
                new CreateDownloadShareRequest()
                    .expiration(new ObjectExpiration().enableExpiration(false))
                    .notifyCreator(false)
                    .sendMail(false)
                    .mailRecipients(null)
                    .sendSms(true)
                    .smsRecipients("invalid")
                    .password("p")
                    .mailSubject(null)
                    .mailBody(null)
                    .maxDownloads(null), new DisabledPasswordCallback());
        }
        finally {
            new SDSDeleteFeature(session).delete(Collections.singletonList(room), new DisabledLoginCallback(), new Delete.DisabledCallback());
            session.close();
        }
    }

    @Test(expected = InteroperabilityException.class)
    public void testToUrlWeakPassword() throws Exception {
        final Host host = new Host(new SDSProtocol(), "duck.ssp-europe.eu", new Credentials(
            System.getProperties().getProperty("sds.user"), System.getProperties().getProperty("sds.key")
        ));
        final SDSSession session = new SDSSession(host, new DisabledX509TrustManager(), new DefaultX509KeyManager());
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        final Path room = new SDSDirectoryFeature(session).mkdir(new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), null, new TransferStatus());
        final Path test = new SDSTouchFeature(session).touch(new Path(room, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        try {
            final DescriptiveUrl url = new SDSSharesUrlProvider(session).toDownloadUrl(test,
                new CreateDownloadShareRequest()
                    .expiration(new ObjectExpiration().enableExpiration(false))
                    .notifyCreator(false)
                    .sendMail(false)
                    .mailRecipients(null)
                    .sendSms(false)
                    .password("p")
                    .mailSubject(null)
                    .mailBody(null)
                    .maxDownloads(null), new DisabledPasswordCallback());
        }
        finally {
            new SDSDeleteFeature(session).delete(Collections.singletonList(room), new DisabledLoginCallback(), new Delete.DisabledCallback());
            session.close();
        }
    }

    @Test(expected = InteroperabilityException.class)
    public void testToUrlInvalidEmail() throws Exception {
        final Host host = new Host(new SDSProtocol(), "duck.ssp-europe.eu", new Credentials(
            System.getProperties().getProperty("sds.user"), System.getProperties().getProperty("sds.key")
        ));
        final SDSSession session = new SDSSession(host, new DisabledX509TrustManager(), new DefaultX509KeyManager());
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        final Path room = new SDSDirectoryFeature(session).mkdir(new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), null, new TransferStatus());
        final Path test = new SDSTouchFeature(session).touch(new Path(room, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        try {
            final DescriptiveUrl url = new SDSSharesUrlProvider(session).toDownloadUrl(test,
                new CreateDownloadShareRequest()
                    .expiration(new ObjectExpiration().enableExpiration(false))
                    .notifyCreator(false)
                    .sendMail(true)
                    .mailRecipients("a@b")
                    .sendSms(false)
                    .mailSubject(null)
                    .mailBody(null)
                    .maxDownloads(null), new DisabledPasswordCallback());
        }
        finally {
            new SDSDeleteFeature(session).delete(Collections.singletonList(room), new DisabledLoginCallback(), new Delete.DisabledCallback());
            session.close();
        }
    }

    @Test
    public void testToUrlExpiry() throws Exception {
        final Host host = new Host(new SDSProtocol(), "duck.ssp-europe.eu", new Credentials(
            System.getProperties().getProperty("sds.user"), System.getProperties().getProperty("sds.key")
        ));
        final SDSSession session = new SDSSession(host, new DisabledX509TrustManager(), new DefaultX509KeyManager());
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        final Path room = new SDSDirectoryFeature(session).mkdir(new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), null, new TransferStatus());
        final Path test = new SDSTouchFeature(session).touch(new Path(room, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        final DescriptiveUrl url = new SDSSharesUrlProvider(session).toDownloadUrl(test,
            new CreateDownloadShareRequest()
                .expiration(new ObjectExpiration().enableExpiration(true).expireAt(new Date(1744300800000L)))
                .notifyCreator(false)
                .sendMail(false)
                .sendSms(false)
                .password(null)
                .mailRecipients(null)
                .mailSubject(null)
                .mailBody(null)
                .maxDownloads(null), new DisabledPasswordCallback());
        assertNotEquals(DescriptiveUrl.EMPTY, url);
        assertEquals(DescriptiveUrl.Type.signed, url.getType());
        assertTrue(url.getUrl().startsWith("https://duck.ssp-europe.eu/#/public/shares-downloads/"));
        new SDSDeleteFeature(session).delete(Collections.singletonList(room), new DisabledLoginCallback(), new Delete.DisabledCallback());
        session.close();
    }

    @Test(expected = InteroperabilityException.class)
    public void testToUrlExpiryInvalidDate() throws Exception {
        final Host host = new Host(new SDSProtocol(), "duck.ssp-europe.eu", new Credentials(
            System.getProperties().getProperty("sds.user"), System.getProperties().getProperty("sds.key")
        ));
        final SDSSession session = new SDSSession(host, new DisabledX509TrustManager(), new DefaultX509KeyManager());
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        final Path room = new SDSDirectoryFeature(session).mkdir(new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), null, new TransferStatus());
        final Path test = new SDSTouchFeature(session).touch(new Path(room, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        final DescriptiveUrl url = new SDSSharesUrlProvider(session).toDownloadUrl(test,
            new CreateDownloadShareRequest()
                .expiration(new ObjectExpiration().enableExpiration(true).expireAt(new Date(17443L)))
                .notifyCreator(false)
                .sendMail(false)
                .sendSms(false)
                .password(null)
                .mailRecipients(null)
                .mailSubject(null)
                .mailBody(null)
                .maxDownloads(null), new DisabledPasswordCallback());
        assertNotEquals(DescriptiveUrl.EMPTY, url);
        assertEquals(DescriptiveUrl.Type.signed, url.getType());
        assertTrue(url.getUrl().startsWith("https://duck.ssp-europe.eu/#/public/shares-downloads/"));
        new SDSDeleteFeature(session).delete(Collections.singletonList(room), new DisabledLoginCallback(), new Delete.DisabledCallback());
        session.close();
    }

    @Test
    public void testEncrypted() throws Exception {
        final Host host = new Host(new SDSProtocol(), "duck.ssp-europe.eu", new Credentials(
            System.getProperties().getProperty("sds.user"), System.getProperties().getProperty("sds.key")
        ));
        final SDSSession session = new SDSSession(host, new DisabledX509TrustManager(), new DefaultX509KeyManager());
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        final Path room = new Path("CD-TEST-ENCRYPTED", EnumSet.of(Path.Type.directory, Path.Type.volume, Path.Type.vault));
        final Path test = new SDSTouchFeature(session).touch(new Path(room, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        final String password = new AlphanumericRandomStringService().random();
        final DescriptiveUrl url = new SDSSharesUrlProvider(session).toDownloadUrl(test,
            new CreateDownloadShareRequest()
                .expiration(new ObjectExpiration().enableExpiration(false))
                .notifyCreator(false)
                .sendMail(false)
                .sendSms(false)
                .password(password)
                .mailRecipients(null)
                .mailSubject(null)
                .mailBody(null)
                .maxDownloads(null), new PasswordCallback() {
                @Override
                public Credentials prompt(final String title, final String reason, final LoginOptions options) throws LoginCanceledException {
                    return new VaultCredentials("ahbic3Ae");
                }
            });
        assertNotEquals(DescriptiveUrl.EMPTY, url);
        assertEquals(DescriptiveUrl.Type.signed, url.getType());
        assertTrue(url.getUrl().startsWith("https://duck.ssp-europe.eu/#/public/shares-downloads/"));
        new SDSDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
        session.close();
    }

    @Test(expected = AccessDeniedException.class)
    public void testEncryptedMissingPassword() throws Exception {
        final Host host = new Host(new SDSProtocol(), "duck.ssp-europe.eu", new Credentials(
            System.getProperties().getProperty("sds.user"), System.getProperties().getProperty("sds.key")
        ));
        final SDSSession session = new SDSSession(host, new DisabledX509TrustManager(), new DefaultX509KeyManager());
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        final Path room = new Path("CD-TEST-ENCRYPTED", EnumSet.of(Path.Type.directory, Path.Type.volume, Path.Type.vault));
        final Path test = new SDSTouchFeature(session).touch(new Path(room, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        try {
            final DescriptiveUrl url = new SDSSharesUrlProvider(session).toDownloadUrl(test,
                new CreateDownloadShareRequest()
                    .expiration(new ObjectExpiration().enableExpiration(false))
                    .notifyCreator(false)
                    .sendMail(false)
                    .sendSms(false)
                    .password(null)
                    .mailRecipients(null)
                    .mailSubject(null)
                    .mailBody(null)
                    .maxDownloads(null), new PasswordCallback() {
                    @Override
                    public Credentials prompt(final String title, final String reason, final LoginOptions options) throws LoginCanceledException {
                        return new VaultCredentials("ahbic3Ae");
                    }
                });
        }
        finally {
            new SDSDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
            session.close();
        }
    }
}
