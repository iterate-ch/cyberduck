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

import ch.cyberduck.core.Filter;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.LocalFactory;
import ch.cyberduck.core.NullFilter;
import ch.cyberduck.core.io.watchservice.RegisterWatchService;
import ch.cyberduck.core.io.watchservice.WatchServiceFactory;
import ch.cyberduck.core.threading.DefaultThreadPool;
import ch.cyberduck.core.threading.ThreadPool;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

import static java.nio.file.StandardWatchEventKinds.*;

public final class FileWatcher {
    private static final Logger log = LogManager.getLogger(FileWatcher.class);

    private final RegisterWatchService monitor;
    private final ThreadPool pool;
    private final Set<Local> registered = new HashSet<>();

    public FileWatcher() {
        this(WatchServiceFactory.get());
    }

    public FileWatcher(final RegisterWatchService monitor) {
        this.monitor = monitor;
        this.pool = new DefaultThreadPool("watcher");
    }

    public static final class DefaultFileFilter extends NullFilter<Local> {
        private final Local file;

        public DefaultFileFilter(final Local file) {
            this.file = file;
        }

        @Override
        public boolean accept(final Local f) {
            return StringUtils.equals(file.getName(), f.getName());
        }
    }

    public CountDownLatch register(final Local file, final FileWatcherListener listener) throws IOException {
        return this.register(file.getParent(), new DefaultFileFilter(file), listener);
    }

    public CountDownLatch register(final Local folder, final Filter<Local> filter, final FileWatcherListener listener) throws IOException {
        if(registered.contains(folder)) {
            if(log.isWarnEnabled()) {
                log.warn(String.format("Skip duplicate registration for %s in %s", folder, monitor));
            }
            return new CountDownLatch(0);
        }
        if(log.isDebugEnabled()) {
            log.debug(String.format("Register folder %s watching with filter %s", folder, filter));
        }
        final WatchKey key = monitor.register(Paths.get(folder.getAbsolute()), new WatchEvent.Kind[]{ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY});
        if(!key.isValid()) {
            throw new IOException(String.format("Failure registering for events in %s", folder));
        }
        registered.add(folder);
        final CountDownLatch lock = new CountDownLatch(1);
        pool.execute(new Callable<Boolean>() {
            @Override
            public Boolean call() {
                while(true) {
                    // wait for key to be signaled
                    final WatchKey key;
                    try {
                        lock.countDown();
                        if(log.isDebugEnabled()) {
                            log.debug(String.format("Wait for key from watch service %s", monitor));
                        }
                        key = monitor.take();
                    }
                    catch(ClosedWatchServiceException e) {
                        if(log.isWarnEnabled()) {
                            log.warn(String.format("Exit watching folder %s for closed monitor %s", folder, monitor));
                        }
                        // If this watch service is closed
                        return true;
                    }
                    catch(InterruptedException e) {
                        return false;
                    }
                    if(log.isDebugEnabled()) {
                        log.debug(String.format("Retrieved key %s from watch service %s", key, monitor));
                    }
                    for(WatchEvent<?> event : key.pollEvents()) {
                        final WatchEvent.Kind<?> kind = event.kind();
                        if(log.isInfoEnabled()) {
                            log.info(String.format("Detected file system event %s", kind.name()));
                        }
                        if(kind == OVERFLOW) {
                            log.error(String.format("Overflow event for %s", folder));
                            continue;
                        }
                        // The filename is the context of the event. May be absolute or relative path name.
                        if(filter.accept(normalize(folder, event.context().toString()))) {
                            callback(folder, event, listener);
                        }
                        else {
                            log.warn(String.format("Ignored file system event for unknown file %s", event.context()));
                        }
                    }
                    // Reset the key -- this step is critical to receive further watch events.
                    boolean valid = key.reset();
                    if(!valid) {
                        // The key is no longer valid and the loop can exit.
                        if(log.isWarnEnabled()) {
                            log.warn(String.format("Exit watching folder %s", folder));
                        }
                        return true;
                    }
                }
            }
        });
        return lock;
    }

    private static Local normalize(final Local parent, final String name) {
        if(StringUtils.startsWith(name, String.valueOf(parent.getDelimiter()))) {
            return LocalFactory.get(name);
        }
        return LocalFactory.get(parent, name);
    }

    private void callback(final Local folder, final WatchEvent<?> event, final FileWatcherListener l) {
        final WatchEvent.Kind<?> kind = event.kind();
        if(log.isInfoEnabled()) {
            log.info(String.format("Process file system event %s for %s", kind.name(), event.context()));
        }
        if(ENTRY_MODIFY == kind) {
            l.fileWritten(normalize(folder, event.context().toString()));
        }
        else if(ENTRY_DELETE == kind) {
            l.fileDeleted(normalize(folder, event.context().toString()));
        }
        else if(ENTRY_CREATE == kind) {
            l.fileCreated(normalize(folder, event.context().toString()));
        }
        else {
            log.debug(String.format("Ignored file system event %s for %s", kind.name(), event.context()));
        }
    }

    public void close() {
        try {
            monitor.close();
            pool.shutdown(false);
            registered.clear();
        }
        catch(IOException e) {
            log.error("Failure closing file watcher monitor", e);
        }
    }
}
