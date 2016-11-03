// 
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

using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Drawing;
using System.Windows.Forms;
using ch.cyberduck.core;
using ch.cyberduck.core.cdn;
using Ch.Cyberduck.Ui.Controller;
using Ch.Cyberduck.Ui.Winforms.Controls;
using java.lang;
using String = System.String;

namespace Ch.Cyberduck.Ui.Winforms
{
    public partial class InfoForm : ToolbarBaseForm, IInfoView
    {
        private String lastCname;

        public InfoForm()
        {
            InitializeComponent();

            Load += delegate
            {
                int minWidth = 10; // border etc.
                foreach (ToolStripItem item in toolStrip.Items)
                {
                    minWidth += item.Size.Width + item.Margin.Left + item.Margin.Right;
                }
                MinimumSize = new Size(minWidth, MinimumSize.Height);
            };

            TopMost = false;
            TopLevel = true;

            toolStrip.Renderer = new FirefoxStyleRenderer();

            ConfigureHelp();
            InitAclGrid();
            InitMetadataGrid();

            panelManager.SelectedIndexChanged += (sender, args) => ActiveTabChanged();

            generalButton_Click(this, EventArgs.Empty);

            LocalizationCompleted += ResizeForm;

            //some font tweaking
            statusLabel.Font = DefaultFontBold;
            authenticatedLabel.Font = DefaultFontBold;
            AclLabel.Font = DefaultFontBold;

            whereLinkLabel.Text = String.Empty;
            cnameUrlLinkLabel.Text = String.Empty;

            MaximumSize = new Size(1000, Height + 30);
        }

        /// <summary>
        /// Activate Double-Buffering for _all_ controls on a form
        /// </summary>
        /// <see cref="http://social.msdn.microsoft.com/Forums/en-US/winforms/thread/aaed00ce-4bc9-424e-8c05-c30213171c2c"/>
        /// <see cref="http://social.msdn.microsoft.com/Forums/en-US/winforms/thread/afc667cd-d877-4e44-9c00-8501998865e2"/>
        /// <see cref="http://stackoverflow.com/questions/3466003/winforms-controls-are-flickering-when-resizing-on-windows7-x64"/>
        protected override CreateParams CreateParams
        {
            get
            {
                CreateParams cp = base.CreateParams;
                cp.ExStyle |= 0x02000000;
                return cp;
            }
        }

        public override ToolStrip ToolStrip
        {
            get { return toolStrip; }
        }

        public override string[] BundleNames
        {
            get { return new[] {"Info"}; }
        }

        public event EventHandler<InfoHelpArgs> ShowHelp;

        public bool ToolbarDistributionEnabled
        {
            set { distributionButton.Enabled = value; }
        }

        public bool ToolbarS3Enabled
        {
            set { s3Button.Enabled = value; }
        }

        public Image ToolbarS3Image
        {
            set { s3Button.Image = value; }
        }

        public String ToolbarS3Label
        {
            set { s3Button.Text = value; }
        }

        public bool ToolbarPermissionsEnabled
        {
            set { permissionsButton.Enabled = value; }
        }

        public bool ToolbarMetadataEnabled
        {
            set { metadataButton.Enabled = value; }
        }

        public InfoTab ActiveTab
        {
            get
            {
                if (panelManager.SelectedPanel == managedGeneralPanel)
                {
                    return InfoTab.General;
                }
                if (panelManager.SelectedPanel == managedPermissionsPanel)
                {
                    if (panelManagerPermissions.SelectedPanel == cloudManagedPanel)
                    {
                        return InfoTab.Acl;
                    }
                    return InfoTab.Permissions;
                }
                if (panelManager.SelectedPanel == managedDistributionPanel)
                {
                    return InfoTab.Distribution;
                }
                if (panelManager.SelectedPanel == managedS3Panel)
                {
                    return InfoTab.S3;
                }
                if (panelManager.SelectedPanel == managedMetadataPanel)
                {
                    return InfoTab.Metadata;
                }
                throw new IllegalArgumentException("Invalid state");
            }
            set
            {
                if (value == InfoTab.General)
                {
                    generalButton_Click(this, EventArgs.Empty);
                }
                if (value == InfoTab.Permissions || value == InfoTab.Acl)
                {
                    //inner panel is already set
                    permissionsButton_Click(this, EventArgs.Empty);
                }
                if (value == InfoTab.Distribution)
                {
                    distributionButton_Click(this, EventArgs.Empty);
                }
                if (value == InfoTab.S3)
                {
                    s3Button_Click(this, EventArgs.Empty);
                }
                if (value == InfoTab.Metadata)
                {
                    metadataButton_Click(this, EventArgs.Empty);
                }
            }
        }

        public Image ToolbarDistributionImage
        {
            set { distributionButton.Image = value; }
        }

        public Image FileIcon
        {
            set { icon.Image = value; }
        }

        public string Filename
        {
            get { return filenameTextbox.Text; }
            set
            {
                filenameTextbox.Text = value;
                Text = String.Format("{0} - {1}", LocaleFactory.localizedString("Info", "Info"), value);
            }
        }

