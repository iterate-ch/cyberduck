package ch.cyberduck.core.local;

/*
 *  Copyright (c) 2009 David Kocher. All rights reserved.
 *  http://cyberduck.ch/
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  Bug fixes, suggestions and comments should be sent to:
 *  dkocher@cyberduck.ch
 */

import ch.cyberduck.core.Local;
import ch.cyberduck.core.io.watchservice.RegisterWatchService;

import org.junit.Test;

import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.Watchable;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static java.nio.file.StandardWatchEventKinds.*;
import static org.junit.Assert.*;

public class FileWatcherTest {

    @Test
    public void testDefaultFileFilterAcceptMatchingName() {
        final Local file = new Local(System.getProperty("java.io.tmpdir"), "test.txt");
        final FileWatcher.DefaultFileFilter filter = new FileWatcher.DefaultFileFilter(file);
        assertTrue(filter.accept(new Local(System.getProperty("java.io.tmpdir"), "test.txt")));
    }

    @Test
    public void testDefaultFileFilterRejectDifferentName() {
        final Local file = new Local(System.getProperty("java.io.tmpdir"), "test.txt");
        final FileWatcher.DefaultFileFilter filter = new FileWatcher.DefaultFileFilter(file);
        assertFalse(filter.accept(new Local(System.getProperty("java.io.tmpdir"), "other.txt")));
    }

    @Test
    public void testDuplicateRegistrationSkipped() throws IOException {
        final Local folder = new Local(System.getProperty("java.io.tmpdir"));
        final RegisterWatchService service = new StubWatchService();
        final FileWatcher watcher = new FileWatcher(service);
        try {
            final CountDownLatch first = watcher.register(folder, f -> true, new DisabledFileWatcherListener());
            first.await(5, TimeUnit.SECONDS);
            // Second registration for the same folder must be skipped
            final CountDownLatch second = watcher.register(folder, f -> true, new DisabledFileWatcherListener());
            assertEquals(0, second.getCount());
        }
        catch(InterruptedException e) {
            fail(e.getMessage());
        }
        finally {
            watcher.close();
        }
    }

    @Test
    public void testFileCreatedEvent() throws IOException, InterruptedException {
        final Local folder = new Local(System.getProperty("java.io.tmpdir"));
        final StubWatchService service = new StubWatchService();
        final FileWatcher watcher = new FileWatcher(service);
        final CountDownLatch listenerCalled = new CountDownLatch(1);
        final AtomicReference<Local> received = new AtomicReference<>();
        try {
            final CountDownLatch registered = watcher.register(folder, f -> true, new DisabledFileWatcherListener() {
                @Override
                public void fileCreated(final Local file) {
                    received.set(file);
                    listenerCalled.countDown();
                }
            });
            assertTrue(registered.await(5, TimeUnit.SECONDS));
            service.signal(new StubWatchKey(Collections.singletonList(
                    new StubWatchEvent<>(ENTRY_CREATE, Paths.get("created.txt")))));
            assertTrue(listenerCalled.await(5, TimeUnit.SECONDS));
            assertEquals("created.txt", received.get().getName());
        }
        finally {
            watcher.close();
        }
    }

    @Test
    public void testFileModifiedEvent() throws IOException, InterruptedException {
        final Local folder = new Local(System.getProperty("java.io.tmpdir"));
        final StubWatchService service = new StubWatchService();
        final FileWatcher watcher = new FileWatcher(service);
        final CountDownLatch listenerCalled = new CountDownLatch(1);
        final AtomicReference<Local> received = new AtomicReference<>();
        try {
            final CountDownLatch registered = watcher.register(folder, f -> true, new DisabledFileWatcherListener() {
                @Override
                public void fileWritten(final Local file) {
                    received.set(file);
                    listenerCalled.countDown();
                }
            });
            assertTrue(registered.await(5, TimeUnit.SECONDS));
            service.signal(new StubWatchKey(Collections.singletonList(
                    new StubWatchEvent<>(ENTRY_MODIFY, Paths.get("modified.txt")))));
            assertTrue(listenerCalled.await(5, TimeUnit.SECONDS));
            assertEquals("modified.txt", received.get().getName());
        }
        finally {
            watcher.close();
        }
    }

    @Test
    public void testFileDeletedEvent() throws IOException, InterruptedException {
        final Local folder = new Local(System.getProperty("java.io.tmpdir"));
        final StubWatchService service = new StubWatchService();
        final FileWatcher watcher = new FileWatcher(service);
        final CountDownLatch listenerCalled = new CountDownLatch(1);
        final AtomicReference<Local> received = new AtomicReference<>();
        try {
            final CountDownLatch registered = watcher.register(folder, f -> true, new DisabledFileWatcherListener() {
                @Override
                public void fileDeleted(final Local file) {
                    received.set(file);
                    listenerCalled.countDown();
                }
            });
            assertTrue(registered.await(5, TimeUnit.SECONDS));
            service.signal(new StubWatchKey(Collections.singletonList(
                    new StubWatchEvent<>(ENTRY_DELETE, Paths.get("deleted.txt")))));
            assertTrue(listenerCalled.await(5, TimeUnit.SECONDS));
            assertEquals("deleted.txt", received.get().getName());
        }
        finally {
            watcher.close();
        }
    }

