package ch.cyberduck.core.fs.fuse;

/*
 * Copyright (c) 2002-2011 David Kocher. All rights reserved.
 *
 * http://cyberduck.ch/
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
 * dkocher@cyberduck.ch
 */

import ch.cyberduck.core.*;
import ch.cyberduck.core.fs.Filesystem;
import ch.cyberduck.core.fs.FilesystemBackgroundAction;
import ch.cyberduck.core.fs.FilesystemFactory;
import ch.cyberduck.core.i18n.Locale;
import ch.cyberduck.core.threading.DefaultMainAction;
import ch.cyberduck.ui.cocoa.ProxyController;
import ch.cyberduck.ui.cocoa.foundation.*;
import ch.cyberduck.ui.cocoa.model.OutlinePathReference;

import org.rococoa.Foundation;
import org.rococoa.ID;
import org.rococoa.Rococoa;
import org.rococoa.cocoa.foundation.NSError;

import org.apache.log4j.Logger;

import com.sun.jna.IntegerType;
import com.sun.jna.Pointer;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * @version $Id$
 */
public class FuseFilesystem extends ProxyController implements Filesystem {
    private static Logger log = Logger.getLogger(FuseFilesystem.class);

    public static void register() {
        FilesystemFactory.addFactory(Factory.NATIVE_PLATFORM, new Factory());
    }

    private static class Factory extends FilesystemFactory {
        @Override
        protected FuseFilesystem create() {
            return new FuseFilesystem();
        }
    }

    private Session session;

    /**
     *
     */
    private Local mountpoint;

    private FuseFilesystem() {
        ;
    }

    private boolean isAvailable() {
        if(null == GMUserFileSystem.CLASS) {
            log.warn("Framework not available");
            return false;
        }
        return true;
    }

    public void mount(Session s) {
        if(!this.isAvailable()) {
            this.invoke(new DefaultMainAction() {
                public void run() {

                }
            });
            return;
        }
        session = s;
        filesystem = GMUserFileSystem.create(
                (fileystemCallback = new FSCallback()).id(), true);

        NSNotificationCenter center = NSNotificationCenter.defaultCenter();
        center.addObserver(fileystemCallback.id(), Foundation.selector("mountFailed:"),
                GMUserFileSystem.kGMUserFileSystemMountFailed, null);
        center.addObserver(fileystemCallback.id(), Foundation.selector("didMount:"),
                GMUserFileSystem.kGMUserFileSystemDidMount, null);
        center.addObserver(fileystemCallback.id(), Foundation.selector("didUnmount:"),
                GMUserFileSystem.kGMUserFileSystemDidUnmount, null);

        final NSMutableArray options = NSMutableArray.array();
        options.addObject("volicon="
                + NSBundle.mainBundle().pathForResource_ofType(session.getHost().getProtocol().disk(), "icns"));
        // You can use the volname option to specify a name for the MacFUSE volume being mounted. This
        // is the name that would show up on the Desktop. In the absence of this option, MacFUSE will
        // automatically generate a name that would incorporate the MacFUSE device index and the
        // user-space file system being used. For example, an SSHFS mount might have an automatically
        // assigned name "MacFUSE Volume 0 (sshfs)".
        final String volume = session.getHost().getHostname();
        options.addObject("volname=" + volume);
        // By default, if MacFUSE detects a change in a file's size during getattr(), it will purge
        // that file's buffer cache. When auto_cache is enabled, MacFUSE will additionally
        // detect modification time changes during getattr() and open() and will automatically
        // purge the buffer cache and/or attributes of the file if necessary. It will also
        // generate the relevant kqueue messages. All this is subject to the attribute timeout.
        // That is, up to one purge can occur per attribute timeout window. As long as the
        // user-space file system's getattr() callback returns up-to-date size and modification
        // time information, this should work as intended. For user-space file systems that wish
        // the kernel to keep up with "remote" changes, this should obviate the need for explicit
        // purging. auto_cache is not enabled by default: it's opt-in.
        options.addObject("auto_cache");
        // This option makes MacFUSE deny all types of access to extended attributes that begin with the "com.apple." prefix. On Mac OS X 10.5.x, this is the preferred option if you wish to disallow entities such as resource forks and Finder information.
        options.addObject("noapplexattr");
        // This option makes MacFUSE deny all types of access to Apple Double (._) files and .DS_Store files. Any existing files will become apparently non-existent. New files that match the criteria will be disallowed from being created.
        options.addObject("noappledouble");
        mountpoint = LocalFactory.createLocal("/Volumes/" + volume);
        if(mountpoint.exists()) {
            // Make sure we do not mount to a already existing path
            final String parent = mountpoint.getParent().getAbsolute();
            int no = 0;
            while(mountpoint.exists()) {
                no++;
                String proposal = volume + "-" + no;
                mountpoint = LocalFactory.createLocal(parent, proposal);
            }
        }
        filesystem.mountAtPath_withOptions_shouldForeground_detachNewThread(
                mountpoint.getAbsolute(), options, true, true);
    }

