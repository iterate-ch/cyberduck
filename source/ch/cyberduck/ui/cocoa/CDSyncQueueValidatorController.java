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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.apple.cocoa.application.*;
import com.apple.cocoa.foundation.NSSelector;

import org.apache.log4j.Logger;

import ch.cyberduck.core.Path;
import ch.cyberduck.core.SyncQueue;
import ch.cyberduck.core.Validator;
import ch.cyberduck.core.ValidatorFactory;

/**
 * @version $Id$
 */
public class CDSyncQueueValidatorController extends CDValidatorController {
	protected static Logger log = Logger.getLogger(CDSyncQueueValidatorController.class);

	static {
		ValidatorFactory.addFactory(SyncQueue.class, new Factory());
	}

	private static class Factory extends ValidatorFactory {
		protected Validator create(boolean resumeRequested) {
			return new CDSyncQueueValidatorController();
		}
	}

	private CDSyncQueueValidatorController() {
		super(false);
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
	}

	protected boolean validateFile(Path p) {
		log.debug("validateFile:"+p);
		if(p.getRemote().exists(false) && p.getLocal().exists()) {
			if (!(p.status.getSize() == p.getLocal().size())) { //@todo size should be correct!?
				this.prompt(p);
			}
		}
		else {
			this.prompt(p);
		}
		return false;
	}
	
	protected boolean exists(Path p) {
		return p.getRemote().exists(false) || p.getLocal().exists();
	}
	
	protected void setEnabled(boolean enabled) {
		this.syncButton.setEnabled(enabled);
	}

	public void mirrorCellClicked(Object sender) {
		if(this.mirrorRadioCell.state() == NSCell.OnState) {
			this.validated = new ArrayList();
			for(Iterator i = this.workset.iterator(); i.hasNext();) {
				Path p = (Path)i.next();
				this.validated.add(p);
			}
			this.reloadTable();
		}
	}

	public void downloadCellClicked(Object sender) {
		if(this.downloadRadioCell.state() == NSCell.OnState) {
			this.validated = new ArrayList();
			for(Iterator i = this.workset.iterator(); i.hasNext();) {
				Path p = (Path)i.next();
				if(p.getRemote().exists(false)) {
					this.validated.add(p);
				}
			}
			this.reloadTable();
		}
	}

	public void uploadCellClicked(Object sender) {
		if(this.uploadRadioCell.state() == NSCell.OnState) {
			this.validated = new ArrayList();
			for(Iterator i = this.workset.iterator(); i.hasNext();) {
				Path p = (Path)i.next();
				if(p.getLocal().exists()) {
					this.validated.add(p);
				}
			}
			this.reloadTable();
		}
	}

	protected void fireDataChanged() {
		this.mirrorCellClicked(null);
		this.downloadCellClicked(null);
		this.uploadCellClicked(null);
	}

	public List getResult() {
		return this.validated;
	}

	// ----------------------------------------------------------
	// Outlets
	// ----------------------------------------------------------
	
	private NSButtonCell mirrorRadioCell;

	public void setSyncRadioCell(NSButtonCell mirrorRadioCell) {
		this.mirrorRadioCell = mirrorRadioCell;
	}

	private NSButtonCell downloadRadioCell;

	public void setDownloadRadioCell(NSButtonCell downloadRadioCell) {
		this.downloadRadioCell = downloadRadioCell;
	}

	private NSButtonCell uploadRadioCell;

	public void setUploadRadioCell(NSButtonCell uploadRadioCell) {
		this.uploadRadioCell = uploadRadioCell;
	}

	private NSButton syncButton;

	public void setSyncButton(NSButton syncButton) {
		this.syncButton = syncButton;
		this.syncButton.setEnabled(false);
	}

	public void syncActionFired(NSButton sender) {
		log.debug("syncActionFired");
		NSApplication.sharedApplication().endSheet(this.window(), sender.tag());
	}
	
	// ----------------------------------------------------------
	// NSTableView.DataSource
	// ----------------------------------------------------------
	
	private static final NSImage ARROW_UP_ICON = NSImage.imageNamed("arrowUpBlack16.tiff");
	private static final NSImage ARROW_DOWN_ICON = NSImage.imageNamed("arrowDownBlack16.tiff");
	private static final NSImage PLUS_ICON = NSImage.imageNamed("plus.tiff");

	public int numberOfRowsInTableView(NSTableView tableView) {
		return this.validated.size();
	}
	
	public Object tableViewObjectValueForLocation(NSTableView tableView, NSTableColumn tableColumn, int row) {
		if(row < this.numberOfRowsInTableView(tableView)) {
			String identifier = (String)tableColumn.identifier();
			Path p = (Path)this.validated.get(row);
			if(p != null) {
				if(identifier.equals("TYPE")) {
					if(p.getRemote().exists(false) && p.getLocal().exists()) {
						if(p.getLocal().getTimestamp().before(p.getRemote().attributes.getTimestamp())) {
							return ARROW_DOWN_ICON;
						}
						if(p.getLocal().getTimestamp().after(p.getRemote().attributes.getTimestamp())) {
							return ARROW_UP_ICON;
						}
					}
					if(p.getRemote().exists(false)) {
						return ARROW_DOWN_ICON;
					}
					if(p.getLocal().exists()) {
						return ARROW_UP_ICON;
					}
					throw new IllegalArgumentException("The file must exist either locally or on the server");
				}
				if(identifier.equals("NEW")) {
					if(!(p.getRemote().exists(false) && p.getLocal().exists())) {
						return PLUS_ICON;
					}
				}
				return super.tableViewObjectValueForLocation(tableView, tableColumn, row);
			}
		}
		return null;
	}
}