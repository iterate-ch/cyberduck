package ch.cyberduck.ui.cocoa;

/*
 * Copyright (c) 2002-2010 David Kocher. All rights reserved.
 *
 * http://cyberduck.ch/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * Bug fixes, suggestions and comments should be sent to:
 * dkocher@cyberduck.ch
 */

import ch.cyberduck.binding.AbstractTableDelegate;
import ch.cyberduck.binding.Action;
import ch.cyberduck.binding.Delegate;
import ch.cyberduck.binding.HyperlinkAttributedStringFactory;
import ch.cyberduck.binding.ListDataSource;
import ch.cyberduck.binding.Outlet;
import ch.cyberduck.binding.ToolbarWindowController;
import ch.cyberduck.binding.application.*;
import ch.cyberduck.binding.foundation.NSAttributedString;
import ch.cyberduck.binding.foundation.NSIndexSet;
import ch.cyberduck.binding.foundation.NSMutableAttributedString;
import ch.cyberduck.binding.foundation.NSNotification;
import ch.cyberduck.binding.foundation.NSNotificationCenter;
import ch.cyberduck.binding.foundation.NSObject;
import ch.cyberduck.binding.foundation.NSString;
import ch.cyberduck.core.*;
import ch.cyberduck.core.analytics.AnalyticsProvider;
import ch.cyberduck.core.cdn.Distribution;
import ch.cyberduck.core.cdn.DistributionConfiguration;
import ch.cyberduck.core.cdn.features.Cname;
import ch.cyberduck.core.cdn.features.DistributionLogging;
import ch.cyberduck.core.cdn.features.Index;
import ch.cyberduck.core.cdn.features.Purge;
import ch.cyberduck.core.date.RFC1123DateFormatter;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.AclPermission;
import ch.cyberduck.core.features.Encryption;
import ch.cyberduck.core.features.Headers;
import ch.cyberduck.core.features.Lifecycle;
import ch.cyberduck.core.features.Location;
import ch.cyberduck.core.features.Logging;
import ch.cyberduck.core.features.Move;
import ch.cyberduck.core.features.Redundancy;
import ch.cyberduck.core.features.UnixPermission;
import ch.cyberduck.core.features.Versioning;
import ch.cyberduck.core.formatter.SizeFormatterFactory;
import ch.cyberduck.core.identity.IdentityConfiguration;
import ch.cyberduck.core.io.Checksum;
import ch.cyberduck.core.lifecycle.LifecycleConfiguration;
import ch.cyberduck.core.local.BrowserLauncherFactory;
import ch.cyberduck.core.local.FileDescriptor;
import ch.cyberduck.core.local.FileDescriptorFactory;
import ch.cyberduck.core.logging.LoggingConfiguration;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.resources.IconCacheFactory;
import ch.cyberduck.core.s3.S3Protocol;
import ch.cyberduck.core.threading.AlertRecursiveCallback;
import ch.cyberduck.core.threading.RegistryBackgroundAction;
import ch.cyberduck.core.threading.WindowMainAction;
import ch.cyberduck.core.threading.WorkerBackgroundAction;
import ch.cyberduck.core.worker.BooleanRecursiveCallback;
import ch.cyberduck.core.worker.CalculateSizeWorker;
import ch.cyberduck.core.worker.ReadAclWorker;
import ch.cyberduck.core.worker.ReadMetadataWorker;
import ch.cyberduck.core.worker.ReadPermissionWorker;
import ch.cyberduck.core.worker.ReadSizeWorker;
import ch.cyberduck.core.worker.WriteAclWorker;
import ch.cyberduck.core.worker.WriteEncryptionWorker;
import ch.cyberduck.core.worker.WriteMetadataWorker;
import ch.cyberduck.core.worker.WritePermissionWorker;
import ch.cyberduck.core.worker.WriteRedundancyWorker;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.jets3t.service.model.S3Object;
import org.rococoa.Foundation;
import org.rococoa.ID;
import org.rococoa.Selector;
import org.rococoa.cocoa.foundation.NSInteger;
import org.rococoa.cocoa.foundation.NSPoint;
import org.rococoa.cocoa.foundation.NSSize;
import org.rococoa.cocoa.foundation.NSUInteger;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TimeZone;

public class InfoController extends ToolbarWindowController {
    private static final Logger log = Logger.getLogger(InfoController.class);

    private static NSPoint cascade = new NSPoint(0, 0);

    private final NSNotificationCenter notificationCenter = NSNotificationCenter.defaultCenter();

    private final Session<?> session;

    private final Cache<Path> cache;

    private final Controller controller;

    private final NSComboBoxCell aclPermissionCellPrototype = NSComboBoxCell.comboBoxCell();

    /**
     * Selected files
     */
    private List<Path> files;

    private final FileDescriptor descriptor = FileDescriptorFactory.get();

    private final LoginCallback prompt = LoginCallbackFactory.get(this);

    private final PathContainerService containerService
            = new PathContainerService();

    private final Preferences preferences
            = PreferencesFactory.get();

    /**
     * Grant editing model.
     */
    private final List<Acl.UserAndRole> acl = new ArrayList<Acl.UserAndRole>();
    /**
     * Custom HTTP headers for REST protocols
     */
    private final List<Header> metadata
            = new ArrayList<Header>();

    @Outlet
    private NSTextField filenameField;
    @Outlet
    private NSTextField groupField;
    @Outlet
    private NSTextField kindField;
    @Outlet
    private NSTextField modifiedField;
    @Outlet
    private NSTextField createdField;
    @Outlet
    private NSTextField permissionsField;
    @Outlet
    private NSTextField octalField;
    @Outlet
    private NSTextField ownerField;
    @Outlet
    private NSTextField sizeField;
    @Outlet
    private NSTextField checksumField;
    @Outlet
    private NSTextField pathField;
    @Outlet
    private NSTextField webUrlField;
    @Outlet
    private NSButton recursiveButton;
    @Outlet
    private NSButton sizeButton;
    @Outlet
    private NSProgressIndicator sizeProgress;
    @Outlet
    private NSProgressIndicator permissionProgress;
    @Outlet
    private NSProgressIndicator s3Progress;
    @Outlet
    private NSProgressIndicator aclProgress;
    @Outlet
    private NSProgressIndicator metadataProgress;
    @Outlet
    private NSProgressIndicator distributionProgress;
    @Outlet
    private NSButton distributionEnableButton;
    @Outlet
    private NSButton distributionLoggingButton;
    @Outlet
    private NSPopUpButton distributionLoggingPopup;
    @Outlet
    private NSButton distributionInvalidateObjectsButton;
    @Outlet
    private NSTextField distributionInvalidationStatusField;
    @Outlet
    private NSPopUpButton distributionDeliveryPopup;
    @Outlet
    private NSPopUpButton distributionDefaultRootPopup;
    @Outlet
    private NSTextField bucketLocationField;
    @Outlet
    private NSPopUpButton storageClassPopup;
    @Outlet
    private NSPopUpButton encryptionPopup;
    @Outlet
    private NSButton bucketLoggingButton;
    @Outlet
    private NSPopUpButton bucketLoggingPopup;
    @Outlet
    private NSButton bucketAnalyticsButton;
    @Outlet
    private NSTextField bucketAnalyticsSetupUrlField;
    @Outlet
    private NSButton bucketVersioningButton;
    @Outlet
    private NSButton bucketMfaButton;
    @Outlet
    private NSTextField s3PublicUrlField;
    @Outlet
    private NSTextField s3PublicUrlValidityField;
    @Outlet
    private NSTextField s3torrentUrlField;
    @Outlet
    private NSButton lifecycleTransitionCheckbox;
    @Outlet
    private NSPopUpButton lifecycleTransitionPopup;
    @Outlet
    private NSButton lifecycleDeleteCheckbox;
    @Outlet
    private NSPopUpButton lifecycleDeletePopup;
    @Outlet
    private NSTextField distributionCnameField;
    @Outlet
    private NSTextField distributionOriginField;
    @Outlet
    private NSTextField distributionStatusField;
    @Outlet
    private NSTextField distributionUrlField;
    @Outlet
    private NSTextField distributionCnameUrlField;
    @Outlet
    private NSTextField aclUrlField;
    @Outlet
    private NSTableView aclTable;
    @Delegate
    private ListDataSource aclTableModel;
    @Delegate
    private AbstractTableDelegate<Acl.UserAndRole> aclTableDelegate;
    @Outlet
    private NSPopUpButton aclAddButton;
    @Outlet
    private NSButton aclRemoveButton;
    @Outlet
    private NSTableView metadataTable;
    @Delegate
    private ListDataSource metadataTableModel;
    @Delegate
    private AbstractTableDelegate<String> metadataTableDelegate;
    @Outlet
    private NSPopUpButton metadataAddButton;
    @Outlet
    private NSButton metadataRemoveButton;
    @Outlet
    private NSButton ownerr;
    @Outlet
    private NSButton ownerw;
    @Outlet
    private NSButton ownerx;
    @Outlet
    private NSButton groupr;
    @Outlet
    private NSButton groupw;
    @Outlet
    private NSButton groupx;
    @Outlet
    private NSButton otherr;
    @Outlet
    private NSButton otherw;
    @Outlet
    private NSButton otherx;
    @Outlet
    private NSImageView iconImageView;
    @Outlet
    private NSView panelMetadata;
    @Outlet
    private NSView panelCloud;
    @Outlet
    private NSView panelDistribution;
    @Outlet
    private NSView panelPermissions;
    @Outlet
    private NSView panelAcl;
    @Outlet
    private NSView panelGeneral;
    @Outlet
    private NSButton distributionAnalyticsButton;
    @Outlet
    private NSTextField distributionAnalyticsSetupUrlField;

    public InfoController(final Controller controller, final Session<?> session, final Cache<Path> cache, final List<Path> files) {
        this.controller = controller;
        this.session = session;
        this.cache = cache;
        this.files = files;
        this.loadBundle();
    }

    private Path getSelected() {
        for(Path file : files) {
            return file;
        }
        return null;
    }

    @Override
    public void setWindow(final NSWindow window) {
        window.setFrameAutosaveName("Info");
        window.setShowsResizeIndicator(true);
        window.setContentMinSize(window.frame().size);
        window.setContentMaxSize(new NSSize(600, window.frame().size.height.doubleValue()));
        super.setWindow(window);
        if(!preferences.getBoolean("browser.info.inspector")) {
            cascade = this.cascade(cascade);
        }
    }

    @Override
    public void windowWillClose(final NSNotification notification) {
        cascade = new NSPoint(this.window().frame().origin.x.doubleValue(),
                this.window().frame().origin.y.doubleValue() + this.window().frame().size.height.doubleValue());
        this.window().endEditingFor(null);
        super.windowWillClose(notification);
    }

    @Override
    public boolean isSingleton() {
        return preferences.getBoolean("browser.info.inspector");
    }

    @Override
    protected void initializePanel(final String identifier) {
        InfoToolbarItem item;
        try {
            item = InfoToolbarItem.valueOf(identifier);
        }
        catch(IllegalArgumentException e) {
            item = InfoToolbarItem.info;
        }
        switch(item) {
            case info:
                this.initGeneral();
                this.initPermissions();
                break;
            case permissions:
                this.initPermissions();
                break;
            case acl:
                this.initAcl();
                break;
            case distribution:
                this.initDistribution();
                break;
            case s3:
                this.initS3();
                break;
            case metadata:
                this.initMetadata();
                break;
        }
    }

    @Override
    protected NSUInteger getToolbarSize() {
        return NSToolbar.NSToolbarSizeModeSmall;
    }

    @Override
    public NSToolbarItem toolbar_itemForItemIdentifier_willBeInsertedIntoToolbar(final NSToolbar toolbar, final String identifier, final boolean flag) {
        NSToolbarItem item = super.toolbar_itemForItemIdentifier_willBeInsertedIntoToolbar(toolbar, identifier, flag);
        switch(InfoToolbarItem.valueOf(identifier)) {
            case distribution:
                if(session.getFeature(DistributionConfiguration.class) != null) {
                    // Give icon and label of the given session
                    item.setImage(IconCacheFactory.<NSImage>get().iconNamed(session.getHost().getProtocol().disk(), 32));
                }
                else {
                    // CloudFront is the default for custom distributions
                    item.setImage(IconCacheFactory.<NSImage>get().iconNamed(new S3Protocol().disk(), 32));
                }
                break;
            case s3:
                if(session.getHost().getProtocol().getType() == Protocol.Type.s3) {
                    // Set icon of cloud service provider
                    item.setLabel(session.getHost().getProtocol().getName());
                    item.setImage(IconCacheFactory.<NSImage>get().iconNamed(session.getHost().getProtocol().disk(), 32));
                }
                else {
                    // Currently these settings are only available for Amazon S3
                    item.setLabel(new S3Protocol().getName());
                    item.setImage(IconCacheFactory.<NSImage>get().iconNamed(new S3Protocol().disk(), 32));
                }
                break;
            case metadata:
                item.setImage(IconCacheFactory.<NSImage>get().iconNamed("pencil.tiff", 32));
                break;
            case acl:
                item.setImage(IconCacheFactory.<NSImage>get().iconNamed("permissions.tiff", 32));
                break;
        }
        return item;
    }

