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
import com.apple.cocoa.foundation.*;

import org.apache.log4j.Logger;

import ch.cyberduck.core.Path;
import ch.cyberduck.core.Permission;
import ch.cyberduck.core.Status;

/**
 * @version $Id$
 */
public class CDInfoController extends NSObject {
	private static Logger log = Logger.getLogger(CDInfoController.class);

	private static NSMutableArray instances = new NSMutableArray();

	private List files;

	// ----------------------------------------------------------
	// Outlets
	// ----------------------------------------------------------

	private NSWindow window; //IBOutlet

	public void setWindow(NSWindow window) {
		this.window = window;
		this.window.setDelegate(this);
	}

	public NSWindow window() {
		return this.window;
	}

	private NSTextField filenameField; //IBOutlet

	public void setFilenameField(NSTextField filenameField) {
		this.filenameField = filenameField;
	}

	private NSTextField groupField; //IBOutlet

	public void setGroupField(NSTextField groupField) {
		this.groupField = groupField;
	}

	private NSTextField kindField; //IBOutlet

	public void setKindField(NSTextField kindField) {
		this.kindField = kindField;
	}

	private NSTextField modifiedField; //IBOutlet

	public void setModifiedField(NSTextField modifiedField) {
		this.modifiedField = modifiedField;
	}

	private NSTextField ownerField; //IBOutlet

	public void setOwnerField(NSTextField ownerField) {
		this.ownerField = ownerField;
	}

	private NSTextField sizeField; //IBOutlet

	public void setSizeField(NSTextField sizeField) {
		this.sizeField = sizeField;
	}

//	private NSTextField octalField; //IBOutlet
//	
//  public void setOctalField(NSTextField octalField) {
//        this.octalField = octalField;
//    }

	private NSTextField pathField; //IBOutlet

	public void setPathField(NSTextField pathField) {
		this.pathField = pathField;
	}

	private NSBox permissionsBox; //IBOutlet

	public void setPermissionsBox(NSBox permissionsBox) {
		this.permissionsBox = permissionsBox;
	}

	private NSButton recursiveCheckbox;

	public void setRecursiveCheckbox(NSButton recursiveCheckbox) {
		this.recursiveCheckbox = recursiveCheckbox;
		this.recursiveCheckbox.setState(NSCell.OffState);
	}

	public NSButton ownerr; //IBOutlet
	public NSButton ownerw; //IBOutlet
	public NSButton ownerx; //IBOutlet
	public NSButton groupr; //IBOutlet
	public NSButton groupw; //IBOutlet
	public NSButton groupx; //IBOutlet
	public NSButton otherr; //IBOutlet
	public NSButton otherw; //IBOutlet
	public NSButton otherx; //IBOutlet

	private NSImageView iconImageView; //IBOutlet

	public void setIconImageView(NSImageView iconImageView) {
		this.iconImageView = iconImageView;
	}

	// ----------------------------------------------------------
	// Constructors
	// ----------------------------------------------------------

	public CDInfoController(Path file) {
		this.files = new ArrayList();
		this.files.add(file);
		instances.addObject(this);
		if(false == NSApplication.loadNibNamed("Info", this)) {
			log.fatal("Couldn't load Info.nib");
		}
	}

	public CDInfoController(List files) {
		this.files = files;
		instances.addObject(this);
		if(false == NSApplication.loadNibNamed("Info", this)) {
			log.fatal("Couldn't load Info.nib");
		}
	}