        public bool FilenameEnabled
        {
            set { filenameTextbox.Enabled = value; }
        }

        public bool SizeButtonEnabled
        {
            set { calculateButton.Enabled = value; }
        }

        public bool SizeAnimationActive
        {
            set { sizeAnimation.Visible = value; }
        }

        public bool PermissionAnimationActive
        {
            set { permissionAnimation.Visible = value; }
        }

        public string FileSize
        {
            set { sizeLabel.Text = value; }
        }

        public string Path
        {
            set { pathLabel.Text = value; }
        }

        public string PathToolTip
        {
            set { toolTip.SetToolTip(pathLabel, value); }
        }

        public string WebUrl
        {
            set { weburlLabel.Text = value; }
        }

        public string WebUrlTooltip
        {
            set { toolTip.SetToolTip(weburlLabel, value); }
        }

        public string Kind
        {
            set { kindLabel.Text = value; }
        }

        public string Permissions
        {
            set { permissionsLabel.Text = value; }
        }

        public string FileOwner
        {
            set { ownerLabel.Text = value; }
        }

        public string Group
        {
            set { groupLabel.Text = value; }
        }

        public string FileCreated
        {
            set { createdLabel.Text = value; }
        }

        public string Modified
        {
            set { modifiedLabel.Text = value; }
        }

        public string Checksum
        {
            set { checksumTextBox.Text = value; }
        }

        public bool LifecycleTransitionCheckboxEnabled
        {
            set { lifecycleTransitionCheckBox.Enabled = value; }
        }

        public bool LifecycleDeleteCheckbox
        {
            get { return lifecycleDeleteCheckBox.Checked; }
            set { lifecycleDeleteCheckBox.Checked = value; }
        }

        public bool LifecycleDeleteCheckboxEnabled
        {
            set { lifecycleDeleteCheckBox.Enabled = value; }
        }

        public string LifecycleTransition
        {
            get { return (string) lifecycleTransitionComboBox.SelectedValue; }
            set { lifecycleTransitionComboBox.SelectedValue = value; }
        }

        public bool LifecycleTransitionPopupEnabled
        {
            set { lifecycleTransitionComboBox.Enabled = value; }
        }

        public string LifecycleDelete
        {
            get { return (string) lifecycleDeleteComboBox.SelectedValue; }
            set { lifecycleDeleteComboBox.SelectedValue = value; }
        }

        public bool LifecycleDeletePopupEnabled
        {
            set { lifecycleDeleteComboBox.Enabled = value; }
        }

        public event VoidHandler FilenameChanged = delegate { };

        public CheckState OwnerRead
        {
            get { return ownerrCheckBox.CheckState; }
            set { ownerrCheckBox.CheckState = value; }
        }

        public bool OwnerReadEnabled
        {
            get { return ownerrCheckBox.Enabled; }
            set { ownerrCheckBox.Enabled = value; }
        }

        public CheckState OwnerWrite
        {
            get { return ownerwCheckBox.CheckState; }
            set { ownerwCheckBox.CheckState = value; }
        }

        public bool OwnerWriteEnabled
        {
            get { return ownerwCheckBox.Enabled; }
            set { ownerwCheckBox.Enabled = value; }
        }

        public CheckState OwnerExecute
        {
            get { return ownerxCheckBox.CheckState; }
            set { ownerxCheckBox.CheckState = value; }
        }

        public bool OwnerExecuteEnabled
        {
            get { return ownerxCheckBox.Enabled; }
            set { ownerxCheckBox.Enabled = value; }
        }

        public CheckState GroupRead
        {
            get { return grouprCheckbox.CheckState; }
            set { grouprCheckbox.CheckState = value; }
        }

        public bool GroupReadEnabled
        {
            get { return grouprCheckbox.Enabled; }
            set { grouprCheckbox.Enabled = value; }
        }

        public CheckState GroupWrite
        {
            get { return groupwCheckbox.CheckState; }
            set { groupwCheckbox.CheckState = value; }
        }

        public bool GroupWriteEnabled
        {
            get { return groupwCheckbox.Enabled; }
            set { groupwCheckbox.Enabled = value; }
        }

        public CheckState GroupExecute
        {
            get { return groupxCheckbox.CheckState; }
            set { groupxCheckbox.CheckState = value; }
        }

        public bool GroupExecuteEnabled
        {
            get { return groupxCheckbox.Enabled; }
            set { groupxCheckbox.Enabled = value; }
        }

        public CheckState OtherRead
        {
            get { return otherrCheckbox.CheckState; }
            set { otherrCheckbox.CheckState = value; }
        }

        public bool OtherReadEnabled
        {
            get { return otherrCheckbox.Enabled; }
            set { otherrCheckbox.Enabled = value; }
        }

        public CheckState OtherWrite
        {
            get { return otherwCheckbox.CheckState; }
            set { otherwCheckbox.CheckState = value; }
        }

        public bool OtherWriteEnabled
        {
            get { return otherwCheckbox.Enabled; }
            set { otherwCheckbox.Enabled = value; }
        }

