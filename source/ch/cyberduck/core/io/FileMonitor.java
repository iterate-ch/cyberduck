package ch.cyberduck.core.io;

/*
 * Copyright (c) 2007 Timothy Wall, All Rights Reserved
 * Parts Copyright (c) 2008 Olivier Chafik
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * <p/>
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 */

import org.apache.log4j.Logger;
import org.rococoa.internal.AutoreleaseBatcher;

import com.sun.jna.Native;
import com.sun.jna.Pointer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

/**
 * Provides notification of file system changes.  Actual capabilities may
 * vary slightly by platform.
 * <p/>
 * Watched files which are removed from the filesystem are no longer watched.
 *
 * @author twall
 */
public abstract class FileMonitor {
    private static Logger log = Logger.getLogger(FileMonitor.class);

    private static final KQueue K_QUEUE = (KQueue) Native.loadLibrary("c", KQueue.class);

    public static final int FILE_CREATED = 0x1;
    public static final int FILE_DELETED = 0x2;
    public static final int FILE_MODIFIED = 0x4;
    public static final int FILE_ACCESSED = 0x8;
    public static final int FILE_NAME_CHANGED_OLD = 0x10;
    public static final int FILE_NAME_CHANGED_NEW = 0x20;
    public static final int FILE_RENAMED = FILE_NAME_CHANGED_OLD | FILE_NAME_CHANGED_NEW;
    public static final int FILE_SIZE_CHANGED = 0x40;
    public static final int FILE_ATTRIBUTES_CHANGED = 0x80;
    public static final int FILE_SECURITY_CHANGED = 0x100;

    //public static final int FILE_WATCHED = 0x200;
    //public static final int FILE_UNWATCHED = 0x400;

    public static final int FILE_ANY = 0x1FF;

    public interface FileListener extends EventListener {
        public void fileChanged(FileEvent e);
    }

    public class FileEvent extends EventObject {
        private final File file;
        private final int type;

        public FileEvent(File file, int type) {
            super(FileMonitor.this);
            this.file = file;
            this.type = type;
        }

        public File getFile() {
            return file;
        }

        public int getType() {
            return type;
        }

        /// this is just to ease up events debugging
        /*public String toString() {
        	Integer typeObj = new Integer(type);
        	for (Field field : FileMonitor.class.getDeclaredFields()) {
        		try {
					if (Modifier.isStatic(field.getModifiers()) && typeObj.equals(field.get(null)))
						return field.getName().replace("FILE_", "") + ": " + file;
				} catch (Exception e) {
					e.printStackTrace();
				}
        	}
            return "FileEvent: " + file + ": " + type;
        }*/
    }

    private final Map<File, Integer> watched = new HashMap<File, Integer>();
    protected Set<FileListener> listeners = new HashSet<FileListener>();

    protected abstract void watch(File file, int mask, boolean recursive) throws IOException;

    protected abstract void unwatch(File file);

    protected abstract void dispose();

    public void addWatch(File dir) throws IOException {
        this.addWatch(dir, FILE_ANY);
    }

    public void addWatch(File dir, int mask) throws IOException {
        this.addWatch(dir, mask, dir.isDirectory());
    }

    public void addWatch(File dir, int mask, boolean recursive) throws IOException {
        watched.put(dir, mask);
        this.watch(dir, mask, recursive);
    }

    public void removeWatch(File file) {
        if(watched.remove(file) != null) {
            unwatch(file);
        }
    }

    protected void notify(FileEvent e) {
        for(FileListener listener : listeners.toArray(new FileListener[listeners.size()])) {
            listener.fileChanged(e);
        }
    }

    public synchronized void addFileListener(FileListener x) {
        listeners.add(x);
    }

    public synchronized void removeFileListener(FileListener x) {
        listeners.remove(x);
    }

    @Override
    protected void finalize() throws Throwable {
        for(Object o : watched.keySet()) {
            removeWatch((File) o);
        }
        this.dispose();
        super.finalize();
    }

    /**
     * Shared FileMonitor instance
     */
    private static FileMonitor sharedInstance = null;

    /**
     * Get the shared FileMonitor instance
     *
     * @return FileMonitor singleton
     */
    public static FileMonitor getInstance() {
        if(null == sharedInstance) {
            try {
                sharedInstance = new KQueueFileMonitor();
            }
            catch(IOException e) {
                throw new RuntimeException(e);
            }
        }
        return sharedInstance;
    }

    /**
     * KQueue implementation of FileMonitor, for BSD-derived systems (including Mac OS X).<br/>
     * Only one dedicated thread and one kqueue is created by this monitor, designed to be as scalable as possible.<br/>
     * TODO handle the FileEvent.FILE_CREATED event and recursive flag (right now, only existing files can be watched).<br/>
     *
     * @author Olivier Chafik
     */
    private static class KQueueFileMonitor extends FileMonitor implements Runnable {
        /// map from file descriptor to file
        private final Map<Integer, File> fdToFile = new TreeMap<Integer, File>();

