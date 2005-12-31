package ch.cyberduck.ui.cocoa;

/*
 *  Copyright (c) 2005 David Kocher. All rights reserved.
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
import ch.cyberduck.core.Status;
import ch.cyberduck.core.Validator;

import com.apple.cocoa.application.*;
import com.apple.cocoa.foundation.*;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @version $Id$
 */
public abstract class CDValidatorController
        extends CDSheetController implements Validator {
    private static Logger log = Logger.getLogger(CDValidatorController.class);

    private static NSMutableParagraphStyle lineBreakByTruncatingMiddleParagraph = new NSMutableParagraphStyle();

    static {
        lineBreakByTruncatingMiddleParagraph.setLineBreakMode(NSParagraphStyle.LineBreakByTruncatingMiddle);
    }

    protected static final NSDictionary TRUNCATE_MIDDLE_PARAGRAPH_DICTIONARY = new NSDictionary(new Object[]{lineBreakByTruncatingMiddleParagraph},
            new Object[]{NSAttributedString.ParagraphStyleAttributeName});


    public CDValidatorController(CDWindowController parent) {
        super(parent);
        this.load();
    }

    public void callback(int returncode) {
        if (returncode == DEFAULT_OPTION) { //overwrite
            for (Iterator i = workList.iterator(); i.hasNext();) {
                Path p = (Path) i.next();
                if (!p.isSkipped()) {
                    p.status.setResume(false);
                    this.validatedList.add(p);
                }
            }
            this.setCanceled(false);
        }
        if (returncode == ALTERNATE_OPTION) { //resume
            for (Iterator i = workList.iterator(); i.hasNext();) {
                Path p = (Path) i.next();
                if (!p.isSkipped()) {
                    p.status.setResume(true);
                    this.validatedList.add(p);
                }
            }
            this.setCanceled(false);
        }
        if (returncode == SKIP_OPTION) { //skip
            this.workList.clear();
            this.setCanceled(false);
        }
        if (returncode == CANCEL_OPTION) {
            this.validatedList.clear();
            this.workList.clear();
            this.setCanceled(true);
        }
    }

    protected abstract void load();

    /**
     *
     */
    protected List validatedList;
    /**
     *
     */
    protected List workList;
    /**
     *
     */
    protected List promptList;

    /**
     * The user canceled this request, no further validation should be taken
     */
    private boolean canceled = false;

    public boolean isCanceled() {
        return this.canceled;
    }

    protected void setCanceled(boolean c) {
        this.canceled = c;
    }

    protected abstract boolean isExisting(Path p);

    {
        this.validatedList = new ArrayList();
        this.workList = new ArrayList();
        this.promptList = new ArrayList();
    }

    public boolean validate(List files, boolean resumeRequested) {
        for (Iterator iter = files.iterator(); iter.hasNext() && !this.isCanceled();) {
            Path child = (Path) iter.next();
            log.debug("Validating:" + child);
            if (this.validate(child, resumeRequested)) {
                log.info("Adding " + child + " to final set.");
                this.validatedList.add(child);
            }
        }
        if (this.hasPrompt() && !this.isCanceled()) {
            this.statusIndicator.stopAnimation(null);
            this.setEnabled(true);
            this.fireDataChanged();
            this.waitForSheetEnd();
        }
        return !this.isCanceled();
    }

    protected boolean validate(Path p, boolean resumeRequested) {
        if (p.attributes.isFile()) {
            p.reset();
            return this.validateFile(p, resumeRequested);
        }
        if (p.attributes.isDirectory()) {
            return this.validateDirectory(p);
        }
        throw new IllegalArgumentException(p.getName() + " is neither file nor directory");
    }


    protected abstract boolean validateDirectory(Path path);

    protected boolean validateFile(Path path, boolean resumeRequested) {
        if (resumeRequested) { // resume existing files independant of settings in preferences
            path.status.setResume(this.isExisting(path));
            return true;
        }
        // When overwriting file anyway we don't have to check if the file already exists
        if (Preferences.instance().getProperty("queue.fileExists").equals("overwrite")) {
            log.info("Apply validation rule to overwrite file " + path.getName());
            path.status.setResume(false);
            return true;
        }
        if (this.isExisting(path)) {
            if (Preferences.instance().getProperty("queue.fileExists").equals("resume")) {
                log.debug("Apply validation rule to resume:" + path.getName());
                path.status.setResume(true);
                return true;
            }
            if (Preferences.instance().getProperty("queue.fileExists").equals("similar")) {
                log.debug("Apply validation rule to apply similar name:" + path.getName());
                path.status.setResume(false);
                this.adjustFilename(path);
                log.info("Changed local name to " + path.getName());
                return true;
            }
            if (Preferences.instance().getProperty("queue.fileExists").equals("ask")) {
                log.debug("Apply validation rule to ask:" + path.getName());
                this.prompt(path);
                return false;
            }
            throw new IllegalArgumentException("No rules set to validate transfers");
        }
        else {
            path.status.setResume(false);
            return true;
        }
    }

    public List getValidated() {
        return this.validatedList;
    }

    protected void adjustFilename(Path path) {
        //        
    }

    protected boolean hasPrompt = false;

    protected boolean hasPrompt() {
        return this.hasPrompt;
    }

    protected void prompt(Path p) {
        if (!this.hasPrompt()) {
            this.beginSheet(false);
            this.statusIndicator.startAnimation(null);
            this.hasPrompt = true;
        }
        this.promptList.add(p);
        this.workList.add(p);
        this.fireDataChanged();
    }

    // ----------------------------------------------------------
    // Outlets
    // ----------------------------------------------------------

    protected NSTextField infoLabel; // IBOutlet

    public void setInfoLabel(NSTextField infoLabel) {
        this.infoLabel = infoLabel;
    }

    private NSTextField urlField; // IBOutlet

    public void setUrlField(NSTextField urlField) {
        this.urlField = urlField;
    }

    private NSTextField localField; // IBOutlet

    public void setLocalField(NSTextField localField) {
        this.localField = localField;
    }

    private NSProgressIndicator statusIndicator; // IBOutlet

    public void setStatusIndicator(NSProgressIndicator statusIndicator) {
        this.statusIndicator = statusIndicator;
        this.statusIndicator.setUsesThreadedAnimation(true);
    }

    protected NSTableView fileTableView; // IBOutlet
    private CDTableDelegate fileTableViewDelegate;

    public void setFileTableView(NSTableView view) {
        this.fileTableView = view;
        this.fileTableView.setDataSource(this);
        this.fileTableView.setDelegate(this.fileTableViewDelegate = new CDAbstractTableDelegate() {

            public String tableViewToolTipForCell(NSTableView tableView, NSCell cell, NSMutableRect rect,
                                                  NSTableColumn tc, int row, NSPoint mouseLocation) {
                if (row < numberOfRowsInTableView(tableView)) {
                    return workList.get(row).toString();
                }
                return null;
            }

            public void enterKeyPressed(Object sender) {
                ;
            }

            public void deleteKeyPressed(Object sender) {
                ;
            }

            public void tableColumnClicked(NSTableView view, NSTableColumn tableColumn) {
                ;
            }

            public void tableRowDoubleClicked(Object sender) {
                ;
            }

            public void selectionDidChange(NSNotification notification) {
                if (fileTableView.selectedRow() != -1) {
                    Path p = (Path) workList.get(fileTableView.selectedRow());
                    if (p != null) {
                        if (p.getLocal().exists()) {
                            localField.setAttributedStringValue(new NSAttributedString(p.getLocal().getAbsolute(),
                                    TRUNCATE_MIDDLE_PARAGRAPH_DICTIONARY));
                        }
                        else {
                            localField.setStringValue("-");
                        }
                        if (p.getRemote().exists()) {
                            urlField.setAttributedStringValue(new NSAttributedString(p.getRemote().getHost().getURL() + p.getRemote().getAbsolute(),
                                    TRUNCATE_MIDDLE_PARAGRAPH_DICTIONARY));
                        }
                        else {
                            urlField.setStringValue("-");
                        }
                    }
                }
                else {
                    urlField.setStringValue("-");
                    localField.setStringValue("-");
                }
            }
        });
        this.fileTableView.setRowHeight(17f);
        // selection properties
        this.fileTableView.setAllowsMultipleSelection(true);
        this.fileTableView.setAllowsEmptySelection(true);
        this.fileTableView.setAllowsColumnResizing(true);
        this.fileTableView.setAllowsColumnSelection(false);
        this.fileTableView.setAllowsColumnReordering(true);
        this.fileTableView.setUsesAlternatingRowBackgroundColors(Preferences.instance().getBoolean("browser.alternatingRows"));
        if (Preferences.instance().getBoolean("browser.horizontalLines") && Preferences.instance().getBoolean("browser.verticalLines"))
        {
            this.fileTableView.setGridStyleMask(NSTableView.SolidHorizontalGridLineMask | NSTableView.SolidVerticalGridLineMask);
        }
        else if (Preferences.instance().getBoolean("browser.verticalLines")) {
            this.fileTableView.setGridStyleMask(NSTableView.SolidVerticalGridLineMask);
        }
        else if (Preferences.instance().getBoolean("browser.horizontalLines")) {
            this.fileTableView.setGridStyleMask(NSTableView.SolidHorizontalGridLineMask);
        }
        else {
            this.fileTableView.setGridStyleMask(NSTableView.GridNone);
        }
        NSSelector setResizableMaskSelector
                = new NSSelector("setResizingMask", new Class[]{int.class});
        {
            NSTableColumn c = new NSTableColumn();
            c.setIdentifier("INCLUDE");
            c.headerCell().setStringValue("");
            c.setMinWidth(20f);
            c.setWidth(20f);
            c.setMaxWidth(20f);
            if (setResizableMaskSelector.implementedByClass(NSTableColumn.class)) {
                c.setResizingMask(NSTableColumn.AutoresizingMask);
            }
            else {
                c.setResizable(true);
            }
            c.setEditable(false);
            NSButtonCell cell = new NSButtonCell();
            cell.setControlSize(NSCell.SmallControlSize);
            cell.setButtonType(NSButtonCell.SwitchButton);
            cell.setAllowsMixedState(false);
            cell.setTarget(this);
            c.setDataCell(cell);
            c.dataCell().setAlignment(NSText.CenterTextAlignment);
            this.fileTableView.addTableColumn(c);
        }
        {
            NSTableColumn c = new NSTableColumn();
            c.setIdentifier("ICON");
            c.headerCell().setStringValue("");
            c.setMinWidth(20f);
            c.setWidth(20f);
            c.setMaxWidth(20f);
            if (setResizableMaskSelector.implementedByClass(NSTableColumn.class)) {
                c.setResizingMask(NSTableColumn.AutoresizingMask);
            }
            else {
                c.setResizable(true);
            }
            c.setEditable(false);
            c.setDataCell(new NSImageCell());
            c.dataCell().setAlignment(NSText.CenterTextAlignment);
            this.fileTableView.addTableColumn(c);
        }
        {
            NSTableColumn c = new NSTableColumn();
            c.headerCell().setStringValue(NSBundle.localizedString("Filename", "A column in the browser"));
            c.setIdentifier("FILENAME");
            c.setMinWidth(100f);
            c.setWidth(220f);
            c.setMaxWidth(500f);
            if (setResizableMaskSelector.implementedByClass(NSTableColumn.class)) {
                c.setResizingMask(NSTableColumn.AutoresizingMask);
            }
            else {
                c.setResizable(true);
            }
            c.setEditable(false);
            c.setDataCell(new NSTextFieldCell());
            c.dataCell().setAlignment(NSText.LeftTextAlignment);
            this.fileTableView.addTableColumn(c);
        }
        {
            NSTableColumn c = new NSTableColumn();
            c.headerCell().setStringValue(NSBundle.localizedString("Server File", ""));
            c.setIdentifier("REMOTE");
            c.setMinWidth(100f);
            c.setWidth(200f);
            c.setMaxWidth(600f);
            if (setResizableMaskSelector.implementedByClass(NSTableColumn.class)) {
                c.setResizingMask(NSTableColumn.AutoresizingMask);
            }
            else {
                c.setResizable(true);
            }
            c.setDataCell(new NSTextFieldCell());
            c.dataCell().setAlignment(NSText.LeftTextAlignment);
            this.fileTableView.addTableColumn(c);
        }
        {
            NSTableColumn c = new NSTableColumn();
            c.headerCell().setStringValue(NSBundle.localizedString("Local File", ""));
            c.setIdentifier("LOCAL");
            c.setMinWidth(100f);
            c.setWidth(200f);
            c.setMaxWidth(600f);
            if (setResizableMaskSelector.implementedByClass(NSTableColumn.class)) {
                c.setResizingMask(NSTableColumn.AutoresizingMask);
            }
            else {
                c.setResizable(true);
            }
            c.setDataCell(new NSTextFieldCell());
            c.dataCell().setAlignment(NSText.LeftTextAlignment);
            this.fileTableView.addTableColumn(c);
        }
        this.fileTableView.sizeToFit();
    }

    protected NSButton skipButton; // IBOutlet

    public void setSkipButton(NSButton skipButton) {
        this.skipButton = skipButton;
    }

    protected NSButton resumeButton; // IBOutlet

    public void setResumeButton(NSButton resumeButton) {
        this.resumeButton = resumeButton;
    }

    protected NSButton overwriteButton; // IBOutlet

    public void setOverwriteButton(NSButton overwriteButton) {
        this.overwriteButton = overwriteButton;
    }

    protected void setEnabled(boolean enabled) {
        this.overwriteButton.setEnabled(enabled);
        this.resumeButton.setEnabled(enabled);
        this.skipButton.setEnabled(enabled);
    }

    protected void fireDataChanged() {
        if (this.hasPrompt()) {
            this.fileTableView.reloadData();
            this.infoLabel.setStringValue(this.workList.size() + " " + NSBundle.localizedString("files", ""));
        }
    }

    // ----------------------------------------------------------
    // NSTableView.DataSource
    // ----------------------------------------------------------

    public void tableViewSetObjectValueForLocation(NSTableView view, Object object, NSTableColumn tableColumn, int row) {
        if (row < this.numberOfRowsInTableView(view)) {
            String identifier = (String) tableColumn.identifier();
            if (identifier.equals("INCLUDE")) {
                Path p = (Path) this.workList.get(row);
                p.setSkipped(((Integer) object).intValue() == NSCell.OffState);
            }
        }
    }

    private static final NSImage FOLDER_ICON = NSImage.imageNamed("folder16.tiff");

    public Object tableViewObjectValueForLocation(NSTableView view, NSTableColumn tableColumn, int row) {
        if (row < this.numberOfRowsInTableView(view)) {
            String identifier = (String) tableColumn.identifier();
            Path p = (Path) this.workList.get(row);
            if (p != null) {
                if (identifier.equals("INCLUDE")) {
                    if (p.isSkipped())
                        return new Integer(NSCell.OffState);
                    return new Integer(NSCell.OnState);
                }
                if (identifier.equals("ICON")) {
                    if (p.attributes.isDirectory()) {
                        return FOLDER_ICON;
                    }
                    if (p.attributes.isFile()) {
                        NSImage icon = CDIconCache.instance().get(p.getExtension());
                        icon.setSize(new NSSize(16f, 16f));
                        return icon;
                    }
                    return NSImage.imageNamed("notfound.tiff");
                }
                if (identifier.equals("FILENAME")) {
                    return new NSAttributedString(p.getRemote().getName(),
                            CDTableCell.TABLE_CELL_PARAGRAPH_DICTIONARY);
                }
                if (identifier.equals("TYPEAHEAD")) {
                    return p.getRemote().getName();
                }
                if (identifier.equals("REMOTE")) {
                    if (p.getRemote().exists()) {
                        if (p.attributes.isFile()) {
                            return new NSAttributedString(Status.getSizeAsString(p.attributes.getSize()) + ", "
                                    + p.attributes.getTimestampAsShortString(),
                                    CDTableCell.TABLE_CELL_PARAGRAPH_DICTIONARY);
                        }
                        if (p.attributes.isDirectory()) {
                            return new NSAttributedString(p.attributes.getTimestampAsShortString(),
                                    CDTableCell.TABLE_CELL_PARAGRAPH_DICTIONARY);
                        }
                    }
                    return null;
                }
                if (identifier.equals("LOCAL")) {
                    if (p.getLocal().exists()) {
                        if (p.attributes.isFile()) {
                            return new NSAttributedString(Status.getSizeAsString(p.getLocal().getSize()) + ", "
                                    + p.getLocal().getTimestampAsShortString(),
                                    CDTableCell.TABLE_CELL_PARAGRAPH_DICTIONARY);
                        }
                        if (p.attributes.isDirectory()) {
                            return new NSAttributedString(p.getLocal().getTimestampAsShortString(),
                                    CDTableCell.TABLE_CELL_PARAGRAPH_DICTIONARY);
                        }
                    }
                    return null;
                }
            }
        }
        return null;
    }

    public int numberOfRowsInTableView(NSTableView view) {
        return this.workList.size();
    }
}