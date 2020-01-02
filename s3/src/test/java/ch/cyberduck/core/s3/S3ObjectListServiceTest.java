package ch.cyberduck.core.s3;

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

import ch.cyberduck.core.*;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.proxy.Proxy;
import ch.cyberduck.core.ssl.DefaultTrustManagerHostnameCallback;
import ch.cyberduck.core.ssl.DisabledX509TrustManager;
import ch.cyberduck.core.ssl.KeychainX509KeyManager;
import ch.cyberduck.core.ssl.KeychainX509TrustManager;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.jets3t.service.Jets3tProperties;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class S3ObjectListServiceTest extends AbstractS3Test {

    @Test
    public void testList() throws Exception {
        final Path container = new Path("test-us-east-1-cyberduck", EnumSet.of(Path.Type.volume));
        container.attributes().setRegion("us-east-1");
        final AttributedList<Path> list = new S3ObjectListService(session).list(container, new DisabledListProgressListener());
        for(Path p : list) {
            assertEquals(container, p.getParent());
            assertEquals("us-east-1", p.attributes().getRegion());
            if(p.isFile()) {
                assertNotEquals(-1L, p.attributes().getModificationDate());
                assertNotEquals(-1L, p.attributes().getSize());
                assertNotNull(p.attributes().getETag());
                assertNotNull(p.attributes().getStorageClass());
            }
        }
    }

    @Test
    public void testDirectory() throws Exception {
        final Path bucket = new Path("test-us-east-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path directory = new S3DirectoryFeature(session, new S3WriteFeature(session)).mkdir(new Path(bucket, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), null, new TransferStatus());
        assertTrue(new S3ObjectListService(session).list(bucket, new DisabledListProgressListener()).contains(directory));
        new S3DefaultDeleteFeature(session).delete(Collections.singletonList(directory), new DisabledLoginCallback(), new Delete.DisabledCallback());
        assertFalse(new S3ObjectListService(session).list(bucket, new DisabledListProgressListener()).contains(directory));
    }

    @Test(expected = NotfoundException.class)
    public void testListNotFoundFolder() throws Exception {
        final Path container = new Path("test-us-east-1-cyberduck", EnumSet.of(Path.Type.volume));
        new S3ObjectListService(session).list(new Path(container, "notfound", EnumSet.of(Path.Type.directory)), new DisabledListProgressListener());
    }

    @Test(expected = NotfoundException.class)
    @Ignore
    public void testListNotFoundFolderMinio() throws Exception {
        final Host host = new Host(new S3Protocol(), "play.minio.io", 9000, new Credentials(
            "Q3AM3UQ867SPQQA43P2F", "zuf+tfteSlswRu7BJ86wekitnifILbZam1KYY3TG"
        ));
        final S3Session session = new S3Session(host);
        final LoginConnectionService login = new LoginConnectionService(new DisabledLoginCallback(), new DisabledHostKeyCallback(),
            new DisabledPasswordStore(), new DisabledProgressListener());
        login.check(session, PathCache.empty(), new DisabledCancelCallback());
        final Path bucket = new S3DirectoryFeature(session, new S3WriteFeature(session)).mkdir(
            new Path(new S3HomeFinderService(session).find(), new AsciiRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), null, new TransferStatus());
        try {
            new S3ObjectListService(session).list(new Path(bucket, "notfound", EnumSet.of(Path.Type.directory)), new DisabledListProgressListener());
        }
        finally {
            new S3DefaultDeleteFeature(session).delete(Collections.singletonList(bucket), new DisabledLoginCallback(), new Delete.DisabledCallback());
        }
    }

    @Test(expected = NotfoundException.class)
    public void testListNotfoundBucket() throws Exception {
        final Path container = new Path("notfound.cyberduck.ch", EnumSet.of(Path.Type.volume));
        new S3ObjectListService(session).list(container, new DisabledListProgressListener());
    }

    @Test
    public void testListBuckenameAnonymous() throws Exception {
        final Host host = new Host(new S3Protocol(), "dist.springframework.org.s3.amazonaws.com", new Credentials(
            PreferencesFactory.get().getProperty("connection.login.anon.name"), null
        ));
        final S3Session session = new S3Session(host);
        final LoginConnectionService login = new LoginConnectionService(new DisabledLoginCallback() {
            @Override
            public Credentials prompt(final Host bookmark, final String username, final String title, final String reason, final LoginOptions options) {
                fail(reason);
                return null;
            }
        }, new DisabledHostKeyCallback(),
            new DisabledPasswordStore(), new DisabledProgressListener());
        login.check(session, PathCache.empty(), new DisabledCancelCallback());
        final AttributedList<Path> list
            = new S3ObjectListService(session).list(new Path("/dist.springframework.org", EnumSet.of(Path.Type.directory)),
            new DisabledListProgressListener());
        assertFalse(list.isEmpty());
        assertTrue(list.contains(new Path("/dist.springframework.org/release", EnumSet.of(Path.Type.directory, Path.Type.placeholder))));
        assertTrue(list.contains(new Path("/dist.springframework.org/milestone", EnumSet.of(Path.Type.directory, Path.Type.placeholder))));
        assertTrue(list.contains(new Path("/dist.springframework.org/snapshot", EnumSet.of(Path.Type.directory, Path.Type.placeholder))));
    }

    @Test
    public void testListDefaultPath() throws Exception {
        final Host host = new Host(new S3Protocol(), "dist.springframework.org.s3.amazonaws.com", new Credentials(
            PreferencesFactory.get().getProperty("connection.login.anon.name"), null
        ));
        host.setDefaultPath("/dist.springframework.org/release");
        final S3Session session = new S3Session(host);
        final LoginConnectionService login = new LoginConnectionService(new DisabledLoginCallback() {
            @Override
            public Credentials prompt(final Host bookmark, final String username, final String title, final String reason, final LoginOptions options) {
                fail(reason);
                return null;
            }
        }, new DisabledHostKeyCallback(),
            new DisabledPasswordStore(), new DisabledProgressListener());
        login.check(session, PathCache.empty(), new DisabledCancelCallback());
        assertEquals(new Path("/dist.springframework.org/release", EnumSet.of(Path.Type.directory)), new S3HomeFinderService(session).find());
        final AttributedList<Path> list
            = new S3ObjectListService(session).list(new S3HomeFinderService(session).find(), new DisabledListProgressListener());
        assertFalse(list.isEmpty());
        assertTrue(list.contains(new Path("/dist.springframework.org/release/SWF", EnumSet.of(Path.Type.directory, Path.Type.placeholder))));
        session.close();
    }

    @Test
    public void testListEncodedCharacter() throws Exception {
        final Path container = new Path("test-us-east-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        container.attributes().setRegion("us-east-1");
        final Path placeholder = new S3TouchFeature(session).touch(
            new Path(container, String.format("^<%%%s", new AlphanumericRandomStringService().random()), EnumSet.of(Path.Type.file)), new TransferStatus());
        assertTrue(new S3ObjectListService(session).list(container, new DisabledListProgressListener()).contains(placeholder));
        new S3DefaultDeleteFeature(session).delete(Collections.singletonList(placeholder), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testListInvisibleCharacter() throws Exception {
        final Path container = new Path("test-us-east-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        container.attributes().setRegion("us-east-1");
        final Path placeholder = new S3TouchFeature(session).touch(
            new Path(container, String.format("test-\u001F-%s", new AlphanumericRandomStringService().random()), EnumSet.of(Path.Type.file)), new TransferStatus());
        assertTrue(new S3ObjectListService(session).list(container, new DisabledListProgressListener()).contains(placeholder));
        new S3DefaultDeleteFeature(session).delete(Collections.singletonList(placeholder), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testListPlaceholder() throws Exception {
        final Path container = new Path("test-us-east-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        container.attributes().setRegion("us-east-1");
        final Path placeholder = new S3DirectoryFeature(session, new S3WriteFeature(session, new S3DisabledMultipartService())).mkdir(
            new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.directory)), null, new TransferStatus());
        final AttributedList<Path> list = new S3ObjectListService(session).list(placeholder, new DisabledListProgressListener());
        assertTrue(list.isEmpty());
        new S3DefaultDeleteFeature(session).delete(Collections.singletonList(placeholder), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testListPlaceholderTilde() throws Exception {
        final Path container = new Path("test-us-east-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        container.attributes().setRegion("us-east-1");
        final Path placeholder = new S3DirectoryFeature(session, new S3WriteFeature(session, new S3DisabledMultipartService())).mkdir(
            new Path(container, String.format("%s~", UUID.randomUUID().toString()), EnumSet.of(Path.Type.directory)), null, new TransferStatus());
        final AttributedList<Path> list = new S3ObjectListService(session).list(placeholder, new DisabledListProgressListener());
        assertTrue(list.isEmpty());
        new S3DefaultDeleteFeature(session).delete(Collections.singletonList(placeholder), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testListPlaceholderAtSignSignatureAWS4() throws Exception {
        final Path container = new Path("test-us-east-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        container.attributes().setRegion("us-east-1");
        final Path placeholder = new S3DirectoryFeature(session, new S3WriteFeature(session, new S3DisabledMultipartService())).mkdir(
            new Path(container, String.format("%s@", UUID.randomUUID().toString()), EnumSet.of(Path.Type.directory)), null, new TransferStatus());
        final AttributedList<Path> list = new S3ObjectListService(session).list(placeholder, new DisabledListProgressListener());
        assertTrue(list.isEmpty());
        new S3DefaultDeleteFeature(session).delete(Collections.singletonList(placeholder), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testListAWS4SignatureFrankfurt() throws Exception {
        session.setSignatureVersion(S3Protocol.AuthenticationHeaderSignatureVersion.AWS4HMACSHA256);
        final Path container = new Path("test-eu-central-1-cyberduck", EnumSet.of(Path.Type.volume));
        final AttributedList<Path> list = new S3ObjectListService(session).list(container, new DisabledListProgressListener());
    }

    @Test
    public void testListAWS2AutoSwitchAWS4SignatureFrankfurt() throws Exception {
        session.setSignatureVersion(S3Protocol.AuthenticationHeaderSignatureVersion.AWS2);
        final Path container = new Path("test-eu-central-1-cyberduck", EnumSet.of(Path.Type.volume));
        final AttributedList<Path> list = new S3ObjectListService(session).list(container, new DisabledListProgressListener());
    }

    @Test(expected = BackgroundException.class)
    public void testAccessPathStyleBucketEuCentral() throws Exception {
        final S3Session session = new S3Session(
            new Host(new S3Protocol(), new S3Protocol().getDefaultHostname(),
                new Credentials(
                    System.getProperties().getProperty("s3.key"), System.getProperties().getProperty("s3.secret")
                ))) {
            @Override
            public S3Protocol.AuthenticationHeaderSignatureVersion getSignatureVersion() {
                return S3Protocol.AuthenticationHeaderSignatureVersion.AWS4HMACSHA256;
            }

            @Override
            protected Jets3tProperties configure() {
                final Jets3tProperties properties = super.configure();
                properties.setProperty("s3service.disable-dns-buckets", String.valueOf(true));
                return properties;
            }
        };
        session.open(Proxy.DIRECT, new DisabledHostKeyCallback(), new DisabledLoginCallback());
        session.login(Proxy.DIRECT, new DisabledLoginCallback(), new DisabledCancelCallback());
        final Path container = new Path("test-eu-central-1-cyberduck", EnumSet.of(Path.Type.volume));
        try {
            final AttributedList<Path> list = new S3ObjectListService(session).list(container, new DisabledListProgressListener());
        }
        catch(BackgroundException e) {
            assertEquals("Listing directory test-eu-central-1-cyberduck failed.", e.getMessage());
            assertEquals("Received redirect response HTTP/1.1 301 Moved Permanently but no location header.", e.getDetail());
            throw e;
        }
    }

    @Test
    public void testLaxHostnameVerification() throws Exception {
        final Host host = new Host(new S3Protocol(), new S3Protocol().getDefaultHostname(), new Credentials(
            System.getProperties().getProperty("s3.key"), System.getProperties().getProperty("s3.secret")
        ));
        final KeychainX509TrustManager trust = new KeychainX509TrustManager(new DisabledCertificateTrustCallback(), new DefaultTrustManagerHostnameCallback(host),
            new DisabledCertificateStore() {
                @Override
                public boolean verify(final CertificateTrustCallback prompt, final String hostname, final List<X509Certificate> certificates) throws CertificateException {
                    assertEquals("ch.s3.amazonaws.com", hostname);
                    return true;
                }
            });
        final S3Session session = new S3Session(host, new DisabledX509TrustManager(),
            new KeychainX509KeyManager(new DisabledCertificateIdentityCallback(), host, new DisabledCertificateStore()));
        final LoginConnectionService login = new LoginConnectionService(new DisabledLoginCallback() {
            @Override
            public Credentials prompt(final Host bookmark, final String username, final String title, final String reason, final LoginOptions options) {
                fail(reason);
                return null;
            }
        }, new DisabledHostKeyCallback(),
            new DisabledPasswordStore(), new DisabledProgressListener());
        login.check(session, PathCache.empty(), new DisabledCancelCallback());
        new S3ObjectListService(session).list(
            new Path("test-us-east-1-cyberduck", EnumSet.of(Path.Type.volume, Path.Type.directory)), new DisabledListProgressListener());
    }

    @Test
    public void testListFilePlusCharacter() throws Exception {
        final Path container = new Path("test-eu-central-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path placeholder = new S3TouchFeature(session).touch(
            new Path(container, String.format("test+%s", new AlphanumericRandomStringService().random()), EnumSet.of(Path.Type.file)), new TransferStatus());
        assertTrue(new S3ObjectListService(session).list(container, new DisabledListProgressListener()).contains(placeholder));
        new S3DefaultDeleteFeature(session).delete(Collections.singletonList(placeholder), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testListPlaceholderPlusCharacter() throws Exception {
        final Path container = new Path("test-eu-central-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path placeholder = new S3DirectoryFeature(session, new S3WriteFeature(session)).mkdir(
            new Path(container, String.format("test+%s", new AlphanumericRandomStringService().random()), EnumSet.of(Path.Type.directory)), null, new TransferStatus());
        assertTrue(new S3ObjectListService(session).list(container, new DisabledListProgressListener()).contains(placeholder));
        assertTrue(new S3ObjectListService(session).list(placeholder, new DisabledListProgressListener()).isEmpty());
        new S3DefaultDeleteFeature(session).delete(Collections.singletonList(placeholder), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
