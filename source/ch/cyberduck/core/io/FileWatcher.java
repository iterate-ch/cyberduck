package ch.cyberduck.core.io;

/*
 *  Copyright (c) 2006 David Kocher. All rights reserved.
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

import com.apple.cocoa.application.NSWorkspace;
import com.apple.cocoa.foundation.NSBundle;
import com.apple.cocoa.foundation.NSNotification;
import com.apple.cocoa.foundation.NSObject;
import com.apple.cocoa.foundation.NSSelector;

import ch.cyberduck.core.Local;

import org.apache.log4j.Logger;

import java.util.*;

public class FileWatcher extends NSObject {
    private static Logger log = Logger.getLogger(FileWatcher.class);

    private static final String UKKQueueFileRenamedNotification = "UKKQueueFileRenamedNotification";
    private static final String UKKQueueFileWrittenToNotification = "UKKQueueFileWrittenToNotification";
    private static final String UKKQueueFileDeletedNotification = "UKKQueueFileDeletedNotification";

    /**
     *
     */
    private static final Map<Local, FileWatcher> instances = new HashMap<Local, FileWatcher>();

    static {
        // Ensure native odb library is loaded
        try {
            NSBundle bundle = NSBundle.mainBundle();
            String lib = bundle.resourcePath() + "/Java/" + "libKQueue.dylib";
            log.info("Locating libKQueue.dylib at '" + lib + "'");
            System.load(lib);
        }
        catch(UnsatisfiedLinkError e) {
            log.error("Could not load the libKQueue library:" + e.getMessage());
            throw e;
        }
    }

    public static FileWatcher instance(final Local path) {
        if(!instances.containsKey(path)) {
            instances.put(path, new FileWatcher(path));
        }
        return instances.get(path);
    }

    /**
     * The file to be watched
     */
    private Local file;

    /**
     * The listeners to get notified about file system changes
     */
    private Set<FileWatcherListener> listeners
            = Collections.synchronizedSet(new HashSet<FileWatcherListener>());

    /**
     * 
     * @param file
     */
    private FileWatcher(final Local file) {
        this.file = file;
    }

    public void fileWritten(NSNotification notification) {
        for(FileWatcherListener listener: listeners) {
            listener.fileWritten(new Local(notification.userInfo().objectForKey("path").toString()));
        }
    }

    public void fileRenamed(NSNotification notification) {
        for(FileWatcherListener listener: listeners) {
            listener.fileRenamed(new Local(notification.userInfo().objectForKey("path").toString()));
        }
    }

    public void fileDeleted(NSNotification notification) {
        for(FileWatcherListener listener: listeners) {
            listener.fileDeleted(new Local(notification.userInfo().objectForKey("path").toString()));
        }
        removePath(file.getAbsolute());
    }

    public void watch(final FileWatcherListener listener) {
        this.listeners.add(listener);
        NSWorkspace.sharedWorkspace().notificationCenter().addObserver(
                this,
                new NSSelector("fileWritten", new Class[]{NSNotification.class}),
                UKKQueueFileWrittenToNotification,
                null);
        NSWorkspace.sharedWorkspace().notificationCenter().addObserver(
                this,
                new NSSelector("fileRenamed", new Class[]{NSNotification.class}),
                UKKQueueFileRenamedNotification,
                null);
        NSWorkspace.sharedWorkspace().notificationCenter().addObserver(
                this, new NSSelector("fileDeleted", new Class[]{NSNotification.class}),
                UKKQueueFileDeletedNotification,
                null);
        this.addPath(file.getAbsolute());
    }

    private native void addPath(String local);

    private native void removePath(String local);
}
