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

import ch.cyberduck.core.*;
import ch.cyberduck.core.cf.CFPath;
import ch.cyberduck.core.cloud.CloudPath;
import ch.cyberduck.core.cloud.CloudSession;
import ch.cyberduck.core.cloud.Distribution;
import ch.cyberduck.core.i18n.Locale;
import ch.cyberduck.core.s3.S3Path;
import ch.cyberduck.ui.cocoa.application.*;
import ch.cyberduck.ui.cocoa.foundation.*;
import ch.cyberduck.ui.cocoa.threading.BrowserBackgroundAction;
import ch.cyberduck.ui.cocoa.util.HyperlinkAttributedStringFactory;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.rococoa.Foundation;
import org.rococoa.ID;
import org.rococoa.Rococoa;
import org.rococoa.cocoa.foundation.NSPoint;
import org.rococoa.cocoa.foundation.NSSize;
import org.rococoa.cocoa.foundation.NSUInteger;

import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.*;

/**
 * @version $Id$
 */
public class InfoController extends ToolbarWindowController {
    private static Logger log = Logger.getLogger(InfoController.class);

    private List<Path> files = Collections.emptyList();

    @Outlet
    private NSTextField filenameField;

    public void setFilenameField(NSTextField filenameField) {
        this.filenameField = filenameField;
        NSNotificationCenter.defaultCenter().addObserver(this.id(),
                Foundation.selector("filenameInputDidEndEditing:"),
                NSControl.NSControlTextDidEndEditingNotification,
                filenameField);
    }

    @Outlet
    private NSTextField groupField;

    public void setGroupField(NSTextField t) {
        this.groupField = t;
    }

    @Outlet
    private NSTextField kindField;

    public void setKindField(NSTextField t) {
        this.kindField = t;
    }

    @Outlet
    private NSTextField modifiedField;

    public void setModifiedField(NSTextField t) {
        this.modifiedField = t;
    }

    @Outlet
    private NSTextField permissionsField;

    public void setPermissionsField(NSTextField permissionsField) {
        this.permissionsField = permissionsField;
    }

    @Outlet
    private NSTextField octalField;

    public void setOctalField(NSTextField octalField) {
        this.octalField = octalField;
        NSNotificationCenter.defaultCenter().addObserver(this.id(),
                Foundation.selector("octalPermissionsInputDidEndEditing:"),
                NSControl.NSControlTextDidEndEditingNotification,
                octalField);
    }

    @Outlet
    private NSTextField ownerField;

    public void setOwnerField(NSTextField ownerField) {
        this.ownerField = ownerField;
    }

    @Outlet
    private NSTextField sizeField;

    public void setSizeField(NSTextField sizeField) {
        this.sizeField = sizeField;
    }

    @Outlet
    private NSTextField checksumField;

    public void setChecksumField(NSTextField checksumField) {
        this.checksumField = checksumField;
    }

    @Outlet
    private NSTextField pathField;

    public void setPathField(NSTextField pathField) {
        this.pathField = pathField;
    }

    @Outlet
    private NSTextField webUrlField;

    public void setWebUrlField(NSTextField webUrlField) {
        this.webUrlField = webUrlField;
        this.webUrlField.setAllowsEditingTextAttributes(true);
        this.webUrlField.setSelectable(true);
    }

    @Outlet
    private NSButton recursiveCheckbox;

    public void setRecursiveCheckbox(NSButton b) {
        this.recursiveCheckbox = b;
        this.recursiveCheckbox.setState(NSCell.NSOffState);
        this.recursiveCheckbox.setTarget(this.id());
        this.recursiveCheckbox.setAction(Foundation.selector("permissionSelectionChanged:"));
    }

    @Outlet
    private NSButton sizeButton;

    public void setSizeButton(NSButton b) {
        this.sizeButton = b;
        this.sizeButton.setTarget(this.id());
        this.sizeButton.setAction(Foundation.selector("calculateSizeButtonClicked:"));
    }

    @Outlet
    private NSProgressIndicator sizeProgress;

    public void setSizeProgress(final NSProgressIndicator p) {
        this.sizeProgress = p;
        this.sizeProgress.setDisplayedWhenStopped(false);
        this.sizeProgress.setStyle(NSProgressIndicator.NSProgressIndicatorSpinningStyle);
    }

    @Outlet
    private NSProgressIndicator permissionProgress;

    public void setPermissionProgress(final NSProgressIndicator p) {
        this.permissionProgress = p;
        this.permissionProgress.setDisplayedWhenStopped(false);
        this.permissionProgress.setStyle(NSProgressIndicator.NSProgressIndicatorSpinningStyle);
    }

    @Outlet
    private NSProgressIndicator s3Progress;

    public void setS3Progress(final NSProgressIndicator p) {
        this.s3Progress = p;
        this.s3Progress.setDisplayedWhenStopped(false);
        this.s3Progress.setStyle(NSProgressIndicator.NSProgressIndicatorSpinningStyle);
    }

    @Outlet
    private NSProgressIndicator distributionProgress;

