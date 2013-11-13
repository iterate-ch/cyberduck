package ch.cyberduck.core.local;

/*
 * Copyright (c) 2012 David Kocher. All rights reserved.
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

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.DeserializerFactory;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.LocalFactory;
import ch.cyberduck.core.Permission;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.io.LocalRepeatableFileInputStream;
import ch.cyberduck.core.library.Native;
import ch.cyberduck.core.serializer.Deserializer;
import ch.cyberduck.core.serializer.Serializer;
import ch.cyberduck.ui.cocoa.application.NSWorkspace;
import ch.cyberduck.ui.cocoa.foundation.NSArray;
import ch.cyberduck.ui.cocoa.foundation.NSData;
import ch.cyberduck.ui.cocoa.foundation.NSDate;
import ch.cyberduck.ui.cocoa.foundation.NSDictionary;
import ch.cyberduck.ui.cocoa.foundation.NSEnumerator;
import ch.cyberduck.ui.cocoa.foundation.NSFileManager;
import ch.cyberduck.ui.cocoa.foundation.NSNumber;
import ch.cyberduck.ui.cocoa.foundation.NSObject;
import ch.cyberduck.ui.cocoa.foundation.NSURL;

import org.apache.commons.io.input.ProxyInputStream;
import org.apache.commons.io.output.ProxyOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.rococoa.Foundation;
import org.rococoa.ObjCObjectByReference;
import org.rococoa.cocoa.foundation.NSError;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @version $Id$
 */
public class FinderLocal extends Local {
    private static final Logger log = Logger.getLogger(FinderLocal.class);

    public static void register() {
        LocalFactory.addFactory(Factory.NATIVE_PLATFORM, new Factory());
    }

    static {
        Native.load("Local");
    }

    private static class Factory extends LocalFactory {
        @Override
        protected Local create() {
            return new FinderLocal(System.getProperty("user.home"));
        }

        @Override
        protected Local create(final Local parent, final String name) {
            return new FinderLocal(parent, name);
        }

        @Override
        protected Local create(final String parent, final String name) {
            return new FinderLocal(parent, name);
        }

        @Override
        protected Local create(final String path) {
            return new FinderLocal(path);
        }

        @Override
        protected <T> Local create(final T serialized) {
            return new FinderLocal(serialized);
        }
    }

    /**
     * Application scoped bookmark to access outside of sandbox
     */
    private String bookmark;

    private FinderLocalAttributes attributes
            = new FinderLocalAttributes(this);

    public FinderLocal(final Local parent, final String name) {
        super(String.format("%s/%s", parent.getAbsolute(), name));
    }

    public FinderLocal(final String parent, final String name) {
        super(String.format("%s/%s", parent, name));
    }

    public FinderLocal(final String path) {
        super(path);
    }

    public <T> FinderLocal(final T serialized) {
        super(serialized);
        final Deserializer dict = DeserializerFactory.createDeserializer(serialized);
        final String data = dict.stringForKey("Bookmark");
        if(data != null) {
            this.bookmark = data;
        }
    }

    @Override
    protected void setPath(final String name) {
        final String expanded = resolveAlias(stringByExpandingTildeInPath(name));
        if(log.isDebugEnabled()) {
            if(!StringUtils.equals(expanded, name)) {
                log.debug(String.format("Expanded %s to %s", name, expanded));
            }
        }
        super.setPath(expanded);

    }

    @Override
    public <T> T serialize(final Serializer dict) {
        dict.setStringForKey(this.getAbbreviatedPath(), "Path");
        final String bookmark = this.getBookmark();
        if(bookmark != null) {
            dict.setStringForKey(bookmark, "Bookmark");
        }
        return dict.getSerialized();
    }

    /**
     * @return Name of the file as displayed in the Finder. E.g. a ':' is replaced with '/'.
     */
    @Override
    public String getDisplayName() {
        return NSFileManager.defaultManager().displayNameAtPath(this.getName());
    }

    /**
     * @return Path relative to the home directory denoted with a tilde.
     */
    @Override
    public String getAbbreviatedPath() {
        return stringByAbbreviatingWithTildeInPath(this.getAbsolute());
    }

    @Override
    public Local getVolume() {
        for(Local parent = this.getParent(); !parent.isRoot(); parent = parent.getParent()) {
            if(parent.getParent().getAbsolute().equals("/Volumes")) {
                return parent;
            }
        }
        return super.getVolume();
    }

