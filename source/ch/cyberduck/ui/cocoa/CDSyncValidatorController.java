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

import ch.cyberduck.core.Validator;
import ch.cyberduck.core.SyncValidator;
import ch.cyberduck.core.Path;

import java.util.List;
import java.util.ArrayList;

/**
* @version $Id$
 */
public class CDSyncValidatorController extends CDValidatorController implements Validator {

    public CDSyncValidatorController(CDController windowController, boolean resume) {
        super(windowController);
        if (false == NSApplication.loadNibNamed("Sync", this)) {
            log.fatal("Couldn't load Sync.nib");
        }
		this.validator = new SyncValidator();
    }
	
	public void awakeFromNib() {
		this.fileTableView.setDelegate(this);
		this.fileTableView.setDataSource(this);
        this.uploadRadioCell.setTarget(this);
        this.uploadRadioCell.setAction(new NSSelector("reloadData", new Class[]{}));
        this.downloadRadioCell.setTarget(this);
        this.downloadRadioCell.setAction(new NSSelector("reloadData", new Class[]{}));
	}
	
	public void reloadData() {
		this.fileTableView.deselectAll(null);
		this.fileTableView.reloadData();
	}

	// ----------------------------------------------------------
    // Outlets
    // ----------------------------------------------------------
	
	private NSButton createFilesCheckbox;
	
	public void setCreateFilesCheckbox(NSButton createFilesCheckbox) {
		this.createFilesCheckbox = createFilesCheckbox;
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
	
	private List remoteCandidates = new ArrayList();
	private List localCandidates = new ArrayList();

	public boolean validate(Path p) {
		log.debug("validate:"+p);
        if (!this.isCanceled()) {
			if(p.remote.exists()) {
				if(this.validator.validate(p)) {
					this.remoteCandidates.add(p);
					this.reloadData();
					return true;
				}
			}
			if(p.local.exists()) {
				if(this.validator.validate(p)) {
					this.localCandidates.add(p);
					this.reloadData();
					return true;
				}
			}
			return false;
        }
        log.info("Canceled " + p.getName() + " - no further validation needed");
        return false;
	}
	
	public void start() {
		this.prompt(null);
		this.statusIndicator.startAnimation(null);
	}
	
	public boolean stop() {
		this.statusIndicator.stopAnimation(null);
		this.syncButton.setEnabled(true);
		// Waiting for user to make choice
		while (windowController.window().attachedSheet() != null) {
			try {
				log.debug("Sleeping...");
				Thread.sleep(1000); //milliseconds
			}
			catch (InterruptedException e) {
				log.error(e.getMessage());
			}
		}
		return !this.isCanceled();
 	}
	
	public boolean prompt(Path ignored) {
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
			Path p = null;
			if(downloadRadioCell.state() == NSCell.OnState) {
				p = (Path)this.remoteCandidates.get(this.fileTableView.selectedRow());
			}
			if(uploadRadioCell.state() == NSCell.OnState) {
				p = (Path)this.localCandidates.get(this.fileTableView.selectedRow());
			}
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
		if(downloadRadioCell.state() == NSCell.OnState) {
			return remoteCandidates.size();
		}
		if(uploadRadioCell.state() == NSCell.OnState) {
			return localCandidates.size();
		}
		return 0;
    }
	
	private static final NSImage arrowUpIcon = NSImage.imageNamed("up.tiff");
    private static final NSImage arrowDownIcon = NSImage.imageNamed("down.tiff");
    private static final NSImage folderIcon = NSImage.imageNamed("folder16.tiff");
	
	static {
		arrowUpIcon.setSize(new NSSize(16f, 16f));	
		arrowDownIcon.setSize(new NSSize(16f, 16f));	
	}
	
    public Object tableViewObjectValueForLocation(NSTableView tableView, NSTableColumn tableColumn, int row) {
        if (row < numberOfRowsInTableView(tableView)) {
            String identifier = (String)tableColumn.identifier();
			Path p = null;
			if(downloadRadioCell.state() == NSCell.OnState) {
				p = (Path)this.remoteCandidates.get(row);
			}
			if(uploadRadioCell.state() == NSCell.OnState) {
				p = (Path)this.localCandidates.get(row);
			}
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
					if (p.attributes.isFile()) {
						NSImage icon = CDIconCache.instance().get(p.getExtension());
						icon.setSize(new NSSize(16f, 16f));
						return icon;
					}
					if (p.attributes.isDirectory()) {
						return folderIcon;
					}
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