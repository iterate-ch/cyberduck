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

import ch.cyberduck.core.Factory;

import org.apache.log4j.Logger;

import java.util.HashSet;
import java.util.Set;

/**
 * @version $Id: GrowlFactory.java 5451 2009-10-09 08:34:10Z dkocher $
 */
public abstract class GrowlFactory extends Factory<Growl> {
    private static final Logger log = Logger.getLogger(GrowlFactory.class);

    /**
     * Registered factories
     */
    protected static final Set<GrowlFactory> factories
            = new HashSet<GrowlFactory>();

    public static void addFactory(final Platform platform, final GrowlFactory f) {
        factories.add(f);
    }

    private static Growl notifier;

    public static Growl get() {
        if(null == notifier) {
            Set<Growl> registered = new HashSet<Growl>();
            for(GrowlFactory f : factories) {
                registered.add(f.create());
            }
            if(factories.isEmpty()) {
                registered.add(new Disabled());
            }
            notifier = new MultipleNotifier(registered);
        }
        return notifier;
    }

    private static final class Disabled implements Growl {

        @Override
        public void setup() {
            log.warn("Growl notifications disabled");
        }

        @Override
        public void unregister() {
            log.warn("Growl notifications disabled");
        }

        @Override
        public void notify(String title, String description) {
            if(log.isInfoEnabled()) {
                log.info(description);
            }
        }

        @Override
        public void notifyWithImage(String title, String description, String image) {
            if(log.isInfoEnabled()) {
                log.info(description);
            }
        }
    }

    private static final class MultipleNotifier implements Growl {

        private final Set<Growl> notifier;

        private MultipleNotifier(final Set<Growl> notifier) {
            this.notifier = notifier;
        }

        @Override
        public void notify(String title, String description) {
            for(Growl notifier : this.notifier) {
                notifier.notify(title, description);
            }
        }

        @Override
        public void notifyWithImage(String title, String description, String image) {
            for(Growl notifier : this.notifier) {
                notifier.notifyWithImage(title, description, image);
            }
        }

        @Override
        public void setup() {
            for(Growl notifier : this.notifier) {
                notifier.setup();
            }
        }

        @Override
        public void unregister() {
            for(Growl notifier : this.notifier) {
                notifier.unregister();
            }
        }
    }
}
