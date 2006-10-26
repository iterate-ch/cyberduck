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

import ch.cyberduck.core.ErrorListener;
import ch.cyberduck.core.TranscriptListener;
import ch.cyberduck.ui.cocoa.CDErrorCell;
import ch.cyberduck.ui.cocoa.CDSheetController;
import ch.cyberduck.ui.cocoa.CDWindowController;

import com.apple.cocoa.application.NSApplication;
import com.apple.cocoa.application.NSButton;
import com.apple.cocoa.application.NSTableColumn;
import com.apple.cocoa.application.NSTableView;
import com.apple.cocoa.application.NSTextView;
import com.apple.cocoa.foundation.NSAttributedString;
import com.apple.cocoa.foundation.NSSelector;

import org.apache.log4j.Logger;

import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @version $Id$
 */
public class BackgroundActionImpl
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
        transcript.append(message + "\n");
    }

    /**
     *
     */
    private CDWindowController controller;

    /**
     *
     */
    private BackgroundAction runnable;

    /**
     * The action will wait in the background until it has access to this lock
     */
    private final Object lock;

    public BackgroundActionImpl(CDWindowController controller, BackgroundAction runnable) {
        this(controller, runnable,  runnable);
    }

    public BackgroundActionImpl(CDWindowController controller, BackgroundAction runnable,
                                Object lock) {
        this.controller = controller;
        this.runnable = runnable;
        this.lock = lock;
    }

    public void prepare() {
        exceptions = new ArrayList();
        transcript = new StringBuffer();
    }

    public void run() {
        new Thread() {
            public void run() {
                // Synchronize all background threads to this lock so actions run
                // sequentially as they were initiated from the main interface thread
                synchronized(lock) {
                    log.debug("Acquired lock for background runnable:"+runnable);

                    try {
                        prepare();
                        runnable.run();
                    }
                    catch(NullPointerException e) {
                        // We might get a null pointer if the session has been interrupted
                        // during the action in progress and closing the underlying socket
                        // asynchronously. See Session#interrupt
                        log.info("Due to closing the underlying socket asynchronously, the " +
                                "action was interrupted while still pending completion");
                    }
                    finally {
                        controller.invoke(new Runnable() {
                            public void run() {
                                cleanup();
                            }
                        });
                    }
                    log.debug("Releasing lock for background runnable:"+runnable);
                }
            }
        }.start();
        log.debug("Started background runnable for:"+runnable);
    }

    /**
     *
     */
    public void cleanup() {
        this.runnable.cleanup();
        if(exceptions.size() > 0) {
            CDSheetController c = new CDSheetController(controller) {

                public void awakeFromNib() {
                    super.awakeFromNib();
                }

                private NSButton diagnosticsButton;

                public void setDiagnosticsButton(NSButton diagnosticsButton) {
                    this.diagnosticsButton = diagnosticsButton;
                    this.diagnosticsButton.setTarget(this);
                    this.diagnosticsButton.setAction(new NSSelector("diagnosticsButtonClicked", new Class[]{NSButton.class}));
                    boolean hidden = true;
                    for(Iterator iter = exceptions.iterator(); iter.hasNext(); ) {
                        Throwable cause = ((BackgroundException)iter.next()).getCause();
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
                        run();
                    }
                }

                /**
                 * NSTableView.DataSource
                 */
                public int numberOfRowsInTableView(NSTableView view) {
                    return exceptions.size();
                }

                /**
                 * NSTableView.DataSource
                 */
                public Object tableViewObjectValueForLocation(NSTableView view, NSTableColumn tableColumn, int row) {
                    return exceptions.get(row);
                }

                /**
                 * NSTableView.Delegate
                 */
                boolean selectionShouldChangeInTableView(NSTableView view) {
                    return false;
                }
            };
            if(!NSApplication.loadNibNamed("Alert", c)) {
                log.fatal("Couldn't load Alert.nib");
            }
            c.beginSheet(true);
        }
    }
}
