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

import ch.cyberduck.core.*;
import ch.cyberduck.core.i18n.Locale;
import ch.cyberduck.ui.growl.Growl;

import org.apache.log4j.Logger;

import com.enterprisedt.net.ftp.FTPNullReplyException;

import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.text.MessageFormat;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

/**
 * @version $Id$
 */
public abstract class RepeatableBackgroundAction extends AbstractBackgroundAction implements ErrorListener, TranscriptListener {
    private static Logger log = Logger.getLogger(RepeatableBackgroundAction.class);
    private static final String lineSeparator = System.getProperty ("line.separator");
    
    /**
     * Contains all exceptions thrown while this action was running
     */
    protected List<BackgroundException> exceptions
            = new Collection<BackgroundException>();

    /**
     * This action encountered one or more exceptions
     */
    private boolean failed;

    /**
     * Contains the transcript of the session while this action was running
     */
    protected StringBuilder transcript;

    /**
     *
     */
    private final int repeatAttempts
            = Preferences.instance().getInteger("connection.retry");

    /**
     * The number of times this action has been run
     */
    protected int repeatCount;

    /**
     * Maximum transcript buffer
     */
    private static final int TRANSCRIPT_MAX_LENGTH =
            Preferences.instance().getInteger("transcript.length");

    /**
     * @param exception
     * @see ch.cyberduck.core.ErrorListener
     */
    public void error(final BackgroundException exception) {
        // Do not report an error when the action was canceled intentionally
        Throwable cause = exception.getCause();
        if(cause instanceof ConnectionCanceledException) {
            log.warn(cause.getMessage());
            // Do not report as failed if instanceof ConnectionCanceledException
            return;
        }
        final String description
                = (null == exception.getPath()) ? exception.getSession().getHost().getHostname() : exception.getPath().getName();
        if(exceptions.size() < Preferences.instance().getInteger("growl.limit")) {
            Growl.instance().notify(exception.getMessage(), description);
        }
        exceptions.add(exception);
        failed = true;
    }

    /**
     * Apppend to the transcript. Reset if maximum length has been reached.
     *
     * @param request
     * @param message @see ch.cyberduck.core.TranscriptListener
     * @see #TRANSCRIPT_MAX_LENGTH
     */
    public void log(boolean request, String message) {
        if(transcript.length() > TRANSCRIPT_MAX_LENGTH) {
            transcript = new StringBuilder();
        }
        transcript.append(message).append(lineSeparator);
    }

    @Override
    public boolean prepare() {
        final Session session = this.getSession();
        if(session != null) {
            session.addErrorListener(this);
            session.addTranscriptListener(this);
        }
        // Clear the transcript and exceptions
        transcript = new StringBuilder();
        return super.prepare();
    }

    /**
     * To be overriden in concrete subclass
     *
     * @return The session if any
     */
    protected abstract Session getSession();

    /**
     * The number of times a new connection attempt should be made. Takes into
     * account the number of times already tried.
     *
     * @return Greater than zero if a failed action should be repeated again
     */
    public int retry() {
        if(!this.isCanceled()) {
            for(BackgroundException e : exceptions) {
                final Throwable cause = e.getCause();
                // Check for an exception we consider possibly temporary
                if(cause instanceof SocketException
                        || cause instanceof SocketTimeoutException
                        || cause instanceof UnknownHostException
                        || cause instanceof FTPNullReplyException) {
                    // The initial connection attempt does not count
                    return repeatAttempts - repeatCount;
                }
            }
        }
        return 0;
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
    public void finish() {
        while(this.hasFailed() && this.retry() > 0) {
            log.info("Retry failed background action:" + this);
            // This is a automated retry. Wait some time first.
            this.pause();
            if(!this.isCanceled()) {
                repeatCount++;
                // Reset the failure status
                failed = false;
                // Re-run the action with the previous lock used
                this.run();
            }
        }

        final Session session = this.getSession();
        if(session != null) {
            // It is important _not_ to do this in #cleanup as otherwise
            // the listeners are still registered when the next BackgroundAction
            // is already running
            session.removeTranscriptListener(this);
            session.removeErrorListener(this);
        }

        super.finish();
    }

    /**
     * Idle this action for some time. Blocks the caller.
     */
    public void pause() {
        if(0 == Preferences.instance().getInteger("connection.retry.delay")) {
            log.info("No pause between retry");
            return;
        }
        final Timer wakeup = new Timer();
        final CyclicBarrier wait = new CyclicBarrier(2);
        wakeup.scheduleAtFixedRate(new TimerTask() {
            /**
             * The delay to wait before execution of the action in seconds
             */
            private int delay = (int) Preferences.instance().getDouble("connection.retry.delay");

            private final String pattern = Locale.localizedString("Retry again in {0} seconds ({1} more attempts)", "Status");

            @Override
            public void run() {
                if(0 == delay || RepeatableBackgroundAction.this.isCanceled()) {
                    // Cancel the timer repetition
                    this.cancel();
                    return;
                }
                final Session session = getSession();
                if(session != null) {
                    session.message(MessageFormat.format(pattern, delay--, RepeatableBackgroundAction.this.retry()));
                }
            }

            @Override
            public boolean cancel() {
                try {
                    // Notifiy to return to caller from #pause()
                    wait.await();
                }
                catch(InterruptedException e) {
                    log.error(e.getMessage());
                }
                catch(BrokenBarrierException e) {
                    log.error(e.getMessage());
                }
                return super.cancel();
            }
        }, 0, 1000); // Schedule for immediate execusion with an interval of 1s
        try {
            // Wait for notify from wakeup timer
            wait.await();
        }
        catch(InterruptedException e) {
            log.error(e.getMessage());
        }
        catch(BrokenBarrierException e) {
            log.error(e.getMessage());
        }
    }

    /**
     * @return The session instance
     */
    @Override
    public Object lock() {
        return this.getSession();
    }

    @Override
    public String toString() {
        final Session session = this.getSession();
        if(session != null) {
            return session.getHost().getHostname();
        }
        return Locale.localizedString("Unknown");
    }
}