    private CountDownLatch unmount;

    public void unmount() {
        log.debug("unmount");
        filesystem.unmount();
//        try {
//            unmount.wait(Preferences.instance().getInteger("disk.unmount.timeout"));
//        }
//        catch(InterruptedException e) {
//            log.warn("Unmount failed:" + e.getMessage());
//        }
//        if(log.isInfoEnabled()) {
//            log.info("Successfully unmounted:" + session);
//        }
        filesystem.setDelegate(null);
    }

    /**
     *
     */
    private GMUserFileSystem filesystem;

    /**
     *
     */
    private FSCallback fileystemCallback;

    /**
     *
     */
    private class FSCallback extends ProxyController implements GMUserFileSystemLifecycle {
        public void willMount() {
            log.debug("willMount");
        }

        public void willUnmount() {
            log.debug("willUnmount");
        }

        public void didMount(NSNotification notification) {
            log.debug("didMount");
            unmount = new CountDownLatch(1);
            try {
                Local folder = LocalFactory.createLocal(mountpoint, session.home().getAbsolute());
                folder.reveal();
            }
            catch(IOException e) {
                log.warn(e.getMessage());
            }
        }

        public void didUnmount(NSNotification notification) {
            log.debug("didUnmount");
            session.close();
            unmount.countDown();
        }

        public void mountFailed(NSNotification notification) {
            log.warn("mountFailed");
            NSDictionary userInfo = notification.userInfo();
            NSError error = Rococoa.cast(userInfo.objectForKey(GMUserFileSystem.kGMUserFileSystemErrorKey), NSError.class);
            session.error(Locale.localizedString("Mount failed", "Error"), null);
        }

        public NSDictionary attributesOfFileSystemForPath_error(String path, long error) {
            log.debug("attributesOfFileSystemForPath_error:" + path);
            final NSMutableDictionary attributes = NSMutableDictionary.dictionary();
            attributes.setObjectForKey(NSNumber.numberWithInt(0),
                    NSFileManager.NSFileSystemSize);
            attributes.setObjectForKey(NSNumber.numberWithInt(0),
                    NSFileManager.NSFileSystemFreeSize);
            attributes.setObjectForKey(NSNumber.numberWithInt(0),
                    NSFileManager.NSFileSystemNodes);
            attributes.setObjectForKey(NSNumber.numberWithInt(0),
                    NSFileManager.NSFileSystemFreeNodes);
            attributes.setObjectForKey(NSNumber.numberWithBoolean(false),
                    "kGMUserFileSystemVolumeSupportsExtendedDatesKey");
            return attributes;
        }

        public NSArray contentsOfDirectoryAtPath_error(final String path, long/*ObjCObjectByReference*/ error) {
            log.debug("contentsOfDirectoryAtPath_error:" + path);
            final Future<NSArray> future = background(new FilesystemBackgroundAction<NSArray>(session) {
                final Path directory = PathFactory.createPath(session, path, Path.DIRECTORY_TYPE);

                @Override
                public NSArray call() {
                    if(directory.exists()) {
                        final NSMutableArray contents = NSMutableArray.array();
                        for(AbstractPath child : directory.children()) {
                            contents.addObject(child.getName());
                        }
                        return contents;
                    }
                    return null;
                }

                @Override
                public String getActivity() {
                    return MessageFormat.format(Locale.localizedString("Listing directory {0}", "Status"),
                            directory.getName());
                }
            });
            try {
                return future.get();
            }
            catch(InterruptedException e) {
                log.error("Error executing action for mounted disk:" + e.getMessage());
            }
            catch(ExecutionException e) {
                log.error("Error executing action for mounted disk:" + e.getMessage());
            }
            return null;
        }

