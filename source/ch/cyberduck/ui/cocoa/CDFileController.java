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
import com.apple.cocoa.foundation.NSPathUtilities;

import java.util.List;

import org.apache.log4j.Logger;

import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathFactory;
import ch.cyberduck.ui.cocoa.odb.Editor;

/**
 * @version $Id$
 */
public class CDFileController extends CDController {
	private static Logger log = Logger.getLogger(CDFileController.class);

	private static NSMutableArray instances = new NSMutableArray();

	private NSTextField filenameField; //IBOutlet

	public void setFilenameField(NSTextField filenameField) {
		this.filenameField = filenameField;
	}

	public void awakeFromNib() {
		this.window().setReleasedWhenClosed(true);
	}

	public CDFileController() {
		instances.addObject(this);
		if(false == NSApplication.loadNibNamed("File", this)) {
			log.fatal("Couldn't load File.nib");
		}
	}

	public void windowWillClose(NSNotification notification) {
		instances.removeObject(this);
	}

	public void createButtonClicked(NSButton sender) {
		// Ends a document modal session by specifying the sheet window, sheet. Also passes along a returnCode to the delegate.
		if(filenameField.stringValue().indexOf('/') != -1) {
			NSAlertPanel.beginInformationalAlertSheet(NSBundle.localizedString("Error", "Alert sheet title"), //title
			    NSBundle.localizedString("OK", "Alert default button"), // defaultbutton
			    null, //alternative button
			    null, //other button
			    this.window(), //docWindow
			    null, //modalDelegate
			    null, //didEndSelector
			    null, // dismiss selector
			    null, // context
			    NSBundle.localizedString("Invalid character in filename.", "") // message
			);
		}
		else if(filenameField.stringValue().length() == 0) {
			//
		}
		else {
			NSApplication.sharedApplication().endSheet(this.window(), sender.tag());
		}
	}

	public void cancelButtonClicked(NSButton sender) {
		NSApplication.sharedApplication().endSheet(this.window(), sender.tag());
	}

	public void editButtonClicked(NSButton sender) {
		this.createButtonClicked(sender);
	}

	public void newFileSheetDidEnd(NSPanel sheet, int returncode, Object contextInfo) {
		log.debug("newFileSheetDidEnd");
		sheet.orderOut(null);
		Path workdir = (Path)contextInfo;
		switch(returncode) {
			case (NSAlertPanel.DefaultReturn): //Edit
				Path path = this.create(workdir, filenameField.stringValue());
				Editor editor = new Editor();
				editor.open(path);
				break;
			case (NSAlertPanel.OtherReturn): //Create
				this.create(workdir, filenameField.stringValue());
				break;
			case (NSAlertPanel.AlternateReturn): //Cancel
				break;
		}
	}

	public Path create(Path workdir, String filename) {
		Path file = PathFactory.createPath(workdir.getSession(), workdir.getAbsolute(), new Local(NSPathUtilities.temporaryDirectory(), filename));
		if(!file.getRemote().exists()) {
			try {
				String proposal;
				int no = 0;
				int index = filename.lastIndexOf(".");
				while(file.getLocal().exists()) {
					no++;
					if(index != -1) {
						proposal = filename.substring(0, index)+"-"+no+filename.substring(index);
					}
					else {
						proposal = filename+"-"+no;
					}
					file.setLocal(new Local(NSPathUtilities.temporaryDirectory(), proposal));
				}
				file.getLocal().createNewFile();
				file.upload();
				file.getLocal().delete();
			}
			catch(java.io.IOException e) {
				log.error(e.getMessage());
			}
		}
		List l = workdir.list(true);
		return (Path)l.get(l.indexOf(file));
	}
}