        public CheckState OtherExecute
        {
            get { return otherxCheckbox.CheckState; }
            set { otherxCheckbox.CheckState = value; }
        }

        public bool OtherExecuteEnabled
        {
            get { return otherxCheckbox.Enabled; }
            set { otherxCheckbox.Enabled = value; }
        }

        public bool RecursivePermissionsEnabled
        {
            set { applyRecursivePermissionsButton.Enabled = value; }
        }

        public string OctalPermissions
        {
            get { return octalTextBox.Text; }
            set { octalTextBox.Text = value; }
        }

        public bool OctalPermissionsEnabled
        {
            set { octalTextBox.Enabled = value; }
        }

        public List<InfoController.UserAndRoleEntry> SelectedAclEntries
        {
            get
            {
                List<InfoController.UserAndRoleEntry> selected = new List<InfoController.UserAndRoleEntry>();
                DataGridViewSelectedRowCollection rows = aclDataGridView.SelectedRows;
                foreach (DataGridViewRow row in rows)
                {
                    selected.Add(row.DataBoundItem as InfoController.UserAndRoleEntry);
                }
                return selected;
            }
        }

        public bool AclPanel
        {
            set { panelManagerPermissions.SelectedPanel = value ? cloudManagedPanel : nonCloudManagedPanel; }
        }

        public BindingList<InfoController.UserAndRoleEntry> AclDataSource
        {
            set { aclDataGridView.DataSource = value; }
        }

        public bool AclTableEnabled
        {
            set { aclDataGridView.Enabled = value; }
        }

        public bool AclAddEnabled
        {
            set { addAclButton.Enabled = value; }
        }

        public bool AclRemoveEnabled
        {
            set { addAclContextMenuStrip.Items[addAclContextMenuStrip.Items.Count - 1].Enabled = value; }
        }

        public void EditAclRow(InfoController.UserAndRoleEntry aclEntry, bool selectRole)
        {
            foreach (DataGridViewRow row in aclDataGridView.Rows)
            {
                if (row.DataBoundItem == aclEntry)
                {
                    aclDataGridView.CurrentCell = selectRole
                        ? row.Cells[AclColumnName.Role.ToString()]
                        : row.Cells[AclColumnName.User.ToString()];
                    break;
                }
            }
            aclDataGridView.BeginEdit(true);
        }

        public void EditMetadataRow(InfoController.CustomHeaderEntry headerEntry, bool selectValue)
        {
            foreach (DataGridViewRow row in metadataDataGridView.Rows)
            {
                if (row.DataBoundItem == headerEntry)
                {
                    metadataDataGridView.CurrentCell = selectValue
                        ? row.Cells[MetadataColumName.Value.ToString()]
                        : row.Cells[MetadataColumName.Name.ToString()];
                    break;
                }
            }
            metadataDataGridView.BeginEdit(true);
        }

        public Distribution.Method DistributionDeliveryMethod
        {
            get { return (Distribution.Method) deliveryMethodComboBox.SelectedValue; }
            set { deliveryMethodComboBox.SelectedValue = value; }
        }

        public bool DistributionCnameUrlEnabled
        {
            set { cnameUrlLinkLabel.Enabled = value; }
        }

        public string DistributionCnameUrlTooltip
        {
            set { toolTip.SetToolTip(cnameUrlLinkLabel, value); }
        }

        public bool DistributionCnameEnabled
        {
            set { distributionCnameTextBox.Enabled = value; }
        }

        public bool DistributionAnimationActive
        {
            set { distributionAnimation.Visible = value; }
        }

        public string DistributionDefaultRoot
        {
            get { return (String) defaultRootComboBox.SelectedValue; }
            set { defaultRootComboBox.SelectedValue = value; }
        }

        public event VoidHandler CalculateSize = delegate { };

        public void PopulateAclUsers(IDictionary<string, AsyncController.SyncDelegate> users)
        {
            addAclContextMenuStrip.Items.Clear();
            ToolStripItem[] items = new ToolStripItem[users.Count + 1];

            int i = 0;
            foreach (KeyValuePair<string, AsyncController.SyncDelegate> user in users)
            {
                ToolStripMenuItem item = new ToolStripMenuItem(user.Key);
                KeyValuePair<string, AsyncController.SyncDelegate> userDelegate = user;
                item.Click += delegate { userDelegate.Value(); };
                items[i] = item;
                i++;

                if ((users.Count - 1) == i)
                {
                    items[i++] = new ToolStripSeparator();
                }
            }
            addAclContextMenuStrip.Items.AddRange(items);
        }

        public void PopulateAclRoles(IList<string> roles)
        {
            ((DataGridViewComboBoxColumn) aclDataGridView.Columns[AclColumnName.Role.ToString()]).DataSource = roles;
        }

        public bool AclAnimationActive
        {
            set { aclAnimation.Visible = value; }
        }

        public string AclUrl
        {
            set { authenticatedUrlLinkLabel.Text = value; }
        }

        public bool AclUrlEnabled
        {
            set { authenticatedUrlLinkLabel.Enabled = value; }
        }

