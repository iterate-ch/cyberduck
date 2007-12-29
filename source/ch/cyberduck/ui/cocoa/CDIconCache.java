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

import com.apple.cocoa.application.NSImage;
import com.apple.cocoa.application.NSWorkspace;
import com.apple.cocoa.foundation.NSPathUtilities;
import com.apple.cocoa.foundation.NSPoint;
import com.apple.cocoa.foundation.NSRect;
import com.apple.cocoa.foundation.NSSize;

import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Preferences;

import java.util.HashMap;

/**
 * @version $Id$
 */
public class CDIconCache extends HashMap {
    private static CDIconCache instance;

    public static CDIconCache instance() {
        if(null == instance) {
            instance = new CDIconCache();
        }
        return instance;
    }

    public void put(String extension, NSImage image) {
        super.put(extension, image);
    }

    public NSImage get(String key) {
        NSImage img = (NSImage) super.get(key);
        if(null == img) {
            this.put(key, img = NSWorkspace.sharedWorkspace().iconForFileType(key));
        }
        return img;
    }

    private static String FOLDER_PATH
            = Preferences.instance().getProperty("application.support.path");

    public static final NSImage FOLDER_NEW_ICON;

    static {
        FOLDER_NEW_ICON = new NSImage(new NSSize(128, 128));
        FOLDER_NEW_ICON.lockFocus();
        NSImage f = NSWorkspace.sharedWorkspace().iconForFile(
                NSPathUtilities.stringByExpandingTildeInPath(FOLDER_PATH));
        f.drawInRect(new NSRect(new NSPoint(0, 0), FOLDER_NEW_ICON.size()),
                NSRect.ZeroRect, NSImage.CompositeSourceOver, 1.0f);
        NSImage o = NSImage.imageNamed("NewFolderBadgeIcon.icns");
        o.drawInRect(new NSRect(new NSPoint(0, 0), FOLDER_NEW_ICON.size()),
                NSRect.ZeroRect, NSImage.CompositeSourceOver, 1.0f);
        FOLDER_NEW_ICON.unlockFocus();
    }

    public static final NSImage FOLDER_ICON = NSWorkspace.sharedWorkspace().iconForFile(
            NSPathUtilities.stringByExpandingTildeInPath(FOLDER_PATH));

    static {
        FOLDER_ICON.setSize(new NSSize(128, 128));
    }

    public NSImage iconForPath(final Local item, int size) {
        final NSImage icon;
        if(item.exists()) {
            icon = NSWorkspace.sharedWorkspace().iconForFile(item.getAbsolute());
        }
        else {
            icon = NSImage.imageNamed("notfound.tiff");
        }
        icon.setCacheMode(NSImage.ImageCacheBySize);
        icon.setScalesWhenResized(true);
        icon.setSize(new NSSize(size, size));
        return icon;
    }

