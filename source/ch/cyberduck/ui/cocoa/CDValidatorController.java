package ch.cyberduck.ui.cocoa;

/*
 *  Copyright (c) 2004 David Kocher. All rights reserved.
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

import java.util.Iterator;
import java.util.Observable;
import java.util.List;

import com.apple.cocoa.application.*;
import com.apple.cocoa.foundation.*;

import org.apache.log4j.Logger;

import ch.cyberduck.core.*;

/**
 * @version $Id$
 */
public abstract class CDValidatorController extends AbstractValidator {
	protected static Logger log = Logger.getLogger(CDValidatorController.class);

	private static NSMutableArray instances = new NSMutableArray();

	public CDValidatorController() {
		instances.addObject(this);
	}

	public void awakeFromNib() {
		(NSNotificationCenter.defaultCenter()).addObserver(this,
		    new NSSelector("tableViewSelectionDidChange", new Class[]{NSNotification.class}),
		    NSTableView.TableViewSelectionDidChangeNotification,
		    this.fileTableView);
	}

	public synchronized void update(Observable observable, Object arg) {
		if(arg instanceof Message) {
			Message msg = (Message)arg;
			if(msg.getTitle().equals(Message.ERROR)) {
				if(CDQueueController.instance().hasSheet()) {
					NSApplication.sharedApplication().endSheet(this.window());
				}
			}
		}
	}
	
	public List validate(Queue q) {
		q.addObserver(this);
		synchronized(CDQueueController.instance()) {
			for(Iterator iter = q.getChilds().iterator(); iter.hasNext() && !this.isCanceled(); ) {
				Path child = (Path)iter.next();
				if(this.validate(child, q.isResume())) {
					this.validated.add(child);
				}
				if(this.visible) {
					this.fireDataChanged();
				}
			}
			if(this.visible && !this.isCanceled()) {
				this.statusIndicator.stopAnimation(null);
				this.setEnabled(true);
				while(CDQueueController.instance().hasSheet()) {
					try {
						log.debug("Sleeping...");
						CDQueueController.instance().wait();
					}
					catch(InterruptedException e) {
						log.error(e.getMessage());
					}
				}
			}
		}
		q.deleteObserver(this);
		return this.getResult();
	}

	protected abstract List getResult();

	protected abstract void load();
	
	protected boolean visible = false;
	
	protected void prompt(Path p) {
		log.debug("prompt:"+p);
		if(!this.visible) {
			this.load();
			CDQueueController.instance().beginSheet(this.window());
			this.statusIndicator.startAnimation(null);
			this.visible = true;
		}
		this.workset.add(p);
	}
	
	protected void adjustFilename(Path path) {
		//
	}
	
	// ----------------------------------------------------------
	// Outlets
	// ----------------------------------------------------------
	
