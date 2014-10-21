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
import ch.cyberduck.core.LocalFactory;
import ch.cyberduck.core.io.watchservice.ClosedWatchServiceException;
import ch.cyberduck.core.io.watchservice.FSEventWatchService;
import ch.cyberduck.core.io.watchservice.WatchEvent;
import ch.cyberduck.core.io.watchservice.WatchKey;
import ch.cyberduck.core.io.watchservice.WatchService;
import ch.cyberduck.core.io.watchservice.WatchableFile;
import ch.cyberduck.core.threading.ThreadPool;
import ch.cyberduck.core.unicode.NFCNormalizer;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

import static ch.cyberduck.core.io.watchservice.StandardWatchEventKind.*;

/**
 * @version $Id$
 */
public final class FileWatcher implements FileWatcherCallback {
    private static Logger log = Logger.getLogger(FileWatcher.class);

    private NFCNormalizer normalizer = new NFCNormalizer();

    private WatchService monitor;

    private ThreadPool pool;

    public FileWatcher() {
        monitor = new FSEventWatchService();
        pool = new ThreadPool(1, "watcher");
    }

    public CountDownLatch register(final Local file) {
        final WatchableFile watchable = new WatchableFile(new File(file.getParent().getAbsolute()));
        try {
            if(log.isDebugEnabled()) {
                log.debug(String.format("Register file %s", watchable.getFile()));
            }
            watchable.register(monitor, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
        }
        catch(IOException e) {
            log.error(String.format("Failure registering file watcher monitor for %s", watchable.getFile()), e);
        }
        final CountDownLatch lock = new CountDownLatch(1);
        pool.execute(new Callable<Boolean>() {
            @Override
            public Boolean call() throws IOException {
                while(true) {
                    // wait for key to be signaled
                    WatchKey key;
                    try {
                        lock.countDown();
                        key = monitor.take();
                    }
                    catch(ClosedWatchServiceException e) {
                        // If this watch service is closed
                        return true;
                    }
                    catch(InterruptedException e) {
                        return false;
                    }
                    for(WatchEvent<?> event : key.pollEvents()) {
                        final WatchEvent.Kind<?> kind = event.kind();
                        if(log.isInfoEnabled()) {
                            log.info(String.format("Detected file system event %s", kind.name()));
                        }
                        if(kind == OVERFLOW) {
                            log.error(String.format("Overflow event for %s", watchable.getFile()));
                            break;
                        }
                        // The filename is the context of the event.
                        final WatchEvent<File> ev = (WatchEvent<File>) event;
                        if(normalizer.normalize(event.context().toString())
                                .equals(new File(file.getAbsolute()).getCanonicalFile().getAbsolutePath())) {
                            callback(ev);
                        }
                        else {
                            log.debug(String.format("Ignored file system event for unknown file %s", event.context()));
                        }
                    }
                    // Reset the key -- this step is critical to receive further watch events.
                    boolean valid = key.reset();
                    if(!valid) {
                        // The key is no longer valid and the loop can exit.
                        return true;
                    }
                }
            }
        });
        return lock;
    }

    @Override
    public void callback(final WatchEvent<File> event) {
        final WatchEvent.Kind<?> kind = event.kind();
        if(log.isInfoEnabled()) {
            log.info(String.format("Process file system event %s for %s", kind.name(), event.context()));
        }
        if(ENTRY_MODIFY == kind) {
            for(FileWatcherListener l : listeners.toArray(new FileWatcherListener[listeners.size()])) {
                l.fileWritten(LocalFactory.get(event.context().getAbsolutePath()));
            }
        }
        else if(ENTRY_DELETE == kind) {
            for(FileWatcherListener l : listeners.toArray(new FileWatcherListener[listeners.size()])) {
                l.fileDeleted(LocalFactory.get(event.context().getAbsolutePath()));
            }
        }
        else if(ENTRY_CREATE == kind) {
            for(FileWatcherListener l : listeners.toArray(new FileWatcherListener[listeners.size()])) {
                l.fileCreated(LocalFactory.get(event.context().getAbsolutePath()));
            }
        }
        else {
            log.debug(String.format("Ignored file system event %s for %s", kind.name(), event.context()));
        }
    }

    private Set<FileWatcherListener> listeners
            = Collections.synchronizedSet(new HashSet<FileWatcherListener>());

    public void addListener(final FileWatcherListener listener) {
        listeners.add(listener);
    }

    public void removeListener(final FileWatcherListener listener) {
        listeners.remove(listener);
    }

    public void close(final Local local) {
        try {
            monitor.close();
            pool.shutdown();
        }
        catch(IOException e) {
            log.error(String.format("Failure closing file watcher monitor"), e);
        }
    }
}