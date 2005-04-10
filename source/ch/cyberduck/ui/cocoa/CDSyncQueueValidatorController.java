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

import com.apple.cocoa.application.*;
import com.apple.cocoa.foundation.NSSelector;
import com.apple.cocoa.foundation.NSArray;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.TimeZone;

import org.apache.log4j.Logger;

import ch.cyberduck.core.*;

/**
 * @version $Id$
 */
public class CDSyncQueueValidatorController extends CDValidatorController {
	private static Logger log = Logger.getLogger(CDSyncQueueValidatorController.class);

	static {
		ValidatorFactory.addFactory(SyncQueue.class, new Factory());
	}

	private static class Factory extends ValidatorFactory {
		protected Validator create() {
			return new CDSyncQueueValidatorController(CDQueueController.instance());
		}
	}

	private CDSyncQueueValidatorController(CDWindowController windowController) {
		super(windowController);
	}

	protected void load() {
		if(false == NSApplication.loadNibNamed("Sync", this)) {
			log.fatal("Couldn't load Sync.nib");
		}
		this.setEnabled(false);
	}

	public void awakeFromNib() {
		super.awakeFromNib();

		this.mirrorRadioCell.setTarget(this);
		this.mirrorRadioCell.setAction(new NSSelector("mirrorCellClicked", new Class[]{Object.class}));
		this.uploadRadioCell.setTarget(this);
		this.uploadRadioCell.setAction(new NSSelector("uploadCellClicked", new Class[]{Object.class}));
		this.downloadRadioCell.setTarget(this);
		this.downloadRadioCell.setAction(new NSSelector("downloadCellClicked", new Class[]{Object.class}));

		{
			NSTableColumn c = new NSTableColumn();
			c.setIdentifier("NEW");
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
			c.setIdentifier("TYPE");
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
	}

	protected boolean validateFile(Path p, boolean resume) {
		if(p.getRemote().exists() && p.getLocal().exists()) {
			if(!(p.getRemote().attributes.getTimestampAsCalendar().equals(p.getLocal().getTimestampAsCalendar()))) {
				this.prompt(p);
			}
		}
		else {
			this.prompt(p);
		}
		return false;
	}

	protected boolean validateDirectory(Path p) {
		if(p.getRemote().exists() && p.getLocal().exists()) {
			//Do not include as it exists both locally and on the server
			return false;
		}
		else {
			//List the directory in the validation window that the user sees it will get created
			if(!p.getRemote().exists()) {
				p.getSession().cache().put(p.getAbsolute(), new ArrayList());
			}
			this.prompt(p);
			return false;
		}
	}

	protected boolean isExisting(Path p) {
		return p.getRemote().exists() || p.getLocal().exists();
	}

	protected void setEnabled(boolean enabled) {
		this.syncButton.setEnabled(enabled);
	}

	public void mirrorCellClicked(Object sender) {
		if(this.mirrorRadioCell.state() == NSCell.OnState) {
			this.workList = new ArrayList();
			for(Iterator i = this.promptList.iterator(); i.hasNext();) {
				Path p = (Path)i.next();
				this.workList.add(p);
			}
		}
		super.fireDataChanged();
	}

	public void downloadCellClicked(Object sender) {
		if(this.downloadRadioCell.state() == NSCell.OnState) {
			this.workList = new ArrayList();
			for(Iterator i = this.promptList.iterator(); i.hasNext();) {
				Path p = (Path)i.next();
				if(p.getRemote().exists()) {
					this.workList.add(p);
				}
			}
		}
		super.fireDataChanged();
	}

	public void uploadCellClicked(Object sender) {
		if(this.uploadRadioCell.state() == NSCell.OnState) {
			this.workList = new ArrayList();
			for(Iterator i = this.promptList.iterator(); i.hasNext();) {
				Path p = (Path)i.next();
				if(p.getLocal().exists()) {
					this.workList.add(p);
				}
			}
		}
		super.fireDataChanged();
	}

	// ----------------------------------------------------------
	// Outlets
	// ----------------------------------------------------------
	
	protected NSButtonCell mirrorRadioCell;

	public void setSyncRadioCell(NSButtonCell mirrorRadioCell) {
		this.mirrorRadioCell = mirrorRadioCell;
	}

	protected NSButtonCell downloadRadioCell;

	public void setDownloadRadioCell(NSButtonCell downloadRadioCell) {
		this.downloadRadioCell = downloadRadioCell;
	}

	protected NSButtonCell uploadRadioCell;

	public void setUploadRadioCell(NSButtonCell uploadRadioCell) {
		this.uploadRadioCell = uploadRadioCell;
	}

	protected NSButton syncButton;

	public void setSyncButton(NSButton syncButton) {
		this.syncButton = syncButton;
		this.syncButton.setEnabled(false);
		this.syncButton.setTarget(this);
		this.syncButton.setAction(new NSSelector("syncActionFired", new Class[]{Object.class}));
	}

	public void syncActionFired(NSButton sender) {
		this.validatedList.addAll(this.workList); //Include the files that have been manually validated
		for(Iterator i = this.workList.iterator(); i.hasNext(); ) {
			Path p = (Path)i.next();
			if(!p.isSkipped()) {
				this.validatedList.add(p);
			}
		}
		this.setCanceled(false);
        this.windowController.endSheet(this.window(), sender.tag());
	}
	
	private NSPopUpButton timezonePopupButton;

	public void setTimezonePopupButton(NSPopUpButton timezonePopupButton) {
		this.timezonePopupButton = timezonePopupButton;
		this.timezonePopupButton.setTarget(this);
		this.timezonePopupButton.setAction(new NSSelector("timezonePopupButtonClicked", new Class[]{NSPopUpButton.class}));
		this.timezonePopupButton.removeAllItems();
		this.timezonePopupButton.addItemsWithTitles(new NSArray(TimeZone.getAvailableIDs()));
		this.timezonePopupButton.setTitle(TimeZone.getDefault().getID());
	}

	public void timezonePopupButtonClicked(NSPopUpButton sender) {
		Preferences.instance().setProperty("queue.sync.timezone", sender.titleOfSelectedItem());
		this.fireDataChanged();
	}

	// ----------------------------------------------------------
	// NSTableView.DataSource
	// ----------------------------------------------------------
	
	protected void fireDataChanged() {
		if(this.hasPrompt()) {
			this.mirrorCellClicked(null);
			this.downloadCellClicked(null);
			this.uploadCellClicked(null);
		}
	}

	private static final NSImage ARROW_UP_ICON = NSImage.imageNamed("arrowUp16.tiff");
	private static final NSImage ARROW_DOWN_ICON = NSImage.imageNamed("arrowDown16.tiff");
	private static final NSImage PLUS_ICON = NSImage.imageNamed("plus.tiff");

	public Object tableViewObjectValueForLocation(NSTableView tableView, NSTableColumn tableColumn, int row) {
		if(row < this.numberOfRowsInTableView(tableView)) {
			String identifier = (String)tableColumn.identifier();
			Path p = (Path)this.workList.get(row);
			if(p != null) {
				if(identifier.equals("TYPE")) {
					if(p.getRemote().exists() && p.getLocal().exists()) {
						if(p.getLocal().getTimestampAsCalendar().before(p.getRemote().attributes.getTimestampAsCalendar())) {
							return ARROW_DOWN_ICON;
						}
						if(p.getLocal().getTimestampAsCalendar().after(p.getRemote().attributes.getTimestampAsCalendar())) {
							return ARROW_UP_ICON;
						}
					}
					if(p.getRemote().exists()) {
						return ARROW_DOWN_ICON;
					}
					if(p.getLocal().exists()) {
						return ARROW_UP_ICON;
					}
					throw new IllegalArgumentException("The file must exist either locally or on the server");
				}
				if(identifier.equals("NEW")) {
					if(!(p.getRemote().exists() && p.getLocal().exists())) {
						return PLUS_ICON;
					}
					return null;
				}
				return super.tableViewObjectValueForLocation(tableView, tableColumn, row);
			}
		}
		return null;
	}
}