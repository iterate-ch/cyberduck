package ch.cyberduck.ui.cocoa;

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

import ch.cyberduck.core.Local;
import ch.cyberduck.core.LocalFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.ui.cocoa.application.NSGraphics;
import ch.cyberduck.ui.cocoa.application.NSImage;
import ch.cyberduck.ui.cocoa.application.NSWorkspace;

import org.rococoa.cocoa.foundation.NSPoint;
import org.rococoa.cocoa.foundation.NSRect;
import org.rococoa.cocoa.foundation.NSSize;

import org.apache.commons.collections.map.LRUMap;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * @version $Id$
 */
public class IconCache {
    private static Logger log = Logger.getLogger(IconCache.class);

    /**
     * No resizing of the cached image.
     *
     * @param name
     * @return
     */
    public static NSImage iconNamed(final String name) {
        return IconCache.instance().iconForName(name);
    }

    /**
     * @param name
     * @param size
     * @return
     */
    public static NSImage iconNamed(final String name, Integer size) {
        return IconCache.instance().iconForName(name, size);
    }

    /**
     * @param name
     * @param width
     * @param height
     * @return
     */
    public static NSImage iconNamed(final String name, Integer width, Integer height) {
        return IconCache.instance().iconForName(name, width, height);
    }

    /**
     * @param size
     * @return Standard folder icon for this platform
     */
    public static NSImage folderIcon(Integer size) {
        return IconCache.instance().iconForFolder(size);
    }

    public static NSImage documentIcon(String extension) {
        return IconCache.instance().iconForExtension(extension, null);
    }

    public static NSImage documentIcon(String extension, Integer size) {
        return IconCache.instance().iconForExtension(extension, size);
    }

    private static IconCache instance = null;

    private static final Object lock = new Object();

    public static IconCache instance() {
        synchronized(lock) {
            if(null == instance) {
                instance = new IconCache();
            }
        }
        return instance;
    }

