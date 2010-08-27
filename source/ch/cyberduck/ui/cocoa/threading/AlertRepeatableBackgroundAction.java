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

import ch.cyberduck.core.Path;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.i18n.Locale;
import ch.cyberduck.core.threading.BackgroundException;
import ch.cyberduck.core.threading.RepeatableBackgroundAction;
import ch.cyberduck.ui.cocoa.*;
import ch.cyberduck.ui.cocoa.application.*;
import ch.cyberduck.ui.cocoa.foundation.NSAttributedString;
import ch.cyberduck.ui.cocoa.foundation.NSNotification;
import ch.cyberduck.ui.cocoa.foundation.NSObject;
import ch.cyberduck.ui.cocoa.view.ControllerCell;

import org.rococoa.Foundation;
import org.rococoa.ID;
import org.rococoa.Rococoa;
import org.rococoa.cocoa.CGFloat;
import org.rococoa.cocoa.foundation.NSInteger;

import org.apache.log4j.Logger;

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
    }

    @Override
    public void finish() {
        super.finish();
        // If there was any failure, display the summary now
        if(this.hasFailed() && !this.isCanceled()) {
            // Display alert if the action was not canceled intentionally
            this.alert();
        }
        this.reset();
    }

    private void callback(final int returncode) {
        if(returncode == SheetCallback.DEFAULT_OPTION) { //Try Again
            for(BackgroundException e : getExceptions()) {
                Path workdir = e.getPath();
                if(null == workdir) {
                    continue;
                }
                workdir.invalidate();
            }
            AlertRepeatableBackgroundAction.this.reset();
            // Re-run the action with the previous lock used
            controller.background(AlertRepeatableBackgroundAction.this);
        }
    }

    /**
     * Display an alert dialog with a summary of all failed tasks
     */
    protected void alert() {
        if(controller.isVisible()) {
            if(this.getExceptions().size() == 1) {
                final BackgroundException failure = this.getExceptions().get(0);
                String detail = failure.getDetailedCauseMessage();
                String title = failure.getReadableTitle() + ": " + failure.getMessage();
                NSAlert alert = NSAlert.alert(title, //title
                        Locale.localizedString(detail),
                        Locale.localizedString("Try Again", "Alert"), // default button
                        AlertRepeatableBackgroundAction.this.isNetworkFailure() ? Locale.localizedString("Network Diagnostics") : null, //other button
                        Locale.localizedString("Cancel") // alternate button
                );
                alert.setShowsHelp(null != failure.getPath());
                final AlertController c = new AlertController(AlertRepeatableBackgroundAction.this.controller, alert) {
                    public void callback(final int returncode) {
                        if(returncode == SheetCallback.ALTERNATE_OPTION) {
                            AlertRepeatableBackgroundAction.this.diagnose();
                        }
                        else {
                            AlertRepeatableBackgroundAction.this.callback(returncode);
                        }
                    }

                    @Override
                    protected void help() {
                        StringBuilder site = new StringBuilder(Preferences.instance().getProperty("website.help"));
                        site.append("/").append(failure.getPath().getSession().getHost().getProtocol().getIdentifier());
                        openUrl(site.toString());
                    }
                };
                if(this.hasTranscript()) {
                    // Display custom multiple file alert
                    //c.setAccessoryView(alert.getTranscriptView());
                }
                c.beginSheet();
            }
            else {
                final SheetController c = new AlertSheetController();
                c.beginSheet();
            }
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

    /**
     *
     */
    private class AlertSheetController extends SheetController {

        public AlertSheetController() {
            super(AlertRepeatableBackgroundAction.this.controller);
        }

        @Override
        protected String getBundleName() {
            return "Alert";
        }

        @Override
        public void awakeFromNib() {
            final boolean log = AlertRepeatableBackgroundAction.this.hasTranscript();
            this.setState(transcriptButton, log && Preferences.instance().getBoolean("alert.toggle.transcript"));
            transcriptButton.setEnabled(log);
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
            this.diagnosticsButton.setHidden(!AlertRepeatableBackgroundAction.this.isNetworkFailure());
        }

        @Action
        public void diagnosticsButtonClicked(final NSButton sender) {
            AlertRepeatableBackgroundAction.this.diagnose();
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
            for(BackgroundException e : getExceptions()) {
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
                    Rococoa.cast(cell, ControllerCell.class).setView(errors.get(row.intValue()).view());
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

        private final NSCell prototype = ControllerCell.controllerCell();

        @Outlet
        private NSTextView transcriptView;

        public void setTranscriptView(NSTextView transcriptView) {
            this.transcriptView = transcriptView;
            this.transcriptView.textStorage().setAttributedString(
                    NSAttributedString.attributedStringWithAttributes(AlertRepeatableBackgroundAction.this.getTranscript(), FIXED_WITH_FONT_ATTRIBUTES));
        }

        public void callback(final int returncode) {
            Preferences.instance().setProperty("alert.toggle.transcript", this.transcriptButton.state());
            AlertRepeatableBackgroundAction.this.callback(returncode);
        }
    }
}