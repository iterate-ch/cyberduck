package ch.cyberduck.core.sparkle;

/*
 * Copyright (c) 2002-2023 iterate GmbH. All rights reserved.
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

import ch.cyberduck.binding.ProxyController;
import ch.cyberduck.binding.application.NSMenuItem;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.updater.UpdateChecker;
import ch.cyberduck.core.updater.UpdateChecker.Handler;

import java.text.MessageFormat;

public class MenuItemSparkleUpdateHandler extends ProxyController implements Handler {

    private final NSMenuItem menu;

    public MenuItemSparkleUpdateHandler(final NSMenuItem menu) {
        this.menu = menu;
    }

    @Override
    public boolean handle(final UpdateChecker.Update item) {
        menu.setTitle(String.format("%s. %s",
                MessageFormat.format(LocaleFactory.localizedString("Version {0} is now available", "Updater"), item.getDisplayVersionString()),
                String.format("%s…", LocaleFactory.localizedString("Install and Relaunch", "Updater"))));
        menu.setRepresentedObject(item.getRevision());
        return false;
    }
}
