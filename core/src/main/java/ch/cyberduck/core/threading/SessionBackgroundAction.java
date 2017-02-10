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
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.TranscriptListener;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.pool.SessionPool;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.text.MessageFormat;

public abstract class SessionBackgroundAction<T> extends AbstractBackgroundAction<T> implements ProgressListener, TranscriptListener {
    private static final Logger log = Logger.getLogger(SessionBackgroundAction.class);

    /**
     * This action encountered one or more exceptions
     */
    private boolean failed;

    /**
     * Contains the transcript of the session while this action was running
     */
    private StringBuilder transcript
            = new StringBuilder();

    private static final String LINE_SEPARATOR
            = System.getProperty("line.separator");

    private final AlertCallback alert;
    private final ProgressListener progressListener;
    private final TranscriptListener transcriptListener;

    private final FailureDiagnostics<BackgroundException> diagnostics
            = new DefaultFailureDiagnostics();

    protected final SessionPool pool;

    public SessionBackgroundAction(final SessionPool pool,
                                   final AlertCallback alert,
                                   final ProgressListener progress,
                                   final TranscriptListener transcript) {
        this.pool = pool;
        this.alert = alert;
        this.progressListener = progress;
        this.transcriptListener = transcript;
    }

    @Override
    public void message(final String message) {
        progressListener.message(message);
    }

    /**
     * Append to the transcript and notify listeners.
     */
    @Override
    public void log(final Type request, final String message) {
        transcript.append(message).append(LINE_SEPARATOR);
        transcriptListener.log(request, message);
    }

    @Override
    public void prepare() {
        super.prepare();
        this.message(this.getActivity());
    }

    protected void reset() throws BackgroundException {
        // Clear the transcript and exceptions
        transcript = new StringBuilder();
        // Reset the failure status but remember the previous exception for automatic retry.
        failed = false;
    }

    /**
     * @return True if the the action had a permanent failures. Returns false if
     * there were only temporary exceptions and the action succeeded upon retry
     */
    public boolean hasFailed() {
        return failed;
    }

    @Override
    public T call() throws BackgroundException {
        try {
            return new DefaultRetryCallable<T>(new DefaultRetryCallable.BackgroundExceptionCallable<T>() {
                @Override
                public T call() throws BackgroundException {
                    // Reset status
                    SessionBackgroundAction.this.reset();
                    // Run action
                    return SessionBackgroundAction.this.run();
                }
            }, progressListener, this).call();
        }
        catch(ConnectionCanceledException e) {
            throw e;
        }
        catch(BackgroundException e) {
            failed = true;
            throw e;
        }
    }

    @Override
    public T run() throws BackgroundException {
        final Session<?> session = pool.borrow(this).withListener(this);
        try {
            return this.run(session);
        }
        catch(BackgroundException e) {
            pool.release(session.removeListener(this), e);
            throw e;
        }
        finally {
            pool.release(session.removeListener(this), null);
        }
    }

    public abstract T run(final Session<?> session) throws BackgroundException;

    /**
     * The number of times a new connection attempt should be made. Takes into
     * account the number of times already tried.
     *
     * @param failure   Connect failure
     * @param remaining Remaining number of connect attempts. The initial connection attempt does not count
     * @return Greater than zero if a failed action should be repeated again
     */
    protected boolean retry(final BackgroundException failure, final int remaining) {
        if(remaining > 0) {
            if(diagnostics.determine(failure) == FailureDiagnostics.Type.network) {
                if(log.isInfoEnabled()) {
                    log.info(String.format("Retry for failure %s", failure));
                }
                // This is an automated retry. Wait some time first.
                this.pause(remaining);
                // Retry to connect
                return true;
            }
        }
        return false;
    }

    /**
     * Idle this action for some time. Blocks the caller.
     */
    protected void pause(final int attempt) {
        final BackgroundActionPauser pauser = new BackgroundActionPauser(new BackgroundActionPauser.Callback() {
            @Override
            public boolean isCanceled() {
                return SessionBackgroundAction.this.isCanceled();
            }

            @Override
            public void progress(final Integer delay) {
                progressListener.message(MessageFormat.format(LocaleFactory.localizedString("Retry again in {0} seconds ({1} more attempts)", "Status"),
                        delay, attempt));
            }
        });
        if(log.isInfoEnabled()) {
            log.info(String.format("Pause failed background action %s", this));
        }
        pauser.await();
    }

    @Override
    public boolean alert(final BackgroundException failure) {
        if(!this.isCanceled()) {
            if(log.isInfoEnabled()) {
                log.info(String.format("Run alert callback %s for failure %s", alert, failure));
            }
            // Display alert if the action was not canceled intentionally
            return alert.alert(pool.getHost(), failure, transcript);
        }
        return false;
    }

    @Override
    public void cleanup() {
        this.message(StringUtils.EMPTY);
    }

    @Override
    public String getName() {
        return BookmarkNameProvider.toString(pool.getHost());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SessionBackgroundAction{");
        sb.append("failed=").append(failed);
        sb.append(", pool=").append(pool);
        sb.append('}');
        return sb.toString();
    }
}