        public NSDictionary attributesOfItemAtPath_userData_error(final String path, ID userData, long/*ObjCObjectByReference*/ error) {
            log.debug("attributesOfItemAtPath_userData_error:" + path);
            final NSMutableDictionary attributes = NSMutableDictionary.dictionary();
            final Future<NSDictionary> future = background(new FilesystemBackgroundAction<NSDictionary>(session) {
                @Override
                public NSDictionary call() {
                    final Path selected = PathFactory.createPath(session, path, Path.DIRECTORY_TYPE);
                    if(selected.isRoot()) {
                        attributes.setObjectForKey(NSFileManager.NSFileTypeDirectory,
                                NSFileManager.NSFileType);
                        return attributes;
                    }
                    final Path directory = selected.getParent();
                    final AbstractPath file = directory.children().get(new OutlinePathReference(NSString.stringWithString(path)));
                    if(null == file) {
                        log.error("Lookup failed for:" + path);
                        return null;
                    }
                    if(!file.exists()) {
                        return null;
                    }
                    attributes.setObjectForKey(file.attributes().isDirectory() ? NSFileManager.NSFileTypeDirectory : NSFileManager.NSFileTypeRegular,
                            NSFileManager.NSFileType);
                    attributes.setObjectForKey(NSNumber.numberWithFloat(file.attributes().getSize()),
                            NSFileManager.NSFileSize);
                    attributes.setObjectForKey(NSNumber.numberWithBoolean(false),
                            NSFileManager.NSFileExtensionHidden);
                    attributes.setObjectForKey(NSDate.dateWithTimeIntervalSince1970(file.attributes().getModificationDate() / 1000),
                            NSFileManager.NSFileModificationDate);
                    attributes.setObjectForKey(NSDate.dateWithTimeIntervalSince1970(file.attributes().getCreationDate() / 1000),
                            NSFileManager.NSFileCreationDate);
                    attributes.setObjectForKey(NSNumber.numberWithInt(Integer.valueOf(file.attributes().getPermission().getOctalString(), 8)),
                            NSFileManager.NSFilePosixPermissions);

                    return attributes;
                }
            });
            try {
                return future.get();
            }
            catch(InterruptedException e) {
                log.error("Error executing action for mounted disk:" + e.getMessage());
            }
            catch(ExecutionException e) {
                log.error("Error executing action for mounted disk:" + e.getMessage());
            }
            return null;
        }

        public boolean setAttributes_ofItemAtPath_userData_error(final NSDictionary attributes, final String path, ID userData, long/*ObjCObjectByReference*/ error) {
            log.debug("setAttributes_ofItemAtPath_userData_error:" + path);
            final Future<Boolean> future = background(new FilesystemBackgroundAction<Boolean>(session) {
                @Override
                public Boolean call() {
                    final Path selected = PathFactory.createPath(session, path, Path.DIRECTORY_TYPE);
                    if(selected.isRoot()) {
                        return false;
                    }
                    final Path directory = selected.getParent();
                    final AbstractPath file = directory.children().get(new OutlinePathReference(NSString.stringWithString(path)));
                    if(null == file) {
                        log.error("Lookup failed for:" + path);
                        return false;
                    }
                    if(!file.exists()) {
                        return false;
                    }
                    if(session.isUnixPermissionsSupported()) {
                        final NSObject posixNumber = attributes.objectForKey(NSFileManager.NSFilePosixPermissions);
                        if(null != posixNumber) {
                            String posixString = Integer.toOctalString(Rococoa.cast(posixNumber, NSNumber.class).intValue());
                            final Permission permission = new Permission(Integer.parseInt(posixString.substring(posixString.length() - 3)));
                            file.writeUnixPermission(permission, false);
                        }
                    }
                    if(session.isWriteTimestampSupported()) {
                        NSObject modificationNumber = attributes.objectForKey(NSFileManager.NSFileModificationDate);
                        if(null != modificationNumber) {
                            final long modification = (long) (Rococoa.cast(modificationNumber, NSDate.class).timeIntervalSince1970() * 1000);
                            file.writeTimestamp(modification, modification, modification);
                        }
                    }
                    return true;
                }
            });
            try {
                future.get();
            }
            catch(InterruptedException e) {
                log.error("Error executing action for mounted disk:" + e.getMessage());
            }
            catch(ExecutionException e) {
                log.error("Error executing action for mounted disk:" + e.getMessage());
            }
            return true;
        }

