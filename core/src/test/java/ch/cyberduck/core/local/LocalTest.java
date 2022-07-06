package ch.cyberduck.core.local;

import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.NullFilter;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.LocalAccessDeniedException;
import ch.cyberduck.core.preferences.PreferencesFactory;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.io.File;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.junit.Assert.*;

public class LocalTest {

    @Test
    public void testWriteNewFile() throws Exception {
        final Local file = new DefaultTemporaryFileService().create(new AlphanumericRandomStringService().random());
        final OutputStream out = file.getOutputStream(false);
        out.close();
        file.delete();
    }

    @Test
    public void testWriteExistingFile() throws Exception {
        final Local file = new DefaultTemporaryFileService().create(new AlphanumericRandomStringService().random());
        new DefaultLocalTouchFeature().touch(file);
        final OutputStream out = file.getOutputStream(false);
        out.close();
        file.delete();
    }

    @Test
    public void testList() throws Exception {
        assertFalse(new Local("../profiles").list().isEmpty());
        assertTrue(new Local("../profiles").list(new NullFilter<String>() {
            @Override
            public boolean accept(final String file) {
                return false;
            }
        }).isEmpty());
    }

    @Test(expected = AccessDeniedException.class)
    public void testReadNoFile() throws Exception {
        final String name = UUID.randomUUID().toString();
        TestLocal l = new TestLocal(System.getProperty("java.io.tmpdir") + "/" + name);
        l.getInputStream();
    }

    @Test
    public void testGetParent() {
        assertNotNull(new TestLocal(System.getProperty("java.io.tmpdir")).getParent());
        final TestLocal root = new TestLocal("/");
        assertSame(root, root.getParent());
    }

    @Test
    public void testEqual() {
        assertEquals(new TestLocal("/p/1"), new TestLocal("/p/1"));
        assertNotEquals(new TestLocal("/p/1"), new TestLocal("/p/2"));
        assertNotEquals(new TestLocal("/p/1"), new TestLocal("/P/1"));
    }

    @Test
    public void testHashCode() {
        assertEquals(new TestLocal("/p/1").hashCode(), new TestLocal("/p/1").hashCode());
        assertNotEquals(new TestLocal("/p/1").hashCode(), new TestLocal("/P/1").hashCode());
    }

    @Test
    public void testAttributes() {
        final TestLocal l = new TestLocal("/p/1");
        assertNotNull(l.attributes());
        assertEquals(l.attributes(), l.attributes());
    }

    @Test
    public void testIsDirectory() {
        assertTrue(new TestLocal("../profiles").isDirectory());
        TestLocal l = new TestLocal(System.getProperty("java.io.tmpdir") + "/" + UUID.randomUUID().toString());
        assertFalse(l.isDirectory());
    }

    @Test
    public void testIsFile() {
        assertTrue(new Local("../profiles/pom.xml").isFile());
        TestLocal l = new TestLocal(System.getProperty("java.io.tmpdir") + "/" + UUID.randomUUID().toString());
        assertFalse(l.isFile());
    }

    @Test
    public void testDelimiter() {
        Local l = new WindowsLocal("G:\\");
        assertEquals("G:\\", l.getAbsolute());
        assertEquals("", l.getName());

        l = new WindowsLocal("C:\\path\\relative");
        assertEquals("relative", l.getName());
        assertEquals("C:\\path\\relative", l.getAbsolute());

        l = new WindowsLocal("C:\\path", "cyberduck.log", "\\");
        assertEquals("cyberduck.log", l.getName());
        assertEquals("C:\\path\\cyberduck.log", l.getAbsolute());

        l = new WindowsLocal("C:\\path", "Sessions", "\\");
        assertEquals("Sessions", l.getName());
        assertEquals("C:\\path\\Sessions", l.getAbsolute());
    }

