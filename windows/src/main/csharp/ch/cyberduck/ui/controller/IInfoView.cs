// 
// Copyright (c) 2010-2013 Yves Langisch. All rights reserved.
// http://cyberduck.ch/
// 
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
// 
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
// 
// Bug fixes, suggestions and comments should be sent to:
// yves@cyberduck.ch
// 

using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Drawing;
using System.Windows.Forms;
using ch.cyberduck.core.cdn;

namespace Ch.Cyberduck.Ui.Controller
{
    public enum InfoTab
    {
        General,
        Permissions,
        Acl,
        Distribution,
        S3,
        Metadata
    }

    public interface IInfoView : IView
    {
        InfoTab ActiveTab { set; get; }

        Image ToolbarDistributionImage { set; }
        bool ToolbarDistributionEnabled { set; }
        bool ToolbarS3Enabled { set; }
        Image ToolbarS3Image { set; }
        String ToolbarS3Label { set; }
        bool ToolbarPermissionsEnabled { set; }
        bool ToolbarMetadataEnabled { set; }

        Image FileIcon { set; }
        string Filename { set; get; }
        bool FilenameEnabled { set; }
        bool SizeButtonEnabled { set; }
        bool SizeAnimationActive { set; }
        bool PermissionAnimationActive { set; }
        string FileSize { set; }
        string Path { set; }
        string PathToolTip { set; }
        string WebUrl { set; }
        string WebUrlTooltip { set; }
        string Kind { set; }
        string Permissions { set; }
        string FileOwner { set; }
        string Group { set; }
        string FileCreated { set; }
        string Modified { set; }
        string Checksum { set; }

        CheckState OwnerRead { set; get; }
        bool OwnerReadEnabled { set; get; }
        CheckState OwnerWrite { set; get; }
        bool OwnerWriteEnabled { set; get; }
        CheckState OwnerExecute { set; get; }
        bool OwnerExecuteEnabled { set; get; }
        CheckState GroupRead { set; get; }
        bool GroupReadEnabled { set; get; }
        CheckState GroupWrite { set; get; }
        bool GroupWriteEnabled { set; get; }
        CheckState GroupExecute { set; get; }
        bool GroupExecuteEnabled { set; get; }
        CheckState OtherRead { set; get; }
        bool OtherReadEnabled { set; get; }
        CheckState OtherWrite { set; get; }
        bool OtherWriteEnabled { set; get; }
        CheckState OtherExecute { set; get; }
        bool OtherExecuteEnabled { set; get; }
        bool RecursivePermissionsEnabled { set; }
        string OctalPermissions { get; set; }
        bool OctalPermissionsEnabled { set; }

        List<InfoController.UserAndRoleEntry> SelectedAclEntries { get; }
        bool AclPanel { set; }

        BindingList<InfoController.UserAndRoleEntry> AclDataSource { set; }
        bool AclTableEnabled { set; }
        bool AclAddEnabled { set; }
        bool AclRemoveEnabled { set; }
        bool AclAnimationActive { set; }
        string AclUrl { set; }
        bool AclUrlEnabled { set; }
        string AclUrlTooltip { set; }

        bool MetadataTableEnabled { set; }
        bool MetadataAddEnabled { set; }
        bool MetadataRemoveEnabled { set; }
        bool MetadataAnimationActive { set; }

        BindingList<InfoController.CustomHeaderEntry> MetadataDataSource { set; }
        List<InfoController.CustomHeaderEntry> SelectedMetadataEntries { get; }

        Distribution.Method DistributionDeliveryMethod { set; get; }
        bool DistributionDeliveryMethodEnabled { set; }
        bool Distribution { set; get; }
        bool DistributionEnabled { set; }
        string DistributionTitle { set; }
        string DistributionStatus { set; }
        bool DistributionLoggingCheckbox { set; get; }
        bool DistributionLoggingCheckboxEnabled { set; }
        string DistributionLoggingPopup { set; get; }
        bool DistributionLoggingPopupEnabled { set; }
        string DistributionUrl { set; }
        string DistributionUrlTooltip { set; }
        bool DistributionUrlEnabled { set; }
        string DistributionOrigin { set; }
        string DistributionCname { set; get; }
        string DistributionCnameUrl { set; }
        bool DistributionCnameUrlEnabled { set; }
        string DistributionCnameUrlTooltip { set; }
        bool DistributionCnameEnabled { set; }
        bool DistributionAnimationActive { set; }
        string DistributionDefaultRoot { set; get; }
        bool DistributionDefaultRootEnabled { set; }
        string DistributionInvalidationStatus { set; }
        string DistributionInvalidateObjectsTooltip { set; }
        bool DistributionInvalidateObjectsEnabled { set; }
        bool DistributionAnalyticsCheckbox { set; get; }
        bool DistributionAnalyticsCheckboxEnabled { set; }
        string DistributionAnalyticsSetupUrl { set; }
        bool DistributionAnalyticsSetupUrlEnabled { set; }

