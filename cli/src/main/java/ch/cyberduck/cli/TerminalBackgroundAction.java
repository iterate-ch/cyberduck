package ch.cyberduck.cli;

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

import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.pool.SessionPool;
import ch.cyberduck.core.threading.AlertCallback;
import ch.cyberduck.core.threading.BackgroundActionListener;
import ch.cyberduck.core.threading.WorkerBackgroundAction;
import ch.cyberduck.core.worker.Worker;

public class TerminalBackgroundAction<T> extends WorkerBackgroundAction<T> {

    public TerminalBackgroundAction(final TerminalController controller,
                                    final SessionPool session,
                                    final Worker<T> worker) {
        super(controller, session, worker);
    }

    public TerminalBackgroundAction(final TerminalController controller,
                                    final SessionPool session,
                                    final Worker<T> worker,
                                    final ProgressListener progress) {
        super(controller, session, worker, progress);
    }

    public TerminalBackgroundAction(final BackgroundActionListener listener,
                                    final SessionPool session,
                                    final Worker<T> worker,
                                    final ProgressListener progress,
                                    final AlertCallback alert) {
        super(listener, session, worker, progress, alert);
    }
}