        public string AclUrlTooltip
        {
            set { toolTip.SetToolTip(authenticatedUrlLinkLabel, value); }
        }

        public event VoidHandler OwnerReadChanged = delegate { };
        public event VoidHandler OwnerWriteChanged = delegate { };
        public event VoidHandler OwnerExecuteChanged = delegate { };
        public event VoidHandler GroupReadChanged = delegate { };
        public event VoidHandler GroupWriteChanged = delegate { };
        public event VoidHandler GroupExecuteChanged = delegate { };
        public event VoidHandler OtherReadChanged = delegate { };
        public event VoidHandler OtherWriteChanged = delegate { };
        public event VoidHandler OtherExecuteChanged = delegate { };
        public event VoidHandler ApplyRecursivePermissions = delegate { };
        public event VoidHandler OctalPermissionsChanged = delegate { };


        public bool DistributionDefaultRootEnabled
        {
            set { defaultRootComboBox.Enabled = value; }
        }

        public string DistributionInvalidationStatus
        {
            set { invalidationStatus.Text = value; }
        }

        public string DistributionInvalidateObjectsTooltip
        {
            set { toolTip.SetToolTip(invalidateButton, value); }
        }

        public bool DistributionInvalidateObjectsEnabled
        {
            set { invalidateButton.Enabled = value; }
        }

        public void PopulateDistributionDeliveryMethod(IList<KeyValuePair<string, Distribution.Method>> methods)
        {
            deliveryMethodComboBox.DataSource = null;
            deliveryMethodComboBox.DataSource = methods;
            deliveryMethodComboBox.DisplayMember = "Key";
            deliveryMethodComboBox.ValueMember = "Value";
        }

        public void PopulateDefaultRoot(IList<KeyValuePair<string, string>> roots)
        {
            defaultRootComboBox.DataSource = null;
            defaultRootComboBox.DataSource = roots;
            defaultRootComboBox.DisplayMember = "Key";
            defaultRootComboBox.ValueMember = "Value";
        }

        public void PopulateBucketLogging(IList<string> buckets)
        {
            bucketLoggingComboBox.DataSource = buckets;
        }

        public void PopulateDistributionLogging(IList<string> buckets)
        {
            distributionLoggingComboBox.DataSource = buckets;
        }

        public void PopulateLifecycleTransitionPeriod(IList<KeyValuePair<string, string>> periods)
        {
            lifecycleTransitionComboBox.DataSource = null;
            lifecycleTransitionComboBox.DataSource = periods;
            lifecycleTransitionComboBox.DisplayMember = "Key";
            lifecycleTransitionComboBox.ValueMember = "Value";
        }

        public void PopulateLifecycleDeletePeriod(IList<KeyValuePair<string, string>> periods)
        {
            lifecycleDeleteComboBox.DataSource = null;
            lifecycleDeleteComboBox.DataSource = periods;
            lifecycleDeleteComboBox.DisplayMember = "Key";
            lifecycleDeleteComboBox.ValueMember = "Value";
        }

        public bool DistributionDeliveryMethodEnabled
        {
            set { deliveryMethodComboBox.Enabled = value; }
        }

        public bool Distribution
        {
            get { return distributionEnableCheckBox.Checked; }
            set { distributionEnableCheckBox.Checked = value; }
        }

        public bool DistributionEnabled
        {
            set { distributionEnableCheckBox.Enabled = value; }
        }

        public string DistributionTitle
        {
            set { distributionEnableCheckBox.Text = value; }
        }

        public string DistributionStatus
        {
            set { statusLabel.Text = value; }
        }

        public bool DistributionLoggingCheckbox
        {
            get { return distributionLoggingCheckBox.Checked; }
            set { distributionLoggingCheckBox.Checked = value; }
        }

        public bool DistributionLoggingCheckboxEnabled
        {
            set { distributionLoggingCheckBox.Enabled = value; }
        }

        public string DistributionLoggingPopup
        {
            get { return distributionLoggingComboBox.Text; }
            set { distributionLoggingComboBox.Text = value; }
        }

        public bool DistributionLoggingPopupEnabled
        {
            set { distributionLoggingComboBox.Enabled = value; }
        }

        public string DistributionUrl
        {
            set { whereLinkLabel.Text = value; }
        }

        public string DistributionUrlTooltip
        {
            set { toolTip.SetToolTip(whereLinkLabel, value); }
        }

        public bool DistributionUrlEnabled
        {
            set { whereLinkLabel.Enabled = value; }
        }

        public string DistributionOrigin
        {
            set { originLinkLabel.Text = value; }
        }

        public string DistributionCname
        {
            get { return distributionCnameTextBox.Text; }
            set
            {
                distributionCnameTextBox.Text = value;
                lastCname = value;
            }
        }

        public string DistributionCnameUrl
        {
            set { cnameUrlLinkLabel.Text = value; }
        }

