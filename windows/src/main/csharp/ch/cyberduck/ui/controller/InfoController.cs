﻿// 
// Copyright (c) 2010-2016 Yves Langisch. All rights reserved.
// http://cyberduck.io/
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
// feedback@cyberduck.io
// 

using ch.cyberduck.core;
using ch.cyberduck.core.cdn;
using ch.cyberduck.core.cdn.features;
using ch.cyberduck.core.exception;
using ch.cyberduck.core.features;
using ch.cyberduck.core.formatter;
using ch.cyberduck.core.io;
using ch.cyberduck.core.lifecycle;
using ch.cyberduck.core.local;
using ch.cyberduck.core.logging;
using ch.cyberduck.core.pool;
using ch.cyberduck.core.preferences;
using ch.cyberduck.core.threading;
using ch.cyberduck.core.worker;
using Ch.Cyberduck.Core;
using Ch.Cyberduck.Core.Refresh;
using Ch.Cyberduck.Core.Refresh.ViewModels.Info;
using Ch.Cyberduck.Core.Refresh.Views;
using Ch.Cyberduck.Ui.Controller.Threading;
using Ch.Cyberduck.Ui.Winforms.Threading;
using java.lang;
using java.text;
using java.util;
using org.apache.commons.lang3;
using org.apache.logging.log4j;
using StructureMap;
using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Globalization;
using System.Linq;
using System.Media;
using System.Windows.Forms;
using static Ch.Cyberduck.ImageHelper;
using Boolean = java.lang.Boolean;
using Object = System.Object;
using String = System.String;
using StringBuilder = System.Text.StringBuilder;

namespace Ch.Cyberduck.Ui.Controller
{
    public sealed class InfoController : WindowController<IInfoView>
    {
        private static readonly Logger Log = LogManager.getLogger(typeof(InfoController).FullName);

        private static readonly string _multipleFilesString = "(" + LocaleFactory.localizedString("Multiple files") + ")";

        private readonly BrowserController _controller;
        private readonly FileDescriptor _descriptor = FileDescriptorFactory.get();
        private readonly LoginCallback _prompt;
        private readonly PathContainerService containerService = new DefaultPathContainerService();
        private BindingList<UserAndRoleEntry> _acl = new BindingList<UserAndRoleEntry>();
        private IList<Path> _files;
        private IList<KeyValuePair<string, string>> _lifecycleDeletePeriods;
        private IList<KeyValuePair<string, string>> _lifecycleTransitionPeriods;
        private PermissionOverwrite permissions = new PermissionOverwrite();

        private InfoController(BrowserController controller, IList<Path> files)
        {
            View = ObjectFactory.GetInstance<IInfoView>();
            _controller = controller;
            _prompt = LoginCallbackFactory.get(this);
            Files = files;

            _controller.View.ViewClosedEvent += delegate
            {
                if (!View.IsDisposed)
                {
                    View.Close();
                }
            };

            View.ActiveTabChanged += View_ActiveTabChanged;
        }

        public override bool Singleton
        {
            get { return PreferencesFactory.get().getBoolean("browser.info.inspector"); }
        }

        public IList<Path> Files
        {
            private get { return _files; }
            set
            {
                if (value.Count == 0)
                {
                    return;
                }

                _files = value;

                View.Filename = Name;
                ConfigureToolbar();
                ConfigureHelp();
                InitTab(View.ActiveTab);
            }
        }

        private int NumberOfFiles
        {
            get { return null == _files ? 0 : _files.Count; }
        }

        private string Name
        {
            get
            {
                if (NumberOfFiles > 1)
                {
                    return _multipleFilesString;
                }
                foreach (Path file in _files)
                {
                    return file.getName();
                }
                return null;
            }
        }

        private Path SelectedPath
        {
            get
            {
                if (_files.Count == 0) return null;

                return _files[0];
            }
        }

        private void View_ActiveTabChanged()
        {
            InitTab(View.ActiveTab);
        }

        private void InitTab(InfoTab activeTab)
        {
            switch (activeTab)
            {
                case InfoTab.General:
                    InitGeneral();
                    InitPermissions();
                    break;
                case InfoTab.Permissions:
                    InitPermissions();
                    break;
                case InfoTab.Acl:
                    InitAcl();
                    break;
                case InfoTab.Distribution:
                    InitDistribution();
                    break;
                case InfoTab.S3:
                    InitS3();
                    break;
                case InfoTab.Metadata:
                    InitMetadata();
                    break;
                case InfoTab.Versions:
                    InitVersions();
                    break;
            }
        }

        private void InitVersions()
        {
            if (View.Versions.ViewModel is not VersionsViewModel viewModel)
            {
                viewModel = new VersionsViewModel(_controller, _controller.Session);
                View.Versions.ViewModel = viewModel;
                viewModel.PromptDelete.RegisterHandler(c => c.SetOutput(_controller.DeletePathsPrompt(c.Input)));
                viewModel.Reverted += Versions_Reverted;
            }
            viewModel.Selection = SelectedPath;
            viewModel.Load.ExecuteIfPossible().Subscribe();
        }

        private void Versions_Reverted(IList<Path> files)
        {
            _controller.Reload(_controller.Workdir, files, new List<Path>());
        }

        private void ConfigureToolbar()
        {
            SessionPool session = _controller.Session;
            bool anonymous = session.getHost().getCredentials().isAnonymousLogin();

            View.ToolbarS3Label = session.getHost().getProtocol().getName();
            View.ToolbarS3Image = IconProvider.GetIcon(session.getHost().getProtocol(), 32);
            //ACL or permission view
            View.AclPanel = session.getFeature(typeof(AclPermission)) != null;
            if (anonymous)
            {
                // Anonymous never has the right to update permissions
                View.ToolbarPermissionsEnabled = false;
            }
            else
            {
                View.ToolbarPermissionsEnabled = session.getFeature(typeof(AclPermission)) != null ||
                                                 session.getFeature(typeof(UnixPermission)) != null;
            }
            if (anonymous)
            {
                View.ToolbarDistributionEnabled = false;
                View.ToolbarDistributionImage = ImageHelper.Images.S3.Size(32);
            }
            else
            {
                bool distribution = session.getFeature(typeof(DistributionConfiguration)) != null;
                View.ToolbarDistributionEnabled = distribution;
                if (distribution)
                {
                    View.ToolbarDistributionImage = IconProvider.GetIcon(session.getHost().getProtocol(), 32);
                }
                else
                {
                    View.ToolbarDistributionImage = ImageHelper.Images.S3.Size(32);
                }
            }
            if (anonymous)
            {
                View.ToolbarS3Enabled = false;
            }
            else
            {
                View.ToolbarS3Enabled = session.getHost().getProtocol().getType() == Protocol.Type.s3
                    || session.getHost().getProtocol().getType() == Protocol.Type.b2
                    || session.getHost().getProtocol().getType() == Protocol.Type.azure
                    || session.getHost().getProtocol().getType() == Protocol.Type.googlestorage;
            }
            if (anonymous)
            {
                View.ToolbarVersionsEnabled = false;
            }
            else
            {
                View.ToolbarVersionsEnabled = session.getFeature(typeof(Versioning)) != null;
            }

            if (anonymous)
            {
                // Anonymous never has the right to update permissions
                View.ToolbarMetadataEnabled = false;
            }
            else
            {
                var feature = (Metadata)session.getFeature(typeof(Metadata));
                bool metadataAvailable = feature != null;
                if (metadataAvailable && View.MetadataViewModel is not MetadataViewModel viewModel)
                {
                    View.MetadataViewModel = viewModel = new(feature, _controller, _controller.Session);
                    viewModel.Recurse.RegisterHandler(c =>
                    {
                        DialogRecursiveCallback dialog = new(this);
                        c.SetOutput(dialog.recurse(c.Input.Directory, c.Input.Value));
                    });
                }
                View.ToolbarMetadataEnabled = metadataAvailable;
            }
        }

        /// <summary>
        /// Read custom metadata HTTP headers from cloud provider
        /// </summary>
        private void InitMetadata()
        {
            if(View.MetadataViewModel is MetadataViewModel viewModel)
            {
                viewModel.Paths = Files;
                viewModel.Load.Execute().Subscribe();
            }
        }

        private void AddAclEntry(Acl.User user, Acl.Role role)
        {
            Log.debug("AddAclItem:" + user.getDisplayName());
            UserAndRoleEntry entry = new UserAndRoleEntry(user, role);
            _acl.Add(entry);
            View.EditAclRow(entry, !user.isEditable());
        }

