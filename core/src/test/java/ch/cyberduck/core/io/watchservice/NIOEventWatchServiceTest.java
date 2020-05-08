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
import ch.cyberduck.core.Factory;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.LocalFactory;
import ch.cyberduck.core.local.DisabledFileWatcherListener;
import ch.cyberduck.core.local.FileWatcher;
import ch.cyberduck.core.local.FileWatcherListener;
import ch.cyberduck.core.local.LocalTouchFactory;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.Watchable;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static java.nio.file.StandardWatchEventKinds.*;
import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;

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
    public void testListenerEventWatchServiceWindows() throws Exception {
        assumeTrue(Factory.Platform.getDefault().equals(Factory.Platform.Name.windows));
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
        final ProcessBuilder sh = new ProcessBuilder("cmd", "/c", String.format("echo 'Test' >> %s", file.getAbsolute()));
        final Process cat = sh.start();
        assertTrue(cat.waitFor(5L, TimeUnit.SECONDS));
        assertTrue(update.await(5L, TimeUnit.SECONDS));
        file.delete();
        assertTrue(delete.await(5L, TimeUnit.SECONDS));
        watcher.close();
    }

    @Test
    public void testListenerEventWatchServiceLinux() throws Exception {
        assumeTrue(Factory.Platform.getDefault().equals(Factory.Platform.Name.linux));
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
        final ProcessBuilder sh = new ProcessBuilder("sh", "-c", String.format("echo 'Test' >> %s", file.getAbsolute()));
        final Process cat = sh.start();
        assertTrue(cat.waitFor(5L, TimeUnit.SECONDS));
        assertTrue(update.await(5L, TimeUnit.SECONDS));
        file.delete();
        assertTrue(delete.await(5L, TimeUnit.SECONDS));
        watcher.close();
    }
}
