package ch.cyberduck.core;

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

import ch.cyberduck.core.local.LocalTouchFactory;

import org.junit.BeforeClass;
import org.junit.Test;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.*;

public class BookmarkCollectionTest {

    @BeforeClass
    public static void register() {
        ProtocolFactory.get().register(new TestProtocol());
    }

    @Test
    public void testDefault() {
        assertNotNull(BookmarkCollection.defaultCollection());
    }

    @Test
    public void testLoad() throws Exception {
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
        os.write(bookmark.getBytes(StandardCharsets.UTF_8));
        os.close();
        assertTrue(source.exists());
        final BookmarkCollection collection = new BookmarkCollection(source);
        collection.load();
        assertFalse(collection.isEmpty());
        assertEquals(1, collection.size());
        assertEquals(uid, collection.get(0).getUuid());
        assertEquals(uid + ".duck", collection.getFile(collection.get(0)).getName());
        collection.getFile(collection.get(0)).delete();
    }

    @Test
    public void testLoadAwaitFilesystemChange() throws Exception {
        final Local source = new Local(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
        final CountDownLatch wait = new CountDownLatch(1);
        final BookmarkCollection collection = new BookmarkCollection(source) {
            @Override
            public void collectionItemAdded(final Host bookmark) {
                super.collectionItemAdded(bookmark);
                wait.countDown();
            }
        };
        collection.load();
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
        final OutputStream os = b.getOutputStream(false);
        os.write(bookmark.getBytes(StandardCharsets.UTF_8));
        os.close();
        assertTrue(source.exists());
        wait.await();
        assertFalse(collection.isEmpty());
        assertEquals(1, collection.size());
        assertEquals(uid, collection.get(0).getUuid());
        assertEquals(uid + ".duck", collection.getFile(collection.get(0)).getName());
        collection.getFile(collection.get(0)).delete();
    }

    @Test
    public void testIndex() {
        BookmarkCollection c = new BookmarkCollection(new NullLocal("", "f")) {
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
    public void testMove() {
        BookmarkCollection f = new BookmarkCollection(new NullLocal("", "f"));
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

    @Test
    public void testFind() {
        BookmarkCollection bookmarks = new BookmarkCollection(new NullLocal("", "f"));
        final Host a = new Host(new TestProtocol(), "a", new Credentials("a"));
        final Host b = new Host(new TestProtocol(), "b", new Credentials("b"));
        bookmarks.add(a);
        bookmarks.add(b);
        {
            final Host input = new Host(new TestProtocol(), "a", new Credentials("a"));
            assertEquals(a, bookmarks.stream().filter(h -> h.compareTo(input) == 0).findFirst()
                // Matching profile
                .orElse(bookmarks.stream().filter(h -> Objects.equals(h.getProtocol(), input.getProtocol()) && Objects.equals(h.getHostname(), input.getHostname())).findFirst()
                    // Matching parent protocol
                    .orElse(bookmarks.stream().filter(h -> Objects.equals(h.getProtocol().getIdentifier(), input.getProtocol().getIdentifier()) && Objects.equals(h.getHostname(), input.getHostname())).findFirst()
                        .orElse(null)
                    )
                ));
        }
        {
            final Host input = new Host(new TestProtocol(), "b", new Credentials("b"));
            assertEquals(b, bookmarks.stream().filter(h -> h.compareTo(input) == 0).findFirst()
                // Matching profile
                .orElse(bookmarks.stream().filter(h -> Objects.equals(h.getProtocol(), input.getProtocol()) && Objects.equals(h.getHostname(), input.getHostname())).findFirst()
                    // Matching parent protocol
                    .orElse(bookmarks.stream().filter(h -> Objects.equals(h.getProtocol().getIdentifier(), input.getProtocol().getIdentifier()) && Objects.equals(h.getHostname(), input.getHostname())).findFirst()
                        .orElse(null)
                    )
                ));
        }
        {
            final Host input = new Host(new TestProtocol(), "a");
            assertEquals(a, bookmarks.stream().filter(h -> h.compareTo(input) == 0).findFirst()
                // Matching profile
                .orElse(bookmarks.stream().filter(h -> Objects.equals(h.getProtocol(), input.getProtocol()) && Objects.equals(h.getHostname(), input.getHostname())).findFirst()
                    // Matching parent protocol
                    .orElse(bookmarks.stream().filter(h -> Objects.equals(h.getProtocol().getIdentifier(), input.getProtocol().getIdentifier()) && Objects.equals(h.getHostname(), input.getHostname())).findFirst()
                        .orElse(null)
                    )
                ));
        }
    }
}
