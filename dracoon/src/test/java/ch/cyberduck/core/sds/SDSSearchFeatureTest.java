package ch.cyberduck.core.sds;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
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
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.SimplePathPredicate;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DisabledX509TrustManager;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;
import ch.cyberduck.ui.browser.SearchFilter;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class SDSSearchFeatureTest {

    @Test
    public void testSearch() throws Exception {
        final Host host = new Host(new SDSProtocol(), "duck.ssp-europe.eu", new Credentials(
                System.getProperties().getProperty("sds.user"), System.getProperties().getProperty("sds.key")
        ));
        final SDSSession session = new SDSSession(host, new DisabledX509TrustManager(), new DefaultX509KeyManager());
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        final String name = new AlphanumericRandomStringService().random();
        final Path room = new SDSDirectoryFeature(session).mkdir(
                new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), null, new TransferStatus());
        final Path directory = new SDSDirectoryFeature(session).mkdir(new Path(room, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), null, new TransferStatus());
        final Path file = new SDSTouchFeature(session).touch(new Path(directory, name, EnumSet.of(Path.Type.file)), new TransferStatus());
        final SDSSearchFeature feature = new SDSSearchFeature(session);
        assertTrue(feature.search(room, new SearchFilter(name), new DisabledListProgressListener()).contains(file));
        assertTrue(feature.search(room, new SearchFilter(StringUtils.substring(name, 2)), new DisabledListProgressListener()).contains(file));
        assertTrue(feature.search(room, new SearchFilter(StringUtils.substring(name, 0, name.length() - 2)), new DisabledListProgressListener()).contains(file));
        assertTrue(feature.search(directory, new SearchFilter(StringUtils.substring(name, 0, name.length() - 2)), new DisabledListProgressListener()).contains(file));
        try {
            assertFalse(feature.search(new Path(room, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new SearchFilter(name), new DisabledListProgressListener()).contains(file));
            fail();
        }
        catch(NotfoundException e) {
            //
        }
        final Path subdir = new SDSDirectoryFeature(session).mkdir(new Path(directory, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), null, new TransferStatus());
        assertNull(feature.search(subdir, new SearchFilter(name), new DisabledListProgressListener()).find(new SimplePathPredicate(file)));
        final Path filesubdir = new SDSTouchFeature(session).touch(new Path(subdir, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        {
            final AttributedList<Path> result = feature.search(directory, new SearchFilter(filesubdir.getName()), new DisabledListProgressListener());
            assertNotNull(result.find(new SimplePathPredicate(filesubdir)));
            assertEquals(subdir, result.find(new SimplePathPredicate(filesubdir)).getParent());
        }
        new SDSDeleteFeature(session).delete(Collections.singletonList(room), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
