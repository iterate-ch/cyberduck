package ch.cyberduck.core;

import ch.cyberduck.core.local.LocalTouchFactory;

import org.junit.Test;

import java.io.OutputStream;
import java.util.UUID;

import static org.junit.Assert.*;

/**
 * @version $Id$
 */
public class FolderBookmarkCollectionTest {

    @Test
    public void testLoad() throws Exception {
        ProtocolFactory.register(new TestProtocol());
        final Local source = new Local(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
        final String uid = "4d6b034c-8635-4e2f-93b1-7306ba22da22";
        final Local b = new Local(source, String.format("%s.duck", uid));
        final String bookmark = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<!DOCTYPE plist PUBLIC \"-//Apple//DTD PLIST 1.0//EN\" \"http://www.apple.com/DTDs/PropertyList-1.0.dtd\">\n" +
                "<plist version=\"1.0\">\n" +
                "<dict>\n" +
                "\t<key>Access Timestamp</key>\n" +
                "\t<string>1296634123295</string>\n" +
                "\t<key>Hostname</key>\n" +
                "\t<string>mirror.switch.ch</string>\n" +
                "\t<key>Nickname</key>\n" +
                "\t<string>mirror.switch.ch – FTP</string>\n" +
                "\t<key>Port</key>\n" +
                "\t<string>21</string>\n" +
                "\t<key>Protocol</key>\n" +
                "\t<string>test</string>\n" +
                "\t<key>UUID</key>\n" +
                "\t<string>" + uid + "</string>\n" +
                "\t<key>Username</key>\n" +
                "\t<string>anonymous</string>\n" +
                "</dict>\n" +
                "</plist>\n";
        LocalTouchFactory.get().touch(b);
        final OutputStream os = b.getOutputStream(false);
        os.write(bookmark.getBytes("UTF-8"));
        os.close();
        assertTrue(source.exists());
        final FolderBookmarkCollection collection = new FolderBookmarkCollection(source);
        collection.load();
        assertFalse(collection.isEmpty());
        assertEquals(1, collection.size());
        assertEquals(uid, collection.get(0).getUuid());
        assertEquals(uid + ".duck", collection.getFile(collection.get(0)).getName());
        collection.getFile(collection.get(0)).delete();
    }

    @Test
    public void testIndex() throws Exception {
        FolderBookmarkCollection c = new FolderBookmarkCollection(new NullLocal("", "f")) {
            @Override
            protected void save(Host bookmark) {
                assertNotNull(bookmark.getUuid());
            }
        };
        final Host d = new Host(new TestProtocol(), "c");
        final Host b = new Host(new TestProtocol(), "b");
        final Host a = new Host(new TestProtocol(), "a");
        c.add(a);
        c.add(b);
        assertEquals(a, c.get(0));
        assertEquals(b, c.get(1));
        c.add(0, d);
        assertEquals(d, c.get(0));
        assertEquals(a, c.get(1));
        assertEquals(b, c.get(2));
    }

    @Test
    public void testMove() throws Exception {
        FolderBookmarkCollection f = new FolderBookmarkCollection(new NullLocal("", "f"));
        final Host a = new Host(new TestProtocol(), "a");
        final Host b = new Host(new TestProtocol(), "b");
        final Host c = new Host(new TestProtocol(), "c");
        f.add(a);
        f.add(b);
        f.add(c);
        f.indexOf(b);

        // Index
        int insert = 2;
        int previous = f.indexOf(b);
        assertEquals(1, previous);
        f.remove(previous);
        assertEquals(2, f.size());
        assertEquals(a, f.get(0));
        assertEquals(c, f.get(1));
        f.add(insert, b);
        assertEquals(3, f.size());
        assertEquals(a, f.get(0));
        assertEquals(c, f.get(1));
        assertEquals(b, f.get(2));
    }
}
