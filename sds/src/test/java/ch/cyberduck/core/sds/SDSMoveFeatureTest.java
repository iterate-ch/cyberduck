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
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.io.StatusOutputStream;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.sds.io.swagger.client.api.NodesApi;
import ch.cyberduck.core.sds.io.swagger.client.model.EncryptRoomRequest;
import ch.cyberduck.core.sds.triplecrypt.CryptoReadFeature;
import ch.cyberduck.core.sds.triplecrypt.CryptoWriteFeature;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DisabledX509TrustManager;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.vault.VaultCredentials;
import ch.cyberduck.core.worker.MoveWorker;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class SDSMoveFeatureTest {

    @Test
    public void testMove() throws Exception {
        final Host host = new Host(new SDSProtocol(), "duck.ssp-europe.eu", new Credentials(
            System.getProperties().getProperty("sds.user"), System.getProperties().getProperty("sds.key")
        ));
        final SDSSession session = new SDSSession(host, new DisabledX509TrustManager(), new DefaultX509KeyManager());
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        final Path room = new SDSDirectoryFeature(session).mkdir(new Path(
            new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), null, new TransferStatus());
        final Path test = new Path(room, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new SDSTouchFeature(session).touch(test, new TransferStatus());
        final Path target = new Path(room, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new SDSMoveFeature(session).move(test, target, new TransferStatus(), new Delete.DisabledCallback(), new DisabledConnectionCallback());
        assertFalse(new SDSFindFeature(session).find(test));
        assertTrue(new SDSFindFeature(session).find(target));
        new SDSDeleteFeature(session).delete(Collections.singletonList(room), new DisabledLoginCallback(), new Delete.DisabledCallback());
        session.close();
    }

    @Test
    public void testMoveDifferentDataRoom() throws Exception {
        final Host host = new Host(new SDSProtocol(), "duck.ssp-europe.eu", new Credentials(
            System.getProperties().getProperty("sds.user"), System.getProperties().getProperty("sds.key")
        ));
        final SDSSession session = new SDSSession(host, new DisabledX509TrustManager(), new DefaultX509KeyManager());
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        final Path room1 = new SDSDirectoryFeature(session).mkdir(new Path(
            new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), null, new TransferStatus());
        final Path room2 = new SDSDirectoryFeature(session).mkdir(new Path(
            new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), null, new TransferStatus());
        final Path test = new Path(room1, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new SDSTouchFeature(session).touch(test, new TransferStatus());
        final Path target = new Path(room2, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new SDSMoveFeature(session).move(test, target, new TransferStatus(), new Delete.DisabledCallback(), new DisabledConnectionCallback());
        assertFalse(new SDSFindFeature(session).find(test));
        assertTrue(new SDSFindFeature(session).find(target));
        new SDSDeleteFeature(session).delete(Arrays.asList(room1, room2), new DisabledLoginCallback(), new Delete.DisabledCallback());
        session.close();
    }

    @Test
    public void testMoveFromEncryptedDataRoom() throws Exception {
        final Host host = new Host(new SDSProtocol(), "duck.ssp-europe.eu", new Credentials(
            System.getProperties().getProperty("sds.user"), System.getProperties().getProperty("sds.key")
        ));
        final SDSSession session = new SDSSession(host, new DisabledX509TrustManager(), new DefaultX509KeyManager());
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        final Path room1 = new Path("CD-TEST-ENCRYPTED", EnumSet.of(Path.Type.directory, Path.Type.volume, Path.Type.vault));
        room1.attributes().getAcl().addAll(new Acl.EmailUser(System.getProperties().getProperty("sds.user")), SDSAttributesFinderFeature.DELETE_ROLE);
        final Path room2 = new SDSDirectoryFeature(session).mkdir(new Path(
            new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), null, new TransferStatus());
        final byte[] content = RandomUtils.nextBytes(32769);
        final TransferStatus status = new TransferStatus();
        status.setLength(content.length);
        final Path test = new Path(room1, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file, Path.Type.decrypted));
        final SDSEncryptionBulkFeature bulk = new SDSEncryptionBulkFeature(session);
        bulk.pre(Transfer.Type.upload, Collections.singletonMap(test, status), new DisabledConnectionCallback());
        final CryptoWriteFeature writer = new CryptoWriteFeature(session, new SDSWriteFeature(session));
        final StatusOutputStream<VersionId> out = writer.write(test, status, new DisabledConnectionCallback());
        assertNotNull(out);
        new StreamCopier(status, status).transfer(new ByteArrayInputStream(content), out);
        final Path target = new Path(room2, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new SDSDelegatingMoveFeature(session, new SDSMoveFeature(session)).move(test, target, new TransferStatus().length(content.length), new Delete.DisabledCallback(), new ConnectionCallback() {
            @Override
            public void warn(final Host bookmark, final String title, final String message, final String defaultButton, final String cancelButton, final String preference) throws ConnectionCanceledException {
                //
            }

            @Override
            public Credentials prompt(final String title, final String reason, final LoginOptions options) throws LoginCanceledException {
                return new VaultCredentials("ahbic3Ae");
            }
        });
        assertFalse(new SDSFindFeature(session).find(test));
        assertTrue(new SDSFindFeature(session).find(target));
        final byte[] compare = new byte[content.length];
        final InputStream stream = new SDSReadFeature(session).read(target, new TransferStatus().length(content.length), new ConnectionCallback() {
            @Override
            public void warn(final Host bookmark, final String title, final String message, final String defaultButton, final String cancelButton, final String preference) throws ConnectionCanceledException {
                //
            }

            @Override
            public Credentials prompt(final String title, final String reason, final LoginOptions options) throws LoginCanceledException {
                return new VaultCredentials("ahbic3Ae");
            }
        });
        IOUtils.readFully(stream, compare);
        stream.close();
        assertArrayEquals(content, compare);
        new SDSDeleteFeature(session).delete(Collections.singletonList(room2), new DisabledLoginCallback(), new Delete.DisabledCallback());
        session.close();
    }

    @Test
    public void testMoveToEncryptedDataRoom() throws Exception {
        final Host host = new Host(new SDSProtocol(), "duck.ssp-europe.eu", new Credentials(
            System.getProperties().getProperty("sds.user"), System.getProperties().getProperty("sds.key")
        ));
        final SDSSession session = new SDSSession(host, new DisabledX509TrustManager(), new DefaultX509KeyManager());
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        final Path room1 = new Path("CD-TEST-ENCRYPTED", EnumSet.of(Path.Type.directory, Path.Type.volume, Path.Type.vault));
        room1.attributes().getAcl().addAll(new Acl.EmailUser(System.getProperties().getProperty("sds.user")), SDSAttributesFinderFeature.DELETE_ROLE);
        final Path room2 = new SDSDirectoryFeature(session).mkdir(new Path(
            new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), null, new TransferStatus());
        final byte[] content = RandomUtils.nextBytes(32769);
        final TransferStatus status = new TransferStatus();
        status.setLength(content.length);
        final Path test = new Path(room2, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file, Path.Type.decrypted));
        final SDSWriteFeature writer = new SDSWriteFeature(session);
        final StatusOutputStream<VersionId> out = writer.write(test, status, new DisabledConnectionCallback());
        assertNotNull(out);
        new StreamCopier(status, status).transfer(new ByteArrayInputStream(content), out);
        final Path target = new Path(room1, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new SDSDelegatingMoveFeature(session, new SDSMoveFeature(session)).move(test, target, new TransferStatus().length(content.length), new Delete.DisabledCallback(), new DisabledConnectionCallback());
        assertFalse(new SDSFindFeature(session).find(test));
        assertTrue(new SDSFindFeature(session).find(target));
        final byte[] compare = new byte[content.length];
        final InputStream stream = new CryptoReadFeature(session, new SDSReadFeature(session)).read(target, new TransferStatus().length(content.length), new ConnectionCallback() {
            @Override
            public void warn(final Host bookmark, final String title, final String message, final String defaultButton, final String cancelButton, final String preference) throws ConnectionCanceledException {
                //
            }

            @Override
            public Credentials prompt(final String title, final String reason, final LoginOptions options) throws LoginCanceledException {
                return new VaultCredentials("ahbic3Ae");
            }
        });
        IOUtils.readFully(stream, compare);
        stream.close();
        assertArrayEquals(content, compare);
        new SDSDeleteFeature(session).delete(Collections.singletonList(room2), new DisabledLoginCallback(), new Delete.DisabledCallback());
        session.close();
    }

    @Test
    public void testMoveDirectory() throws Exception {
        final Host host = new Host(new SDSProtocol(), "duck.ssp-europe.eu", new Credentials(
            System.getProperties().getProperty("sds.user"), System.getProperties().getProperty("sds.key")
        ));
        final SDSSession session = new SDSSession(host, new DisabledX509TrustManager(), new DefaultX509KeyManager());
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        final Path room = new SDSDirectoryFeature(session).mkdir(new Path(
            new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), null, new TransferStatus());
        final Path test = new Path(room, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        new SDSDirectoryFeature(session).mkdir(test, null, new TransferStatus());
        final Path target = new Path(room, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        new SDSMoveFeature(session).move(test, target, new TransferStatus(), new Delete.DisabledCallback(), new DisabledConnectionCallback());
        assertFalse(new SDSFindFeature(session).find(test));
        assertTrue(new SDSFindFeature(session).find(target));
        new SDSDeleteFeature(session).delete(Collections.singletonList(room), new DisabledLoginCallback(), new Delete.DisabledCallback());
        session.close();
    }

    @Test
    public void testMoveDataRoom() throws Exception {
        final Host host = new Host(new SDSProtocol(), "duck.ssp-europe.eu", new Credentials(
            System.getProperties().getProperty("sds.user"), System.getProperties().getProperty("sds.key")
        ));
        final SDSSession session = new SDSSession(host, new DisabledX509TrustManager(), new DefaultX509KeyManager());
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        final Path test = new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume));
        new SDSDirectoryFeature(session).mkdir(test, null, new TransferStatus());
        final Path target = new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume));
        new SDSMoveFeature(session).move(test, target, new TransferStatus(), new Delete.DisabledCallback(), new DisabledConnectionCallback());
        assertFalse(new SDSFindFeature(session).find(test));
        assertTrue(new SDSFindFeature(session).find(target));
        new SDSDeleteFeature(session).delete(Collections.singletonList(target), new DisabledLoginCallback(), new Delete.DisabledCallback());
        session.close();
    }

    @Test
    public void testMoveEncryptedDataRoom() throws Exception {
        final Host host = new Host(new SDSProtocol(), "duck.ssp-europe.eu", new Credentials(
            System.getProperties().getProperty("sds.user"), System.getProperties().getProperty("sds.key")
        ));
        final SDSSession session = new SDSSession(host, new DisabledX509TrustManager(), new DefaultX509KeyManager());
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        final Path room = new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume));
        new SDSDirectoryFeature(session).mkdir(room, null, new TransferStatus());
        final EncryptRoomRequest encrypt = new EncryptRoomRequest();
        encrypt.setIsEncrypted(true);
        new NodesApi(session.getClient()).encryptRoom(StringUtils.EMPTY, Long.parseLong(new SDSNodeIdProvider(session).getFileid(room,
            new DisabledListProgressListener())), encrypt, null);
        final AttributedList<Path> list = new SDSListService(session).list(room.getParent(), new DisabledListProgressListener());
        final Path encrypted = list.get(room);
        // create file inside encrypted room
        final Path folder = new Path(encrypted, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.directory));
        new SDSDirectoryFeature(session).mkdir(folder, null, new TransferStatus());
        final Path renamed = new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume, Path.Type.vault));
        final MoveWorker worker = new MoveWorker(Collections.singletonMap(encrypted, renamed), new DisabledProgressListener(), PathCache.empty(), new DisabledConnectionCallback());
        worker.run(session);
        assertFalse(new SDSFindFeature(session).find(room));
        assertTrue(new SDSFindFeature(session).find(renamed));
        assertTrue(new SDSFindFeature(session).find(new Path(renamed, folder.getName(), EnumSet.of(Path.Type.directory, Path.Type.directory))));
        assertTrue(renamed.getType().contains(Path.Type.vault));
        new SDSDeleteFeature(session).delete(Collections.singletonList(renamed), new DisabledLoginCallback(), new Delete.DisabledCallback());
        session.close();
    }

    @Test
    public void testMoveOverride() throws Exception {
        final Host host = new Host(new SDSProtocol(), "duck.ssp-europe.eu", new Credentials(
            System.getProperties().getProperty("sds.user"), System.getProperties().getProperty("sds.key")
        ));
        final SDSSession session = new SDSSession(host, new DisabledX509TrustManager(), new DefaultX509KeyManager());
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        final Path room = new SDSDirectoryFeature(session).mkdir(new Path(
            new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), null, new TransferStatus());
        final Path test = new Path(room, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new SDSTouchFeature(session).touch(test, new TransferStatus());
        final Path target = new Path(room, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new SDSTouchFeature(session).touch(target, new TransferStatus());
        new SDSMoveFeature(session).move(test, target, new TransferStatus().exists(true), new Delete.DisabledCallback(), new DisabledConnectionCallback());
        assertFalse(new SDSFindFeature(session).find(test));
        assertTrue(new SDSFindFeature(session).find(target));
        new SDSDeleteFeature(session).delete(Collections.singletonList(room), new DisabledLoginCallback(), new Delete.DisabledCallback());
        session.close();
    }

    @Test(expected = NotfoundException.class)
    public void testMoveNotFound() throws Exception {
        final Host host = new Host(new SDSProtocol(), "duck.ssp-europe.eu", new Credentials(
            System.getProperties().getProperty("sds.user"), System.getProperties().getProperty("sds.key")
        ));
        final SDSSession session = new SDSSession(host, new DisabledX509TrustManager(), new DefaultX509KeyManager());
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        final Path room = new Path(
            new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new Path(room, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new SDSMoveFeature(session).move(test, new Path(room, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus(), new Delete.DisabledCallback(), new DisabledConnectionCallback());
        session.close();
    }
}