    @Override
    protected boolean validateTabWithIdentifier(final String identifier) {
        final boolean anonymous = session.getHost().getCredentials().isAnonymousLogin();
        switch(InfoToolbarItem.valueOf(identifier)) {
            case permissions:
                if(anonymous) {
                    // Anonymous never has the right to updated permissions
                    return false;
                }
                return session.getFeature(UnixPermission.class) != null;
            case acl:
                if(anonymous) {
                    // Anonymous never has the right to updated permissions
                    return false;
                }
                return session.getFeature(AclPermission.class) != null;
            case distribution:
                if(anonymous) {
                    return false;
                }
                // Not enabled if not a cloud session
                return session.getFeature(DistributionConfiguration.class) != null;
            case s3:
                if(anonymous) {
                    return false;
                }
                return session.getHost().getProtocol().getType() == Protocol.Type.s3
                        || session.getHost().getProtocol().getType() == Protocol.Type.googlestorage;
            case metadata:
                if(anonymous) {
                    return false;
                }
                // Not enabled if not a cloud session
                return session.getFeature(Headers.class) != null;
        }
        return true;
    }

    @Override
    public String getTitle(NSTabViewItem item) {
        return String.format("%s – %s", item.label(), this.getName());
    }

    @Override
    public void invalidate() {
        notificationCenter.removeObserver(this.id());
        super.invalidate();
    }

    @Override
    protected String getBundleName() {
        return "Info";
    }

    public void setFiles(List<Path> files) {
        if(files.isEmpty()) {
            return;
        }
        this.files = files;
        this.initializePanel(this.getSelectedTab());
        this.setTitle(this.getTitle(tabView.selectedTabViewItem()));
    }

    @Override
    public void awakeFromNib() {
        this.ownerr.setTarget(this.id());
        final Selector s = Foundation.selector("permissionSelectionChanged:");
        this.ownerr.setAction(s);
        this.ownerr.setAllowsMixedState(true);
        this.ownerw.setTarget(this.id());
        this.ownerw.setAction(s);
        this.ownerw.setAllowsMixedState(true);
        this.ownerx.setTarget(this.id());
        this.ownerx.setAction(s);
        this.ownerx.setAllowsMixedState(true);

        this.groupr.setTarget(this.id());
        this.groupr.setAction(s);
        this.groupr.setAllowsMixedState(true);
        this.groupw.setTarget(this.id());
        this.groupw.setAction(s);
        this.groupw.setAllowsMixedState(true);
        this.groupx.setTarget(this.id());
        this.groupx.setAction(s);
        this.groupx.setAllowsMixedState(true);

        this.otherr.setTarget(this.id());
        this.otherr.setAction(s);
        this.otherr.setAllowsMixedState(true);
        this.otherw.setTarget(this.id());
        this.otherw.setAction(s);
        this.otherw.setAllowsMixedState(true);
        this.otherx.setTarget(this.id());
        this.otherx.setAction(s);
        this.otherx.setAllowsMixedState(true);

        super.awakeFromNib();
    }

    @Override
    protected List<NSView> getPanels() {
        List<NSView> views = new ArrayList<NSView>();
        views.add(panelGeneral);
        if(session.getFeature(UnixPermission.class) != null) {
            views.add(panelPermissions);
        }
        if(session.getFeature(AclPermission.class) != null) {
            views.add(panelAcl);
        }
        views.add(panelMetadata);
        views.add(panelDistribution);
        views.add(panelCloud);
        return views;
    }

    @Override
    protected List<String> getPanelIdentifiers() {
        List<String> identifiers = new ArrayList<String>();
        identifiers.add(InfoToolbarItem.info.name());
        if(session.getFeature(UnixPermission.class) != null) {
            identifiers.add(InfoToolbarItem.permissions.name());
        }
        if(session.getFeature(AclPermission.class) != null) {
            identifiers.add(InfoToolbarItem.acl.name());
        }
        identifiers.add(InfoToolbarItem.metadata.name());
        identifiers.add(InfoToolbarItem.distribution.name());
        identifiers.add(InfoToolbarItem.s3.name());
        return identifiers;
    }

    private String getName() {
        final int count = this.numberOfFiles();
        if(count > 1) {
            return String.format("(%s)", LocaleFactory.localizedString("Multiple files"));
        }
        return this.getSelected().getName();
    }

    public void setFilenameField(NSTextField filenameField) {
        this.filenameField = filenameField;
        this.filenameField.setEditable(false);
    }

    public void setGroupField(NSTextField t) {
        this.groupField = t;
    }

    public void setKindField(NSTextField t) {
        this.kindField = t;
    }

    public void setModifiedField(NSTextField t) {
        this.modifiedField = t;
    }

    public void setCreatedField(NSTextField t) {
        this.createdField = t;
    }

    public void setPermissionsField(NSTextField permissionsField) {
        this.permissionsField = permissionsField;
    }

    public void setOctalField(NSTextField octalField) {
        this.octalField = octalField;
        notificationCenter.addObserver(this.id(),
                Foundation.selector("octalPermissionsInputDidEndEditing:"),
                NSControl.NSControlTextDidEndEditingNotification,
                octalField);
    }

    public void setOwnerField(NSTextField ownerField) {
        this.ownerField = ownerField;
    }

    public void setSizeField(NSTextField sizeField) {
        this.sizeField = sizeField;
    }

    public void setChecksumField(NSTextField checksumField) {
        this.checksumField = checksumField;
    }

    public void setPathField(NSTextField pathField) {
        this.pathField = pathField;
    }

    public void setWebUrlField(NSTextField webUrlField) {
        this.webUrlField = webUrlField;
        this.webUrlField.setAllowsEditingTextAttributes(true);
        this.webUrlField.setSelectable(true);
    }

    public void setRecursiveButton(NSButton b) {
        this.recursiveButton = b;
        this.recursiveButton.setTarget(this.id());
        this.recursiveButton.setAction(Foundation.selector("recursiveButtonClicked:"));
    }

    public void setSizeButton(NSButton b) {
        this.sizeButton = b;
        this.sizeButton.setTarget(this.id());
        this.sizeButton.setAction(Foundation.selector("calculateSizeButtonClicked:"));
    }

    public void setSizeProgress(final NSProgressIndicator p) {
        this.sizeProgress = p;
        this.sizeProgress.setDisplayedWhenStopped(false);
        this.sizeProgress.setStyle(NSProgressIndicator.NSProgressIndicatorSpinningStyle);
    }

    public void setPermissionProgress(final NSProgressIndicator p) {
        this.permissionProgress = p;
        this.permissionProgress.setDisplayedWhenStopped(false);
        this.permissionProgress.setStyle(NSProgressIndicator.NSProgressIndicatorSpinningStyle);
    }

    public void setS3Progress(final NSProgressIndicator p) {
        this.s3Progress = p;
        this.s3Progress.setDisplayedWhenStopped(false);
        this.s3Progress.setStyle(NSProgressIndicator.NSProgressIndicatorSpinningStyle);
    }

    public void setAclProgress(final NSProgressIndicator p) {
        this.aclProgress = p;
        this.aclProgress.setDisplayedWhenStopped(false);
        this.aclProgress.setStyle(NSProgressIndicator.NSProgressIndicatorSpinningStyle);
    }

    public void setMetadataProgress(final NSProgressIndicator p) {
        this.metadataProgress = p;
        this.metadataProgress.setDisplayedWhenStopped(false);
        this.metadataProgress.setStyle(NSProgressIndicator.NSProgressIndicatorSpinningStyle);
    }

    public void setDistributionProgress(final NSProgressIndicator p) {
        this.distributionProgress = p;
        this.distributionProgress.setDisplayedWhenStopped(false);
        this.distributionProgress.setStyle(NSProgressIndicator.NSProgressIndicatorSpinningStyle);
    }

    public void setDistributionEnableButton(NSButton b) {
        this.distributionEnableButton = b;
        this.distributionEnableButton.setTarget(this.id());
        this.distributionEnableButton.setAction(Foundation.selector("distributionApplyButtonClicked:"));
    }

    public void setDistributionLoggingButton(NSButton b) {
        this.distributionLoggingButton = b;
        this.distributionLoggingButton.setTarget(this.id());
        this.distributionLoggingButton.setAction(Foundation.selector("distributionApplyButtonClicked:"));
    }

    public void setDistributionLoggingPopup(NSPopUpButton b) {
        this.distributionLoggingPopup = b;
        this.distributionLoggingPopup.setTarget(this.id());
        this.distributionLoggingPopup.setAction(Foundation.selector("distributionLoggingPopupClicked:"));
    }

    public void setDistributionInvalidateObjectsButton(NSButton b) {
        this.distributionInvalidateObjectsButton = b;
        this.distributionInvalidateObjectsButton.setTarget(this.id());
        this.distributionInvalidateObjectsButton.setAction(Foundation.selector("distributionInvalidateObjectsButtonClicked:"));
    }

    public void setDistributionInvalidationStatusField(NSTextField t) {
        this.distributionInvalidationStatusField = t;
    }

    public void setDistributionDeliveryPopup(NSPopUpButton b) {
        this.distributionDeliveryPopup = b;
        this.distributionDeliveryPopup.setTarget(this.id());
        this.distributionDeliveryPopup.setAction(Foundation.selector("distributionStatusButtonClicked:"));
    }

    public void setDistributionDefaultRootPopup(NSPopUpButton b) {
        this.distributionDefaultRootPopup = b;
        this.distributionDefaultRootPopup.setTarget(this.id());
        this.distributionDefaultRootPopup.setAction(Foundation.selector("distributionApplyButtonClicked:"));
    }

    public void setBucketLocationField(NSTextField t) {
        this.bucketLocationField = t;
    }

    public void setStorageClassPopup(NSPopUpButton b) {
        this.storageClassPopup = b;
        this.storageClassPopup.setTarget(this.id());
        this.storageClassPopup.setAction(Foundation.selector("storageClassPopupClicked:"));
        this.storageClassPopup.setAllowsMixedState(true);
    }

    @Action
    public void storageClassPopupClicked(final NSPopUpButton sender) {
        if(this.toggleS3Settings(false)) {
            final String redundancy = sender.selectedItem().representedObject();
            controller.background(new WorkerBackgroundAction<Boolean>(controller, session, cache,
                    new WriteRedundancyWorker(files, redundancy, new AlertRecursiveCallback<String>(this), controller) {
                                @Override
                                public void cleanup(final Boolean v) {
                                    toggleS3Settings(true);
                                    initS3();
                                }
                            }
                    )
            );
        }
    }

    public void setEncryptionPopup(NSPopUpButton b) {
        this.encryptionPopup = b;
        this.encryptionPopup.setTarget(this.id());
        this.encryptionPopup.setAction(Foundation.selector("encryptionPopupClicked:"));
        this.encryptionPopup.setAllowsMixedState(true);
    }

    @Action
    public void encryptionPopupClicked(final NSPopUpButton sender) {
        final String algorithm = sender.selectedItem().representedObject();
        if(null != algorithm && this.toggleS3Settings(false)) {
            final Encryption.Algorithm encryption = Encryption.Algorithm.fromString(algorithm);
            controller.background(new WorkerBackgroundAction<Boolean>(controller, session, cache,
                    new WriteEncryptionWorker(files, encryption, new AlertRecursiveCallback<Encryption.Algorithm>(this), controller) {
                                @Override
                                public void cleanup(final Boolean v) {
                                    toggleS3Settings(true);
                                    initS3();
                                }
                            }
                    )
            );
        }
    }

    public void setBucketLoggingButton(NSButton b) {
        this.bucketLoggingButton = b;
        this.bucketLoggingButton.setAction(Foundation.selector("bucketLoggingButtonClicked:"));
    }

    @Action
    public void bucketLoggingButtonClicked(final NSButton sender) {
        if(this.toggleS3Settings(false)) {
            controller.background(new RegistryBackgroundAction<Boolean>(controller, session, cache) {
                @Override
                public Boolean run() throws BackgroundException {
                    final Logging logging = session.getFeature(Logging.class);
                    logging.setConfiguration(containerService.getContainer(getSelected()),
                            new LoggingConfiguration(
                                    bucketLoggingButton.state() == NSCell.NSOnState,
                                    null == bucketLoggingPopup.selectedItem() ? null : bucketLoggingPopup.selectedItem().representedObject()
                            )
                    );
                    return true;
                }

                @Override
                public void cleanup() {
                    super.cleanup();
                    toggleS3Settings(true);
                    initS3();
                }

                @Override
                public String getActivity() {
                    return MessageFormat.format(LocaleFactory.localizedString("Writing metadata of {0}", "Status"),
                            this.toString(files));
                }
            });
        }
    }