        public boolean createDirectoryAtPath_attributes_error(final String path, NSDictionary attributes, long/*ObjCObjectByReference*/ error) {
            log.debug("createDirectoryAtPath_attributes_error:" + path);
            final Future<Boolean> future = background(new FilesystemBackgroundAction<Boolean>(session) {
                @Override
                public Boolean call() {
                    final Path directory = PathFactory.createPath(session, path, Path.DIRECTORY_TYPE);
                    if(session.isCreateFolderSupported(directory.getParent())) {
                        directory.mkdir();
                        return true;
                    }
                    return false;
                }
            });
            try {
                return future.get();
            }
            catch(InterruptedException e) {
                log.error("Error executing action for mounted disk:" + e.getMessage());
            }
            catch(ExecutionException e) {
                log.error("Error executing action for mounted disk:" + e.getMessage());
            }
            return false;
        }

        public boolean createFileAtPath_attributes_userData_error(final String path, NSDictionary attributes, ID userData, long/*ObjCObjectByReference*/ error) {
            log.debug("createFileAtPath_attributes_userData_error:" + path);
            final Future<Boolean> future = background(new FilesystemBackgroundAction<Boolean>(session) {
                @Override
                public Boolean call() {
                    final Path file = PathFactory.createPath(session, path, Path.DIRECTORY_TYPE);
                    if(session.isCreateFileSupported(file.getParent())) {
                        file.touch();
                        return true;
                    }
                    return false;
                }
            });
            try {
                return future.get();
            }
            catch(InterruptedException e) {
                log.error("Error executing action for mounted disk:" + e.getMessage());
            }
            catch(ExecutionException e) {
                log.error("Error executing action for mounted disk:" + e.getMessage());
            }
            return false;
        }

        public boolean removeDirectoryAtPath_error(final String path, long/*ObjCObjectByReference*/ error) {
            log.debug("removeDirectoryAtPath_error:" + path);
            final Future<Boolean> future = background(new FilesystemBackgroundAction<Boolean>(session) {
                @Override
                public Boolean call() {
                    final Path file = PathFactory.createPath(session, path, Path.DIRECTORY_TYPE);
                    file.delete();
                    return true;
                }
            });
            try {
                return future.get();
            }
            catch(InterruptedException e) {
                log.error("Error executing action for mounted disk:" + e.getMessage());
            }
            catch(ExecutionException e) {
                log.error("Error executing action for mounted disk:" + e.getMessage());
            }
            return false;
        }

        public boolean removeItemAtPath_error(final String path, long/*ObjCObjectByReference*/ error) {
            log.debug("removeItemAtPath_error:" + path);
            final Future<Boolean> future = background(new FilesystemBackgroundAction<Boolean>(session) {
                @Override
                public Boolean call() {
                    final Path file = PathFactory.createPath(session, path, Path.FILE_TYPE);
                    if(!file.exists()) {
                        return false;
                    }
                    file.delete();
                    return true;
                }
            });
            try {
                return future.get();
            }
            catch(InterruptedException e) {
                log.error("Error executing action for mounted disk:" + e.getMessage());
            }
            catch(ExecutionException e) {
                log.error("Error executing action for mounted disk:" + e.getMessage());
            }
            return false;
        }

        public boolean moveItemAtPath_toPath_error(final String source, final String destination, long/*ObjCObjectByReference*/ error) {
            log.debug("moveItemAtPath_toPath_error:" + source);
            final Future<Boolean> future = background(new FilesystemBackgroundAction<Boolean>(session) {
                @Override
                public Boolean call() {
                    final Path file = PathFactory.createPath(session, source, Path.FILE_TYPE);
                    if(!file.exists()) {
                        return false;
                    }
                    if(!session.isRenameSupported(file)) {
                        return false;
                    }
                    file.rename(PathFactory.createPath(session, destination, Path.FILE_TYPE));
                    return true;
                }
            });
            try {
                return future.get();
            }
            catch(InterruptedException e) {
                log.error("Error executing action for mounted disk:" + e.getMessage());
            }
            catch(ExecutionException e) {
                log.error("Error executing action for mounted disk:" + e.getMessage());
            }
            return false;
        }

        public boolean openFileAtPath_mode_userData_error(final String path, int mode, ID userData, long/*ObjCObjectByReference*/ error) {
            log.debug("openFileAtPath_mode_userData_error:" + path);
            // Need no action before read
            return false;
        }

        public int readFileAtPath_userData_buffer_size_offset_error(String path, ID userData, Pointer buffer,
                                                                    IntegerType size, IntegerType offset, long/*ObjCObjectByReference*/ error) {
            return 0;
        }

        public int writeFileAtPath_userData_buffer_size_offset_error(String path, ID userData, Pointer buffer,
                                                                     IntegerType size, IntegerType offset, long/*ObjCObjectByReference*/ error) {
            return 0;
        }
    }
}
