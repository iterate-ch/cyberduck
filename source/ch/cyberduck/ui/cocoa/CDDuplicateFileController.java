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
import com.apple.cocoa.foundation.NSPathUtilities;
import com.apple.cocoa.foundation.NSSize;

import java.util.List;

import ch.cyberduck.core.*;
import ch.cyberduck.ui.cocoa.odb.Editor;

/**
* @version $Id$
 */
public class CDDuplicateFileController extends CDFileController {
	
	private NSImageView iconView;
	
	public void setIconView(NSImageView iconView) {
		this.iconView = iconView;
	}
	
	private Path file;
	
	public CDDuplicateFileController(Path file) {
		this.file = file;
		if(false == NSApplication.loadNibNamed("Duplicate", this)) {
			log.fatal("Couldn't load Duplicate.nib");
		}
	}
	
	public void awakeFromNib() {
        super.awakeFromNib();

		NSImage icon = NSWorkspace.sharedWorkspace().iconForFileType(this.file.getExtension());
        icon.setScalesWhenResized(true);
		icon.setSize(new NSSize(64f, 64f));
		this.iconView.setImage(icon);
		this.filenameField.setStringValue(this.file.getName()+"-Copy");
		this.window().setReleasedWhenClosed(true);
	}
	
	public void duplicateFileSheetDidEnd(NSPanel sheet, int returncode, Object contextInfo) {
		log.debug("duplicateFileSheetDidEnd");
        sheet.orderOut(null);
		Path workdir = (Path)contextInfo;
		switch(returncode) {
			case (NSAlertPanel.DefaultReturn): //Duplicate
				this.duplicate(workdir, filenameField.stringValue());
				break;
			case (NSAlertPanel.OtherReturn): //Edit
				Path path = this.duplicate(workdir, filenameField.stringValue());
				if(path != null) {
					Editor editor = new Editor();
					editor.open(path);
				}
					break;
			case (NSAlertPanel.AlternateReturn): //Cancel
				break;
		}
	}
	
	protected Path duplicate(Path workdir, String filename) {
		Path p = PathFactory.createPath(workdir.getSession(), 
										workdir.getAbsolute(), 
										new Local(NSPathUtilities.temporaryDirectory(), 
												  this.file.getName()));
		p.download();
		p.setPath(workdir.getAbsolute(), filename);
		p.upload();
		List l = null;
		if(filename.charAt(0) == '.')
			l = workdir.list(true, new NullFilter());
		else 
			l = workdir.list(true, new HiddenFilesFilter());
		if(l.contains(p))
			return (Path)l.get(l.indexOf(p));
		return null;
	}	
}