    public void setBucketLoggingPopup(NSPopUpButton b) {
        this.bucketLoggingPopup = b;
        this.bucketLoggingPopup.setTarget(this.id());
        this.bucketLoggingPopup.setAction(Foundation.selector("bucketLoggingPopupClicked:"));
    }

    @Action
    public void bucketLoggingPopupClicked(final NSPopUpButton sender) {
        if(bucketLoggingButton.state() == NSCell.NSOnState) {
            // Only write change if logging is already enabled
            this.bucketLoggingButtonClicked(sender);
        }
    }

    public void setBucketAnalyticsButton(NSButton b) {
        this.bucketAnalyticsButton = b;
        this.bucketAnalyticsButton.setAction(Foundation.selector("bucketAnalyticsButtonClicked:"));
    }

    @Action
    public void bucketAnalyticsButtonClicked(final NSButton sender) {
        if(this.toggleS3Settings(false)) {
            controller.background(new RegistryBackgroundAction<Void>(controller, session, cache) {
                @Override
                public Void run() throws BackgroundException {
                    final IdentityConfiguration iam = session.getFeature(IdentityConfiguration.class);
                    if(bucketAnalyticsButton.state() == NSCell.NSOnState) {
                        final String document = preferences.getProperty("analytics.provider.qloudstat.iam.policy");
                        iam.create(session.getFeature(AnalyticsProvider.class).getName(), document, prompt);
                    }
                    else {
                        iam.delete(session.getFeature(AnalyticsProvider.class).getName(), prompt);
                    }
                    return null;
                }

                @Override
                public void cleanup() {
                    super.cleanup();
                    toggleS3Settings(true);
                    initS3();
                }
            });
        }
    }

    public void setBucketAnalyticsSetupUrlField(NSTextField f) {
        this.bucketAnalyticsSetupUrlField = f;
        this.bucketAnalyticsSetupUrlField.setAllowsEditingTextAttributes(true);
        this.bucketAnalyticsSetupUrlField.setSelectable(true);
    }

    public void setBucketVersioningButton(NSButton b) {
        this.bucketVersioningButton = b;
        this.bucketVersioningButton.setAction(Foundation.selector("bucketVersioningButtonClicked:"));
    }

    @Action
    public void bucketVersioningButtonClicked(final NSButton sender) {
        if(this.toggleS3Settings(false)) {
            controller.background(new RegistryBackgroundAction<Void>(controller, session, cache) {
                @Override
                public Void run() throws BackgroundException {
                    session.getFeature(Versioning.class).setConfiguration(containerService.getContainer(getSelected()), prompt,
                            new VersioningConfiguration(
                                    bucketVersioningButton.state() == NSCell.NSOnState,
                                    bucketMfaButton.state() == NSCell.NSOnState)
                    );
                    return null;
                }

                @Override
                public void cleanup() {
                    super.cleanup();
                    toggleS3Settings(true);
                    initS3();
                }
            });
        }
    }

    public void setBucketMfaButton(NSButton b) {
        this.bucketMfaButton = b;
        this.bucketMfaButton.setAction(Foundation.selector("bucketMfaButtonClicked:"));
    }

    @Action
    public void bucketMfaButtonClicked(final NSButton sender) {
        if(this.toggleS3Settings(false)) {
            controller.background(new RegistryBackgroundAction<Void>(controller, session, cache) {
                @Override
                public Void run() throws BackgroundException {
                    session.getFeature(Versioning.class).setConfiguration(containerService.getContainer(getSelected()),
                            prompt,
                            new VersioningConfiguration(
                                    bucketVersioningButton.state() == NSCell.NSOnState,
                                    bucketMfaButton.state() == NSCell.NSOnState)
                    );
                    return null;
                }

                @Override
                public void cleanup() {
                    super.cleanup();
                    toggleS3Settings(true);
                    initS3();
                }
            });
        }
    }

    public void setS3PublicUrlField(NSTextField t) {
        this.s3PublicUrlField = t;
        this.s3PublicUrlField.setAllowsEditingTextAttributes(true);
        this.s3PublicUrlField.setSelectable(true);
    }

    public void setS3PublicUrlValidityField(NSTextField s3PublicUrlValidityField) {
        this.s3PublicUrlValidityField = s3PublicUrlValidityField;
    }

    public void setS3torrentUrlField(NSTextField t) {
        this.s3torrentUrlField = t;
        this.s3torrentUrlField.setAllowsEditingTextAttributes(true);
        this.s3torrentUrlField.setSelectable(true);
    }

    public void setLifecycleTransitionCheckbox(final NSButton b) {
        this.lifecycleTransitionCheckbox = b;
        this.lifecycleTransitionCheckbox.setAction(Foundation.selector("lifecyclePopupClicked:"));
    }

    public void setLifecycleTransitionPopup(final NSPopUpButton b) {
        this.lifecycleTransitionPopup = b;
        this.lifecycleTransitionPopup.setTarget(this.id());
        for(String option : preferences.getList("s3.lifecycle.transition.options")) {
            this.lifecycleTransitionPopup.addItemWithTitle(MessageFormat.format(LocaleFactory.localizedString("after {0} Days", "S3"), option));
            this.lifecycleTransitionPopup.lastItem().setAction(Foundation.selector("lifecyclePopupClicked:"));
            this.lifecycleTransitionPopup.lastItem().setTarget(this.id());
            this.lifecycleTransitionPopup.lastItem().setRepresentedObject(option);
        }
    }

    public void setLifecycleDeleteCheckbox(final NSButton b) {
        this.lifecycleDeleteCheckbox = b;
        this.lifecycleDeleteCheckbox.setAction(Foundation.selector("lifecyclePopupClicked:"));
    }

    public void setLifecycleDeletePopup(final NSPopUpButton b) {
        this.lifecycleDeletePopup = b;
        for(String option : preferences.getList("s3.lifecycle.delete.options")) {
            this.lifecycleDeletePopup.addItemWithTitle(MessageFormat.format(LocaleFactory.localizedString("after {0} Days", "S3"), option));
            this.lifecycleDeletePopup.lastItem().setAction(Foundation.selector("lifecyclePopupClicked:"));
            this.lifecycleDeletePopup.lastItem().setTarget(this.id());
            this.lifecycleDeletePopup.lastItem().setRepresentedObject(option);
        }
    }

    @Action
    public void lifecyclePopupClicked(final NSButton sender) {
        if(this.toggleS3Settings(false)) {
            controller.background(new RegistryBackgroundAction<Void>(controller, session, cache) {
                @Override
                public Void run() throws BackgroundException {
                    session.getFeature(Lifecycle.class).setConfiguration(containerService.getContainer(getSelected()),
                            new LifecycleConfiguration(
                                    lifecycleTransitionCheckbox.state() == NSCell.NSOnState ? Integer.valueOf(lifecycleTransitionPopup.selectedItem().representedObject()) : null,
                                    S3Object.STORAGE_CLASS_GLACIER,
                                    lifecycleDeleteCheckbox.state() == NSCell.NSOnState ? Integer.valueOf(lifecycleDeletePopup.selectedItem().representedObject()) : null)
                    );
                    return null;
                }

                @Override
                public void cleanup() {
                    super.cleanup();
                    toggleS3Settings(true);
                    initS3();
                }
            });
        }
    }

    public void setDistributionCnameField(NSTextField t) {
        this.distributionCnameField = t;
        notificationCenter.addObserver(this.id(),
                Foundation.selector("distributionApplyButtonClicked:"),
                NSControl.NSControlTextDidEndEditingNotification,
                distributionCnameField);
    }

    public void setDistributionOriginField(NSTextField t) {
        this.distributionOriginField = t;
        this.distributionOriginField.setAllowsEditingTextAttributes(true);
        this.distributionOriginField.setSelectable(true);
    }

    public void setDistributionStatusField(NSTextField t) {
        this.distributionStatusField = t;
    }

    public void setDistributionUrlField(NSTextField t) {
        this.distributionUrlField = t;
        this.distributionUrlField.setAllowsEditingTextAttributes(true);
        this.distributionUrlField.setSelectable(true);
    }

    public void setDistributionCnameUrlField(NSTextField t) {
        this.distributionCnameUrlField = t;
        this.distributionCnameUrlField.setAllowsEditingTextAttributes(true);
        this.distributionCnameUrlField.setSelectable(true);
    }

    public void setAclUrlField(NSTextField t) {
        this.aclUrlField = t;
        this.aclUrlField.setAllowsEditingTextAttributes(true);
        this.aclUrlField.setSelectable(true);
    }

    /**
     * Replace current metadata model. Will reload the table view.
     *
     * @param permissions The updated access control list
     */
    private void setAcl(List<Acl.UserAndRole> permissions) {
        this.acl.clear();
        this.acl.addAll(permissions);
        this.aclTable.reloadData();
    }

    public void setAclTable(final NSTableView t) {
        this.aclTable = t;
        this.aclTable.setAllowsMultipleSelection(true);
        this.aclPermissionCellPrototype.setFont(NSFont.systemFontOfSize(NSFont.smallSystemFontSize()));
        this.aclPermissionCellPrototype.setControlSize(NSCell.NSSmallControlSize);
        this.aclPermissionCellPrototype.setCompletes(false);
        this.aclPermissionCellPrototype.setBordered(false);
        this.aclPermissionCellPrototype.setButtonBordered(false);
        this.aclTable.setColumnAutoresizingStyle(NSTableView.NSTableViewUniformColumnAutoresizingStyle);
        this.aclTable.tableColumnWithIdentifier(AclColumns.PERMISSION.name()).setDataCell(aclPermissionCellPrototype);
        this.aclTable.setDataSource((aclTableModel = new ListDataSource() {
            @Override
            public NSInteger numberOfRowsInTableView(NSTableView view) {
                return new NSInteger(acl.size());
            }

            public NSObject tableView_objectValueForTableColumn_row(NSTableView view, NSTableColumn tableColumn,
                                                                    NSInteger row) {
                if(row.intValue() < acl.size()) {
                    final String identifier = tableColumn.identifier();
                    final Acl.UserAndRole grant = acl.get(row.intValue());
                    if(identifier.equals(AclColumns.GRANTEE.name())) {
                        return NSString.stringWithString(grant.getUser().getDisplayName());
                    }
                    if(identifier.equals(AclColumns.PERMISSION.name())) {
                        return NSString.stringWithString(grant.getRole().getName());
                    }
                }
                return null;
            }

            @Override
            public void tableView_setObjectValue_forTableColumn_row(NSTableView view, NSObject value,
                                                                    NSTableColumn c, NSInteger row) {
                if(row.intValue() < acl.size()) {
                    final Acl.UserAndRole grant = acl.get(row.intValue());
                    if(c.identifier().equals(AclColumns.GRANTEE.name())) {
                        grant.getUser().setIdentifier(value.toString());
                    }
                    if(c.identifier().equals(AclColumns.PERMISSION.name())) {
                        grant.getRole().setName(value.toString());
                    }
                    if(StringUtils.isNotBlank(grant.getUser().getIdentifier())
                            && StringUtils.isNotBlank(grant.getRole().getName())) {
                        InfoController.this.aclInputDidEndEditing();
                    }
                }
            }
        }).id());
        this.aclTable.setDelegate((aclTableDelegate = new AbstractTableDelegate<Acl.UserAndRole>(
                aclTable.tableColumnWithIdentifier(AclColumns.GRANTEE.name())
        ) {
            @Override
            public boolean isColumnRowEditable(NSTableColumn column, int row) {
                if(column.identifier().equals(AclColumns.GRANTEE.name())) {
                    final Acl.UserAndRole grant = acl.get(row);
                    if(grant.getUser().isEditable()) {
                        return true;
                    }
                    // Group Grantee identifier is not editable
                    return false;
                }
                if(column.identifier().equals(AclColumns.PERMISSION.name())) {
                    final Acl.UserAndRole grant = acl.get(row);
                    if(grant.getRole().isEditable()) {
                        return true;
                    }
                    // Static role that cannot be modified
                    return false;
                }
                return true;
            }

            @Override
            public void tableRowDoubleClicked(final ID sender) {
                this.enterKeyPressed(sender);
            }

            @Override
            public void enterKeyPressed(final ID sender) {
                aclTable.editRow(aclTable.columnWithIdentifier(AclColumns.GRANTEE.name()), aclTable.selectedRow(), true);
            }

            @Override
            public void deleteKeyPressed(final ID sender) {
                aclRemoveButtonClicked(sender);
            }

            public String tableView_toolTipForCell_rect_tableColumn_row_mouseLocation(NSTableView t, NSCell cell,
                                                                                      ID rect, NSTableColumn c,
                                                                                      NSInteger row, NSPoint mouseLocation) {
                return this.tooltip(acl.get(row.intValue()));
            }

            @Override
            public String tooltip(Acl.UserAndRole c) {
                return c.getUser().getIdentifier();
            }

            @Override
            public void tableColumnClicked(NSTableView view, NSTableColumn c) {
                //
            }

            @Override
            public void selectionDidChange(NSNotification notification) {
                aclRemoveButton.setEnabled(aclTable.numberOfSelectedRows().intValue() > 0);
            }

            public void tableView_willDisplayCell_forTableColumn_row(NSTableView view, NSTextFieldCell cell,
                                                                     NSTableColumn c, NSInteger row) {
                if(c.identifier().equals(AclColumns.GRANTEE.name())) {
                    final Acl.UserAndRole grant = acl.get(row.intValue());
                    cell.setPlaceholderString(grant.getUser().getPlaceholder());
                    if(grant.getUser().isEditable()) {
                        cell.setTextColor(NSColor.controlTextColor());
                    }
                    else {
                        // Group Grantee identifier is not editable
                        cell.setTextColor(NSColor.disabledControlTextColor());
                    }
                }
            }

            @Override
            protected boolean isTypeSelectSupported() {
                return false;
            }
        }).id());
        this.aclTable.sizeToFit();
    }