    @Override
    public String getBookmark() {
        if(null == bookmark) {
            if(this.exists()) {
                // Create new security scoped bookmark
                bookmark = this.createBookmark();
            }
            else {
                log.warn(String.format("Skip creating bookmark for file not found %s", this));
            }
        }
        return bookmark;
    }

    private String createBookmark() {
        final ObjCObjectByReference error = new ObjCObjectByReference();
        final NSData data = NSURL.fileURLWithPath(this.getAbsolute()).bookmarkDataWithOptions_includingResourceValuesForKeys_relativeToURL_error(
                NSURL.NSURLBookmarkCreationOptions.NSURLBookmarkCreationWithSecurityScope, null, null, error);
        if(null == data) {
            final NSError f = error.getValueAs(NSError.class);
            log.warn(String.format("Failure getting bookmark data for file %s %s", this, f));
            return null;
        }
        final String encoded = data.base64EncodedString();
        if(log.isDebugEnabled()) {
            log.debug(String.format("Encoded bookmark for %s as %s", this, encoded));
        }
        return encoded;
    }

    @Override
    public void setBookmark(final String data) {
        this.bookmark = data;
    }

    @Override
    public OutputStream getOutputStream(boolean append) throws AccessDeniedException {
        if(null == bookmark) {
            log.warn(String.format("No security scoped bookmark for %s", this));
            return super.getOutputStream(append);
        }
        final NSURL resolved = this.lock();
        try {
            return new ProxyOutputStream(new FileOutputStream(new File(resolved.path()), append)) {
                @Override
                public void close() throws IOException {
                    try {
                        super.close();
                    }
                    finally {
                        release(resolved);
                    }
                }
            };
        }
        catch(FileNotFoundException e) {
            throw new AccessDeniedException(e.getMessage(), e);
        }
    }

    @Override
    public NSURL lock() {
        if(null == bookmark) {
            log.warn(String.format("No security scoped bookmark for %s", this));
            return null;
        }
        final NSURL resolved = this.resolve(bookmark);
        if(null == resolved) {
            log.warn(String.format("Failure resolving bookmark %s", bookmark));
            return null;
        }
        if(resolved.respondsToSelector(Foundation.selector("startAccessingSecurityScopedResource"))) {
            resolved.startAccessingSecurityScopedResource();
        }
        return resolved;
    }

    protected NSURL resolve(final String data) {
        final ObjCObjectByReference error = new ObjCObjectByReference();
        final NSData bookmark = NSData.dataWithBase64EncodedString(data);
        final NSURL resolved = NSURL.URLByResolvingBookmarkData(bookmark, error);
        if(null == resolved) {
            final NSError f = error.getValueAs(NSError.class);
            log.error(String.format("Error resolving bookmark %s to URL %s", bookmark, f));
        }
        return resolved;
    }

    @Override
    public void release(final Object lock) {
        if(null == lock) {
            return;
        }
        final NSURL resolved = (NSURL) lock;
        if(resolved.respondsToSelector(Foundation.selector("stopAccessingSecurityScopedResource"))) {
            resolved.stopAccessingSecurityScopedResource();
        }
    }

    @Override
    public InputStream getInputStream() throws AccessDeniedException {
        if(null == bookmark) {
            log.warn(String.format("No security scoped bookmark for %s", this));
            return super.getInputStream();
        }
        final NSURL resolved = this.lock();
        try {
            return new ProxyInputStream(new LocalRepeatableFileInputStream(new File(resolved.path()))) {
                @Override
                public void close() throws IOException {
                    try {
                        super.close();
                    }
                    finally {
                        release(resolved);
                    }
                }
            };
        }
        catch(FileNotFoundException e) {
            throw new AccessDeniedException(e.getMessage(), e);
        }
    }

    @Override
    public AttributedList<Local> list() throws AccessDeniedException {
        if(Preferences.instance().getBoolean("local.list.native")) {
            final AttributedList<Local> children = new AttributedList<Local>();
            final ObjCObjectByReference error = new ObjCObjectByReference();
            final NSArray files = NSFileManager.defaultManager().contentsOfDirectoryAtPath_error(this.getAbsolute(), error);
            if(null == files) {
                final NSError f = error.getValueAs(NSError.class);
                log.error(String.format("Error listing children for folder %s %s", this, f));
                throw new AccessDeniedException(String.format("Error listing files in directory %s", path));
            }
            final NSEnumerator i = files.objectEnumerator();
            NSObject next;
            while(((next = i.nextObject()) != null)) {
                children.add(new FinderLocal(this.getAbsolute(), next.toString()));
            }
            return children;
        }
        else {
            return super.list();
        }
    }

