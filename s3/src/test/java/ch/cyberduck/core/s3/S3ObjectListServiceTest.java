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

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledCertificateStore;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.DisabledTranscriptListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.proxy.DisabledProxyFinder;
import ch.cyberduck.core.ssl.DefaultTrustManagerHostnameCallback;
import ch.cyberduck.core.ssl.DisabledX509TrustManager;
import ch.cyberduck.core.ssl.KeychainX509KeyManager;
import ch.cyberduck.core.ssl.KeychainX509TrustManager;
import ch.cyberduck.test.IntegrationTest;

import org.jets3t.service.Jets3tProperties;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class S3ObjectListServiceTest {

    @Test
    public void testList() throws Exception {
        final S3Session session = new S3Session(
                new Host(new S3Protocol(), new S3Protocol().getDefaultHostname(),
                        new Credentials(
                                System.getProperties().getProperty("s3.key"), System.getProperties().getProperty("s3.secret")
                        )));
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final Path container = new Path("test-us-east-1-cyberduck", EnumSet.of(Path.Type.volume));
        container.attributes().setRegion("us-east-1");
        final List<Path> list = new S3ObjectListService(session).list(container, new DisabledListProgressListener());
        for(Path p : list) {
            assertEquals(container, p.getParent());
            assertEquals("us-east-1", p.attributes().getRegion());
            if(p.isFile()) {
                assertNotNull(p.attributes().getModificationDate());
                assertNotNull(p.attributes().getSize());
                assertNotNull(p.attributes().getETag());
                assertNotNull(p.attributes().getStorageClass());
            }
        }
        session.close();
    }

    @Test
    public void tetsEmptyPlaceholder() throws Exception {
        final S3Session session = new S3Session(
                new Host(new S3Protocol(), new S3Protocol().getDefaultHostname(),
                        new Credentials(
                                System.getProperties().getProperty("s3.key"), System.getProperties().getProperty("s3.secret")
                        )));
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final Path container = new Path("test-us-east-1-cyberduck", EnumSet.of(Path.Type.volume));
        final List<Path> list = new S3ObjectListService(session).list(new Path(container, "empty", EnumSet.of(Path.Type.directory, Path.Type.placeholder)),
                new DisabledListProgressListener());
        assertTrue(list.isEmpty());
        session.close();
    }

    @Test(expected = NotfoundException.class)
    public void testListNotfound() throws Exception {
        final S3Session session = new S3Session(
                new Host(new S3Protocol(), new S3Protocol().getDefaultHostname(),
                        new Credentials(
                                System.getProperties().getProperty("s3.key"), System.getProperties().getProperty("s3.secret")
                        )));
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final Path container = new Path("notfound.cyberduck.ch", EnumSet.of(Path.Type.volume));
        new S3ObjectListService(session).list(container, new DisabledListProgressListener());
        session.close();
    }

    @Test
    @Ignore
    public void testListCnameAnonymous() throws Exception {
        final Host host = new Host(new S3Protocol(), "dist.springframework.org", new Credentials(
                PreferencesFactory.get().getProperty("connection.login.anon.name"), null
        ));
        final S3Session session = new S3Session(host);
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        final AttributedList<Path> list
                = new S3ObjectListService(session).list(new Path("/dist.springframework.org", EnumSet.of(Path.Type.directory)),
                new DisabledListProgressListener());
        assertFalse(list.isEmpty());
        assertTrue(list.contains(new Path("/dist.springframework.org/release", EnumSet.of(Path.Type.directory))));
        assertTrue(list.contains(new Path("/dist.springframework.org/milestone", EnumSet.of(Path.Type.directory))));
        assertTrue(list.contains(new Path("/dist.springframework.org/snapshot", EnumSet.of(Path.Type.directory))));
        assertTrue(list.contains(new Path("/dist.springframework.org/robots.txt", EnumSet.of(Path.Type.file))));
        session.close();
    }

    @Test
    public void testListBuckenameAnonymous() throws Exception {
        final Host host = new Host(new S3Protocol(), "dist.springframework.org.s3.amazonaws.com", new Credentials(
                PreferencesFactory.get().getProperty("connection.login.anon.name"), null
        ));
        final S3Session session = new S3Session(host);
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        final AttributedList<Path> list
                = new S3ObjectListService(session).list(new Path("/dist.springframework.org", EnumSet.of(Path.Type.directory)),
                new DisabledListProgressListener());
        assertFalse(list.isEmpty());
        assertTrue(list.contains(new Path("/dist.springframework.org/release", EnumSet.of(Path.Type.directory, Path.Type.placeholder))));
        assertTrue(list.contains(new Path("/dist.springframework.org/milestone", EnumSet.of(Path.Type.directory, Path.Type.placeholder))));
        assertTrue(list.contains(new Path("/dist.springframework.org/snapshot", EnumSet.of(Path.Type.directory, Path.Type.placeholder))));
        session.close();
    }

    @Test
    public void testListDefaultPath() throws Exception {
        final Host host = new Host(new S3Protocol(), "dist.springframework.org.s3.amazonaws.com", new Credentials(
                PreferencesFactory.get().getProperty("connection.login.anon.name"), null
        ));
        host.setDefaultPath("/dist.springframework.org/release");
        final S3Session session = new S3Session(host);
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        assertEquals(new Path("/dist.springframework.org/release", EnumSet.of(Path.Type.directory)), new S3HomeFinderService(session).find());
        final AttributedList<Path> list
                = new S3ObjectListService(session).list(new S3HomeFinderService(session).find(), new DisabledListProgressListener());
        assertFalse(list.isEmpty());
        assertTrue(list.contains(new Path("/dist.springframework.org/release/SWF", EnumSet.of(Path.Type.directory, Path.Type.placeholder))));
        session.close();
    }

    @Test
    public void testListVersioning() throws Exception {
        final S3Session session = new S3Session(
                new Host(new S3Protocol(), new S3Protocol().getDefaultHostname(),
                        new Credentials(
                                System.getProperties().getProperty("s3.key"), System.getProperties().getProperty("s3.secret")
                        )));
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final AttributedList<Path> list = new S3ObjectListService(session).list(new Path("versioning-test-us-east-1-cyberduck",
                        EnumSet.of(Path.Type.directory, Path.Type.volume)),
                new DisabledListProgressListener());
        final PathAttributes att = new PathAttributes();
        assertTrue(list.contains(new Path("/versioning-test-us-east-1-cyberduck/test", EnumSet.of(Path.Type.file), att)));
        att.setVersionId("VLphaWnNt9MNseMuYVsLSmCFe6EuJJAq");
        assertTrue(list.contains(new Path("/versioning-test-us-east-1-cyberduck/test", EnumSet.of(Path.Type.file), att)));
        session.close();
    }

    @Test
    public void testListPlaceholder() throws Exception {
        final S3Session session = new S3Session(
                new Host(new S3Protocol(), new S3Protocol().getDefaultHostname(),
                        new Credentials(
                                System.getProperties().getProperty("s3.key"), System.getProperties().getProperty("s3.secret")
                        )));
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final Path container = new Path("test-us-east-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        container.attributes().setRegion("us-east-1");
        final Path placeholder = new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.directory));
        new S3DirectoryFeature(session).mkdir(placeholder);
        placeholder.setType(EnumSet.of(Path.Type.directory, Path.Type.placeholder));
        final AttributedList<Path> list = new S3ObjectListService(session).list(placeholder, new DisabledListProgressListener());
        assertTrue(list.isEmpty());
        new S3DefaultDeleteFeature(session).delete(Collections.singletonList(placeholder), new DisabledLoginCallback(), new Delete.DisabledCallback());
        session.close();
    }

    @Test
    public void testListPlaceholderTilde() throws Exception {
        final S3Session session = new S3Session(
                new Host(new S3Protocol(), new S3Protocol().getDefaultHostname(),
                        new Credentials(
                                System.getProperties().getProperty("s3.key"), System.getProperties().getProperty("s3.secret")
                        )));
        session.setSignatureVersion(S3Protocol.AuthenticationHeaderSignatureVersion.AWS2);
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final Path container = new Path("test-us-east-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        container.attributes().setRegion("us-east-1");
        final Path placeholder = new Path(container, String.format("%s~", UUID.randomUUID().toString()), EnumSet.of(Path.Type.directory));
        new S3DirectoryFeature(session).mkdir(placeholder);
        placeholder.setType(EnumSet.of(Path.Type.directory, Path.Type.placeholder));
        final AttributedList<Path> list = new S3ObjectListService(session).list(placeholder, new DisabledListProgressListener());
        assertTrue(list.isEmpty());
        new S3DefaultDeleteFeature(session).delete(Collections.singletonList(placeholder), new DisabledLoginCallback(), new Delete.DisabledCallback());
        session.close();
    }

    @Test
    public void testListPlaceholderTildeSignatureAWS4() throws Exception {
        final S3Session session = new S3Session(
                new Host(new S3Protocol(), new S3Protocol().getDefaultHostname(),
                        new Credentials(
                                System.getProperties().getProperty("s3.key"), System.getProperties().getProperty("s3.secret")
                        )));
        session.setSignatureVersion(S3Protocol.AuthenticationHeaderSignatureVersion.AWS4HMACSHA256);
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final Path container = new Path("test-us-east-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        container.attributes().setRegion("us-east-1");
        final Path placeholder = new Path(container, String.format("%s~", UUID.randomUUID().toString()), EnumSet.of(Path.Type.directory));
        new S3DirectoryFeature(session).mkdir(placeholder);
        placeholder.setType(EnumSet.of(Path.Type.directory, Path.Type.placeholder));
        final AttributedList<Path> list = new S3ObjectListService(session).list(placeholder, new DisabledListProgressListener());
        assertTrue(list.isEmpty());
        new S3DefaultDeleteFeature(session).delete(Collections.singletonList(placeholder), new DisabledLoginCallback(), new Delete.DisabledCallback());
        session.close();
    }

    @Test
    public void testListPlaceholderAtSignSignatureAWS4() throws Exception {
        final S3Session session = new S3Session(
                new Host(new S3Protocol(), new S3Protocol().getDefaultHostname(),
                        new Credentials(
                                System.getProperties().getProperty("s3.key"), System.getProperties().getProperty("s3.secret")
                        )));
        session.setSignatureVersion(S3Protocol.AuthenticationHeaderSignatureVersion.AWS4HMACSHA256);
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final Path container = new Path("test-us-east-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        container.attributes().setRegion("us-east-1");
        final Path placeholder = new Path(container, String.format("%s@", UUID.randomUUID().toString()), EnumSet.of(Path.Type.directory));
        new S3DirectoryFeature(session).mkdir(placeholder);
        placeholder.setType(EnumSet.of(Path.Type.directory, Path.Type.placeholder));
        final AttributedList<Path> list = new S3ObjectListService(session).list(placeholder, new DisabledListProgressListener());
        assertTrue(list.isEmpty());
        new S3DefaultDeleteFeature(session).delete(Collections.singletonList(placeholder), new DisabledLoginCallback(), new Delete.DisabledCallback());
        session.close();
    }

    @Test
    public void testListAWS4SignatureFrankfurt() throws Exception {
        final S3Session session = new S3Session(
                new Host(new S3Protocol(), new S3Protocol().getDefaultHostname(),
                        new Credentials(
                                System.getProperties().getProperty("s3.key"), System.getProperties().getProperty("s3.secret")
                        ))) {
            @Override
            public S3Protocol.AuthenticationHeaderSignatureVersion getSignatureVersion() {
                return S3Protocol.AuthenticationHeaderSignatureVersion.AWS4HMACSHA256;
            }

        };
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final Path container = new Path("test-eu-central-1-cyberduck", EnumSet.of(Path.Type.volume));
        final AttributedList<Path> list = new S3ObjectListService(session).list(container, new DisabledListProgressListener());
        session.close();
    }

    @Test
    public void testListAWS2AutoSwitchAWS4SignatureFrankfurt() throws Exception {
        final S3Session session = new S3Session(
                new Host(new S3Protocol(), new S3Protocol().getDefaultHostname(),
                        new Credentials(
                                System.getProperties().getProperty("s3.key"), System.getProperties().getProperty("s3.secret")
                        ))) {
            @Override
            public S3Protocol.AuthenticationHeaderSignatureVersion getSignatureVersion() {
                return S3Protocol.AuthenticationHeaderSignatureVersion.AWS2;
            }
        };
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final Path container = new Path("test-eu-central-1-cyberduck", EnumSet.of(Path.Type.volume));
        final AttributedList<Path> list = new S3ObjectListService(session).list(container, new DisabledListProgressListener());
        session.close();
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
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final Path container = new Path("test-eu-central-1-cyberduck", EnumSet.of(Path.Type.volume));
        try {
            final AttributedList<Path> list = new S3ObjectListService(session).list(container, new DisabledListProgressListener());
        }
        catch(BackgroundException e) {
            assertEquals("Listing directory test-eu-central-1-cyberduck failed.", e.getMessage());
            assertEquals("Received redirect response HTTP/1.1 301 Moved Permanently but no location header.", e.getDetail());
            throw e;
        }
        finally {
            session.close();
        }
    }

    @Test
    public void testLaxHostnameVerification() throws Exception {
        final Host host = new Host(new S3Protocol(), new S3Protocol().getDefaultHostname(), new Credentials(
                System.getProperties().getProperty("s3.key"), System.getProperties().getProperty("s3.secret")
        ));
        final KeychainX509TrustManager trust = new KeychainX509TrustManager(new DefaultTrustManagerHostnameCallback(host),
                new DisabledCertificateStore() {
                    @Override
                    public boolean isTrusted(final String hostname, final List<X509Certificate> certificates) {
                        assertEquals("ch.s3.amazonaws.com", hostname);
                        return true;
                    }
                });
        final S3Session session = new S3Session(host, new DisabledX509TrustManager(),
                new KeychainX509KeyManager(host, new DisabledCertificateStore()), new DisabledProxyFinder());
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        new S3ObjectListService(session).list(
                new Path("test-us-east-1-cyberduck", EnumSet.of(Path.Type.volume, Path.Type.directory)), new DisabledListProgressListener());
        session.close();
    }
}
