package ch.cyberduck.ui.growl;

/*
 * Copyright (c) 2002-2014 David Kocher. All rights reserved.
 * http://cyberduck.io/
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
 * feedback@cyberduck.io
 */

import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.library.Native;

import org.apache.log4j.Logger;

/**
 * @version $Id$
 */
public final class GrowlNative implements Growl {
    private static final Logger log = Logger.getLogger(GrowlFactory.class);

    protected GrowlNative() {
        Native.load("Growl");
    }

    public static void register() {
        if(Preferences.instance().getBoolean("growl.enable")) {
            if(Factory.VERSION_PLATFORM.matches("10\\.(5|6|7).*")) {
                GrowlFactory.addFactory(Factory.NATIVE_PLATFORM, new Factory());
            }
            else {
                log.warn(String.format("Skip registering Growl on %s", Factory.NATIVE_PLATFORM));
            }
        }
        else {
            log.warn("Skip registering Growl.");
        }
    }

    private static class Factory extends GrowlFactory {
        @Override
        protected Growl create() {
            return new GrowlNative();
        }
    }

    @Override
    public native void setup();

    @Override
    public void unregister() {
        //
    }

    @Override
    public native void notify(String title, String description);

    @Override
    public native void notifyWithImage(String title, String description, String image);

}
