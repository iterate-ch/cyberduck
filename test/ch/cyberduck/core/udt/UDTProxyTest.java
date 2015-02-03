package ch.cyberduck.core.udt;

/*
 * Copyright (c) 2002-2014 David Kocher. All rights reserved.
 * http://cyberduck.io/
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
 * Bug fixes, suggestions and comments should be sent to:
 * feedback@cyberduck.io
 */

import ch.cyberduck.core.*;
import ch.cyberduck.core.aquaticprime.License;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.exception.ConnectionRefusedException;
import ch.cyberduck.core.exception.QuotaException;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.features.Location;
import ch.cyberduck.core.features.Upload;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.DisabledStreamListener;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.s3.S3DefaultDeleteFeature;
import ch.cyberduck.core.s3.S3LocationFeature;
import ch.cyberduck.core.s3.S3Protocol;
import ch.cyberduck.core.s3.S3ReadFeature;
import ch.cyberduck.core.s3.S3Session;
import ch.cyberduck.core.s3.S3SingleUploadService;
import ch.cyberduck.core.s3.S3TouchFeature;
import ch.cyberduck.core.s3.S3WriteFeature;
import ch.cyberduck.core.ssl.AbstractX509TrustManager;
import ch.cyberduck.core.ssl.X509TrustManager;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.udt.qloudsonic.MissingReceiptException;
import ch.cyberduck.core.udt.qloudsonic.QloudsonicProxyProvider;
import ch.cyberduck.core.udt.qloudsonic.QloudsonicTestVoucher;
import ch.cyberduck.core.udt.qloudsonic.QloudsonicTestVoucherFinder;
import ch.cyberduck.core.udt.qloudsonic.QloudsonicVoucherFinder;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

import com.barchart.udt.ExceptionUDT;

import static org.junit.Assert.*;

public class UDTProxyTest extends AbstractTestCase {

