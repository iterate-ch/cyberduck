package ch.cyberduck.ui.cocoa.controller;

/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
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
import ch.cyberduck.binding.foundation.NSNotification;
import ch.cyberduck.binding.foundation.NSNotificationCenter;
import ch.cyberduck.binding.foundation.NSObject;
import ch.cyberduck.binding.foundation.NSString;
import ch.cyberduck.core.*;
import ch.cyberduck.core.cdn.Distribution;
import ch.cyberduck.core.cdn.DistributionConfiguration;
import ch.cyberduck.core.cdn.DistributionUrlProvider;
import ch.cyberduck.core.cdn.features.Cname;
import ch.cyberduck.core.cdn.features.DistributionLogging;
import ch.cyberduck.core.cdn.features.Index;
import ch.cyberduck.core.cdn.features.Purge;
import ch.cyberduck.core.date.RFC1123DateFormatter;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConflictException;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.features.AclPermission;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Encryption;
import ch.cyberduck.core.features.Lifecycle;
import ch.cyberduck.core.features.Location;
import ch.cyberduck.core.features.Logging;
import ch.cyberduck.core.features.Metadata;
import ch.cyberduck.core.features.Move;
import ch.cyberduck.core.features.Redundancy;
import ch.cyberduck.core.features.TransferAcceleration;
import ch.cyberduck.core.features.UnixPermission;
import ch.cyberduck.core.features.Versioning;
import ch.cyberduck.core.formatter.SizeFormatterFactory;
import ch.cyberduck.core.io.Checksum;
import ch.cyberduck.core.lifecycle.LifecycleConfiguration;
import ch.cyberduck.core.local.BrowserLauncherFactory;
import ch.cyberduck.core.local.FileDescriptor;
import ch.cyberduck.core.local.FileDescriptorFactory;
import ch.cyberduck.core.local.TemporaryFileService;
import ch.cyberduck.core.local.TemporaryFileServiceFactory;
import ch.cyberduck.core.logging.LoggingConfiguration;
import ch.cyberduck.core.pool.SessionPool;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.resources.IconCacheFactory;
import ch.cyberduck.core.s3.S3Protocol;
import ch.cyberduck.core.threading.QuicklookTransferBackgroundAction;
import ch.cyberduck.core.threading.RegistryBackgroundAction;
import ch.cyberduck.core.threading.WindowMainAction;
import ch.cyberduck.core.threading.WorkerBackgroundAction;
import ch.cyberduck.core.transfer.TransferItem;
import ch.cyberduck.core.worker.*;
import ch.cyberduck.ui.cocoa.callback.PromptRecursiveCallback;
import ch.cyberduck.ui.quicklook.QuickLook;
import ch.cyberduck.ui.quicklook.QuickLookFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.rococoa.Foundation;
import org.rococoa.ID;
import org.rococoa.Rococoa;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

public class InfoController extends ToolbarWindowController {
    private static final Logger log = LogManager.getLogger(InfoController.class);

    private static NSPoint cascade = new NSPoint(0, 0);

    private final Controller controller;
    private final SessionPool session;
    private final NSComboBoxCell aclPermissionCellPrototype = NSComboBoxCell.comboBoxCell();
    private final NSNotificationCenter notificationCenter
            = NSNotificationCenter.defaultCenter();
    private final QuickLook quicklook
            = QuickLookFactory.get();
    private final TemporaryFileService temporary
            = TemporaryFileServiceFactory.instance();

    /**
     * Selected files
     */
    private List<Path> files;

    private final FileDescriptor descriptor = FileDescriptorFactory.get();
    private final LoginCallback prompt = LoginCallbackFactory.get(this);

    private final PathContainerService containerService
            = new DefaultPathContainerService();

    private final Preferences preferences
            = PreferencesFactory.get();

    /**
     * Grant editing model.
     */
    private final List<Acl.UserAndRole> acl = new ArrayList<>();
    /**
     * Custom HTTP headers for REST protocols
     */
    private final List<Header> metadata = new ArrayList<>();

    /**
     * Previous versions of selected file
     */
    private final AttributedList<Path> versions = new AttributedList<>();

    private final ReloadCallback reload;

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
    private NSProgressIndicator versionsProgress;
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
    private NSButton bucketTransferAccelerationButton;
    @Outlet
    private NSButton bucketVersioningButton;
    @Outlet
    private NSButton bucketMfaButton;
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
    private NSTableView aclTable;
    @Delegate
    private ListDataSource aclTableModel;
    @Delegate
    private AbstractTableDelegate<Acl.UserAndRole, AclColumn> aclTableDelegate;
    @Outlet
    private NSPopUpButton aclAddButton;
    @Outlet
    private NSButton aclRemoveButton;
    @Outlet
    private NSTableView metadataTable;
    @Delegate
    private ListDataSource metadataTableModel;
    @Delegate
    private AbstractTableDelegate<String, MetadataColumn> metadataTableDelegate;
    @Outlet
    private NSPopUpButton metadataAddButton;
    @Outlet
    private NSButton metadataRemoveButton;
    @Outlet
    private NSTableView versionsTable;
    @Delegate
    private ListDataSource versionsTableModel;
    @Delegate
    private AbstractTableDelegate<String, MetadataColumn> versionsTableDelegate;
    @Outlet
    private NSButton versionsRevertButton;
    @Outlet
    private NSButton versionsDeleteButton;
    @Outlet
    private NSButton versionsQuicklookButton;
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
    private NSView panelVersions;

    public InfoController(final Controller controller, final SessionPool session, final List<Path> files, final ReloadCallback reload) {
        this.controller = controller;
        this.session = session;
        this.files = files;
        this.reload = new DelegatingReloadCallback(new InternalVersionsReloadCallback(), reload);
    }

    @Override
    public void invalidate() {
        temporary.shutdown();
        quicklook.close();
        super.invalidate();
    }

    private Path getSelected() {
        for(Path file : files) {
            return file;
        }
        throw new IllegalArgumentException();
    }

    @Override
    public void setWindow(final NSWindow window) {
        window.setFrameAutosaveName("Info");
        window.setHidesOnDeactivate(false);
        window.setShowsResizeIndicator(true);
        window.setContentMinSize(window.frame().size);
        window.setContentMaxSize(new NSSize(600, window.frame().size.height.doubleValue()));
        if(window.respondsToSelector(Foundation.selector("setToolbarStyle:"))) {
            window.setToolbarStyle(NSWindow.NSWindowToolbarStyle.NSWindowToolbarStyleExpanded);
        }
        super.setWindow(window);
        if(!preferences.getBoolean("browser.info.inspector")) {
            cascade = this.cascade(cascade);
        }
    }