        public event VoidHandler DistributionDeliveryMethodChanged = delegate { };
        public event VoidHandler DistributionEnabledChanged = delegate { };
        public event VoidHandler DistributionLoggingCheckboxChanged = delegate { };
        public event VoidHandler DistributionLoggingPopupChanged = delegate { };
        public event VoidHandler DistributionCnameChanged = delegate { };
        public event VoidHandler DistributionInvalidateObjects = delegate { };

        public void PopulateStorageClass(IList<KeyValuePair<string, string>> classes)
        {
            storageClassComboBox.DataSource = null;
            storageClassComboBox.DataSource = classes;
            storageClassComboBox.DisplayMember = "Key";
            storageClassComboBox.ValueMember = "Value";
        }

        public void PopulateEncryption(IList<KeyValuePair<string, string>> algorithms)
        {
            encryptionComboBox.DataSource = null;
            encryptionComboBox.DataSource = algorithms;
            encryptionComboBox.DisplayMember = "Key";
            encryptionComboBox.ValueMember = "Value";
        }

        public event VoidHandler TransferAccelerationCheckboxChanged = delegate { };

        public string BucketLocation
        {
            set { bucketLocationLabel.Text = value; }
        }

        public string Encryption
        {
            get { return (string) encryptionComboBox.SelectedValue; }
            set { encryptionComboBox.SelectedValue = value; }
        }

        public bool EncryptionEnabled
        {
            get { return encryptionComboBox.Enabled; }
            set { encryptionComboBox.Enabled = value; }
        }

        public string StorageClass
        {
            get { return (string) storageClassComboBox.SelectedValue; }
            set { storageClassComboBox.SelectedValue = value; }
        }

        public bool StorageClassEnabled
        {
            set { storageClassComboBox.Enabled = value; }
        }

        public string BucketLoggingPopup
        {
            get { return bucketLoggingComboBox.Text; }
            set { bucketLoggingComboBox.Text = value; }
        }

        public bool BucketLoggingPopupEnabled
        {
            set { bucketLoggingComboBox.Enabled = value; }
        }

        public bool S3AnimationActive
        {
            set { s3Animation.Visible = value; }
        }

        public bool TransferAccelerationCheckbox
        {
            get { return transferAccelerationCheckBox.Checked; }
            set { transferAccelerationCheckBox.Checked = value; }
        }

        public bool TransferAccelerationCheckboxEnabled
        {
            set { transferAccelerationCheckBox.Enabled = value; }
        }

        public bool BucketLoggingCheckbox
        {
            get { return bucketLoggingCheckBox.Checked; }
            set { bucketLoggingCheckBox.Checked = value; }
        }

        public string BucketLoggingTooltip
        {
            set { toolTip.SetToolTip(bucketLoggingCheckBox, value); }
        }

        public bool BucketLoggingCheckboxEnabled
        {
            set { bucketLoggingCheckBox.Enabled = value; }
        }

        public bool BucketAnalyticsCheckbox
        {
            get { return bucketAnalyticsCheckBox.Checked; }
            set { bucketAnalyticsCheckBox.Checked = value; }
        }

        public bool BucketAnalyticsCheckboxEnabled
        {
            set { bucketAnalyticsCheckBox.Enabled = value; }
        }

        public string BucketAnalyticsSetupUrl
        {
            set { bucketAnalyticsSetupUrlLinkLabel.Text = value; }
        }

        public bool BucketAnalyticsSetupUrlEnabled
        {
            set { bucketAnalyticsSetupUrlLinkLabel.Enabled = value; }
        }

        public bool BucketVersioning
        {
            get { return bucketVersioningCheckBox.Checked; }
            set { bucketVersioningCheckBox.Checked = value; }
        }

        public bool BucketVersioningEnabled
        {
            set { bucketVersioningCheckBox.Enabled = value; }
        }

        public bool BucketMfa
        {
            set { bucketMfaCheckBox.Checked = value; }
            get { return bucketMfaCheckBox.Checked; }
        }

        public bool BucketMfaEnabled
        {
            set { bucketMfaCheckBox.Enabled = value; }
        }

        public bool LifecycleTransitionCheckbox
        {
            get { return lifecycleTransitionCheckBox.Checked; }
            set { lifecycleTransitionCheckBox.Checked = value; }
        }

        public event VoidHandler BucketLoggingCheckboxChanged = delegate { };
        public event VoidHandler BucketLoggingPopupChanged = delegate { };
        public event VoidHandler EncryptionChanged = delegate { };
        public event VoidHandler StorageClassChanged = delegate { };
        public event VoidHandler BucketVersioningChanged = delegate { };
        public event VoidHandler BucketMfaChanged = delegate { };
        public event VoidHandler LifecycleTransitionCheckboxChanged = delegate { };
        public event VoidHandler LifecycleTransitionPopupChanged = delegate { };
        public event VoidHandler LifecycleDeleteCheckboxChanged = delegate { };
        public event VoidHandler LifecycleDeletePopupChanged = delegate { };
        public event VoidHandler ActiveTabChanged = delegate { };
        public event VoidHandler BucketAnalyticsCheckboxChanged = delegate { };
        public event VoidHandler DistributionAnalyticsCheckboxChanged = delegate { };

