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
import com.apple.cocoa.foundation.*;

import ch.cyberduck.core.Queue;
import ch.cyberduck.core.Status;
import ch.cyberduck.core.Path;

import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

/**
* @version $Id$
 */
public class CDSyncValidatorController extends CDValidatorController {

    public CDSyncValidatorController() {
        super(false);
    }
	
	protected void load() {
        if (false == NSApplication.loadNibNamed("Sync", this)) {
            log.fatal("Couldn't load Sync.nib");
        }
		this.setEnabled(false);
	}
	
	public void awakeFromNib() {
		super.awakeFromNib();
        this.mirrorRadioCell.setTarget(this);
        this.mirrorRadioCell.setAction(new NSSelector("mirrorCellClicked", new Class[]{Object.class}));
        this.uploadRadioCell.setTarget(this);
        this.uploadRadioCell.setAction(new NSSelector("downloadCellClicked", new Class[]{Object.class}));
        this.downloadRadioCell.setTarget(this);
        this.downloadRadioCell.setAction(new NSSelector("uploadCellClicked", new Class[]{Object.class}));
	}
	
	protected void setEnabled(boolean enabled) {
		this.syncButton.setEnabled(enabled);
	}	
	
	public void mirrorCellClicked(Object sender) {
		if(this.mirrorRadioCell.state() == NSCell.OnState) {
			this.workset = new ArrayList();
			for(Iterator i = this.validated.iterator(); i.hasNext(); ) {
				Path p = (Path)i.next();
				this.workset.add(p);
			}
			this.reloadTable();
		}
	}

	public void downloadCellClicked(Object sender) {
		if(this.downloadRadioCell.state() == NSCell.OnState) {
			this.workset = new ArrayList();
			for(Iterator i = this.validated.iterator(); i.hasNext(); ) {
				Path p = (Path)i.next();
				if(p.getRemote().exists()) {
					this.workset.add(p);
				}
			}
			this.reloadTable();
		}
	}

	public void uploadCellClicked(Object sender) {
		if(this.uploadRadioCell.state() == NSCell.OnState) {
			this.workset = new ArrayList();
			for(Iterator i = this.validated.iterator(); i.hasNext(); ) {
				Path p = (Path)i.next();
				if(p.getLocal().exists()) {
					this.workset.add(p);
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
		log.debug("getResult:"+this.workset);
		return this.workset;
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
        NSApplication.sharedApplication().endSheet(this.window(), sender.tag());
    }

	protected boolean validateFile(Path p) {
		log.debug("validateFile:"+p);
		if(p.getRemote().exists() && p.getLocal().exists()) {
			//@todo should we even bother about modification dates at this stage?
			//@todo modficiation date only relevant if download/upload
//			boolean equalTimestamp = p.modificationDate().equals(p.getLocal().getTimestamp());
//			log.info(p.getRemote().getName()+" : Same modification date:"+equalTimestamp);
//			boolean equalSize = (p.size() == p.getLocal().size()); //@todo size should be correct!?
			boolean equalSize = (p.status.getSize() == p.getLocal().size()); //@todo size should be correct!?
			log.info(p.getRemote().getName()+" : Same size:"+equalSize);
//			return !equalTimestamp && !equalSize;
			return !equalSize;
		}
		return true; // Include if mirroring
	}
	
	protected boolean validateDirectory(Path path) {
		if(!path.getLocal().exists())
			path.getLocal().mkdirs();
		return false;
	}
	
	protected boolean exists(Path p) {
		return p.getRemote().exists() || p.getLocal().exists();
	}
	
	// ----------------------------------------------------------
    // NSTableView.DataSource
    // ----------------------------------------------------------
	
	private static final NSImage arrowUpIcon = NSImage.imageNamed("arrowUp16.tiff");
    private static final NSImage arrowDownIcon = NSImage.imageNamed("arrowDown16.tiff");
	
    public Object tableViewObjectValueForLocation(NSTableView tableView, NSTableColumn tableColumn, int row) {
        if (row < numberOfRowsInTableView(tableView)) {
            String identifier = (String)tableColumn.identifier();
			Path p = (Path)this.workset.get(row);
			if(p != null) {
				if (identifier.equals("TYPE")) {
					if(p.getRemote().exists() && p.getLocal().exists()) {
						if(p.getLocal().getTimestamp().before(p.getRemote().attributes.getTimestamp())) {
							return arrowDownIcon;
						}
						if(p.getLocal().getTimestamp().after(p.getRemote().attributes.getTimestamp())) {
							return arrowUpIcon;
						}
					}
					if(p.getRemote().exists()) {
						return arrowDownIcon;
					}
					if(p.getLocal().exists()) {
						return arrowUpIcon;
					}
					return NSImage.imageNamed("notfound.tiff"); // illegal argument
				}
				if (identifier.equals("ICON")) {
					NSImage icon = CDIconCache.instance().get(p.getExtension());
					icon.setSize(new NSSize(16f, 16f));
					return icon;
				}
				if (identifier.equals("NAME")) {
					return p.getRemote().getName();
				}
				if (identifier.equals("TOOLTIP")) {
					try {
						String localTimestamp = formatter.stringForObjectValue(new NSGregorianDate((double)p.getLocal().getTimestamp().getTime()/1000, 
																								   NSDate.DateFor1970)
																			   );
						String remoteTimestamp = formatter.stringForObjectValue(new NSGregorianDate((double)p.getRemote().attributes.getTimestamp().getTime()/1000, 
																									NSDate.DateFor1970)
																				);
						return
							NSBundle.localizedString("Local", "")+":\n"
							+"\t"+p.getLocal().getAbsolute()+"\n"
							+"\t"+Status.getSizeAsString(p.getLocal().length())+"\n"
							+"\t"+localTimestamp+"\n"
							+ NSBundle.localizedString("Remote", "")+":\n"
							+"\t"+p.getAbsolute()+"\n"
							+"\t"+Status.getSizeAsString(p.status.getSize())+"\n"
							+"\t"+remoteTimestamp+"\n";
					}
					catch(NSFormatter.FormattingException e) {
						log.error(e.toString());
					}
				}
				throw new IllegalArgumentException("Unknown identifier: " + identifier);
			}
        }
        return null;
    }
}