    public void setDistributionProgress(final NSProgressIndicator p) {
        this.distributionProgress = p;
        this.distributionProgress.setDisplayedWhenStopped(false);
        this.distributionProgress.setStyle(NSProgressIndicator.NSProgressIndicatorSpinningStyle);
    }

    @Outlet
    private NSButton distributionEnableButton;

    public void setDistributionEnableButton(NSButton b) {
        this.distributionEnableButton = b;
        this.distributionEnableButton.setTarget(this.id());
        this.distributionEnableButton.setAction(Foundation.selector("distributionApplyButtonClicked:"));
    }

    @Outlet
    private NSButton distributionLoggingButton;

    public void setDistributionLoggingButton(NSButton b) {
        this.distributionLoggingButton = b;
        this.distributionLoggingButton.setTarget(this.id());
        this.distributionLoggingButton.setAction(Foundation.selector("distributionApplyButtonClicked:"));
    }

    @Outlet
    private NSPopUpButton distributionDeliveryPopup;

    public void setDistributionDeliveryPopup(NSPopUpButton b) {
        this.distributionDeliveryPopup = b;
        this.distributionDeliveryPopup.setTarget(this.id());
        this.distributionDeliveryPopup.setAction(Foundation.selector("distributionStatusButtonClicked:"));
    }

    @Outlet
    private NSTextField bucketLocationField;

    public void setBucketLocationField(NSTextField t) {
        this.bucketLocationField = t;
        this.bucketLocationField.setStringValue(
                Locale.localizedString("Unknown")
        );
    }

    @Outlet
    private NSButton bucketLoggingButton;

    public void setBucketLoggingButton(NSButton b) {
        this.bucketLoggingButton = b;
        this.bucketLoggingButton.setAction(Foundation.selector("bucketLoggingButtonClicked:"));
    }

    @Outlet
    private NSTextField s3PublicUrlField;

    public void setS3PublicUrlField(NSTextField t) {
        this.s3PublicUrlField = t;
        this.s3PublicUrlField.setAllowsEditingTextAttributes(true);
        this.s3PublicUrlField.setSelectable(true);
        this.s3PublicUrlField.setToolTip(
                "Expires in " + Preferences.instance().getDouble("s3.url.expire.seconds") / 60 / 60 + " hours"
        );
    }

    @Outlet
    private NSTextField s3torrentUrlField;

    public void setS3torrentUrlField(NSTextField t) {
        this.s3torrentUrlField = t;
        this.s3torrentUrlField.setAllowsEditingTextAttributes(true);
        this.s3torrentUrlField.setSelectable(true);
    }

    @Outlet
    private NSPopUpButton s3CachePopup;

    public void setS3CachePopup(NSPopUpButton b) {
        this.s3CachePopup = b;
        this.s3CachePopup.removeAllItems();
        this.s3CachePopup.setTarget(this.id());
        this.s3CachePopup.setAction(Foundation.selector("s3CachePopupClicked:"));
        this.s3CachePopup.addItemWithTitle(Locale.localizedString("None"));
        this.s3CachePopup.addItemWithTitle("public,max-age=" + Preferences.instance().getInteger("s3.cache.seconds"));
    }

    @Action
    public void s3CachePopupClicked(final NSPopUpButton sender) {
        if(sender.indexOfSelectedItem().intValue() == 0) {
            this.toggleS3Settings(false);
            controller.background(new BrowserBackgroundAction(controller) {
                public void run() {
                    for(Path next : files) {
                        ((S3Path) next).setCacheControl(null);
                    }
                }

                @Override
                public void cleanup() {
                    toggleS3Settings(true);
                }
            });
        }
        if(sender.indexOfSelectedItem().intValue() == 1) {
            final String cache = sender.selectedItem().title();
            this.toggleS3Settings(false);
            controller.background(new BrowserBackgroundAction(controller) {
                public void run() {
                    for(Path next : files) {
                        ((S3Path) next).setCacheControl(cache);
                    }
                }

                @Override
                public void cleanup() {
                    toggleS3Settings(true);
                }
            });
        }
    }

    @Outlet
    private NSTextField distributionCnameField;

    public void setDistributionCnameField(NSTextField t) {
        this.distributionCnameField = t;
    }

    @Outlet
    private NSTextField distributionStatusField;

    public void setDistributionStatusField(NSTextField t) {
        this.distributionStatusField = t;
    }

    @Outlet
    private NSTextField distributionUrlField;

    public void setDistributionUrlField(NSTextField t) {
        this.distributionUrlField = t;
        this.distributionUrlField.setAllowsEditingTextAttributes(true);
        this.distributionUrlField.setSelectable(true);
    }

    @Outlet
    private NSTextField distributionCnameUrlField;

    public void setDistributionCnameUrlField(NSTextField t) {
        this.distributionCnameUrlField = t;
        this.distributionCnameUrlField.setAllowsEditingTextAttributes(true);
        this.distributionCnameUrlField.setSelectable(true);
    }

    @Outlet
    public NSButton ownerr;
    @Outlet
    public NSButton ownerw;
    @Outlet
    public NSButton ownerx;
    @Outlet
    public NSButton groupr;
    @Outlet
    public NSButton groupw;
    @Outlet
    public NSButton groupx;
    @Outlet
    public NSButton otherr;
    @Outlet
    public NSButton otherw;
    @Outlet
    public NSButton otherx;