        private void PopulateLifecycleTransitionPeriod()
        {
            if (_lifecycleTransitionPeriods == null)
            {
                _lifecycleTransitionPeriods =
                    Utils.ConvertFromJavaList(PreferencesFactory.get().getList("s3.lifecycle.transition.options"),
                        item =>
                            new KeyValuePair<string, string>(
                                MessageFormat.format(LocaleFactory.localizedString("after {0} Days", "S3"),
                                    item.ToString()), item.ToString()));
            }
            View.PopulateLifecycleTransitionPeriod(_lifecycleTransitionPeriods);
        }

        private void PopulateLifecycleDeletePeriod()
        {
            if (_lifecycleDeletePeriods == null)
            {
                _lifecycleDeletePeriods =
                    Utils.ConvertFromJavaList(PreferencesFactory.get().getList("s3.lifecycle.delete.options"),
                        item =>
                            new KeyValuePair<string, string>(
                                MessageFormat.format(LocaleFactory.localizedString("after {0} Days", "S3"),
                                    item.ToString()), item.ToString()));
            }
            View.PopulateLifecycleDeletePeriod(_lifecycleDeletePeriods);
        }

        private void ConfigureHelp()
        {
            View.ShowHelp += delegate (object sender, InfoHelpArgs args)
            {
                BrowserLauncherFactory.get().open(ProviderHelpServiceFactory.get().help());
            };
        }

        /// <summary>
        /// Read grants in the background
        /// </summary>
        private void InitAcl()
        {
            SetAcl(new List<UserAndRoleEntry>());
            if (ToggleAclSettings(false))
            {
                PopulateAclUsers();
                PopulateAclRoles();
                _controller.Background(new ReadAclBackgroundAction(_controller, this));
            }
        }

        private void PopulateAclUsers()
        {
            AclPermission feature = (AclPermission)_controller.Session.getFeature(typeof(AclPermission));
            IDictionary<string, SyncDelegate> mapping = new Dictionary<string, SyncDelegate>();
            List aclUsers = feature.getAvailableAclUsers();
            for (int i = 0; i < aclUsers.size(); i++)
            {
                Acl.User user = (Acl.User)aclUsers.get(i);
                mapping.Add(user.getPlaceholder(), () => AddAclEntry(user, new Acl.Role(String.Empty)));
            }
            mapping.Add(LocaleFactory.localizedString("Remove"), RemoveAcl);
            View.PopulateAclUsers(mapping);
        }

        private void PopulateAclRoles()
        {
            AclPermission feature = (AclPermission)_controller.Session.getFeature(typeof(AclPermission));
            IList<string> roles = Utils.ConvertFromJavaList(
                feature.getAvailableAclRoles(Utils.ConvertToJavaList(Files)), item => ((Acl.Role)item).getName());
            View.PopulateAclRoles(roles);
        }

        private void SetAcl(IList<UserAndRoleEntry> userAndRoleEntries)
        {
            _acl = new BindingList<UserAndRoleEntry>(userAndRoleEntries);
            View.AclDataSource = _acl;
            _acl.ListChanged += delegate (object sender, ListChangedEventArgs args)
            {
                switch (args.ListChangedType)
                {
                    case ListChangedType.ItemDeleted:
                        if (ToggleAclSettings(false))
                        {
                            Background(new WriteAclBackgroundAction(_controller, this));
                        }
                        break;

                    case ListChangedType.ItemChanged:
                        if (Utils.IsNotBlank(_acl[args.NewIndex].getUser().getIdentifier()) &&
                            Utils.IsNotBlank(_acl[args.NewIndex].getRole().getName()))
                        {
                            if (ToggleAclSettings(false))
                            {
                                Background(new WriteAclBackgroundAction(_controller, this));
                            }
                        }
                        break;
                }
            };
        }

        /// <summary>
        /// Toggle settings before and after update
        /// </summary>
        /// <param name="stop">Enable controls and stop progress spinner</param>
        /// <returns>True if progress animation has started and settings are toggled</returns>
        private bool ToggleAclSettings(bool stop)
        {
            SessionPool session = _controller.Session;
            Credentials credentials = session.getHost().getCredentials();
            bool enable = !credentials.isAnonymousLogin() && session.getFeature(typeof(AclPermission)) != null;
            View.AclTableEnabled = stop && enable;
            View.AclAddEnabled = stop && enable;
            bool selection = View.SelectedAclEntries.Count > 0;
            View.AclRemoveEnabled = stop && enable && selection;
            if (stop)
            {
                View.AclAnimationActive = false;
            }
            else if (enable)
            {
                View.AclAnimationActive = true;
            }
            return enable;
        }

        private void CalculateSize()
        {
            if (ToggleSizeSettings(false))
            {
                _controller.background(new RecursiveSizeAction(_controller, this, _files));
            }
        }

        private void FilenameChanged()
        {
            if (NumberOfFiles == 1)
            {
                Path current = _files[0];

                if (!View.Filename.Equals(current.getName()))
                {
                    if (View.Filename.Contains(Path.DELIMITER.ToString()))
                    {
                        SystemSounds.Beep.Play();
                        return;
                    }
                    if (string.IsNullOrEmpty(View.Filename))
                    {
                        View.Filename = current.getName();
                    }
                    else
                    {
                        Path renamed = new Path(current.getParent(), View.Filename, current.getType());
                        _controller.RenamePath(current, renamed);
                        InitWebUrl();
                    }
                }
            }
        }

        private void BucketLoggingCheckboxChanged()
        {
            if (ToggleS3Settings(false))
            {
                _controller.background(new SetBucketLoggingBackgroundAction(_controller, this));
            }
        }

        private void DistributionApply()
        {
            if (ToggleDistributionSettings(false))
            {
                _controller.background(new WriteDistributionBackgroundAction(_controller, this));
            }
        }

        private void DistributionCnameChanged()
        {
            if (ToggleDistributionSettings(false))
            {
                _controller.background(new WriteDistributionBackgroundAction(_controller, this));
            }
        }

        private void DistributionDeliveryMethodChanged()
        {
            if (ToggleDistributionSettings(false))
            {
                _controller.background(new ReadDistributionBackgroundAction(_controller, this));
            }
        }

        /// <summary>
        /// Toggle settings before and after update
        /// </summary>
        /// <param name="stop">Enable controls and stop progress spinner</param>
        /// <returns>True if controls are enabled for the given protocol in idle state</returns>
        private bool ToggleDistributionSettings(bool stop)
        {
            SessionPool session = _controller.Session;
            Credentials credentials = session.getHost().getCredentials();
            DistributionConfiguration cdn =
                (DistributionConfiguration)session.getFeature(typeof(DistributionConfiguration));
            bool enable = !credentials.isAnonymousLogin() && cdn != null;
            Path container = containerService.getContainer(SelectedPath);
            if (enable)
            {
                // Not enabled if multiple files selected with not same parent container
                foreach (Path next in _files)
                {
                    if (containerService.getContainer(next).Equals(container))
                    {
                        continue;
                    }
                    enable = false;
                    break;
                }
            }
            View.DistributionEnabled = stop && enable;
            View.DistributionDeliveryMethodEnabled = stop && enable;
            View.DistributionLoggingCheckboxEnabled = stop && enable &&
                                                      cdn.getFeature(typeof(DistributionLogging),
                                                          View.DistributionDeliveryMethod) != null;
            View.DistributionLoggingPopupEnabled = stop && enable &&
                                                   cdn.getFeature(typeof(DistributionLogging),
                                                       View.DistributionDeliveryMethod) != null;
            View.DistributionCnameEnabled = stop && enable &&
                                            cdn.getFeature(typeof(Cname), View.DistributionDeliveryMethod) != null;
            View.DistributionInvalidateObjectsEnabled = stop && enable &&
                                                        cdn.getFeature(typeof(Purge), View.DistributionDeliveryMethod) !=
                                                        null;
            View.DistributionDefaultRootEnabled = stop && enable &&
                                                  cdn.getFeature(typeof(Index), View.DistributionDeliveryMethod) !=
                                                  null;
            if (stop)
            {
                View.DistributionAnimationActive = false;
            }
            else if (enable)
            {
                View.DistributionAnimationActive = true;
            }
            return enable;
        }

        private void AttachPermissionHandlers()
        {
            View.OwnerReadChanged += OwnerReadChanged;
            View.OwnerWriteChanged += OwnerWriteChanged;
            View.OwnerExecuteChanged += OwnerExecuteChanged;
            View.GroupReadChanged += GroupReadChanged;
            View.GroupWriteChanged += GroupWriteChanged;
            View.GroupExecuteChanged += GroupExecuteChanged;
            View.OtherReadChanged += OtherReadChanged;
            View.OtherWriteChanged += OtherWriteChanged;
            View.OtherExecuteChanged += OtherExecuteChanged;
            View.ApplyRecursivePermissions += ApplyRecursivePermissions;
            View.OctalPermissionsChanged += OctalPermissionsChanged;
        }