        public bool DistributionAnalyticsCheckbox
        {
            get { return distributionAnalyticsCheckBox.Checked; }
            set { distributionAnalyticsCheckBox.Checked = value; }
        }

        public bool DistributionAnalyticsCheckboxEnabled
        {
            set { distributionAnalyticsCheckBox.Enabled = value; }
        }

        public string DistributionAnalyticsSetupUrl
        {
            set { distributionAnalyticsSetupUrlLinkLabel.Text = value; }
        }

        public bool DistributionAnalyticsSetupUrlEnabled
        {
            set { distributionAnalyticsSetupUrlLinkLabel.Enabled = value; }
        }

        public bool MetadataTableEnabled
        {
            set { metadataDataGridView.Enabled = value; }
        }

        public bool MetadataAddEnabled
        {
            set { addHeaderButton.Enabled = value; }
        }

        public bool MetadataRemoveEnabled
        {
            set { addMetadataContextMenuStrip.Items[addMetadataContextMenuStrip.Items.Count - 1].Enabled = value; }
        }

        public bool MetadataAnimationActive
        {
            set { metadataAnimation.Visible = value; }
        }

        public BindingList<InfoController.CustomHeaderEntry> MetadataDataSource
        {
            set { metadataDataGridView.DataSource = value; }
        }

        public void PopulateMetadata(IDictionary<string, AsyncController.SyncDelegate> metadata)
        {
            addMetadataContextMenuStrip.Items.Clear();
            ToolStripItem[] items = new ToolStripItem[metadata.Count + 2];

            int i = 0;
            foreach (KeyValuePair<string, AsyncController.SyncDelegate> header in metadata)
            {
                ToolStripMenuItem item = new ToolStripMenuItem(header.Key);
                KeyValuePair<string, AsyncController.SyncDelegate> header1 = header;
                item.Click += delegate { header1.Value(); };
                items[i] = item;
                i++;

                if (1 == i || (metadata.Count) == i)
                {
                    items[i++] = new ToolStripSeparator();
                }
            }
            addMetadataContextMenuStrip.Items.AddRange(items);
        }

        public List<InfoController.CustomHeaderEntry> SelectedMetadataEntries
        {
            get
            {
                List<InfoController.CustomHeaderEntry> selected = new List<InfoController.CustomHeaderEntry>();
                DataGridViewSelectedRowCollection rows = metadataDataGridView.SelectedRows;
                foreach (DataGridViewRow row in rows)
                {
                    selected.Add((InfoController.CustomHeaderEntry) row.DataBoundItem);
                }
                return selected;
            }
        }

        public event VoidHandler DistributionDefaultRootChanged = delegate { };

        private void InitMetadataGrid()
        {
            metadataDataGridView.SelectionMode = DataGridViewSelectionMode.FullRowSelect;
            metadataDataGridView.MultiSelect = true;
            metadataDataGridView.RowHeadersVisible = false;
            metadataDataGridView.AutoGenerateColumns = false;
            metadataDataGridView.AutoSizeColumnsMode = DataGridViewAutoSizeColumnsMode.Fill;
            metadataDataGridView.AutoSizeRowsMode = DataGridViewAutoSizeRowsMode.AllCells;

            DataGridViewTextBoxColumn nameColumn = new DataGridViewTextBoxColumn();
            nameColumn.HeaderText = "Name";
            nameColumn.DataPropertyName = MetadataColumName.Name.ToString();
            nameColumn.Name = MetadataColumName.Name.ToString();

            DataGridViewTextBoxColumn valueColumn = new DataGridViewTextBoxColumn();
            valueColumn.DataPropertyName = MetadataColumName.Value.ToString();
            valueColumn.HeaderText = "Value";
            valueColumn.Name = MetadataColumName.Value.ToString();

            metadataDataGridView.Columns.Add(nameColumn);
            metadataDataGridView.Columns.Add(valueColumn);
        }

        private void HandleHelpRequest()
        {
            ManagedPanel selected = panelManager.SelectedPanel;
            if (selected == managedGeneralPanel)
            {
                ShowHelp(this, new InfoHelpArgs(InfoHelpArgs.Context.General));
            }
            if (selected == managedPermissionsPanel)
            {
                ShowHelp(this, new InfoHelpArgs(InfoHelpArgs.Context.Permissions));
            }
            if (selected == managedMetadataPanel)
            {
                ShowHelp(this, new InfoHelpArgs(InfoHelpArgs.Context.Metdadata));
            }
            if (selected == managedDistributionPanel)
            {
                ShowHelp(this, new InfoHelpArgs(InfoHelpArgs.Context.Cdn));
            }
            if (selected == managedS3Panel)
            {
                ShowHelp(this, new InfoHelpArgs(InfoHelpArgs.Context.S3));
            }
        }

        private void ConfigureHelp()
        {
            HelpButtonClicked += (sender, args) => HandleHelpRequest();
            HelpRequested += (sender, hlpevent) => HandleHelpRequest();
        }

