package ch.cyberduck.ui.cocoa;

/*
 *  Copyright (c) 2002 David Kocher. All rights reserved.
 *  http://icu.unizh.ch/~dkocher/
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

import com.apple.cocoa.foundation.*;
import com.apple.cocoa.application.*;
import org.apache.log4j.Logger;
import java.util.Observer;
import java.util.Observable;
import ch.cyberduck.core.Path;
//import ch.cyberduck.core.Message;
import ch.cyberduck.core.Permission;
//import ch.cyberduck.ui.ObserverList;

/**
* @version $Id$
 */
public class CDInfoController {//implements Observer {
    private static Logger log = Logger.getLogger(CDInfoController.class);

    private Path file;

    // ----------------------------------------------------------
    // Outlets
    // ----------------------------------------------------------
    
    private NSTextField filenameField; // IBOutlet
    public void setFilenameField(NSTextField filenameField) {
	this.filenameField = filenameField;
    }
    private NSTextField groupField; // IBOutlet
    public void setGroupField(NSTextField groupField) {
	this.groupField = groupField;
    }
    private NSTextField kindField; // IBOutlet
    public void setKindField(NSTextField kindField) {
	this.kindField = kindField;
    }
    private NSTextField modifiedField; // IBOutlet
    public void setModifiedField(NSTextField modifiedField) {
	this.modifiedField = modifiedField;
    }
    private NSTextField ownerField; // IBOutlet
    public void setOwnerField(NSTextField ownerField) {
	this.ownerField = ownerField;
    }
    private NSTextField sizeField; // IBOutlet
    public void setSizeField(NSTextField sizeField) {
	this.sizeField = sizeField;
    }
    private NSBox permissionsBox; // IBOutlet
    public void setPermissionsBox(NSBox permissionsBox) {
	this.permissionsBox = permissionsBox;
    }
    
    public NSButton ownerr; // IBOutlet
    public NSButton ownerw; // IBOutlet
    public NSButton ownerx; // IBOutlet
    public NSButton groupr; // IBOutlet
    public NSButton groupw; // IBOutlet
    public NSButton groupx; // IBOutlet
    public NSButton otherr; // IBOutlet
    public NSButton otherw; // IBOutlet
    public NSButton otherx; // IBOutlet

    private NSImageView iconImageView;
    public void setIconImageView(NSImageView iconImageView) {
	this.iconImageView = iconImageView;
    }

    private NSWindow window;
    public void setWindow(NSWindow window) {
	this.window = window;
    }
    
    
    // ----------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------
    
    public CDInfoController(Path file) {
	log.debug("CDInfoController:"+file);
	this.file = file;
        if (false == NSApplication.loadNibNamed("Info", this)) {
            log.error("Couldn't load Info.nib");
            return;
        }
	this.init();
    }

