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

import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.proxy.Proxy;
import ch.cyberduck.core.shared.DefaultFindFeature;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class B2DirectoryFeatureTest {

    @Test
    public void testCreateBucket() throws Exception {
        final B2Session session = new B2Session(
            new Host(new B2Protocol(), new B2Protocol().getDefaultHostname(),
                new Credentials(
                    System.getProperties().getProperty("b2.user"), System.getProperties().getProperty("b2.key")
                )));
        session.open(Proxy.DIRECT, new DisabledHostKeyCallback(), new DisabledLoginCallback());
        session.login(Proxy.DIRECT, new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        final Path bucket = new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume));
        final B2DirectoryFeature feature = new B2DirectoryFeature(session);
        assertTrue(feature.isSupported(bucket.getParent(), bucket.getName()));
        feature.mkdir(bucket, null, new TransferStatus());
        new B2DeleteFeature(session).delete(Collections.singletonList(bucket), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test(expected = InteroperabilityException.class)
    public void testBucketExists() throws Exception {
        final B2Session session = new B2Session(
            new Host(new B2Protocol(), new B2Protocol().getDefaultHostname(),
                new Credentials(
                    System.getProperties().getProperty("b2.user"), System.getProperties().getProperty("b2.key")
                )));
        session.open(Proxy.DIRECT, new DisabledHostKeyCallback(), new DisabledLoginCallback());
        session.login(Proxy.DIRECT, new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        final Path bucket = new Path("/test-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        try {
            new B2DirectoryFeature(session).mkdir(bucket, null, new TransferStatus());
        }
        catch(InteroperabilityException e) {
            assertEquals("Bucket name is already in use. Please contact your web hosting service provider for assistance.", e.getDetail());
            assertEquals("Cannot create folder test-cyberduck.", e.getMessage());
            throw e;
        }
    }

    @Test(expected = InteroperabilityException.class)
    public void testBucketInvalidCharacter() throws Exception {
        final B2Session session = new B2Session(
            new Host(new B2Protocol(), new B2Protocol().getDefaultHostname(),
                new Credentials(
                    System.getProperties().getProperty("b2.user"), System.getProperties().getProperty("b2.key")
                )));
        session.open(Proxy.DIRECT, new DisabledHostKeyCallback(), new DisabledLoginCallback());
        session.login(Proxy.DIRECT, new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        final Path bucket = new Path("untitled folder", EnumSet.of(Path.Type.directory, Path.Type.volume));
        assertFalse(new B2DirectoryFeature(session).isSupported(bucket.getParent(), bucket.getName()));
        try {
            new B2DirectoryFeature(session).mkdir(bucket, null, new TransferStatus());
        }
        catch(InteroperabilityException e) {
            assertEquals("Invalid characters in bucketName: must be alphanumeric or '-'. Please contact your web hosting service provider for assistance.", e.getDetail());
            assertEquals("Cannot create folder untitled folder.", e.getMessage());
            throw e;
        }
    }

    @Test
    public void testCreatePlaceholder() throws Exception {
        final B2Session session = new B2Session(
            new Host(new B2Protocol(), new B2Protocol().getDefaultHostname(),
                new Credentials(
                    System.getProperties().getProperty("b2.user"), System.getProperties().getProperty("b2.key")
                )));
        session.open(Proxy.DIRECT, new DisabledHostKeyCallback(), new DisabledLoginCallback());
        session.login(Proxy.DIRECT, new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        final Path bucket = new Path("/test-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new B2DirectoryFeature(session, new B2WriteFeature(session)).mkdir(new Path(bucket, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), null, new TransferStatus());
        assertTrue(test.getType().contains(Path.Type.placeholder));
        assertTrue(new B2FindFeature(session).find(test));
        assertTrue(new DefaultFindFeature(session).find(test));
        new B2DeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
        session.close();
    }

    @Test
    public void testModificationDate() throws Exception {
        final B2Session session = new B2Session(
            new Host(new B2Protocol(), new B2Protocol().getDefaultHostname(),
                new Credentials(
                    System.getProperties().getProperty("b2.user"), System.getProperties().getProperty("b2.key")
                )));
        session.open(Proxy.DIRECT, new DisabledHostKeyCallback(), new DisabledLoginCallback());
        session.login(Proxy.DIRECT, new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        final Path bucket = new Path("/test-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final TransferStatus status = new TransferStatus();
        final long timestamp = 1509959502930L;
        status.setTimestamp(timestamp);
        final Path directory = new B2DirectoryFeature(session, new B2WriteFeature(session)).mkdir(new Path(bucket, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), null, status);
        final Path test = new B2DirectoryFeature(session, new B2WriteFeature(session)).mkdir(new Path(directory, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), null, status);
        assertEquals(timestamp, new B2AttributesFinderFeature(session).find(test).getModificationDate());
        // Timestamp for placeholder is unknown. Only set on /.bzEmpty
        assertEquals(timestamp, new B2ObjectListService(session).list(directory, new DisabledListProgressListener()).get(test).attributes().getModificationDate());
        new B2DeleteFeature(session).delete(Arrays.asList(test, directory), new DisabledLoginCallback(), new Delete.DisabledCallback());
        session.close();
    }
}