    @Test
    public void testFilterRejectsUnrelatedFile() throws IOException, InterruptedException {
        final Local folder = new Local(System.getProperty("java.io.tmpdir"));
        final StubWatchService service = new StubWatchService();
        final FileWatcher watcher = new FileWatcher(service);
        final CountDownLatch listenerCalled = new CountDownLatch(1);
        try {
            // Filter only accepts "target.txt"
            final Local target = new Local(System.getProperty("java.io.tmpdir"), "target.txt");
            final CountDownLatch registered = watcher.register(folder, new FileWatcher.DefaultFileFilter(target), new DisabledFileWatcherListener() {
                @Override
                public void fileWritten(final Local file) {
                    listenerCalled.countDown();
                }
            });
            assertTrue(registered.await(5, TimeUnit.SECONDS));
            service.signal(new StubWatchKey(Collections.singletonList(
                    new StubWatchEvent<>(ENTRY_MODIFY, Paths.get("other.txt")))));
            // Listener must NOT be called
            assertFalse(listenerCalled.await(1, TimeUnit.SECONDS));
        }
        finally {
            watcher.close();
        }
    }

    @Test
    public void testRegisterByFileDelegate() throws IOException, InterruptedException {
        final Local file = new Local(System.getProperty("java.io.tmpdir"), "delegate.txt");
        final StubWatchService service = new StubWatchService();
        final FileWatcher watcher = new FileWatcher(service);
        final CountDownLatch listenerCalled = new CountDownLatch(1);
        final AtomicReference<Local> received = new AtomicReference<>();
        try {
            final CountDownLatch registered = watcher.register(file, new DisabledFileWatcherListener() {
                @Override
                public void fileWritten(final Local f) {
                    received.set(f);
                    listenerCalled.countDown();
                }
            });
            assertTrue(registered.await(5, TimeUnit.SECONDS));
            service.signal(new StubWatchKey(Collections.singletonList(
                    new StubWatchEvent<>(ENTRY_MODIFY, Paths.get("delegate.txt")))));
            assertTrue(listenerCalled.await(5, TimeUnit.SECONDS));
            assertEquals("delegate.txt", received.get().getName());
        }
        finally {
            watcher.close();
        }
    }

    // --- Stubs ---

    private static final class StubWatchService implements RegisterWatchService {
        private final LinkedBlockingDeque<WatchKey> keys = new LinkedBlockingDeque<>();
        private volatile boolean closed = false;

        void signal(final WatchKey key) {
            keys.offer(key);
        }

        @Override
        public WatchKey register(final Watchable folder, final WatchEvent.Kind<?>[] events,
                                 final WatchEvent.Modifier... modifiers) {
            return new StubWatchKey(Collections.emptyList());
        }

        @Override
        public void release() {
        }

        @Override
        public void close() {
            closed = true;
            // Unblock any thread waiting in take()
            keys.offer(new StubWatchKey(Collections.emptyList()) {
                @Override
                public boolean isValid() {
                    return false;
                }
            });
        }

        @Override
        public WatchKey poll() {
            return keys.poll();
        }

        @Override
        public WatchKey poll(final long timeout, final TimeUnit unit) throws InterruptedException {
            return keys.poll(timeout, unit);
        }

        @Override
        public WatchKey take() throws InterruptedException {
            if(closed) {
                throw new ClosedWatchServiceException();
            }
            final WatchKey key = keys.take();
            if(closed) {
                throw new ClosedWatchServiceException();
            }
            return key;
        }
    }

    private static class StubWatchKey implements WatchKey {
        private final List<WatchEvent<?>> events;
        private volatile boolean valid = true;

        StubWatchKey(final List<WatchEvent<?>> events) {
            this.events = events;
        }

        @Override
        public boolean isValid() {
            return valid;
        }

        @Override
        public List<WatchEvent<?>> pollEvents() {
            return events;
        }

        @Override
        public boolean reset() {
            return valid;
        }

        @Override
        public void cancel() {
            valid = false;
        }

        @Override
        public Watchable watchable() {
            return null;
        }
    }

    private static final class StubWatchEvent<T> implements WatchEvent<T> {
        private final Kind<T> kind;
        private final T context;

        StubWatchEvent(final Kind<T> kind, final T context) {
            this.kind = kind;
            this.context = context;
        }

        @Override
        public Kind<T> kind() {
            return kind;
        }

        @Override
        public int count() {
            return 1;
        }

        @Override
        public T context() {
            return context;
        }
    }
}