    private IconCache() {
        ;
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

    private void put(String key, NSImage image, Integer size) {
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

    private NSImage load(String key, Integer size) {
        if(!cache.containsKey(key)) {
            log.warn("No cached image for " + key);
            return null;
        }
        final Map<Integer, NSImage> versions = cache.get(key);
        return versions.get(size);
    }

    /**
     * @param extension
     * @return
     */
    public NSImage iconForExtension(String extension, Integer size) {
        NSImage img = this.load(extension, size);
        if(null == img) {
            img = NSWorkspace.sharedWorkspace().iconForFileType(extension);
            this.put(extension, this.convert(img, size), size);
        }
        return img;
    }

    public NSImage iconForExtension(NSImage badge, String extension, Integer size) {
        final String name = extension + badge.name();
        NSImage icon = this.iconForName(name, size);
        if(null == icon) {
            icon = this.badge(badge, this.iconForExtension(extension, size));
            this.put(name, icon, size);
        }
        return icon;
    }

    public NSImage iconForFolder(Integer size) {
        NSImage folder = this.iconForName("NSFolder", size);
        if(null == folder) {
            return this.iconForPath(FOLDER_PATH, size);
        }
        return folder;
    }

    private NSImage iconForFolder(NSImage badge, Integer size) {
        final String name = "NSFolder" + badge.name();
        NSImage folder = this.iconForName(name, size);
        if(null == folder) {
            folder = this.badge(badge, this.convert(FOLDER_ICON, size));
            this.put(name, folder, size);
        }
        return folder;
    }

    /**
     * Overlay badge image.
     *
     * @param badge
     * @param icon
     */
    private NSImage badge(NSImage badge, NSImage icon) {
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
     * @param name
     * @return
     */
    protected NSImage iconForName(final String name) {
        return this.iconForName(name, null);
    }

    /**
     * @param name
     * @param size
     * @return
     * @see #convert(ch.cyberduck.ui.cocoa.application.NSImage, Integer)
     */
    protected NSImage iconForName(final String name, Integer size) {
        return this.iconForName(name, size, size);
    }

    /**
     * @param name   When looking for files in the application bundle, it is better (but not required)
     *               to include the filename extension in the name parameter
     * @param width
     * @param height
     * @return
     * @see NSImage#imageNamed(String)
     * @see #convert(ch.cyberduck.ui.cocoa.application.NSImage, Integer, Integer)
     */
    protected NSImage iconForName(final String name, Integer width, Integer height) {
        NSImage image = this.load(name, width);
        if(null == image) {
            image = NSImage.imageNamed(name);
            if(null == image) {
                log.warn("No icon named " + name);
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

    public NSImage iconForPath(final Local item) {
        return this.iconForPath(item, null);
    }

    /**
     * @param item
     * @param size
     * @return
     */
    public NSImage iconForPath(final Local item, Integer size) {
        NSImage icon = null;
        if(item.exists()) {
            icon = this.iconForName(item.getAbsolute(), size);
            if(null == icon) {
                icon = NSWorkspace.sharedWorkspace().iconForFile(item.getAbsolute());
                this.put(item.getAbsolute(), this.convert(icon, size), size);
            }
        }
        if(null == icon) {
            return this.iconForName("notfound.tiff", size);
        }
        return icon;
    }

    public NSImage iconForApplication(final String bundleIdentifier) {
        return this.iconForApplication(bundleIdentifier, null);
    }

    /**
     * @param bundleIdentifier
     * @param size
     * @return
     */
    public NSImage iconForApplication(final String bundleIdentifier, Integer size) {
        NSImage icon = this.iconForName(bundleIdentifier, size);
        if(null == icon) {
            icon = NSWorkspace.sharedWorkspace().iconForFile(
                    NSWorkspace.sharedWorkspace().absolutePathForAppBundleWithIdentifier(bundleIdentifier));
            this.put(bundleIdentifier, this.convert(icon, size), size);
        }
        if(null == icon) {
            return this.iconForName("notfound.tiff", size);
        }
        return icon;
    }

    private final NSRect NSZeroRect = new NSRect(0, 0);

    private static Local FOLDER_PATH
            = LocalFactory.createLocal(Preferences.instance().getProperty("application.support.path"));

    private final NSImage FOLDER_ICON = this.iconForPath(FOLDER_PATH);

    /**
     * @param item
     * @param size
     * @return
     */
    public NSImage iconForPath(final Path item, Integer size) {
        return this.iconForPath(item, size, Preferences.instance().getBoolean("browser.markInaccessibleFolders"));
    }

    public NSImage iconForPath(final Path item, Integer size, boolean overlay) {
        if(item.attributes().isSymbolicLink()) {
            NSImage badge = this.convert(NSImage.imageNamed("aliasbadge.png"), size);
            if(item.attributes().isDirectory()) {
                return this.iconForFolder(badge, size);
            }
            return this.iconForExtension(badge, item.getExtension(), size);
        }
        if(item.attributes().isFile()) {
            if(StringUtils.isEmpty(item.getExtension())) {
                if(item.attributes().getPermission().isExecutable()) {
                    return this.iconForName("executable.tiff", size);
                }
            }
            return this.iconForExtension(item.getExtension(), size);
        }
        if(item.attributes().isVolume()) {
            return this.iconForName(item.getHost().getProtocol().disk(), size);
        }
        if(item.attributes().isDirectory()) {
            if(overlay) {
                if(!item.attributes().getPermission().isExecutable()
                        || (item.isCached() && !item.cache().get(item.getReference()).attributes().isReadable())) {
                    NSImage badge = this.convert(NSImage.imageNamed("privatefolderbadge.png"), size);
                    return this.iconForFolder(badge, size);
                }
                if(!item.attributes().getPermission().isReadable()) {
                    if(item.attributes().getPermission().isWritable()) {
                        NSImage badge = this.convert(NSImage.imageNamed("dropfolderbadge.png"), size);
                        return this.iconForFolder(badge, size);
                    }
                }
                if(!item.attributes().getPermission().isWritable()) {
                    NSImage badge = this.convert(NSImage.imageNamed("readonlyfolderbadge.png"), size);
                    return this.iconForFolder(badge, size);
                }
            }
            return this.iconForFolder(size);
        }
        return this.iconForName("notfound.tiff", size);
    }

    public NSImage convert(NSImage icon, Integer size) {
        return this.convert(icon, size, size);
    }

    public NSImage convert(NSImage icon, Integer width, Integer height) {
        if(null == width || null == height) {
            log.info("Return default size for " + icon.name());
            return icon;
        }
        icon.setSize(new NSSize(width, height));
        return icon;
    }
}