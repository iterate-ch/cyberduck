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
import ch.cyberduck.core.updater.AbstractPeriodicUpdateChecker;

public class SparklePeriodicUpdateChecker extends AbstractPeriodicUpdateChecker {

    @Outlet
    private final Updater updater = Updater.create();

    public SparklePeriodicUpdateChecker(final Controller controller) {
        super(controller);
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
        return null != updater;
    }
}
