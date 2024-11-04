package ch.cyberduck.core.resources;

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

import ch.cyberduck.binding.application.NSGraphics;
import ch.cyberduck.binding.application.NSImage;
import ch.cyberduck.binding.application.NSWorkspace;
import ch.cyberduck.core.Factory;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Permission;
import ch.cyberduck.core.local.Application;
import ch.cyberduck.core.preferences.PreferencesFactory;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.rococoa.cocoa.foundation.NSPoint;
import org.rococoa.cocoa.foundation.NSRect;
import org.rococoa.cocoa.foundation.NSSize;

public class NSImageIconCache implements IconCache<NSImage> {
    private static final Logger log = LogManager.getLogger(NSImageIconCache.class);

    private final static NSRect NSZeroRect = new NSRect(0, 0);

    private final NSWorkspace workspace = NSWorkspace.sharedWorkspace();

    private NSImage cache(final String name, final NSImage image, final Integer size) {
        if(null == image) {
            log.warn("No icon named {}", name);
            return image;
        }
        if(null == name) {
            return image;
        }
        // When naming an image with the setName: method, it is convention not to include filename extensions
        // in the names you specify
        image.setName(null == size ? name : toName(name, size));
        return image;
    }

    private static String toName(final String name, final Integer size) {
        return String.format("%s (%dpx)", name, size);
    }

