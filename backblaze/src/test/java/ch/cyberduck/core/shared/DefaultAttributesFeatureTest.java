package ch.cyberduck.core.shared;

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
import ch.cyberduck.core.DisabledTranscriptListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.b2.B2DeleteFeature;
import ch.cyberduck.core.b2.B2FileidProvider;
import ch.cyberduck.core.b2.B2Protocol;
import ch.cyberduck.core.b2.B2Session;
import ch.cyberduck.core.b2.B2TouchFeature;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;
import java.util.UUID;

import synapticloop.b2.response.B2StartLargeFileResponse;

import static org.junit.Assert.assertNotNull;

@Category(IntegrationTest.class)
public class DefaultAttributesFeatureTest {

    @Test
    public void testFind() throws Exception {
        final B2Session session = new B2Session(
                new Host(new B2Protocol(), new B2Protocol().getDefaultHostname(),
                        new Credentials(
                                System.getProperties().getProperty("b2.user"), System.getProperties().getProperty("b2.key")
                        )));
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final Path bucket = new Path("test-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path file = new Path(bucket, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        new B2TouchFeature(session).touch(file);
        // Find without version id set in attributes
        assertNotNull(new DefaultAttributesFeature(session).find(file).getVersionId());
        new B2DeleteFeature(session).delete(Collections.singletonList(file), new DisabledLoginCallback(), new Delete.DisabledCallback());
        session.close();
    }

    @Test
    public void testFindLargeUpload() throws Exception {
        final B2Session session = new B2Session(
                new Host(new B2Protocol(), new B2Protocol().getDefaultHostname(),
                        new Credentials(
                                System.getProperties().getProperty("b2.user"), System.getProperties().getProperty("b2.key")
                        )));
        final Path bucket = new Path("test-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final Path file = new Path(bucket, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        final B2StartLargeFileResponse startResponse = session.getClient().startLargeFileUpload(
                new B2FileidProvider(session).getFileid(bucket),
                file.getName(), null, Collections.emptyMap());
        assertNotNull(new DefaultAttributesFeature(session).find(file));
        session.getClient().cancelLargeFileUpload(startResponse.getFileId());
        session.close();
    }
}