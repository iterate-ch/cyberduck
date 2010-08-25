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
using System.Globalization;
using System.Media;
using System.Windows.Forms;
using ch.cyberduck.core;
using Ch.Cyberduck.Core;
using ch.cyberduck.core.cloud;
using ch.cyberduck.core.s3;
using ch.cyberduck.ui;
using ch.cyberduck.ui.action;
using ch.cyberduck.ui.controller;
using ch.cyberduck.ui.controller.threading;
using Ch.Cyberduck.Ui.Controller.Threading;
using java.lang;
using java.util;
using org.apache.log4j;
using StructureMap;
using Locale = ch.cyberduck.core.i18n.Locale;
using Object = System.Object;
using String = System.String;
using StringBuilder = System.Text.StringBuilder;

namespace Ch.Cyberduck.Ui.Controller
{
    public sealed class InfoController : WindowController<IInfoView>
    {
        private static readonly Logger Log = Logger.getLogger(typeof (InfoController).Name);

        private readonly BrowserController _controller;
        private readonly string _multipleFilesString = "(" + Locale.localizedString("Multiple files") + ")";
        private readonly IList<string> _roots = new List<string>();
        private readonly string _unknownString = Locale.localizedString("Unknown");
        private BindingList<UserAndRoleEntry> _acl = new BindingList<UserAndRoleEntry>();
        private List<Path> _files;
        private BindingList<CustomHeader> _metadata = new BindingList<CustomHeader>();

        private InfoController(BrowserController controller, List<Path> files)
        {
            View = ObjectFactory.GetInstance<IInfoView>();
            _controller = controller;

            _controller.View.ViewClosedEvent += delegate
                                                    {
                                                        if (!View.IsDisposed)
                                                        {
                                                            View.Close();
                                                        }
                                                    };

            ConfigureToolbar();
            ConfigureHelp();

            PopulateAclUsers();
            PopulateAclRoles();
            PopulateMetadata();
            
            Files = files;
        }

        private Map ConvertMetadataToMap()
        {
            TreeMap map = new TreeMap();
            foreach (CustomHeader header in _metadata)
            {
                map.Add(header.Name, header.Value);
            }
            return map;
        }

        private void ConfigureToolbar()
        {
            Session session = _controller.getSession();
            bool anonymous = session.getHost().getCredentials().isAnonymousLogin();

            View.ToolbarDistributionImage = IconCache.Instance.GetProtocolImages(32).Images[
                session.getHost().getProtocol().getIdentifier()];

            if (session is S3Session)
            {
                // Set icon of cloud service provider
                View.ToolbarS3Label = session.getHost().getProtocol().getName();
                View.ToolbarS3Image = IconCache.Instance.GetProtocolImages(32).Images[
                session.getHost().getProtocol().getIdentifier()];
            } else
            {
                // Currently these settings are only available for Amazon S3
                View.ToolbarS3Label = Protocol.S3.getName();
                View.ToolbarS3Image = IconCache.Instance.GetProtocolImages(32).Images[Protocol.S3.disk()];
                View.ToolbarDistributionImage = IconCache.Instance.GetProtocolImages(32).Images[Protocol.S3.disk()];
            }

            // Anonymous never has the right to updated permissions
            View.ToolbarPermissionsEnabled = anonymous
                                                 ? false
                                                 : session.isAclSupported() || session.isUnixPermissionsSupported();
            View.ToolbarDistributionEnabled = anonymous || !(session is CloudSession)
                                                  ? false
                                                  : ((CloudSession) session).getSupportedDistributionMethods().size() > 0;
            View.ToolbarS3Enabled = session is S3Session ? !anonymous : false;
            View.ToolbarMetadataEnabled = session is CloudSession ? !anonymous : false;
        }

        public override bool Singleton
        {
            get { return Preferences.instance().getBoolean("browser.info.isInspector"); }
        }

        public List<Path> Files
        {
            private get { return _files; }
            set
            {
                if (value.Count == 0)
                {
                    return;
                }

                _files = value;

                InitGeneral();
                // Sum of files
                InitSize();
                InitChecksum();
                // Read HTTP URL
                InitWebUrl();
                // Read Permissions
                InitPermissions();
                InitAcl();
                // S3 Bucket attributes
                InitS3();
                // HTTP custom headers
                InitMetadata();
                InitDistribution();
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

        /// <summary>
        /// Read custom metadata HTTP headers from cloud provider
        /// </summary>
        private void InitMetadata()
        {
            SetMetadata(new List<InfoController.CustomHeader>());
            if (ToggleMetadataSettings(false))
            {
                _controller.Background(new ReadMetadataBackgroundAction(_controller, this));
            }
        }

        /// <summary>
        /// Toggle settings before and after update
        /// </summary>
        /// <param name="stop">Enable controls and stop progress spinner</param>
        /// <returns>True if progress animation has started and settings are toggled</returns>
        private bool ToggleMetadataSettings(bool stop)
        {
            bool enable = NumberOfFiles > 0;
            if (enable)
            {
                foreach (Path file in _files)
                {
                    Credentials credentials = file.getHost().getCredentials();
                    enable = enable && !credentials.isAnonymousLogin();
                    enable = enable && file is CloudPath;
                    enable = enable && file.attributes().isFile();
                }
            }
            View.MetadataTableEnabled = stop && enable;
            View.MetadataAddEnabled = stop && enable;
            bool selection = View.SelectedMetadataEntries.Count > 0;
            View.MetadataRemoveEnabled = stop && enable && selection;
            if (stop)
            {
                View.MetadataAnimationActive = false;
            }
            else if (enable)
            {
                View.MetadataAnimationActive = true;
            }
            return enable;
        }

        public class CustomHeader
        {
            private string _name;
            private string _value;

            public CustomHeader(string name, string value)
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
                    }
                }
            }

            public string Value
            {
                get { return _value; }
                set
                {
                    if (Utils.IsNotBlank(value))
                    {
                        _value = value;
                    }
                }
            }
        }

