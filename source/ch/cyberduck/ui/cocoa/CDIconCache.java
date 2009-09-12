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

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.rococoa.cocoa.foundation.NSPoint;
import org.rococoa.cocoa.foundation.NSRect;
import org.rococoa.cocoa.foundation.NSSize;

import java.util.HashMap;

/**
 * @version $Id$
 */
public class CDIconCache extends HashMap<String, NSImage> {
    private static Logger log = Logger.getLogger(CDIconCache.class);

    private static CDIconCache instance = null;

    private static final Object lock = new Object();

    public static CDIconCache instance() {
        synchronized(lock) {
            if(null == instance) {
                instance = new CDIconCache();
            }
        }
        return instance;
    }

    @Override
    public NSImage put(String extension, NSImage image) {
        return super.put(extension, image);
    }

    /**
     * @param key
     * @return
     */
    public NSImage iconForFileType(String key, int size) {
        NSImage img = this.get(key);
        if(null == img) {
            img = NSWorkspace.sharedWorkspace().iconForFileType(key);
            this.put(key, img);
        }
        return this.convert(img, size);
    }

    private static Local FOLDER_PATH
            = LocalFactory.createLocal(Preferences.instance().getProperty("application.support.path"));

    public static final NSImage FOLDER_ICON
            = NSWorkspace.sharedWorkspace().iconForFile(FOLDER_PATH.getAbsolute());

    static {
        FOLDER_ICON.setSize(new NSSize(128, 128));
    }

    public NSImage iconForName(final String name, int size) {
        return this.iconForName(name, size, size);
    }

    /**
     *
     * @param name
     * @param width
     * @param height
     * @return
     */
    public NSImage iconForName(final String name, int width, int height) {
        NSImage loaded = NSImage.imageNamed(name + width);
        if(null == loaded) {
            loaded = NSImage.imageNamed(name);
            if(null == loaded) {
                log.error("No icon named " + name);
                return null;
            }
            loaded.setName(name + width);
        }
//        if(null == image) {
//            // Look for icon in system System Core Types Bundle
//            Local l = LocalFactory.createLocalLocal(NSBundle.bundleWithPath(
//                    "/System/Library/CoreServices/CoreTypes.bundle").resourcePath(),
//                    name);
//            if(!l.exists()) {
//                return null;
//            }
//            image = new NSImage(l.getAbsolute(), false);
//        }
        return this.convert(loaded, width, height);
    }

    /**
     * @param item
     * @param size
     * @return
     */
    public NSImage iconForPath(final Local item, int size) {
        final NSImage icon;
        if(item.exists()) {
            icon = NSWorkspace.sharedWorkspace().iconForFile(item.getAbsolute());
        }
        else {
            icon = NSImage.imageNamed("notfound.tiff");
        }
        return this.convert(icon, size);
    }

    private final NSRect NSZeroRect = new NSRect(0, 0);