        private void RemoveAcl()
        {
            List<UserAndRoleEntry> entries = View.SelectedAclEntries;
            foreach (UserAndRoleEntry entry in entries)
            {
                _acl.Remove(entry);
            }
        }

        private void AttachDistributionHandlers()
        {
            DetachDistributionHandlers();
            View.DistributionDeliveryMethodChanged += DistributionDeliveryMethodChanged;
            View.DistributionCnameChanged += DistributionCnameChanged;
            View.DistributionEnabledChanged += DistributionApply;
            View.DistributionLoggingCheckboxChanged += DistributionApply;
            View.DistributionLoggingPopupChanged += DistributionLoggingPopupChanged;
            View.DistributionDefaultRootChanged += DistributionApply;
            View.DistributionInvalidateObjects += DistributionInvalidateObjects;
        }

        private void DistributionLoggingPopupChanged()
        {
            if (View.DistributionLoggingCheckbox)
            {
                // Only write change if logging is already enabled
                DistributionApply();
            }
        }

        private void DistributionInvalidateObjects()
        {
            if (ToggleDistributionSettings(false))
            {
                _controller.Background(new InvalidateObjectsBackgroundAction(_controller, this));
            }
        }

        private void AttachGeneralHandlers()
        {
            View.CalculateSize += CalculateSize;
            View.FilenameChanged += FilenameChanged;
        }

        private void AttachS3Handlers()
        {
            DetachS3Handlers();
            View.TransferAccelerationCheckboxChanged += TransferAccelerationCheckboxChanged;
            View.BucketLoggingCheckboxChanged += BucketLoggingCheckboxChanged;
            View.BucketLoggingPopupChanged += BucketLoggingPopupChanged;
            View.EncryptionChanged += EncryptionChanged;
            View.StorageClassChanged += StorageClassChanged;
            View.BucketVersioningChanged += BucketVersioningChanged;
            View.BucketMfaChanged += BucketMfaChanged;
            View.LifecycleTransitionCheckboxChanged += LifecycleChanged;
            View.LifecycleTransitionPopupChanged += LifecycleChanged;
            View.LifecycleDeleteCheckboxChanged += LifecycleChanged;
            View.LifecycleDeletePopupChanged += LifecycleChanged;
        }

        private void TransferAccelerationCheckboxChanged()
        {
            if (ToggleS3Settings(false))
            {
                _controller.background(new SetTransferAccelerationBackgroundAction(_controller, this));
            }
        }
        private void BucketLoggingPopupChanged()
        {
            if (View.BucketLoggingCheckbox)
            {
                // Only write change if logging is already enabled
                BucketLoggingCheckboxChanged();
            }
        }

        private void BucketVersioningChanged()
        {
            if (ToggleS3Settings(false))
            {
                _controller.Background(new SetBucketVersioningAndMfaBackgroundAction(_controller, this));
            }
        }

        private void StorageClassChanged()
        {
            if (!"Multiple".Equals(View.StorageClass) && ToggleS3Settings(false))
            {
                _controller.Background(new SetStorageClassBackgroundAction(_controller, this, _files, View.StorageClass));
            }
        }

        private void EncryptionChanged()
        {
            if (!"Multiple".Equals(View.Encryption) && ToggleS3Settings(false))
            {
                Encryption.Algorithm algorithm = Encryption.Algorithm.fromString(View.Encryption);
                _controller.Background(new SetEncryptionBackgroundAction(_controller, this, _files, algorithm));
            }
        }

        private void DetachS3Handlers()
        {
            View.TransferAccelerationCheckboxChanged -= TransferAccelerationCheckboxChanged;
            View.BucketLoggingCheckboxChanged -= BucketLoggingCheckboxChanged;
            View.BucketLoggingPopupChanged -= BucketLoggingPopupChanged;
            View.EncryptionChanged -= EncryptionChanged;
            View.StorageClassChanged -= StorageClassChanged;
            View.BucketVersioningChanged -= BucketVersioningChanged;
            View.BucketMfaChanged -= BucketMfaChanged;
            View.LifecycleTransitionCheckboxChanged -= LifecycleChanged;
            View.LifecycleTransitionPopupChanged -= LifecycleChanged;
            View.LifecycleDeleteCheckboxChanged -= LifecycleChanged;
            View.LifecycleDeletePopupChanged -= LifecycleChanged;
        }

        private void LifecycleChanged()
        {
            if (ToggleS3Settings(false))
            {
                _controller.Background(new LifecycleBackgroundAction(_controller, this));
            }
        }

        private void BucketMfaChanged()
        {
            BucketVersioningChanged();
        }

        private void DetachGeneralHandlers()
        {
            View.CalculateSize -= CalculateSize;
            View.FilenameChanged -= FilenameChanged;
        }

        private void DetachDistributionHandlers()
        {
            View.DistributionDeliveryMethodChanged -= DistributionDeliveryMethodChanged;
            View.DistributionCnameChanged -= DistributionCnameChanged;
            View.DistributionEnabledChanged -= DistributionApply;
            View.DistributionLoggingCheckboxChanged -= DistributionApply;
            View.DistributionLoggingPopupChanged -= DistributionLoggingPopupChanged;
            View.DistributionDefaultRootChanged -= DistributionApply;
            View.DistributionInvalidateObjects -= DistributionInvalidateObjects;
        }
        private void OtherExecuteChanged()
        {
            DetachPermissionHandlers();
            permissions.other.execute = View.OtherExecute == CheckState.Checked ? Boolean.TRUE : Boolean.FALSE;
            PermissionsChanged();
        }

        private void OtherWriteChanged()
        {
            DetachPermissionHandlers();
            permissions.other.write = View.OtherWrite == CheckState.Checked ? Boolean.TRUE : Boolean.FALSE;
            PermissionsChanged();
        }

        private void OtherReadChanged()
        {
            DetachPermissionHandlers();
            permissions.other.read = View.OtherRead == CheckState.Checked ? Boolean.TRUE : Boolean.FALSE;
            PermissionsChanged();
        }

        private void GroupExecuteChanged()
        {
            DetachPermissionHandlers();
            permissions.group.execute = View.GroupExecute == CheckState.Checked ? Boolean.TRUE : Boolean.FALSE;
            PermissionsChanged();
        }

        private void GroupWriteChanged()
        {
            DetachPermissionHandlers();
            permissions.group.write = View.GroupWrite == CheckState.Checked ? Boolean.TRUE : Boolean.FALSE;
            PermissionsChanged();
        }

        private void GroupReadChanged()
        {
            DetachPermissionHandlers();
            permissions.group.read = View.GroupRead == CheckState.Checked ? Boolean.TRUE : Boolean.FALSE;
            PermissionsChanged();
        }

        private void OwnerExecuteChanged()
        {
            DetachPermissionHandlers();
            permissions.user.execute = View.OwnerExecute == CheckState.Checked ? Boolean.TRUE : Boolean.FALSE;
            PermissionsChanged();
        }

        private void OwnerWriteChanged()
        {
            DetachPermissionHandlers();
            permissions.user.write = View.OwnerWrite == CheckState.Checked ? Boolean.TRUE : Boolean.FALSE;
            PermissionsChanged();
        }

        private void OwnerReadChanged()
        {
            DetachPermissionHandlers();
            permissions.user.read = View.OwnerRead == CheckState.Checked ? Boolean.TRUE : Boolean.FALSE;
            PermissionsChanged();
        }

        private void DetachPermissionHandlers()
        {
            View.OwnerReadChanged -= OwnerReadChanged;
            View.OwnerWriteChanged -= OwnerWriteChanged;
            View.OwnerExecuteChanged -= OwnerExecuteChanged;
            View.GroupReadChanged -= GroupReadChanged;
            View.GroupWriteChanged -= GroupWriteChanged;
            View.GroupExecuteChanged -= GroupExecuteChanged;
            View.OtherReadChanged -= OtherReadChanged;
            View.OtherWriteChanged -= OtherWriteChanged;
            View.OtherExecuteChanged -= OtherExecuteChanged;
            View.ApplyRecursivePermissions -= ApplyRecursivePermissions;
            View.OctalPermissionsChanged -= OctalPermissionsChanged;
        }

        private void ApplyRecursivePermissions()
        {
            ChangePermissions(true);
        }

        private void OctalPermissionsChanged()
        {
            permissions.fromOctal(View.OctalPermissions);
            ChangePermissions(true);
        }

        private void PermissionsChanged()
        {
            ChangePermissions(false);
        }

        /// <summary>
        /// Write altered permissions to the server
        /// </summary>
        /// <param name="permission">UNIX permissions to apply to files</param>
        /// <param name="recursive">Recursively apply to child of directories</param>
        private void ChangePermissions(bool recursive)
        {
            if (TogglePermissionSettings(false))
            {
                _controller.background(new WritePermissionBackgroundAction(_controller, this, recursive));
            }
        }