    private NSImage load(final String name, final Integer size) {
        NSImage cached = NSImage.imageNamed(toName(name, size));
        if(null == cached) {
            if(!Factory.Platform.osversion.matches("(10)\\..*")) {
                cached = NSImage.imageWithSymbol(name);
            }
            if(null == cached) {
                log.debug("No cached image for {}", name);
            }
            else {
                log.trace("Loaded symbol image {}", cached);
            }
        }
        else {
            log.trace("Loaded image {}", cached);
        }
        return cached;
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
            return this.cache(extension,
                    this.convert(extension, workspace.iconForFileType(extension), size), size);
        }
        return image;
    }

    @Override
    public NSImage documentIcon(final String extension, final Integer size, final NSImage badge) {
        final String name = String.format("NSDocument-%s%s", extension, badge.name());
        NSImage icon = this.iconNamed(name, size);
        if(null == icon) {
            icon = this.badge(badge, this.documentIcon(extension, size));
            this.cache(name, icon, size);
        }
        return icon;
    }

    @Override
    public NSImage folderIcon(final Integer size) {
        NSImage folder = this.iconNamed("NSFolder", size);
        if(null == folder) {
            return this.iconNamed("NSFolder", size);
        }
        return folder;
    }

    @Override
    public NSImage folderIcon(final Integer size, final NSImage badge) {
        final String name = String.format("NSFolder-%s", badge.name());
        NSImage folder = this.load(name, size);
        if(null == folder) {
            folder = this.convert(name, this.iconNamed("NSFolder", size), size);
            folder = this.badge(badge, folder);
            this.cache(name, folder, size);
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
    private NSImage badge(final NSImage badge, final NSImage icon) {
        NSImage f = NSImage.imageWithSize(icon.size());
        f.lockFocus();
        icon.drawInRect(new NSRect(new NSPoint(0, 0), icon.size()),
                NSZeroRect, NSGraphics.NSCompositeSourceOver, 1.0f);
        badge.drawInRect(new NSRect(new NSPoint(0, 0), badge.size()),
                NSZeroRect, NSGraphics.NSCompositeSourceOver, 1.0f);
        f.unlockFocus();
        return f;
    }

    /**
     * @param name   When looking for files in the application bundle, it is better (but not required) to include the
     *               filename extension in the name parameter
     * @param width  Requested size
     * @param height Requested size
     * @return Cached icon
     * @see NSImage#imageNamed(String)
     * @see #convert(String, ch.cyberduck.binding.application.NSImage, Integer, Integer)
     */
    @Override
    public NSImage iconNamed(final String name, final Integer width, final Integer height) {
        // Search for an object whose name was set explicitly using the setName: method and currently
        // resides in the image cache
        NSImage image = this.load(name, width);
        if(null == image) {
            if(null == name) {
                return this.iconNamed("notfound.tiff", width, height);
            }
            else if(name.contains(PreferencesFactory.get().getProperty("local.delimiter"))) {
                return this.cache(FilenameUtils.getName(name), this.convert(FilenameUtils.getName(name),
                        NSImage.imageWithContentsOfFile(name), width, height), width);
            }
            else {
                return this.cache(name, this.convert(name,
                        NSImage.imageNamed(name), width, height), width);
            }
        }
        return image;
    }

    /**
     * @param file File
     * @param size Requested size
     * @return Cached icon
     */
    @Override
    public NSImage fileIcon(final Local file, final Integer size) {
        NSImage icon = null;
        if(file.exists()) {
            icon = this.load(file.getAbsolute(), size);
            if(null == icon) {
                return this.cache(file.getName(),
                        this.convert(file.getName(), workspace.iconForFile(file.getAbsolute()), size), size);
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
            final String path = workspace.absolutePathForAppBundleWithIdentifier(app.getIdentifier());
            // Null if the bundle cannot be found
            if(StringUtils.isNotBlank(path)) {
                return this.cache(app.getIdentifier(),
                        this.convert(app.getIdentifier(), workspace.iconForFile(path), size), size);
            }
        }
        if(null == icon) {
            return this.iconNamed("notfound.tiff", size);
        }
        return icon;
    }

    /**
     * @param file File
     * @param size Requested size
     * @return Cached icon
     */
    @Override
    public NSImage fileIcon(final Path file, final Integer size) {
        if(file.getType().contains(Path.Type.decrypted)) {
            final NSImage badge = this.iconNamed("unlockedbadge.tiff", size);
            badge.setName("unlockedbadge");
            if(file.isDirectory()) {
                return this.folderIcon(size, badge);
            }
            return this.documentIcon(StringUtils.lowerCase(file.getExtension()), size, badge);
        }
        if(file.isSymbolicLink()) {
            final NSImage badge = this.iconNamed("aliasbadge.tiff", size);
            badge.setName("aliasbadge");
            if(file.isDirectory()) {
                return this.folderIcon(size, badge);
            }
            return this.documentIcon(StringUtils.lowerCase(file.getExtension()), size, badge);
        }
        if(file.isFile()) {
            if(StringUtils.isEmpty(file.getExtension())) {
                if(file.attributes().getPermission().isExecutable()) {
                    return this.iconNamed("executable.tiff", size);
                }
            }
            return this.documentIcon(StringUtils.lowerCase(file.getExtension()), size);
        }
        if(file.isDirectory()) {
            if(Permission.EMPTY != file.attributes().getPermission()) {
                if(!file.attributes().getPermission().isExecutable()) {
                    final NSImage badge = this.iconNamed("privatefolderbadge.tiff", size);
                    badge.setName("privatefolderbadge");
                    return this.folderIcon(size, badge);
                }
                if(!file.attributes().getPermission().isReadable()) {
                    if(file.attributes().getPermission().isWritable()) {
                        final NSImage badge = this.iconNamed("dropfolderbadge.tiff", size);
                        badge.setName("dropfolderbadge");
                        return this.folderIcon(size, badge);
                    }
                }
                if(!file.attributes().getPermission().isWritable()) {
                    final NSImage badge = this.iconNamed("readonlyfolderbadge.tiff", size);
                    badge.setName("readonlyfolderbadge");
                    return this.folderIcon(size, badge);
                }
            }
            return this.folderIcon(size);
        }
        return this.iconNamed("notfound.tiff", size);
    }

    @Override
    public NSImage aliasIcon(final String extension, final Integer size) {
        return this.badge(this.iconNamed("aliasbadge.tiff", size), this.documentIcon(extension, size));
    }

    private NSImage convert(final String name, final NSImage icon, final Integer size) {
        return this.convert(name, icon, size, size);
    }

    private NSImage convert(final String name, final NSImage image, final Integer width, final Integer height) {
        if(null == image) {
            return null;
        }
        if(StringUtils.equals(Path.getExtension(name), "pdf")) {
            image.setTemplate(true);
            // Images requested using this method and whose name ends in the word “Template”
            // are automatically marked as template images
        }
        if(null == width || null == height) {
            log.debug("Return default size for {}", image.name());
            return image;
        }
        // Make a copy of original image. Otherwise might resize other references already displayed
        final NSImage copy = image.copy();
        copy.setSize(new NSSize(width, height));
        return copy;
    }
}
