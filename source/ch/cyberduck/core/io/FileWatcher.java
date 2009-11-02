package ch.cyberduck.core.io;

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
import ch.cyberduck.ui.cocoa.foundation.NSAutoreleasePool;

import org.apache.log4j.Logger;

import com.barbarysoftware.watchservice.*;

import java.io.File;
import java.io.IOException;

import static com.barbarysoftware.watchservice.StandardWatchEventKind.*;

/**
 * @version $Id$
 */
public class FileWatcher {
    private static Logger log = Logger.getLogger(FileWatcher.class);

    private WatchService monitor;
    private Local file;

    public FileWatcher(Local file) {
        this.file = file;
        this.monitor = WatchService.newWatchService();
    }

    public void watch(final FileWatcherListener listener) throws IOException {
        final WatchableFile watchable = new WatchableFile(new File(file.getParent().getAbsolute()));
        watchable.register(monitor, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
        final Thread consumer = new Thread(new Runnable() {
            public void run() {
                while(true) {
                    final NSAutoreleasePool pool = NSAutoreleasePool.push();

                    try {
                        // wait for key to be signaled
                        WatchKey key;
                        try {
                            key = monitor.take();
                        }
                        catch(ClosedWatchServiceException e) {
                            // If this watch service is closed
                            return;
                        }
                        catch(InterruptedException e) {
                            return;
                        }
                        for(WatchEvent<?> event : key.pollEvents()) {
                            WatchEvent.Kind<?> kind = event.kind();
                            if(kind == OVERFLOW) {
                                continue;
                            }
                            // The filename is the context of the event.
                            WatchEvent<File> ev = (WatchEvent<File>) event;
                            log.debug("Detected file system event: " + ev.context() + " " + kind);
                            if(ENTRY_CREATE == kind) {
                                listener.fileWritten(LocalFactory.createLocal(ev.context()));
                            }
                            if(ENTRY_MODIFY == kind) {
                                listener.fileWritten(LocalFactory.createLocal(ev.context()));
                            }
                            if(ENTRY_DELETE == kind) {
                                listener.fileWritten(LocalFactory.createLocal(ev.context()));
                            }
                        }
                        // Reset the key -- this step is critical to receive further watch events.
                        boolean valid = key.reset();
                        if(!valid) {
                            // The key is no longer valid and the loop can exit.
                            break;
                        }
                    }
                    finally {
                        pool.release();
                    }
                }
            }
        });
        consumer.start();
    }

    public void unwatch() throws IOException {
        monitor.close();
    }
}
