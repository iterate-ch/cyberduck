package ch.cyberduck.core.s3;

/*
 * Copyright (c) 2002-2026 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.BytecountStreamListener;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.HostKeyCallback;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.LoginConnectionService;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Profile;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.ProtocolFactory;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.features.Copy;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.features.Location;
import ch.cyberduck.core.features.Move;
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.features.Upload;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.serializer.impl.dd.ProfilePlistReader;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DefaultX509TrustManager;
import ch.cyberduck.core.threading.CancelCallback;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;

import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;

/**
 * Runs against a live branch of Neon Object Storage. Requires environment variables
 * <code>NEON_S3_ENDPOINT</code> (branch endpoint hostname), <code>NEON_S3_ACCESS_KEY</code> (<code>token_id</code>),
 * <code>NEON_S3_SECRET_KEY</code> (<code>s3_secret_access_key</code>) and <code>NEON_S3_BUCKET</code> (existing bucket).
 */
@Category(IntegrationTest.class)
public class NeonObjectStorageIntegrationTest {

    private S3Session session;
    private Path bucket;

    @Before
    public void connect() throws Exception {
        assumeTrue("Missing Neon Object Storage configuration", StringUtils.isNoneBlank(
                System.getenv("NEON_S3_ENDPOINT"), System.getenv("NEON_S3_ACCESS_KEY"),
                System.getenv("NEON_S3_SECRET_KEY"), System.getenv("NEON_S3_BUCKET")));
        session = this.session(System.getenv("NEON_S3_ACCESS_KEY"), System.getenv("NEON_S3_SECRET_KEY"));
        bucket = new Path(System.getenv("NEON_S3_BUCKET"), EnumSet.of(Path.Type.directory, Path.Type.volume));
    }

    @After
    public void disconnect() throws Exception {
        if(session != null) {
            session.close();
        }
    }

    private S3Session session(final String key, final String secret) throws Exception {
        return this.session(key, secret, null);
    }

    private S3Session session(final String key, final String secret, final String defaultPath) throws Exception {
        final ProtocolFactory factory = new ProtocolFactory(new HashSet<>(Collections.singleton(new S3Protocol())));
        final Profile profile = new ProfilePlistReader(factory).read(
                this.getClass().getResourceAsStream("/Neon Object Storage.cyberduckprofile"));
        final Host host = new Host(profile, System.getenv("NEON_S3_ENDPOINT"), new Credentials(key, secret));
        if(defaultPath != null) {
            host.setDefaultPath(defaultPath);
        }
        final S3Session connection = new S3Session(host, new DefaultX509TrustManager(), new DefaultX509KeyManager());
        // Cancel any prompt for credentials on login failure
        new LoginConnectionService(new DisabledLoginCallback(), HostKeyCallback.noop, new DisabledPasswordStore(), ProgressListener.noop).check(connection, CancelCallback.noop);
        return connection;
    }

    @Test(expected = LoginCanceledException.class)
    public void testLoginFailure() throws Exception {
        this.session(System.getenv("NEON_S3_ACCESS_KEY"), "nsk_live_invalid");
    }

    @Test(expected = LoginCanceledException.class)
    public void testLoginFailureDefaultPathBucket() throws Exception {
        // Region is determined locally from the endpoint and must not skip validating credentials
        this.session(System.getenv("NEON_S3_ACCESS_KEY"), "nsk_live_invalid", String.format("/%s", System.getenv("NEON_S3_BUCKET")));
    }

    @Test
    public void testLoginDefaultPathBucket() throws Exception {
        final S3Session connection = this.session(System.getenv("NEON_S3_ACCESS_KEY"), System.getenv("NEON_S3_SECRET_KEY"),
                String.format("/%s", System.getenv("NEON_S3_BUCKET")));
        assertTrue(connection.isConnected());
        connection.close();
    }

    @Test
    public void testListBuckets() throws Exception {
        final AttributedList<Path> buckets = session.getFeature(ListService.class)
                .list(new Path("/", EnumSet.of(Path.Type.directory, Path.Type.volume)), new DisabledListProgressListener());
        assertTrue(buckets.contains(bucket));
        assertEquals(new S3LocationFeature.S3Region(S3Session.findRegionInNeonHostname(session.getHost().getHostname())),
                session.getFeature(Location.class).getLocation(bucket));
    }

