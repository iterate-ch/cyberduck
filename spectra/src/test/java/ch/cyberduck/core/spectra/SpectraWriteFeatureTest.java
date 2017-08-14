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

import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.Cache;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledConnectionCallback;
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

import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.text.RandomStringGenerator;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
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
        final Path container = new Path("cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final byte[] content = RandomUtils.nextBytes(1000);
        final TransferStatus status = new TransferStatus().length(content.length);
        status.setChecksum(new CRC32ChecksumCompute().compute(new ByteArrayInputStream(content), status));
        // Allocate
        final SpectraBulkService bulk = new SpectraBulkService(session);
        bulk.pre(Transfer.Type.upload, Collections.singletonMap(test, status), new DisabledConnectionCallback());
        {
            final OutputStream out = new SpectraWriteFeature(session).write(test, status, new DisabledConnectionCallback());
            assertNotNull(out);
            new StreamCopier(new TransferStatus(), new TransferStatus()).transfer(new ByteArrayInputStream(content), out);
            out.close();
        }
        assertEquals(content.length, new S3AttributesFinderFeature(session).find(test).getSize());
        // Overwrite
        bulk.pre(Transfer.Type.upload, Collections.singletonMap(test, status.exists(true)), new DisabledConnectionCallback());
        {
            final OutputStream out = new SpectraWriteFeature(session).write(test, status.exists(true), new DisabledConnectionCallback());
            new StreamCopier(new TransferStatus(), new TransferStatus()).transfer(new ByteArrayInputStream(content), out);
            out.close();
        }
        assertEquals(content.length, new S3AttributesFinderFeature(session).find(test).getSize());
        new SpectraDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
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
        final Path container = new Path("cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        // Make 0-byte file
        new SpectraTouchFeature(session).touch(test, new TransferStatus());
        // Replace content
        final byte[] content = new RandomStringGenerator.Builder().build().generate(1000).getBytes();
        final TransferStatus status = new TransferStatus().length(content.length);
        status.setChecksum(new CRC32ChecksumCompute().compute(new ByteArrayInputStream(content), status));
        final SpectraBulkService bulk = new SpectraBulkService(session);
        bulk.pre(Transfer.Type.upload, Collections.singletonMap(test, status.exists(true)), new DisabledConnectionCallback());
        final OutputStream out = new SpectraWriteFeature(session).write(test, status, new DisabledConnectionCallback());
        assertNotNull(out);
        new StreamCopier(new TransferStatus(), new TransferStatus()).transfer(new ByteArrayInputStream(content), out);
        out.close();
        new SpectraDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
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
            public Find withCache(final Cache<Path> cache) {
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
            public AttributesFinder withCache(final Cache<Path> cache) {
                return this;
            }
        });
        final Write.Append append = feature.append(new Path("/p", EnumSet.of(Path.Type.file)), 0L, PathCache.empty());
        assertFalse(append.append);
        assertTrue(append.override);
        assertEquals(3L, append.size, 0L);
    }

    @Test
    public void testSPECTRA69() throws Exception {
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
        final Path container = new Path("CYBERDUCK-SPECTRA-69", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        // Allocate
        final SpectraBulkService bulk = new SpectraBulkService(session);
        {
            final byte[] content1 = RandomUtils.nextBytes(1000);
            final TransferStatus status = new TransferStatus().length(content1.length);
            status.setChecksum(new CRC32ChecksumCompute().compute(new ByteArrayInputStream(content1), status));
            bulk.pre(Transfer.Type.upload, Collections.singletonMap(test, status), new DisabledConnectionCallback());
            final OutputStream out = new SpectraWriteFeature(session).write(test, status, new DisabledConnectionCallback());
            assertNotNull(out);
            new StreamCopier(new TransferStatus(), new TransferStatus()).transfer(new ByteArrayInputStream(content1), out);
            out.close();
            assertEquals(content1.length, new S3AttributesFinderFeature(session).find(test).getSize());
            bulk.pre(Transfer.Type.download, Collections.singletonMap(test, status), new DisabledConnectionCallback());
            final InputStream in = new SpectraReadFeature(session).read(test, status, new DisabledConnectionCallback());
            assertNotNull(in);
            final ByteArrayOutputStream buffer = new ByteArrayOutputStream(content1.length);
            new StreamCopier(status, status).transfer(in, buffer);
            assertArrayEquals(content1, buffer.toByteArray());
        }
        {
            final byte[] content2 = RandomUtils.nextBytes(1000);
            final TransferStatus status = new TransferStatus().length(content2.length);
            // Overwrite
            bulk.pre(Transfer.Type.upload, Collections.singletonMap(test, status.exists(true)), new DisabledConnectionCallback());
            final OutputStream out = new SpectraWriteFeature(session).write(test, status.exists(true), new DisabledConnectionCallback());
            new StreamCopier(new TransferStatus(), new TransferStatus()).transfer(new ByteArrayInputStream(content2), out);
            out.close();
            assertEquals(content2.length, new S3AttributesFinderFeature(session).find(test).getSize());
            bulk.pre(Transfer.Type.download, Collections.singletonMap(test, status), new DisabledConnectionCallback());
            final InputStream in = new SpectraReadFeature(session).read(test, status, new DisabledConnectionCallback());
            assertNotNull(in);
            final ByteArrayOutputStream buffer = new ByteArrayOutputStream(content2.length);
            new StreamCopier(status, status).transfer(in, buffer);
            assertArrayEquals(content2, buffer.toByteArray());
        }
        new SpectraDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
        session.close();
    }
}
