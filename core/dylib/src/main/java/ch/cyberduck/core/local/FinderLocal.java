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

import ch.cyberduck.binding.foundation.NSArray;
import ch.cyberduck.binding.foundation.NSEnumerator;
import ch.cyberduck.binding.foundation.NSFileManager;
import ch.cyberduck.binding.foundation.NSObject;
import ch.cyberduck.binding.foundation.NSURL;
import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.LocalAccessDeniedException;
import ch.cyberduck.core.exception.LocalNotfoundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.io.LocalRepeatableFileInputStream;
import ch.cyberduck.core.library.Native;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.serializer.Serializer;

import org.apache.commons.io.FilenameUtils;
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

public class FinderLocal extends Local {
    private static final Logger log = Logger.getLogger(FinderLocal.class);

    static {
        Native.load("core");
    }

    private static final NSFileManager manager = NSFileManager.defaultManager();

    /**
     * Application scoped bookmark to access outside of sandbox
     */
    private String bookmark;

    private FinderLocalAttributes attributes
            = new FinderLocalAttributes(this);

    public FinderLocal(final Local parent, final String name) throws LocalAccessDeniedException {
        super(parent, name);
    }

    public FinderLocal(final String parent, final String name) throws LocalAccessDeniedException {
        super(parent, name);
    }

    public FinderLocal(final String path) throws LocalAccessDeniedException {
        super(resolveAlias(new TildeExpander().expand(path)));
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
        return manager.displayNameAtPath(this.getName());
    }

    /**
     * @return Path relative to the home directory denoted with a tilde.
     */
    @Override
    public String getAbbreviatedPath() {
        return new TildeExpander().abbreviate(this.getAbsolute());
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
            try {
                bookmark = new SecurityScopedBookmarkResolver().create(this);
            }
            catch(AccessDeniedException e) {
                log.warn(String.format("Failure resolving bookmark %s", bookmark));
            }
        }
        return bookmark;
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
        catch(LocalAccessDeniedException e) {
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
            throw new LocalAccessDeniedException(e.getMessage(), e);
        }
    }

    @Override
    public NSURL lock() throws AccessDeniedException {
        if(SecurityScopedBookmarkResolver.isSandboxed()) {
            final NSURL resolved = new SecurityScopedBookmarkResolver().resolve(this);
            if(resolved.respondsToSelector(Foundation.selector("startAccessingSecurityScopedResource"))) {
                if(!resolved.startAccessingSecurityScopedResource()) {
                    throw new LocalAccessDeniedException(String.format("Failure accessing security scoped resource for %s", this));
                }
            }
            return resolved;
        }
        throw new LocalAccessDeniedException("Sandbox disabled");
    }

    @Override
    public void release(final Object lock) {
        if(null == lock) {
            return;
        }
        if(SecurityScopedBookmarkResolver.isSandboxed()) {
            final NSURL resolved = (NSURL) lock;
            if(resolved.respondsToSelector(Foundation.selector("stopAccessingSecurityScopedResource"))) {
                resolved.stopAccessingSecurityScopedResource();
            }
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
            throw new LocalAccessDeniedException(e.getMessage(), e);
        }
    }

    @Override
    public AttributedList<Local> list() throws AccessDeniedException {
        if(PreferencesFactory.get().getBoolean("local.list.native")) {
            final AttributedList<Local> children = new AttributedList<Local>();
            final ObjCObjectByReference error = new ObjCObjectByReference();
            final NSArray files = manager.contentsOfDirectoryAtPath_error(this.getAbsolute(), error);
            if(null == files) {
                final NSError f = error.getValueAs(NSError.class);
                if(null == f) {
                    throw new LocalAccessDeniedException(this.getAbsolute());
                }
                throw new LocalAccessDeniedException(String.format("%s", f.localizedDescription()));
            }
            final NSEnumerator i = files.objectEnumerator();
            NSObject next;
            while(((next = i.nextObject()) != null)) {
                children.add(new FinderLocal(this, next.toString()));
            }
            return children;
        }
        else {
            return super.list();
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
    public Local getSymlinkTarget() throws NotfoundException, LocalAccessDeniedException {
        final ObjCObjectByReference error = new ObjCObjectByReference();
        final String destination = manager.destinationOfSymbolicLinkAtPath_error(
                this.getAbsolute(), error);
        if(null == destination) {
            final NSError f = error.getValueAs(NSError.class);
            if(null == f) {
                throw new LocalNotfoundException(this.getAbsolute());
            }
            throw new LocalNotfoundException(String.format("%s", f.localizedDescription()));
        }
        if(FilenameUtils.getPrefixLength(destination) != 0) {
            // Absolute path
            return new FinderLocal(destination);
        }
        // Relative path
        return new FinderLocal(this.getParent(), destination);
    }
}
