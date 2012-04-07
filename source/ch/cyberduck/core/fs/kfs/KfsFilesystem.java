package ch.cyberduck.core.fs.kfs;

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

import ch.cyberduck.core.AbstractPath;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.LocalFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathFactory;
import ch.cyberduck.core.Permission;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.fs.Filesystem;
import ch.cyberduck.core.fs.FilesystemBackgroundAction;
import ch.cyberduck.core.fs.FilesystemFactory;
import ch.cyberduck.ui.cocoa.ProxyController;
import ch.cyberduck.ui.cocoa.foundation.NSString;
import ch.cyberduck.ui.cocoa.model.OutlinePathReference;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

/**
 * @version $Id$
 */
public final class KfsFilesystem extends ProxyController implements Filesystem {
    private static Logger log = Logger.getLogger(KfsFilesystem.class);

    public static void register() {
        FilesystemFactory.addFactory(Factory.NATIVE_PLATFORM, new Factory());
    }

    private static class Factory extends FilesystemFactory {
        @Override
        protected KfsFilesystem create() {
            return new KfsFilesystem();
        }
    }

    private Session session;

    private KfsFilesystem() {
        ;
    }

    /**
     * Reference to mounted filesystem
     */
    private long identifier;

    private KfsLibrary.kfsfilesystem delegate;

