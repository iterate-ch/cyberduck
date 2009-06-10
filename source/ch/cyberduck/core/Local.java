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

import ch.cyberduck.core.i18n.Locale;
import ch.cyberduck.core.io.FileWatcher;
import ch.cyberduck.core.io.FileWatcherListener;
import ch.cyberduck.core.io.RepeatableFileInputStream;
import ch.cyberduck.ui.cocoa.CDMainApplication;
import ch.cyberduck.ui.cocoa.application.NSWorkspace;
import ch.cyberduck.ui.cocoa.foundation.*;
import ch.cyberduck.ui.cocoa.threading.DefaultMainAction;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.rococoa.Rococoa;

import java.io.*;
import java.net.MalformedURLException;

/**
 * @version $Id$
 */
public class Local extends AbstractPath {
    private static Logger log = Logger.getLogger(Local.class);

    {
        attributes = new Attributes() {
            public Permission getPermission() {
                try {
                    NSDictionary fileAttributes = NSFileManager.defaultManager().fileAttributes(
                            _impl.getAbsolutePath());
                    if(null == fileAttributes) {
                        log.error("No such file:" + getAbsolute());
                        return null;
                    }
                    NSObject object = fileAttributes.objectForKey(NSFileManager.NSFilePosixPermissions);
                    if(null == object) {
                        log.error("No such file:" + getAbsolute());
                        return null;
                    }
                    NSNumber posix = Rococoa.cast(object, NSNumber.class);
                    String posixString = Integer.toString(posix.intValue() & 0177777, 8);
                    return new Permission(Integer.parseInt(posixString.substring(posixString.length() - 3)));
                }
                catch(NumberFormatException e) {
                    return Permission.EMPTY;
                }
            }

            public void setPermission(Permission p) {
                ;
            }

            public boolean isVolume() {
                return null == _impl.getParent();
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

            public void setSize(long size) {
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
                NSDictionary fileAttributes = NSFileManager.defaultManager().fileAttributes(_impl.getAbsolutePath());
                // If flag is true and path is a symbolic link, the attributes of the linked-to file are returned;
                // if the link points to a nonexistent file, this method returns null. If flag is false,
                // the attributes of the symbolic link are returned.
                if(null == fileAttributes) {
                    log.error("No such file:" + getAbsolute());
                    return -1;
                }
                NSObject date = fileAttributes.objectForKey(NSFileManager.NSFileCreationDate);
                if(null == date) {
                    // Returns an entry’s value given its key, or null if no value is associated with key.
                    log.error("No such file:" + getAbsolute());
                    return -1;
                }
                return (long) (Rococoa.cast(date, NSDate.class).timeIntervalSince1970() * 1000);
            }

            public void setCreationDate(long millis) {
                NSDate date = NSDate.dateWithTimeIntervalSince1970(millis / 1000);
                boolean success = NSFileManager.defaultManager().changeFileAttributes(
                        NSDictionary.dictionaryWithObjectsForKeys(
                                NSArray.arrayWithObject(date),
                                NSArray.arrayWithObject(NSFileManager.NSFileCreationDate)),
                        _impl.getAbsolutePath());
                if(!success) {
                    log.error("File attribute changed failed:" + getAbsolute());
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

            public long getSize() {
                if(this.isDirectory()) {
                    return 0;
                }
                return _impl.length();
            }
        };
    }

    private static final Object lock = new Object();

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
                    throw e;
                }
            }
            return JNI_LOADED;
        }
    }

    protected File _impl;

    public Local(Local parent, String name) {
        this(parent.getAbsolute(), name);
    }

    public Local(String parent, String name) {
        if(!Path.DELIMITER.equals(name)) {
            name = name.replace('/', ':');
        }
        // See trac #933
        this.setPath(parent, name);
    }

    public Local(String path) {
        this.setPath(path);
    }

    public Local(File path) {
        this.setPath(path.getAbsolutePath());
    }

    private void init() {
        if(!Local.jni_load()) {
            return;
        }
//        FileForker forker = new MacOSXForker();
//        forker.usePathname(new Pathname(_impl.getAbsoluteFile()));
//        if(forker.isAlias()) {
//            try {
//                this.setPath(forker.makeResolved().getPath());
//            }
//            catch(IOException e) {
//                log.error("Error resolving alias:" + e.getMessage());
//            }
//        }
    }

    /**
     * @param listener
     */
    public void watch(FileWatcherListener listener) throws IOException {
        FileWatcher.instance().watch(this, listener);
    }

    public boolean isReadable() {
        return _impl.canRead();
    }

    public boolean isWritable() {
        return _impl.canWrite();
    }

    /**
     * Creates a new file and sets its resource fork to feature a custom progress icon
     *
     * @return
     */
    public boolean touch() {
        if(!this.exists()) {
            try {
                if(_impl.createNewFile()) {
                    this.setIcon(0);
                }
            }
            catch(IOException e) {
                log.error(e.getMessage());
            }
        }
        return false;
    }

    public void delete() {
        this.delete(true);
    }

    /**
     * @param trash Moves the file to the Trash. NSWorkspace.RecycleOperation
     */
    public void delete(final boolean trash) {
        if(this.exists()) {
            if(trash) {
                final Local file = this;
                CDMainApplication.invoke(new DefaultMainAction() {
                    public void run() {
                        log.debug("Move " + file + " to Trash");
                        if(!NSWorkspace.sharedWorkspace().performFileOperation(
                                NSWorkspace.RecycleOperation,
                                file.getParent().getAbsolute(), "",
                                NSArray.arrayWithObject(file.getName()))) {
                            log.warn("Failed to move " + file.getAbsolute() + " to Trash");
                        }
                    }
                });
            }
            else {
                _impl.delete();
            }
        }
    }

    /**
     * @return Always return false
     */
    public boolean isCached() {
        return false;
    }

    private Cache<Local> cache = new Cache<Local>();

    public Cache<Local> cache() {
        return this.cache;
    }

    public AttributedList<Local> list() {
        final AttributedList<Local> childs = new AttributedList<Local>();
        File[] files = _impl.listFiles();
        if(null == files) {
            log.error("_impl.listFiles == null");
            return childs;
        }
        for(File file : files) {
            childs.add(new Local(file));
        }
        return childs;
    }


    /**
     * @return the file type for the extension of this file provided by launch services
     */
    public String kind() {
        if(this.attributes.isDirectory()) {
            return Locale.localizedString("Folder");
        }
        final String extension = this.getExtension();
        if(StringUtils.isEmpty(extension)) {
            return Locale.localizedString("Unknown");
        }
        if(!Local.jni_load()) {
            return Locale.localizedString("Unknown");
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
        return _impl.exists();
    }

    public void setPath(String name) {
        _impl = new File(Path.normalize(NSString.stringByExpandingTildeInPath(name)));
        this.init();
    }

    public void mkdir(boolean recursive) {
        if(recursive) {
            _impl.mkdirs();
        }
        else {
            _impl.mkdir();
        }
    }

    public void writePermissions(final Permission perm, final boolean recursive) {
        CDMainApplication.invoke(new DefaultMainAction() {
            public void run() {
                boolean success = NSFileManager.defaultManager().changeFileAttributes(
                        NSDictionary.dictionaryWithObjectsForKeys(
                                NSArray.arrayWithObject(NSNumber.numberWithInt(perm.getOctalNumber())),
                                NSArray.arrayWithObject(NSFileManager.NSFilePosixPermissions)),
                        _impl.getAbsolutePath());
                if(!success) {
                    log.error("File attribute changed failed:" + getAbsolute());
                }
                if(attributes.isDirectory() && recursive) {
                    for(AbstractPath child : childs()) {
                        child.writePermissions(perm, recursive);
                    }
                }
            }
        });
    }

    public void writeModificationDate(final long millis) {
        CDMainApplication.invoke(new DefaultMainAction() {
            public void run() {
                boolean success = NSFileManager.defaultManager().changeFileAttributes(
                        NSDictionary.dictionaryWithObjectsForKeys(
                                NSArray.arrayWithObject(NSDate.dateWithTimeIntervalSince1970(millis / 1000)),
                                NSArray.arrayWithObject(NSFileManager.NSFileModificationDate)),
                        _impl.getAbsolutePath());
                if(!success) {
                    log.error("File attribute changed failed:" + getAbsolute());
                }
            }
        });
    }

    public void rename(AbstractPath renamed) {
        _impl.renameTo(new File(this.getParent().getAbsolute(), renamed.getAbsolute()));
        this.setPath(this.getParent().getAbsolute(), renamed.getAbsolute());
    }

    public void copy(AbstractPath copy) {
        if(copy.equals(this)) {
            return;
        }
        FileInputStream in = null;
        FileOutputStream out = null;
        try {
            in = new FileInputStream(_impl);
            out = new FileOutputStream(copy.getAbsolute());
            IOUtils.copy(in, out);
        }
        catch(IOException e) {
            log.error(e.getMessage());
        }
        finally {
            IOUtils.closeQuietly(in);
            IOUtils.closeQuietly(out);
        }
    }

    /**
     * @param originUrl The URL of the resource originally hosting the quarantined item, from the user's point of
     *                  view. For web downloads, this property is the URL of the web page on which the user initiated
     *                  the download. For attachments, this property is the URL of the resource to which the quarantined
     *                  item was attached (e.g. the email message, calendar event, etc.). The origin URL may be a file URL
     *                  for local resources, or a custom URL to which the quarantining application will respond when asked
     *                  to open it. The quarantining application should respond by displaying the resource to the user.
     *                  Note: The origin URL should not be set to the data URL, or the quarantining application may start
     *                  downloading the file again if the user choses to view the origin URL while resolving a quarantine
     *                  warning.
     * @param dataUrl   The URL from which the data for the quarantined item data was
     *                  actaully streamed or downloaded, if available
     */
    public void setQuarantine(final String originUrl, final String dataUrl) {
        this.setQuarantine(this.getAbsolute(), originUrl, dataUrl);
    }

    /**
     * UKXattrMetadataStore
     *
     * @param path
     * @param key
     * @param value
     */
    private native void setQuarantine(String path, String originUrl, String dataUrl);

    /**
     * Set the kMDItemWhereFroms on the file.
     *
     * @param dataUrl
     */
    public void setWhereFrom(final String dataUrl) {
        this.setWhereFrom(this.getAbsolute(), dataUrl);
    }

    /**
     * Set the kMDItemWhereFroms on the file.
     *
     * @param path
     * @param dataUrl
     */
    private native void setWhereFrom(String path, String dataUrl);

    /**
     * Update the custom icon for the file in the Finder
     *
     * @param progress An integer from -1 and 9. If -1 is passed,
     *                 the resource fork with the custom icon is removed from the file.
     */
    public void setIcon(final int progress) {
        if(progress > 9 || progress < -1) {
            log.warn("Local#setIcon:" + progress);
            return;
        }
        if(Preferences.instance().getBoolean("queue.download.updateIcon")) {
            if(!Local.jni_load()) {
                return;
            }
            final String path = this.getAbsolute();
            CDMainApplication.invoke(new DefaultMainAction() {
                public void run() {
                    if(-1 == progress) {
                        removeResourceFork();
                    }
                    else {
                        setIconFromFile(path, "download" + progress + ".icns");
                    }
                }
            });
        }
        // Disabled because of #221
        // NSWorkspace.sharedWorkspace().noteFileSystemChanged(this.getAbsolute());
    }

    /**
     * Removes the resource fork from the file alltogether
     */
    private void removeResourceFork() {
        this.removeCustomIcon();
//        try {
//            FileForker forker = new MacOSXForker();
//            forker.usePathname(new Pathname(_impl.getAbsoluteFile()));
//            forker.makeForkOutputStream(true, false).close();
//        }
//        catch(IOException e) {
//            log.error("Failed to remove resource fork from file:" + e.getMessage());
//        }
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
        if(null == other) {
            return false;
        }
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
            return _impl.toURI().toURL().toString();
        }
        catch(MalformedURLException e) {
            log.error(e.getMessage());
            return null;
        }
    }

    /**
     * The default application for this file as set by the launch services
     *
     * @return Full path to the application bundle. Null if unknown
     */
    public String getDefaultEditor() {
        if(!Local.jni_load()) {
            return null;
        }
        final String extension = this.getExtension();
        if(StringUtils.isEmpty(extension)) {
            return null;
        }
        return this.applicationForExtension(extension);
    }

    private native String applicationForExtension(String extension);

    public static class OutputStream extends FileOutputStream {
        public OutputStream(Local local, boolean resume) throws FileNotFoundException {
            super(local._impl, resume);
        }
    }

    public static class InputStream extends RepeatableFileInputStream {
        public InputStream(Local local) throws FileNotFoundException {
            super(local._impl);
        }
    }
}