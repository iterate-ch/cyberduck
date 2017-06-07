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
import ch.cyberduck.binding.application.SheetCallback;
import ch.cyberduck.core.Cache;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.LoginCallbackFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathNormalizer;
import ch.cyberduck.core.threading.WorkerBackgroundAction;
import ch.cyberduck.core.worker.DeleteWorker;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class DeleteController extends ProxyController {

    private final BrowserController parent;

    public DeleteController(final BrowserController parent) {
        this.parent = parent;
    }

    /**
     * Recursively deletes the files
     *
     * @param selected The files selected in the browser to delete
     */
    public void delete(final List<Path> selected) {
        final List<Path> normalized = PathNormalizer.normalize(selected);
        if(normalized.isEmpty()) {
            return;
        }
        final StringBuilder alertText = new StringBuilder(MessageFormat.format(LocaleFactory.localizedString("Delete {0} files"), selected.size()));
        int i = 0;
        Iterator<Path> iter;
        for(iter = normalized.iterator(); i < 10 && iter.hasNext(); ) {
            alertText.append("\n").append(Character.toString('\u2022')).append(" ").append(iter.next().getName());
            i++;
        }
        if(iter.hasNext()) {
            alertText.append("\n").append(Character.toString('\u2022')).append(" " + "…");
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
                    run(normalized);
                }
            }
        });
    }

    private void run(final List<Path> files) {
        final Cache<Path> cache = parent.getCache();
        parent.background(new WorkerBackgroundAction<List<Path>>(parent, parent.getSession(),
                new DeleteWorker(LoginCallbackFactory.get(parent), files, cache, parent) {
                            @Override
                            public void cleanup(final List<Path> deleted) {
                                super.cleanup(deleted);
                                parent.reload(parent.workdir(), files, Collections.emptyList());
                            }
                        }
                )
        );
    }
}
