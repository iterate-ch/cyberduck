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

import ch.cyberduck.binding.foundation.CFIndex;
import ch.cyberduck.binding.foundation.CFRunLoopRef;
import ch.cyberduck.binding.foundation.CFStringRef;
import ch.cyberduck.core.io.watchservice.jna.FSEventStreamRef;
import ch.cyberduck.core.io.watchservice.jna.FSEvents;
import ch.cyberduck.core.threading.NamedThreadFactory;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.Watchable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;

import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;

/**
 * This class contains the bulk of my implementation of the Watch Service API.
 *
 * @author Steve McLeod
 */
public class FSEventWatchService extends AbstractWatchService {
    private static final Logger log = Logger.getLogger(FSEventWatchService.class);

    private final FSEvents library = FSEvents.library;

    private final Map<WatchKey, CFRunLoop> loops
            = new HashMap<WatchKey, CFRunLoop>();

    private final Map<WatchKey, FSEvents.FSEventStreamCallback> callbacks
            = new HashMap<WatchKey, FSEvents.FSEventStreamCallback>();

    private final ThreadFactory threadFactory
            = new NamedThreadFactory("fsevent");

    public FSEventWatchService() {
        if(log.isDebugEnabled()) {
            log.debug("Create new watch service");
        }
    }

    private static final int kFSEventStreamCreateFlagNone = 0x00000000;
    private static final int kFSEventStreamCreateFlagUseCFTypes = 0x00000001;
    private static final int kFSEventStreamCreateFlagNoDefer = 0x00000002;
    private static final int kFSEventStreamCreateFlagWatchRoot = 0x00000004;
    private static final int kFSEventStreamCreateFlagIgnoreSelf = 0x00000008;
    private static final int kFSEventStreamCreateFlagFileEvents = 0x00000010;

    @Override
    public WatchKey register(final Watchable folder,
                             final WatchEvent.Kind<?>[] events,
                             final WatchEvent.Modifier... modifiers)
            throws IOException {
        if(log.isInfoEnabled()) {
            log.info(String.format("Register file %s for events %s", folder, Arrays.toString(events)));
        }
        final Pointer[] values = {
                CFStringRef.toCFString(folder.toString()).getPointer()};

        final MacOSXWatchKey key = new MacOSXWatchKey(folder, this, events);

        final double latency = 1.0; // Latency in seconds

        final Map<File, Long> timestamps = createLastModifiedMap(new File(folder.toString()));
        final FSEvents.FSEventStreamCallback callback = new Callback(key, timestamps);
        final FSEventStreamRef stream = library.FSEventStreamCreate(
                Pointer.NULL, callback, Pointer.NULL,
                library.CFArrayCreate(null, values, CFIndex.valueOf(1), null),
                -1, latency,
                kFSEventStreamCreateFlagNoDefer);
        final CountDownLatch lock = new CountDownLatch(1);
        final CFRunLoop loop = new CFRunLoop(lock, stream);
        threadFactory.newThread(loop).start();
        try {
            lock.await();
        }
        catch(InterruptedException e) {
            throw new IOException(String.format("Failure registering for events in %s", folder), e);
        }
        loops.put(key, loop);
        callbacks.put(key, callback);
        return key;
    }

    private final class CFRunLoop implements Runnable {

        private final CountDownLatch lock;
        private final FSEventStreamRef streamRef;
        private CFRunLoopRef runLoop;

        public CFRunLoop(final CountDownLatch lock, final FSEventStreamRef streamRef) {
            this.streamRef = streamRef;
            this.lock = lock;
        }

        @Override
        public void run() {
            runLoop = FSEvents.library.CFRunLoopGetCurrent();
            // Schedule an FSEventStream on a runloop
            library.FSEventStreamScheduleWithRunLoop(streamRef, runLoop,
                    CFStringRef.toCFString("kCFRunLoopDefaultMode"));
            // Start receiving events on the stream
            library.FSEventStreamStart(streamRef);
            lock.countDown();
            library.CFRunLoopRun();
        }

        public CFRunLoopRef getRunLoop() {
            return runLoop;
        }

        public FSEventStreamRef getStreamRef() {
            return streamRef;
        }

        public boolean isStarted() {
            return lock.getCount() == 0;
        }
    }

    private static Map<File, Long> createLastModifiedMap(final File folder) {
        Map<File, Long> lastModifiedMap = new ConcurrentHashMap<File, Long>();
        for(File file : listFiles(folder)) {
            lastModifiedMap.put(file, file.lastModified());
        }
        return lastModifiedMap;
    }

    private static Set<File> listFiles(final File folder) {
        Set<File> files = new HashSet<File>();
        if(folder.isDirectory()) {
            final File[] children = folder.listFiles();
            if(null == children) {
                return files;
            }
            Collections.addAll(files, children);
        }
        return files;
    }