        private void SetMetadata(IList<CustomHeader> metadata)
        {
            _metadata = new BindingList<CustomHeader>(metadata);
            View.MetadataDataSource = _metadata;

            _metadata.ListChanged += delegate(object sender, ListChangedEventArgs args)
                                         {
                                             switch (args.ListChangedType)
                                             {
                                                 case ListChangedType.ItemDeleted:
                                                     if (ToggleMetadataSettings(false))
                                                     {
                                                         Background(new WriteMetadataBackgroundAction(_controller, this));
                                                     }
                                                     break;

                                                 case ListChangedType.ItemChanged:
                                                     if (
                                                         Utils.IsNotBlank(
                                                             _metadata[args.NewIndex].Name) &&
                                                         Utils.IsNotBlank(_metadata[args.NewIndex].Value))
                                                     {
                                                         if (ToggleMetadataSettings(false))
                                                         {
                                                             Background(new WriteMetadataBackgroundAction(_controller,
                                                                                                          this));
                                                         }
                                                     }
                                                     break;
                                             }
                                         };            
        }

        /// <summary>
        /// Add new metadata row and selects the name column
        /// </summary>
        private void AddMetadataItem(string name)
        {
            AddMetadataItem(name, "", false);
        }

        /// <summary>
        /// Add new metadata row and selects the name column
        /// </summary>
        private void AddMetadataItem(string name, string value)
        {
            AddMetadataItem(name, value, true);
        }

        /// <summary>
        /// 
        /// </summary>
        /// <param name="name">HTTP header name</param>
        /// <param name="value">HTTP header value</param>
        /// <param name="selectValue">Select the value field or the name header field</param>
        private void AddMetadataItem(string name, string value, bool selectValue)
        {
            Log.debug("AddMetadataItem:" + name);
            IList<InfoController.CustomHeader> l = new List<InfoController.CustomHeader>();
            foreach (InfoController.CustomHeader pair in _metadata)
            {
                l.Add(pair);
            }
            l.Add(new CustomHeader(name, value));
            SetMetadata(l);
            View.EditMetadataRow(name, selectValue);
        }

        private void PopulateMetadata()
        {
            IDictionary<string, SyncDelegate> metadata = new Dictionary<string, SyncDelegate>();
            metadata.Add(Locale.localizedString("Custom Header"),
                        () => AddMetadataItem(Locale.localizedString("Unknown")));
            metadata.Add(Locale.localizedString("Cache-Control"),
                        () =>
                        AddMetadataItem("Cache-Control",
                                        "public,max-age=" + Preferences.instance().getInteger("s3.cache.seconds")));
            metadata.Add(Locale.localizedString("Expires"),
                        delegate
                            {
                                DateTimeFormatInfo format = new CultureInfo("en-US").DateTimeFormat;
                                DateTime expires =
                                    DateTime.Now.AddSeconds(Preferences.instance().getInteger("s3.cache.seconds"));
                                AddMetadataItem("Expires", expires.ToString("r", format));
                            });
            metadata.Add(Locale.localizedString("Pragma"),
                        () => AddMetadataItem("Pragma", "", true));
            metadata.Add(Locale.localizedString("Content-Type"),
                        () => AddMetadataItem("Content-Type", "", true));
            metadata.Add(Locale.localizedString("Content-Encoding"),
                        () => AddMetadataItem("Content-Encoding", "", true));

            metadata.Add(Locale.localizedString("Remove"), RemoveMetadata);

            View.PopulateMetadata(metadata);
        }

        private void RemoveMetadata()
        {
            List<InfoController.CustomHeader> entries = View.SelectedMetadataEntries;
            foreach (InfoController.CustomHeader entry in entries)
            {
                _metadata.Remove(entry);
            }
        }

        private void ConfigureHelp()
        {
            View.ShowHelp += delegate(object sender, InfoHelpArgs args)
                                 {
                                     StringBuilder site =
                                         new StringBuilder(Preferences.instance().getProperty("website.help"));
                                     switch (args.Section)
                                     {
                                         case InfoHelpArgs.Context.General:
                                             site.Append("/howto/info");
                                             break;
                                         case InfoHelpArgs.Context.Permissions:
                                             site.Append("/howto/info");
                                             break;
                                         case InfoHelpArgs.Context.Metdadata:
                                             site.Append("/").Append(
                                                 _controller.getSession().getHost().getProtocol().getIdentifier());
                                             break;
                                         case InfoHelpArgs.Context.Cdn:
                                             site.Append("/howto/cdn");
                                             break;
                                         case InfoHelpArgs.Context.S3:
                                             site.Append("/").Append(
                                                 _controller.getSession().getHost().getProtocol().getIdentifier());
                                             break;
                                     }
                                     Utils.StartProcess(site.ToString());
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
                _controller.Background(new FetchAclBackgroundAction(_controller, this));
            }
        }

        private void PopulateAclUsers()
        {
            IDictionary<string, SyncDelegate> mapping = new Dictionary<string, SyncDelegate>();
            List aclUsers = _controller.getSession().getAvailableAclUsers();
            for (int i = 0; i < aclUsers.size(); i++)
            {
                Acl.User user = (Acl.User) aclUsers.get(i);
                mapping.Add(user.getPlaceholder(),
                            () => _acl.Add(new UserAndRoleEntry(user, new Acl.Role(String.Empty))));
            }
            mapping.Add(Locale.localizedString("Remove"), RemoveAcl);
            View.PopulateAclUsers(mapping);
        }

        private void PopulateAclRoles()
        {
            IList<string> roles = Utils.ConvertFromJavaList(_controller.getSession().getAvailableAclRoles(),
                                                            item => ((Acl.Role)
                                                                     item).
                                                                        getName());
            View.PopulateAclRoles(roles);
        }

