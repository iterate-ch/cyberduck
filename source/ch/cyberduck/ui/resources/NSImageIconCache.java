package ch.cyberduck.ui.resources;

/*
 * Copyright (c) 2002-2013 David Kocher. All rights reserved.
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
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.ch
 */

import ch.cyberduck.core.Path;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.local.Application;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.LocalFactory;
import ch.cyberduck.ui.cocoa.application.NSGraphics;
import ch.cyberduck.ui.cocoa.application.NSImage;
import ch.cyberduck.ui.cocoa.application.NSWorkspace;

import org.apache.commons.collections.map.LRUMap;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.rococoa.cocoa.foundation.NSPoint;
import org.rococoa.cocoa.foundation.NSRect;
import org.rococoa.cocoa.foundation.NSSize;

import java.util.HashMap;
import java.util.Map;

/**
 * @version $Id$
 */
public class NSImageIconCache extends AbstractIconCache<NSImage> {
    private static Logger log = Logger.getLogger(NSImageIconCache.class);

    public static void register() {
        IconCacheFactory.addFactory(Factory.NATIVE_PLATFORM, new Factory());
    }

    private static class Factory extends IconCacheFactory {
        @Override
        protected IconCache create() {
            return new NSImageIconCache();
        }
    }

    public NSImageIconCache() {
        //
    }

    /**
     * Cache limited to n entries
     */
    private Map<String, Map<Integer, NSImage>> cache = new LRUMap(
            Preferences.instance().getInteger("icon.cache.size")) {
        @Override
        protected boolean removeLRU(LinkEntry entry) {
            if(log.isDebugEnabled()) {
                log.debug("Removing from cache:" + entry);
            }
            return true;
        }
    };

    private void put(final String key, final NSImage image, final Integer size) {
        Map<Integer, NSImage> versions;
        if(cache.containsKey(key)) {
            versions = cache.get(key);
        }
        else {
            versions = new HashMap<Integer, NSImage>();
        }
        versions.put(size, image);
        cache.put(key, versions);
    }

    private NSImage load(final String key, final Integer size) {
        if(!cache.containsKey(key)) {
            if(log.isDebugEnabled()) {
                log.debug(String.format("No cached image for %s", key));
            }
            return null;
        }
        final Map<Integer, NSImage> versions = cache.get(key);
        return versions.get(size);
    }

    /**
     * @param extension File type
     * @param size      Requested size
     * @return Cached icon
     */
    @Override
    public NSImage documentIcon(final String extension, final Integer size) {
        NSImage image = this.load(extension, size);
        if(null == image) {
            image = NSWorkspace.sharedWorkspace().iconForFileType(extension);
            this.put(extension, this.convert(image, size), size);
        }
        return image;
    }

    @Override
    public NSImage documentIcon(final String extension, final Integer size, final NSImage badge) {
        final String name = extension + badge.name();
        NSImage icon = this.iconNamed(name, size);
        if(null == icon) {
            icon = this.badge(badge, this.documentIcon(extension, size));
            this.put(name, icon, size);
        }
        return icon;
    }

    @Override
    public NSImage folderIcon(final Integer size) {
        NSImage folder = this.iconNamed("NSFolder", size);
        if(null == folder) {
            return this.fileIcon(FOLDER_PATH, size);
        }
        return folder;
    }

    @Override
    public NSImage folderIcon(final Integer size, final NSImage badge) {
        final String name = String.format("NSFolder-%s", badge.name());
        NSImage folder = this.load(name, size);
        if(null == folder) {
            folder = this.badge(badge, this.convert(FOLDER_ICON, size));
            this.put(name, folder, size);
        }
        return folder;
    }

    /**
     * Overlay badge image.
     *
     * @param badge Overlay
     * @param icon  Icon
     * @return Cached icon
     */
    @Override
    protected NSImage badge(final NSImage badge, final NSImage icon) {
        NSImage f = NSImage.imageWithSize(icon.size());
        f.lockFocus();
        icon.drawInRect(new NSRect(new NSPoint(0, 0), icon.size()),
                NSZeroRect, NSGraphics.NSCompositeSourceOver, 1.0f);
        badge.drawInRect(new NSRect(new NSPoint(0, 0), icon.size()),
                NSZeroRect, NSGraphics.NSCompositeSourceOver, 1.0f);
        f.unlockFocus();
        return f;
    }