    @Override
    public void windowWillClose(final NSNotification notification) {
        cascade = new NSPoint(this.window().frame().origin.x.doubleValue(), this.window().frame().origin.y.doubleValue() + this.window().frame().size.height.doubleValue());
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
            item = InfoToolbarItem.general;
        }
        switch(item) {
            case general:
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
            case versions:
                this.initVersions();
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
                    item.setImage(IconCacheFactory.<NSImage>get().iconNamed(session.getHost().getProtocol().icon(), 32));
                }
                else {
                    // CloudFront is the default for custom distributions
                    item.setImage(IconCacheFactory.<NSImage>get().iconNamed(new S3Protocol().icon(), 32));
                }
                break;
            case s3:
                // Set icon of cloud service provider
                item.setLabel(session.getHost().getProtocol().getName());
                item.setToolTip(session.getHost().getProtocol().getName());
                item.setImage(IconCacheFactory.<NSImage>get().iconNamed(session.getHost().getProtocol().icon(), 32));
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
                        || session.getHost().getProtocol().getType() == Protocol.Type.b2
                        || session.getHost().getProtocol().getType() == Protocol.Type.azure
                        || session.getHost().getProtocol().getType() == Protocol.Type.googlestorage;
            case metadata:
                if(anonymous) {
                    return false;
                }
                // Not enabled if not a cloud session
                return session.getFeature(Metadata.class) != null;
            case versions:
                return session.getFeature(Versioning.class) != null;
        }
        return true;
    }

    @Override
    public String getWindowTitleForSelectedTab(final NSTabViewItem item) {
        return String.format("%s – %s", item.label(), this.getName());
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
        this.setWindowTitle(this.getWindowTitleForSelectedTab(tabView.selectedTabViewItem()));
    }

    @Override
    protected Map<Label, NSView> getPanels() {
        final Map<Label, NSView> views = new LinkedHashMap<>();
        this.addPanel(views, InfoToolbarItem.general, panelGeneral);
        this.addPanel(views, InfoToolbarItem.versions, panelVersions);
        if(session.getFeature(AclPermission.class) != null) {
            this.addPanel(views, InfoToolbarItem.acl, panelAcl);
        }
        else {
            this.addPanel(views, InfoToolbarItem.permissions, panelPermissions);
        }
        this.addPanel(views, InfoToolbarItem.metadata, panelMetadata);
        this.addPanel(views, InfoToolbarItem.distribution, panelDistribution);
        this.addPanel(views, InfoToolbarItem.s3, panelCloud);
        return views;
    }

    private void addPanel(final Map<Label, NSView> views, final InfoToolbarItem item, final NSView panel) {
        if(preferences.getBoolean(String.format("info.%s.enable", item.name()))) {
            switch(item) {
                case s3:
                    views.put(new Label(item.name(), session.getHost().getProtocol().getName(), item.image()), panel);
                    break;
                default:
                    views.put(new Label(item.name(), item.label(), item.image()), panel);
                    break;
            }
        }
    }

    private String getName() {
        final int count = this.numberOfFiles();
        if(count > 1) {
            return String.format("(%s)", LocaleFactory.localizedString("Multiple files"));
        }
        final Path file = this.getSelected();
        return file.getName();
    }

    public void setFilenameField(NSTextField filenameField) {
        this.filenameField = filenameField;
        this.filenameField.setSelectable(true);
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

    public void setOctalField(NSTextField t) {
        this.octalField = t;
        notificationCenter.addObserver(this.id(),
                Foundation.selector("octalPermissionsInputDidEndEditing:"),
                NSControl.NSControlTextDidEndEditingNotification,
                t.id());
    }

    public void setOwnerField(NSTextField t) {
        this.ownerField = t;
    }

    public void setSizeField(NSTextField t) {
        this.sizeField = t;
    }

    public void setChecksumField(NSTextField t) {
        this.checksumField = t;
    }

    public void setPathField(NSTextField t) {
        this.pathField = t;
    }

    public void setWebUrlField(NSTextField t) {
        this.webUrlField = t;
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

    public void setVersionsProgress(NSProgressIndicator versionsProgress) {
        this.versionsProgress = versionsProgress;
        this.versionsProgress.setDisplayedWhenStopped(false);
        this.versionsProgress.setStyle(NSProgressIndicator.NSProgressIndicatorSpinningStyle);
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
            this.background(new WorkerBackgroundAction<>(controller, session,
                            new WriteRedundancyWorker(files, redundancy, new PromptRecursiveCallback<>(this), controller) {
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
            this.background(new WorkerBackgroundAction<>(controller, session,
                            new WriteEncryptionWorker(files, encryption, new PromptRecursiveCallback<>(this), controller) {
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
            final LoggingConfiguration configuration = new LoggingConfiguration(
                    bucketLoggingButton.state() == NSCell.NSOnState,
                    null == bucketLoggingPopup.selectedItem() ? null : bucketLoggingPopup.selectedItem().representedObject()
            );
            this.background(new WorkerBackgroundAction<>(controller, session, new WriteLoggingWorker(files, configuration) {
                @Override
                public void cleanup(final Boolean result) {
                    toggleS3Settings(true);
                    initS3();
                }
            }));
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

    public void setBucketVersioningButton(NSButton b) {
        this.bucketVersioningButton = b;
        this.bucketVersioningButton.setAction(Foundation.selector("bucketVersioningButtonClicked:"));
    }

    @Action
    public void bucketVersioningButtonClicked(final NSButton sender) {
        if(this.toggleS3Settings(false)) {
            final VersioningConfiguration configuration = new VersioningConfiguration(
                    bucketVersioningButton.state() == NSCell.NSOnState,
                    bucketMfaButton.state() == NSCell.NSOnState);
            this.background(new WorkerBackgroundAction<>(controller, session, new WriteVersioningWorker(files, prompt, configuration) {
                @Override
                public void cleanup(final Boolean result) {
                    toggleS3Settings(true);
                    initS3();
                }
            }));
        }
    }

    public void setBucketMfaButton(NSButton b) {
        this.bucketMfaButton = b;
        this.bucketMfaButton.setAction(Foundation.selector("bucketMfaButtonClicked:"));
    }

    @Action
    public void bucketMfaButtonClicked(final NSButton sender) {
        this.bucketVersioningButtonClicked(sender);
    }

    public void setBucketTransferAccelerationButton(final NSButton bucketTransferAccelerationButton) {
        this.bucketTransferAccelerationButton = bucketTransferAccelerationButton;
        this.bucketTransferAccelerationButton.setAction(Foundation.selector("bucketTransferAccelerationButtonClicked:"));
    }

    @Action
    public void bucketTransferAccelerationButtonClicked(final NSButton sender) {
        if(this.toggleS3Settings(false)) {
            this.background(new WorkerBackgroundAction<>(controller, session,
                            new WriteTransferAccelerationWorker(files, bucketTransferAccelerationButton.state() == NSCell.NSOnState) {
                                @Override
                                public void cleanup(final Boolean done) {
                                    super.cleanup(done);
                                    toggleS3Settings(true);
                                    initS3();
                                }
                            }
                    )
            );
        }
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
            final LifecycleConfiguration configuration = new LifecycleConfiguration(
                    lifecycleTransitionCheckbox.state() == NSCell.NSOnState ? Integer.valueOf(lifecycleTransitionPopup.selectedItem().representedObject()) : null,
                    lifecycleDeleteCheckbox.state() == NSCell.NSOnState ? Integer.valueOf(lifecycleDeletePopup.selectedItem().representedObject()) : null);
            this.background(new WorkerBackgroundAction<>(controller, session, new WriteLifecycleWorker(files, configuration) {
                @Override
                public void cleanup(final Boolean result) {
                    toggleS3Settings(true);
                    initS3();
                }
            }));
        }
    }

    public void setDistributionCnameField(NSTextField t) {
        this.distributionCnameField = t;
        notificationCenter.addObserver(this.id(),
                Foundation.selector("distributionApplyButtonClicked:"),
                NSControl.NSControlTextDidEndEditingNotification,
                t.id());
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
        this.aclTable.tableColumnWithIdentifier(AclColumn.PERMISSION.name()).setDataCell(aclPermissionCellPrototype);
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
                    if(identifier.equals(AclColumn.GRANTEE.name())) {
                        return NSString.stringWithString(grant.getUser().getDisplayName());
                    }
                    if(identifier.equals(AclColumn.PERMISSION.name())) {
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
                    if(c.identifier().equals(AclColumn.GRANTEE.name())) {
                        grant.getUser().setIdentifier(value.toString());
                    }
                    if(c.identifier().equals(AclColumn.PERMISSION.name())) {
                        grant.getRole().setName(value.toString());
                    }
                    if(StringUtils.isNotBlank(grant.getUser().getIdentifier())
                            && StringUtils.isNotBlank(grant.getRole().getName())) {
                        InfoController.this.aclInputDidEndEditing();
                    }
                }
            }
        }).id());
        this.aclTable.setDelegate((aclTableDelegate = new AbstractTableDelegate<Acl.UserAndRole, AclColumn>(
                aclTable.tableColumnWithIdentifier(AclColumn.GRANTEE.name())
        ) {
            @Override
            public boolean isColumnRowEditable(NSTableColumn column, NSInteger row) {
                if(column.identifier().equals(AclColumn.GRANTEE.name())) {
                    final Acl.UserAndRole grant = acl.get(row.intValue());
                    if(grant.getUser().isEditable()) {
                        return true;
                    }
                    // Group Grantee identifier is not editable
                    return false;
                }
                if(column.identifier().equals(AclColumn.PERMISSION.name())) {
                    final Acl.UserAndRole grant = acl.get(row.intValue());
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
                aclTable.editRow(aclTable.columnWithIdentifier(AclColumn.GRANTEE.name()), aclTable.selectedRow(), true);
            }

            @Override
            public void deleteKeyPressed(final ID sender) {
                aclRemoveButtonClicked(sender);
            }

            public String tableView_toolTipForCell_rect_tableColumn_row_mouseLocation(NSTableView t, NSCell cell,
                                                                                      ID rect, NSTableColumn c,
                                                                                      NSInteger row, NSPoint mouseLocation) {
                return this.tooltip(acl.get(row.intValue()), AclColumn.valueOf(c.identifier()));
            }

            @Override
            public String tooltip(Acl.UserAndRole c, final AclColumn column) {
                return c.getUser().getIdentifier();
            }

            @Override
            public void tableColumnClicked(NSTableView view, NSTableColumn c) {
                //
            }

            @Override
            public void selectionDidChange(final NSNotification notification) {
                validateAclActions(true);
            }

            public void tableView_willDisplayCell_forTableColumn_row(NSTableView view, NSCell cell,
                                                                     NSTableColumn c, NSInteger row) {
                final Acl.UserAndRole grant = acl.get(row.intValue());
                if(c.identifier().equals(AclColumn.GRANTEE.name())) {
                    final NSTextFieldCell textFieldCell = Rococoa.cast(cell, NSTextFieldCell.class);
                    textFieldCell.setPlaceholderString(grant.getUser().getPlaceholder());
                    if(grant.getUser().isEditable()) {
                        textFieldCell.setTextColor(NSColor.controlTextColor());
                    }
                    else {
                        // Group Grantee identifier is not editable
                        textFieldCell.setTextColor(NSColor.disabledControlTextColor());
                    }
                }
                if(c.identifier().equals(AclColumn.PERMISSION.name())) {
                    if(grant.getRole().isEditable()) {
                        cell.setEnabled(true);
                    }
                    else {
                        cell.setEnabled(false);
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
        List<Acl.UserAndRole> updated = new ArrayList<>(acl);
        final int index = updated.size();
        updated.add(index, update);
        this.setAcl(updated);
        aclTable.selectRowIndexes(NSIndexSet.indexSetWithIndex(new NSInteger(index)), false);
        if(update.getUser().isEditable()) {
            aclTable.editRow(aclTable.columnWithIdentifier(AclColumn.GRANTEE.name()), new NSInteger(index), true);
        }
        else {
            aclTable.editRow(aclTable.columnWithIdentifier(AclColumn.PERMISSION.name()), new NSInteger(index), true);
        }
    }

    public void setAclRemoveButton(NSButton b) {
        this.aclRemoveButton = b;
        this.aclRemoveButton.setAction(Foundation.selector("aclRemoveButtonClicked:"));
        this.aclRemoveButton.setTarget(this.id());
    }

    @Action
    public void aclRemoveButtonClicked(ID sender) {
        List<Acl.UserAndRole> updated = new ArrayList<>(acl);
        NSIndexSet iterator = aclTable.selectedRowIndexes();
        List<Acl.UserAndRole> remove = new ArrayList<>();
        for(NSUInteger index = iterator.firstIndex(); !index.equals(NSIndexSet.NSNotFound); index = iterator.indexGreaterThanIndex(index)) {
            remove.add(updated.get(index.intValue()));
        }
        updated.removeAll(remove);
        this.setAcl(updated);
        this.aclInputDidEndEditing();
    }

    private void aclInputDidEndEditing() {
        if(this.toggleAclSettings(false)) {
            this.background(new WorkerBackgroundAction<>(controller, session,
                            new WriteAclWorker(files, new Acl(acl.toArray(new Acl.UserAndRole[acl.size()])), new PromptRecursiveCallback<>(this), controller) {
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

    private void setVersions(AttributedList<Path> versions) {
        this.versions.clear();
        this.versions.addAll(versions);
        versionsTable.reloadData();
    }

    public void setVersionsTable(NSTableView t) {
        this.versionsTable = t;
        this.versionsTable.setAllowsMultipleSelection(false);
        this.versionsTable.setColumnAutoresizingStyle(NSTableView.NSTableViewUniformColumnAutoresizingStyle);
        this.versionsTable.setDataSource((versionsTableModel = new ListDataSource() {
            @Override
            public NSInteger numberOfRowsInTableView(NSTableView view) {
                return new NSInteger(versions.size());
            }

            public NSObject tableView_objectValueForTableColumn_row(NSTableView view, NSTableColumn tableColumn, NSInteger row) {
                if(row.intValue() < versions.size()) {
                    final String identifier = tableColumn.identifier();
                    if(identifier.equals(VersionsColumn.TIMESTAMP.name())) {
                        final String timestamp = UserDateFormatterFactory.get().getMediumFormat(versions.get(row.intValue()).attributes().getModificationDate());
                        return NSAttributedString.attributedStringWithAttributes(StringUtils.isNotEmpty(timestamp) ? timestamp : StringUtils.EMPTY, TRUNCATE_MIDDLE_ATTRIBUTES);
                    }
                    if(identifier.equals(VersionsColumn.CHECKSUM.name())) {
                        final Checksum checksum = versions.get(row.intValue()).attributes().getChecksum();
                        return NSAttributedString.attributedStringWithAttributes(!Checksum.NONE.equals(checksum) ? checksum.hash : LocaleFactory.localizedString("None"), TRUNCATE_MIDDLE_ATTRIBUTES);
                    }
                    if(identifier.equals(VersionsColumn.SIZE.name())) {
                        final long size = versions.get(row.intValue()).attributes().getSize();
                        return NSAttributedString.attributedStringWithAttributes(SizeFormatterFactory.get().format(size), TRUNCATE_MIDDLE_ATTRIBUTES);
                    }
                    if(identifier.equals(VersionsColumn.OWNER.name())) {
                        final String owner = versions.get(row.intValue()).attributes().getOwner();
                        return NSAttributedString.attributedStringWithAttributes(StringUtils.isBlank(owner) ? LocaleFactory.localizedString("Unknown") : owner, TRUNCATE_MIDDLE_ATTRIBUTES);
                    }
                }
                return null;
            }
        }).id());
        this.versionsTable.setDelegate((versionsTableDelegate = new AbstractTableDelegate<String, MetadataColumn>(versionsTable.tableColumnWithIdentifier(VersionsColumn.TIMESTAMP.name())) {
            @Override
            public void tableRowDoubleClicked(final ID sender) {
                this.enterKeyPressed(sender);
            }

            @Override
            public void enterKeyPressed(final ID sender) {
            }

            @Action
            public void spaceKeyPressed(final ID sender) {
                versionsQuicklookButtonClicked(sender);
            }

            @Override
            public void deleteKeyPressed(final ID sender) {
                versionsDeleteButtonClicked(sender);
            }

            @Override
            public String tooltip(String c, final MetadataColumn column) {
                return c;
            }

            @Override
            public void tableColumnClicked(NSTableView view, NSTableColumn c) {
                //
            }

            @Override
            public void selectionDidChange(final NSNotification notification) {
                validateVersionsActions(true);
                if(quicklook.isOpen()) {
                    versionsQuicklookButtonClicked(null);
                }
            }

            @Override
            protected boolean isTypeSelectSupported() {
                return false;
            }
        }).id());
        this.versionsTable.sizeToFit();
    }

    public void setVersionsRevertButton(final NSButton b) {
        this.versionsRevertButton = b;
        this.versionsRevertButton.setTarget(this.id());
        if(!Factory.Platform.osversion.matches("(10)\\..*")) {
            // Available in macOS 11+ or later
            this.versionsRevertButton.setImage(IconCacheFactory.<NSImage>get().iconNamed("clock.arrow.circlepath"));
        }
        this.versionsRevertButton.setAction(Foundation.selector("versionsRevertButtonClicked:"));
    }

    @Action
    public void versionsRevertButtonClicked(final ID sender) {
        this.versionsRevertButtonClicked(reload);
    }

    protected void versionsRevertButtonClicked(final ReloadCallback callback) {
        if(this.toggleVersionsSettings(false)) {
            final Path selected = versions.get(versionsTable.selectedRow().intValue());
            new RevertController(this, session).revert(Collections.singletonList(selected), callback);
        }
    }

    public void setVersionsDeleteButton(final NSButton b) {
        this.versionsDeleteButton = b;
        this.versionsDeleteButton.setTarget(this.id());
        this.versionsDeleteButton.setAction(Foundation.selector("versionsDeleteButtonClicked:"));
    }

    @Action
    public void versionsDeleteButtonClicked(final ID sender) {
        this.versionsDeleteButtonClicked(reload);
    }

    protected void versionsDeleteButtonClicked(final ReloadCallback callback) {
        if(this.toggleVersionsSettings(false)) {
            final Path selected = versions.get(versionsTable.selectedRow().intValue());
            new DeleteController(this, session, false).delete(Collections.singletonList(selected), callback);
        }
    }

    public void setVersionsQuicklookButton(NSButton b) {
        this.versionsQuicklookButton = b;
        // Only enable upon selection change
        this.versionsQuicklookButton.setEnabled(false);
        this.versionsQuicklookButton.setAction(Foundation.selector("versionsQuicklookButtonClicked:"));
        this.versionsQuicklookButton.setTarget(this.id());
    }

    @Action
    public void versionsQuicklookButtonClicked(ID sender) {
        NSIndexSet iterator = versionsTable.selectedRowIndexes();
        final List<TransferItem> items = new ArrayList<>();
        for(NSUInteger index = iterator.firstIndex(); !index.equals(NSIndexSet.NSNotFound); index = iterator.indexGreaterThanIndex(index)) {
            final Path f = versions.get(index.intValue());
            items.add(new TransferItem(f, temporary.create(session.getHost().getUuid(), f)));
        }
        if(toggleVersionsSettings(false)) {
            this.background(new QuicklookTransferBackgroundAction(this, quicklook, session, items) {
                @Override
                public void cleanup() {
                    super.cleanup();
                    toggleVersionsSettings(true);
                }
            });
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
                    if(identifier.equals(MetadataColumn.NAME.name())) {
                        final String name = metadata.get(row.intValue()).getName();
                        return NSAttributedString.attributedString(StringUtils.isNotEmpty(name) ? name : StringUtils.EMPTY);
                    }
                    if(identifier.equals(MetadataColumn.VALUE.name())) {
                        final String value = metadata.get(row.intValue()).getValue();
                        return NSAttributedString.attributedString(value != null ? value : LocaleFactory.localizedString("Multiple files"));
                    }
                }
                return null;
            }

            @Override
            public void tableView_setObjectValue_forTableColumn_row(NSTableView view, NSObject value,
                                                                    NSTableColumn c, NSInteger row) {
                if(row.intValue() < metadata.size()) {
                    Header header = metadata.get(row.intValue());
                    if(c.identifier().equals(MetadataColumn.NAME.name())) {
                        header.setName(value.toString());
                    }
                    if(c.identifier().equals(MetadataColumn.VALUE.name())) {
                        header.setValue(value.toString());
                    }
                    if(StringUtils.isNotBlank(header.getName()) && StringUtils.isNotBlank(header.getValue())) {
                        // Only update if both fields are set
                        metadataInputDidEndEditing();
                    }
                }
            }
        }).id());
        this.metadataTable.setDelegate((metadataTableDelegate = new AbstractTableDelegate<String, MetadataColumn>(
                metadataTable.tableColumnWithIdentifier(MetadataColumn.NAME.name())
        ) {
            @Override
            public boolean isColumnRowEditable(NSTableColumn column, NSInteger row) {
                return true;
            }

            @Override
            public void tableRowDoubleClicked(final ID sender) {
                this.enterKeyPressed(sender);
            }

            @Override
            public void enterKeyPressed(final ID sender) {
                metadataTable.editRow(
                        metadataTable.columnWithIdentifier(MetadataColumn.VALUE.name()),
                        metadataTable.selectedRow(), true);
            }

            @Override
            public void deleteKeyPressed(final ID sender) {
                metadataRemoveButtonClicked(sender);
            }

            @Override
            public String tooltip(String c, final MetadataColumn column) {
                return c;
            }

            @Override
            public void tableColumnClicked(NSTableView view, NSTableColumn c) {
                //
            }

            @Override
            public void selectionDidChange(final NSNotification notification) {
                validateMetadataActions(true);
            }

            @Override
            protected boolean isTypeSelectSupported() {
                return false;
            }

            public void tableView_willDisplayCell_forTableColumn_row(final NSTableView view, final NSTextFieldCell cell, final NSTableColumn c, final NSInteger row) {
                if(c.identifier().equals(MetadataColumn.VALUE.name())) {
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
        this.metadataAddButton.lastItem().setImage(IconCacheFactory.<NSImage>get().iconNamed("NSActionTemplate"));
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
        List<Header> updated = new ArrayList<>(metadata);
        updated.add(row, new Header(name, value));
        this.setMetadata(updated);
        metadataTable.selectRowIndexes(NSIndexSet.indexSetWithIndex(new NSInteger(row)), false);
        metadataTable.editRow(
                selectValue ? metadataTable.columnWithIdentifier(MetadataColumn.VALUE.name()) : metadataTable.columnWithIdentifier(MetadataColumn.NAME.name()),
                new NSInteger(row), true);
    }

    public void setMetadataRemoveButton(NSButton b) {
        this.metadataRemoveButton = b;
        this.metadataRemoveButton.setAction(Foundation.selector("metadataRemoveButtonClicked:"));
        this.metadataRemoveButton.setTarget(this.id());
    }

    @Action
    public void metadataRemoveButtonClicked(ID sender) {
        List<Header> updated = new ArrayList<>(metadata);
        NSIndexSet iterator = metadataTable.selectedRowIndexes();
        List<Header> remove = new ArrayList<>();
        for(NSUInteger index = iterator.firstIndex(); !index.equals(NSIndexSet.NSNotFound); index = iterator.indexGreaterThanIndex(index)) {
            remove.add(updated.get(index.intValue()));
        }
        updated.removeAll(remove);
        this.setMetadata(updated);
        this.metadataInputDidEndEditing();
    }

    private void metadataInputDidEndEditing() {
        if(toggleMetadataSettings(false)) {
            final Map<String, String> update = new HashMap<>();
            for(Header header : metadata) {
                update.put(header.getName(), header.getValue());
            }
            this.background(new WorkerBackgroundAction<>(controller, session,
                            new WriteMetadataWorker(files, update, new PromptRecursiveCallback<>(this), controller) {
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
        this.ownerr.setTarget(this.id());
        this.ownerr.setAction(Foundation.selector("permissionSelectionChanged:"));
        this.ownerr.setAllowsMixedState(true);
    }

    public void setOwnerw(NSButton ownerw) {
        this.ownerw = ownerw;
        this.ownerw.setTarget(this.id());
        this.ownerw.setAction(Foundation.selector("permissionSelectionChanged:"));
        this.ownerw.setAllowsMixedState(true);
    }

    public void setOwnerx(NSButton ownerx) {
        this.ownerx = ownerx;
        this.ownerx.setTarget(this.id());
        this.ownerx.setAction(Foundation.selector("permissionSelectionChanged:"));
        this.ownerx.setAllowsMixedState(true);
    }

    public void setGroupr(NSButton groupr) {
        this.groupr = groupr;
        this.groupr.setTarget(this.id());
        this.groupr.setAction(Foundation.selector("permissionSelectionChanged:"));
        this.groupr.setAllowsMixedState(true);
    }

    public void setGroupw(NSButton groupw) {
        this.groupw = groupw;
        this.groupw.setTarget(this.id());
        this.groupw.setAction(Foundation.selector("permissionSelectionChanged:"));
        this.groupw.setAllowsMixedState(true);
    }

    public void setGroupx(NSButton groupx) {
        this.groupx = groupx;
        this.groupx.setTarget(this.id());
        this.groupx.setAction(Foundation.selector("permissionSelectionChanged:"));
        this.groupx.setAllowsMixedState(true);
    }

    public void setOtherr(NSButton otherr) {
        this.otherr = otherr;
        this.otherr.setTarget(this.id());
        this.otherr.setAction(Foundation.selector("permissionSelectionChanged:"));
        this.otherr.setAllowsMixedState(true);
    }

    public void setOtherw(NSButton otherw) {
        this.otherw = otherw;
        this.otherw.setTarget(this.id());
        this.otherw.setAction(Foundation.selector("permissionSelectionChanged:"));
        this.otherw.setAllowsMixedState(true);
    }

    public void setOtherx(NSButton otherx) {
        this.otherx = otherx;
        this.otherx.setTarget(this.id());
        this.otherx.setAction(Foundation.selector("permissionSelectionChanged:"));
        this.otherx.setAllowsMixedState(true);
    }

    public void setIconImageView(NSImageView iconImageView) {
        this.iconImageView = iconImageView;
    }

    public void setPanelMetadata(NSView v) {
        this.panelMetadata = v;
    }

    public void setPanelVersions(final NSView v) {
        this.panelVersions = v;
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

    protected void initGeneral() {
        final int count = this.numberOfFiles();
        if(count > 0) {
            filenameField.setStringValue(this.getName());
            final Path file = this.getSelected();
            filenameField.setEnabled(1 == count
                    && session.getFeature(Move.class).isSupported(file, file));
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
                if(file.isVolume()) {
                    iconImageView.setImage(IconCacheFactory.<NSImage>get().volumeIcon(session.getHost().getProtocol(), 32));
                }
                else {
                    iconImageView.setImage(IconCacheFactory.<NSImage>get().fileIcon(file, 32));
                }
            }
        }
        // Sum of files
        this.initSize();
        this.initChecksum();
        // Read HTTP URL
        this.initWebUrl();
    }

    protected void initWebUrl() {
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
    protected void initPermissions() {
        permissionsField.setStringValue(LocaleFactory.localizedString("Unknown"));
        // Disable Apply button and start progress indicator
        if(this.togglePermissionSettings(false)) {
            this.background(new WorkerBackgroundAction<>(controller, session,
                    new ReadPermissionWorker(files) {
                        @Override
                        public void cleanup(final PermissionOverwrite permissions) {
                            setPermissions(permissions);
                            togglePermissionSettings(true);
                        }
                    }
            ));
        }
    }

    private void setPermissions(final PermissionOverwrite permissions) {
        this.updateCheckbox(ownerr, permissions.user.read);
        this.updateCheckbox(ownerw, permissions.user.write);
        this.updateCheckbox(ownerx, permissions.user.execute);

        this.updateCheckbox(groupr, permissions.group.read);
        this.updateCheckbox(groupw, permissions.group.write);
        this.updateCheckbox(groupx, permissions.group.execute);

        this.updateCheckbox(otherr, permissions.other.read);
        this.updateCheckbox(otherw, permissions.other.write);
        this.updateCheckbox(otherx, permissions.other.execute);

        if(this.numberOfFiles() > 1) {
            permissionsField.setStringValue(permissions.toString());
            octalField.setStringValue(permissions.getMode());
        }
        else {
            final Permission permission = permissions.resolve(Permission.EMPTY);
            permissionsField.setStringValue(permission.toString());
            octalField.setStringValue(permission.getMode());
        }
    }

    /**
     * @param checkbox The checkbox to update
     * @param enabled  Set the checkbox to on state
     */
    private void updateCheckbox(NSButton checkbox, Boolean enabled) {
        // Sets the cell's state to value, which can be NSCell.NSOnState, NSCell.NSOffState, or NSCell.MixedState.
        // If necessary, this method also redraws the receiver.
        checkbox.setState(enabled != null ? enabled ? NSCell.NSOnState : NSCell.NSOffState : NSCell.NSMixedState);
        checkbox.setEnabled(true);
    }

    /**
     * Read content distribution settings
     */
    protected void initDistribution() {
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

        final Path file = this.getSelected();

        final DistributionConfiguration cdn = session.getFeature(DistributionConfiguration.class);
        distributionEnableButton.setTitle(MessageFormat.format(LocaleFactory.localizedString("Enable {0} Distribution", "Status"),
                cdn.getName()));
        distributionDeliveryPopup.removeItemWithTitle(LocaleFactory.localizedString("None"));
        for(Distribution.Method method : cdn.getMethods(file)) {
            distributionDeliveryPopup.addItemWithTitle(method.toString());
            distributionDeliveryPopup.itemWithTitle(method.toString()).setRepresentedObject(method.toString());
        }

        distributionDeliveryPopup.selectItemWithTitle(selected);
        if(null == distributionDeliveryPopup.selectedItem()) {
            // Select first distribution option
            Distribution.Method method = cdn.getMethods(file).iterator().next();
            distributionDeliveryPopup.selectItemWithTitle(method.toString());
        }

        distributionLoggingPopup.removeAllItems();
        distributionLoggingPopup.addItemWithTitle(LocaleFactory.localizedString("None"));
        distributionLoggingPopup.itemWithTitle(LocaleFactory.localizedString("None")).setEnabled(false);

        this.distributionStatusButtonClicked(null);
    }

    /**
     * Updates the size field by iterating over all files and reading the cached size value in the attributes of the
     * path
     */
    protected void initSize() {
        if(this.toggleSizeSettings(false)) {
            this.background(new WorkerBackgroundAction<>(controller, session,
                    new ReadSizeWorker(files) {
                        @Override
                        public void cleanup(final Long size) {
                            setSize(size);
                            toggleSizeSettings(true);
                        }
                    }
            ));
        }
    }

    private void setSize(final Long size) {
        sizeField.setAttributedStringValue(NSAttributedString.attributedStringWithAttributes(
                SizeFormatterFactory.get().format(size, true),
                TRUNCATE_MIDDLE_ATTRIBUTES));
    }

    protected void initChecksum() {
        if(this.numberOfFiles() > 1) {
            checksumField.setStringValue(String.format("(%s)", LocaleFactory.localizedString("Multiple files")));
        }
        else {
            final Path file = this.getSelected();
            final Checksum checksum = file.attributes().getChecksum();
            if(Checksum.NONE == checksum) {
                if(StringUtils.isNotBlank(file.attributes().getETag())) {
                    this.updateField(checksumField, file.attributes().getETag(), TRUNCATE_MIDDLE_ATTRIBUTES);
                }
                else {
                    checksumField.setStringValue(LocaleFactory.localizedString("Unknown"));
                }
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
    protected boolean toggleS3Settings(final boolean stop) {
        this.window().endEditingFor(null);
        final Credentials credentials = session.getHost().getCredentials();
        boolean enable = session.getHost().getProtocol().getType() == Protocol.Type.s3
                || session.getHost().getProtocol().getType() == Protocol.Type.b2
                || session.getHost().getProtocol().getType() == Protocol.Type.azure
                || session.getHost().getProtocol().getType() == Protocol.Type.googlestorage;
        if(enable) {
            enable = !credentials.isAnonymousLogin();
        }
        boolean logging = false;
        boolean versioning = false;
        boolean storageclass = false;
        boolean encryption = false;
        boolean lifecycle = false;
        boolean acceleration = false;
        if(enable) {
            logging = session.getFeature(Logging.class) != null;
            versioning = session.getFeature(Versioning.class) != null;
            lifecycle = session.getFeature(Lifecycle.class) != null;
            encryption = session.getFeature(Encryption.class) != null;
            storageclass = session.getFeature(Redundancy.class) != null;
            acceleration = session.getFeature(TransferAcceleration.class) != null;
        }
        storageClassPopup.setEnabled(stop && enable && storageclass);
        encryptionPopup.setEnabled(stop && enable && encryption);
        bucketVersioningButton.setEnabled(stop && enable && versioning);
        bucketMfaButton.setEnabled(stop && enable && session.getHost().getProtocol().getType() == Protocol.Type.s3
                && versioning && bucketVersioningButton.state() == NSCell.NSOnState);
        bucketTransferAccelerationButton.setEnabled(stop && enable && acceleration);
        bucketLoggingButton.setEnabled(stop && enable && logging);
        bucketLoggingPopup.setEnabled(stop && enable && logging);
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
    protected void initS3() {
        bucketLocationField.setStringValue(LocaleFactory.localizedString("Unknown"));

        bucketLoggingPopup.removeAllItems();
        bucketLoggingPopup.addItemWithTitle(LocaleFactory.localizedString("None"));
        bucketLoggingPopup.lastItem().setEnabled(false);

        storageClassPopup.removeAllItems();
        storageClassPopup.addItemWithTitle(LocaleFactory.localizedString("Unknown"));
        storageClassPopup.lastItem().setEnabled(false);
        storageClassPopup.selectItem(storageClassPopup.lastItem());

        encryptionPopup.removeAllItems();
        encryptionPopup.addItemWithTitle(LocaleFactory.localizedString("Unknown"));
        encryptionPopup.lastItem().setEnabled(false);
        encryptionPopup.selectItem(encryptionPopup.lastItem());

        if(this.toggleS3Settings(false)) {
            final Path file = this.getSelected();
            if(session.getFeature(Redundancy.class) != null) {
                for(String redundancy : session.getFeature(Redundancy.class).getClasses()) {
                    storageClassPopup.addItemWithTitle(LocaleFactory.localizedString(redundancy, "S3"));
                    storageClassPopup.lastItem().setRepresentedObject(redundancy);
                }
            }
            this.background(new RegistryBackgroundAction<Void>(controller, session) {
                Location.Name location;
                LoggingConfiguration logging;
                VersioningConfiguration versioning;
                // Available encryption keys in KMS
                Set<Encryption.Algorithm> managedEncryptionKeys = new HashSet<>();
                final Set<Encryption.Algorithm> selectedEncryptionKeys = new HashSet<>();
                final Set<String> selectedStorageClasses = new HashSet<>();
                LifecycleConfiguration lifecycle;
                Boolean transferAcceleration;

                @Override
                public Void run(final Session<?> session) throws BackgroundException {
                    if(session.getFeature(Location.class) != null) {
                        location = session.getFeature(Location.class).getLocation(file);
                    }
                    if(session.getFeature(Logging.class) != null) {
                        logging = session.getFeature(Logging.class).getConfiguration(file);
                    }
                    if(session.getFeature(Versioning.class) != null) {
                        versioning = session.getFeature(Versioning.class).getConfiguration(file);
                    }
                    if(session.getFeature(Lifecycle.class) != null) {
                        lifecycle = session.getFeature(Lifecycle.class).getConfiguration(file);
                    }
                    if(session.getFeature(Redundancy.class) != null) {
                        for(final Path f : files) {
                            final String value = session.getFeature(Redundancy.class).getClass(f);
                            if(StringUtils.isNotBlank(value)) {
                                selectedStorageClasses.add(value);
                            }
                        }
                    }
                    if(session.getFeature(Encryption.class) != null) {
                        // Add additional keys stored in KMS
                        managedEncryptionKeys = session.getFeature(Encryption.class).getKeys(file, prompt);
                        for(final Path f : files) {
                            selectedEncryptionKeys.add(session.getFeature(Encryption.class).getEncryption(f));
                        }
                        managedEncryptionKeys.addAll(selectedEncryptionKeys);
                    }
                    if(session.getFeature(TransferAcceleration.class) != null) {
                        try {
                            transferAcceleration = session.getFeature(TransferAcceleration.class).getStatus(file);
                        }
                        catch(InteroperabilityException | ConflictException e) {
                            log.warn(String.format("Ignore failure %s reading transfer acceleration", e));
                            // 405 The specified method is not allowed against this resource
                        }
                    }
                    return null;
                }

                @Override
                public void cleanup() {
                    super.cleanup();
                    if(logging != null) {
                        bucketLoggingButton.setState(logging.isEnabled() ? NSCell.NSOnState : NSCell.NSOffState);
                        if(!logging.getContainers().isEmpty()) {
                            bucketLoggingPopup.removeAllItems();
                        }
                        for(Path c : logging.getContainers()) {
                            bucketLoggingPopup.addItemWithTitle(c.getName());
                            bucketLoggingPopup.lastItem().setRepresentedObject(c.getName());
                        }
                        if(logging.isEnabled()) {
                            bucketLoggingPopup.selectItemWithTitle(logging.getLoggingTarget());
                        }
                        else {
                            if(!logging.getContainers().isEmpty()) {
                                // Default to write log files to origin bucket
                                bucketLoggingPopup.selectItemAtIndex(bucketLoggingPopup.indexOfItemWithRepresentedObject(containerService.getContainer(file).getName()));
                            }
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
                        if(-1 != storageClassPopup.indexOfItemWithRepresentedObject(storageClass).intValue()) {
                            final NSInteger index = storageClassPopup.indexOfItemWithRepresentedObject(storageClass);
                            storageClassPopup.itemAtIndex(index).setState(selectedStorageClasses.size() == 1 ? NSCell.NSOnState : NSCell.NSMixedState);
                            storageClassPopup.selectItemAtIndex(index);
                        }
                    }
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
                    if(transferAcceleration != null) {
                        bucketTransferAccelerationButton.setState(transferAcceleration ? NSCell.NSOnState : NSCell.NSOffState);
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
    protected boolean toggleAclSettings(final boolean stop) {
        this.window().endEditingFor(null);
        final boolean enabled = this.validateAclActions(stop);
        if(stop) {
            aclProgress.stopAnimation(null);
        }
        else if(enabled) {
            aclProgress.startAnimation(null);
        }
        return enabled;
    }

    /**
     * @param enable True if actions should be enabled if current selection allows
     * @return True if feature is supported
     */
    protected boolean validateAclActions(final boolean enable) {
        final boolean feature = session.getFeature(AclPermission.class) != null;
        aclTable.setEnabled(enable && feature);
        aclAddButton.setEnabled(enable && feature);
        boolean selection = aclTable.numberOfSelectedRows().intValue() > 0;
        aclRemoveButton.setEnabled(enable && feature && selection);
        return feature;
    }

    /**
     * Toggle settings before and after update
     *
     * @param stop Enable controls and stop progress spinner
     * @return True if progress animation has started and settings are toggled
     */
    protected boolean toggleMetadataSettings(final boolean stop) {
        this.window().endEditingFor(null);
        final boolean feature = this.validateMetadataActions(stop);
        if(stop) {
            metadataProgress.stopAnimation(null);
        }
        else if(feature) {
            metadataProgress.startAnimation(null);
        }
        return feature;
    }

    /**
     * @param enable True if actions should be enabled if current selection allows
     */
    protected boolean validateMetadataActions(final boolean enable) {
        boolean feature = session.getFeature(Metadata.class) != null;
        metadataTable.setEnabled(enable && feature);
        metadataAddButton.setEnabled(enable && feature);
        boolean selection = metadataTable.numberOfSelectedRows().intValue() > 0;
        metadataRemoveButton.setEnabled(enable && feature && selection);
        return feature;
    }

    /**
     * Read custom metadata HTTP headers from cloud provider
     */
    protected void initMetadata() {
        this.setMetadata(Collections.emptyList());
        if(this.toggleMetadataSettings(false)) {
            this.background(new WorkerBackgroundAction<>(controller, session, new ReadMetadataWorker(files) {
                @Override
                public void cleanup(final Map<String, String> updated) {
                    final List<Header> m = new ArrayList<>();
                    if(updated != null) {
                        for(Map.Entry<String, String> key : updated.entrySet()) {
                            m.add(new Header(key.getKey(), key.getValue()));
                        }
                    }
                    setMetadata(m);
                    toggleMetadataSettings(true);
                }
            }));
        }
    }

    /**
     * Toggle settings before and after update
     *
     * @param stop Enable controls and stop progress spinner
     * @return True if progress animation has started and settings are toggled
     */
    protected boolean toggleVersionsSettings(final boolean stop) {
        this.window().endEditingFor(null);
        final boolean enabled = this.validateVersionsActions(stop);
        if(stop) {
            versionsProgress.stopAnimation(null);
        }
        else if(enabled) {
            versionsProgress.startAnimation(null);
        }
        return enabled;
    }

    /**
     * @param enable True if actions should be enabled if current selection allows
     */
    protected boolean validateVersionsActions(final boolean enable) {
        boolean feature = session.getFeature(Versioning.class) != null;
        versionsTable.setEnabled(enable && feature);
        boolean selection = versionsTable.numberOfSelectedRows().intValue() == 1;
        if(selection) {
            final Path version = versions.get(versionsTable.selectedRow().intValue());
            versionsDeleteButton.setEnabled(enable && feature && session.getFeature(Delete.class).isSupported(version));
            versionsRevertButton.setEnabled(enable && feature && session.getFeature(Versioning.class).isRevertable(version));
            versionsQuicklookButton.setEnabled(enable && feature && version.attributes().getPermission().isReadable());
        }
        else {
            versionsDeleteButton.setEnabled(false);
            versionsRevertButton.setEnabled(false);
            versionsQuicklookButton.setEnabled(false);
        }
        return feature;
    }

    /**
     * Read file versions
     */
    protected void initVersions() {
        this.setVersions(AttributedList.emptyList());
        if(this.toggleVersionsSettings(false)) {
            final Path selected = this.getSelected();
            this.background(new WorkerBackgroundAction<>(controller, session,
                    new VersionsWorker(selected, new DisabledListProgressListener()) {
                        @Override
                        public void cleanup(AttributedList<Path> result) {
                            setVersions(result);
                            toggleVersionsSettings(true);
                        }
                    }
            ));
        }
    }

    /**
     * Read grants in the background
     */
    protected void initAcl() {
        this.setAcl(Collections.emptyList());
        if(this.toggleAclSettings(false)) {
            final AclPermission feature = session.getFeature(AclPermission.class);
            aclAddButton.removeAllItems();
            this.aclAddButton.addItemWithTitle(StringUtils.EMPTY);
            this.aclAddButton.lastItem().setImage(IconCacheFactory.<NSImage>get().iconNamed("NSActionTemplate"));
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
            this.background(new WorkerBackgroundAction<>(controller, session,
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
            if(this.togglePermissionSettings(false)) {
                this.background(new WorkerBackgroundAction<>(controller, session,
                                new WritePermissionWorker(files, permission, new BooleanRecursiveCallback<>(false), controller) {
                                    @Override
                                    public void cleanup(final Boolean done) {
                                        togglePermissionSettings(true);
                                        initPermissions();
                                    }
                                }
                        )
                );
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
                    return new Permission(Integer.parseInt(octalField.stringValue()));
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
            if(this.togglePermissionSettings(false)) {
                this.background(new WorkerBackgroundAction<>(controller, session,
                                new WritePermissionWorker(files, permission, new PromptRecursiveCallback<>(this), controller) {
                                    @Override
                                    public void cleanup(final Boolean done) {
                                        togglePermissionSettings(true);
                                        initPermissions();
                                    }
                                }
                        )
                );
            }
        }
    }

    @Action
    public void permissionSelectionChanged(final NSButton sender) {
        if(sender.state() == NSCell.NSMixedState) {
            sender.setState(NSCell.NSOnState);
        }
        final PermissionOverwrite permission = new PermissionOverwrite(
                new PermissionOverwrite.Action(ownerr.state() == NSCell.NSOnState, ownerw.state() == NSCell.NSOnState, ownerx.state() == NSCell.NSOnState),
                new PermissionOverwrite.Action(groupr.state() == NSCell.NSOnState, groupw.state() == NSCell.NSOnState, groupx.state() == NSCell.NSOnState),
                new PermissionOverwrite.Action(otherr.state() == NSCell.NSOnState, otherw.state() == NSCell.NSOnState, otherx.state() == NSCell.NSOnState));
        if(this.togglePermissionSettings(false)) {
            this.background(new WorkerBackgroundAction<>(controller, session,
                            new WritePermissionWorker(files, permission, new BooleanRecursiveCallback<>(false), controller) {
                                @Override
                                public void cleanup(final Boolean done) {
                                    togglePermissionSettings(true);
                                    initPermissions();
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
    protected boolean togglePermissionSettings(final boolean stop) {
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
    protected boolean toggleDistributionSettings(final boolean stop) {
        this.window().endEditingFor(null);
        final Credentials credentials = session.getHost().getCredentials();
        final DistributionConfiguration cdn = session.getFeature(DistributionConfiguration.class);
        boolean enable = !credentials.isAnonymousLogin() && cdn != null;
        final Path file = this.getSelected();
        final Path container = containerService.getContainer(file);
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
            final Distribution.Method method = Distribution.Method.forName(distributionDeliveryPopup.selectedItem().representedObject());
            this.background(new WorkerBackgroundAction<>(controller, session, new DistributionPurgeWorker(files, prompt, method) {
                @Override
                public void cleanup(final Boolean result) {
                    // Refresh the current distribution status
                    distributionStatusButtonClicked(sender);
                }
            }));
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
            final Distribution.Method method = Distribution.Method.forName(distributionDeliveryPopup.selectedItem().representedObject());
            final Distribution configuration = new Distribution(method, distributionEnableButton.state() == NSCell.NSOnState);
            configuration.setIndexDocument(distributionDefaultRootPopup.selectedItem().representedObject());
            configuration.setLogging(distributionLoggingButton.state() == NSCell.NSOnState);
            configuration.setLoggingContainer(distributionLoggingPopup.selectedItem().representedObject());
            configuration.setCNAMEs(StringUtils.split(distributionCnameField.stringValue()));
            this.background(new WorkerBackgroundAction<>(controller, session, new WriteDistributionWorker(files, prompt, configuration) {
                @Override
                public void cleanup(final Boolean result) {
                    // Refresh the current distribution status
                    distributionStatusButtonClicked(sender);
                }
            }));
        }
    }

    @Action
    public void distributionStatusButtonClicked(final ID sender) {
        if(this.toggleDistributionSettings(false)) {
            final Path file = this.getSelected();
            final Distribution.Method method
                    = Distribution.Method.forName(distributionDeliveryPopup.selectedItem().representedObject());
            this.background(new WorkerBackgroundAction<>(controller, session, new ReadDistributionWorker(files, prompt, method) {
                @Override
                public void cleanup(final Distribution distribution) {
                    // Cache distribution

                    distributionEnableButton.setTitle(MessageFormat.format(LocaleFactory.localizedString("Enable {0} Distribution", "Status"),
                            distribution.getName()));
                    distributionEnableButton.setState(distribution.isEnabled() ? NSCell.NSOnState : NSCell.NSOffState);
                    distributionStatusField.setAttributedStringValue(NSAttributedString.attributedStringWithAttributes(distribution.getStatus(), TRUNCATE_MIDDLE_ATTRIBUTES));

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
                        if(distributionLoggingPopup.itemWithTitle(containerService.getContainer(file).getName()) != null) {
                            distributionLoggingPopup.selectItemWithTitle(containerService.getContainer(file).getName());
                        }
                    }
                    if(null == distributionLoggingPopup.selectedItem()) {
                        distributionLoggingPopup.selectItemWithTitle(LocaleFactory.localizedString("None"));
                    }
                    if(distribution.getOrigin() != null) {
                        distributionOriginField.setAttributedStringValue(HyperlinkAttributedStringFactory.create(distribution.getOrigin().toString()));
                    }
                    // Concatenate URLs
                    if(numberOfFiles() > 1) {
                        distributionUrlField.setStringValue(String.format("(%s)", LocaleFactory.localizedString("Multiple files")));
                        distributionUrlField.setToolTip(StringUtils.EMPTY);
                        distributionCnameUrlField.setStringValue(String.format("(%s)", LocaleFactory.localizedString("Multiple files")));
                    }
                    else {
                        if(distribution.getUrl() != null) {
                            distributionUrlField.setAttributedStringValue(HyperlinkAttributedStringFactory.create(distribution.getUrl().toString()));
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
                        final DescriptiveUrl url = new DistributionUrlProvider(distribution).toUrl(file).find(DescriptiveUrl.Type.cname);
                        if(!url.equals(DescriptiveUrl.EMPTY)) {
                            // We only support one CNAME URL to be displayed
                            distributionCnameUrlField.setAttributedStringValue(HyperlinkAttributedStringFactory.create(url));
                            distributionCnameUrlField.setToolTip(LocaleFactory.localizedString("CDN URL"));
                        }
                    }
                    for(Path next : distribution.getRootDocuments()) {
                        if(next.isFile()) {
                            distributionDefaultRootPopup.addItemWithTitle(next.getName());
                            distributionDefaultRootPopup.lastItem().setRepresentedObject(next.getName());
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
            }));
        }
    }

    @Action
    public void calculateSizeButtonClicked(final ID sender) {
        if(this.toggleSizeSettings(false)) {
            this.background(new WorkerBackgroundAction<>(controller, session,
                    new CalculateSizeWorker(files, controller) {
                        @Override
                        public void cleanup(final Long size) {
                            setSize(size);
                            toggleSizeSettings(true);
                        }

                        @Override
                        protected void update(final long size) {
                            invoke(new WindowMainAction(InfoController.this) {
                                @Override
                                public void run() {
                                    setSize(size);
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
    protected boolean toggleSizeSettings(final boolean stop) {
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
    public void helpButtonClicked(final ID sender) {
        BrowserLauncherFactory.get().open(ProviderHelpServiceFactory.get().help());
    }

    private enum AclColumn {
        GRANTEE,
        PERMISSION,
    }

    private enum MetadataColumn {
        NAME,
        VALUE
    }

    private enum VersionsColumn {
        TIMESTAMP,
        CHECKSUM,
        SIZE,
        OWNER
    }

    private enum InfoToolbarItem {
        /**
         * General
         */
        general {
            @Override
            public String label() {
                return LocaleFactory.localizedString(StringUtils.capitalize("General"), "Info");
            }

            @Override
            public String image() {
                return "NSInfo";
            }
        },
        permissions {
            @Override
            public String image() {
                return "NSUserGroup";
            }
        },
        acl {
            @Override
            public String label() {
                return LocaleFactory.localizedString(StringUtils.capitalize("Permissions"), "Info");
            }

            @Override
            public String image() {
                return "NSUserGroup";
            }
        },
        distribution {
            @Override
            public String label() {
                return LocaleFactory.localizedString(StringUtils.capitalize("Distribution (CDN)"), "Info");
            }
        },
        s3 {
            @Override
            public String label() {
                return LocaleFactory.localizedString(StringUtils.capitalize("Amazon S3"), "Info");
            }
        },
        metadata {
            @Override
            public String image() {
                return "pencil.tiff";
            }
        },
        versions {
            @Override
            public String label() {
                return LocaleFactory.localizedString(StringUtils.capitalize("Versions"), "Info");
            }

            @Override
            public String image() {
                return "NSMultipleDocuments";
            }
        };

        public String label() {
            return LocaleFactory.localizedString(StringUtils.capitalize(this.name()), "Info");
        }

        public String image() {
            return this.name();
        }
    }

    public class InternalVersionsReloadCallback implements ReloadCallback {
        @Override
        public void cancel() {
            toggleVersionsSettings(true);
        }

        @Override
        public void done(final List<Path> files) {
            toggleVersionsSettings(true);
            initVersions();
        }
    }
}