    public void setOwnerr(NSButton ownerr) {
        this.ownerr = ownerr;
    }

    public void setOwnerw(NSButton ownerw) {
        this.ownerw = ownerw;
    }

    public void setOwnerx(NSButton ownerx) {
        this.ownerx = ownerx;
    }

    public void setGroupr(NSButton groupr) {
        this.groupr = groupr;
    }

    public void setGroupw(NSButton groupw) {
        this.groupw = groupw;
    }

    public void setGroupx(NSButton groupx) {
        this.groupx = groupx;
    }

    public void setOtherr(NSButton otherr) {
        this.otherr = otherr;
    }

    public void setOtherw(NSButton otherw) {
        this.otherw = otherw;
    }

    public void setOtherx(NSButton otherx) {
        this.otherx = otherx;
    }

    private NSImageView iconImageView;

    public void setIconImageView(NSImageView iconImageView) {
        this.iconImageView = iconImageView;
    }

    private String title;

    @Override
    public void setWindow(NSWindow window) {
        title = window.title();
        window.setShowsResizeIndicator(true);
        window.setMaxSize(new NSSize(500, window.maxSize().height.doubleValue()));
        super.setWindow(window);
    }

    @Override
    public void windowWillClose(NSNotification notification) {
        this.window().endEditingFor(null);
        super.windowWillClose(notification);
    }

    @Override
    public boolean isSingleton() {
        return Preferences.instance().getBoolean("browser.info.isInspector");
    }

    public static class Factory {
        private static Map<List<Path>, InfoController> open = new HashMap<List<Path>, InfoController>();

        public static InfoController create(final BrowserController controller, final List<Path> files) {
            if(open.containsKey(files)) {
                return open.get(files);
            }
            final InfoController c = new InfoController(controller, files) {
                @Override
                public void windowWillClose(NSNotification notification) {
                    Factory.open.remove(files);
                    super.windowWillClose(notification);
                }
            };
            controller.getSession().addConnectionListener(new ConnectionAdapter() {
                @Override
                public void connectionDidClose() {
                    c.window().close();
                    controller.getSession().removeConnectionListener(this);
                }
            });
            open.put(files, c);
            return c;
        }
    }

    private BrowserController controller;

    private final WindowListener browserWindowListener = new WindowListener() {
        public void windowWillClose() {
            final NSWindow window = window();
            if(null != window) {
                window.close();
            }
        }
    };

    /**
     * @param controller
     * @param files
     */
    private InfoController(final BrowserController controller, List<Path> files) {
        this.controller = controller;
        this.controller.addListener(browserWindowListener);
        this.loadBundle();
        this.setFiles(files);
    }

    @Override
    public boolean validateToolbarItem(final NSToolbarItem item) {
        final String itemIdentifier = item.itemIdentifier();
        log.debug("validateToolbarItem:" + itemIdentifier);
        if(itemIdentifier.equals("cloud")) {
            item.setImage(IconCache.iconNamed(controller.getSession().getHost().getProtocol().disk(), 32));
            for(Path path : files) {
                return path instanceof CloudPath && !controller.getSession().getHost().getCredentials().isAnonymousLogin();
            }
            return false;
        }
        if(itemIdentifier.equals("s3")) {
            for(Path path : files) {
                return path instanceof S3Path && !controller.getSession().getHost().getCredentials().isAnonymousLogin();
            }
            return false;
        }
        return super.validateToolbarItem(item);
    }

    @Override
    public String getTitle(NSTabViewItem item) {
        return item.label() + " – " + this.getName();
    }

    @Outlet
    private NSView panelGeneral;
    @Outlet
    private NSView panelPermissions;
    @Outlet
    private NSView panelDistribution;
    @Outlet
    private NSView panelAmazon;

    public void setPanelAmazon(NSView panelAmazon) {
        this.panelAmazon = panelAmazon;
    }

    public void setPanelDistribution(NSView panelDistribution) {
        this.panelDistribution = panelDistribution;
    }

    public void setPanelPermissions(NSView panelPermissions) {
        this.panelPermissions = panelPermissions;
    }

    public void setPanelGeneral(NSView panelGeneral) {
        this.panelGeneral = panelGeneral;
    }

    @Override
    protected void invalidate() {
        this.controller.removeListener(browserWindowListener);
        super.invalidate();
    }

    @Override
    protected String getBundleName() {
        return "Info";
    }

    public void setFiles(List<Path> files) {
        this.files = files;
        this.init();
    }

    private static NSPoint cascadedWindowPoint;

    @Override
    protected void cascade() {
        if(null == cascadedWindowPoint) {
            cascadedWindowPoint = this.window.cascadeTopLeftFromPoint(this.window.frame().origin);
        }
        else {
            cascadedWindowPoint = this.window.cascadeTopLeftFromPoint(cascadedWindowPoint);
        }
    }