    public void setAclAddButton(NSPopUpButton b) {
        this.aclAddButton = b;
        this.aclAddButton.setTarget(this.id());
    }

    public void aclAddButtonClicked(NSMenuItem sender) {
        final AclPermission feature = session.getFeature(AclPermission.class);
        for(Acl.User grantee : feature.getAvailableAclUsers()) {
            if(sender.representedObject().equals(grantee.getPlaceholder())) {
                this.addAclItem(new Acl.UserAndRole(grantee, new Acl.Role(StringUtils.EMPTY)));
            }
        }
    }

    /**
     * Add to the table, reload data and select inserted row.
     *
     * @param update The acl to insert.
     */
    private void addAclItem(Acl.UserAndRole update) {
        List<Acl.UserAndRole> updated = new ArrayList<Acl.UserAndRole>(acl);
        final int index = updated.size();
        updated.add(index, update);
        this.setAcl(updated);
        aclTable.selectRowIndexes(NSIndexSet.indexSetWithIndex(new NSInteger(index)), false);
        if(update.getUser().isEditable()) {
            aclTable.editRow(aclTable.columnWithIdentifier(AclColumns.GRANTEE.name()), new NSInteger(index), true);
        }
        else {
            aclTable.editRow(aclTable.columnWithIdentifier(AclColumns.PERMISSION.name()), new NSInteger(index), true);
        }
    }

    public void setAclRemoveButton(NSButton b) {
        this.aclRemoveButton = b;
        // Only enable upon selection change
        this.aclRemoveButton.setEnabled(false);
        this.aclRemoveButton.setAction(Foundation.selector("aclRemoveButtonClicked:"));
        this.aclRemoveButton.setTarget(this.id());
    }

    @Action
    public void aclRemoveButtonClicked(ID sender) {
        List<Acl.UserAndRole> updated = new ArrayList<Acl.UserAndRole>(acl);
        NSIndexSet iterator = aclTable.selectedRowIndexes();
        List<Acl.UserAndRole> remove = new ArrayList<Acl.UserAndRole>();
        for(NSUInteger index = iterator.firstIndex(); !index.equals(NSIndexSet.NSNotFound); index = iterator.indexGreaterThanIndex(index)) {
            remove.add(updated.get(index.intValue()));
        }
        updated.removeAll(remove);
        this.setAcl(updated);
        this.aclInputDidEndEditing();
    }

    private void aclInputDidEndEditing() {
        if(this.toggleAclSettings(false)) {
            controller.background(new WorkerBackgroundAction<Boolean>(controller, session, cache,
                    new WriteAclWorker(files, new Acl(acl.toArray(new Acl.UserAndRole[acl.size()])), new AlertRecursiveCallback<Acl>(this), controller) {
                                @Override
                                public void cleanup(final Boolean v) {
                                    toggleAclSettings(true);
                                    initAcl();
                                }
                            }
                    )
            );
        }
    }

    /**
     * Replace current metadata model. Will reload the table view.
     *
     * @param m The new header key and values
     */
    private void setMetadata(List<Header> m) {
        metadata.clear();
        metadata.addAll(m);
        metadataTable.reloadData();
    }

    public void setMetadataTable(final NSTableView t) {
        this.metadataTable = t;
        this.metadataTable.setAllowsMultipleSelection(true);
        this.metadataTable.setColumnAutoresizingStyle(NSTableView.NSTableViewUniformColumnAutoresizingStyle);
        this.metadataTable.setDataSource((metadataTableModel = new ListDataSource() {
            @Override
            public NSInteger numberOfRowsInTableView(NSTableView view) {
                return new NSInteger(metadata.size());
            }

            public NSObject tableView_objectValueForTableColumn_row(NSTableView view, NSTableColumn tableColumn,
                                                                    NSInteger row) {
                if(row.intValue() < metadata.size()) {
                    final String identifier = tableColumn.identifier();
                    if(identifier.equals(MetadataColumns.NAME.name())) {
                        final String name = metadata.get(row.intValue()).getName();
                        return NSAttributedString.attributedString(StringUtils.isNotEmpty(name) ? name : StringUtils.EMPTY);
                    }
                    if(identifier.equals(MetadataColumns.VALUE.name())) {
                        final String value = metadata.get(row.intValue()).getValue();
                        if(StringUtils.isEmpty(value)) {
                            return null;
                        }
                        return NSAttributedString.attributedString(StringUtils.isNotEmpty(value) ? value : StringUtils.EMPTY);
                    }
                }
                return null;
            }

            @Override
            public void tableView_setObjectValue_forTableColumn_row(NSTableView view, NSObject value,
                                                                    NSTableColumn c, NSInteger row) {
                if(row.intValue() < metadata.size()) {
                    Header header = metadata.get(row.intValue());
                    if(c.identifier().equals(MetadataColumns.NAME.name())) {
                        header.setName(value.toString());
                    }
                    if(c.identifier().equals(MetadataColumns.VALUE.name())) {
                        header.setValue(value.toString());
                    }
                    if(StringUtils.isNotBlank(header.getName()) && StringUtils.isNotBlank(header.getValue())) {
                        // Only update if both fields are set
                        metadataInputDidEndEditing();
                    }
                }
            }
        }).id());
        this.metadataTable.setDelegate((metadataTableDelegate = new AbstractTableDelegate<String>(
                metadataTable.tableColumnWithIdentifier(MetadataColumns.NAME.name())
        ) {
            @Override
            public boolean isColumnRowEditable(NSTableColumn column, int row) {
                return true;
            }

            @Override
            public void tableRowDoubleClicked(final ID sender) {
                this.enterKeyPressed(sender);
            }

            @Override
            public void enterKeyPressed(final ID sender) {
                metadataTable.editRow(
                        metadataTable.columnWithIdentifier(MetadataColumns.VALUE.name()),
                        metadataTable.selectedRow(), true);
            }

            @Override
            public void deleteKeyPressed(final ID sender) {
                metadataRemoveButtonClicked(sender);
            }

            @Override
            public String tooltip(String c) {
                return c;
            }

            @Override
            public void tableColumnClicked(NSTableView view, NSTableColumn c) {
                //
            }

            @Override
            public void selectionDidChange(NSNotification notification) {
                metadataRemoveButton.setEnabled(metadataTable.numberOfSelectedRows().intValue() > 0);
            }

            @Override
            protected boolean isTypeSelectSupported() {
                return false;
            }

            public void tableView_willDisplayCell_forTableColumn_row(NSTableView view, NSTextFieldCell cell,
                                                                     NSTableColumn c, NSInteger row) {
                if(c.identifier().equals(MetadataColumns.VALUE.name())) {
                    final String value = metadata.get(row.intValue()).getValue();
                    if(null == value) {
                        cell.setPlaceholderString(LocaleFactory.localizedString("Multiple files"));
                    }
                }
            }
        }).id());
        this.metadataTable.sizeToFit();
    }

    public void setMetadataAddButton(NSPopUpButton b) {
        this.metadataAddButton = b;
        this.metadataAddButton.setTarget(this.id());
        this.metadataAddButton.addItemWithTitle(StringUtils.EMPTY);
        this.metadataAddButton.lastItem().setImage(IconCacheFactory.<NSImage>get().iconNamed("gear.tiff"));
        this.metadataAddButton.addItemWithTitle(LocaleFactory.localizedString("Custom Header", "S3"));
        this.metadataAddButton.lastItem().setAction(Foundation.selector("metadataAddCustomClicked:"));
        this.metadataAddButton.lastItem().setTarget(this.id());
        this.metadataAddButton.menu().addItem(NSMenuItem.separatorItem());
        this.metadataAddButton.addItemWithTitle("Content-Disposition");
        this.metadataAddButton.lastItem().setAction(Foundation.selector("metadataAddContentDispositionClicked:"));
        this.metadataAddButton.lastItem().setTarget(this.id());
        this.metadataAddButton.addItemWithTitle("Cache-Control");
        this.metadataAddButton.lastItem().setAction(Foundation.selector("metadataAddCacheControlClicked:"));
        this.metadataAddButton.lastItem().setTarget(this.id());
        this.metadataAddButton.addItemWithTitle("Expires");
        this.metadataAddButton.lastItem().setAction(Foundation.selector("metadataAddExpiresClicked:"));
        this.metadataAddButton.lastItem().setTarget(this.id());
        this.metadataAddButton.addItemWithTitle("Pragma");
        this.metadataAddButton.lastItem().setAction(Foundation.selector("metadataAddPragmaClicked:"));
        this.metadataAddButton.lastItem().setTarget(this.id());
        this.metadataAddButton.addItemWithTitle("Content-Type");
        this.metadataAddButton.lastItem().setAction(Foundation.selector("metadataAddContentTypeClicked:"));
        this.metadataAddButton.lastItem().setTarget(this.id());
        this.metadataAddButton.addItemWithTitle("x-amz-website-redirect-location");
        this.metadataAddButton.lastItem().setAction(Foundation.selector("metadataAddRedirectLocationClicked:"));
        this.metadataAddButton.lastItem().setTarget(this.id());
    }

    /**
     * Add a custom metadata header. This will be prefixed depending on the service.
     */
    @Action
    public void metadataAddCustomClicked(ID sender) {
        this.addMetadataItem();
    }

    @Action
    public void metadataAddContentDispositionClicked(ID sender) {
        this.addMetadataItem("Content-Disposition", "attachment");
    }

    @Action
    public void metadataAddCacheControlClicked(ID sender) {
        this.addMetadataItem("Cache-Control", "public,max-age=" + preferences.getInteger("s3.cache.seconds"));
    }

    @Action
    public void metadataAddContentTypeClicked(ID sender) {
        this.addMetadataItem("Content-Type", StringUtils.EMPTY, true);
    }

    @Action
    public void metadataAddRedirectLocationClicked(ID sender) {
        this.addMetadataItem("x-amz-website-redirect-location", StringUtils.EMPTY, true);
    }

    @Action
    public void metadataAddExpiresClicked(ID sender) {
        final Calendar time = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        time.add(Calendar.SECOND, preferences.getInteger("s3.cache.seconds"));
        this.addMetadataItem("Expires", new RFC1123DateFormatter().format(time.getTime(), TimeZone.getTimeZone("UTC")));
    }

    @Action
    public void metadataAddPragmaClicked(ID sender) {
        this.addMetadataItem("Pragma", StringUtils.EMPTY, true);
    }

    private void addMetadataItem() {
        this.addMetadataItem(StringUtils.EMPTY);
    }

    /**
     * Add new metadata row and selects the name column
     *
     * @param name Header name
     */
    private void addMetadataItem(String name) {
        this.addMetadataItem(name, StringUtils.EMPTY, false);
    }

    /**
     * Add new metadata row and selects the value column
     *
     * @param name  Header name
     * @param value Header value
     */
    private void addMetadataItem(String name, String value) {
        this.addMetadataItem(name, value, true);
    }

    /**
     * @param name        HTTP header name
     * @param value       HTTP header value
     * @param selectValue Select the value field or the name header field
     */
    private void addMetadataItem(String name, String value, boolean selectValue) {
        int row = metadata.size();
        List<Header> updated = new ArrayList<Header>(metadata);
        updated.add(row, new Header(name, value));
        this.setMetadata(updated);
        metadataTable.selectRowIndexes(NSIndexSet.indexSetWithIndex(new NSInteger(row)), false);
        metadataTable.editRow(
                selectValue ? metadataTable.columnWithIdentifier(MetadataColumns.VALUE.name()) : metadataTable.columnWithIdentifier(MetadataColumns.NAME.name()),
                new NSInteger(row), true);
    }

    public void setMetadataRemoveButton(NSButton b) {
        this.metadataRemoveButton = b;
        // Only enable upon selection change
        this.metadataRemoveButton.setEnabled(false);
        this.metadataRemoveButton.setAction(Foundation.selector("metadataRemoveButtonClicked:"));
        this.metadataRemoveButton.setTarget(this.id());
    }

