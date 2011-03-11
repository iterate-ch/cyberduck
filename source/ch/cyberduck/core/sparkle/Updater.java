package ch.cyberduck.core.sparkle;

/*
 * Copyright (c) 2002-2010 David Kocher. All rights reserved.
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

import ch.cyberduck.core.Preferences;
import ch.cyberduck.ui.cocoa.foundation.NSObject;

import org.rococoa.ID;
import org.rococoa.ObjCClass;

import org.apache.log4j.Logger;

/**
 * @version $Id:$
 */
public abstract class Updater extends NSObject {
    private static final _Class CLASS = org.rococoa.Rococoa.createClass("SUUpdater", _Class.class);

    private static Logger log = Logger.getLogger(Updater.class);

    public static Updater create() {
        return CLASS.alloc().init();
    }

    public interface _Class extends ObjCClass {
        Updater alloc();
    }

    public static String getFeed() {
        return Preferences.instance().getDefault("SUFeedURL");
    }

    public abstract Updater init();

    public abstract void checkForUpdates(ID sender);
}
