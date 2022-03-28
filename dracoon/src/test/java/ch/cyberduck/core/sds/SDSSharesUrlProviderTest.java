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
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordCallback;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.sds.io.swagger.client.model.CreateDownloadShareRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.CreateUploadShareRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.ObjectExpiration;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.vault.VaultCredentials;
import ch.cyberduck.test.IntegrationTest;

import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class SDSSharesUrlProviderTest extends AbstractSDSTest {

    @Test
    public void testShareFile() throws Exception {
        final SDSNodeIdProvider nodeid = new SDSNodeIdProvider(session);
        final Path room = new SDSDirectoryFeature(session, nodeid).mkdir(new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), new TransferStatus());
        final Path test = new SDSTouchFeature(session, nodeid).touch(new Path(room, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        final DescriptiveUrl url = new SDSSharesUrlProvider(session, nodeid).toDownloadUrl(test,
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
        assertTrue(url.getUrl().startsWith("https://duck.dracoon.com/public/download-shares/"));
        new SDSDeleteFeature(session, nodeid).delete(Collections.singletonList(room), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testShareTopLevelRoom() throws Exception {
        final SDSNodeIdProvider nodeid = new SDSNodeIdProvider(session);
        final Path room = new SDSDirectoryFeature(session, nodeid).mkdir(new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), new TransferStatus());
        final DescriptiveUrl url = new SDSSharesUrlProvider(session, nodeid).toDownloadUrl(room,
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
        assertTrue(url.getUrl().startsWith("https://duck.dracoon.com/public/download-shares/"));
        new SDSDeleteFeature(session, nodeid).delete(Collections.singletonList(room), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testShareSubRoom() throws Exception {
        final SDSNodeIdProvider nodeid = new SDSNodeIdProvider(session);
        final Path room = new SDSDirectoryFeature(session, nodeid).mkdir(new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), new TransferStatus());
        final Path test = new SDSDirectoryFeature(session, nodeid).mkdir(new Path(room, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        final DescriptiveUrl url = new SDSSharesUrlProvider(session, nodeid).toDownloadUrl(test,
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
        assertTrue(url.getUrl().startsWith("https://duck.dracoon.com/public/download-shares/"));
        new SDSDeleteFeature(session, nodeid).delete(Collections.singletonList(room), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test(expected = InteroperabilityException.class)
    public void testToUrlMissingEmailRecipients() throws Exception {
        final SDSNodeIdProvider nodeid = new SDSNodeIdProvider(session);
        final Path room = new SDSDirectoryFeature(session, nodeid).mkdir(new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), new TransferStatus());
        final Path test = new SDSTouchFeature(session, nodeid).touch(new Path(room, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        try {
            final DescriptiveUrl url = new SDSSharesUrlProvider(session, nodeid).toDownloadUrl(test,
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
            new SDSDeleteFeature(session, nodeid).delete(Collections.singletonList(room), new DisabledLoginCallback(), new Delete.DisabledCallback());
        }
    }

    @Test(expected = InteroperabilityException.class)
    public void testToUrlInvalidSMSRecipients() throws Exception {
        final SDSNodeIdProvider nodeid = new SDSNodeIdProvider(session);
        final Path room = new SDSDirectoryFeature(session, nodeid).mkdir(new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), new TransferStatus());
        final Path test = new SDSTouchFeature(session, nodeid).touch(new Path(room, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        try {
            final DescriptiveUrl url = new SDSSharesUrlProvider(session, nodeid).toDownloadUrl(test,
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
            new SDSDeleteFeature(session, nodeid).delete(Collections.singletonList(room), new DisabledLoginCallback(), new Delete.DisabledCallback());
        }
    }

    @Test(expected = InteroperabilityException.class)
    public void testToUrlWeakPassword() throws Exception {
        final SDSNodeIdProvider nodeid = new SDSNodeIdProvider(session);
        final Path room = new SDSDirectoryFeature(session, nodeid).mkdir(new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), new TransferStatus());
        final Path test = new SDSTouchFeature(session, nodeid).touch(new Path(room, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        try {
            final DescriptiveUrl url = new SDSSharesUrlProvider(session, nodeid).toDownloadUrl(test,
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
            new SDSDeleteFeature(session, nodeid).delete(Collections.singletonList(room), new DisabledLoginCallback(), new Delete.DisabledCallback());
        }
    }

    @Test(expected = InteroperabilityException.class)
    public void testToUrlInvalidEmail() throws Exception {
        final SDSNodeIdProvider nodeid = new SDSNodeIdProvider(session);
        final Path room = new SDSDirectoryFeature(session, nodeid).mkdir(new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), new TransferStatus());
        final Path test = new SDSTouchFeature(session, nodeid).touch(new Path(room, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        try {
            final DescriptiveUrl url = new SDSSharesUrlProvider(session, nodeid).toDownloadUrl(test,
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
            new SDSDeleteFeature(session, nodeid).delete(Collections.singletonList(room), new DisabledLoginCallback(), new Delete.DisabledCallback());
        }
    }

    @Test
    public void testToUrlExpiry() throws Exception {
        final SDSNodeIdProvider nodeid = new SDSNodeIdProvider(session);
        final Path room = new SDSDirectoryFeature(session, nodeid).mkdir(new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), new TransferStatus());
        final Path test = new SDSTouchFeature(session, nodeid).touch(new Path(room, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        final DescriptiveUrl url = new SDSSharesUrlProvider(session, nodeid).toDownloadUrl(test,
            new CreateDownloadShareRequest()
                .expiration(new ObjectExpiration().enableExpiration(true).expireAt(new DateTime(1744300800000L)))
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
        assertTrue(url.getUrl().startsWith("https://duck.dracoon.com/public/download-shares/"));
        new SDSDeleteFeature(session, nodeid).delete(Collections.singletonList(room), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test(expected = InteroperabilityException.class)
    public void testToUrlExpiryInvalidDate() throws Exception {
        final SDSNodeIdProvider nodeid = new SDSNodeIdProvider(session);
        final Path room = new SDSDirectoryFeature(session, nodeid).mkdir(new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), new TransferStatus());
        final Path test = new SDSTouchFeature(session, nodeid).touch(new Path(room, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        final DescriptiveUrl url = new SDSSharesUrlProvider(session, nodeid).toDownloadUrl(test,
            new CreateDownloadShareRequest()
                .expiration(new ObjectExpiration().enableExpiration(true).expireAt(new DateTime(17443L)))
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
        assertTrue(url.getUrl().startsWith("https://duck.dracoon.com/public/download-shares/"));
        new SDSDeleteFeature(session, nodeid).delete(Collections.singletonList(room), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testEncrypted() throws Exception {
        final SDSNodeIdProvider nodeid = new SDSNodeIdProvider(session);
        final Path room = new SDSDirectoryFeature(session, nodeid).createRoom(
                new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), true);
        final Path test = new SDSTouchFeature(session, nodeid).touch(new Path(room, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        final DescriptiveUrl url = new SDSSharesUrlProvider(session, nodeid).toDownloadUrl(test,
                new CreateDownloadShareRequest()
                        .expiration(new ObjectExpiration().enableExpiration(false))
                        .notifyCreator(false)
                        .sendMail(false)
                        .sendSms(false)
                        .password(null)
                        .mailRecipients(null)
                        .mailSubject(null)
                .mailBody(null)
                .maxDownloads(null), new DisabledPasswordCallback() {
                @Override
                public Credentials prompt(final Host bookmark, final String title, final String reason, final LoginOptions options) {
                    return new VaultCredentials("eth[oh8uv4Eesij");
                }
            });
        assertNotEquals(DescriptiveUrl.EMPTY, url);
        assertEquals(DescriptiveUrl.Type.signed, url.getType());
        assertTrue(url.getUrl().startsWith("https://duck.dracoon.com/public/download-shares/"));
        new SDSDeleteFeature(session, nodeid).delete(Collections.singletonList(room), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test(expected = LoginCanceledException.class)
    public void testEncryptedMissingPassword() throws Exception {
        final SDSNodeIdProvider nodeid = new SDSNodeIdProvider(session);
        final Path room = new SDSDirectoryFeature(session, nodeid).createRoom(
                new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), true);
        final Path test = new SDSTouchFeature(session, nodeid).touch(new Path(room, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        try {
            final DescriptiveUrl url = new SDSSharesUrlProvider(session, nodeid).toDownloadUrl(test,
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
        }
        finally {
            new SDSDeleteFeature(session, nodeid).delete(Collections.singletonList(room), new DisabledLoginCallback(), new Delete.DisabledCallback());
        }
    }

    @Test
    public void testUploadAccountTopLevelRoom() throws Exception {
        final SDSNodeIdProvider nodeid = new SDSNodeIdProvider(session);
        final Path room = new SDSDirectoryFeature(session, nodeid).mkdir(new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), new TransferStatus());
        final DescriptiveUrl url = new SDSSharesUrlProvider(session, nodeid).toUploadUrl(room,
            new CreateUploadShareRequest()
                .name(new AlphanumericRandomStringService().random())
                .expiration(new ObjectExpiration().enableExpiration(false))
                .notifyCreator(false)
                .sendMail(false)
                .sendSms(false)
                .password(null)
                .mailRecipients(null)
                .mailSubject(null)
                .mailBody(null)
                .maxSize(null)
                .maxSlots(null)
                .notes(null)
                .filesExpiryPeriod(null), new DisabledPasswordCallback());
        assertNotEquals(DescriptiveUrl.EMPTY, url);
        assertEquals(DescriptiveUrl.Type.signed, url.getType());
        assertTrue(url.getUrl().startsWith("https://duck.dracoon.com/public/upload-shares/"));
        new SDSDeleteFeature(session, nodeid).delete(Collections.singletonList(room), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testUploadAccountEncrypted() throws Exception {
        final SDSNodeIdProvider nodeid = new SDSNodeIdProvider(session);
        final Path room = new SDSDirectoryFeature(session, nodeid).createRoom(
                new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), true);
        final Path folder = new SDSDirectoryFeature(session, nodeid).mkdir(new Path(room, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        final DescriptiveUrl url = new SDSSharesUrlProvider(session, nodeid).toUploadUrl(folder,
                new CreateUploadShareRequest()
                        .name(new AlphanumericRandomStringService().random())
                        .expiration(new ObjectExpiration().enableExpiration(false))
                        .notifyCreator(false)
                        .sendMail(false)
                        .sendSms(false)
                        .password(null)
                        .mailRecipients(null)
                .mailSubject(null)
                .mailBody(null)
                .maxSize(null)
                .maxSlots(null)
                .notes(null)
                .filesExpiryPeriod(null), new DisabledPasswordCallback());
        assertNotEquals(DescriptiveUrl.EMPTY, url);
        assertEquals(DescriptiveUrl.Type.signed, url.getType());
        assertTrue(url.getUrl().startsWith("https://duck.dracoon.com/public/upload-shares/"));
        new SDSDeleteFeature(session, nodeid).delete(Collections.singletonList(room), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testUploadAccountSubRoom() throws Exception {
        final SDSNodeIdProvider nodeid = new SDSNodeIdProvider(session);
        final Path room = new SDSDirectoryFeature(session, nodeid).mkdir(new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), new TransferStatus());
        final Path test = new SDSDirectoryFeature(session, nodeid).mkdir(new Path(room, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        final DescriptiveUrl url = new SDSSharesUrlProvider(session, nodeid).toUploadUrl(test,
            new CreateUploadShareRequest()
                .name(new AlphanumericRandomStringService().random())
                .expiration(new ObjectExpiration().enableExpiration(false))
                .notifyCreator(false)
                .sendMail(false)
                .sendSms(false)
                .password(null)
                .mailRecipients(null)
                .mailSubject(null)
                .mailBody(null)
                .maxSize(null)
                .maxSlots(null)
                .notes(null)
                .filesExpiryPeriod(null), new DisabledPasswordCallback());
        assertNotEquals(DescriptiveUrl.EMPTY, url);
        assertEquals(DescriptiveUrl.Type.signed, url.getType());
        assertTrue(url.getUrl().startsWith("https://duck.dracoon.com/public/upload-shares/"));
        new SDSDeleteFeature(session, nodeid).delete(Collections.singletonList(room), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

}
