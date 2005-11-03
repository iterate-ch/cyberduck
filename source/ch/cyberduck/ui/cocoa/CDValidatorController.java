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

import com.apple.cocoa.application.*;
import com.apple.cocoa.foundation.*;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import ch.cyberduck.core.*;

/**
 * @version $Id$
 */
public abstract class CDValidatorController extends CDWindowController implements Validator {
	private static Logger log = Logger.getLogger(CDValidatorController.class);

	private static NSMutableArray instances = new NSMutableArray();

	private static NSMutableParagraphStyle lineBreakByTruncatingMiddleParagraph = new NSMutableParagraphStyle();
	
	static {
		lineBreakByTruncatingMiddleParagraph.setLineBreakMode(NSParagraphStyle.LineBreakByTruncatingMiddle);
	}
	
	protected static final NSDictionary TRUNCATE_MIDDLE_PARAGRAPH_DICTIONARY = new NSDictionary(new Object[]{lineBreakByTruncatingMiddleParagraph},
			new Object[]{NSAttributedString.ParagraphStyleAttributeName});
	
	protected CDWindowController windowController;

	public CDValidatorController(CDWindowController windowController) {
		this.windowController = windowController;
		this.load();
		instances.addObject(this);
	}

    protected abstract void load();

	public void awakeFromNib() {
        super.awakeFromNib();

		this.window().setReleasedWhenClosed(true);
		(NSNotificationCenter.defaultCenter()).addObserver(this,
		    new NSSelector("tableViewSelectionDidChange", new Class[]{NSNotification.class}),
		    NSTableView.TableViewSelectionDidChangeNotification,
		    this.fileTableView);
	}