    @Override
    public void awakeFromNib() {
        this.cascade();

        this.ownerr.setTarget(this.id());
        this.ownerr.setAction(Foundation.selector("permissionSelectionChanged:"));
        this.ownerr.setAllowsMixedState(true);
        this.ownerw.setTarget(this.id());
        this.ownerw.setAction(Foundation.selector("permissionSelectionChanged:"));
        this.ownerw.setAllowsMixedState(true);
        this.ownerx.setTarget(this.id());
        this.ownerx.setAction(Foundation.selector("permissionSelectionChanged:"));
        this.ownerx.setAllowsMixedState(true);

        this.groupr.setTarget(this.id());
        this.groupr.setAction(Foundation.selector("permissionSelectionChanged:"));
        this.groupr.setAllowsMixedState(true);
        this.groupw.setTarget(this.id());
        this.groupw.setAction(Foundation.selector("permissionSelectionChanged:"));
        this.groupw.setAllowsMixedState(true);
        this.groupx.setTarget(this.id());
        this.groupx.setAction(Foundation.selector("permissionSelectionChanged:"));
        this.groupx.setAllowsMixedState(true);

        this.otherr.setTarget(this.id());
        this.otherr.setAction(Foundation.selector("permissionSelectionChanged:"));
        this.otherr.setAllowsMixedState(true);
        this.otherw.setTarget(this.id());
        this.otherw.setAction(Foundation.selector("permissionSelectionChanged:"));
        this.otherw.setAllowsMixedState(true);
        this.otherx.setTarget(this.id());
        this.otherx.setAction(Foundation.selector("permissionSelectionChanged:"));
        this.otherx.setAllowsMixedState(true);

        super.awakeFromNib();
    }

    @Override
    protected List<NSView> getPanels() {
        return Arrays.asList(panelGeneral, panelPermissions, panelDistribution, panelAmazon);
    }

    private String getName() {
        final int count = this.numberOfFiles();
        if(count > 0) {
            Path file = this.files.get(0);
            if(count > 1) {
                return "(" + Locale.localizedString("Multiple files") + ")";
            }
            for(Path next : files) {
                return next.getName();
            }
        }
        return null;
    }

    @Override
    protected NSUInteger getToolbarSize() {
        return NSToolbar.NSToolbarSizeModeSmall;
    }

    private void init() {
        final int count = this.numberOfFiles();
        if(count > 0) {
            Path file = this.files.get(0);
            final String filename = this.getName();
            filenameField.setStringValue(filename);
            this.window().setTitle(title + " – " + filename);
            filenameField.setEnabled(1 == count && file.isRenameSupported());
            String path;
            if(file.attributes.isSymbolicLink() && file.getSymlinkTarget() != null) {
                path = file.getSymlinkTarget();
            }
            else {
                path = file.getParent().getAbsolute();
            }
            this.updateField(pathField, path, TRUNCATE_MIDDLE_ATTRIBUTES);
            pathField.setToolTip(path);
            groupField.setStringValue(count > 1 ? "(" + Locale.localizedString("Multiple files") + ")" :
                    file.attributes.getGroup());
            if(count > 1) {
                kindField.setStringValue("(" + Locale.localizedString("Multiple files") + ")");
                checksumField.setStringValue("(" + Locale.localizedString("Multiple files") + ")");
            }
            else {
                this.updateField(kindField, file.kind(), TRUNCATE_MIDDLE_ATTRIBUTES);
            }
            if(count > 1) {
                modifiedField.setStringValue("(" + Locale.localizedString("Multiple files") + ")");
            }
            else {
                if(-1 == file.attributes.getModificationDate()) {
                    this.updateField(modifiedField, Locale.localizedString("Unknown"));
                }
                else {
                    this.updateField(modifiedField, DateFormatter.getLongFormat(file.attributes.getModificationDate()), TRUNCATE_MIDDLE_ATTRIBUTES);
                }
            }
            this.updateField(ownerField, count > 1 ? "(" + Locale.localizedString("Multiple files") + ")" :
                    file.attributes.getOwner(), TRUNCATE_MIDDLE_ATTRIBUTES);

            this.initIcon();
            // Sum of files
            this.initSize();
            this.initChecksum(file);
            // Cloudfront status
            this.initDistribution(file);
            // S3 Bucket attributes
            this.initS3(file);
            // Read HTTP URL
            this.initWebUrl();

            // Clear all rwx checkboxes
            ownerr.setState(NSCell.NSOffState);
            ownerw.setState(NSCell.NSOffState);
            ownerx.setState(NSCell.NSOffState);
            groupr.setState(NSCell.NSOffState);
            groupw.setState(NSCell.NSOffState);
            groupx.setState(NSCell.NSOffState);
            otherr.setState(NSCell.NSOffState);
            otherw.setState(NSCell.NSOffState);
            otherx.setState(NSCell.NSOffState);

            // Read permissions
            this.initPermissions();
        }
    }

    private void initWebUrl() {
        if(this.numberOfFiles() > 1) {
            webUrlField.setStringValue("(" + Locale.localizedString("Multiple files") + ")");
            webUrlField.setToolTip("");
        }
        else {
            controller.background(new BrowserBackgroundAction(controller) {
                String url;

                public void run() {
                    for(Path next : files) {
                        url = next.toHttpURL();
                    }
                }

                @Override
                public void cleanup() {
                    webUrlField.setAttributedStringValue(
                            HyperlinkAttributedStringFactory.create(
                                    NSMutableAttributedString.create(url, TRUNCATE_MIDDLE_ATTRIBUTES), url)
                    );
                    webUrlField.setToolTip(url);
                }
            });
        }
    }