    private void init() {
	this.filenameField.setStringValue(file.getName());
	this.groupField.setStringValue(file.attributes.getGroup());
	this.kindField.setStringValue(file.getKind());
	this.modifiedField.setStringValue(file.attributes.getModified());
	this.ownerField.setStringValue(file.attributes.getOwner());
	this.sizeField.setStringValue(file.status.getSizeAsString());

	Permission permission = file.attributes.getPermission();
	boolean[] ownerPerm = permission.getOwnerPermissions();
	boolean[] groupPerm = permission.getGroupPermissions();
	boolean[] otherPerm = permission.getOtherPermissions();
		    //Sets the cell's state to value, which can be NSCell.OnState, NSCell.OffState, or NSCell.MixedState. If necessary, this method also redraws the receiver.
	ownerr.setState(ownerPerm[Permission.READ] ? NSCell.OnState : NSCell.OffState);
	ownerw.setState(ownerPerm[Permission.WRITE] ? NSCell.OnState : NSCell.OffState);
	ownerx.setState(ownerPerm[Permission.EXECUTE] ? NSCell.OnState : NSCell.OffState);
	groupr.setState(groupPerm[Permission.READ] ? NSCell.OnState : NSCell.OffState);
	groupw.setState(groupPerm[Permission.WRITE] ? NSCell.OnState : NSCell.OffState);
	groupx.setState(groupPerm[Permission.EXECUTE] ? NSCell.OnState : NSCell.OffState);
	otherr.setState(otherPerm[Permission.READ] ? NSCell.OnState : NSCell.OffState);
	otherw.setState(otherPerm[Permission.WRITE] ? NSCell.OnState : NSCell.OffState);
	otherx.setState(otherPerm[Permission.EXECUTE] ? NSCell.OnState : NSCell.OffState);

	permissionsBox.setTitle("Permissions | "+permission.getString()+" ("+permission.getCode()+")");

	if(file.isFile()) {
	    this.iconImageView.setImage(NSWorkspace.sharedWorkspace().iconForFileType(file.getExtension()));
	}
	if(file.isDirectory())
	    this.iconImageView.setImage(NSImage.imageNamed("folder.tiff"));

	
//	ObserverList.instance().registerObserver(this);

	(NSNotificationCenter.defaultCenter()).addObserver(
						    this,
						    new NSSelector("textInputDidEndEditing", new Class[]{NSNotification.class}),
						    NSControl.ControlTextDidEndEditingNotification,
						    filenameField);
    }


/*    public void update(Observable o, Object arg) {
	log.debug("update:"+o+","+arg);
	if(o instanceof Path) {
	    if(arg instanceof Message) {
		Message msg = (Message)arg;
		if(msg.getTitle().equals(Message.SELECTION)) {
		    this.file = (Path)o;
		    this.filenameField.setStringValue(file.getName());
		    this.groupField.setStringValue(file.attributes.getGroup());
		    this.kindField.setStringValue(file.getKind());
		    this.modifiedField.setStringValue(file.attributes.getModified());
		    this.ownerField.setStringValue(file.attributes.getOwner());
		    this.sizeField.setStringValue(file.status.getSizeAsString());

		    Permission permission = file.attributes.getPermission();
		    boolean[] ownerPerm = permission.getOwnerPermissions();
		    boolean[] groupPerm = permission.getGroupPermissions();
		    boolean[] otherPerm = permission.getOtherPermissions();
		    //Sets the cell's state to value, which can be NSCell.OnState, NSCell.OffState, or NSCell.MixedState. If necessary, this method also redraws the receiver.
		    ownerr.setState(ownerPerm[Permission.READ] ? NSCell.OnState : NSCell.OffState);
		    ownerw.setState(ownerPerm[Permission.WRITE] ? NSCell.OnState : NSCell.OffState);
		    ownerx.setState(ownerPerm[Permission.EXECUTE] ? NSCell.OnState : NSCell.OffState);
		    groupr.setState(groupPerm[Permission.READ] ? NSCell.OnState : NSCell.OffState);
		    groupw.setState(groupPerm[Permission.WRITE] ? NSCell.OnState : NSCell.OffState);
		    groupx.setState(groupPerm[Permission.EXECUTE] ? NSCell.OnState : NSCell.OffState);
		    otherr.setState(otherPerm[Permission.READ] ? NSCell.OnState : NSCell.OffState);
		    otherw.setState(otherPerm[Permission.WRITE] ? NSCell.OnState : NSCell.OffState);
		    otherx.setState(otherPerm[Permission.EXECUTE] ? NSCell.OnState : NSCell.OffState);

		    permissionsBox.setTitle("Permissions | "+permission.getString()+" ("+permission.getCode()+")");

		    if(file.isFile()) {
			this.iconImageView.setImage(NSWorkspace.sharedWorkspace().iconForFileType(file.getExtension()));
		    }
		    if(file.isDirectory())
			this.iconImageView.setImage(NSImage.imageNamed("folder.tiff"));
		}
	    }
	}
    }
*/


    public void textInputDidEndEditing(NSNotification sender) {
	log.debug("textInputDidEndEditing");
	if(file != null)
	    file.rename(filenameField.stringValue());
    }

    public void permissionsSelectionChanged(Object sender) {
	log.debug("permissionsSelectionChanged");
        boolean[][] p = new boolean[3][3];
	p[Permission.OWNER][Permission.READ] = ownerr.state() == NSCell.OnState;
	p[Permission.OWNER][Permission.WRITE] = ownerw.state() == NSCell.OnState;
	p[Permission.OWNER][Permission.EXECUTE] = ownerx.state() == NSCell.OnState;

	p[Permission.GROUP][Permission.READ] = groupr.state() == NSCell.OnState;
	p[Permission.GROUP][Permission.WRITE] = groupw.state() == NSCell.OnState;
	p[Permission.GROUP][Permission.EXECUTE] = groupx.state() == NSCell.OnState;

	p[Permission.OTHER][Permission.READ] = otherr.state() == NSCell.OnState;
	p[Permission.OTHER][Permission.WRITE] = otherw.state() == NSCell.OnState;
	p[Permission.OTHER][Permission.EXECUTE] = otherx.state() == NSCell.OnState;

	Permission permission = new Permission(p);
	file.attributes.setPermission(permission);
	
	file.changePermissions(permission.getCode());
	permissionsBox.setTitle("Permissions | "+permission.getString()+" ("+permission.getCode()+")");
    }

    public NSWindow window() {
	return this.window;
    }
}
