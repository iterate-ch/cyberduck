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

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.Scheme;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.io.CRC32ChecksumCompute;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.s3.S3AttributesFinderFeature;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DisabledX509TrustManager;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.ByteArrayInputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.EnumSet;
import java.util.UUID;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class SpectraWriteFeatureTest {

    @Test
    public void testWriteOverwrite() throws Exception {
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
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final Path container = new Path("test.cyberduck.ch", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        final byte[] content = RandomStringUtils.random(1000).getBytes();
        final TransferStatus status = new TransferStatus().length(content.length);
        status.setChecksum(new CRC32ChecksumCompute().compute(new ByteArrayInputStream(content), status));
        // Allocate
        final SpectraBulkService bulk = new SpectraBulkService(session);
        bulk.pre(Transfer.Type.upload, Collections.singletonMap(test, status));
        {
            final OutputStream out = new SpectraWriteFeature(session).write(test, status);
            assertNotNull(out);
            new StreamCopier(new TransferStatus(), new TransferStatus()).transfer(new ByteArrayInputStream(content), out);
            out.close();
        }
        assertEquals(content.length, new S3AttributesFinderFeature(session).find(test).getSize());
        // Overwrite
        bulk.pre(Transfer.Type.upload, Collections.singletonMap(test, status.exists(true)));
        {
            final OutputStream out = new SpectraWriteFeature(session).write(test, status.exists(true));
            new StreamCopier(new TransferStatus(), new TransferStatus()).transfer(new ByteArrayInputStream(content), out);
            out.close();
        }
        assertEquals(content.length, new S3AttributesFinderFeature(session).find(test).getSize());
        new SpectraDeleteFeature(session).delete(Collections.<Path>singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
        session.close();
    }

    @Test
    public void testOverwriteZeroSized() throws Exception {
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
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final Path container = new Path("test.cyberduck.ch", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        // Make 0-byte file
        new SpectraTouchFeature(session).touch(test);
        // Replace content
        final byte[] content = RandomStringUtils.random(1000).getBytes();
        final TransferStatus status = new TransferStatus().length(content.length);
        status.setChecksum(new CRC32ChecksumCompute().compute(new ByteArrayInputStream(content), status));
        final SpectraBulkService bulk = new SpectraBulkService(session);
        bulk.pre(Transfer.Type.upload, Collections.singletonMap(test, status.exists(true)));
        final OutputStream out = new SpectraWriteFeature(session).write(test, status);
        assertNotNull(out);
        new StreamCopier(new TransferStatus(), new TransferStatus()).transfer(new ByteArrayInputStream(content), out);
        out.close();
        new SpectraDeleteFeature(session).delete(Collections.<Path>singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
        session.close();
    }

    @Test
    public void testSize() throws Exception {
        final SpectraSession session = new SpectraSession(new Host(new SpectraProtocol() {
            @Override
            public Scheme getScheme() {
                return Scheme.http;
            }
        }), new DisabledX509TrustManager(),
                new DefaultX509KeyManager());
        final SpectraWriteFeature feature = new SpectraWriteFeature(session, new Find() {
            @Override
            public boolean find(final Path file) throws BackgroundException {
                return true;
            }

            @Override
            public Find withCache(final PathCache cache) {
                return this;
            }
        }, new AttributesFinder() {
            @Override
            public PathAttributes find(final Path file) throws BackgroundException {
                final PathAttributes attributes = new PathAttributes();
                attributes.setSize(3L);
                return attributes;
            }

            @Override
            public AttributesFinder withCache(final PathCache cache) {
                return this;
            }
        });
        final Write.Append append = feature.append(new Path("/p", EnumSet.of(Path.Type.file)), 0L, PathCache.empty());
        assertFalse(append.append);
        assertTrue(append.override);
        assertEquals(3L, append.size, 0L);
    }
}