    @Test
    public void testCreateDeleteBucket() throws Exception {
        final Path test = new Path(new AlphanumericRandomStringService().random().toLowerCase(), EnumSet.of(Path.Type.directory, Path.Type.volume));
        session.getFeature(Directory.class).mkdir(session.getFeature(Write.class), test, new TransferStatus());
        assertTrue(session.getFeature(Find.class).find(test));
        session.getFeature(Delete.class).delete(Collections.singletonList(test), LoginCallback.noop, new Delete.DisabledCallback());
        assertFalse(session.getFeature(Find.class).find(test));
    }

    @Test
    public void testFolderUploadCopyMoveDownloadDelete() throws Exception {
        final Path folder = session.getFeature(Directory.class).mkdir(session.getFeature(Write.class),
                new Path(bucket, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        assertTrue(session.getFeature(Find.class).find(folder));
        final Path file = new Path(folder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final byte[] content = this.upload(file, RandomUtils.nextBytes(1024));
        assertTrue(session.getFeature(Find.class).find(file));
        assertEquals(content.length, session.getFeature(AttributesFinder.class).find(file).getSize());
        assertTrue(session.getFeature(ListService.class).list(folder, new DisabledListProgressListener()).contains(file));
        // Server side copy
        final Path copy = new Path(folder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        session.getFeature(Copy.class).copy(file, copy, new TransferStatus().setLength(content.length), new DisabledConnectionCallback(), StreamListener.noop);
        assertArrayEquals(content, this.download(copy, content.length));
        // Rename
        final Path renamed = new Path(folder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        session.getFeature(Move.class).move(copy, renamed, new TransferStatus().setLength(content.length), new Delete.DisabledCallback(), new DisabledConnectionCallback());
        assertFalse(session.getFeature(Find.class).find(copy));
        assertArrayEquals(content, this.download(renamed, content.length));
        session.getFeature(Delete.class).delete(Arrays.asList(file, renamed, folder), LoginCallback.noop, new Delete.DisabledCallback());
        assertFalse(session.getFeature(Find.class).find(file));
        assertFalse(session.getFeature(Find.class).find(folder));
    }

    @Test
    public void testMultipartUpload() throws Exception {
        final Path file = new Path(bucket, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        // Use multipart upload service directly without fallback to single upload on failure. Three parts of 5MiB
        final byte[] content = this.upload(file, RandomUtils.nextBytes(11 * 1024 * 1024),
                new S3MultipartUploadService(session, new S3AccessControlListFeature(session), 5 * 1024 * 1024L, 2));
        assertEquals(content.length, session.getFeature(AttributesFinder.class).find(file).getSize());
        assertArrayEquals(content, this.download(file, content.length));
        session.getFeature(Delete.class).delete(Collections.singletonList(file), LoginCallback.noop, new Delete.DisabledCallback());
    }

    private byte[] upload(final Path file, final byte[] content) throws Exception {
        return this.upload(file, content, session.getFeature(Upload.class));
    }

    @SuppressWarnings("unchecked")
    private byte[] upload(final Path file, final byte[] content, final Upload upload) throws Exception {
        final Local local = new Local(System.getProperty("java.io.tmpdir"), new AlphanumericRandomStringService().random());
        try(OutputStream out = local.getOutputStream(false)) {
            IOUtils.write(content, out);
        }
        final TransferStatus status = new TransferStatus().setLength(content.length).setMime("application/octet-stream");
        final BytecountStreamListener count = new BytecountStreamListener();
        upload.upload(session.getFeature(Write.class), file, local, new BandwidthThrottle(BandwidthThrottle.UNLIMITED),
                ProgressListener.noop, count, status, LoginCallback.noop);
        assertEquals(content.length, count.getSent());
        assertTrue(status.isComplete());
        local.delete();
        return content;
    }

    private byte[] download(final Path file, final long length) throws Exception {
        try(InputStream in = session.getFeature(Read.class).read(file, new TransferStatus().setLength(length), new DisabledConnectionCallback())) {
            return IOUtils.toByteArray(in);
        }
    }
}
