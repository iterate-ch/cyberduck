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

import ch.cyberduck.core.AbstractPath;
import ch.cyberduck.core.Acl;
import ch.cyberduck.core.ConnectionAdapter;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathFactory;
import ch.cyberduck.core.Permission;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.Protocol;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.Status;
import ch.cyberduck.core.cdn.Distribution;
import ch.cyberduck.core.cloud.CloudPath;
import ch.cyberduck.core.cloud.CloudSession;
import ch.cyberduck.core.i18n.Locale;
import ch.cyberduck.core.s3.S3Path;
import ch.cyberduck.core.s3.S3Session;
import ch.cyberduck.ui.DateFormatterFactory;
import ch.cyberduck.ui.action.CalculateSizeWorker;
import ch.cyberduck.ui.action.ChecksumWorker;
import ch.cyberduck.ui.action.ReadAclWorker;
import ch.cyberduck.ui.action.ReadMetadataWorker;
import ch.cyberduck.ui.action.ReadPermissionWorker;
import ch.cyberduck.ui.action.ReadSizeWorker;
import ch.cyberduck.ui.action.WriteAclWorker;
import ch.cyberduck.ui.action.WriteMetadataWorker;
import ch.cyberduck.ui.action.WritePermissionWorker;
import ch.cyberduck.ui.cocoa.application.*;
import ch.cyberduck.ui.cocoa.foundation.NSAttributedString;
import ch.cyberduck.ui.cocoa.foundation.NSIndexSet;
import ch.cyberduck.ui.cocoa.foundation.NSMutableAttributedString;
import ch.cyberduck.ui.cocoa.foundation.NSNotification;
import ch.cyberduck.ui.cocoa.foundation.NSNotificationCenter;
import ch.cyberduck.ui.cocoa.foundation.NSObject;
import ch.cyberduck.ui.cocoa.foundation.NSString;
import ch.cyberduck.ui.cocoa.threading.BrowserBackgroundAction;
import ch.cyberduck.ui.cocoa.threading.WindowMainAction;
import ch.cyberduck.ui.cocoa.threading.WorkerBackgroundAction;
import ch.cyberduck.ui.cocoa.util.HyperlinkAttributedStringFactory;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.rococoa.Foundation;
import org.rococoa.ID;
import org.rococoa.Selector;
import org.rococoa.cocoa.foundation.NSInteger;
import org.rococoa.cocoa.foundation.NSPoint;
import org.rococoa.cocoa.foundation.NSUInteger;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

/**
 * @version $Id$
 */
public class InfoController extends ToolbarWindowController {
    private static Logger log = Logger.getLogger(InfoController.class);

    /**
     * Selected files
     */
    private List<Path> files = Collections.emptyList();

    private Path getSelected() {
        for(Path file : files) {
            return file;
        }
        return null;
    }

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
    private NSTextField createdField;

