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

import com.apple.cocoa.application.*;
import com.apple.cocoa.foundation.*;

import ch.cyberduck.core.*;
import ch.cyberduck.core.cf.CFPath;
import ch.cyberduck.core.cloud.CloudPath;
import ch.cyberduck.core.cloud.Distribution;
import ch.cyberduck.core.s3.S3Path;

import org.apache.log4j.Logger;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @version $Id$
 */
public class CDInfoController extends CDWindowController {
    private static Logger log = Logger.getLogger(CDInfoController.class);

    private List<Path> files;

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

    public void setGroupField(NSTextField t) {
        this.groupField = t;
    }

    private NSTextField kindField; //IBOutlet

    public void setKindField(NSTextField t) {
        this.kindField = t;
    }

//    private NSTextField mimeField; //IBOutlet
//
//    public void setMimeField(NSTextField t) {
//        this.mimeField = t;
//    }

    private NSTextField modifiedField; //IBOutlet

    public void setModifiedField(NSTextField t) {
        this.modifiedField = t;
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

    private NSTextField webUrlField; //IBOutlet

    public void setWebUrlField(NSTextField webUrlField) {
        this.webUrlField = webUrlField;
    }

    private NSTextField permissionsBox; //IBOutlet

    public void setPermissionsBox(NSTextField t) {
        this.permissionsBox = t;
    }

    private NSButton recursiveCheckbox;

    public void setRecursiveCheckbox(NSButton b) {
        this.recursiveCheckbox = b;
        this.recursiveCheckbox.setState(NSCell.OffState);
    }

    private NSButton permissionApplyButton;

    public void setPermissionApplyButton(NSButton b) {
        this.permissionApplyButton = b;
        this.permissionApplyButton.setTarget(this);
        this.permissionApplyButton.setAction(new NSSelector("permissionApplyButtonClicked", new Class[]{Object.class}));
    }

    private NSButton distributionApplyButton;

    public void setDistributionApplyButton(NSButton b) {
        this.distributionApplyButton = b;
        this.distributionApplyButton.setTarget(this);
        this.distributionApplyButton.setAction(new NSSelector("distributionApplyButtonClicked", new Class[]{Object.class}));
    }

    private NSButton distributionStatusButton;

    public void setDistributionStatusButton(NSButton b) {
        this.distributionStatusButton = b;
        this.distributionStatusButton.setTarget(this);
        this.distributionStatusButton.setAction(new NSSelector("distributionStatusButtonClicked", new Class[]{Object.class}));
    }

    private NSButton sizeButton;

    public void setSizeButton(NSButton sizeButton) {
        this.sizeButton = sizeButton;
        this.sizeButton.setTarget(this);
        this.sizeButton.setAction(new NSSelector("sizeButtonClicked", new Class[]{Object.class}));
    }

    private NSProgressIndicator sizeProgress; // IBOutlet

    public void setSizeProgress(final NSProgressIndicator p) {
        this.sizeProgress = p;
        this.sizeProgress.setDisplayedWhenStopped(false);
        this.sizeProgress.setStyle(NSProgressIndicator.ProgressIndicatorSpinningStyle);
    }

    private NSProgressIndicator permissionProgress; // IBOutlet

    public void setPermissionProgress(final NSProgressIndicator p) {
        this.permissionProgress = p;
        this.permissionProgress.setDisplayedWhenStopped(false);
        this.permissionProgress.setStyle(NSProgressIndicator.ProgressIndicatorSpinningStyle);
    }

    private NSProgressIndicator distributionProgress; // IBOutlet

    public void setDistributionProgress(final NSProgressIndicator p) {
        this.distributionProgress = p;
        this.distributionProgress.setDisplayedWhenStopped(false);
        this.distributionProgress.setStyle(NSProgressIndicator.ProgressIndicatorSpinningStyle);
    }

    private NSButton distributionEnableButton;

    public void setDistributionEnableButton(NSButton b) {
        this.distributionEnableButton = b;
    }

    private NSTextField distributionCnameField;

    public void setCloudfrontCnameField(NSTextField t) {
        this.distributionCnameField = t;
        ((NSTextFieldCell) this.distributionCnameField.cell()).setPlaceholderString(
                NSBundle.localizedString("Unknown", "")
        );
    }

    private NSTextField distributionStatusField;

    public void setCloudfrontStatusField(NSTextField t) {
        this.distributionStatusField = t;
        this.distributionStatusField.setStringValue(
                NSBundle.localizedString("Unknown", "")
        );
    }

    private NSTextField distributionUrlField;

    public void setDistributionUrlField(NSTextField t) {
        this.distributionUrlField = t;
        this.distributionUrlField.setStringValue(
                NSBundle.localizedString("Unknown", "")
        );
    }

    private NSTextField distributionCnameUrlField;

    public void setDistributionCnameUrlField(NSTextField t) {
        this.distributionCnameUrlField = t;
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
        this.window().endEditingForObject(null);
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
        private static Map<List<Path>, CDInfoController> open = new HashMap<List<Path>, CDInfoController>();

        public static CDInfoController create(final CDBrowserController controller, final List<Path> files) {
            if(open.containsKey(files)) {
                return open.get(files);
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

    /**
     * @param controller
     * @param files
     */
    private CDInfoController(final CDBrowserController controller, List<Path> files) {
        this.controller = controller;
        this.controller.addListener(new CDWindowListener() {
            public void windowWillClose() {
                final NSWindow window = window();
                if(null != window) {
                    window.close();
                }
            }
        });
        this.loadBundle();
        this.setFiles(files);
    }

    protected String getBundleName() {
        return "Info";
    }

    public void setFiles(List<Path> files) {
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
        this.permissionApplyButton.setEnabled(controller.isConnected());

        final int count = this.numberOfFiles();
        if(count > 0) {
            Path file = this.files.get(0);
            this.filenameField.setStringValue(count > 1 ? "(" + NSBundle.localizedString("Multiple files", "") + ")" :
                    file.getName());
            this.filenameField.setEnabled(1 == count);
            if(file.attributes.isSymbolicLink() && file.getSymbolicLinkPath() != null) {
                this.pathField.setAttributedStringValue(new NSAttributedString(file.getSymbolicLinkPath(),
                        TRUNCATE_MIDDLE_ATTRIBUTES));
            }
            else {
                this.pathField.setAttributedStringValue(new NSAttributedString(file.getParent().getAbsolute(),
                        TRUNCATE_MIDDLE_ATTRIBUTES));
            }
            this.webUrlField.setAttributedStringValue(new NSAttributedString(file.toHttpURL(),
                    TRUNCATE_MIDDLE_ATTRIBUTES));
            this.groupField.setStringValue(count > 1 ? "(" + NSBundle.localizedString("Multiple files", "") + ")" :
                    file.attributes.getGroup());
            if(count > 1) {
                this.kindField.setStringValue("(" + NSBundle.localizedString("Multiple files", "") + ")");
            }
            else {
                this.kindField.setAttributedStringValue(new NSAttributedString(file.kind(),
                        TRUNCATE_MIDDLE_ATTRIBUTES));
            }
            if(count > 1) {
                this.modifiedField.setStringValue("(" + NSBundle.localizedString("Multiple files", "") + ")");
            }
            else {
                if(-1 == file.attributes.getModificationDate()) {
                    this.modifiedField.setAttributedStringValue(new NSAttributedString(
                            NSBundle.localizedString("Unknown", ""),
                            TRUNCATE_MIDDLE_ATTRIBUTES));

                }
                else {
                    this.modifiedField.setAttributedStringValue(new NSAttributedString(
                            CDDateFormatter.getLongFormat(file.attributes.getModificationDate(), file.getHost().getTimezone()),
                            TRUNCATE_MIDDLE_ATTRIBUTES));
                }
            }
            this.ownerField.setStringValue(count > 1 ? "(" + NSBundle.localizedString("Multiple files", "") + ")" :
                    file.attributes.getOwner());

            boolean isDirectorySelected = false;
            for(Path next : files) {
                if(next.attributes.isDirectory()) {
                    isDirectorySelected = true;
                    break;
                }
            }
            this.sizeButton.setEnabled(isDirectorySelected);
            this.recursiveCheckbox.setEnabled(isDirectorySelected);
            if(!isDirectorySelected) {
                this.recursiveCheckbox.setState(NSCell.OffState);
            }

            this.initSize();
            this.initPermissions();
            this.initIcon(count, file);
            this.initDistribution(file);
        }
    }

    /**
     *
     */
    private void initPermissions() {
        // Clear all rwx checkboxes
        this.initPermissionsCheckboxes();

        // Disable Apply button and start progress indicator
        this.enablePermissionSettings(false);
        this.permissionProgress.startAnimation(null);

        controller.background(new BrowserBackgroundAction(controller) {
            public void run() {
                for(Path next : files) {
                    if(this.isCanceled()) {
                        break;
                    }
                    if(null == next.attributes.getPermission()) {
                        // Read permission of every selected path
                        next.readPermission();
                    }
                }
            }

            public void cleanup() {
                try {
                    Permission permission = null;
                    for(Path next : files) {
                        permission = next.attributes.getPermission();
                        if(null == permission) {
                            // Clear all rwx checkboxes
                            initPermissionsCheckboxes();

                            enablePermissionSettings(false);
                            return;
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
                    if(numberOfFiles() > 1) {
                        permissionsBox.setStringValue(NSBundle.localizedString("Permissions", "")
                                + " | " + "(" + NSBundle.localizedString("Multiple files", "") + ")");
                    }
                    else {
                        permissionsBox.setStringValue(NSBundle.localizedString("Permissions", "")
                                + " | " + (null == permission ? NSBundle.localizedString("Unknown", "") : permission.toString()));
                    }
                    enablePermissionSettings(true);
                }
                finally {
                    permissionProgress.stopAnimation(null);
                }
            }

            /**
             *
             * @param checkbox
             * @param condition
             */
            private void updatePermisssionsCheckbox(NSButton checkbox, boolean condition) {
//                if(null == condition) {
//                    checkbox.setEnabled(false);
//                    checkbox.setState(NSCell.OffState);
//                    return;
//                }
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
        });
    }

    /**
     * @param enabled
     */
    private void initPermissionsCheckboxes() {
        ownerr.setState(NSCell.OffState);
        ownerw.setState(NSCell.OffState);
        ownerx.setState(NSCell.OffState);
        groupr.setState(NSCell.OffState);
        groupw.setState(NSCell.OffState);
        groupx.setState(NSCell.OffState);
        otherr.setState(NSCell.OffState);
        otherw.setState(NSCell.OffState);
        otherx.setState(NSCell.OffState);
    }

    /**
     * @param count
     * @param file
     */
    private void initIcon(int count, Path file) {
        NSImage fileIcon;
        if(count > 1) {
            fileIcon = NSImage.imageNamed("multipleDocuments32.tiff");
        }
        else {
            fileIcon = CDIconCache.instance().iconForPath(file, 32);
        }
        this.iconImageView.setImage(fileIcon);
    }

    /**
     * @param file
     */
    private void initDistribution(Path file) {
        final boolean cloud = file instanceof CloudPath;
        this.distributionEnableButton.setEnabled(cloud);
        this.distributionStatusButton.setEnabled(cloud);
        this.distributionApplyButton.setEnabled(cloud);
        this.distributionUrlField.setEnabled(cloud);
        this.distributionStatusField.setEnabled(cloud);
        if(cloud) {
            this.distributionStatusButtonClicked(null);
        }
        // Amazon S3 only
        final boolean amazon = file instanceof S3Path;
        this.distributionCnameField.setEnabled(amazon);
        if(amazon) {
            this.distributionEnableButton.setTitle(
                    NSBundle.localizedString("Enable Amazon CloudFront Distribution", "S3", ""));
        }
        // Mosso only
        final boolean mosso = file instanceof CFPath;
        if(mosso) {
            this.distributionEnableButton.setTitle(
                    NSBundle.localizedString("Enable Mosso Cloud Files Distribution", "Mosso", ""));
        }
    }

    /**
     * Updates the size field by iterating over all files and
     * rading the cached size value in the attributes of the path
     */
    private void initSize() {
        long size = 0;
        for(Path next : files) {
            size += next.attributes.getSize();
        }
        this.sizeField.setAttributedStringValue(
                new NSAttributedString(Status.getSizeAsString(size) + " (" + size + " bytes)",
                        TRUNCATE_MIDDLE_ATTRIBUTES));
    }

    /**
     * @return
     */
    private int numberOfFiles() {
        return null == files ? 0 : files.size();
    }

    public void filenameInputDidEndEditing(NSNotification sender) {
        if(this.numberOfFiles() == 1) {
            final Path current = this.files.get(0);
            if(!this.filenameField.stringValue().equals(current.getName())) {
                if(this.filenameField.stringValue().indexOf('/') == -1) {
                    final Path renamed = PathFactory.createPath(controller.workdir().getSession(),
                            current.getParent().getAbsolute(), this.filenameField.stringValue(), current.attributes.getType());
                    controller.renamePath(current, renamed);
                }
                else if(!StringUtils.hasText(filenameField.stringValue())) {
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

    /**
     * @return
     */
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

    /**
     * @param sender
     */
    public void permissionSelectionChanged(final NSButton sender) {
        if(sender.state() == NSCell.MixedState) {
            sender.setState(NSCell.OnState);
        }
        final Permission permission = this.getPermissionFromSelection();
        permissionsBox.setStringValue(NSBundle.localizedString("Permissions", "") + " | " + permission.toString());
    }

    /**
     * Write altered permissions to the server
     *
     * @param sender
     */
    public void permissionApplyButtonClicked(final Object sender) {
        this.enablePermissionSettings(false);
        this.permissionProgress.startAnimation(null);
        final Permission permission = this.getPermissionFromSelection();
        // send the changes to the remote host
        controller.background(new BrowserBackgroundAction(controller) {
            public void run() {
                for(Path next : files) {
                    next.writePermissions(permission,
                            recursiveCheckbox.state() == NSCell.OnState);
                    if(!controller.isConnected()) {
                        break;
                    }
                    next.getParent().invalidate();
                }
            }

            public void cleanup() {
                controller.reloadData(true);
                enablePermissionSettings(true);
                permissionProgress.stopAnimation(null);
            }

            public String getActivity() {
                return MessageFormat.format(NSBundle.localizedString("Changing permission of {0} to {1}", "Status", ""),
                        files.get(0).getName(), permission);
            }
        });
    }

    /**
     * @param enabled
     */
    private void enablePermissionSettings(boolean enabled) {
        for(Path next : files) {
            if(!next.isWritePermissionsSupported()) {
                enabled = false;
            }
        }
        permissionApplyButton.setEnabled(enabled);
        recursiveCheckbox.setEnabled(enabled);
        ownerr.setEnabled(enabled);
        ownerw.setEnabled(enabled);
        ownerx.setEnabled(enabled);
        groupr.setEnabled(enabled);
        groupw.setEnabled(enabled);
        groupx.setEnabled(enabled);
        otherr.setEnabled(enabled);
        otherw.setEnabled(enabled);
        otherx.setEnabled(enabled);
    }

    /**
     * @param enable
     */
    private void enableDistributionSettings(boolean enable) {
        this.distributionStatusButton.setEnabled(enable);
        this.distributionApplyButton.setEnabled(enable);
        this.distributionEnableButton.setEnabled(enable);
        if(enable) {
            this.distributionProgress.stopAnimation(null);
        }
        else {
            this.distributionProgress.startAnimation(null);
        }
    }

    /**
     * @param sender
     */
    public void distributionApplyButtonClicked(final Object sender) {
        this.enableDistributionSettings(false);
        controller.background(new BrowserBackgroundAction(controller) {
            public void run() {
                for(Path next : files) {
                    if(StringUtils.hasText(distributionCnameField.stringValue())) {
                        ((CloudPath) next).writeDistribution(distributionEnableButton.state() == NSCell.OnState,
                                new String[]{distributionCnameField.stringValue()});
                    }
                    else {
                        ((CloudPath) next).writeDistribution(distributionEnableButton.state() == NSCell.OnState,
                                new String[]{});
                    }
                    break;
                }
            }

            public void cleanup() {
                enableDistributionSettings(true);
                // Refresh the current distribution status
                distributionStatusButtonClicked(sender);
            }
        });
    }

    /**
     * @param sender
     */
    public void distributionStatusButtonClicked(final Object sender) {
        this.enableDistributionSettings(false);
        controller.background(new BrowserBackgroundAction(controller) {
            Distribution distribution;

            public void run() {
                for(Path next : files) {
                    // We only support one distribution per bucket for the sake of simplicity
                    distribution = ((CloudPath) next).readDistribution();
                    break;
                }
            }

            public void cleanup() {
                enableDistributionSettings(true);

                distributionEnableButton.setState(distribution.isEnabled() ? NSCell.OnState : NSCell.OffState);
                distributionStatusField.setStringValue(distribution.getStatus());

                final CloudPath file = ((CloudPath) files.get(0));
                // Concatenate URLs
                final String key = file.encode(file.getKey());
                if(numberOfFiles() > 1) {
                    distributionUrlField.setStringValue("(" + NSBundle.localizedString("Multiple files", "") + ")");
                    distributionCnameUrlField.setStringValue("(" + NSBundle.localizedString("Multiple files", "") + ")");
                }
                else {
                    distributionUrlField.setStringValue(distribution.getUrl() + key);
                }
                final String[] cnames = distribution.getCNAMEs();
                for(String cname : cnames) {
                    distributionCnameField.setStringValue(cname);
                    distributionCnameUrlField.setStringValue("http://" + cname + key);
                    // We only support one CNAME for now
                    break;
                }
                if(0 == cnames.length) {
                    distributionCnameField.setStringValue("");
                    distributionCnameUrlField.setStringValue("");
                }
            }
        });
    }

    /**
     * @param sender
     */
    public void sizeButtonClicked(final Object sender) {
        log.debug("sizeButtonClicked");
        this.sizeButton.setEnabled(false);
        this.sizeProgress.startAnimation(null);
        // send the changes to the remote host
        controller.background(new BrowserBackgroundAction(controller) {
            public void run() {
                for(Path next : files) {
                    this.calculateSize(next);
                    if(!controller.isConnected()) {
                        break;
                    }
                }
            }

            public void cleanup() {
                controller.reloadData(true);
                initSize();
                sizeButton.setEnabled(true);
                sizeProgress.stopAnimation(null);
            }

            /**
             * Calculates recursively the size of this path
             *
             * @return The size of the file or the sum of all containing files if a directory
             * @warn Potentially lengthy operation
             */
            private double calculateSize(AbstractPath p) {
                if(p.attributes.isDirectory()) {
                    long size = 0;
                    for(AbstractPath next : p.childs()) {
                        size += this.calculateSize(next);
                    }
                    p.attributes.setSize(size);
                }
                return p.attributes.getSize();
            }

            public String getActivity() {
                return MessageFormat.format(NSBundle.localizedString("Getting size of {0}", "Status", ""),
                        files.get(0).getName());
            }
        });
    }
}
