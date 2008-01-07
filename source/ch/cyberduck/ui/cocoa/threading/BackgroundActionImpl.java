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

import com.enterprisedt.net.ftp.FTPNullReplyException;

import ch.cyberduck.core.*;
import ch.cyberduck.ui.cocoa.*;
import ch.cyberduck.ui.cocoa.growl.Growl;

import com.apple.cocoa.application.*;
import com.apple.cocoa.foundation.NSAttributedString;
import com.apple.cocoa.foundation.NSBundle;
import com.apple.cocoa.foundation.NSSelector;

import org.apache.log4j.Logger;

import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @version $Id: BackgroundActionImpl.java 2524 2006-10-26 13:14:03Z dkocher $
 */
public abstract class BackgroundActionImpl extends CDController
        implements BackgroundAction, ErrorListener, TranscriptListener {


    private static Logger log = Logger.getLogger(BackgroundActionImpl.class);

    /**
     * Contains all exceptions thrown while
     * this action was running
     */
    private List exceptions;

    /**
     * @see ch.cyberduck.core.ErrorListener
     * @param exception
     */
    public void error(final BackgroundException exception) {
        // Do not report an error when the action was canceled intentionally
        Throwable cause = exception.getCause();
        if(cause instanceof ConnectionCanceledException) {
            // Do not report as failed if instanceof ConnectionCanceledException
            return;
        }
        if(cause instanceof SocketException) {
            if(cause.getMessage().equals("Software caused connection abort")) {
                // Do not report as failed if socket opening interrupted
                log.warn("Supressed socket exception:"+cause.getMessage());
                return;
            }
            if(cause.getMessage().equals("Socket closed")) {
                // Do not report as failed if socket opening interrupted
                log.warn("Supressed socket exception:"+cause.getMessage());
                return;
            }
        }
        CDMainApplication.invoke(new DefaultMainAction() {
            public void run() {
                Growl.instance().notify(exception.getMessage(),
                        null == exception.getPath() ? exception.getSession().getHost().getHostname()
                                : exception.getPath().getName());
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
     * @see ch.cyberduck.core.TranscriptListener
     * @param message
     */
    public void log(String message) {
        transcript.append(message).append("\n");
    }
    
    public void clearTranscript() {
        if(transcript.length() > 0) {
            transcript.delete(0, transcript.length()-1);
        }
    }

    /**
     *
     */
    private CDWindowController controller;

    public BackgroundActionImpl(CDWindowController controller) {
        this.controller = controller;
        this.exceptions = new Collection();
        this.transcript = new StringBuffer();
    }

    /**
     * Clear the transcript and exceptions
     */
    public void init() {
        this.clearTranscript();
        exceptions.clear();
    }

    /**
     * To be overriden in concrete subclass
     * @return The session if any
     */
    public Session session() {
        return null;
    }

    /**
     * The number of times this action has been run
     */
    protected int count = 0;

    /**
     * The number of times a new connection attempt should be made. Takes into
     * account the number of times already tried.
     * @return Greater than zero if a failed action should be repeated again
     */
    public int retry() {
        if(!this.isCanceled()) {
            for(Iterator iter = exceptions.iterator(); iter.hasNext(); ) {
                Throwable cause = ((Throwable)iter.next()).getCause();
                // Check for an exception we consider possibly temporary
                if(cause instanceof SocketException
                        || cause instanceof SocketTimeoutException
                        || cause instanceof UnknownHostException
                        || cause instanceof FTPNullReplyException)
                {
                    // The initial connection attempt does not count
                    return (int)Preferences.instance().getDouble("connection.retry") - (count -1);
                }
            }
        }
        return 0;
    }

    private boolean canceled;

    public void cancel() {
        canceled = true;
    }

    /**
     * To be overriden by a concrete subclass. Returns false by default for actions
     * not connected to a graphical user interface
     * @return True if the user canceled this action
     */
    public boolean isCanceled() {
        return canceled;
    }

    /**
     * Called just before #run. After this method is called, a delay
     * of #delay is added before continuing.
     * @see #run()
     */
    public void prepare() {
        ;
    }

    public abstract void run();

    /**
     * Called after #run but still on the working thread
     * @see #run
     */
    public void finish() {
        count++;
    }

    public abstract void cleanup();

    /**
     * @return True if an error was reported the last time this action was run
     */
    public boolean hasFailed()  {
        return this.exceptions.size() > 0;
    }

    /**
     * Display an alert dialog with a summary of all failed tasks
     * @param lock The lock to use when the user chooses to re-run the action
     */
    public void alert(final Object lock) {
        CDSheetController c = new CDSheetController(controller) {
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
                for(Iterator iter = exceptions.iterator(); iter.hasNext(); ) {
                    Throwable cause = ((Throwable)iter.next()).getCause();
                    if(cause instanceof SocketException || cause instanceof UnknownHostException) {
                        hidden = false;
                        break;
                    }
                }
                this.diagnosticsButton.setHidden(hidden);
            }

            public void diagnosticsButtonClicked(final NSButton sender) {
                ((BackgroundException)exceptions.get(exceptions.size()-1)).getSession().getHost().diagnose();
            }

            private NSButton transcriptButton;

            public void setTranscriptButton(NSButton transcriptButton) {
                this.transcriptButton = transcriptButton;
            }

            private NSTableView errorView;

            public void setErrorView(NSTableView errorView) {
                this.errorView = errorView;
                this.errorView.setDataSource(this);
                this.errorView.setDelegate(this);
                {
                    NSTableColumn c = new NSTableColumn();
                    c.setMinWidth(50f);
                    c.setWidth(400f);
                    c.setMaxWidth(1000f);
                    c.setDataCell(new CDErrorCell());
                    this.errorView.addTableColumn(c);
                }
            }

            public NSTextView transcriptView;

            public void setTranscriptView(NSTextView transcriptView) {
                this.transcriptView = transcriptView;
                this.transcriptView.textStorage().setAttributedString(
                        new NSAttributedString(transcript.toString(), FIXED_WITH_FONT_ATTRIBUTES));
            }

            public void callback(final int returncode) {
                if(returncode == DEFAULT_OPTION) { //Try Again
                    for(Iterator iter = exceptions.iterator(); iter.hasNext(); ) {
                        BackgroundException e = (BackgroundException)iter.next();
                        Path workdir = e.getPath();
                        if(null == workdir) {
                            continue;
                        }
                        workdir.invalidate();
                    }
                    // Revert any exceptions and transcript
                    init();
                    // Re-run the action with the previous lock used
                    controller.background(BackgroundActionImpl.this, lock);
                }
            }

            /**
             * @see NSTableView.DataSource
             */
            public int numberOfRowsInTableView(NSTableView view) {
                return exceptions.size();
            }

            /**
             * @see NSTableView.DataSource
             */
            public Object tableViewObjectValueForLocation(NSTableView view, NSTableColumn tableColumn, int row) {
                return exceptions.get(row);
            }

            /**
             * @see NSTableView.Delegate
             */
            boolean selectionShouldChangeInTableView(NSTableView view) {
                return false;
            }
        };
        c.beginSheet();
    }

    /**
     * Idle this action for some time. Blocks the caller.
     * @see Preferences#connection.retry.delay
     * @param lock
     */
    public void pause(final Object lock) {
        Timer wakeup = new Timer();
        wakeup.scheduleAtFixedRate(new TimerTask() {
            /**
             * The delay to wait before execution of the action in seconds
             */
            private int delay = (int)Preferences.instance().getDouble("connection.retry.delay");

            public void run() {
                if(0 == delay || 0 == retry()) { // Cancel if the delay is set to zero or if too many attempts
                    this.cancel();
                    return;
                }
                session().message(NSBundle.localizedString("Retry in", "Status", "")
                        +" "+(delay)+" "+NSBundle.localizedString("seconds", "Status", "")
                        +" ("+NSBundle.localizedString("Will try", "Status", "")+" "+retry()
                        +" "+NSBundle.localizedString("more times", "Status", "")+")");
                delay--;
            }

            public boolean cancel() {
                synchronized(lock) {
                    lock.notify();
                }
                return super.cancel();
            }
        }, 0, 1000); // Schedule for immediate execusion with an interval of 1s
        try {
            synchronized(lock) {
                // Wait for notify from wakeup timer
                lock.wait();
            }
        }
        catch(InterruptedException e) {
            log.error(e.getMessage());
        }
    }
}
