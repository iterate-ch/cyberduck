using Ch.Cyberduck.Ui.Winforms.Controls;

namespace Ch.Cyberduck.Ui.Winforms
{
    partial class InfoForm
    {
        /// <summary>
        /// Required designer variable.
        /// </summary>
        private System.ComponentModel.IContainer components = null;

        /// <summary>
        /// Clean up any resources being used.
        /// </summary>
        /// <param name="disposing">true if managed resources should be disposed; otherwise, false.</param>
        protected override void Dispose(bool disposing)
        {
            if (disposing && (components != null))
            {
                components.Dispose();
            }
            base.Dispose(disposing);
        }

        #region Windows Form Designer generated code

        /// <summary>
        /// Required method for Designer support - do not modify
        /// the contents of this method with the code editor.
        /// </summary>
        private void InitializeComponent()
        {
            this.components = new System.ComponentModel.Container();
            this.toolStrip = new Ch.Cyberduck.Ui.Winforms.Controls.ClickThroughToolStrip();
            this.generalButton = new System.Windows.Forms.ToolStripButton();
            this.permissionsButton = new System.Windows.Forms.ToolStripButton();
            this.metadataButton = new System.Windows.Forms.ToolStripButton();
            this.distributionButton = new System.Windows.Forms.ToolStripButton();
            this.s3Button = new System.Windows.Forms.ToolStripButton();
            this.distributionLayoutPanel = new System.Windows.Forms.TableLayoutPanel();
            this.label14 = new System.Windows.Forms.Label();
            this.label15 = new System.Windows.Forms.Label();
            this.label16 = new System.Windows.Forms.Label();
            this.label17 = new System.Windows.Forms.Label();
            this.deliveryMethodComboBox = new System.Windows.Forms.ComboBox();
            this.distributionEnableCheckBox = new System.Windows.Forms.CheckBox();
            this.distributionLoggingCheckBox = new System.Windows.Forms.CheckBox();
            this.statusLabel = new System.Windows.Forms.Label();
            this.distributionCnameTextBox = new System.Windows.Forms.TextBox();
            this.whereLinkLabel = new Ch.Cyberduck.Ui.Winforms.Controls.ClickLinkLabel();
            this.distributionAnimation = new System.Windows.Forms.PictureBox();
            this.cnameUrlLinkLabel = new Ch.Cyberduck.Ui.Winforms.Controls.ClickLinkLabel();
            this.label25 = new System.Windows.Forms.Label();
            this.defaultRootComboBox = new System.Windows.Forms.ComboBox();
            this.label13 = new System.Windows.Forms.Label();
            this.invalidationStatus = new System.Windows.Forms.Label();
            this.label24 = new System.Windows.Forms.Label();
            this.invalidateButton = new System.Windows.Forms.Button();
            this.label27 = new System.Windows.Forms.Label();
            this.originLinkLabel = new Ch.Cyberduck.Ui.Winforms.Controls.ClickLinkLabel();
            this.label28 = new System.Windows.Forms.Label();
            this.label29 = new System.Windows.Forms.Label();
            this.distributionLoggingComboBox = new System.Windows.Forms.ComboBox();
            this.label37 = new System.Windows.Forms.Label();
            this.distributionAnalyticsCheckBox = new System.Windows.Forms.CheckBox();
            this.distributionAnalyticsSetupUrlLinkLabel = new System.Windows.Forms.LinkLabel();
            this.generalLayoutPanel = new System.Windows.Forms.TableLayoutPanel();
            this.icon = new System.Windows.Forms.PictureBox();
            this.filenameTextbox = new System.Windows.Forms.TextBox();
            this.label1 = new System.Windows.Forms.Label();
            this.label2 = new System.Windows.Forms.Label();
            this.label3 = new System.Windows.Forms.Label();
            this.label4 = new System.Windows.Forms.Label();
            this.label5 = new System.Windows.Forms.Label();
            this.label6 = new System.Windows.Forms.Label();
            this.label7 = new System.Windows.Forms.Label();
            this.label8 = new System.Windows.Forms.Label();
            this.label9 = new System.Windows.Forms.Label();
            this.sizeLabel = new System.Windows.Forms.Label();
            this.pathLabel = new System.Windows.Forms.Label();
            this.weburlLabel = new Ch.Cyberduck.Ui.Winforms.Controls.ClickLinkLabel();
            this.kindLabel = new System.Windows.Forms.Label();
            this.permissionsLabel = new System.Windows.Forms.Label();
            this.ownerLabel = new System.Windows.Forms.Label();
            this.groupLabel = new System.Windows.Forms.Label();
            this.modifiedLabel = new System.Windows.Forms.Label();
            this.calculateButton = new System.Windows.Forms.Button();
            this.sizeAnimation = new System.Windows.Forms.PictureBox();
            this.label22 = new System.Windows.Forms.Label();
            this.createdLabel = new System.Windows.Forms.Label();
            this.checksumTextBox = new System.Windows.Forms.TextBox();
            this.s3LayoutPanel = new System.Windows.Forms.TableLayoutPanel();
            this.s3Animation = new System.Windows.Forms.PictureBox();
            this.label18 = new System.Windows.Forms.Label();
            this.label19 = new System.Windows.Forms.Label();
            this.label20 = new System.Windows.Forms.Label();
            this.bucketLocationLabel = new System.Windows.Forms.Label();
            this.s3PublicUrlLinkLabel = new Ch.Cyberduck.Ui.Winforms.Controls.ClickLinkLabel();
            this.s3TorrentUrlLinkLabel = new Ch.Cyberduck.Ui.Winforms.Controls.ClickLinkLabel();
            this.label26 = new System.Windows.Forms.Label();
            this.s3PublicUrlValidityLabel = new System.Windows.Forms.Label();
            this.storageClassComboBox = new System.Windows.Forms.ComboBox();
            this.bucketLoggingCheckBox = new System.Windows.Forms.CheckBox();
            this.bucketVersioningCheckBox = new System.Windows.Forms.CheckBox();
            this.label21 = new System.Windows.Forms.Label();
            this.bucketMfaCheckBox = new System.Windows.Forms.CheckBox();
            this.label31 = new System.Windows.Forms.Label();
            this.label32 = new System.Windows.Forms.Label();
            this.label33 = new System.Windows.Forms.Label();
            this.bucketLoggingComboBox = new System.Windows.Forms.ComboBox();
            this.label34 = new System.Windows.Forms.Label();
            this.label35 = new System.Windows.Forms.Label();
            this.bucketAnalyticsCheckBox = new System.Windows.Forms.CheckBox();
            this.bucketAnalyticsSetupUrlLinkLabel = new Ch.Cyberduck.Ui.Winforms.Controls.ClickLinkLabel();
            this.label36 = new System.Windows.Forms.Label();
            this.lifecycleTransitionCheckBox = new System.Windows.Forms.CheckBox();
            this.label38 = new System.Windows.Forms.Label();
            this.lifecycleTransitionComboBox = new System.Windows.Forms.ComboBox();
            this.lifecycleDeleteCheckBox = new System.Windows.Forms.CheckBox();
            this.lifecycleDeleteComboBox = new System.Windows.Forms.ComboBox();
            this.toolTip = new System.Windows.Forms.ToolTip(this.components);
            this.panelManager = new Ch.Cyberduck.Ui.Winforms.Controls.PanelManager();
            this.managedDistributionPanel = new Ch.Cyberduck.Ui.Winforms.Controls.ManagedPanel();
            this.managedGeneralPanel = new Ch.Cyberduck.Ui.Winforms.Controls.ManagedPanel();
            this.managedMetadataPanel = new Ch.Cyberduck.Ui.Winforms.Controls.ManagedPanel();
            this.metadataTableLayoutPanel = new System.Windows.Forms.TableLayoutPanel();
            this.label30 = new System.Windows.Forms.Label();
            this.metadataDataGridView = new System.Windows.Forms.DataGridView();
            this.addHeaderButton = new Ch.Cyberduck.Ui.Winforms.Controls.SplitButton();
            this.addMetadataContextMenuStrip = new System.Windows.Forms.ContextMenuStrip(this.components);
            this.toolStripMenuItem1 = new System.Windows.Forms.ToolStripMenuItem();
            this.toolStripMenuItem2 = new System.Windows.Forms.ToolStripMenuItem();
            this.toolStripSeparator2 = new System.Windows.Forms.ToolStripSeparator();
            this.toolStripMenuItem3 = new System.Windows.Forms.ToolStripMenuItem();
            this.metadataAnimation = new System.Windows.Forms.PictureBox();
            this.managedPermissionsPanel = new Ch.Cyberduck.Ui.Winforms.Controls.ManagedPanel();
            this.panelManagerPermissions = new Ch.Cyberduck.Ui.Winforms.Controls.PanelManager();
            this.nonCloudManagedPanel = new Ch.Cyberduck.Ui.Winforms.Controls.ManagedPanel();
            this.permissionsLayoutPanel = new System.Windows.Forms.TableLayoutPanel();
            this.label10 = new System.Windows.Forms.Label();
            this.ownerrCheckBox = new System.Windows.Forms.CheckBox();
            this.ownerwCheckBox = new System.Windows.Forms.CheckBox();
            this.ownerxCheckBox = new System.Windows.Forms.CheckBox();
            this.label11 = new System.Windows.Forms.Label();
            this.grouprCheckbox = new System.Windows.Forms.CheckBox();
            this.groupwCheckbox = new System.Windows.Forms.CheckBox();
            this.groupxCheckbox = new System.Windows.Forms.CheckBox();
            this.label12 = new System.Windows.Forms.Label();
            this.otherwCheckbox = new System.Windows.Forms.CheckBox();
            this.otherxCheckbox = new System.Windows.Forms.CheckBox();
            this.permissionAnimation = new System.Windows.Forms.PictureBox();
            this.otherrCheckbox = new System.Windows.Forms.CheckBox();
            this.applyRecursivePermissionsButton = new System.Windows.Forms.Button();
            this.label23 = new System.Windows.Forms.Label();
            this.octalTextBox = new System.Windows.Forms.TextBox();
            this.cloudManagedPanel = new Ch.Cyberduck.Ui.Winforms.Controls.ManagedPanel();
            this.tableLayoutPanel1 = new System.Windows.Forms.TableLayoutPanel();
            this.AclLabel = new System.Windows.Forms.Label();
            this.aclDataGridView = new System.Windows.Forms.DataGridView();
            this.addAclButton = new Ch.Cyberduck.Ui.Winforms.Controls.SplitButton();
            this.addAclContextMenuStrip = new System.Windows.Forms.ContextMenuStrip(this.components);
            this.yvesOwnerToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.canoncalUserIDToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.toolStripSeparator1 = new System.Windows.Forms.ToolStripSeparator();
            this.removeToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.authenticatedLabel = new System.Windows.Forms.Label();
            this.authenticatedUrlLinkLabel = new Ch.Cyberduck.Ui.Winforms.Controls.ClickLinkLabel();
            this.aclAnimation = new System.Windows.Forms.PictureBox();
            this.managedS3Panel = new Ch.Cyberduck.Ui.Winforms.Controls.ManagedPanel();
            this.encryptionComboBox = new System.Windows.Forms.ComboBox();
            this.toolStrip.SuspendLayout();
            this.distributionLayoutPanel.SuspendLayout();
            ((System.ComponentModel.ISupportInitialize)(this.distributionAnimation)).BeginInit();
            this.generalLayoutPanel.SuspendLayout();
            ((System.ComponentModel.ISupportInitialize)(this.icon)).BeginInit();
            ((System.ComponentModel.ISupportInitialize)(this.sizeAnimation)).BeginInit();
            this.s3LayoutPanel.SuspendLayout();
            ((System.ComponentModel.ISupportInitialize)(this.s3Animation)).BeginInit();
            this.panelManager.SuspendLayout();
            this.managedDistributionPanel.SuspendLayout();
            this.managedGeneralPanel.SuspendLayout();
            this.managedMetadataPanel.SuspendLayout();
            this.metadataTableLayoutPanel.SuspendLayout();
            ((System.ComponentModel.ISupportInitialize)(this.metadataDataGridView)).BeginInit();
            this.addMetadataContextMenuStrip.SuspendLayout();
            ((System.ComponentModel.ISupportInitialize)(this.metadataAnimation)).BeginInit();
            this.managedPermissionsPanel.SuspendLayout();
            this.panelManagerPermissions.SuspendLayout();
            this.nonCloudManagedPanel.SuspendLayout();
            this.permissionsLayoutPanel.SuspendLayout();
            ((System.ComponentModel.ISupportInitialize)(this.permissionAnimation)).BeginInit();
            this.cloudManagedPanel.SuspendLayout();
            this.tableLayoutPanel1.SuspendLayout();
            ((System.ComponentModel.ISupportInitialize)(this.aclDataGridView)).BeginInit();
            this.addAclContextMenuStrip.SuspendLayout();
            ((System.ComponentModel.ISupportInitialize)(this.aclAnimation)).BeginInit();
            this.managedS3Panel.SuspendLayout();
            this.SuspendLayout();
            // 
            // toolStrip
            // 
            this.toolStrip.GripStyle = System.Windows.Forms.ToolStripGripStyle.Hidden;
            this.toolStrip.ImageScalingSize = new System.Drawing.Size(32, 32);
            this.toolStrip.Items.AddRange(new System.Windows.Forms.ToolStripItem[] {
            this.generalButton,
            this.permissionsButton,
            this.metadataButton,
            this.distributionButton,
            this.s3Button});
            this.toolStrip.Location = new System.Drawing.Point(0, 0);
            this.toolStrip.MinimumSize = new System.Drawing.Size(0, 56);
            this.toolStrip.Name = "toolStrip";
            this.toolStrip.Size = new System.Drawing.Size(500, 56);
            this.toolStrip.TabIndex = 0;
            this.toolStrip.Text = "toolStrip1";
            // 
            // generalButton
            // 
            this.generalButton.Image = global::Ch.Cyberduck.ResourcesBundle.info;
            this.generalButton.ImageAlign = System.Drawing.ContentAlignment.BottomCenter;
            this.generalButton.ImageTransparentColor = System.Drawing.Color.Magenta;
            this.generalButton.Margin = new System.Windows.Forms.Padding(5, 0, 5, 0);
            this.generalButton.Name = "generalButton";
            this.generalButton.Size = new System.Drawing.Size(51, 56);
            this.generalButton.Text = "General";
            this.generalButton.TextImageRelation = System.Windows.Forms.TextImageRelation.ImageAboveText;
            this.generalButton.Click += new System.EventHandler(this.generalButton_Click);
            // 
            // permissionsButton
            // 
            this.permissionsButton.Image = global::Ch.Cyberduck.ResourcesBundle.permissions;
            this.permissionsButton.ImageAlign = System.Drawing.ContentAlignment.BottomCenter;
            this.permissionsButton.ImageTransparentColor = System.Drawing.Color.Magenta;
            this.permissionsButton.Margin = new System.Windows.Forms.Padding(5, 0, 5, 0);
            this.permissionsButton.Name = "permissionsButton";
            this.permissionsButton.Size = new System.Drawing.Size(74, 56);
            this.permissionsButton.Text = "Permissions";
            this.permissionsButton.TextImageRelation = System.Windows.Forms.TextImageRelation.ImageAboveText;
            this.permissionsButton.Click += new System.EventHandler(this.permissionsButton_Click);
            // 
            // metadataButton
            // 
            this.metadataButton.Image = global::Ch.Cyberduck.ResourcesBundle.pencil;
            this.metadataButton.ImageAlign = System.Drawing.ContentAlignment.BottomCenter;
            this.metadataButton.ImageTransparentColor = System.Drawing.Color.Magenta;
            this.metadataButton.Margin = new System.Windows.Forms.Padding(5, 0, 5, 0);
            this.metadataButton.Name = "metadataButton";
            this.metadataButton.Size = new System.Drawing.Size(61, 56);
            this.metadataButton.Text = "Metadata";
            this.metadataButton.TextImageRelation = System.Windows.Forms.TextImageRelation.ImageAboveText;
            this.metadataButton.Click += new System.EventHandler(this.metadataButton_Click);
            // 
            // distributionButton
            // 
            this.distributionButton.ImageAlign = System.Drawing.ContentAlignment.BottomCenter;
            this.distributionButton.ImageTransparentColor = System.Drawing.Color.Magenta;
            this.distributionButton.Margin = new System.Windows.Forms.Padding(5, 0, 5, 0);
            this.distributionButton.Name = "distributionButton";
            this.distributionButton.Size = new System.Drawing.Size(109, 56);
            this.distributionButton.Text = "Distribution (CDN)";
            this.distributionButton.TextImageRelation = System.Windows.Forms.TextImageRelation.ImageAboveText;
            this.distributionButton.Click += new System.EventHandler(this.distributionButton_Click);
            // 
            // s3Button
            // 
            this.s3Button.ImageAlign = System.Drawing.ContentAlignment.BottomCenter;
            this.s3Button.ImageTransparentColor = System.Drawing.Color.Magenta;
            this.s3Button.Margin = new System.Windows.Forms.Padding(5, 0, 5, 0);
            this.s3Button.Name = "s3Button";
            this.s3Button.Size = new System.Drawing.Size(23, 56);
            this.s3Button.Text = "S3";
            this.s3Button.TextImageRelation = System.Windows.Forms.TextImageRelation.ImageAboveText;
            this.s3Button.Click += new System.EventHandler(this.s3Button_Click);
            // 
            // distributionLayoutPanel
            // 
            this.distributionLayoutPanel.AutoSize = true;
            this.distributionLayoutPanel.ColumnCount = 3;
            this.distributionLayoutPanel.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle());
            this.distributionLayoutPanel.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle(System.Windows.Forms.SizeType.Percent, 100F));
            this.distributionLayoutPanel.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle());
            this.distributionLayoutPanel.Controls.Add(this.label14, 0, 0);
            this.distributionLayoutPanel.Controls.Add(this.label15, 0, 3);
            this.distributionLayoutPanel.Controls.Add(this.label16, 0, 15);
            this.distributionLayoutPanel.Controls.Add(this.label17, 0, 17);
            this.distributionLayoutPanel.Controls.Add(this.deliveryMethodComboBox, 1, 0);
            this.distributionLayoutPanel.Controls.Add(this.distributionEnableCheckBox, 1, 1);
            this.distributionLayoutPanel.Controls.Add(this.distributionLoggingCheckBox, 1, 4);
            this.distributionLayoutPanel.Controls.Add(this.statusLabel, 1, 3);
            this.distributionLayoutPanel.Controls.Add(this.distributionCnameTextBox, 1, 17);
            this.distributionLayoutPanel.Controls.Add(this.whereLinkLabel, 1, 15);
            this.distributionLayoutPanel.Controls.Add(this.distributionAnimation, 2, 1);
            this.distributionLayoutPanel.Controls.Add(this.cnameUrlLinkLabel, 1, 18);
            this.distributionLayoutPanel.Controls.Add(this.label25, 0, 20);
            this.distributionLayoutPanel.Controls.Add(this.defaultRootComboBox, 1, 20);
            this.distributionLayoutPanel.Controls.Add(this.label13, 0, 21);
            this.distributionLayoutPanel.Controls.Add(this.invalidationStatus, 1, 21);
            this.distributionLayoutPanel.Controls.Add(this.label24, 1, 22);
            this.distributionLayoutPanel.Controls.Add(this.invalidateButton, 1, 23);
            this.distributionLayoutPanel.Controls.Add(this.label27, 0, 13);
            this.distributionLayoutPanel.Controls.Add(this.originLinkLabel, 1, 13);
            this.distributionLayoutPanel.Controls.Add(this.label28, 0, 4);
            this.distributionLayoutPanel.Controls.Add(this.label29, 1, 5);
            this.distributionLayoutPanel.Controls.Add(this.distributionLoggingComboBox, 1, 6);
            this.distributionLayoutPanel.Controls.Add(this.label37, 0, 8);
            this.distributionLayoutPanel.Controls.Add(this.distributionAnalyticsCheckBox, 1, 8);
            this.distributionLayoutPanel.Controls.Add(this.distributionAnalyticsSetupUrlLinkLabel, 1, 9);
            this.distributionLayoutPanel.Dock = System.Windows.Forms.DockStyle.Fill;
            this.distributionLayoutPanel.Location = new System.Drawing.Point(0, 0);
            this.distributionLayoutPanel.Name = "distributionLayoutPanel";
            this.distributionLayoutPanel.Padding = new System.Windows.Forms.Padding(10);
            this.distributionLayoutPanel.RowCount = 24;
            this.distributionLayoutPanel.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.distributionLayoutPanel.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.distributionLayoutPanel.RowStyles.Add(new System.Windows.Forms.RowStyle(System.Windows.Forms.SizeType.Absolute, 10F));
            this.distributionLayoutPanel.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.distributionLayoutPanel.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.distributionLayoutPanel.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.distributionLayoutPanel.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.distributionLayoutPanel.RowStyles.Add(new System.Windows.Forms.RowStyle(System.Windows.Forms.SizeType.Absolute, 20F));
            this.distributionLayoutPanel.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.distributionLayoutPanel.RowStyles.Add(new System.Windows.Forms.RowStyle(System.Windows.Forms.SizeType.Absolute, 20F));
            this.distributionLayoutPanel.RowStyles.Add(new System.Windows.Forms.RowStyle(System.Windows.Forms.SizeType.Absolute, 20F));
            this.distributionLayoutPanel.RowStyles.Add(new System.Windows.Forms.RowStyle(System.Windows.Forms.SizeType.Absolute, 20F));
            this.distributionLayoutPanel.RowStyles.Add(new System.Windows.Forms.RowStyle(System.Windows.Forms.SizeType.Absolute, 10F));
            this.distributionLayoutPanel.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.distributionLayoutPanel.RowStyles.Add(new System.Windows.Forms.RowStyle(System.Windows.Forms.SizeType.Absolute, 10F));
            this.distributionLayoutPanel.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.distributionLayoutPanel.RowStyles.Add(new System.Windows.Forms.RowStyle(System.Windows.Forms.SizeType.Absolute, 10F));
            this.distributionLayoutPanel.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.distributionLayoutPanel.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.distributionLayoutPanel.RowStyles.Add(new System.Windows.Forms.RowStyle(System.Windows.Forms.SizeType.Absolute, 10F));
            this.distributionLayoutPanel.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.distributionLayoutPanel.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.distributionLayoutPanel.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.distributionLayoutPanel.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.distributionLayoutPanel.Size = new System.Drawing.Size(0, 0);
            this.distributionLayoutPanel.TabIndex = 0;
            // 
            // label14
            // 
            this.label14.Anchor = System.Windows.Forms.AnchorStyles.Left;
            this.label14.AutoSize = true;
            this.label14.Location = new System.Drawing.Point(13, 17);
            this.label14.Name = "label14";
            this.label14.Size = new System.Drawing.Size(94, 15);
            this.label14.TabIndex = 0;
            this.label14.Text = "Delivery Method";
            this.label14.TextAlign = System.Drawing.ContentAlignment.MiddleLeft;
            // 
            // label15
            // 
            this.label15.Anchor = ((System.Windows.Forms.AnchorStyles)(((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Bottom) 
            | System.Windows.Forms.AnchorStyles.Left)));
            this.label15.AutoSize = true;
            this.label15.Location = new System.Drawing.Point(13, 76);
            this.label15.Name = "label15";
            this.label15.Size = new System.Drawing.Size(39, 15);
            this.label15.TabIndex = 1;
            this.label15.Text = "Status";
            this.label15.TextAlign = System.Drawing.ContentAlignment.MiddleLeft;
            // 
            // label16
            // 
            this.label16.Anchor = ((System.Windows.Forms.AnchorStyles)(((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Bottom) 
            | System.Windows.Forms.AnchorStyles.Left)));
            this.label16.AutoSize = true;
            this.label16.Location = new System.Drawing.Point(13, 290);
            this.label16.Name = "label16";
            this.label16.Size = new System.Drawing.Size(41, 15);
            this.label16.TabIndex = 2;
            this.label16.Text = "Where";
            this.label16.TextAlign = System.Drawing.ContentAlignment.MiddleLeft;
            // 
            // label17
            // 
            this.label17.Anchor = System.Windows.Forms.AnchorStyles.Left;
            this.label17.AutoSize = true;
            this.label17.Location = new System.Drawing.Point(13, 322);
            this.label17.Name = "label17";
            this.label17.Size = new System.Drawing.Size(49, 15);
            this.label17.TabIndex = 3;
            this.label17.Text = "CNAME";
            this.label17.TextAlign = System.Drawing.ContentAlignment.MiddleLeft;
            // 
            // deliveryMethodComboBox
            // 
            this.deliveryMethodComboBox.Anchor = ((System.Windows.Forms.AnchorStyles)(((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Left) 
            | System.Windows.Forms.AnchorStyles.Right)));
            this.distributionLayoutPanel.SetColumnSpan(this.deliveryMethodComboBox, 2);
            this.deliveryMethodComboBox.DropDownStyle = System.Windows.Forms.ComboBoxStyle.DropDownList;
            this.deliveryMethodComboBox.FormattingEnabled = true;
            this.deliveryMethodComboBox.Location = new System.Drawing.Point(113, 13);
            this.deliveryMethodComboBox.Name = "deliveryMethodComboBox";
            this.deliveryMethodComboBox.Size = new System.Drawing.Size(1, 23);
            this.deliveryMethodComboBox.TabIndex = 4;
            this.deliveryMethodComboBox.SelectionChangeCommitted += new System.EventHandler(this.deliveryMethodComboBox_SelectionChangeCommitted);
            // 
            // distributionEnableCheckBox
            // 
            this.distributionEnableCheckBox.Anchor = System.Windows.Forms.AnchorStyles.Left;
            this.distributionEnableCheckBox.AutoSize = true;
            this.distributionEnableCheckBox.Location = new System.Drawing.Point(113, 42);
            this.distributionEnableCheckBox.MinimumSize = new System.Drawing.Size(0, 21);
            this.distributionEnableCheckBox.Name = "distributionEnableCheckBox";
            this.distributionEnableCheckBox.Size = new System.Drawing.Size(1, 21);
            this.distributionEnableCheckBox.TabIndex = 5;
            this.distributionEnableCheckBox.Text = "Enable Amazon CloudFront Distribution";
            this.distributionEnableCheckBox.UseVisualStyleBackColor = true;
            this.distributionEnableCheckBox.CheckedChanged += new System.EventHandler(this.distributionEnableCheckBox_CheckedChanged);
            // 
            // distributionLoggingCheckBox
            // 
            this.distributionLoggingCheckBox.Anchor = System.Windows.Forms.AnchorStyles.Left;
            this.distributionLoggingCheckBox.AutoSize = true;
            this.distributionLayoutPanel.SetColumnSpan(this.distributionLoggingCheckBox, 2);
            this.distributionLoggingCheckBox.Location = new System.Drawing.Point(113, 94);
            this.distributionLoggingCheckBox.Name = "distributionLoggingCheckBox";
            this.distributionLoggingCheckBox.Size = new System.Drawing.Size(1, 19);
            this.distributionLoggingCheckBox.TabIndex = 6;
            this.distributionLoggingCheckBox.Text = "Distribution Access Logging";
            this.distributionLoggingCheckBox.UseVisualStyleBackColor = true;
            this.distributionLoggingCheckBox.CheckedChanged += new System.EventHandler(this.distributionLoggingCheckBox_CheckedChanged);
            // 
            // statusLabel
            // 
            this.statusLabel.Anchor = ((System.Windows.Forms.AnchorStyles)(((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Bottom) 
            | System.Windows.Forms.AnchorStyles.Left)));
            this.statusLabel.AutoSize = true;
            this.distributionLayoutPanel.SetColumnSpan(this.statusLabel, 2);
            this.statusLabel.Location = new System.Drawing.Point(113, 76);
            this.statusLabel.Name = "statusLabel";
            this.statusLabel.Size = new System.Drawing.Size(0, 15);
            this.statusLabel.TabIndex = 7;
            this.statusLabel.TextAlign = System.Drawing.ContentAlignment.MiddleLeft;
            // 
            // distributionCnameTextBox
            // 
            this.distributionCnameTextBox.Anchor = ((System.Windows.Forms.AnchorStyles)(((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Left) 
            | System.Windows.Forms.AnchorStyles.Right)));
            this.distributionLayoutPanel.SetColumnSpan(this.distributionCnameTextBox, 2);
            this.distributionCnameTextBox.Location = new System.Drawing.Point(113, 318);
            this.distributionCnameTextBox.Name = "distributionCnameTextBox";
            this.distributionCnameTextBox.Size = new System.Drawing.Size(1, 23);
            this.distributionCnameTextBox.TabIndex = 9;
            this.distributionCnameTextBox.Validated += new System.EventHandler(this.distributionCnameTextBox_Validated);
            // 
            // whereLinkLabel
            // 
            this.whereLinkLabel.Anchor = ((System.Windows.Forms.AnchorStyles)(((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Bottom) 
            | System.Windows.Forms.AnchorStyles.Left)));
            this.whereLinkLabel.AutoSize = true;
            this.distributionLayoutPanel.SetColumnSpan(this.whereLinkLabel, 2);
            this.whereLinkLabel.Location = new System.Drawing.Point(113, 290);
            this.whereLinkLabel.Name = "whereLinkLabel";
            this.whereLinkLabel.Size = new System.Drawing.Size(0, 15);
            this.whereLinkLabel.TabIndex = 10;
            this.whereLinkLabel.TextAlign = System.Drawing.ContentAlignment.MiddleLeft;
            // 
            // distributionAnimation
            // 
            this.distributionAnimation.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Right)));
            this.distributionAnimation.Image = global::Ch.Cyberduck.ResourcesBundle.throbber_small;
            this.distributionAnimation.Location = new System.Drawing.Point(-22, 42);
            this.distributionAnimation.Name = "distributionAnimation";
            this.distributionAnimation.Size = new System.Drawing.Size(30, 20);
            this.distributionAnimation.SizeMode = System.Windows.Forms.PictureBoxSizeMode.CenterImage;
            this.distributionAnimation.TabIndex = 25;
            this.distributionAnimation.TabStop = false;
            this.distributionAnimation.Visible = false;
            // 
            // cnameUrlLinkLabel
            // 
            this.cnameUrlLinkLabel.Anchor = ((System.Windows.Forms.AnchorStyles)(((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Bottom) 
            | System.Windows.Forms.AnchorStyles.Left)));
            this.cnameUrlLinkLabel.AutoSize = true;
            this.cnameUrlLinkLabel.Location = new System.Drawing.Point(113, 344);
            this.cnameUrlLinkLabel.Name = "cnameUrlLinkLabel";
            this.cnameUrlLinkLabel.Size = new System.Drawing.Size(0, 15);
            this.cnameUrlLinkLabel.TabIndex = 26;
            this.cnameUrlLinkLabel.TextAlign = System.Drawing.ContentAlignment.MiddleLeft;
            // 
            // label25
            // 
            this.label25.Anchor = System.Windows.Forms.AnchorStyles.Left;
            this.label25.AutoSize = true;
            this.label25.Location = new System.Drawing.Point(13, 376);
            this.label25.Name = "label25";
            this.label25.Size = new System.Drawing.Size(56, 15);
            this.label25.TabIndex = 27;
            this.label25.Text = "Index File";
            // 
            // defaultRootComboBox
            // 
            this.defaultRootComboBox.Anchor = ((System.Windows.Forms.AnchorStyles)(((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Left) 
            | System.Windows.Forms.AnchorStyles.Right)));
            this.distributionLayoutPanel.SetColumnSpan(this.defaultRootComboBox, 2);
            this.defaultRootComboBox.DropDownStyle = System.Windows.Forms.ComboBoxStyle.DropDownList;
            this.defaultRootComboBox.FormattingEnabled = true;
            this.defaultRootComboBox.Location = new System.Drawing.Point(113, 372);
            this.defaultRootComboBox.Name = "defaultRootComboBox";
            this.defaultRootComboBox.Size = new System.Drawing.Size(1, 23);
            this.defaultRootComboBox.TabIndex = 28;
            this.defaultRootComboBox.SelectionChangeCommitted += new System.EventHandler(this.defaultRootComboBox_SelectionChangeCommitted);
            // 
            // label13
            // 
            this.label13.AutoSize = true;
            this.label13.Location = new System.Drawing.Point(13, 398);
            this.label13.Name = "label13";
            this.label13.Size = new System.Drawing.Size(69, 15);
            this.label13.TabIndex = 29;
            this.label13.Text = "Invalidation";
            // 
            // invalidationStatus
            // 
            this.invalidationStatus.AutoSize = true;
            this.distributionLayoutPanel.SetColumnSpan(this.invalidationStatus, 2);
            this.invalidationStatus.Location = new System.Drawing.Point(113, 398);
            this.invalidationStatus.Name = "invalidationStatus";
            this.invalidationStatus.Size = new System.Drawing.Size(0, 15);
            this.invalidationStatus.TabIndex = 30;
            // 
            // label24
            // 
            this.label24.AutoSize = true;
            this.distributionLayoutPanel.SetColumnSpan(this.label24, 2);
            this.label24.ForeColor = System.Drawing.SystemColors.GrayText;
            this.label24.Location = new System.Drawing.Point(113, 413);
            this.label24.Name = "label24";
            this.label24.Padding = new System.Windows.Forms.Padding(0, 5, 0, 0);
            this.label24.Size = new System.Drawing.Size(1, 5);
            this.label24.TabIndex = 31;
            this.label24.Text = "Remove selected files from distribution cache.";
            // 
            // invalidateButton
            // 
            this.invalidateButton.AutoSize = true;
            this.invalidateButton.Location = new System.Drawing.Point(113, 421);
            this.invalidateButton.Name = "invalidateButton";
            this.invalidateButton.Size = new System.Drawing.Size(1, 27);
            this.invalidateButton.TabIndex = 32;
            this.invalidateButton.Text = "Invalidate";
            this.invalidateButton.UseVisualStyleBackColor = true;
            this.invalidateButton.Click += new System.EventHandler(this.invalidateButton_Click);
            // 
            // label27
            // 
            this.label27.AutoSize = true;
            this.label27.Location = new System.Drawing.Point(13, 265);
            this.label27.Name = "label27";
            this.label27.Size = new System.Drawing.Size(40, 15);
            this.label27.TabIndex = 33;
            this.label27.Text = "Origin";
            // 
            // originLinkLabel
            // 
            this.originLinkLabel.AutoSize = true;
            this.distributionLayoutPanel.SetColumnSpan(this.originLinkLabel, 2);
            this.originLinkLabel.Location = new System.Drawing.Point(113, 265);
            this.originLinkLabel.Name = "originLinkLabel";
            this.originLinkLabel.Size = new System.Drawing.Size(0, 15);
            this.originLinkLabel.TabIndex = 34;
            // 
            // label28
            // 
            this.label28.Anchor = ((System.Windows.Forms.AnchorStyles)(((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Bottom) 
            | System.Windows.Forms.AnchorStyles.Left)));
            this.label28.AutoSize = true;
            this.label28.Location = new System.Drawing.Point(13, 91);
            this.label28.Name = "label28";
            this.label28.Size = new System.Drawing.Size(51, 25);
            this.label28.TabIndex = 35;
            this.label28.Text = "Logging";
            this.label28.TextAlign = System.Drawing.ContentAlignment.MiddleLeft;
            // 
            // label29
            // 
            this.label29.AutoSize = true;
            this.distributionLayoutPanel.SetColumnSpan(this.label29, 2);
            this.label29.ForeColor = System.Drawing.SystemColors.GrayText;
            this.label29.Location = new System.Drawing.Point(113, 116);
            this.label29.Name = "label29";
            this.label29.Padding = new System.Windows.Forms.Padding(0, 5, 0, 0);
            this.label29.Size = new System.Drawing.Size(1, 5);
            this.label29.TabIndex = 36;
            this.label29.Text = "Write access logs to selected container.";
            // 
            // distributionLoggingComboBox
            // 
            this.distributionLoggingComboBox.Anchor = ((System.Windows.Forms.AnchorStyles)(((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Left) 
            | System.Windows.Forms.AnchorStyles.Right)));
            this.distributionLayoutPanel.SetColumnSpan(this.distributionLoggingComboBox, 2);
            this.distributionLoggingComboBox.DropDownStyle = System.Windows.Forms.ComboBoxStyle.DropDownList;
            this.distributionLoggingComboBox.FormattingEnabled = true;
            this.distributionLoggingComboBox.Location = new System.Drawing.Point(113, 124);
            this.distributionLoggingComboBox.Name = "distributionLoggingComboBox";
            this.distributionLoggingComboBox.Size = new System.Drawing.Size(1, 23);
            this.distributionLoggingComboBox.TabIndex = 37;
            this.distributionLoggingComboBox.SelectionChangeCommitted += new System.EventHandler(this.distributionLoggingComboBox_SelectionChangeCommitted);
            // 
            // label37
            // 
            this.label37.AutoSize = true;
            this.label37.Location = new System.Drawing.Point(13, 170);
            this.label37.Name = "label37";
            this.label37.Size = new System.Drawing.Size(55, 15);
            this.label37.TabIndex = 38;
            this.label37.Text = "Analytics";
            // 
            // distributionAnalyticsCheckBox
            // 
            this.distributionAnalyticsCheckBox.Anchor = System.Windows.Forms.AnchorStyles.Left;
            this.distributionAnalyticsCheckBox.AutoSize = true;
            this.distributionLayoutPanel.SetColumnSpan(this.distributionAnalyticsCheckBox, 2);
            this.distributionAnalyticsCheckBox.Location = new System.Drawing.Point(113, 173);
            this.distributionAnalyticsCheckBox.Name = "distributionAnalyticsCheckBox";
            this.distributionAnalyticsCheckBox.Size = new System.Drawing.Size(1, 19);
            this.distributionAnalyticsCheckBox.TabIndex = 39;
            this.distributionAnalyticsCheckBox.Text = "Read Access for Qloudstat";
            this.distributionAnalyticsCheckBox.UseVisualStyleBackColor = true;
            this.distributionAnalyticsCheckBox.CheckedChanged += new System.EventHandler(this.distributionAnalyticsCheckBox_CheckedChanged);
            // 
            // distributionAnalyticsSetupUrlLinkLabel
            // 
            this.distributionAnalyticsSetupUrlLinkLabel.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Left | System.Windows.Forms.AnchorStyles.Right)));
            this.distributionAnalyticsSetupUrlLinkLabel.AutoEllipsis = true;
            this.distributionLayoutPanel.SetColumnSpan(this.distributionAnalyticsSetupUrlLinkLabel, 2);
            this.distributionAnalyticsSetupUrlLinkLabel.Location = new System.Drawing.Point(113, 198);
            this.distributionAnalyticsSetupUrlLinkLabel.Name = "distributionAnalyticsSetupUrlLinkLabel";
            this.distributionAnalyticsSetupUrlLinkLabel.Size = new System.Drawing.Size(1, 13);
            this.distributionAnalyticsSetupUrlLinkLabel.TabIndex = 40;
            this.distributionAnalyticsSetupUrlLinkLabel.TabStop = true;
            this.distributionAnalyticsSetupUrlLinkLabel.Text = "linkLabel1";
            // 
            // generalLayoutPanel
            // 
            this.generalLayoutPanel.AutoSize = true;
            this.generalLayoutPanel.ColumnCount = 5;
            this.generalLayoutPanel.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle(System.Windows.Forms.SizeType.Absolute, 70F));
            this.generalLayoutPanel.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle());
            this.generalLayoutPanel.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle(System.Windows.Forms.SizeType.Percent, 100F));
            this.generalLayoutPanel.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle(System.Windows.Forms.SizeType.Absolute, 23F));
            this.generalLayoutPanel.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle());
            this.generalLayoutPanel.Controls.Add(this.icon, 0, 0);
            this.generalLayoutPanel.Controls.Add(this.filenameTextbox, 1, 0);
            this.generalLayoutPanel.Controls.Add(this.label1, 1, 1);
            this.generalLayoutPanel.Controls.Add(this.label2, 1, 2);
            this.generalLayoutPanel.Controls.Add(this.label3, 1, 3);
            this.generalLayoutPanel.Controls.Add(this.label4, 1, 4);
            this.generalLayoutPanel.Controls.Add(this.label5, 1, 5);
            this.generalLayoutPanel.Controls.Add(this.label6, 1, 6);
            this.generalLayoutPanel.Controls.Add(this.label7, 1, 7);
            this.generalLayoutPanel.Controls.Add(this.label8, 1, 9);
            this.generalLayoutPanel.Controls.Add(this.label9, 1, 10);
            this.generalLayoutPanel.Controls.Add(this.sizeLabel, 2, 1);
            this.generalLayoutPanel.Controls.Add(this.pathLabel, 2, 2);
            this.generalLayoutPanel.Controls.Add(this.weburlLabel, 2, 3);
            this.generalLayoutPanel.Controls.Add(this.kindLabel, 2, 4);
            this.generalLayoutPanel.Controls.Add(this.permissionsLabel, 2, 5);
            this.generalLayoutPanel.Controls.Add(this.ownerLabel, 2, 6);
            this.generalLayoutPanel.Controls.Add(this.groupLabel, 2, 7);
            this.generalLayoutPanel.Controls.Add(this.modifiedLabel, 2, 9);
            this.generalLayoutPanel.Controls.Add(this.calculateButton, 4, 1);
            this.generalLayoutPanel.Controls.Add(this.sizeAnimation, 3, 1);
            this.generalLayoutPanel.Controls.Add(this.label22, 1, 8);
            this.generalLayoutPanel.Controls.Add(this.createdLabel, 2, 8);
            this.generalLayoutPanel.Controls.Add(this.checksumTextBox, 2, 10);
            this.generalLayoutPanel.Dock = System.Windows.Forms.DockStyle.Fill;
            this.generalLayoutPanel.Location = new System.Drawing.Point(0, 0);
            this.generalLayoutPanel.Name = "generalLayoutPanel";
            this.generalLayoutPanel.Padding = new System.Windows.Forms.Padding(10);
            this.generalLayoutPanel.RowCount = 12;
            this.generalLayoutPanel.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.generalLayoutPanel.RowStyles.Add(new System.Windows.Forms.RowStyle(System.Windows.Forms.SizeType.Absolute, 28F));
            this.generalLayoutPanel.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.generalLayoutPanel.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.generalLayoutPanel.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.generalLayoutPanel.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.generalLayoutPanel.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.generalLayoutPanel.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.generalLayoutPanel.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.generalLayoutPanel.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.generalLayoutPanel.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.generalLayoutPanel.RowStyles.Add(new System.Windows.Forms.RowStyle(System.Windows.Forms.SizeType.Percent, 100F));
            this.generalLayoutPanel.Size = new System.Drawing.Size(0, 0);
            this.generalLayoutPanel.TabIndex = 0;
            // 
            // icon
            // 
            this.icon.Location = new System.Drawing.Point(13, 13);
            this.icon.Name = "icon";
            this.icon.Size = new System.Drawing.Size(63, 51);
            this.icon.SizeMode = System.Windows.Forms.PictureBoxSizeMode.CenterImage;
            this.icon.TabIndex = 0;
            this.icon.TabStop = false;
            // 
            // filenameTextbox
            // 
            this.filenameTextbox.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Left | System.Windows.Forms.AnchorStyles.Right)));
            this.generalLayoutPanel.SetColumnSpan(this.filenameTextbox, 4);
            this.filenameTextbox.Location = new System.Drawing.Point(83, 27);
            this.filenameTextbox.Name = "filenameTextbox";
            this.filenameTextbox.Size = new System.Drawing.Size(1, 23);
            this.filenameTextbox.TabIndex = 1;
            this.filenameTextbox.Validated += new System.EventHandler(this.filenameTextbox_Validated);
            // 
            // label1
            // 
            this.label1.Anchor = System.Windows.Forms.AnchorStyles.Left;
            this.label1.AutoSize = true;
            this.label1.Location = new System.Drawing.Point(83, 70);
            this.label1.MinimumSize = new System.Drawing.Size(0, 21);
            this.label1.Name = "label1";
            this.label1.Size = new System.Drawing.Size(27, 21);
            this.label1.TabIndex = 2;
            this.label1.Text = "Size";
            this.label1.TextAlign = System.Drawing.ContentAlignment.MiddleLeft;
            // 
            // label2
            // 
            this.label2.Anchor = System.Windows.Forms.AnchorStyles.Left;
            this.label2.AutoSize = true;
            this.label2.Location = new System.Drawing.Point(83, 96);
            this.label2.MinimumSize = new System.Drawing.Size(0, 21);
            this.label2.Name = "label2";
            this.label2.Size = new System.Drawing.Size(41, 21);
            this.label2.TabIndex = 3;
            this.label2.Text = "Where";
            this.label2.TextAlign = System.Drawing.ContentAlignment.MiddleLeft;
            // 
            // label3
            // 
            this.label3.Anchor = System.Windows.Forms.AnchorStyles.Left;
            this.label3.AutoSize = true;
            this.label3.Location = new System.Drawing.Point(83, 119);
            this.label3.MinimumSize = new System.Drawing.Size(0, 21);
            this.label3.Name = "label3";
            this.label3.Size = new System.Drawing.Size(55, 21);
            this.label3.TabIndex = 4;
            this.label3.Text = "Web URL";
            this.label3.TextAlign = System.Drawing.ContentAlignment.MiddleLeft;
            // 
            // label4
            // 
            this.label4.Anchor = System.Windows.Forms.AnchorStyles.Left;
            this.label4.AutoSize = true;
            this.label4.Location = new System.Drawing.Point(83, 142);
            this.label4.MinimumSize = new System.Drawing.Size(0, 21);
            this.label4.Name = "label4";
            this.label4.Size = new System.Drawing.Size(31, 21);
            this.label4.TabIndex = 5;
            this.label4.Text = "Kind";
            this.label4.TextAlign = System.Drawing.ContentAlignment.MiddleLeft;
            // 
            // label5
            // 
            this.label5.Anchor = System.Windows.Forms.AnchorStyles.Left;
            this.label5.AutoSize = true;
            this.label5.Location = new System.Drawing.Point(83, 165);
            this.label5.MinimumSize = new System.Drawing.Size(0, 21);
            this.label5.Name = "label5";
            this.label5.Size = new System.Drawing.Size(70, 21);
            this.label5.TabIndex = 6;
            this.label5.Text = "Permissions";
            this.label5.TextAlign = System.Drawing.ContentAlignment.MiddleLeft;
            // 
            // label6
            // 
            this.label6.Anchor = System.Windows.Forms.AnchorStyles.Left;
            this.label6.AutoSize = true;
            this.label6.Location = new System.Drawing.Point(83, 188);
            this.label6.MinimumSize = new System.Drawing.Size(0, 21);
            this.label6.Name = "label6";
            this.label6.Size = new System.Drawing.Size(42, 21);
            this.label6.TabIndex = 7;
            this.label6.Text = "Owner";
            this.label6.TextAlign = System.Drawing.ContentAlignment.MiddleLeft;
            // 
            // label7
            // 
            this.label7.Anchor = System.Windows.Forms.AnchorStyles.Left;
            this.label7.AutoSize = true;
            this.label7.Location = new System.Drawing.Point(83, 211);
            this.label7.MinimumSize = new System.Drawing.Size(0, 21);
            this.label7.Name = "label7";
            this.label7.Size = new System.Drawing.Size(40, 21);
            this.label7.TabIndex = 8;
            this.label7.Text = "Group";
            this.label7.TextAlign = System.Drawing.ContentAlignment.MiddleLeft;
            // 
            // label8
            // 
            this.label8.Anchor = System.Windows.Forms.AnchorStyles.Left;
            this.label8.AutoSize = true;
            this.label8.Location = new System.Drawing.Point(83, 257);
            this.label8.MinimumSize = new System.Drawing.Size(0, 21);
            this.label8.Name = "label8";
            this.label8.Size = new System.Drawing.Size(55, 21);
            this.label8.TabIndex = 9;
            this.label8.Text = "Modified";
            this.label8.TextAlign = System.Drawing.ContentAlignment.MiddleLeft;
            // 
            // label9
            // 
            this.label9.Anchor = System.Windows.Forms.AnchorStyles.Left;
            this.label9.AutoSize = true;
            this.label9.Location = new System.Drawing.Point(83, 279);
            this.label9.MinimumSize = new System.Drawing.Size(0, 21);
            this.label9.Name = "label9";
            this.label9.Size = new System.Drawing.Size(63, 21);
            this.label9.TabIndex = 10;
            this.label9.Text = "Checksum";
            this.label9.TextAlign = System.Drawing.ContentAlignment.MiddleLeft;
            // 
            // sizeLabel
            // 
            this.sizeLabel.Anchor = System.Windows.Forms.AnchorStyles.Left;
            this.sizeLabel.AutoEllipsis = true;
            this.sizeLabel.AutoSize = true;
            this.sizeLabel.Location = new System.Drawing.Point(159, 69);
            this.sizeLabel.MinimumSize = new System.Drawing.Size(0, 23);
            this.sizeLabel.Name = "sizeLabel";
            this.sizeLabel.Size = new System.Drawing.Size(1, 23);
            this.sizeLabel.TabIndex = 11;
            this.sizeLabel.Text = "ellipsisLabel1";
            this.sizeLabel.TextAlign = System.Drawing.ContentAlignment.MiddleLeft;
            // 
            // pathLabel
            // 
            this.pathLabel.Anchor = System.Windows.Forms.AnchorStyles.Left;
            this.pathLabel.AutoEllipsis = true;
            this.pathLabel.AutoSize = true;
            this.generalLayoutPanel.SetColumnSpan(this.pathLabel, 2);
            this.pathLabel.Location = new System.Drawing.Point(159, 95);
            this.pathLabel.MinimumSize = new System.Drawing.Size(0, 23);
            this.pathLabel.Name = "pathLabel";
            this.pathLabel.Size = new System.Drawing.Size(1, 23);
            this.pathLabel.TabIndex = 12;
            this.pathLabel.Text = "ellipsisLabel2";
            this.pathLabel.TextAlign = System.Drawing.ContentAlignment.MiddleLeft;
            // 
            // weburlLabel
            // 
            this.weburlLabel.Anchor = System.Windows.Forms.AnchorStyles.Left;
            this.weburlLabel.AutoEllipsis = true;
            this.weburlLabel.AutoSize = true;
            this.generalLayoutPanel.SetColumnSpan(this.weburlLabel, 3);
            this.weburlLabel.Location = new System.Drawing.Point(159, 118);
            this.weburlLabel.MinimumSize = new System.Drawing.Size(0, 23);
            this.weburlLabel.Name = "weburlLabel";
            this.weburlLabel.Size = new System.Drawing.Size(1, 23);
            this.weburlLabel.TabIndex = 13;
            this.weburlLabel.TabStop = true;
            this.weburlLabel.Text = "linkLabel1";
            this.weburlLabel.TextAlign = System.Drawing.ContentAlignment.MiddleLeft;
            // 
            // kindLabel
            // 
            this.kindLabel.Anchor = System.Windows.Forms.AnchorStyles.Left;
            this.kindLabel.AutoEllipsis = true;
            this.kindLabel.AutoSize = true;
            this.generalLayoutPanel.SetColumnSpan(this.kindLabel, 3);
            this.kindLabel.Location = new System.Drawing.Point(159, 141);
            this.kindLabel.MinimumSize = new System.Drawing.Size(0, 23);
            this.kindLabel.Name = "kindLabel";
            this.kindLabel.Size = new System.Drawing.Size(1, 23);
            this.kindLabel.TabIndex = 14;
            this.kindLabel.Text = "ellipsisLabel3";
            this.kindLabel.TextAlign = System.Drawing.ContentAlignment.MiddleLeft;
            // 
            // permissionsLabel
            // 
            this.permissionsLabel.Anchor = System.Windows.Forms.AnchorStyles.Left;
            this.permissionsLabel.AutoEllipsis = true;
            this.permissionsLabel.AutoSize = true;
            this.generalLayoutPanel.SetColumnSpan(this.permissionsLabel, 3);
            this.permissionsLabel.Location = new System.Drawing.Point(159, 164);
            this.permissionsLabel.MinimumSize = new System.Drawing.Size(0, 23);
            this.permissionsLabel.Name = "permissionsLabel";
            this.permissionsLabel.Size = new System.Drawing.Size(1, 23);
            this.permissionsLabel.TabIndex = 15;
            this.permissionsLabel.Text = "ellipsisLabel4";
            this.permissionsLabel.TextAlign = System.Drawing.ContentAlignment.MiddleLeft;
            // 
            // ownerLabel
            // 
            this.ownerLabel.Anchor = System.Windows.Forms.AnchorStyles.Left;
            this.ownerLabel.AutoEllipsis = true;
            this.ownerLabel.AutoSize = true;
            this.generalLayoutPanel.SetColumnSpan(this.ownerLabel, 3);
            this.ownerLabel.Location = new System.Drawing.Point(159, 187);
            this.ownerLabel.MinimumSize = new System.Drawing.Size(0, 23);
            this.ownerLabel.Name = "ownerLabel";
            this.ownerLabel.Size = new System.Drawing.Size(1, 23);
            this.ownerLabel.TabIndex = 16;
            this.ownerLabel.Text = "ellipsisLabel5";
            this.ownerLabel.TextAlign = System.Drawing.ContentAlignment.MiddleLeft;
            // 
            // groupLabel
            // 
            this.groupLabel.Anchor = System.Windows.Forms.AnchorStyles.Left;
            this.groupLabel.AutoEllipsis = true;
            this.groupLabel.AutoSize = true;
            this.generalLayoutPanel.SetColumnSpan(this.groupLabel, 3);
            this.groupLabel.Location = new System.Drawing.Point(159, 210);
            this.groupLabel.MinimumSize = new System.Drawing.Size(0, 23);
            this.groupLabel.Name = "groupLabel";
            this.groupLabel.Size = new System.Drawing.Size(1, 23);
            this.groupLabel.TabIndex = 17;
            this.groupLabel.Text = "ellipsisLabel6";
            this.groupLabel.TextAlign = System.Drawing.ContentAlignment.MiddleLeft;
            // 
            // modifiedLabel
            // 
            this.modifiedLabel.Anchor = System.Windows.Forms.AnchorStyles.Left;
            this.modifiedLabel.AutoEllipsis = true;
            this.modifiedLabel.AutoSize = true;
            this.generalLayoutPanel.SetColumnSpan(this.modifiedLabel, 3);
            this.modifiedLabel.Location = new System.Drawing.Point(159, 256);
            this.modifiedLabel.MinimumSize = new System.Drawing.Size(0, 23);
            this.modifiedLabel.Name = "modifiedLabel";
            this.modifiedLabel.Size = new System.Drawing.Size(1, 23);
            this.modifiedLabel.TabIndex = 19;
            this.modifiedLabel.Text = "ellipsisLabel8";
            this.modifiedLabel.TextAlign = System.Drawing.ContentAlignment.MiddleLeft;
            // 
            // calculateButton
            // 
            this.calculateButton.AutoSize = true;
            this.calculateButton.AutoSizeMode = System.Windows.Forms.AutoSizeMode.GrowAndShrink;
            this.calculateButton.Location = new System.Drawing.Point(-58, 70);
            this.calculateButton.Name = "calculateButton";
            this.generalLayoutPanel.SetRowSpan(this.calculateButton, 2);
            this.calculateButton.Size = new System.Drawing.Size(66, 25);
            this.calculateButton.TabIndex = 21;
            this.calculateButton.Text = "Calculate";
            this.calculateButton.UseVisualStyleBackColor = true;
            this.calculateButton.Click += new System.EventHandler(this.calculateButton_Click);
            // 
            // sizeAnimation
            // 
            this.sizeAnimation.Anchor = ((System.Windows.Forms.AnchorStyles)(((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Left) 
            | System.Windows.Forms.AnchorStyles.Right)));
            this.sizeAnimation.Image = global::Ch.Cyberduck.ResourcesBundle.throbber_small;
            this.sizeAnimation.Location = new System.Drawing.Point(-81, 70);
            this.sizeAnimation.Name = "sizeAnimation";
            this.sizeAnimation.Size = new System.Drawing.Size(17, 21);
            this.sizeAnimation.SizeMode = System.Windows.Forms.PictureBoxSizeMode.CenterImage;
            this.sizeAnimation.TabIndex = 22;
            this.sizeAnimation.TabStop = false;
            this.sizeAnimation.Visible = false;
            // 
            // label22
            // 
            this.label22.Anchor = System.Windows.Forms.AnchorStyles.Left;
            this.label22.AutoSize = true;
            this.label22.Location = new System.Drawing.Point(83, 234);
            this.label22.MinimumSize = new System.Drawing.Size(0, 21);
            this.label22.Name = "label22";
            this.label22.Size = new System.Drawing.Size(48, 21);
            this.label22.TabIndex = 23;
            this.label22.Text = "Created";
            this.label22.TextAlign = System.Drawing.ContentAlignment.MiddleLeft;
            // 
            // createdLabel
            // 
            this.createdLabel.Anchor = System.Windows.Forms.AnchorStyles.Left;
            this.createdLabel.AutoEllipsis = true;
            this.createdLabel.AutoSize = true;
            this.generalLayoutPanel.SetColumnSpan(this.createdLabel, 3);
            this.createdLabel.Location = new System.Drawing.Point(159, 233);
            this.createdLabel.MinimumSize = new System.Drawing.Size(0, 23);
            this.createdLabel.Name = "createdLabel";
            this.createdLabel.Size = new System.Drawing.Size(1, 23);
            this.createdLabel.TabIndex = 24;
            this.createdLabel.Text = "ellipsisLabel8";
            this.createdLabel.TextAlign = System.Drawing.ContentAlignment.MiddleLeft;
            // 
            // checksumTextBox
            // 
            this.checksumTextBox.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Left | System.Windows.Forms.AnchorStyles.Right)));
            this.checksumTextBox.BorderStyle = System.Windows.Forms.BorderStyle.None;
            this.generalLayoutPanel.SetColumnSpan(this.checksumTextBox, 3);
            this.checksumTextBox.Location = new System.Drawing.Point(162, 282);
            this.checksumTextBox.Margin = new System.Windows.Forms.Padding(6, 3, 3, 3);
            this.checksumTextBox.Name = "checksumTextBox";
            this.checksumTextBox.ReadOnly = true;
            this.checksumTextBox.Size = new System.Drawing.Size(1, 16);
            this.checksumTextBox.TabIndex = 25;
            this.checksumTextBox.TabStop = false;
            // 
            // s3LayoutPanel
            // 
            this.s3LayoutPanel.ColumnCount = 4;
            this.s3LayoutPanel.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle(System.Windows.Forms.SizeType.Absolute, 20F));
            this.s3LayoutPanel.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle());
            this.s3LayoutPanel.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle(System.Windows.Forms.SizeType.Percent, 100F));
            this.s3LayoutPanel.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle(System.Windows.Forms.SizeType.Absolute, 37F));
            this.s3LayoutPanel.Controls.Add(this.s3Animation, 3, 0);
            this.s3LayoutPanel.Controls.Add(this.label18, 0, 0);
            this.s3LayoutPanel.Controls.Add(this.label19, 0, 6);
            this.s3LayoutPanel.Controls.Add(this.label20, 0, 9);
            this.s3LayoutPanel.Controls.Add(this.bucketLocationLabel, 2, 0);
            this.s3LayoutPanel.Controls.Add(this.s3PublicUrlLinkLabel, 2, 6);
            this.s3LayoutPanel.Controls.Add(this.s3TorrentUrlLinkLabel, 2, 9);
            this.s3LayoutPanel.Controls.Add(this.label26, 0, 2);
            this.s3LayoutPanel.Controls.Add(this.s3PublicUrlValidityLabel, 2, 7);
            this.s3LayoutPanel.Controls.Add(this.storageClassComboBox, 2, 2);
            this.s3LayoutPanel.Controls.Add(this.bucketLoggingCheckBox, 2, 11);
            this.s3LayoutPanel.Controls.Add(this.bucketVersioningCheckBox, 2, 19);
            this.s3LayoutPanel.Controls.Add(this.label21, 2, 20);
            this.s3LayoutPanel.Controls.Add(this.bucketMfaCheckBox, 2, 22);
            this.s3LayoutPanel.Controls.Add(this.label31, 0, 11);
            this.s3LayoutPanel.Controls.Add(this.label32, 0, 19);
            this.s3LayoutPanel.Controls.Add(this.label33, 2, 12);
            this.s3LayoutPanel.Controls.Add(this.bucketLoggingComboBox, 2, 13);
            this.s3LayoutPanel.Controls.Add(this.label34, 0, 4);
            this.s3LayoutPanel.Controls.Add(this.label35, 0, 15);
            this.s3LayoutPanel.Controls.Add(this.bucketAnalyticsCheckBox, 2, 15);
            this.s3LayoutPanel.Controls.Add(this.bucketAnalyticsSetupUrlLinkLabel, 2, 16);
            this.s3LayoutPanel.Controls.Add(this.label36, 2, 17);
            this.s3LayoutPanel.Controls.Add(this.lifecycleTransitionCheckBox, 2, 24);
            this.s3LayoutPanel.Controls.Add(this.label38, 0, 24);
            this.s3LayoutPanel.Controls.Add(this.lifecycleTransitionComboBox, 2, 25);
            this.s3LayoutPanel.Controls.Add(this.lifecycleDeleteCheckBox, 2, 26);
            this.s3LayoutPanel.Controls.Add(this.lifecycleDeleteComboBox, 2, 27);
            this.s3LayoutPanel.Controls.Add(this.encryptionComboBox, 2, 4);
            this.s3LayoutPanel.Dock = System.Windows.Forms.DockStyle.Fill;
            this.s3LayoutPanel.Location = new System.Drawing.Point(0, 0);
            this.s3LayoutPanel.Name = "s3LayoutPanel";
            this.s3LayoutPanel.Padding = new System.Windows.Forms.Padding(10);
            this.s3LayoutPanel.RowCount = 29;
            this.s3LayoutPanel.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.s3LayoutPanel.RowStyles.Add(new System.Windows.Forms.RowStyle(System.Windows.Forms.SizeType.Absolute, 5F));
            this.s3LayoutPanel.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.s3LayoutPanel.RowStyles.Add(new System.Windows.Forms.RowStyle(System.Windows.Forms.SizeType.Absolute, 10F));
            this.s3LayoutPanel.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.s3LayoutPanel.RowStyles.Add(new System.Windows.Forms.RowStyle(System.Windows.Forms.SizeType.Absolute, 10F));
            this.s3LayoutPanel.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.s3LayoutPanel.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.s3LayoutPanel.RowStyles.Add(new System.Windows.Forms.RowStyle(System.Windows.Forms.SizeType.Absolute, 10F));
            this.s3LayoutPanel.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.s3LayoutPanel.RowStyles.Add(new System.Windows.Forms.RowStyle(System.Windows.Forms.SizeType.Absolute, 10F));
            this.s3LayoutPanel.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.s3LayoutPanel.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.s3LayoutPanel.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.s3LayoutPanel.RowStyles.Add(new System.Windows.Forms.RowStyle(System.Windows.Forms.SizeType.Absolute, 10F));
            this.s3LayoutPanel.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.s3LayoutPanel.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.s3LayoutPanel.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.s3LayoutPanel.RowStyles.Add(new System.Windows.Forms.RowStyle(System.Windows.Forms.SizeType.Absolute, 10F));
            this.s3LayoutPanel.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.s3LayoutPanel.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.s3LayoutPanel.RowStyles.Add(new System.Windows.Forms.RowStyle(System.Windows.Forms.SizeType.Absolute, 10F));
            this.s3LayoutPanel.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.s3LayoutPanel.RowStyles.Add(new System.Windows.Forms.RowStyle(System.Windows.Forms.SizeType.Absolute, 10F));
            this.s3LayoutPanel.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.s3LayoutPanel.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.s3LayoutPanel.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.s3LayoutPanel.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.s3LayoutPanel.RowStyles.Add(new System.Windows.Forms.RowStyle(System.Windows.Forms.SizeType.Absolute, 30F));
            this.s3LayoutPanel.RowStyles.Add(new System.Windows.Forms.RowStyle(System.Windows.Forms.SizeType.Absolute, 20F));
            this.s3LayoutPanel.Size = new System.Drawing.Size(500, 548);
            this.s3LayoutPanel.TabIndex = 0;
            // 
            // s3Animation
            // 
            this.s3Animation.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Right)));
            this.s3Animation.Image = global::Ch.Cyberduck.ResourcesBundle.throbber_small;
            this.s3Animation.Location = new System.Drawing.Point(463, 13);
            this.s3Animation.Name = "s3Animation";
            this.s3Animation.Size = new System.Drawing.Size(24, 18);
            this.s3Animation.SizeMode = System.Windows.Forms.PictureBoxSizeMode.CenterImage;
            this.s3Animation.TabIndex = 24;
            this.s3Animation.TabStop = false;
            this.s3Animation.Visible = false;
            // 
            // label18
            // 
            this.label18.Anchor = System.Windows.Forms.AnchorStyles.Left;
            this.label18.AutoSize = true;
            this.s3LayoutPanel.SetColumnSpan(this.label18, 2);
            this.label18.Location = new System.Drawing.Point(13, 10);
            this.label18.MinimumSize = new System.Drawing.Size(0, 25);
            this.label18.Name = "label18";
            this.label18.Size = new System.Drawing.Size(53, 25);
            this.label18.TabIndex = 0;
            this.label18.Text = "Location";
            this.label18.TextAlign = System.Drawing.ContentAlignment.MiddleLeft;
            // 
            // label19
            // 
            this.label19.Anchor = System.Windows.Forms.AnchorStyles.Left;
            this.label19.AutoSize = true;
            this.s3LayoutPanel.SetColumnSpan(this.label19, 2);
            this.label19.Location = new System.Drawing.Point(13, 116);
            this.label19.Name = "label19";
            this.label19.Size = new System.Drawing.Size(67, 15);
            this.label19.TabIndex = 1;
            this.label19.Text = "Signed URL";
            this.label19.TextAlign = System.Drawing.ContentAlignment.MiddleLeft;
            // 
            // label20
            // 
            this.label20.Anchor = System.Windows.Forms.AnchorStyles.Left;
            this.label20.AutoSize = true;
            this.s3LayoutPanel.SetColumnSpan(this.label20, 2);
            this.label20.Location = new System.Drawing.Point(13, 156);
            this.label20.Name = "label20";
            this.label20.Size = new System.Drawing.Size(70, 15);
            this.label20.TabIndex = 2;
            this.label20.Text = "Torrent URL";
            this.label20.TextAlign = System.Drawing.ContentAlignment.MiddleLeft;
            // 
            // bucketLocationLabel
            // 
            this.bucketLocationLabel.Anchor = System.Windows.Forms.AnchorStyles.Left;
            this.bucketLocationLabel.AutoSize = true;
            this.bucketLocationLabel.Location = new System.Drawing.Point(96, 10);
            this.bucketLocationLabel.MinimumSize = new System.Drawing.Size(0, 25);
            this.bucketLocationLabel.Name = "bucketLocationLabel";
            this.bucketLocationLabel.Size = new System.Drawing.Size(44, 25);
            this.bucketLocationLabel.TabIndex = 5;
            this.bucketLocationLabel.Text = "label22";
            this.bucketLocationLabel.TextAlign = System.Drawing.ContentAlignment.MiddleLeft;
            // 
            // s3PublicUrlLinkLabel
            // 
            this.s3PublicUrlLinkLabel.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Left | System.Windows.Forms.AnchorStyles.Right)));
            this.s3PublicUrlLinkLabel.AutoEllipsis = true;
            this.s3LayoutPanel.SetColumnSpan(this.s3PublicUrlLinkLabel, 2);
            this.s3PublicUrlLinkLabel.Location = new System.Drawing.Point(96, 117);
            this.s3PublicUrlLinkLabel.Name = "s3PublicUrlLinkLabel";
            this.s3PublicUrlLinkLabel.Size = new System.Drawing.Size(391, 13);
            this.s3PublicUrlLinkLabel.TabIndex = 6;
            this.s3PublicUrlLinkLabel.TabStop = true;
            this.s3PublicUrlLinkLabel.Text = "linkLabel1";
            this.s3PublicUrlLinkLabel.TextAlign = System.Drawing.ContentAlignment.MiddleLeft;
            // 
            // s3TorrentUrlLinkLabel
            // 
            this.s3TorrentUrlLinkLabel.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Left | System.Windows.Forms.AnchorStyles.Right)));
            this.s3TorrentUrlLinkLabel.AutoEllipsis = true;
            this.s3LayoutPanel.SetColumnSpan(this.s3TorrentUrlLinkLabel, 2);
            this.s3TorrentUrlLinkLabel.Location = new System.Drawing.Point(96, 157);
            this.s3TorrentUrlLinkLabel.Name = "s3TorrentUrlLinkLabel";
            this.s3TorrentUrlLinkLabel.Size = new System.Drawing.Size(391, 13);
            this.s3TorrentUrlLinkLabel.TabIndex = 7;
            this.s3TorrentUrlLinkLabel.TabStop = true;
            this.s3TorrentUrlLinkLabel.Text = "linkLabel2";
            this.s3TorrentUrlLinkLabel.TextAlign = System.Drawing.ContentAlignment.MiddleLeft;
            // 
            // label26
            // 
            this.label26.Anchor = System.Windows.Forms.AnchorStyles.Left;
            this.label26.AutoSize = true;
            this.s3LayoutPanel.SetColumnSpan(this.label26, 2);
            this.label26.Location = new System.Drawing.Point(13, 47);
            this.label26.Name = "label26";
            this.label26.Size = new System.Drawing.Size(77, 15);
            this.label26.TabIndex = 25;
            this.label26.Text = "Storage Class";
            // 
            // s3PublicUrlValidityLabel
            // 
            this.s3PublicUrlValidityLabel.Anchor = System.Windows.Forms.AnchorStyles.Left;
            this.s3PublicUrlValidityLabel.AutoSize = true;
            this.s3PublicUrlValidityLabel.Location = new System.Drawing.Point(96, 131);
            this.s3PublicUrlValidityLabel.Name = "s3PublicUrlValidityLabel";
            this.s3PublicUrlValidityLabel.Size = new System.Drawing.Size(44, 15);
            this.s3PublicUrlValidityLabel.TabIndex = 26;
            this.s3PublicUrlValidityLabel.Text = "label27";
            // 
            // storageClassComboBox
            // 
            this.storageClassComboBox.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Left | System.Windows.Forms.AnchorStyles.Right)));
            this.s3LayoutPanel.SetColumnSpan(this.storageClassComboBox, 2);
            this.storageClassComboBox.DropDownStyle = System.Windows.Forms.ComboBoxStyle.DropDownList;
            this.storageClassComboBox.FormattingEnabled = true;
            this.storageClassComboBox.Location = new System.Drawing.Point(96, 43);
            this.storageClassComboBox.Name = "storageClassComboBox";
            this.storageClassComboBox.Size = new System.Drawing.Size(391, 23);
            this.storageClassComboBox.TabIndex = 30;
            this.storageClassComboBox.SelectionChangeCommitted += new System.EventHandler(this.storageClassComboBox_SelectionChangeCommitted);
            // 
            // bucketLoggingCheckBox
            // 
            this.bucketLoggingCheckBox.Anchor = System.Windows.Forms.AnchorStyles.Left;
            this.bucketLoggingCheckBox.AutoSize = true;
            this.s3LayoutPanel.SetColumnSpan(this.bucketLoggingCheckBox, 2);
            this.bucketLoggingCheckBox.Location = new System.Drawing.Point(96, 184);
            this.bucketLoggingCheckBox.Name = "bucketLoggingCheckBox";
            this.bucketLoggingCheckBox.Size = new System.Drawing.Size(148, 19);
            this.bucketLoggingCheckBox.TabIndex = 4;
            this.bucketLoggingCheckBox.Text = "Bucket Access Logging";
            this.bucketLoggingCheckBox.UseVisualStyleBackColor = true;
            this.bucketLoggingCheckBox.CheckedChanged += new System.EventHandler(this.bucketLoggingCheckBox_CheckedChanged);
            // 
            // bucketVersioningCheckBox
            // 
            this.bucketVersioningCheckBox.Anchor = System.Windows.Forms.AnchorStyles.Left;
            this.bucketVersioningCheckBox.AutoSize = true;
            this.s3LayoutPanel.SetColumnSpan(this.bucketVersioningCheckBox, 2);
            this.bucketVersioningCheckBox.Location = new System.Drawing.Point(96, 336);
            this.bucketVersioningCheckBox.Name = "bucketVersioningCheckBox";
            this.bucketVersioningCheckBox.Size = new System.Drawing.Size(121, 19);
            this.bucketVersioningCheckBox.TabIndex = 27;
            this.bucketVersioningCheckBox.Text = "Bucket Versioning";
            this.bucketVersioningCheckBox.UseVisualStyleBackColor = true;
            this.bucketVersioningCheckBox.CheckedChanged += new System.EventHandler(this.bucketVersioningCheckBox_CheckedChanged);
            // 
            // label21
            // 
            this.label21.Anchor = System.Windows.Forms.AnchorStyles.Left;
            this.label21.AutoSize = true;
            this.s3LayoutPanel.SetColumnSpan(this.label21, 2);
            this.label21.ForeColor = System.Drawing.SystemColors.GrayText;
            this.label21.Location = new System.Drawing.Point(96, 358);
            this.label21.Name = "label21";
            this.label21.Size = new System.Drawing.Size(373, 30);
            this.label21.TabIndex = 28;
            this.label21.Text = "You can view all revisions of a file in the browser by choosing View → Show Hidde" +
    "n Files.";
            // 
            // bucketMfaCheckBox
            // 
            this.bucketMfaCheckBox.Anchor = System.Windows.Forms.AnchorStyles.Left;
            this.bucketMfaCheckBox.AutoSize = true;
            this.s3LayoutPanel.SetColumnSpan(this.bucketMfaCheckBox, 2);
            this.bucketMfaCheckBox.Location = new System.Drawing.Point(96, 401);
            this.bucketMfaCheckBox.Name = "bucketMfaCheckBox";
            this.bucketMfaCheckBox.Size = new System.Drawing.Size(246, 19);
            this.bucketMfaCheckBox.TabIndex = 29;
            this.bucketMfaCheckBox.Text = "Multi-Factor Authentication (MFA) Delete";
            this.bucketMfaCheckBox.UseVisualStyleBackColor = true;
            this.bucketMfaCheckBox.CheckedChanged += new System.EventHandler(this.bucketMfaCheckBox_CheckedChanged);
            // 
            // label31
            // 
            this.label31.Anchor = System.Windows.Forms.AnchorStyles.Left;
            this.label31.AutoSize = true;
            this.s3LayoutPanel.SetColumnSpan(this.label31, 2);
            this.label31.Location = new System.Drawing.Point(13, 186);
            this.label31.Name = "label31";
            this.label31.Size = new System.Drawing.Size(51, 15);
            this.label31.TabIndex = 31;
            this.label31.Text = "Logging";
            this.label31.TextAlign = System.Drawing.ContentAlignment.MiddleLeft;
            // 
            // label32
            // 
            this.label32.Anchor = System.Windows.Forms.AnchorStyles.Left;
            this.label32.AutoSize = true;
            this.s3LayoutPanel.SetColumnSpan(this.label32, 2);
            this.label32.Location = new System.Drawing.Point(13, 338);
            this.label32.Name = "label32";
            this.label32.Size = new System.Drawing.Size(63, 15);
            this.label32.TabIndex = 32;
            this.label32.Text = "Versioning";
            this.label32.TextAlign = System.Drawing.ContentAlignment.MiddleLeft;
            // 
            // label33
            // 
            this.label33.AutoSize = true;
            this.s3LayoutPanel.SetColumnSpan(this.label33, 2);
            this.label33.ForeColor = System.Drawing.SystemColors.GrayText;
            this.label33.Location = new System.Drawing.Point(96, 206);
            this.label33.Name = "label33";
            this.label33.Padding = new System.Windows.Forms.Padding(0, 5, 0, 0);
            this.label33.Size = new System.Drawing.Size(213, 20);
            this.label33.TabIndex = 37;
            this.label33.Text = "Write access logs to selected container.";
            // 
            // bucketLoggingComboBox
            // 
            this.bucketLoggingComboBox.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Left | System.Windows.Forms.AnchorStyles.Right)));
            this.s3LayoutPanel.SetColumnSpan(this.bucketLoggingComboBox, 2);
            this.bucketLoggingComboBox.DropDownStyle = System.Windows.Forms.ComboBoxStyle.DropDownList;
            this.bucketLoggingComboBox.FormattingEnabled = true;
            this.bucketLoggingComboBox.Location = new System.Drawing.Point(96, 229);
            this.bucketLoggingComboBox.Name = "bucketLoggingComboBox";
            this.bucketLoggingComboBox.Size = new System.Drawing.Size(391, 23);
            this.bucketLoggingComboBox.TabIndex = 38;
            this.bucketLoggingComboBox.SelectionChangeCommitted += new System.EventHandler(this.bucketLoggingComboBox_SelectionChangeCommitted);
            // 
            // label34
            // 
            this.label34.Anchor = System.Windows.Forms.AnchorStyles.Left;
            this.label34.AutoSize = true;
            this.s3LayoutPanel.SetColumnSpan(this.label34, 2);
            this.label34.Location = new System.Drawing.Point(13, 85);
            this.label34.Name = "label34";
            this.label34.Size = new System.Drawing.Size(64, 15);
            this.label34.TabIndex = 39;
            this.label34.Text = "Encryption";
            this.label34.TextAlign = System.Drawing.ContentAlignment.MiddleLeft;
            // 
            // label35
            // 
            this.label35.Anchor = System.Windows.Forms.AnchorStyles.Left;
            this.label35.AutoSize = true;
            this.s3LayoutPanel.SetColumnSpan(this.label35, 2);
            this.label35.Location = new System.Drawing.Point(13, 270);
            this.label35.Name = "label35";
            this.label35.Size = new System.Drawing.Size(55, 15);
            this.label35.TabIndex = 40;
            this.label35.Text = "Analytics";
            this.label35.TextAlign = System.Drawing.ContentAlignment.MiddleLeft;
            // 
            // bucketAnalyticsCheckBox
            // 
            this.bucketAnalyticsCheckBox.Anchor = System.Windows.Forms.AnchorStyles.Left;
            this.bucketAnalyticsCheckBox.AutoSize = true;
            this.s3LayoutPanel.SetColumnSpan(this.bucketAnalyticsCheckBox, 2);
            this.bucketAnalyticsCheckBox.Location = new System.Drawing.Point(96, 268);
            this.bucketAnalyticsCheckBox.Name = "bucketAnalyticsCheckBox";
            this.bucketAnalyticsCheckBox.Size = new System.Drawing.Size(164, 19);
            this.bucketAnalyticsCheckBox.TabIndex = 41;
            this.bucketAnalyticsCheckBox.Text = "Read Access for Qloudstat";
            this.bucketAnalyticsCheckBox.UseVisualStyleBackColor = true;
            this.bucketAnalyticsCheckBox.CheckedChanged += new System.EventHandler(this.bucketAnalyticsCheckBox_CheckedChanged);
            // 
            // bucketAnalyticsSetupUrlLinkLabel
            // 
            this.bucketAnalyticsSetupUrlLinkLabel.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Left | System.Windows.Forms.AnchorStyles.Right)));
            this.bucketAnalyticsSetupUrlLinkLabel.AutoEllipsis = true;
            this.s3LayoutPanel.SetColumnSpan(this.bucketAnalyticsSetupUrlLinkLabel, 2);
            this.bucketAnalyticsSetupUrlLinkLabel.Location = new System.Drawing.Point(96, 290);
            this.bucketAnalyticsSetupUrlLinkLabel.Margin = new System.Windows.Forms.Padding(3, 0, 3, 5);
            this.bucketAnalyticsSetupUrlLinkLabel.Name = "bucketAnalyticsSetupUrlLinkLabel";
            this.bucketAnalyticsSetupUrlLinkLabel.Size = new System.Drawing.Size(391, 13);
            this.bucketAnalyticsSetupUrlLinkLabel.TabIndex = 42;
            this.bucketAnalyticsSetupUrlLinkLabel.TabStop = true;
            this.bucketAnalyticsSetupUrlLinkLabel.Text = "linkLabel2";
            this.bucketAnalyticsSetupUrlLinkLabel.TextAlign = System.Drawing.ContentAlignment.MiddleLeft;
            // 
            // label36
            // 
            this.label36.Anchor = System.Windows.Forms.AnchorStyles.Left;
            this.label36.AutoSize = true;
            this.s3LayoutPanel.SetColumnSpan(this.label36, 2);
            this.label36.ForeColor = System.Drawing.SystemColors.GrayText;
            this.label36.Location = new System.Drawing.Point(96, 308);
            this.label36.Name = "label36";
            this.label36.Size = new System.Drawing.Size(279, 15);
            this.label36.TabIndex = 43;
            this.label36.Text = "Open the URL to setup log analytics with Qloudstat.";
            // 
            // lifecycleTransitionCheckBox
            // 
            this.lifecycleTransitionCheckBox.Anchor = System.Windows.Forms.AnchorStyles.Left;
            this.lifecycleTransitionCheckBox.AutoSize = true;
            this.s3LayoutPanel.SetColumnSpan(this.lifecycleTransitionCheckBox, 2);
            this.lifecycleTransitionCheckBox.Location = new System.Drawing.Point(96, 436);
            this.lifecycleTransitionCheckBox.Name = "lifecycleTransitionCheckBox";
            this.lifecycleTransitionCheckBox.Size = new System.Drawing.Size(132, 19);
            this.lifecycleTransitionCheckBox.TabIndex = 45;
            this.lifecycleTransitionCheckBox.Text = "Transition to Glacier";
            this.lifecycleTransitionCheckBox.UseVisualStyleBackColor = true;
            this.lifecycleTransitionCheckBox.CheckedChanged += new System.EventHandler(this.lifecycleTransitionCheckBox_CheckedChanged);
            // 
            // label38
            // 
            this.label38.Anchor = System.Windows.Forms.AnchorStyles.Left;
            this.label38.AutoSize = true;
            this.s3LayoutPanel.SetColumnSpan(this.label38, 2);
            this.label38.Location = new System.Drawing.Point(13, 438);
            this.label38.Name = "label38";
            this.label38.Size = new System.Drawing.Size(53, 15);
            this.label38.TabIndex = 44;
            this.label38.Text = "Lifecycle";
            this.label38.TextAlign = System.Drawing.ContentAlignment.MiddleLeft;
            // 
            // lifecycleTransitionComboBox
            // 
            this.lifecycleTransitionComboBox.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Left | System.Windows.Forms.AnchorStyles.Right)));
            this.s3LayoutPanel.SetColumnSpan(this.lifecycleTransitionComboBox, 2);
            this.lifecycleTransitionComboBox.DropDownStyle = System.Windows.Forms.ComboBoxStyle.DropDownList;
            this.lifecycleTransitionComboBox.FormattingEnabled = true;
            this.lifecycleTransitionComboBox.Location = new System.Drawing.Point(96, 461);
            this.lifecycleTransitionComboBox.Name = "lifecycleTransitionComboBox";
            this.lifecycleTransitionComboBox.Size = new System.Drawing.Size(391, 23);
            this.lifecycleTransitionComboBox.TabIndex = 46;
            this.lifecycleTransitionComboBox.SelectionChangeCommitted += new System.EventHandler(this.lifecycleTransitionComboBox_SelectionChangeCommitted);
            // 
            // lifecycleDeleteCheckBox
            // 
            this.lifecycleDeleteCheckBox.AutoSize = true;
            this.s3LayoutPanel.SetColumnSpan(this.lifecycleDeleteCheckBox, 2);
            this.lifecycleDeleteCheckBox.Location = new System.Drawing.Point(96, 490);
            this.lifecycleDeleteCheckBox.Name = "lifecycleDeleteCheckBox";
            this.lifecycleDeleteCheckBox.Size = new System.Drawing.Size(83, 19);
            this.lifecycleDeleteCheckBox.TabIndex = 47;
            this.lifecycleDeleteCheckBox.Text = "Delete files";
            this.lifecycleDeleteCheckBox.UseVisualStyleBackColor = true;
            this.lifecycleDeleteCheckBox.CheckedChanged += new System.EventHandler(this.lifecycleDeleteCheckBox_CheckedChanged);
            // 
            // lifecycleDeleteComboBox
            // 
            this.lifecycleDeleteComboBox.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Left | System.Windows.Forms.AnchorStyles.Right)));
            this.s3LayoutPanel.SetColumnSpan(this.lifecycleDeleteComboBox, 2);
            this.lifecycleDeleteComboBox.DropDownStyle = System.Windows.Forms.ComboBoxStyle.DropDownList;
            this.lifecycleDeleteComboBox.FormattingEnabled = true;
            this.lifecycleDeleteComboBox.Location = new System.Drawing.Point(96, 515);
            this.lifecycleDeleteComboBox.Name = "lifecycleDeleteComboBox";
            this.lifecycleDeleteComboBox.Size = new System.Drawing.Size(391, 23);
            this.lifecycleDeleteComboBox.TabIndex = 48;
            this.lifecycleDeleteComboBox.SelectionChangeCommitted += new System.EventHandler(this.lifecycleDeleteComboBox_SelectionChangeCommitted);
            // 
            // panelManager
            // 
            this.panelManager.Controls.Add(this.managedDistributionPanel);
            this.panelManager.Controls.Add(this.managedGeneralPanel);
            this.panelManager.Controls.Add(this.managedMetadataPanel);
            this.panelManager.Controls.Add(this.managedPermissionsPanel);
            this.panelManager.Controls.Add(this.managedS3Panel);
            this.panelManager.Dock = System.Windows.Forms.DockStyle.Fill;
            this.panelManager.Location = new System.Drawing.Point(0, 56);
            this.panelManager.Name = "panelManager";
            this.panelManager.SelectedIndex = 4;
            this.panelManager.SelectedPanel = this.managedS3Panel;
            this.panelManager.Size = new System.Drawing.Size(500, 548);
            this.panelManager.TabIndex = 2;
            // 
            // managedDistributionPanel
            // 
            this.managedDistributionPanel.Controls.Add(this.distributionLayoutPanel);
            this.managedDistributionPanel.Location = new System.Drawing.Point(0, 0);
            this.managedDistributionPanel.Name = "managedDistributionPanel";
            this.managedDistributionPanel.Size = new System.Drawing.Size(0, 0);
            this.managedDistributionPanel.Text = "managedPanel1";
            // 
            // managedGeneralPanel
            // 
            this.managedGeneralPanel.Controls.Add(this.generalLayoutPanel);
            this.managedGeneralPanel.Location = new System.Drawing.Point(0, 0);
            this.managedGeneralPanel.Name = "managedGeneralPanel";
            this.managedGeneralPanel.Size = new System.Drawing.Size(0, 0);
            this.managedGeneralPanel.Text = "managedPanel1";
            // 
            // managedMetadataPanel
            // 
            this.managedMetadataPanel.Controls.Add(this.metadataTableLayoutPanel);
            this.managedMetadataPanel.Location = new System.Drawing.Point(0, 0);
            this.managedMetadataPanel.Name = "managedMetadataPanel";
            this.managedMetadataPanel.Size = new System.Drawing.Size(0, 0);
            this.managedMetadataPanel.Text = "managedPanel1";
            // 
            // metadataTableLayoutPanel
            // 
            this.metadataTableLayoutPanel.ColumnCount = 4;
            this.metadataTableLayoutPanel.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle(System.Windows.Forms.SizeType.Absolute, 140F));
            this.metadataTableLayoutPanel.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle(System.Windows.Forms.SizeType.Percent, 33.33332F));
            this.metadataTableLayoutPanel.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle(System.Windows.Forms.SizeType.Percent, 33.33334F));
            this.metadataTableLayoutPanel.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle(System.Windows.Forms.SizeType.Percent, 33.33334F));
            this.metadataTableLayoutPanel.Controls.Add(this.label30, 0, 0);
            this.metadataTableLayoutPanel.Controls.Add(this.metadataDataGridView, 0, 2);
            this.metadataTableLayoutPanel.Controls.Add(this.addHeaderButton, 0, 3);
            this.metadataTableLayoutPanel.Controls.Add(this.metadataAnimation, 3, 0);
            this.metadataTableLayoutPanel.Dock = System.Windows.Forms.DockStyle.Fill;
            this.metadataTableLayoutPanel.Location = new System.Drawing.Point(0, 0);
            this.metadataTableLayoutPanel.Name = "metadataTableLayoutPanel";
            this.metadataTableLayoutPanel.Padding = new System.Windows.Forms.Padding(10);
            this.metadataTableLayoutPanel.RowCount = 4;
            this.metadataTableLayoutPanel.RowStyles.Add(new System.Windows.Forms.RowStyle(System.Windows.Forms.SizeType.Absolute, 25F));
            this.metadataTableLayoutPanel.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.metadataTableLayoutPanel.RowStyles.Add(new System.Windows.Forms.RowStyle(System.Windows.Forms.SizeType.Percent, 100F));
            this.metadataTableLayoutPanel.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.metadataTableLayoutPanel.RowStyles.Add(new System.Windows.Forms.RowStyle(System.Windows.Forms.SizeType.Absolute, 20F));
            this.metadataTableLayoutPanel.RowStyles.Add(new System.Windows.Forms.RowStyle(System.Windows.Forms.SizeType.Absolute, 20F));
            this.metadataTableLayoutPanel.Size = new System.Drawing.Size(0, 0);
            this.metadataTableLayoutPanel.TabIndex = 1;
            // 
            // label30
            // 
            this.label30.AutoSize = true;
            this.metadataTableLayoutPanel.SetColumnSpan(this.label30, 3);
            this.label30.Location = new System.Drawing.Point(13, 10);
            this.label30.Name = "label30";
            this.label30.Padding = new System.Windows.Forms.Padding(0, 10, 0, 0);
            this.label30.Size = new System.Drawing.Size(41, 25);
            this.label30.TabIndex = 27;
            this.label30.Text = "Headers";
            // 
            // metadataDataGridView
            // 
            this.metadataDataGridView.BorderStyle = System.Windows.Forms.BorderStyle.None;
            this.metadataDataGridView.ColumnHeadersHeightSizeMode = System.Windows.Forms.DataGridViewColumnHeadersHeightSizeMode.AutoSize;
            this.metadataTableLayoutPanel.SetColumnSpan(this.metadataDataGridView, 4);
            this.metadataDataGridView.Dock = System.Windows.Forms.DockStyle.Fill;
            this.metadataDataGridView.Location = new System.Drawing.Point(13, 38);
            this.metadataDataGridView.Name = "metadataDataGridView";
            this.metadataDataGridView.Size = new System.Drawing.Size(1, 1);
            this.metadataDataGridView.TabIndex = 29;
            // 
            // addHeaderButton
            // 
            this.addHeaderButton.AutoSize = true;
            this.addHeaderButton.AutoSizeMode = System.Windows.Forms.AutoSizeMode.GrowAndShrink;
            this.addHeaderButton.ContextMenuStrip = this.addMetadataContextMenuStrip;
            this.addHeaderButton.Image = global::Ch.Cyberduck.ResourcesBundle.gear;
            this.addHeaderButton.Location = new System.Drawing.Point(13, -16);
            this.addHeaderButton.Name = "addHeaderButton";
            this.addHeaderButton.Size = new System.Drawing.Size(24, 6);
            this.addHeaderButton.SplitMenuStrip = this.addMetadataContextMenuStrip;
            this.addHeaderButton.TabIndex = 30;
            this.addHeaderButton.UseVisualStyleBackColor = true;
            // 
            // addMetadataContextMenuStrip
            // 
            this.addMetadataContextMenuStrip.Items.AddRange(new System.Windows.Forms.ToolStripItem[] {
            this.toolStripMenuItem1,
            this.toolStripMenuItem2,
            this.toolStripSeparator2,
            this.toolStripMenuItem3});
            this.addMetadataContextMenuStrip.Name = "contextMenuStrip1";
            this.addMetadataContextMenuStrip.Size = new System.Drawing.Size(165, 76);
            this.addMetadataContextMenuStrip.Opening += new System.ComponentModel.CancelEventHandler(this.addMetadataContextMenuStrip_Opening);
            // 
            // toolStripMenuItem1
            // 
            this.toolStripMenuItem1.Name = "toolStripMenuItem1";
            this.toolStripMenuItem1.Size = new System.Drawing.Size(164, 22);
            this.toolStripMenuItem1.Text = "yves (Owner)";
            // 
            // toolStripMenuItem2
            // 
            this.toolStripMenuItem2.Name = "toolStripMenuItem2";
            this.toolStripMenuItem2.Size = new System.Drawing.Size(164, 22);
            this.toolStripMenuItem2.Text = "Canoncal User ID";
            // 
            // toolStripSeparator2
            // 
            this.toolStripSeparator2.Name = "toolStripSeparator2";
            this.toolStripSeparator2.Size = new System.Drawing.Size(161, 6);
            // 
            // toolStripMenuItem3
            // 
            this.toolStripMenuItem3.Name = "toolStripMenuItem3";
            this.toolStripMenuItem3.Size = new System.Drawing.Size(164, 22);
            this.toolStripMenuItem3.Text = "Remove";
            // 
            // metadataAnimation
            // 
            this.metadataAnimation.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Right)));
            this.metadataAnimation.Image = global::Ch.Cyberduck.ResourcesBundle.throbber_small;
            this.metadataAnimation.Location = new System.Drawing.Point(61, 13);
            this.metadataAnimation.Name = "metadataAnimation";
            this.metadataAnimation.Size = new System.Drawing.Size(1, 18);
            this.metadataAnimation.SizeMode = System.Windows.Forms.PictureBoxSizeMode.CenterImage;
            this.metadataAnimation.TabIndex = 31;
            this.metadataAnimation.TabStop = false;
            this.metadataAnimation.Visible = false;
            // 
            // managedPermissionsPanel
            // 
            this.managedPermissionsPanel.Controls.Add(this.panelManagerPermissions);
            this.managedPermissionsPanel.Location = new System.Drawing.Point(0, 0);
            this.managedPermissionsPanel.Name = "managedPermissionsPanel";
            this.managedPermissionsPanel.Size = new System.Drawing.Size(0, 0);
            this.managedPermissionsPanel.Text = "managedPanel2";
            // 
            // panelManagerPermissions
            // 
            this.panelManagerPermissions.Controls.Add(this.nonCloudManagedPanel);
            this.panelManagerPermissions.Controls.Add(this.cloudManagedPanel);
            this.panelManagerPermissions.Dock = System.Windows.Forms.DockStyle.Fill;
            this.panelManagerPermissions.Location = new System.Drawing.Point(0, 0);
            this.panelManagerPermissions.Name = "panelManagerPermissions";
            this.panelManagerPermissions.SelectedIndex = 0;
            this.panelManagerPermissions.SelectedPanel = this.nonCloudManagedPanel;
            this.panelManagerPermissions.Size = new System.Drawing.Size(0, 0);
            this.panelManagerPermissions.TabIndex = 1;
            // 
            // nonCloudManagedPanel
            // 
            this.nonCloudManagedPanel.Controls.Add(this.permissionsLayoutPanel);
            this.nonCloudManagedPanel.Location = new System.Drawing.Point(0, 0);
            this.nonCloudManagedPanel.Name = "nonCloudManagedPanel";
            this.nonCloudManagedPanel.Size = new System.Drawing.Size(0, 0);
            // 
            // permissionsLayoutPanel
            // 
            this.permissionsLayoutPanel.ColumnCount = 4;
            this.permissionsLayoutPanel.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle(System.Windows.Forms.SizeType.Absolute, 140F));
            this.permissionsLayoutPanel.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle(System.Windows.Forms.SizeType.Percent, 33.33332F));
            this.permissionsLayoutPanel.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle(System.Windows.Forms.SizeType.Percent, 33.33334F));
            this.permissionsLayoutPanel.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle(System.Windows.Forms.SizeType.Percent, 33.33334F));
            this.permissionsLayoutPanel.Controls.Add(this.label10, 0, 2);
            this.permissionsLayoutPanel.Controls.Add(this.ownerrCheckBox, 1, 2);
            this.permissionsLayoutPanel.Controls.Add(this.ownerwCheckBox, 2, 2);
            this.permissionsLayoutPanel.Controls.Add(this.ownerxCheckBox, 3, 2);
            this.permissionsLayoutPanel.Controls.Add(this.label11, 0, 3);
            this.permissionsLayoutPanel.Controls.Add(this.grouprCheckbox, 1, 3);
            this.permissionsLayoutPanel.Controls.Add(this.groupwCheckbox, 2, 3);
            this.permissionsLayoutPanel.Controls.Add(this.groupxCheckbox, 3, 3);
            this.permissionsLayoutPanel.Controls.Add(this.label12, 0, 4);
            this.permissionsLayoutPanel.Controls.Add(this.otherwCheckbox, 2, 4);
            this.permissionsLayoutPanel.Controls.Add(this.otherxCheckbox, 3, 4);
            this.permissionsLayoutPanel.Controls.Add(this.permissionAnimation, 3, 0);
            this.permissionsLayoutPanel.Controls.Add(this.otherrCheckbox, 1, 4);
            this.permissionsLayoutPanel.Controls.Add(this.applyRecursivePermissionsButton, 2, 6);
            this.permissionsLayoutPanel.Controls.Add(this.label23, 0, 0);
            this.permissionsLayoutPanel.Controls.Add(this.octalTextBox, 1, 0);
            this.permissionsLayoutPanel.Dock = System.Windows.Forms.DockStyle.Fill;
            this.permissionsLayoutPanel.Location = new System.Drawing.Point(0, 0);
            this.permissionsLayoutPanel.Name = "permissionsLayoutPanel";
            this.permissionsLayoutPanel.Padding = new System.Windows.Forms.Padding(10);
            this.permissionsLayoutPanel.RowCount = 7;
            this.permissionsLayoutPanel.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.permissionsLayoutPanel.RowStyles.Add(new System.Windows.Forms.RowStyle(System.Windows.Forms.SizeType.Absolute, 20F));
            this.permissionsLayoutPanel.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.permissionsLayoutPanel.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.permissionsLayoutPanel.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.permissionsLayoutPanel.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.permissionsLayoutPanel.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.permissionsLayoutPanel.RowStyles.Add(new System.Windows.Forms.RowStyle(System.Windows.Forms.SizeType.Absolute, 20F));
            this.permissionsLayoutPanel.Size = new System.Drawing.Size(0, 0);
            this.permissionsLayoutPanel.TabIndex = 0;
            // 
            // label10
            // 
            this.label10.AutoSize = true;
            this.label10.Location = new System.Drawing.Point(13, 59);
            this.label10.Name = "label10";
            this.label10.Padding = new System.Windows.Forms.Padding(0, 5, 0, 0);
            this.label10.Size = new System.Drawing.Size(42, 20);
            this.label10.TabIndex = 0;
            this.label10.Text = "Owner";
            // 
            // ownerrCheckBox
            // 
            this.ownerrCheckBox.AutoSize = true;
            this.ownerrCheckBox.Location = new System.Drawing.Point(153, 62);
            this.ownerrCheckBox.Name = "ownerrCheckBox";
            this.ownerrCheckBox.Size = new System.Drawing.Size(1, 19);
            this.ownerrCheckBox.TabIndex = 1;
            this.ownerrCheckBox.Text = "Read";
            this.ownerrCheckBox.ThreeState = true;
            this.ownerrCheckBox.UseVisualStyleBackColor = true;
            this.ownerrCheckBox.CheckStateChanged += new System.EventHandler(this.ownerrCheckBox_CheckStateChanged);
            // 
            // ownerwCheckBox
            // 
            this.ownerwCheckBox.AutoSize = true;
            this.ownerwCheckBox.Location = new System.Drawing.Point(107, 62);
            this.ownerwCheckBox.Name = "ownerwCheckBox";
            this.ownerwCheckBox.Size = new System.Drawing.Size(1, 19);
            this.ownerwCheckBox.TabIndex = 2;
            this.ownerwCheckBox.Text = "Write";
            this.ownerwCheckBox.ThreeState = true;
            this.ownerwCheckBox.UseVisualStyleBackColor = true;
            this.ownerwCheckBox.CheckStateChanged += new System.EventHandler(this.ownerwCheckBox_CheckStateChanged);
            // 
            // ownerxCheckBox
            // 
            this.ownerxCheckBox.AutoSize = true;
            this.ownerxCheckBox.Location = new System.Drawing.Point(61, 62);
            this.ownerxCheckBox.Name = "ownerxCheckBox";
            this.ownerxCheckBox.Size = new System.Drawing.Size(1, 19);
            this.ownerxCheckBox.TabIndex = 3;
            this.ownerxCheckBox.Text = "Execute";
            this.ownerxCheckBox.ThreeState = true;
            this.ownerxCheckBox.UseVisualStyleBackColor = true;
            this.ownerxCheckBox.CheckStateChanged += new System.EventHandler(this.ownerxCheckBox_CheckStateChanged);
            // 
            // label11
            // 
            this.label11.AutoSize = true;
            this.label11.Location = new System.Drawing.Point(13, 84);
            this.label11.Name = "label11";
            this.label11.Padding = new System.Windows.Forms.Padding(0, 5, 0, 0);
            this.label11.Size = new System.Drawing.Size(40, 20);
            this.label11.TabIndex = 4;
            this.label11.Text = "Group";
            // 
            // grouprCheckbox
            // 
            this.grouprCheckbox.AutoSize = true;
            this.grouprCheckbox.Location = new System.Drawing.Point(153, 87);
            this.grouprCheckbox.Name = "grouprCheckbox";
            this.grouprCheckbox.Size = new System.Drawing.Size(1, 19);
            this.grouprCheckbox.TabIndex = 5;
            this.grouprCheckbox.Text = "Read";
            this.grouprCheckbox.ThreeState = true;
            this.grouprCheckbox.UseVisualStyleBackColor = true;
            this.grouprCheckbox.CheckStateChanged += new System.EventHandler(this.grouprCheckbox_CheckStateChanged);
            // 
            // groupwCheckbox
            // 
            this.groupwCheckbox.AutoSize = true;
            this.groupwCheckbox.Location = new System.Drawing.Point(107, 87);
            this.groupwCheckbox.Name = "groupwCheckbox";
            this.groupwCheckbox.Size = new System.Drawing.Size(1, 19);
            this.groupwCheckbox.TabIndex = 6;
            this.groupwCheckbox.Text = "Write";
            this.groupwCheckbox.ThreeState = true;
            this.groupwCheckbox.UseVisualStyleBackColor = true;
            this.groupwCheckbox.CheckStateChanged += new System.EventHandler(this.groupwCheckbox_CheckStateChanged);
            // 
            // groupxCheckbox
            // 
            this.groupxCheckbox.AutoSize = true;
            this.groupxCheckbox.Location = new System.Drawing.Point(61, 87);
            this.groupxCheckbox.Name = "groupxCheckbox";
            this.groupxCheckbox.Size = new System.Drawing.Size(1, 19);
            this.groupxCheckbox.TabIndex = 7;
            this.groupxCheckbox.Text = "Execute";
            this.groupxCheckbox.ThreeState = true;
            this.groupxCheckbox.UseVisualStyleBackColor = true;
            this.groupxCheckbox.CheckStateChanged += new System.EventHandler(this.groupxCheckbox_CheckStateChanged);
            // 
            // label12
            // 
            this.label12.AutoSize = true;
            this.label12.Location = new System.Drawing.Point(13, 109);
            this.label12.Name = "label12";
            this.label12.Padding = new System.Windows.Forms.Padding(0, 6, 0, 0);
            this.label12.Size = new System.Drawing.Size(42, 21);
            this.label12.TabIndex = 8;
            this.label12.Text = "Others";
            // 
            // otherwCheckbox
            // 
            this.otherwCheckbox.AutoSize = true;
            this.otherwCheckbox.Location = new System.Drawing.Point(107, 112);
            this.otherwCheckbox.Name = "otherwCheckbox";
            this.otherwCheckbox.Size = new System.Drawing.Size(1, 19);
            this.otherwCheckbox.TabIndex = 10;
            this.otherwCheckbox.Text = "Write";
            this.otherwCheckbox.ThreeState = true;
            this.otherwCheckbox.UseVisualStyleBackColor = true;
            this.otherwCheckbox.CheckStateChanged += new System.EventHandler(this.otherwCheckbox_CheckStateChanged);
            // 
            // otherxCheckbox
            // 
            this.otherxCheckbox.AutoSize = true;
            this.otherxCheckbox.Location = new System.Drawing.Point(61, 112);
            this.otherxCheckbox.Name = "otherxCheckbox";
            this.otherxCheckbox.Size = new System.Drawing.Size(1, 19);
            this.otherxCheckbox.TabIndex = 11;
            this.otherxCheckbox.Text = "Execute";
            this.otherxCheckbox.ThreeState = true;
            this.otherxCheckbox.UseVisualStyleBackColor = true;
            this.otherxCheckbox.CheckStateChanged += new System.EventHandler(this.otherxCheckbox_CheckStateChanged);
            // 
            // permissionAnimation
            // 
            this.permissionAnimation.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Right)));
            this.permissionAnimation.Image = global::Ch.Cyberduck.ResourcesBundle.throbber_small;
            this.permissionAnimation.Location = new System.Drawing.Point(61, 13);
            this.permissionAnimation.Name = "permissionAnimation";
            this.permissionAnimation.Size = new System.Drawing.Size(1, 18);
            this.permissionAnimation.TabIndex = 23;
            this.permissionAnimation.TabStop = false;
            this.permissionAnimation.Visible = false;
            // 
            // otherrCheckbox
            // 
            this.otherrCheckbox.AutoSize = true;
            this.otherrCheckbox.Location = new System.Drawing.Point(153, 112);
            this.otherrCheckbox.Name = "otherrCheckbox";
            this.otherrCheckbox.Size = new System.Drawing.Size(1, 19);
            this.otherrCheckbox.TabIndex = 9;
            this.otherrCheckbox.Text = "Read";
            this.otherrCheckbox.ThreeState = true;
            this.otherrCheckbox.UseVisualStyleBackColor = true;
            this.otherrCheckbox.CheckStateChanged += new System.EventHandler(this.otherrCheckbox_CheckStateChanged);
            // 
            // applyRecursivePermissionsButton
            // 
            this.applyRecursivePermissionsButton.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Right)));
            this.applyRecursivePermissionsButton.AutoSize = true;
            this.permissionsLayoutPanel.SetColumnSpan(this.applyRecursivePermissionsButton, 2);
            this.applyRecursivePermissionsButton.Location = new System.Drawing.Point(107, 137);
            this.applyRecursivePermissionsButton.Name = "applyRecursivePermissionsButton";
            this.applyRecursivePermissionsButton.Size = new System.Drawing.Size(1, 27);
            this.applyRecursivePermissionsButton.TabIndex = 28;
            this.applyRecursivePermissionsButton.Text = "Apply changes recursively";
            this.applyRecursivePermissionsButton.UseVisualStyleBackColor = true;
            this.applyRecursivePermissionsButton.Click += new System.EventHandler(this.applyRecursivePermissionsButton_Click);
            // 
            // label23
            // 
            this.label23.Anchor = System.Windows.Forms.AnchorStyles.Left;
            this.label23.AutoSize = true;
            this.label23.Location = new System.Drawing.Point(13, 17);
            this.label23.Name = "label23";
            this.label23.Size = new System.Drawing.Size(96, 15);
            this.label23.TabIndex = 25;
            this.label23.Text = "Unix Permissions";
            // 
            // octalTextBox
            // 
            this.octalTextBox.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Left | System.Windows.Forms.AnchorStyles.Right)));
            this.octalTextBox.Location = new System.Drawing.Point(156, 13);
            this.octalTextBox.Margin = new System.Windows.Forms.Padding(6, 3, 3, 3);
            this.octalTextBox.Name = "octalTextBox";
            this.octalTextBox.Size = new System.Drawing.Size(1, 23);
            this.octalTextBox.TabIndex = 24;
            this.octalTextBox.Validated += new System.EventHandler(this.octalTextBox_Validated);
            // 
            // cloudManagedPanel
            // 
            this.cloudManagedPanel.Controls.Add(this.tableLayoutPanel1);
            this.cloudManagedPanel.Location = new System.Drawing.Point(0, 0);
            this.cloudManagedPanel.Name = "cloudManagedPanel";
            this.cloudManagedPanel.Size = new System.Drawing.Size(0, 0);
            // 
            // tableLayoutPanel1
            // 
            this.tableLayoutPanel1.ColumnCount = 2;
            this.tableLayoutPanel1.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle(System.Windows.Forms.SizeType.Percent, 100F));
            this.tableLayoutPanel1.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle(System.Windows.Forms.SizeType.Absolute, 24F));
            this.tableLayoutPanel1.Controls.Add(this.AclLabel, 0, 3);
            this.tableLayoutPanel1.Controls.Add(this.aclDataGridView, 0, 4);
            this.tableLayoutPanel1.Controls.Add(this.addAclButton, 0, 5);
            this.tableLayoutPanel1.Controls.Add(this.authenticatedLabel, 0, 1);
            this.tableLayoutPanel1.Controls.Add(this.authenticatedUrlLinkLabel, 0, 2);
            this.tableLayoutPanel1.Controls.Add(this.aclAnimation, 1, 1);
            this.tableLayoutPanel1.Dock = System.Windows.Forms.DockStyle.Fill;
            this.tableLayoutPanel1.Location = new System.Drawing.Point(0, 0);
            this.tableLayoutPanel1.Name = "tableLayoutPanel1";
            this.tableLayoutPanel1.Padding = new System.Windows.Forms.Padding(10);
            this.tableLayoutPanel1.RowCount = 6;
            this.tableLayoutPanel1.RowStyles.Add(new System.Windows.Forms.RowStyle(System.Windows.Forms.SizeType.Absolute, 5F));
            this.tableLayoutPanel1.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel1.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel1.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel1.RowStyles.Add(new System.Windows.Forms.RowStyle(System.Windows.Forms.SizeType.Percent, 100F));
            this.tableLayoutPanel1.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel1.Size = new System.Drawing.Size(0, 0);
            this.tableLayoutPanel1.TabIndex = 0;
            // 
            // AclLabel
            // 
            this.AclLabel.AutoSize = true;
            this.tableLayoutPanel1.SetColumnSpan(this.AclLabel, 2);
            this.AclLabel.Location = new System.Drawing.Point(13, 63);
            this.AclLabel.Name = "AclLabel";
            this.AclLabel.Padding = new System.Windows.Forms.Padding(0, 10, 0, 0);
            this.AclLabel.Size = new System.Drawing.Size(1, 10);
            this.AclLabel.TabIndex = 27;
            this.AclLabel.Text = "Access Control List (ACL)";
            // 
            // aclDataGridView
            // 
            this.aclDataGridView.BorderStyle = System.Windows.Forms.BorderStyle.None;
            this.aclDataGridView.ColumnHeadersHeightSizeMode = System.Windows.Forms.DataGridViewColumnHeadersHeightSizeMode.AutoSize;
            this.tableLayoutPanel1.SetColumnSpan(this.aclDataGridView, 2);
            this.aclDataGridView.Dock = System.Windows.Forms.DockStyle.Fill;
            this.aclDataGridView.Location = new System.Drawing.Point(13, 76);
            this.aclDataGridView.Name = "aclDataGridView";
            this.aclDataGridView.Size = new System.Drawing.Size(1, 1);
            this.aclDataGridView.TabIndex = 29;
            // 
            // addAclButton
            // 
            this.addAclButton.AutoSize = true;
            this.addAclButton.AutoSizeMode = System.Windows.Forms.AutoSizeMode.GrowAndShrink;
            this.addAclButton.ContextMenuStrip = this.addAclContextMenuStrip;
            this.addAclButton.Image = global::Ch.Cyberduck.ResourcesBundle.gear;
            this.addAclButton.Location = new System.Drawing.Point(13, -16);
            this.addAclButton.Name = "addAclButton";
            this.addAclButton.Size = new System.Drawing.Size(1, 6);
            this.addAclButton.SplitMenuStrip = this.addAclContextMenuStrip;
            this.addAclButton.TabIndex = 30;
            this.addAclButton.UseVisualStyleBackColor = true;
            // 
            // addAclContextMenuStrip
            // 
            this.addAclContextMenuStrip.Items.AddRange(new System.Windows.Forms.ToolStripItem[] {
            this.yvesOwnerToolStripMenuItem,
            this.canoncalUserIDToolStripMenuItem,
            this.toolStripSeparator1,
            this.removeToolStripMenuItem});
            this.addAclContextMenuStrip.Name = "contextMenuStrip1";
            this.addAclContextMenuStrip.Size = new System.Drawing.Size(165, 76);
            this.addAclContextMenuStrip.Opening += new System.ComponentModel.CancelEventHandler(this.addAclContextMenuStrip_Opening);
            // 
            // yvesOwnerToolStripMenuItem
            // 
            this.yvesOwnerToolStripMenuItem.Name = "yvesOwnerToolStripMenuItem";
            this.yvesOwnerToolStripMenuItem.Size = new System.Drawing.Size(164, 22);
            this.yvesOwnerToolStripMenuItem.Text = "yves (Owner)";
            // 
            // canoncalUserIDToolStripMenuItem
            // 
            this.canoncalUserIDToolStripMenuItem.Name = "canoncalUserIDToolStripMenuItem";
            this.canoncalUserIDToolStripMenuItem.Size = new System.Drawing.Size(164, 22);
            this.canoncalUserIDToolStripMenuItem.Text = "Canoncal User ID";
            // 
            // toolStripSeparator1
            // 
            this.toolStripSeparator1.Name = "toolStripSeparator1";
            this.toolStripSeparator1.Size = new System.Drawing.Size(161, 6);
            // 
            // removeToolStripMenuItem
            // 
            this.removeToolStripMenuItem.Name = "removeToolStripMenuItem";
            this.removeToolStripMenuItem.Size = new System.Drawing.Size(164, 22);
            this.removeToolStripMenuItem.Text = "Remove";
            // 
            // authenticatedLabel
            // 
            this.authenticatedLabel.AutoSize = true;
            this.authenticatedLabel.Location = new System.Drawing.Point(13, 15);
            this.authenticatedLabel.MinimumSize = new System.Drawing.Size(0, 28);
            this.authenticatedLabel.Name = "authenticatedLabel";
            this.authenticatedLabel.Size = new System.Drawing.Size(1, 28);
            this.authenticatedLabel.TabIndex = 31;
            this.authenticatedLabel.Text = "Authenticated URL";
            // 
            // authenticatedUrlLinkLabel
            // 
            this.authenticatedUrlLinkLabel.AutoSize = true;
            this.tableLayoutPanel1.SetColumnSpan(this.authenticatedUrlLinkLabel, 2);
            this.authenticatedUrlLinkLabel.Location = new System.Drawing.Point(13, 43);
            this.authenticatedUrlLinkLabel.MinimumSize = new System.Drawing.Size(0, 20);
            this.authenticatedUrlLinkLabel.Name = "authenticatedUrlLinkLabel";
            this.authenticatedUrlLinkLabel.Size = new System.Drawing.Size(1, 20);
            this.authenticatedUrlLinkLabel.TabIndex = 32;
            this.authenticatedUrlLinkLabel.TabStop = true;
            this.authenticatedUrlLinkLabel.Text = "linkLabel1";
            // 
            // aclAnimation
            // 
            this.aclAnimation.Anchor = ((System.Windows.Forms.AnchorStyles)(((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Left) 
            | System.Windows.Forms.AnchorStyles.Right)));
            this.aclAnimation.Image = global::Ch.Cyberduck.ResourcesBundle.throbber_small;
            this.aclAnimation.Location = new System.Drawing.Point(-10, 18);
            this.aclAnimation.Name = "aclAnimation";
            this.aclAnimation.Size = new System.Drawing.Size(18, 21);
            this.aclAnimation.SizeMode = System.Windows.Forms.PictureBoxSizeMode.CenterImage;
            this.aclAnimation.TabIndex = 33;
            this.aclAnimation.TabStop = false;
            this.aclAnimation.Visible = false;
            // 
            // managedS3Panel
            // 
            this.managedS3Panel.Controls.Add(this.s3LayoutPanel);
            this.managedS3Panel.Location = new System.Drawing.Point(0, 0);
            this.managedS3Panel.Name = "managedS3Panel";
            this.managedS3Panel.Size = new System.Drawing.Size(500, 548);
            this.managedS3Panel.Text = "managedPanel1";
            // 
            // encryptionComboBox
            // 
            this.encryptionComboBox.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Left | System.Windows.Forms.AnchorStyles.Right)));
            this.s3LayoutPanel.SetColumnSpan(this.encryptionComboBox, 2);
            this.encryptionComboBox.DropDownStyle = System.Windows.Forms.ComboBoxStyle.DropDownList;
            this.encryptionComboBox.FormattingEnabled = true;
            this.encryptionComboBox.Location = new System.Drawing.Point(96, 82);
            this.encryptionComboBox.Name = "encryptionComboBox";
            this.encryptionComboBox.Size = new System.Drawing.Size(391, 23);
            this.encryptionComboBox.TabIndex = 31;
            this.encryptionComboBox.SelectionChangeCommitted += new System.EventHandler(this.encryptionCheckBox_CheckedChanged);
            // 
            // InfoForm
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(7F, 15F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.AutoSize = true;
            this.ClientSize = new System.Drawing.Size(500, 604);
            this.Controls.Add(this.panelManager);
            this.Controls.Add(this.toolStrip);
            this.FormBorderStyle = System.Windows.Forms.FormBorderStyle.SizableToolWindow;
            this.HelpButton = true;
            this.MaximizeBox = false;
            this.MaximumSize = new System.Drawing.Size(998, 799);
            this.MinimizeBox = false;
            this.MinimumSize = new System.Drawing.Size(499, 643);
            this.Name = "InfoForm";
            this.Text = "Info";
            this.toolStrip.ResumeLayout(false);
            this.toolStrip.PerformLayout();
            this.distributionLayoutPanel.ResumeLayout(false);
            this.distributionLayoutPanel.PerformLayout();
            ((System.ComponentModel.ISupportInitialize)(this.distributionAnimation)).EndInit();
            this.generalLayoutPanel.ResumeLayout(false);
            this.generalLayoutPanel.PerformLayout();
            ((System.ComponentModel.ISupportInitialize)(this.icon)).EndInit();
            ((System.ComponentModel.ISupportInitialize)(this.sizeAnimation)).EndInit();
            this.s3LayoutPanel.ResumeLayout(false);
            this.s3LayoutPanel.PerformLayout();
            ((System.ComponentModel.ISupportInitialize)(this.s3Animation)).EndInit();
            this.panelManager.ResumeLayout(false);
            this.managedDistributionPanel.ResumeLayout(false);
            this.managedDistributionPanel.PerformLayout();
            this.managedGeneralPanel.ResumeLayout(false);
            this.managedGeneralPanel.PerformLayout();
            this.managedMetadataPanel.ResumeLayout(false);
            this.metadataTableLayoutPanel.ResumeLayout(false);
            this.metadataTableLayoutPanel.PerformLayout();
            ((System.ComponentModel.ISupportInitialize)(this.metadataDataGridView)).EndInit();
            this.addMetadataContextMenuStrip.ResumeLayout(false);
            ((System.ComponentModel.ISupportInitialize)(this.metadataAnimation)).EndInit();
            this.managedPermissionsPanel.ResumeLayout(false);
            this.panelManagerPermissions.ResumeLayout(false);
            this.nonCloudManagedPanel.ResumeLayout(false);
            this.permissionsLayoutPanel.ResumeLayout(false);
            this.permissionsLayoutPanel.PerformLayout();
            ((System.ComponentModel.ISupportInitialize)(this.permissionAnimation)).EndInit();
            this.cloudManagedPanel.ResumeLayout(false);
            this.tableLayoutPanel1.ResumeLayout(false);
            this.tableLayoutPanel1.PerformLayout();
            ((System.ComponentModel.ISupportInitialize)(this.aclDataGridView)).EndInit();
            this.addAclContextMenuStrip.ResumeLayout(false);
            ((System.ComponentModel.ISupportInitialize)(this.aclAnimation)).EndInit();
            this.managedS3Panel.ResumeLayout(false);
            this.ResumeLayout(false);
            this.PerformLayout();

        }

        #endregion

        private System.Windows.Forms.ToolStripButton generalButton;
        private System.Windows.Forms.ToolStripButton permissionsButton;
        private System.Windows.Forms.TableLayoutPanel generalLayoutPanel;
        private System.Windows.Forms.PictureBox icon;
        private System.Windows.Forms.TextBox filenameTextbox;
        private System.Windows.Forms.Label label1;
        private System.Windows.Forms.Label label2;
        private System.Windows.Forms.Label label3;
        private System.Windows.Forms.Label label4;
        private System.Windows.Forms.Label label5;
        private System.Windows.Forms.Label label6;
        private System.Windows.Forms.Label label7;
        private System.Windows.Forms.Label label8;
        private System.Windows.Forms.Label label9;
        private ClickLinkLabel weburlLabel;
        private System.Windows.Forms.Button calculateButton;
        private System.Windows.Forms.PictureBox sizeAnimation;
        private System.Windows.Forms.ToolTip toolTip;
        private System.Windows.Forms.ToolStripButton distributionButton;
        private System.Windows.Forms.ToolStripButton s3Button;
        private System.Windows.Forms.TableLayoutPanel distributionLayoutPanel;
        private System.Windows.Forms.Label label14;
        private System.Windows.Forms.Label label15;
        private System.Windows.Forms.Label label16;
        private System.Windows.Forms.Label label17;
        private System.Windows.Forms.ComboBox deliveryMethodComboBox;
        private System.Windows.Forms.CheckBox distributionEnableCheckBox;
        private System.Windows.Forms.CheckBox distributionLoggingCheckBox;
        private System.Windows.Forms.Label statusLabel;
        private System.Windows.Forms.TextBox distributionCnameTextBox;
        private System.Windows.Forms.TableLayoutPanel s3LayoutPanel;
        private System.Windows.Forms.Label label18;
        private System.Windows.Forms.Label label19;
        private System.Windows.Forms.Label label20;
        private System.Windows.Forms.CheckBox bucketLoggingCheckBox;
        private System.Windows.Forms.Label bucketLocationLabel;
        private ClickLinkLabel s3PublicUrlLinkLabel;
        private ClickLinkLabel s3TorrentUrlLinkLabel;
        private ClickLinkLabel whereLinkLabel;
        private System.Windows.Forms.PictureBox s3Animation;
        private System.Windows.Forms.PictureBox distributionAnimation;
        private ClickLinkLabel cnameUrlLinkLabel;
        private System.Windows.Forms.ToolStripButton metadataButton;
        private PanelManager panelManager;
        private ManagedPanel managedGeneralPanel;
        private ManagedPanel managedPermissionsPanel;
        private ManagedPanel managedMetadataPanel;
        private ManagedPanel managedDistributionPanel;
        private ManagedPanel managedS3Panel;
        private System.Windows.Forms.Label label22;
        private System.Windows.Forms.Label sizeLabel;
        private System.Windows.Forms.Label pathLabel;
        private System.Windows.Forms.Label kindLabel;
        private System.Windows.Forms.Label permissionsLabel;
        private System.Windows.Forms.Label ownerLabel;
        private System.Windows.Forms.Label groupLabel;
        private System.Windows.Forms.Label modifiedLabel;
        private System.Windows.Forms.Label createdLabel;
        private System.Windows.Forms.TableLayoutPanel permissionsLayoutPanel;
        private System.Windows.Forms.Label label10;
        private System.Windows.Forms.CheckBox ownerrCheckBox;
        private System.Windows.Forms.CheckBox ownerwCheckBox;
        private System.Windows.Forms.CheckBox ownerxCheckBox;
        private System.Windows.Forms.Label label11;
        private System.Windows.Forms.CheckBox grouprCheckbox;
        private System.Windows.Forms.CheckBox groupwCheckbox;
        private System.Windows.Forms.CheckBox groupxCheckbox;
        private System.Windows.Forms.Label label12;
        private System.Windows.Forms.CheckBox otherwCheckbox;
        private System.Windows.Forms.CheckBox otherxCheckbox;
        private System.Windows.Forms.TextBox octalTextBox;
        private System.Windows.Forms.PictureBox permissionAnimation;
        private System.Windows.Forms.CheckBox otherrCheckbox;
        private System.Windows.Forms.Label label23;
        private System.Windows.Forms.Label AclLabel;
        private System.Windows.Forms.Button applyRecursivePermissionsButton;
        private System.Windows.Forms.DataGridView aclDataGridView;
        private SplitButton addAclButton;
        private System.Windows.Forms.ContextMenuStrip addAclContextMenuStrip;
        private System.Windows.Forms.ToolStripMenuItem yvesOwnerToolStripMenuItem;
        private System.Windows.Forms.ToolStripMenuItem canoncalUserIDToolStripMenuItem;
        private System.Windows.Forms.ToolStripSeparator toolStripSeparator1;
        private System.Windows.Forms.ToolStripMenuItem removeToolStripMenuItem;
        private System.Windows.Forms.TableLayoutPanel metadataTableLayoutPanel;
        private System.Windows.Forms.Label label30;
        private System.Windows.Forms.DataGridView metadataDataGridView;
        private SplitButton addHeaderButton;
        private System.Windows.Forms.ContextMenuStrip addMetadataContextMenuStrip;
        private System.Windows.Forms.ToolStripMenuItem toolStripMenuItem1;
        private System.Windows.Forms.ToolStripMenuItem toolStripMenuItem2;
        private System.Windows.Forms.ToolStripSeparator toolStripSeparator2;
        private System.Windows.Forms.ToolStripMenuItem toolStripMenuItem3;
        private System.Windows.Forms.PictureBox metadataAnimation;
        private System.Windows.Forms.Label label25;
        private System.Windows.Forms.ComboBox defaultRootComboBox;
        private System.Windows.Forms.Label label26;
        private System.Windows.Forms.Label s3PublicUrlValidityLabel;
        private System.Windows.Forms.CheckBox bucketVersioningCheckBox;
        private System.Windows.Forms.CheckBox bucketMfaCheckBox;
        private System.Windows.Forms.Label label21;
        private System.Windows.Forms.ComboBox storageClassComboBox;
        private PanelManager panelManagerPermissions;
        private ManagedPanel nonCloudManagedPanel;
        private ManagedPanel cloudManagedPanel;
        private System.Windows.Forms.TableLayoutPanel tableLayoutPanel1;
        private System.Windows.Forms.Label authenticatedLabel;
        private ClickLinkLabel authenticatedUrlLinkLabel;
        private System.Windows.Forms.PictureBox aclAnimation;
        private ClickThroughToolStrip toolStrip;
        private System.Windows.Forms.TextBox checksumTextBox;
        private System.Windows.Forms.Label label13;
        private System.Windows.Forms.Label invalidationStatus;
        private System.Windows.Forms.Label label24;
        private System.Windows.Forms.Button invalidateButton;
        private System.Windows.Forms.Label label27;
        private ClickLinkLabel originLinkLabel;
        private System.Windows.Forms.Label label28;
        private System.Windows.Forms.Label label29;
        private System.Windows.Forms.ComboBox distributionLoggingComboBox;
        private System.Windows.Forms.Label label31;
        private System.Windows.Forms.Label label32;
        private System.Windows.Forms.Label label33;
        private System.Windows.Forms.ComboBox bucketLoggingComboBox;
        private System.Windows.Forms.Label label34;
        private System.Windows.Forms.Label label35;
        private System.Windows.Forms.CheckBox bucketAnalyticsCheckBox;
        private ClickLinkLabel bucketAnalyticsSetupUrlLinkLabel;
        private System.Windows.Forms.Label label36;
        private System.Windows.Forms.Label label37;
        private System.Windows.Forms.CheckBox distributionAnalyticsCheckBox;
        private System.Windows.Forms.LinkLabel distributionAnalyticsSetupUrlLinkLabel;
        private System.Windows.Forms.CheckBox lifecycleTransitionCheckBox;
        private System.Windows.Forms.Label label38;
        private System.Windows.Forms.ComboBox lifecycleTransitionComboBox;
        private System.Windows.Forms.CheckBox lifecycleDeleteCheckBox;
        private System.Windows.Forms.ComboBox lifecycleDeleteComboBox;
        private System.Windows.Forms.ComboBox encryptionComboBox;
    }
}