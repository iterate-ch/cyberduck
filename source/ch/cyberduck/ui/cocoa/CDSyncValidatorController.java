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
import ch.cyberduck.core.Path;

import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

/**
* @version $Id$
 */
public class CDSyncValidatorController extends CDValidatorController {

    public CDSyncValidatorController(CDController windowController) {
        super(windowController, false);
        if (false == NSApplication.loadNibNamed("Sync", this)) {
            log.fatal("Couldn't load Sync.nib");
        }
    }
	
	public void awakeFromNib() {
		this.fileTableView.setDelegate(this);
		this.fileTableView.setDataSource(this);
		this.fileTableView.sizeToFit();
        this.uploadRadioCell.setTarget(this);
        this.uploadRadioCell.setAction(new NSSelector("reloadData", new Class[]{}));
        this.downloadRadioCell.setTarget(this);
        this.downloadRadioCell.setAction(new NSSelector("reloadData", new Class[]{}));
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
	
	private NSTextField urlField;
	
	public void setUrlField(NSTextField urlField) {
		this.urlField = urlField;
	}
	
	private NSTextField localField;
	
	public void setLocalField(NSTextField localField) {
		this.localField = localField;
	}
	
	private NSProgressIndicator statusIndicator;
	
	public void setStatusIndicator(NSProgressIndicator statusIndicator) {
		this.statusIndicator = statusIndicator;
	}
	
	private NSTableView fileTableView;
	
	public void setFileTableView(NSTableView fileTableView) {
		this.fileTableView = fileTableView;
	}
	
	private NSButton syncButton;
	
	public void setSyncButton(NSButton syncButton) {
		this.syncButton = syncButton;
		this.syncButton.setEnabled(false);
	}
	
	public void syncActionFired(NSButton sender) {
        NSApplication.sharedApplication().endSheet(this.window(), sender.tag());
    }

	public void cancelActionFired(NSButton sender) {
        this.setCanceled(true);
        NSApplication.sharedApplication().endSheet(this.window(), sender.tag());
    }
	
	public void validateSheetDidEnd(NSWindow sheet, int returncode, Object contextInfo) {
        sheet.close();
	}
	
	private List candidates = new ArrayList();
	private List workset = new ArrayList();

	public void reloadData() {
		this.fileTableView.deselectAll(null);
		this.workset = new ArrayList();
		for(Iterator i = this.candidates.iterator(); i.hasNext(); ) {
			Path p = (Path)i.next();
			if(mirrorRadioCell.state() == NSCell.OnState) {
				this.workset.add(p);
				continue;
			}
			if(downloadRadioCell.state() == NSCell.OnState) {
				if(p.remote.exists())
					this.workset.add(p);
				continue;
			}
			if(uploadRadioCell.state() == NSCell.OnState) {
				if(p.local.exists())
					this.workset.add(p);
				continue;
			}
		}
		this.fileTableView.reloadData();
	}
	
	public List validate(Queue q) {
		this.statusIndicator.startAnimation(null);
		this.prompt(null);
		this.candidates = super.validate(q);
		this.reloadData();
		this.statusIndicator.stopAnimation(null);
		this.syncButton.setEnabled(true);
		while (windowController.window().attachedSheet() != null) {
			try {
				log.debug("Sleeping...");
				//block the caller thread
				Thread.sleep(1000); //milliseconds
			}
			catch (InterruptedException e) {
				log.error(e.getMessage());
			}
		}
		return workset;
	}
	
	protected boolean validateFile(Path p) {
		this.reloadData(); //@todo should be after return statement
		return !p.modificationDate().equals(p.getLocal().getTimestamp());
	}
	
	protected boolean validateDirectory(Path path) {
		if(!path.local.exists())
			path.local.mkdirs();
		return false;
	}
	
	protected boolean prompt(Path ignored) {
        while (windowController.window().attachedSheet() != null) {
            try {
                log.debug("Sleeping...");
                Thread.sleep(1000); //milliseconds
            }
            catch (InterruptedException e) {
                log.error(e.getMessage());
            }
        }
		windowController.window().makeKeyAndOrderFront(null);
		NSApplication.sharedApplication().beginSheet(this.window(), //sheet
													 windowController.window(),
													 this, //modalDelegate
													 new NSSelector("validateSheetDidEnd",
																	new Class[]{NSWindow.class, int.class, Object.class}), // did end selector
													 null); //contextInfo
		windowController.window().makeKeyAndOrderFront(null);
		return true;
	}
	
	protected boolean exists(Path p) {
		return p.remote.exists() || p.local.exists();
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
				this.urlField.setAttributedStringValue(new NSAttributedString(p.getHost().getURL()+p.getAbsolute(), 
																			  TRUNCATE_MIDDLE_PARAGRAPH_DICTIONARY));
				this.localField.setAttributedStringValue(new NSAttributedString(p.getLocal().getAbsolute(),
																				TRUNCATE_MIDDLE_PARAGRAPH_DICTIONARY));
			}
		}
		else {
			this.urlField.setStringValue("");
			this.localField.setStringValue("");
		}
	}
	
	public int numberOfRowsInTableView(NSTableView tableView) {
		return workset.size();
    }
	
	private static final NSImage arrowUpIcon = NSImage.imageNamed("up.tiff");
    private static final NSImage arrowDownIcon = NSImage.imageNamed("down.tiff");
	
	static {
		arrowUpIcon.setSize(new NSSize(16f, 16f));	
		arrowDownIcon.setSize(new NSSize(16f, 16f));	
	}
	
    public Object tableViewObjectValueForLocation(NSTableView tableView, NSTableColumn tableColumn, int row) {
        if (row < numberOfRowsInTableView(tableView)) {
            String identifier = (String)tableColumn.identifier();
			Path p = (Path)this.workset.get(row);
			if(p != null) {
				if (identifier.equals("TYPE")) {
					if(p.getLocal().getTimestamp().before(p.attributes.getTimestamp())) {
						return arrowDownIcon;
					}
					if(p.getLocal().getTimestamp().after(p.attributes.getTimestamp())) {
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
					return p.getName();
				}
				throw new IllegalArgumentException("Unknown identifier: " + identifier);
			}
        }
        return null;
    }
}