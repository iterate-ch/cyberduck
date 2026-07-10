package ch.cyberduck.core.io.watchservice;

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

import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.LocalFactory;
import ch.cyberduck.core.local.DisabledFileWatcherListener;
import ch.cyberduck.core.local.FileWatcher;
import ch.cyberduck.core.local.FileWatcherListener;
import ch.cyberduck.core.local.LocalTouchFactory;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.Watchable;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static java.nio.file.StandardWatchEventKinds.*;
import static org.junit.Assert.*;

public class NIOEventWatchServiceTest {

    @Test(expected = IOException.class)
    public void testNotfound() throws Exception {
        final FileWatcher watcher = new FileWatcher(new NIOEventWatchService());
        final Local file = new Local(System.getProperty("java.io.tmpdir") + "/notfound", UUID.randomUUID().toString());
        assertFalse(file.exists());
        watcher.register(file.getParent(), new FileWatcher.DefaultFileFilter(file), new DisabledFileWatcherListener());
    }

    @Test
    public void testRegister() throws Exception {
        final RegisterWatchService fs = new NIOEventWatchService();
        final Watchable folder = Paths.get(
            File.createTempFile(UUID.randomUUID().toString(), "t").getParent());
        final WatchKey key = fs.register(folder, new WatchEvent.Kind[]{ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY});
        assertTrue(key.isValid());
        fs.close();
        assertFalse(key.isValid());
    }

    @Test
    public void testListenerEventWatchCanonicalPath() throws Exception {
        final FileWatcher watcher = new FileWatcher(new NIOEventWatchService());
        final Local file = LocalFactory.get(LocalFactory.get(System.getProperty("java.io.tmpdir")), String.format("é%s", new AlphanumericRandomStringService().random()));
        final CountDownLatch update = new CountDownLatch(1);
        final CountDownLatch delete = new CountDownLatch(1);
        final FileWatcherListener listener = new DisabledFileWatcherListener() {
            @Override
            public void fileWritten(final Local file) {
                try {
                    assertEquals(new File(file.getAbsolute()).getCanonicalPath(), new File(file.getAbsolute()).getCanonicalPath());
                }
                catch(IOException e) {
                    fail();
                }
                update.countDown();
            }

            @Override
            public void fileDeleted(final Local file) {
                try {
                    assertEquals(new File(file.getAbsolute()).getCanonicalPath(), new File(file.getAbsolute()).getCanonicalPath());
                }
                catch(IOException e) {
                    fail();
                }
                delete.countDown();
            }
        };
        LocalTouchFactory.get().touch(file);
        assertTrue(watcher.register(file.getParent(), new FileWatcher.DefaultFileFilter(file), listener).await(1, TimeUnit.SECONDS));
        Files.write(Paths.get(file.getAbsolute()), "Test".getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
        assertTrue(update.await(5L, TimeUnit.SECONDS));
        file.delete();
        assertTrue(delete.await(5L, TimeUnit.SECONDS));
        watcher.close();
    }

    @Test
    public void testListenerEventWatchService() throws Exception {
        final FileWatcher watcher = new FileWatcher(new NIOEventWatchService());
        final Local file = LocalFactory.get(LocalFactory.get(System.getProperty("java.io.tmpdir")), String.format("é%s", new AlphanumericRandomStringService().random()));
        final CountDownLatch create = new CountDownLatch(1);
        final CountDownLatch update = new CountDownLatch(1);
        final CountDownLatch delete = new CountDownLatch(1);
        final AtomicReference<Local> created = new AtomicReference<>();
        final AtomicReference<Local> updated = new AtomicReference<>();
        final AtomicReference<Local> deleted = new AtomicReference<>();
        final FileWatcherListener listener = new DisabledFileWatcherListener() {
            @Override
            public void fileCreated(final Local f) {
                created.set(f);
                create.countDown();
            }

            @Override
            public void fileWritten(final Local f) {
                updated.set(f);
                update.countDown();
            }

            @Override
            public void fileDeleted(final Local f) {
                deleted.set(f);
                delete.countDown();
            }
        };
        assertTrue(watcher.register(file.getParent(), new FileWatcher.DefaultFileFilter(file), listener).await(5L, TimeUnit.SECONDS));
        LocalTouchFactory.get().touch(file);
        assertTrue(create.await(5L, TimeUnit.SECONDS));
        assertEquals(file.getName(), created.get().getName());
        Files.write(Paths.get(file.getAbsolute()), "Test".getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
        assertTrue(update.await(5L, TimeUnit.SECONDS));
        assertEquals(file.getName(), updated.get().getName());
        file.delete();
        assertTrue(delete.await(5L, TimeUnit.SECONDS));
        assertEquals(file.getName(), deleted.get().getName());
        watcher.close();
    }

    @Test
    public void testRenameIntoWatchedFileTriggersCreatedEvent() throws Exception {
        final FileWatcher watcher = new FileWatcher(new NIOEventWatchService());
        final Local folder = LocalFactory.get(System.getProperty("java.io.tmpdir"));
        final Local source = LocalFactory.get(folder, new AlphanumericRandomStringService().random());
        final Local target = LocalFactory.get(folder, new AlphanumericRandomStringService().random());
        // Create the source file before registering so the watcher is active before the rename
        LocalTouchFactory.get().touch(source);
        final CountDownLatch create = new CountDownLatch(1);
        final AtomicReference<Local> created = new AtomicReference<>();
        assertTrue(watcher.register(folder, new FileWatcher.DefaultFileFilter(target), new DisabledFileWatcherListener() {
            @Override
            public void fileCreated(final Local f) {
                created.set(f);
                create.countDown();
            }
        }).await(5L, TimeUnit.SECONDS));
        // Rename source → target; NIO reports ENTRY_CREATE for the new name
        Files.move(Paths.get(source.getAbsolute()), Paths.get(target.getAbsolute()));
        assertTrue(create.await(5L, TimeUnit.SECONDS));
        assertEquals(target.getName(), created.get().getName());
        target.delete();
        watcher.close();
    }

    @Test
    public void testRelease() throws Exception {
        final RegisterWatchService fs = new NIOEventWatchService();
        final Watchable folder = Paths.get(System.getProperty("java.io.tmpdir"));
        final WatchKey key = fs.register(folder, new WatchEvent.Kind[]{ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY});
        assertTrue(key.isValid());
        fs.release();
        assertFalse(key.isValid());
    }

    @Test
    public void testPollReturnsNullWhenNoEvents() throws Exception {
        final RegisterWatchService fs = new NIOEventWatchService();
        final Watchable folder = Paths.get(System.getProperty("java.io.tmpdir"));
        fs.register(folder, new WatchEvent.Kind[]{ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY});
        assertNull(fs.poll());
        fs.close();
    }

    @Test
    public void testPollWithTimeoutReturnsNullWhenNoEvents() throws Exception {
        final RegisterWatchService fs = new NIOEventWatchService();
        final Watchable folder = Paths.get(System.getProperty("java.io.tmpdir"));
        fs.register(folder, new WatchEvent.Kind[]{ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY});
        assertNull(fs.poll(100, TimeUnit.MILLISECONDS));
        fs.close();
    }
}
