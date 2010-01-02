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
public class IconCache {
    private static Logger log = Logger.getLogger(IconCache.class);

    /**
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

    private NSImage get(String key, Integer size) {
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
        NSImage img = this.get(extension, size);
        if(null == img) {
            img = NSWorkspace.sharedWorkspace().iconForFileType(extension);
            this.put(extension, this.convert(img, size), size);
        }
        return img;
    }

    public NSImage iconForFolder(Integer size) {
        NSImage folder = this.iconForName("NSFolder", size);
        if(null == folder) {
            return this.iconForPath(FOLDER_PATH, size);
        }
        return folder;
    }

    private NSImage iconForFolder(String badge, Integer size) {
        final String name = "BadgedFolder" + badge;
        NSImage folder = this.iconForName(name, size);
        if(null == folder) {
            folder = NSImage.imageWithSize(new NSSize(size, size));
            folder.lockFocus();
            NSImage f = FOLDER_ICON;
            f.drawInRect(new NSRect(new NSPoint(0, 0), folder.size()),
                    NSZeroRect, NSGraphics.NSCompositeSourceOver, 1.0f);
            NSImage o = this.iconForName(badge);
            o.drawInRect(new NSRect(new NSPoint(0, 0), folder.size()),
                    NSZeroRect, NSGraphics.NSCompositeSourceOver, 1.0f);
            folder.unlockFocus();
            this.put(name, this.convert(folder, size), size);
        }
        return folder;
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
     * @param name
     * @param width
     * @param height
     * @return
     * @see NSImage#imageNamed(String)
     * @see #convert(ch.cyberduck.ui.cocoa.application.NSImage, Integer, Integer)
     */
    protected NSImage iconForName(final String name, Integer width, Integer height) {
        NSImage image = this.get(name, width);
        if(null == image) {
            image = NSImage.imageNamed(name);
            if(null == image) {
                log.warn("No icon named " + name);
            }
            this.put(name, this.convert(image, width, height), width);
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
        if(item.exists()) {
            NSImage icon = this.iconForName(item.getAbsolute(), size);
            if(null == icon) {
                icon = NSWorkspace.sharedWorkspace().iconForFile(item.getAbsolute());
                this.put(item.getAbsolute(), this.convert(icon, size), size);
            }
            return icon;
        }
        return this.iconForName("notfound.tiff", size);
    }

    private final NSRect NSZeroRect = new NSRect(0, 0);

    private final boolean overlayFolderImage
            = Preferences.instance().getBoolean("browser.markInaccessibleFolders");

    private static Local FOLDER_PATH
            = LocalFactory.createLocal(Preferences.instance().getProperty("application.support.path"));

    private final NSImage FOLDER_ICON = this.iconForPath(FOLDER_PATH);

    /**
     * @param item
     * @param size
     * @return
     */
    public NSImage iconForPath(final Path item, Integer size) {
        if(item.attributes.isSymbolicLink()) {
            if(item.attributes.isDirectory()) {
                final NSImage folder = NSImage.imageWithSize(new NSSize(size, size));
                folder.lockFocus();
                NSImage f = FOLDER_ICON;
                f.drawInRect(new NSRect(new NSPoint(0, 0), folder.size()),
                        NSZeroRect, NSGraphics.NSCompositeSourceOver, 1.0f);
                NSImage o = this.iconForName("AliasBadgeIcon.icns");
                o.drawInRect(new NSRect(new NSPoint(0, 0), folder.size()),
                        NSZeroRect, NSGraphics.NSCompositeSourceOver, 1.0f);
                folder.unlockFocus();
                return this.convert(folder, size);
            }
            final NSImage symlink = NSImage.imageWithSize(new NSSize(size, size));
            symlink.lockFocus();
            NSImage f = this.iconForExtension(item.getExtension(), size);
            f.drawInRect(new NSRect(new NSPoint(0, 0), symlink.size()),
                    NSZeroRect, NSGraphics.NSCompositeSourceOver, 1.0f);
            NSImage o = this.iconForName("AliasBadgeIcon.icns");
            o.drawInRect(new NSRect(new NSPoint(0, 0), symlink.size()),
                    NSZeroRect, NSGraphics.NSCompositeSourceOver, 1.0f);
            symlink.unlockFocus();
            return symlink;
        }
        if(item.attributes.isFile()) {
            if(StringUtils.isEmpty(item.getExtension()) && null != item.attributes.getPermission()) {
                if(item.isExecutable()) {
                    return this.iconForName("executable.tiff", size);
                }
            }
            return this.iconForExtension(item.getExtension(), size);
        }
        if(item.attributes.isVolume()) {
            return this.iconForName(item.getHost().getProtocol().disk(), size);
        }
        if(item.attributes.isDirectory()) {
            if(overlayFolderImage && null != item.attributes.getPermission()) {
                if(!item.isExecutable()
                        || (item.isCached() && !item.cache().get(item).attributes().isReadable())) {
                    return this.iconForFolder("PrivateFolderBadgeIcon.icns", size);
                }
                if(!item.isReadable()) {
                    if(item.isWritable()) {
                        return this.iconForFolder("DropFolderBadgeIcon.icns", size);
                    }
                }
                if(!item.isWritable()) {
                    return this.iconForFolder("ReadOnlyFolderBadgeIcon.icns", size);
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
        if(null == icon) {
            log.warn("Icon is null");
            return null;
        }
        if(null == width || null == height) {
            log.info("Return default size for " + icon.name());
            return icon;
        }
        icon.setName(icon.name() + width + height);
        icon.setCacheMode(NSImage.NSImageCacheNever);
        icon.setScalesWhenResized(true);
        icon.setSize(new NSSize(width, height));
        return icon;
    }
}