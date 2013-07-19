package ch.cyberduck.core;

import ch.cyberduck.core.local.FinderLocal;
import ch.cyberduck.core.local.Local;
import ch.cyberduck.core.serializer.Writer;
import ch.cyberduck.core.serializer.impl.HostWriterFactory;

import org.junit.Test;

import java.util.Collection;
import java.util.UUID;

import static org.junit.Assert.*;

/**
 * @version $Id$
 */
public class FolderBookmarkCollectionTest extends AbstractTestCase {

    @Test
    public void testLoad() throws Exception {
        final FinderLocal source = new FinderLocal(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
        final FinderLocal b = new FinderLocal(source, String.format("%s.duck", UUID.randomUUID().toString()));
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
                "\t<string>ftp</string>\n" +
                "\t<key>UUID</key>\n" +
                "\t<string>4d6b034c-8635-4e2f-93b1-7306ba22da22</string>\n" +
                "\t<key>Username</key>\n" +
                "\t<string>anonymous</string>\n" +
                "</dict>\n" +
                "</plist>\n";
        b.touch();
        b.getOutputStream(false).write(bookmark.getBytes("UTF-8"));
        assertTrue(source.exists());
        final FolderBookmarkCollection collection = new FolderBookmarkCollection(source);
        collection.load();
        assertFalse(collection.isEmpty());
        assertEquals(1, collection.size());
        assertEquals("4d6b034c-8635-4e2f-93b1-7306ba22da22", collection.get(0).getUuid());
        assertEquals("4d6b034c-8635-4e2f-93b1-7306ba22da22.duck", collection.getFile(collection.get(0)).getName());
        assertFalse(b.exists());
        collection.getFile(collection.get(0)).delete();
    }

    @Test
    public void testIndex() {
        HostWriterFactory.addFactory(Factory.NATIVE_PLATFORM, new HostWriterFactory() {
            @Override
            protected Writer<Host> create() {
                return new Writer<Host>() {
                    @Override
                    public void write(Collection<Host> collection, Local file) {
                        fail();
                    }

                    @Override
                    public void write(Host item, Local file) {
                        assertNotNull(item.getUuid());
                    }
                };
            }
        });
        FolderBookmarkCollection c = new FolderBookmarkCollection(new NullLocal("", "f")) {
            @Override
            protected void save(Host bookmark) {
                assertNotNull(bookmark.getUuid());
            }
        };
        final Host b = new Host("b");
        c.add(b);
        final Host a = new Host("a");
        c.add(a);
        c.set(0, b);
        c.sort();
        assertEquals(b, c.get(0));
        assertEquals(a, c.get(1));
    }
}