        private void InitGeneral()
        {
            int count = NumberOfFiles;
            if (count > 0)
            {
                DetachGeneralHandlers();

                Path file = _files[0];
                
                View.FilenameEnabled = (1 == count &&
                                        ((Move)_controller.Session.getFeature(typeof(Move))).isSupported(file, file.getParent(), String.Empty));
                string path;
                if (file.isSymbolicLink())
                {
                    path = file.getSymlinkTarget().getAbsolute();
                }
                else
                {
                    path = file.getParent().getAbsolute();
                }
                View.Path = path;
                View.PathToolTip = path;
                View.Group = count > 1 ? _multipleFilesString : file.attributes().getGroup();
                if (count > 1)
                {
                    View.Kind = _multipleFilesString;
                    View.Checksum = _multipleFilesString;
                    View.Modified = _multipleFilesString;
                    View.FileCreated = _multipleFilesString;
                }
                else
                {
                    View.Kind = _descriptor.getKind(file);
                    if (-1 == file.attributes().getModificationDate())
                    {
                        View.Modified = LocaleFactory.localizedString("Unknown");
                    }
                    else
                    {
                        View.Modified =
                            UserDateFormatterFactory.get().getLongFormat(file.attributes().getModificationDate());
                    }
                    if (-1 == file.attributes().getCreationDate())
                    {
                        View.FileCreated = LocaleFactory.localizedString("Unknown");
                    }
                    else
                    {
                        View.FileCreated =
                            UserDateFormatterFactory.get().getLongFormat(file.attributes().getCreationDate());
                    }
                }
                View.FileOwner = count > 1
                    ? _multipleFilesString
                    : Utils.IsBlank(file.attributes().getOwner())
                        ? LocaleFactory.localizedString("Unknown")
                        : file.attributes().getOwner();
                ;

                if (count > 1)
                {
                    View.FileIcon = ImageHelper.Images.Multiple;
                }
                else
                {
                    View.FileIcon = IconProvider.GetPath(_files[0], 32);
                }
            }

            // Sum of files
            InitSize();
            InitChecksum();
            InitPermissions();
            // Read HTTP URL
            InitWebUrl();
        }

        private void InitS3()
        {
            DetachS3Handlers();

            View.BucketLocation = LocaleFactory.localizedString("Unknown");
            View.BucketLoggingTooltip = LocaleFactory.localizedString("Unknown");

            IList<string> none = new List<string> { LocaleFactory.localizedString("None") };
            View.PopulateBucketLogging(none);

            IList<KeyValuePair<string, string>> classes = new List<KeyValuePair<string, string>>();
            classes.Add(new KeyValuePair<string, string>(LocaleFactory.localizedString("Unknown"), "Unknown"));
            View.PopulateStorageClass(classes);
            View.StorageClass = "Unknown";

            IList<KeyValuePair<string, string>> algorithms = new List<KeyValuePair<string, string>>();
            algorithms.Add(new KeyValuePair<string, string>(LocaleFactory.localizedString("Unknown"), "Unknown"));
            View.PopulateEncryption(algorithms);
            View.Encryption = "Unknown";

            PopulateLifecycleTransitionPeriod();
            PopulateLifecycleDeletePeriod();
            if (ToggleS3Settings(false))
            {
                _controller.background(new FetchS3BackgroundAction(_controller, this));
            }
            else
            {
                AttachS3Handlers();
            }
        }

        /// <summary>
        /// Toggle settings before and after update
        /// </summary>
        /// <param name="stop">Enable controls and stop progress spinner</param>
        private bool ToggleS3Settings(bool stop)
        {
            SessionPool session = _controller.Session;
            Credentials credentials = session.getHost().getCredentials();
            bool enable = session.getHost().getProtocol().getType() == Protocol.Type.s3 ||
                          session.getHost().getProtocol().getType() == Protocol.Type.b2 ||
                          session.getHost().getProtocol().getType() == Protocol.Type.azure ||
                          session.getHost().getProtocol().getType() == Protocol.Type.googlestorage;
            if (enable)
            {
                enable = !credentials.isAnonymousLogin();
            }
            bool logging = false;
            bool versioning = false;
            bool storageclass = false;
            bool encryption = false;
            bool lifecycle = false;
            bool acceleration = false;
            if (enable)
            {
                logging = session.getFeature(typeof(Logging)) != null;
                versioning = session.getFeature(typeof(Versioning)) != null;
                lifecycle = session.getFeature(typeof(Lifecycle)) != null;
                encryption = session.getFeature(typeof(Encryption)) != null;
                storageclass = session.getFeature(typeof(Redundancy)) != null;
                acceleration = session.getFeature(typeof(TransferAcceleration)) != null;
            }
            View.BucketVersioningEnabled = stop && enable && versioning;
            View.BucketMfaEnabled = stop && enable && session.getHost().getProtocol().getType() == Protocol.Type.s3 && versioning && View.BucketVersioning;
            View.BucketLoggingCheckboxEnabled = stop && enable && logging;
            View.TransferAccelerationCheckboxEnabled = stop && enable && acceleration;
            View.BucketLoggingPopupEnabled = stop && enable && logging;
            View.StorageClassEnabled = stop && enable && storageclass;
            View.EncryptionEnabled = stop && enable && encryption;

            View.LifecycleDeleteCheckboxEnabled = stop && enable && lifecycle;
            View.LifecycleDeletePopupEnabled = stop && enable && lifecycle;
            View.LifecycleTransitionCheckboxEnabled = stop && enable && lifecycle;
            View.LifecycleTransitionPopupEnabled = stop && enable && lifecycle;
            if (stop)
            {
                View.S3AnimationActive = false;
            }
            else if (enable)
            {
                View.S3AnimationActive = true;
            }
            return enable;
        }

        /// <summary>
        /// Read content distribution settings
        /// </summary>
        private void InitDistribution()
        {
            DetachDistributionHandlers();

            View.DistributionStatus = LocaleFactory.localizedString("Unknown");
            View.DistributionUrl = LocaleFactory.localizedString("Unknown");
            View.DistributionUrlEnabled = false;
            View.DistributionCname = LocaleFactory.localizedString("None");
            View.DistributionCnameUrlEnabled = false;

            // Remember last selection
            Distribution.Method selected = View.DistributionDeliveryMethod;

            IList<KeyValuePair<string, Distribution.Method>> methods =
                new List<KeyValuePair<string, Distribution.Method>>
                {
                    new KeyValuePair<string, Distribution.Method>(LocaleFactory.localizedString("None"), null)
                };
            View.PopulateDistributionDeliveryMethod(methods);
            View.PopulateDefaultRoot(new List<KeyValuePair<string, string>>
            {
                new KeyValuePair<string, string>(LocaleFactory.localizedString("None"), String.Empty)
            });

            SessionPool session = _controller.Session;
            DistributionConfiguration cdn =
                (DistributionConfiguration)session.getFeature(typeof(DistributionConfiguration));
            View.DistributionTitle = String.Format(LocaleFactory.localizedString("Enable {0} Distribution", "Status"),
                cdn.getName());
            methods = new List<KeyValuePair<string, Distribution.Method>>();
            Path container = containerService.getContainer(SelectedPath);
            List list = cdn.getMethods(container);
            for (int i = 0; i < list.size(); i++)
            {
                Distribution.Method method = (Distribution.Method)list.get(i);
                methods.Add(new KeyValuePair<string, Distribution.Method>(method.ToString(), method));
            }
            View.PopulateDistributionDeliveryMethod(methods);
            if (null == selected)
            {
                // Select first distribution option
                View.DistributionDeliveryMethod = (Distribution.Method)cdn.getMethods(container).iterator().next();
            }
            else
            {
                View.DistributionDeliveryMethod = selected;
            }
            IList<string> none = new List<string> { LocaleFactory.localizedString("None") };
            View.PopulateDistributionLogging(none);
            DistributionDeliveryMethodChanged();
        }

        private void InitPermissions()
        {
            DetachPermissionHandlers();

            View.Permissions = LocaleFactory.localizedString("Unknown");
            View.OctalPermissions = LocaleFactory.localizedString("Unknown");
            if (TogglePermissionSettings(false))
            {
                _controller.background(new FetchPermissionsBackgroundAction(_controller, this));
            }
        }

