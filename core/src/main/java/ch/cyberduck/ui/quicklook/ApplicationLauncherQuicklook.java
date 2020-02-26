package ch.cyberduck.ui.quicklook;

/*
 * Copyright (c) 2002-2020 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import ch.cyberduck.core.Local;
import ch.cyberduck.core.local.ApplicationLauncher;
import ch.cyberduck.core.local.ApplicationLauncherFactory;

import java.util.ArrayList;
import java.util.List;

public class ApplicationLauncherQuicklook implements QuickLook {

    private final ApplicationLauncher launcher = ApplicationLauncherFactory.get();
    private final List<Local> selected = new ArrayList<>();

    @Override
    public void select(final List<Local> files) {
        selected.clear();
        selected.addAll(files);
    }

    @Override
    public boolean isOpen() {
        return false;
    }

    @Override
    public void open() {
        for(Local f : selected) {
            launcher.open(f);
        }
    }

    @Override
    public void close() {
    }
}
