package ch.cyberduck.ui.cocoa.controller;

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

import ch.cyberduck.binding.DisabledSheetCallback;
import ch.cyberduck.binding.ProxyController;
import ch.cyberduck.binding.application.NSAlert;
import ch.cyberduck.core.Cache;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.threading.MainAction;

import java.util.Iterator;
import java.util.List;

public class OverwriteController extends ProxyController {

    private final BrowserController parent;

    private final Cache<Path> cache;

    public OverwriteController(final BrowserController parent) {
        this.parent = parent;
        this.cache = parent.getCache();
    }

    public OverwriteController(final BrowserController parent, final Cache<Path> cache) {
        this.parent = parent;
        this.cache = cache;
    }

    /**
     * Displays a warning dialog about already existing files
     *
     * @param selected The files to check
     */
    public void overwrite(final List<Path> selected, final MainAction action) {
        StringBuilder alertText = new StringBuilder(
                LocaleFactory.localizedString("A file with the same name already exists. Do you want to replace the existing file?"));
        int i = 0;
        Iterator<Path> iter;
        boolean shouldWarn = false;
        for(iter = selected.iterator(); iter.hasNext(); ) {
            final Path item = iter.next();
            if(cache.get(item.getParent()).contains(item)) {
                if(i < 10) {
                    alertText.append("\n").append(Character.toString('\u2022')).append(" ").append(item.getName());
                }
                shouldWarn = true;
            }
            i++;
        }
        if(i >= 10) {
            alertText.append("\n").append(Character.toString('\u2022')).append(" …)");
        }
        if(shouldWarn) {
            NSAlert alert = NSAlert.alert(
                    LocaleFactory.localizedString("Overwrite"), //title
                    alertText.toString(),
                    LocaleFactory.localizedString("Overwrite"), // defaultbutton
                    LocaleFactory.localizedString("Cancel"), //alternative button
                    null //other button
            );
            parent.alert(alert, new DisabledSheetCallback() {
                @Override
                public void callback(final int returncode) {
                    if(returncode == DEFAULT_OPTION) {
                        action.run();
                    }
                }
            });
        }
        else {
            action.run();
        }
    }
}
