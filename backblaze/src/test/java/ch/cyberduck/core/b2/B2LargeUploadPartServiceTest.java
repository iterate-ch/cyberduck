package ch.cyberduck.core.b2;

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
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

import synapticloop.b2.response.B2FileInfoResponse;
import synapticloop.b2.response.B2StartLargeFileResponse;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class B2LargeUploadPartServiceTest {

    @Test
    public void testFind() throws Exception {
        final B2Session session = new B2Session(
                new Host(new B2Protocol(), new B2Protocol().getDefaultHostname(),
                        new Credentials(
                                System.getProperties().getProperty("b2.user"), System.getProperties().getProperty("b2.key")
                        )));
        final Path bucket = new Path("test-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final Path file = new Path(bucket, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        final B2StartLargeFileResponse startResponse = session.getClient().startLargeFileUpload(
                new B2FileidProvider(session).getFileid(bucket),
                file.getName(), null, Collections.emptyMap());
        assertEquals(1, new B2LargeUploadPartService(session).find(file).size());
        session.getClient().cancelLargeFileUpload(startResponse.getFileId());
        session.close();
    }

    @Test
    public void testFindAllPendingInBucket() throws Exception {
        final B2Session session = new B2Session(
                new Host(new B2Protocol(), new B2Protocol().getDefaultHostname(),
                        new Credentials(
                                System.getProperties().getProperty("b2.user"), System.getProperties().getProperty("b2.key")
                        )));
        final Path bucket = new Path("test-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final Path file = new Path(bucket, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        final B2StartLargeFileResponse startResponse = session.getClient().startLargeFileUpload(
                new B2FileidProvider(session).getFileid(bucket),
                file.getName(), null, Collections.emptyMap());
        final List<B2FileInfoResponse> list = new B2LargeUploadPartService(session).find(bucket);
        assertFalse(list.isEmpty());
        session.getClient().cancelLargeFileUpload(startResponse.getFileId());
        session.close();
    }

    @Test
    public void testList() throws Exception {
        final B2Session session = new B2Session(
                new Host(new B2Protocol(), new B2Protocol().getDefaultHostname(),
                        new Credentials(
                                System.getProperties().getProperty("b2.user"), System.getProperties().getProperty("b2.key")
                        )));
        final Path bucket = new Path("test-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final Path file = new Path(bucket, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        final B2StartLargeFileResponse startResponse = session.getClient().startLargeFileUpload(
                new B2FileidProvider(session).getFileid(bucket),
                file.getName(), null, Collections.emptyMap());
        assertTrue(new B2LargeUploadPartService(session).list(startResponse.getFileId()).isEmpty());
        session.getClient().cancelLargeFileUpload(startResponse.getFileId());
        session.close();
    }

    @Test
    public void testDelete() throws Exception {
        final B2Session session = new B2Session(
                new Host(new B2Protocol(), new B2Protocol().getDefaultHostname(),
                        new Credentials(
                                System.getProperties().getProperty("b2.user"), System.getProperties().getProperty("b2.key")
                        )));
        final Path bucket = new Path("test-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final Path file = new Path(bucket, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        final B2StartLargeFileResponse startResponse = session.getClient().startLargeFileUpload(
                new B2FileidProvider(session).getFileid(bucket),
                file.getName(), null, Collections.emptyMap());
        final String fileid = startResponse.getFileId();
        new B2LargeUploadPartService(session).delete(startResponse.getFileId());
        session.close();
    }
}