    @Action
    public void metadataRemoveButtonClicked(ID sender) {
        List<Header> updated = new ArrayList<Header>(metadata);
        NSIndexSet iterator = metadataTable.selectedRowIndexes();
        List<Header> remove = new ArrayList<Header>();
        for(NSUInteger index = iterator.firstIndex(); !index.equals(NSIndexSet.NSNotFound); index = iterator.indexGreaterThanIndex(index)) {
            remove.add(updated.get(index.intValue()));
        }
        updated.removeAll(remove);
        this.setMetadata(updated);
        this.metadataInputDidEndEditing();
    }

    private void metadataInputDidEndEditing() {
        if(toggleMetadataSettings(false)) {
            final Map<String, String> update = new HashMap<String, String>();
            for(Header header : metadata) {
                update.put(header.getName(), header.getValue());
            }
            controller.background(new WorkerBackgroundAction<Boolean>(controller, session, cache,
                    new WriteMetadataWorker(files, update, new AlertRecursiveCallback<String>(this), controller) {
                                @Override
                                public void cleanup(final Boolean v) {
                                    toggleMetadataSettings(true);
                                    initMetadata();
                                }
                            }
                    )
            );
        }
    }

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

    public void setIconImageView(NSImageView iconImageView) {
        this.iconImageView = iconImageView;
    }

    public void setPanelMetadata(NSView v) {
        this.panelMetadata = v;
    }

    public void setPanelCloud(NSView v) {
        this.panelCloud = v;
    }

    public void setPanelDistribution(NSView v) {
        this.panelDistribution = v;
    }

    public void setPanelPermissions(NSView v) {
        this.panelPermissions = v;
    }

    public void setPanelAcl(NSView v) {
        this.panelAcl = v;
    }

    public void setPanelGeneral(NSView v) {
        this.panelGeneral = v;
    }

    private void initGeneral() {
        final int count = this.numberOfFiles();
        if(count > 0) {
            filenameField.setStringValue(this.getName());
            final Path file = getSelected();
            filenameField.setEnabled(1 == count
                    && session.getFeature(Move.class).isSupported(file));
            // Where
            String path;
            if(file.isSymbolicLink()) {
                path = file.getSymlinkTarget().getAbsolute();
            }
            else {
                path = file.getParent().getAbsolute();
            }
            this.updateField(pathField, path, TRUNCATE_MIDDLE_ATTRIBUTES);
            pathField.setToolTip(path);
            if(count > 1) {
                kindField.setStringValue(String.format("(%s)", LocaleFactory.localizedString("Multiple files")));
                checksumField.setStringValue(String.format("(%s)", LocaleFactory.localizedString("Multiple files")));
            }
            else {
                this.updateField(kindField, descriptor.getKind(file), TRUNCATE_MIDDLE_ATTRIBUTES);
            }
            // Timestamps
            if(count > 1) {
                modifiedField.setStringValue(String.format("(%s)", LocaleFactory.localizedString("Multiple files")));
                createdField.setStringValue(String.format("(%s)", LocaleFactory.localizedString("Multiple files")));
            }
            else {
                if(-1 == file.attributes().getModificationDate()) {
                    this.updateField(modifiedField, LocaleFactory.localizedString("Unknown"));
                }
                else {
                    this.updateField(modifiedField, UserDateFormatterFactory.get().getLongFormat(
                            file.attributes().getModificationDate()),
                            TRUNCATE_MIDDLE_ATTRIBUTES
                    );
                }
                if(-1 == file.attributes().getCreationDate()) {
                    this.updateField(createdField, LocaleFactory.localizedString("Unknown"));
                }
                else {
                    this.updateField(createdField, UserDateFormatterFactory.get().getLongFormat(
                            file.attributes().getCreationDate()),
                            TRUNCATE_MIDDLE_ATTRIBUTES
                    );
                }
            }
            // Owner
            this.updateField(ownerField, count > 1 ? String.format("(%s)", LocaleFactory.localizedString("Multiple files")) :
                            StringUtils.isBlank(file.attributes().getOwner()) ? LocaleFactory.localizedString("Unknown") : file.attributes().getOwner(),
                    TRUNCATE_MIDDLE_ATTRIBUTES
            );
            this.updateField(groupField, count > 1 ? String.format("(%s)", LocaleFactory.localizedString("Multiple files")) :
                            StringUtils.isBlank(file.attributes().getGroup()) ? LocaleFactory.localizedString("Unknown") : file.attributes().getGroup(),
                    TRUNCATE_MIDDLE_ATTRIBUTES
            );
            // Icon
            if(count > 1) {
                iconImageView.setImage(IconCacheFactory.<NSImage>get().iconNamed("NSMultipleDocuments", 32));
            }
            else {
                if(this.getSelected().isVolume()) {
                    iconImageView.setImage(IconCacheFactory.<NSImage>get().volumeIcon(session.getHost().getProtocol(), 32));
                }
                else {
                    iconImageView.setImage(IconCacheFactory.<NSImage>get().fileIcon(this.getSelected(), 32));
                }
            }
        }
        // Sum of files
        this.initSize();
        this.initChecksum();
        // Read HTTP URL
        this.initWebUrl();
    }

    private void initWebUrl() {
        // Web URL
        if(this.numberOfFiles() > 1) {
            this.updateField(webUrlField, String.format("(%s)", LocaleFactory.localizedString("Multiple files")));
            webUrlField.setToolTip(StringUtils.EMPTY);
        }
        else {
            this.updateField(webUrlField, LocaleFactory.localizedString("Unknown"));
            final Path file = this.getSelected();
            final DescriptiveUrl http = session.getFeature(UrlProvider.class).toUrl(file).find(DescriptiveUrl.Type.http);
            if(!http.equals(DescriptiveUrl.EMPTY)) {
                webUrlField.setAttributedStringValue(HyperlinkAttributedStringFactory.create(http));
                webUrlField.setToolTip(LocaleFactory.localizedString("Open in Web Browser"));
            }
        }
    }

    /**
     *
     */
    private void initPermissions() {
        permissionsField.setStringValue(LocaleFactory.localizedString("Unknown"));
        // Disable Apply button and start progress indicator
        if(this.togglePermissionSettings(false)) {
            controller.background(new WorkerBackgroundAction<List<Permission>>(controller, session, cache,
                    new ReadPermissionWorker(files) {
                        @Override
                        public void cleanup(final List<Permission> permissions) {
                            setPermissions(permissions);
                            togglePermissionSettings(true);
                        }
                    }
            ));
        }
    }

    private void setPermissions(final List<Permission> permissions) {
        boolean overwrite = true;
        for(Permission permission : permissions) {
            updateCheckbox(ownerr, overwrite, permission.getUser().implies(Permission.Action.read));
            updateCheckbox(ownerw, overwrite, permission.getUser().implies(Permission.Action.write));
            updateCheckbox(ownerx, overwrite, permission.getUser().implies(Permission.Action.execute));

            updateCheckbox(groupr, overwrite, permission.getGroup().implies(Permission.Action.read));
            updateCheckbox(groupw, overwrite, permission.getGroup().implies(Permission.Action.write));
            updateCheckbox(groupx, overwrite, permission.getGroup().implies(Permission.Action.execute));

            updateCheckbox(otherr, overwrite, permission.getOther().implies(Permission.Action.read));
            updateCheckbox(otherw, overwrite, permission.getOther().implies(Permission.Action.write));
            updateCheckbox(otherx, overwrite, permission.getOther().implies(Permission.Action.execute));

            // For more than one file selected, take into account permissions of previous file
            overwrite = false;
        }
        final int count = permissions.size();
        if(count > 1) {
            permissionsField.setStringValue(String.format("(%s)", LocaleFactory.localizedString("Multiple files")));
        }
        else {
            for(Permission permission : permissions) {
                permissionsField.setStringValue(permission.toString());
                octalField.setStringValue(permission.getMode());
            }
        }
    }

    /**
     * @param checkbox  The checkbox to update
     * @param overwrite Overwrite previous state
     * @param on        Set the checkbox to on state
     */
    private void updateCheckbox(NSButton checkbox, boolean overwrite, boolean on) {
        // Sets the cell's state to value, which can be NSCell.NSOnState, NSCell.NSOffState, or NSCell.MixedState.
        // If necessary, this method also redraws the receiver.
        if((checkbox.state() == NSCell.NSOffState || overwrite) && !on) {
            checkbox.setState(NSCell.NSOffState);
        }
        else if((checkbox.state() == NSCell.NSOnState || overwrite) && on) {
            checkbox.setState(NSCell.NSOnState);
        }
        else {
            checkbox.setState(NSCell.NSMixedState);
        }
        checkbox.setEnabled(true);
    }

    /**
     * Read content distribution settings
     */
    private void initDistribution() {
        distributionStatusField.setStringValue(LocaleFactory.localizedString("Unknown"));
        distributionCnameField.cell().setPlaceholderString(LocaleFactory.localizedString("None"));
        distributionOriginField.setStringValue(LocaleFactory.localizedString("Unknown"));
        distributionUrlField.setStringValue(LocaleFactory.localizedString("Unknown"));
        distributionInvalidationStatusField.setStringValue(LocaleFactory.localizedString("None"));

        // Remember last selection
        final String selected = distributionDeliveryPopup.titleOfSelectedItem();

        distributionDeliveryPopup.removeAllItems();
        distributionDeliveryPopup.addItemWithTitle(LocaleFactory.localizedString("None"));
        distributionDefaultRootPopup.removeAllItems();
        distributionDefaultRootPopup.addItemWithTitle(LocaleFactory.localizedString("None"));
        distributionDefaultRootPopup.menu().addItem(NSMenuItem.separatorItem());

        final Path container = containerService.getContainer(getSelected());

        final DistributionConfiguration cdn = session.getFeature(DistributionConfiguration.class);
        distributionEnableButton.setTitle(MessageFormat.format(LocaleFactory.localizedString("Enable {0} Distribution", "Status"),
                cdn.getName()));
        distributionDeliveryPopup.removeItemWithTitle(LocaleFactory.localizedString("None"));
        for(Distribution.Method method : cdn.getMethods(container)) {
            distributionDeliveryPopup.addItemWithTitle(method.toString());
            distributionDeliveryPopup.itemWithTitle(method.toString()).setRepresentedObject(method.toString());
        }

        distributionDeliveryPopup.selectItemWithTitle(selected);
        if(null == distributionDeliveryPopup.selectedItem()) {
            // Select first distribution option
            Distribution.Method method = cdn.getMethods(container).iterator().next();
            distributionDeliveryPopup.selectItemWithTitle(method.toString());
        }

        distributionLoggingPopup.removeAllItems();
        distributionLoggingPopup.addItemWithTitle(LocaleFactory.localizedString("None"));
        distributionLoggingPopup.itemWithTitle(LocaleFactory.localizedString("None")).setEnabled(false);

        distributionAnalyticsSetupUrlField.setStringValue(LocaleFactory.localizedString("None"));

        this.distributionStatusButtonClicked(null);
    }

    /**
     * Updates the size field by iterating over all files and
     * reading the cached size value in the attributes of the path
     */
    private void initSize() {
        if(this.toggleSizeSettings(false)) {
            controller.background(new WorkerBackgroundAction<Long>(controller, session, cache,
                    new ReadSizeWorker(files) {
                        @Override
                        public void cleanup(final Long size) {
                            updateSize(size);
                            toggleSizeSettings(true);
                        }
                    }
            ));
        }
    }

    private void updateSize(long size) {
        sizeField.setAttributedStringValue(NSAttributedString.attributedStringWithAttributes(
                SizeFormatterFactory.get().format(size, true),
                TRUNCATE_MIDDLE_ATTRIBUTES));
    }

    private void initChecksum() {
        if(this.numberOfFiles() > 1) {
            checksumField.setStringValue(String.format("(%s)", LocaleFactory.localizedString("Multiple files")));
        }
        else {
            final Path file = this.getSelected();
            final Checksum checksum = file.attributes().getChecksum();
            if(checksum == null) {
                checksumField.setStringValue(LocaleFactory.localizedString("Unknown"));
            }
            else {
                this.updateField(checksumField, checksum.hash, TRUNCATE_MIDDLE_ATTRIBUTES);
            }
        }
    }

