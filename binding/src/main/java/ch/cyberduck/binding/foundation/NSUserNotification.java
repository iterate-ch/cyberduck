package ch.cyberduck.binding.foundation;

/*
 * Copyright (c) 2002-2012 David Kocher. All rights reserved.
 *
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

import ch.cyberduck.binding.application.NSImage;

import org.rococoa.ObjCClass;

public abstract class NSUserNotification extends NSObject {
    private static final _Class CLASS = org.rococoa.Rococoa.createClass("NSUserNotification", _Class.class);

    public static NSUserNotification notification() {
        return CLASS.alloc().init();
    }

    public interface _Class extends ObjCClass {
        NSUserNotification alloc();
    }

    public abstract NSUserNotification init();

    public abstract String getTitle();

    public abstract void setTitle(String title);

    public abstract String getSubtitle();

    public abstract void setSubtitle(String subtitle);

    public abstract String getInformativeText();

    public abstract void setInformativeText(String informativeText);

    /**
     * Available in OS X v10.9 and later.
     */
    public abstract NSImage getContentImage();

    /**
     * Available in OS X v10.9 and later.
     */
    public abstract void setContentImage(NSImage contentImage);

    /**
     * Available in OS X v10.9 and later.
     */
    public abstract String getIdentifier();

    /**
     * This identifier is unique to a notification. A notification delivered with the same identifier
     * as an existing notification will replace that notification, rather then display a new one.
     * <p/>
     * Available in OS X v10.9 and later.
     */
    public abstract void setIdentifier(String identifier);
}