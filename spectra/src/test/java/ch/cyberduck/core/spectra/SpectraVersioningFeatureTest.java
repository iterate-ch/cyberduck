package ch.cyberduck.core.spectra;

/*
 * Copyright (c) 2002-2018 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Scheme;
import ch.cyberduck.core.VersioningConfiguration;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Versioning;
import ch.cyberduck.core.s3.S3HomeFinderService;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DisabledX509TrustManager;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;
import java.util.UUID;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@Category(IntegrationTest.class)
public class SpectraVersioningFeatureTest {

    @Test
    public void testGetConfigurationEnabled() throws Exception {
        final Host host = new Host(new SpectraProtocol() {
            @Override
            public Scheme getScheme() {
                return Scheme.http;
            }
        }, System.getProperties().getProperty("spectra.hostname"), Integer.valueOf(System.getProperties().getProperty("spectra.port")), new Credentials(
            System.getProperties().getProperty("spectra.user"), System.getProperties().getProperty("spectra.key")
        ));
        final SpectraSession session = new SpectraSession(host, new DisabledX509TrustManager(),
            new DefaultX509KeyManager());
        session.open(new DisabledHostKeyCallback(), new DisabledLoginCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        final Path bucket = new Path(new S3HomeFinderService(session).find(), UUID.randomUUID().toString(), EnumSet.of(Path.Type.directory, Path.Type.volume));
        new SpectraDirectoryFeature(session, new SpectraWriteFeature(session)).mkdir(bucket, null, new TransferStatus());
        final Versioning feature = new SpectraVersioningFeature(session);
        feature.getConfiguration(bucket);
        assertFalse(feature.getConfiguration(bucket).isEnabled());
        new SpectraDeleteFeature(session).delete(Collections.singletonList(bucket), new DisabledLoginCallback(), new Delete.DisabledCallback());
        session.close();
    }

    @Test
    @Ignore
    public void testSetConfiguration() throws Exception {
        final Host host = new Host(new SpectraProtocol() {
            @Override
            public Scheme getScheme() {
                return Scheme.http;
            }
        }, System.getProperties().getProperty("spectra.hostname"), Integer.valueOf(System.getProperties().getProperty("spectra.port")), new Credentials(
            System.getProperties().getProperty("spectra.user"), System.getProperties().getProperty("spectra.key")
        ));
        final SpectraSession session = new SpectraSession(host, new DisabledX509TrustManager(),
            new DefaultX509KeyManager());
        session.open(new DisabledHostKeyCallback(), new DisabledLoginCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        final Path bucket = new Path(new S3HomeFinderService(session).find(), UUID.randomUUID().toString(), EnumSet.of(Path.Type.directory, Path.Type.volume));
        new SpectraDirectoryFeature(session, new SpectraWriteFeature(session)).mkdir(bucket, null, new TransferStatus());
        final Versioning feature = new SpectraVersioningFeature(session);
        feature.setConfiguration(bucket, new DisabledLoginCallback(), new VersioningConfiguration(true, false));
        assertTrue(feature.getConfiguration(bucket).isEnabled());
        new SpectraDeleteFeature(session).delete(Collections.singletonList(bucket), new DisabledLoginCallback(), new Delete.DisabledCallback());
        session.close();
    }
}