        /// <summary>
        /// Toggle settings before and after update
        /// </summary>
        /// <param name="stop">Enable controls and stop progress spinner</param>
        /// <returns>True if controls are enabled for the given protocol in idle state</returns>
        private bool TogglePermissionSettings(bool stop)
        {
            if (!stop)
            {
                DetachPermissionHandlers();
            }
            SessionPool session = _controller.Session;
            Credentials credentials = session.getHost().getCredentials();
            bool enable = !credentials.isAnonymousLogin() && session.getFeature(typeof(UnixPermission)) != null;
            View.RecursivePermissionsEnabled = stop && enable;
            foreach (Path next in _files)
            {
                if (next.isFile())
                {
                    View.RecursivePermissionsEnabled = false;
                    break;
                }
            }
            View.OctalPermissionsEnabled = stop && enable;
            View.OwnerReadEnabled = stop && enable;
            View.OwnerWriteEnabled = stop && enable;
            View.OwnerExecuteEnabled = stop && enable;
            View.GroupReadEnabled = stop && enable;
            View.GroupWriteEnabled = stop && enable;
            View.GroupExecuteEnabled = stop && enable;
            View.OtherReadEnabled = stop && enable;
            View.OtherWriteEnabled = stop && enable;
            View.OtherExecuteEnabled = stop && enable;

            if (stop)
            {
                View.PermissionAnimationActive = false;
                AttachPermissionHandlers();
            }
            else if (enable)
            {
                View.PermissionAnimationActive = true;
            }
            return enable;
        }

        private void InitWebUrl()
        {
            if (NumberOfFiles > 1)
            {
                View.WebUrl = _multipleFilesString;
                View.WebUrlTooltip = null;
            }
            else
            {
                DescriptiveUrl http =
                    ((UrlProvider)_controller.Session.getFeature(typeof(UrlProvider))).toUrl(SelectedPath)
                        .find(DescriptiveUrl.Type.http);
                if (!http.Equals(DescriptiveUrl.EMPTY))
                {
                    View.WebUrl = http.getUrl();
                    View.WebUrlTooltip = LocaleFactory.localizedString("Open in Web Browser");
                }
                else
                {
                    View.WebUrl = LocaleFactory.localizedString("Unknown");
                }
            }
        }

        private void InitChecksum()
        {
            DetachGeneralHandlers();
            if (NumberOfFiles > 1)
            {
                View.Checksum = _multipleFilesString;
            }
            else
            {
                Path file = SelectedPath;
                if (Checksum.NONE == file.attributes().getChecksum())
                {
                    View.Checksum = Utils.IsBlank(file.attributes().getETag())
                                            ? LocaleFactory.localizedString("Unknown")
                                            : file.attributes().getETag();
                }
                else
                {
                    View.Checksum = file.attributes().getChecksum().hash;
                }
            }
            AttachGeneralHandlers();
        }

        /// <summary>
        /// Updates the size field by iterating over all files and
        /// reading the cached size value in the attributes of the path
        /// </summary>
        private void InitSize()
        {
            DetachGeneralHandlers();
            if (ToggleSizeSettings(false))
            {
                _controller.background(new ReadSizeBackgroundAction(_controller, this));
            }
        }

        /// <summary>
        /// </summary>
        /// <param name="stop">Enable controls and stop progress spinner</param>
        /// <returns>True if progress animation has started and settings are toggled</returns>
        private bool ToggleSizeSettings(bool stop)
        {
            View.SizeButtonEnabled = false;
            foreach (Path aPath in _files)
            {
                if (aPath.isDirectory())
                {
                    View.SizeButtonEnabled = stop;
                    break;
                }
            }
            View.SizeAnimationActive = !stop;
            return true;
        }

        private void UpdateSize(long size)
        {
            View.FileSize = SizeFormatterFactory.get().format(size, true);
        }

        public class CustomHeaderEntry : INotifyPropertyChanged
        {
            private string _name;
            private string _value;

            public CustomHeaderEntry(string name, string value)
            {
                _name = name;
                _value = value;
            }

            public string Name
            {
                get { return _name; }
                set
                {
                    if (Utils.IsNotBlank(value))
                    {
                        _name = value;
                        NotifyPropertyChanged("Name");
                    }
                }
            }

            public string Value
            {
                get { return _value == null ? _multipleFilesString : _value; }
                set
                {
                    _value = value;
                    NotifyPropertyChanged("Value");
                }
            }

            public string ActualValue
            {
                get { return _value; }
            }

            public event PropertyChangedEventHandler PropertyChanged;

            private void NotifyPropertyChanged(String info)
            {
                if (PropertyChanged != null)
                {
                    PropertyChanged(this, new PropertyChangedEventArgs(info));
                }
            }
        }

        public static class Factory
        {
            private static readonly IDictionary<BrowserController, InfoController> Open =
                new Dictionary<BrowserController, InfoController>();

            private static readonly object SyncRoot = new Object();

            public static InfoController Create(BrowserController controller, IList<Path> files)
            {
                if (PreferencesFactory.get().getBoolean("browser.info.inspector"))
                {
                    if (Open.ContainsKey(controller))
                    {
                        lock (SyncRoot)
                        {
                            InfoController ic = Open[controller];
                            ic.Files = files;
                            return ic;
                        }
                    }
                }
                InfoController c = new InfoController(controller, files);
                c.View.ViewClosedEvent += delegate
                {
                    lock (SyncRoot)
                    {
                        Open.Remove(controller);
                    }
                };

                lock (SyncRoot)
                {
                    Open.Add(controller, c);
                }
                return c;
            }

            /// <summary>
            ///
            /// </summary>
            /// <param name="controller"></param>
            /// <returns>Null if the browser does not have an Info window.</returns>
            public static InfoController Get(BrowserController controller)
            {
                InfoController result;
                Open.TryGetValue(controller, out result);
                return result;
            }
        }

        private class FetchPermissionsBackgroundAction : WorkerBackgroundAction
        {
            public FetchPermissionsBackgroundAction(BrowserController browserController, InfoController infoController)
                : base(
                    browserController, browserController.Session,
                    new InnerReadPermissionWorker(infoController, Utils.ConvertToJavaList(infoController._files)))
            {
            }

            private class InnerReadPermissionWorker : ReadPermissionWorker
            {
                private readonly InfoController _infoController;

                public InnerReadPermissionWorker(InfoController infoController, List files) : base(files)
                {
                    _infoController = infoController;
                }

                public override void cleanup(object obj)
                {
                    IInfoView view = _infoController.View;
                    var permission = (PermissionOverwrite)obj;
                    _infoController.permissions = permission;

                    view.OwnerRead = GetCheckState(permission.user.read);
                    view.OwnerWrite = GetCheckState(permission.user.write);
                    view.OwnerExecute = GetCheckState(permission.user.execute);

                    view.GroupRead = GetCheckState(permission.group.read);
                    view.GroupWrite = GetCheckState(permission.group.write);
                    view.GroupExecute = GetCheckState(permission.group.execute);

                    view.OtherRead = GetCheckState(permission.other.read);
                    view.OtherWrite = GetCheckState(permission.other.write);
                    view.OtherExecute = GetCheckState(permission.other.execute);

                    if (_infoController.NumberOfFiles > 1)
                    {
                        view.OctalPermissions = permission.getMode();
                        view.Permissions = permission.toString();
                    }
                    else
                    {
                        view.OctalPermissions = permission.resolve(Permission.EMPTY).getMode();
                        view.Permissions = permission.resolve(Permission.EMPTY).toString();
                    }
                    _infoController.TogglePermissionSettings(true);
                }

                private static CheckState GetCheckState(Boolean state) =>
                    state != null
                        ? state.booleanValue() ? CheckState.Checked : CheckState.Unchecked
                        : CheckState.Indeterminate;

                // if count = 0: unchecked, count = permission count: checked, else: indeterminate
            }
        }

        private class FetchS3BackgroundAction : BrowserControllerBackgroundAction
        {
            private readonly Path _container;

            private readonly HashSet<KeyValuePair<string, string>> _encryptionKeys =
                new HashSet<KeyValuePair<string, string>>();

            private readonly InfoController _infoController;
            private readonly Path _selected;

            private readonly HashSet<KeyValuePair<string, string>> _storageClasses =
                new HashSet<KeyValuePair<string, string>>();

            private readonly IInfoView _view;

            private bool _acceleration;
            private Credentials _credentials;

            private String _encryption;

            private LifecycleConfiguration _lifecycle;
            private Location.Name _location;
            private LoggingConfiguration _logging;

            private String _storageClass;
            private VersioningConfiguration _versioning;

            public FetchS3BackgroundAction(BrowserController browserController, InfoController infoController)
                : base(browserController)
            {
                _infoController = infoController;
                _view = infoController.View;
                _selected = infoController.SelectedPath;
                _container = _infoController.containerService.getContainer(_selected);
            }

