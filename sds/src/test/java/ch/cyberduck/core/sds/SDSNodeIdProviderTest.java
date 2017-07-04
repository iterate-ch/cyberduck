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
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DisabledX509TrustManager;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Arrays;
import java.util.EnumSet;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class SDSNodeIdProviderTest {

    @Test
    public void withCache() throws Exception {
        final Host host = new Host(new SDSProtocol(), "duck.ssp-europe.eu", new Credentials(
                System.getProperties().getProperty("sds.user"), System.getProperties().getProperty("sds.key")
        ));
        final SDSSession session = new SDSSession(host, new DisabledX509TrustManager(), new DefaultX509KeyManager());
        assertNotNull(new SDSNodeIdProvider(session).withCache(PathCache.empty()));
    }

    @Test
    public void getFileIdFile() throws Exception {
        final Host host = new Host(new SDSProtocol(), "duck.ssp-europe.eu", new Credentials(
                System.getProperties().getProperty("sds.user"), System.getProperties().getProperty("sds.key")
        ));
        final SDSSession session = new SDSSession(host, new DisabledX509TrustManager(), new DefaultX509KeyManager());
        final LoginConnectionService service = new LoginConnectionService(new DisabledLoginCallback(), new DisabledHostKeyCallback(),
                new DisabledPasswordStore(), new DisabledProgressListener());
        service.connect(session, PathCache.empty(), new DisabledCancelCallback());
        final Path bucket = new SDSDirectoryFeature(session).mkdir(new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), null, new TransferStatus());
        final String name = new AlphanumericRandomStringService().random();
        final Path file = new SDSTouchFeature(session).touch(new Path(bucket, name, EnumSet.of(Path.Type.file)), new TransferStatus());
        assertNotNull(new SDSNodeIdProvider(session).getFileid(file, new DisabledListProgressListener()));
        try {
            assertNull(new SDSNodeIdProvider(session).getFileid(new Path(bucket, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new DisabledListProgressListener()));
            fail();
        }
        catch(NotfoundException e) {
            // Expected
        }
        try {
            assertNull(new SDSNodeIdProvider(session).getFileid(new Path(bucket, name, EnumSet.of(Path.Type.directory)), new DisabledListProgressListener()));
            fail();
        }
        catch(NotfoundException e) {
            // Expected
        }
        new SDSDeleteFeature(session).delete(Arrays.asList(file, bucket), new DisabledLoginCallback(), new Delete.DisabledCallback());
        session.close();
    }

    @Test
    public void getFileIdDirectory() throws Exception {
        final Host host = new Host(new SDSProtocol(), "duck.ssp-europe.eu", new Credentials(
                System.getProperties().getProperty("sds.user"), System.getProperties().getProperty("sds.key")
        ));
        final SDSSession session = new SDSSession(host, new DisabledX509TrustManager(), new DefaultX509KeyManager());
        final LoginConnectionService service = new LoginConnectionService(new DisabledLoginCallback(), new DisabledHostKeyCallback(),
                new DisabledPasswordStore(), new DisabledProgressListener());
        service.connect(session, PathCache.empty(), new DisabledCancelCallback());
        final Path bucket = new SDSDirectoryFeature(session).mkdir(new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), null, new TransferStatus());
        final String name = new AlphanumericRandomStringService().random();
        final Path folder = new SDSDirectoryFeature(session).mkdir(new Path(bucket, name, EnumSet.of(Path.Type.directory)), null, new TransferStatus());
        assertNotNull(new SDSNodeIdProvider(session).getFileid(folder, new DisabledListProgressListener()));
        try {
            assertNull(new SDSNodeIdProvider(session).getFileid(new Path(bucket, name, EnumSet.of(Path.Type.file)), new DisabledListProgressListener()));
            fail();
        }
        catch(NotfoundException e) {
            //
        }
        new SDSDeleteFeature(session).delete(Arrays.asList(folder, bucket), new DisabledLoginCallback(), new Delete.DisabledCallback());
        session.close();
    }
}