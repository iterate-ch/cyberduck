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
import ch.cyberduck.binding.WindowController;
import ch.cyberduck.binding.application.NSAlert;
import ch.cyberduck.binding.application.SheetCallback;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.LoginCallbackFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathNormalizer;
import ch.cyberduck.core.pool.SessionPool;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.threading.WorkerBackgroundAction;
import ch.cyberduck.core.worker.DeleteWorker;

import java.text.MessageFormat;
import java.util.Iterator;
import java.util.List;

public class DeleteController extends ProxyController {

    private final WindowController parent;
    private final SessionPool pool;
    private final boolean trash;

    public DeleteController(final WindowController parent, final SessionPool pool) {
        this(parent, pool, PreferencesFactory.get().getBoolean("browser.delete.trash"));
    }

    public DeleteController(final WindowController parent, final SessionPool pool, final boolean trash) {
        this.parent = parent;
        this.pool = pool;
        this.trash = trash;
    }

    /**
     * Recursively deletes the files
     *
     * @param selected The files selected in the browser to delete
     */
    public void delete(final List<Path> selected, final ReloadCallback callback) {
        final List<Path> normalized = PathNormalizer.normalize(selected);
        if(normalized.isEmpty()) {
            return;
        }
        final StringBuilder alertText = new StringBuilder(MessageFormat.format(LocaleFactory.localizedString("Delete {0} files"), selected.size()));
        int i = 0;
        Iterator<Path> iter;
        for(iter = normalized.iterator(); i < 10 && iter.hasNext(); ) {
            alertText.append('\n').append('\u2022').append(' ').append(iter.next().getName());
            i++;
        }
        if(iter.hasNext()) {
            alertText.append('\n').append('\u2022').append(' ').append('…');
        }
        final NSAlert alert = NSAlert.alert(LocaleFactory.localizedString("Delete"), //title
                alertText.toString(),
                LocaleFactory.localizedString("Delete"), // defaultbutton
                LocaleFactory.localizedString("Cancel"), //alternative button
                null //other button
        );
        parent.alert(alert, new SheetCallback() {
            @Override
            public void callback(final int returncode) {
                if(returncode == DEFAULT_OPTION) {
                    parent.background(new WorkerBackgroundAction<>(parent, pool,
                                    new DeleteWorker(LoginCallbackFactory.get(parent), normalized, parent, trash) {
                                        @Override
                                        public void cleanup(final List<Path> deleted) {
                                            callback.done(deleted);
                                        }
                                    }
                            )
                    );
                }
                else {
                    callback.cancel();
                }
            }
        });
    }
}
