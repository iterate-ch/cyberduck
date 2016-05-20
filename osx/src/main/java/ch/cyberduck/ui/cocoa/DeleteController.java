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

import ch.cyberduck.binding.application.NSAlert;
import ch.cyberduck.binding.application.SheetCallback;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.LoginCallbackFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathNormalizer;
import ch.cyberduck.core.threading.WorkerBackgroundAction;
import ch.cyberduck.core.worker.DeleteWorker;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class DeleteController extends ProxyController {

    private BrowserController parent;

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
        StringBuilder alertText =
                new StringBuilder(LocaleFactory.localizedString("Really delete the following files? This cannot be undone."));
        int i = 0;
        Iterator<Path> iter;
        for(iter = normalized.iterator(); i < 10 && iter.hasNext(); ) {
            alertText.append("\n").append(Character.toString('\u2022')).append(" ").append(iter.next().getName());
            i++;
        }
        if(iter.hasNext()) {
            alertText.append("\n").append(Character.toString('\u2022')).append(" " + "…");
        }
        NSAlert alert = NSAlert.alert(LocaleFactory.localizedString("Delete"), //title
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


    /**
     * Recursively deletes the file
     *
     * @param file File or directory
     */
    public void delete(final Path file) {
        this.delete(Collections.singletonList(file));
    }

    private void run(final List<Path> files) {
        parent.background(new WorkerBackgroundAction<Boolean>(parent, parent.getSession(), parent.getCache(),
                        new DeleteWorker(LoginCallbackFactory.get(parent), files, parent) {
                            @Override
                            public void cleanup(final Boolean done) {
                                if(done) {
                                    parent.reload(parent.workdir(), files, Collections.<Path>emptyList());
                                }
                            }
                        }
                )
        );
    }
}