    /**
     * Toggle settings before and after update
     *
     * @param stop Enable controls and stop progress spinner
     * @return True if progress animation has started and settings are toggled
     */
    private boolean toggleS3Settings(final boolean stop) {
        this.window().endEditingFor(null);
        final Credentials credentials = session.getHost().getCredentials();
        boolean enable = session.getHost().getProtocol().getType() == Protocol.Type.s3
                || session.getHost().getProtocol().getType() == Protocol.Type.googlestorage;
        if(enable) {
            enable = !credentials.isAnonymousLogin();
        }
        boolean logging = false;
        boolean analytics = false;
        boolean versioning = false;
        boolean storageclass = false;
        boolean encryption = false;
        boolean lifecycle = false;
        if(enable) {
            logging = session.getFeature(Logging.class) != null;
            analytics = session.getFeature(AnalyticsProvider.class) != null;
            versioning = session.getFeature(Versioning.class) != null;
            lifecycle = session.getFeature(Lifecycle.class) != null;
            encryption = session.getFeature(Encryption.class) != null;
            storageclass = session.getFeature(Redundancy.class) != null;
        }
        storageClassPopup.setEnabled(stop && enable && storageclass);
        encryptionPopup.setEnabled(stop && enable && encryption);
        bucketVersioningButton.setEnabled(stop && enable && versioning);
        bucketMfaButton.setEnabled(stop && enable && versioning
                && bucketVersioningButton.state() == NSCell.NSOnState);
        bucketLoggingButton.setEnabled(stop && enable && logging);
        bucketLoggingPopup.setEnabled(stop && enable && logging);
        if(analytics && Objects.equals(session.getFeature(IdentityConfiguration.class).getCredentials(
                session.getFeature(AnalyticsProvider.class).getName()), credentials)) {
            // No need to create new IAM credentials when same as session credentials
            bucketAnalyticsButton.setEnabled(false);
        }
        else {
            bucketAnalyticsButton.setEnabled(stop && enable && analytics);
        }
        lifecycleDeletePopup.setEnabled(stop && enable && lifecycle);
        lifecycleDeleteCheckbox.setEnabled(stop && enable && lifecycle);
        lifecycleTransitionPopup.setEnabled(stop && enable && lifecycle);
        lifecycleTransitionCheckbox.setEnabled(stop && enable && lifecycle);
        if(stop) {
            s3Progress.stopAnimation(null);
        }
        else if(enable) {
            s3Progress.startAnimation(null);
        }
        return enable;
    }

    /**
     *
     */
    private void initS3() {
        bucketLocationField.setStringValue(LocaleFactory.localizedString("Unknown"));
        bucketAnalyticsSetupUrlField.setStringValue(LocaleFactory.localizedString("None"));

        bucketLoggingPopup.removeAllItems();
        bucketLoggingPopup.addItemWithTitle(LocaleFactory.localizedString("None"));
        bucketLoggingPopup.lastItem().setEnabled(false);

        s3PublicUrlField.setStringValue(LocaleFactory.localizedString("None"));
        s3PublicUrlValidityField.setStringValue(LocaleFactory.localizedString("Unknown"));
        s3torrentUrlField.setStringValue(LocaleFactory.localizedString("None"));

        storageClassPopup.removeAllItems();
        storageClassPopup.addItemWithTitle(LocaleFactory.localizedString("Unknown"));
        storageClassPopup.lastItem().setEnabled(false);
        storageClassPopup.selectItem(storageClassPopup.lastItem());

        encryptionPopup.removeAllItems();
        encryptionPopup.addItemWithTitle(LocaleFactory.localizedString("Unknown"));
        encryptionPopup.lastItem().setEnabled(false);
        encryptionPopup.selectItem(encryptionPopup.lastItem());

        if(this.toggleS3Settings(false)) {
            if(session.getFeature(Redundancy.class) != null) {
                for(String redundancy : session.getFeature(Redundancy.class).getClasses()) {
                    storageClassPopup.addItemWithTitle(LocaleFactory.localizedString(redundancy, "S3"));
                    storageClassPopup.lastItem().setRepresentedObject(redundancy);
                }
            }
            if(this.numberOfFiles() > 1) {
                s3PublicUrlField.setStringValue(String.format("(%s)", LocaleFactory.localizedString("Multiple files")));
                s3PublicUrlField.setToolTip(StringUtils.EMPTY);
                s3torrentUrlField.setStringValue(String.format("(%s)", LocaleFactory.localizedString("Multiple files")));
                s3torrentUrlField.setToolTip(StringUtils.EMPTY);
            }
            else {
                Path file = this.getSelected();
                final DescriptiveUrl signed = session.getFeature(UrlProvider.class).toUrl(file).find(DescriptiveUrl.Type.signed);
                if(!signed.equals(DescriptiveUrl.EMPTY)) {
                    s3PublicUrlField.setAttributedStringValue(HyperlinkAttributedStringFactory.create(signed));
                    s3PublicUrlField.setToolTip(signed.getHelp());
                    s3PublicUrlValidityField.setStringValue(signed.getHelp());
                }
                final DescriptiveUrl torrent = session.getFeature(UrlProvider.class).toUrl(file).find(DescriptiveUrl.Type.torrent);
                if(!torrent.equals(DescriptiveUrl.EMPTY)) {
                    s3torrentUrlField.setAttributedStringValue(HyperlinkAttributedStringFactory.create(torrent));
                    s3torrentUrlField.setToolTip(torrent.getHelp());
                }
            }
            controller.background(new RegistryBackgroundAction<Void>(controller, session, cache) {
                Location.Name location;
                LoggingConfiguration logging;
                VersioningConfiguration versioning;
                final Set<String> containers = new HashSet<String>();
                // Available encryption keys in KMS
                Set<Encryption.Algorithm> managedEncryptionKeys = new HashSet<Encryption.Algorithm>();
                final Set<Encryption.Algorithm> selectedEncryptionKeys = new HashSet<Encryption.Algorithm>();
                final Set<String> selectedStorageClasses = new HashSet<String>();
                LifecycleConfiguration lifecycle;
                Credentials credentials;

                @Override
                public Void run() throws BackgroundException {
                    final Path container = containerService.getContainer(getSelected());
                    if(session.getFeature(Location.class) != null) {
                        location = session.getFeature(Location.class).getLocation(container);
                    }
                    if(session.getFeature(Logging.class) != null) {
                        logging = session.getFeature(Logging.class).getConfiguration(container);
                        for(Path c : session.list(containerService.getContainer(getSelected()).getParent(), new DisabledListProgressListener())) {
                            containers.add(c.getName());
                        }
                    }
                    if(session.getFeature(Versioning.class) != null) {
                        versioning = session.getFeature(Versioning.class).getConfiguration(container);
                    }
                    if(session.getFeature(Lifecycle.class) != null) {
                        lifecycle = session.getFeature(Lifecycle.class).getConfiguration(container);
                    }
                    if(session.getFeature(AnalyticsProvider.class) != null && session.getFeature(IdentityConfiguration.class) != null) {
                        credentials = session.getFeature(IdentityConfiguration.class)
                                .getCredentials(session.getFeature(AnalyticsProvider.class).getName());
                    }
                    if(session.getFeature(Redundancy.class) != null) {
                        for(final Path file : files) {
                            selectedStorageClasses.add(session.getFeature(Redundancy.class).getClass(file));
                        }
                    }
                    if(session.getFeature(Encryption.class) != null) {
                        // Add additional keys stored in KMS
                        managedEncryptionKeys = session.getFeature(Encryption.class).getKeys(container, prompt);
                        for(final Path file : files) {
                            selectedEncryptionKeys.add(session.getFeature(Encryption.class).getEncryption(file));
                        }
                        managedEncryptionKeys.addAll(selectedEncryptionKeys);
                    }
                    return null;
                }

                @Override
                public void cleanup() {
                    super.cleanup();
                    if(logging != null) {
                        bucketLoggingButton.setState(logging.isEnabled() ? NSCell.NSOnState : NSCell.NSOffState);
                        if(!containers.isEmpty()) {
                            bucketLoggingPopup.removeAllItems();
                        }
                        for(String c : containers) {
                            bucketLoggingPopup.addItemWithTitle(c);
                            bucketLoggingPopup.lastItem().setRepresentedObject(c);
                        }
                        if(logging.isEnabled()) {
                            bucketLoggingPopup.selectItemWithTitle(logging.getLoggingTarget());
                        }
                        else {
                            // Default to write log files to origin bucket
                            bucketLoggingPopup.selectItemWithTitle(containerService.getContainer(getSelected()).getName());
                        }
                    }
                    if(location != null) {
                        bucketLocationField.setStringValue(location.toString());
                    }
                    if(versioning != null) {
                        bucketVersioningButton.setState(versioning.isEnabled() ? NSCell.NSOnState : NSCell.NSOffState);
                        bucketMfaButton.setState(versioning.isMultifactor() ? NSCell.NSOnState : NSCell.NSOffState);
                    }

                    for(Encryption.Algorithm algorithm : managedEncryptionKeys) {
                        encryptionPopup.addItemWithTitle(LocaleFactory.localizedString(algorithm.getDescription(), "S3"));
                        encryptionPopup.lastItem().setRepresentedObject(algorithm.toString());
                    }
                    if(!selectedEncryptionKeys.isEmpty()) {
                        encryptionPopup.selectItemAtIndex(new NSInteger(-1));
                        if(-1 != encryptionPopup.indexOfItemWithTitle(LocaleFactory.localizedString("Unknown")).intValue()) {
                            encryptionPopup.removeItemWithTitle(LocaleFactory.localizedString("Unknown"));
                        }

                    }
                    for(Encryption.Algorithm algorithm : selectedEncryptionKeys) {
                        encryptionPopup.selectItemAtIndex(encryptionPopup.indexOfItemWithRepresentedObject(algorithm.toString()));
                    }
                    for(Encryption.Algorithm algorithm : selectedEncryptionKeys) {
                        encryptionPopup.itemAtIndex(encryptionPopup.indexOfItemWithRepresentedObject(algorithm.toString()))
                                .setState(selectedEncryptionKeys.size() == 1 ? NSCell.NSOnState : NSCell.NSMixedState);
                    }

                    if(!selectedStorageClasses.isEmpty()) {
                        storageClassPopup.selectItemAtIndex(new NSInteger(-1));
                        if(-1 != storageClassPopup.indexOfItemWithTitle(LocaleFactory.localizedString("Unknown")).intValue()) {
                            storageClassPopup.removeItemWithTitle(LocaleFactory.localizedString("Unknown"));
                        }
                    }
                    for(String storageClass : selectedStorageClasses) {
                        storageClassPopup.selectItemAtIndex(storageClassPopup.indexOfItemWithRepresentedObject(storageClass));
                    }
                    for(String storageClass : selectedStorageClasses) {
                        storageClassPopup.itemAtIndex(storageClassPopup.indexOfItemWithRepresentedObject(storageClass))
                                .setState(selectedStorageClasses.size() == 1 ? NSCell.NSOnState : NSCell.NSMixedState);
                    }

                    if(null != credentials) {
                        bucketAnalyticsSetupUrlField.setAttributedStringValue(HyperlinkAttributedStringFactory.create(
                                session.getFeature(AnalyticsProvider.class).getSetup(session.getHost().getProtocol().getDefaultHostname(),
                                        session.getHost().getProtocol().getScheme(),
                                        containerService.getContainer(getSelected()).getName(), credentials)
                        ));
                    }
                    bucketAnalyticsButton.setState(null != credentials ? NSCell.NSOnState : NSCell.NSOffState);
                    if(lifecycle != null) {
                        lifecycleDeleteCheckbox.setState(lifecycle.getExpiration() != null ? NSCell.NSOnState : NSCell.NSOffState);
                        if(lifecycle.getExpiration() != null) {
                            final NSInteger index = lifecycleDeletePopup.indexOfItemWithRepresentedObject(String.valueOf(lifecycle.getExpiration()));
                            if(-1 == index.intValue()) {
                                lifecycleDeletePopup.addItemWithTitle(MessageFormat.format(LocaleFactory.localizedString("after {0} Days", "S3"), String.valueOf(lifecycle.getExpiration())));
                                lifecycleDeletePopup.lastItem().setAction(Foundation.selector("lifecyclePopupClicked:"));
                                lifecycleDeletePopup.lastItem().setTarget(id());
                                lifecycleDeletePopup.lastItem().setRepresentedObject(String.valueOf(lifecycle.getExpiration()));
                            }
                            lifecycleDeletePopup.selectItemAtIndex(lifecycleDeletePopup.indexOfItemWithRepresentedObject(String.valueOf(lifecycle.getExpiration())));
                        }
                        lifecycleTransitionCheckbox.setState(lifecycle.getTransition() != null ? NSCell.NSOnState : NSCell.NSOffState);
                        if(lifecycle.getTransition() != null) {
                            final NSInteger index = lifecycleTransitionPopup.indexOfItemWithRepresentedObject(String.valueOf(lifecycle.getTransition()));
                            if(-1 == index.intValue()) {
                                lifecycleTransitionPopup.addItemWithTitle(MessageFormat.format(LocaleFactory.localizedString("after {0} Days", "S3"), String.valueOf(lifecycle.getTransition())));
                                lifecycleTransitionPopup.lastItem().setAction(Foundation.selector("lifecyclePopupClicked:"));
                                lifecycleTransitionPopup.lastItem().setTarget(id());
                                lifecycleTransitionPopup.lastItem().setRepresentedObject(String.valueOf(lifecycle.getTransition()));
                            }
                            lifecycleTransitionPopup.selectItemAtIndex(lifecycleTransitionPopup.indexOfItemWithRepresentedObject(String.valueOf(lifecycle.getTransition())));
                        }
                    }
                    toggleS3Settings(true);
                }

                @Override
                public String getActivity() {
                    return MessageFormat.format(LocaleFactory.localizedString("Reading metadata of {0}", "Status"),
                            this.toString(files));
                }
            });
        }
    }