	private NSTextField infoLabel; // IBOutlet

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
	}

	protected NSTableView fileTableView; // IBOutlet

	public void setFileTableView(NSTableView fileTableView) {
		this.fileTableView = fileTableView;
		this.fileTableView.setDataSource(this);
		this.fileTableView.sizeToFit();
		this.fileTableView.setRowHeight(17f);
		this.fileTableView.setAutoresizesAllColumnsToFit(true);
		NSSelector setUsesAlternatingRowBackgroundColorsSelector =
		    new NSSelector("setUsesAlternatingRowBackgroundColors", new Class[]{boolean.class});
		if(setUsesAlternatingRowBackgroundColorsSelector.implementedByClass(NSTableView.class)) {
			this.fileTableView.setUsesAlternatingRowBackgroundColors(Preferences.instance().getProperty("browser.alternatingRows").equals("true"));
		}
		NSSelector setGridStyleMaskSelector =
		    new NSSelector("setGridStyleMask", new Class[]{int.class});
		if(setGridStyleMaskSelector.implementedByClass(NSTableView.class)) {
			if(Preferences.instance().getProperty("browser.horizontalLines").equals("true") && Preferences.instance().getProperty("browser.verticalLines").equals("true")) {
				this.fileTableView.setGridStyleMask(NSTableView.SolidHorizontalGridLineMask | NSTableView.SolidVerticalGridLineMask);
			}
			else if(Preferences.instance().getProperty("browser.verticalLines").equals("true")) {
				this.fileTableView.setGridStyleMask(NSTableView.SolidVerticalGridLineMask);
			}
			else if(Preferences.instance().getProperty("browser.horizontalLines").equals("true")) {
				this.fileTableView.setGridStyleMask(NSTableView.SolidHorizontalGridLineMask);
			}
			else {
				this.fileTableView.setGridStyleMask(NSTableView.GridNone);
			}
		}
		{
			NSTableColumn c = new NSTableColumn();
			c.setIdentifier("ICON");
			c.headerCell().setStringValue("");
			c.setMinWidth(20f);
			c.setWidth(20f);
			c.setMaxWidth(20f);
			c.setResizable(true);
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
			c.setWidth(250f);
			c.setMaxWidth(500f);
			c.setResizable(true);
			c.setEditable(false);
			c.setDataCell(new NSTextFieldCell());
			c.dataCell().setAlignment(NSText.LeftTextAlignment);
			this.fileTableView.addTableColumn(c);
		}
		
		// selection properties
		this.fileTableView.setAllowsMultipleSelection(true);
		this.fileTableView.setAllowsEmptySelection(true);
		this.fileTableView.setAllowsColumnReordering(true);
		
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
		this.cancelButton.setEnabled(false);
		this.cancelButton.setTarget(this);
		this.cancelButton.setAction(new NSSelector("cancelActionFired", new Class[]{Object.class}));
	}
	
	private NSPanel window; // IBOutlet

	public void setWindow(NSPanel window) {
		this.window = window;
		this.window.setDelegate(this);
	}

	public NSPanel window() {
		return this.window;
	}

	protected void setEnabled(boolean enabled) {
		this.cancelButton.setEnabled(enabled);
		this.overwriteButton.setEnabled(enabled);
		this.resumeButton.setEnabled(enabled);
		this.skipButton.setEnabled(enabled);
	}

	protected void reloadTable() {
		this.fileTableView.reloadData();
		this.infoLabel.setStringValue(this.workset.size()+" "+NSBundle.localizedString("files", ""));
	}

	public void windowWillClose(NSNotification notification) {
		instances.removeObject(this);
	}

	protected void fireDataChanged() {
		this.reloadTable();
	}

	public void resumeActionFired(NSButton sender) {
		for(Iterator i = this.workset.iterator(); i.hasNext();) {
			((Path)i.next()).status.setResume(true);
		}
		this.setCanceled(false);
		NSApplication.sharedApplication().endSheet(this.window(), sender.tag());
	}

	public void overwriteActionFired(NSButton sender) {
		for(Iterator i = this.workset.iterator(); i.hasNext();) {
			((Path)i.next()).status.setResume(false);
		}
		this.setCanceled(false);
		NSApplication.sharedApplication().endSheet(this.window(), sender.tag());
	}

	public void skipActionFired(NSButton sender) {
		this.workset.clear();
		this.setCanceled(false);
		NSApplication.sharedApplication().endSheet(this.window(), sender.tag());
	}

	public void cancelActionFired(NSButton sender) {
		this.validated.clear();
		this.workset.clear();
		this.setCanceled(true);
		NSApplication.sharedApplication().endSheet(this.window(), sender.tag());
	}

	private static NSMutableParagraphStyle lineBreakByTruncatingMiddleParagraph = new NSMutableParagraphStyle();

	static {
		lineBreakByTruncatingMiddleParagraph.setLineBreakMode(NSParagraphStyle.LineBreakByTruncatingMiddle);
	}

	private static final NSDictionary TRUNCATE_MIDDLE_PARAGRAPH_DICTIONARY = new NSDictionary(new Object[]{lineBreakByTruncatingMiddleParagraph},
	    new Object[]{NSAttributedString.ParagraphStyleAttributeName});
	
	// ----------------------------------------------------------
	// NSTableView.DataSource
	// ----------------------------------------------------------
	
	public void tableViewSelectionDidChange(NSNotification notification) {
		if(this.fileTableView.selectedRow() != -1) {
			Path p = (Path)this.workset.get(this.fileTableView.selectedRow());
			if(p != null) {
				if(p.getLocal().exists()) {
					this.localField.setAttributedStringValue(new NSAttributedString(p.getLocal().getAbsolute(),
					    TRUNCATE_MIDDLE_PARAGRAPH_DICTIONARY));
				}
				else {
					this.localField.setStringValue("-");
				}
				if(p.getRemote().exists()) { //@todo
					this.urlField.setAttributedStringValue(new NSAttributedString(p.getRemote().getHost().getURL()+p.getRemote().getAbsolute(),
					    TRUNCATE_MIDDLE_PARAGRAPH_DICTIONARY));
				}
			}
		}
		else {
			this.urlField.setStringValue("-");
			this.localField.setStringValue("-");
		}
	}

	private static final NSImage FOLDER_ICON = NSImage.imageNamed("folder16.tiff");

	public Object tableViewObjectValueForLocation(NSTableView tableView, NSTableColumn tableColumn, int row) {
		if(row < this.numberOfRowsInTableView(tableView)) {
			String identifier = (String)tableColumn.identifier();
			Path p = (Path)this.workset.get(row);
			if(p != null) {
				if(identifier.equals("FILENAME")) {
					return new NSAttributedString(p.getRemote().getName());
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
				if(identifier.equals("TOOLTIP")) {
					StringBuffer tooltip = new StringBuffer();
					if(p.exists()) { //@todo
						tooltip.append(NSBundle.localizedString("Remote", "")+":\n"
						    +"  "+Status.getSizeAsString(p.status.getSize())+"\n"
						    +"  "+p.attributes.getTimestampAsString());
					}
					if(p.getRemote().exists() && p.getLocal().exists())
						tooltip.append("\n");
					if(p.getLocal().exists()) {
						tooltip.append(NSBundle.localizedString("Local", "")+":\n"
						    +"  "+Status.getSizeAsString(p.getLocal().length())+"\n"
						    +"  "+p.getLocal().getTimestampAsString());
					}
					return tooltip.toString();
				}
			}
		}
		return null;
	}

	public int numberOfRowsInTableView(NSTableView tableView) {
		return this.workset.size();
	}
}