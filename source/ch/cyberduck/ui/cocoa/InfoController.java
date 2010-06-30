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

import ch.cyberduck.core.*;
import ch.cyberduck.core.cloud.CloudPath;
import ch.cyberduck.core.cloud.CloudSession;
import ch.cyberduck.core.cloud.Distribution;
import ch.cyberduck.core.i18n.Locale;
import ch.cyberduck.core.s3h.S3HPath;
import ch.cyberduck.core.s3h.S3HSession;
import ch.cyberduck.ui.DateFormatterFactory;
import ch.cyberduck.ui.cocoa.application.*;
import ch.cyberduck.ui.cocoa.foundation.*;
import ch.cyberduck.ui.cocoa.threading.BrowserBackgroundAction;
import ch.cyberduck.ui.cocoa.util.HyperlinkAttributedStringFactory;

import org.rococoa.Foundation;
import org.rococoa.ID;
import org.rococoa.Selector;
import org.rococoa.cocoa.foundation.NSInteger;
import org.rococoa.cocoa.foundation.NSPoint;
import org.rococoa.cocoa.foundation.NSUInteger;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.text.MessageFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @version $Id$
 */
public class InfoController extends ToolbarWindowController {
    private static Logger log = Logger.getLogger(InfoController.class);

    /**
     * Selected files
     */
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
    }

    @Outlet
    private NSPopUpButton storageClassPopup;

    public void setStorageClassPopup(NSPopUpButton b) {
        this.storageClassPopup = b;
        this.storageClassPopup.setAutoenablesItems(false);
        this.storageClassPopup.removeAllItems();
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
                }

                @Override
                public String getActivity() {
                    return MessageFormat.format(Locale.localizedString("Writing metadata of {0}", "Status"),
                            files.get(0).getName());
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
                    for(Path next : files) {
                        final String container = ((S3HPath) next).getContainerName();
                        ((S3HSession) controller.getSession()).setLogging(container,
                                bucketLoggingButton.state() == NSCell.NSOnState);
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
                    for(Path next : files) {
                        final String container = ((S3HPath) next).getContainerName();
                        ((S3HSession) controller.getSession()).setVersioning(container,
                                bucketMfaButton.state() == NSCell.NSOnState,
                                bucketVersioningButton.state() == NSCell.NSOnState);
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
                    for(Path next : files) {
                        final String container = ((S3HPath) next).getContainerName();
                        ((S3HSession) controller.getSession()).setVersioning(container,
                                bucketMfaButton.state() == NSCell.NSOnState,
                                bucketVersioningButton.state() == NSCell.NSOnState
                        );
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
        this.acl = acl;
        this.aclTable.reloadData();
    }

    @Outlet
    private NSTableView aclTable;
    private ListDataSource aclTableModel;
    private AbstractTableDelegate<Acl.UserAndRole> aclTableDelegate;

    public static final String HEADER_ACL_GRANTEE_COLUMN = "GRANTEE";
    public static final String HEADER_ACL_PERMISSION_COLUMN = "PERMISSION";

    private final NSComboBoxCell permissionCellPrototype = NSComboBoxCell.comboBoxCell();

    public void setAclTable(final NSTableView t) {
        this.aclTable = t;
        {
            this.permissionCellPrototype.setFont(NSFont.systemFontOfSize(NSFont.smallSystemFontSize()));
            this.permissionCellPrototype.setControlSize(NSCell.NSSmallControlSize);
            this.permissionCellPrototype.setCompletes(false);
            this.permissionCellPrototype.setBordered(false);
            this.permissionCellPrototype.setButtonBordered(false);
            for(Acl.Role permission : controller.getSession().getAvailableAclRoles()) {
                this.permissionCellPrototype.addItemWithObjectValue(NSString.stringWithString(permission.getName()));
            }
        }
        this.aclTable.tableColumnWithIdentifier(HEADER_ACL_PERMISSION_COLUMN).setDataCell(permissionCellPrototype);
        this.aclTable.setDataSource((aclTableModel = new ListDataSource() {
            /**
             * @param view
             */
            public NSInteger numberOfRowsInTableView(NSTableView view) {
                return new NSInteger(acl.size());
            }

            /**
             * @param view
             * @param tableColumn
             * @param row
             */
            public NSObject tableView_objectValueForTableColumn_row(NSTableView view, NSTableColumn tableColumn,
                                                                    NSInteger row) {
                final String identifier = tableColumn.identifier();
                final Acl.UserAndRole grant = acl.get(row.intValue());
                if(identifier.equals(HEADER_ACL_GRANTEE_COLUMN)) {
                    return NSString.stringWithString(grant.getUser().getDisplayName());
                }
                if(identifier.equals(HEADER_ACL_PERMISSION_COLUMN)) {
                    return NSString.stringWithString(grant.getRole().getName());
                }
                return null;
            }

            @Override
            public void tableView_setObjectValue_forTableColumn_row(NSTableView view, NSObject value,
                                                                    NSTableColumn c, NSInteger row) {
                if(StringUtils.isNotBlank(value.toString())) {
                    final Acl.UserAndRole grant = acl.get(row.intValue());
                    if(c.identifier().equals(HEADER_ACL_GRANTEE_COLUMN)) {
                        grant.getUser().setIdentifier(value.toString());
                        if(StringUtils.isNotBlank(grant.getUser().getIdentifier())
                                && StringUtils.isNotBlank(grant.getRole().getName())) {
                            InfoController.this.aclInputDidEndEditing();
                        }
                    }
                    if(c.identifier().equals(HEADER_ACL_PERMISSION_COLUMN)) {
                        grant.getRole().setName(value.toString());
                        if(StringUtils.isNotBlank(grant.getUser().getIdentifier())
                                && StringUtils.isNotBlank(grant.getRole().getName())) {
                            InfoController.this.aclInputDidEndEditing();
                        }
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
                if(StringUtils.isNotEmpty(c.getUser().getDisplayName())) {
                    return c.getUser().getDisplayName();
                }
                return Locale.localizedString(c.getUser().getIdentifier(), "S3");
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
                final Acl.UserAndRole grant = acl.get(row.intValue());
                cell.setPlaceholderString(grant.getUser().getPlaceholder());
                if(c.identifier().equals(HEADER_ACL_GRANTEE_COLUMN)) {
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
    }

    private Selector getAclSelector(Acl.User user) {
        if(user.isDomainIdentifier()) {
            return Foundation.selector("aclDomainAddButtonClicked:");
        }
        if(user.isEmailIdentifier()) {
            return Foundation.selector("aclEmailAddButtonClicked:");
        }
        if(user.isGroupIdentifier()) {
            return Foundation.selector("aclGroupAddButtonClicked:");
        }
        return Foundation.selector("aclCanonicalAddButtonClicked:");
    }

    @Outlet
    private NSPopUpButton aclAddButton;

    public void setAclAddButton(NSPopUpButton b) {
        this.aclAddButton = b;
        this.aclAddButton.setTarget(this.id());
        this.aclAddButton.addItemWithTitle("");
        this.aclAddButton.lastItem().setImage(IconCache.iconNamed("gear.tiff"));
        for(Acl.User user : controller.getSession().getAvailableAclUsers()) {
            this.aclAddButton.addItemWithTitle(user.getDisplayName());
            this.aclAddButton.lastItem().setAction(this.getAclSelector(user));
            this.aclAddButton.lastItem().setTarget(this.id());
            this.aclAddButton.lastItem().setRepresentedObject(user.getIdentifier());
        }
    }

    @Action
    public void aclCanonicalAddButtonClicked(NSMenuItem sender) {
        this.aclAddButtonClicked(new Acl.CanonicalUser(sender.representedObject()));
    }

    @Action
    public void aclDomainAddButtonClicked(NSMenuItem sender) {
        this.aclAddButtonClicked(new Acl.DomainUser(sender.representedObject()));
    }

    @Action
    public void aclEmailAddButtonClicked(NSMenuItem sender) {
        this.aclAddButtonClicked(new Acl.EmailUser(sender.representedObject()));
    }

    @Action
    public void aclGroupAddButtonClicked(NSMenuItem sender) {
        this.aclAddButtonClicked(new Acl.GroupUser(sender.representedObject()));
    }

    private void aclAddButtonClicked(Acl.User grantee) {
        this.aclAddButtonClicked(new Acl.UserAndRole(grantee, new Acl.Role("")));
        this.aclInputDidEndEditing();
    }

    /**
     * Add to the table, reload data and select inserted row.
     *
     * @param acl The acl to insert.
     */
    private void aclAddButtonClicked(Acl.UserAndRole acl) {
        final int index = this.acl.size();
        this.acl.add(index, acl);
        this.setAcl(this.acl);
        aclTable.selectRowIndexes(NSIndexSet.indexSetWithIndex(new NSInteger(index)), false);
        aclTable.editRow(aclTable.columnWithIdentifier(HEADER_ACL_GRANTEE_COLUMN), new NSInteger(index), true);
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
        int row = aclTable.selectedRow().intValue();
        acl.remove(row);
        this.setAcl(acl);
        this.aclInputDidEndEditing();
    }

    private void aclInputDidEndEditing() {
        if(this.toggleAclSettings(false)) {
            controller.background(new BrowserBackgroundAction(controller) {

                public void run() {
                    for(Path next : files) {
                        Acl acl = new Acl();
                        acl.addAll(InfoController.this.acl.toArray(new Acl.UserAndRole[InfoController.this.acl.size()]));
                        next.writeAcl(acl, true);
                    }
                }

                @Override
                public void cleanup() {
                    toggleAclSettings(true);
                }

                @Override
                public String getActivity() {
                    return MessageFormat.format(Locale.localizedString("Changing permission of {0} to {1}", "Status"),
                            files.get(0).getName(), acl);
                }
            });
        }
    }

    @Outlet
    private NSTableView metadataTable;
    private ListDataSource metadataTableModel;
    private AbstractTableDelegate<String> metadataTableDelegate;

    public static final String HEADER_METADATA_NAME_COLUMN = "NAME";
    public static final String HEADER_METADATA_VALUE_COLUMN = "VALUE";

    /**
     * Custom HTTP headers for REST protocols
     */
    private Map<String, String> metadata
            = new TreeMap<String, String>();

    /**
     * Replace current metadata model. Will reload the table view.
     *
     * @param m The new header key and values
     */
    private void setMetadata(Map<String, String> m) {
        metadata.clear();
        metadata.putAll(m);
        metadataTable.reloadData();
    }

    public void setMetadataTable(final NSTableView t) {
        this.metadataTable = t;
        this.metadataTable.setDataSource((metadataTableModel = new ListDataSource() {
            /**
             * @param view
             */
            public NSInteger numberOfRowsInTableView(NSTableView view) {
                return new NSInteger(metadata.size());
            }

            /**
             * @param view
             * @param tableColumn
             * @param row
             */
            public NSObject tableView_objectValueForTableColumn_row(NSTableView view, NSTableColumn tableColumn,
                                                                    NSInteger row) {
                final String identifier = tableColumn.identifier();
                if(identifier.equals(HEADER_METADATA_NAME_COLUMN)) {
                    final String name = metadata.keySet().toArray(new String[metadata.size()])[row.intValue()];
                    return NSString.stringWithString(StringUtils.isNotEmpty(name) ? name : "");
                }
                if(identifier.equals(HEADER_METADATA_VALUE_COLUMN)) {
                    final String value = metadata.values().toArray(new String[metadata.size()])[row.intValue()];
                    return NSString.stringWithString(StringUtils.isNotEmpty(value) ? value : "");
                }
                return null;
            }

            @Override
            public void tableView_setObjectValue_forTableColumn_row(NSTableView view, NSObject value,
                                                                    NSTableColumn c, NSInteger row) {
                if(StringUtils.isNotBlank(value.toString())) {
                    final String previousKey = metadata.keySet().toArray(new String[metadata.size()])[row.intValue()];
                    final String previousValue = metadata.values().toArray(new String[metadata.size()])[row.intValue()];
                    metadata.remove(previousKey);
                    if(c.identifier().equals(HEADER_METADATA_NAME_COLUMN)) {
                        metadata.put(value.toString(), previousValue);
                        if(StringUtils.isNotBlank(previousValue)) {
                            // Only update if both fields are set
                            metadataInputDidEndEditing();
                        }
                    }
                    if(c.identifier().equals(HEADER_METADATA_VALUE_COLUMN)) {
                        metadata.put(previousKey, value.toString());
                        if(StringUtils.isNotBlank(previousKey)) {
                            // Only update if both fields are set
                            metadataInputDidEndEditing();
                        }
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
        }).id());
    }

    @Outlet
    private NSPopUpButton metadataAddButton;

    public void setMetadataAddButton(NSPopUpButton b) {
        this.metadataAddButton = b;
        this.metadataAddButton.setTarget(this.id());
        this.metadataAddButton.addItemWithTitle("");
        this.metadataAddButton.lastItem().setImage(IconCache.iconNamed("gear.tiff"));
        this.metadataAddButton.addItemWithTitle("Custom Header");
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
     * @param sender
     * @see org.jets3t.service.Constants#REST_METADATA_PREFIX
     * @see com.rackspacecloud.client.cloudfiles.FilesConstants#X_OBJECT_META
     */
    @Action
    public void metadataAddCustomClicked(ID sender) {
        this.addMetadataItem(Locale.localizedString("Unknown"));
    }

    @Action
    public void metadataAddCacheControlClicked(ID sender) {
        this.addMetadataItem("Cache-Control",
                "public,max-age=" + Preferences.instance().getInteger("s3.cache.seconds"));
    }

    @Action
    public void metadataAddContentTypeClicked(ID sender) {
        this.addMetadataItem("Content-Type", "", true);
    }

    @Action
    public void metadataAddContentEncodingClicked(ID sender) {
        this.addMetadataItem("Content-Encoding", "", true);
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
        this.addMetadataItem("Pragma", "", true);
    }

    /**
     * Add new metadata row and selects the name column
     *
     * @param name
     */
    private void addMetadataItem(String name) {
        this.addMetadataItem(name, "", false);
    }

    /**
     * Add new metadata row and selects the value column
     *
     * @param name
     * @param value
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
        Map<String, String> m = new TreeMap<String, String>(metadata);
        m.put(name, value);
        this.setMetadata(m);
        int row = 0;
        for(String key : m.keySet()) {
            if(key.equals(name)) {
                break;
            }
            row++;
        }
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
        Map<String, String> m = new TreeMap<String, String>(metadata);
        NSIndexSet iterator = metadataTable.selectedRowIndexes();
        for(NSUInteger index = iterator.firstIndex(); !index.equals(NSIndexSet.NSNotFound); index = iterator.indexGreaterThanIndex(index)) {
            m.remove(new ArrayList<String>(m.keySet()).get(index.intValue()));
        }
        this.setMetadata(m);
        this.metadataInputDidEndEditing();
    }

    private void metadataInputDidEndEditing() {
        if(toggleMetadataSettings(false)) {
            controller.background(new BrowserBackgroundAction(controller) {

                public void run() {
                    for(Path next : files) {
                        ((CloudPath) next).writeMetadata(metadata);
                    }
                }

                @Override
                public void cleanup() {
                    toggleMetadataSettings(true);
                }

                @Override
                public String getActivity() {
                    return MessageFormat.format(Locale.localizedString("Writing metadata of {0}", "Status"),
                            files.get(0).getName());
                }
            });
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
        return 500;
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
         * @param controller
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

    private static final String TOOLBAR_ITEM_GENERAL = "general";
    private static final String TOOLBAR_ITEM_PERMISSIONS = "permissions";
    private static final String TOOLBAR_ITEM_DISTRIBUTION = "distribution";
    private static final String TOOLBAR_ITEM_S3 = "s3";
    private static final String TOOLBAR_ITEM_METADATA = "metadata";

    @Override
    protected void setSelectedTab(int tab) {
        if(-1 == tab) {
            tab = 0;
        }
        final String item = tabView.tabViewItemAtIndex(tab).identifier();
        if(this.validateTabWithIdentifier(item)) {
            super.setSelectedTab(tab);
        }
        else {
            super.setSelectedTab(-1);
        }
    }

    @Override
    public boolean validateToolbarItem(final NSToolbarItem item) {
        final String itemIdentifier = item.itemIdentifier();
        final Session session = controller.getSession();
        if(itemIdentifier.equals(TOOLBAR_ITEM_DISTRIBUTION)) {
            // Give icon and label of the given session
            item.setImage(IconCache.iconNamed(session.getHost().getProtocol().disk(), 32));
        }
        else if(itemIdentifier.equals(TOOLBAR_ITEM_PERMISSIONS)) {
            item.setImage(IconCache.iconNamed("NSUserAccounts", 32));
        }
        else if(itemIdentifier.equals(TOOLBAR_ITEM_S3)) {
            // Currently these settings are only available for Amazon S3
            item.setLabel(Protocol.S3.getName());
            item.setImage(IconCache.iconNamed(Protocol.S3.disk(), 32));
        }
        else if(itemIdentifier.equals(TOOLBAR_ITEM_METADATA)) {
            // Give icon of the given session
            item.setImage(IconCache.iconNamed(session.getHost().getProtocol().disk(), 32));
            item.setImage(IconCache.iconNamed("pencil", 32));
        }
        return super.validateToolbarItem(item);
    }

    @Override
    protected boolean validateTabWithIdentifier(String itemIdentifier) {
        final Session session = controller.getSession();
        final boolean anonymous = session.getHost().getCredentials().isAnonymousLogin();
        if(itemIdentifier.equals(TOOLBAR_ITEM_PERMISSIONS)) {
            // Anonymous never has the right to updated permissions
            return !anonymous;
        }
        if(itemIdentifier.equals(TOOLBAR_ITEM_DISTRIBUTION)) {
            if(anonymous) {
                return false;
            }
            if(session instanceof CloudSession) {
                return ((CloudSession) session).getSupportedDistributionMethods().size() > 0;
            }
            // Not enabled if not a cloud session
            return false;
        }
        if(itemIdentifier.equals(TOOLBAR_ITEM_S3)) {
            if(session instanceof S3HSession) {
                return !anonymous;
            }
            // Not enabled if not a cloud session
            return false;
        }
        if(itemIdentifier.equals(TOOLBAR_ITEM_METADATA)) {
            if(session instanceof CloudSession) {
                return !anonymous;
            }
            // Not enabled if not a cloud session
            return false;
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

    /**
     * @param files
     */
    public void setFiles(List<Path> files) {
        if(files.isEmpty()) {
            return;
        }
        // Initialized before with a different file set
        boolean update = !this.files.isEmpty();
        this.files = files;
        this.initGeneral();
        // Sum of files
        this.initSize();
        this.initChecksum();
        // Read HTTP URL
        this.initWebUrl();
        // Read permissions
        this.initPermissions();
        this.initAcl();
        // S3 Bucket attributes
        this.initS3();
        // HTTP custom headers
        this.initMetadata();
        if(!update) {
            this.initDistribution();
        }
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
        return Arrays.asList(panelGeneral, panelPermissions, panelMetadata, panelDistribution, panelCloud);
    }

    private String getName() {
        final int count = this.numberOfFiles();
        if(count > 1) {
            return "(" + Locale.localizedString("Multiple files") + ")";
        }
        for(Path next : files) {
            return next.getName();
        }
        return null;
    }

    @Override
    protected NSUInteger getToolbarSize() {
        return NSToolbar.NSToolbarSizeModeSmall;
    }

    private void initGeneral() {
        final int count = this.numberOfFiles();
        if(count > 0) {
            Path file = this.files.get(0);
            final String filename = this.getName();
            filenameField.setStringValue(filename);
            this.window().setTitle(title + " – " + filename);
            filenameField.setEnabled(1 == count && file.isRenameSupported());
            String path;
            if(StringUtils.isNotEmpty(file.getSymlinkTarget())) {
                path = file.getSymlinkTarget();
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
            this.updateField(ownerField, count > 1 ? "(" + Locale.localizedString("Multiple files") + ")" :
                    file.attributes().getOwner(), TRUNCATE_MIDDLE_ATTRIBUTES);

            if(count > 1) {
                iconImageView.setImage(IconCache.iconNamed("NSMultipleDocuments", 32));
            }
            else {
                iconImageView.setImage(IconCache.instance().iconForPath(files.get(0), 32));
            }
        }
    }

    private void initWebUrl() {
        if(this.numberOfFiles() > 1) {
            this.updateField(webUrlField, "(" + Locale.localizedString("Multiple files") + ")");
            webUrlField.setToolTip("");
        }
        else {
            this.updateField(webUrlField, Locale.localizedString("Unknown"));
            controller.background(new BrowserBackgroundAction(controller) {
                String url;

                public void run() {
                    for(Path next : files) {
                        url = next.toHttpURL();
                    }
                }

                @Override
                public void cleanup() {
                    if(StringUtils.isNotBlank(url)) {
                        webUrlField.setAttributedStringValue(
                                HyperlinkAttributedStringFactory.create(
                                        NSMutableAttributedString.create(url, TRUNCATE_MIDDLE_ATTRIBUTES), url)
                        );
                        webUrlField.setToolTip(url);
                    }
                }
            });
        }
    }

    /**
     *
     */
    private void initPermissions() {
        permissionsField.setStringValue(Locale.localizedString("Unknown"));
        octalField.setStringValue(Locale.localizedString("Unknown"));
        // Disable Apply button and start progress indicator
        if(this.togglePermissionSettings(false)) {
            controller.background(new BrowserBackgroundAction(controller) {
                public void run() {
                    for(Path next : files) {
                        if(this.isCanceled()) {
                            break;
                        }
                        if(next.attributes().getPermission().equals(Permission.EMPTY)) {
                            // Read permission of every selected path
                            next.readUnixPermission();
                        }
                    }
                }

                @Override
                public void cleanup() {
                    Permission permission = null;
                    for(Path next : files) {
                        permission = next.attributes().getPermission();
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
                    octalField.setStringValue(permission.getOctalString());
                    final int count = numberOfFiles();
                    if(count > 1) {
                        permissionsField.setStringValue("(" + Locale.localizedString("Multiple files") + ")");
                    }
                    else {
                        permissionsField.setStringValue(permission.toString());
                    }
                    togglePermissionSettings(true);
                }

                @Override
                public String getActivity() {
                    return MessageFormat.format(Locale.localizedString("Getting permission of {0}", "Status"),
                            files.get(0).getName());
                }
            });
        }
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

    /**
     * Read content distribution settings
     */
    private void initDistribution() {
        this.distributionStatusField.setStringValue(Locale.localizedString("Unknown"));
        this.distributionCnameField.cell().setPlaceholderString(Locale.localizedString("Unknown"));
        distributionUrlField.setStringValue(Locale.localizedString("Unknown"));
        distributionStatusField.setStringValue(Locale.localizedString("Unknown"));
        distributionCnameField.setStringValue(Locale.localizedString("Unknown"));
        distributionDeliveryPopup.removeAllItems();
        distributionDeliveryPopup.addItemWithTitle(Locale.localizedString("Unknown"));
        if(this.toggleDistributionSettings(false)) {
            CloudSession session = (CloudSession) controller.getSession();
            distributionEnableButton.setTitle(MessageFormat.format(Locale.localizedString("Enable {0} Distribution", "Status"),
                    session.getDistributionServiceName()));
            distributionDeliveryPopup.removeItemWithTitle(Locale.localizedString("Unknown"));
            for(Distribution.Method method : session.getSupportedDistributionMethods()) {
                distributionDeliveryPopup.addItemWithTitle(method.toString());
                distributionDeliveryPopup.itemWithTitle(method.toString()).setRepresentedObject(method.toString());
            }
            distributionDeliveryPopup.selectItemWithTitle(Distribution.DOWNLOAD.toString());

            this.distributionStatusButtonClicked(null);
        }
    }

    /**
     * Updates the size field by iterating over all files and
     * reading the cached size value in the attributes of the path
     */
    private void initSize() {
        if(this.toggleSizeSettings(false)) {
            controller.background(new BrowserBackgroundAction(controller) {
                double size = 0;

                public void run() {
                    for(Path next : files) {
                        if(-1 == next.attributes().getSize()) {
                            next.readSize();
                        }
                        if(-1 < next.attributes().getSize()) {
                            size += next.attributes().getSize();
                        }
                    }
                }

                @Override
                public void cleanup() {
                    StringBuilder formatted = new StringBuilder(Status.getSizeAsString(size));
                    if(size > -1) {
                        formatted.append(" (").append(NumberFormat.getInstance().format(size)).append(" bytes)");
                    }
                    sizeField.setAttributedStringValue(NSAttributedString.attributedStringWithAttributes(
                            formatted.toString(),
                            TRUNCATE_MIDDLE_ATTRIBUTES));
                    toggleSizeSettings(true);
                }

                @Override
                public String getActivity() {
                    return MessageFormat.format(Locale.localizedString("Getting size of {0}", "Status"),
                            files.get(0).getName());
                }
            });
        }
    }

    private void initChecksum() {
        if(this.numberOfFiles() > 1) {
            checksumField.setStringValue("(" + Locale.localizedString("Multiple files") + ")");
        }
        else {
            if(this.toggleSizeSettings(false)) {
                controller.background(new BrowserBackgroundAction(controller) {

                    public void run() {
                        for(Path file : files) {
                            if(null == file.attributes().getChecksum()) {
                                file.readChecksum();
                            }
                        }
                    }

                    @Override
                    public void cleanup() {
                        for(Path file : files) {
                            if(StringUtils.isEmpty(file.attributes().getChecksum())) {
                                updateField(checksumField, Locale.localizedString("Unknown"));
                            }
                            else {
                                updateField(checksumField, file.attributes().getChecksum());
                            }
                        }
                        toggleSizeSettings(true);
                    }

                    @Override
                    public String getActivity() {
                        return MessageFormat.format(Locale.localizedString("Compute MD5 hash of {0}", "Status"),
                                files.get(0).getName());
                    }
                });
            }
        }
    }

    /**
     * Toggle settings before and after update
     *
     * @param stop Enable controls and stop progress spinner
     */
    private boolean toggleS3Settings(final boolean stop) {
        final Session session = controller.getSession();
        // Amazon S3 only
        boolean enable = session instanceof S3HSession;
        if(enable) {
            final Credentials credentials = session.getHost().getCredentials();
            enable = !credentials.isAnonymousLogin();
        }
        boolean logging = false;
        boolean versioning = false;
        if(enable) {
            logging = ((S3HSession) session).isLoggingSupported();
            versioning = ((S3HSession) session).isVersioningSupported();
        }
        for(Path file : files) {
            boolean container = false;
            if(enable) {
                container = ((CloudPath) file).isContainer();
            }
            bucketVersioningButton.setEnabled(stop && enable && versioning && container);
            bucketMfaButton.setEnabled(stop && enable && versioning && container
                    && bucketVersioningButton.state() == NSCell.NSOnState);
            bucketLoggingButton.setEnabled(stop && enable && logging && container);
            storageClassPopup.setEnabled(stop && enable && file.attributes().isFile());
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
        bucketLoggingButton.setToolTip(Locale.localizedString("Unknown"));
        s3PublicUrlField.setStringValue(Locale.localizedString("Unknown"));
        s3torrentUrlField.setStringValue(Locale.localizedString("Unknown"));
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
                s3PublicUrlField.setToolTip("");
                s3torrentUrlField.setStringValue("(" + Locale.localizedString("Multiple files") + ")");
                s3torrentUrlField.setToolTip("");
            }
            else {
                for(Path file : files) {
                    if(file.attributes().isFile()) {
                        final S3HPath s3 = (S3HPath) file;
                        bucketLoggingButton.setToolTip(
                                s3.getContainerName() + "/" + Preferences.instance().getProperty("s3.logging.prefix"));
                        final String redundancy = s3.attributes().getStorageClass();
                        if(StringUtils.isNotEmpty(redundancy)) {
                            storageClassPopup.removeItemWithTitle(Locale.localizedString("Unknown"));
                            storageClassPopup.selectItemWithTitle(Locale.localizedString(redundancy, "S3"));
                        }
                        final String signedUrl = s3.createSignedUrl();
                        if(StringUtils.isNotBlank(signedUrl)) {
                            s3PublicUrlField.setAttributedStringValue(
                                    HyperlinkAttributedStringFactory.create(
                                            NSMutableAttributedString.create(signedUrl, TRUNCATE_MIDDLE_ATTRIBUTES), signedUrl)
                            );
                            s3PublicUrlField.setToolTip(signedUrl);
                        }
                        final String torrent = s3.createTorrentUrl();
                        if(StringUtils.isNotBlank(torrent)) {
                            s3torrentUrlField.setAttributedStringValue(
                                    HyperlinkAttributedStringFactory.create(
                                            NSMutableAttributedString.create(torrent, TRUNCATE_MIDDLE_ATTRIBUTES), torrent)
                            );
                            s3torrentUrlField.setToolTip(torrent);
                        }
                    }
                }
            }
            controller.background(new BrowserBackgroundAction(controller) {
                private String location = null;
                private boolean logging = false;
                private boolean versioning = false;
                private boolean mfa = false;

                public void run() {
                    for(Path file : files) {
                        final S3HSession s = (S3HSession) controller.getSession();
                        final String container = ((S3HPath) file).getContainerName();
                        location = s.getLocation(container);
                        logging = s.isLogging(container);
                        versioning = s.isVersioning(container);
                        mfa = s.isMultiFactorAuthentication(container);
                    }
                }

                @Override
                public void cleanup() {
                    bucketLoggingButton.setState(logging ? NSCell.NSOnState : NSCell.NSOffState);
                    if(StringUtils.isNotBlank(location)) {
                        bucketLocationField.setStringValue(Locale.localizedString(location, "S3"));
                    }
                    bucketVersioningButton.setState(versioning ? NSCell.NSOnState : NSCell.NSOffState);
                    bucketMfaButton.setEnabled(versioning);
                    bucketMfaButton.setState(mfa ? NSCell.NSOnState : NSCell.NSOffState);
                    toggleS3Settings(true);
                }

                @Override
                public String getActivity() {
                    return MessageFormat.format(Locale.localizedString("Reading metadata of {0}", "Status"),
                            files.get(0).getName());
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
        boolean enable = this.numberOfFiles() == 1;
        if(enable) {
            final Session session = controller.getSession();
            final Credentials credentials = session.getHost().getCredentials();
            enable = enable && !credentials.isAnonymousLogin();
            enable = enable && session.isAclSupported();
        }
        aclTable.setEnabled(stop && enable);
        aclAddButton.setEnabled(stop && enable);
        aclRemoveButton.setEnabled(stop && enable);
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
        boolean enable = this.numberOfFiles() == 1;
        if(enable) {
            for(Path file : files) {
                final Credentials credentials = file.getHost().getCredentials();
                enable = enable && !credentials.isAnonymousLogin();
                enable = enable && file instanceof CloudPath;
                enable = enable && file.attributes().isFile();
            }
        }
        metadataTable.setEnabled(stop && enable);
        metadataAddButton.setEnabled(stop && enable);
        metadataRemoveButton.setEnabled(stop && enable);
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
        this.setMetadata(Collections.<String, String>emptyMap());
        if(this.toggleMetadataSettings(false)) {
            controller.background(new BrowserBackgroundAction(controller) {
                private Map<String, String> updated = new HashMap<String, String>();

                public void run() {
                    for(Path next : files) {
                        // Reading HTTP headers custom metadata
                        updated.putAll(((CloudPath) next).readMetadata());
                    }
                }

                @Override
                public void cleanup() {
                    setMetadata(updated);
                    toggleMetadataSettings(true);
                }

                @Override
                public String getActivity() {
                    return MessageFormat.format(Locale.localizedString("Reading metadata of {0}", "Status"),
                            files.get(0).getName());
                }
            });
        }
    }

    /**
     * Read grants in the background
     */
    private void initAcl() {
        this.setAcl(Collections.<Acl.UserAndRole>emptyList());
        if(this.toggleAclSettings(false)) {
            controller.background(new BrowserBackgroundAction(controller) {
                private List<Acl.UserAndRole> updated = new ArrayList<Acl.UserAndRole>();

                public void run() {
                    for(Path next : files) {
                        if(Acl.EMPTY.equals(next.attributes().getAcl())) {
                            next.readAcl();
                        }
                        updated.addAll(next.attributes().getAcl().asList());
                    }
                }

                @Override
                public void cleanup() {
                    setAcl(updated);
                    toggleAclSettings(true);
                }

                @Override
                public String getActivity() {
                    return MessageFormat.format(Locale.localizedString("Getting permission of {0}", "Status"),
                            files.get(0).getName());
                }
            });
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
                            current.getParent().getAbsolute(), filenameField.stringValue(), current.attributes().getType());
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
                if(!file.attributes().getPermission().equals(permission)) {
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
        if(this.togglePermissionSettings(false)) {
            final boolean recursive = recursiveCheckbox.state() == NSCell.NSOnState;
            // send the changes to the remote host
            controller.background(new BrowserBackgroundAction(controller) {
                public void run() {
                    for(Path next : files) {
                        if(recursive || !next.attributes().getPermission().equals(permission)) {
                            next.writeUnixPermission(permission, recursive);
                        }
                        if(!controller.isConnected()) {
                            break;
                        }
                    }
                }

                @Override
                public void cleanup() {
                    initPermissions();
                    togglePermissionSettings(true);
                }

                @Override
                public String getActivity() {
                    return MessageFormat.format(Locale.localizedString("Changing permission of {0} to {1}", "Status"),
                            files.get(0).getName(), permission);
                }
            });
        }
    }

    /**
     * Toggle settings before and after update
     *
     * @param stop Enable controls and stop progress spinner
     * @return True if controls are enabled for the given protocol in idle state
     */
    private boolean togglePermissionSettings(final boolean stop) {
        final Session session = controller.getSession();
        final Credentials credentials = session.getHost().getCredentials();
        boolean enable = !credentials.isAnonymousLogin() && session.isUnixPermissionsSupported();
        recursiveCheckbox.setEnabled(stop && enable);
        for(Path next : files) {
            if(next.attributes().isFile()) {
                recursiveCheckbox.setState(NSCell.NSOffState);
                recursiveCheckbox.setEnabled(false);
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
        // Not all cloud providers support different distributions
        final Session session = controller.getSession();
        boolean enable = session instanceof CloudSession;
        final Credentials credentials = session.getHost().getCredentials();
        enable = enable && !credentials.isAnonymousLogin();
        if(enable) {
            enable = ((CloudSession) session).getSupportedDistributionMethods().size() > 0;
        }

        distributionEnableButton.setEnabled(stop && enable);
        distributionLoggingButton.setEnabled(stop && enable);
        distributionCnameField.setEnabled(stop && enable);
        // Amazon S3 only
        distributionCnameField.setEnabled(stop && enable && session instanceof S3HSession);
        distributionDeliveryPopup.setEnabled(stop && enable);
        if(stop) {
            distributionProgress.stopAnimation(null);
        }
        else if(enable) {
            distributionProgress.startAnimation(null);
        }
        return enable;
    }

    @Action
    public void distributionApplyButtonClicked(final ID sender) {
        if(this.toggleDistributionSettings(false)) {
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

                @Override
                public String getActivity() {
                    return MessageFormat.format(Locale.localizedString("Writing metadata of {0}", "Status"),
                            files.get(0).getName());
                }
            });
        }
    }

    @Action
    public void distributionStatusButtonClicked(final ID sender) {
        if(this.toggleDistributionSettings(false)) {
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
                    toggleDistributionSettings(true);
                }

                @Override
                public String getActivity() {
                    return MessageFormat.format(Locale.localizedString("Reading metadata of {0}", "Status"),
                            files.get(0).getName());
                }
            });
        }
    }

    @Action
    public void calculateSizeButtonClicked(final ID sender) {
        if(this.toggleSizeSettings(false)) {
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
                    initSize();
                    toggleSizeSettings(true);
                }

                /**
                 * Calculates recursively the size of this path
                 *
                 * @return The size of the file or the sum of all containing files if a directory
                 * @warn Potentially lengthy operation
                 */
                private double calculateSize(AbstractPath p) {
                    if(p.attributes().isDirectory()) {
                        long size = 0;
                        for(AbstractPath next : p.childs()) {
                            size += this.calculateSize(next);
                        }
                        ((PathAttributes) p.attributes()).setSize(size);
                    }
                    return p.attributes().getSize();
                }

                @Override
                public String getActivity() {
                    return MessageFormat.format(Locale.localizedString("Getting size of {0}", "Status"),
                            files.get(0).getName());
                }
            });
        }
    }

    /**
     * @param stop Enable controls and stop progress spinner
     * @return
     */
    private boolean toggleSizeSettings(final boolean stop) {
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
        if(tab.equals(TOOLBAR_ITEM_GENERAL)) {
            NSWorkspace.sharedWorkspace().openURL(
                    NSURL.URLWithString(Preferences.instance().getProperty("website.help")
                            + "/howto/info")
            );
        }
        if(tab.equals(TOOLBAR_ITEM_PERMISSIONS)) {
            NSWorkspace.sharedWorkspace().openURL(
                    NSURL.URLWithString(Preferences.instance().getProperty("website.help")
                            + "/howto/info")
            );
        }
        if(tab.equals(TOOLBAR_ITEM_METADATA)) {
            NSWorkspace.sharedWorkspace().openURL(
                    NSURL.URLWithString(Preferences.instance().getProperty("website.help")
                            + "/howto/s3")
            );
        }
        if(tab.equals(TOOLBAR_ITEM_S3)) {
            NSWorkspace.sharedWorkspace().openURL(
                    NSURL.URLWithString(Preferences.instance().getProperty("website.help")
                            + "/howto/s3")
            );
        }
        if(tab.equals(TOOLBAR_ITEM_DISTRIBUTION)) {
            NSWorkspace.sharedWorkspace().openURL(
                    NSURL.URLWithString(Preferences.instance().getProperty("website.help")
                            + "/howto/cdn")
            );
        }
    }
}
