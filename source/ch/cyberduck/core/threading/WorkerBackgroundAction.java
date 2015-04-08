package ch.cyberduck.core.threading;

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
import ch.cyberduck.core.ConnectionService;
import ch.cyberduck.core.Controller;
import ch.cyberduck.core.LoginService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.worker.Worker;

import org.apache.log4j.Logger;

/**
 * @version $Id$
 */
public class WorkerBackgroundAction<T> extends BrowserBackgroundAction<Boolean> {
    private static final Logger log = Logger.getLogger(WorkerBackgroundAction.class);

    protected Worker<T> worker;

    private T result;

    public WorkerBackgroundAction(final Controller controller,
                                  final Session session,
                                  final Worker<T> worker) {
        this(controller, session, PathCache.empty(), worker);
    }

    public WorkerBackgroundAction(final LoginService login,
                                  final Controller controller,
                                  final Session session,
                                  final Cache<Path> cache,
                                  final Worker<T> worker) {
        super(login, controller, session, cache);
        this.worker = worker;
    }

    public WorkerBackgroundAction(final ConnectionService connection,
                                  final Controller controller,
                                  final Session session,
                                  final Cache<Path> cache,
                                  final Worker<T> worker) {
        super(connection, controller, session, cache);
        this.worker = worker;
    }

    public WorkerBackgroundAction(final Controller controller,
                                  final Session session,
                                  final Cache<Path> cache,
                                  final Worker<T> worker) {
        super(controller, session, cache);
        this.worker = worker;
    }

    @Override
    protected void reset() throws BackgroundException {
        worker.reset();
        super.reset();
    }

    @Override
    public Boolean run() throws BackgroundException {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Run worker %s", worker));
        }
        try {
            result = worker.run();
        }
        catch(ConnectionCanceledException e) {
            worker.cancel();
            throw e;
        }
        return true;
    }

    @Override
    public void cleanup() {
        super.cleanup();
        if(null == result) {
            log.warn(String.format("Missing result for worker %s. Use default value.", worker));
            worker.cleanup(worker.initialize());
        }
        else {
            if(log.isDebugEnabled()) {
                log.debug(String.format("Cleanup worker %s", worker));
            }
            worker.cleanup(result);
        }
    }

    @Override
    public void cancel() {
        super.cancel();
        if(log.isDebugEnabled()) {
            log.debug(String.format("Cancel worker %s", worker));
        }
        worker.cancel();
    }

    @Override
    public boolean isCanceled() {
        return worker.isCanceled();
    }

    @Override
    public String getActivity() {
        return worker.getActivity();
    }

    @Override
    public boolean equals(final Object o) {
        if(this == o) {
            return true;
        }
        if(o == null || getClass() != o.getClass()) {
            return false;
        }
        final WorkerBackgroundAction that = (WorkerBackgroundAction) o;
        if(worker != null ? !worker.equals(that.worker) : that.worker != null) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return worker != null ? worker.hashCode() : 0;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("WorkerBackgroundAction{");
        sb.append("worker=").append(worker);
        sb.append(", result=").append(result);
        sb.append('}');
        return sb.toString();
    }
}