    /**
     * @param name   When looking for files in the application bundle, it is better (but not required)
     *               to include the filename extension in the name parameter
     * @param width  Requested size
     * @param height Requested size
     * @return Cached icon
     * @see NSImage#imageNamed(String)
     * @see #convert(ch.cyberduck.ui.cocoa.application.NSImage, Integer, Integer)
     */
    @Override
    public NSImage iconNamed(final String name, final Integer width, final Integer height) {
        NSImage image = this.load(name, width);
        if(null == image) {
            if(name.startsWith("/")) {
                image = NSImage.imageWithContentsOfFile(name);
            }
            else {
                image = NSImage.imageNamed(name);
            }
            if(null == image) {
                log.warn(String.format("No icon named %s", name));
                this.put(name, null, width);
            }
            else {
                // You can clear an image object from the cache explicitly by passing nil for the image name.
                image.setName(null);
                this.put(name, this.convert(image, width, height), width);
            }
        }
        return image;
    }

    /**
     * @param item File
     * @param size Requested size
     * @return Cached icon
     */
    @Override
    public NSImage fileIcon(final Local item, final Integer size) {
        NSImage icon = null;
        if(item.exists()) {
            icon = this.load(item.getAbsolute(), size);
            if(null == icon) {
                icon = NSWorkspace.sharedWorkspace().iconForFile(item.getAbsolute());
                this.put(item.getAbsolute(), this.convert(icon, size), size);
            }
        }
        if(null == icon) {
            return this.iconNamed("notfound.tiff", size);
        }
        return icon;
    }

    /**
     * @param app  Application
     * @param size Requested size
     * @return Cached icon
     */
    @Override
    public NSImage applicationIcon(final Application app, final Integer size) {
        NSImage icon = this.load(app.getIdentifier(), size);
        if(null == icon) {
            icon = NSWorkspace.sharedWorkspace().iconForFile(
                    NSWorkspace.sharedWorkspace().absolutePathForAppBundleWithIdentifier(app.getIdentifier()));
            this.put(app.getIdentifier(), this.convert(icon, size), size);
        }
        if(null == icon) {
            return this.iconNamed("notfound.tiff", size);
        }
        return icon;
    }

    private final NSRect NSZeroRect = new NSRect(0, 0);

    private static final Local FOLDER_PATH
            = LocalFactory.createLocal(Preferences.instance().getProperty("application.support.path"));

    private final NSImage FOLDER_ICON
            = this.fileIcon(FOLDER_PATH, null);

    /**
     * @param item File
     * @param size Requested size
     * @return Cached icon
     */
    @Override
    public NSImage fileIcon(final Path item, final Integer size) {
        if(item.attributes().isSymbolicLink()) {
            final NSImage badge = this.iconNamed("aliasbadge.tiff", size);
            badge.setName("aliasbadge");
            if(item.attributes().isDirectory()) {
                return this.folderIcon(size, badge);
            }
            return this.documentIcon(item.getExtension(), size, badge);
        }
        if(item.attributes().isFile()) {
            if(StringUtils.isEmpty(item.getExtension())) {
                if(item.attributes().getPermission().isExecutable()) {
                    return this.iconNamed("executable.tiff", size);
                }
            }
            return this.documentIcon(item.getExtension(), size);
        }
        if(item.attributes().isDirectory()) {
            if(!item.attributes().getPermission().isExecutable()) {
                final NSImage badge = this.iconNamed("privatefolderbadge.tiff", size);
                badge.setName("privatefolderbadge");
                return this.folderIcon(size, badge);
            }
            if(!item.attributes().getPermission().isReadable()) {
                if(item.attributes().getPermission().isWritable()) {
                    final NSImage badge = this.iconNamed("dropfolderbadge.tiff", size);
                    badge.setName("dropfolderbadge");
                    return this.folderIcon(size, badge);
                }
            }
            if(!item.attributes().getPermission().isWritable()) {
                final NSImage badge = this.iconNamed("readonlyfolderbadge.tiff", size);
                badge.setName("readonlyfolderbadge");
                return this.folderIcon(size, badge);
            }
            return this.folderIcon(size);
        }
        return this.iconNamed("notfound.tiff", size);
    }

    private NSImage convert(final NSImage icon, final Integer size) {
        return this.convert(icon, size, size);
    }

    private NSImage convert(final NSImage icon, final Integer width, final Integer height) {
        if(null == width || null == height) {
            log.debug(String.format("Return default size for %s", icon.name()));
            return icon;
        }
        icon.setSize(new NSSize(width, height));
        return icon;
    }
}