        /// map from file to file descriptor
        private final Map<File, Integer> fileToFd = new TreeMap<File, Integer>();

        int notificationFileDescriptor;
        File notificationFile;
        final int kqueueHandle;
        KQueue.timespec timeout;

        /// Map from file to pending declaration kevent
        private final Map<File, KQueue.kevent> pendingDeclarationEvents = new LinkedHashMap<File, KQueue.kevent>();

        public KQueueFileMonitor() throws IOException {
            try {
                notificationFile = File.createTempFile("kqueueFileWatch", ".sync");
                notificationFile.createNewFile();
                notificationFile.deleteOnExit();

                add(notificationFile, FileMonitor.FILE_ATTRIBUTES_CHANGED, false);
                notificationFileDescriptor = fileToFd.get(notificationFile);

                // as there is a synchronization file, we will be able to interrupt calls to kevent easily. So we setup a timeout as high as we want :
                timeout = new KQueue.timespec(100, 0);
            }
            catch(IOException e) {
                log.error("Failed to create notification file " + notificationFile);
                // we'll do fine even without notification file : however, there will be some delay to add / remove files
                timeout = new KQueue.timespec(0, 500000000);
            }
            kqueueHandle = K_QUEUE.kqueue();
            if(kqueueHandle == -1) {
                throw new IOException("Unable to create kqueue !");
            }
        }

        protected int convertMask(int fileMonitorMask) {
            int keventMask = 0;
            if((fileMonitorMask & FileMonitor.FILE_DELETED) != 0) {
                keventMask |= KQueue.NOTE_DELETE;
            }
            if((fileMonitorMask & FileMonitor.FILE_MODIFIED) != 0) {
                keventMask |= KQueue.NOTE_WRITE;
            }
            if((fileMonitorMask & FileMonitor.FILE_RENAMED) != 0) {
                keventMask |= KQueue.NOTE_RENAME;
            }
            if((fileMonitorMask & FileMonitor.FILE_SIZE_CHANGED) != 0) {
                keventMask |= KQueue.NOTE_EXTEND;
            }
            if((fileMonitorMask & FileMonitor.FILE_ACCESSED) != 0) {
//                keventMask |= Kernel32.FILE_NOTIFY_CHANGE_LAST_ACCESS;
            }
            if((fileMonitorMask & FileMonitor.FILE_ATTRIBUTES_CHANGED) != 0) {
                keventMask |= KQueue.NOTE_ATTRIB;
            }
            if((fileMonitorMask & FileMonitor.FILE_SECURITY_CHANGED) != 0) {
                keventMask |= KQueue.NOTE_ATTRIB;
            }
            return keventMask;
        }
        //final Pointer NATIVE_0L = Pointer.createConstant(0), NATIVE_1L = Pointer.createConstant(1);

        protected synchronized void add(File file, int keventMask, boolean recursive) throws IOException {
            int fd;
            Integer fdObj = fileToFd.get(file);
            if(fdObj == null) {
                fd = K_QUEUE.open(file.toString(), KQueue.O_EVTONLY, 0);
                if(fd < 0) {
                    throw new FileNotFoundException(file.toString());
                }

                fdObj = fd;
                fdToFile.put(fdObj, file);
                fileToFd.put(file, fdObj);
            }
            else {
                fd = fdObj;
            }

            KQueue.kevent ke = new KQueue.kevent();
            ke.ident = fd;
            ke.filter = KQueue.EVFILT_VNODE;
            ke.flags = KQueue.EV_ADD | KQueue.EV_CLEAR;
            ke.fflags = keventMask;
            ke.data = 0;

            recursive = recursive && file.isDirectory();
            //ke.udata = recursive ? NATIVE_1L : NATIVE_0L;
            ke.udata = Pointer.NULL;

            synchronized(pendingDeclarationEvents) {
                pendingDeclarationEvents.put(file, ke);
            }

            //System.err.println("File " + file + " : recursive = "+recursive);
            if(recursive) {
                File[] children = file.listFiles();
                for(int i = children.length; i-- != 0;) {
                    File child = children[i];
                    log.error("Child " + child);
                    add(child, keventMask, recursive);
                }
            }
        }

        protected synchronized void remove(File file) {
            Integer fdObj = fileToFd.remove(file);
            if(fdObj == null) {
                return;
            }

            fdToFile.remove(fdObj);

            // This will automatically remove the file from the kqueue :
            K_QUEUE.close(fdObj);
        }

        @Override
        public synchronized void dispose() {
            if(loopThread != null) {
                loopThread.interrupt();
            }
        }