    @Override
    public boolean exists() {
        return NSFileManager.defaultManager().fileExistsAtPath(this.getAbsolute());
    }

    @Override
    public void symlink(String target) {
        final boolean success = NSFileManager.defaultManager().createSymbolicLinkAtPath_pathContent(
                this.getAbsolute(), target);
        if(!success) {
            log.error(String.format("File attribute changed failed for file %s", this));
        }
    }

    /**
     * @param absolute The absolute path of the alias file.
     * @return The absolute path this alias is pointing to.
     */
    private static native String resolveAlias(String absolute);

    @Override
    public FinderLocalAttributes attributes() {
        return attributes;
    }

    @Override
    public Local getSymlinkTarget() {
        return new FinderLocal(this.getParent().getAbsolute(),
                NSFileManager.defaultManager().destinationOfSymbolicLinkAtPath_error(this.getAbsolute(), null));
    }

    private static final Object workspace = new Object();

    @Override
    public void writeUnixPermission(final Permission permission) {
        synchronized(workspace) {
            final ObjCObjectByReference error = new ObjCObjectByReference();
            boolean success = NSFileManager.defaultManager().setAttributes_ofItemAtPath_error(
                    NSDictionary.dictionaryWithObjectsForKeys(
                            NSArray.arrayWithObject(NSNumber.numberWithInt(Integer.valueOf(permission.getMode(), 8))),
                            NSArray.arrayWithObject(NSFileManager.NSFilePosixPermissions)),
                    this.getAbsolute(), error);
            if(!success) {
                final NSError f = error.getValueAs(NSError.class);
                log.error(String.format("File attribute changed failed for file %s %s", this, f));
            }
        }
    }

    /**
     * Write <code>NSFileModificationDate</code>.
     *
     * @param created  Milliseconds
     * @param modified Milliseconds
     * @param accessed Milliseconds
     */
    @Override
    public void writeTimestamp(final long created, final long modified, final long accessed) {
        synchronized(workspace) {
            final ObjCObjectByReference error = new ObjCObjectByReference();
            boolean success = NSFileManager.defaultManager().setAttributes_ofItemAtPath_error(
                    NSDictionary.dictionaryWithObjectsForKeys(
                            NSArray.arrayWithObject(NSDate.dateWithTimeIntervalSince1970(modified / 1000d)),
                            NSArray.arrayWithObject(NSFileManager.NSFileModificationDate)),
                    getAbsolute(), error);
            if(!success) {
                final NSError f = error.getValueAs(NSError.class);
                log.error(String.format("File attribute changed failed for file %s %s", this, f));
            }
        }
    }

    /**
     * Move file to trash on main interface thread using <code>NSWorkspace.RecycleOperation</code>.
     */
    @Override
    public void trash() {
        if(this.exists()) {
            synchronized(workspace) {
                if(log.isDebugEnabled()) {
                    log.debug(String.format("Move %s to Trash", this));
                }
                if(!NSWorkspace.sharedWorkspace().performFileOperation(
                        NSWorkspace.RecycleOperation,
                        this.getParent().getAbsolute(), StringUtils.EMPTY,
                        NSArray.arrayWithObject(this.getName()))) {
                    log.warn(String.format("Failed to move %s to Trash", this));
                }
            }
        }
    }

    /**
     * Comparing by inode if the file exists.
     */
    @Override
    public boolean equals(final Object o) {
        if(this == o) {
            return true;
        }
        if(o == null || getClass() != o.getClass()) {
            return false;
        }
        if(!super.equals(o)) {
            return false;
        }
        // Now test with inode for case sensitive volumes
        final FinderLocal that = (FinderLocal) o;
        if(attributes != null ? !attributes.equals(that.attributes) : that.attributes != null) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (attributes != null ? attributes.hashCode() : 0);
        return result;
    }

    private static String stringByAbbreviatingWithTildeInPath(final String path) {
        if(StringUtils.startsWith(path, Preferences.instance().getProperty("local.user.home"))) {
            return "~" + StringUtils.removeStart(path, Preferences.instance().getProperty("local.user.home"));
        }
        return path;
    }

    private static String stringByExpandingTildeInPath(final String path) {
        if(path.startsWith("~")) {
            return Preferences.instance().getProperty("local.user.home") + StringUtils.substring(path, 1);
        }
        return path;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("FinderLocal{");
        sb.append("bookmark=").append(bookmark);
        sb.append(", path='").append(path).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