    protected List validatedList;
    protected List workList;
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
		for(Iterator iter = files.iterator(); iter.hasNext() && !this.isCanceled();) {
			Path child = (Path)iter.next();
			log.debug("Validating:"+child);
			if(this.validate(child, resumeRequested)) {
				log.info("Adding "+child+" to final set.");
				this.validatedList.add(child);
			}
		}
		if(this.hasPrompt() && !this.isCanceled()) {
			this.statusIndicator.stopAnimation(null);
			this.setEnabled(true);
            this.fireDataChanged();
			this.windowController.waitForSheetEnd();
		}
		return !this.isCanceled();
	}

    protected boolean validate(Path p, boolean resumeRequested) {
        if(p.attributes.isFile()) {
            p.reset();
            if(Preferences.instance().getBoolean("queue.transformer.useTransformer")) {
                p.setPath(p.getParent().getAbsolute(), NameTransformer.instance().transform(p.getName()));
            }
            return this.validateFile(p, resumeRequested);
        }
        if(p.attributes.isDirectory()) {
            return this.validateDirectory(p);
        }
        throw new IllegalArgumentException(p.getName()+" is neither file nor directory");
    }


    protected abstract boolean validateDirectory(Path path);

    protected boolean validateFile(Path path, boolean resumeRequested) {
        if(resumeRequested) { // resume existing files independant of settings in preferences
            path.status.setResume(this.isExisting(path));
            return true;
        }
        // When overwriting file anyway we don't have to check if the file already exists
        if(Preferences.instance().getProperty("queue.fileExists").equals("overwrite")) {
            log.info("Apply validation rule to overwrite file "+path.getName());
            path.status.setResume(false);
            return true;
        }
        if(this.isExisting(path)) {
            if(Preferences.instance().getProperty("queue.fileExists").equals("resume")) {
                log.debug("Apply validation rule to resume:"+path.getName());
                path.status.setResume(true);
                return true;
            }
            if(Preferences.instance().getProperty("queue.fileExists").equals("similar")) {
                log.debug("Apply validation rule to apply similar name:"+path.getName());
                path.status.setResume(false);
                this.adjustFilename(path);
                log.info("Changed local name to "+path.getName());
                return true;
            }
            if(Preferences.instance().getProperty("queue.fileExists").equals("ask")) {
                log.debug("Apply validation rule to ask:"+path.getName());
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
		if(!this.hasPrompt()) {
            this.windowController.beginSheet(this.window());
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

    public void setFileTableView(NSTableView fileTableView) {
		this.fileTableView = fileTableView;
		this.fileTableView.setDataSource(this);
		this.fileTableView.setDelegate(this.fileTableViewDelegate = new CDAbstractTableDelegate() {

            public String tableViewToolTipForCell(NSTableView tableView, NSCell cell, NSMutableRect rect,
                                                  NSTableColumn tc, int row, NSPoint mouseLocation) {
                if(row < numberOfRowsInTableView(tableView)) {
                    return workList.get(row).toString();
                }
                return null;
            }

            public void enterKeyPressed(Object sender) {

            }

            public void deleteKeyPressed(Object sender) {

            }

            public void tableColumnClicked(NSTableView view, NSTableColumn tableColumn) {
                //
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
        if(Preferences.instance().getBoolean("browser.horizontalLines") && Preferences.instance().getBoolean("browser.verticalLines")) {
            this.fileTableView.setGridStyleMask(NSTableView.SolidHorizontalGridLineMask | NSTableView.SolidVerticalGridLineMask);
        }
        else if(Preferences.instance().getBoolean("browser.verticalLines")) {
            this.fileTableView.setGridStyleMask(NSTableView.SolidVerticalGridLineMask);
        }
        else if(Preferences.instance().getBoolean("browser.horizontalLines")) {
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
            if(setResizableMaskSelector.implementedByClass(NSTableColumn.class)) {
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
            if(setResizableMaskSelector.implementedByClass(NSTableColumn.class)) {
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
            if(setResizableMaskSelector.implementedByClass(NSTableColumn.class)) {
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
            if(setResizableMaskSelector.implementedByClass(NSTableColumn.class)) {
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
            if(setResizableMaskSelector.implementedByClass(NSTableColumn.class)) {
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
		this.skipButton.setEnabled(false);
		this.skipButton.setTarget(this);
		this.skipButton.setAction(new NSSelector("skipActionFired", new Class[]{Object.class}));
	}

	protected NSButton resumeButton; // IBOutlet

	public void setResumeButton(NSButton resumeButton) {
		this.resumeButton = resumeButton;
		this.resumeButton.setEnabled(false);
		this.resumeButton.setTarget(this);
		this.resumeButton.setAction(new NSSelector("resumeActionFired", new Class[]{Object.class}));
	}

	protected NSButton overwriteButton; // IBOutlet

	public void setOverwriteButton(NSButton overwriteButton) {
		this.overwriteButton = overwriteButton;
		this.overwriteButton.setEnabled(false);
		this.overwriteButton.setTarget(this);
		this.overwriteButton.setAction(new NSSelector("overwriteActionFired", new Class[]{Object.class}));
	}

	protected NSButton cancelButton; // IBOutlet

	public void setCancelButton(NSButton cancelButton) {
		this.cancelButton = cancelButton;
		this.cancelButton.setTarget(this);
		this.cancelButton.setAction(new NSSelector("cancelActionFired", new Class[]{Object.class}));
	}

	protected void setEnabled(boolean enabled) {
		this.overwriteButton.setEnabled(enabled);
		this.resumeButton.setEnabled(enabled);
		this.skipButton.setEnabled(enabled);
	}

	public void windowWillClose(NSNotification notification) {
		instances.removeObject(this);
	}

	public void resumeActionFired(NSButton sender) {
		for(Iterator i = this.workList.iterator(); i.hasNext();) {
            Path p = (Path)i.next();
            if(!p.isSkipped()) {
                p.status.setResume(true);
                this.validatedList.add(p);
            }
		}
		this.setCanceled(false);
		this.windowController.endSheet(this.window(), sender.tag());
	}

	public void overwriteActionFired(NSButton sender) {
        for(Iterator i = this.workList.iterator(); i.hasNext();) {
            Path p = (Path)i.next();
            if(!p.isSkipped()) {
                p.status.setResume(false);
                this.validatedList.add(p);
            }
        }
		this.setCanceled(false);
        this.windowController.endSheet(this.window(), sender.tag());
	}

	public void skipActionFired(NSButton sender) {
		this.workList.clear();
		this.setCanceled(false);
        this.windowController.endSheet(this.window(), sender.tag());
	}

	public void cancelActionFired(NSButton sender) {
		this.validatedList.clear();
		this.workList.clear();
		this.setCanceled(true);
        this.windowController.endSheet(this.window(), sender.tag());
	}

	// ----------------------------------------------------------
	// NSTableView.DataSource
	// ----------------------------------------------------------
	
	protected void fireDataChanged() {
		if(this.hasPrompt()) {
            this.fileTableView.reloadData();
            this.infoLabel.setStringValue(this.workList.size()+" "+NSBundle.localizedString("files", ""));
		}
	}
	
	public void tableViewSelectionDidChange(NSNotification notification) {
		if(this.fileTableView.selectedRow() != -1) {
			Path p = (Path)this.workList.get(this.fileTableView.selectedRow());
			if(p != null) {
				if(p.getLocal().exists()) {
					this.localField.setAttributedStringValue(new NSAttributedString(p.getLocal().getAbsolute(),
					    TRUNCATE_MIDDLE_PARAGRAPH_DICTIONARY));
				}
				else {
					this.localField.setStringValue("-");
				}
				if(p.getRemote().exists()) {
					this.urlField.setAttributedStringValue(new NSAttributedString(p.getRemote().getHost().getURL()+p.getRemote().getAbsolute(),
					    TRUNCATE_MIDDLE_PARAGRAPH_DICTIONARY));
				}
				else {
					this.urlField.setStringValue("-");
				}
			}
		}
		else {
			this.urlField.setStringValue("-");
			this.localField.setStringValue("-");
		}
	}

    public void tableViewSetObjectValueForLocation(NSTableView tableView, Object object, NSTableColumn tableColumn, int row) {
        if(row < this.numberOfRowsInTableView(tableView)) {
            String identifier = (String)tableColumn.identifier();
            if(identifier.equals("INCLUDE")) {
                Path p = (Path)this.workList.get(row);
                p.setSkipped(((Integer)object).intValue() == NSCell.OffState);
            }
        }
    }

	private static final NSImage FOLDER_ICON = NSImage.imageNamed("folder16.tiff");

	public Object tableViewObjectValueForLocation(NSTableView tableView, NSTableColumn tableColumn, int row) {
		if(row < this.numberOfRowsInTableView(tableView)) {
			String identifier = (String)tableColumn.identifier();
			Path p = (Path)this.workList.get(row);
			if(p != null) {
                if(identifier.equals("INCLUDE")) {
                    if(p.isSkipped())
                        return new Integer(NSCell.OffState);
                    return new Integer(NSCell.OnState);
                }
				if(identifier.equals("ICON")) {
					if(p.attributes.isDirectory()) {
						return FOLDER_ICON;
					}
					if(p.attributes.isFile()) {
						NSImage icon = CDIconCache.instance().get(p.getExtension());
						icon.setSize(new NSSize(16f, 16f));
						return icon;
					}
					return NSImage.imageNamed("notfound.tiff");
				}
				if(identifier.equals("FILENAME")) {
					return new NSAttributedString(p.getRemote().getName(),
					    CDTableCell.TABLE_CELL_PARAGRAPH_DICTIONARY);
				}
				if(identifier.equals("TYPEAHEAD")) {
					return p.getRemote().getName();
				}
				if(identifier.equals("REMOTE")) {
					if(p.getRemote().exists()) {
						if(p.attributes.isFile()) {
							return new NSAttributedString(Status.getSizeAsString(p.attributes.getSize())+", "
                                    +p.attributes.getTimestampAsShortString(),
							    CDTableCell.TABLE_CELL_PARAGRAPH_DICTIONARY);
						}
						if(p.attributes.isDirectory()) {
							return new NSAttributedString(p.attributes.getTimestampAsShortString(),
							    CDTableCell.TABLE_CELL_PARAGRAPH_DICTIONARY);
						}
					}
					return null;
				}
				if(identifier.equals("LOCAL")) {
					if(p.getLocal().exists()) {
						if(p.attributes.isFile()) {
							return new NSAttributedString(Status.getSizeAsString(p.getLocal().getSize())+", "
                                    +p.getLocal().getTimestampAsShortString(),
							    CDTableCell.TABLE_CELL_PARAGRAPH_DICTIONARY);
						}
						if(p.attributes.isDirectory()) {
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

	public int numberOfRowsInTableView(NSTableView tableView) {
		return this.workList.size();
	}
}