            public override object run(Session session)
            {
                if (session.getFeature(typeof(Location)) != null)
                {
                    _location = ((Location)session.getFeature(typeof(Location))).getLocation(_container);
                }
                if (session.getFeature(typeof(Logging)) != null)
                {
                    _logging = ((Logging)session.getFeature(typeof(Logging))).getConfiguration(_container);
                }
                if (session.getFeature(typeof(Versioning)) != null)
                {
                    _versioning = ((Versioning)session.getFeature(typeof(Versioning))).getConfiguration(_container);
                }
                if (session.getFeature(typeof(Lifecycle)) != null)
                {
                    _lifecycle = ((Lifecycle)session.getFeature(typeof(Lifecycle))).getConfiguration(_container);
                }
                Redundancy redundancyFeature = (Redundancy)session.getFeature(typeof(Redundancy));
                if (redundancyFeature != null)
                {
                    Iterator iter = redundancyFeature.getClasses().iterator();
                    while (iter.hasNext())
                    {
                        string redundancy = (string)iter.next();
                        _storageClasses.Add(
                            new KeyValuePair<string, string>(LocaleFactory.localizedString(redundancy, "S3"), redundancy));
                    }
                    HashSet<String> selectedClasses = new HashSet<string>();
                    foreach (Path file in _infoController.Files)
                    {
                        string storageClass = redundancyFeature.getClass(file);
                        if (storageClass != null)
                        {
                            selectedClasses.Add(storageClass);
                            _storageClass = storageClass;
                        }
                    }
                    if (selectedClasses.Count > 1)
                    {
                        _storageClasses.Add(new KeyValuePair<string, string>(LocaleFactory.localizedString("Multiple"),
                            "Multiple"));
                        _storageClass = "Multiple";
                    }
                }
                Encryption encryptionFeature = (Encryption)session.getFeature(typeof(Encryption));
                if (encryptionFeature != null)
                {
                    HashSet<Encryption.Algorithm> selectedEncryptionKeys = new HashSet<Encryption.Algorithm>();
                    foreach (Path file in _infoController.Files)
                    {
                        Encryption.Algorithm algorithm = encryptionFeature.getEncryption(file);
                        selectedEncryptionKeys.Add(algorithm);
                        _encryptionKeys.Add(
                            new KeyValuePair<string, string>(
                                LocaleFactory.localizedString(algorithm.getDescription(), "S3"), algorithm.ToString()));
                        _encryption = algorithm.ToString();
                    }
                    // Add additional keys stored in KMS
                    Set keys = encryptionFeature.getKeys(_container, _infoController._prompt);
                    Iterator iterator = keys.iterator();
                    while (iterator.hasNext())
                    {
                        Encryption.Algorithm algorithm = (Encryption.Algorithm)iterator.next();
                        _encryptionKeys.Add(
                            new KeyValuePair<string, string>(
                                LocaleFactory.localizedString(algorithm.getDescription(), "S3"), algorithm.ToString()));
                    }
                    if (selectedEncryptionKeys.Count > 1)
                    {
                        _encryptionKeys.Add(new KeyValuePair<string, string>(LocaleFactory.localizedString("Multiple"),
                            "Multiple"));
                        _encryption = "Multiple";
                    }
                }
                TransferAcceleration accelerationFeature =
                    (TransferAcceleration)session.getFeature(typeof(TransferAcceleration));
                if (accelerationFeature != null)
                {
                    try
                    {
                        _acceleration = accelerationFeature.getStatus(_container);
                    }
                    catch (InteroperabilityException ex)
                    {
                        Log.warn("Ignore failure reading transfer acceleration", ex);
                    }
                }
                return true;
            }

            public override void cleanup()
            {
                base.cleanup();
                try
                {
                    if (_logging != null)
                    {
                        _view.BucketLoggingCheckbox = _logging.isEnabled();
                        if (_logging.getContainers().size() > 0)
                        {
                            _view.PopulateBucketLogging(Utils.ConvertFromJavaList<String>(_logging.getContainers(),
                                o => ((Path)o).getName()));
                        }
                        if (_logging.isEnabled())
                        {
                            _view.BucketLoggingPopup = _logging.getLoggingTarget();
                        }
                        else
                        {
                            // Default to write log files to origin bucket
                            _view.BucketLoggingPopup = _selected.getName();
                        }
                    }
                    if (_location != null)
                    {
                        _view.BucketLocation = LocaleFactory.localizedString(_location.toString(), "S3");
                    }
                    if (_versioning != null)
                    {
                        _view.BucketVersioning = _versioning.isEnabled();
                        _view.BucketMfa = _versioning.isMultifactor();
                    }
                    if (_encryption != null)
                    {
                        _view.PopulateEncryption(_encryptionKeys.ToList());
                        _view.Encryption = _encryption;
                    }
                    if (_storageClass != null)
                    {
                        _view.PopulateStorageClass(_storageClasses.ToList());
                        _view.StorageClass = _storageClass;
                    }
                    if (_lifecycle != null)
                    {
                        _view.LifecycleDeleteCheckbox = null != _lifecycle.getExpiration();
                        if (_lifecycle.getExpiration() != null)
                        {
                            _view.LifecycleDelete = _lifecycle.getExpiration().toString();
                            if (null == _view.LifecycleDelete)
                            {
                                _infoController._lifecycleDeletePeriods.Add(
                                    new KeyValuePair<string, string>(
                                        MessageFormat.format(LocaleFactory.localizedString("after {0} Days", "S3"),
                                            _lifecycle.getExpiration().toString()),
                                        _lifecycle.getExpiration().toString()));
                                _infoController.PopulateLifecycleDeletePeriod();
                                _view.LifecycleDelete = _lifecycle.getExpiration().toString();
                            }
                        }
                        _view.LifecycleTransitionCheckbox = null != _lifecycle.getTransition();
                        if (_lifecycle.getTransition() != null)
                        {
                            _view.LifecycleTransition = _lifecycle.getTransition().toString();
                            if (null == _view.LifecycleTransition)
                            {
                                _infoController._lifecycleTransitionPeriods.Add(
                                    new KeyValuePair<string, string>(
                                        MessageFormat.format(LocaleFactory.localizedString("after {0} Days", "S3"),
                                            _lifecycle.getTransition().toString()),
                                        _lifecycle.getTransition().toString()));
                                _infoController.PopulateLifecycleTransitionPeriod();
                                _view.LifecycleTransition = _lifecycle.getTransition().toString();
                            }
                        }
                    }
                    _view.TransferAccelerationCheckbox = _acceleration;
                }
                finally
                {
                    _infoController.ToggleS3Settings(true);
                    _infoController.AttachS3Handlers();
                }
            }

            public override string getActivity()
            {
                return MessageFormat.format(LocaleFactory.localizedString("Reading metadata of {0}", "Status"),
                    toString(Utils.ConvertToJavaList(_infoController.Files)));
            }
        }

        private class InvalidateObjectsBackgroundAction : WorkerBackgroundAction
        {
            public InvalidateObjectsBackgroundAction(BrowserController browserController, InfoController infoController)
                : base(
                    browserController, browserController.Session,
                    new InnerDistributionPurgeWorker(infoController, Utils.ConvertToJavaList(infoController.Files),
                        infoController._prompt, infoController.View.DistributionDeliveryMethod))
            {
            }

            private class InnerDistributionPurgeWorker : DistributionPurgeWorker
            {
                private readonly InfoController _infoController;

                public InnerDistributionPurgeWorker(InfoController infoController, List files, LoginCallback prompt,
                    Distribution.Method method) : base(files, prompt, method)
                {
                    _infoController = infoController;
                }

                public override void cleanup(object result)
                {
                    // Refresh the current distribution status
                    _infoController.DistributionDeliveryMethodChanged();
                }
            }
        }

        private class LifecycleBackgroundAction : WorkerBackgroundAction
        {
            public LifecycleBackgroundAction(BrowserController browserController, InfoController infoController)
                : base(
                    browserController, browserController.Session,
                    new InnerWriteLifecycleWorker(infoController, Utils.ConvertToJavaList(infoController.Files),
                        new LifecycleConfiguration(
                            infoController.View.LifecycleTransitionCheckbox
                                ? Integer.valueOf(infoController.View.LifecycleTransition)
                                : null,
                            infoController.View.LifecycleDeleteCheckbox
                                ? Integer.valueOf(infoController.View.LifecycleDelete)
                                : null)))

            {
            }

            private class InnerWriteLifecycleWorker : WriteLifecycleWorker
            {
                private readonly InfoController _infoController;

                public InnerWriteLifecycleWorker(InfoController infoController, List files,
                    LifecycleConfiguration configuration) : base(files, configuration)
                {
                    _infoController = infoController;
                }

                public override void cleanup(object result)
                {
                    _infoController.ToggleS3Settings(true);
                    _infoController.InitS3();
                }
            }
        }

