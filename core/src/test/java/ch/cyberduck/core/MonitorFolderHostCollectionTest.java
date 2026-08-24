package ch.cyberduck.core;

/*
 * Copyright (c) 2002-2020 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import ch.cyberduck.core.local.LocalTouchFactory;

import org.junit.BeforeClass;
import org.junit.Test;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

public class MonitorFolderHostCollectionTest {

    @BeforeClass
    public static void register() {
        ProtocolFactory.get().register(new TestProtocol());
    }

    @Test
    public void testFileCreatedEmptyFile() throws Exception {
        final Local source = new Local(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
        final MonitorFolderHostCollection c = new MonitorFolderHostCollection(source);
        c.load();
        assertEquals(0, c.size());
        // Simulate fileCreated event fired before the file content is written (empty file)
        final Local file = new Local(source, UUID.randomUUID() + ".duck");
        LocalTouchFactory.get().touch(file);
        c.fileCreated(file);
        // Parsing must fail silently; no bookmark should be added to the collection
        assertEquals(0, c.size());
    }

    @Test
    public void testFileCreatedTruncatedFile() throws Exception {
        final Local source = new Local(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
        final MonitorFolderHostCollection c = new MonitorFolderHostCollection(source);
        c.load();
        assertEquals(0, c.size());
        // Simulate fileCreated event fired while the file is only partially written (truncated plist)
        final Local file = new Local(source, UUID.randomUUID() + ".duck");
        LocalTouchFactory.get().touch(file);
        final OutputStream os = file.getOutputStream(false);
        os.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<!DOCTYPE plist".getBytes(StandardCharsets.UTF_8));
        os.close();
        c.fileCreated(file);
        // Parsing must fail silently; no bookmark should be added to the collection
        assertEquals(0, c.size());
    }

    @Test
    public void testLoad() throws Exception {
        final Local source = new Local(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
        final MonitorFolderHostCollection c = new MonitorFolderHostCollection(source);
        c.load();
        final Host bookmark = new Host(new TestProtocol());
        c.add(bookmark);
        assertEquals(1, c.size());
        bookmark.setLabels(Collections.singleton("l"));
        c.collectionItemChanged(bookmark);
        assertEquals(1, c.size());
    }
}
