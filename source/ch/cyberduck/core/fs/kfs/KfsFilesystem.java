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

import ch.cyberduck.core.Cache;
import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledProgressListener;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.LocalFactory;
import ch.cyberduck.core.NSObjectPathReference;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Permission;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.features.Move;
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.features.Timestamp;
import ch.cyberduck.core.features.Touch;
import ch.cyberduck.core.features.UnixPermission;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.fs.Filesystem;
import ch.cyberduck.core.fs.FilesystemBackgroundAction;
import ch.cyberduck.core.local.RevealService;
import ch.cyberduck.core.local.RevealServiceFactory;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.ui.cocoa.ProxyController;
import ch.cyberduck.ui.cocoa.foundation.NSString;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.rococoa.ObjCObjectByReference;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.EnumSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import com.sun.jna.Pointer;

/**
 * @version $Id$
 */
public final class KfsFilesystem extends ProxyController implements Filesystem {
    private static final Logger log = Logger.getLogger(KfsFilesystem.class);

    private Session<?> session;

    private Cache<Path> cache = Cache.empty();

    private RevealService reveal = RevealServiceFactory.get();

    private KfsFilesystem() {
        //
    }

    /**
     * Reference to mounted filesystem
     */
    private long identifier;

    private KfsLibrary.kfsfilesystem delegate;

