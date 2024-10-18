package ch.cyberduck.core.dav;

import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledProgressListener;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.DisabledStreamListener;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.shared.DefaultDownloadFeature;
import ch.cyberduck.core.shared.DefaultHomeFinderService;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.CountingInputStream;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class DAVReadFeatureTest extends AbstractDAVTest {

    @Test(expected = NotfoundException.class)
    public void testReadNotFound() throws Exception {
        final TransferStatus status = new TransferStatus();
        try {
            new DAVReadFeature(session).read(new Path(new DefaultHomeFinderService(session).find(), "nosuchname", EnumSet.of(Path.Type.file)), status, new DisabledConnectionCallback());
        }
        catch(NotfoundException e) {
            assertTrue(StringUtils.startsWith(e.getDetail(), "Unexpected response"));
            throw e;
        }
    }

    @Test
    public void testReadChunkedTransfer() throws Exception {
        final Path test = new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final Local local = new Local(System.getProperty("java.io.tmpdir"), new AlphanumericRandomStringService().random());
        final byte[] content = RandomUtils.nextBytes(923);
        final OutputStream out = local.getOutputStream(false);
        assertNotNull(out);
        IOUtils.write(content, out);
        out.close();
        new DAVUploadFeature(session).upload(
                test, local, new BandwidthThrottle(BandwidthThrottle.UNLIMITED), new DisabledProgressListener(), new DisabledStreamListener(),
                new TransferStatus().withLength(content.length),
                new DisabledConnectionCallback());
        // Unknown length in status
        final TransferStatus status = new TransferStatus() {
            @Override
            public void setLength(long length) {
                assertEquals(923L, length);
                // Ignore update. As with unknown length for chunked transfer
            }
        };
        new DefaultDownloadFeature(session.getFeature(Read.class)).download(test, local, new BandwidthThrottle(BandwidthThrottle.UNLIMITED),
            new DisabledStreamListener(), status, new DisabledLoginCallback());
        assertEquals(923L, local.attributes().getSize());
        new DAVDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testReadInterrupt() throws Exception {
        final Path test = new DAVTouchFeature(session).touch(new Path(new DefaultHomeFinderService(session).find(),
                new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        // Unknown length in status
        final TransferStatus status = new TransferStatus();
        // Read a single byte
        {
            final InputStream in = new DAVReadFeature(session).read(test, status, new DisabledConnectionCallback());
            assertNotNull(in.read());
            in.close();
        }
        {
            final InputStream in = new DAVReadFeature(session).read(test, status, new DisabledConnectionCallback());
            assertNotNull(in);
            in.close();
        }
        new DAVDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testReadRange() throws Exception {
        final Path test = new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final Local local = new Local(System.getProperty("java.io.tmpdir"), new AlphanumericRandomStringService().random());
        final byte[] content = RandomUtils.nextBytes(1023);
        final OutputStream out = local.getOutputStream(false);
        assertNotNull(out);
        IOUtils.write(content, out);
        out.close();
        new DAVUploadFeature(session).upload(
                test, local, new BandwidthThrottle(BandwidthThrottle.UNLIMITED), new DisabledProgressListener(), new DisabledStreamListener(),
                new TransferStatus().withLength(content.length),
                new DisabledConnectionCallback());
        final TransferStatus status = new TransferStatus();
        status.setLength(content.length);
        status.setAppend(true);
        status.setOffset(100L);
        final InputStream in = new DAVReadFeature(session).read(test, status.withLength(content.length - 100), new DisabledConnectionCallback());
        assertNotNull(in);
        final ByteArrayOutputStream buffer = new ByteArrayOutputStream(content.length - 100);
        new StreamCopier(status, status).transfer(in, buffer);
        final byte[] reference = new byte[content.length - 100];
        System.arraycopy(content, 100, reference, 0, content.length - 100);
        assertArrayEquals(reference, buffer.toByteArray());
        in.close();
        new DAVDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testReadRangeUnknownLength() throws Exception {
        final Path test = new DAVTouchFeature(session).touch(new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        final Local local = new Local(System.getProperty("java.io.tmpdir"), new AlphanumericRandomStringService().random());
        final byte[] content = RandomUtils.nextBytes(1023);
        final OutputStream out = local.getOutputStream(false);
        assertNotNull(out);
        IOUtils.write(content, out);
        out.close();
        new DAVUploadFeature(session).upload(
                test, local, new BandwidthThrottle(BandwidthThrottle.UNLIMITED), new DisabledProgressListener(), new DisabledStreamListener(),
                new TransferStatus().withLength(content.length),
                new DisabledConnectionCallback());
        final TransferStatus status = new TransferStatus();
        status.setLength(-1L);
        status.setAppend(true);
        status.setOffset(100L);
        final InputStream in = new DAVReadFeature(session).read(test, status, new DisabledConnectionCallback());
        assertNotNull(in);
        final ByteArrayOutputStream buffer = new ByteArrayOutputStream(content.length - 100);
        new StreamCopier(status, status).transfer(in, buffer);
        final byte[] reference = new byte[content.length - 100];
        System.arraycopy(content, 100, reference, 0, content.length - 100);
        assertArrayEquals(reference, buffer.toByteArray());
        in.close();
        new DAVDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testReadCloseReleaseEntity() throws Exception {
        final Path test = new DAVTouchFeature(session).touch(new Path(new DefaultHomeFinderService(session).find(),
                new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        final TransferStatus status = new TransferStatus();
        final CountingInputStream in = new CountingInputStream(new DAVReadFeature(session).read(test, status, new DisabledConnectionCallback()));
        in.close();
        assertEquals(0L, in.getByteCount(), 0L);
        new DAVDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
