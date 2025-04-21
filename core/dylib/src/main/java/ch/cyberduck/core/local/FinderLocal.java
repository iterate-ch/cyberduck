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
import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.Filter;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.LocalAccessDeniedException;
import ch.cyberduck.core.library.Native;
import ch.cyberduck.core.preferences.BundleApplicationResourcesFinder;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.preferences.SecurityApplicationGroupSupportDirectoryFinder;
import ch.cyberduck.core.preferences.TemporarySupportDirectoryFinder;

import org.apache.commons.io.input.ProxyInputStream;
import org.apache.commons.io.output.ProxyOutputStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Paths;

public class FinderLocal extends Local {
    private static final Logger log = LogManager.getLogger(FinderLocal.class);

    private static final Local APP_PACKAGE = new BundleApplicationResourcesFinder().find();
    private static final Local TEMPORARY = new TemporarySupportDirectoryFinder().find();
    private static final Local GROUP_CONTAINER = new SecurityApplicationGroupSupportDirectoryFinder().find();

    static {
        Native.load("core");
    }

    private static final FilesystemBookmarkResolver<NSURL> resolver
            = FilesystemBookmarkResolverFactory.get();

    public FinderLocal(final Local parent, final String name) {
        super(parent, name);
    }

    public FinderLocal(final String parent, final String name) {
        super(parent, name);
    }

    public FinderLocal(final String name) {
        super(name);
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
    public boolean exists(final LinkOption... options) {
        NSURL resolved = null;
        try {
            resolved = this.lock(false);
            if(null == resolved) {
                return super.exists(options);
            }
            return Files.exists(Paths.get(resolved.path()));
        }
        catch(AccessDeniedException e) {
            return super.exists(options);
        }
        finally {
            this.release(resolved);
        }
    }

    @Override
    public AttributedList<Local> list(final Filter<String> filter) throws AccessDeniedException {
        final NSURL resolved;
        try {
            resolved = this.lock(true);
            if(null == resolved) {
                return super.list(filter);
            }
            final AttributedList<Local> list = super.list(resolved.path(), filter);
            this.release(resolved);
            return list;
        }
        catch(LocalAccessDeniedException e) {
            log.warn("Failure obtaining lock for {}. {}", this, e);
            return super.list(filter);
        }
    }

    @Override
    public OutputStream getOutputStream(boolean append) throws AccessDeniedException {
        final NSURL resolved;
        try {
            resolved = this.lock(this.exists());
            if(null == resolved) {
                return super.getOutputStream(append);
            }
        }
        catch(LocalAccessDeniedException e) {
            log.warn("Failure obtaining lock for {}. {}", this, e);
            return super.getOutputStream(append);
        }
        return new LockReleaseProxyOutputStream(super.getOutputStream(resolved.path(), append), resolved, append);
    }

    /**
     * @param interactive Prompt to resolve bookmark of file outside of sandbox with choose panel
     */
    @Override
    public NSURL lock(final boolean interactive) throws AccessDeniedException {
        return this.lock(interactive, resolver);
    }

    protected NSURL lock(final boolean interactive, final FilesystemBookmarkResolver<NSURL> resolver) throws AccessDeniedException {
        final String path = this.getAbbreviatedPath();
        if(null == bookmark) {
            if(!skip(this)) {
                bookmark = resolver.create(this, interactive);
            }
        }
        if(null == bookmark) {
            log.warn("No security scoped bookmark for {}", path);
            return null;
        }
        log.debug("Lock with bookmark {}", bookmark);
        try {
            final NSURL resolved = resolver.resolve(bookmark);
            if(null == resolved) {
                log.warn("Unable to resolve bookmark {}", bookmark);
                return null;
            }
            if(!resolved.startAccessingSecurityScopedResource()) {
                throw new LocalAccessDeniedException(String.format("Failure accessing security scoped resource for %s", path));
            }
            return resolved;
        }
        catch(LocalAccessDeniedException e) {
            log.warn("Ignore failure {} resolving bookmark {}", e, bookmark);
            return null;
        }
    }

    /**
     * Determine if creating security scoped bookmarks for file should be skipped
     */
    private static boolean skip(final Local file) {
        if(null != TEMPORARY) {
            if(file.isChild(TEMPORARY)) {
                // Skip prompt for file in temporary folder where access is not sandboxed
                return true;
            }
        }
        if(null != GROUP_CONTAINER) {
            if(file.isChild(GROUP_CONTAINER)) {
                // Skip prompt for file in application group folder where access is not sandboxed
                return true;
            }
        }
        if(null != APP_PACKAGE) {
            if(file.isChild(APP_PACKAGE)) {
                // Skip prompt for file in application bundle resources
                return true;
            }
        }
        return false;
    }

    @Override
    public void release(final Object lock) {
        if(null == lock) {
            return;
        }
        final NSURL resolved = (NSURL) lock;
        resolved.stopAccessingSecurityScopedResource();
    }

    @Override
    public InputStream getInputStream() throws AccessDeniedException {
        final NSURL resolved;
        try {
            resolved = this.lock(false);
            if(null == resolved) {
                return super.getInputStream();
            }
        }
        catch(LocalAccessDeniedException e) {
            log.warn("Failure obtaining lock for {}. {}", this, e);
            return super.getInputStream();
        }
        return new LockReleaseProxyInputStream(super.getInputStream(resolved.path()), resolved);
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

    private final class LockReleaseProxyInputStream extends ProxyInputStream {
        private final NSURL resolved;

        public LockReleaseProxyInputStream(final InputStream proxy, final NSURL resolved) {
            super(proxy);
            this.resolved = resolved;
        }

        @Override
        public void close() throws IOException {
            try {
                super.close();
            }
            finally {
                release(resolved);
            }
        }
    }

    private final class LockReleaseProxyOutputStream extends ProxyOutputStream {
        private final NSURL resolved;

        public LockReleaseProxyOutputStream(final OutputStream proxy, final NSURL resolved, final boolean append) {
            super(proxy);
            this.resolved = resolved;
        }

        @Override
        public void close() throws IOException {
            try {
                super.close();
            }
            finally {
                release(resolved);
            }
        }
    }
}