        string BucketLocation { set; }
        string Encryption { set; get; }
        bool EncryptionEnabled { set; get; }
        string StorageClass { set; get; }
        bool StorageClassEnabled { set; }
        string BucketLoggingPopup { set; get; }
        bool BucketLoggingPopupEnabled { set; }
        string S3PublicUrl { set; }
        bool S3PublicUrlEnabled { set; }
        string S3PublicUrlTooltip { set; }
        string S3PublicUrlValidity { set; }
        string S3TorrentUrl { set; }
        bool S3TorrentUrlEnabled { set; }
        string S3TorrentUrlTooltip { set; }
        bool S3AnimationActive { set; }
        bool BucketLoggingCheckbox { set; get; }
        bool BucketLoggingCheckboxEnabled { set; }
        string BucketLoggingTooltip { set; }
        bool BucketAnalyticsCheckbox { set; get; }
        bool BucketAnalyticsCheckboxEnabled { set; }
        string BucketAnalyticsSetupUrl { set; }
        bool BucketAnalyticsSetupUrlEnabled { set; }
        bool BucketVersioning { get; set; }
        bool BucketVersioningEnabled { set; }
        bool BucketMfa { set; get; }
        bool BucketMfaEnabled { set; }
        bool LifecycleTransitionCheckbox { set; get; }
        bool LifecycleTransitionCheckboxEnabled { set; }
        bool LifecycleDeleteCheckbox { set; get; }
        bool LifecycleDeleteCheckboxEnabled { set; }
        string LifecycleTransition { get; set; }
        bool LifecycleTransitionPopupEnabled { set; }
        string LifecycleDelete { get; set; }
        bool LifecycleDeletePopupEnabled { set; }

        event VoidHandler FilenameChanged;
        event VoidHandler CalculateSize;
        event VoidHandler OwnerReadChanged;
        event VoidHandler OwnerWriteChanged;
        event VoidHandler OwnerExecuteChanged;
        event VoidHandler GroupReadChanged;
        event VoidHandler GroupWriteChanged;
        event VoidHandler GroupExecuteChanged;
        event VoidHandler OtherReadChanged;
        event VoidHandler OtherWriteChanged;
        event VoidHandler OtherExecuteChanged;
        event VoidHandler ApplyRecursivePermissions;
        event VoidHandler OctalPermissionsChanged;

        event EventHandler<InfoHelpArgs> ShowHelp;
        void PopulateAclUsers(IDictionary<string, AsyncController.SyncDelegate> users);
        void PopulateAclRoles(IList<string> roles);
        void PopulateMetadata(IDictionary<string, AsyncController.SyncDelegate> metadata);
        void EditAclRow(InfoController.UserAndRoleEntry aclEntry, bool selectRole);
        void EditMetadataRow(InfoController.CustomHeaderEntry headerEntry, bool selectValue);
        void PopulateDistributionDeliveryMethod(IList<KeyValuePair<string, Distribution.Method>> methods);
        void PopulateDefaultRoot(IList<KeyValuePair<string, string>> roots);
        void PopulateBucketLogging(IList<string> buckets);
        void PopulateDistributionLogging(IList<string> buckets);
        void PopulateLifecycleTransitionPeriod(IList<KeyValuePair<string, string>> periods);
        void PopulateLifecycleDeletePeriod(IList<KeyValuePair<string, string>> periods);

        event VoidHandler DistributionDeliveryMethodChanged;
        event VoidHandler DistributionEnabledChanged;
        event VoidHandler DistributionLoggingCheckboxChanged;
        event VoidHandler DistributionLoggingPopupChanged;
        event VoidHandler DistributionAnalyticsCheckboxChanged;
        event VoidHandler DistributionCnameChanged;
        event VoidHandler DistributionDefaultRootChanged;
        event VoidHandler DistributionInvalidateObjects;

        void PopulateStorageClass(IList<KeyValuePair<string, string>> classes);
        void PopulateEncryption(IList<KeyValuePair<string, string>> algorithms);

        event VoidHandler BucketLoggingCheckboxChanged;
        event VoidHandler BucketAnalyticsCheckboxChanged;
        event VoidHandler BucketLoggingPopupChanged;
        event VoidHandler EncryptionChanged;
        event VoidHandler StorageClassChanged;
        event VoidHandler BucketVersioningChanged;
        event VoidHandler BucketMfaChanged;
        event VoidHandler LifecycleTransitionCheckboxChanged;
        event VoidHandler LifecycleTransitionPopupChanged;
        event VoidHandler LifecycleDeleteCheckboxChanged;
        event VoidHandler LifecycleDeletePopupChanged;

        event VoidHandler ActiveTabChanged;
    }
}