	public void awakeFromNib() {
		log.debug("awakeFromNib");
		Path file = (Path)this.files.get(0);

		NSPoint origin = this.window.frame().origin();
		this.window.setFrameOrigin(this.window.cascadeTopLeftFromPoint(new NSPoint(origin.x(), origin.y())));

		this.filenameField.setStringValue(this.numberOfFiles() > 1 ? "("+NSBundle.localizedString("Multiple files", "")+")" :
		                                  file.getName());
		if(this.numberOfFiles() > 1) {
			this.filenameField.setEnabled(false);
		}
		this.pathField.setStringValue(file.getParent().getAbsolute());
		this.groupField.setStringValue(this.numberOfFiles() > 1 ? "("+NSBundle.localizedString("Multiple files", "")+")" :
		                               file.attributes.getGroup());
		if(this.numberOfFiles() > 1) {
			this.kindField.setStringValue("("+NSBundle.localizedString("Multiple files", "")+")");
		}
		else {
			if(file.attributes.isSymbolicLink()) {
				if(file.attributes.isFile()) {
					this.kindField.setStringValue(NSBundle.localizedString("Symbolic Link (File)", ""));
				}
				if(file.attributes.isDirectory()) {
					this.kindField.setStringValue(NSBundle.localizedString("Symbolic Link (Folder)", ""));
				}
			}
			else if(file.attributes.isFile()) {
				this.kindField.setStringValue(NSBundle.localizedString("File", ""));
			}
			else if(file.attributes.isDirectory()) {
				this.kindField.setStringValue(NSBundle.localizedString("Folder", ""));
			}
			else {
				this.kindField.setStringValue(NSBundle.localizedString("Unknown", ""));
			}
		}

		try {
			NSGregorianDateFormatter formatter = new NSGregorianDateFormatter((String)NSUserDefaults.standardUserDefaults().objectForKey(NSUserDefaults.TimeDateFormatString), false);
			String timestamp = formatter.stringForObjectValue(new NSGregorianDate((double)file.attributes.getTimestamp().getTime()/1000,
			    NSDate.DateFor1970));
			this.modifiedField.setStringValue(this.numberOfFiles() > 1 ? "("+NSBundle.localizedString("Multiple files", "")+")" :
			                                  timestamp);
		}
		catch(NSFormatter.FormattingException e) {
			log.error(e.toString());
		}
		this.ownerField.setStringValue(this.numberOfFiles() > 1 ? "("+NSBundle.localizedString("Multiple files", "")+")" :
		                               file.attributes.getOwner());
		int size = 0;
		for(Iterator i = files.iterator(); i.hasNext();) {
			size += ((Path)i.next()).getSize();
		}
		this.sizeField.setStringValue(Status.getSizeAsString(size)+" ("+size+" bytes)");

		{
			ownerr.setAllowsMixedState(true);
			ownerr.setEnabled(false);
			ownerw.setAllowsMixedState(true);
			ownerw.setEnabled(false);
			ownerx.setAllowsMixedState(true);
			ownerx.setEnabled(false);
			groupr.setAllowsMixedState(true);
			groupr.setEnabled(false);
			groupw.setAllowsMixedState(true);
			groupw.setEnabled(false);
			groupx.setAllowsMixedState(true);
			groupx.setEnabled(false);
			otherr.setAllowsMixedState(true);
			otherr.setEnabled(false);
			otherw.setAllowsMixedState(true);
			otherw.setEnabled(false);
			otherx.setAllowsMixedState(true);
			otherx.setEnabled(false);
		}

		Permission permission = null;
		for(Iterator i = files.iterator(); i.hasNext();) {
			permission = ((Path)i.next()).attributes.getPermission();
			log.debug("Permission:"+permission);
			boolean[] ownerPerm = permission.getOwnerPermissions();
			boolean[] groupPerm = permission.getGroupPermissions();
			boolean[] otherPerm = permission.getOtherPermissions();

			this.update(ownerr, ownerPerm[Permission.READ]);
			this.update(ownerw, ownerPerm[Permission.WRITE]);
			this.update(ownerx, ownerPerm[Permission.EXECUTE]);

			this.update(groupr, groupPerm[Permission.READ]);
			this.update(groupw, groupPerm[Permission.WRITE]);
			this.update(groupx, groupPerm[Permission.EXECUTE]);

			this.update(otherr, otherPerm[Permission.READ]);
			this.update(otherw, otherPerm[Permission.WRITE]);
			this.update(otherx, otherPerm[Permission.EXECUTE]);
		}
		
//		octalField.setStringValue(""+file.getOctalCode());
		if(this.numberOfFiles() > 1) {
			permissionsBox.setTitle(NSBundle.localizedString("Permissions", "")+" | "+"("+NSBundle.localizedString("Multiple files", "")+")");
		}
		else {
			permissionsBox.setTitle(NSBundle.localizedString("Permissions", "")+" | "+permission.toString());
		}

		NSImage fileIcon = null;
		if(this.numberOfFiles() > 1) {
			fileIcon = NSImage.imageNamed("multipleDocuments32.tiff");
		}
		else {
			if(file.attributes.isFile()) {
				fileIcon = CDIconCache.instance().get(file.getExtension());
				fileIcon.setSize(new NSSize(32f, 32f));
			}
			if(file.attributes.isDirectory()) {
				fileIcon = NSImage.imageNamed("folder32.tiff");
			}
		}
		this.iconImageView.setImage(fileIcon);

		(NSNotificationCenter.defaultCenter()).addObserver(this,
		    new NSSelector("filenameInputDidEndEditing", new Class[]{NSNotification.class}),
		    NSControl.ControlTextDidEndEditingNotification,
		    filenameField);
		//        (NSNotificationCenter.defaultCenter()).addObserver(this,
		//														   new NSSelector("octalInputDidEndEditing", new Class[]{NSNotification.class}),
		//														   NSControl.ControlTextDidEndEditingNotification,
		//														   octalField);
	}

