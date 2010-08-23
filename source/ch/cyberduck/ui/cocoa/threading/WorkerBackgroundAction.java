package ch.cyberduck.ui.cocoa.threading;

/*
 * Copyright (c) 2002-2010 David Kocher. All rights reserved.
 *
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
 * Bug fixes, suggestions and comments should be sent to:
 * dkocher@cyberduck.ch
 */

import ch.cyberduck.ui.action.Worker;
import ch.cyberduck.ui.cocoa.BrowserController;

/**
 * @version $Id:$
 */
public class WorkerBackgroundAction<T> extends BrowserBackgroundAction {
    private Worker<T> worker;

    private T result;

    public WorkerBackgroundAction(BrowserController controller, Worker<T> worker) {
        super(controller);
        this.worker = worker;
    }

    public void run() {
        result = worker.run();
    }

    @Override
    public void cleanup() {
        worker.cleanup(result);
    }

    @Override
    public String getActivity() {
        return worker.getActivity();
    }
}