    @Test
    public void testIsChild() {
        TestLocal l1 = new TestLocal("/");
        TestLocal l2 = new TestLocal("/");
        assertFalse(l1.isChild(l2));
        assertFalse(l2.isChild(l1));

        l1 = new TestLocal("/p/1");
        l2 = new TestLocal("/p/1");
        assertFalse(l1.isChild(l2));
        assertFalse(l2.isChild(l1));

        l1 = new TestLocal("/p/1");
        l2 = new TestLocal("/p/2");
        assertFalse(l1.isChild(l2));
        assertFalse(l2.isChild(l1));

        l1 = new TestLocal("/");
        l2 = new TestLocal("/p");
        assertFalse(l1.isChild(l2));
        assertTrue(l2.isChild(l1));

        l1 = new TestLocal("/");
        l2 = new TestLocal("/p/1");
        assertFalse(l1.isChild(l2));
        assertTrue(l2.isChild(l1));

        l1 = new TestLocal("/p/1");
        l2 = new TestLocal("/p/1/2");
        assertFalse(l1.isChild(l2));
        assertTrue(l2.isChild(l1));

        WindowsLocal wl1 = new WindowsLocal("G:\\");
        WindowsLocal wl2 = new WindowsLocal("G:\\");
        assertFalse(wl1.isChild(wl2));
        assertFalse(wl2.isChild(wl1));

        wl1 = new WindowsLocal("G:\\");
        wl2 = new WindowsLocal("G:\\p");
        assertFalse(wl1.isChild(wl2));
        assertTrue(wl2.isChild(wl1));

        wl1 = new WindowsLocal("G:\\");
        wl2 = new WindowsLocal("H:\\p");
        assertFalse(wl1.isChild(wl2));
        assertFalse(wl2.isChild(wl1));

        wl1 = new WindowsLocal("G:\\");
        wl2 = new WindowsLocal("G:\\p\\1");
        assertFalse(wl1.isChild(wl2));
        assertTrue(wl2.isChild(wl1));

        wl1 = new WindowsLocal("G:\\p");
        wl2 = new WindowsLocal("G:\\p\\1\\2");
        assertFalse(wl1.isChild(wl2));
        assertTrue(wl2.isChild(wl1));
    }

    @Test(expected = LocalAccessDeniedException.class)
    public void testRenameExistingDirectory() throws Exception {
        final TestLocal l = new TestLocal(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
        final TestLocal n = new TestLocal(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
        new DefaultLocalDirectoryFeature().mkdir(l);
        n.rename(l);
    }

    @Test
    public void testRenameDirectory() throws Exception {
        final TestLocal l = new TestLocal(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
        final TestLocal n = new TestLocal(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
        new DefaultLocalDirectoryFeature().mkdir(n);
        n.rename(l);
        assertTrue(n.exists());
        assertTrue(l.exists());
        l.delete();
        assertFalse(l.exists());
    }

    @Test
    public void testMoveOverride() throws Exception {
        final TestLocal l = new TestLocal(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
        final TestLocal n = new TestLocal(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
        new DefaultLocalTouchFeature().touch(l);
        new DefaultLocalTouchFeature().touch(n);
        l.rename(n);
        assertTrue(n.exists());
        assertTrue(l.exists());
        n.delete();
    }

    @Test
    public void testOpenInputStream() throws Exception {
        Local l = new TestLocal(String.format("%s/%s", System.getProperty("java.io.tmpdir"), new AlphanumericRandomStringService().random()));
        new DefaultLocalTouchFeature().touch(l);
        assertNotNull(l.getInputStream());
        l.delete();
    }

    @Test
    public void testOpenOutputStream() throws Exception {
        Local l = new TestLocal(String.format("%s/%s", System.getProperty("java.io.tmpdir"), new AlphanumericRandomStringService().random()));
        assertNotNull(l.getOutputStream(false));
        new DefaultLocalTouchFeature().touch(l);
        assertNotNull(l.getOutputStream(false));
        assertNotNull(l.getOutputStream(true));
        l.delete();
    }

    @Test
    public void testFastCopy() throws Exception {
        String a = "TestA";
        String b = "TestB";
        RandomAccessFile writer = new RandomAccessFile("a", "rw");
        FileChannel channel = writer.getChannel();
        channel.write(ByteBuffer.wrap(a.getBytes(StandardCharsets.UTF_8)));
        IOUtils.closeQuietly(channel);
        IOUtils.closeQuietly(writer);

        writer = new RandomAccessFile("b", "rw");
        channel = writer.getChannel();
        channel.write(ByteBuffer.wrap(b.getBytes(StandardCharsets.UTF_8)));
        IOUtils.closeQuietly(channel);
        IOUtils.closeQuietly(writer);

        File file = new File("new");
        file.createNewFile();
        Local l = new Local("new");
        Local newLocal = new Local("a");
        newLocal.copy(l, new Local.CopyOptions().append(true));
        newLocal.delete();
        newLocal = new Local("b");
        newLocal.copy(l, new Local.CopyOptions().append(true));
        newLocal.delete();


        String output = FileUtils.readFileToString(new File("new"), StandardCharsets.UTF_8);
        l.delete();
        assertEquals("TestATestB", output);
    }

    @Test
    public void testNormalize() {
        assertEquals(StringUtils.removeEnd(System.getProperty("java.io.tmpdir"),
            PreferencesFactory.get().getProperty("local.delimiter")), new Local(System.getProperty("java.io.tmpdir")).getAbsolute());
    }

    private static class WindowsLocal extends Local {

        public WindowsLocal(final String parent, final String name, final String delimiter) {
            super(parent, name, delimiter);
        }

        public WindowsLocal(final String name) {
            super(name);
        }

        @Override
        public char getDelimiter() {
            return '\\';
        }
    }

    private final class TestLocal extends Local {
        private TestLocal(final String name) {
            super(name);
        }

        public TestLocal(final String parent, final String name) {
            super(parent, name);
        }
    }
}
