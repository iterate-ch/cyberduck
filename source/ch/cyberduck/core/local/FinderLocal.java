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
import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.io.LocalRepeatableFileInputStream;
import ch.cyberduck.core.library.Native;
import ch.cyberduck.core.serializer.Deserializer;
import ch.cyberduck.core.serializer.Serializer;
import ch.cyberduck.ui.cocoa.foundation.NSArray;
import ch.cyberduck.ui.cocoa.foundation.NSData;
import ch.cyberduck.ui.cocoa.foundation.NSEnumerator;
import ch.cyberduck.ui.cocoa.foundation.NSFileManager;
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
        if(StringUtils.isNotBlank(data)) {
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
        // Get or create application scope bookmark
        final String bookmark = this.getBookmark();
        if(StringUtils.isNotBlank(bookmark)) {
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
    public boolean isSymbolicLink() {
        return attributes.isSymbolicLink();
    }

    @Override
    public String getBookmark() {
        if(StringUtils.isBlank(bookmark)) {
            bookmark = this.createBookmark();
        }
        return bookmark;
    }

    private String createBookmark() {
        if(this.exists()) {
            // Create new security scoped bookmark
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
        else {
            log.warn(String.format("Skip creating bookmark for file not found %s", this));
            return null;
        }
    }

    @Override
    public void setBookmark(final String data) {
        this.bookmark = data;
    }

    @Override
    public OutputStream getOutputStream(boolean append) throws AccessDeniedException {
        final NSURL resolved;
        try {
            resolved = this.lock();
        }
        catch(AccessDeniedException e) {
            return super.getOutputStream(append);
        }
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
    public NSURL lock() throws AccessDeniedException {
        if(StringUtils.isBlank(bookmark)) {
            throw new AccessDeniedException(String.format("No security scoped bookmark for %s", this));
        }
        final NSURL resolved = this.resolve(bookmark);
        if(resolved.respondsToSelector(Foundation.selector("startAccessingSecurityScopedResource"))) {
            resolved.startAccessingSecurityScopedResource();
        }
        return resolved;
    }

    protected NSURL resolve(final String data) throws AccessDeniedException {
        final ObjCObjectByReference error = new ObjCObjectByReference();
        final NSData bookmark = NSData.dataWithBase64EncodedString(data);
        final NSURL resolved = NSURL.URLByResolvingBookmarkData(bookmark, error);
        if(null == resolved) {
            final NSError f = error.getValueAs(NSError.class);
            log.error(String.format("Error resolving bookmark for %s to URL %s", this, f));
            throw new AccessDeniedException(String.format("%s", f));
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
        final NSURL resolved;
        try {
            resolved = this.lock();
        }
        catch(AccessDeniedException e) {
            return super.getInputStream();
        }
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
                log.error(String.format("Error listing children for folder %s with error %s", this, f));
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
