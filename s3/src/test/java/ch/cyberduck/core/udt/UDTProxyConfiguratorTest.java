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
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.exception.ConnectionRefusedException;
import ch.cyberduck.core.exception.QuotaException;
import ch.cyberduck.core.features.Delete;
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
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DefaultX509TrustManager;
import ch.cyberduck.core.ssl.X509TrustManager;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.udt.qloudsonic.MissingReceiptException;
import ch.cyberduck.core.udt.qloudsonic.QloudsonicProxyProvider;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

import com.barchart.udt.ExceptionUDT;

import static org.junit.Assert.*;

@Ignore
@Category(IntegrationTest.class)
public class UDTProxyConfiguratorTest {

    @Test(expected = ConnectionRefusedException.class)
    public void testConnectNoServer() throws Exception {
        final Host host = new Host(new S3Protocol(), "s3.amazonaws.com", new Credentials(
                System.getProperties().getProperty("s3.key"), System.getProperties().getProperty("s3.secret")
        ));
        final UDTProxyConfigurator proxy = new UDTProxyConfigurator(new S3LocationFeature.S3Region("ap-northeast-1"),
                new LocalhostProxyProvider() {
                    @Override
                    public Host find(final Location.Name region, final boolean tls) {
                        // No server here
                        return new Host(new UDTProtocol(), "test-us-east-1-cyberduck", Scheme.udt.getPort());
                    }
                }, new DefaultX509TrustManager(), new DefaultX509KeyManager());
        final S3Session tunneled = new S3Session(host);
        proxy.configure(tunneled);
        try {
            assertNotNull(tunneled.open(new DisabledHostKeyCallback()));
            tunneled.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(),
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
                System.getProperties().getProperty("s3.key"), System.getProperties().getProperty("s3.secret")
        ));
        final UDTProxyConfigurator proxy = new UDTProxyConfigurator(new S3LocationFeature.S3Region("ap-northeast-1"),
                new QloudsonicProxyProvider(), new DefaultX509TrustManager(), new DefaultX509KeyManager());
        final S3Session tunneled = new S3Session(host);
        proxy.configure(tunneled);
        tunneled.open(new DisabledHostKeyCallback());
    }

    @Test(expected = ConnectionCanceledException.class)
    public void testConnectFailureCertificateTls() throws Exception {
        final Host host = new Host(new S3Protocol(), "s3.amazonaws.com", new Credentials(
                System.getProperties().getProperty("s3.key"), System.getProperties().getProperty("s3.secret")
        ));
        final UDTProxyConfigurator proxy = new UDTProxyConfigurator(new S3LocationFeature.S3Region("ap-northeast-1"),
                new LocalhostProxyProvider(), new AbstractX509TrustManager() {
            @Override
            public X509TrustManager init() {
                return this;
            }

            @Override
            public void verify(final String hostname, final X509Certificate[] certs, final String cipher) throws CertificateException {
                throw new CertificateException();
            }

            @Override
            public void checkClientTrusted(final X509Certificate[] x509Certificates, final String s) throws CertificateException {
                //
            }

            @Override
            public void checkServerTrusted(final X509Certificate[] x509Certificates, final String s) throws CertificateException {
                throw new CertificateException();
            }
        }, new DefaultX509KeyManager());
        final S3Session tunneled = new S3Session(host);
        proxy.configure(tunneled);
        assertNotNull(tunneled.open(new DisabledHostKeyCallback()));
        tunneled.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(),
                PathCache.empty());
    }

    @Test(expected = QuotaException.class)
    public void testUploadQuotaFailure() throws Exception {
        final Host host = new Host(new S3Protocol(), "s3.amazonaws.com", new Credentials(
                System.getProperties().getProperty("s3.key"), System.getProperties().getProperty("s3.secret")
        ));
        final UDTProxyConfigurator proxy = new UDTProxyConfigurator(new S3LocationFeature.S3Region("ap-northeast-1"),
                new LocalhostProxyProvider() {
                    @Override
                    public List<Header> headers() {
                        final List<Header> headers = new ArrayList<Header>();
                        headers.add(new Header("X-Qloudsonic-Voucher", "-u9zTIKCXHTWPO9WA4fBsIaQ5SjEH5von"));
                        return headers;
                    }
                }, new DefaultX509TrustManager() {
            @Override
            public void checkServerTrusted(final X509Certificate[] certs, final String cipher) throws CertificateException {

            }
        }, new DefaultX509KeyManager());
        final S3Session tunneled = new S3Session(host);
        proxy.configure(tunneled);
        assertNotNull(tunneled.open(new DisabledHostKeyCallback()));
        assertTrue(tunneled.isConnected());

        final TransferStatus status = new TransferStatus();
        final Local local = new Local(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
        final byte[] content = "test".getBytes("UTF-8");
        final OutputStream out = local.getOutputStream(false);
        IOUtils.write(content, out);
        out.close();
        status.setLength(content.length);
        final Path test = new Path(new Path("container", EnumSet.of(Path.Type.volume)),
                UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        final Upload upload = new S3SingleUploadService(tunneled);
        try {
            upload.upload(test, local, new BandwidthThrottle(BandwidthThrottle.UNLIMITED),
                    new DisabledStreamListener(), status, new DisabledConnectionCallback());
        }
        catch(QuotaException e) {
            assertEquals("Voucher -u9zTIKCXHTWPO9WA4fBsIaQ5SjEH5von not found. Request Error. Please contact your web hosting service provider for assistance.", e.getDetail());
            throw e;
        }
    }

    @Test
    public void testUnsecureConnection() throws Exception {
        ProtocolFactory.register(new S3Protocol());
        final Profile profile = ProfileReaderFactory.get().read(
                new Local("../profiles/S3 (HTTP).cyberduckprofile"));
        final Host host = new Host(profile, "s3.amazonaws.com", new Credentials(
                System.getProperties().getProperty("s3.key"), System.getProperties().getProperty("s3.secret")
        ));
        final UDTProxyConfigurator proxy = new UDTProxyConfigurator(new S3LocationFeature.S3Region("ap-northeast-1"),
                new LocalhostProxyProvider(), new DefaultX509TrustManager(), new DefaultX509KeyManager());
        final S3Session tunneled = new S3Session(host);
        proxy.configure(tunneled);
        assertNotNull(tunneled.open(new DisabledHostKeyCallback()));
        tunneled.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(),
                PathCache.empty());
        tunneled.close();
    }

    @Test
    public void testWrite() throws Exception {
        ProtocolFactory.register(new S3Protocol());
        final Profile profile = ProfileReaderFactory.get().read(
                new Local("../profiles/S3 (HTTP).cyberduckprofile"));
        final Host host = new Host(profile, "s3.amazonaws.com", new Credentials(
                System.getProperties().getProperty("s3.key"), System.getProperties().getProperty("s3.secret")
        ));
        final UDTProxyConfigurator proxy = new UDTProxyConfigurator(new S3LocationFeature.S3Region("ap-northeast-1"),
                new LocalhostProxyProvider(), new DefaultX509TrustManager(), new DefaultX509KeyManager());
        final S3Session tunneled = new S3Session(host);
        proxy.configure(tunneled);
        assertNotNull(tunneled.open(new DisabledHostKeyCallback()));
        assertTrue(tunneled.isConnected());
        tunneled.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(),
                PathCache.empty());

        final TransferStatus status = new TransferStatus();
        final Local local = new Local(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());

        final String random = RandomStringUtils.random(1000);
        final OutputStream out = local.getOutputStream(false);
        IOUtils.write(random, out, Charset.defaultCharset());
        out.close();
        status.setLength(random.getBytes().length);

        final Path test = new Path(new Path("test-us-east-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume)),
                UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        final Upload upload = new S3SingleUploadService(tunneled);
        upload.upload(test, local, new BandwidthThrottle(BandwidthThrottle.UNLIMITED),
                new DisabledStreamListener(), status, new DisabledConnectionCallback());

        assertTrue(tunneled.getFeature(Find.class).find(test));
        assertEquals(status.getLength(), tunneled.list(test.getParent(), new DisabledListProgressListener()).get(test).attributes().getSize(), 0L);
        assertTrue(new S3WriteFeature(tunneled).append(test, status.getLength(), PathCache.empty()).override);
        {
            final byte[] buffer = new byte[random.getBytes().length];
            IOUtils.readFully(new S3ReadFeature(tunneled).read(test, new TransferStatus()), buffer);
            assertArrayEquals(random.getBytes(), buffer);
        }
        {
            final byte[] buffer = new byte[random.getBytes().length - 1];
            final InputStream in = new S3ReadFeature(tunneled).read(test, new TransferStatus().length(random.getBytes().length).append(true).skip(1L));
            IOUtils.readFully(in, buffer);
            in.close();
            final byte[] reference = new byte[random.getBytes().length - 1];
            System.arraycopy(random.getBytes(), 1, reference, 0, random.getBytes().length - 1);
            assertArrayEquals(reference, buffer);
        }
        new S3DefaultDeleteFeature(tunneled).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
        tunneled.close();
        assertFalse(tunneled.isConnected());
    }

    @Test
    public void testReadRange() throws Exception {
        final Host host = new Host(new S3Protocol(), new S3Protocol().getDefaultHostname(), new Credentials(
                System.getProperties().getProperty("s3.key"), System.getProperties().getProperty("s3.secret")
        ));
        final UDTProxyConfigurator proxy = new UDTProxyConfigurator(new S3LocationFeature.S3Region("ap-northeast-1"),
                new LocalhostProxyProvider(), new DefaultX509TrustManager() {
            @Override
            public void checkServerTrusted(final X509Certificate[] certs, final String cipher) throws CertificateException {
                //
            }
        }, new DefaultX509KeyManager());
        final S3Session tunneled = new S3Session(host);
        proxy.configure(tunneled);
        assertNotNull(tunneled.open(new DisabledHostKeyCallback()));
        assertTrue(tunneled.isConnected());
        tunneled.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(),
                PathCache.empty());

        final Path container = new Path("test-us-east-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        new S3TouchFeature(tunneled).touch(test);
        final byte[] content = RandomStringUtils.random(1000).getBytes();
        final OutputStream out = new S3WriteFeature(tunneled).write(test, new TransferStatus().length(content.length));
        assertNotNull(out);
        new StreamCopier(new TransferStatus(), new TransferStatus()).transfer(new ByteArrayInputStream(content), out);
        out.close();
        final TransferStatus status = new TransferStatus();
        status.setLength(content.length);
        status.setAppend(true);
        status.setOffset(100L);
        final InputStream in = new S3ReadFeature(tunneled).read(test, status);
        assertNotNull(in);
        final ByteArrayOutputStream buffer = new ByteArrayOutputStream(content.length - 100);
        new StreamCopier(status, status).transfer(in, buffer);
        final byte[] reference = new byte[content.length - 100];
        System.arraycopy(content, 100, reference, 0, content.length - 100);
        assertArrayEquals(reference, buffer.toByteArray());
        in.close();
        new S3DefaultDeleteFeature(tunneled).delete(Collections.<Path>singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
        tunneled.close();
    }
}