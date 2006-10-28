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
public abstract class BackgroundActionImpl
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

    public BackgroundActionImpl(CDWindowController controller) {
        this.controller = controller;
        this.exceptions = new ArrayList();
        this.transcript = new StringBuffer();
    }

    public abstract void run();

    public abstract void cleanup();

    /**
     *
     * @return
     */
    public boolean hasFailed()  {
        return this.exceptions.size() > 0;
    }

    /**
     *
     * @param lock
     */
    public void alert(final Object lock) {
        CDSheetController c = new CDSheetController(controller) {
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
                exceptions.clear();
                transcript.delete(0, transcript.length()-1);
                if(returncode == DEFAULT_OPTION) { //Try Again
                    // Re-run the action with the previous lock used
                    controller.background(BackgroundActionImpl.this, lock);
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
        synchronized(NSApplication.sharedApplication()) {
            if(!NSApplication.loadNibNamed("Alert", c)) {
                log.fatal("Couldn't load Alert.nib");
            }
        }
        c.beginSheet(false);
    }
}
