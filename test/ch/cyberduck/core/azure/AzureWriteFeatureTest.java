package ch.cyberduck.core.azure;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Cache;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.DisabledProgressListener;
import ch.cyberduck.core.DisabledTranscriptListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LoginConnectionService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.EnumSet;
import java.util.UUID;

import static org.junit.Assert.*;

/**
 * @version $Id$
 */
public class AzureWriteFeatureTest extends AbstractTestCase {

    @Test
    public void testWrite() throws Exception {
        final Host host = new Host(new AzureProtocol(), "cyberduck.blob.core.windows.net", new Credentials(
                properties.getProperty("azure.account"), properties.getProperty("azure.key")
        ));
        final AzureSession session = new AzureSession(host);
        new LoginConnectionService(new DisabledLoginCallback(), new DisabledHostKeyCallback(),
                new DisabledPasswordStore(), new DisabledProgressListener(), new DisabledTranscriptListener()).connect(session, Cache.<Path>empty());
        final TransferStatus status = new TransferStatus();
        status.setMime("text/plain");
        final byte[] content = "test".getBytes("UTF-8");
        status.setLength(content.length);
        final Path container = new Path("cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new Path(container, UUID.randomUUID().toString() + ".txt", EnumSet.of(Path.Type.file));
        final OutputStream out = new AzureWriteFeature(session).write(test, status);
        assertNotNull(out);
        new StreamCopier(new TransferStatus(), new TransferStatus()).transfer(new ByteArrayInputStream(content), out);
        IOUtils.closeQuietly(out);
        assertTrue(new AzureFindFeature(session).find(test));
        final PathAttributes attributes = session.list(test.getParent(), new DisabledListProgressListener()).get(test.getReference()).attributes();
        assertEquals(content.length, attributes.getSize());
        assertEquals(0L, new AzureWriteFeature(session).append(test, status.getLength(), Cache.<Path>empty()).size, 0L);
        final byte[] buffer = new byte[content.length];
        final InputStream in = new AzureReadFeature(session).read(test, new TransferStatus());
        IOUtils.readFully(in, buffer);
        IOUtils.closeQuietly(in);
        assertArrayEquals(content, buffer);
        new AzureDeleteFeature(session).delete(Collections.<Path>singletonList(test), new DisabledLoginCallback(), new DisabledProgressListener());
        session.close();
    }
}
