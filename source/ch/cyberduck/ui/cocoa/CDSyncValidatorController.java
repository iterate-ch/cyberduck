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
	
	private NSButton addFilesCheckbox;
	
	public void setAddFilesCheckbox(NSButton addFilesCheckbox) {
		this.addFilesCheckbox = addFilesCheckbox;
	}

	private NSButton deleteFilesCheckbox;
	
	public void setDeleteFilesCheckbox(NSButton deleteFilesCheckbox) {
		this.deleteFilesCheckbox = deleteFilesCheckbox;
	}

	private NSButtonCell downloadRadioCell;
	
	public void setDownloadRadioCell(NSButtonCell downloadRadioCell) {
		this.downloadRadioCell = downloadRadioCell;
	}

	private NSButtonCell uploadRadioCell;
	
	public void setUploadRadioCell(NSButtonCell uploadRadioCell) {
		this.uploadRadioCell = uploadRadioCell;
	}
	
	private NSTableView fileTableView;
	
	public void setFileTableView(NSTableView fileTableView) {
		this.fileTableView = fileTableView;
	}
	
	public void syncActionFired(NSButton sender) {
        NSApplication.sharedApplication().endSheet(this.window(), sender.tag());
    }

	public void cancelActionFired(NSButton sender) {
        NSApplication.sharedApplication().endSheet(this.window(), sender.tag());
    }
	
	public void validateSheetDidEnd(NSWindow sheet, int returncode, Object contextInfo) {
        sheet.close();
	}

	public boolean prompt(Path path) {
        while (windowController.window().attachedSheet() != null) {
            try {
                log.debug("Sleeping...");
                Thread.sleep(1000); //milliseconds
            }
            catch (InterruptedException e) {
                log.error(e.getMessage());
            }
        }
		NSApplication.sharedApplication().beginSheet(this.window(), //sheet
													 windowController.window(),
													 this, //modalDelegate
													 new NSSelector("validateSheetDidEnd",
																	new Class[]{NSWindow.class, int.class, Object.class}), // did end selector
													 path); //contextInfo
		windowController.window().makeKeyAndOrderFront(null);
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
//		boolean shouldAddFiles = addFilesCheckbox.isSelected();
//		boolean shouldDeleteFiles = deleteFilesCheckbox.isSelected();
		return true;
	}
}