        private class ReadAclBackgroundAction : WorkerBackgroundAction
        {
            public ReadAclBackgroundAction(BrowserController browserController, InfoController infoController)
                : base(
                    browserController, browserController.Session,
                    new InnerReadAclWorker(browserController, infoController,
                        Utils.ConvertToJavaList(infoController._files)))
            {
            }

            private class InnerReadAclWorker : ReadAclWorker
            {
                private readonly InfoController _infoController;

                public InnerReadAclWorker(BrowserController browserController, InfoController infoController, List files)
                    : base(files)
                {
                    _infoController = infoController;
                }

                public override void cleanup(object obj)
                {
                    IList<UserAndRoleEntry> entries = Utils.ConvertFromJavaList((List)obj, delegate (object item)
                    {
                        Acl.UserAndRole entry = (Acl.UserAndRole)item;
                        return new UserAndRoleEntry(entry.getUser(), entry.getRole());
                    });
                    _infoController.SetAcl(entries);
                    _infoController.ToggleAclSettings(true);
                }
            }
        }

        private class ReadDistributionBackgroundAction : WorkerBackgroundAction
        {
            public ReadDistributionBackgroundAction(BrowserController browserController, InfoController infoController)
                : base(
                    browserController, browserController.Session,
                    new InnerReadDistributionWorker(infoController, browserController,
                        Utils.ConvertToJavaList(infoController.Files), infoController._prompt,
                        infoController.View.DistributionDeliveryMethod))
            {
            }

            private class InnerReadDistributionWorker : ReadDistributionWorker
            {
                private readonly BrowserController _controller;
                private readonly InfoController _infoController;

                public InnerReadDistributionWorker(InfoController infoController, BrowserController controller,
                    List files, LoginCallback prompt, Distribution.Method method) : base(files, prompt, method)
                {
                    _infoController = infoController;
                    _controller = controller;
                }

                public override void cleanup(Object obj)
                {
                    Distribution distribution = (Distribution)obj;
                    IInfoView view = _infoController.View;
                    try
                    {
                        _infoController.DetachDistributionHandlers();
                        Path container = _infoController.containerService.getContainer(_infoController.SelectedPath);
                        view.DistributionTitle =
                            String.Format(LocaleFactory.localizedString("Enable {0} Distribution", "Status"),
                                distribution.getName());
                        //Path file = _infoController.SelectedPath;
                        view.Distribution = distribution.isEnabled();
                        view.DistributionStatus = distribution.getStatus();
                        view.DistributionLoggingCheckboxEnabled = distribution.isEnabled();
                        view.DistributionLoggingCheckbox = distribution.isLogging();


                        List containers = distribution.getContainers();
                        IList<string> buckets = new List<string>();
                        bool containerForSelectionAvailable = false;
                        for (Iterator iter = containers.iterator(); iter.hasNext();)
                        {
                            Path c = (Path)iter.next();
                            buckets.Add(c.getName());
                            if (!containerForSelectionAvailable && c.Equals(c.getName()))
                            {
                                containerForSelectionAvailable = true;
                            }
                        }
                        view.PopulateDistributionLogging(buckets);
                        if (Utils.IsNotBlank(distribution.getLoggingContainer()))
                        {
                            // Select configured logging container if any
                            view.DistributionLoggingPopup = distribution.getLoggingContainer();
                        }
                        else
                        {
                            if (containerForSelectionAvailable)
                            {
                                view.DistributionLoggingPopup = container.getName();
                            }
                        }
                        if (null == view.DistributionLoggingPopup)
                        {
                            view.DistributionLoggingPopup = LocaleFactory.localizedString("None");
                        }
                        if (distribution.getOrigin() != null)
                        {
                            view.DistributionOrigin = distribution.getOrigin().toString();
                        }
                        // Concatenate URLs
                        if (_infoController.NumberOfFiles > 1)
                        {
                            view.DistributionUrl = _multipleFilesString;
                            view.DistributionUrlTooltip = null;
                            view.DistributionCnameUrl = _multipleFilesString;
                        }
                        else
                        {
                            if (distribution.getUrl() != null)
                            {
                                view.DistributionUrl = distribution.getUrl().toString();
                                view.DistributionUrlEnabled = true;
                                view.DistributionUrlTooltip = LocaleFactory.localizedString("CDN URL");
                            }
                            else
                            {
                                view.DistributionUrl = LocaleFactory.localizedString("None");
                                view.DistributionUrlTooltip = null;
                            }
                        }
                        string[] cnames = distribution.getCNAMEs();
                        if (0 == cnames.Length)
                        {
                            view.DistributionCname = string.Empty;
                            view.DistributionCnameUrl = string.Empty;
                            view.DistributionCnameUrlTooltip = null;
                        }
                        else
                        {
                            view.DistributionCname = string.Join(" ", cnames);
                            DescriptiveUrl url = new DistributionUrlProvider(distribution).toUrl(_infoController.SelectedPath).find(DescriptiveUrl.Type.cname);
                            if (!url.equals(DescriptiveUrl.EMPTY))
                            {
                                // We only support one CNAME URL to be displayed
                                view.DistributionCnameUrl = url.getUrl();
                                view.DistributionCnameUrlEnabled = true;
                                view.DistributionCnameUrlTooltip = LocaleFactory.localizedString("CDN URL");
                            }
                        }
                        KeyValuePair<string, string> noneEntry =
                            new KeyValuePair<string, string>(LocaleFactory.localizedString("None"), String.Empty);

                        List<KeyValuePair<string, string>> defaultRoots = new List<KeyValuePair<string, string>>
                        {
                            noneEntry
                        };
                        foreach (Path next in Utils.ConvertFromJavaList<Path>(distribution.getRootDocuments()))
                        {
                            if (next.isFile())
                            {
                                defaultRoots.Add(new KeyValuePair<string, string>(next.getName(), next.getName()));
                            }
                        }
                        view.PopulateDefaultRoot(defaultRoots);
                        String defaultRoot = distribution.getIndexDocument();
                        if (Utils.IsNotBlank(defaultRoot))
                        {
                            view.DistributionDefaultRoot = defaultRoot;
                        }
                        else
                        {
                            view.DistributionDefaultRoot = LocaleFactory.localizedString("None");
                        }
                        StringBuilder tooltip = new StringBuilder();
                        int i = 0;
                        foreach (Path f in _infoController._files)
                        {
                            if (i > 0) tooltip.Append(Environment.NewLine);
                            tooltip.Append(f.getAbsolute());
                            i++;
                        }

                        _infoController.View.DistributionInvalidateObjectsTooltip = tooltip.ToString();
                        _infoController.View.DistributionInvalidationStatus = distribution.getInvalidationStatus();
                        _infoController.ToggleDistributionSettings(true);
                    }
                    finally
                    {
                        _infoController.AttachDistributionHandlers();
                    }
                }
            }
        }

        private class ReadSizeBackgroundAction : WorkerBackgroundAction
        {
            public ReadSizeBackgroundAction(BrowserController browserController, InfoController infoController)
                : base(
                    browserController, browserController.Session,
                    new InnerReadSizeWorker(infoController, Utils.ConvertToJavaList(infoController._files)))
            {
            }

            private class InnerReadSizeWorker : ReadSizeWorker
            {
                private readonly InfoController _infoController;

                public InnerReadSizeWorker(InfoController infoController, List files) : base(files)
                {
                    _infoController = infoController;
                }

                public override void cleanup(object obj)
                {
                    long size = ((Long)obj).longValue();
                    _infoController.UpdateSize(size);
                    _infoController.ToggleSizeSettings(true);
                    _infoController.AttachGeneralHandlers();
                }
            }
        }

        private class RecursiveSizeAction : WorkerBackgroundAction
        {
            public RecursiveSizeAction(BrowserController controller, InfoController infoController, IList<Path> files)
                : base(
                    controller, controller.Session,
                    new InnerCalculateSizeWorker(infoController, Utils.ConvertToJavaList(files)))
            {
            }

            private class InnerCalculateSizeWorker : CalculateSizeWorker
            {
                private readonly InfoController _infoController;

                public InnerCalculateSizeWorker(InfoController infoController, List files)
                    : base(files, infoController._controller)
                {
                    _infoController = infoController;
                }

                public override void cleanup(object obj)
                {
                    _infoController.InitSize();
                    _infoController.ToggleSizeSettings(true);
                }

                protected override void update(long l)
                {
                    _infoController.Invoke(() => _infoController.UpdateSize(l));
                }
            }
        }

        private class SetTransferAccelerationBackgroundAction : WorkerBackgroundAction
        {
            public SetTransferAccelerationBackgroundAction(BrowserController browserController,
                InfoController infoController)
                : base(
                    browserController, browserController.Session,
                    new InnerWriteTransferAccelerationWorker(infoController,
                        Utils.ConvertToJavaList(infoController.Files), infoController.View.TransferAccelerationCheckbox)
                )
            {
            }

