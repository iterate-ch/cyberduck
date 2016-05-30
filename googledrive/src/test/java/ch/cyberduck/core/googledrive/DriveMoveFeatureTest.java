package ch.cyberduck.core.googledrive;

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
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.DisabledProgressListener;
import ch.cyberduck.core.DisabledTranscriptListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LoginConnectionService;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.Scheme;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DefaultX509TrustManager;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.UUID;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class DriveMoveFeatureTest {

    @Test
    public void testMoveFile() throws Exception {
        final Host host = new Host(new DriveProtocol(), "www.googleapis.com", new Credentials());
        final DriveSession session = new DriveSession(host, new DefaultX509TrustManager(), new DefaultX509KeyManager());
        new LoginConnectionService(new DisabledLoginCallback() {
            @Override
            public void prompt(final Host bookmark, final Credentials credentials, final String title, final String reason, final LoginOptions options) throws LoginCanceledException {
                fail(reason);
            }
        }, new DisabledHostKeyCallback(),
                new DisabledPasswordStore() {
                    @Override
                    public String getPassword(Scheme scheme, int port, String hostname, String user) {
                        if(user.equals("Google Drive OAuth2 Access Token")) {
                            return System.getProperties().getProperty("googledrive.accesstoken");
                        }
                        if(user.equals("Google Drive OAuth2 Refresh Token")) {
                            return System.getProperties().getProperty("googledrive.refreshtoken");
                        }
                        return null;
                    }

                    @Override
                    public String getPassword(String hostname, String user) {
                        return super.getPassword(hostname, user);
                    }
                }, new DisabledProgressListener(),
                new DisabledTranscriptListener()).connect(session, PathCache.empty());
        final Path test = new Path(new DriveHomeFinderService(session).find(), UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        new DriveTouchFeature(session).touch(test);
        final Path folder = new Path(new DriveHomeFinderService(session).find(), UUID.randomUUID().toString(), EnumSet.of(Path.Type.directory));
        new DriveDirectoryFeature(session).mkdir(folder);
        final Path target = new Path(folder, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        new DriveMoveFeature(session).move(test, target, false, new Delete.Callback() {
            @Override
            public void delete(final Path file) {
            }
        });
        assertFalse(session.getFeature(Find.class).find(test));
        assertTrue(session.getFeature(Find.class).find(target));
        new DriveDeleteFeature(session).delete(Arrays.asList(target, folder), new DisabledLoginCallback(), new Delete.Callback() {
            @Override
            public void delete(final Path file) {
            }
        });
    }

    @Test
    public void testMoveDirectory() throws Exception {
        final Host host = new Host(new DriveProtocol(), "www.googleapis.com", new Credentials());
        final DriveSession session = new DriveSession(host, new DefaultX509TrustManager(), new DefaultX509KeyManager());
        new LoginConnectionService(new DisabledLoginCallback() {
            @Override
            public void prompt(final Host bookmark, final Credentials credentials, final String title, final String reason, final LoginOptions options) throws LoginCanceledException {
                fail(reason);
            }
        }, new DisabledHostKeyCallback(),
                new DisabledPasswordStore() {
                    @Override
                    public String getPassword(Scheme scheme, int port, String hostname, String user) {
                        if(user.equals("Google Drive OAuth2 Access Token")) {
                            return System.getProperties().getProperty("googledrive.accesstoken");
                        }
                        if(user.equals("Google Drive OAuth2 Refresh Token")) {
                            return System.getProperties().getProperty("googledrive.refreshtoken");
                        }
                        return null;
                    }

                    @Override
                    public String getPassword(String hostname, String user) {
                        return super.getPassword(hostname, user);
                    }
                }, new DisabledProgressListener(),
                new DisabledTranscriptListener()).connect(session, PathCache.empty());
        final Path sourceDirectory = new Path(new DriveHomeFinderService(session).find(), UUID.randomUUID().toString(), EnumSet.of(Path.Type.directory));
        final Path targetDirectory = new Path(new DriveHomeFinderService(session).find(), UUID.randomUUID().toString(), EnumSet.of(Path.Type.directory));
        new DriveDirectoryFeature(session).mkdir(sourceDirectory);
        final Path sourceFile = new Path(sourceDirectory, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        new DriveTouchFeature(session).touch(sourceFile);
        final Path targetFile = new Path(targetDirectory, sourceFile.getName(), EnumSet.of(Path.Type.file));
        new DriveMoveFeature(session).move(sourceDirectory, targetDirectory, false, new Delete.Callback() {
            @Override
            public void delete(final Path file) {
            }
        });
        final Find find = session.getFeature(Find.class);
        assertFalse(find.find(sourceDirectory));
        assertFalse(find.find(sourceFile));
        assertTrue(find.find(targetDirectory));
        assertTrue(find.find(targetFile));
        new DriveDeleteFeature(session).delete(Collections.singletonList(targetDirectory), new DisabledLoginCallback(), new Delete.Callback() {
            @Override
            public void delete(final Path file) {
            }
        });
    }
}
