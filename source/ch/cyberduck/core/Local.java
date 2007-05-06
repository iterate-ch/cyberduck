package ch.cyberduck.core;

/*
 *  Copyright (c) 2005 David Kocher. All rights reserved.
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

import glguerin.io.FileForker;
import glguerin.io.Pathname;
import glguerin.io.imp.mac.macosx.MacOSXForker;

import com.apple.cocoa.application.NSWorkspace;
import com.apple.cocoa.foundation.NSBundle;
import com.apple.cocoa.foundation.NSDate;
import com.apple.cocoa.foundation.NSDictionary;
import com.apple.cocoa.foundation.NSPathUtilities;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;

/**
 * @version $Id$
 */
public class Local extends AbstractPath {
    private static Logger log = Logger.getLogger(Local.class);

    {
        attributes = new Attributes() {
            public Permission getPermission() {
                NSDictionary fileAttributes = NSPathUtilities.fileAttributes(_impl.getAbsolutePath(), true);
                if(null == fileAttributes) {
                    log.error("No such file:"+getAbsolute());
                    return null;
                }
                Object posix = fileAttributes.objectForKey(NSPathUtilities.FilePosixPermissions);
                if(null == posix) {
                    log.error("No such file:"+getAbsolute());
                    return null;
                }
                return new Permission(Integer.parseInt(Integer.toOctalString(((Number) posix).intValue())));
            }

            public void setPermission(Permission p) {
                ;
            }

            public boolean isDirectory() {
                return _impl.isDirectory();
            }

            public boolean isFile() {
                return _impl.isFile();
            }

            /**
             * Checks whether a given file is a symbolic link.
             * <p/>
             * <p>It doesn't really test for symbolic links but whether the
             * canonical and absolute paths of the file are identical - this
             * may lead to false positives on some platforms.</p>
             *
             * @return true if the file is a symbolic link.
             */
            public boolean isSymbolicLink() {
                if(!Local.this.exists()) {
                    return false;
                }
                // For a link that actually points to something (either a file or a directory),
                // the absolute path is the path through the link, whereas the canonical path
                // is the path the link references.
                try {
                    return !_impl.getAbsolutePath().equals(_impl.getCanonicalPath());
                }
                catch(IOException e) {
                    return false;
                }
            }

            public void setType(int i) {
                ;
            }

            public void setSize(double size) {
                ;
            }

            public void setOwner(String owner) {
                ;
            }

            public void setGroup(String group) {
                ;
            }

            public String getOwner() {
                return null;
            }

            public String getGroup() {
                return null;
            }

            public long getModificationDate() {
                return _impl.lastModified();
            }

            public void setModificationDate(long millis) {
                ;
            }

            public long getCreationDate() {
                NSDictionary fileAttributes = NSPathUtilities.fileAttributes(_impl.getAbsolutePath(), true);
                // If flag is true and path is a symbolic link, the attributes of the linked-to file are returned;
                // if the link points to a nonexistent file, this method returns null. If flag is false,
                // the attributes of the symbolic link are returned.
                if(null == fileAttributes) {
                    log.error("No such file:"+getAbsolute());
                    return -1;
                }
                Object date = fileAttributes.objectForKey(NSPathUtilities.FileCreationDate);
                if(null == date) {
                    // Returns an entry’s value given its key, or null if no value is associated with key.
                    log.error("No such file:"+getAbsolute());
                    return -1;
                }
                return NSDate.timeIntervalToMilliseconds(((NSDate)date).timeIntervalSinceDate(NSDate.DateFor1970));
            }

            public void setCreationDate(long millis) {
                boolean success = NSPathUtilities.setFileAttributes(_impl.getAbsolutePath(),
                        new NSDictionary(new NSDate(NSDate.millisecondsToTimeInterval(millis), NSDate.DateFor1970),
                                NSPathUtilities.FileCreationDate));
                if(!success) {
                    log.error("File attribute changed failed:"+getAbsolute());
                }
            }

            public long getAccessedDate() {
                return this.getModificationDate();
            }

            public void setAccessedDate(long millis) {
                ;
            }

            public int getType() {
                final int t = this.isFile() ? AbstractPath.FILE_TYPE : AbstractPath.DIRECTORY_TYPE;
                if(this.isSymbolicLink()) {
                    return t | AbstractPath.SYMBOLIC_LINK_TYPE;
                }
                return t;
            }

            public double getSize() {
                if(this.isDirectory()) {
                    return 0;
                }
                return _impl.length();
            }
        };
    }

