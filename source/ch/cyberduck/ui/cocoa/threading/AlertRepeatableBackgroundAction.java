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
import ch.cyberduck.ui.cocoa.application.*;
import ch.cyberduck.ui.cocoa.foundation.NSAttributedString;
import ch.cyberduck.ui.cocoa.foundation.NSNotification;
import ch.cyberduck.ui.cocoa.foundation.NSObject;
import ch.cyberduck.ui.cocoa.view.CDControllerCell;

import org.apache.log4j.Logger;
import org.rococoa.Foundation;
import org.rococoa.ID;
import org.rococoa.Rococoa;
import org.rococoa.cocoa.CGFloat;
import org.rococoa.cocoa.foundation.NSInteger;

import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public abstract class AlertRepeatableBackgroundAction extends RepeatableBackgroundAction {
    private static Logger log = Logger.getLogger(AlertRepeatableBackgroundAction.class);

    /**
     *
     */
    private WindowController controller;

    public AlertRepeatableBackgroundAction(WindowController controller) {
        this.controller = controller;
        this.exceptions = new Collection<BackgroundException>();
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
     * Display an alert dialog with a summary of all failed tasks
     */
    protected void alert() {
        if(controller.isVisible()) {
            final SheetController alert = new SheetController(controller) {

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

                @Override
                protected void invalidate() {
                    errorView.setDataSource(null);
                    errorView.setDelegate(null);
                    super.invalidate();
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

                @Action
                public void diagnosticsButtonClicked(final NSButton sender) {
                    exceptions.get(exceptions.size() - 1).getSession().getHost().diagnose();
                }

                @Outlet
                private NSButton transcriptButton;

                public void setTranscriptButton(NSButton transcriptButton) {
                    this.transcriptButton = transcriptButton;
                }

                private final TableColumnFactory tableColumnsFactory = new TableColumnFactory();

                @Outlet
                private NSTableView errorView;
                private ListDataSource model;
                private AbstractTableDelegate<ErrorController> delegate;

                private List<ErrorController> errors;

                public void setErrorView(NSTableView errorView) {
                    this.errorView = errorView;
                    this.errorView.setRowHeight(new CGFloat(77));
                    this.errors = new ArrayList<ErrorController>();
                    for(BackgroundException e : exceptions) {
                        errors.add(new ErrorController(e));
                    }
                    this.errorView.setDataSource((model = new ListDataSource() {
                        public NSInteger numberOfRowsInTableView(NSTableView view) {
                            return new NSInteger(errors.size());
                        }

                        public NSObject tableView_objectValueForTableColumn_row(NSTableView view, NSTableColumn tableColumn, NSInteger row) {
                            return null;
                        }
                    }).id());
                    this.errorView.setDelegate((delegate = new AbstractTableDelegate<ErrorController>() {
                        @Override
                        public void tableColumnClicked(NSTableView view, NSTableColumn tableColumn) {
                        }

                        @Override
                        public void tableRowDoubleClicked(final ID sender) {
                        }

                        @Override
                        public boolean selectionShouldChange() {
                            return false;
                        }

                        @Override
                        public void selectionDidChange(NSNotification notification) {
                        }

                        @Override
                        protected boolean isTypeSelectSupported() {
                            return false;
                        }

                        public void enterKeyPressed(final ID sender) {
                        }

                        public void deleteKeyPressed(final ID sender) {
                        }

                        public String tooltip(ErrorController e) {
                            return e.getTooltip();
                        }

                        public void tableView_willDisplayCell_forTableColumn_row(NSTableView view, NSCell cell, NSTableColumn tableColumn, NSInteger row) {
                            Rococoa.cast(cell, CDControllerCell.class).setView(errors.get(row.intValue()).view());
                        }
                    }).id());
                    {
                        NSTableColumn c = tableColumnsFactory.create("Error");
                        c.setMinWidth(50f);
                        c.setWidth(400f);
                        c.setMaxWidth(1000f);
                        c.setDataCell(prototype);
                        this.errorView.addTableColumn(c);
                    }
                }

                private final NSCell prototype = CDControllerCell.controllerCell();

                @Outlet
                private NSTextView transcriptView;

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

    private static class TableColumnFactory extends HashMap<String, NSTableColumn> {
        private NSTableColumn create(String identifier) {
            if(!this.containsKey(identifier)) {
                this.put(identifier, NSTableColumn.tableColumnWithIdentifier(identifier));
            }
            return this.get(identifier);
        }
    }
}