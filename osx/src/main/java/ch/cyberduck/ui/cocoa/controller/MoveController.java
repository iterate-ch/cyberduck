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

import ch.cyberduck.binding.ProxyController;
import ch.cyberduck.binding.application.NSAlert;
import ch.cyberduck.binding.application.NSCell;
import ch.cyberduck.binding.application.SheetCallback;
import ch.cyberduck.core.Cache;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.LoginCallbackFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Protocol;
import ch.cyberduck.core.SessionPoolFactory;
import ch.cyberduck.core.pool.SessionPool;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.threading.DefaultMainAction;
import ch.cyberduck.core.threading.WorkerBackgroundAction;
import ch.cyberduck.core.worker.MoveWorker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MoveController extends ProxyController {

    private final Preferences preferences
        = PreferencesFactory.get();

    private final BrowserController parent;
    private final Cache<Path> cache;

    public MoveController(final BrowserController parent) {
        this(parent, parent.getCache());
    }

    public MoveController(final BrowserController parent, final Cache<Path> cache) {
        this.parent = parent;
        this.cache = cache;
    }

    /**
     * @param path    The existing file
     * @param renamed The renamed file
     */
    public void rename(final Path path, final Path renamed) {
        this.rename(Collections.singletonMap(path, renamed));
    }

    /**
     * @param selected A map with the original files as the key and the destination
     *                 files as the value
     */
    public void rename(final Map<Path, Path> selected) {
        final DefaultMainAction action = new DefaultMainAction() {
            @Override
            public void run() {
                final SessionPool pool = parent.getSession();
                final MoveWorker move = new MoveWorker(selected, pool.getHost().getProtocol().getStatefulness() == Protocol.Statefulness.stateful ? SessionPoolFactory.create(parent, pool.getHost()) : pool, cache, parent, LoginCallbackFactory.get(parent)) {
                    @Override
                    public void cleanup(final Map<Path, Path> result) {
                        final List<Path> changed = new ArrayList<>();
                        changed.addAll(selected.keySet());
                        changed.addAll(selected.values());
                        parent.reload(parent.workdir(), changed, new ArrayList<>(selected.values()));
                    }
                };
                parent.background(new WorkerBackgroundAction<Map<Path, Path>>(parent, parent.getSession(), move));
            }
        };
        this.rename(selected, action);
    }

    /**
     * Displays a warning dialog about files to be moved
     *
     * @param selected A map with the original files as the key and the destination
     *                 files as the value
     * @param action   Background task
     */
    private void rename(final Map<Path, Path> selected, final DefaultMainAction action) {
        if(preferences.getBoolean("browser.move.confirm")) {
            StringBuilder alertText = new StringBuilder(
                LocaleFactory.localizedString("Do you want to move the selected files?", "Duplicate"));
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
                alertText.append(String.format("\n%s …)", Character.toString('\u2022')));
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
            new OverwriteController(parent, cache).overwrite(new ArrayList<Path>(selected.values()), action);
        }
    }
}