    private static boolean JNI_LOADED = false;

    private static boolean jni_load() {
        synchronized(lock) {
            if(!JNI_LOADED) {
                try {
                    NSBundle bundle = NSBundle.mainBundle();
                    String lib = bundle.resourcePath() + "/Java/" + "libLocal.dylib";
                    log.info("Locating libLocal.dylib at '" + lib + "'");
                    System.load(lib);
                    JNI_LOADED = true;
                    log.info("libLocal.dylib loaded");
                }
                catch(UnsatisfiedLinkError e) {
                    log.error("Could not load the libLocal.dylib library:" + e.getMessage());
                }
            }
            return JNI_LOADED;
        }
    }

    protected File _impl;

    public Local(Local parent, String name) {
        // See trac #933
        _impl = new File(NSPathUtilities.stringByExpandingTildeInPath(parent.getAbsolute()),
                name.replace('/', ':'));
    }

    public Local(String parent, String name) {
        // See trac #933
        _impl = new File(NSPathUtilities.stringByExpandingTildeInPath(parent),
                name.replace('/', ':'));
    }

    public Local(String path) {
        _impl = new File(NSPathUtilities.stringByExpandingTildeInPath(path));
    }

    public Local(File path) {
        _impl = new File(NSPathUtilities.stringByExpandingTildeInPath(path.getAbsolutePath()));
    }

//    private FileWatcher uk;
//
//    /**
//     *
//     * @param listener
//     */
//    public void watch(FileWatcherListener listener) {
//        if(null == uk) {
//            uk = FileWatcher.instance(this);
//        }
//        uk.watch(listener);
//    }

    public boolean isReadable() {
        return _impl.canRead();
    }

    public boolean isWritable() {
        return _impl.canWrite();
    }

    /**
     * Creates a new file and sets its resource fork to feature a custom progress icon
     * @return
     */
    public boolean createNewFile() {
        try {
            if(_impl.createNewFile()) {
                this.setIcon(0);
            }
        }
        catch(IOException e) {
            log.error(e.getMessage());
        }
        return false;
    }

//    /**
//     * @param time     Modification date measured in milliseconds since 00:00:00 <code>timezone</code>, January 1, 1970
//     * @param timezone
//     * @return <code>true</code> if and only if the operation succeeded;
//     *         <code>false</code> otherwise
//     */
//    public boolean setLastModified(final long time, final TimeZone timezone) {
//        super.setLastModified(time);
//        int offset = TimeZone.getDefault().getRawOffset() /*amount of raw offset time in milliseconds to add to UTC*/
//                - timezone.getOffset(time); /*amount of time in milliseconds to add to UTC to get local time*/
//        return super.setLastModified(time + offset);
//    }

    /**
     * @param recursively If true, descend into directories and delete recursively
     * @return  <code>true</code> if and only if the file or directory is
     *          successfully deleted; <code>false</code> otherwise
     */
    public boolean delete(boolean recursively) {
        if(!recursively) {
            return _impl.delete();
        }
        return this.deleteImpl(_impl);
    }

    /**
     * Recursively deletes this file
     * @return  <code>true</code> if and only if the file or directory is
     *          successfully deleted; <code>false</code> otherwise
     */
    private boolean deleteImpl(File f) {
        if(f.isDirectory()) {
            File[] files = f.listFiles();
            for(int i = 0; i < files.length; i++) {
                this.deleteImpl(files[i]);
            }
        }
        return f.delete();
    }

    private Cache cache = new Cache();

    public Cache cache() {
        return this.cache;
    }

    public AttributedList list(ListParseListener listener) {
        final AttributedList childs = new AttributedList();
        File[] f = _impl.listFiles();
        for(int i = 0; i < f.length; i++) {
            childs.add(new Local(f[i]));
        }
        return childs;
    }

    /**
     * @return the file type for the extension of this file provided by launch services
     */
    public String kind() {
        if(this.attributes.isDirectory()) {
            return NSBundle.localizedString("Folder", "");
        }
        final String extension = this.getExtension();
        if(null == extension) {
            return NSBundle.localizedString("Unknown", "");
        }
        if(!Local.jni_load()) {
            return NSBundle.localizedString("Unknown", "");
        }
        return this.kind(this.getExtension());
    }

    /**
     * @param extension
     * @return
     */
    private native String kind(String extension);

    public String getAbsolute() {
        return _impl.getAbsolutePath();
    }

