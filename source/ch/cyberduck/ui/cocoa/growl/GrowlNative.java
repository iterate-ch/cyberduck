package ch.cyberduck.ui.cocoa.growl;

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

import com.apple.cocoa.foundation.NSBundle;

import org.apache.log4j.Logger;

public class GrowlNative extends Growl {
    private static Logger log = Logger.getLogger(Growl.class);

    static {
        try {
            NSBundle bundle = NSBundle.mainBundle();
            String lib = bundle.resourcePath() + "/Java/" + "libGrowl.dylib";
            log.info("Locating libGrowl.dylib at '" + lib + "'");
            System.load(lib);
        }
        catch (UnsatisfiedLinkError e) {
            log.error("Could not load the Growl library:" + e.getMessage());
        }
    }

    public native void register();

    public native void notify(String title, String description);

    public native void notifyWithImage(String title, String description, String image);

}
