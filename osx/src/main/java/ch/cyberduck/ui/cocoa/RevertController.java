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
import ch.cyberduck.core.Path;
import ch.cyberduck.core.threading.WorkerBackgroundAction;
import ch.cyberduck.core.worker.RevertWorker;

import java.util.List;

public class RevertController extends ProxyController {

    private final BrowserController parent;

    public RevertController(final BrowserController parent) {
        this.parent = parent;
    }

    public void revert(final List<Path> files) {
        parent.background(new WorkerBackgroundAction<List<Path>>(parent, parent.getSession(),
                new RevertWorker(files) {
                    @Override
                    public void cleanup(final List<Path> result) {
                        parent.reload(parent.workdir(), files, files);
                    }
                }
        ));
    }
}
