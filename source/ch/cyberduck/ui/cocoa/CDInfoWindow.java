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
import ch.cyberduck.core.Message;
import ch.cyberduck.core.Permission;

/**
* @version $Id$
 */
public class CDInfoWindow extends NSPanel implements Observer {
    private static Logger log = Logger.getLogger(CDInfoWindow.class);

    public NSTextField filenameField; // IBOutlet
    public NSTextField groupField; // IBOutlet
    public NSTextField kindField; // IBOutlet
    public NSTextField modifiedField; // IBOutlet
    public NSTextField ownerField; // IBOutlet
    public NSTextField sizeField; // IBOutlet
    public NSBox permissionsBox; // IBOutlet
    
    public NSButton ownerr; // IBOutlet
    public NSButton ownerw; // IBOutlet
    public NSButton ownerx; // IBOutlet
    public NSButton groupr; // IBOutlet
    public NSButton groupw; // IBOutlet
    public NSButton groupx; // IBOutlet
    public NSButton otherr; // IBOutlet
    public NSButton otherw; // IBOutlet
    public NSButton otherx; // IBOutlet

    public NSImageView iconImageView;

    private Path selectedPath;

    public CDInfoWindow() {
	super();
	log.debug("CDInfoWindow");
    }

    public CDInfoWindow(NSRect contentRect, int styleMask, int backingType, boolean defer) {
	super(contentRect, styleMask, backingType, defer);
	log.debug("CDInfoWindow");
    }

    public CDInfoWindow(NSRect contentRect, int styleMask, int bufferingType, boolean defer, NSScreen aScreen) {
	super(contentRect, styleMask, bufferingType, defer, aScreen);
	log.debug("CDInfoWindow");
    }

    public void awakeFromNib() {
	log.debug("awakeFromNib");
	(NSNotificationCenter.defaultCenter()).addObserver(
						    this,
						    new NSSelector("textInputDidEndEditing", new Class[]{NSNotification.class}),
						    NSControl.ControlTextDidEndEditingNotification,
						    filenameField);
    }

    public void update(Observable o, Object arg) {
	if(o instanceof Path) {
	    if(arg instanceof Message) {
		Message msg = (Message)arg;
		if(msg.getTitle().equals(Message.SELECTION)) {
		    this.selectedPath = (Path)o;
		    this.filenameField.setStringValue(selectedPath.getName());
		    this.groupField.setStringValue(selectedPath.attributes.getGroup());
		    this.kindField.setStringValue(selectedPath.attributes.getKind());
		    this.modifiedField.setStringValue(selectedPath.attributes.getModified());
		    this.ownerField.setStringValue(selectedPath.attributes.getOwner());
		    this.sizeField.setStringValue(selectedPath.attributes.getSizeAsString());

		    Permission permission = selectedPath.attributes.getPermission();
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

		    if(selectedPath.isFile()) {
			this.iconImageView.setImage(NSWorkspace.sharedWorkspace().iconForFileType(selectedPath.getExtension()));
		    }
		    if(selectedPath.isDirectory())
			this.iconImageView.setImage(NSImage.imageNamed("folder.tiff"));
		}
	    }
	}
    }

    public void textInputDidEndEditing(NSNotification sender) {
	log.debug("textInputDidEndEditing");
	selectedPath.rename(filenameField.stringValue());
    }

    public void permissionsSelectionChanged(NSObject sender) {
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
	selectedPath.attributes.setPermission(permission);
	
	selectedPath.changePermissions(permission.getCode());
	permissionsBox.setTitle("Permissions | "+permission.getString()+" ("+permission.getCode()+")");
    }
}
