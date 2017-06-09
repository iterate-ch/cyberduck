package ch.cyberduck.core.b2;

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
import ch.cyberduck.core.AttributedList;
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
import ch.cyberduck.core.SimplePathPredicate;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;
import ch.cyberduck.ui.browser.SearchFilter;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class B2SearchFeatureTest {

    @Test
    public void testSearchInBucket() throws Exception {
        final B2Session session = new B2Session(
                new Host(new B2Protocol(), new B2Protocol().getDefaultHostname(),
                        new Credentials(
                                System.getProperties().getProperty("b2.user"), System.getProperties().getProperty("b2.key")
                        )));
        final LoginConnectionService service = new LoginConnectionService(new DisabledLoginCallback(), new DisabledHostKeyCallback(),
                new DisabledPasswordStore(), new DisabledProgressListener());
        service.connect(session, PathCache.empty(), new DisabledCancelCallback());
        final String name = new AlphanumericRandomStringService().random();
        final Path bucket = new Path("test-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path file = new B2TouchFeature(session).touch(new Path(bucket, name, EnumSet.of(Path.Type.file)), new TransferStatus());
        final B2SearchFeature feature = new B2SearchFeature(session);
        assertNotNull(feature.search(bucket, new SearchFilter(name), new DisabledListProgressListener()).find(new SimplePathPredicate(file)));
        // Supports prefix matching only
        assertNull(feature.search(bucket, new SearchFilter(StringUtils.substring(name, 2)), new DisabledListProgressListener()).find(new SimplePathPredicate(file)));
        assertNotNull(feature.search(bucket, new SearchFilter(StringUtils.substring(name, 0, name.length() - 2)), new DisabledListProgressListener()).find(new SimplePathPredicate(file)));
        final Path subdir = new Path(bucket, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        assertNull(feature.search(subdir, new SearchFilter(name), new DisabledListProgressListener()).find(new SimplePathPredicate(file)));
        new B2DeleteFeature(session).delete(Collections.singletonList(file), new DisabledLoginCallback(), new Delete.DisabledCallback());
        session.close();
    }

    @Test
    public void testSearchInRoot() throws Exception {
        final B2Session session = new B2Session(
                new Host(new B2Protocol(), new B2Protocol().getDefaultHostname(),
                        new Credentials(
                                System.getProperties().getProperty("b2.user"), System.getProperties().getProperty("b2.key")
                        )));
        final LoginConnectionService service = new LoginConnectionService(new DisabledLoginCallback(), new DisabledHostKeyCallback(),
                new DisabledPasswordStore(), new DisabledProgressListener());
        service.connect(session, PathCache.empty(), new DisabledCancelCallback());
        final String name = new AlphanumericRandomStringService().random();
        final Path bucket = new Path("test-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path file = new B2TouchFeature(session).touch(new Path(bucket, name, EnumSet.of(Path.Type.file)), new TransferStatus());
        final B2SearchFeature feature = new B2SearchFeature(session);
        assertNotNull(feature.search(bucket, new SearchFilter(name), new DisabledListProgressListener()).find(new SimplePathPredicate(file)));
        // Supports prefix matching only
        assertNull(feature.search(new Path("/", EnumSet.of(Path.Type.directory, Path.Type.volume)), new SearchFilter(StringUtils.substring(name, 2)), new DisabledListProgressListener()).find(new SimplePathPredicate(file)));
        assertNotNull(feature.search(new Path("/", EnumSet.of(Path.Type.directory, Path.Type.volume)), new SearchFilter(StringUtils.substring(name, 0, name.length() - 2)), new DisabledListProgressListener()).find(new SimplePathPredicate(file)));
        new B2DeleteFeature(session).delete(Collections.singletonList(file), new DisabledLoginCallback(), new Delete.DisabledCallback());
        session.close();
    }

    @Test
    public void testSearchInDirectory() throws Exception {
        final B2Session session = new B2Session(
                new Host(new B2Protocol(), new B2Protocol().getDefaultHostname(),
                        new Credentials(
                                System.getProperties().getProperty("b2.user"), System.getProperties().getProperty("b2.key")
                        )));
        final LoginConnectionService service = new LoginConnectionService(new DisabledLoginCallback(), new DisabledHostKeyCallback(),
                new DisabledPasswordStore(), new DisabledProgressListener());
        service.connect(session, PathCache.empty(), new DisabledCancelCallback());
        final String name = new AlphanumericRandomStringService().random();
        final Path bucket = new Path("test-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path workdir = new B2DirectoryFeature(session).mkdir(new Path(bucket, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), null, new TransferStatus());
        final Path file = new B2TouchFeature(session).touch(new Path(workdir, name, EnumSet.of(Path.Type.file)), new TransferStatus());
        final B2SearchFeature feature = new B2SearchFeature(session);
        assertNotNull(feature.search(workdir, new SearchFilter(name), new DisabledListProgressListener()).find(new SimplePathPredicate(file)));
        // Supports prefix matching only
        assertNull(feature.search(workdir, new SearchFilter(StringUtils.substring(name, 2)), new DisabledListProgressListener()).find(new SimplePathPredicate(file)));
        {
            final AttributedList<Path> result = feature.search(workdir, new SearchFilter(StringUtils.substring(name, 0, name.length() - 2)), new DisabledListProgressListener());
            assertNotNull(result.find(new SimplePathPredicate(file)));
            assertEquals(workdir, result.get(result.indexOf(file)).getParent());
        }
        final Path subdir = new B2DirectoryFeature(session).mkdir(new Path(workdir, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), null, new TransferStatus());
        assertNull(feature.search(subdir, new SearchFilter(name), new DisabledListProgressListener()).find(new SimplePathPredicate(file)));
        final Path filesubdir = new B2TouchFeature(session).touch(new Path(subdir, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        {
            final AttributedList<Path> result = feature.search(workdir, new SearchFilter(filesubdir.getName()), new DisabledListProgressListener());
            assertNotNull(result.find(new SimplePathPredicate(filesubdir)));
            assertEquals(subdir, result.find(new SimplePathPredicate(filesubdir)).getParent());
        }
        new B2DeleteFeature(session).delete(Arrays.asList(file, filesubdir, subdir), new DisabledLoginCallback(), new Delete.DisabledCallback());
        session.close();
    }
}