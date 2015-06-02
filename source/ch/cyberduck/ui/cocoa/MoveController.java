package ch.cyberduck.ui.cocoa;

/*
 * Copyright (c) 2002-2015 David Kocher. All rights reserved.
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

import ch.cyberduck.binding.ProxyController;
import ch.cyberduck.binding.application.NSAlert;
import ch.cyberduck.binding.application.NSCell;
import ch.cyberduck.binding.application.SheetCallback;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.threading.DefaultMainAction;
import ch.cyberduck.core.threading.MainAction;
import ch.cyberduck.core.threading.WorkerBackgroundAction;
import ch.cyberduck.core.worker.MoveWorker;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @version $Id$
 */
public class MoveController extends ProxyController {

    private Preferences preferences
            = PreferencesFactory.get();

    private BrowserController parent;

    public MoveController(final BrowserController parent) {
        this.parent = parent;
    }

    /**
     * Displays a warning dialog about files to be moved
     *
     * @param selected The files to check for existence
     */
    private void checkMove(final Map<Path, Path> selected, final MainAction action) {
        if(preferences.getBoolean("browser.move.confirm")) {
            StringBuilder alertText = new StringBuilder(
                    LocaleFactory.localizedString("Do you want to move the selected files?"));
            int i = 0;
            boolean rename = false;
            Iterator<Map.Entry<Path, Path>> iter;
            for(iter = selected.entrySet().iterator(); i < 10 && iter.hasNext(); ) {
                final Map.Entry<Path, Path> next = iter.next();
                if(next.getKey().getParent().equals(next.getValue().getParent())) {
                    rename = true;
                }
                alertText.append(String.format("\n%s %s", Character.toString('\u2022'), next.getKey().getName()));
                i++;
            }
            if(iter.hasNext()) {
                alertText.append(String.format("\n%s ...)", Character.toString('\u2022')));
            }
            final NSAlert alert = NSAlert.alert(
                    rename ? LocaleFactory.localizedString("Rename", "Transfer") : LocaleFactory.localizedString("Move", "Transfer"), //title
                    alertText.toString(),
                    rename ? LocaleFactory.localizedString("Rename", "Transfer") : LocaleFactory.localizedString("Move", "Transfer"), // default button
                    LocaleFactory.localizedString("Cancel"), //alternative button
                    null //other button
            );
            alert.setShowsSuppressionButton(true);
            alert.suppressionButton().setTitle(LocaleFactory.localizedString("Don't ask again", "Configuration"));
            parent.alert(alert, new SheetCallback() {
                @Override
                public void callback(final int returncode) {
                    if(alert.suppressionButton().state() == NSCell.NSOnState) {
                        // Never show again.
                        preferences.setProperty("browser.move.confirm", false);
                    }
                    if(returncode == DEFAULT_OPTION) {
                        new OverwriteController(parent).overwrite(new ArrayList<Path>(selected.values()), action);
                    }
                }
            });
        }
        else {
            new OverwriteController(parent).overwrite(new ArrayList<Path>(selected.values()), action);
        }
    }

    public void rename(final Map<Path, Path> selected) {
        this.checkMove(selected, new DefaultMainAction() {
            @Override
            public void run() {
                background(new WorkerBackgroundAction<List<Path>>(parent, parent.getSession(), parent.getCache(),
                                new MoveWorker(parent.getSession(), selected, parent) {
                                    @Override
                                    public void cleanup(final List<Path> moved) {
                                        parent.reload(moved, new ArrayList<Path>(selected.values()));
                                    }
                                }
                        )
                );
            }
        });
    }
}
