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
import com.apple.cocoa.foundation.NSSize;

import ch.cyberduck.core.Path;

import java.net.URL;
import java.util.HashMap;

/**
 * @version $Id$
 */
public class CDIconCache extends HashMap {
    private static CDIconCache instance;

    public static CDIconCache instance() {
        if (null == instance) {
            instance = new CDIconCache();
        }
        return instance;
    }

    public void put(String extension, NSImage image) {
        super.put(extension, image);
    }

    public NSImage get(String key) {
        NSImage img = (NSImage) super.get(key);
        if (null == img) {
            this.put(key, img = NSWorkspace.sharedWorkspace().iconForFileType(key));
        }
        return img;
    }

    public static final NSImage DISK_ICON = NSImage.imageNamed("disk.tiff");
    public static final NSImage SYMLINK_ICON = NSImage.imageNamed("symlink.tiff");
    public static final NSImage FOLDER_ICON = NSImage.imageNamed("folder16.tiff");
    public static final NSImage FOLDER_NOACCESS_ICON = NSImage.imageNamed("folder_noaccess.tiff");
    public static final NSImage FOLDER_WRITEONLY_ICON = NSImage.imageNamed("folder_writeonly.tiff");
    public static final NSImage NOT_FOUND_ICON = NSImage.imageNamed("notfound.tiff");

    static {
        SYMLINK_ICON.setSize(new NSSize(16f, 16f));
        FOLDER_ICON.setSize(new NSSize(16f, 16f));
        FOLDER_NOACCESS_ICON.setSize(new NSSize(16f, 16f));
        FOLDER_WRITEONLY_ICON.setSize(new NSSize(16f, 16f));
        NOT_FOUND_ICON.setSize(new NSSize(16f, 16f));
    }

    public NSImage iconForPath(final Path item) {
        final String extension = item.getExtension();
        NSImage icon = null;
        if(item.attributes.isSymbolicLink()) {
            icon = SYMLINK_ICON;
        }
        else if(item.attributes.isDirectory()) {
            icon = FOLDER_ICON;
        }
        else if(item.attributes.isFile()) {
            icon = this.get(extension);
        }
        else {
            icon = NOT_FOUND_ICON;
        }
        icon.setSize(new NSSize(16f, 16f));
        return icon;
    }
}