    /**
     * Toggle settings before and after update
     *
     * @param stop Enable controls and stop progress spinner
     * @return True if progress animation has started and settings are toggled
     */
    private boolean toggleAclSettings(final boolean stop) {
        this.window().endEditingFor(null);
        final Credentials credentials = session.getHost().getCredentials();
        boolean enable = !credentials.isAnonymousLogin() && session.getFeature(AclPermission.class) != null;
        aclTable.setEnabled(stop && enable);
        aclAddButton.setEnabled(stop && enable);
        boolean selection = aclTable.selectedRowIndexes().count().intValue() > 0;
        aclRemoveButton.setEnabled(stop && enable && selection);
        if(stop) {
            aclProgress.stopAnimation(null);
        }
        else if(enable) {
            aclProgress.startAnimation(null);
        }
        return enable;
    }

    /**
     * Toggle settings before and after update
     *
     * @param stop Enable controls and stop progress spinner
     * @return True if progress animation has started and settings are toggled
     */
    private boolean toggleMetadataSettings(final boolean stop) {
        this.window().endEditingFor(null);
        final Credentials credentials = session.getHost().getCredentials();
        boolean enable = !credentials.isAnonymousLogin() && session.getFeature(Headers.class) != null;
        metadataTable.setEnabled(stop && enable);
        metadataAddButton.setEnabled(stop && enable);
        boolean selection = metadataTable.selectedRowIndexes().count().intValue() > 0;
        metadataRemoveButton.setEnabled(stop && enable && selection);
        if(stop) {
            metadataProgress.stopAnimation(null);
        }
        else if(enable) {
            metadataProgress.startAnimation(null);
        }
        return enable;
    }

    /**
     * Read custom metadata HTTP headers from cloud provider
     */
    private void initMetadata() {
        this.setMetadata(Collections.emptyList());
        if(this.toggleMetadataSettings(false)) {
            controller.background(new WorkerBackgroundAction<Map<String, String>>(controller, session, cache,
                    new ReadMetadataWorker(files) {
                        @Override
                        public void cleanup(final Map<String, String> updated) {
                            final List<Header> m = new ArrayList<Header>();
                            if(updated != null) {
                                for(Map.Entry<String, String> key : updated.entrySet()) {
                                    m.add(new Header(key.getKey(), key.getValue()));
                                }
                            }
                            setMetadata(m);
                            toggleMetadataSettings(true);
                        }
                    }
            ));
        }
    }

    /**
     * Read grants in the background
     */
    private void initAcl() {
        this.setAcl(Collections.emptyList());
        aclUrlField.setStringValue(LocaleFactory.localizedString("None"));
        if(this.toggleAclSettings(false)) {
            final AclPermission feature = session.getFeature(AclPermission.class);
            aclAddButton.removeAllItems();
            this.aclAddButton.addItemWithTitle(StringUtils.EMPTY);
            this.aclAddButton.lastItem().setImage(IconCacheFactory.<NSImage>get().iconNamed("gear.tiff"));
            for(Acl.User user : feature.getAvailableAclUsers()) {
                this.aclAddButton.addItemWithTitle(user.getPlaceholder());
                this.aclAddButton.lastItem().setAction(Foundation.selector("aclAddButtonClicked:"));
                this.aclAddButton.lastItem().setTarget(this.id());
                this.aclAddButton.lastItem().setRepresentedObject(user.getPlaceholder());
            }
            aclPermissionCellPrototype.removeAllItems();
            for(Acl.Role permission : feature.getAvailableAclRoles(files)) {
                aclPermissionCellPrototype.addItemWithObjectValue(NSString.stringWithString(permission.getName()));
            }
            if(this.numberOfFiles() > 1) {
                aclUrlField.setStringValue(String.format("(%s)", LocaleFactory.localizedString("Multiple files")));
                aclUrlField.setToolTip(StringUtils.EMPTY);
            }
            else {
                for(Path file : files) {
                    if(file.isFile()) {
                        final DescriptiveUrl authenticated = session.getFeature(UrlProvider.class).toUrl(file).find(DescriptiveUrl.Type.authenticated);
                        if(!authenticated.equals(DescriptiveUrl.EMPTY)) {
                            aclUrlField.setAttributedStringValue(HyperlinkAttributedStringFactory.create(authenticated));
                            aclUrlField.setToolTip(authenticated.getHelp());
                        }
                    }
                }
            }
            controller.background(new WorkerBackgroundAction<List<Acl.UserAndRole>>(controller, session, cache,
                    new ReadAclWorker(files) {
                        @Override
                        public void cleanup(final List<Acl.UserAndRole> updated) {
                            if(updated != null) {
                                setAcl(updated);
                            }
                            toggleAclSettings(true);
                        }
                    }
            ));
        }
    }

    /**
     * Selected files in browser.
     *
     * @return The number of selected files to display information for.
     */
    private int numberOfFiles() {
        return null == files ? 0 : files.size();
    }

    @Action
    public void octalPermissionsInputDidEndEditing(NSNotification sender) {
        final Permission permission = this.getPermissionFromOctalField();
        if(null == permission) {
            AppKitFunctionsLibrary.beep();
            this.initPermissions();
        }
        else {
            boolean change = false;
            for(Path file : files) {
                if(!file.attributes().getPermission().equals(permission)) {
                    change = true;
                }
            }
            if(change) {
                this.setPermissions(Collections.singletonList(permission));
                this.changePermissions(permission, false);
            }
        }
    }

    /**
     * Permission value from input field.
     *
     * @return Null if invalid string has been entered entered,
     */
    private Permission getPermissionFromOctalField() {
        if(StringUtils.isNotBlank(octalField.stringValue())) {
            if(StringUtils.length(octalField.stringValue()) >= 3) {
                if(StringUtils.isNumeric(octalField.stringValue())) {
                    return new Permission(Integer.valueOf(octalField.stringValue()).intValue());
                }
            }
        }
        log.warn(String.format("Invalid octal field input %s", octalField.stringValue()));
        return null;
    }

    @Action
    public void recursiveButtonClicked(final NSButton sender) {
        final Permission permission = this.getPermissionFromOctalField();
        if(null == permission) {
            AppKitFunctionsLibrary.beep();
            this.initPermissions();
        }
        else {
            this.changePermissions(permission, true);
        }
    }

    @Action
    public void permissionSelectionChanged(final NSButton sender) {
        if(sender.state() == NSCell.NSMixedState) {
            sender.setState(NSCell.NSOnState);
        }
        final Permission p = this.getPermissionFromCheckboxes();
        this.setPermissions(Collections.singletonList(p));
        this.changePermissions(p, false);
    }

    /**
     * Permission selection from checkboxes.
     *
     * @return Never null.
     */
    private Permission getPermissionFromCheckboxes() {
        Permission.Action u = Permission.Action.none;
        if(ownerr.state() == NSCell.NSOnState) {
            u = u.or(Permission.Action.read);
        }
        if(ownerw.state() == NSCell.NSOnState) {
            u = u.or(Permission.Action.write);
        }
        if(ownerx.state() == NSCell.NSOnState) {
            u = u.or(Permission.Action.execute);
        }
        Permission.Action g = Permission.Action.none;
        if(groupr.state() == NSCell.NSOnState) {
            g = g.or(Permission.Action.read);
        }
        if(groupw.state() == NSCell.NSOnState) {
            g = g.or(Permission.Action.write);
        }
        if(groupx.state() == NSCell.NSOnState) {
            g = g.or(Permission.Action.execute);
        }
        Permission.Action o = Permission.Action.none;
        if(otherr.state() == NSCell.NSOnState) {
            o = o.or(Permission.Action.read);
        }
        if(otherw.state() == NSCell.NSOnState) {
            o = o.or(Permission.Action.write);
        }
        if(otherx.state() == NSCell.NSOnState) {
            o = o.or(Permission.Action.execute);
        }
        return new Permission(u, g, o);
    }

    /**
     * @param permission UNIX permissions to apply to files
     * @param recursive  Recursively apply to child of directories
     */
    private void changePermissions(final Permission permission, final boolean recursive) {
        if(this.togglePermissionSettings(false)) {
            controller.background(new WorkerBackgroundAction<Boolean>(controller, session, cache,
                    new WritePermissionWorker(files, permission, recursive ? new AlertRecursiveCallback<Permission>(this) : new BooleanRecursiveCallback<Permission>(false), controller) {
                                @Override
                                public void cleanup(final Boolean v) {
                                    togglePermissionSettings(true);
                                }
                            }
                    )
            );
        }
    }

    /**
     * Toggle settings before and after update
     *
     * @param stop Enable controls and stop progress spinner
     * @return True if controls are enabled for the given protocol in idle state
     */
    private boolean togglePermissionSettings(final boolean stop) {
        this.window().endEditingFor(null);
        final Credentials credentials = session.getHost().getCredentials();
        boolean enable = !credentials.isAnonymousLogin() && session.getFeature(UnixPermission.class) != null;
        recursiveButton.setEnabled(stop && enable);
        for(Path next : files) {
            if(next.isFile()) {
                recursiveButton.setEnabled(false);
                break;
            }
        }
        octalField.setEnabled(stop && enable);
        ownerr.setEnabled(stop && enable);
        ownerw.setEnabled(stop && enable);
        ownerx.setEnabled(stop && enable);
        groupr.setEnabled(stop && enable);
        groupw.setEnabled(stop && enable);
        groupx.setEnabled(stop && enable);
        otherr.setEnabled(stop && enable);
        otherw.setEnabled(stop && enable);
        otherx.setEnabled(stop && enable);
        if(stop) {
            permissionProgress.stopAnimation(null);
        }
        else if(enable) {
            permissionProgress.startAnimation(null);
        }
        return enable;
    }

    /**
     * Toggle settings before and after update
     *
     * @param stop Enable controls and stop progress spinner
     * @return True if controls are enabled for the given protocol in idle state
     */
    private boolean toggleDistributionSettings(final boolean stop) {
        this.window().endEditingFor(null);
        final Credentials credentials = session.getHost().getCredentials();
        final DistributionConfiguration cdn = session.getFeature(DistributionConfiguration.class);
        boolean enable = !credentials.isAnonymousLogin() && cdn != null;
        final Path container = containerService.getContainer(getSelected());
        if(enable) {
            // Not enabled if multiple files selected with not same parent container
            for(Path next : files) {
                if(containerService.getContainer(next).equals(container)) {
                    continue;
                }
                enable = false;
                break;
            }
        }
        Distribution.Method method = Distribution.Method.forName(distributionDeliveryPopup.selectedItem().representedObject());
        distributionEnableButton.setEnabled(stop && enable);
        distributionDeliveryPopup.setEnabled(stop && enable);
        distributionLoggingButton.setEnabled(stop && enable && cdn.getFeature(DistributionLogging.class, method) != null);
        if(enable) {
            final AnalyticsProvider analyticsFeature = cdn.getFeature(AnalyticsProvider.class, method);
            final IdentityConfiguration identityFeature = cdn.getFeature(IdentityConfiguration.class, method);
            if(null == analyticsFeature || null == identityFeature) {
                distributionAnalyticsButton.setEnabled(false);
            }
            else {
                if(Objects.equals(identityFeature.getCredentials(analyticsFeature.getName()), credentials)) {
                    // No need to create new IAM credentials when same as session credentials
                    distributionAnalyticsButton.setEnabled(false);
                }
                else {
                    distributionAnalyticsButton.setEnabled(stop);
                }
            }
        }
        else {
            distributionAnalyticsButton.setEnabled(false);
        }
        distributionLoggingPopup.setEnabled(stop && enable && cdn.getFeature(DistributionLogging.class, method) != null);
        distributionCnameField.setEnabled(stop && enable && cdn.getFeature(Cname.class, method) != null);
        distributionInvalidateObjectsButton.setEnabled(stop && enable && cdn.getFeature(Purge.class, method) != null);
        distributionDefaultRootPopup.setEnabled(stop && enable && cdn.getFeature(Index.class, method) != null);
        if(stop) {
            distributionProgress.stopAnimation(null);
        }
        else if(enable) {
            distributionProgress.startAnimation(null);
        }
        return enable;
    }

    @Action
    public void distributionInvalidateObjectsButtonClicked(final ID sender) {
        if(this.toggleDistributionSettings(false)) {
            controller.background(new RegistryBackgroundAction<Void>(controller, session, cache) {
                @Override
                public Void run() throws BackgroundException {
                    Distribution.Method method = Distribution.Method.forName(distributionDeliveryPopup.selectedItem().representedObject());
                    final Path container = containerService.getContainer(getSelected());
                    final DistributionConfiguration cdn = session.getFeature(DistributionConfiguration.class);
                    final Purge feature = cdn.getFeature(Purge.class, method);
                    feature.invalidate(container, method, files, prompt);
                    return null;
                }

                @Override
                public void cleanup() {
                    super.cleanup();
                    // Refresh the current distribution status
                    distributionStatusButtonClicked(sender);
                }

                @Override
                public String getActivity() {
                    return MessageFormat.format(LocaleFactory.localizedString("Writing CDN configuration of {0}", "Status"),
                            containerService.getContainer(getSelected()).getName());
                }
            });
        }
    }

