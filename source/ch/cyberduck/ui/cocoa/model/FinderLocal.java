package ch.cyberduck.ui.cocoa.model;

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

import ch.cyberduck.core.*;
import ch.cyberduck.core.threading.DefaultMainAction;
import ch.cyberduck.ui.cocoa.IconCache;
import ch.cyberduck.ui.cocoa.ProxyController;
import ch.cyberduck.ui.cocoa.application.NSWorkspace;
import ch.cyberduck.ui.cocoa.foundation.*;

import org.rococoa.Rococoa;
import org.rococoa.cocoa.foundation.NSUInteger;

import org.apache.commons.collections.map.AbstractLinkedMap;
import org.apache.commons.collections.map.LRUMap;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.*;

/**
 * @version $Id$
 */
public class FinderLocal extends Local {
    private static Logger log = Logger.getLogger(FinderLocal.class);

    public FinderLocal(Local parent, String name) {
        super(parent, name);
    }

    public FinderLocal(String parent, String name) {
        super(parent, name);
    }

    public FinderLocal(String path) {
        super(path);
    }

    public FinderLocal(File path) {
        super(path);
    }

    public static void register() {
        LocalFactory.addFactory(Factory.NATIVE_PLATFORM, new Factory());
    }

    private static class Factory extends LocalFactory {
        @Override
        protected Local create() {
            return new FinderLocal(System.getProperty("user.home"));
        }

        @Override
        protected Local create(Local parent, String name) {
            return new FinderLocal(parent, name);
        }

        @Override
        protected Local create(String parent, String name) {
            return new FinderLocal(parent, name);
        }

        @Override
        protected Local create(String path) {
            return new FinderLocal(path);
        }

        @Override
        protected Local create(File path) {
            return new FinderLocal(path);
        }
    }

    @Override
    protected void setPath(String name) {
        if(loadNative()) {
            super.setPath(this.resolveAlias(stringByExpandingTildeInPath(name)));
        }
        else {
            super.setPath(stringByExpandingTildeInPath(name));
        }
    }

    @Override
    public void setPath(String parent, String name) {
        if(!String.valueOf(this.getPathDelimiter()).equals(name)) {
            // See #933
            name = name.replace(this.getPathDelimiter(), ':');
        }
        super.setPath(stringByExpandingTildeInPath(parent), name);
    }

    /**
     * @return Name of the file as displayed in the Finder. E.g. a ':' is replaced with '/'.
     */
    @Override
    public String getDisplayName() {
        return NSFileManager.defaultManager().displayNameAtPath(super.getName());
    }

    /**
     * @return Path relative to the home directory denoted with a tilde.
     */
    @Override
    public String getAbbreviatedPath() {
        return stringByAbbreviatingWithTildeInPath(this.getAbsolute());
    }

    @Override
    public boolean exists() {
        return NSFileManager.defaultManager().fileExistsAtPath(this.getAbsolute());
    }

    private static boolean JNI_LOADED = false;

    private static boolean loadNative() {
        if(!JNI_LOADED) {
            JNI_LOADED = Native.load("Local");
        }
        return JNI_LOADED;
    }

    /**
     * @param absolute The absolute path of the alias file.
     * @return The absolute path this alias is pointing to.
     */
    private native String resolveAlias(String absolute);

    /**
     * Executable, readable and writable flags based on <code>NSFileManager</code>.
     */
    private class FinderLocalPermission extends Permission {
        public FinderLocalPermission(int octal) {
            super(octal);
        }

        @Override
        public boolean isExecutable() {
            return NSFileManager.defaultManager().isExecutableFileAtPath(FinderLocal.this.getAbsolute());
        }

        @Override
        public boolean isReadable() {
            return NSFileManager.defaultManager().isReadableFileAtPath(FinderLocal.this.getAbsolute());
        }

        @Override
        public boolean isWritable() {
            return NSFileManager.defaultManager().isWritableFileAtPath(FinderLocal.this.getAbsolute());
        }
    }

    /**
     * Extending attributes with <code>NSFileManager</code>.
     *
     * @see ch.cyberduck.ui.cocoa.foundation.NSFileManager
     */
    private FinderLocalAttributes attributes;

