package ch.cyberduck.core.azure;

import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.DisabledProgressListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LoginConnectionService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.io.DisabledStreamListener;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.shared.DefaultFindFeature;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.lang3.RandomUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.ByteArrayInputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.EnumSet;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class AzureCopyFeatureTest extends AbstractAzureTest {

    @Test
    public void testCopy() throws Exception {
        final Path container = new Path("cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new AzureTouchFeature(session, null).touch(
                new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        Thread.sleep(1000L);
        final Path copy = new AzureCopyFeature(session, null).copy(test,
                new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus(), new DisabledConnectionCallback(), new DisabledStreamListener());
        assertEquals(test.attributes().getChecksum(), copy.attributes().getChecksum());
        assertNotEquals(test.attributes().getModificationDate(), copy.attributes().getModificationDate());
        assertTrue(new AzureFindFeature(session, null).find(test));
        assertTrue(new AzureFindFeature(session, null).find(copy));
        new AzureDeleteFeature(session, null).delete(Arrays.asList(test, copy), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    @Ignore
    public void testCopySharedAccessSignature() throws Exception {
        final Host host = new Host(new AzureProtocol() {
            @Override
            public boolean isUsernameConfigurable() {
                return false;
            }

            @Override
            public boolean isPasswordConfigurable() {
                return false;
            }

            @Override
            public boolean isTokenConfigurable() {
                return true;
            }
        }, "kahy9boj3eib.blob.core.windows.net", new Credentials(
                null, null, "?sv=2017-07-29&ss=bfqt&srt=sco&sp=rwdlacup&se=2030-05-20T04:29:30Z&st=2018-05-09T20:29:30Z&spr=https&sig=bMKAZ3tXmX%2B56%2Bb5JhHAeWnMOpMp%2BoYlHDIAZVAjHzE%3D"));
        final AzureSession session = new AzureSession(host);
        new LoginConnectionService(new DisabledLoginCallback(), new DisabledHostKeyCallback(),
                new DisabledPasswordStore(), new DisabledProgressListener()).connect(session, new DisabledCancelCallback());
        final Path container = new Path("cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new AzureTouchFeature(session, null).touch(test, new TransferStatus());
        final Path copy = new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new AzureCopyFeature(session, null).copy(test, copy, new TransferStatus(), new DisabledConnectionCallback(), new DisabledStreamListener());
        assertTrue(new AzureFindFeature(session, null).find(test));
        assertTrue(new AzureFindFeature(session, null).find(copy));
        new AzureDeleteFeature(session, null).delete(Arrays.asList(test, copy), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testCopyToExistingFile() throws Exception {
        final Path container = new Path("cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path folder = new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        new AzureDirectoryFeature(session, null).mkdir(folder, new TransferStatus());
        final Path test = new AzureTouchFeature(session, null).touch(
                new Path(folder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        final byte[] content = RandomUtils.nextBytes(1023);
        final OutputStream out = new AzureWriteFeature(session, null).write(test, new TransferStatus().withLength(content.length), new DisabledConnectionCallback());
        new StreamCopier(new TransferStatus(), new TransferStatus()).transfer(new ByteArrayInputStream(content), out);
        final Path copy = new AzureTouchFeature(session, null).touch(
                new Path(folder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        assertNotEquals(copy.attributes(), new AzureCopyFeature(session, null).copy(test, copy, new TransferStatus().exists(true), new DisabledConnectionCallback(), new DisabledStreamListener()).attributes());
        assertEquals(1023L, copy.attributes().getSize());
        final Find find = new DefaultFindFeature(session);
        assertTrue(find.find(test));
        assertTrue(find.find(copy));
        new AzureDeleteFeature(session, null).delete(Arrays.asList(test, copy), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