        private void SetAcl(IList<UserAndRoleEntry> userAndRoleEntries)
        {
            _acl = new BindingList<UserAndRoleEntry>(userAndRoleEntries);
            View.AclDataSource = _acl;
            _acl.ListChanged += delegate(object sender, ListChangedEventArgs args)
                                    {
                                        switch (args.ListChangedType)
                                        {
                                                //case ListChangedType.ItemAdded:
                                            case ListChangedType.ItemDeleted:
                                                if (ToggleAclSettings(false))
                                                {
                                                    Background(new WriteAclBackgroundAction(_controller, this));
                                                }
                                                break;

                                            case ListChangedType.ItemChanged:
                                                if (
                                                    Utils.IsNotBlank(
                                                        _acl[args.NewIndex].getUser().getIdentifier()) &&
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
            //todo detachAclHandlers?

            Session session = _controller.getSession();
            Credentials credentials = session.getHost().getCredentials();
            bool enable = !credentials.isAnonymousLogin() && session.isAclSupported();
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

                if (View.Filename.Equals(current))
                {
                    //todo evtl. auf '/' und '\' prüfen
                    if (View.Filename.Contains(Path.DELIMITER.ToString()))
                    {
                        SystemSounds.Beep.Play();
                        return;
                    }
                    if (string.IsNullOrEmpty(View.Filename))
                    {
                        View.Filename = current.getName();
                        InitWebUrl();
                    }
                    else
                    {
                        Path renamed = PathFactory.createPath(_controller.getSession(),
                                                              current.getParent().getAbsolute(), View.Filename,
                                                              current.attributes().getType());
                        _controller.RenamePath(current, renamed);
                    }
                }
            }
        }

        private void BucketLoggingChanged()
        {
            if (ToggleS3Settings(false)){
                _controller.background(new SetBucketLoggingBackgroundAction(_controller, this));
            }
        }

        private void DistributionApply()
        {
            if (ToggleDistributionSettings(false)){
                _controller.background(new WriteDistributionBackgroundAction(_controller, this, _files));
            }
        }

        private void DistributionCnameChanged()
        {
            //todo
            throw new NotImplementedException();
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
            // Not all cloud providers support different distributions
            Session session = _controller.getSession();
            bool enable = session is CloudSession;
            Credentials credentials = session.getHost().getCredentials();
            enable = enable && !credentials.isAnonymousLogin();
            if (enable)
            {
                enable = ((CloudSession) session).getSupportedDistributionMethods().size() > 0;
            }
            if (enable)
            {
                String container = _files[0].getContainerName();
                // Not enabled if multiple files selected with not same parent container
                foreach (Path next in _files)
                {
                    if (next.getContainerName().Equals(container))
                    {
                        continue;
                    }
                    enable = false;
                    break;
                }
            }
            View.DistributionEnabled = stop && enable;
            View.DistributionLoggingEnabled = stop && enable;
            View.DistributionCnameEnabled = stop && enable;
            View.DistributionDeliveryMethodEnabled = stop && enable;
            View.DistributionDefaultRootEnabled = stop && enable;
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

        //todo doppelt aufgerufen? PermissionFetch, AclFetch
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
            View.DistributionDeliveryMethodChanged += DistributionDeliveryMethodChanged;
            View.DistributionCnameChanged += DistributionCnameChanged;
            View.DistributionEnabledChanged += DistributionApply;
            View.DistributionLoggingChanged += DistributionApply;
            View.DistributionDefaultRootChanged += DistributionApply;
        }

        private void AttachGeneralHandlers()
        {
            View.CalculateSize += CalculateSize;
            View.FilenameChanged += FilenameChanged;
        }

        private void AttachS3Handlers()
        {
            View.BucketLoggingChanged += BucketLoggingChanged;
            View.StorageClassChanged += StorageClassChanged;
            View.BucketVersioningChanged += BucketVersioningChanged;
            View.BucketMfaChanged += BucketMfaChanged;
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
            if (ToggleS3Settings(false))
            {
                _controller.Background(new SetStorageClassBackgroundAction(_controller, this));
            }
        }

        private void DetachS3Handlers()
        {
            View.BucketLoggingChanged -= BucketLoggingChanged;
            View.StorageClassChanged -= StorageClassChanged;
            View.BucketVersioningChanged -= BucketVersioningChanged;
            View.BucketMfaChanged -= BucketMfaChanged;
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
            View.DistributionLoggingChanged -= DistributionApply;
            View.DistributionDefaultRootChanged -= DistributionApply;
        }

        private void OtherExecuteChanged()
        {
            DetachPermissionHandlers();
            if (View.OtherExecute == CheckState.Indeterminate)
            {
                View.OtherExecute = CheckState.Unchecked;
            }
            PermissionsChanged();
        }

        private void OtherWriteChanged()
        {
            DetachPermissionHandlers();
            if (View.OtherWrite == CheckState.Indeterminate)
            {
                View.OtherWrite = CheckState.Unchecked;
            }
            PermissionsChanged();
        }

        private void OtherReadChanged()
        {
            DetachPermissionHandlers();
            if (View.OtherRead == CheckState.Indeterminate)
            {
                View.OtherRead = CheckState.Unchecked;
            }
            PermissionsChanged();
        }

        private void GroupExecuteChanged()
        {
            DetachPermissionHandlers();
            if (View.GroupExecute == CheckState.Indeterminate)
            {
                View.GroupExecute = CheckState.Unchecked;
            }
            PermissionsChanged();
        }

        private void GroupWriteChanged()
        {
            DetachPermissionHandlers();
            if (View.GroupWrite == CheckState.Indeterminate)
            {
                View.GroupWrite = CheckState.Unchecked;
            }
            PermissionsChanged();
        }

        private void GroupReadChanged()
        {
            DetachPermissionHandlers();
            if (View.GroupRead == CheckState.Indeterminate)
            {
                View.GroupRead = CheckState.Unchecked;
            }
            PermissionsChanged();
        }

        private void OwnerExecuteChanged()
        {
            DetachPermissionHandlers();
            if (View.OwnerExecute == CheckState.Indeterminate)
            {
                View.OwnerExecute = CheckState.Unchecked;
            }
            PermissionsChanged();
        }

        private void OwnerWriteChanged()
        {
            DetachPermissionHandlers();
            if (View.OwnerWrite == CheckState.Indeterminate)
            {
                View.OwnerWrite = CheckState.Unchecked;
            }
            PermissionsChanged();
        }

        private void OwnerReadChanged()
        {
            DetachPermissionHandlers();
            if (View.OwnerRead == CheckState.Indeterminate)
            {
                View.OwnerRead = CheckState.Unchecked;
            }
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
            ChangePermissions(GetPermissionFromCheckboxes(), true);
        }

        private void OctalPermissionsChanged()
        {
            Permission permission = GetPermissionsFromOctalField();
            if (null == permission)
            {
                SystemSounds.Beep.Play();
                InitPermissions();
            }
            else
            {
                bool change = false;
                foreach (Path path in _files)
                {
                    if (!path.attributes().getPermission().Equals(permission))
                    {
                        change = true;
                    }
                }
                if (change)
                {
                    ChangePermissions(permission, false);
                }
            }
        }

        private Permission GetPermissionsFromOctalField()
        {
            if (!String.IsNullOrEmpty(View.OctalPermissions))
            {
                if (View.OctalPermissions.Length == 3)
                {
                    if (Utils.IsInt(View.OctalPermissions))
                    {
                        return new Permission(int.Parse(View.OctalPermissions));
                    }
                }
            }
            return null;
        }

        private void PermissionsChanged()
        {
            ChangePermissions(GetPermissionFromCheckboxes(), false);
        }

        /// <summary>
        /// Write altered permissions to the server
        /// </summary>
        /// <param name="permission"></param>
        /// <param name="recursive"></param>
        private void ChangePermissions(Permission permission, bool recursive)
        {
            if (TogglePermissionSettings(false)){
                _controller.background(new WritePermissionBackgroundAction(_controller, this, permission, recursive));
            }
        }

        private Permission GetPermissionFromCheckboxes()
        {
            bool[][] p = new[] {new bool[3], new bool[3], new bool[3]};

            p[Permission.OWNER][Permission.READ] = (View.OwnerRead == CheckState.Checked);
            p[Permission.OWNER][Permission.WRITE] = (View.OwnerWrite == CheckState.Checked);
            p[Permission.OWNER][Permission.EXECUTE] = (View.OwnerExecute == CheckState.Checked);

            p[Permission.GROUP][Permission.READ] = (View.GroupRead == CheckState.Checked);
            p[Permission.GROUP][Permission.WRITE] = (View.GroupWrite == CheckState.Checked);
            p[Permission.GROUP][Permission.EXECUTE] = (View.GroupExecute == CheckState.Checked);

            p[Permission.OTHER][Permission.READ] = (View.OtherRead == CheckState.Checked);
            p[Permission.OTHER][Permission.WRITE] = (View.OtherWrite == CheckState.Checked);
            p[Permission.OTHER][Permission.EXECUTE] = (View.OtherExecute == CheckState.Checked);

            return new Permission(p);
        }

        private void InitGeneral()
        {
            int count = NumberOfFiles;
            if (count > 0)
            {
                DetachGeneralHandlers();

                Path file = _files[0];
                View.Filename = Name;
                View.FilenameEnabled = (1 == count && _controller.getSession().isRenameSupported(file));
                string path;
                if (!String.IsNullOrEmpty(file.getSymlinkTarget()))
                {
                    path = file.getSymlinkTarget();
                }
                else
                {
                    path = file.getParent().getAbsolute();
                }
                View.Path = path;
                View.PathToolTip = path;
                View.Group = count > 1
                                 ? _multipleFilesString
                                 : file.attributes().getGroup();
                if (count > 1)
                {
                    View.Kind = _multipleFilesString;
                    View.Checksum = _multipleFilesString;
                    View.Modified = _multipleFilesString;
                    View.Created = _multipleFilesString;
                }
                else
                {
                    View.Kind = file.kind();
                    if (-1 == file.attributes().getModificationDate())
                    {
                        View.Modified = _unknownString;
                    }
                    else
                    {
                        View.Modified =
                            DateFormatterFactory.instance().getLongFormat(file.attributes().getModificationDate());
                    }
                    if (-1 == file.attributes().getCreationDate())
                    {
                        View.Created = _unknownString;
                    }
                    else
                    {
                        View.Created =
                            DateFormatterFactory.instance().getLongFormat(file.attributes().getModificationDate());
                    }
                }
                View.FileOwner = count > 1
                                     ? _multipleFilesString
                                     : file.attributes().getOwner();

                if (count > 1)
                {
                    View.FileIcon = IconCache.Instance.IconForName("multiple");
                }
                else
                {
                    View.FileIcon = IconCache.Instance.IconForPath(_files[0], IconCache.IconSize.Large);
                }
            }
        }

        private void InitS3()
        {
            DetachS3Handlers();

            View.BucketLocation = _unknownString;
            View.BucketLoggingTooltip = _unknownString;
            View.S3PublicUrl = _unknownString;
            View.S3TorrentUrl = _unknownString;


            IList<string> classes = new List<string>();
            classes.Add(Locale.localizedString("Unknown"));
            View.PopulateStorageClass(classes);
            View.StorageClass = Locale.localizedString("Unknown");

            if (ToggleS3Settings(false))
            {
                List list = ((CloudSession) _controller.getSession()).getSupportedStorageClasses();
                for (int i = 0; i < list.size(); i++)
                {
                    classes.Add((string) list.get(i));
                }
                if (NumberOfFiles > 1)
                {
                    View.S3PublicUrl = _multipleFilesString;
                    View.S3PublicUrlTooltip = null;
                    View.S3TorrentUrl = _multipleFilesString;
                    View.S3TorrentUrlTooltip = null;
                }
                else
                {
                    foreach (Path file in _files)
                    {
                        if (file.attributes().isFile())
                        {
                            S3Path s3 = (S3Path) file;
                            View.BucketLoggingTooltip = s3.getContainerName() + "/" +
                                                        Preferences.instance().getProperty("s3.logging.prefix");
                            String redundancy = s3.attributes().getStorageClass();
                            if (Utils.IsNotBlank(redundancy))
                            {
                                classes.Remove(Locale.localizedString("Unknown"));
                                View.StorageClass = Locale.localizedString(redundancy, "S3");
                            }
                            CloudPath.DescriptiveUrl url = s3.toSignedUrl();
                            if (null != url)
                            {
                                View.S3PublicUrl = url.getUrl();
                                View.S3PublicUrlTooltip = url.getUrl();

                                View.S3PublicUrlValidity = url.getHelp();
                            }
                            CloudPath.DescriptiveUrl torrent = s3.toTorrentUrl();
                            if (null != torrent)
                            {
                                View.S3TorrentUrl = torrent.getUrl();
                                View.S3TorrentUrlTooltip = torrent.getUrl();
                            }
                        }
                    }
                }
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
            Session session = _controller.getSession();
            // Amazon S3 only
            bool enable = session is S3Session;
            if (enable)
            {
                Credentials credentials = session.getHost().getCredentials();
                enable = !credentials.isAnonymousLogin();
            }
            bool logging = false;
            bool versioning = false;
            bool storageclass = false;
            if (enable)
            {
                logging = ((S3Session) session).isLoggingSupported();
                versioning = ((S3Session) session).isVersioningSupported();
                storageclass = ((S3Session) session).getSupportedStorageClasses().size() > 1;
            }
            View.BucketVersioningEnabled = stop && enable && versioning;
            View.BucketMfaEnabled = stop && enable && versioning && View.BucketVersioning;
            View.BucketLoggingEnabled = stop && enable && logging;
            View.StorageClassEnabled = stop && enable && storageclass;

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

            View.DistributionStatus = _unknownString;
            View.DistributionUrl = _unknownString;
            View.DistributionStatus = _unknownString;
            View.DistributionCname = _unknownString;

            KeyValuePair<string, Distribution.Method> unknownEntry =
                new KeyValuePair<string, Distribution.Method>(_unknownString, null);
            IList<KeyValuePair<string, Distribution.Method>> methods = new List
                <KeyValuePair<string, Distribution.Method>>
                                                                           {
                                                                               unknownEntry
                                                                           };
            View.PopulateDistributionDeliveryMethod(methods);

            _roots.Add(_unknownString);
            View.PopulateDefaultRoot(_roots);

            if (ToggleDistributionSettings(false))
            {
                CloudSession session = (CloudSession) _controller.getSession();
                View.DistributionTitle = String.Format(Locale.localizedString("Enable {0} Distribution", "Status"),
                                                       session.getDistributionServiceName());
                methods.Remove(unknownEntry);

                List list = session.getSupportedDistributionMethods();
                for (int i = 0; i < list.size(); i++)
                {
                    Distribution.Method method = (Distribution.Method) list.get(i);
                    methods.Add(new KeyValuePair<string, Distribution.Method>(method.ToString(), method));
                }
                View.DistributionDeliveryMethod = Distribution.DOWNLOAD;
            }
            DistributionDeliveryMethodChanged();
            AttachDistributionHandlers();
        }

        private void InitPermissions()
        {
            DetachPermissionHandlers();

            View.Permissions = _unknownString;
            View.OctalPermissions = _unknownString;

            if (TogglePermissionSettings(false))
            {
                _controller.background(new FetchPermissionsBackgroundAction(_controller, this));
            }
            else
            {
                AttachPermissionHandlers();
            }
        }

        /// <summary>
        /// Toggle settings before and after update
        /// </summary>
        /// <param name="stop">Enable controls and stop progress spinner</param>
        /// <returns>True if controls are enabled for the given protocol in idle state</returns>
        private bool TogglePermissionSettings(bool stop)
        {
            Session session = _controller.getSession();
            Credentials credentials = session.getHost().getCredentials();
            bool enable = !credentials.isAnonymousLogin() && session.isUnixPermissionsSupported();
            View.RecursivePermissionsEnabled = stop && enable;
            foreach (Path next in _files)
            {
                if (next.attributes().isFile())
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
            } else if (enable)
            {
                View.PermissionAnimationActive = true;
            }
            return enable;
        }

        private void InitWebUrl()
        {
            DetachGeneralHandlers();
            if (NumberOfFiles > 1)
            {
                View.WebUrl = _multipleFilesString;
                View.WebUrlTooltip = null;
            }
            else
            {
                View.WebUrl = _unknownString;
                _controller.Background(new WebUrlBackgroundAction(_controller, this));
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
                if (ToggleSizeSettings(false))
                {
                    _controller.background(new ChecksumBackgroundAction(_controller, this));
                }
            }
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

        private bool ToggleSizeSettings(bool stop)
        {
            View.SizeButtonEnabled = false;
            foreach (Path aPath in _files)
            {
                if (aPath.attributes().isDirectory())
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
            StringBuilder formatted = new StringBuilder(Status.getSizeAsString(size));
            if (size > -1)
            {
                formatted.Append(" (").Append(size.ToString("N0")).Append(" bytes)");
            }
            View.FileSize = formatted.ToString();
        }

        private class SetBucketLoggingBackgroundAction : BrowserBackgroundAction
        {
            private readonly InfoController _infoController;

            public SetBucketLoggingBackgroundAction(BrowserController browserController, InfoController infoController) : base(browserController)
            {
                _infoController = infoController;
            }

            public override void run()
            {
                foreach (Path next in _infoController._files)
                {
                    string container = next.getContainerName();
                    ((S3Session)BrowserController.getSession()).setLogging(container, _infoController.View.BucketLogging);
                    break;
                }
            }

            public override void cleanup()
            {
                _infoController.ToggleS3Settings(true);
            }

            public override string getActivity()
            {
                return String.Format(Locale.localizedString("Writing metadata of {0}", "Status"),
                                     toString(Utils.ConvertToJavaList(_infoController.Files)));
            }
        }

        private class SetBucketVersioningAndMfaBackgroundAction : BrowserBackgroundAction
        {
            private readonly InfoController _infoController;

            public SetBucketVersioningAndMfaBackgroundAction(BrowserController browserController, InfoController infoController)
                : base(browserController)
            {
                _infoController = infoController;
            }

            public override void run()
            {
                foreach (Path next in _infoController._files)
                {
                    string container = next.getContainerName();
                    ((S3Session)BrowserController.getSession()).setVersioning(container, _infoController.View.BucketMfa, _infoController.View.BucketVersioning);
                    break;
                }
            }

            public override void cleanup()
            {
                _infoController.ToggleS3Settings(true);
            }
        }

        private class CacheControlBackgroundAction : BrowserBackgroundAction
        {
            private readonly string _cache;
            private readonly List<Path> _files;
            private readonly InfoController _infoController;

            public CacheControlBackgroundAction(BrowserController browserController, InfoController infoController,
                                                List<Path> files, string cache) : base(browserController)
            {
                _files = files;
                _cache = cache;
                _infoController = infoController;
            }

            public override void run()
            {
                foreach (Path file in _files)
                {
                    ((S3Path) file).setCacheControl(_cache);
                }
            }

            public override void cleanup()
            {
                _infoController.ToggleS3Settings(true);
            }
        }

        private class WritePermissionBackgroundAction : InfoBackgroundAction
        {
            public WritePermissionBackgroundAction(BrowserController browserController, 
                InfoController infoController, 
                Permission permission, bool recursive) : base(browserController, 
                new InnerWritePermissionWorker(infoController, 
                    Utils.ConvertToJavaList(infoController._files), 
                    permission, 
                    recursive))
            {
            }

            private class InnerWritePermissionWorker : WritePermissionWorker
            {
                private readonly InfoController _infoController;

                public InnerWritePermissionWorker(InfoController infoController, 
                    List files, 
                    Permission permission, 
                    bool recursive) : base(files, permission, recursive)
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

        public static class Factory
        {
            private static readonly IDictionary<BrowserController, InfoController> Open =
                new Dictionary<BrowserController, InfoController>();

            private static readonly object SyncRoot = new Object();

            public static InfoController Create(BrowserController controller, List<Path> files)
            {
                if (Preferences.instance().getBoolean("browser.info.isInspector"))
                {
                    if (Open.ContainsKey(controller))
                    {
                        lock (SyncRoot)
                        {
                            return Open[controller];
                        }
                    }
                }

                InfoController c = new InfoController(controller, files);
                controller.View.ViewClosedEvent += delegate
                                                       {
                                                           lock (SyncRoot)
                                                           {
                                                               Open.Remove(controller);
                                                           }
                                                       };
                controller.getSession().addConnectionListener(new InfoConnectionListener(c, controller));

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

            private class InfoConnectionListener : ConnectionAdapter
            {
                private readonly BrowserController _browserController;
                private readonly InfoController _infoController;

                public InfoConnectionListener(InfoController infoController, BrowserController browserController)
                {
                    _infoController = infoController;
                    _browserController = browserController;
                }

                public override void connectionDidClose()
                {
                    _infoController.View.Close();
                    _browserController.getSession().removeConnectionListener(this);
                }
            }
        }

        private class FetchAclBackgroundAction : BrowserBackgroundAction
        {
            private readonly InfoController _infoController;
            private readonly List _jUpdated = new ArrayList();
            private readonly IList<UserAndRoleEntry> _updated = new List<UserAndRoleEntry>();

            public FetchAclBackgroundAction(BrowserController controller,
                                            InfoController infoController) : base(controller)
            {
                _infoController = infoController;
            }

            public override void run()
            {
                foreach (Path next in _infoController.Files)
                {
                    if (Acl.EMPTY.equals(next.attributes().getAcl()))
                    {
                        next.readAcl();
                    }

                    List acls = next.attributes().getAcl().asList();
                    for (int i = 0; i < acls.size(); i++)
                    {
                        Acl.UserAndRole uar = acls.get(i) as Acl.UserAndRole;
                        if (_jUpdated.contains(uar))
                        {
                            continue;
                        }
                        _updated.Add(new UserAndRoleEntry(uar.getUser(), uar.getRole()));
                        _jUpdated.add(uar);
                    }
                }
            }

            public override void cleanup()
            {
                _infoController.SetAcl(_updated);
                _infoController.ToggleAclSettings(true);

                //todo attachAclHandler?
            }

            public override string getActivity()
            {
                return String.Format(Locale.localizedString("Getting permission of {0}", "Status"),
                                     toString(Utils.ConvertToJavaList(_infoController.Files)));
            }
        }

        private class SetStorageClassBackgroundAction : BrowserBackgroundAction
        {
            private readonly InfoController _infoController;

            public SetStorageClassBackgroundAction(BrowserController controller,
                                            InfoController infoController) : base(controller)
            {
                _infoController = infoController;
            }

            public override void run()
            {
                foreach (Path next in _infoController._files)
                {
                    next.attributes().setStorageClass(_infoController.View.StorageClass);
                    // Copy item in place to write new attributes
                    next.copy(next);                    
                }
            }

            public override void cleanup()
            {
                _infoController.ToggleS3Settings(true);
            }

            public override string getActivity()
            {
                return String.Format(Locale.localizedString("Writing metadata of {0}", "Status"),
                                     toString(Utils.ConvertToJavaList(_infoController.Files)));
            }
        }

        private class FetchS3BackgroundAction : BrowserBackgroundAction
        {
            private readonly InfoController _infoController;
            private readonly IInfoView _view;

            private String _location;
            private bool _logging;
            private bool _mfa;
            private bool _versioning;

            public FetchS3BackgroundAction(BrowserController browserController, InfoController infoController)
                : base(browserController)
            {
                _infoController = infoController;
                _view = infoController.View;
            }

            public override void run()
            {
                foreach (Path file in _infoController.Files)
                {
                    S3Session s = (S3Session) BrowserController.getSession();
                    String container = file.getContainerName();
                    _location = s.getLocation(container);
                    _logging = s.isLogging(container);
                    _versioning = s.isVersioning(container);
                    _mfa = s.isMultiFactorAuthentication(container);
                }
            }

            public override void cleanup()
            {
                _view.BucketLogging = _logging;
                if (Utils.IsNotBlank(_location))
                {
                    _view.BucketLocation = Locale.localizedString(_location, "S3");
                }
                _view.BucketVersioning = _versioning;
                _view.BucketMfaEnabled = _versioning;
                _view.BucketMfa = _mfa;
                _infoController.ToggleS3Settings(true);
            }
        }

        private class ReadDistributionBackgroundAction : BrowserBackgroundAction
        {
            private readonly Distribution.Method _deliveryMethod;
            private readonly List<Path> _files;
            private readonly InfoController _infoController;
            private readonly IInfoView _view;
            private Distribution _distribution;

            public ReadDistributionBackgroundAction(BrowserController browserController, InfoController infoController)
                : base(browserController)
            {
                _infoController = infoController;
                _view = infoController.View;
                _deliveryMethod = _view.DistributionDeliveryMethod;
            }

            public override void run()
            {
                foreach (Path file in _files)
                {
                    CloudPath cloud = (CloudPath) file;
                    CloudSession session = (CloudSession) BrowserController.getSession();
                    // We only support one distribution per bucket for the sake of simplicity
                    if (_deliveryMethod == Distribution.STREAMING || _deliveryMethod == Distribution.DOWNLOAD)
                    {
                        _distribution = session.readDistribution(cloud.getContainerName(), _deliveryMethod);
                    }
                    break;
                }
                AttributedList children = _files[0].getContainer().children();
                foreach (AbstractPath next in children)
                {
                    if (next.attributes().isFile())
                    {
                        _infoController._roots.Add(next.getName());
                    }
                }
            }

            public override void cleanup()
            {
                _view.Distribution = _distribution.isEnabled();
                _view.DistributionStatus = _distribution.getStatus();
                _view.DistributionLoggingEnabled = _distribution.isEnabled();
                _view.DistributionLogging = _distribution.isLogging();

                CloudPath file = ((CloudPath) _files[0]);
                // Concatenate URLs
                if (_infoController.NumberOfFiles > 1)
                {
                    _view.DistributionUrl = _infoController._multipleFilesString;
                    _view.DistributionUrlTooltip = null;
                    _view.DistributionCnameUrl = _infoController._multipleFilesString;
                }
                else
                {
                    String url = _distribution.getUrl(file.getKey());
                        _view.DistributionUrl = url;
                        _view.DistributionUrlTooltip = url;
                }
                string[] cnames = _distribution.getCNAMEs();
                if (0 == cnames.Length)
                {
                    _view.DistributionCname = string.Empty;
                    _view.DistributionCnameUrl = string.Empty;
                    _view.DistributionCnameUrlTooltip = null;
                }
                else
                {
                    _view.DistributionCname = string.Join(" ", cnames);
                    foreach (string cname in cnames)
                    {
                        string url = _distribution.getCnameUrl(file.getKey());
                        _view.DistributionCname = url;
                        _view.DistributionCnameUrlTooltip = url;
                        // We only support one CNAME URL
                        break;
                    }
                }
                String defaultRoot = _distribution.getDefaultRootObject();
                if (Utils.IsNotBlank(defaultRoot))
                {
                    _infoController._roots.Add(defaultRoot);
                    _view.DistributionDefaultRoot = defaultRoot;
                }
                _infoController.ToggleDistributionSettings(true);
            }

            public override string getActivity()
            {
                return String.Format(Locale.localizedString("Reading metadata of {0}", "Status"),
                                     toString(Utils.ConvertToJavaList(_infoController.Files)));
            }
        }

        private class WriteMetadataBackgroundAction : InfoBackgroundAction
        {
            public WriteMetadataBackgroundAction(BrowserController controller, InfoController infoController)
                : base(
                    controller,
                    new InnerWriteMetadataWorker(infoController, Utils.ConvertToJavaList(infoController._files),
                                                 infoController.ConvertMetadataToMap()))
            {
            }

            private class InnerWriteMetadataWorker : WriteMetadataWorker
            {
                private readonly InfoController _infoController;

                public InnerWriteMetadataWorker(InfoController infoController, List files, Map metadata)
                    : base(files, metadata)
                {
                    _infoController = infoController;
                }

                public override void cleanup(object obj)
                {
                    _infoController.ToggleMetadataSettings(true);
                    _infoController.InitMetadata();
                }
            }
        }

        private class ReadMetadataBackgroundAction : InfoBackgroundAction
        {
            public ReadMetadataBackgroundAction(BrowserController controller,
                                                 InfoController infoController)
                : base(controller, new InnerReadMetadataWorker(infoController, Utils.ConvertToJavaList(infoController._files)))
            {
            }

            private class InnerReadMetadataWorker : ReadMetadataWorker
            {
                private readonly InfoController _infoController;

                public InnerReadMetadataWorker(InfoController infoController, List files) : base(files)
                {
                    _infoController = infoController;
                }

                public override void cleanup(object obj)
                {
                    Map updated = (Map) obj;
                    Iterator it = updated.entrySet().iterator();
                    IList<InfoController.CustomHeader> metadata = new List<InfoController.CustomHeader>();
                    while (it.hasNext())
                    {
                        Map.Entry pair = (Map.Entry)it.next();
                        metadata.Add(new CustomHeader((string)pair.getKey(), (string)pair.getValue()));
                    }
                    _infoController.ToggleMetadataSettings(true);
                    _infoController.SetMetadata(metadata);
                    //todo attachmetadatahandle`r?
                }
            }
        }

        private class RecursiveSizeAction : InfoBackgroundAction
        {
            public RecursiveSizeAction(BrowserController controller, InfoController infoController,
                                       List<Path> files) : base(controller, new InnerCalculateSizeWorker(infoController, Utils.ConvertToJavaList(files)) )
            {
            }

            private class InnerCalculateSizeWorker : CalculateSizeWorker
            {
                private readonly InfoController _infoController;

                public InnerCalculateSizeWorker(InfoController infoController, List files) : base(files)
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

        private class ReadSizeBackgroundAction : InfoBackgroundAction
        {
            public ReadSizeBackgroundAction(BrowserController browserController, InfoController infoController)
                : base(browserController, new InnerReadSizeWorker(infoController, Utils.ConvertToJavaList(infoController._files)))
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
                    long size = ((Long) obj).longValue();
                    _infoController.UpdateSize(size);
                    _infoController.ToggleSizeSettings(true);
                    _infoController.AttachGeneralHandlers();
                }
            }
        }

        private class ChecksumBackgroundAction : InfoBackgroundAction
        {
            public ChecksumBackgroundAction(BrowserController browserController, InfoController infoController)
                : base(browserController, new InnerChecksumWorker(infoController, Utils.ConvertToJavaList(infoController._files)))
            {
            }

            private class InnerChecksumWorker : ChecksumWorker
            {
                private readonly InfoController _infoController;

                public InnerChecksumWorker(InfoController infoController, List files) : base(files)
                {
                    _infoController = infoController;
                }

                public override void cleanup(object obj)
                {
                    foreach (Path checksum in _infoController.Files)
                    {
                        if (String.IsNullOrEmpty(checksum.attributes().getChecksum()))
                        {
                            _infoController.View.Checksum = _infoController._unknownString;
                        }
                        else
                        {
                            _infoController.View.Checksum = checksum.attributes().getChecksum();
                        }
                    }
                    _infoController.ToggleSizeSettings(true);
                    _infoController.AttachGeneralHandlers();
                }
            } 
        }

        private class FetchPermissionsBackgroundAction : InfoBackgroundAction
        {
            public FetchPermissionsBackgroundAction(BrowserController browserController, InfoController infoController)
                : base(browserController, new InnerReadPermissionWorker(infoController, Utils.ConvertToJavaList(infoController._files)))
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
                    ICollection<Permission> permissions = Utils.ConvertFromJavaList<Permission>((List) obj);
                    foreach (Permission permission in permissions)
                    {
                        view.OwnerRead = GetCheckboxState(view.OwnerRead, view.OwnerReadEnabled,
                                                            permission.getOwnerPermissions()[Permission.READ]);
                        view.OwnerWrite = GetCheckboxState(view.OwnerWrite, view.OwnerWriteEnabled,
                                                            permission.getOwnerPermissions()[Permission.WRITE]);
                        view.OwnerExecute = GetCheckboxState(view.OwnerExecute, view.OwnerExecuteEnabled,
                                                              permission.getOwnerPermissions()[Permission.EXECUTE]);
                        view.GroupRead = GetCheckboxState(view.GroupRead, view.GroupReadEnabled,
                                                           permission.getGroupPermissions()[Permission.READ]);
                        view.GroupWrite = GetCheckboxState(view.GroupWrite, view.GroupWriteEnabled,
                                                            permission.getGroupPermissions()[Permission.WRITE]);
                        view.GroupExecute = GetCheckboxState(view.GroupExecute, view.GroupExecuteEnabled,
                                                              permission.getGroupPermissions()[Permission.EXECUTE]);
                        view.OtherRead = GetCheckboxState(view.OtherRead, view.OtherReadEnabled,
                                                           permission.getOtherPermissions()[Permission.READ]);
                        view.OtherWrite = GetCheckboxState(view.OtherWrite, view.OtherWriteEnabled,
                                                            permission.getOtherPermissions()[Permission.WRITE]);
                        view.OtherExecute = GetCheckboxState(view.OtherExecute, view.OtherExecuteEnabled,
                                                              permission.getOtherPermissions()[Permission.EXECUTE]);
                        view.OwnerReadEnabled = true;
                        view.OwnerWriteEnabled = true;
                        view.OwnerExecuteEnabled = true;
                        view.GroupReadEnabled = true;
                        view.GroupWriteEnabled = true;
                        view.GroupExecuteEnabled = true;
                        view.OtherReadEnabled = true;
                        view.OtherWriteEnabled = true;
                        view.OtherExecuteEnabled = true;                        
                    }

                    if (permissions.Count > 1 )
                    {
                        view.Permissions = _infoController._multipleFilesString;
                    } else
                    {
                        foreach (Permission permission in permissions)
                        {
                            view.OctalPermissions = permission.getOctalString();
                            view.Permissions = permission.toString();
                        }
                    }
                    _infoController.TogglePermissionSettings(true);
                }

                private static CheckState GetCheckboxState(CheckState state, bool enabled, bool condition)
                {
                    // Gets the state which can be CheckState.Checked, CheckState.Unchecked, or CheckState.Indeterminate.
                    if ((state == CheckState.Unchecked || !enabled) && !condition)
                    {
                        return CheckState.Unchecked;
                    }
                    if ((state == CheckState.Checked || !enabled) && condition)
                    {
                        return CheckState.Checked;
                    }
                    return CheckState.Indeterminate;
                }

            }
        }

        public class UserAndRoleEntry : Acl.UserAndRole, INotifyPropertyChanged
        {
            public UserAndRoleEntry(Acl.User user, Acl.Role role)
                : base(user, role)
            {
            }

            public string User
            {
                get { return base.getUser().getDisplayName(); }
                set
                {
                    base.getUser().setIdentifier(value ?? string.Empty);
                    NotifyPropertyChanged("User");
                }
            }

            public string Role
            {
                get { return base.getRole().getName(); }
                set
                {
                    base.getRole().setName(value);
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

        private class WebUrlBackgroundAction : BrowserBackgroundAction
        {
            private readonly InfoController _infoController;
            private AbstractPath.DescriptiveUrl _url;

            public WebUrlBackgroundAction(BrowserController browserController, InfoController infoController)
                : base(browserController)
            {
                _infoController = infoController;
            }

            public override void run()
            {
                foreach (Path file in _infoController.Files)
                {
                    _url = file.toHttpURL();
                    break;
                }
            }

            public override void cleanup()
            {
                if (Utils.IsNotBlank(_url.getUrl()))
                {
                    _infoController.View.WebUrl = _url.getUrl();
                    _infoController.View.WebUrlTooltip = _url.getUrl();
                }
                _infoController.AttachGeneralHandlers();
            }
        }

        private class WriteAclBackgroundAction : BrowserBackgroundAction
        {
            private readonly InfoController _infoController;

            public WriteAclBackgroundAction(BrowserController controller, InfoController infoController)
                : base(controller)
            {
                _infoController = infoController;
            }

            public override void run()
            {
                foreach (Path next in _infoController.Files)
                {
                    Acl.UserAndRole[] aclEntries = new Acl.UserAndRole[_infoController._acl.Count];
                    for (int index = 0; index < _infoController._acl.Count; index++)
                    {
                        aclEntries[index] = _infoController._acl[index];
                    }
                    Acl acl = new Acl();
                    acl.addAll(aclEntries);
                    next.writeAcl(acl, true);
                }
            }

            public override void cleanup()
            {
                _infoController.ToggleAclSettings(true);
                _infoController.InitAcl();
            }

            public override string getActivity()
            {
                return String.Format(Locale.localizedString("Changing permission of {0} to {1}", "Status"),
                                     toString(Utils.ConvertToJavaList(_infoController.Files)),
                                     _infoController._acl);
            }
        }

        private class WriteDistributionBackgroundAction : BrowserBackgroundAction
        {
            private readonly string _cname;
            private readonly Distribution.Method _deliveryMethod;
            private readonly List<Path> _files;
            private readonly InfoController _infoController;
            private readonly IInfoView _view;

            // temporary fields in order prevent cross-context access to the GUI while reading in run()
            private bool _distribution;
            private bool _logging;

            public WriteDistributionBackgroundAction(BrowserController browserController, InfoController infoController,
                                                     List<Path> files) : base(browserController)
            {
                _files = files;
                _infoController = infoController;
                _view = infoController.View;

                _deliveryMethod = _view.DistributionDeliveryMethod;
                _logging = _view.DistributionLogging;
                _cname = _view.DistributionCname;
                _distribution = _view.Distribution;
            }

            public override void run()
            {
                foreach (Path next in _files)
                {
                    CloudPath cloud = (CloudPath) next;
                    CloudSession session = (CloudSession) BrowserController.getSession();
                    Distribution.Method method = Distribution.DOWNLOAD;
                    if (_deliveryMethod == Distribution.STREAMING)
                    {
                        method = Distribution.STREAMING;
                    }
                    if (Utils.IsNotBlank(_cname))
                    {
                        session.writeDistribution(_distribution, cloud.getContainerName(), method,
                                                  _cname.Split(new[] {' '},
                                                  StringSplitOptions.RemoveEmptyEntries),
                                                  _logging,
                                                  _view.DistributionDefaultRoot);
                    }
                    else
                    {
                        session.writeDistribution(_distribution, cloud.getContainerName(), method,
                                                  new string[] {}, _logging, _view.DistributionDefaultRoot);
                    }
                    break;
                }
            }

            public override void cleanup()
            {
                // Refresh the current distribution status
                _infoController.DistributionDeliveryMethodChanged();
            }

            public override string getActivity()
            {
                return String.Format(Locale.localizedString("Writing metadata of {0}", "Status"),
                                     toString(Utils.ConvertToJavaList(_infoController.Files)));
            }
        }
    }
}