    /**
     * Uses <code>NSFileManager</code> for reading file attributes.
     */
    private class FinderLocalAttributes extends LocalAttributes {
        /**
         * @return Null if no such file.
         */
        private NSDictionary getNativeAttributes() {
            if(!exists()) {
                return null;
            }
            // If flag is true and path is a symbolic link, the attributes of the linked-to file are returned;
            // if the link points to a nonexistent file, this method returns null. If flag is false,
            // the attributes of the symbolic link are returned.
            return NSFileManager.defaultManager().fileAttributesAtPath_traverseLink(
                    _impl.getAbsolutePath(), false);
        }

        /**
         * @param name
         * @return Null if no such file or attribute.
         */
        private NSObject getNativeAttribute(String name) {
            NSDictionary dict = this.getNativeAttributes();
            if(null == dict) {
                log.error("No such file:" + getAbsolute());
                return null;
            }
            // Returns an entry’s value given its key, or null if no value is associated with key.
            return dict.objectForKey(name);
        }

        @Override
        public long getSize() {
            if(this.isDirectory()) {
                return -1;
            }
            NSObject size = this.getNativeAttribute(NSFileManager.NSFileSize);
            if(null == size) {
                return -1;
            }
            // Refer to #5503 and http://code.google.com/p/rococoa/issues/detail?id=3
            return (long) Rococoa.cast(size, NSNumber.class).doubleValue();
        }

        @Override
        public Permission getPermission() {
            try {
                NSObject object = this.getNativeAttribute(NSFileManager.NSFilePosixPermissions);
                if(null == object) {
                    return Permission.EMPTY;
                }
                NSNumber posix = Rococoa.cast(object, NSNumber.class);
                String posixString = Integer.toString(posix.intValue() & 0177777, 8);
                return new FinderLocalPermission(Integer.parseInt(posixString.substring(posixString.length() - 3)));
            }
            catch(NumberFormatException e) {
                log.error(e.getMessage());
            }
            return Permission.EMPTY;
        }

        /**
         * Read <code>NSFileCreationDate</code>.
         *
         * @return Milliseconds since 1970
         */
        @Override
        public long getCreationDate() {
            NSObject object = this.getNativeAttribute(NSFileManager.NSFileCreationDate);
            if(null == object) {
                return -1;
            }
            return (long) (Rococoa.cast(object, NSDate.class).timeIntervalSince1970() * 1000);
        }

        @Override
        public long getAccessedDate() {
            return -1;
        }

        @Override
        public String getOwner() {
            NSObject object = this.getNativeAttribute(NSFileManager.NSFileOwnerAccountName);
            if(null == object) {
                return super.getOwner();
            }
            return object.toString();
        }

        @Override
        public String getGroup() {
            NSObject object = this.getNativeAttribute(NSFileManager.NSFileGroupOwnerAccountName);
            if(null == object) {
                return super.getGroup();
            }
            return object.toString();
        }

        /**
         * @return The value for the key NSFileSystemFileNumber, or 0 if the receiver doesn’t have an entry for the key
         */
        public long getInode() {
            NSObject object = this.getNativeAttribute(NSFileManager.NSFileSystemFileNumber);
            if(null == object) {
                return 0;
            }
            NSNumber number = Rococoa.cast(object, NSNumber.class);
            return number.longValue();
        }

        @Override
        public boolean isBundle() {
            return NSWorkspace.sharedWorkspace().isFilePackageAtPath(getAbsolute());
        }
    }

    @Override
    public FinderLocalAttributes attributes() {
        if(null == attributes) {
            attributes = new FinderLocalAttributes();
        }
        return attributes;
    }

    /**
     * @return The file type for the extension of this file provided by launch services
     *         if the path is a file.
     */
    @Override
    public String kind() {
        String suffix = this.getExtension();
        if(StringUtils.isEmpty(suffix)) {
            return super.kind();
        }
        // Native file type mapping
        final String kind = kind(suffix);
        if(StringUtils.isEmpty(kind)) {
            return super.kind();
        }
        return kind;
    }

    public static native String kind(String extension);

    @Override
    public void writeUnixPermission(final Permission perm, final boolean recursive) {
        new ProxyController().invoke(new DefaultMainAction() {
            public void run() {
                boolean success = NSFileManager.defaultManager().setAttributes_ofItemAtPath_error(
                        NSDictionary.dictionaryWithObjectsForKeys(
                                NSArray.arrayWithObject(NSNumber.numberWithInt(perm.getOctalNumber())),
                                NSArray.arrayWithObject(NSFileManager.NSFilePosixPermissions)),
                        _impl.getAbsolutePath(), null);
                if(!success) {
                    log.error("File attribute changed failed:" + getAbsolute());
                }
                if(attributes().isDirectory() && recursive) {
                    for(AbstractPath child : children()) {
                        child.writeUnixPermission(perm, recursive);
                    }
                }
            }
        });
    }