    /**
     * @param item
     * @param size
     * @return
     */
    public NSImage iconForPath(final Path item, int size) {
        if(item.attributes.isSymbolicLink()) {
            if(item.attributes.isDirectory()) {
                final NSImage folder = NSImage.imageWithSize(new NSSize(size, size));
                folder.lockFocus();
                NSImage f = NSWorkspace.sharedWorkspace().iconForFile(FOLDER_PATH.getAbsolute());
                f.drawInRect(new NSRect(new NSPoint(0, 0), folder.size()),
                        NSZeroRect, NSGraphics.NSCompositeSourceOver, 1.0f);
                NSImage o = NSImage.imageNamed("AliasBadgeIcon.icns");
                o.drawInRect(new NSRect(new NSPoint(0, 0), folder.size()),
                        NSZeroRect, NSGraphics.NSCompositeSourceOver, 1.0f);
                folder.unlockFocus();
                return this.convert(folder, size);
            }
            final NSImage symlink = NSImage.imageWithSize(new NSSize(size, size));
            symlink.lockFocus();
            NSImage f = this.iconForFileType(item.getExtension(), size);
            f.drawInRect(new NSRect(new NSPoint(0, 0), symlink.size()),
                    NSZeroRect, NSGraphics.NSCompositeSourceOver, 1.0f);
            NSImage o = NSImage.imageNamed("AliasBadgeIcon.icns");
            o.drawInRect(new NSRect(new NSPoint(0, 0), symlink.size()),
                    NSZeroRect, NSGraphics.NSCompositeSourceOver, 1.0f);
            symlink.unlockFocus();
            return symlink;
        }
        if(item.attributes.isFile()) {
            if(StringUtils.isEmpty(item.getExtension()) && null != item.attributes.getPermission()) {
                if(item.isExecutable()) {
                    return this.convert(NSImage.imageNamed("executable.tiff"), size);
                }
            }
            return this.iconForFileType(item.getExtension(), size);
        }
        if(item.attributes.isVolume()) {
            return this.iconForName(item.getHost().getProtocol().disk(), size);
        }
        if(item.attributes.isDirectory()) {
            if(Preferences.instance().getBoolean("browser.markInaccessibleFolders")
                    && null != item.attributes.getPermission()) {
                if(!item.isExecutable()
                        || (item.isCached() && !item.cache().get(item).attributes().isReadable())) {
                    NSImage folder = NSImage.imageWithSize(new NSSize(size, size));
                    folder.lockFocus();
                    NSImage f = NSWorkspace.sharedWorkspace().iconForFile(FOLDER_PATH.getAbsolute());
                    f.drawInRect(new NSRect(new NSPoint(0, 0), folder.size()),
                            NSZeroRect, NSGraphics.NSCompositeSourceOver, 1.0f);
                    NSImage o = NSImage.imageNamed("PrivateFolderBadgeIcon.icns");
                    o.drawInRect(new NSRect(new NSPoint(0, 0), folder.size()),
                            NSZeroRect, NSGraphics.NSCompositeSourceOver, 1.0f);
                    folder.unlockFocus();
                    return this.convert(folder, size);
                }
                if(!item.isReadable()) {
                    if(item.isWritable()) {
                        NSImage folder = NSImage.imageWithSize(new NSSize(size, size));
                        folder.lockFocus();
                        NSImage f = NSWorkspace.sharedWorkspace().iconForFile(FOLDER_PATH.getAbsolute());
                        f.drawInRect(new NSRect(new NSPoint(0, 0), folder.size()),
                                NSZeroRect, NSGraphics.NSCompositeSourceOver, 1.0f);
                        NSImage o = NSImage.imageNamed("DropFolderBadgeIcon.icns");
                        o.drawInRect(new NSRect(new NSPoint(0, 0), folder.size()),
                                NSZeroRect, NSGraphics.NSCompositeSourceOver, 1.0f);
                        folder.unlockFocus();
                        return this.convert(folder, size);
                    }
                }
                if(!item.isWritable()) {
                    NSImage folder = NSImage.imageWithSize(new NSSize(size, size));
                    folder.lockFocus();
                    NSImage f = NSWorkspace.sharedWorkspace().iconForFile(FOLDER_PATH.getAbsolute());
                    f.drawInRect(new NSRect(new NSPoint(0, 0), folder.size()),
                            NSZeroRect, NSGraphics.NSCompositeSourceOver, 1.0f);
                    NSImage o = NSImage.imageNamed("ReadOnlyFolderBadgeIcon.icns");
                    o.drawInRect(new NSRect(new NSPoint(0, 0), folder.size()),
                            NSZeroRect, NSGraphics.NSCompositeSourceOver, 1.0f);
                    folder.unlockFocus();
                    return this.convert(folder, size);
                }
            }
            return this.convert(NSWorkspace.sharedWorkspace().iconForFile(FOLDER_PATH.getAbsolute()), size);
        }
        return this.convert(NSImage.imageNamed("notfound.tiff"), size);
    }

    protected NSImage convert(NSImage icon, int size) {
        return this.convert(icon, size, size);
    }

    protected NSImage convert(NSImage icon, int width, int height) {
        if(null == icon) {
            log.warn("Icon is null");
            return null;
        }
        icon.setScalesWhenResized(true);
//        icon.setCacheMode(NSImage.NSImageCacheBySize);
//        icon.setCachedSeparately(true);
//        icon.setMatchesOnMultipleResolution(false);
        icon.setSize(new NSSize(width, height));
        return icon;
    }
}