        private void InitAclGrid()
        {
            aclDataGridView.SelectionMode = DataGridViewSelectionMode.FullRowSelect;
            aclDataGridView.MultiSelect = true;
            aclDataGridView.RowHeadersVisible = false;
            aclDataGridView.AutoGenerateColumns = false;
            aclDataGridView.AutoSizeColumnsMode = DataGridViewAutoSizeColumnsMode.Fill;
            aclDataGridView.AutoSizeRowsMode = DataGridViewAutoSizeRowsMode.AllCells;

            aclDataGridView.CellFormatting += delegate(object sender, DataGridViewCellFormattingEventArgs args)
            {
                if (null != aclDataGridView.DataSource)
                {
                    if (aclDataGridView.IsCurrentCellInEditMode || args.ColumnIndex != 0)
                    {
                        return;
                    }
                    if (String.IsNullOrEmpty(args.Value as String))
                    {
                        args.Value =
                            ((InfoController.UserAndRoleEntry) aclDataGridView.Rows[args.RowIndex].DataBoundItem)
                                .getUser().getPlaceholder();
                        args.CellStyle.ForeColor = Color.Gray;
                        args.CellStyle.Font = new Font(args.CellStyle.Font, FontStyle.Italic);
                    }
                }
            };


            //make combobox directly editable without multiple clicks
            aclDataGridView.CellClick += delegate(object o, DataGridViewCellEventArgs a)
            {
                if (a.RowIndex < 0)
                {
                    return; // Header
                }
                if (a.ColumnIndex != 1)
                {
                    return; // Filter out other columns
                }

                aclDataGridView.BeginEdit(true);
                ComboBox comboBox = (ComboBox) aclDataGridView.EditingControl;
                comboBox.DroppedDown = true;
            };

            aclDataGridView.CellBeginEdit += delegate(object sender, DataGridViewCellCancelEventArgs args)
            {
                if (args.ColumnIndex == 0)
                {
                    args.Cancel =
                        !((InfoController.UserAndRoleEntry) aclDataGridView.Rows[args.RowIndex].DataBoundItem).getUser()
                            .isEditable();
                }
            };

            DataGridViewTextBoxColumn userColumn = new DataGridViewTextBoxColumn();
            userColumn.HeaderText = "Grantee";
            userColumn.DataPropertyName = AclColumnName.User.ToString();
            userColumn.DefaultCellStyle.NullValue = String.Empty;
            userColumn.Name = AclColumnName.User.ToString();

            DataGridViewComboBoxColumn rolesColumn = new DataGridViewComboBoxColumn();
            rolesColumn.DisplayStyle = DataGridViewComboBoxDisplayStyle.Nothing;
            rolesColumn.DataPropertyName = AclColumnName.Role.ToString();
            rolesColumn.HeaderText = "Permission";
            rolesColumn.Name = AclColumnName.Role.ToString();

            aclDataGridView.Columns.Add(userColumn);
            aclDataGridView.Columns.Add(rolesColumn);
        }

        protected void ResizeForm(object sender, EventArgs e)
        {
            int size = 0;
            foreach (ToolStripItem item in toolStrip.Items)
            {
                size += item.GetPreferredSize(Size.Empty).Width + item.Margin.Left + item.Margin.Right;
            }
            // add border
            size += 20;
            if (size > Width)
            {
                Width = size;
            }
        }

        private void DisableAll()
        {
            foreach (var item in toolStrip.Items)
            {
                if (item is ToolStripButton)
                {
                    (item as ToolStripButton).Checked = false;
                }
            }
        }

        private void generalButton_Click(object sender, EventArgs e)
        {
            if (!generalButton.Checked)
            {
                DisableAll();
                generalButton.Checked = true;
                panelManager.SelectedPanel = managedGeneralPanel;
            }
        }

        private void permissionsButton_Click(object sender, EventArgs e)
        {
            if (!permissionsButton.Checked)
            {
                DisableAll();
                permissionsButton.Checked = true;
                panelManager.SelectedPanel = managedPermissionsPanel;
            }
        }

        private void octalTextBox_Validated(object sender, EventArgs e)
        {
            OctalPermissionsChanged();
        }

        private void ownerrCheckBox_CheckStateChanged(object sender, EventArgs e)
        {
            OwnerReadChanged();
        }

        private void ownerwCheckBox_CheckStateChanged(object sender, EventArgs e)
        {
            OwnerWriteChanged();
        }

        private void ownerxCheckBox_CheckStateChanged(object sender, EventArgs e)
        {
            OwnerExecuteChanged();
        }

        private void grouprCheckbox_CheckStateChanged(object sender, EventArgs e)
        {
            GroupReadChanged();
        }

        private void groupwCheckbox_CheckStateChanged(object sender, EventArgs e)
        {
            GroupWriteChanged();
        }

        private void groupxCheckbox_CheckStateChanged(object sender, EventArgs e)
        {
            GroupExecuteChanged();
        }

        private void otherrCheckbox_CheckStateChanged(object sender, EventArgs e)
        {
            OtherReadChanged();
        }

        private void otherwCheckbox_CheckStateChanged(object sender, EventArgs e)
        {
            OtherWriteChanged();
        }

