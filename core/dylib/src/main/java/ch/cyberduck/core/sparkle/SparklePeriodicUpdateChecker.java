package ch.cyberduck.core.sparkle;

/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
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
 */

import ch.cyberduck.binding.Outlet;
import ch.cyberduck.core.Controller;
import ch.cyberduck.core.FactoryException;
import ch.cyberduck.core.updater.AbstractPeriodicUpdateChecker;

import org.apache.log4j.Logger;

public class SparklePeriodicUpdateChecker extends AbstractPeriodicUpdateChecker {
    private static final Logger log = Logger.getLogger(SparklePeriodicUpdateChecker.class);

    @Outlet
    private Updater updater;

    public SparklePeriodicUpdateChecker(final Controller controller) {
        super(controller);
        try {
            updater = Updater.create();
        }
        catch(FactoryException e) {
            log.warn(String.format("Updater is disabled. %s", e.getMessage()));
        }
    }

    @Override
    public void check(boolean background) {
        if(this.hasUpdatePrivileges()) {
            if(background) {
                updater.checkForUpdatesInBackground();
            }
            else {
                updater.checkForUpdates(null);
            }
        }
    }

    @Override
    public boolean hasUpdatePrivileges() {
        return null != Updater.getFeed();
    }
}
