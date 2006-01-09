package ch.cyberduck.ui.cocoa;

/*
 *  Copyright (c) 2005 David Kocher. All rights reserved.
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

import ch.cyberduck.core.Path;
import ch.cyberduck.core.Permission;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.Status;

import com.apple.cocoa.application.*;
import com.apple.cocoa.foundation.*;

import org.apache.log4j.Logger;

import java.util.Iterator;
import java.util.List;

/**
 * @version $Id$
 */
public class CDInfoController extends CDWindowController {
    private static Logger log = Logger.getLogger(CDInfoController.class);

    private List files;

    // ----------------------------------------------------------
    // Outlets
    // ----------------------------------------------------------

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

    private NSButton applyButton;

    public void setApplyButton(NSButton applyButton) {
        this.applyButton = applyButton;
        this.applyButton.setTarget(this);
        this.applyButton.setAction(new NSSelector("applyButtonClicked", new Class[]{Object.class}));
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

    public void setWindow(NSWindow window) {
        super.setWindow(window);
        this.window.setReleasedWhenClosed(false);
    }

    public void windowWillClose(NSNotification notification) {
        if (Preferences.instance().getBoolean("browser.info.isInspector")) {
            //Do not mark this controller as invalid if it should be used again 
            return;
        }
        super.windowWillClose(notification);
    }

    // ----------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------

    private CDBrowserController controller;

    public CDInfoController(CDBrowserController controller) {
        this.controller = controller;
        if (!NSApplication.loadNibNamed("Info", this)) {
            log.fatal("Couldn't load Info.nib");
        }
    }

    public void setFiles(List files) {
        this.files = files;
        this.init();
    }

    private static NSPoint cascadedWindowPoint;

    public void awakeFromNib() {
        if (null == cascadedWindowPoint) {
            cascadedWindowPoint = this.window.cascadeTopLeftFromPoint(this.window.frame().origin());
        }
        else {
            cascadedWindowPoint = this.window.cascadeTopLeftFromPoint(cascadedWindowPoint);
        }
        this.ownerr.setTarget(this);
        this.ownerr.setAction(new NSSelector("permissionSelectionChanged", new Class[]{Object.class}));
        this.ownerw.setTarget(this);
        this.ownerw.setAction(new NSSelector("permissionSelectionChanged", new Class[]{Object.class}));
        this.ownerx.setTarget(this);
        this.ownerx.setAction(new NSSelector("permissionSelectionChanged", new Class[]{Object.class}));

        this.groupr.setTarget(this);
        this.groupr.setAction(new NSSelector("permissionSelectionChanged", new Class[]{Object.class}));
        this.groupw.setTarget(this);
        this.groupw.setAction(new NSSelector("permissionSelectionChanged", new Class[]{Object.class}));
        this.groupx.setTarget(this);
        this.groupx.setAction(new NSSelector("permissionSelectionChanged", new Class[]{Object.class}));

        this.otherr.setTarget(this);
        this.otherr.setAction(new NSSelector("permissionSelectionChanged", new Class[]{Object.class}));
        this.otherw.setTarget(this);
        this.otherw.setAction(new NSSelector("permissionSelectionChanged", new Class[]{Object.class}));
        this.otherx.setTarget(this);
        this.otherx.setAction(new NSSelector("permissionSelectionChanged", new Class[]{Object.class}));
    }

    private void init() {
        if (this.numberOfFiles() > 0) {
            Path file = (Path) this.files.get(0);
            this.filenameField.setStringValue(this.numberOfFiles() > 1 ? "(" + NSBundle.localizedString("Multiple files", "") + ")" :
                    file.getName());
            if (this.numberOfFiles() > 1) {
                this.filenameField.setEnabled(false);
            }
            else {
                this.filenameField.setEnabled(true);
            }
            this.pathField.setAttributedStringValue(new NSAttributedString(file.getParent().getAbsolute(),
                    TRUNCATE_MIDDLE_PARAGRAPH_DICTIONARY));
            this.groupField.setStringValue(this.numberOfFiles() > 1 ? "(" + NSBundle.localizedString("Multiple files", "") + ")" :
                    file.attributes.getGroup());
            if (this.numberOfFiles() > 1) {
                this.kindField.setStringValue("(" + NSBundle.localizedString("Multiple files", "") + ")");
            }
            else {
                if (file.attributes.isSymbolicLink()) {
                    if (file.attributes.isFile()) {
                        this.kindField.setStringValue(NSBundle.localizedString("Symbolic Link (File)", ""));
                    }
                    if (file.attributes.isDirectory()) {
                        this.kindField.setStringValue(NSBundle.localizedString("Symbolic Link (Folder)", ""));
                    }
                }
                else if (file.attributes.isFile()) {
                    this.kindField.setStringValue(NSBundle.localizedString("File", ""));
                }
                else if (file.attributes.isDirectory()) {
                    this.kindField.setStringValue(NSBundle.localizedString("Folder", ""));
                }
                else {
                    this.kindField.setStringValue(NSBundle.localizedString("Unknown", ""));
                }
            }

            try {
                if (this.numberOfFiles() > 1) {
                    this.modifiedField.setStringValue("(" + NSBundle.localizedString("Multiple files", "") + ")");
                }
                else {
                    NSGregorianDateFormatter formatter = new NSGregorianDateFormatter((String) NSUserDefaults.standardUserDefaults().objectForKey(NSUserDefaults.TimeDateFormatString), false);
                    String timestamp = formatter.stringForObjectValue(new NSGregorianDate((double) file.attributes.getTimestamp().getTime() / 1000,
                            NSDate.DateFor1970));
                    this.modifiedField.setAttributedStringValue(new NSAttributedString(timestamp,
                            TRUNCATE_MIDDLE_PARAGRAPH_DICTIONARY));
                }
            }
            catch (NSFormatter.FormattingException e) {
                log.error(e.toString());
            }
            this.ownerField.setStringValue(this.numberOfFiles() > 1 ? "(" + NSBundle.localizedString("Multiple files", "") + ")" :
                    file.attributes.getOwner());
            int size = 0;
            for (Iterator i = files.iterator(); i.hasNext();) {
                size += ((Path) i.next()).attributes.getSize();
            }
            this.sizeField.setAttributedStringValue(new NSAttributedString(Status.getSizeAsString(size) + " (" + size + " bytes)",
                    TRUNCATE_MIDDLE_PARAGRAPH_DICTIONARY));
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
            for (Iterator i = files.iterator(); i.hasNext();) {
                permission = ((Path) i.next()).attributes.getPermission();
                log.debug("Permission:" + permission);
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
            if (this.numberOfFiles() > 1) {
                this.permissionsBox.setTitle(NSBundle.localizedString("Permissions", "") + " | " + "(" + NSBundle.localizedString("Multiple files", "") + ")");
            }
            else {
                this.permissionsBox.setTitle(NSBundle.localizedString("Permissions", "") + " | " + permission.toString());
            }

            NSImage fileIcon = null;
            if (this.numberOfFiles() > 1) {
                fileIcon = NSImage.imageNamed("multipleDocuments32.tiff");
            }
            else {
                if (file.attributes.isFile()) {
                    fileIcon = NSWorkspace.sharedWorkspace().iconForFileType(file.getExtension());
                    fileIcon.setSize(new NSSize(32f, 32f));
                }
                if (file.attributes.isDirectory()) {
                    fileIcon = NSImage.imageNamed("folder32.tiff");
                }
            }
            this.iconImageView.setImage(fileIcon);
            (NSNotificationCenter.defaultCenter()).addObserver(this,
                    new NSSelector("filenameInputDidEndEditing", new Class[]{NSNotification.class}),
                    NSControl.ControlTextDidEndEditingNotification,
                    filenameField);

        }
    }

    private void update(NSButton checkbox, boolean condition) {
        // Sets the cell's state to value, which can be NSCell.OnState, NSCell.OffState, or NSCell.MixedState.
        // If necessary, this method also redraws the receiver.
        if ((checkbox.state() == NSCell.OffState || !checkbox.isEnabled()) && !condition) {
            checkbox.setState(NSCell.OffState);
        }
        else if ((checkbox.state() == NSCell.OnState || !checkbox.isEnabled()) && condition) {
            checkbox.setState(NSCell.OnState);
        }
        else {
            checkbox.setState(NSCell.MixedState);
        }
        checkbox.setEnabled(true);
    }

    private int numberOfFiles() {
        return files.size();
    }

    public void filenameInputDidEndEditing(NSNotification sender) {
        if (this.numberOfFiles() == 1) {
            final Path file = (Path) this.files.get(0);
            if (!this.filenameField.stringValue().equals(file.getName())) {
                if (this.filenameField.stringValue().indexOf('/') == -1) {
                    controller.renamePath(file, file.getParent(), this.filenameField.stringValue());
                    file.getParent().invalidate();
                    controller.reloadData(true);
                }
                else if (filenameField.stringValue().length() == 0) {
                    this.filenameField.setStringValue(file.getName());
                }
                else {
                    this.alert(NSAlertPanel.informationalAlertPanel(
                            NSBundle.localizedString("Error", "Alert sheet title"),
                            NSBundle.localizedString("Invalid character in filename.", ""), // message
                            NSBundle.localizedString("OK", "Alert default button"), // defaultbutton
                            null, //alternative button
                            null //other button
                    ));
                }
            }
        }
    }

    private Permission getPermissionFromSelection() {
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

        return new Permission(p);
    }

    public void permissionSelectionChanged(NSButton sender) {
        if (sender.state() == NSCell.MixedState) {
            sender.setState(NSCell.OnState);
        }
        final Permission permission = this.getPermissionFromSelection();
        permissionsBox.setTitle(NSBundle.localizedString("Permissions", "") + " | " + permission.toString());
    }

    public void applyButtonClicked(Object sender) {
        log.debug("applyButtonClicked");
        final Permission permission = this.getPermissionFromSelection();
        // send the changes to the remote host
        Path f = null;
        for (Iterator i = files.iterator(); i.hasNext();) {
            f = (Path) i.next();
            f.changePermissions(permission,
                    this.recursiveCheckbox.state() == NSCell.OnState);
        }
        controller.reloadData(true);
    }
}
