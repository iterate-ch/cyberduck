package ch.cyberduck.core.ftp;

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
import ch.cyberduck.core.DisabledTranscriptListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.shared.DefaultHomeFinderService;
import ch.cyberduck.core.shared.DefaultTouchFeature;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;
import java.util.UUID;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@Category(IntegrationTest.class)
public class FTPMoveFeatureTest {

    @Test
    public void testMove() throws Exception {
        final Host host = new Host(new FTPTLSProtocol(), "test.cyberduck.ch", new Credentials(
                System.getProperties().getProperty("ftp.user"), System.getProperties().getProperty("ftp.password")
        ));
        final FTPSession session = new FTPSession(host);
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final FTPWorkdirService workdir = new FTPWorkdirService(session);
        final Path test = new Path(workdir.find(), UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        new DefaultTouchFeature(session).touch(test);
        final Path target = new Path(workdir.find(), UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        new FTPMoveFeature(session).move(test, target, false, new Delete.DisabledCallback());
        assertFalse(session.getFeature(Find.class).find(test));
        assertTrue(session.getFeature(Find.class).find(target));
        new FTPDeleteFeature(session).delete(Collections.<Path>singletonList(target), new DisabledLoginCallback(), new Delete.DisabledCallback());
        session.close();
    }

    @Test
    public void testMoveOverride() throws Exception {
        final Host host = new Host(new FTPTLSProtocol(), "test.cyberduck.ch", new Credentials(
                System.getProperties().getProperty("ftp.user"), System.getProperties().getProperty("ftp.password")
        ));
        final FTPSession session = new FTPSession(host);
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final DefaultHomeFinderService workdir = new FTPWorkdirService(session);
        final Path test = new Path(workdir.find(), UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        new DefaultTouchFeature(session).touch(test);
        final Path target = new Path(workdir.find(), UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        new DefaultTouchFeature(session).touch(target);
        new FTPMoveFeature(session).move(test, target, false, new Delete.DisabledCallback());
        assertFalse(session.getFeature(Find.class).find(test));
        assertTrue(session.getFeature(Find.class).find(target));
        new FTPDeleteFeature(session).delete(Collections.<Path>singletonList(target), new DisabledLoginCallback(), new Delete.DisabledCallback());
        session.close();
    }

    @Test(expected = NotfoundException.class)
    public void testMoveNotFound() throws Exception {
        final Host host = new Host(new FTPTLSProtocol(), "test.cyberduck.ch", new Credentials(
                System.getProperties().getProperty("ftp.user"), System.getProperties().getProperty("ftp.password")
        ));
        final FTPSession session = new FTPSession(host);
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final DefaultHomeFinderService workdir = new FTPWorkdirService(session);
        final Path test = new Path(workdir.find(), UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        new FTPMoveFeature(session).move(test, new Path(workdir.find(), UUID.randomUUID().toString(), EnumSet.of(Path.Type.file)), false, new Delete.DisabledCallback());
        session.close();
    }
}