        private void otherxCheckbox_CheckStateChanged(object sender, EventArgs e)
        {
            OtherExecuteChanged();
        }

        private void s3Button_Click(object sender, EventArgs e)
        {
            if (!s3Button.Checked)
            {
                DisableAll();
                s3Button.Checked = true;
                panelManager.SelectedPanel = managedS3Panel;
                //ResizeForm(s3LayoutPanel, true);
            }
        }

        private void distributionButton_Click(object sender, EventArgs e)
        {
            if (!distributionButton.Checked)
            {
                DisableAll();
                distributionButton.Checked = true;
                panelManager.SelectedPanel = managedDistributionPanel;
                //ResizeForm(distributionLayoutPanel, true);
            }
        }

        private void deliveryMethodComboBox_SelectionChangeCommitted(object sender, EventArgs e)
        {
            DistributionDeliveryMethodChanged();
        }

        private void distributionEnableCheckBox_CheckedChanged(object sender, EventArgs e)
        {
            DistributionEnabledChanged();
        }

        private void distributionLoggingCheckBox_CheckedChanged(object sender, EventArgs e)
        {
            DistributionLoggingCheckboxChanged();
        }

        private void bucketLoggingCheckBox_CheckedChanged(object sender, EventArgs e)
        {
            BucketLoggingCheckboxChanged();
        }

        private void filenameTextbox_Validated(object sender, EventArgs e)
        {
            FilenameChanged();
        }

        private void calculateButton_Click(object sender, EventArgs e)
        {
            CalculateSize();
        }

        private void applyRecursivePermissionsButton_Click(object sender, EventArgs e)
        {
            ApplyRecursivePermissions();
        }

        private void metadataButton_Click(object sender, EventArgs e)
        {
            if (!metadataButton.Checked)
            {
                DisableAll();
                metadataButton.Checked = true;
                panelManager.SelectedPanel = managedMetadataPanel;
            }
        }

        private void defaultRootComboBox_SelectionChangeCommitted(object sender, EventArgs e)
        {
            DistributionDefaultRootChanged();
        }

        private void storageClassComboBox_SelectionChangeCommitted(object sender, EventArgs e)
        {
            StorageClassChanged();
        }

        private void bucketVersioningCheckBox_CheckedChanged(object sender, EventArgs e)
        {
            BucketVersioningChanged();
        }

        private void bucketMfaCheckBox_CheckedChanged(object sender, EventArgs e)
        {
            BucketMfaChanged();
        }

        private void addAclContextMenuStrip_Opening(object sender, CancelEventArgs e)
        {
            addAclContextMenuStrip.Items[addAclContextMenuStrip.Items.Count - 1].Enabled =
                aclDataGridView.SelectedRows.Count > 0;
        }

        private void addMetadataContextMenuStrip_Opening(object sender, CancelEventArgs e)
        {
            addMetadataContextMenuStrip.Items[addMetadataContextMenuStrip.Items.Count - 1].Enabled =
                metadataDataGridView.SelectedRows.Count > 0;
        }

        private void distributionCnameTextBox_Validated(object sender, EventArgs e)
        {
            if (!distributionCnameTextBox.Text.Equals(lastCname))
            {
                DistributionCnameChanged();
            }
        }

        private void invalidateButton_Click(object sender, EventArgs e)
        {
            DistributionInvalidateObjects();
        }

        private void bucketLoggingComboBox_SelectionChangeCommitted(object sender, EventArgs e)
        {
            BucketLoggingPopupChanged();
        }

        private void distributionLoggingComboBox_SelectionChangeCommitted(object sender, EventArgs e)
        {
            DistributionLoggingPopupChanged();
        }

        private void encryptionCheckBox_CheckedChanged(object sender, EventArgs e)
        {
            EncryptionChanged();
        }

        private void bucketAnalyticsCheckBox_CheckedChanged(object sender, EventArgs e)
        {
            BucketAnalyticsCheckboxChanged();
        }

        private void distributionAnalyticsCheckBox_CheckedChanged(object sender, EventArgs e)
        {
            DistributionAnalyticsCheckboxChanged();
        }

        private void lifecycleTransitionCheckBox_CheckedChanged(object sender, EventArgs e)
        {
            LifecycleTransitionCheckboxChanged();
        }

        private void lifecycleTransitionComboBox_SelectionChangeCommitted(object sender, EventArgs e)
        {
            LifecycleTransitionPopupChanged();
        }

        private void lifecycleDeleteCheckBox_CheckedChanged(object sender, EventArgs e)
        {
            LifecycleDeleteCheckboxChanged();
        }

        private void lifecycleDeleteComboBox_SelectionChangeCommitted(object sender, EventArgs e)
        {
            LifecycleDeletePopupChanged();
        }

        private void transferAccelerationCheckBox_CheckedChanged(object sender, EventArgs e)
        {
            TransferAccelerationCheckboxChanged();
        }

        private enum AclColumnName
        {
            User,
            Role
        }

        private enum MetadataColumName
        {
            Name,
            Value
        }
    }
}