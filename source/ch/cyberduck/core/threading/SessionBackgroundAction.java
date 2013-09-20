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

import ch.cyberduck.core.HostKeyController;
import ch.cyberduck.core.LoginConnectionService;
import ch.cyberduck.core.LoginController;
import ch.cyberduck.core.PasswordStoreFactory;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.TranscriptListener;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.ui.growl.Growl;
import ch.cyberduck.ui.growl.GrowlFactory;

import org.apache.log4j.Logger;

import java.util.List;

/**
 * @version $Id$
 */
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
    private StringBuilder transcript;

    /**
     * The number of times this action has been run
     */
    protected int repeatCount;

    private static final String LINE_SEPARATOR
            = System.getProperty("line.separator");

    private AlertCallback alert;

    protected ProgressListener progressListener;

    private TranscriptListener transcriptListener;

    private LoginConnectionService connection;

    private Growl growl = GrowlFactory.get();

    public SessionBackgroundAction(final AlertCallback alert,
                                   final ProgressListener progressListener,
                                   final TranscriptListener transcriptListener,
                                   final LoginController prompt,
                                   final HostKeyController key) {
        this.alert = alert;
        this.progressListener = progressListener;
        this.transcriptListener = transcriptListener;
        this.connection = new LoginConnectionService(prompt, key,
                PasswordStoreFactory.get(), progressListener);
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
    public void log(final boolean request, final String message) {
        transcript.append(message).append(LINE_SEPARATOR);
        transcriptListener.log(request, message);
    }

    @Override
    public void init() {
        this.reset();
    }

    @Override
    public void prepare() throws ConnectionCanceledException {
        super.prepare();
        this.message(this.getActivity());
        for(Session s : this.getSessions()) {
            s.addProgressListener(this);
            s.addTranscriptListener(this);
        }
    }

    @Override
    public void cancel() {
        connection.cancel();
        super.cancel();
    }

    /**
     * To be overridden in concrete subclass
     *
     * @return The session if any or null if invalid
     */
    public abstract List<Session<?>> getSessions();

    /**
     * The number of times a new connection attempt should be made. Takes into
     * account the number of times already tried.
     *
     * @return Greater than zero if a failed action should be repeated again
     */
    public int retry() {
        if(this.hasFailed() && !this.isCanceled()) {
            // Check for an exception we consider possibly temporary
            if(new NetworkFailureDiagnostics().isNetworkFailure(exception)) {
                // The initial connection attempt does not count
                return Preferences.instance().getInteger("connection.retry") - repeatCount;
            }
        }
        return 0;
    }

    protected void reset() {
        // Clear the transcript and exceptions
        transcript = new StringBuilder();
        failed = false;
        exception = null;
    }

    /**
     * @return True if the the action had a permanent failures. Returns false if
     *         there were only temporary exceptions and the action suceeded upon retry
     * @see #retry()
     */
    protected boolean hasFailed() {
        return failed;
    }

    @Override
    public T call() {
        try {
            for(Session session : this.getSessions()) {
                this.connect(session);
            }
            return super.call();
        }
        catch(ConnectionCanceledException failure) {
            // Do not report as failed if instanceof ConnectionCanceledException
            log.warn(String.format("Connection canceled %s", failure.getMessage()));
        }
        catch(BackgroundException failure) {
            log.warn(String.format("Failure executing background action: %s", failure));
            for(Session session : this.getSessions()) {
                growl.notify(failure.getMessage(), session.getHost().getHostname());
            }
            exception = failure;
            failed = true;
        }
        return null;
    }

    protected void connect(final Session session) throws BackgroundException {
        if(connection.check(session)) {
            // New connection opened
            growl.notify("Connection opened", session.getHost().getHostname());
        }
    }

    @Override
    public void finish() {
        while(this.retry() > 0) {
            if(log.isInfoEnabled()) {
                log.info(String.format("Retry failed background action %s", this));
            }
            // This is an automated retry. Wait some time first.
            this.pause();
            if(!this.isCanceled()) {
                repeatCount++;
                // Reset the failure status but remember the previous exception for automatic retry.
                failed = false;
                // Re-run the action with the previous lock used
                this.call();
            }
        }
        for(Session session : this.getSessions()) {
            session.removeProgressListener(this);
            // It is important _not_ to do this in #cleanup as otherwise
            // the listeners are still registered when the next BackgroundAction
            // is already running
            session.removeTranscriptListener(this);
        }
        super.finish();
        // If there was any failure, display the summary now
        if(this.hasFailed() && !this.isCanceled()) {
            // Display alert if the action was not canceled intentionally
            alert.alert(this, exception, transcript);
        }
        this.reset();
    }

    @Override
    public void cleanup() {
        this.message(null);
    }

    /**
     * Idle this action for some time. Blocks the caller.
     */
    public void pause() {
        if(0 == Preferences.instance().getInteger("connection.retry.delay")) {
            log.info("No pause between retry");
            return;
        }
        final BackgroundActionPauser pauser = new BackgroundActionPauser(this);
        pauser.await(this);
    }

    @Override
    public String getName() {
        for(Session session : this.getSessions()) {
            return session.getHost().getNickname();
        }
        return super.getName();
    }

    /**
     * @return The session instance
     */
    @Override
    public Object lock() {
        if(this.getSessions().isEmpty()) {
            return super.lock();
        }
        return this.getSessions().iterator().next();
    }
}
