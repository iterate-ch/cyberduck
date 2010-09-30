// 
// Copyright (c) 2010 Yves Langisch. All rights reserved.
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
using ch.cyberduck.core.cloud;
using ch.cyberduck.ui.controller;

namespace Ch.Cyberduck.Ui.Controller
{
    public interface IInfoView : IView
    {
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

        BindingList<InfoController.CustomHeader> MetadataDataSource { set; }
        List<InfoController.CustomHeader> SelectedMetadataEntries { get; }

        Distribution.Method DistributionDeliveryMethod { set; get; }
        bool DistributionDeliveryMethodEnabled { set; }
        bool Distribution { set; get; }
        bool DistributionEnabled { set; }
        string DistributionTitle { set; }
        string DistributionStatus { set; }
        bool DistributionLogging { set; get; }
        bool DistributionLoggingEnabled { set; }
        string DistributionUrl { set; }
        string DistributionUrlTooltip { set; }
        bool DistributionUrlEnabled { set; }
        string DistributionCname { set; get; }
        string DistributionCnameUrl { set; }
        bool DistributionCnameUrlEnabled { set; }
        string DistributionCnameUrlTooltip { set; }
        bool DistributionCnameEnabled { set; }
        bool DistributionAnimationActive { set; }
        string DistributionDefaultRoot { set; get; }
        bool DistributionDefaultRootEnabled { set; }

        string BucketLocation { set; }
        string StorageClass { set; get; }
        bool StorageClassEnabled { set; }
        string S3PublicUrl { set; }
        bool S3PublicUrlEnabled { set; }
        string S3PublicUrlTooltip { set; }
        string S3PublicUrlValidity { set; }
        string S3TorrentUrl { set; }
        bool S3TorrentUrlEnabled { set; }
        string S3TorrentUrlTooltip { set; }
        bool S3AnimationActive { set; }
        bool BucketLogging { set; get; }
        string BucketLoggingTooltip { set; }
        bool BucketLoggingEnabled { set; }
        bool BucketVersioning { get; set; }
        bool BucketVersioningEnabled { set; }
        bool BucketMfa { set; get; }
        bool BucketMfaEnabled { set; }
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
        void EditMetadataRow(string name, bool selectValue);
        void PopulateDistributionDeliveryMethod(IList<KeyValuePair<string, Distribution.Method>> methods);
        void PopulateDefaultRoot(IList<string> roots);

        event VoidHandler DistributionDeliveryMethodChanged;
        event VoidHandler DistributionEnabledChanged;
        event VoidHandler DistributionLoggingChanged;
        event VoidHandler DistributionCnameChanged;
        event VoidHandler DistributionDefaultRootChanged;

        void PopulateStorageClass(IList<KeyValuePair<string, string>> classes);

        event VoidHandler BucketLoggingChanged;
        event VoidHandler StorageClassChanged;
        event VoidHandler BucketVersioningChanged;
        event VoidHandler BucketMfaChanged;
    }
}