	private void update(NSButton checkbox, boolean condition) {
		// Sets the cell's state to value, which can be NSCell.OnState, NSCell.OffState, or NSCell.MixedState.
		// If necessary, this method also redraws the receiver.
		log.debug("Checkbox state:"+checkbox.state());
		log.debug("Should be enabled:"+condition);
		if((checkbox.state() == NSCell.OffState || !checkbox.isEnabled()) && !condition) {
			checkbox.setState(NSCell.OffState);
		}
		else if((checkbox.state() == NSCell.OnState || !checkbox.isEnabled()) && condition) {
			checkbox.setState(NSCell.OnState);
		}
		else {
			checkbox.setState(NSCell.MixedState);
		}
		checkbox.setEnabled(true);
		log.debug("New state:"+checkbox.state());
	}

	public boolean windowShouldClose(NSWindow sender) {
		return true;
	}

	public void windowWillClose(NSNotification notification) {
		log.debug("windowWillClose");
		NSNotificationCenter.defaultCenter().removeObserver(this);
		instances.removeObject(this);
	}

	private int numberOfFiles() {
		return files.size();
	}

	public void filenameInputDidEndEditing(NSNotification sender) {
		log.debug("textInputDidEndEditing");
		Path file = (Path)this.files.get(0);
		if(!filenameField.stringValue().equals(file.getName())) {
			if(filenameField.stringValue().indexOf('/') == -1) {
				file.rename(file.getParent().getAbsolute()+"/"+filenameField.stringValue());
				// refresh the file listing so that the observers (if any) get notified of the change
				file.getParent().list(true);
			}
			else if(filenameField.stringValue().length() == 0) {
				filenameField.setStringValue(file.getName());
			}
			else {
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
		}
	}

	public void permissionsSelectionChanged(Object sender) {
		log.debug("permissionsSelectionChanged");
		boolean[][] p = new boolean[3][3];
		if(((NSButton)sender).state() == NSCell.MixedState) {
			((NSButton)sender).setState(NSCell.OnState);
		}

		p[Permission.OWNER][Permission.READ] = (ownerr.state() == NSCell.OnState);
		p[Permission.OWNER][Permission.WRITE] = (ownerw.state() == NSCell.OnState);
		p[Permission.OWNER][Permission.EXECUTE] = (ownerx.state() == NSCell.OnState);

		p[Permission.GROUP][Permission.READ] = (groupr.state() == NSCell.OnState);
		p[Permission.GROUP][Permission.WRITE] = (groupw.state() == NSCell.OnState);
		p[Permission.GROUP][Permission.EXECUTE] = (groupx.state() == NSCell.OnState);

		p[Permission.OTHER][Permission.READ] = (otherr.state() == NSCell.OnState);
		p[Permission.OTHER][Permission.WRITE] = (otherw.state() == NSCell.OnState);
		p[Permission.OTHER][Permission.EXECUTE] = (otherx.state() == NSCell.OnState);

		Permission permission = new Permission(p);
		permissionsBox.setTitle(NSBundle.localizedString("Permissions", "")+" | "+permission.toString());
		// send the changes to the remote host
		Path f = null;
		for(Iterator i = files.iterator(); i.hasNext();) {
			f = (Path)i.next();
			f.changePermissions(permission, recursiveCheckbox.state() == NSCell.OnState);
		}
		// refresh the file listing so that the observers (if any) get notified of the change
		f.getParent().list(true);
	}
}