    /**
     * Write <code>NSFileModificationDate</code>.
     *
     * @param created
     * @param modified
     * @param accessed
     */
    @Override
    public void writeTimestamp(final long created, final long modified, final long accessed) {
        new ProxyController().invoke(new DefaultMainAction() {
            public void run() {
                boolean success = NSFileManager.defaultManager().setAttributes_ofItemAtPath_error(
                        NSDictionary.dictionaryWithObjectsForKeys(
                                NSArray.arrayWithObject(NSDate.dateWithTimeIntervalSince1970(modified / 1000)),
                                NSArray.arrayWithObject(NSFileManager.NSFileModificationDate)),
                        _impl.getAbsolutePath(), null);
                if(!success) {
                    log.error("File attribute changed failed:" + getAbsolute());
                }
            }
        });
    }

    /**
     * Move file to trash on main interface thread using <code>NSWorkspace.RecycleOperation</code>.
     */
    @Override
    public void trash() {
        if(this.exists()) {
            final Local file = this;
            new ProxyController().invoke(new DefaultMainAction() {
                public void run() {
                    log.debug("Move " + file + " to Trash");
                    if(!NSWorkspace.sharedWorkspace().performFileOperation(
                            NSWorkspace.RecycleOperation,
                            file.getParent().getAbsolute(), StringUtils.EMPTY,
                            NSArray.arrayWithObject(file.getName()))) {
                        log.warn("Failed to move " + file.getAbsolute() + " to Trash");
                    }
                }
            });
        }
    }

    @Override
    public boolean reveal() {
        // If a second path argument is specified, a new file viewer is opened. If you specify an
        // empty string (@"") for this parameter, the file is selected in the main viewer.
        return NSWorkspace.sharedWorkspace().selectFile(this.getAbsolute(), this.getParent().getAbsolute());
    }

