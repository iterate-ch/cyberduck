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
        this.mirrorRadioCell.setTarget(this);
        this.mirrorRadioCell.setAction(new NSSelector("mirrorCellClicked", new Class[]{Object.class}));
        this.uploadRadioCell.setTarget(this);
        this.uploadRadioCell.setAction(new NSSelector("uploadCellClicked", new Class[]{Object.class}));
        this.downloadRadioCell.setTarget(this);
        this.downloadRadioCell.setAction(new NSSelector("downloadCellClicked", new Class[]{Object.class}));
	}
	
	public void mirrorCellClicked(Object sender) {
		this.workset = new ArrayList();
		for(Iterator i = this.candidates.iterator(); i.hasNext(); ) {
			Path p = (Path)i.next();
			log.debug("> add");
			this.workset.add(p);
		}
	}

	public void downloadCellClicked(Object sender) {
		this.workset = new ArrayList();
		for(Iterator i = this.candidates.iterator(); i.hasNext(); ) {
			Path p = (Path)i.next();
			if(p.remote.exists()) {
				log.debug("> add");
				this.workset.add(p);
			}
		}
	}

	public void uploadCellClicked(Object sender) {
		this.workset = new ArrayList();
		for(Iterator i = this.candidates.iterator(); i.hasNext(); ) {
			Path p = (Path)i.next();
			if(p.local.exists()) {
				log.debug("> add");
				this.workset.add(p);
			}
		}
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

	private NSTextField localTimestampField;
	
	public void setLocalTimestampField(NSTextField localTimestampField) {
		this.localTimestampField = localTimestampField;
	}

	private NSTextField remoteTimestampField;
	
	public void setRemoteTimestampField(NSTextField remoteTimestampField) {
		this.remoteTimestampField = remoteTimestampField;
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
		this.workset.clear();
        NSApplication.sharedApplication().endSheet(this.window(), sender.tag());
    }
	
	public void validateSheetDidEnd(NSWindow sheet, int returncode, Object contextInfo) {
        sheet.close();
	}
	
	private List candidates = new ArrayList();
	private List workset = new ArrayList();

	public void reloadData() {
		log.debug("reloadData");
		this.fileTableView.deselectAll(null);
		log.debug("Working set: "+this.workset);
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
		if(p.remote.exists() && p.local.exists()) {
			return !p.modificationDate().equals(p.getLocal().getTimestamp());
		}
		return true; // Include if mirroring
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

	private final NSGregorianDateFormatter formatter = new NSGregorianDateFormatter((String)NSUserDefaults.standardUserDefaults().objectForKey(NSUserDefaults.TimeDateFormatString), false);
	
	public void tableViewSelectionDidChange(NSNotification notification) {
		if(this.fileTableView.selectedRow() != -1) {
			Path p = (Path)this.workset.get(this.fileTableView.selectedRow());
			if(p != null) {
				try {
					if(p.local.exists()) {
						this.localField.setAttributedStringValue(new NSAttributedString(p.getLocal().getAbsolute(),
																						TRUNCATE_MIDDLE_PARAGRAPH_DICTIONARY)
																 );
						String localeTS = formatter.stringForObjectValue(new NSGregorianDate((double)p.local.getTimestamp().getTime()/1000, 
																							 NSDate.DateFor1970)
																		 );
						this.localTimestampField.setAttributedStringValue(new NSAttributedString(localeTS, TRUNCATE_MIDDLE_PARAGRAPH_DICTIONARY));
					}
					else {
						this.localField.setStringValue("-");
						this.localTimestampField.setStringValue("-");
					}
					if(p.remote.exists()) {
						this.urlField.setAttributedStringValue(new NSAttributedString(p.getHost().getURL()+p.getAbsolute(), 
																					  TRUNCATE_MIDDLE_PARAGRAPH_DICTIONARY)
															   );
						String remoteTS = formatter.stringForObjectValue(new NSGregorianDate((double)p.remote.attributes.getTimestamp().getTime()/1000, 
																							 NSDate.DateFor1970)
																		 );
						this.remoteTimestampField.setAttributedStringValue(new NSAttributedString(remoteTS, TRUNCATE_MIDDLE_PARAGRAPH_DICTIONARY));
					}
					else {
						this.urlField.setStringValue("-");
						this.remoteTimestampField.setStringValue("-");
					}
				}
				catch(NSFormatter.FormattingException e) {
					log.error(e.toString());
				}
			}
		}
		else {
			this.urlField.setStringValue("");
			this.remoteTimestampField.setStringValue("");
			this.localField.setStringValue("");
			this.localTimestampField.setStringValue("");
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