    public void mount(Session s) {
        session = s;
        filesystem = KfsLibrary.INSTANCE;
        delegate = new KfsLibrary.kfsfilesystem();
        delegate.statfs = new KfsLibrary.kfsstatfs_f() {
            public boolean apply(final String path, final KfsLibrary.kfsstatfs stat, Pointer context) {
                final Future<Boolean> future = background(new FilesystemBackgroundAction<Boolean>(session) {
                    @Override
                    public Boolean call() {
                        log.debug("kfsstatfs_f:" + path);
                        final Path selected = PathFactory.createPath(session, path, Path.DIRECTORY_TYPE);
                        if(selected.isRoot()) {
                            stat.free = -1;
                            stat.size = -1;
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
        };
        delegate.stat = new KfsLibrary.kfsstat_f() {
            public boolean apply(final String path, final KfsLibrary.kfsstat stat, Pointer context) {
                final Future<Boolean> future = background(new FilesystemBackgroundAction<Boolean>(session) {
                    @Override
                    public Boolean call() {
                        log.debug("kfsstat_f:" + path);
                        final Path selected = PathFactory.createPath(session, path, Path.DIRECTORY_TYPE);
                        if(selected.isRoot()) {
                            stat.type = KfsLibrary.kfstype_t.KFS_DIR;
                            final long time = System.currentTimeMillis();
                            stat.mtime = new KfsLibrary.kfstime(time / 1000, 0);
                            stat.atime = new KfsLibrary.kfstime(time / 1000, 0);
                            stat.mode |= KfsLibrary.kfsmode_t.KFS_IRUSR;
                            stat.mode |= KfsLibrary.kfsmode_t.KFS_IWUSR;
                            stat.mode |= KfsLibrary.kfsmode_t.KFS_IXUSR;
                            stat.size = -1;
                            return true;
                        }
                        final Path directory = selected.getParent();
                        if(!directory.isCached()) {
                            log.warn("Return empty stat for directory not cached:" + path);
                            return false;
                        }
                        final AbstractPath file = directory.children().get(new OutlinePathReference(NSString.stringWithString(path)));
                        if(null == file) {
                            log.warn("Lookup failed for:" + path);
                            return false;
                        }
                        if(!file.exists()) {
                            return false;
                        }
                        stat.type = file.attributes().isDirectory() ? KfsLibrary.kfstype_t.KFS_DIR : KfsLibrary.kfstype_t.KFS_REG;
                        if(session.isWriteTimestampSupported()) {
                            stat.mtime = new KfsLibrary.kfstime(file.attributes().getModificationDate() / 1000, 0);
                            stat.atime = new KfsLibrary.kfstime(file.attributes().getAccessedDate() / 1000, 0);
                            stat.ctime = new KfsLibrary.kfstime(file.attributes().getCreationDate() / 1000, 0);
                        }
                        else {
                            stat.mtime = new KfsLibrary.kfstime();
                            stat.atime = new KfsLibrary.kfstime();
                            stat.ctime = new KfsLibrary.kfstime();
                        }
                        stat.size = file.attributes().getSize();
                        if(session.isUnixPermissionsSupported()) {
                            final Permission permission = file.attributes().getPermission();
                            if(permission.getOwnerPermissions()[Permission.READ]) {
                                stat.mode |= KfsLibrary.kfsmode_t.KFS_IRUSR;
                            }
                            if(permission.getOwnerPermissions()[Permission.WRITE]) {
                                stat.mode |= KfsLibrary.kfsmode_t.KFS_IWUSR;
                            }
                            if(permission.getOwnerPermissions()[Permission.EXECUTE]) {
                                stat.mode |= KfsLibrary.kfsmode_t.KFS_IXUSR;
                            }
                            if(permission.getGroupPermissions()[Permission.READ]) {
                                stat.mode |= KfsLibrary.kfsmode_t.KFS_IRGRP;
                            }
                            if(permission.getGroupPermissions()[Permission.WRITE]) {
                                stat.mode |= KfsLibrary.kfsmode_t.KFS_IWGRP;
                            }
                            if(permission.getGroupPermissions()[Permission.EXECUTE]) {
                                stat.mode |= KfsLibrary.kfsmode_t.KFS_IXGRP;
                            }
                            if(permission.getOtherPermissions()[Permission.READ]) {
                                stat.mode |= KfsLibrary.kfsmode_t.KFS_IROTH;
                            }
                            if(permission.getOtherPermissions()[Permission.WRITE]) {
                                stat.mode |= KfsLibrary.kfsmode_t.KFS_IWOTH;
                            }
                            if(permission.getOtherPermissions()[Permission.EXECUTE]) {
                                stat.mode |= KfsLibrary.kfsmode_t.KFS_IXOTH;
                            }
                        }
                        else {
                            // Apply default permissions
                            stat.mode |= KfsLibrary.kfsmode_t.KFS_IRUSR;
                            stat.mode |= KfsLibrary.kfsmode_t.KFS_IWUSR;
                            stat.mode |= KfsLibrary.kfsmode_t.KFS_IXUSR;
                        }
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
        };
        delegate.readdir = new KfsLibrary.kfsreaddir_f() {
            public boolean apply(final String path, final Pointer contents, Pointer context) {
                final Future<Boolean> future = background(new FilesystemBackgroundAction<Boolean>(session) {
                    @Override
                    public Boolean call() {
                        log.debug("kfsreaddir_f:" + path);
                        final Path directory = PathFactory.createPath(session, path, Path.DIRECTORY_TYPE);
                        if(directory.exists()) {
                            for(AbstractPath child : directory.children()) {
                                filesystem.kfscontents_append(contents, child.getName());
                            }
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
        };
        delegate.read = new KfsLibrary.kfsread_f() {
            public KfsLibrary.size_t apply(final String path, final Pointer buf, final KfsLibrary.size_t offset, final KfsLibrary.size_t length, Pointer context) {
                final Future<KfsLibrary.size_t> future = background(new FilesystemBackgroundAction<KfsLibrary.size_t>(session) {
                    @Override
                    public KfsLibrary.size_t call() {
                        log.debug("kfsread_f:" + path);
                        final Path file = PathFactory.createPath(session, path, Path.FILE_TYPE);
                        file.status().setResume(false);
                        try {
                            final InputStream in = file.read(true);
                            try {
                                long total = 0;
                                byte[] chunk = new byte[length.intValue()];
                                int read = in.read(chunk, 0, length.intValue());
                                if(-1 == read) {
                                    log.debug("Read complete:" + path);
                                }
                                else {
                                    buf.write(offset.intValue(), chunk, 0, read);
                                    total += read;
                                }
                                return new KfsLibrary.size_t(read);
                            }
                            finally {
                                IOUtils.closeQuietly(in);
                            }
                        }
                        catch(IOException e) {
                            log.error(e.getMessage());
                        }
                        return new KfsLibrary.size_t(-1);
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
                return new KfsLibrary.size_t(-1);
            }
        };
        delegate.write = new KfsLibrary.kfswrite_f() {
            public KfsLibrary.size_t apply(final String path, final Pointer buf, final KfsLibrary.size_t offset, final KfsLibrary.size_t length, Pointer context) {
                final Future<KfsLibrary.size_t> future = background(new FilesystemBackgroundAction<KfsLibrary.size_t>(session) {
                    @Override
                    public KfsLibrary.size_t call() {
                        log.debug("kfswrite_f:" + path);
//                        final Path file = PathFactory.createPath(session, path, Path.FILE_TYPE);
//                        try {
//                            final OutputStream out = file.write();
//                            try {
//                                byte[] chunk = new byte[length.intValue()];
//                                buf.read(offset.longValue(), chunk, 0, length.intValue());
//                                out.write(chunk, 0, length.intValue());
//                                return new KfsLibrary.size_t(length.longValue());
//                            }
//                            finally {
//                                IOUtils.closeQuietly(out);
//                            }
//                        }
//                        catch(IOException e) {
//                            log.error(e.getMessage());
//                        }
                        return new KfsLibrary.size_t(-1);
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
                return new KfsLibrary.size_t(-1);
            }
        };
        delegate.symlink = new KfsLibrary.kfssymlink_f() {
            public boolean apply(final String path, String value, Pointer context) {
                log.debug("kfssymlink_f:" + path);
                // Not supported
                return false;
            }
        };
        delegate.readlink = new KfsLibrary.kfsreadlink_f() {
            public boolean apply(final String path, PointerByReference value, Pointer context) {
                log.debug("kfsreadlink_f:" + path);
                // Not supported
                return false;
            }
        };
        delegate.create = new KfsLibrary.kfscreate_f() {
            public boolean apply(final String path, Pointer context) {
                final Future<Boolean> future = background(new FilesystemBackgroundAction<Boolean>(session) {
                    @Override
                    public Boolean call() {
                        log.debug("kfscreate_f:" + path);
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
        };
        delegate.remove = new KfsLibrary.kfsremove_f() {
            public boolean apply(final String path, Pointer context) {
                final Future<Boolean> future = background(new FilesystemBackgroundAction<Boolean>(session) {
                    @Override
                    public Boolean call() {
                        log.debug("kfsremove_f:" + path);
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
        };
        delegate.rename = new KfsLibrary.kfsrename_f() {
            public boolean apply(final String path, final String destination, Pointer context) {
                final Future<Boolean> future = background(new FilesystemBackgroundAction<Boolean>(session) {
                    @Override
                    public Boolean call() {
                        log.debug("kfsrename_f:" + path);
                        final Path file = PathFactory.createPath(session, path, Path.FILE_TYPE);
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
        };
        delegate.remove = new KfsLibrary.kfsremove_f() {
            public boolean apply(final String path, Pointer context) {
                final Future<Boolean> future = background(new FilesystemBackgroundAction<Boolean>(session) {
                    @Override
                    public Boolean call() {
                        log.debug("kfsremove_f:" + path);
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
        };
        delegate.truncate = new KfsLibrary.kfstruncate_f() {
            public boolean apply(final String path, long size, Pointer context) {
                log.debug("kfstruncate_f:" + path);
                // Not supported
                return false;
            }
        };
        delegate.chmod = new KfsLibrary.kfschmod_f() {
            public boolean apply(final String path, int mode, Pointer context) {
                log.debug("kfschmod_f:" + path);
                // Not supported
                return true;
            }
        };
        delegate.utimes = new KfsLibrary.kfsutimes_f() {
            public boolean apply(final String path, KfsLibrary.kfstime atime, KfsLibrary.kfstime mtime, Pointer context) {
                log.debug("kfsutimes_f:" + path);
                // Not supported
                return true;
            }
        };
        delegate.mkdir = new KfsLibrary.kfsmkdir_f() {
            public boolean apply(final String path, Pointer context) {
                final Future<Boolean> future = background(new FilesystemBackgroundAction<Boolean>(session) {
                    @Override
                    public Boolean call() {
                        log.debug("kfsmkdir_f:" + path);
                        final Path directory = PathFactory.createPath(session, path, Path.DIRECTORY_TYPE);
                        directory.mkdir();
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
        };
        delegate.rmdir = new KfsLibrary.kfsrmdir_f() {
            public boolean apply(final String path, Pointer context) {
                final Future<Boolean> future = background(new FilesystemBackgroundAction<Boolean>(session) {
                    @Override
                    public Boolean call() {
                        log.debug("kfsrmdir_f:" + path);
                        final Path directory = PathFactory.createPath(session, path, Path.DIRECTORY_TYPE);
                        if(directory.exists()) {
                            directory.delete();
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
        };
        final String volume = session.getHost().getHostname();
        Local target = LocalFactory.createLocal("/Volumes/" + volume);
        if(target.exists()) {
            // Make sure we do not mount to a already existing path
            final String parent = target.getParent().getAbsolute();
            int no = 0;
            while(target.exists()) {
                no++;
                String proposal = volume + "-" + no;
                target = LocalFactory.createLocal(parent, proposal);
            }
        }
        final Local mountpoint = target;
        delegate.options = new KfsLibrary.kfsoptions(mountpoint.getAbsolute());
        final Future<Void> future = background(new FilesystemBackgroundAction<Void>(this) {
            @Override
            public Void call() {
                identifier = filesystem.kfs_mount(delegate);
                // Must wait for mount notification
                while(true) {
                    if(mountpoint.exists()) {
                        break;
                    }
                }
                try {
                    Path workdir = session.home();
                    Local folder = LocalFactory.createLocal(new File(delegate.options.mountpoint, workdir.getAbsolute()));
                    folder.reveal();
                }
                catch(IOException e) {
                    log.warn(e.getMessage());
                }
                return null;
            }
        });
    }

    public void unmount() {
        log.debug("unmount");
        filesystem.kfs_unmount(identifier);
    }

    /**
     *
     */
    private KfsLibrary filesystem;
}
