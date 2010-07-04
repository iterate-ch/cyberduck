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

    private static Growl current = null;

    private static final Object lock = new Object();

    /**
     * @return The singleton instance of me.
     */
    public static Growl instance() {
        synchronized(lock) {
            if(null == current) {
                if(Preferences.instance().getBoolean("growl.enable")) {
                    current = GrowlFactory.createGrowl();
                }
                else {
                    current = new Growl() {
                        @Override
                        public void register() {
                            log.warn("Growl notifications disabled");
                        }

                        @Override
                        public void notify(String title, String description) {
                            log.info(description);
                        }

                        @Override
                        public void notifyWithImage(String title, String description, String image) {
                            log.info(description);
                        }
                    };
                }
            }
        }
        return current;
    }

    /**
     * Register application
     */
    public abstract void register();

    public abstract void notify(String title, String description);

    public abstract void notifyWithImage(String title, String description, String image);
}