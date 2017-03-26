package ch.cyberduck.core.s3;

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
import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Touch;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.ui.browser.SearchFilter;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.util.Arrays;
import java.util.EnumSet;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class S3SearchFeatureTest {

    @Test
    public void search() throws Exception {
        final S3Session session = new S3Session(
                new Host(new S3Protocol(), new S3Protocol().getDefaultHostname(),
                        new Credentials(
                                System.getProperties().getProperty("s3.key"), System.getProperties().getProperty("s3.secret")
                        )));
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final Path workdir = new Path("test-us-east-1-cyberduck", EnumSet.of(Path.Type.volume));
        workdir.attributes().setRegion("us-east-1");
        final String name = new AlphanumericRandomStringService().random();
        final Path file = new Path(workdir, name, EnumSet.of(Path.Type.file));
        session.getFeature(Touch.class).touch(file, new TransferStatus());
        final S3SearchFeature feature = new S3SearchFeature(session);
        assertTrue(feature.search(workdir, new SearchFilter(name), new DisabledListProgressListener()).contains(file));
        // Supports prefix matching only
        assertFalse(feature.search(workdir, new SearchFilter(StringUtils.substring(name, 2)), new DisabledListProgressListener()).contains(file));
        assertTrue(feature.search(workdir, new SearchFilter(StringUtils.substring(name, 0, name.length() - 2)), new DisabledListProgressListener()).contains(file));
        final Path subdir = new Path(workdir, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        assertFalse(feature.search(subdir, new SearchFilter(name), new DisabledListProgressListener()).contains(file));
        new S3DirectoryFeature(session, new S3WriteFeature(session)).mkdir(subdir, null, new TransferStatus());
        assertFalse(feature.search(subdir, new SearchFilter(name), new DisabledListProgressListener()).contains(file));
        new S3DefaultDeleteFeature(session).delete(Arrays.asList(file, subdir), new DisabledLoginCallback(), new Delete.DisabledCallback());
        session.close();
    }

}