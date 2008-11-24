package ch.cyberduck.ui.cocoa.threading;

/*
 *  Copyright (c) 2006 David Kocher. All rights reserved.
 *  http://cyberduck.ch/
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  Bug fixes, suggestions and comments should be sent to:
 *  dkocher@cyberduck.ch
 */

import com.apple.cocoa.application.*;
import com.apple.cocoa.foundation.NSAttributedString;
import com.apple.cocoa.foundation.NSBundle;
import com.apple.cocoa.foundation.NSSelector;

import ch.cyberduck.core.Collection;
import ch.cyberduck.core.*;
import ch.cyberduck.ui.cocoa.*;
import ch.cyberduck.ui.cocoa.growl.Growl;

import org.apache.log4j.Logger;

import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.text.MessageFormat;
import java.util.*;

import com.enterprisedt.net.ftp.FTPNullReplyException;

/**
 * @version $Id: BackgroundActionImpl.java 2524 2006-10-26 13:14:03Z dkocher $
 */
public abstract class RepeatableBackgroundAction extends AbstractBackgroundAction
        implements ErrorListener, TranscriptListener {

    private static Logger log = Logger.getLogger(RepeatableBackgroundAction.class);

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
        if(cause instanceof SocketException) {
            if(cause.getMessage().equals("Software caused connection abort")) {
                // Do not report as failed if socket opening interrupted
                log.warn("Supressed socket exception:" + cause.getMessage());
                return;
            }
            if(cause.getMessage().equals("Socket closed")) {
                // Do not report as failed if socket opening interrupted
                log.warn("Supressed socket exception:" + cause.getMessage());
                return;
            }
        }
        CDMainApplication.invoke(new DefaultMainAction() {
            public void run() {
                final String description = null == exception.getPath() ? exception.getSession().getHost().getHostname() : exception.getPath().getName();
                Growl.instance().notify(exception.getMessage(), description);
            }
        });
        exceptions.add(exception);
    }

    /**
     * Contains the transcript of the session
     * while this action was running
     */
    private StringBuffer transcript;

    /**
     * @param request
     * @param message @see ch.cyberduck.core.TranscriptListener
     */
    public void log(boolean request, String message) {
        if(message.length() > transcript.capacity()) {
            final int newline = transcript.indexOf("\n");
            if(newline > 0) {
                // Delete up to end - The ending index, exclusive.
                transcript.delete(0, newline + 1);
            }
        }
        transcript.append(message).append("\n");
    }

    /**
     *
     */
    private CDWindowController controller;

    public RepeatableBackgroundAction(CDWindowController controller) {
        this.controller = controller;
        this.exceptions = new Collection<BackgroundException>();
        this.transcript = new StringBuffer(100);
    }

    public Object lock() {
        return controller;
    }

    public boolean prepare() {
        final Session session = this.getSession();
        if(session != null) {
            session.addErrorListener(this);
            session.addTranscriptListener(this);
        }
        // Clear the transcript and exceptions
        if(transcript.length() > 0) {
            transcript.delete(0, transcript.length() - 1);
        }
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
            for(BackgroundException e: exceptions) {
                final Throwable cause = e.getCause();
                // Check for an exception we consider possibly temporary
                if(cause instanceof SocketException
                        || cause instanceof SocketTimeoutException
                        || cause instanceof UnknownHostException
                        || cause instanceof FTPNullReplyException) {
                    // The initial connection attempt does not count
                    return (int) Preferences.instance().getDouble("connection.retry") - repeatCount;
                }
            }
        }
        return 0;
    }

    /**
     * Contains all exceptions thrown while
     * this action was running
     */
    protected List<BackgroundException> exceptions;

    private boolean hasFailed() {
        return this.exceptions.size() > 0;
    }

    /**
     * The number of times this action has been run
     */
    protected int repeatCount;

    public void finish() {
        while(this.hasFailed() && this.retry() > 0) {
            log.info("Retry failed background action:" + this);
            // This is a automated retry. Wait some time first.
            this.pause();
            if(!this.isCanceled()) {
                repeatCount++;
                exceptions.clear();
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

        // If there was any failure, display the summary now
        if(this.hasFailed() && !this.isCanceled()) {
            // Display alert if the action was not canceled intentionally
            this.alert();
        }
    }

    public abstract void cleanup();

    /**
     * Display an alert dialog with a summary of all failed tasks
     */
    public void alert() {
        final CDSheetController c = new CDSheetController(controller) {

            protected String getBundleName() {
                return "Alert";
            }

            public void awakeFromNib() {
                boolean enabled = transcript.length() > 0;
                if(!enabled) {
                    if(this.transcriptButton.state() == NSCell.OnState) {
                        log.debug("Closing transcript view");
                        this.transcriptButton.performClick(null);
                    }
                }
                this.transcriptButton.setEnabled(enabled);
                super.awakeFromNib();
            }

            private NSButton diagnosticsButton;

            public void setDiagnosticsButton(NSButton diagnosticsButton) {
                this.diagnosticsButton = diagnosticsButton;
                this.diagnosticsButton.setTarget(this);
                this.diagnosticsButton.setAction(new NSSelector("diagnosticsButtonClicked", new Class[]{NSButton.class}));
                boolean hidden = true;
                for(BackgroundException e: exceptions) {
                    final Throwable cause = e.getCause();
                    if(cause instanceof SocketException || cause instanceof UnknownHostException) {
                        hidden = false;
                        break;
                    }
                }
                this.diagnosticsButton.setHidden(hidden);
            }

            public void diagnosticsButtonClicked(final NSButton sender) {
                ((BackgroundException) exceptions.get(exceptions.size() - 1)).getSession().getHost().diagnose();
            }

            private NSButton transcriptButton;

            public void setTranscriptButton(NSButton transcriptButton) {
                this.transcriptButton = transcriptButton;
            }

            private NSTableView errorView;

            private List<CDErrorController> errors;

            public void setErrorView(NSTableView errorView) {
                this.errorView = errorView;
                this.errors = new ArrayList<CDErrorController>();
                for(BackgroundException e: exceptions) {
                    errors.add(new CDErrorController(e));
                }
                this.errorView.setDataSource(this);
                this.errorView.setDelegate(this);
                {
                    NSTableColumn c = new NSTableColumn();
                    c.setMinWidth(50f);
                    c.setWidth(400f);
                    c.setMaxWidth(1000f);
                    c.setDataCell(new CDControllerCell());
                    this.errorView.addTableColumn(c);
                }
                this.errorView.setRowHeight(77f);
            }

            public NSTextView transcriptView;

            public void setTranscriptView(NSTextView transcriptView) {
                this.transcriptView = transcriptView;
                this.transcriptView.textStorage().setAttributedString(
                        new NSAttributedString(transcript.toString(), FIXED_WITH_FONT_ATTRIBUTES));
            }

            public void callback(final int returncode) {
                if(returncode == DEFAULT_OPTION) { //Try Again
                    for(BackgroundException e: exceptions) {
                        Path workdir = e.getPath();
                        if(null == workdir) {
                            continue;
                        }
                        workdir.invalidate();
                    }
                    exceptions.clear();
                    // Re-run the action with the previous lock used
                    controller.background(RepeatableBackgroundAction.this);
                }
            }

            /**
             * @see NSTableView.DataSource
             */
            public int numberOfRowsInTableView(NSTableView view) {
                return errors.size();
            }

            /**
             * @see NSTableView.DataSource
             */
            public Object tableViewObjectValueForLocation(NSTableView view, NSTableColumn tableColumn, int row) {
                return errors.get(row);
            }

            /**
             * @see NSTableView.Delegate
             */
            boolean selectionShouldChangeInTableView(NSTableView view) {
                return false;
            }
        };
        CDMainApplication.invoke(new WindowMainAction(controller) {
            public void run() {
                c.beginSheet();
            }
        });
    }

    /**
     * Idle this action for some time. Blocks the caller.
     */
    public void pause() {
        Timer wakeup = new Timer();
        wakeup.scheduleAtFixedRate(new TimerTask() {
            /**
             * The delay to wait before execution of the action in seconds
             */
            private int delay = (int) Preferences.instance().getDouble("connection.retry.delay");

            public void run() {
                if(0 == delay || RepeatableBackgroundAction.this.isCanceled()) {
                    // Cancel the timer repetition
                    this.cancel();
                    return;
                }
                final Session session = getSession();
                if(session != null) {
                    session.message(MessageFormat.format(
                            NSBundle.localizedString("Retry again in {0} seconds ({1} more attempts)", "Status", ""),
                            String.valueOf(delay), String.valueOf(retry())));
                }
                delay--;
            }

            public boolean cancel() {
                final Object lock = lock();
                if(lock != null) {
                    synchronized(lock) {
                        // Notifiy to return to caller from #pause()
                        lock.notify();
                    }
                }
                return super.cancel();
            }
        }, 0, 1000); // Schedule for immediate execusion with an interval of 1s
        try {
            final Object lock = lock();
            if(lock != null) {
                synchronized(lock) {
                    // Wait for notify from wakeup timer
                    lock.wait();
                }
            }
        }
        catch(InterruptedException e) {
            log.error(e.getMessage());
        }
    }

    public String toString() {
        final Session session = this.getSession();
        if(session != null) {
            return session.getHost().getHostname();
        }
        return NSBundle.localizedString("Unknown", "");
    }
}