        @Override
        protected void finalize() throws Throwable {
            notificationFile.delete();
            if(kqueueHandle != -1) {
                K_QUEUE.close(kqueueHandle);
            }
            super.finalize();
        }

        /**
         * @param f
         * @param fileMonitorMask
         * @param recurse         ignored !
         * @throws IOException
         */
        @Override
        public synchronized void watch(File f, int fileMonitorMask, boolean recurse) throws IOException {
            this.doWatch(f, this.convertMask(fileMonitorMask), recurse);
        }

        protected void doWatch(File f, int keventMask, boolean recurse) throws IOException {
            this.checkStarted();
            this.add(f, keventMask, recurse);
            this.tryAndSync();
        }

        /// Whether the file monitor thread was started or not
        protected boolean started;

        protected Thread loopThread;

        protected synchronized void checkStarted() {
            if(loopThread == null) {
                (loopThread = new Thread(this)).start();
            }
        }

        @Override
        public synchronized void unwatch(File file) {
            remove(file);
            tryAndSync();
        }


        /**
         * This will try to cause the kevent call in run() to return before the timeout, by modifying the notificationFile
         */
        protected void tryAndSync() {
            notificationFile.setLastModified(System.currentTimeMillis());
        }

        public void run() {
            final AutoreleaseBatcher batcher = AutoreleaseBatcher.forThread(10);
            KQueue.kevent event = new KQueue.kevent();
            Pointer pEvent = event.getPointer();
            timeout.write();
            Pointer pTimeout = timeout.getPointer();

            KQueue.kevent modifEvent = new KQueue.kevent();
            modifEvent.write();
            Pointer pModifEvent = modifEvent.getPointer();

            for(; !Thread.interrupted();) {
                // First, handle pending declarations : add watched files
                synchronized(pendingDeclarationEvents) {
                    for(Map.Entry<File, KQueue.kevent> entry : pendingDeclarationEvents.entrySet()) {
                        modifEvent.set(entry.getValue());
                        modifEvent.write();

                        int nev = K_QUEUE.kevent(kqueueHandle, pModifEvent, 1, Pointer.NULL, 0, Pointer.NULL);
                        if(nev != 0) {
                            log.error("kevent did not like modification event for " + entry.getKey());
                        }
                    }
                    pendingDeclarationEvents.clear();
                }

                // Now listen to events : call returns on first event or after timeout is reached.
                int nev = K_QUEUE.kevent(kqueueHandle, Pointer.NULL, 0, pEvent, 1, pTimeout);
                event.read();
                log.debug("kevent = " + nev);
                if(nev < 0) {
                    throw new RuntimeException("kevent call returned negative value !");
                }
                else if(nev > 0) {
                    if(notificationFileDescriptor == event.ident) {
                        // This is a fake event, triggered by modification of the synchronization file.
                        // It is only meant to interrupt the blocking call to kevent and handle pending declarations before doing another blocking call to kevent.
                        continue;
                    }
                    File file = (File) fdToFile.get(event.ident);
                    if((event.fflags & KQueue.NOTE_DELETE) != 0) {
                        remove(file);
                        notify(new FileEvent(file, FileMonitor.FILE_DELETED));
                    }
                    if((event.fflags & KQueue.NOTE_RENAME) != 0) {
                        // A file that is renamed is also declared as deleted, and its track is lost.
                        remove(file);
                        notify(new FileMonitor.FileEvent(file, FileMonitor.FILE_RENAMED));
                        notify(new FileMonitor.FileEvent(file, FileMonitor.FILE_DELETED));
                    }
                    if((event.fflags & KQueue.NOTE_EXTEND) != 0 || (event.fflags & KQueue.NOTE_WRITE) != 0) {
                        notify(new FileMonitor.FileEvent(file, FileMonitor.FILE_SIZE_CHANGED));
                    }
                    if((event.fflags & KQueue.NOTE_ATTRIB) != 0) {
                        // TODO handle recursivity : if file is a directory and is marked as recursively watched, add children
                        /*if (event.udata.getPointer().getInt(0) == 1) // Was this file watched recursively ?
                                      if (file.isDirectory())
                                          for (File child : file.listFiles())
                                              if (!fileToFd.containsKey(child)) {
                                                  // child is not watched yet : add it
                                                  notify(new FileEvent(child, FileMonitor.FILE_CREATED));
                                                  try {
                                                      doWatch(child, event.fflags, true);
                                                  } catch (IOException e) {
                                                      log.error("Failed to add recursive file "+child +" : "+e);
                                                  }
                                              }*/
                        notify(new FileMonitor.FileEvent(file, FileMonitor.FILE_ATTRIBUTES_CHANGED));
                    }
                }
                batcher.operate();
            }
        }
    }
}