    @Override
    public void mount(final Session s) {
        session = s;
        filesystem = KfsLibrary.INSTANCE;
        delegate = new KfsLibrary.kfsfilesystem();
        delegate.statfs = new KfsLibrary.kfsstatfs_f() {
            @Override
            public boolean apply(final String path, final KfsLibrary.kfsstatfs stat, Pointer context) {
                final Future<Boolean> future = background(new FilesystemBackgroundAction<Boolean>(session, cache) {
                    @Override
                    public Boolean run() {
                        log.debug("kfsstatfs_f:" + path);
                        final Path selected = new Path(path, EnumSet.of(Path.Type.directory));
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
            @Override
            public boolean apply(final String path, final KfsLibrary.kfsstat stat, Pointer context) {
                final Future<Boolean> future = background(new FilesystemBackgroundAction<Boolean>(session, cache) {
                    @Override
                    public Boolean run() throws BackgroundException {
                        log.debug("kfsstat_f:" + path);
                        final Path selected = new Path(path, EnumSet.of(Path.Type.directory));
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
                        final Path file = session.list(directory, new DisabledListProgressListener()).get(new NSObjectPathReference(NSString.stringWithString(path)));
                        stat.type = file.isDirectory() ? KfsLibrary.kfstype_t.KFS_DIR : KfsLibrary.kfstype_t.KFS_REG;
                        if(session.getFeature(Timestamp.class) != null) {
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
                        if(session.getFeature(UnixPermission.class) != null) {
                            final Permission permission = file.attributes().getPermission();
                            if(permission.getUser().implies(Permission.Action.read)) {
                                stat.mode |= KfsLibrary.kfsmode_t.KFS_IRUSR;
                            }
                            if(permission.getUser().implies(Permission.Action.write)) {
                                stat.mode |= KfsLibrary.kfsmode_t.KFS_IWUSR;
                            }
                            if(permission.getUser().implies(Permission.Action.execute)) {
                                stat.mode |= KfsLibrary.kfsmode_t.KFS_IXUSR;
                            }
                            if(permission.getGroup().implies(Permission.Action.read)) {
                                stat.mode |= KfsLibrary.kfsmode_t.KFS_IRGRP;
                            }
                            if(permission.getGroup().implies(Permission.Action.write)) {
                                stat.mode |= KfsLibrary.kfsmode_t.KFS_IWGRP;
                            }
                            if(permission.getGroup().implies(Permission.Action.execute)) {
                                stat.mode |= KfsLibrary.kfsmode_t.KFS_IXGRP;
                            }
                            if(permission.getOther().implies(Permission.Action.read)) {
                                stat.mode |= KfsLibrary.kfsmode_t.KFS_IROTH;
                            }
                            if(permission.getOther().implies(Permission.Action.write)) {
                                stat.mode |= KfsLibrary.kfsmode_t.KFS_IWOTH;
                            }
                            if(permission.getOther().implies(Permission.Action.execute)) {
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
            @Override
            public boolean apply(final String path, final Pointer contents, Pointer context) {
                final Future<Boolean> future = background(new FilesystemBackgroundAction<Boolean>(session, cache) {
                    @Override
                    public Boolean run() throws BackgroundException {
                        log.debug("kfsreaddir_f:" + path);
                        final Path directory = new Path(path, EnumSet.of(Path.Type.directory));
                        for(Path child : session.list(directory, new DisabledListProgressListener())) {
                            filesystem.kfscontents_append(contents, child.getName());
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
        delegate.read = new KfsLibrary.kfsread_f() {
            @Override
            public KfsLibrary.size_t apply(final String path, final Pointer buf, final KfsLibrary.size_t offset, final KfsLibrary.size_t length, Pointer context) {
                final Future<KfsLibrary.size_t> future = background(new FilesystemBackgroundAction<KfsLibrary.size_t>(session, cache) {
                    @Override
                    public KfsLibrary.size_t run() throws BackgroundException {
                        log.debug("kfsread_f:" + path);
                        final Path file = new Path(path, EnumSet.of(Path.Type.file));
                        final TransferStatus status = new TransferStatus();
                        try {
                            final InputStream in = session.getFeature(Read.class).read(file, status);
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
                            throw new DefaultIOExceptionMappingService().map(e);
                        }
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
            @Override
            public KfsLibrary.size_t apply(final String path, final Pointer buf, final KfsLibrary.size_t offset, final KfsLibrary.size_t length, Pointer context) {
                final Future<KfsLibrary.size_t> future = background(new FilesystemBackgroundAction<KfsLibrary.size_t>(session, cache) {
                    @Override
                    public KfsLibrary.size_t run() throws BackgroundException {
                        log.debug("kfswrite_f:" + path);
                        final Path file = new Path(path, EnumSet.of(Path.Type.file));
                        try {
                            final TransferStatus status = new TransferStatus();
                            final OutputStream out = session.getFeature(Write.class).write(file, status);
                            try {
                                byte[] chunk = new byte[length.intValue()];
                                buf.read(offset.longValue(), chunk, 0, length.intValue());
                                out.write(chunk, 0, length.intValue());
                                return new KfsLibrary.size_t(length.longValue());
                            }
                            finally {
                                IOUtils.closeQuietly(out);
                            }
                        }
                        catch(IOException e) {
                            throw new DefaultIOExceptionMappingService().map(e);
                        }
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
            @Override
            public boolean apply(final String path, String value, Pointer context) {
                log.debug("kfssymlink_f:" + path);
                // Not supported
                return false;
            }
        };
        delegate.readlink = new KfsLibrary.kfsreadlink_f() {
            @Override
            public boolean apply(final String path, ObjCObjectByReference value, Pointer context) {
                log.debug("kfsreadlink_f:" + path);
                // Not supported
                return false;
            }
        };
        delegate.create = new KfsLibrary.kfscreate_f() {
            @Override
            public boolean apply(final String path, Pointer context) {
                final Future<Boolean> future = background(new FilesystemBackgroundAction<Boolean>(session, cache) {
                    @Override
                    public Boolean run() throws BackgroundException {
                        log.debug("kfscreate_f:" + path);
                        final Path file = new Path(path, EnumSet.of(Path.Type.file));
                        final Touch feature = session.getFeature(Touch.class);
                        if(feature.isSupported(file.getParent())) {
                            feature.touch(file);
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
            @Override
            public boolean apply(final String path, Pointer context) {
                final Future<Boolean> future = background(new FilesystemBackgroundAction<Boolean>(session, cache) {
                    @Override
                    public Boolean run() throws BackgroundException {
                        log.debug("kfsremove_f:" + path);
                        final Path file = new Path(path, EnumSet.of(Path.Type.file));
                        session.getFeature(Delete.class).delete(
                                Collections.singletonList(file), new DisabledLoginCallback(), new DisabledProgressListener());
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
            @Override
            public boolean apply(final String path, final String destination, Pointer context) {
                final Future<Boolean> future = background(new FilesystemBackgroundAction<Boolean>(session, cache) {
                    @Override
                    public Boolean run() throws BackgroundException {
                        log.debug("kfsrename_f:" + path);
                        final Path file = new Path(path, EnumSet.of(Path.Type.file));
                        if(!session.getFeature(Move.class).isSupported(file)) {
                            return false;
                        }
                        session.getFeature(Move.class).move(file, new Path(destination, EnumSet.of(Path.Type.file)), false, new DisabledProgressListener());
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
            @Override
            public boolean apply(final String path, long size, Pointer context) {
                log.debug("kfstruncate_f:" + path);
                // Not supported
                return false;
            }
        };
        delegate.chmod = new KfsLibrary.kfschmod_f() {
            @Override
            public boolean apply(final String path, int mode, Pointer context) {
                log.debug("kfschmod_f:" + path);
                // Not supported
                return true;
            }
        };
        delegate.utimes = new KfsLibrary.kfsutimes_f() {
            @Override
            public boolean apply(final String path, KfsLibrary.kfstime atime, KfsLibrary.kfstime mtime, Pointer context) {
                log.debug("kfsutimes_f:" + path);
                // Not supported
                return true;
            }
        };
        delegate.mkdir = new KfsLibrary.kfsmkdir_f() {
            @Override
            public boolean apply(final String path, Pointer context) {
                final Future<Boolean> future = background(new FilesystemBackgroundAction<Boolean>(session, cache) {
                    @Override
                    public Boolean run() throws BackgroundException {
                        log.debug("kfsmkdir_f:" + path);
                        final Path directory = new Path(path, EnumSet.of(Path.Type.directory));
                        final Directory feature = session.getFeature(Directory.class);
                        feature.mkdir(directory);
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
            @Override
            public boolean apply(final String path, Pointer context) {
                final Future<Boolean> future = background(new FilesystemBackgroundAction<Boolean>(session, cache) {
                    @Override
                    public Boolean run() throws BackgroundException {
                        log.debug("kfsrmdir_f:" + path);
                        final Path directory = new Path(path, EnumSet.of(Path.Type.directory));
                        session.getFeature(Delete.class).delete(
                                Collections.singletonList(directory), new DisabledLoginCallback(), new DisabledProgressListener());
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
        final String volume = session.getHost().getHostname();
        Local target = LocalFactory.get("/Volumes/" + volume);
        if(target.exists()) {
            // Make sure we do not mount to a already existing path
            final String parent = target.getParent().getAbsolute();
            int no = 0;
            while(target.exists()) {
                no++;
                String proposal = String.format("%s-%d", volume, no);
                target = LocalFactory.get(parent, proposal);
            }
        }
        final Local mountpoint = target;
        delegate.options = new KfsLibrary.kfsoptions(mountpoint.getAbsolute());
        final Future<Void> future = background(new FilesystemBackgroundAction<Void>(session, cache) {
            @Override
            public Void run() throws BackgroundException {
                identifier = filesystem.kfs_mount(delegate);
                // Must wait for mount notification
                while(true) {
                    if(mountpoint.exists()) {
                        break;
                    }
                }
                final Path workdir = session.workdir();
                final Local folder = LocalFactory.get(delegate.options.mountpoint, workdir.getAbsolute());
                reveal.reveal(folder);
                return null;
            }
        });
    }

    @Override
    public void unmount() {
        log.debug("unmount");
        filesystem.kfs_unmount(identifier);
    }

    /**
     *
     */
    private KfsLibrary filesystem;
}
