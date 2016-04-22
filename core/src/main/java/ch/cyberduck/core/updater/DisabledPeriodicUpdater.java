package ch.cyberduck.core.updater;

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

import java.time.Duration;

public class DisabledPeriodicUpdater implements PeriodicUpdateChecker {
    @Override
    public void unregister() {
        //
    }

    @Override
    public Duration register() {
        return Duration.ofMillis(0L);
    }

    @Override
    public void check(final boolean background) {
        //
    }

    @Override
    public boolean hasUpdatePrivileges() {
        return false;
    }
}
