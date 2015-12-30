/*
 * Copyright (c) 2015-2016 Spectra Logic Corporation. All rights reserved.
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

package ch.cyberduck.core.spectra;

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
import ch.cyberduck.core.TranscriptListener;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Location;
import ch.cyberduck.core.s3.S3DefaultDeleteFeature;
import ch.cyberduck.core.s3.S3DirectoryFeature;
import ch.cyberduck.core.s3.S3FindFeature;
import ch.cyberduck.core.s3.S3Protocol;
import ch.cyberduck.core.shared.DefaultFindFeature;
import ch.cyberduck.core.shared.DefaultHomeFinderService;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DisabledX509TrustManager;

import org.junit.Test;

import java.util.Collections;
import java.util.EnumSet;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertTrue;

/**
 * @version $Id$
 */
public class SpectraDirectoryFeatureTest {

    @Test
    public void testCreateBucket() throws Exception {
        final Host host = new Host(new SpectraProtocol(), System.getProperties().getProperty("spectra.hostname"), 8080, new Credentials(
                System.getProperties().getProperty("spectra.user"), System.getProperties().getProperty("spectra.key")
        ));
        final SpectraSession session = new SpectraSession(host, new DisabledX509TrustManager(),
                new DefaultX509KeyManager());
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        final S3DirectoryFeature feature = new S3DirectoryFeature(session);
        for(Location.Name region : new S3Protocol().getRegions()) {
            final Path test = new Path(new DefaultHomeFinderService(session).find(), UUID.randomUUID().toString(), EnumSet.of(Path.Type.directory, Path.Type.volume));
            test.attributes().setRegion(region.getIdentifier());
            feature.mkdir(test, region.getIdentifier());
            assertTrue(new S3FindFeature(session).find(test));
            new S3DefaultDeleteFeature(session).delete(Collections.<Path>singletonList(test), new DisabledLoginCallback(), new Delete.Callback() {
                @Override
                public void delete(final Path file) {
                }
            });
        }
        session.close();
    }

    @Test
    public void testCreatePlaceholder() throws Exception {
        final Host host = new Host(new SpectraProtocol(), System.getProperties().getProperty("spectra.hostname"), 8080, new Credentials(
                System.getProperties().getProperty("spectra.user"), System.getProperties().getProperty("spectra.key")
        ));
        final SpectraSession session = new SpectraSession(host, new DisabledX509TrustManager(),
                new DefaultX509KeyManager());
        final AtomicBoolean b = new AtomicBoolean();
        final String name = UUID.randomUUID().toString();
        session.open(new DisabledHostKeyCallback(), new TranscriptListener() {
            @Override
            public void log(final boolean request, final String message) {
                if(request) {
                    if(("PUT /cyberduck/" + name + "/ HTTP/1.1").equals(message)) {
                        b.set(true);
                    }
                }
            }
        });
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        final Path container = new Path("cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new Path(container, name, EnumSet.of(Path.Type.directory));
        new S3DirectoryFeature(session).mkdir(test, null);
        assertTrue(b.get());
        test.setType(EnumSet.of(Path.Type.directory, Path.Type.placeholder));
        assertTrue(new S3FindFeature(session).find(test));
        assertTrue(new DefaultFindFeature(session).find(test));
        new S3DefaultDeleteFeature(session).delete(Collections.<Path>singletonList(test), new DisabledLoginCallback(), new Delete.Callback() {
            @Override
            public void delete(final Path file) {
            }
        });
        session.close();
    }
}
