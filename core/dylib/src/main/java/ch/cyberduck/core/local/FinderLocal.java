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

import ch.cyberduck.binding.foundation.NSFileManager;
import ch.cyberduck.binding.foundation.NSURL;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.LocalAccessDeniedException;
import ch.cyberduck.core.io.LocalRepeatableFileInputStream;
import ch.cyberduck.core.library.Native;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.serializer.Serializer;

import org.apache.commons.io.input.ProxyInputStream;
import org.apache.commons.io.output.ProxyOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.rococoa.Foundation;

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

    private final FilesystemBookmarkResolver<NSURL> resolver;

    /**
     * Application scoped bookmark to access outside of sandbox
     */
    private String bookmark;

    public FinderLocal(final Local parent, final String name) {
        this(parent, name, FilesystemBookmarkResolverFactory.get());
    }

    public FinderLocal(final Local parent, final String name, final FilesystemBookmarkResolver<NSURL> resolver) {
        super(parent, name);
        this.resolver = resolver;
    }

    public FinderLocal(final String parent, final String name) {
        this(parent, name, FilesystemBookmarkResolverFactory.get());
    }

    public FinderLocal(final String parent, final String name, final FilesystemBookmarkResolver<NSURL> resolver) {
        super(parent, name);
        this.resolver = resolver;
    }

    public FinderLocal(final String path) {
        this(resolveAlias(new TildeExpander().expand(path)), FilesystemBookmarkResolverFactory.get());
    }

    public FinderLocal(final String name, final FilesystemBookmarkResolver<NSURL> resolver) {
        super(name);
        this.resolver = resolver;
    }

    @Override
    public <T> T serialize(final Serializer dict) {
        dict.setStringForKey(this.getAbbreviatedPath(), "Path");
        // Get or create application scope bookmark
        final String bookmark = this.getBookmark();
        if(StringUtils.isNotBlank(bookmark)) {
            dict.setStringForKey(bookmark, String.format("%s Bookmark", PreferencesFactory.get().getProperty("application.name")));
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
        return new TildeExpander().abbreviate(this.getAbsolute());
    }

    @Override
    public Local getVolume() {
        for(Local parent = this.getParent(); !parent.isRoot(); parent = parent.getParent()) {
            final Local directory = parent.getParent();
            if(null == directory) {
                return super.getVolume();
            }
            if("/Volumes".equals(directory.getAbsolute())) {
                return parent;
            }
        }
        return super.getVolume();
    }

    @Override
    public String getBookmark() {
        if(StringUtils.isBlank(bookmark)) {
            try {
                bookmark = resolver.create(this);
            }
            catch(AccessDeniedException e) {
                log.warn(String.format("Failure resolving bookmark for %s. %s", this, e.getDetail()));
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
            resolved = this.lock(false);
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

    /**
     * @param interactive Prompt to resolve bookmark of file outside of sandbox with choose panel
     */
    @Override
    public NSURL lock(final boolean interactive) throws AccessDeniedException {
        final NSURL resolved = resolver.resolve(this, interactive);
        if(resolved.respondsToSelector(Foundation.selector("startAccessingSecurityScopedResource"))) {
            if(!resolved.startAccessingSecurityScopedResource()) {
                throw new LocalAccessDeniedException(String.format("Failure accessing security scoped resource for %s", this));
            }
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
            resolved = this.lock(false);
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

    private static String resolveAlias(final String absolute) {
        if(PreferencesFactory.get().getBoolean("local.alias.resolve")) {
            return resolveAliasNative(absolute);
        }
        return absolute;
    }

    /**
     * @param absolute The absolute path of the alias file.
     * @return The absolute path this alias is pointing to.
     */
    private static native String resolveAliasNative(String absolute);

    @Override
    public FinderLocalAttributes attributes() {
        return new FinderLocalAttributes(this);
    }
}