    public void setCreatedField(NSTextField t) {
        this.createdField = t;
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
    private NSButton recursiveButton;

    public void setRecursiveButton(NSButton b) {
        this.recursiveButton = b;
        this.recursiveButton.setTarget(this.id());
        this.recursiveButton.setAction(Foundation.selector("recursiveButtonClicked:"));
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
    private NSProgressIndicator aclProgress;

    public void setAclProgress(final NSProgressIndicator p) {
        this.aclProgress = p;
        this.aclProgress.setDisplayedWhenStopped(false);
        this.aclProgress.setStyle(NSProgressIndicator.NSProgressIndicatorSpinningStyle);
    }

    @Outlet
    private NSProgressIndicator metadataProgress;

    public void setMetadataProgress(final NSProgressIndicator p) {
        this.metadataProgress = p;
        this.metadataProgress.setDisplayedWhenStopped(false);
        this.metadataProgress.setStyle(NSProgressIndicator.NSProgressIndicatorSpinningStyle);
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

    private NSPopUpButton distributionLoggingPopup;

    public void setDistributionLoggingPopup(NSPopUpButton b) {
        this.distributionLoggingPopup = b;
        this.distributionLoggingPopup.setTarget(this.id());
        this.distributionLoggingPopup.setAction(Foundation.selector("distributionLoggingPopupClicked:"));
    }

    @Outlet
    private NSButton distributionInvalidateObjectsButton;

    public void setDistributionInvalidateObjectsButton(NSButton b) {
        this.distributionInvalidateObjectsButton = b;
        this.distributionInvalidateObjectsButton.setTarget(this.id());
        this.distributionInvalidateObjectsButton.setAction(Foundation.selector("distributionInvalidateObjectsButtonClicked:"));
    }

    @Outlet
    private NSTextField distributionInvalidationStatusField;

    public void setDistributionInvalidationStatusField(NSTextField t) {
        this.distributionInvalidationStatusField = t;
    }

    @Outlet
    private NSPopUpButton distributionDeliveryPopup;

    public void setDistributionDeliveryPopup(NSPopUpButton b) {
        this.distributionDeliveryPopup = b;
        this.distributionDeliveryPopup.setTarget(this.id());
        this.distributionDeliveryPopup.setAction(Foundation.selector("distributionStatusButtonClicked:"));
    }

    @Outlet
    private NSPopUpButton distributionDefaultRootPopup;

    public void setDistributionDefaultRootPopup(NSPopUpButton b) {
        this.distributionDefaultRootPopup = b;
        this.distributionDefaultRootPopup.setTarget(this.id());
        this.distributionDefaultRootPopup.setAction(Foundation.selector("distributionApplyButtonClicked:"));
    }

    @Outlet
    private NSTextField bucketLocationField;

    public void setBucketLocationField(NSTextField t) {
        this.bucketLocationField = t;
    }

    @Outlet
    private NSPopUpButton storageClassPopup;

    public void setStorageClassPopup(NSPopUpButton b) {
        this.storageClassPopup = b;
        this.storageClassPopup.setTarget(this.id());
        this.storageClassPopup.setAction(Foundation.selector("storageClassPopupClicked:"));
    }

    @Action
    public void storageClassPopupClicked(final NSPopUpButton sender) {
        if(this.toggleS3Settings(false)) {
            controller.background(new BrowserBackgroundAction(controller) {
                public void run() {
                    for(Path next : files) {
                        next.attributes().setStorageClass(sender.selectedItem().representedObject());
                        // Copy item in place to write new attributes
                        next.copy(next);
                    }
                }

                @Override
                public void cleanup() {
                    toggleS3Settings(true);
                    initS3();
                }

                @Override
                public String getActivity() {
                    return MessageFormat.format(Locale.localizedString("Writing metadata of {0}", "Status"),
                            this.toString(files));
                }
            });
        }
    }

    @Outlet
    private NSButton encryptionButton;

    public void setEncryptionButton(NSButton b) {
        this.encryptionButton = b;
        this.encryptionButton.setTarget(this.id());
        this.encryptionButton.setAction(Foundation.selector("encryptionButtonClicked:"));
    }

    @Action
    public void encryptionButtonClicked(final NSButton sender) {
        if(this.toggleS3Settings(false)) {
            controller.background(new BrowserBackgroundAction(controller) {
                public void run() {
                    for(Path next : files) {
                        next.attributes().setEncryption(encryptionButton.state() == NSCell.NSOnState ?
                                ((CloudSession) controller.getSession()).getSupportedEncryptionAlgorithms().iterator().next() : null);
                        // Copy item in place to write new attributes
                        next.copy(next);
                    }
                }

                @Override
                public void cleanup() {
                    toggleS3Settings(true);
                    initMetadata();
                    initS3();
                }

                @Override
                public String getActivity() {
                    return MessageFormat.format(Locale.localizedString("Writing metadata of {0}", "Status"),
                            this.toString(files));
                }
            });
        }
    }

    @Outlet
    private NSButton bucketLoggingButton;

    public void setBucketLoggingButton(NSButton b) {
        this.bucketLoggingButton = b;
        this.bucketLoggingButton.setAction(Foundation.selector("bucketLoggingButtonClicked:"));
    }

    @Action
    public void bucketLoggingButtonClicked(final NSButton sender) {
        if(this.toggleS3Settings(false)) {
            controller.background(new BrowserBackgroundAction(controller) {
                public void run() {
                    final String container = getSelected().getContainerName();
                    ((CloudSession) controller.getSession()).setLogging(container,
                            bucketLoggingButton.state() == NSCell.NSOnState,
                            null == bucketLoggingPopup.selectedItem() ? null : bucketLoggingPopup.selectedItem().representedObject());
                }

                @Override
                public void cleanup() {
                    toggleS3Settings(true);
                    initS3();
                }

                @Override
                public String getActivity() {
                    return MessageFormat.format(Locale.localizedString("Writing metadata of {0}", "Status"),
                            this.toString(files));
                }
            });
        }
    }

    private NSPopUpButton bucketLoggingPopup;

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

    @Outlet
    private NSButton bucketAnalyticsButton;

    public void setBucketAnalyticsButton(NSButton b) {
        this.bucketAnalyticsButton = b;
        this.bucketAnalyticsButton.setAction(Foundation.selector("bucketAnalyticsButtonClicked:"));
    }

    @Action
    public void bucketAnalyticsButtonClicked(final NSButton sender) {
        if(this.toggleS3Settings(false)) {
            controller.background(new BrowserBackgroundAction(controller) {
                @Override
                public void run() {
                    final Session session = controller.getSession();
                    if(bucketAnalyticsButton.state() == NSCell.NSOnState) {
                        final String document = Preferences.instance().getProperty(
                                "analytics.provider.qloudstat.iam.policy.s3");
                        session.iam().createUser(session.analytics().getName(), document);
                    }
                    else {
                        session.iam().deleteUser(session.analytics().getName());
                    }
                }

                @Override
                public void cleanup() {
                    toggleS3Settings(true);
                    initS3();
                }
            });
        }
    }

    @Outlet
    private NSTextField bucketAnalyticsSetupUrlField;

    public void setBucketAnalyticsSetupUrlField(NSTextField f) {
        this.bucketAnalyticsSetupUrlField = f;
        this.bucketAnalyticsSetupUrlField.setAllowsEditingTextAttributes(true);
        this.bucketAnalyticsSetupUrlField.setSelectable(true);
    }

    @Outlet
    private NSButton bucketVersioningButton;

    public void setBucketVersioningButton(NSButton b) {
        this.bucketVersioningButton = b;
        this.bucketVersioningButton.setAction(Foundation.selector("bucketVersioningButtonClicked:"));
    }

    @Action
    public void bucketVersioningButtonClicked(final NSButton sender) {
        if(this.toggleS3Settings(false)) {
            controller.background(new BrowserBackgroundAction(controller) {
                public void run() {
                    final String container = getSelected().getContainerName();
                    ((CloudSession) controller.getSession()).setVersioning(container,
                            bucketMfaButton.state() == NSCell.NSOnState,
                            bucketVersioningButton.state() == NSCell.NSOnState);
                }

                @Override
                public void cleanup() {
                    toggleS3Settings(true);
                    initS3();
                }
            });
        }
    }

    @Outlet
    private NSButton bucketMfaButton;

    public void setBucketMfaButton(NSButton b) {
        this.bucketMfaButton = b;
        this.bucketMfaButton.setAction(Foundation.selector("bucketMfaButtonClicked:"));
    }

    @Action
    public void bucketMfaButtonClicked(final NSButton sender) {
        if(this.toggleS3Settings(false)) {
            controller.background(new BrowserBackgroundAction(controller) {
                public void run() {
                    final String container = getSelected().getContainerName();
                    ((CloudSession) controller.getSession()).setVersioning(container,
                            bucketMfaButton.state() == NSCell.NSOnState,
                            bucketVersioningButton.state() == NSCell.NSOnState
                    );
                }

                @Override
                public void cleanup() {
                    toggleS3Settings(true);
                    initS3();
                }
            });
        }
    }

    @Outlet
    private NSTextField s3PublicUrlField;

    public void setS3PublicUrlField(NSTextField t) {
        this.s3PublicUrlField = t;
        this.s3PublicUrlField.setAllowsEditingTextAttributes(true);
        this.s3PublicUrlField.setSelectable(true);
    }

    @Outlet
    private NSTextField s3PublicUrlValidityField;

    public void setS3PublicUrlValidityField(NSTextField s3PublicUrlValidityField) {
        this.s3PublicUrlValidityField = s3PublicUrlValidityField;
    }

    @Outlet
    private NSTextField s3torrentUrlField;

    public void setS3torrentUrlField(NSTextField t) {
        this.s3torrentUrlField = t;
        this.s3torrentUrlField.setAllowsEditingTextAttributes(true);
        this.s3torrentUrlField.setSelectable(true);
    }

    @Outlet
    private NSTextField distributionCnameField;

    public void setDistributionCnameField(NSTextField t) {
        this.distributionCnameField = t;
        NSNotificationCenter.defaultCenter().addObserver(this.id(),
                Foundation.selector("distributionApplyButtonClicked:"),
                NSControl.NSControlTextDidEndEditingNotification,
                distributionCnameField);
    }

    @Outlet
    private NSTextField distributionOriginField;

    public void setDistributionOriginField(NSTextField t) {
        this.distributionOriginField = t;
        this.distributionOriginField.setAllowsEditingTextAttributes(true);
        this.distributionOriginField.setSelectable(true);
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
    private NSTextField aclUrlField;

    public void setAclUrlField(NSTextField t) {
        this.aclUrlField = t;
        this.aclUrlField.setAllowsEditingTextAttributes(true);
        this.aclUrlField.setSelectable(true);
    }

    /**
     * Grant editing model.
     */
    private List<Acl.UserAndRole> acl = new ArrayList<Acl.UserAndRole>();

    /**
     * Replace current metadata model. Will reload the table view.
     *
     * @param acl The updated access control list
     */
    private void setAcl(List<Acl.UserAndRole> acl) {
        this.acl.clear();
        this.acl.addAll(acl);
        this.aclTable.reloadData();
    }

    @Outlet
    private NSTableView aclTable;
    private ListDataSource aclTableModel;
    private AbstractTableDelegate<Acl.UserAndRole> aclTableDelegate;

    public static final String HEADER_ACL_GRANTEE_COLUMN = "GRANTEE";
    public static final String HEADER_ACL_PERMISSION_COLUMN = "PERMISSION";

    private final NSComboBoxCell aclPermissionCellPrototype = NSComboBoxCell.comboBoxCell();

    public void setAclTable(final NSTableView t) {
        this.aclTable = t;
        this.aclTable.setAllowsMultipleSelection(true);
        this.aclPermissionCellPrototype.setFont(NSFont.systemFontOfSize(NSFont.smallSystemFontSize()));
        this.aclPermissionCellPrototype.setControlSize(NSCell.NSSmallControlSize);
        this.aclPermissionCellPrototype.setCompletes(false);
        this.aclPermissionCellPrototype.setBordered(false);
        this.aclPermissionCellPrototype.setButtonBordered(false);
        this.aclTable.setColumnAutoresizingStyle(NSTableView.NSTableViewUniformColumnAutoresizingStyle);
        this.aclTable.tableColumnWithIdentifier(HEADER_ACL_PERMISSION_COLUMN).setDataCell(aclPermissionCellPrototype);
        this.aclTable.setDataSource((aclTableModel = new ListDataSource() {
            public NSInteger numberOfRowsInTableView(NSTableView view) {
                return new NSInteger(acl.size());
            }

            public NSObject tableView_objectValueForTableColumn_row(NSTableView view, NSTableColumn tableColumn,
                                                                    NSInteger row) {
                if(row.intValue() < acl.size()) {
                    final String identifier = tableColumn.identifier();
                    final Acl.UserAndRole grant = acl.get(row.intValue());
                    if(identifier.equals(HEADER_ACL_GRANTEE_COLUMN)) {
                        return NSString.stringWithString(grant.getUser().getDisplayName());
                    }
                    if(identifier.equals(HEADER_ACL_PERMISSION_COLUMN)) {
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
                    if(c.identifier().equals(HEADER_ACL_GRANTEE_COLUMN)) {
                        grant.getUser().setIdentifier(value.toString());
                    }
                    if(c.identifier().equals(HEADER_ACL_PERMISSION_COLUMN)) {
                        grant.getRole().setName(value.toString());
                    }
                    if(StringUtils.isNotBlank(grant.getUser().getIdentifier())
                            && StringUtils.isNotBlank(grant.getRole().getName())) {
                        InfoController.this.aclInputDidEndEditing();
                    }
                }
            }
        }).id());
        this.aclTable.setDelegate((aclTableDelegate = new AbstractTableDelegate<Acl.UserAndRole>() {
            @Override
            public boolean isColumnRowEditable(NSTableColumn column, int row) {
                if(column.identifier().equals(HEADER_ACL_GRANTEE_COLUMN)) {
                    final Acl.UserAndRole grant = acl.get(row);
                    if(grant.getUser().isEditable()) {
                        return true;
                    }
                    // Group Grantee identifier is not editable
                    return false;
                }
                if(column.identifier().equals(HEADER_ACL_PERMISSION_COLUMN)) {
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

            public void enterKeyPressed(final ID sender) {
                aclTable.editRow(aclTable.columnWithIdentifier(HEADER_ACL_GRANTEE_COLUMN), aclTable.selectedRow(), true);
            }

            public void deleteKeyPressed(final ID sender) {
                aclRemoveButtonClicked(sender);
            }

            public String tableView_toolTipForCell_rect_tableColumn_row_mouseLocation(NSTableView t, NSCell cell,
                                                                                      ID rect, NSTableColumn c,
                                                                                      NSInteger row, NSPoint mouseLocation) {
                return this.tooltip(acl.get(row.intValue()));
            }

            public String tooltip(Acl.UserAndRole c) {
                return c.getUser().getIdentifier();
            }

            @Override
            public void tableColumnClicked(NSTableView view, NSTableColumn c) {
                ;
            }

            @Override
            public void selectionDidChange(NSNotification notification) {
                aclRemoveButton.setEnabled(aclTable.numberOfSelectedRows().intValue() > 0);
            }

            public void tableView_willDisplayCell_forTableColumn_row(NSTableView view, NSTextFieldCell cell,
                                                                     NSTableColumn c, NSInteger row) {
                if(c.identifier().equals(HEADER_ACL_GRANTEE_COLUMN)) {
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

    @Outlet
    private NSPopUpButton aclAddButton;

    public void setAclAddButton(NSPopUpButton b) {
        this.aclAddButton = b;
        this.aclAddButton.setTarget(this.id());
        this.aclAddButton.addItemWithTitle(StringUtils.EMPTY);
        this.aclAddButton.lastItem().setImage(IconCache.iconNamed("gear.tiff"));
        for(Acl.User user : controller.getSession().getAvailableAclUsers()) {
            this.aclAddButton.addItemWithTitle(user.getPlaceholder());
            this.aclAddButton.lastItem().setAction(Foundation.selector("aclAddButtonClicked:"));
            this.aclAddButton.lastItem().setTarget(this.id());
            this.aclAddButton.lastItem().setRepresentedObject(user.getPlaceholder());
        }
    }

    public void aclAddButtonClicked(NSMenuItem sender) {
        for(Acl.User grantee : controller.getSession().getAvailableAclUsers()) {
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
            aclTable.editRow(aclTable.columnWithIdentifier(HEADER_ACL_GRANTEE_COLUMN), new NSInteger(index), true);
        }
        else {
            aclTable.editRow(aclTable.columnWithIdentifier(HEADER_ACL_PERMISSION_COLUMN), new NSInteger(index), true);
        }
    }

    @Outlet
    private NSButton aclRemoveButton;

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
            controller.background(new WorkerBackgroundAction<Acl>(controller,
                    new WriteAclWorker(files, new Acl(acl.toArray(new Acl.UserAndRole[acl.size()])), true) {
                        @Override
                        public void cleanup(Acl permission) {
                            toggleAclSettings(true);
                            initAcl();
                        }
                    })
            );
        }
    }

    @Outlet
    private NSTableView metadataTable;
    private ListDataSource metadataTableModel;
    private AbstractTableDelegate<String> metadataTableDelegate;

    public static final String HEADER_METADATA_NAME_COLUMN = "NAME";
    public static final String HEADER_METADATA_VALUE_COLUMN = "VALUE";

    /**
     *
     */
    private static final class Header implements Comparable<Header> {
        private String name;
        private String value;

        private Header(String name, String value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public int compareTo(Header o) {
            return this.getName().compareTo(o.getName());
        }
    }

    /**
     * Custom HTTP headers for REST protocols
     */
    private List<Header> metadata
            = new ArrayList<Header>();

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
            public NSInteger numberOfRowsInTableView(NSTableView view) {
                return new NSInteger(metadata.size());
            }

            public NSObject tableView_objectValueForTableColumn_row(NSTableView view, NSTableColumn tableColumn,
                                                                    NSInteger row) {
                if(row.intValue() < metadata.size()) {
                    final String identifier = tableColumn.identifier();
                    if(identifier.equals(HEADER_METADATA_NAME_COLUMN)) {
                        final String name = metadata.get(row.intValue()).getName();
                        return NSAttributedString.attributedString(StringUtils.isNotEmpty(name) ? name : StringUtils.EMPTY);
                    }
                    if(identifier.equals(HEADER_METADATA_VALUE_COLUMN)) {
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
                    if(c.identifier().equals(HEADER_METADATA_NAME_COLUMN)) {
                        header.setName(value.toString());
                    }
                    if(c.identifier().equals(HEADER_METADATA_VALUE_COLUMN)) {
                        header.setValue(value.toString());
                    }
                    if(StringUtils.isNotBlank(header.getName()) && StringUtils.isNotBlank(header.getValue())) {
                        // Only update if both fields are set
                        metadataInputDidEndEditing();
                    }
                }
            }
        }).id());
        this.metadataTable.setDelegate((metadataTableDelegate = new AbstractTableDelegate<String>() {
            @Override
            public boolean isColumnRowEditable(NSTableColumn column, int row) {
                return true;
            }

            @Override
            public void tableRowDoubleClicked(final ID sender) {
                this.enterKeyPressed(sender);
            }

            public void enterKeyPressed(final ID sender) {
                metadataTable.editRow(
                        metadataTable.columnWithIdentifier(HEADER_METADATA_VALUE_COLUMN),
                        metadataTable.selectedRow(), true);
            }

            public void deleteKeyPressed(final ID sender) {
                metadataRemoveButtonClicked(sender);
            }

            public String tooltip(String c) {
                return c;
            }

            @Override
            public void tableColumnClicked(NSTableView view, NSTableColumn c) {
                ;
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
                if(c.identifier().equals(HEADER_METADATA_VALUE_COLUMN)) {
                    final String value = metadata.get(row.intValue()).getValue();
                    if(null == value) {
                        cell.setPlaceholderString(Locale.localizedString("Multiple files"));
                    }
                }
            }
        }).id());
        this.metadataTable.sizeToFit();
    }

    @Outlet
    private NSPopUpButton metadataAddButton;

    public void setMetadataAddButton(NSPopUpButton b) {
        this.metadataAddButton = b;
        this.metadataAddButton.setTarget(this.id());
        this.metadataAddButton.addItemWithTitle(StringUtils.EMPTY);
        this.metadataAddButton.lastItem().setImage(IconCache.iconNamed("gear.tiff"));
        this.metadataAddButton.addItemWithTitle(Locale.localizedString("Custom Header", "S3"));
        this.metadataAddButton.lastItem().setAction(Foundation.selector("metadataAddCustomClicked:"));
        this.metadataAddButton.lastItem().setTarget(this.id());
        this.metadataAddButton.menu().addItem(NSMenuItem.separatorItem());
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
        this.metadataAddButton.addItemWithTitle("Content-Encoding");
        this.metadataAddButton.lastItem().setAction(Foundation.selector("metadataAddContentEncodingClicked:"));
        this.metadataAddButton.lastItem().setTarget(this.id());
    }

    /**
     * Add a custom metadata header. This will be prefixed depending on the service.
     *
     * @param sender Button
     * @see org.jets3t.service.Constants#REST_METADATA_PREFIX
     * @see com.rackspacecloud.client.cloudfiles.FilesConstants#X_OBJECT_META
     */
    @Action
    public void metadataAddCustomClicked(ID sender) {
        this.addMetadataItem();
    }

    @Action
    public void metadataAddCacheControlClicked(ID sender) {
        this.addMetadataItem("Cache-Control",
                "public,max-age=" + Preferences.instance().getInteger("s3.cache.seconds"));
    }

    @Action
    public void metadataAddContentTypeClicked(ID sender) {
        this.addMetadataItem("Content-Type", StringUtils.EMPTY, true);
    }

    @Action
    public void metadataAddContentEncodingClicked(ID sender) {
        this.addMetadataItem("Content-Encoding", StringUtils.EMPTY, true);
    }

    /**
     * Format to RFC 1123 timestamp
     * Expires: Thu, 01 Dec 1994 16:00:00 GMT
     */
    private SimpleDateFormat rfc1123 =
            new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", java.util.Locale.ENGLISH);

    {
        rfc1123.setTimeZone(TimeZone.getDefault());
    }

    @Action
    public void metadataAddExpiresClicked(ID sender) {
        final Calendar time = Calendar.getInstance();
        time.add(Calendar.SECOND, Preferences.instance().getInteger("s3.cache.seconds"));
        this.addMetadataItem("Expires", rfc1123.format(time.getTime()));
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
        log.debug("addMetadataItem:" + name);
        int row = metadata.size();
        List<Header> updated = new ArrayList<Header>(metadata);
        updated.add(row, new Header(name, value));
        this.setMetadata(updated);
        metadataTable.selectRowIndexes(NSIndexSet.indexSetWithIndex(new NSInteger(row)), false);
        metadataTable.editRow(
                selectValue ? metadataTable.columnWithIdentifier(HEADER_METADATA_VALUE_COLUMN) : metadataTable.columnWithIdentifier(HEADER_METADATA_NAME_COLUMN),
                new NSInteger(row), true);
    }

    @Outlet
    private NSButton metadataRemoveButton;

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
            controller.background(new WorkerBackgroundAction<Map<String, String>>(controller,
                    new WriteMetadataWorker(files, update) {
                        @Override
                        public void cleanup(Map<String, String> metadata) {
                            try {
                                initMetadata();
                            }
                            finally {
                                toggleMetadataSettings(true);
                            }
                        }
                    })
            );
        }
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

    /**
     * Window title including the filename
     */
    private String title;

    @Override
    public void setWindow(NSWindow window) {
        // Remember original window title
        title = window.title();
        window.setShowsResizeIndicator(true);
        super.setWindow(window);
    }

    @Override
    protected double getMaxWindowWidth() {
        return 600;
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

    public static final class Factory {
        private static Map<BrowserController, InfoController> open
                = new HashMap<BrowserController, InfoController>();

        public static InfoController create(final BrowserController controller, final List<Path> files) {
            if(Preferences.instance().getBoolean("browser.info.isInspector")) {
                if(open.containsKey(controller)) {
                    final InfoController c = open.get(controller);
                    c.setFiles(files);
                    return c;
                }
            }
            final InfoController c = new InfoController(controller, files) {
                @Override
                public void windowWillClose(NSNotification notification) {
                    Factory.open.remove(controller);
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
            open.put(controller, c);
            return c;
        }

        /**
         * @param controller Browser
         * @return Null if the browser does not have an Info window.
         */
        public static InfoController get(final BrowserController controller) {
            return open.get(controller);
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

    private InfoController(final BrowserController controller, List<Path> files) {
        this.controller = controller;
        this.controller.addListener(browserWindowListener);
        this.files = files;
        this.loadBundle();
    }

    private static final String TOOLBAR_ITEM_GENERAL = "info";
    private static final String TOOLBAR_ITEM_PERMISSIONS = "permissions";
    private static final String TOOLBAR_ITEM_ACL = "acl";
    private static final String TOOLBAR_ITEM_DISTRIBUTION = "distribution";
    private static final String TOOLBAR_ITEM_S3 = "s3";
    private static final String TOOLBAR_ITEM_METADATA = "metadata";

    @Override
    protected void setSelectedTab(int tab) {
        if(-1 == tab) {
            tab = 0;
        }
        String identifier = tabView.tabViewItemAtIndex(tab).identifier();
        if(!this.validateTabWithIdentifier(identifier)) {
            tab = 0;
            identifier = tabView.tabViewItemAtIndex(tab).identifier();
        }
        super.setSelectedTab(tab);
        this.initTab(identifier);
    }

    private void initTab(String identifier) {
        if(identifier.equals(TOOLBAR_ITEM_GENERAL)) {
            this.initGeneral();
        }
        if(identifier.equals(TOOLBAR_ITEM_PERMISSIONS)) {
            this.initPermissions();
        }
        if(identifier.equals(TOOLBAR_ITEM_ACL)) {
            this.initAcl();
        }
        if(identifier.equals(TOOLBAR_ITEM_DISTRIBUTION)) {
            this.initDistribution();
        }
        if(identifier.equals(TOOLBAR_ITEM_S3)) {
            this.initMetadata();
            this.initS3();
        }
        if(identifier.equals(TOOLBAR_ITEM_METADATA)) {
            this.initMetadata();
        }
    }

    @Override
    public NSToolbarItem toolbar_itemForItemIdentifier_willBeInsertedIntoToolbar(NSToolbar toolbar,
                                                                                 final String itemIdentifier,
                                                                                 boolean flag) {
        NSToolbarItem item = super.toolbar_itemForItemIdentifier_willBeInsertedIntoToolbar(toolbar, itemIdentifier, flag);
        final Session session = controller.getSession();
        if(itemIdentifier.equals(TOOLBAR_ITEM_DISTRIBUTION)) {
            if(session instanceof CloudSession) {
                // Give icon and label of the given session
                item.setImage(IconCache.iconNamed(session.getHost().getProtocol().disk(), 32));
            }
            else {
                // CloudFront is the default for custom distributions
                item.setImage(IconCache.iconNamed(Protocol.S3_SSL.disk(), 32));
            }
        }
        else if(itemIdentifier.equals(TOOLBAR_ITEM_S3)) {
            if(session instanceof CloudSession) {
                // Set icon of cloud service provider
                item.setLabel(session.getHost().getProtocol().getName());
                item.setImage(IconCache.iconNamed(session.getHost().getProtocol().disk(), 32));
            }
            else {
                // Currently these settings are only available for Amazon S3
                item.setLabel(Protocol.S3_SSL.getName());
                item.setImage(IconCache.iconNamed(Protocol.S3_SSL.disk(), 32));
            }
        }
        else if(itemIdentifier.equals(TOOLBAR_ITEM_METADATA)) {
            item.setImage(IconCache.iconNamed("pencil.tiff", 32));
        }
        else if(itemIdentifier.equals(TOOLBAR_ITEM_ACL)) {
            item.setImage(IconCache.iconNamed("permissions.tiff", 32));
        }
        return item;
    }

    @Override
    protected boolean validateTabWithIdentifier(String itemIdentifier) {
        final Session session = controller.getSession();
        final boolean anonymous = session.getHost().getCredentials().isAnonymousLogin();
        if(itemIdentifier.equals(TOOLBAR_ITEM_PERMISSIONS)) {
            if(anonymous) {
                // Anonymous never has the right to updated permissions
                return false;
            }
            return session.isUnixPermissionsSupported();
        }
        if(itemIdentifier.equals(TOOLBAR_ITEM_ACL)) {
            if(anonymous) {
                // Anonymous never has the right to updated permissions
                return false;
            }
            return session.isAclSupported();
        }
        if(itemIdentifier.equals(TOOLBAR_ITEM_DISTRIBUTION)) {
            if(anonymous) {
                return false;
            }
            // Not enabled if not a cloud session
            return session.isCDNSupported();
        }
        if(itemIdentifier.equals(TOOLBAR_ITEM_S3)) {
            if(anonymous) {
                return false;
            }
            return session instanceof S3Session;
        }
        if(itemIdentifier.equals(TOOLBAR_ITEM_METADATA)) {
            if(anonymous) {
                return false;
            }
            // Not enabled if not a cloud session
            return session.isMetadataSupported();
        }
        return true;
    }

    @Override
    public String getTitle(NSTabViewItem item) {
        return item.label() + " – " + this.getName();
    }

    @Outlet
    private NSView panelMetadata;

    public void setPanelMetadata(NSView v) {
        this.panelMetadata = v;
    }

    @Outlet
    private NSView panelCloud;

    public void setPanelCloud(NSView v) {
        this.panelCloud = v;
    }

    @Outlet
    private NSView panelDistribution;

    public void setPanelDistribution(NSView v) {
        this.panelDistribution = v;
    }

    @Outlet
    private NSView panelPermissions;

    public void setPanelPermissions(NSView v) {
        this.panelPermissions = v;
    }

    @Outlet
    private NSView panelAcl;

    public void setPanelAcl(NSView v) {
        this.panelAcl = v;
    }

    @Outlet
    private NSView panelGeneral;

    public void setPanelGeneral(NSView v) {
        this.panelGeneral = v;
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
        if(files.isEmpty()) {
            return;
        }
        this.files = files;
        this.initTab(this.getSelectedTab());
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
        if(controller.getSession().isUnixPermissionsSupported()) {
            views.add(panelPermissions);
        }
        if(controller.getSession().isAclSupported()) {
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
        identifiers.add(TOOLBAR_ITEM_GENERAL);
        if(controller.getSession().isUnixPermissionsSupported()) {
            identifiers.add(TOOLBAR_ITEM_PERMISSIONS);
        }
        if(controller.getSession().isAclSupported()) {
            identifiers.add(TOOLBAR_ITEM_ACL);
        }
        identifiers.add(TOOLBAR_ITEM_METADATA);
        identifiers.add(TOOLBAR_ITEM_DISTRIBUTION);
        identifiers.add(TOOLBAR_ITEM_S3);
        return identifiers;
    }

    private String getName() {
        final int count = this.numberOfFiles();
        if(count > 1) {
            return "(" + Locale.localizedString("Multiple files") + ")";
        }
        return this.getSelected().getName();
    }

    @Override
    protected NSUInteger getToolbarSize() {
        return NSToolbar.NSToolbarSizeModeSmall;
    }

    private void initGeneral() {
        final int count = this.numberOfFiles();
        if(count > 0) {
            Path file = getSelected();
            filenameField.setStringValue(this.getName());
            filenameField.setEnabled(1 == count && controller.getSession().isRenameSupported(file));
            // Where
            String path;
            if(file.attributes().isSymbolicLink()) {
                path = file.getSymlinkTarget().getAbsolute();
            }
            else {
                path = file.getParent().getAbsolute();
            }
            this.updateField(pathField, path, TRUNCATE_MIDDLE_ATTRIBUTES);
            pathField.setToolTip(path);
            groupField.setStringValue(count > 1 ? "(" + Locale.localizedString("Multiple files") + ")" :
                    file.attributes().getGroup());
            if(count > 1) {
                kindField.setStringValue("(" + Locale.localizedString("Multiple files") + ")");
                checksumField.setStringValue("(" + Locale.localizedString("Multiple files") + ")");
            }
            else {
                this.updateField(kindField, file.kind(), TRUNCATE_MIDDLE_ATTRIBUTES);
            }
            // Timestamps
            if(count > 1) {
                modifiedField.setStringValue("(" + Locale.localizedString("Multiple files") + ")");
                createdField.setStringValue("(" + Locale.localizedString("Multiple files") + ")");
            }
            else {
                if(-1 == file.attributes().getModificationDate()) {
                    this.updateField(modifiedField, Locale.localizedString("Unknown"));
                }
                else {
                    this.updateField(modifiedField, DateFormatterFactory.instance().getLongFormat(
                            file.attributes().getModificationDate()),
                            TRUNCATE_MIDDLE_ATTRIBUTES);
                }
                if(-1 == file.attributes().getCreationDate()) {
                    this.updateField(createdField, Locale.localizedString("Unknown"));
                }
                else {
                    this.updateField(createdField, DateFormatterFactory.instance().getLongFormat(
                            file.attributes().getCreationDate()),
                            TRUNCATE_MIDDLE_ATTRIBUTES);
                }
            }
            // Owner
            this.updateField(ownerField, count > 1 ? "(" + Locale.localizedString("Multiple files") + ")" :
                    file.attributes().getOwner(), TRUNCATE_MIDDLE_ATTRIBUTES);
            // Icon
            if(count > 1) {
                iconImageView.setImage(IconCache.iconNamed("NSMultipleDocuments", 32));
            }
            else {
                iconImageView.setImage(IconCache.instance().iconForPath(this.getSelected(), 32));
            }
        }
        // Sum of files
        this.initSize();
        this.initChecksum();
        this.initPermissions();
        // Read HTTP URL
        this.initWebUrl();
    }

    private void initWebUrl() {
        // Web URL
        if(this.numberOfFiles() > 1) {
            this.updateField(webUrlField, "(" + Locale.localizedString("Multiple files") + ")");
            webUrlField.setToolTip(StringUtils.EMPTY);
        }
        else {
            this.updateField(webUrlField, Locale.localizedString("Unknown"));
            String url = getSelected().toHttpURL();
            if(StringUtils.isNotBlank(url)) {
                webUrlField.setAttributedStringValue(HyperlinkAttributedStringFactory.create(url));
                webUrlField.setToolTip(Locale.localizedString("Open in Web Browser"));
            }
        }
    }

    /**
     *
     */
    private void initPermissions() {
        permissionsField.setStringValue(Locale.localizedString("Unknown"));
        // Disable Apply button and start progress indicator
        if(this.togglePermissionSettings(false)) {
            controller.background(new WorkerBackgroundAction<List<Permission>>(controller, new ReadPermissionWorker(files) {
                @Override
                public void cleanup(List<Permission> permissions) {
                    try {
                        initPermissions(permissions);
                    }
                    finally {
                        togglePermissionSettings(true);
                    }
                }
            }));
        }
    }

    private void initPermissions(List<Permission> permissions) {
        boolean overwrite = true;
        for(Permission permission : permissions) {
            updateCheckbox(ownerr, overwrite, permission.getOwnerPermissions()[Permission.READ]);
            updateCheckbox(ownerw, overwrite, permission.getOwnerPermissions()[Permission.WRITE]);
            updateCheckbox(ownerx, overwrite, permission.getOwnerPermissions()[Permission.EXECUTE]);

            updateCheckbox(groupr, overwrite, permission.getGroupPermissions()[Permission.READ]);
            updateCheckbox(groupw, overwrite, permission.getGroupPermissions()[Permission.WRITE]);
            updateCheckbox(groupx, overwrite, permission.getGroupPermissions()[Permission.EXECUTE]);

            updateCheckbox(otherr, overwrite, permission.getOtherPermissions()[Permission.READ]);
            updateCheckbox(otherw, overwrite, permission.getOtherPermissions()[Permission.WRITE]);
            updateCheckbox(otherx, overwrite, permission.getOtherPermissions()[Permission.EXECUTE]);

            // For more than one file selected, take into account permissions of previous file
            overwrite = false;
        }
        final int count = permissions.size();
        if(count > 1) {
            permissionsField.setStringValue("(" + Locale.localizedString("Multiple files") + ")");
        }
        else {
            for(Permission permission : permissions) {
                permissionsField.setStringValue(permission.toString());
                octalField.setStringValue(permission.getOctalString());
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
        distributionStatusField.setStringValue(Locale.localizedString("Unknown"));
        distributionCnameField.cell().setPlaceholderString(Locale.localizedString("None"));
        distributionOriginField.setStringValue(Locale.localizedString("Unknown"));
        distributionUrlField.setStringValue(Locale.localizedString("Unknown"));
        distributionInvalidationStatusField.setStringValue(Locale.localizedString("None"));

        // Remember last selection
        final String selected = distributionDeliveryPopup.titleOfSelectedItem();

        distributionDeliveryPopup.removeAllItems();
        distributionDeliveryPopup.addItemWithTitle(Locale.localizedString("None"));
        distributionDefaultRootPopup.removeAllItems();
        distributionDefaultRootPopup.addItemWithTitle(Locale.localizedString("None"));
        distributionDefaultRootPopup.menu().addItem(NSMenuItem.separatorItem());

        Session session = controller.getSession();
        distributionEnableButton.setTitle(MessageFormat.format(Locale.localizedString("Enable {0} Distribution", "Status"),
                session.cdn().toString()));
        distributionDeliveryPopup.removeItemWithTitle(Locale.localizedString("None"));
        for(Distribution.Method method : session.cdn().getMethods()) {
            distributionDeliveryPopup.addItemWithTitle(method.toString());
            distributionDeliveryPopup.itemWithTitle(method.toString()).setRepresentedObject(method.toString());
        }

        distributionDeliveryPopup.selectItemWithTitle(selected);
        if(null == distributionDeliveryPopup.selectedItem()) {
            // Select first distribution option
            Distribution.Method method = session.cdn().getMethods().iterator().next();
            distributionDeliveryPopup.selectItemWithTitle(method.toString());
        }

        distributionLoggingPopup.removeAllItems();
        distributionLoggingPopup.addItemWithTitle(Locale.localizedString("None"));
        distributionLoggingPopup.itemWithTitle(Locale.localizedString("None")).setEnabled(false);

        distributionAnalyticsSetupUrlField.setStringValue(Locale.localizedString("None"));

        this.distributionStatusButtonClicked(null);
    }

    /**
     * Updates the size field by iterating over all files and
     * reading the cached size value in the attributes of the path
     */
    private void initSize() {
        if(this.toggleSizeSettings(false)) {
            controller.background(new WorkerBackgroundAction<Long>(controller, new ReadSizeWorker(files) {
                @Override
                public void cleanup(Long size) {
                    try {
                        updateSize(size);
                    }
                    finally {
                        toggleSizeSettings(true);
                    }
                }
            }));
        }
    }

    private void updateSize(long size) {
        sizeField.setAttributedStringValue(NSAttributedString.attributedStringWithAttributes(
                Status.getSizeAsString(size, true),
                TRUNCATE_MIDDLE_ATTRIBUTES));
    }

    private void initChecksum() {
        if(this.numberOfFiles() > 1) {
            checksumField.setStringValue("(" + Locale.localizedString("Multiple files") + ")");
        }
        else {
            checksumField.setStringValue(Locale.localizedString("Unknown"));
            if(this.toggleSizeSettings(false)) {
                controller.background(new WorkerBackgroundAction<List<String>>(controller, new ChecksumWorker(files) {
                    @Override
                    public void cleanup(List<String> checksums) {
                        try {
                            for(String checksum : checksums) {
                                if(StringUtils.isNotBlank(checksum)) {
                                    updateField(checksumField, checksum, TRUNCATE_MIDDLE_ATTRIBUTES);
                                }
                            }
                        }
                        finally {
                            toggleSizeSettings(true);
                        }
                    }
                }));
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
        final Session session = controller.getSession();
        final Credentials credentials = session.getHost().getCredentials();
        boolean enable = session instanceof S3Session;
        if(enable) {
            enable = !credentials.isAnonymousLogin();
        }
        boolean logging = false;
        boolean analytics = false;
        boolean versioning = false;
        boolean storageclass = false;
        boolean encryption = false;
        if(enable) {
            logging = ((CloudSession) session).isLoggingSupported();
            analytics = ((CloudSession) session).isAnalyticsSupported();
            versioning = ((CloudSession) session).isVersioningSupported();
            encryption = ((CloudSession) session).getSupportedEncryptionAlgorithms().size() > 0;
            storageclass = ((CloudSession) session).getSupportedStorageClasses().size() > 1;
        }
        storageClassPopup.setEnabled(stop && enable && storageclass);
        encryptionButton.setEnabled(stop && enable && encryption);
        bucketVersioningButton.setEnabled(stop && enable && versioning);
        bucketMfaButton.setEnabled(stop && enable && versioning
                && bucketVersioningButton.state() == NSCell.NSOnState);
        bucketLoggingButton.setEnabled(stop && enable && logging);
        bucketLoggingPopup.setEnabled(stop && enable && logging);
        if(ObjectUtils.equals(session.iam().getUserCredentials(session.analytics().getName()), credentials)) {
            // No need to create new IAM credentials when same as session credentials
            bucketAnalyticsButton.setEnabled(false);
        }
        else {
            bucketAnalyticsButton.setEnabled(stop && enable && analytics);
        }
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
        bucketLocationField.setStringValue(Locale.localizedString("Unknown"));
        bucketAnalyticsSetupUrlField.setStringValue(Locale.localizedString("None"));

        bucketLoggingPopup.removeAllItems();
        bucketLoggingPopup.addItemWithTitle(Locale.localizedString("None"));
        bucketLoggingPopup.itemWithTitle(Locale.localizedString("None")).setEnabled(false);

        s3PublicUrlField.setStringValue(Locale.localizedString("None"));
        s3PublicUrlValidityField.setStringValue(Locale.localizedString("Unknown"));
        s3torrentUrlField.setStringValue(Locale.localizedString("None"));

        storageClassPopup.removeAllItems();
        storageClassPopup.addItemWithTitle(Locale.localizedString("Unknown"));
        storageClassPopup.itemWithTitle(Locale.localizedString("Unknown")).setEnabled(false);
        storageClassPopup.selectItemWithTitle(Locale.localizedString("Unknown"));

        if(this.toggleS3Settings(false)) {
            for(String redundancy : ((CloudSession) controller.getSession()).getSupportedStorageClasses()) {
                storageClassPopup.addItemWithTitle(Locale.localizedString(redundancy, "S3"));
                storageClassPopup.lastItem().setRepresentedObject(redundancy);
            }
            if(this.numberOfFiles() > 1) {
                s3PublicUrlField.setStringValue("(" + Locale.localizedString("Multiple files") + ")");
                s3PublicUrlField.setToolTip(StringUtils.EMPTY);
                s3torrentUrlField.setStringValue("(" + Locale.localizedString("Multiple files") + ")");
                s3torrentUrlField.setToolTip(StringUtils.EMPTY);
            }
            else {
                Path file = this.getSelected();
                final String redundancy = file.attributes().getStorageClass();
                if(StringUtils.isNotEmpty(redundancy)) {
                    storageClassPopup.removeItemWithTitle(Locale.localizedString("Unknown"));
                    storageClassPopup.selectItemWithTitle(Locale.localizedString(redundancy, "S3"));
                }
                if(file.attributes().isFile()) {
                    if(file instanceof S3Path) {
                        final S3Path s3 = (S3Path) file;
                        final AbstractPath.DescriptiveUrl url = s3.toSignedUrl();
                        if(StringUtils.isNotBlank(url.getUrl())) {
                            s3PublicUrlField.setAttributedStringValue(
                                    HyperlinkAttributedStringFactory.create(url.getUrl())
                            );
                            s3PublicUrlField.setToolTip(url.getHelp());
                        }
                        if(StringUtils.isNotBlank(url.getHelp())) {
                            s3PublicUrlValidityField.setStringValue(url.getHelp());
                        }
                        final AbstractPath.DescriptiveUrl torrent = s3.toTorrentUrl();
                        if(StringUtils.isNotBlank(torrent.getUrl())) {
                            s3torrentUrlField.setAttributedStringValue(
                                    HyperlinkAttributedStringFactory.create(torrent.getUrl())
                            );
                            s3torrentUrlField.setToolTip(Locale.localizedString("Open in Web Browser"));
                        }
                    }
                }
            }
            final Path selected = getSelected();
            controller.background(new BrowserBackgroundAction(controller) {
                String location = null;
                boolean logging = false;
                String analytics = null;
                String loggingBucket = null;
                boolean versioning = false;
                boolean mfa = false;
                List<String> containers = new ArrayList<String>();
                String encryption = null;

                public void run() {
                    final CloudSession s = (CloudSession) controller.getSession();
                    if(s.isLocationSupported()) {
                        location = s.getLocation(selected.getContainerName());
                    }
                    if(s.isLoggingSupported()) {
                        logging = s.isLogging(selected.getContainerName());
                        loggingBucket = s.getLoggingTarget(selected.getContainerName());
                        for(AbstractPath c : getSelected().getContainer().getParent().children()) {
                            containers.add(c.getName());
                        }
                    }
                    if(s.isVersioningSupported()) {
                        versioning = s.isVersioning(selected.getContainerName());
                        mfa = s.isMultiFactorAuthentication(selected.getContainerName());
                    }
                    if(s.isAnalyticsSupported()) {
                        final Credentials credentials = s.iam().getUserCredentials(controller.getSession().analytics().getName());
                        if(null != credentials) {
                            analytics = s.analytics().getSetup(s.getHost().getProtocol(), selected.getContainerName(),
                                    credentials);
                        }
                    }
                    if(numberOfFiles() == 1) {
                        encryption = selected.attributes().getEncryption();
                    }
                }

                @Override
                public void cleanup() {
                    try {
                        bucketLoggingButton.setState(logging ? NSCell.NSOnState : NSCell.NSOffState);
                        if(!containers.isEmpty()) {
                            bucketLoggingPopup.removeAllItems();
                        }
                        for(String c : containers) {
                            bucketLoggingPopup.addItemWithTitle(c);
                            bucketLoggingPopup.lastItem().setRepresentedObject(c);
                        }
                        if(logging) {
                            bucketLoggingPopup.selectItemWithTitle(loggingBucket);
                        }
                        else {
                            // Default to write log files to origin bucket
                            bucketLoggingPopup.selectItemWithTitle(selected.getContainerName());
                        }
                        if(StringUtils.isNotBlank(location)) {
                            bucketLocationField.setStringValue(Locale.localizedString(location, "S3"));
                        }
                        bucketVersioningButton.setState(versioning ? NSCell.NSOnState : NSCell.NSOffState);
                        bucketMfaButton.setState(mfa ? NSCell.NSOnState : NSCell.NSOffState);
                        encryptionButton.setState(StringUtils.isNotBlank(encryption) ? NSCell.NSOnState : NSCell.NSOffState);
                        bucketAnalyticsButton.setState(StringUtils.isNotBlank(analytics) ? NSCell.NSOnState : NSCell.NSOffState);
                        if(StringUtils.isNotBlank(analytics)) {
                            bucketAnalyticsSetupUrlField.setAttributedStringValue(HyperlinkAttributedStringFactory.create(analytics));
                        }
                    }
                    finally {
                        toggleS3Settings(true);
                    }
                }

                @Override
                public String getActivity() {
                    return MessageFormat.format(Locale.localizedString("Reading metadata of {0}", "Status"),
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
        final Session session = controller.getSession();
        final Credentials credentials = session.getHost().getCredentials();
        boolean enable = !credentials.isAnonymousLogin() && session.isAclSupported();
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
        final Session session = controller.getSession();
        final Credentials credentials = session.getHost().getCredentials();
        boolean enable = !credentials.isAnonymousLogin() && session.isMetadataSupported();
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
        this.setMetadata(Collections.<Header>emptyList());
        if(this.toggleMetadataSettings(false)) {
            controller.background(new WorkerBackgroundAction<Map<String, String>>(controller, new ReadMetadataWorker(files) {
                @Override
                public void cleanup(Map<String, String> updated) {
                    try {
                        List<Header> m = new ArrayList<Header>();
                        for(String key : updated.keySet()) {
                            m.add(new Header(key, updated.get(key)));
                        }
                        setMetadata(m);
                    }
                    finally {
                        toggleMetadataSettings(true);
                    }
                }
            }));
        }
    }

    /**
     * Read grants in the background
     */
    private void initAcl() {
        this.setAcl(Collections.<Acl.UserAndRole>emptyList());
        aclUrlField.setStringValue(Locale.localizedString("None"));
        if(this.toggleAclSettings(false)) {
            aclPermissionCellPrototype.removeAllItems();
            for(Acl.Role permission : controller.getSession().getAvailableAclRoles(files)) {
                aclPermissionCellPrototype.addItemWithObjectValue(NSString.stringWithString(permission.getName()));
            }
            if(this.numberOfFiles() > 1) {
                aclUrlField.setStringValue("(" + Locale.localizedString("Multiple files") + ")");
                aclUrlField.setToolTip(StringUtils.EMPTY);
            }
            else {
                for(Path file : files) {
                    if(file.attributes().isFile()) {
                        final CloudPath.DescriptiveUrl url = file.toAuthenticatedUrl();
                        if(StringUtils.isNotBlank(url.getUrl())) {
                            aclUrlField.setAttributedStringValue(
                                    HyperlinkAttributedStringFactory.create(url.getUrl())
                            );
                            aclUrlField.setToolTip(url.getHelp());
                        }
                    }
                }
            }
            controller.background(new WorkerBackgroundAction<List<Acl.UserAndRole>>(controller, new ReadAclWorker(files) {
                @Override
                public void cleanup(List<Acl.UserAndRole> updated) {
                    try {
                        setAcl(updated);
                    }
                    finally {
                        toggleAclSettings(true);
                    }
                }
            }));
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
    public void filenameInputDidEndEditing(NSNotification sender) {
        if(this.numberOfFiles() == 1) {
            final Path current = getSelected();
            if(!filenameField.stringValue().equals(current.getName())) {
                if(StringUtils.contains(filenameField.stringValue(), Path.DELIMITER)) {
                    AppKitFunctionsLibrary.beep();
                    return;
                }
                if(StringUtils.isBlank(filenameField.stringValue())) {
                    filenameField.setStringValue(current.getName());
                }
                else {
                    final Path renamed = PathFactory.createPath(controller.getSession(),
                            current.getParent().getAbsolute(), filenameField.stringValue(), current.attributes().getType());
                    controller.renamePath(current, renamed);
                    this.initWebUrl();
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
                if(!file.attributes().getPermission().equals(permission)) {
                    change = true;
                }
            }
            if(change) {
                this.initPermissions(Collections.singletonList(permission));
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
        Permission permission = this.getPermissionFromOctalField();
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
        Permission p = this.getPermissionFromCheckboxes();
        this.initPermissions(Collections.singletonList(p));
        this.changePermissions(p, false);
    }

    /**
     * Permission selection from checkboxes.
     *
     * @return Never null.
     */
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
     * @param permission UNIX permissions to apply to files
     * @param recursive  Recursively apply to child of directories
     */
    private void changePermissions(final Permission permission, final boolean recursive) {
        if(this.togglePermissionSettings(false)) {
            controller.background(new WorkerBackgroundAction<Permission>(controller,
                    new WritePermissionWorker(files, permission, recursive) {
                        @Override
                        public void cleanup(Permission permission) {
                            try {
                                initPermissions(Collections.singletonList(permission));
                            }
                            finally {
                                togglePermissionSettings(true);
                            }
                        }
                    })
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
        final Session session = controller.getSession();
        final Credentials credentials = session.getHost().getCredentials();
        boolean enable = !credentials.isAnonymousLogin() && session.isUnixPermissionsSupported();
        recursiveButton.setEnabled(stop && enable);
        for(Path next : files) {
            if(next.attributes().isFile()) {
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
        final Session session = controller.getSession();
        final Credentials credentials = session.getHost().getCredentials();
        boolean enable = !credentials.isAnonymousLogin() && session.isCDNSupported();
        if(enable) {
            String container = getSelected().getContainerName();
            // Not enabled if multiple files selected with not same parent container
            for(Path next : files) {
                if(next.getContainerName().equals(container)) {
                    continue;
                }
                enable = false;
                break;
            }
        }
        Distribution.Method method = Distribution.Method.forName(distributionDeliveryPopup.selectedItem().representedObject());
        distributionEnableButton.setEnabled(stop && enable);
        distributionDeliveryPopup.setEnabled(stop && enable);
        distributionLoggingButton.setEnabled(stop && enable && session.cdn().isLoggingSupported(method));
        if(ObjectUtils.equals(session.iam().getUserCredentials(session.analytics().getName()), credentials)) {
            // No need to create new IAM credentials when same as session credentials
            distributionAnalyticsButton.setEnabled(false);
        }
        else {
            distributionAnalyticsButton.setEnabled(stop && enable && session.cdn().isAnalyticsSupported(method));
        }
        distributionLoggingPopup.setEnabled(stop && enable && session.cdn().isLoggingSupported(method));
        distributionCnameField.setEnabled(stop && enable && session.cdn().isCnameSupported(method));
        distributionInvalidateObjectsButton.setEnabled(stop && enable && session.cdn().isInvalidationSupported(method));
        distributionDefaultRootPopup.setEnabled(stop && enable && session.cdn().isDefaultRootSupported(method));
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
            controller.background(new BrowserBackgroundAction(controller) {
                public void run() {
                    final Session session = controller.getSession();
                    Distribution.Method method = Distribution.Method.forName(distributionDeliveryPopup.selectedItem().representedObject());
                    session.cdn().invalidate(session.cdn().getOrigin(method, getSelected().getContainerName()), method, files, false);
                }

                @Override
                public void cleanup() {
                    // Refresh the current distribution status
                    distributionStatusButtonClicked(sender);
                }

                @Override
                public String getActivity() {
                    return MessageFormat.format(Locale.localizedString("Writing CDN configuration of {0}", "Status"),
                            getSelected().getContainerName());
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
            controller.background(new BrowserBackgroundAction(controller) {
                public void run() {
                    final Session session = controller.getSession();
                    Distribution.Method method = Distribution.Method.forName(distributionDeliveryPopup.selectedItem().representedObject());
                    final String origin = session.cdn().getOrigin(method, getSelected().getContainerName());
                    if(StringUtils.isNotBlank(distributionCnameField.stringValue())) {
                        session.cdn().write(distributionEnableButton.state() == NSCell.NSOnState,
                                origin, method,
                                StringUtils.split(distributionCnameField.stringValue()),
                                distributionLoggingButton.state() == NSCell.NSOnState,
                                distributionLoggingPopup.selectedItem().representedObject(),
                                distributionDefaultRootPopup.selectedItem().representedObject());
                    }
                    else {
                        session.cdn().write(distributionEnableButton.state() == NSCell.NSOnState,
                                origin, method,
                                new String[]{}, distributionLoggingButton.state() == NSCell.NSOnState,
                                null == distributionLoggingPopup.selectedItem() ? getSelected().getContainerName() : distributionLoggingPopup.selectedItem().representedObject(),
                                distributionDefaultRootPopup.selectedItem().representedObject());
                    }
                }

                @Override
                public void cleanup() {
                    // Refresh the current distribution status
                    distributionStatusButtonClicked(sender);
                }

                @Override
                public String getActivity() {
                    return MessageFormat.format(Locale.localizedString("Writing CDN configuration of {0}", "Status"),
                            getSelected().getContainerName());
                }
            });
        }
    }

    @Action
    public void distributionStatusButtonClicked(final ID sender) {
        if(this.toggleDistributionSettings(false)) {
            controller.background(new BrowserBackgroundAction(controller) {
                private Distribution distribution;
                private String analytics;

                public void run() {
                    final Session session = controller.getSession();
                    final Distribution.Method method
                            = Distribution.Method.forName(distributionDeliveryPopup.selectedItem().representedObject());
                    // We only support one distribution per bucket for the sake of simplicity
                    final String container = getSelected().getContainerName();
                    distribution = session.cdn().read(
                            session.cdn().getOrigin(method, container), method);
                    // Make sure container items are cached for default root object.
                    getSelected().getContainer().children();
                    if(session.cdn().isAnalyticsSupported(method)) {
                        final Credentials credentials = session.iam().getUserCredentials(controller.getSession().analytics().getName());
                        analytics = session.analytics().getSetup(session.cdn().getProtocol(), container, credentials);
                    }
                }

                @Override
                public void cleanup() {
                    try {
                        final Session session = controller.getSession();
                        distributionEnableButton.setTitle(MessageFormat.format(Locale.localizedString("Enable {0} Distribution", "Status"),
                                session.cdn().toString(distribution.getMethod())));
                        distributionEnableButton.setState(distribution.isEnabled() ? NSCell.NSOnState : NSCell.NSOffState);
                        distributionStatusField.setAttributedStringValue(NSMutableAttributedString.create(distribution.getStatus(), TRUNCATE_MIDDLE_ATTRIBUTES));

                        distributionLoggingButton.setState(distribution.isLogging() ? NSCell.NSOnState : NSCell.NSOffState);
                        final List<String> containers = distribution.getContainers();
                        for(String c : containers) {
                            // Populate with list of available logging targets
                            distributionLoggingPopup.addItemWithTitle(c);
                            distributionLoggingPopup.lastItem().setRepresentedObject(c);
                        }
                        if(StringUtils.isNotBlank(distribution.getLoggingTarget())) {
                            // Select configured logging container if any
                            distributionLoggingPopup.selectItemWithTitle(distribution.getLoggingTarget());
                        }
                        else {
                            final String container = getSelected().getContainerName();
                            if(distributionLoggingPopup.itemWithTitle(container) != null) {
                                distributionLoggingPopup.selectItemWithTitle(container);
                            }
                        }
                        if(null == distributionLoggingPopup.selectedItem()) {
                            distributionLoggingPopup.selectItemWithTitle(Locale.localizedString("None"));
                        }
                        distributionAnalyticsButton.setState(StringUtils.isNotBlank(analytics) ? NSCell.NSOnState : NSCell.NSOffState);
                        if(StringUtils.isNotBlank(analytics)) {
                            distributionAnalyticsSetupUrlField.setAttributedStringValue(
                                    HyperlinkAttributedStringFactory.create(analytics));
                        }

                        String origin = distribution.getOrigin(getSelected());
                        distributionOriginField.setAttributedStringValue(
                                HyperlinkAttributedStringFactory.create(origin));

                        final Path file = getSelected();
                        // Concatenate URLs
                        if(numberOfFiles() > 1) {
                            distributionUrlField.setStringValue("(" + Locale.localizedString("Multiple files") + ")");
                            distributionUrlField.setToolTip(StringUtils.EMPTY);
                            distributionCnameUrlField.setStringValue("(" + Locale.localizedString("Multiple files") + ")");
                        }
                        else {
                            String url = distribution.getURL(file);
                            if(StringUtils.isNotBlank(url)) {
                                distributionUrlField.setAttributedStringValue(HyperlinkAttributedStringFactory.create(url));
                                distributionUrlField.setToolTip(Locale.localizedString("CDN URL"));
                            }
                            else {
                                distributionUrlField.setStringValue(Locale.localizedString("None"));
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
                            for(AbstractPath.DescriptiveUrl url : distribution.getCnameURL(file)) {
                                distributionCnameUrlField.setAttributedStringValue(
                                        HyperlinkAttributedStringFactory.create(url.getUrl())
                                );
                                distributionCnameUrlField.setToolTip(Locale.localizedString("CDN URL"));
                                // We only support one CNAME URL to be displayed
                                break;
                            }
                        }
                        if(session.cdn().isDefaultRootSupported(distribution.getMethod())) {
                            for(AbstractPath next : getSelected().getContainer().children()) {
                                if(next.attributes().isFile()) {
                                    distributionDefaultRootPopup.addItemWithTitle(next.getName());
                                    distributionDefaultRootPopup.lastItem().setRepresentedObject(next.getName());
                                }
                            }
                        }
                        if(StringUtils.isNotBlank(distribution.getDefaultRootObject())) {
                            if(null == distributionDefaultRootPopup.itemWithTitle(distribution.getDefaultRootObject())) {
                                distributionDefaultRootPopup.addItemWithTitle(distribution.getDefaultRootObject());
                            }
                            distributionDefaultRootPopup.selectItemWithTitle(distribution.getDefaultRootObject());
                        }
                        else {
                            distributionDefaultRootPopup.selectItemWithTitle(Locale.localizedString("None"));
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
                    }
                    finally {
                        toggleDistributionSettings(true);
                    }
                }

                @Override
                public String getActivity() {
                    return MessageFormat.format(Locale.localizedString("Reading CDN configuration of {0}", "Status"),
                            getSelected().getContainerName());
                }
            });
        }
    }

    @Outlet
    private NSButton distributionAnalyticsButton;

    public void setDistributionAnalyticsButton(NSButton b) {
        this.distributionAnalyticsButton = b;
        this.distributionAnalyticsButton.setAction(Foundation.selector("distributionAnalyticsButtonClicked:"));
    }

    @Action
    public void distributionAnalyticsButtonClicked(final NSButton sender) {
        if(this.toggleDistributionSettings(false)) {
            controller.background(new BrowserBackgroundAction(controller) {
                @Override
                public void run() {
                    final Session session = controller.getSession();
                    if(distributionAnalyticsButton.state() == NSCell.NSOnState) {
                        final String document = Preferences.instance().getProperty(
                                "analytics.provider.qloudstat.iam.policy.cloudfront");
                        session.iam().createUser(session.analytics().getName(), document);
                    }
                    else {
                        session.iam().deleteUser(session.analytics().getName());
                    }
                }

                @Override
                public void cleanup() {
                    toggleDistributionSettings(true);
                    initDistribution();
                }
            });
        }
    }

    @Outlet
    private NSTextField distributionAnalyticsSetupUrlField;

    public void setDistributionAnalyticsSetupUrlField(NSTextField f) {
        this.distributionAnalyticsSetupUrlField = f;
        this.distributionAnalyticsSetupUrlField.setAllowsEditingTextAttributes(true);
        this.distributionAnalyticsSetupUrlField.setSelectable(true);
    }

    @Action
    public void calculateSizeButtonClicked(final ID sender) {
        if(this.toggleSizeSettings(false)) {
            controller.background(new WorkerBackgroundAction<Long>(controller, new CalculateSizeWorker(files) {
                @Override
                public void cleanup(Long size) {
                    try {
                        updateSize(size);
                    }
                    finally {
                        toggleSizeSettings(true);
                    }
                }

                @Override
                protected void update(final long size) {
                    invoke(new WindowMainAction(InfoController.this) {
                        public void run() {
                            updateSize(size);
                        }
                    });
                }
            }));
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
            if(next.attributes().isDirectory()) {
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
        final String tab = this.getSelectedTab();
        StringBuilder site = new StringBuilder(Preferences.instance().getProperty("website.help"));
        if(tab.equals(TOOLBAR_ITEM_GENERAL)) {
            site.append("/howto/info");
        }
        else if(tab.equals(TOOLBAR_ITEM_PERMISSIONS)) {
            site.append("/howto/permissions");
        }
        else if(tab.equals(TOOLBAR_ITEM_ACL)) {
            site.append("/howto/acl");
        }
        else if(tab.equals(TOOLBAR_ITEM_METADATA)) {
            site.append("/").append(controller.getSession().getHost().getProtocol().getProvider());
        }
        else if(tab.equals(TOOLBAR_ITEM_S3)) {
            site.append("/").append(controller.getSession().getHost().getProtocol().getProvider());
        }
        else if(tab.equals(TOOLBAR_ITEM_DISTRIBUTION)) {
            site.append("/howto/cdn");
        }
        openUrl(site.toString());
    }
}
