package ch.cyberduck.core.threading;

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

import ch.cyberduck.core.Controller;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.TranscriptListener;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.pool.SessionPool;
import ch.cyberduck.core.worker.TransferWorker;

import org.apache.log4j.Logger;

import java.util.Objects;

public class TransferWorkerBackgroundAction<T> extends RegistryBackgroundAction<T> {
    private static final Logger log = Logger.getLogger(WorkerBackgroundAction.class);

    protected final SessionPool destination;
    protected final TransferWorker<T> worker;
    protected T result;

    public TransferWorkerBackgroundAction(final Controller controller,
                                          final SessionPool source, final SessionPool destination,
                                          final TransferWorker<T> worker) {
        super(controller, source);
        this.destination = destination;
        this.worker = worker;
    }

    public TransferWorkerBackgroundAction(final Controller controller,
                                          final SessionPool source, final SessionPool destination,
                                          final TransferWorker<T> worker,
                                          final ProgressListener progress,
                                          final TranscriptListener transcript) {
        super(controller, source, progress, transcript);
        this.destination = destination;
        this.worker = worker;
    }

    @Override
    protected void reset() throws BackgroundException {
        worker.reset();
        super.reset();
    }

    @Override
    public T run(final Session<?> source) throws BackgroundException {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Run worker %s", worker));
        }
        final Session<?> target = destination.borrow(this);
        try {
            result = worker.run(source, target);
        }
        catch(ConnectionCanceledException e) {
            worker.cancel();
            throw e;
        }
        catch(BackgroundException e) {
            destination.release(source, e);
            throw e;
        }
        finally {
            destination.release(source, null);
        }
        return result;
    }


    @Override
    public void cleanup() {
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
        super.cleanup();
    }

    @Override
    public void cancel() {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Cancel worker %s", worker));
        }
        worker.cancel();
        super.cancel();
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
        final TransferWorkerBackgroundAction<?> that = (TransferWorkerBackgroundAction<?>) o;
        return Objects.equals(worker, that.worker);
    }

    @Override
    public int hashCode() {
        return Objects.hash(worker);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("WorkerBackgroundAction{");
        sb.append("worker=").append(worker);
        sb.append('}');
        return sb.toString();
    }
}
