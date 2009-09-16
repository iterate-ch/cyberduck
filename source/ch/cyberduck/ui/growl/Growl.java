package ch.cyberduck.ui.growl;

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

import ch.cyberduck.core.Preferences;

import org.apache.log4j.Logger;

/**
 * @version $Id$
 */
public abstract class Growl {
    private static Logger log = Logger.getLogger(Growl.class);

    private static Growl instance = null;

    protected Growl() {
        //
    }

    private static final Object lock = new Object();

    public static Growl instance() {
        synchronized(lock) {
            if (null == instance) {
                if (Preferences.instance().getBoolean("growl.enable")) {
                    instance = new GrowlNative();
                }
                else {
                    instance = new Growl() {
                        protected void register() {
                            ;
                        }

                        public void notify(String title, String description) {
                            log.info(description);
                        }

                        public void notifyWithImage(String title, String description, String image) {
                            log.info(description);
                        }
                    };
                }
                // Make sure we register to Growl first
                instance.register();
            }
        }
        return instance;
    }

    /**
     * Register application
     */
    protected abstract void register();

    public abstract void notify(String title, String description);

    public abstract void notifyWithImage(String title, String description, String image);
}