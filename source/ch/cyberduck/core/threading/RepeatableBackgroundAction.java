package ch.cyberduck.core.threading;

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

import ch.cyberduck.core.ConnectionCheckService;
import ch.cyberduck.core.HostKeyController;
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
public abstract class RepeatableBackgroundAction extends AbstractBackgroundAction<Boolean>
        implements TranscriptListener {
    private static final Logger log = Logger.getLogger(RepeatableBackgroundAction.class);

    private static final String lineSeparator
            = System.getProperty("line.separator");

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

    /**
     * Maximum transcript buffer
     */
    private static final int TRANSCRIPT_MAX_LENGTH =
            Preferences.instance().getInteger("transcript.length");

    private LoginController prompt;

    private HostKeyController key;

    private AlertCallback alert;

    protected ProgressListener progressListener;

    private TranscriptListener transcriptListener;

    private Growl growl = GrowlFactory.get();

    public RepeatableBackgroundAction(final AlertCallback alert,
                                      final ProgressListener progressListener,
                                      final TranscriptListener transcriptListener,
                                      final LoginController prompt,
                                      final HostKeyController key) {
        this.alert = alert;
        this.progressListener = progressListener;
        this.transcriptListener = transcriptListener;
        this.prompt = prompt;
        this.key = key;
    }

    public BackgroundException getException() {
        return exception;
    }

    /**
     * Apppend to the transcript. Reset if maximum length has been reached.
     *
     * @param request Message was sent to the server
     * @param message @see ch.cyberduck.core.TranscriptListener
     * @see #TRANSCRIPT_MAX_LENGTH
     */
    @Override
    public void log(final boolean request, final String message) {
        if(transcript.length() > TRANSCRIPT_MAX_LENGTH) {
            transcript = new StringBuilder();
        }
        transcript.append(message).append(lineSeparator);
        transcriptListener.log(request, message);
    }

    @Override
    public void init() {
        this.reset();
    }

    @Override
    public void prepare() throws ConnectionCanceledException {
        super.prepare();
        progressListener.message(this.getActivity());
        try {
            for(Session s : this.getSessions()) {
                s.addProgressListener(progressListener);
                s.addTranscriptListener(this);
            }
            // Clear the transcript and exceptions
            transcript = new StringBuilder();
            final ConnectionCheckService c = new ConnectionCheckService(prompt, key, PasswordStoreFactory.get(),
                    progressListener);
            for(Session session : this.getSessions()) {
                c.check(session);
                growl.notify("Connection opened", session.getHost().getHostname());
            }
        }
        catch(BackgroundException failure) {
            log.warn(String.format("Failure starting background action: %s", failure));
            this.error(failure);
            throw new ConnectionCanceledException(failure);
        }
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
            if(exception.isNetworkFailure()) {
                // The initial connection attempt does not count
                return Preferences.instance().getInteger("connection.retry") - repeatCount;
            }
        }
        return 0;
    }

    protected void reset() {
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
    public Boolean call() {
        try {
            return super.call();
        }
        catch(BackgroundException failure) {
            log.warn(String.format("Failure executing background action: %s", failure));
            this.error(failure);
        }
        return false;
    }

    public void error(final BackgroundException failure) {
        // Do not report an error when the action was canceled intentionally
        if(failure instanceof ConnectionCanceledException) {
            // Do not report as failed if instanceof ConnectionCanceledException
            log.warn(String.format("Connection canceled %s", failure.getMessage()));
        }
        else {
            for(Session session : this.getSessions()) {
                growl.notify(failure.getMessage(), session.getHost().getHostname());
            }
            exception = failure;
            failed = true;
        }
    }

    @Override
    public void finish() throws BackgroundException {
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
            session.removeProgressListener(progressListener);
            // It is important _not_ to do this in #cleanup as otherwise
            // the listeners are still registered when the next BackgroundAction
            // is already running
            session.removeTranscriptListener(this);
        }
        try {
            super.finish();
            // If there was any failure, display the summary now
            if(this.hasFailed() && !this.isCanceled()) {
                // Display alert if the action was not canceled intentionally
                alert.alert(this, exception, transcript);
            }
            this.reset();
        }
        catch(BackgroundException failure) {
            log.warn(String.format("Failure finishing background action: %s", failure));
            this.error(failure);
        }
    }

    @Override
    public void cleanup() {
        progressListener.message(null);
    }

    @Override
    public void cancel() {
        if(this.isRunning()) {
            for(Session s : this.getSessions()) {
                try {
                    s.interrupt();
                }
                catch(BackgroundException e) {
                    this.error(e);
                }
            }
        }
        super.cancel();
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
        pauser.await(progressListener);
    }

    /**
     * @return The session instance
     */
    @Override
    public Object lock() {
        return this.getSessions().iterator().next();
    }
}
