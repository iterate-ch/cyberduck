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

import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.AsciiRandomStringService;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.DisabledProgressListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LoginConnectionService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.TranscriptListener;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.shared.DefaultFindFeature;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@Category(IntegrationTest.class)
public class S3DirectoryFeatureTest extends AbstractS3Test {

    @Test
    public void testCreateBucket() throws Exception {
        final S3DirectoryFeature feature = new S3DirectoryFeature(session, new S3WriteFeature(session));
        final Path test = new Path(new S3HomeFinderService(session).find(), new AsciiRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume));
        assertTrue(feature.isSupported(test.getParent(), test.getName()));
        final S3LocationFeature.S3Region region = new S3LocationFeature.S3Region("eu-west-2");
        test.attributes().setRegion(region.getIdentifier());
        feature.mkdir(test, region.getIdentifier(), new TransferStatus());
        assertTrue(new S3FindFeature(session).find(test));
        new S3DefaultDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test(expected = InteroperabilityException.class)
    public void testCreateBucketInvalidName() throws Exception {
        final S3DirectoryFeature feature = new S3DirectoryFeature(session, new S3WriteFeature(session));
        final Path test = new Path(new S3HomeFinderService(session).find(), "untitled folder", EnumSet.of(Path.Type.directory, Path.Type.volume));
        assertFalse(feature.isSupported(test.getParent(), test.getName()));
        final S3LocationFeature.S3Region region = new S3LocationFeature.S3Region("eu-west-2");
        test.attributes().setRegion(region.getIdentifier());
        feature.mkdir(test, region.getIdentifier(), new TransferStatus());
        assertTrue(new S3FindFeature(session).find(test));
        new S3DefaultDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testCreatePlaceholder() throws Exception {
        final AtomicBoolean b = new AtomicBoolean();
        final String name = new AlphanumericRandomStringService().random();
        session.withListener(new TranscriptListener() {
            @Override
            public void log(final Type request, final String message) {
                switch(request) {
                    case request:
                        if(("PUT /" + name + "/ HTTP/1.1").equals(message)) {
                            b.set(true);
                        }
                }
            }
        });
        final Path container = new Path("test-us-east-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new S3DirectoryFeature(session, new S3WriteFeature(session)).mkdir(new Path(container, name, EnumSet.of(Path.Type.directory)), null, new TransferStatus());
        assertTrue(test.getType().contains(Path.Type.placeholder));
        assertTrue(b.get());
        assertTrue(new S3FindFeature(session).find(test));
        assertTrue(new S3ObjectListService(session).list(container, new DisabledListProgressListener()).contains(test));
        assertTrue(new S3ObjectListService(session).list(test, new DisabledListProgressListener()).isEmpty());
        assertTrue(new S3VersionedObjectListService(session).list(container, new DisabledListProgressListener()).contains(test));
        assertTrue(new S3VersionedObjectListService(session).list(test, new DisabledListProgressListener()).isEmpty());
        assertTrue(new DefaultFindFeature(session).find(test));
        new S3DefaultDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    @Ignore
    public void testCreatePlaceholderMinio() throws Exception {
        final Host host = new Host(new S3Protocol(), "play.minio.io", 9000, new Credentials(
            "Q3AM3UQ867SPQQA43P2F", "zuf+tfteSlswRu7BJ86wekitnifILbZam1KYY3TG"
        ));
        final S3Session session = new S3Session(host);
        final LoginConnectionService login = new LoginConnectionService(new DisabledLoginCallback(), new DisabledHostKeyCallback(),
            new DisabledPasswordStore(), new DisabledProgressListener());
        login.check(session, PathCache.empty(), new DisabledCancelCallback());
        final String name = new AlphanumericRandomStringService().random();
        final Path bucket = new S3DirectoryFeature(session, new S3WriteFeature(session)).mkdir(
            new Path(new S3HomeFinderService(session).find(), new AsciiRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), null, new TransferStatus());
        final Path test = new S3DirectoryFeature(session, new S3WriteFeature(session)).mkdir(new Path(bucket, name, EnumSet.of(Path.Type.directory)), null, new TransferStatus());
        assertTrue(test.getType().contains(Path.Type.placeholder));
        assertTrue(new S3FindFeature(session).find(test));
        assertTrue(new S3ObjectListService(session).list(bucket, new DisabledListProgressListener()).contains(test));
        assertTrue(new S3ObjectListService(session).list(test, new DisabledListProgressListener()).isEmpty());
        assertTrue(new DefaultFindFeature(session).find(test));
        new S3DefaultDeleteFeature(session).delete(Arrays.asList(test, bucket), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testCreatePlaceholderVersioning() throws Exception {
        final Path bucket = new Path("versioning-test-us-east-1-cyberduck", EnumSet.of(Path.Type.volume));
        final Path test = new S3DirectoryFeature(session, new S3WriteFeature(session)).mkdir(new Path(bucket, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), null, new TransferStatus());
        assertTrue(test.getType().contains(Path.Type.placeholder));
        assertTrue(new S3FindFeature(session).find(test));
        assertTrue(new S3ObjectListService(session).list(bucket, new DisabledListProgressListener()).contains(test));
        new S3DefaultDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
        assertFalse(new S3ObjectListService(session).list(bucket, new DisabledListProgressListener()).contains(test));
    }

    @Test
    public void testCreatePlaceholderEqualSign() throws Exception {
        final String name = String.format("%s=", new AlphanumericRandomStringService().random());
        final Path container = new Path("test-us-east-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new S3DirectoryFeature(session, new S3WriteFeature(session)).mkdir(new Path(container, name, EnumSet.of(Path.Type.directory)), null, new TransferStatus());
        assertTrue(new S3FindFeature(session).find(test));
        assertTrue(new DefaultFindFeature(session).find(test));
        assertTrue(new S3ObjectListService(session).list(test, new DisabledListProgressListener()).isEmpty());
        new S3DefaultDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
