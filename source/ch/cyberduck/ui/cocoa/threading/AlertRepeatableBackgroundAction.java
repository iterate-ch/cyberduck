package ch.cyberduck.ui.cocoa.threading;

/*
 *  Copyright (c) 2008 David Kocher. All rights reserved.
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

import ch.cyberduck.core.Collection;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.threading.BackgroundException;
import ch.cyberduck.core.threading.RepeatableBackgroundAction;
import ch.cyberduck.ui.cocoa.*;
import ch.cyberduck.ui.cocoa.application.NSButton;
import ch.cyberduck.ui.cocoa.application.NSTableColumn;
import ch.cyberduck.ui.cocoa.application.NSTableView;
import ch.cyberduck.ui.cocoa.application.NSTextView;
import ch.cyberduck.ui.cocoa.foundation.NSAttributedString;
import ch.cyberduck.ui.cocoa.foundation.NSNotification;
import ch.cyberduck.ui.cocoa.foundation.NSObject;

import org.apache.log4j.Logger;
import org.rococoa.Foundation;
import org.rococoa.cocoa.CGFloat;

import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public abstract class AlertRepeatableBackgroundAction extends RepeatableBackgroundAction {
    private static Logger log = Logger.getLogger(AlertRepeatableBackgroundAction.class);

    /**
     *
     */
    private CDWindowController controller;

    public AlertRepeatableBackgroundAction(CDWindowController controller) {
        this.controller = controller;
        this.exceptions = new Collection<BackgroundException>();
    }

    @Override
    public Object lock() {
        return controller;
    }

    @Override
    public void finish() {
        super.finish();
        // If there was any failure, display the summary now
        if(this.hasFailed() && !this.isCanceled()) {
            // Display alert if the action was not canceled intentionally
            this.alert();
        }
    }

    /**
     *
     */
    private CDSheetController alert;

    /**
     * Display an alert dialog with a summary of all failed tasks
     */
    public void alert() {
        if(controller.isVisible()) {
            alert = new CDSheetController(controller) {

                @Override
                protected String getBundleName() {
                    return "Alert";
                }

                @Override
                public void awakeFromNib() {
                    this.setState(this.transcriptButton,
                            transcript.length() > 0 && Preferences.instance().getBoolean("alert.toggle.transcript"));
                    super.awakeFromNib();
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
                        public int numberOfRowsInTableView(NSTableView view) {
                            return errors.size();
                        }

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

                        public void enterKeyPressed(NSObject sender) {
                        }

                        public void deleteKeyPressed(NSObject sender) {
                        }

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
                        controller.background(AlertRepeatableBackgroundAction.this);
                    }
                    Preferences.instance().setProperty("alert.toggle.transcript", this.transcriptButton.state());
                }
            };
            alert.beginSheet();
        }
    }
}