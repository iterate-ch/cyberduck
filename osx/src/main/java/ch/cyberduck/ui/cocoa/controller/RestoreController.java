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
import ch.cyberduck.core.LoginCallbackFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.pool.SessionPool;
import ch.cyberduck.core.threading.WorkerBackgroundAction;
import ch.cyberduck.core.worker.RestoreWorker;

import java.util.List;

public class RestoreController extends ProxyController {

    private final WindowController parent;
    private final SessionPool pool;

    public RestoreController(final BrowserController parent, final SessionPool pool) {
        this.parent = parent;
        this.pool = pool;
    }

    public void restore(final List<Path> files, final ReloadCallback callback) {
        parent.background(new WorkerBackgroundAction<>(parent, pool,
            new RestoreWorker(LoginCallbackFactory.get(parent), files) {
                @Override
                public void cleanup(final List<Path> result) {
                    callback.done(result);
                }
            }
        ));
    }
}