    @Action
    public void distributionLoggingPopupClicked(final ID sender) {
        if(distributionLoggingButton.state() == NSCell.NSOnState) {
            // Only write change if logging is already enabled
            this.distributionApplyButtonClicked(sender);
        }
    }

    @Action
    public void distributionApplyButtonClicked(final ID sender) {
        if(this.toggleDistributionSettings(false)) {
            controller.background(new RegistryBackgroundAction<Void>(controller, session, cache) {
                @Override
                public Void run() throws BackgroundException {
                    Distribution.Method method = Distribution.Method.forName(distributionDeliveryPopup.selectedItem().representedObject());
                    final Path container = containerService.getContainer(getSelected());
                    final DistributionConfiguration cdn = session.getFeature(DistributionConfiguration.class);
                    final Distribution configuration = new Distribution(method, distributionEnableButton.state() == NSCell.NSOnState);
                    configuration.setIndexDocument(distributionDefaultRootPopup.selectedItem().representedObject());
                    configuration.setLogging(distributionLoggingButton.state() == NSCell.NSOnState);
                    configuration.setLoggingContainer(distributionLoggingPopup.selectedItem().representedObject());
                    configuration.setCNAMEs(StringUtils.split(distributionCnameField.stringValue()));
                    cdn.write(container, configuration, prompt);
                    return null;
                }

                @Override
                public void cleanup() {
                    super.cleanup();
                    // Refresh the current distribution status
                    distributionStatusButtonClicked(sender);
                }

                @Override
                public String getActivity() {
                    return MessageFormat.format(LocaleFactory.localizedString("Writing CDN configuration of {0}", "Status"),
                            getSelected().getName());
                }
            });
        }
    }

    @Action
    public void distributionStatusButtonClicked(final ID sender) {
        if(this.toggleDistributionSettings(false)) {
            final Path container = containerService.getContainer(getSelected());
            final Distribution.Method method
                    = Distribution.Method.forName(distributionDeliveryPopup.selectedItem().representedObject());
            final List<Path> rootDocuments = new ArrayList<Path>();
            controller.background(new RegistryBackgroundAction<Distribution>(controller, session, cache) {
                private Distribution distribution = new Distribution(method, false);

                @Override
                public Distribution run() throws BackgroundException {
                    final DistributionConfiguration cdn = session.getFeature(DistributionConfiguration.class);
                    distribution = cdn.read(container, method, prompt);
                    if(cdn.getFeature(Index.class, distribution.getMethod()) != null) {
                        // Make sure container items are cached for default root object.
                        rootDocuments.addAll(session.list(container, new DisabledListProgressListener()));
                    }
                    return distribution;
                }

                @Override
                public void cleanup() {
                    super.cleanup();
                    final Path file = getSelected();
                    final Path container = containerService.getContainer(file);
                    final DistributionConfiguration cdn = session.getFeature(DistributionConfiguration.class);
                    distributionEnableButton.setTitle(MessageFormat.format(LocaleFactory.localizedString("Enable {0} Distribution", "Status"),
                            cdn.getName(distribution.getMethod())));
                    distributionEnableButton.setState(distribution.isEnabled() ? NSCell.NSOnState : NSCell.NSOffState);
                    distributionStatusField.setAttributedStringValue(NSMutableAttributedString.create(distribution.getStatus(), TRUNCATE_MIDDLE_ATTRIBUTES));

                    distributionLoggingButton.setState(distribution.isLogging() ? NSCell.NSOnState : NSCell.NSOffState);
                    final List<Path> containers = distribution.getContainers();
                    if(!containers.isEmpty()) {
                        distributionLoggingPopup.removeAllItems();
                    }
                    for(Path c : containers) {
                        // Populate with list of available logging targets
                        distributionLoggingPopup.addItemWithTitle(c.getName());
                        distributionLoggingPopup.lastItem().setRepresentedObject(c.getName());
                    }
                    if(StringUtils.isNotBlank(distribution.getLoggingContainer())) {
                        // Select configured logging container if any
                        distributionLoggingPopup.selectItemWithTitle(distribution.getLoggingContainer());
                    }
                    else {
                        if(distributionLoggingPopup.itemWithTitle(container.getName()) != null) {
                            distributionLoggingPopup.selectItemWithTitle(container.getName());
                        }
                    }
                    if(null == distributionLoggingPopup.selectedItem()) {
                        distributionLoggingPopup.selectItemWithTitle(LocaleFactory.localizedString("None"));
                    }
                    final AnalyticsProvider analyticsFeature = cdn.getFeature(AnalyticsProvider.class, method);
                    final IdentityConfiguration identityFeature = cdn.getFeature(IdentityConfiguration.class, method);
                    if(analyticsFeature != null && identityFeature != null) {
                        final Credentials credentials = identityFeature.getCredentials(analyticsFeature.getName());
                        distributionAnalyticsButton.setState(credentials != null ? NSCell.NSOnState : NSCell.NSOffState);
                        if(credentials != null) {
                            distributionAnalyticsSetupUrlField.setAttributedStringValue(
                                    HyperlinkAttributedStringFactory.create(analyticsFeature.getSetup(cdn.getHostname(),
                                            distribution.getMethod().getScheme(), container.getName(), credentials))
                            );
                        }
                    }
                    final DescriptiveUrl origin = cdn.toUrl(file).find(DescriptiveUrl.Type.origin);
                    if(!origin.equals(DescriptiveUrl.EMPTY)) {
                        distributionOriginField.setAttributedStringValue(HyperlinkAttributedStringFactory.create(origin));
                    }
                    // Concatenate URLs
                    if(numberOfFiles() > 1) {
                        distributionUrlField.setStringValue(String.format("(%s)", LocaleFactory.localizedString("Multiple files")));
                        distributionUrlField.setToolTip(StringUtils.EMPTY);
                        distributionCnameUrlField.setStringValue(String.format("(%s)", LocaleFactory.localizedString("Multiple files")));
                    }
                    else {
                        final DescriptiveUrl url = cdn.toUrl(file).find(DescriptiveUrl.Type.cdn);
                        if(!url.equals(DescriptiveUrl.EMPTY)) {
                            distributionUrlField.setAttributedStringValue(HyperlinkAttributedStringFactory.create(url));
                            distributionUrlField.setToolTip(LocaleFactory.localizedString("CDN URL"));
                        }
                        else {
                            distributionUrlField.setStringValue(LocaleFactory.localizedString("None"));
                            distributionUrlField.setToolTip(StringUtils.EMPTY);
                        }
                    }
                    final String[] cnames = distribution.getCNAMEs();
                    if(0 == cnames.length) {
                        distributionCnameField.setStringValue(StringUtils.EMPTY);
                        distributionCnameUrlField.setStringValue(StringUtils.EMPTY);
                        distributionCnameUrlField.setToolTip(StringUtils.EMPTY);
                    }
                    else {
                        distributionCnameField.setStringValue(StringUtils.join(cnames, ' '));
                        final DescriptiveUrl url = cdn.toUrl(file).find(DescriptiveUrl.Type.cname);
                        if(!url.equals(DescriptiveUrl.EMPTY)) {
                            // We only support one CNAME URL to be displayed
                            distributionCnameUrlField.setAttributedStringValue(HyperlinkAttributedStringFactory.create(url));
                            distributionCnameUrlField.setToolTip(LocaleFactory.localizedString("CDN URL"));
                        }
                    }
                    if(cdn.getFeature(Index.class, distribution.getMethod()) != null) {
                        for(Path next : rootDocuments) {
                            if(next.isFile()) {
                                distributionDefaultRootPopup.addItemWithTitle(next.getName());
                                distributionDefaultRootPopup.lastItem().setRepresentedObject(next.getName());
                            }
                        }
                    }
                    if(StringUtils.isNotBlank(distribution.getIndexDocument())) {
                        if(null == distributionDefaultRootPopup.itemWithTitle(distribution.getIndexDocument())) {
                            distributionDefaultRootPopup.addItemWithTitle(distribution.getIndexDocument());
                        }
                        distributionDefaultRootPopup.selectItemWithTitle(distribution.getIndexDocument());
                    }
                    else {
                        distributionDefaultRootPopup.selectItemWithTitle(LocaleFactory.localizedString("None"));
                    }
                    StringBuilder tooltip = new StringBuilder();
                    for(Iterator<Path> iter = files.iterator(); iter.hasNext(); ) {
                        Path f = iter.next();
                        tooltip.append(f.getAbsolute());
                        if(iter.hasNext()) {
                            tooltip.append("\n");
                        }
                    }
                    distributionInvalidateObjectsButton.setToolTip(tooltip.toString());
                    distributionInvalidationStatusField.setStringValue(distribution.getInvalidationStatus());
                    toggleDistributionSettings(true);
                }

                @Override
                public String getActivity() {
                    return MessageFormat.format(LocaleFactory.localizedString("Reading CDN configuration of {0}", "Status"),
                            getSelected().getName());
                }
            });
        }
    }

    public void setDistributionAnalyticsButton(NSButton b) {
        this.distributionAnalyticsButton = b;
        this.distributionAnalyticsButton.setAction(Foundation.selector("distributionAnalyticsButtonClicked:"));
    }

    @Action
    public void distributionAnalyticsButtonClicked(final NSButton sender) {
        if(this.toggleDistributionSettings(false)) {
            controller.background(new RegistryBackgroundAction<Void>(controller, session, cache) {
                @Override
                public Void run() throws BackgroundException {
                    if(distributionAnalyticsButton.state() == NSCell.NSOnState) {
                        final String document = preferences.getProperty("analytics.provider.qloudstat.iam.policy");
                        session.getFeature(IdentityConfiguration.class).create(session.getFeature(AnalyticsProvider.class).getName(), document, prompt);
                    }
                    else {
                        session.getFeature(IdentityConfiguration.class).delete(session.getFeature(AnalyticsProvider.class).getName(), prompt);
                    }
                    return null;
                }

                @Override
                public void cleanup() {
                    super.cleanup();
                    toggleDistributionSettings(true);
                    initDistribution();
                }
            });
        }
    }

    public void setDistributionAnalyticsSetupUrlField(NSTextField f) {
        this.distributionAnalyticsSetupUrlField = f;
        this.distributionAnalyticsSetupUrlField.setAllowsEditingTextAttributes(true);
        this.distributionAnalyticsSetupUrlField.setSelectable(true);
    }

    @Action
    public void calculateSizeButtonClicked(final ID sender) {
        if(this.toggleSizeSettings(false)) {
            controller.background(new WorkerBackgroundAction<Long>(controller, session, cache,
                    new CalculateSizeWorker(files, controller) {
                        @Override
                        public void cleanup(final Long size) {
                            updateSize(size);
                            toggleSizeSettings(true);
                        }

                        @Override
                        protected void update(final long size) {
                            invoke(new WindowMainAction(InfoController.this) {
                                @Override
                                public void run() {
                                    updateSize(size);
                                }
                            });
                        }
                    }
            ));
        }
    }

    /**
     * @param stop Enable controls and stop progress spinner
     * @return True if progress animation has started and settings are toggled
     */
    private boolean toggleSizeSettings(final boolean stop) {
        this.window().endEditingFor(null);
        sizeButton.setEnabled(false);
        for(Path next : files) {
            if(next.isDirectory()) {
                sizeButton.setEnabled(stop);
                break;
            }
        }
        if(stop) {
            sizeProgress.stopAnimation(null);
        }
        else {
            sizeProgress.startAnimation(null);
        }
        return true;
    }

    @Override
    @Action
    public void helpButtonClicked(final NSButton sender) {
        final StringBuilder site = new StringBuilder(preferences.getProperty("website.help"));
        switch(InfoToolbarItem.valueOf(this.getSelectedTab())) {
            case info:
                site.append("/howto/info");
                BrowserLauncherFactory.get().open(site.toString());
                break;
            case permissions:
                site.append("/howto/permissions");
                BrowserLauncherFactory.get().open(site.toString());
                break;
            case acl:
                site.append("/howto/acl");
                BrowserLauncherFactory.get().open(site.toString());
                break;
            case distribution:
                site.append("/howto/cdn");
                BrowserLauncherFactory.get().open(site.toString());
                break;
            default:
                new DefaultProviderHelpService().help(session.getHost().getProtocol());
        }
    }

    private enum AclColumns {
        GRANTEE,
        PERMISSION,
    }

    private enum MetadataColumns {
        NAME,
        VALUE
    }

    private enum InfoToolbarItem {
        /**
         * General
         */
        info,
        permissions,
        acl,
        distribution,
        s3,
        metadata
    }
}
