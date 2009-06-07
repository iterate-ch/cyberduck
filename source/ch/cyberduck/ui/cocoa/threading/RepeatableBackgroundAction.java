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

import ch.cyberduck.core.*;
import ch.cyberduck.core.i18n.Locale;
import ch.cyberduck.ui.cocoa.*;
import ch.cyberduck.ui.cocoa.application.NSButton;
import ch.cyberduck.ui.cocoa.application.NSTableColumn;
import ch.cyberduck.ui.cocoa.application.NSTableView;
import ch.cyberduck.ui.cocoa.application.NSTextView;
import ch.cyberduck.ui.cocoa.foundation.NSAttributedString;
import ch.cyberduck.ui.cocoa.foundation.NSNotification;
import ch.cyberduck.ui.cocoa.foundation.NSObject;
import ch.cyberduck.ui.cocoa.growl.Growl;

import org.apache.log4j.Logger;
import org.rococoa.Foundation;
import org.rococoa.cocoa.CGFloat;

import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

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
        CDMainApplication.invoke(new DefaultMainAction() {
            public void run() {
                final String description = null == exception.getPath() ? exception.getSession().getHost().getHostname() : exception.getPath().getName();
                Growl.instance().notify(exception.getMessage(), description);
            }
        });
        exceptions.add(exception);
    }

    /**
     * Contains the transcript of the session while this action was running
     */
    private StringBuffer transcript;

    /**
     * Maximum transcript buffer
     */
    private static final int TRANSCRIPT_MAX_LENGTH =
            Preferences.instance().getInteger("transcript.length");

    /**
     * @param request
     * @param message @see ch.cyberduck.core.TranscriptListener
     */
    public void log(boolean request, String message) {
        if(transcript.length() > TRANSCRIPT_MAX_LENGTH) {
            transcript = new StringBuffer();
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
    }

    @Override
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
        transcript = new StringBuffer();
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

    @Override
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
     *
     */
    private CDSheetController alert;

    /**
     * Display an alert dialog with a summary of all failed tasks
     */
    public void alert() {
        alert = new CDSheetController(controller) {

            @Override
            protected String getBundleName() {
                return "Alert";
            }

            @Outlet
            private NSButton diagnosticsButton;

            public void setDiagnosticsButton(NSButton diagnosticsButton) {
                this.diagnosticsButton = diagnosticsButton;
                this.diagnosticsButton.setTarget(this.id());
                this.diagnosticsButton.setAction(Foundation.selector("diagnosticsButtonClicked:"));
                boolean hidden = true;
                for(BackgroundException e : exceptions) {
                    final Throwable cause = e.getCause();
                    if(cause instanceof SocketException || cause instanceof UnknownHostException) {
                        hidden = false;
                        break;
                    }
                }
                this.diagnosticsButton.setHidden(hidden);
            }

            public void diagnosticsButtonClicked(final NSButton sender) {
                exceptions.get(exceptions.size() - 1).getSession().getHost().diagnose();
            }

            @Outlet
            private NSButton transcriptButton;

            public void setTranscriptButton(NSButton transcriptButton) {
                this.transcriptButton = transcriptButton;
                this.setState(this.transcriptButton,
                        transcript.length() > 0 && Preferences.instance().getBoolean("alert.toggle.transcript"));
            }

            private NSTableView errorView;
            private CDListDataSource model;
            private CDAbstractTableDelegate<CDErrorController> delegate;

            private List<CDErrorController> errors;

            public void setErrorView(NSTableView errorView) {
                this.errorView = errorView;
                this.errors = new ArrayList<CDErrorController>();
                for(BackgroundException e : exceptions) {
                    errors.add(new CDErrorController(e));
                }
                this.errorView.setDataSource((model = new CDListDataSource() {
                    @Override
                    public int numberOfRowsInTableView(NSTableView view) {
                        return errors.size();
                    }

                    @Override
                    public NSObject tableView_objectValueForTableColumn_row(NSTableView view, NSTableColumn tableColumn, int row) {
                        return errors.get(row).view();
                    }
                }).id());
                this.errorView.setDelegate((delegate = new CDAbstractTableDelegate<CDErrorController>() {
                    @Override
                    public void tableColumnClicked(NSTableView view, NSTableColumn tableColumn) {
                    }

                    @Override
                    public void tableRowDoubleClicked(NSObject sender) {
                    }

                    @Override
                    public boolean selectionShouldChange() {
                        return false;
                    }

                    @Override
                    public void selectionDidChange(NSNotification notification) {
                    }

                    @Override
                    public void enterKeyPressed(NSObject sender) {
                    }

                    @Override
                    public void deleteKeyPressed(NSObject sender) {
                    }

                    @Override
                    public String tooltip(CDErrorController e) {
                        return e.getTooltip();
                    }
                }).id());
                {
                    NSTableColumn c = NSTableColumn.tableColumnWithIdentifier("Error");
                    c.setMinWidth(50f);
                    c.setWidth(400f);
                    c.setMaxWidth(1000f);
                    c.setDataCell(CDControllerCell.controllerCell());
                    this.errorView.addTableColumn(c);
                }
                this.errorView.setRowHeight(new CGFloat(77f));
            }

            public NSTextView transcriptView;

            public void setTranscriptView(NSTextView transcriptView) {
                this.transcriptView = transcriptView;
                this.transcriptView.textStorage().setAttributedString(
                        NSAttributedString.attributedStringWithAttributes(transcript.toString(), FIXED_WITH_FONT_ATTRIBUTES));
            }

            public void callback(final int returncode) {
                if(returncode == DEFAULT_OPTION) { //Try Again
                    for(BackgroundException e : exceptions) {
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
                Preferences.instance().setProperty("alert.toggle.transcript", this.transcriptButton.state());
            }
        };
        CDMainApplication.invoke(new WindowMainAction(controller) {
            public void run() {
                alert.beginSheet();
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
                            Locale.localizedString("Retry again in {0} seconds ({1} more attempts)", "Status"),
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
        return Locale.localizedString("Unknown", "");
    }
}