    /**
     *
     */
    private void initPermissions() {
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

            @Override
            public void cleanup() {
                Permission permission = null;
                for(Path next : files) {
                    permission = next.attributes.getPermission();
                    if(null != permission) {
                        updateCheckbox(ownerr, permission.getOwnerPermissions()[Permission.READ]);
                        updateCheckbox(ownerw, permission.getOwnerPermissions()[Permission.WRITE]);
                        updateCheckbox(ownerx, permission.getOwnerPermissions()[Permission.EXECUTE]);

                        updateCheckbox(groupr, permission.getGroupPermissions()[Permission.READ]);
                        updateCheckbox(groupw, permission.getGroupPermissions()[Permission.WRITE]);
                        updateCheckbox(groupx, permission.getGroupPermissions()[Permission.EXECUTE]);

                        updateCheckbox(otherr, permission.getOtherPermissions()[Permission.READ]);
                        updateCheckbox(otherw, permission.getOtherPermissions()[Permission.WRITE]);
                        updateCheckbox(otherx, permission.getOtherPermissions()[Permission.EXECUTE]);
                    }
                }
                octalField.setStringValue(null == permission ? Locale.localizedString("Unknown") : permission.getOctalString());
                final int count = numberOfFiles();
                if(count > 1) {
                    permissionsField.setStringValue("(" + Locale.localizedString("Multiple files") + ")");
                }
                else {
                    permissionsField.setStringValue(null == permission ? Locale.localizedString("Unknown") : permission.toString());
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
        // Sets the cell's state to value, which can be NSCell.NSOnState, NSCell.NSOffState, or NSCell.MixedState.
        // If necessary, this method also redraws the receiver.
        if((checkbox.state() == NSCell.NSOffState || !checkbox.isEnabled()) && !condition) {
            checkbox.setState(NSCell.NSOffState);
        }
        else if((checkbox.state() == NSCell.NSOnState || !checkbox.isEnabled()) && condition) {
            checkbox.setState(NSCell.NSOnState);
        }
        else {
            checkbox.setState(NSCell.NSMixedState);
        }
        checkbox.setEnabled(true);
    }

    private void initIcon() {
        if(this.numberOfFiles() > 1) {
            iconImageView.setImage(IconCache.iconNamed("NSMultipleDocuments", 32));
        }
        else {
            iconImageView.setImage(IconCache.instance().iconForPath(files.get(0), 32));
        }
    }

    /**
     * @param file
     */
    private void initDistribution(Path file) {
        final boolean cloud = file instanceof CloudPath;

        this.distributionStatusField.setStringValue(Locale.localizedString("Unknown"));
        Rococoa.cast(this.distributionCnameField.cell(), NSTextFieldCell.class).setPlaceholderString(Locale.localizedString("Unknown"));

        distributionUrlField.setStringValue(Locale.localizedString("Unknown"));
        distributionUrlField.setEnabled(cloud);

        distributionStatusField.setStringValue(Locale.localizedString("Unknown"));
        distributionStatusField.setEnabled(cloud);

        // Amazon S3 only
        final boolean amazon = file instanceof S3Path;

        distributionCnameField.setStringValue(Locale.localizedString("Unknown"));
        distributionCnameField.setEnabled(amazon);

        distributionLoggingButton.setEnabled(cloud);

        String servicename = "";
        if(amazon) {
            servicename = Locale.localizedString("Amazon CloudFront", "S3");
        }
        // Mosso only
        final boolean mosso = file instanceof CFPath;
        if(mosso) {
            servicename = Locale.localizedString("Limelight Content", "Mosso");
        }
        distributionEnableButton.setEnabled(cloud);
        distributionEnableButton.setTitle(MessageFormat.format(Locale.localizedString("Enable {0} Distribution", "Status"),
                servicename));

        distributionDeliveryPopup.setEnabled(cloud);
        distributionDeliveryPopup.removeAllItems();
        if(cloud) {
            CloudSession session = (CloudSession) controller.getSession();
            for(Distribution.Method method : session.getSupportedMethods()) {
                distributionDeliveryPopup.addItemWithTitle(method.toString());
                distributionDeliveryPopup.itemWithTitle(method.toString()).setRepresentedObject(method.toString());
            }
            distributionDeliveryPopup.selectItemWithTitle(Distribution.DOWNLOAD.toString());
        }

        if(cloud) {
            distributionStatusButtonClicked(null);
        }
    }

    /**
     * Updates the size field by iterating over all files and
     * rading the cached size value in the attributes of the path
     */
    private void initSize() {
        this.toggleSizeSettings(false);
        controller.background(new BrowserBackgroundAction(controller) {
            double size = 0;

            public void run() {
                for(Path next : files) {
                    if(-1 == next.attributes.getSize()) {
                        next.readSize();
                    }
                    size += next.attributes.getSize();
                }
            }

            @Override
            public void cleanup() {
                toggleSizeSettings(true);
                StringBuilder formatted = new StringBuilder(Status.getSizeAsString(size));
                if(size > -1) {
                    formatted.append(" (").append(NumberFormat.getInstance().format(size)).append(" bytes)");
                }
                sizeField.setAttributedStringValue(NSAttributedString.attributedStringWithAttributes(
                        formatted.toString(),
                        TRUNCATE_MIDDLE_ATTRIBUTES));
            }

            @Override
            public String getActivity() {
                return MessageFormat.format(Locale.localizedString("Getting size of {0}", "Status"),
                        files.get(0).getName());
            }
        });
    }

    private void initChecksum(final Path file) {
        this.toggleSizeSettings(false);
        controller.background(new BrowserBackgroundAction(controller) {

            public void run() {
                if(null == file.attributes.getChecksum()) {
                    file.readChecksum();
                }
            }

            @Override
            public void cleanup() {
                toggleSizeSettings(true);
                if(StringUtils.isEmpty(file.attributes.getChecksum())) {
                    updateField(checksumField, Locale.localizedString("Unknown"));
                }
                else {
                    updateField(checksumField, file.attributes.getChecksum());
                }
            }

            @Override
            public String getActivity() {
                return MessageFormat.format(Locale.localizedString("Compute MD5 hash of {0}", "Status"),
                        files.get(0).getName());
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

        bucketLocationField.setStringValue(Locale.localizedString("Unknown"));
        bucketLocationField.setEnabled(amazon);
        s3CachePopup.setEnabled(amazon && file.attributes.isFile());
        bucketLoggingButton.setEnabled(amazon);
        bucketLoggingButton.setToolTip("");
        s3PublicUrlField.setStringValue(Locale.localizedString("Unknown"));
        s3torrentUrlField.setStringValue(Locale.localizedString("Unknown"));
        if(amazon) {
            final S3Path s3 = (S3Path) file;
            if(file.attributes.isFile()) {
                if(this.numberOfFiles() > 1) {
                    s3PublicUrlField.setStringValue("(" + Locale.localizedString("Multiple files") + ")");
                    s3PublicUrlField.setToolTip("");
                    s3torrentUrlField.setStringValue("(" + Locale.localizedString("Multiple files") + ")");
                    s3torrentUrlField.setToolTip("");
                }
                else {
                    final String signedUrl = s3.createSignedUrl();
                    s3PublicUrlField.setAttributedStringValue(
                            HyperlinkAttributedStringFactory.create(
                                    NSMutableAttributedString.create(signedUrl, TRUNCATE_MIDDLE_ATTRIBUTES), signedUrl)
                    );
                    s3PublicUrlField.setToolTip(signedUrl);
                    final String torrent = s3.createTorrentUrl();
                    s3torrentUrlField.setAttributedStringValue(
                            HyperlinkAttributedStringFactory.create(
                                    NSMutableAttributedString.create(torrent, TRUNCATE_MIDDLE_ATTRIBUTES), torrent)
                    );
                    s3torrentUrlField.setToolTip(torrent);
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

                @Override
                public void cleanup() {
                    bucketLoggingButton.setState(logging ? NSCell.NSOnState : NSCell.NSOffState);
                    if(StringUtils.isNotBlank(location)) {
                        bucketLocationField.setStringValue(Locale.localizedString(location, "S3"));
                    }
                    if(metadata.containsKey(S3Path.METADATA_HEADER_CACHE_CONTROL)) {
                        String cache = (String) metadata.get(S3Path.METADATA_HEADER_CACHE_CONTROL);
                        if(StringUtils.isNotBlank(cache)) {
                            if(s3CachePopup.indexOfItemWithTitle(cache).intValue() == -1) {
                                s3CachePopup.addItemWithTitle(cache);
                            }
                            s3CachePopup.selectItemWithTitle(cache);
                        }
                    }
                    toggleS3Settings(true);
                }
            });
        }
    }

    @Action
    public void bucketLoggingButtonClicked(final NSButton sender) {
        this.toggleS3Settings(false);
        controller.background(new BrowserBackgroundAction(controller) {
            public void run() {
                for(Path next : files) {
                    ((S3Path) next).setLogging(bucketLoggingButton.state() == NSCell.NSOnState);
                    break;
                }
            }

            @Override
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

    @Action
    public void filenameInputDidEndEditing(NSNotification sender) {
        if(this.numberOfFiles() == 1) {
            final Path current = files.get(0);
            if(!filenameField.stringValue().equals(current.getName())) {
                if(StringUtils.contains(filenameField.stringValue(), Path.DELIMITER)) {
                    AppKitFunctionsLibrary.beep();
                    return;
                }
                if(StringUtils.isBlank(filenameField.stringValue())) {
                    filenameField.setStringValue(current.getName());
                    this.initWebUrl();
                }
                else {
                    final Path renamed = PathFactory.createPath(controller.getSession(),
                            current.getParent().getAbsolute(), filenameField.stringValue(), current.attributes.getType());
                    controller.renamePath(current, renamed);
                }
            }
        }
    }

    @Action
    public void octalPermissionsInputDidEndEditing(NSNotification sender) {
        Permission permission = this.getPermissionFromOctalField();
        if(null == permission) {
            AppKitFunctionsLibrary.beep();
            this.initPermissions();
        }
        else {
            boolean change = false;
            for(Path file : files) {
                if(!file.attributes.getPermission().equals(permission)) {
                    change = true;
                }
            }
            if(change) {
                this.changePermissions(permission);
            }
        }
    }

    private Permission getPermissionFromOctalField() {
        if(StringUtils.isNotBlank(octalField.stringValue())) {
            if(StringUtils.length(octalField.stringValue()) == 3) {
                if(StringUtils.isNumeric(octalField.stringValue())) {
                    return new Permission(Integer.valueOf(octalField.stringValue()).intValue());
                }
            }
        }
        return null;
    }

    @Action
    public void permissionSelectionChanged(final NSButton sender) {
        if(sender.state() == NSCell.NSMixedState) {
            sender.setState(NSCell.NSOnState);
        }
        this.changePermissions(this.getPermissionFromCheckboxes());
    }

    private Permission getPermissionFromCheckboxes() {
        boolean[][] p = new boolean[3][3];

        p[Permission.OWNER][Permission.READ] = (ownerr.state() == NSCell.NSOnState);
        p[Permission.OWNER][Permission.WRITE] = (ownerw.state() == NSCell.NSOnState);
        p[Permission.OWNER][Permission.EXECUTE] = (ownerx.state() == NSCell.NSOnState);

        p[Permission.GROUP][Permission.READ] = (groupr.state() == NSCell.NSOnState);
        p[Permission.GROUP][Permission.WRITE] = (groupw.state() == NSCell.NSOnState);
        p[Permission.GROUP][Permission.EXECUTE] = (groupx.state() == NSCell.NSOnState);

        p[Permission.OTHER][Permission.READ] = (otherr.state() == NSCell.NSOnState);
        p[Permission.OTHER][Permission.WRITE] = (otherw.state() == NSCell.NSOnState);
        p[Permission.OTHER][Permission.EXECUTE] = (otherx.state() == NSCell.NSOnState);

        return new Permission(p);
    }

    /**
     * @param permission
     */
    private void changePermissions(final Permission permission) {
        // Write altered permissions to the server
        this.togglePermissionSettings(false);
        final boolean recursive = recursiveCheckbox.state() == NSCell.NSOnState;
        // send the changes to the remote host
        controller.background(new BrowserBackgroundAction(controller) {
            public void run() {
                for(Path next : files) {
                    if(recursive || !next.attributes.getPermission().equals(permission)) {
                        next.writePermissions(permission, recursive);
                    }
                    if(!controller.isConnected()) {
                        break;
                    }
                }
            }

            @Override
            public void cleanup() {
                togglePermissionSettings(true);
                initPermissions();
            }

            @Override
            public String getActivity() {
                return MessageFormat.format(Locale.localizedString("Changing permission of {0} to {1}", "Status"),
                        files.get(0).getName(), permission);
            }
        });
    }

    /**
     * Toggle settings before and after update
     *
     * @param enabled
     */
    private void togglePermissionSettings(boolean enabled) {
        if(enabled) {
            permissionProgress.stopAnimation(null);
        }
        else {
            permissionProgress.startAnimation(null);
        }
        boolean cloud = false;
        for(Path next : files) {
            if(!next.isWritePermissionsSupported()) {
                enabled = false;
            }
            if(next instanceof CloudPath) {
                cloud = true;
            }
        }
        recursiveCheckbox.setEnabled(enabled);
        for(Path next : files) {
            if(next.attributes.isFile()) {
                recursiveCheckbox.setState(NSCell.NSOffState);
                recursiveCheckbox.setEnabled(false);
                break;
            }
        }
        octalField.setEnabled(enabled);
        ownerr.setEnabled(enabled);
        ownerw.setEnabled(enabled);
        ownerx.setEnabled(enabled);
        groupr.setEnabled(!cloud && enabled);
        groupw.setEnabled(!cloud && enabled);
        groupx.setEnabled(!cloud && enabled);
        otherr.setEnabled(enabled);
        otherw.setEnabled(enabled);
        otherx.setEnabled(enabled);
    }

    /**
     * Toggle settings before and after update
     *
     * @param enabled
     */
    private void toggleDistributionSettings(boolean enabled) {
        distributionEnableButton.setEnabled(enabled);
        distributionLoggingButton.setEnabled(enabled);
        distributionLoggingButton.setEnabled(enabled);
        distributionDeliveryPopup.setEnabled(enabled);
        if(enabled) {
            distributionProgress.stopAnimation(null);
        }
        else {
            distributionProgress.startAnimation(null);
        }
    }

    @Action
    public void distributionApplyButtonClicked(final ID sender) {
        this.toggleDistributionSettings(false);
        controller.background(new BrowserBackgroundAction(controller) {
            public void run() {
                for(Path next : files) {
                    CloudPath cloud = (CloudPath) next;
                    CloudSession session = (CloudSession) controller.getSession();
                    String container = cloud.getContainerName();
                    Distribution.Method method = Distribution.DOWNLOAD;
                    if(distributionDeliveryPopup.selectedItem().representedObject().equals(Distribution.STREAMING.toString())) {
                        method = Distribution.STREAMING;
                    }
                    if(StringUtils.isNotBlank(distributionCnameField.stringValue())) {
                        session.writeDistribution(distributionEnableButton.state() == NSCell.NSOnState, container, method,
                                StringUtils.split(distributionCnameField.stringValue()),
                                distributionLoggingButton.state() == NSCell.NSOnState);
                    }
                    else {
                        session.writeDistribution(distributionEnableButton.state() == NSCell.NSOnState, container, method,
                                new String[]{}, distributionLoggingButton.state() == NSCell.NSOnState);
                    }
                    break;
                }
            }

            @Override
            public void cleanup() {
                // Refresh the current distribution status
                distributionStatusButtonClicked(sender);
            }
        });
    }

    @Action
    public void distributionStatusButtonClicked(final ID sender) {
        this.toggleDistributionSettings(false);
        controller.background(new BrowserBackgroundAction(controller) {
            Distribution distribution;

            public void run() {
                for(Path next : files) {
                    CloudPath cloud = (CloudPath) next;
                    CloudSession session = (CloudSession) controller.getSession();
                    // We only support one distribution per bucket for the sake of simplicity
                    if(distributionDeliveryPopup.selectedItem().representedObject().equals(Distribution.STREAMING.toString())) {
                        distribution = session.readDistribution(cloud.getContainerName(), Distribution.STREAMING);
                    }
                    if(distributionDeliveryPopup.selectedItem().representedObject().equals(Distribution.DOWNLOAD.toString())) {
                        distribution = session.readDistribution(cloud.getContainerName(), Distribution.DOWNLOAD);
                    }
                    break;
                }
            }

            @Override
            public void cleanup() {
                toggleDistributionSettings(true);

                distributionEnableButton.setState(distribution.isEnabled() ? NSCell.NSOnState : NSCell.NSOffState);
                distributionStatusField.setStringValue(distribution.getStatus());
                distributionLoggingButton.setEnabled(distribution.isEnabled());
                distributionLoggingButton.setState(distribution.isLogging() ? NSCell.NSOnState : NSCell.NSOffState);

                final CloudPath file = ((CloudPath) files.get(0));
                // Concatenate URLs
                final String key = file.isContainer() ? "" : file.encode(file.getKey());
                if(numberOfFiles() > 1) {
                    distributionUrlField.setStringValue("(" + Locale.localizedString("Multiple files") + ")");
                    distributionUrlField.setToolTip("");
                    distributionCnameUrlField.setStringValue("(" + Locale.localizedString("Multiple files") + ")");
                }
                else {
                    if(null == distribution.getUrl()) {
                        distributionUrlField.setStringValue(Locale.localizedString("Unknown"));
                    }
                    else {
                        final String url = distribution.getUrl() + key;
                        distributionUrlField.setAttributedStringValue(HyperlinkAttributedStringFactory.create(
                                NSMutableAttributedString.create(url, TRUNCATE_MIDDLE_ATTRIBUTES), url));
                        distributionUrlField.setToolTip(url);
                    }
                }
                final String[] cnames = distribution.getCNAMEs();
                if(0 == cnames.length) {
                    distributionCnameField.setStringValue("");
                    distributionCnameUrlField.setStringValue("");
                    distributionCnameUrlField.setToolTip("");
                }
                else {
                    distributionCnameField.setStringValue(StringUtils.join(cnames, ' '));
                    for(String cname : cnames) {
                        final String url = distribution.getMethod().getProtocol() + cname + distribution.getMethod().getContext() + key;
                        distributionCnameUrlField.setAttributedStringValue(
                                HyperlinkAttributedStringFactory.create(
                                        NSMutableAttributedString.create(url, TRUNCATE_MIDDLE_ATTRIBUTES), url)
                        );
                        distributionCnameUrlField.setToolTip(url);
                        // We only support one CNAME URL
                        break;
                    }
                }
            }
        });
    }

    @Action
    public void calculateSizeButtonClicked(final ID sender) {
        log.debug("calculateSizeButtonClicked");
        this.toggleSizeSettings(false);
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

            @Override
            public void cleanup() {
                toggleSizeSettings(true);
                initSize();
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

            @Override
            public String getActivity() {
                return MessageFormat.format(Locale.localizedString("Getting size of {0}", "Status"),
                        files.get(0).getName());
            }
        });
    }

    private void toggleSizeSettings(boolean enabled) {
        if(enabled) {
            sizeProgress.stopAnimation(null);
        }
        else {
            sizeProgress.startAnimation(null);
        }
        sizeButton.setEnabled(false);
        for(Path next : files) {
            if(next.attributes.isDirectory()) {
                sizeButton.setEnabled(enabled);
                break;
            }
        }
    }


    @Override
    @Action
    public void helpButtonClicked(final NSButton sender) {
        NSWorkspace.sharedWorkspace().openURL(
                NSURL.URLWithString(Preferences.instance().getProperty("website.help")
                        + "/howto/info")
        );
    }
}
