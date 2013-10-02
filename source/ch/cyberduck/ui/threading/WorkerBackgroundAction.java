package ch.cyberduck.ui.threading;

/*
 * Copyright (c) 2002-2013 David Kocher. All rights reserved.
 * http://cyberduck.ch/
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
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.ch
 */

import ch.cyberduck.core.Cache;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.ui.Controller;
import ch.cyberduck.ui.action.Worker;

/**
 * @version $Id$
 */
public class WorkerBackgroundAction<T> extends BrowserBackgroundAction<Boolean> {

    private Worker<T> worker;

    private T result;

    public WorkerBackgroundAction(final Controller controller, final Session session,
                                  final Worker<T> worker) {
        super(controller, session, Cache.empty());
        this.worker = worker;
    }

    @Override
    public Boolean run() throws BackgroundException {
        result = worker.run();
        return true;
    }

    @Override
    public void cleanup() {
        super.cleanup();
        worker.cleanup(result);
    }

    @Override
    public void cancel() {
        super.cancel();
        worker.cancel();
    }

    @Override
    public String getActivity() {
        return worker.getActivity();
    }
}