    public NSImage iconForPath(final Path item, int size) {
        if(item.attributes.isSymbolicLink()) {
            if(item.attributes.isDirectory()) {
                NSImage folder = new NSImage(new NSSize(size, size));
                folder.lockFocus();
                NSImage f = NSWorkspace.sharedWorkspace().iconForFile(
                        NSPathUtilities.stringByExpandingTildeInPath(FOLDER_PATH));
                f.drawInRect(new NSRect(new NSPoint(0, 0), folder.size()),
                        NSRect.ZeroRect, NSImage.CompositeSourceOver, 1.0f);
                NSImage o = NSImage.imageNamed("AliasBadgeIcon.icns");
                o.drawInRect(new NSRect(new NSPoint(0, 0), folder.size()),
                        NSRect.ZeroRect, NSImage.CompositeSourceOver, 1.0f);
                folder.unlockFocus();
                return this.convert(folder, size);
            }
            NSImage symlink = new NSImage(new NSSize(size, size));
            symlink.lockFocus();
            NSImage f = this.get(item.getExtension());
            f.drawInRect(new NSRect(new NSPoint(0, 0), symlink.size()),
                    NSRect.ZeroRect, NSImage.CompositeSourceOver, 1.0f);
            NSImage o = NSImage.imageNamed("AliasBadgeIcon.icns");
            o.drawInRect(new NSRect(new NSPoint(0, 0), symlink.size()),
                    NSRect.ZeroRect, NSImage.CompositeSourceOver, 1.0f);
            symlink.unlockFocus();
            return this.convert(symlink, size);
        }
        if(item.attributes.isFile()) {
            if(null == item.getExtension()) {
                if(item.attributes.isExecutable()) {
                    return this.convert(NSImage.imageNamed("executable.tiff"), size);
                }
            }
            return this.convert(this.get(item.getExtension()), size);
        }
        if(item.attributes.isDirectory()) {
            if(item.isRoot()) {
                return this.convert(NSImage.imageNamed("disk.icns"), size);
            }
            if(Preferences.instance().getBoolean("browser.markInaccessibleFolders")) {
                if(!item.attributes.isExecutable()
                        || (item.isCached() && !item.cache().get(item).attributes().isReadable())) {
                    NSImage folder = new NSImage(new NSSize(size, size));
                    folder.lockFocus();
                    NSImage f = NSWorkspace.sharedWorkspace().iconForFile(
                            NSPathUtilities.stringByExpandingTildeInPath(FOLDER_PATH));
                    f.drawInRect(new NSRect(new NSPoint(0, 0), folder.size()),
                            NSRect.ZeroRect, NSImage.CompositeSourceOver, 1.0f);
                    NSImage o = NSImage.imageNamed("PrivateFolderBadgeIcon.icns");
                    o.drawInRect(new NSRect(new NSPoint(0, 0), folder.size()),
                            NSRect.ZeroRect, NSImage.CompositeSourceOver, 1.0f);
                    folder.unlockFocus();
                    return this.convert(folder, size);
                }
                if(!item.attributes.isReadable()) {
                    if(item.attributes.isWritable()) {
                        NSImage folder = new NSImage(new NSSize(size, size));
                        folder.lockFocus();
                        NSImage f = NSWorkspace.sharedWorkspace().iconForFile(
                                NSPathUtilities.stringByExpandingTildeInPath(FOLDER_PATH));
                        f.drawInRect(new NSRect(new NSPoint(0, 0), folder.size()),
                                NSRect.ZeroRect, NSImage.CompositeSourceOver, 1.0f);
                        NSImage o = NSImage.imageNamed("DropFolderBadgeIcon.icns");
                        o.drawInRect(new NSRect(new NSPoint(0, 0), folder.size()),
                                NSRect.ZeroRect, NSImage.CompositeSourceOver, 1.0f);
                        folder.unlockFocus();
                        return this.convert(folder, size);
                    }
                }
                if(!item.attributes.isWritable()) {
                    NSImage folder = new NSImage(new NSSize(size, size));
                    folder.lockFocus();
                    NSImage f = NSWorkspace.sharedWorkspace().iconForFile(
                            NSPathUtilities.stringByExpandingTildeInPath(FOLDER_PATH));
                    f.drawInRect(new NSRect(new NSPoint(0, 0), folder.size()),
                            NSRect.ZeroRect, NSImage.CompositeSourceOver, 1.0f);
                    NSImage o = NSImage.imageNamed("ReadOnlyFolderBadgeIcon.icns");
                    o.drawInRect(new NSRect(new NSPoint(0, 0), folder.size()),
                            NSRect.ZeroRect, NSImage.CompositeSourceOver, 1.0f);
                    folder.unlockFocus();
                    return this.convert(folder, size);
                }
            }
            return this.convert(NSWorkspace.sharedWorkspace().iconForFile(
                    NSPathUtilities.stringByExpandingTildeInPath(FOLDER_PATH)), size);
        }
        return this.convert(NSImage.imageNamed("notfound.tiff"), size);
    }

    private NSImage convert(NSImage icon, int size) {
        icon.setCacheMode(NSImage.ImageCacheBySize);
        icon.setScalesWhenResized(true);
        icon.setSize(new NSSize(size, size));
        return icon;
    }
}