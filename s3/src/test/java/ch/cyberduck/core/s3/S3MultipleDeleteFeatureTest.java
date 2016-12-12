package ch.cyberduck.core.s3;

/*
 * Copyright (c) 2002-2013 David Kocher. All rights reserved.
 * http://cyberduck.ch/
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
 *
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.ch
 */

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.shared.DefaultFindFeature;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.jets3t.service.model.container.ObjectKeyAndVersion;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@Category(IntegrationTest.class)
public class S3MultipleDeleteFeatureTest {

    @Test
    public void testDeleteFile() throws Exception {
        final S3Session session = new S3Session(
                new Host(new S3Protocol(), new S3Protocol().getDefaultHostname(),
                        new Credentials(
                                System.getProperties().getProperty("s3.key"), System.getProperties().getProperty("s3.secret")
                        )));
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final Path container = new Path("test-us-east-1-cyberduck", EnumSet.of(Path.Type.volume));
        final Path test = new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        new S3TouchFeature(session).touch(test, new TransferStatus());
        assertTrue(new S3FindFeature(session).find(test));
        new S3MultipleDeleteFeature(session).delete(Arrays.asList(test, test), new DisabledLoginCallback(), new Delete.DisabledCallback());
        assertFalse(new S3FindFeature(session).find(test));
        session.close();
    }

    @Test
    public void testDeletePlaceholder() throws Exception {
        final S3Session session = new S3Session(
                new Host(new S3Protocol(), new S3Protocol().getDefaultHostname(),
                        new Credentials(
                                System.getProperties().getProperty("s3.key"), System.getProperties().getProperty("s3.secret")
                        )));
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final Path container = new Path("test-us-east-1-cyberduck", EnumSet.of(Path.Type.volume));
        final Path test = new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.directory));
        new S3DirectoryFeature(session).mkdir(test);
        test.setType(EnumSet.of(Path.Type.directory, Path.Type.placeholder));
        assertTrue(new S3FindFeature(session).find(test));
        assertTrue(new DefaultFindFeature(session).find(test));
        new S3MultipleDeleteFeature(session).delete(Arrays.asList(test, test), new DisabledLoginCallback(), new Delete.DisabledCallback());
        assertFalse(new S3FindFeature(session).find(test));
        session.close();
    }

    @Test
    public void testDeleteContainer() throws Exception {
        final S3Session session = new S3Session(
                new Host(new S3Protocol(), new S3Protocol().getDefaultHostname(),
                        new Credentials(
                                System.getProperties().getProperty("s3.key"), System.getProperties().getProperty("s3.secret")
                        )));
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final Path container = new Path(UUID.randomUUID().toString(), EnumSet.of(Path.Type.volume, Path.Type.directory));
        new S3DirectoryFeature(session).mkdir(container);
        assertTrue(new S3FindFeature(session).find(container));
        new S3MultipleDeleteFeature(session).delete(Arrays.asList(container,
                new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file))), new DisabledLoginCallback(), new Delete.DisabledCallback());
        assertFalse(new S3FindFeature(session).find(container));
        session.close();
    }

    @Test
    public void testDeleteNotFoundKey() throws Exception {
        final S3Session session = new S3Session(
                new Host(new S3Protocol(), new S3Protocol().getDefaultHostname(),
                        new Credentials(
                                System.getProperties().getProperty("s3.key"), System.getProperties().getProperty("s3.secret")
                        )));
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final Path container = new Path("test-us-east-1-cyberduck", EnumSet.of(Path.Type.volume));
        final List<ObjectKeyAndVersion> keys = new ArrayList<ObjectKeyAndVersion>();
        for(int i = 0; i < 1010; i++) {
            keys.add(new ObjectKeyAndVersion(UUID.randomUUID().toString()));
        }
        new S3MultipleDeleteFeature(session).delete(container, keys, new DisabledLoginCallback());
        session.close();
    }

    @Test(expected = NotfoundException.class)
    public void testDeleteNotFoundBucket() throws Exception {
        final S3Session session = new S3Session(
                new Host(new S3Protocol(), new S3Protocol().getDefaultHostname(),
                        new Credentials(
                                System.getProperties().getProperty("s3.key"), System.getProperties().getProperty("s3.secret")
                        )));
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final Path container = new Path(UUID.randomUUID().toString(), EnumSet.of(Path.Type.volume));
        new S3MultipleDeleteFeature(session).delete(Arrays.asList(container, container), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