    public String getSymbolicLinkPath() {
        try {
            return _impl.getCanonicalPath();
        }
        catch(IOException e) {
            log.error(e.getMessage());
            return this.getAbsolute();
        }
    }

    public String getName() {
        return _impl.getName();
    }

    public AbstractPath getParent() {
        return new Local(_impl.getParentFile());
    }

    public boolean exists() {
        boolean v =_impl.exists();
        if(v) {
            if(this.attributes.isFile()) {
                return this.attributes.getSize() > 0;
            }
        }
        return v;
    }

    public void setPath(String name) {
        _impl = new File(Path.normalize(name));
    }

    public void mkdir(boolean recursive) {
        if(recursive) _impl.mkdirs(); else _impl.mkdir();
    }

    public void writePermissions(Permission perm, boolean recursive) {
        boolean success = NSPathUtilities.setFileAttributes(_impl.getAbsolutePath(),
                new NSDictionary(new Integer(perm.getOctalNumber()),
                        NSPathUtilities.FilePosixPermissions));
        if(!success) {
            log.error("File attribute changed failed:"+getAbsolute());
        }
        if(this.attributes.isDirectory() && recursive) {
            for(Iterator iter = this.childs().iterator(); iter.hasNext(); ) {
                Local child = (Local)iter.next();
                child.writePermissions(perm, recursive);
            }
        }
    }

    public void writeModificationDate(long millis) {
        boolean success = NSPathUtilities.setFileAttributes(_impl.getAbsolutePath(),
                new NSDictionary(new NSDate(NSDate.millisecondsToTimeInterval(millis), NSDate.DateFor1970),
                        NSPathUtilities.FileModificationDate));
        if(!success) {
            log.error("File attribute changed failed:"+getAbsolute());
        }
    }

    public void delete() {
        _impl.delete();
    }

    public void rename(String name) {
        _impl.renameTo(new File(this.getParent().getAbsolute(), name));
    }

    public void cwdir() throws IOException {
        ;
    }

    private final static Object lock = new Object();

    /**
     * Update the custom icon for the file in the Finder
     * @param progress An integer from -1 and 9. If -1 is passed,
     * the resource fork with the custom icon is removed from the file.
     */
    public void setIcon(int progress) {
        if(progress > 9 || progress < -1) {
            log.warn("Local#setIcon:"+progress);
            return;
        }
        if(Preferences.instance().getBoolean("queue.download.updateIcon")) {
            synchronized(lock) {
                if(!Local.jni_load()) {
                    return;
                }
                if(-1 == progress) {
                    this.removeResourceFork();
                }
                else {
                    this.setIconFromFile(this.getAbsolute(), "download" + progress + ".icns");
                }
            }
        }
        // Disabled because of #221
        // NSWorkspace.sharedWorkspace().noteFileSystemChangedAtPath(this.getAbsolute());
    }

    /**
     * Removes the resource fork from the file alltogether
     */
    private void removeResourceFork() {
        try {
            this.removeCustomIcon();
            FileForker forker = new MacOSXForker();
            forker.usePathname(new Pathname(_impl.getAbsoluteFile()));
            forker.makeForkOutputStream(true, false).close();
        }
        catch(IOException e) {
            log.error("Failed to remove resource fork from file:" + e.getMessage());
        }
    }

    /**
     * @param icon the absolute path to the image file to use as an icon
     */
    private native void setIconFromFile(String path, String icon);

    private void removeCustomIcon() {
        if(!Local.jni_load()) {
            return;
        }
        this.removeCustomIcon(this.getAbsolute());
    }

    private native void removeCustomIcon(String path);

    public int hashCode() {
        return _impl.getAbsolutePath().hashCode();
    }

    public boolean equals(Object other) {
        if(other instanceof Local) {
            return this.getAbsolute().equalsIgnoreCase(((AbstractPath) other).getAbsolute());
        }
        return false;
    }

    public String toString() {
        return this.getAbsolute();
    }

    public String toURL() {
        try {
            return _impl.toURL().toString();
        }
        catch(MalformedURLException e) {
            log.error(e.getMessage());
            return null;
        }
    }

    public static class OutputStream extends FileOutputStream {
        public OutputStream(Local local, boolean resume) throws FileNotFoundException {
            super(local._impl, resume);
        }
    }

    public static class InputStream extends FileInputStream {
        public InputStream(Local local) throws FileNotFoundException {
            super(local._impl);
        }
    }
}
