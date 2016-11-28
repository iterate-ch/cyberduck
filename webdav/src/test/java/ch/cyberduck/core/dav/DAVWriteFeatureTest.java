package ch.cyberduck.core.dav;

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.http.HttpUploadFeature;
import ch.cyberduck.core.http.RedirectCallback;
import ch.cyberduck.core.http.ResponseOutputStream;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.DisabledStreamListener;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.shared.DefaultAttributesFinderFeature;
import ch.cyberduck.core.shared.DefaultHomeFinderService;
import ch.cyberduck.core.shared.DefaultTouchFeature;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.EnumSet;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class DAVWriteFeatureTest {

    @Test
    public void testReadWrite() throws Exception {
        final Host host = new Host(new DAVProtocol(), "test.cyberduck.ch", new Credentials(
                System.getProperties().getProperty("webdav.user"), System.getProperties().getProperty("webdav.password")
        ));
        host.setDefaultPath("/dav/basic");
        final DAVSession session = new DAVSession(host);
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final TransferStatus status = new TransferStatus();
        final Local local = new Local(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
        final byte[] content = "test".getBytes("UTF-8");
        final OutputStream out = local.getOutputStream(false);
        IOUtils.write(content, out);
        out.close();
        status.setLength(content.length);
        final Path test = new Path(new DefaultHomeFinderService(session).find(), UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        final HttpUploadFeature upload = new DAVUploadFeature(session);
        upload.upload(test, local, new BandwidthThrottle(BandwidthThrottle.UNLIMITED),
                new DisabledStreamListener(), status, new DisabledConnectionCallback());
        assertTrue(session.getFeature(Find.class).find(test));
        assertEquals(content.length, session.list(test.getParent(), new DisabledListProgressListener()).get(test).attributes().getSize(), 0L);
        assertEquals(content.length, new DAVWriteFeature(session).append(test, status.getLength(), PathCache.empty()).size, 0L);
        {
            final byte[] buffer = new byte[content.length];
            IOUtils.readFully(new DAVReadFeature(session).read(test, new TransferStatus()), buffer);
            assertArrayEquals(content, buffer);
        }
        {
            final byte[] buffer = new byte[content.length - 1];
            final InputStream in = new DAVReadFeature(session).read(test, new TransferStatus().length(content.length - 1L).append(true).skip(1L));
            IOUtils.readFully(in, buffer);
            in.close();
            final byte[] reference = new byte[content.length - 1];
            System.arraycopy(content, 1, reference, 0, content.length - 1);
            assertArrayEquals(reference, buffer);
        }
        new DAVDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
        session.close();
    }

    @Test
    public void testWriteContentRange() throws Exception {
        final Host host = new Host(new DAVProtocol(), "test.cyberduck.ch", new Credentials(
                System.getProperties().getProperty("webdav.user"), System.getProperties().getProperty("webdav.password")
        ));
        host.setDefaultPath("/dav/basic");
        final DAVSession session = new DAVSession(host);
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final DAVWriteFeature feature = new DAVWriteFeature(session);
        final Path test = new Path("/dav/basic/" + UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        final byte[] content = RandomUtils.nextBytes(64000);
        {
            final TransferStatus status = new TransferStatus();
            status.setOffset(0L);
            status.setLength(1024L);
            final ResponseOutputStream<String> out = feature.write(test, status);
            // Write first 1024
            new StreamCopier(status, status).withOffset(status.getOffset()).withLimit(status.getLength()).transfer(new ByteArrayInputStream(content), out);
            out.close();
        }
        assertTrue(new DAVFindFeature(session).find(test));
        assertEquals(1024L, new DefaultAttributesFinderFeature(session).find(test).getSize());
        {
            // Remaining chunked transfer with offset
            final TransferStatus status = new TransferStatus();
            status.setLength(content.length - 1024L);
            status.setOffset(1024L);
            status.setAppend(true);
            final ResponseOutputStream<String> out = feature.write(test, status);
            new StreamCopier(status, status).withOffset(status.getOffset()).withLimit(status.getLength()).transfer(new ByteArrayInputStream(content), out);
            out.close();
        }
        final ByteArrayOutputStream out = new ByteArrayOutputStream(content.length);
        IOUtils.copy(new DAVReadFeature(session).read(test, new TransferStatus().length(content.length)), out);
        assertArrayEquals(content, out.toByteArray());
        new DAVDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testWriteRangeEndFirst() throws Exception {
        final Host host = new Host(new DAVProtocol(), "test.cyberduck.ch", new Credentials(
                System.getProperties().getProperty("webdav.user"), System.getProperties().getProperty("webdav.password")
        ));
        host.setDefaultPath("/dav/basic");
        final DAVSession session = new DAVSession(host);
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final DAVWriteFeature feature = new DAVWriteFeature(session);
        final Path test = new Path("/dav/basic/" + UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        final byte[] content = RandomUtils.nextBytes(2048);
        {
            // Write end of file first
            final TransferStatus status = new TransferStatus();
            status.setLength(1024L);
            status.setOffset(1024L);
            status.setAppend(true);
            final ResponseOutputStream<String> out = feature.write(test, status);
            new StreamCopier(status, status).withOffset(status.getOffset()).withLimit(status.getLength()).transfer(new ByteArrayInputStream(content), out);
            out.close();
        }
        assertTrue(new DAVFindFeature(session).find(test));
        assertEquals(content.length, new DefaultAttributesFinderFeature(session).find(test).getSize());
        {
            // Write beginning of file up to the last chunk
            final TransferStatus status = new TransferStatus();
            status.setOffset(0L);
            status.setLength(1024L);
            status.setAppend(true);
            final ResponseOutputStream<String> out = feature.write(test, status);
            new StreamCopier(status, status).withOffset(status.getOffset()).withLimit(status.getLength()).transfer(new ByteArrayInputStream(content), out);
            out.close();
        }
        final ByteArrayOutputStream out = new ByteArrayOutputStream(content.length);
        IOUtils.copy(new DAVReadFeature(session).read(test, new TransferStatus().length(content.length)), out);
        assertArrayEquals(content, out.toByteArray());
        assertTrue(new DAVFindFeature(session).find(test));
        assertEquals(content.length, new DefaultAttributesFinderFeature(session).find(test).getSize());
        final byte[] buffer = new byte[content.length];
        final InputStream in = new DAVReadFeature(session).read(test, new TransferStatus().length(content.length));
        IOUtils.readFully(in, buffer);
        in.close();
        assertArrayEquals(content, buffer);
        new DAVDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testWriteContentRangeTwoBytes() throws Exception {
        final Host host = new Host(new DAVProtocol(), "test.cyberduck.ch", new Credentials(
                System.getProperties().getProperty("webdav.user"), System.getProperties().getProperty("webdav.password")
        ));
        host.setDefaultPath("/dav/basic");
        final DAVSession session = new DAVSession(host);
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final DAVWriteFeature feature = new DAVWriteFeature(session);
        final Path test = new Path("/dav/basic/" + UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        final byte[] source = RandomUtils.nextBytes(2);
        {
            final TransferStatus status = new TransferStatus();
            status.setLength(1L);
            status.setOffset(0L);
            final ResponseOutputStream<String> out = feature.write(test, status);
            new StreamCopier(status, status).withOffset(status.getOffset()).withLimit(status.getLength()).transfer(new ByteArrayInputStream(source), out);
            out.close();
        }
        {
            final TransferStatus status = new TransferStatus();
            status.setLength(1L);
            status.setOffset(1L);
            status.setAppend(true);
            final ResponseOutputStream<String> out = feature.write(test, status);
            new StreamCopier(status, status).withOffset(status.getOffset()).withLimit(status.getLength()).transfer(new ByteArrayInputStream(source), out);
            out.close();
        }
        final ByteArrayOutputStream out = new ByteArrayOutputStream(source.length);
        IOUtils.copy(new DAVReadFeature(session).read(test, new TransferStatus().length(source.length)), out);
        assertArrayEquals(source, out.toByteArray());
        new DAVDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testWriteContentRangeThreeBytes() throws Exception {
        final Host host = new Host(new DAVProtocol(), "test.cyberduck.ch", new Credentials(
                System.getProperties().getProperty("webdav.user"), System.getProperties().getProperty("webdav.password")
        ));
        host.setDefaultPath("/dav/basic");
        final DAVSession session = new DAVSession(host);
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final DAVWriteFeature feature = new DAVWriteFeature(session);
        final Path test = new Path("/dav/basic/" + UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        final byte[] source = RandomUtils.nextBytes(3);
        {
            final TransferStatus status = new TransferStatus();
            status.setLength(1L);
            status.setOffset(0L);
            final ResponseOutputStream<String> out = feature.write(test, status);
            new StreamCopier(status, status).withOffset(status.getOffset()).withLimit(status.getLength()).transfer(new ByteArrayInputStream(source), out);
            out.close();
        }
        {
            final TransferStatus status = new TransferStatus();
            status.setLength(2L);
            status.setOffset(1L);
            status.setAppend(true);
            final ResponseOutputStream<String> out = feature.write(test, status);
            new StreamCopier(status, status).withOffset(status.getOffset()).withLimit(status.getLength()).transfer(new ByteArrayInputStream(source), out);
            out.close();
        }
        final ByteArrayOutputStream out = new ByteArrayOutputStream(source.length);
        IOUtils.copy(new DAVReadFeature(session).read(test, new TransferStatus().length(source.length)), out);
        assertArrayEquals(source, out.toByteArray());
        new DAVDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test(expected = AccessDeniedException.class)
    public void testWriteZeroBytesAccessDenied() throws Exception {
        final Host host = new Host(new DAVProtocol(), "test.cyberduck.ch", new Credentials(
                System.getProperties().getProperty("webdav.user"), System.getProperties().getProperty("webdav.password")
        ));
        host.setDefaultPath("/dav/basic");
        final DAVSession session = new DAVSession(host);
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final Path test = new Path(new DefaultHomeFinderService(session).find().getAbsolute() + "/nosuchdirectory/" + UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        final ResponseOutputStream<String> write = new DAVWriteFeature(session).write(test, new TransferStatus());
        try {
            write.close();
            write.getResponse();
        }
        catch(IOException e) {
            throw (Exception) e.getCause();
        }
    }

    @Test(expected = AccessDeniedException.class)
    public void testWriteAccessDenied() throws Exception {
        final Host host = new Host(new DAVProtocol(), "test.cyberduck.ch", new Credentials(
                System.getProperties().getProperty("webdav.user"), System.getProperties().getProperty("webdav.password")
        ));
        host.setDefaultPath("/dav/basic");
        final DAVSession session = new DAVSession(host);
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final Path test = new Path(new DefaultHomeFinderService(session).find().getAbsolute() + "/nosuchdirectory/" + UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        // With Expect: Continue header
        new DAVWriteFeature(session).write(test, new TransferStatus().length(1L));
    }

    @Test
    public void testAppend() throws Exception {
        final Host host = new Host(new DAVProtocol(), "test.cyberduck.ch", new Credentials(
                System.getProperties().getProperty("webdav.user"), System.getProperties().getProperty("webdav.password")
        ));
        host.setDefaultPath("/dav/basic");
        final DAVSession session = new DAVSession(host);
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final DAVWriteFeature feature = new DAVWriteFeature(session);
        assertFalse(feature.append(
                new Path(new DefaultHomeFinderService(session).find(), UUID.randomUUID().toString(), EnumSet.of(Path.Type.file)), 0L, PathCache.empty()).append);
        final Path test = new Path(new DefaultHomeFinderService(session).find(), UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        new DefaultTouchFeature(session).touch(test);
        assertTrue(feature.append(test, 0L, PathCache.empty()).append);
        new DAVDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testUploadRedirect() throws Exception {
        final Host host = new Host(new DAVProtocol(), "test.cyberduck.ch", new Credentials(
                System.getProperties().getProperty("webdav.user"), System.getProperties().getProperty("webdav.password")
        ));
        host.setDefaultPath("/dav/basic");
        final AtomicBoolean redirected = new AtomicBoolean();
        final DAVSession session = new DAVSession(host, new RedirectCallback() {
            @Override
            public boolean redirect(String method) {
                if("PUT".equals(method)) {
                    redirected.set(true);
                }
                return true;
            }
        });
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final DAVWriteFeature feature = new DAVWriteFeature(session);
        final String name = UUID.randomUUID().toString();
        final Path test = new Path(String.format("/redir-tmp/%s", name), EnumSet.of(Path.Type.file));
        final TransferStatus status = new TransferStatus();
        final byte[] content = RandomUtils.nextBytes(1024);
        status.setLength(content.length);
        final ResponseOutputStream<String> out = feature.write(test, status);
        assertNotNull(out);
        new StreamCopier(status, status).transfer(new ByteArrayInputStream(content), out);
        out.close();
        assertEquals(content.length, status.getOffset());
        assertTrue(status.isComplete());
        assertEquals(content.length, new DefaultAttributesFinderFeature(session).find(test).getSize());
        assertEquals(content.length, new DAVAttributesFinderFeature(session).find(test).getSize());
        assertTrue(redirected.get());
        new DAVDeleteFeature(session).delete(Collections.singletonList(
                new Path(new DefaultHomeFinderService(session).find(), name, EnumSet.of(Path.Type.file))
        ), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
