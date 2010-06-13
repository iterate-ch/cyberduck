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

import org.apache.commons.collections.map.AbstractLinkedMap;
import org.apache.commons.collections.map.LRUMap;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.rococoa.Rococoa;
import org.rococoa.cocoa.foundation.NSUInteger;

import java.io.File;
import java.util.*;

/**
 * @version $Id$
 */
public class FinderLocal extends Local {
    private static Logger log = Logger.getLogger(FinderLocal.class);

    public FinderLocal(ch.cyberduck.core.Local parent, String name) {
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
        protected Local create(java.io.File path) {
            return new FinderLocal(path);
        }
    }

    @Override
    public void setPath(String name) {
        final String absolute = stringByExpandingTildeInPath(name);
        if(loadNative()) {
            super.setPath(this.resolveAlias(absolute));
        }
        else {
            super.setPath(absolute);
        }
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

    private static boolean JNI_LOADED = false;

    private static boolean loadNative() {
        if(!JNI_LOADED) {
            JNI_LOADED = Native.load("Local");
        }
        return JNI_LOADED;
    }

    private native String resolveAlias(String absolute);

    /**
     * @return Human readable description of file type
     */
    @Override
    public String kind() {
        if(attributes.isFile()) {
            // Native file type mapping
            final String kind = kind(this.getExtension());
            if(StringUtils.isEmpty(kind)) {
                return ch.cyberduck.core.i18n.Locale.localizedString("Unknown");
            }
            return kind;
        }
        return super.kind();
    }

    public static native String kind(String extension);

    @Override
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
            log.error(e.getMessage());
            return null;
        }
    }

    @Override
    public void writePermissions(final Permission perm, final boolean recursive) {
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
                if(attributes.isDirectory() && recursive) {
                    for(AbstractPath child : childs()) {
                        child.writePermissions(perm, recursive);
                    }
                }
            }
        });
    }


    /**
     * Write <code>NSFileModificationDate</code>.
     *
     * @param millis Milliseconds since 1970
     */
    @Override
    public void writeModificationDate(final long millis) {
        new ProxyController().invoke(new DefaultMainAction() {
            public void run() {
                boolean success = NSFileManager.defaultManager().setAttributes_ofItemAtPath_error(
                        NSDictionary.dictionaryWithObjectsForKeys(
                                NSArray.arrayWithObject(NSDate.dateWithTimeIntervalSince1970(millis / 1000)),
                                NSArray.arrayWithObject(NSFileManager.NSFileModificationDate)),
                        _impl.getAbsolutePath(), null);
                if(!success) {
                    log.error("File attribute changed failed:" + getAbsolute());
                }
            }
        });
    }

    /**
     * Read <code>NSFileCreationDate</code>.
     *
     * @return Milliseconds since 1970
     */
    @Override
    public long getCreationDate() {
        final NSDictionary fileAttributes = NSFileManager.defaultManager().fileAttributes(_impl.getAbsolutePath());
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
                            file.getParent().getAbsolute(), "",
                            NSArray.arrayWithObject(file.getName()))) {
                        log.warn("Failed to move " + file.getAbsolute() + " to Trash");
                    }
                }
            });
        }
    }

    /**
     * Create a new symbolic link from this
     * path if <code>local.symboliclink.resolve</code is true
     */
    @Override
    public boolean touch() {
        if(!Preferences.instance().getBoolean("local.symboliclink.resolve")) {
            if(StringUtils.isNotEmpty(this.getSymlinkTarget())) {
                return NSFileManager.defaultManager().createSymbolicLinkAtPath_withDestinationPath_error(this.getAbsolute(),
                        this.getSymlinkTarget(), null);
            }
        }
        return super.touch();
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
                defaultApplicationCache.put(extension, NSBundle.bundleWithPath(path).bundleIdentifier());
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
        return this.toURL();
    }

    @Override
    public String toURL() {
        return stringByAbbreviatingWithTildeInPath(this.getAbsolute());
    }

    @Override
    public boolean isExecutable() {
        return NSFileManager.defaultManager().isExecutableFileAtPath(this.getAbsolute());
    }

    @Override
    public boolean isReadable() {
        return NSFileManager.defaultManager().isReadableFileAtPath(this.getAbsolute());
    }

    @Override
    public boolean isWritable() {
        return NSFileManager.defaultManager().isWritableFileAtPath(this.getAbsolute());
    }

    public String getStorageClass() {
        throw new UnsupportedOperationException();
    }

    public void setStorageClass(String redundancy) {
        throw new UnsupportedOperationException();
    }

    public void setVersionId(String versionId) {
        throw new UnsupportedOperationException();
    }

    public String getVersionId() {
        throw new UnsupportedOperationException();
    }
}