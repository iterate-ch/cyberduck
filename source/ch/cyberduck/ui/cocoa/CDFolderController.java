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
import com.apple.cocoa.foundation.NSBundle;
import com.apple.cocoa.foundation.NSMutableArray;
import com.apple.cocoa.foundation.NSNotification;

import java.util.List;

import org.apache.log4j.Logger;

import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathFactory;
import ch.cyberduck.core.HiddenFilesFilter;
import ch.cyberduck.core.NullFilter;

/**
 * @version $Id$
 */
public class CDFolderController extends CDController {
	private static Logger log = Logger.getLogger(CDFolderController.class);

	private static NSMutableArray instances = new NSMutableArray();

	private NSTextField folderField; //IBOutlet

	public void setFolderField(NSTextField folderField) {
		this.folderField = folderField;
	}

	public CDFolderController() {
		instances.addObject(this);
		if(false == NSApplication.loadNibNamed("Folder", this)) {
			log.fatal("Couldn't load Folder.nib");
		}
	}

	public void awakeFromNib() {
		this.window().setReleasedWhenClosed(true);
	}

	public void windowWillClose(NSNotification notification) {
		instances.removeObject(this);
	}

	public void createButtonClicked(NSButton sender) {
		// Ends a document modal session by specifying the sheet window, sheet. Also passes along a returnCode to the delegate.
		if(folderField.stringValue().indexOf('/') != -1) {
			NSAlertPanel.beginInformationalAlertSheet(NSBundle.localizedString("Error", "Alert sheet title"), //title
			    NSBundle.localizedString("OK", "Alert default button"), // defaultbutton
			    null, //alternative button
			    null, //other button
			    this.window(), //docWindow
			    null, //modalDelegate
			    null, //didEndSelector
			    null, // dismiss selector
			    null, // context
			    NSBundle.localizedString("Invalid character in folder name.", "") // message
			);
		}
		else if(folderField.stringValue().length() == 0) {
			//
		}
		else {
			NSApplication.sharedApplication().endSheet(this.window(), sender.tag());
		}
	}

	public void cancelButtonClicked(NSButton sender) {
		NSApplication.sharedApplication().endSheet(this.window(), sender.tag());
	}

	public void newFolderSheetDidEnd(NSPanel sheet, int returncode, Object contextInfo) {
		log.debug("newFolderSheetDidEnd");
		sheet.orderOut(null);
		switch(returncode) {
			case (NSAlertPanel.DefaultReturn):
				Path workdir = (Path)contextInfo;
				this.create(workdir, folderField.stringValue());
				break;
			case (NSAlertPanel.AlternateReturn):
				break;
		}
	}
	
	public Path create(Path workdir, String filename) {
		Path folder = PathFactory.createPath(workdir.getSession(), workdir.getAbsolute(), filename);
		folder.mkdir(false);
		List l = null;
		if(filename.charAt(0) == '.')
			l = workdir.list(true, new NullFilter());
		else 
			l = workdir.list(true, new HiddenFilesFilter());
		if(l.contains(folder))
			return (Path)l.get(l.indexOf(folder));
		return null;
	}
}
