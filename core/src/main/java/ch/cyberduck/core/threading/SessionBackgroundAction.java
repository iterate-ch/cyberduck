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
import ch.cyberduck.core.Cache;
import ch.cyberduck.core.ConnectionService;
import ch.cyberduck.core.HostKeyCallback;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.LoginConnectionService;
import ch.cyberduck.core.LoginService;
import ch.cyberduck.core.PasswordStoreFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.TranscriptListener;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.preferences.PreferencesFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.text.MessageFormat;

public abstract class SessionBackgroundAction<T> extends AbstractBackgroundAction<T>
        implements ProgressListener, TranscriptListener {
    private static final Logger log = Logger.getLogger(SessionBackgroundAction.class);

    /**
     * Contains all exceptions thrown while this action was running
     */
    private BackgroundException exception;

    /**
     * This action encountered one or more exceptions
     */
    private boolean failed;

    /**
     * Contains the transcript of the session while this action was running
     */
    private StringBuilder transcript
            = new StringBuilder();

    /**
     * The number of times this action has been run
     */
    private int repeat = 0;

    private static final String LINE_SEPARATOR
            = System.getProperty("line.separator");

    private final AlertCallback alert;

    private final ProgressListener progressListener;

    private final TranscriptListener transcriptListener;

    protected final ConnectionService connection;

    private final FailureDiagnostics<Exception> diagnostics
            = new DefaultFailureDiagnostics();

    protected final Session<?> session;

    private final Cache<Path> cache;

    public SessionBackgroundAction(final Session<?> session,
                                   final Cache<Path> cache,
                                   final AlertCallback alert,
                                   final ProgressListener progress,
                                   final TranscriptListener transcript,
                                   final LoginCallback prompt,
                                   final HostKeyCallback key) {
        this(new LoginConnectionService(prompt, key, PasswordStoreFactory.get(),
                progress, transcript), session, cache, alert, progress, transcript);
    }

    public SessionBackgroundAction(final LoginService login,
                                   final Session<?> session,
                                   final Cache<Path> cache,
                                   final AlertCallback alert,
                                   final ProgressListener progress,
                                   final TranscriptListener transcript,
                                   final HostKeyCallback key) {
        this(new LoginConnectionService(login, key, progress, transcript), session, cache, alert, progress, transcript);
    }

    public SessionBackgroundAction(final ConnectionService connection,
                                   final Session<?> session,
                                   final Cache<Path> cache,
                                   final AlertCallback alert,
                                   final ProgressListener progress,
                                   final TranscriptListener transcript) {
        this.connection = connection;
        this.session = session;
        this.cache = cache;
        this.alert = alert;
        this.progressListener = progress;
        this.transcriptListener = transcript;
    }

    public BackgroundException getException() {
        return exception;
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
    public void prepare() throws ConnectionCanceledException {
        super.prepare();
        this.message(this.getActivity());
    }

    @Override
    public void cancel() {
        connection.cancel();
        super.cancel();
    }

    /**
     * The number of times a new connection attempt should be made. Takes into
     * account the number of times already tried.
     *
     * @param failure Failure
     * @return Greater than zero if a failed action should be repeated again
     */
    protected int retry(final BackgroundException failure) throws BackgroundException {
        // The initial connection attempt does not count
        return PreferencesFactory.get().getInteger("connection.retry") - repeat;
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
            // Reset status
            this.reset();
            // Open connection
            this.connect(session);
            // Run action
            return super.call();
        }
        catch(ConnectionCanceledException e) {
            throw e;
        }
        catch(BackgroundException failure) {
            log.warn(String.format("Failure executing background action: %s", failure));
            exception = failure;
            failed = true;
            if(diagnostics.determine(failure) == FailureDiagnostics.Type.network) {
                if(this.retry(failure) > 0) {
                    if(log.isInfoEnabled()) {
                        log.info(String.format("Retry failed background action %s", this));
                    }
                    // This is an automated retry. Wait some time first.
                    this.pause(failure);
                    if(!this.isCanceled()) {
                        repeat++;
                        // Re-run the action with the previous lock used
                        return this.call();
                    }
                }
            }
            throw failure;
        }
        catch(Exception e) {
            log.fatal(String.format("Failure running background task. %s", e.getMessage()), e);
            exception = new BackgroundException(e);
            failed = true;
            throw e;
        }
    }

    protected boolean connect(final Session session) throws BackgroundException {
        if(connection.check(session, cache, exception)) {
            return true;
        }
        // Use existing connection
        return false;
    }

    protected void close(final Session session) throws BackgroundException {
        session.close();
        cache.clear();
    }

    @Override
    public boolean alert(final BackgroundException e) {
        if(this.hasFailed() && !this.isCanceled()) {
            if(log.isInfoEnabled()) {
                log.info(String.format("Display alert for failure %s", exception));
            }
            // Display alert if the action was not canceled intentionally
            return alert.alert(session.getHost(), exception, transcript);
        }
        return false;
    }

    @Override
    public void cleanup() {
        this.message(StringUtils.EMPTY);
    }

    /**
     * Idle this action for some time. Blocks the caller.
     *
     * @param failure Failure
     */
    protected void pause(final BackgroundException failure) throws BackgroundException {
        final int attempt = this.retry(failure);
        final BackgroundActionPauser pauser = new BackgroundActionPauser(new BackgroundActionPauser.Callback() {
            @Override
            public boolean isCanceled() {
                return SessionBackgroundAction.this.isCanceled();
            }

            @Override
            public void progress(final Integer delay) {
                SessionBackgroundAction.this.message(MessageFormat.format(LocaleFactory.localizedString("Retry again in {0} seconds ({1} more attempts)", "Status"),
                        delay, attempt));
            }
        });
        pauser.await();
    }

    @Override
    public String getName() {
        return BookmarkNameProvider.toString(session.getHost());
    }

    /**
     * @return The session instance
     */
    @Override
    public Object lock() {
        return session;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SessionBackgroundAction{");
        sb.append("session=").append(session);
        sb.append(", failed=").append(failed);
        sb.append(", exception=").append(exception);
        sb.append('}');
        return sb.toString();
    }
}