    /**
     * Comparing by inode if the file exists.
     *
     * @param o
     * @return
     */
    @Override
    public boolean equals(Object o) {
        if(o instanceof FinderLocal) {
            if(!this.exists()) {
                return super.equals(o);
            }
            FinderLocal other = (FinderLocal) o;
            if(!other.exists()) {
                return super.equals(o);
            }
            return this.attributes().getInode() == other.attributes().getInode();
        }
        return super.equals(o);
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
    @Override
    public void setQuarantine(final String originUrl, final String dataUrl) {
        if(!loadNative()) {
            return;
        }
        if(StringUtils.isEmpty(originUrl)) {
            log.warn("No origin url given for quarantine");
            return;
        }
        if(StringUtils.isEmpty(dataUrl)) {
            log.warn("No data url given for quarantine");
            return;
        }
        new ProxyController().invoke(new DefaultMainAction() {
            public void run() {
                setQuarantine(getAbsolute(), originUrl, dataUrl);
            }
        });
    }

    /**
     * UKXattrMetadataStore
     *
     * @param path      Absolute path reference
     * @param originUrl Page that linked to the downloaded file
     * @param dataUrl   Href where the file was downloaded from
     */
    private native void setQuarantine(String path, String originUrl, String dataUrl);

    /**
     * Set the kMDItemWhereFroms on the file.
     *
     * @param dataUrl Href where the file was downloaded from
     */
    @Override
    public void setWhereFrom(final String dataUrl) {
        if(!loadNative()) {
            return;
        }
        if(StringUtils.isEmpty(dataUrl)) {
            log.warn("No data url given for quarantine");
            return;
        }
        new ProxyController().invoke(new DefaultMainAction() {
            public void run() {
                setWhereFrom(getAbsolute(), dataUrl);
            }
        });
    }

    /**
     * Set the kMDItemWhereFroms on the file.
     *
     * @param path    Absolute path reference
     * @param dataUrl Href where the file was downloaded from
     */
    private native void setWhereFrom(String path, String dataUrl);

    /**
     * Update the custom icon for the file in the Finder
     *
     * @param progress An integer from -1 and 9. If -1 is passed,
     *                 the resource fork with the custom icon is removed from the file.
     */
    @Override
    public void setIcon(final int progress) {
        if(progress > 9 || progress < -1) {
            log.warn("Local#setIcon:" + progress);
            return;
        }
        if(Preferences.instance().getBoolean("queue.download.updateIcon")) {
            if(!loadNative()) {
                return;
            }
            final String path = this.getAbsolute();
            new ProxyController().invoke(new DefaultMainAction() {
                public void run() {
                    if(-1 == progress) {
                        NSWorkspace.sharedWorkspace().setIcon_forFile_options(
                                null, getAbsolute(), new NSUInteger(0));
                    }
                    else {
                        // Specify 0 if you want to generate icons in all available icon representation formats
                        NSWorkspace.sharedWorkspace().setIcon_forFile_options(
                                IconCache.iconNamed("download" + progress + ".icns"), getAbsolute(), new NSUInteger(0));
                    }
                }
            });
        }
    }

    private static String stringByAbbreviatingWithTildeInPath(String string) {
        return NSString.stringByAbbreviatingWithTildeInPath(string);
    }

    private static String stringByExpandingTildeInPath(String string) {
        return NSString.stringByExpandingTildeInPath(string);
    }

    /**
     *
     */
    private static Map<String, String> defaultApplicationCache
            = Collections.<String, String>synchronizedMap(new LRUMap(20) {
        @Override
        protected boolean removeLRU(AbstractLinkedMap.LinkEntry entry) {
            log.debug("Removing from cache:" + entry);
            return true;
        }
    });

    /**
     * The default application for this file as set by the launch services
     *
     * @return The bundle identifier of the default application to open the
     *         file of this type or null if unknown
     */
    @Override
    public String getDefaultApplication() {
        final String extension = this.getExtension();
        if(!defaultApplicationCache.containsKey(extension)) {
            if(StringUtils.isEmpty(extension)) {
                return null;
            }
            final String path = this.applicationForExtension(extension);
            if(StringUtils.isEmpty(path)) {
                defaultApplicationCache.put(extension, null);
            }
            else {
                NSBundle bundle = NSBundle.bundleWithPath(path);
                if(null == bundle) {
                    log.error("Loading bundle failed:" + path);
                    defaultApplicationCache.put(extension, null);
                }
                else {
                    defaultApplicationCache.put(extension, bundle.bundleIdentifier());
                }
            }
        }
        return defaultApplicationCache.get(extension);
    }

    /**
     * Uses LSGetApplicationForInfo
     *
     * @param extension
     * @return Null if not found
     */
    protected native String applicationForExtension(String extension);

    /**
     * Caching map between application bundle identifiers and
     * file type extensions.
     */
    private static Map<String, List<String>> defaultApplicationListCache
            = Collections.<String, List<String>>synchronizedMap(new LRUMap(20) {
        @Override
        protected boolean removeLRU(AbstractLinkedMap.LinkEntry entry) {
            log.debug("Removing from cache:" + entry);
            return true;
        }
    });

    /**
     * @eturn All of the application bundle identifiers that are capable of handling
     * the specified content type in the specified roles.
     */
    @Override
    public List<String> getDefaultApplications() {
        final String extension = this.getExtension();
        if(StringUtils.isEmpty(extension)) {
            return Collections.emptyList();
        }
        if(!defaultApplicationListCache.containsKey(extension)) {
            final List<String> applications = new ArrayList<String>(Arrays.asList(
                    this.applicationListForExtension(extension)));
            // Because of the different API used the default opening application may not be included
            // in the above list returned. Always add the default application anyway.
            final String defaultApplication = this.getDefaultApplication();
            if(null != defaultApplication) {
                if(!applications.contains(defaultApplication)) {
                    applications.add(defaultApplication);
                }
            }
            defaultApplicationListCache.put(extension, applications);
        }
        return defaultApplicationListCache.get(extension);
    }

    /**
     * Uses LSCopyAllRoleHandlersForContentType
     *
     * @param extension
     * @return Empty array if none found
     */
    protected native String[] applicationListForExtension(String extension);

    @Override
    public boolean open() {
        return NSWorkspace.sharedWorkspace().openFile(this.getAbsolute());
    }

    /**
     * Post a download finished notification to the distributed notification center. Will cause the
     * download folder to bounce just once.
     */
    @Override
    public void bounce() {
        NSDistributedNotificationCenter.defaultCenter().postNotification(
                NSNotification.notificationWithName("com.apple.DownloadFileFinished", this.getAbsolute())
        );
    }

    @Override
    public String toString() {
        return stringByAbbreviatingWithTildeInPath(this.getAbsolute());
    }
}