package ch.cyberduck.ui.cocoa;

/*
 *  Copyright (c) 2003 David Kocher. All rights reserved.
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

import org.apache.log4j.Logger;

import ch.cyberduck.core.Path;
import ch.cyberduck.core.Permission;
import ch.cyberduck.core.Status;

/**
 * @version $Id$
 */
public class CDInfoController {
    private static Logger log = Logger.getLogger(CDInfoController.class);

    private Path file;

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

    private static NSMutableArray instances = new NSMutableArray();

    // ----------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------

    public CDInfoController(Path file) {
        this.file = file;
        instances.addObject(this);
        if (false == NSApplication.loadNibNamed("Info", this)) {
            log.fatal("Couldn't load Info.nib");
        }
        this.window().makeKeyAndOrderFront(null);
    }

    public void awakeFromNib() {
        log.debug("awakeFromNib");
        NSPoint origin = this.window.frame().origin();
        this.window.setFrameOrigin(new NSPoint(origin.x() + 16, origin.y() - 16));

        this.filenameField.setStringValue(file.getName());
        this.pathField.setStringValue(file.getParent().getAbsolute());
        this.groupField.setStringValue(file.attributes.getGroup());
        this.kindField.setStringValue(file.getKind());
        this.modifiedField.setStringValue(file.attributes.getTimestampAsString());
        this.ownerField.setStringValue(file.attributes.getOwner());
        this.sizeField.setStringValue(Status.getSizeAsString(file.status.getSize()) + " (" + file.status.getSize() + " bytes)");

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

        permissionsBox.setTitle(NSBundle.localizedString("Permissions", "") + " | " + permission.getString() + " (" + permission.getOctalCode() + ")");

        NSImage fileIcon = null;
        if (file.isFile()) {
            this.iconImageView.setImage(fileIcon = NSWorkspace.sharedWorkspace().iconForFileType(file.getExtension()));
        }
        if (file.isDirectory()) {
            this.iconImageView.setImage(fileIcon = NSImage.imageNamed("folder32.tiff"));
        }

        (NSNotificationCenter.defaultCenter()).addObserver(this,
                new NSSelector("filenameInputDidEndEditing", new Class[]{NSNotification.class}),
                NSControl.ControlTextDidEndEditingNotification,
                filenameField);
        (NSNotificationCenter.defaultCenter()).addObserver(this,
                new NSSelector("ownerInputDidEndEditing", new Class[]{NSNotification.class}),
                NSControl.ControlTextDidEndEditingNotification,
                ownerField);
        (NSNotificationCenter.defaultCenter()).addObserver(this,
                new NSSelector("groupInputDidEndEditing", new Class[]{NSNotification.class}),
                NSControl.ControlTextDidEndEditingNotification,
                groupField);
    }

    public void windowWillClose(NSNotification notification) {
        if (!filenameField.stringValue().equals(file.getName())) {
            file.rename(filenameField.stringValue());
        }
        NSNotificationCenter.defaultCenter().removeObserver(this);
        instances.removeObject(this);
    }


    public void filenameInputDidEndEditing(NSNotification sender) {
        log.debug("textInputDidEndEditing");
        if (!filenameField.stringValue().equals(file.getName())) {
            file.rename(filenameField.stringValue());
        }
    }

    public void ownerInputDidEndEditing(NSNotification sender) {
        log.debug("ownerInputDidEndEditing");
        if (!ownerField.stringValue().equals(file.attributes.getOwner())) {
            file.changeOwner(ownerField.stringValue(), recursiveCheckbox.state() == NSCell.OnState);
        }
    }

    public void groupInputDidEndEditing(NSNotification sender) {
        log.debug("groupInputDidEndEditing");
        if (!groupField.stringValue().equals(file.attributes.getGroup())) {
            file.changeGroup(groupField.stringValue(), recursiveCheckbox.state() == NSCell.OnState);
        }
    }

    public void permissionsSelectionChanged(Object sender) {
        log.debug("permissionsSelectionChanged");
        boolean[][] p = new boolean[3][3];
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

        file.changePermissions(permission, recursiveCheckbox.state() == NSCell.OnState);
        permissionsBox.setTitle(NSBundle.localizedString("Permissions", "") + " | " + permission.getString() + " (" + permission.getOctalCode() + ")");
    }
}
