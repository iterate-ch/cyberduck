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
import ch.cyberduck.ui.cocoa.util.HyperlinkAttributedStringFactory;

import org.apache.commons.lang.StringUtils;
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
        this.webUrlField.setAllowsEditingTextAttributes(true);
        this.webUrlField.setSelectable(true);
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

    public void setSizeButton(NSButton b) {
        this.sizeButton = b;
        this.sizeButton.setTarget(this);
        this.sizeButton.setAction(new NSSelector("calculateSizeButtonClicked", new Class[]{Object.class}));
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

    private NSProgressIndicator s3Progress; // IBOutlet

    public void setS3Progress(final NSProgressIndicator p) {
        this.s3Progress = p;
        this.s3Progress.setDisplayedWhenStopped(false);
        this.s3Progress.setStyle(NSProgressIndicator.ProgressIndicatorSpinningStyle);
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

    private NSButton distributionLoggingButton;

    public void setDistributionLoggingButton(NSButton b) {
        this.distributionLoggingButton = b;
    }

    private NSTextField bucketLocationField;

    public void setBucketLocationField(NSTextField t) {
        this.bucketLocationField = t;
        this.bucketLocationField.setStringValue(
                NSBundle.localizedString("Unknown", "")
        );
    }

    private NSButton bucketLoggingButton;

    public void setBucketLoggingButton(NSButton b) {
        this.bucketLoggingButton = b;
        this.bucketLoggingButton.setAction(new NSSelector("bucketLoggingButtonClicked", new Class[]{Object.class}));
    }

    private NSTextField s3PublicUrlField;

    public void setS3PublicUrlField(NSTextField t) {
        this.s3PublicUrlField = t;
        this.s3PublicUrlField.setAllowsEditingTextAttributes(true);
        this.s3PublicUrlField.setSelectable(true);
        this.s3PublicUrlField.setToolTip(
                "Expires in " + Preferences.instance().getDouble("s3.url.expire.seconds") / 60 / 60 + " hours"
        );
    }

    private NSTextField s3torrentUrlField;

    public void setS3torrentUrlField(NSTextField t) {
        this.s3torrentUrlField = t;
        this.s3torrentUrlField.setAllowsEditingTextAttributes(true);
        this.s3torrentUrlField.setSelectable(true);
    }

    private NSPopUpButton s3CachePopup; //IBOutlet

    public void setS3CachePopup(NSPopUpButton b) {
        this.s3CachePopup = b;
        this.s3CachePopup.removeAllItems();
        this.s3CachePopup.setTarget(this);
        this.s3CachePopup.setAction(new NSSelector("s3CachePopupClicked", new Class[]{NSPopUpButton.class}));
        for(int i = 0; i < this.s3CachePopup.numberOfItems(); i++) {
            this.s3CachePopup.itemAtIndex(i).setState(NSCell.OffState);
        }
        this.s3CachePopup.addItem(NSBundle.localizedString("None", ""));
        this.s3CachePopup.addItem("public,max-age=" + Preferences.instance().getInteger("s3.cache.seconds"));
    }

    /**
     * @param sender
     */
    public void s3CachePopupClicked(final NSPopUpButton sender) {
        if(sender.indexOfSelectedItem() == 0) {
            this.toggleS3Settings(false);
            controller.background(new BrowserBackgroundAction(controller) {
                public void run() {
                    for(Path next : files) {
                        ((S3Path) next).setCacheControl(null);
                    }
                }

                public void cleanup() {
                    toggleS3Settings(true);
                }
            });
        }
        if(sender.indexOfSelectedItem() == 1) {
            final String cache = sender.selectedItem().title();
            this.toggleS3Settings(false);
            controller.background(new BrowserBackgroundAction(controller) {
                public void run() {
                    for(Path next : files) {
                        ((S3Path) next).setCacheControl(cache);
                    }
                }

                public void cleanup() {
                    toggleS3Settings(true);
                }
            });
        }
    }

    private NSTextField distributionCnameField;

    public void setDistributionCnameField(NSTextField t) {
        this.distributionCnameField = t;
    }

    private NSTextField distributionStatusField;

    public void setDistributionStatusField(NSTextField t) {
        this.distributionStatusField = t;
    }

    private NSTextField distributionUrlField;

    public void setDistributionUrlField(NSTextField t) {
        this.distributionUrlField = t;
        this.distributionUrlField.setAllowsEditingTextAttributes(true);
        this.distributionUrlField.setSelectable(true);
    }

    private NSTextField distributionCnameUrlField;

    public void setDistributionCnameUrlField(NSTextField t) {
        this.distributionCnameUrlField = t;
        this.distributionCnameUrlField.setAllowsEditingTextAttributes(true);
        this.distributionCnameUrlField.setSelectable(true);
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

    private NSButton distributionToggle;

    public void setDistributionToggle(NSButton b) {
        this.distributionToggle = b;
    }

    private NSButton permissionToggle;

    public void setPermissionToggle(NSButton t) {
        this.permissionToggle = t;
    }

    private NSButton s3Toggle;

    public void setS3Toggle(NSButton s3Toggle) {
        this.s3Toggle = s3Toggle;
    }

    public void setWindow(NSWindow window) {
        super.setWindow(window);
        this.window.setReleasedWhenClosed(false);
    }

    public void windowWillClose(NSNotification notification) {
        this.window().endEditingForObject(null);

        Preferences.instance().setProperty("info.toggle.permission", permissionToggle.state());
        Preferences.instance().setProperty("info.toggle.distribution", distributionToggle.state());
        Preferences.instance().setProperty("info.toggle.s3", s3Toggle.state());

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

    private final CDWindowListener browserWindowListener = new CDWindowListener() {
        public void windowWillClose() {
            final NSWindow window = window();
            if (null != window) {
                window.close();
            }
        }
    };

    /**
     * @param controller
     * @param files
     */
    private CDInfoController(final CDBrowserController controller, List<Path> files) {
        this.controller = controller;
        this.controller.addListener(browserWindowListener);
        this.loadBundle();
        this.setFiles(files);

        this.setState(permissionToggle, Preferences.instance().getBoolean("info.toggle.permission"));

        final Credentials credentials = controller.getSession().getHost().getCredentials();

        final Path path = files.get(0);
        boolean cloud = path instanceof CloudPath && !credentials.isAnonymousLogin();
        boolean amazon = path instanceof S3Path && !credentials.isAnonymousLogin();

        this.setState(distributionToggle, cloud && Preferences.instance().getBoolean("info.toggle.distribution"));
        distributionToggle.setEnabled(cloud);

        this.setState(s3Toggle, amazon && Preferences.instance().getBoolean("info.toggle.s3"));
        s3Toggle.setEnabled(amazon);
    }

    protected void invalidate() {
        this.controller.removeListener(browserWindowListener);
        super.invalidate();
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
        final int count = this.numberOfFiles();
        if(count > 0) {
            Path file = this.files.get(0);
            this.filenameField.setStringValue(count > 1 ? "(" + NSBundle.localizedString("Multiple files", "") + ")" :
                    file.getName());
            this.filenameField.setEnabled(1 == count && file.isRenameSupported());
            if(file.attributes.isSymbolicLink() && file.getSymbolicLinkPath() != null) {
                this.pathField.setAttributedStringValue(new NSAttributedString(file.getSymbolicLinkPath(),
                        TRUNCATE_MIDDLE_ATTRIBUTES));
            }
            else {
                this.pathField.setAttributedStringValue(new NSAttributedString(file.getParent().getAbsolute(),
                        TRUNCATE_MIDDLE_ATTRIBUTES));
            }
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
                            CDDateFormatter.getLongFormat(file.attributes.getModificationDate()),
                            TRUNCATE_MIDDLE_ATTRIBUTES));
                }
            }
            this.ownerField.setStringValue(count > 1 ? "(" + NSBundle.localizedString("Multiple files", "") + ")" :
                    file.attributes.getOwner());

            this.recursiveCheckbox.setEnabled(true);
            for(Path next : files) {
                if(next.attributes.isFile()) {
                    this.recursiveCheckbox.setState(NSCell.OffState);
                    this.recursiveCheckbox.setEnabled(false);
                    this.sizeButton.setEnabled(false);
                    break;
                }
            }
            this.sizeButton.setEnabled(false);
            for(Path next : files) {
                if(next.attributes.isDirectory()) {
                    this.sizeButton.setEnabled(true);
                    break;
                }
            }

            this.initIcon();
            // Sum of files
            this.initSize();
            // Cloudfront status
            this.initDistribution(file);
            // S3 Bucket attributes
            this.initS3(file);
            // Read HTTP URL
            this.initWebUrl();
            // Read permissions
            this.initPermissions();
        }
    }

    private void initWebUrl() {
        if(this.numberOfFiles() > 1) {
            this.webUrlField.setStringValue("(" + NSBundle.localizedString("Multiple files", "") + ")");
        }
        else {
            controller.background(new BrowserBackgroundAction(controller) {
                String url;

                public void run() {
                    url = files.get(0).toHttpURL();
                }

                public void cleanup() {
                    webUrlField.setAttributedStringValue(
                            HyperlinkAttributedStringFactory.create(
                                    new NSMutableAttributedString(new NSAttributedString(url, TRUNCATE_MIDDLE_ATTRIBUTES)), url)
                    );
                }
            });
        }
    }

    /**
     *
     */
    private void initPermissions() {
        // Clear all rwx checkboxes
        this.initPermissionsCheckboxes();

        // Disable Apply button and start progress indicator
        this.togglePermissionSettings(false);

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
                Permission permission = null;
                for(Path next : files) {
                    permission = next.attributes.getPermission();
                    if(null == permission) {
                        // Clear all rwx checkboxes
                        initPermissionsCheckboxes();

                        togglePermissionSettings(false);
                        permissionProgress.stopAnimation(null);
                        return;
                    }
                    else {
                        updateCheckbox(ownerr, permission.getOwnerPermissions()[Permission.READ]);
                        updateCheckbox(ownerw, permission.getOwnerPermissions()[Permission.WRITE]);
                        updateCheckbox(ownerx, permission.getOwnerPermissions()[Permission.EXECUTE]);

                        if(!(next instanceof CloudPath)) {
                            updateCheckbox(groupr, permission.getGroupPermissions()[Permission.READ]);
                            updateCheckbox(groupw, permission.getGroupPermissions()[Permission.WRITE]);
                            updateCheckbox(groupx, permission.getGroupPermissions()[Permission.EXECUTE]);
                        }

                        updateCheckbox(otherr, permission.getOtherPermissions()[Permission.READ]);
                        updateCheckbox(otherw, permission.getOtherPermissions()[Permission.WRITE]);
                        updateCheckbox(otherx, permission.getOtherPermissions()[Permission.EXECUTE]);
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
                togglePermissionSettings(true);
            }
        });
    }

    /**
     * @param checkbox
     * @param condition
     */
    private void updateCheckbox(NSButton checkbox, boolean condition) {
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
    private void initIcon() {
        if(this.numberOfFiles() > 1) {
            iconImageView.setImage(CDIconCache.instance().iconForName("multipleDocuments", 32));
        }
        else {
            iconImageView.setImage(CDIconCache.instance().iconForPath(files.get(0), 32));
        }
    }

    /**
     * @param file
     */
    private void initDistribution(Path file) {
        final boolean cloud = file instanceof CloudPath;

        this.distributionStatusField.setStringValue(NSBundle.localizedString("Unknown", ""));
        ((NSTextFieldCell) this.distributionCnameField.cell()).setPlaceholderString(NSBundle.localizedString("Unknown", ""));

        distributionStatusButton.setEnabled(cloud);
        distributionApplyButton.setEnabled(cloud);

        distributionUrlField.setStringValue(NSBundle.localizedString("Unknown", ""));
        distributionUrlField.setEnabled(cloud);

        distributionStatusField.setStringValue(NSBundle.localizedString("Unknown", ""));
        distributionStatusField.setEnabled(cloud);

        // Amazon S3 only
        final boolean amazon = file instanceof S3Path;

        distributionCnameField.setStringValue(NSBundle.localizedString("Unknown", ""));
        distributionCnameField.setEnabled(amazon);

        distributionLoggingButton.setEnabled(amazon);

        String servicename = "";
        if(amazon) {
            servicename = NSBundle.localizedString("Amazon CloudFront", "S3", "");
        }
        // Mosso only
        final boolean mosso = file instanceof CFPath;
        if(mosso) {
            servicename = NSBundle.localizedString("Mosso Cloud Files", "Mosso", "");
        }
        distributionEnableButton.setEnabled(cloud);
        distributionEnableButton.setTitle(MessageFormat.format(NSBundle.localizedString("Enable {0} Distribution", "Status", ""),
                servicename));

        if(cloud) {
            distributionStatusButtonClicked(null);
        }
    }

    /**
     * Updates the size field by iterating over all files and
     * rading the cached size value in the attributes of the path
     */
    private void initSize() {
        sizeProgress.startAnimation(null);
        controller.background(new BrowserBackgroundAction(controller) {
            long size = 0;

            public void run() {
                for(Path next : files) {
                    if(-1 == next.attributes.getSize()) {
                        next.readSize();
                    }
                    size += next.attributes.getSize();
                }
            }

            public void cleanup() {
                sizeField.setAttributedStringValue(
                        new NSAttributedString(Status.getSizeAsString(size) + " (" + size + " bytes)",
                                TRUNCATE_MIDDLE_ATTRIBUTES));
                sizeProgress.stopAnimation(null);
            }
        });
    }

    /**
     * Toggle settings before and after update
     *
     * @param enabled
     */
    private void toggleS3Settings(boolean enabled) {
        bucketLoggingButton.setEnabled(enabled);
        s3CachePopup.setEnabled(enabled);
        if(enabled) {
            s3Progress.stopAnimation(null);
        }
        else {
            s3Progress.startAnimation(null);
        }
    }

    /**
     * @param file
     */
    private void initS3(final Path file) {
        // Amazon S3 only
        final Credentials credentials = file.getHost().getCredentials();
        final boolean amazon = file instanceof S3Path && !credentials.isAnonymousLogin();

        bucketLocationField.setStringValue(NSBundle.localizedString("Unknown", ""));
        bucketLocationField.setEnabled(amazon);
        s3CachePopup.setEnabled(amazon && file.attributes.isFile());
        bucketLoggingButton.setToolTip("");
        s3PublicUrlField.setStringValue(NSBundle.localizedString("Unknown", ""));
        s3torrentUrlField.setStringValue(NSBundle.localizedString("Unknown", ""));
        if(amazon) {
            final S3Path s3 = (S3Path) file;
            if(file.attributes.isFile()) {
                if(this.numberOfFiles() > 1) {
                    s3PublicUrlField.setStringValue("(" + NSBundle.localizedString("Multiple files", "") + ")");
                    s3torrentUrlField.setStringValue("(" + NSBundle.localizedString("Multiple files", "") + ")");
                }
                else {
                    final String signedUrl = s3.createSignedUrl();
                    s3PublicUrlField.setAttributedStringValue(
                            HyperlinkAttributedStringFactory.create(
                                    new NSMutableAttributedString(new NSAttributedString(signedUrl, TRUNCATE_MIDDLE_ATTRIBUTES)), signedUrl)
                    );
                    final String torrent = s3.createTorrentUrl();
                    s3torrentUrlField.setAttributedStringValue(
                            HyperlinkAttributedStringFactory.create(
                                    new NSMutableAttributedString(new NSAttributedString(torrent, TRUNCATE_MIDDLE_ATTRIBUTES)), torrent)
                    );
                }
            }
            bucketLoggingButton.setToolTip(
                    s3.getContainerName() + "/" + Preferences.instance().getProperty("s3.logging.prefix")
            );
            this.toggleS3Settings(false);
            controller.background(new BrowserBackgroundAction(controller) {
                String location = null;
                boolean logging;
                Map metadata = null;

                public void run() {
                    location = s3.getLocation();
                    if(null == location) {
                        location = "US";
                    }
                    logging = s3.isLogging();
                    metadata = s3.getMetadata();
                }

                public void cleanup() {
                    bucketLoggingButton.setState(logging ? NSCell.OnState : NSCell.OffState);
                    if(StringUtils.isNotBlank(location)) {
                        bucketLocationField.setStringValue(location);
                    }
                    if(metadata.containsKey(S3Path.METADATA_HEADER_CACHE_CONTROL)) {
                        String cache = (String) metadata.get(S3Path.METADATA_HEADER_CACHE_CONTROL);
                        if(StringUtils.isNotBlank(cache)) {
                            if(s3CachePopup.indexOfItemWithTitle(cache) == -1) {
                                s3CachePopup.addItem(cache);
                            }
                            s3CachePopup.selectItemWithTitle(cache);
                        }
                    }
                    toggleS3Settings(true);
                }
            });
        }
    }

    /**
     * @param sender
     */
    public void bucketLoggingButtonClicked(Object sender) {
        this.toggleS3Settings(false);
        controller.background(new BrowserBackgroundAction(controller) {
            public void run() {
                for(Path next : files) {
                    ((S3Path) next).setLogging(bucketLoggingButton.state() == NSCell.OnState);
                    break;
                }
            }

            public void cleanup() {
                toggleS3Settings(true);
            }
        });
    }


    /**
     * @return
     */
    private int numberOfFiles() {
        return null == files ? 0 : files.size();
    }

    public void filenameInputDidEndEditing(NSNotification sender) {
        if(this.numberOfFiles() == 1) {
            final Path current = files.get(0);
            if(!filenameField.stringValue().equals(current.getName())) {
                if(filenameField.stringValue().indexOf('/') == -1) {
                    final Path renamed = PathFactory.createPath(controller.workdir().getSession(),
                            current.getParent().getAbsolute(), filenameField.stringValue(), current.attributes.getType());
                    controller.renamePath(current, renamed);
                }
                else if(StringUtils.isBlank(filenameField.stringValue())) {
                    filenameField.setStringValue(current.getName());
                    this.initWebUrl();
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
        this.togglePermissionSettings(false);
        permissionProgress.startAnimation(null);
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
                togglePermissionSettings(true);
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
    private void togglePermissionSettings(boolean enabled) {
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
        if(enabled) {
            permissionProgress.stopAnimation(null);
        }
        else {
            permissionProgress.startAnimation(null);
        }
    }

    /**
     * Toggle settings before and after update
     *
     * @param statusEnabled
     */
    private void toggleDistributionSettings(boolean statusEnabled, boolean applyEnabled) {
        distributionStatusButton.setEnabled(statusEnabled);
        distributionEnableButton.setEnabled(applyEnabled);
        distributionApplyButton.setEnabled(applyEnabled);
        distributionLoggingButton.setEnabled(statusEnabled);
        if(statusEnabled) {
            distributionProgress.stopAnimation(null);
        }
        else {
            distributionProgress.startAnimation(null);
        }
    }

    /**
     * @param sender
     */
    public void distributionApplyButtonClicked(final Object sender) {
        this.toggleDistributionSettings(false, false);
        controller.background(new BrowserBackgroundAction(controller) {
            public void run() {
                for(Path next : files) {
                    if(StringUtils.isNotBlank(distributionCnameField.stringValue())) {
                        ((CloudPath) next).writeDistribution(distributionEnableButton.state() == NSCell.OnState,
                                StringUtils.split(distributionCnameField.stringValue()),
                                distributionLoggingButton.state() == NSCell.OnState);
                    }
                    else {
                        ((CloudPath) next).writeDistribution(distributionEnableButton.state() == NSCell.OnState,
                                new String[]{}, distributionLoggingButton.state() == NSCell.OnState);
                    }
                    break;
                }
            }

            public void cleanup() {
                // Refresh the current distribution status
                distributionStatusButtonClicked(sender);
            }
        });
    }

    /**
     * @param sender
     */
    public void distributionStatusButtonClicked(final Object sender) {
        this.toggleDistributionSettings(false, false);
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
                toggleDistributionSettings(true, !distribution.isInprogress());

                distributionEnableButton.setState(distribution.isEnabled() ? NSCell.OnState : NSCell.OffState);
                distributionStatusField.setStringValue(distribution.getStatus());
                distributionLoggingButton.setState(distribution.isLogging() ? NSCell.OnState : NSCell.OffState);
//                distributionLoggingButton.setToolTip(
//                        s3.getContainerName() + "/" + Preferences.instance().getProperty("cloudfront.logging.prefix")
//                );

                final CloudPath file = ((CloudPath) files.get(0));
                distributionLoggingButton.setEnabled(file instanceof S3Path);
                // Concatenate URLs
                final String key = file.isContainer() ? "" : file.encode(file.getKey());
                if(numberOfFiles() > 1) {
                    distributionUrlField.setStringValue("(" + NSBundle.localizedString("Multiple files", "") + ")");
                    distributionCnameUrlField.setStringValue("(" + NSBundle.localizedString("Multiple files", "") + ")");
                }
                else {
                    if(null == distribution.getUrl()) {
                        distributionUrlField.setStringValue(NSBundle.localizedString("Unknown", ""));
                    }
                    else {
                        final String url = distribution.getUrl() + key;
                        distributionUrlField.setAttributedStringValue(HyperlinkAttributedStringFactory.create(new NSMutableAttributedString(
                                new NSAttributedString(url, TRUNCATE_MIDDLE_ATTRIBUTES)), url));
                    }
                }
                final String[] cnames = distribution.getCNAMEs();
                if(0 == cnames.length) {
                    distributionCnameField.setStringValue("");
                    distributionCnameUrlField.setStringValue("");
                }
                else {
                    distributionCnameField.setStringValue(StringUtils.join(cnames, ' '));
                    for(String cname : cnames) {
                        final String url = "http://" + cname + key;
                        distributionCnameUrlField.setAttributedStringValue(
                                HyperlinkAttributedStringFactory.create(
                                        new NSMutableAttributedString(new NSAttributedString(url, TRUNCATE_MIDDLE_ATTRIBUTES)), url)
                        );
                        // We only support one CNAME URL
                        break;
                    }
                }
            }
        });
    }

    /**
     * @param sender
     */
    public void calculateSizeButtonClicked(final Object sender) {
        log.debug("calculateSizeButtonClicked");
        sizeButton.setEnabled(false);
        sizeProgress.startAnimation(null);
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

    public void helpButtonClicked(final NSButton sender) {
        try {
            NSWorkspace.sharedWorkspace().openURL(
                    new java.net.URL(Preferences.instance().getProperty("website.help")
                            + "/howto/s3")
            );
        }
        catch(java.net.MalformedURLException e) {
            log.error(e.getMessage());
        }
    }
}
