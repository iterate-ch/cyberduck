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
import ch.cyberduck.core.PathFactory;
import ch.cyberduck.core.Permission;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.Status;
import ch.cyberduck.ui.cocoa.threading.BackgroundAction;

import com.apple.cocoa.application.*;
import com.apple.cocoa.foundation.*;

import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
        NSNotificationCenter.defaultCenter().addObserver(this,
                new NSSelector("filenameInputDidEndEditing", new Class[]{NSNotification.class}),
                NSControl.ControlTextDidEndEditingNotification,
                filenameField);
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

    private NSButton sizeButton;

    public void setSizeButton(NSButton sizeButton) {
        this.sizeButton = sizeButton;
        this.sizeButton.setTarget(this);
        this.sizeButton.setAction(new NSSelector("sizeButtonClicked", new Class[]{Object.class}));
    }

    private NSProgressIndicator sizeProgress; // IBOutlet

    public void setSizeProgress(final NSProgressIndicator sizeProgress) {
        this.sizeProgress = sizeProgress;
        this.sizeProgress.setDisplayedWhenStopped(false);
        this.sizeProgress.setStyle(NSProgressIndicator.ProgressIndicatorSpinningStyle);
        this.sizeProgress.setUsesThreadedAnimation(true);
    }

    private NSProgressIndicator permissionProgress; // IBOutlet

    public void setPermissionProgress(final NSProgressIndicator permissionProgress) {
        this.permissionProgress = permissionProgress;
        this.permissionProgress.setDisplayedWhenStopped(false);
        this.permissionProgress.setStyle(NSProgressIndicator.ProgressIndicatorSpinningStyle);
        this.permissionProgress.setUsesThreadedAnimation(true);
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
        if(Preferences.instance().getBoolean("browser.info.isInspector")) {
            //Do not mark this controller as invalid if it should be used again 
            return;
        }
        super.windowWillClose(notification);
    }

    // ----------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------

    public static class Factory {
        private static Map open = new HashMap();

        public static CDInfoController create(final CDBrowserController controller, final List files) {
            if(open.containsKey(files)) {
                return (CDInfoController) open.get(files);
            }
            final CDInfoController c = new CDInfoController(controller, files) {
                public void windowWillClose(NSNotification notification) {
                    Factory.open.remove(files);
                    super.windowWillClose(notification);
                }
            };
            open.put(files, c);
            return c;
        }
    }

    private CDBrowserController controller;

    private CDInfoController(final CDBrowserController controller, List files) {
        this.controller = controller;
        synchronized(NSApplication.sharedApplication()) {
            if(!NSApplication.loadNibNamed("Info", this)) {
                log.fatal("Couldn't load Info.nib");
            }
        }
        this.setFiles(files);
    }

    public void setFiles(List files) {
        this.files = files;
        this.init();
    }

    private static NSPoint cascadedWindowPoint;

    public void awakeFromNib() {
        if(null == cascadedWindowPoint) {
            cascadedWindowPoint = this.window.cascadeTopLeftFromPoint(this.window.frame().origin());
        }
        else {
            cascadedWindowPoint = this.window.cascadeTopLeftFromPoint(cascadedWindowPoint);
        }
        this.ownerr.setTarget(this);
        this.ownerr.setAction(new NSSelector("permissionSelectionChanged", new Class[]{Object.class}));
        this.ownerr.setAllowsMixedState(true);
        this.ownerw.setTarget(this);
        this.ownerw.setAction(new NSSelector("permissionSelectionChanged", new Class[]{Object.class}));
        this.ownerw.setAllowsMixedState(true);
        this.ownerx.setTarget(this);
        this.ownerx.setAction(new NSSelector("permissionSelectionChanged", new Class[]{Object.class}));
        this.ownerx.setAllowsMixedState(true);

        this.groupr.setTarget(this);
        this.groupr.setAction(new NSSelector("permissionSelectionChanged", new Class[]{Object.class}));
        this.groupr.setAllowsMixedState(true);
        this.groupw.setTarget(this);
        this.groupw.setAction(new NSSelector("permissionSelectionChanged", new Class[]{Object.class}));
        this.groupw.setAllowsMixedState(true);
        this.groupx.setTarget(this);
        this.groupx.setAction(new NSSelector("permissionSelectionChanged", new Class[]{Object.class}));
        this.groupx.setAllowsMixedState(true);

        this.otherr.setTarget(this);
        this.otherr.setAction(new NSSelector("permissionSelectionChanged", new Class[]{Object.class}));
        this.otherr.setAllowsMixedState(true);
        this.otherw.setTarget(this);
        this.otherw.setAction(new NSSelector("permissionSelectionChanged", new Class[]{Object.class}));
        this.otherw.setAllowsMixedState(true);
        this.otherx.setTarget(this);
        this.otherx.setAction(new NSSelector("permissionSelectionChanged", new Class[]{Object.class}));
        this.otherx.setAllowsMixedState(true);
    }

    private void init() {
        if(this.numberOfFiles() > 0) {
            Path file = (Path) this.files.get(0);
            this.filenameField.setStringValue(this.numberOfFiles() > 1 ? "(" + NSBundle.localizedString("Multiple files", "") + ")" :
                    file.getName());
            if(this.numberOfFiles() > 1) {
                this.filenameField.setEnabled(false);
            }
            else {
                this.filenameField.setEnabled(true);
            }
            if(file.attributes.isSymbolicLink() && file.getSymbolicLinkPath() != null) {
                this.pathField.setAttributedStringValue(new NSAttributedString(file.getSymbolicLinkPath(),
                        TRUNCATE_MIDDLE_ATTRIBUTES));
            }
            else {
                this.pathField.setAttributedStringValue(new NSAttributedString(file.getParent().getAbsolute(),
                        TRUNCATE_MIDDLE_ATTRIBUTES));
            }
            this.groupField.setStringValue(this.numberOfFiles() > 1 ? "(" + NSBundle.localizedString("Multiple files", "") + ")" :
                    file.attributes.getGroup());
            if(this.numberOfFiles() > 1) {
                this.kindField.setStringValue("(" + NSBundle.localizedString("Multiple files", "") + ")");
            }
            else {
                this.kindField.setAttributedStringValue(new NSAttributedString(file.kind(),
                        TRUNCATE_MIDDLE_ATTRIBUTES));
            }
            if(this.numberOfFiles() > 1) {
                this.modifiedField.setStringValue("(" + NSBundle.localizedString("Multiple files", "") + ")");
            }
            else {
                this.modifiedField.setAttributedStringValue(new NSAttributedString(
                        CDDateFormatter.getLongFormat(file.attributes.getModificationDate(), file.getHost().getTimezone()),
                        TRUNCATE_MIDDLE_ATTRIBUTES));
            }
            this.ownerField.setStringValue(this.numberOfFiles() > 1 ? "(" + NSBundle.localizedString("Multiple files", "") + ")" :
                    file.attributes.getOwner());
            this.sizeButton.setEnabled(file.attributes.isDirectory());
            this.updateSize();
            this.initPermissionsCheckbox(false);
            Permission permission = Permission.DEFAULT;
            for(Iterator i = files.iterator(); i.hasNext();) {
                permission = ((Path) i.next()).attributes.getPermission();
                log.debug("Permission:" + permission);
                if(null == permission) {
                    this.initPermissionsCheckbox(false);
                    break;
                }
                else {
                    this.updatePermisssionsCheckbox(ownerr, permission.getOwnerPermissions()[Permission.READ]);
                    this.updatePermisssionsCheckbox(ownerw, permission.getOwnerPermissions()[Permission.WRITE]);
                    this.updatePermisssionsCheckbox(ownerx, permission.getOwnerPermissions()[Permission.EXECUTE]);

                    this.updatePermisssionsCheckbox(groupr, permission.getGroupPermissions()[Permission.READ]);
                    this.updatePermisssionsCheckbox(groupw, permission.getGroupPermissions()[Permission.WRITE]);
                    this.updatePermisssionsCheckbox(groupx, permission.getGroupPermissions()[Permission.EXECUTE]);

                    this.updatePermisssionsCheckbox(otherr, permission.getOtherPermissions()[Permission.READ]);
                    this.updatePermisssionsCheckbox(otherw, permission.getOtherPermissions()[Permission.WRITE]);
                    this.updatePermisssionsCheckbox(otherx, permission.getOtherPermissions()[Permission.EXECUTE]);
                }
            }

            //		octalField.setStringValue(""+file.getOctalCode());
            if(this.numberOfFiles() > 1) {
                this.permissionsBox.setTitle(NSBundle.localizedString("Permissions", "")
                        + " | " + "(" + NSBundle.localizedString("Multiple files", "") + ")");
            }
            else {
                this.permissionsBox.setTitle(NSBundle.localizedString("Permissions", "")
                        + " | " + permission.toString());
            }

            NSImage fileIcon = null;
            if(this.numberOfFiles() > 1) {
                fileIcon = NSImage.imageNamed("multipleDocuments32.tiff");
            }
            else {
                if(file.attributes.isFile()) {
                    fileIcon = NSWorkspace.sharedWorkspace().iconForFileType(file.getExtension());
                    fileIcon.setSize(new NSSize(32f, 32f));
                }
                if(file.attributes.isDirectory()) {
                    fileIcon = NSImage.imageNamed("folder32.tiff");
                }
            }
            this.iconImageView.setImage(fileIcon);
        }
        this.applyButton.setEnabled(controller.isConnected());
    }

    private void initPermissionsCheckbox(boolean enabled) {
        ownerr.setEnabled(enabled);
        ownerr.setState(NSCell.OffState);
        ownerw.setEnabled(enabled);
        ownerw.setState(NSCell.OffState);
        ownerx.setEnabled(enabled);
        ownerx.setState(NSCell.OffState);
        groupr.setEnabled(enabled);
        groupr.setState(NSCell.OffState);
        groupw.setEnabled(enabled);
        groupw.setState(NSCell.OffState);
        groupx.setEnabled(enabled);
        groupx.setState(NSCell.OffState);
        otherr.setEnabled(enabled);
        otherr.setState(NSCell.OffState);
        otherw.setEnabled(enabled);
        otherw.setState(NSCell.OffState);
        otherx.setEnabled(enabled);
        otherx.setState(NSCell.OffState);
    }

    private void updatePermisssionsCheckbox(NSButton checkbox, boolean condition) {
        // Sets the cell's state to value, which can be NSCell.OnState, NSCell.OffState, or NSCell.MixedState.
        // If necessary, this method also redraws the receiver.
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
    }

    private int numberOfFiles() {
        return null == files ? 0 : files.size();
    }

    public void filenameInputDidEndEditing(NSNotification sender) {
        if(this.numberOfFiles() == 1) {
            final Path current = (Path) this.files.get(0);
            if(!this.filenameField.stringValue().equals(current.getName())) {
                if(this.filenameField.stringValue().indexOf('/') == -1) {
                    final Path renamed = PathFactory.createPath(controller.workdir().getSession(),
                            current.getParent().getAbsolute(), this.filenameField.stringValue());
                    controller.renamePath(current, renamed);
                }
                else if(filenameField.stringValue().length() == 0) {
                    this.filenameField.setStringValue(current.getName());
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

    public void permissionSelectionChanged(final NSButton sender) {
        if(sender.state() == NSCell.MixedState) {
            sender.setState(NSCell.OnState);
        }
        final Permission permission = this.getPermissionFromSelection();
        permissionsBox.setTitle(NSBundle.localizedString("Permissions", "") + " | " + permission.toString());
    }

    public void applyButtonClicked(final Object sender) {
        log.debug("applyButtonClicked");
        this.applyButton.setEnabled(false);
        this.permissionProgress.startAnimation(null);
        final Permission permission = this.getPermissionFromSelection();
        // send the changes to the remote host
        controller.background(new BackgroundAction() {
            public void run() {
                for(Iterator i = files.iterator(); i.hasNext();) {
                    ((Path) i.next()).changePermissions(permission,
                            recursiveCheckbox.state() == NSCell.OnState);
                    if(!controller.isConnected()) {
                        break;
                    }
                }
            }

            public void cleanup() {
                controller.reloadData(true);
                applyButton.setEnabled(true);
                permissionProgress.stopAnimation(null);
            }
        });
    }

    public void sizeButtonClicked(final Object sender) {
        log.debug("sizeButtonClicked");
        this.sizeButton.setEnabled(false);
        this.sizeProgress.startAnimation(null);
        // send the changes to the remote host
        controller.background(new BackgroundAction() {
            public void run() {
                for(Iterator i = files.iterator(); i.hasNext();) {
                    Path p = (Path) i.next();
                    p.attributes.setSize(p.size());
                    if(!controller.isConnected()) {
                        break;
                    }
                }
            }

            public void cleanup() {
                controller.reloadData(true);
                updateSize();
                sizeButton.setEnabled(true);
                sizeProgress.stopAnimation(null);
            }
        });
    }

    /**
     * Updates the size field by iterating over all files and
     * rading the cached size value in the attributes of the path
     */
    private void updateSize() {
        int size = 0;
        for(Iterator i = files.iterator(); i.hasNext();) {
            size += ((Path) i.next()).attributes.getSize();
        }
        this.sizeField.setAttributedStringValue(
                new NSAttributedString(Status.getSizeAsString(size) + " (" + size + " bytes)",
                        TRUNCATE_MIDDLE_ATTRIBUTES));
    }
}