    @Test(expected = ConnectionRefusedException.class)
    public void testConnectNoServer() throws Exception {
        final Host host = new Host(new S3Protocol(), "s3.amazonaws.com", new Credentials(
                properties.getProperty("s3.key"), properties.getProperty("s3.secret")
        ));
        final UDTProxy<S3Session> proxy = new UDTProxy<S3Session>(new S3LocationFeature.S3Region("ap-northeast-1"),
                new LocalhostProxyProvider() {
                    @Override
                    public URI find(final Location.Name region) {
                        // No server here
                        return URI.create("udt://test.cyberduck.ch:8007");
                    }
                });
        final Session session = proxy.proxy(new S3Session(host), new DisabledTranscriptListener());
        try {
            assertNotNull(session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener()));
            session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(),
                    PathCache.empty());
        }
        catch(BackgroundException e) {
            final Throwable cause = ExceptionUtils.getRootCause(e);
            if(cause instanceof ExceptionUDT) {
                throw new UDTExceptionMappingService().map((ExceptionUDT) cause);
            }
            throw e;
        }
    }

    @Test(expected = MissingReceiptException.class)
    public void testConnectNoReceipt() throws Exception {
        final Host host = new Host(new S3Protocol(), "s3.amazonaws.com", new Credentials(
                properties.getProperty("s3.key"), properties.getProperty("s3.secret")
        ));
        final UDTProxy<S3Session> proxy = new UDTProxy<S3Session>(new S3LocationFeature.S3Region("ap-northeast-1"),
                new QloudsonicProxyProvider());
        final Session session = proxy.proxy(new S3Session(host), new DisabledTranscriptListener());
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
    }

    @Test(expected = ConnectionCanceledException.class)
    public void testConnectFailureCertificateTls() throws Exception {
        final Host host = new Host(new S3Protocol(), "s3.amazonaws.com", new Credentials(
                properties.getProperty("s3.key"), properties.getProperty("s3.secret")
        ));
        final UDTProxy<S3Session> proxy = new UDTProxy<S3Session>(new S3LocationFeature.S3Region("ap-northeast-1"),
                new QloudsonicProxyProvider(new QloudsonicTestVoucherFinder()), new AbstractX509TrustManager() {
            @Override
            public X509TrustManager init() throws IOException {
                return this;
            }

            @Override
            public void checkClientTrusted(final X509Certificate[] x509Certificates, final String s) throws CertificateException {
                //
            }

            @Override
            public void checkServerTrusted(final X509Certificate[] x509Certificates, final String s) throws CertificateException {
                throw new CertificateException();
            }
        });
        final Session session = proxy.proxy(new S3Session(host), new DisabledTranscriptListener());
        assertNotNull(session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener()));
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(),
                PathCache.empty());
    }

    @Test(expected = QuotaException.class)
    public void testUploadQuotaFailure() throws Exception {
        final Host host = new Host(new S3Protocol(), "s3.amazonaws.com", new Credentials(
                properties.getProperty("s3.key"), properties.getProperty("s3.secret")
        ));
        final UDTProxy<S3Session> proxy = new UDTProxy<S3Session>(new S3LocationFeature.S3Region("ap-northeast-1"),
                new QloudsonicProxyProvider(new QloudsonicVoucherFinder() {
                    @Override
                    public List<License> open() throws AccessDeniedException {
                        return Collections.<License>singletonList(new QloudsonicTestVoucher() {
                            @Override
                            public String getValue(final String property) {
                                return "-" + super.getValue(property);
                            }
                        });
                    }
                }));
        final S3Session session = new S3Session(host);
        proxy.proxy(session, new DisabledTranscriptListener());
        assertNotNull(session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener()));
        assertTrue(session.isConnected());

        final TransferStatus status = new TransferStatus();
        final Local local = new Local(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
        final byte[] content = "test".getBytes("UTF-8");
        final OutputStream out = local.getOutputStream(false);
        IOUtils.write(content, out);
        IOUtils.closeQuietly(out);
        status.setLength(content.length);
        final Path test = new Path(new Path("container", EnumSet.of(Path.Type.volume)),
                UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        final Upload upload = new S3SingleUploadService(session);
        try {
        upload.upload(test, local, new BandwidthThrottle(BandwidthThrottle.UNLIMITED),
                new DisabledStreamListener(), status, new DisabledConnectionCallback());
    }
        catch(QuotaException e) {
            assertEquals("Voucher -u9zTIKCXHTWPO9WA4fBsIaQ5SjEH5von not found. Please contact your web hosting service provider for assistance.", e.getDetail());
            throw e;
        }
    }

    @Test
    public void testWrite() throws Exception {
        final Host host = new Host(new S3Protocol(), "s3.amazonaws.com", new Credentials(
                properties.getProperty("s3.key"), properties.getProperty("s3.secret")
        ));
        final UDTProxy<S3Session> proxy = new UDTProxy<S3Session>(new S3LocationFeature.S3Region("ap-northeast-1"),
                new QloudsonicProxyProvider(new QloudsonicTestVoucherFinder()));
        final S3Session proxied = new S3Session(host);
        proxy.proxy(proxied, new DisabledTranscriptListener());
        assertNotNull(proxied.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener()));
        assertTrue(proxied.isConnected());

        final TransferStatus status = new TransferStatus();
        final Local local = new Local(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());

        final String random = RandomStringUtils.random(1000);
        final OutputStream out = local.getOutputStream(false);
        IOUtils.write(random, out);
        IOUtils.closeQuietly(out);
        status.setLength(random.getBytes().length);

        final Path test = new Path(new Path("test.cyberduck.ch", EnumSet.of(Path.Type.directory, Path.Type.volume)),
                UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        final Upload upload = new S3SingleUploadService(proxied);
        upload.upload(test, local, new BandwidthThrottle(BandwidthThrottle.UNLIMITED),
                new DisabledStreamListener(), status, new DisabledConnectionCallback());
        proxied.close();

        final S3Session session = new S3Session(host);
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());

        assertTrue(session.getFeature(Find.class).find(test));
        assertEquals(status.getLength(), session.list(test.getParent(), new DisabledListProgressListener()).get(test).attributes().getSize(), 0L);
        assertTrue(new S3WriteFeature(session).append(test, status.getLength(), PathCache.empty()).override);
        {
            final byte[] buffer = new byte[random.getBytes().length];
            IOUtils.readFully(new S3ReadFeature(session).read(test, new TransferStatus()), buffer);
            assertArrayEquals(random.getBytes(), buffer);
        }
        {
            final byte[] buffer = new byte[random.getBytes().length - 1];
            final InputStream in = new S3ReadFeature(session).read(test, new TransferStatus().length(random.getBytes().length).append(true).current(1L));
            IOUtils.readFully(in, buffer);
            IOUtils.closeQuietly(in);
            final byte[] reference = new byte[random.getBytes().length - 1];
            System.arraycopy(random.getBytes(), 1, reference, 0, random.getBytes().length - 1);
            assertArrayEquals(reference, buffer);
        }
        new S3DefaultDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new DisabledProgressListener());
        session.close();
        assertFalse(session.isConnected());
    }

    @Test
    public void testReadRange() throws Exception {
        final Host host = new Host(new S3Protocol(), new S3Protocol().getDefaultHostname(), new Credentials(
                properties.getProperty("s3.key"), properties.getProperty("s3.secret")
        ));
        final UDTProxy<S3Session> proxy = new UDTProxy<S3Session>(new S3LocationFeature.S3Region("ap-northeast-1"),
                new QloudsonicProxyProvider(new QloudsonicTestVoucherFinder()));
        final S3Session session = new S3Session(host);
        proxy.proxy(session, new DisabledTranscriptListener());
        assertNotNull(session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener()));
        assertTrue(session.isConnected());

        final Path container = new Path("test.cyberduck.ch", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        new S3TouchFeature(session).touch(test);
        final byte[] content = RandomStringUtils.random(1000).getBytes();
        final OutputStream out = new S3WriteFeature(session).write(test, new TransferStatus().length(content.length));
        assertNotNull(out);
        new StreamCopier(new TransferStatus(), new TransferStatus()).transfer(new ByteArrayInputStream(content), out);
        IOUtils.closeQuietly(out);
        final TransferStatus status = new TransferStatus();
        status.setLength(content.length);
        status.setAppend(true);
        status.setCurrent(100L);
        final InputStream in = new S3ReadFeature(session).read(test, status);
        assertNotNull(in);
        final ByteArrayOutputStream buffer = new ByteArrayOutputStream(content.length - 100);
        new StreamCopier(status, status).transfer(in, buffer);
        final byte[] reference = new byte[content.length - 100];
        System.arraycopy(content, 100, reference, 0, content.length - 100);
        assertArrayEquals(reference, buffer.toByteArray());
        in.close();
        new S3DefaultDeleteFeature(session).delete(Collections.<Path>singletonList(test), new DisabledLoginCallback(), new DisabledProgressListener());
        session.close();
    }
}