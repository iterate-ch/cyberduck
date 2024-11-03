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
 * Bug fixes, suggestions and comments should be sent to:
 * feedback@cyberduck.ch
 */

import ch.cyberduck.core.BookmarkNameProvider;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.TranscriptListener;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.pool.SessionPool;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class SessionBackgroundAction<T> extends AbstractBackgroundAction<T> implements ProgressListener, TranscriptListener {
    private static final Logger log = LogManager.getLogger(SessionBackgroundAction.class);

    /**
     * This action encountered one or more exceptions
     */
    private BackgroundException failure;

    /**
     * Contains the transcript of the session while this action was running
     */
    private final StringBuffer transcript
            = new StringBuffer();

    private static final String LINE_SEPARATOR
            = System.lineSeparator();

    private final AlertCallback alert;
    private final ProgressListener progress;

    protected final SessionPool pool;

    public SessionBackgroundAction(final SessionPool pool,
                                   final AlertCallback alert,
                                   final ProgressListener progress) {
        this.pool = pool;
        this.alert = alert;
        this.progress = progress;
    }

    @Override
    public void message(final String message) {
        progress.message(message);
    }

    /**
     * Append to the transcript and notify listeners.
     */
    @Override
    public void log(final Type request, final String message) {
        transcript.append(message).append(LINE_SEPARATOR);
    }

    @Override
    public void prepare() {
        super.prepare();
        this.message(this.getActivity());
    }

    protected void reset() throws BackgroundException {
        // Reset the failure status but remember the previous exception for automatic retry.
        failure = null;
    }

    /**
     * @return True if the action had a permanent failures. Returns false if there were only temporary exceptions
     * and the action succeeded upon retry
     */
    public boolean hasFailed() {
        return failure != null;
    }

    public BackgroundException getFailure() {
        return failure;
    }

    @Override
    public T call() throws BackgroundException {
        return new DefaultRetryCallable<>(pool.getHost(), new BackgroundExceptionCallable<T>() {
            @Override
            public T call() throws BackgroundException {
                // Reset status
                SessionBackgroundAction.this.reset();
                // Run action
                return SessionBackgroundAction.this.run();
            }
        }, this, this).call();
    }

    @Override
    public T run() throws BackgroundException {
        final Session<?> session;
        try {
            session = pool.borrow(this).withListener(this);
        }
        catch(BackgroundException e) {
            failure = e;
            throw e;
        }
        try {
            return this.run(session);
        }
        catch(BackgroundException e) {
            failure = e;
            throw e;
        }
        finally {
            pool.release(session.removeListener(this), failure);
        }
    }

    public abstract T run(final Session<?> session) throws BackgroundException;

    @Override
    public boolean alert(final BackgroundException failure) {
        // Display alert if the action was not canceled intentionally
        if(this.isCanceled()) {
            return false;
        }
        log.info("Run alert callback {} for failure {}", alert, failure);
        // Display alert if the action was not canceled intentionally
        return alert.alert(pool.getHost(), failure, new StringBuilder(transcript.toString()));
    }

    @Override
    public void cleanup() {
        transcript.setLength(0);
        this.message(StringUtils.EMPTY);
    }

    @Override
    public String getName() {
        return BookmarkNameProvider.toString(pool.getHost());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SessionBackgroundAction{");
        sb.append("failure=").append(failure);
        sb.append(", pool=").append(pool);
        sb.append('}');
        return sb.toString();
    }

}