            private class InnerWriteTransferAccelerationWorker : WriteTransferAccelerationWorker
            {
                private readonly InfoController _infoController;

                public InnerWriteTransferAccelerationWorker(InfoController infoController, List files, bool enabled)
                    : base(files, enabled)
                {
                    _infoController = infoController;
                }

                public override void cleanup(object result)
                {
                    _infoController.ToggleS3Settings(true);
                    _infoController.InitS3();
                }
            }
        }
        private class SetBucketLoggingBackgroundAction : WorkerBackgroundAction
        {
            public SetBucketLoggingBackgroundAction(BrowserController browserController, InfoController infoController)
                : base(
                    browserController, browserController.Session,
                    new InnerWriteLoggingWorker(infoController, Utils.ConvertToJavaList(infoController.Files),
                        new LoggingConfiguration(infoController.View.BucketLoggingCheckbox,
                            infoController.View.BucketLoggingPopup)))
            {
            }

            private class InnerWriteLoggingWorker : WriteLoggingWorker
            {
                private readonly InfoController _infoController;

                public InnerWriteLoggingWorker(InfoController infoController, List files,
                    LoggingConfiguration configuration) : base(files, configuration)
                {
                    _infoController = infoController;
                }

                public override void cleanup(object result)
                {
                    _infoController.ToggleS3Settings(true);
                    _infoController.InitS3();
                }
            }
        }

        private class SetBucketVersioningAndMfaBackgroundAction : WorkerBackgroundAction
        {
            public SetBucketVersioningAndMfaBackgroundAction(BrowserController browserController,
                InfoController infoController)
                : base(
                    browserController, browserController.Session,
                    new InnerWriteVersioningWorker(infoController, Utils.ConvertToJavaList(infoController.Files),
                        infoController._prompt,
                        new VersioningConfiguration(infoController.View.BucketVersioning, infoController.View.BucketMfa))
                )
            {
            }

            private class InnerWriteVersioningWorker : WriteVersioningWorker
            {
                private readonly InfoController _infoController;

                public InnerWriteVersioningWorker(InfoController infoController, List files, LoginCallback prompt,
                    VersioningConfiguration configuration) : base(files, prompt, configuration)
                {
                    _infoController = infoController;
                }

                public override void cleanup(object result)
                {
                    _infoController.ToggleS3Settings(true);
                    _infoController.InitS3();
                }
            }
        }
        private class SetEncryptionBackgroundAction : WorkerBackgroundAction
        {
            public SetEncryptionBackgroundAction(BrowserController controller, InfoController infoController,
                IList<Path> files, Encryption.Algorithm algorithm)
                : base(
                    controller, controller.Session,
                    new InnerWriteEncryptionWorker(controller, infoController, Utils.ConvertToJavaList(files), algorithm)
                )
            {
            }

            private class InnerWriteEncryptionWorker : WriteEncryptionWorker
            {
                private readonly InfoController _infoController;

                public InnerWriteEncryptionWorker(BrowserController controller, InfoController infoController,
                    List files, Encryption.Algorithm algorithm)
                    : base(files, algorithm, new DialogRecursiveCallback(infoController), controller)
                {
                    _infoController = infoController;
                }

                public override void cleanup(object obj)
                {
                    _infoController.ToggleS3Settings(true);
                    _infoController.InitS3();
                }
            }
        }

        private class SetStorageClassBackgroundAction : WorkerBackgroundAction
        {
            public SetStorageClassBackgroundAction(BrowserController controller, InfoController infoController,
                IList<Path> files, String redundancy)
                : base(
                    controller, controller.Session,
                    new InnerWriteRedundancyWorker(controller, infoController, Utils.ConvertToJavaList(files),
                        redundancy))
            {
            }

            private class InnerWriteRedundancyWorker : WriteRedundancyWorker
            {
                private readonly InfoController _infoController;

                public InnerWriteRedundancyWorker(BrowserController controller, InfoController infoController,
                    List files, String redundancy)
                    : base(files, redundancy, new DialogRecursiveCallback(infoController), controller)
                {
                    _infoController = infoController;
                }

                public override void cleanup(object obj)
                {
                    _infoController.ToggleS3Settings(true);
                    _infoController.InitS3();
                }
            }
        }

        public class UserAndRoleEntry : Acl.UserAndRole, INotifyPropertyChanged
        {
            public UserAndRoleEntry(Acl.User user, Acl.Role role) : base(user, role)
            {
            }

            public string User
            {
                get { return getUser().getDisplayName(); }
                set
                {
                    getUser().setIdentifier(value ?? string.Empty);
                    NotifyPropertyChanged("User");
                }
            }

            public string Role
            {
                get { return getRole().getName(); }
                set
                {
                    getRole().setName(value);
                    NotifyPropertyChanged("Role");
                }
            }

            public event PropertyChangedEventHandler PropertyChanged;

            private void NotifyPropertyChanged(String info)
            {
                if (PropertyChanged != null)
                {
                    PropertyChanged(this, new PropertyChangedEventArgs(info));
                }
            }
        }

        private class WriteAclBackgroundAction : WorkerBackgroundAction
        {
            public WriteAclBackgroundAction(BrowserController browserController, InfoController infoController)
                : base(
                    browserController, browserController.Session,
                    new InnerWriteAclWorker(browserController, infoController,
                        Utils.ConvertToJavaList(infoController._files), GetAcl(infoController)))
            {
            }

            private static Acl GetAcl(InfoController infoController)
            {
                Acl.UserAndRole[] aclEntries = new Acl.UserAndRole[infoController._acl.Count];
                for (int index = 0; index < infoController._acl.Count; index++)
                {
                    aclEntries[index] = infoController._acl[index];
                }
                Acl acl = new Acl();
                acl.addAll(aclEntries);
                return acl;
            }

            private class InnerWriteAclWorker : WriteAclWorker
            {
                private readonly InfoController _infoController;

                public InnerWriteAclWorker(BrowserController controller, InfoController infoController, List files,
                    Acl acl) : base(files, acl, new DialogRecursiveCallback(infoController), controller)
                {
                    _infoController = infoController;
                }

                public override void cleanup(object obj)
                {
                    _infoController.ToggleAclSettings(true);
                    _infoController.InitAcl();
                }
            }
        }

        private class WriteDistributionBackgroundAction : WorkerBackgroundAction
        {
            public WriteDistributionBackgroundAction(BrowserController browserController, InfoController infoController)
                : base(
                    browserController, browserController.Session,
                    new InnerWriteDistributionWorker(infoController, Utils.ConvertToJavaList(infoController.Files),
                        infoController._prompt, GetDistribution(infoController.View)))
            {
            }

            private static Distribution GetDistribution(IInfoView view)
            {
                Distribution configuration = new Distribution(view.DistributionDeliveryMethod, view.Distribution);
                configuration.setIndexDocument(Utils.IsBlank(view.DistributionDefaultRoot)
                    ? null
                    : view.DistributionDefaultRoot);
                configuration.setLogging(view.DistributionLoggingCheckbox);
                configuration.setLoggingContainer(view.DistributionLoggingPopup);
                configuration.setCNAMEs(StringUtils.split(view.DistributionCname));
                return configuration;
            }

            private class InnerWriteDistributionWorker : WriteDistributionWorker
            {
                private readonly InfoController _infoController;

                public InnerWriteDistributionWorker(InfoController infoController, List files, LoginCallback prompt,
                    Distribution configuration) : base(files, prompt, configuration)
                {
                    _infoController = infoController;
                }

                public override void cleanup(object result)
                {
                    // Refresh the current distribution status
                    _infoController.DistributionDeliveryMethodChanged();
                }
            }
        }

        private class WritePermissionBackgroundAction : WorkerBackgroundAction
        {
            public WritePermissionBackgroundAction(BrowserController browserController, InfoController infoController,
                bool recursive)
                : base(
                    browserController, browserController.Session,
                    new InnerWritePermissionWorker(infoController, Utils.ConvertToJavaList(infoController._files),
                        recursive
                            ? (Worker.RecursiveCallback)new DialogRecursiveCallback(infoController)
                            : new BooleanRecursiveCallback(false)))
            {
            }

            private class InnerWritePermissionWorker : WritePermissionWorker
            {
                private readonly InfoController _infoController;

                public InnerWritePermissionWorker(InfoController infoController, List files, RecursiveCallback callback)
                    : base(files, infoController.permissions, callback, infoController._controller)
                {
                    _infoController = infoController;
                }

                public override void cleanup(object obj)
                {
                    _infoController.TogglePermissionSettings(true);
                    _infoController.InitPermissions();
                }
            }
        }
    }
}