    @Override
    public void release() throws IOException {
        for(CFRunLoop l : loops.values()) {
            // Tells the daemon to stop sending events
            library.FSEventStreamStop(l.getStreamRef());
            // Removes the stream from the specified run loop
            library.FSEventStreamUnscheduleFromRunLoop(l.getStreamRef(), l.getRunLoop(),
                    CFStringRef.toCFString("kCFRunLoopDefaultMode"));
            // Remove the stream from the run loops upon which it has been scheduled
            library.FSEventStreamInvalidate(l.getStreamRef());
            // Release reference to the stream
            library.FSEventStreamRelease(l.getStreamRef());
        }
        loops.clear();
        callbacks.clear();
    }

    private final class MacOSXWatchKey extends AbstractWatchKey {
        private final Watchable file;
        private final AtomicBoolean cancelled = new AtomicBoolean(false);
        private final boolean reportCreateEvents;
        private final boolean reportModifyEvents;
        private final boolean reportDeleteEvents;

        public MacOSXWatchKey(final Watchable file, final FSEventWatchService service, final WatchEvent.Kind<?>[] events) {
            super(service);
            this.file = file;

            boolean reportCreateEvents = false;
            boolean reportModifyEvents = false;
            boolean reportDeleteEvents = false;

            for(WatchEvent.Kind<?> event : events) {
                if(event == StandardWatchEventKinds.ENTRY_CREATE) {
                    reportCreateEvents = true;
                }
                else if(event == StandardWatchEventKinds.ENTRY_MODIFY) {
                    reportModifyEvents = true;
                }
                else if(event == StandardWatchEventKinds.ENTRY_DELETE) {
                    reportDeleteEvents = true;
                }
            }
            this.reportCreateEvents = reportCreateEvents;
            this.reportDeleteEvents = reportDeleteEvents;
            this.reportModifyEvents = reportModifyEvents;
        }

        @Override
        public boolean isValid() {
            return !cancelled.get()
                    && callbacks.containsKey(this)
                    && loops.containsKey(this)
                    && loops.get(this).isStarted();
        }

        @Override
        public void cancel() {
            cancelled.set(true);
        }

        @Override
        public Watchable watchable() {
            return file;
        }

        public boolean isReportCreateEvents() {
            return reportCreateEvents;
        }

        public boolean isReportModifyEvents() {
            return reportModifyEvents;
        }

        public boolean isReportDeleteEvents() {
            return reportDeleteEvents;
        }
    }

    private static final class Callback implements FSEvents.FSEventStreamCallback {
        private final MacOSXWatchKey key;
        private final Map<File, Long> timestamps;

        private Callback(final MacOSXWatchKey key, final Map<File, Long> timestamps) {
            this.key = key;
            this.timestamps = timestamps;
        }

        public void invoke(FSEventStreamRef streamRef, Pointer clientCallBackInfo, NativeLong numEvents,
                           Pointer eventPaths, Pointer /* array of unsigned int */ eventFlags, /* array of unsigned long */ Pointer eventIds) {
            final int length = numEvents.intValue();
            for(String folder : eventPaths.getStringArray(0, length)) {
                final Set<File> filesOnDisk = listFiles(new File(folder));
                for(File file : findCreatedFiles(filesOnDisk)) {
                    if(key.isReportCreateEvents()) {
                        key.signalEvent(StandardWatchEventKinds.ENTRY_CREATE, file);
                    }
                    timestamps.put(file, file.lastModified());
                }

                for(File file : findModifiedFiles(filesOnDisk)) {
                    if(key.isReportModifyEvents()) {
                        key.signalEvent(StandardWatchEventKinds.ENTRY_MODIFY, file);
                    }
                    timestamps.put(file, file.lastModified());
                }

                for(File file : findDeletedFiles(folder, filesOnDisk)) {
                    if(key.isReportDeleteEvents()) {
                        key.signalEvent(StandardWatchEventKinds.ENTRY_DELETE, file);
                    }
                    timestamps.remove(file);
                }
            }
        }

        private List<File> findModifiedFiles(final Set<File> files) {
            List<File> modifiedFileList = new ArrayList<File>();
            for(File file : files) {
                final Long lastModified = timestamps.get(file);
                if(lastModified != null && lastModified != file.lastModified()) {
                    modifiedFileList.add(file);
                }
            }
            return modifiedFileList;
        }

        private List<File> findCreatedFiles(final Set<File> files) {
            List<File> createdFileList = new ArrayList<File>();
            for(File file : files) {
                if(!timestamps.containsKey(file)) {
                    createdFileList.add(file);
                }
            }
            return createdFileList;
        }

        private List<File> findDeletedFiles(final String folder, final Set<File> files) {
            List<File> deletedFileList = new ArrayList<File>();
            for(File file : timestamps.keySet()) {
                if(file.getAbsolutePath().startsWith(folder) && !files.contains(file)) {
                    deletedFileList.add(file);
                }
            }
            return deletedFileList;
        }

        @Override
        protected void finalize() throws Throwable {
            try {
                log.warn(String.format("Callback for %s is finalized", key));
            }
            finally {
                super.finalize();
            }
        }
    }
}
