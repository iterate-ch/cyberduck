using Ch.Cyberduck.Ui.Winforms.Controls;

namespace Ch.Cyberduck.Ui.Winforms
{
    partial class PreferencesForm
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
            this.iconList = new System.Windows.Forms.ImageList(this.components);
            this.downloadFolderBrowserDialog = new System.Windows.Forms.FolderBrowserDialog();
            this.editorOpenFileDialog = new System.Windows.Forms.OpenFileDialog();
            this.panelManager = new Ch.Cyberduck.Ui.Winforms.Controls.PanelManager();
            this.managedGeneralPanel = new Ch.Cyberduck.Ui.Winforms.Controls.ManagedPanel();
            this.tableLayoutPanelGeneral = new System.Windows.Forms.TableLayoutPanel();
            this.groupBox13 = new System.Windows.Forms.GroupBox();
            this.tableLayoutPanel22 = new System.Windows.Forms.TableLayoutPanel();
            this.label15 = new System.Windows.Forms.Label();
            this.bookmarkSizeComboBox = new System.Windows.Forms.ComboBox();
            this.browserGroupbox = new System.Windows.Forms.GroupBox();
            this.tableLayoutPanel1 = new System.Windows.Forms.TableLayoutPanel();
            this.saveWorkspaceCheckbox = new System.Windows.Forms.CheckBox();
            this.labelOpenEmtpyBrowser = new System.Windows.Forms.Label();
            this.managedFtpPanel = new Ch.Cyberduck.Ui.Winforms.Controls.ManagedPanel();
            this.tableLayoutPanel25 = new System.Windows.Forms.TableLayoutPanel();
            this.groupBox4 = new System.Windows.Forms.GroupBox();
            this.tableLayoutPanel38 = new System.Windows.Forms.TableLayoutPanel();
            this.label2 = new System.Windows.Forms.Label();
            this.defaultEncodingCombobox = new System.Windows.Forms.ComboBox();
            this.managedS3Panel = new Ch.Cyberduck.Ui.Winforms.Controls.ManagedPanel();
            this.tableLayoutPanel28 = new System.Windows.Forms.TableLayoutPanel();
            this.groupBox19 = new System.Windows.Forms.GroupBox();
            this.tableLayoutPanel29 = new System.Windows.Forms.TableLayoutPanel();
            this.label29 = new System.Windows.Forms.Label();
            this.defaultBucketLocationCombobox = new System.Windows.Forms.ComboBox();
            this.groupBox22 = new System.Windows.Forms.GroupBox();
            this.tableLayoutPanel30 = new System.Windows.Forms.TableLayoutPanel();
            this.defaultStorageClassComboBox = new System.Windows.Forms.ComboBox();
            this.label3 = new System.Windows.Forms.Label();
            this.groupBox3 = new System.Windows.Forms.GroupBox();
            this.tableLayoutPanel20 = new System.Windows.Forms.TableLayoutPanel();
            this.defaultEncryptionComboBox = new System.Windows.Forms.ComboBox();
            this.label8 = new System.Windows.Forms.Label();
            this.groupBox23 = new System.Windows.Forms.GroupBox();
            this.tableLayoutPanel33 = new System.Windows.Forms.TableLayoutPanel();
            this.defaultACLComboBox = new System.Windows.Forms.ComboBox();
            this.label5 = new System.Windows.Forms.Label();
            this.managedBandwidthPanel = new Ch.Cyberduck.Ui.Winforms.Controls.ManagedPanel();
            this.tableLayoutPanel34 = new System.Windows.Forms.TableLayoutPanel();
            this.groupBox25 = new System.Windows.Forms.GroupBox();
            this.tableLayoutPanel35 = new System.Windows.Forms.TableLayoutPanel();
            this.label31 = new System.Windows.Forms.Label();
            this.defaultDownloadThrottleCombobox = new System.Windows.Forms.ComboBox();
            this.groupBox26 = new System.Windows.Forms.GroupBox();
            this.tableLayoutPanel36 = new System.Windows.Forms.TableLayoutPanel();
            this.defaultUploadThrottleCombobox = new System.Windows.Forms.ComboBox();
            this.label32 = new System.Windows.Forms.Label();
            this.managedConnectionPanel = new Ch.Cyberduck.Ui.Winforms.Controls.ManagedPanel();
            this.tableLayoutPanel37 = new System.Windows.Forms.TableLayoutPanel();
            this.groupBox1 = new System.Windows.Forms.GroupBox();
            this.tableLayoutPanel2 = new System.Windows.Forms.TableLayoutPanel();
            this.keychainCheckbox = new System.Windows.Forms.CheckBox();
            this.labelKeychain = new System.Windows.Forms.Label();
            this.confirmDisconnectCheckbox = new System.Windows.Forms.CheckBox();
            this.label7 = new System.Windows.Forms.Label();
            this.labelConfirmDisconnect = new System.Windows.Forms.Label();
            this.defaultProtocolCombobox = new Ch.Cyberduck.Ui.Winforms.Controls.ImageComboBox();
            this.groupBox31 = new System.Windows.Forms.GroupBox();
            this.tableLayoutPanel48 = new System.Windows.Forms.TableLayoutPanel();
            this.debugLogCheckBox = new System.Windows.Forms.CheckBox();
            this.showDebugLogButton = new System.Windows.Forms.Button();
            this.groupBox21 = new System.Windows.Forms.GroupBox();
            this.tableLayoutPanel39 = new System.Windows.Forms.TableLayoutPanel();
            this.connectionTimeoutUpDown = new System.Windows.Forms.NumericUpDown();
            this.retriesUpDown = new System.Windows.Forms.NumericUpDown();
            this.retryCheckbox = new System.Windows.Forms.CheckBox();
            this.label34 = new System.Windows.Forms.Label();
            this.label36 = new System.Windows.Forms.Label();
            this.retryDelayUpDown = new System.Windows.Forms.NumericUpDown();
            this.label16 = new System.Windows.Forms.Label();
            this.groupBox20 = new System.Windows.Forms.GroupBox();
            this.tableLayoutPanel40 = new System.Windows.Forms.TableLayoutPanel();
            this.systemProxyCheckBox = new System.Windows.Forms.CheckBox();
            this.changeSystemProxyButton = new System.Windows.Forms.Button();
            this.managedUpdatePanel = new Ch.Cyberduck.Ui.Winforms.Controls.ManagedPanel();
            this.tableLayoutPanel41 = new System.Windows.Forms.TableLayoutPanel();
            this.groupBox27 = new System.Windows.Forms.GroupBox();
            this.tableLayoutPanel42 = new System.Windows.Forms.TableLayoutPanel();
            this.updateCheckBox = new System.Windows.Forms.CheckBox();
            this.updateCheckButton = new System.Windows.Forms.Button();
            this.lastUpdateLabel = new System.Windows.Forms.Label();
            this.updateFeedComboBox = new System.Windows.Forms.ComboBox();
            this.managedLanguagePanel = new Ch.Cyberduck.Ui.Winforms.Controls.ManagedPanel();
            this.tableLayoutPanel43 = new System.Windows.Forms.TableLayoutPanel();
            this.groupBox28 = new System.Windows.Forms.GroupBox();
            this.tableLayoutPanel44 = new System.Windows.Forms.TableLayoutPanel();
            this.languageComboBox = new System.Windows.Forms.ComboBox();
            this.label18 = new System.Windows.Forms.Label();
            this.managedEditorPanel = new Ch.Cyberduck.Ui.Winforms.Controls.ManagedPanel();
            this.tableLayoutPanel3 = new System.Windows.Forms.TableLayoutPanel();
            this.groupBox2 = new System.Windows.Forms.GroupBox();
            this.tableLayoutPanel4 = new System.Windows.Forms.TableLayoutPanel();
            this.label14 = new System.Windows.Forms.Label();
            this.editorComboBox = new Ch.Cyberduck.Ui.Winforms.Controls.ImageComboBox();
            this.alwaysUseDefaultEditorCheckBox = new System.Windows.Forms.CheckBox();
            this.managedBrowserPanel = new Ch.Cyberduck.Ui.Winforms.Controls.ManagedPanel();
            this.tableLayoutPanel21 = new System.Windows.Forms.TableLayoutPanel();
            this.groupBox18 = new System.Windows.Forms.GroupBox();
            this.tableLayoutPanel27 = new System.Windows.Forms.TableLayoutPanel();
            this.label13 = new System.Windows.Forms.Label();
            this.connectBookmarkCombobox = new Ch.Cyberduck.Ui.Winforms.Controls.ImageComboBox();
            this.label19 = new System.Windows.Forms.Label();
            this.newBrowserOnStartupCheckbox = new System.Windows.Forms.CheckBox();
            this.groupBox14 = new System.Windows.Forms.GroupBox();
            this.tableLayoutPanel23 = new System.Windows.Forms.TableLayoutPanel();
            this.infoWindowCheckbox = new System.Windows.Forms.CheckBox();
            this.showHiddenFilesCheckbox = new System.Windows.Forms.CheckBox();
            this.returnKeyCheckbox = new System.Windows.Forms.CheckBox();
            this.doubleClickEditorCheckbox = new System.Windows.Forms.CheckBox();
            this.managedTransfersPanel = new Ch.Cyberduck.Ui.Winforms.Controls.ManagedPanel();
            this.transfersTabControl = new System.Windows.Forms.TabControl();
            this.tabPage1 = new System.Windows.Forms.TabPage();
            this.tableLayoutPanel5 = new System.Windows.Forms.TableLayoutPanel();
            this.groupBox6 = new System.Windows.Forms.GroupBox();
            this.tableLayoutPanel8 = new System.Windows.Forms.TableLayoutPanel();
            this.label12 = new System.Windows.Forms.Label();
            this.duplicateUploadOverwriteCheckbox = new System.Windows.Forms.CheckBox();
            this.duplicateUploadCombobox = new System.Windows.Forms.ComboBox();
            this.uploadTemporaryNameCheckBox = new System.Windows.Forms.CheckBox();
            this.groupBox5 = new System.Windows.Forms.GroupBox();
            this.tableLayoutPanel7 = new System.Windows.Forms.TableLayoutPanel();
            this.openAfterDownloadCheckbox = new System.Windows.Forms.CheckBox();
            this.duplicateDownloadCombobox = new System.Windows.Forms.ComboBox();
            this.label11 = new System.Windows.Forms.Label();
            this.downloadFolderLabel = new Ch.Cyberduck.Ui.Winforms.Controls.EllipsisLabel();
            this.label10 = new System.Windows.Forms.Label();
            this.duplicateDownloadOverwriteCheckbox = new System.Windows.Forms.CheckBox();
            this.showDownloadFolderDialogButton = new System.Windows.Forms.Button();
            this.segmentedDownloadsCheckbox = new System.Windows.Forms.CheckBox();
            this.groupBox29 = new System.Windows.Forms.GroupBox();
            this.tableLayoutPanel6 = new System.Windows.Forms.TableLayoutPanel();
            this.transfersToFrontCheckbox = new System.Windows.Forms.CheckBox();
            this.removeFromTransfersCheckbox = new System.Windows.Forms.CheckBox();
            this.transferFilesCombobox = new System.Windows.Forms.ComboBox();
            this.transfersToBackCheckbox = new System.Windows.Forms.CheckBox();
            this.label1 = new System.Windows.Forms.Label();
            this.tabPage2 = new System.Windows.Forms.TabPage();
            this.tableLayoutPanel9 = new System.Windows.Forms.TableLayoutPanel();
            this.groupBox7 = new System.Windows.Forms.GroupBox();
            this.tableLayoutPanel10 = new System.Windows.Forms.TableLayoutPanel();
            this.chmodDownloadCheckbox = new System.Windows.Forms.CheckBox();
            this.chmodDownloadCustomRadioButton = new System.Windows.Forms.RadioButton();
            this.chmodDownloadDefaultRadioButton = new System.Windows.Forms.RadioButton();
            this.chmodDownloadTypeCombobox = new System.Windows.Forms.ComboBox();
            this.tableLayoutPanel11 = new System.Windows.Forms.TableLayoutPanel();
            this.ownerDownloadLabel = new System.Windows.Forms.Label();
            this.dotherxCheckbox = new System.Windows.Forms.CheckBox();
            this.groupDownloadLabel = new System.Windows.Forms.Label();
            this.dotherwCheckbox = new System.Windows.Forms.CheckBox();
            this.othersDownloadLabel = new System.Windows.Forms.Label();
            this.dgroupxCheckbox = new System.Windows.Forms.CheckBox();
            this.dotherrCheckbox = new System.Windows.Forms.CheckBox();
            this.dgroupwCheckbox = new System.Windows.Forms.CheckBox();
            this.downerrCheckbox = new System.Windows.Forms.CheckBox();
            this.downerwCheckbox = new System.Windows.Forms.CheckBox();
            this.downerxCheckbox = new System.Windows.Forms.CheckBox();
            this.dgrouprCheckbox = new System.Windows.Forms.CheckBox();
            this.groupBox8 = new System.Windows.Forms.GroupBox();
            this.tableLayoutPanel12 = new System.Windows.Forms.TableLayoutPanel();
            this.chmodUploadCheckbox = new System.Windows.Forms.CheckBox();
            this.chmodUploadCustomRadioButton = new System.Windows.Forms.RadioButton();
            this.chmodUploadDefaultRadioButton = new System.Windows.Forms.RadioButton();
            this.chmodUploadTypeCombobox = new System.Windows.Forms.ComboBox();
            this.tableLayoutPanel13 = new System.Windows.Forms.TableLayoutPanel();
            this.ownerUploadLabel = new System.Windows.Forms.Label();
            this.uotherxCheckbox = new System.Windows.Forms.CheckBox();
            this.uownerrCheckbox = new System.Windows.Forms.CheckBox();
            this.uotherwCheckbox = new System.Windows.Forms.CheckBox();
            this.uownerwCheckbox = new System.Windows.Forms.CheckBox();
            this.uotherrCheckbox = new System.Windows.Forms.CheckBox();
            this.uownerxCheckbox = new System.Windows.Forms.CheckBox();
            this.othersUploadLabel = new System.Windows.Forms.Label();
            this.groupUploadLabel = new System.Windows.Forms.Label();
            this.ugroupxCheckbox = new System.Windows.Forms.CheckBox();
            this.ugrouprCheckbox = new System.Windows.Forms.CheckBox();
            this.ugroupwCheckbox = new System.Windows.Forms.CheckBox();
            this.tabPage3 = new System.Windows.Forms.TabPage();
            this.tableLayoutPanel14 = new System.Windows.Forms.TableLayoutPanel();
            this.groupBox9 = new System.Windows.Forms.GroupBox();
            this.tableLayoutPanel15 = new System.Windows.Forms.TableLayoutPanel();
            this.preserveModificationDownloadCheckbox = new System.Windows.Forms.CheckBox();
            this.groupBox10 = new System.Windows.Forms.GroupBox();
            this.tableLayoutPanel16 = new System.Windows.Forms.TableLayoutPanel();
            this.preserveModificationUploadCheckbox = new System.Windows.Forms.CheckBox();
            this.tabPage5 = new System.Windows.Forms.TabPage();
            this.tableLayoutPanel26 = new System.Windows.Forms.TableLayoutPanel();
            this.groupBox16 = new System.Windows.Forms.GroupBox();
            this.tableLayoutPanel31 = new System.Windows.Forms.TableLayoutPanel();
            this.verifyChecksumDownloadCheckbox = new System.Windows.Forms.CheckBox();
            this.groupBox17 = new System.Windows.Forms.GroupBox();
            this.tableLayoutPanel32 = new System.Windows.Forms.TableLayoutPanel();
            this.verifyChecksumUploadCheckbox = new System.Windows.Forms.CheckBox();
            this.tabPage4 = new System.Windows.Forms.TabPage();
            this.tableLayoutPanel17 = new System.Windows.Forms.TableLayoutPanel();
            this.groupBox11 = new System.Windows.Forms.GroupBox();
            this.tableLayoutPanel18 = new System.Windows.Forms.TableLayoutPanel();
            this.downloadSkipCheckbox = new System.Windows.Forms.CheckBox();
            this.downloadSkipRegexRichTextbox = new System.Windows.Forms.RichTextBox();
            this.downloadSkipRegexDefaultButton = new System.Windows.Forms.Button();
            this.groupBox12 = new System.Windows.Forms.GroupBox();
            this.tableLayoutPanel19 = new System.Windows.Forms.TableLayoutPanel();
            this.uploadSkipCheckbox = new System.Windows.Forms.CheckBox();
            this.uploadSkipRegexRichTextbox = new System.Windows.Forms.RichTextBox();
            this.uploadSkipRegexDefaultButton = new System.Windows.Forms.Button();
            this.managedGoogleCloudPanel = new Ch.Cyberduck.Ui.Winforms.Controls.ManagedPanel();
            this.tableLayoutPanel45 = new System.Windows.Forms.TableLayoutPanel();
            this.groupBox24 = new System.Windows.Forms.GroupBox();
            this.tableLayoutPanel46 = new System.Windows.Forms.TableLayoutPanel();
            this.label6 = new System.Windows.Forms.Label();
            this.defaultGoogleLocationComboBox = new System.Windows.Forms.ComboBox();
            this.groupBox30 = new System.Windows.Forms.GroupBox();
            this.tableLayoutPanel47 = new System.Windows.Forms.TableLayoutPanel();
            this.defaultGoogleStorageClassComboBox = new System.Windows.Forms.ComboBox();
            this.label9 = new System.Windows.Forms.Label();
            this.groupBox32 = new System.Windows.Forms.GroupBox();
            this.tableLayoutPanel49 = new System.Windows.Forms.TableLayoutPanel();
            this.defaultGoogleACLComboBox = new System.Windows.Forms.ComboBox();
            this.label17 = new System.Windows.Forms.Label();
            this.managedCryptomatorPanel = new Ch.Cyberduck.Ui.Winforms.Controls.ManagedPanel();
            this.tableLayoutPanel24 = new System.Windows.Forms.TableLayoutPanel();
            this.groupBox15 = new System.Windows.Forms.GroupBox();
            this.flowLayoutPanel1 = new System.Windows.Forms.FlowLayoutPanel();
            this.cryptomatorAutoDetectCheckBox = new System.Windows.Forms.CheckBox();
            this.cryptomatorUseKeychain = new System.Windows.Forms.CheckBox();
            this.managedProfilesPanel = new Ch.Cyberduck.Ui.Winforms.Controls.ManagedPanel();
            this.profilesPageHost = new System.Windows.Forms.Integration.ElementHost();
            this.toolStrip = new Ch.Cyberduck.Ui.Winforms.Controls.ClickThroughToolStrip();
            this.generalButton = new System.Windows.Forms.ToolStripButton();
            this.browserButton = new System.Windows.Forms.ToolStripButton();
            this.transfersButton = new System.Windows.Forms.ToolStripButton();
            this.editStripButton = new System.Windows.Forms.ToolStripButton();
            this.profilesButton = new System.Windows.Forms.ToolStripButton();
            this.ftpButton = new System.Windows.Forms.ToolStripButton();
            this.s3Button = new System.Windows.Forms.ToolStripButton();
            this.googleCloudButton = new System.Windows.Forms.ToolStripButton();
            this.bandwidthButton = new System.Windows.Forms.ToolStripButton();
            this.connectionButton = new System.Windows.Forms.ToolStripButton();
            this.cryptomatorButton = new System.Windows.Forms.ToolStripButton();
            this.updateButton = new System.Windows.Forms.ToolStripButton();
            this.languageButton = new System.Windows.Forms.ToolStripButton();
            this.panelManager.SuspendLayout();
            this.managedGeneralPanel.SuspendLayout();
            this.tableLayoutPanelGeneral.SuspendLayout();
            this.groupBox13.SuspendLayout();
            this.tableLayoutPanel22.SuspendLayout();
            this.browserGroupbox.SuspendLayout();
            this.tableLayoutPanel1.SuspendLayout();
            this.managedFtpPanel.SuspendLayout();
            this.tableLayoutPanel25.SuspendLayout();
            this.groupBox4.SuspendLayout();
            this.tableLayoutPanel38.SuspendLayout();
            this.managedS3Panel.SuspendLayout();
            this.tableLayoutPanel28.SuspendLayout();
            this.groupBox19.SuspendLayout();
            this.tableLayoutPanel29.SuspendLayout();
            this.groupBox22.SuspendLayout();
            this.tableLayoutPanel30.SuspendLayout();
            this.groupBox3.SuspendLayout();
            this.tableLayoutPanel20.SuspendLayout();
            this.groupBox23.SuspendLayout();
            this.tableLayoutPanel33.SuspendLayout();
            this.managedBandwidthPanel.SuspendLayout();
            this.tableLayoutPanel34.SuspendLayout();
            this.groupBox25.SuspendLayout();
            this.tableLayoutPanel35.SuspendLayout();
            this.groupBox26.SuspendLayout();
            this.tableLayoutPanel36.SuspendLayout();
            this.managedConnectionPanel.SuspendLayout();
            this.tableLayoutPanel37.SuspendLayout();
            this.groupBox1.SuspendLayout();
            this.tableLayoutPanel2.SuspendLayout();
            this.groupBox31.SuspendLayout();
            this.tableLayoutPanel48.SuspendLayout();
            this.groupBox21.SuspendLayout();
            this.tableLayoutPanel39.SuspendLayout();
            ((System.ComponentModel.ISupportInitialize)(this.connectionTimeoutUpDown)).BeginInit();
            ((System.ComponentModel.ISupportInitialize)(this.retriesUpDown)).BeginInit();
            ((System.ComponentModel.ISupportInitialize)(this.retryDelayUpDown)).BeginInit();
            this.groupBox20.SuspendLayout();
            this.tableLayoutPanel40.SuspendLayout();
            this.managedUpdatePanel.SuspendLayout();
            this.tableLayoutPanel41.SuspendLayout();
            this.groupBox27.SuspendLayout();
            this.tableLayoutPanel42.SuspendLayout();
            this.managedLanguagePanel.SuspendLayout();
            this.tableLayoutPanel43.SuspendLayout();
            this.groupBox28.SuspendLayout();
            this.tableLayoutPanel44.SuspendLayout();
            this.managedEditorPanel.SuspendLayout();
            this.tableLayoutPanel3.SuspendLayout();
            this.groupBox2.SuspendLayout();
            this.tableLayoutPanel4.SuspendLayout();
            this.managedBrowserPanel.SuspendLayout();
            this.tableLayoutPanel21.SuspendLayout();
            this.groupBox18.SuspendLayout();
            this.tableLayoutPanel27.SuspendLayout();
            this.groupBox14.SuspendLayout();
            this.tableLayoutPanel23.SuspendLayout();
            this.managedTransfersPanel.SuspendLayout();
            this.transfersTabControl.SuspendLayout();
            this.tabPage1.SuspendLayout();
            this.tableLayoutPanel5.SuspendLayout();
            this.groupBox6.SuspendLayout();
            this.tableLayoutPanel8.SuspendLayout();
            this.groupBox5.SuspendLayout();
            this.tableLayoutPanel7.SuspendLayout();
            this.groupBox29.SuspendLayout();
            this.tableLayoutPanel6.SuspendLayout();
            this.tabPage2.SuspendLayout();
            this.tableLayoutPanel9.SuspendLayout();
            this.groupBox7.SuspendLayout();
            this.tableLayoutPanel10.SuspendLayout();
            this.tableLayoutPanel11.SuspendLayout();
            this.groupBox8.SuspendLayout();
            this.tableLayoutPanel12.SuspendLayout();
            this.tableLayoutPanel13.SuspendLayout();
            this.tabPage3.SuspendLayout();
            this.tableLayoutPanel14.SuspendLayout();
            this.groupBox9.SuspendLayout();
            this.tableLayoutPanel15.SuspendLayout();
            this.groupBox10.SuspendLayout();
            this.tableLayoutPanel16.SuspendLayout();
            this.tabPage5.SuspendLayout();
            this.tableLayoutPanel26.SuspendLayout();
            this.groupBox16.SuspendLayout();
            this.tableLayoutPanel31.SuspendLayout();
            this.groupBox17.SuspendLayout();
            this.tableLayoutPanel32.SuspendLayout();
            this.tabPage4.SuspendLayout();
            this.tableLayoutPanel17.SuspendLayout();
            this.groupBox11.SuspendLayout();
            this.tableLayoutPanel18.SuspendLayout();
            this.groupBox12.SuspendLayout();
            this.tableLayoutPanel19.SuspendLayout();
            this.managedGoogleCloudPanel.SuspendLayout();
            this.tableLayoutPanel45.SuspendLayout();
            this.groupBox24.SuspendLayout();
            this.tableLayoutPanel46.SuspendLayout();
            this.groupBox30.SuspendLayout();
            this.tableLayoutPanel47.SuspendLayout();
            this.groupBox32.SuspendLayout();
            this.tableLayoutPanel49.SuspendLayout();
            this.managedCryptomatorPanel.SuspendLayout();
            this.tableLayoutPanel24.SuspendLayout();
            this.groupBox15.SuspendLayout();
            this.flowLayoutPanel1.SuspendLayout();
            this.managedProfilesPanel.SuspendLayout();
            this.toolStrip.SuspendLayout();
            this.SuspendLayout();
            // 
            // iconList
            // 
            this.iconList.ColorDepth = System.Windows.Forms.ColorDepth.Depth8Bit;
            this.iconList.ImageSize = new System.Drawing.Size(16, 16);
            this.iconList.TransparentColor = System.Drawing.Color.Transparent;
            // 
            // editorOpenFileDialog
            // 
            this.editorOpenFileDialog.Filter = "|*.exe;*.com;*.cmd;*.bat";
            // 
            // panelManager
            // 
            this.panelManager.Anchor = ((System.Windows.Forms.AnchorStyles)((((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Bottom) 
            | System.Windows.Forms.AnchorStyles.Left) 
            | System.Windows.Forms.AnchorStyles.Right)));
            this.panelManager.Controls.Add(this.managedGeneralPanel);
            this.panelManager.Controls.Add(this.managedFtpPanel);
            this.panelManager.Controls.Add(this.managedS3Panel);
            this.panelManager.Controls.Add(this.managedBandwidthPanel);
            this.panelManager.Controls.Add(this.managedConnectionPanel);
            this.panelManager.Controls.Add(this.managedUpdatePanel);
            this.panelManager.Controls.Add(this.managedLanguagePanel);
            this.panelManager.Controls.Add(this.managedEditorPanel);
            this.panelManager.Controls.Add(this.managedBrowserPanel);
            this.panelManager.Controls.Add(this.managedTransfersPanel);
            this.panelManager.Controls.Add(this.managedGoogleCloudPanel);
            this.panelManager.Controls.Add(this.managedCryptomatorPanel);
            this.panelManager.Controls.Add(this.managedProfilesPanel);
            this.panelManager.Location = new System.Drawing.Point(12, 76);
            this.panelManager.Name = "panelManager";
            this.panelManager.SelectedIndex = 8;
            this.panelManager.SelectedPanel = this.managedBrowserPanel;
            this.panelManager.Size = new System.Drawing.Size(760, 501);
            this.panelManager.TabIndex = 1;
            // 
            // managedGeneralPanel
            // 
            this.managedGeneralPanel.Controls.Add(this.tableLayoutPanelGeneral);
            this.managedGeneralPanel.Location = new System.Drawing.Point(0, 0);
            this.managedGeneralPanel.Name = "managedGeneralPanel";
            this.managedGeneralPanel.Size = new System.Drawing.Size(760, 501);
            this.managedGeneralPanel.Text = "managedPanel1";
            // 
            // tableLayoutPanelGeneral
            // 
            this.tableLayoutPanelGeneral.AutoSize = true;
            this.tableLayoutPanelGeneral.ColumnCount = 1;
            this.tableLayoutPanelGeneral.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle(System.Windows.Forms.SizeType.Percent, 100F));
            this.tableLayoutPanelGeneral.Controls.Add(this.groupBox13, 0, 2);
            this.tableLayoutPanelGeneral.Controls.Add(this.browserGroupbox, 0, 0);
            this.tableLayoutPanelGeneral.Dock = System.Windows.Forms.DockStyle.Fill;
            this.tableLayoutPanelGeneral.Location = new System.Drawing.Point(0, 0);
            this.tableLayoutPanelGeneral.Name = "tableLayoutPanelGeneral";
            this.tableLayoutPanelGeneral.RowCount = 4;
            this.tableLayoutPanelGeneral.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanelGeneral.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanelGeneral.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanelGeneral.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanelGeneral.Size = new System.Drawing.Size(760, 501);
            this.tableLayoutPanelGeneral.TabIndex = 7;
            // 
            // groupBox13
            // 
            this.groupBox13.AutoSize = true;
            this.groupBox13.Controls.Add(this.tableLayoutPanel22);
            this.groupBox13.Dock = System.Windows.Forms.DockStyle.Fill;
            this.groupBox13.Location = new System.Drawing.Point(3, 81);
            this.groupBox13.Name = "groupBox13";
            this.groupBox13.Size = new System.Drawing.Size(754, 76);
            this.groupBox13.TabIndex = 2;
            this.groupBox13.TabStop = false;
            this.groupBox13.Text = "Bookmarks";
            // 
            // tableLayoutPanel22
            // 
            this.tableLayoutPanel22.AutoSize = true;
            this.tableLayoutPanel22.ColumnCount = 2;
            this.tableLayoutPanel22.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle(System.Windows.Forms.SizeType.Absolute, 199F));
            this.tableLayoutPanel22.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle());
            this.tableLayoutPanel22.Controls.Add(this.label15, 0, 1);
            this.tableLayoutPanel22.Controls.Add(this.bookmarkSizeComboBox, 0, 4);
            this.tableLayoutPanel22.Dock = System.Windows.Forms.DockStyle.Fill;
            this.tableLayoutPanel22.Location = new System.Drawing.Point(3, 19);
            this.tableLayoutPanel22.Name = "tableLayoutPanel22";
            this.tableLayoutPanel22.Padding = new System.Windows.Forms.Padding(5);
            this.tableLayoutPanel22.RowCount = 6;
            this.tableLayoutPanel22.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel22.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel22.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel22.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel22.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel22.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel22.Size = new System.Drawing.Size(748, 54);
            this.tableLayoutPanel22.TabIndex = 10;
            // 
            // label15
            // 
            this.label15.AutoEllipsis = true;
            this.label15.AutoSize = true;
            this.tableLayoutPanel22.SetColumnSpan(this.label15, 2);
            this.label15.ForeColor = System.Drawing.SystemColors.GrayText;
            this.label15.Location = new System.Drawing.Point(8, 5);
            this.label15.Name = "label15";
            this.label15.Size = new System.Drawing.Size(394, 15);
            this.label15.TabIndex = 6;
            this.label15.Text = "When using small icons, only the nickname of the bookmark is displayed.";
            // 
            // bookmarkSizeComboBox
            // 
            this.bookmarkSizeComboBox.DropDownStyle = System.Windows.Forms.ComboBoxStyle.DropDownList;
            this.bookmarkSizeComboBox.FormattingEnabled = true;
            this.bookmarkSizeComboBox.ItemHeight = 15;
            this.bookmarkSizeComboBox.Location = new System.Drawing.Point(8, 23);
            this.bookmarkSizeComboBox.Name = "bookmarkSizeComboBox";
            this.bookmarkSizeComboBox.Size = new System.Drawing.Size(193, 23);
            this.bookmarkSizeComboBox.TabIndex = 6;
            this.bookmarkSizeComboBox.SelectedIndexChanged += new System.EventHandler(this.bookmarkSizeComboBox_SelectionChangeCommitted);
            // 
            // browserGroupbox
            // 
            this.browserGroupbox.AutoSize = true;
            this.browserGroupbox.Controls.Add(this.tableLayoutPanel1);
            this.browserGroupbox.Dock = System.Windows.Forms.DockStyle.Fill;
            this.browserGroupbox.Location = new System.Drawing.Point(3, 3);
            this.browserGroupbox.Name = "browserGroupbox";
            this.browserGroupbox.Size = new System.Drawing.Size(754, 72);
            this.browserGroupbox.TabIndex = 0;
            this.browserGroupbox.TabStop = false;
            this.browserGroupbox.Text = "Browser";
            // 
            // tableLayoutPanel1
            // 
            this.tableLayoutPanel1.AutoSize = true;
            this.tableLayoutPanel1.ColumnCount = 2;
            this.tableLayoutPanel1.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle(System.Windows.Forms.SizeType.Absolute, 199F));
            this.tableLayoutPanel1.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle(System.Windows.Forms.SizeType.Absolute, 433F));
            this.tableLayoutPanel1.Controls.Add(this.saveWorkspaceCheckbox, 0, 0);
            this.tableLayoutPanel1.Controls.Add(this.labelOpenEmtpyBrowser, 0, 1);
            this.tableLayoutPanel1.Dock = System.Windows.Forms.DockStyle.Fill;
            this.tableLayoutPanel1.Location = new System.Drawing.Point(3, 19);
            this.tableLayoutPanel1.Name = "tableLayoutPanel1";
            this.tableLayoutPanel1.Padding = new System.Windows.Forms.Padding(5);
            this.tableLayoutPanel1.RowCount = 3;
            this.tableLayoutPanel1.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel1.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel1.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel1.RowStyles.Add(new System.Windows.Forms.RowStyle(System.Windows.Forms.SizeType.Absolute, 20F));
            this.tableLayoutPanel1.RowStyles.Add(new System.Windows.Forms.RowStyle(System.Windows.Forms.SizeType.Absolute, 20F));
            this.tableLayoutPanel1.Size = new System.Drawing.Size(748, 50);
            this.tableLayoutPanel1.TabIndex = 6;
            // 
            // saveWorkspaceCheckbox
            // 
            this.saveWorkspaceCheckbox.AutoSize = true;
            this.tableLayoutPanel1.SetColumnSpan(this.saveWorkspaceCheckbox, 2);
            this.saveWorkspaceCheckbox.Location = new System.Drawing.Point(8, 8);
            this.saveWorkspaceCheckbox.Name = "saveWorkspaceCheckbox";
            this.saveWorkspaceCheckbox.Size = new System.Drawing.Size(111, 19);
            this.saveWorkspaceCheckbox.TabIndex = 0;
            this.saveWorkspaceCheckbox.Text = "Save Workspace";
            this.saveWorkspaceCheckbox.UseVisualStyleBackColor = true;
            this.saveWorkspaceCheckbox.CheckedChanged += new System.EventHandler(this.saveWorkspaceCheckbox_CheckedChanged);
            // 
            // labelOpenEmtpyBrowser
            // 
            this.labelOpenEmtpyBrowser.AutoEllipsis = true;
            this.labelOpenEmtpyBrowser.AutoSize = true;
            this.tableLayoutPanel1.SetColumnSpan(this.labelOpenEmtpyBrowser, 2);
            this.labelOpenEmtpyBrowser.ForeColor = System.Drawing.SystemColors.GrayText;
            this.labelOpenEmtpyBrowser.Location = new System.Drawing.Point(8, 30);
            this.labelOpenEmtpyBrowser.Name = "labelOpenEmtpyBrowser";
            this.labelOpenEmtpyBrowser.Size = new System.Drawing.Size(458, 15);
            this.labelOpenEmtpyBrowser.TabIndex = 3;
            this.labelOpenEmtpyBrowser.Text = "Will save all open browsers when quitting and restore the connections upon relaun" +
    "ch.";
            // 
            // managedFtpPanel
            // 
            this.managedFtpPanel.Controls.Add(this.tableLayoutPanel25);
            this.managedFtpPanel.Location = new System.Drawing.Point(0, 0);
            this.managedFtpPanel.Name = "managedFtpPanel";
            this.managedFtpPanel.Size = new System.Drawing.Size(0, 0);
            this.managedFtpPanel.Text = "managedPanel1";
            // 
            // tableLayoutPanel25
            // 
            this.tableLayoutPanel25.AutoSize = true;
            this.tableLayoutPanel25.ColumnCount = 1;
            this.tableLayoutPanel25.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle(System.Windows.Forms.SizeType.Percent, 100F));
            this.tableLayoutPanel25.Controls.Add(this.groupBox4, 0, 0);
            this.tableLayoutPanel25.Dock = System.Windows.Forms.DockStyle.Fill;
            this.tableLayoutPanel25.Location = new System.Drawing.Point(0, 0);
            this.tableLayoutPanel25.Name = "tableLayoutPanel25";
            this.tableLayoutPanel25.RowCount = 2;
            this.tableLayoutPanel25.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel25.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel25.RowStyles.Add(new System.Windows.Forms.RowStyle(System.Windows.Forms.SizeType.Absolute, 20F));
            this.tableLayoutPanel25.Size = new System.Drawing.Size(0, 0);
            this.tableLayoutPanel25.TabIndex = 0;
            // 
            // groupBox4
            // 
            this.groupBox4.AutoSize = true;
            this.groupBox4.Controls.Add(this.tableLayoutPanel38);
            this.groupBox4.Dock = System.Windows.Forms.DockStyle.Fill;
            this.groupBox4.Location = new System.Drawing.Point(3, 3);
            this.groupBox4.Name = "groupBox4";
            this.groupBox4.Size = new System.Drawing.Size(1, 96);
            this.groupBox4.TabIndex = 3;
            this.groupBox4.TabStop = false;
            this.groupBox4.Text = "Text Encoding";
            // 
            // tableLayoutPanel38
            // 
            this.tableLayoutPanel38.AutoSize = true;
            this.tableLayoutPanel38.ColumnCount = 1;
            this.tableLayoutPanel38.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle(System.Windows.Forms.SizeType.Absolute, 738F));
            this.tableLayoutPanel38.Controls.Add(this.label2, 0, 0);
            this.tableLayoutPanel38.Controls.Add(this.defaultEncodingCombobox, 0, 1);
            this.tableLayoutPanel38.Dock = System.Windows.Forms.DockStyle.Fill;
            this.tableLayoutPanel38.Location = new System.Drawing.Point(3, 19);
            this.tableLayoutPanel38.Name = "tableLayoutPanel38";
            this.tableLayoutPanel38.Padding = new System.Windows.Forms.Padding(5);
            this.tableLayoutPanel38.RowCount = 2;
            this.tableLayoutPanel38.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel38.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel38.Size = new System.Drawing.Size(0, 74);
            this.tableLayoutPanel38.TabIndex = 6;
            // 
            // label2
            // 
            this.label2.Anchor = System.Windows.Forms.AnchorStyles.Left;
            this.label2.AutoEllipsis = true;
            this.label2.AutoSize = true;
            this.label2.ForeColor = System.Drawing.SystemColors.GrayText;
            this.label2.Location = new System.Drawing.Point(8, 5);
            this.label2.Name = "label2";
            this.label2.Padding = new System.Windows.Forms.Padding(0, 0, 0, 5);
            this.label2.Size = new System.Drawing.Size(688, 35);
            this.label2.TabIndex = 0;
            this.label2.Text = "The selected default text encoding is used to convert characters in filenames dis" +
    "played in the browser. This should match the text encoding used on the server.";
            // 
            // defaultEncodingCombobox
            // 
            this.defaultEncodingCombobox.Anchor = System.Windows.Forms.AnchorStyles.Left;
            this.defaultEncodingCombobox.DropDownStyle = System.Windows.Forms.ComboBoxStyle.DropDownList;
            this.defaultEncodingCombobox.FormattingEnabled = true;
            this.defaultEncodingCombobox.ItemHeight = 15;
            this.defaultEncodingCombobox.Location = new System.Drawing.Point(8, 43);
            this.defaultEncodingCombobox.Name = "defaultEncodingCombobox";
            this.defaultEncodingCombobox.Size = new System.Drawing.Size(206, 23);
            this.defaultEncodingCombobox.TabIndex = 5;
            this.defaultEncodingCombobox.SelectionChangeCommitted += new System.EventHandler(this.defaultEncodingCombobox_SelectionChangeCommitted);
            // 
            // managedS3Panel
            // 
            this.managedS3Panel.Controls.Add(this.tableLayoutPanel28);
            this.managedS3Panel.Location = new System.Drawing.Point(0, 0);
            this.managedS3Panel.Name = "managedS3Panel";
            this.managedS3Panel.Size = new System.Drawing.Size(0, 0);
            this.managedS3Panel.Text = "managedPanel1";
            // 
            // tableLayoutPanel28
            // 
            this.tableLayoutPanel28.AutoSize = true;
            this.tableLayoutPanel28.ColumnCount = 1;
            this.tableLayoutPanel28.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle(System.Windows.Forms.SizeType.Percent, 100F));
            this.tableLayoutPanel28.Controls.Add(this.groupBox19, 0, 0);
            this.tableLayoutPanel28.Controls.Add(this.groupBox22, 0, 1);
            this.tableLayoutPanel28.Controls.Add(this.groupBox3, 0, 2);
            this.tableLayoutPanel28.Controls.Add(this.groupBox23, 0, 4);
            this.tableLayoutPanel28.Dock = System.Windows.Forms.DockStyle.Fill;
            this.tableLayoutPanel28.Location = new System.Drawing.Point(0, 0);
            this.tableLayoutPanel28.Name = "tableLayoutPanel28";
            this.tableLayoutPanel28.RowCount = 5;
            this.tableLayoutPanel28.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel28.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel28.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel28.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel28.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel28.Size = new System.Drawing.Size(0, 0);
            this.tableLayoutPanel28.TabIndex = 0;
            // 
            // groupBox19
            // 
            this.groupBox19.AutoSize = true;
            this.groupBox19.Controls.Add(this.tableLayoutPanel29);
            this.groupBox19.Dock = System.Windows.Forms.DockStyle.Fill;
            this.groupBox19.Location = new System.Drawing.Point(3, 3);
            this.groupBox19.Name = "groupBox19";
            this.groupBox19.Size = new System.Drawing.Size(1, 81);
            this.groupBox19.TabIndex = 0;
            this.groupBox19.TabStop = false;
            this.groupBox19.Text = "Default Bucket Location";
            // 
            // tableLayoutPanel29
            // 
            this.tableLayoutPanel29.AutoSize = true;
            this.tableLayoutPanel29.ColumnCount = 1;
            this.tableLayoutPanel29.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle(System.Windows.Forms.SizeType.Absolute, 738F));
            this.tableLayoutPanel29.Controls.Add(this.label29, 0, 0);
            this.tableLayoutPanel29.Controls.Add(this.defaultBucketLocationCombobox, 0, 1);
            this.tableLayoutPanel29.Dock = System.Windows.Forms.DockStyle.Fill;
            this.tableLayoutPanel29.Location = new System.Drawing.Point(3, 19);
            this.tableLayoutPanel29.Name = "tableLayoutPanel29";
            this.tableLayoutPanel29.Padding = new System.Windows.Forms.Padding(5);
            this.tableLayoutPanel29.RowCount = 2;
            this.tableLayoutPanel29.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel29.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel29.Size = new System.Drawing.Size(0, 59);
            this.tableLayoutPanel29.TabIndex = 2;
            // 
            // label29
            // 
            this.label29.AutoSize = true;
            this.label29.ForeColor = System.Drawing.SystemColors.GrayText;
            this.label29.Location = new System.Drawing.Point(8, 5);
            this.label29.Name = "label29";
            this.label29.Padding = new System.Windows.Forms.Padding(0, 0, 0, 5);
            this.label29.Size = new System.Drawing.Size(365, 20);
            this.label29.TabIndex = 0;
            this.label29.Text = "The geographic location in which buckets will be created by default.";
            // 
            // defaultBucketLocationCombobox
            // 
            this.defaultBucketLocationCombobox.DropDownStyle = System.Windows.Forms.ComboBoxStyle.DropDownList;
            this.defaultBucketLocationCombobox.FormattingEnabled = true;
            this.defaultBucketLocationCombobox.Location = new System.Drawing.Point(8, 28);
            this.defaultBucketLocationCombobox.Name = "defaultBucketLocationCombobox";
            this.defaultBucketLocationCombobox.Size = new System.Drawing.Size(279, 23);
            this.defaultBucketLocationCombobox.TabIndex = 1;
            this.defaultBucketLocationCombobox.SelectionChangeCommitted += new System.EventHandler(this.defaultBucketLocationCombobox_SelectionChangeCommitted);
            // 
            // groupBox22
            // 
            this.groupBox22.AutoSize = true;
            this.groupBox22.Controls.Add(this.tableLayoutPanel30);
            this.groupBox22.Dock = System.Windows.Forms.DockStyle.Fill;
            this.groupBox22.Location = new System.Drawing.Point(3, 90);
            this.groupBox22.Name = "groupBox22";
            this.groupBox22.Size = new System.Drawing.Size(1, 86);
            this.groupBox22.TabIndex = 1;
            this.groupBox22.TabStop = false;
            this.groupBox22.Text = "Default Storage Class";
            // 
            // tableLayoutPanel30
            // 
            this.tableLayoutPanel30.AutoSize = true;
            this.tableLayoutPanel30.ColumnCount = 1;
            this.tableLayoutPanel30.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle(System.Windows.Forms.SizeType.Absolute, 738F));
            this.tableLayoutPanel30.Controls.Add(this.defaultStorageClassComboBox, 0, 1);
            this.tableLayoutPanel30.Controls.Add(this.label3, 0, 0);
            this.tableLayoutPanel30.Dock = System.Windows.Forms.DockStyle.Fill;
            this.tableLayoutPanel30.Location = new System.Drawing.Point(3, 19);
            this.tableLayoutPanel30.Name = "tableLayoutPanel30";
            this.tableLayoutPanel30.Padding = new System.Windows.Forms.Padding(5);
            this.tableLayoutPanel30.RowCount = 2;
            this.tableLayoutPanel30.RowStyles.Add(new System.Windows.Forms.RowStyle(System.Windows.Forms.SizeType.Percent, 50F));
            this.tableLayoutPanel30.RowStyles.Add(new System.Windows.Forms.RowStyle(System.Windows.Forms.SizeType.Percent, 50F));
            this.tableLayoutPanel30.Size = new System.Drawing.Size(0, 64);
            this.tableLayoutPanel30.TabIndex = 0;
            // 
            // defaultStorageClassComboBox
            // 
            this.defaultStorageClassComboBox.DropDownStyle = System.Windows.Forms.ComboBoxStyle.DropDownList;
            this.defaultStorageClassComboBox.FormattingEnabled = true;
            this.defaultStorageClassComboBox.Location = new System.Drawing.Point(8, 35);
            this.defaultStorageClassComboBox.Name = "defaultStorageClassComboBox";
            this.defaultStorageClassComboBox.Size = new System.Drawing.Size(279, 23);
            this.defaultStorageClassComboBox.TabIndex = 2;
            this.defaultStorageClassComboBox.SelectionChangeCommitted += new System.EventHandler(this.defaultStorageClassComboBox_SelectionChangeCommitted);
            // 
            // label3
            // 
            this.label3.AutoSize = true;
            this.label3.ForeColor = System.Drawing.SystemColors.GrayText;
            this.label3.Location = new System.Drawing.Point(8, 5);
            this.label3.Name = "label3";
            this.label3.Padding = new System.Windows.Forms.Padding(0, 0, 0, 5);
            this.label3.Size = new System.Drawing.Size(700, 20);
            this.label3.TabIndex = 0;
            this.label3.Text = "Choose Reduced Reduncancy Storage (RRS) to reduce costs by storing non-critical, " +
    "reproducible data at lower levels of redundancy.";
            // 
            // groupBox3
            // 
            this.groupBox3.Controls.Add(this.tableLayoutPanel20);
            this.groupBox3.Dock = System.Windows.Forms.DockStyle.Fill;
            this.groupBox3.Location = new System.Drawing.Point(3, 182);
            this.groupBox3.Name = "groupBox3";
            this.groupBox3.Size = new System.Drawing.Size(1, 100);
            this.groupBox3.TabIndex = 2;
            this.groupBox3.TabStop = false;
            this.groupBox3.Text = "Encryption";
            // 
            // tableLayoutPanel20
            // 
            this.tableLayoutPanel20.AutoSize = true;
            this.tableLayoutPanel20.ColumnCount = 1;
            this.tableLayoutPanel20.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle(System.Windows.Forms.SizeType.Absolute, 738F));
            this.tableLayoutPanel20.Controls.Add(this.defaultEncryptionComboBox, 0, 1);
            this.tableLayoutPanel20.Controls.Add(this.label8, 0, 0);
            this.tableLayoutPanel20.Dock = System.Windows.Forms.DockStyle.Fill;
            this.tableLayoutPanel20.Location = new System.Drawing.Point(3, 19);
            this.tableLayoutPanel20.Name = "tableLayoutPanel20";
            this.tableLayoutPanel20.Padding = new System.Windows.Forms.Padding(5);
            this.tableLayoutPanel20.RowCount = 2;
            this.tableLayoutPanel20.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel20.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel20.Size = new System.Drawing.Size(0, 78);
            this.tableLayoutPanel20.TabIndex = 0;
            // 
            // defaultEncryptionComboBox
            // 
            this.defaultEncryptionComboBox.DropDownStyle = System.Windows.Forms.ComboBoxStyle.DropDownList;
            this.defaultEncryptionComboBox.FormattingEnabled = true;
            this.defaultEncryptionComboBox.Location = new System.Drawing.Point(8, 28);
            this.defaultEncryptionComboBox.Name = "defaultEncryptionComboBox";
            this.defaultEncryptionComboBox.Size = new System.Drawing.Size(279, 23);
            this.defaultEncryptionComboBox.TabIndex = 3;
            this.defaultEncryptionComboBox.SelectionChangeCommitted += new System.EventHandler(this.defaultEncryptionComboBox_SelectionChangeCommitted);
            // 
            // label8
            // 
            this.label8.AutoSize = true;
            this.label8.ForeColor = System.Drawing.SystemColors.GrayText;
            this.label8.Location = new System.Drawing.Point(8, 5);
            this.label8.Name = "label8";
            this.label8.Padding = new System.Windows.Forms.Padding(0, 0, 0, 5);
            this.label8.Size = new System.Drawing.Size(289, 20);
            this.label8.TabIndex = 1;
            this.label8.Text = "Choose the algorithm to encrypt files with on upload.";
            // 
            // groupBox23
            // 
            this.groupBox23.Controls.Add(this.tableLayoutPanel33);
            this.groupBox23.Dock = System.Windows.Forms.DockStyle.Fill;
            this.groupBox23.Location = new System.Drawing.Point(3, 288);
            this.groupBox23.Name = "groupBox23";
            this.groupBox23.Size = new System.Drawing.Size(1, 188);
            this.groupBox23.TabIndex = 3;
            this.groupBox23.TabStop = false;
            this.groupBox23.Text = "Default ACL";
            // 
            // tableLayoutPanel33
            // 
            this.tableLayoutPanel33.AutoSize = true;
            this.tableLayoutPanel33.ColumnCount = 1;
            this.tableLayoutPanel33.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle(System.Windows.Forms.SizeType.Absolute, 738F));
            this.tableLayoutPanel33.Controls.Add(this.defaultACLComboBox, 0, 1);
            this.tableLayoutPanel33.Controls.Add(this.label5, 0, 0);
            this.tableLayoutPanel33.Dock = System.Windows.Forms.DockStyle.Fill;
            this.tableLayoutPanel33.Location = new System.Drawing.Point(3, 19);
            this.tableLayoutPanel33.Name = "tableLayoutPanel33";
            this.tableLayoutPanel33.Padding = new System.Windows.Forms.Padding(5);
            this.tableLayoutPanel33.RowCount = 2;
            this.tableLayoutPanel33.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel33.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel33.Size = new System.Drawing.Size(0, 166);
            this.tableLayoutPanel33.TabIndex = 0;
            // 
            // defaultACLComboBox
            // 
            this.defaultACLComboBox.DropDownStyle = System.Windows.Forms.ComboBoxStyle.DropDownList;
            this.defaultACLComboBox.FormattingEnabled = true;
            this.defaultACLComboBox.Location = new System.Drawing.Point(8, 28);
            this.defaultACLComboBox.Name = "defaultACLComboBox";
            this.defaultACLComboBox.Size = new System.Drawing.Size(279, 23);
            this.defaultACLComboBox.TabIndex = 4;
            this.defaultACLComboBox.SelectionChangeCommitted += new System.EventHandler(this.defaultACLComboBox_SelectionChangeCommitted);
            // 
            // label5
            // 
            this.label5.AutoSize = true;
            this.label5.ForeColor = System.Drawing.SystemColors.GrayText;
            this.label5.Location = new System.Drawing.Point(8, 5);
            this.label5.Name = "label5";
            this.label5.Padding = new System.Windows.Forms.Padding(0, 0, 0, 5);
            this.label5.Size = new System.Drawing.Size(475, 20);
            this.label5.TabIndex = 2;
            this.label5.Text = "Choose the predefined set of grantees and permissions to apply to new buckets and" +
    " files.";
            // 
            // managedBandwidthPanel
            // 
            this.managedBandwidthPanel.Controls.Add(this.tableLayoutPanel34);
            this.managedBandwidthPanel.Location = new System.Drawing.Point(0, 0);
            this.managedBandwidthPanel.Name = "managedBandwidthPanel";
            this.managedBandwidthPanel.Size = new System.Drawing.Size(0, 0);
            this.managedBandwidthPanel.Text = "managedPanel1";
            // 
            // tableLayoutPanel34
            // 
            this.tableLayoutPanel34.AutoSize = true;
            this.tableLayoutPanel34.ColumnCount = 1;
            this.tableLayoutPanel34.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle(System.Windows.Forms.SizeType.Percent, 100F));
            this.tableLayoutPanel34.Controls.Add(this.groupBox25, 0, 0);
            this.tableLayoutPanel34.Controls.Add(this.groupBox26, 0, 1);
            this.tableLayoutPanel34.Dock = System.Windows.Forms.DockStyle.Fill;
            this.tableLayoutPanel34.Location = new System.Drawing.Point(0, 0);
            this.tableLayoutPanel34.Name = "tableLayoutPanel34";
            this.tableLayoutPanel34.RowCount = 3;
            this.tableLayoutPanel34.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel34.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel34.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel34.Size = new System.Drawing.Size(0, 0);
            this.tableLayoutPanel34.TabIndex = 0;
            // 
            // groupBox25
            // 
            this.groupBox25.AutoSize = true;
            this.groupBox25.Controls.Add(this.tableLayoutPanel35);
            this.groupBox25.Dock = System.Windows.Forms.DockStyle.Fill;
            this.groupBox25.Location = new System.Drawing.Point(3, 3);
            this.groupBox25.Name = "groupBox25";
            this.groupBox25.Size = new System.Drawing.Size(1, 61);
            this.groupBox25.TabIndex = 0;
            this.groupBox25.TabStop = false;
            this.groupBox25.Text = "Downloads";
            // 
            // tableLayoutPanel35
            // 
            this.tableLayoutPanel35.AutoSize = true;
            this.tableLayoutPanel35.ColumnCount = 2;
            this.tableLayoutPanel35.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle(System.Windows.Forms.SizeType.Absolute, 250F));
            this.tableLayoutPanel35.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle());
            this.tableLayoutPanel35.Controls.Add(this.label31, 0, 0);
            this.tableLayoutPanel35.Controls.Add(this.defaultDownloadThrottleCombobox, 1, 0);
            this.tableLayoutPanel35.Dock = System.Windows.Forms.DockStyle.Fill;
            this.tableLayoutPanel35.Location = new System.Drawing.Point(3, 19);
            this.tableLayoutPanel35.Name = "tableLayoutPanel35";
            this.tableLayoutPanel35.Padding = new System.Windows.Forms.Padding(5);
            this.tableLayoutPanel35.RowCount = 1;
            this.tableLayoutPanel35.RowStyles.Add(new System.Windows.Forms.RowStyle(System.Windows.Forms.SizeType.Percent, 100F));
            this.tableLayoutPanel35.Size = new System.Drawing.Size(0, 39);
            this.tableLayoutPanel35.TabIndex = 0;
            // 
            // label31
            // 
            this.label31.Anchor = System.Windows.Forms.AnchorStyles.Right;
            this.label31.AutoSize = true;
            this.label31.Location = new System.Drawing.Point(121, 12);
            this.label31.Name = "label31";
            this.label31.Size = new System.Drawing.Size(131, 15);
            this.label31.TabIndex = 2;
            this.label31.Text = "Maximum Throughput:";
            this.label31.TextAlign = System.Drawing.ContentAlignment.TopRight;
            // 
            // defaultDownloadThrottleCombobox
            // 
            this.defaultDownloadThrottleCombobox.Anchor = System.Windows.Forms.AnchorStyles.Left;
            this.defaultDownloadThrottleCombobox.DropDownStyle = System.Windows.Forms.ComboBoxStyle.DropDownList;
            this.defaultDownloadThrottleCombobox.FormattingEnabled = true;
            this.defaultDownloadThrottleCombobox.Location = new System.Drawing.Point(258, 8);
            this.defaultDownloadThrottleCombobox.Name = "defaultDownloadThrottleCombobox";
            this.defaultDownloadThrottleCombobox.Size = new System.Drawing.Size(272, 23);
            this.defaultDownloadThrottleCombobox.TabIndex = 1;
            this.defaultDownloadThrottleCombobox.SelectionChangeCommitted += new System.EventHandler(this.defaultDownloadThrottleCombobox_SelectionChangeCommitted);
            // 
            // groupBox26
            // 
            this.groupBox26.AutoSize = true;
            this.groupBox26.Controls.Add(this.tableLayoutPanel36);
            this.groupBox26.Dock = System.Windows.Forms.DockStyle.Fill;
            this.groupBox26.Location = new System.Drawing.Point(3, 70);
            this.groupBox26.Name = "groupBox26";
            this.groupBox26.Size = new System.Drawing.Size(1, 61);
            this.groupBox26.TabIndex = 1;
            this.groupBox26.TabStop = false;
            this.groupBox26.Text = "Uploads";
            // 
            // tableLayoutPanel36
            // 
            this.tableLayoutPanel36.AutoSize = true;
            this.tableLayoutPanel36.ColumnCount = 2;
            this.tableLayoutPanel36.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle(System.Windows.Forms.SizeType.Absolute, 250F));
            this.tableLayoutPanel36.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle());
            this.tableLayoutPanel36.Controls.Add(this.defaultUploadThrottleCombobox, 1, 0);
            this.tableLayoutPanel36.Controls.Add(this.label32, 0, 0);
            this.tableLayoutPanel36.Dock = System.Windows.Forms.DockStyle.Fill;
            this.tableLayoutPanel36.Location = new System.Drawing.Point(3, 19);
            this.tableLayoutPanel36.Name = "tableLayoutPanel36";
            this.tableLayoutPanel36.Padding = new System.Windows.Forms.Padding(5);
            this.tableLayoutPanel36.RowCount = 1;
            this.tableLayoutPanel36.RowStyles.Add(new System.Windows.Forms.RowStyle(System.Windows.Forms.SizeType.Percent, 100F));
            this.tableLayoutPanel36.Size = new System.Drawing.Size(0, 39);
            this.tableLayoutPanel36.TabIndex = 0;
            // 
            // defaultUploadThrottleCombobox
            // 
            this.defaultUploadThrottleCombobox.Anchor = System.Windows.Forms.AnchorStyles.Left;
            this.defaultUploadThrottleCombobox.DropDownStyle = System.Windows.Forms.ComboBoxStyle.DropDownList;
            this.defaultUploadThrottleCombobox.FormattingEnabled = true;
            this.defaultUploadThrottleCombobox.Location = new System.Drawing.Point(258, 8);
            this.defaultUploadThrottleCombobox.Name = "defaultUploadThrottleCombobox";
            this.defaultUploadThrottleCombobox.Size = new System.Drawing.Size(272, 23);
            this.defaultUploadThrottleCombobox.TabIndex = 4;
            this.defaultUploadThrottleCombobox.SelectionChangeCommitted += new System.EventHandler(this.defaultUploadThrottleCombobox_SelectionChangeCommitted);
            // 
            // label32
            // 
            this.label32.Anchor = System.Windows.Forms.AnchorStyles.Right;
            this.label32.AutoSize = true;
            this.label32.Location = new System.Drawing.Point(121, 12);
            this.label32.Name = "label32";
            this.label32.Size = new System.Drawing.Size(131, 15);
            this.label32.TabIndex = 5;
            this.label32.Text = "Maximum Throughput:";
            this.label32.TextAlign = System.Drawing.ContentAlignment.TopRight;
            // 
            // managedConnectionPanel
            // 
            this.managedConnectionPanel.Controls.Add(this.tableLayoutPanel37);
            this.managedConnectionPanel.Location = new System.Drawing.Point(0, 0);
            this.managedConnectionPanel.Name = "managedConnectionPanel";
            this.managedConnectionPanel.Size = new System.Drawing.Size(0, 0);
            this.managedConnectionPanel.Text = "managedPanel1";
            // 
            // tableLayoutPanel37
            // 
            this.tableLayoutPanel37.AutoSize = true;
            this.tableLayoutPanel37.ColumnCount = 1;
            this.tableLayoutPanel37.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle(System.Windows.Forms.SizeType.Percent, 100F));
            this.tableLayoutPanel37.Controls.Add(this.groupBox1, 0, 1);
            this.tableLayoutPanel37.Controls.Add(this.groupBox31, 0, 4);
            this.tableLayoutPanel37.Controls.Add(this.groupBox21, 0, 2);
            this.tableLayoutPanel37.Controls.Add(this.groupBox20, 0, 3);
            this.tableLayoutPanel37.Dock = System.Windows.Forms.DockStyle.Fill;
            this.tableLayoutPanel37.Location = new System.Drawing.Point(0, 0);
            this.tableLayoutPanel37.Name = "tableLayoutPanel37";
            this.tableLayoutPanel37.RowCount = 5;
            this.tableLayoutPanel37.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel37.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel37.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel37.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel37.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel37.RowStyles.Add(new System.Windows.Forms.RowStyle(System.Windows.Forms.SizeType.Absolute, 20F));
            this.tableLayoutPanel37.Size = new System.Drawing.Size(0, 0);
            this.tableLayoutPanel37.TabIndex = 0;
            // 
            // groupBox1
            // 
            this.groupBox1.AutoSize = true;
            this.groupBox1.Controls.Add(this.tableLayoutPanel2);
            this.groupBox1.Dock = System.Windows.Forms.DockStyle.Fill;
            this.groupBox1.Location = new System.Drawing.Point(3, 3);
            this.groupBox1.Name = "groupBox1";
            this.groupBox1.Size = new System.Drawing.Size(1, 152);
            this.groupBox1.TabIndex = 5;
            this.groupBox1.TabStop = false;
            this.groupBox1.Text = "Connection";
            // 
            // tableLayoutPanel2
            // 
            this.tableLayoutPanel2.AutoSize = true;
            this.tableLayoutPanel2.ColumnCount = 2;
            this.tableLayoutPanel2.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle(System.Windows.Forms.SizeType.Absolute, 199F));
            this.tableLayoutPanel2.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle());
            this.tableLayoutPanel2.Controls.Add(this.keychainCheckbox, 0, 0);
            this.tableLayoutPanel2.Controls.Add(this.labelKeychain, 0, 1);
            this.tableLayoutPanel2.Controls.Add(this.confirmDisconnectCheckbox, 0, 2);
            this.tableLayoutPanel2.Controls.Add(this.label7, 0, 4);
            this.tableLayoutPanel2.Controls.Add(this.labelConfirmDisconnect, 0, 3);
            this.tableLayoutPanel2.Controls.Add(this.defaultProtocolCombobox, 1, 4);
            this.tableLayoutPanel2.Dock = System.Windows.Forms.DockStyle.Fill;
            this.tableLayoutPanel2.Location = new System.Drawing.Point(3, 19);
            this.tableLayoutPanel2.Name = "tableLayoutPanel2";
            this.tableLayoutPanel2.Padding = new System.Windows.Forms.Padding(5);
            this.tableLayoutPanel2.RowCount = 6;
            this.tableLayoutPanel2.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel2.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel2.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel2.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel2.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel2.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel2.Size = new System.Drawing.Size(0, 130);
            this.tableLayoutPanel2.TabIndex = 10;
            // 
            // keychainCheckbox
            // 
            this.keychainCheckbox.AutoSize = true;
            this.tableLayoutPanel2.SetColumnSpan(this.keychainCheckbox, 2);
            this.keychainCheckbox.Location = new System.Drawing.Point(8, 8);
            this.keychainCheckbox.Name = "keychainCheckbox";
            this.keychainCheckbox.Size = new System.Drawing.Size(96, 19);
            this.keychainCheckbox.TabIndex = 0;
            this.keychainCheckbox.Text = "Use Keychain";
            this.keychainCheckbox.UseVisualStyleBackColor = true;
            this.keychainCheckbox.CheckedChanged += new System.EventHandler(this.keychainCheckbox_CheckedChanged);
            // 
            // labelKeychain
            // 
            this.labelKeychain.AutoEllipsis = true;
            this.labelKeychain.AutoSize = true;
            this.tableLayoutPanel2.SetColumnSpan(this.labelKeychain, 2);
            this.labelKeychain.ForeColor = System.Drawing.SystemColors.GrayText;
            this.labelKeychain.Location = new System.Drawing.Point(8, 30);
            this.labelKeychain.Name = "labelKeychain";
            this.labelKeychain.Size = new System.Drawing.Size(551, 15);
            this.labelKeychain.TabIndex = 6;
            this.labelKeychain.Text = "Search for passwords in the Keychain. Save passwords upon successful login in the" +
    " Keychain by default.";
            // 
            // confirmDisconnectCheckbox
            // 
            this.confirmDisconnectCheckbox.AutoSize = true;
            this.confirmDisconnectCheckbox.Location = new System.Drawing.Point(8, 48);
            this.confirmDisconnectCheckbox.Name = "confirmDisconnectCheckbox";
            this.confirmDisconnectCheckbox.Padding = new System.Windows.Forms.Padding(0, 5, 0, 0);
            this.confirmDisconnectCheckbox.Size = new System.Drawing.Size(131, 24);
            this.confirmDisconnectCheckbox.TabIndex = 7;
            this.confirmDisconnectCheckbox.Text = "Confirm disconnect";
            this.confirmDisconnectCheckbox.UseVisualStyleBackColor = true;
            this.confirmDisconnectCheckbox.CheckedChanged += new System.EventHandler(this.confirmDisconnectCheckbox_CheckedChanged);
            // 
            // label7
            // 
            this.label7.Anchor = System.Windows.Forms.AnchorStyles.Right;
            this.label7.AutoEllipsis = true;
            this.label7.AutoSize = true;
            this.label7.Location = new System.Drawing.Point(105, 102);
            this.label7.Name = "label7";
            this.label7.Size = new System.Drawing.Size(96, 15);
            this.label7.TabIndex = 6;
            this.label7.Text = "Default protocol:";
            this.label7.TextAlign = System.Drawing.ContentAlignment.TopRight;
            // 
            // labelConfirmDisconnect
            // 
            this.labelConfirmDisconnect.AutoEllipsis = true;
            this.labelConfirmDisconnect.AutoSize = true;
            this.tableLayoutPanel2.SetColumnSpan(this.labelConfirmDisconnect, 2);
            this.labelConfirmDisconnect.ForeColor = System.Drawing.SystemColors.GrayText;
            this.labelConfirmDisconnect.Location = new System.Drawing.Point(8, 75);
            this.labelConfirmDisconnect.Name = "labelConfirmDisconnect";
            this.labelConfirmDisconnect.Padding = new System.Windows.Forms.Padding(0, 0, 0, 5);
            this.labelConfirmDisconnect.Size = new System.Drawing.Size(265, 20);
            this.labelConfirmDisconnect.TabIndex = 6;
            this.labelConfirmDisconnect.Text = "Ask before closing a connected browser window.";
            // 
            // defaultProtocolCombobox
            // 
            this.defaultProtocolCombobox.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Left | System.Windows.Forms.AnchorStyles.Right)));
            this.defaultProtocolCombobox.DrawMode = System.Windows.Forms.DrawMode.OwnerDrawFixed;
            this.defaultProtocolCombobox.DropDownStyle = System.Windows.Forms.ComboBoxStyle.DropDownList;
            this.defaultProtocolCombobox.FormattingEnabled = true;
            this.defaultProtocolCombobox.IconMember = null;
            this.defaultProtocolCombobox.ItemHeight = 18;
            this.defaultProtocolCombobox.Location = new System.Drawing.Point(207, 98);
            this.defaultProtocolCombobox.Name = "defaultProtocolCombobox";
            this.defaultProtocolCombobox.Size = new System.Drawing.Size(533, 24);
            this.defaultProtocolCombobox.TabIndex = 6;
            this.defaultProtocolCombobox.SelectionChangeCommitted += new System.EventHandler(this.defaultProtocolCombobox_SelectionChangeCommitted);
            // 
            // groupBox31
            // 
            this.groupBox31.AutoSize = true;
            this.groupBox31.Controls.Add(this.tableLayoutPanel48);
            this.groupBox31.Dock = System.Windows.Forms.DockStyle.Fill;
            this.groupBox31.Location = new System.Drawing.Point(3, 405);
            this.groupBox31.Name = "groupBox31";
            this.groupBox31.Size = new System.Drawing.Size(1, 88);
            this.groupBox31.TabIndex = 4;
            this.groupBox31.TabStop = false;
            this.groupBox31.Text = "Log";
            // 
            // tableLayoutPanel48
            // 
            this.tableLayoutPanel48.AutoSize = true;
            this.tableLayoutPanel48.ColumnCount = 1;
            this.tableLayoutPanel48.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle(System.Windows.Forms.SizeType.Percent, 100F));
            this.tableLayoutPanel48.Controls.Add(this.debugLogCheckBox, 0, 0);
            this.tableLayoutPanel48.Controls.Add(this.showDebugLogButton, 0, 1);
            this.tableLayoutPanel48.Dock = System.Windows.Forms.DockStyle.Fill;
            this.tableLayoutPanel48.Location = new System.Drawing.Point(3, 19);
            this.tableLayoutPanel48.Name = "tableLayoutPanel48";
            this.tableLayoutPanel48.Padding = new System.Windows.Forms.Padding(5);
            this.tableLayoutPanel48.RowCount = 2;
            this.tableLayoutPanel48.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel48.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel48.Size = new System.Drawing.Size(0, 66);
            this.tableLayoutPanel48.TabIndex = 0;
            // 
            // debugLogCheckBox
            // 
            this.debugLogCheckBox.AutoSize = true;
            this.debugLogCheckBox.Location = new System.Drawing.Point(8, 8);
            this.debugLogCheckBox.Name = "debugLogCheckBox";
            this.debugLogCheckBox.Size = new System.Drawing.Size(1, 19);
            this.debugLogCheckBox.TabIndex = 0;
            this.debugLogCheckBox.Text = "Enable debug log";
            this.debugLogCheckBox.UseVisualStyleBackColor = true;
            this.debugLogCheckBox.CheckStateChanged += new System.EventHandler(this.debugLogCheckBox_CheckStateChanged);
            // 
            // showDebugLogButton
            // 
            this.showDebugLogButton.Anchor = System.Windows.Forms.AnchorStyles.Top;
            this.showDebugLogButton.AutoSize = true;
            this.showDebugLogButton.Location = new System.Drawing.Point(8, 33);
            this.showDebugLogButton.Name = "showDebugLogButton";
            this.showDebugLogButton.Size = new System.Drawing.Size(1, 25);
            this.showDebugLogButton.TabIndex = 1;
            this.showDebugLogButton.Text = "Show";
            this.showDebugLogButton.UseVisualStyleBackColor = true;
            this.showDebugLogButton.Click += new System.EventHandler(this.showDebugLogButton_Click);
            // 
            // groupBox21
            // 
            this.groupBox21.AutoSize = true;
            this.groupBox21.Controls.Add(this.tableLayoutPanel39);
            this.groupBox21.Dock = System.Windows.Forms.DockStyle.Fill;
            this.groupBox21.Location = new System.Drawing.Point(3, 161);
            this.groupBox21.Name = "groupBox21";
            this.groupBox21.Size = new System.Drawing.Size(1, 144);
            this.groupBox21.TabIndex = 0;
            this.groupBox21.TabStop = false;
            this.groupBox21.Text = "Timeouts";
            // 
            // tableLayoutPanel39
            // 
            this.tableLayoutPanel39.AutoSize = true;
            this.tableLayoutPanel39.ColumnCount = 2;
            this.tableLayoutPanel39.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle(System.Windows.Forms.SizeType.Percent, 50F));
            this.tableLayoutPanel39.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle(System.Windows.Forms.SizeType.Percent, 50F));
            this.tableLayoutPanel39.Controls.Add(this.connectionTimeoutUpDown, 1, 0);
            this.tableLayoutPanel39.Controls.Add(this.retriesUpDown, 1, 3);
            this.tableLayoutPanel39.Controls.Add(this.retryCheckbox, 0, 1);
            this.tableLayoutPanel39.Controls.Add(this.label34, 0, 3);
            this.tableLayoutPanel39.Controls.Add(this.label36, 0, 0);
            this.tableLayoutPanel39.Controls.Add(this.retryDelayUpDown, 1, 2);
            this.tableLayoutPanel39.Controls.Add(this.label16, 0, 2);
            this.tableLayoutPanel39.Dock = System.Windows.Forms.DockStyle.Fill;
            this.tableLayoutPanel39.Location = new System.Drawing.Point(3, 19);
            this.tableLayoutPanel39.Name = "tableLayoutPanel39";
            this.tableLayoutPanel39.Padding = new System.Windows.Forms.Padding(5);
            this.tableLayoutPanel39.RowCount = 4;
            this.tableLayoutPanel39.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel39.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel39.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel39.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel39.Size = new System.Drawing.Size(0, 122);
            this.tableLayoutPanel39.TabIndex = 12;
            // 
            // connectionTimeoutUpDown
            // 
            this.connectionTimeoutUpDown.Anchor = System.Windows.Forms.AnchorStyles.Left;
            this.connectionTimeoutUpDown.AutoSize = true;
            this.connectionTimeoutUpDown.Location = new System.Drawing.Point(8, 8);
            this.connectionTimeoutUpDown.Maximum = new decimal(new int[] {
            60,
            0,
            0,
            0});
            this.connectionTimeoutUpDown.Minimum = new decimal(new int[] {
            10,
            0,
            0,
            0});
            this.connectionTimeoutUpDown.Name = "connectionTimeoutUpDown";
            this.connectionTimeoutUpDown.Size = new System.Drawing.Size(1, 23);
            this.connectionTimeoutUpDown.TabIndex = 6;
            this.connectionTimeoutUpDown.Value = new decimal(new int[] {
            10,
            0,
            0,
            0});
            this.connectionTimeoutUpDown.ValueChanged += new System.EventHandler(this.connectionTimeoutUpDown_ValueChanged);
            // 
            // retriesUpDown
            // 
            this.retriesUpDown.Anchor = System.Windows.Forms.AnchorStyles.Left;
            this.retriesUpDown.AutoSize = true;
            this.retriesUpDown.Location = new System.Drawing.Point(8, 91);
            this.retriesUpDown.Maximum = new decimal(new int[] {
            9,
            0,
            0,
            0});
            this.retriesUpDown.Name = "retriesUpDown";
            this.retriesUpDown.Size = new System.Drawing.Size(1, 23);
            this.retriesUpDown.TabIndex = 11;
            this.retriesUpDown.Value = new decimal(new int[] {
            9,
            0,
            0,
            0});
            this.retriesUpDown.ValueChanged += new System.EventHandler(this.retriesUpDown_ValueChanged);
            // 
            // retryCheckbox
            // 
            this.retryCheckbox.AutoSize = true;
            this.retryCheckbox.Location = new System.Drawing.Point(8, 37);
            this.retryCheckbox.Name = "retryCheckbox";
            this.retryCheckbox.Size = new System.Drawing.Size(1, 19);
            this.retryCheckbox.TabIndex = 7;
            this.retryCheckbox.Text = "Repeat failed networking tasks";
            this.retryCheckbox.UseVisualStyleBackColor = true;
            this.retryCheckbox.CheckedChanged += new System.EventHandler(this.retryCheckbox_CheckedChanged);
            // 
            // label34
            // 
            this.label34.Anchor = System.Windows.Forms.AnchorStyles.Right;
            this.label34.AutoSize = true;
            this.label34.Location = new System.Drawing.Point(8, 95);
            this.label34.Name = "label34";
            this.label34.Size = new System.Drawing.Size(1, 15);
            this.label34.TabIndex = 10;
            this.label34.Text = "Number of retries:";
            this.label34.TextAlign = System.Drawing.ContentAlignment.TopRight;
            // 
            // label36
            // 
            this.label36.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Left | System.Windows.Forms.AnchorStyles.Right)));
            this.label36.AutoSize = true;
            this.label36.Location = new System.Drawing.Point(8, 12);
            this.label36.Name = "label36";
            this.label36.Size = new System.Drawing.Size(1, 15);
            this.label36.TabIndex = 2;
            this.label36.Text = "Timeout for opening connections (seconds):";
            this.label36.TextAlign = System.Drawing.ContentAlignment.TopRight;
            // 
            // retryDelayUpDown
            // 
            this.retryDelayUpDown.Anchor = System.Windows.Forms.AnchorStyles.Left;
            this.retryDelayUpDown.AutoSize = true;
            this.retryDelayUpDown.Location = new System.Drawing.Point(8, 62);
            this.retryDelayUpDown.Maximum = new decimal(new int[] {
            30,
            0,
            0,
            0});
            this.retryDelayUpDown.Name = "retryDelayUpDown";
            this.retryDelayUpDown.Size = new System.Drawing.Size(1, 23);
            this.retryDelayUpDown.TabIndex = 9;
            this.retryDelayUpDown.Value = new decimal(new int[] {
            10,
            0,
            0,
            0});
            this.retryDelayUpDown.ValueChanged += new System.EventHandler(this.retryDelayUpDown_ValueChanged);
            // 
            // label16
            // 
            this.label16.Anchor = System.Windows.Forms.AnchorStyles.Right;
            this.label16.AutoSize = true;
            this.label16.Location = new System.Drawing.Point(8, 66);
            this.label16.Name = "label16";
            this.label16.Size = new System.Drawing.Size(1, 15);
            this.label16.TabIndex = 12;
            this.label16.Text = "with delay (seconds):";
            // 
            // groupBox20
            // 
            this.groupBox20.AutoSize = true;
            this.groupBox20.Controls.Add(this.tableLayoutPanel40);
            this.groupBox20.Dock = System.Windows.Forms.DockStyle.Fill;
            this.groupBox20.Location = new System.Drawing.Point(3, 311);
            this.groupBox20.Name = "groupBox20";
            this.groupBox20.Size = new System.Drawing.Size(1, 88);
            this.groupBox20.TabIndex = 3;
            this.groupBox20.TabStop = false;
            this.groupBox20.Text = "Proxies";
            // 
            // tableLayoutPanel40
            // 
            this.tableLayoutPanel40.AutoSize = true;
            this.tableLayoutPanel40.ColumnCount = 1;
            this.tableLayoutPanel40.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle(System.Windows.Forms.SizeType.Percent, 100F));
            this.tableLayoutPanel40.Controls.Add(this.systemProxyCheckBox, 0, 0);
            this.tableLayoutPanel40.Controls.Add(this.changeSystemProxyButton, 0, 1);
            this.tableLayoutPanel40.Dock = System.Windows.Forms.DockStyle.Fill;
            this.tableLayoutPanel40.Location = new System.Drawing.Point(3, 19);
            this.tableLayoutPanel40.Name = "tableLayoutPanel40";
            this.tableLayoutPanel40.Padding = new System.Windows.Forms.Padding(5);
            this.tableLayoutPanel40.RowCount = 2;
            this.tableLayoutPanel40.RowStyles.Add(new System.Windows.Forms.RowStyle(System.Windows.Forms.SizeType.Percent, 100F));
            this.tableLayoutPanel40.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel40.Size = new System.Drawing.Size(0, 66);
            this.tableLayoutPanel40.TabIndex = 0;
            // 
            // systemProxyCheckBox
            // 
            this.systemProxyCheckBox.AutoSize = true;
            this.systemProxyCheckBox.Location = new System.Drawing.Point(8, 8);
            this.systemProxyCheckBox.Name = "systemProxyCheckBox";
            this.systemProxyCheckBox.Size = new System.Drawing.Size(1, 19);
            this.systemProxyCheckBox.TabIndex = 0;
            this.systemProxyCheckBox.Text = "Use system proxy settings";
            this.systemProxyCheckBox.UseVisualStyleBackColor = true;
            this.systemProxyCheckBox.CheckStateChanged += new System.EventHandler(this.systemProxyCheckBox_CheckStateChanged);
            // 
            // changeSystemProxyButton
            // 
            this.changeSystemProxyButton.Anchor = System.Windows.Forms.AnchorStyles.Top;
            this.changeSystemProxyButton.AutoSize = true;
            this.changeSystemProxyButton.Location = new System.Drawing.Point(8, 33);
            this.changeSystemProxyButton.Name = "changeSystemProxyButton";
            this.changeSystemProxyButton.Size = new System.Drawing.Size(1, 25);
            this.changeSystemProxyButton.TabIndex = 1;
            this.changeSystemProxyButton.Text = "Change Settings…";
            this.changeSystemProxyButton.UseVisualStyleBackColor = true;
            this.changeSystemProxyButton.Click += new System.EventHandler(this.changeSystemProxyButton_Click);
            // 
            // managedUpdatePanel
            // 
            this.managedUpdatePanel.Controls.Add(this.tableLayoutPanel41);
            this.managedUpdatePanel.Location = new System.Drawing.Point(0, 0);
            this.managedUpdatePanel.Name = "managedUpdatePanel";
            this.managedUpdatePanel.Size = new System.Drawing.Size(0, 0);
            this.managedUpdatePanel.Text = "managedPanel1";
            // 
            // tableLayoutPanel41
            // 
            this.tableLayoutPanel41.ColumnCount = 1;
            this.tableLayoutPanel41.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle(System.Windows.Forms.SizeType.Percent, 100F));
            this.tableLayoutPanel41.Controls.Add(this.groupBox27, 0, 0);
            this.tableLayoutPanel41.Dock = System.Windows.Forms.DockStyle.Fill;
            this.tableLayoutPanel41.Location = new System.Drawing.Point(0, 0);
            this.tableLayoutPanel41.Name = "tableLayoutPanel41";
            this.tableLayoutPanel41.RowCount = 2;
            this.tableLayoutPanel41.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel41.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel41.Size = new System.Drawing.Size(0, 0);
            this.tableLayoutPanel41.TabIndex = 0;
            // 
            // groupBox27
            // 
            this.groupBox27.AutoSize = true;
            this.groupBox27.Controls.Add(this.tableLayoutPanel42);
            this.groupBox27.Dock = System.Windows.Forms.DockStyle.Fill;
            this.groupBox27.Location = new System.Drawing.Point(3, 3);
            this.groupBox27.Name = "groupBox27";
            this.groupBox27.Size = new System.Drawing.Size(1, 112);
            this.groupBox27.TabIndex = 0;
            this.groupBox27.TabStop = false;
            this.groupBox27.Text = "Update";
            // 
            // tableLayoutPanel42
            // 
            this.tableLayoutPanel42.AutoSize = true;
            this.tableLayoutPanel42.ColumnCount = 2;
            this.tableLayoutPanel42.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle());
            this.tableLayoutPanel42.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle());
            this.tableLayoutPanel42.Controls.Add(this.updateCheckBox, 0, 0);
            this.tableLayoutPanel42.Controls.Add(this.updateCheckButton, 0, 1);
            this.tableLayoutPanel42.Controls.Add(this.lastUpdateLabel, 0, 3);
            this.tableLayoutPanel42.Controls.Add(this.updateFeedComboBox, 1, 0);
            this.tableLayoutPanel42.Dock = System.Windows.Forms.DockStyle.Fill;
            this.tableLayoutPanel42.Location = new System.Drawing.Point(3, 19);
            this.tableLayoutPanel42.Name = "tableLayoutPanel42";
            this.tableLayoutPanel42.Padding = new System.Windows.Forms.Padding(5);
            this.tableLayoutPanel42.RowCount = 4;
            this.tableLayoutPanel42.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel42.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel42.RowStyles.Add(new System.Windows.Forms.RowStyle(System.Windows.Forms.SizeType.Absolute, 5F));
            this.tableLayoutPanel42.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel42.Size = new System.Drawing.Size(0, 90);
            this.tableLayoutPanel42.TabIndex = 0;
            // 
            // updateCheckBox
            // 
            this.updateCheckBox.Anchor = System.Windows.Forms.AnchorStyles.Left;
            this.updateCheckBox.AutoSize = true;
            this.updateCheckBox.Location = new System.Drawing.Point(8, 10);
            this.updateCheckBox.Name = "updateCheckBox";
            this.updateCheckBox.Size = new System.Drawing.Size(210, 19);
            this.updateCheckBox.TabIndex = 0;
            this.updateCheckBox.Text = "Automatically check for updates in";
            this.updateCheckBox.UseVisualStyleBackColor = true;
            this.updateCheckBox.CheckedChanged += new System.EventHandler(this.updateCheckBox_CheckedChanged);
            // 
            // updateCheckButton
            // 
            this.updateCheckButton.Anchor = System.Windows.Forms.AnchorStyles.Top;
            this.updateCheckButton.AutoSize = true;
            this.updateCheckButton.AutoSizeMode = System.Windows.Forms.AutoSizeMode.GrowAndShrink;
            this.tableLayoutPanel42.SetColumnSpan(this.updateCheckButton, 2);
            this.updateCheckButton.Location = new System.Drawing.Point(161, 37);
            this.updateCheckButton.Name = "updateCheckButton";
            this.updateCheckButton.Size = new System.Drawing.Size(137, 25);
            this.updateCheckButton.TabIndex = 1;
            this.updateCheckButton.Text = "Check for Update Now";
            this.updateCheckButton.UseVisualStyleBackColor = true;
            this.updateCheckButton.Click += new System.EventHandler(this.updateCheckButton_Click);
            // 
            // lastUpdateLabel
            // 
            this.lastUpdateLabel.Anchor = System.Windows.Forms.AnchorStyles.Top;
            this.lastUpdateLabel.AutoSize = true;
            this.tableLayoutPanel42.SetColumnSpan(this.lastUpdateLabel, 2);
            this.lastUpdateLabel.Location = new System.Drawing.Point(207, 70);
            this.lastUpdateLabel.Name = "lastUpdateLabel";
            this.lastUpdateLabel.Size = new System.Drawing.Size(44, 15);
            this.lastUpdateLabel.TabIndex = 2;
            this.lastUpdateLabel.Text = "label17";
            // 
            // updateFeedComboBox
            // 
            this.updateFeedComboBox.DropDownStyle = System.Windows.Forms.ComboBoxStyle.DropDownList;
            this.updateFeedComboBox.FormattingEnabled = true;
            this.updateFeedComboBox.Location = new System.Drawing.Point(224, 8);
            this.updateFeedComboBox.Name = "updateFeedComboBox";
            this.updateFeedComboBox.Size = new System.Drawing.Size(227, 23);
            this.updateFeedComboBox.TabIndex = 3;
            this.updateFeedComboBox.SelectionChangeCommitted += new System.EventHandler(this.updateFeedComboBox_SelectionChangeCommitted);
            // 
            // managedLanguagePanel
            // 
            this.managedLanguagePanel.Controls.Add(this.tableLayoutPanel43);
            this.managedLanguagePanel.Location = new System.Drawing.Point(0, 0);
            this.managedLanguagePanel.Name = "managedLanguagePanel";
            this.managedLanguagePanel.Size = new System.Drawing.Size(0, 0);
            this.managedLanguagePanel.Text = "managedPanel1";
            // 
            // tableLayoutPanel43
            // 
            this.tableLayoutPanel43.AutoSize = true;
            this.tableLayoutPanel43.ColumnCount = 1;
            this.tableLayoutPanel43.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle(System.Windows.Forms.SizeType.Percent, 100F));
            this.tableLayoutPanel43.Controls.Add(this.groupBox28, 0, 0);
            this.tableLayoutPanel43.Dock = System.Windows.Forms.DockStyle.Fill;
            this.tableLayoutPanel43.Location = new System.Drawing.Point(0, 0);
            this.tableLayoutPanel43.Name = "tableLayoutPanel43";
            this.tableLayoutPanel43.RowCount = 2;
            this.tableLayoutPanel43.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel43.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel43.Size = new System.Drawing.Size(0, 0);
            this.tableLayoutPanel43.TabIndex = 0;
            // 
            // groupBox28
            // 
            this.groupBox28.AutoSize = true;
            this.groupBox28.Controls.Add(this.tableLayoutPanel44);
            this.groupBox28.Dock = System.Windows.Forms.DockStyle.Fill;
            this.groupBox28.Location = new System.Drawing.Point(3, 3);
            this.groupBox28.Name = "groupBox28";
            this.groupBox28.Size = new System.Drawing.Size(1, 81);
            this.groupBox28.TabIndex = 0;
            this.groupBox28.TabStop = false;
            this.groupBox28.Text = "Language";
            // 
            // tableLayoutPanel44
            // 
            this.tableLayoutPanel44.AutoSize = true;
            this.tableLayoutPanel44.ColumnCount = 1;
            this.tableLayoutPanel44.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle(System.Windows.Forms.SizeType.Absolute, 577F));
            this.tableLayoutPanel44.Controls.Add(this.languageComboBox, 0, 1);
            this.tableLayoutPanel44.Controls.Add(this.label18, 0, 0);
            this.tableLayoutPanel44.Dock = System.Windows.Forms.DockStyle.Fill;
            this.tableLayoutPanel44.Location = new System.Drawing.Point(3, 19);
            this.tableLayoutPanel44.Name = "tableLayoutPanel44";
            this.tableLayoutPanel44.Padding = new System.Windows.Forms.Padding(5);
            this.tableLayoutPanel44.RowCount = 2;
            this.tableLayoutPanel44.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel44.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel44.Size = new System.Drawing.Size(0, 59);
            this.tableLayoutPanel44.TabIndex = 0;
            // 
            // languageComboBox
            // 
            this.languageComboBox.DropDownStyle = System.Windows.Forms.ComboBoxStyle.DropDownList;
            this.languageComboBox.Location = new System.Drawing.Point(8, 28);
            this.languageComboBox.Name = "languageComboBox";
            this.languageComboBox.Size = new System.Drawing.Size(308, 23);
            this.languageComboBox.TabIndex = 2;
            this.languageComboBox.SelectionChangeCommitted += new System.EventHandler(this.languageComboBox_SelectionChangeCommitted);
            // 
            // label18
            // 
            this.label18.AutoSize = true;
            this.label18.ForeColor = System.Drawing.SystemColors.GrayText;
            this.label18.Location = new System.Drawing.Point(8, 5);
            this.label18.Name = "label18";
            this.label18.Padding = new System.Windows.Forms.Padding(0, 0, 0, 5);
            this.label18.Size = new System.Drawing.Size(322, 20);
            this.label18.TabIndex = 0;
            this.label18.Text = "Changes take effect the next time you open the application.";
            // 
            // managedEditorPanel
            // 
            this.managedEditorPanel.Controls.Add(this.tableLayoutPanel3);
            this.managedEditorPanel.Location = new System.Drawing.Point(0, 0);
            this.managedEditorPanel.Name = "managedEditorPanel";
            this.managedEditorPanel.Size = new System.Drawing.Size(0, 0);
            this.managedEditorPanel.Text = "managedPanel1";
            // 
            // tableLayoutPanel3
            // 
            this.tableLayoutPanel3.ColumnCount = 1;
            this.tableLayoutPanel3.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle(System.Windows.Forms.SizeType.Percent, 50F));
            this.tableLayoutPanel3.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle(System.Windows.Forms.SizeType.Percent, 50F));
            this.tableLayoutPanel3.Controls.Add(this.groupBox2, 0, 0);
            this.tableLayoutPanel3.Dock = System.Windows.Forms.DockStyle.Fill;
            this.tableLayoutPanel3.Location = new System.Drawing.Point(0, 0);
            this.tableLayoutPanel3.Name = "tableLayoutPanel3";
            this.tableLayoutPanel3.RowCount = 2;
            this.tableLayoutPanel3.RowStyles.Add(new System.Windows.Forms.RowStyle(System.Windows.Forms.SizeType.Percent, 50F));
            this.tableLayoutPanel3.RowStyles.Add(new System.Windows.Forms.RowStyle(System.Windows.Forms.SizeType.Percent, 50F));
            this.tableLayoutPanel3.Size = new System.Drawing.Size(0, 0);
            this.tableLayoutPanel3.TabIndex = 0;
            // 
            // groupBox2
            // 
            this.groupBox2.Controls.Add(this.tableLayoutPanel4);
            this.groupBox2.Dock = System.Windows.Forms.DockStyle.Fill;
            this.groupBox2.Location = new System.Drawing.Point(3, 3);
            this.groupBox2.Name = "groupBox2";
            this.groupBox2.Size = new System.Drawing.Size(1, 1);
            this.groupBox2.TabIndex = 0;
            this.groupBox2.TabStop = false;
            this.groupBox2.Text = "Editor";
            // 
            // tableLayoutPanel4
            // 
            this.tableLayoutPanel4.ColumnCount = 1;
            this.tableLayoutPanel4.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle(System.Windows.Forms.SizeType.Percent, 100F));
            this.tableLayoutPanel4.Controls.Add(this.label14, 0, 0);
            this.tableLayoutPanel4.Controls.Add(this.editorComboBox, 0, 1);
            this.tableLayoutPanel4.Controls.Add(this.alwaysUseDefaultEditorCheckBox, 0, 2);
            this.tableLayoutPanel4.Dock = System.Windows.Forms.DockStyle.Fill;
            this.tableLayoutPanel4.Location = new System.Drawing.Point(3, 19);
            this.tableLayoutPanel4.Name = "tableLayoutPanel4";
            this.tableLayoutPanel4.Padding = new System.Windows.Forms.Padding(5);
            this.tableLayoutPanel4.RowCount = 3;
            this.tableLayoutPanel4.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel4.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel4.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel4.Size = new System.Drawing.Size(0, 0);
            this.tableLayoutPanel4.TabIndex = 0;
            // 
            // label14
            // 
            this.label14.Anchor = System.Windows.Forms.AnchorStyles.Left;
            this.label14.AutoEllipsis = true;
            this.label14.AutoSize = true;
            this.label14.ForeColor = System.Drawing.SystemColors.GrayText;
            this.label14.Location = new System.Drawing.Point(8, 5);
            this.label14.Name = "label14";
            this.label14.Padding = new System.Windows.Forms.Padding(0, 0, 0, 5);
            this.label14.Size = new System.Drawing.Size(1, 5);
            this.label14.TabIndex = 1;
            this.label14.Text = "Select a text editor to open files with by default if no other application instal" +
    "led can be found to edit a given file type.";
            // 
            // editorComboBox
            // 
            this.editorComboBox.Anchor = ((System.Windows.Forms.AnchorStyles)(((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Left) 
            | System.Windows.Forms.AnchorStyles.Right)));
            this.editorComboBox.DrawMode = System.Windows.Forms.DrawMode.OwnerDrawFixed;
            this.editorComboBox.DropDownStyle = System.Windows.Forms.ComboBoxStyle.DropDownList;
            this.editorComboBox.FormattingEnabled = true;
            this.editorComboBox.IconMember = null;
            this.editorComboBox.Location = new System.Drawing.Point(8, 13);
            this.editorComboBox.Name = "editorComboBox";
            this.editorComboBox.Size = new System.Drawing.Size(1, 24);
            this.editorComboBox.TabIndex = 2;
            this.editorComboBox.SelectionChangeCommitted += new System.EventHandler(this.editorComboBox_SelectionChangeCommitted);
            // 
            // alwaysUseDefaultEditorCheckBox
            // 
            this.alwaysUseDefaultEditorCheckBox.AutoSize = true;
            this.alwaysUseDefaultEditorCheckBox.Location = new System.Drawing.Point(8, 43);
            this.alwaysUseDefaultEditorCheckBox.Name = "alwaysUseDefaultEditorCheckBox";
            this.alwaysUseDefaultEditorCheckBox.Size = new System.Drawing.Size(1, 19);
            this.alwaysUseDefaultEditorCheckBox.TabIndex = 3;
            this.alwaysUseDefaultEditorCheckBox.Text = "Always use this application";
            this.alwaysUseDefaultEditorCheckBox.UseVisualStyleBackColor = true;
            this.alwaysUseDefaultEditorCheckBox.CheckedChanged += new System.EventHandler(this.alwaysUseDefaultEditorCheckBox_CheckedChanged);
            // 
            // managedBrowserPanel
            // 
            this.managedBrowserPanel.Controls.Add(this.tableLayoutPanel21);
            this.managedBrowserPanel.Location = new System.Drawing.Point(0, 0);
            this.managedBrowserPanel.Name = "managedBrowserPanel";
            this.managedBrowserPanel.Size = new System.Drawing.Size(760, 501);
            this.managedBrowserPanel.Text = "managedBrowserPanel";
            // 
            // tableLayoutPanel21
            // 
            this.tableLayoutPanel21.AutoSize = true;
            this.tableLayoutPanel21.ColumnCount = 1;
            this.tableLayoutPanel21.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle(System.Windows.Forms.SizeType.Percent, 100F));
            this.tableLayoutPanel21.Controls.Add(this.groupBox18, 0, 0);
            this.tableLayoutPanel21.Controls.Add(this.groupBox14, 0, 1);
            this.tableLayoutPanel21.Dock = System.Windows.Forms.DockStyle.Fill;
            this.tableLayoutPanel21.Location = new System.Drawing.Point(0, 0);
            this.tableLayoutPanel21.Name = "tableLayoutPanel21";
            this.tableLayoutPanel21.RowCount = 3;
            this.tableLayoutPanel21.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel21.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel21.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel21.Size = new System.Drawing.Size(760, 501);
            this.tableLayoutPanel21.TabIndex = 8;
            // 
            // groupBox18
            // 
            this.groupBox18.AutoSize = true;
            this.groupBox18.Controls.Add(this.tableLayoutPanel27);
            this.groupBox18.Dock = System.Windows.Forms.DockStyle.Fill;
            this.groupBox18.Location = new System.Drawing.Point(3, 3);
            this.groupBox18.Name = "groupBox18";
            this.groupBox18.Size = new System.Drawing.Size(754, 107);
            this.groupBox18.TabIndex = 0;
            this.groupBox18.TabStop = false;
            this.groupBox18.Text = "Browser";
            // 
            // tableLayoutPanel27
            // 
            this.tableLayoutPanel27.AutoSize = true;
            this.tableLayoutPanel27.ColumnCount = 2;
            this.tableLayoutPanel27.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle(System.Windows.Forms.SizeType.Absolute, 199F));
            this.tableLayoutPanel27.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle(System.Windows.Forms.SizeType.Absolute, 539F));
            this.tableLayoutPanel27.Controls.Add(this.label13, 0, 2);
            this.tableLayoutPanel27.Controls.Add(this.connectBookmarkCombobox, 1, 1);
            this.tableLayoutPanel27.Controls.Add(this.label19, 0, 1);
            this.tableLayoutPanel27.Controls.Add(this.newBrowserOnStartupCheckbox, 0, 0);
            this.tableLayoutPanel27.Dock = System.Windows.Forms.DockStyle.Fill;
            this.tableLayoutPanel27.Location = new System.Drawing.Point(3, 19);
            this.tableLayoutPanel27.Name = "tableLayoutPanel27";
            this.tableLayoutPanel27.Padding = new System.Windows.Forms.Padding(5);
            this.tableLayoutPanel27.RowCount = 7;
            this.tableLayoutPanel27.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel27.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel27.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel27.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel27.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel27.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel27.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel27.RowStyles.Add(new System.Windows.Forms.RowStyle(System.Windows.Forms.SizeType.Absolute, 20F));
            this.tableLayoutPanel27.RowStyles.Add(new System.Windows.Forms.RowStyle(System.Windows.Forms.SizeType.Absolute, 20F));
            this.tableLayoutPanel27.Size = new System.Drawing.Size(748, 85);
            this.tableLayoutPanel27.TabIndex = 6;
            // 
            // label13
            // 
            this.label13.AutoEllipsis = true;
            this.label13.AutoSize = true;
            this.tableLayoutPanel27.SetColumnSpan(this.label13, 2);
            this.label13.ForeColor = System.Drawing.SystemColors.GrayText;
            this.label13.Location = new System.Drawing.Point(8, 65);
            this.label13.Name = "label13";
            this.label13.Size = new System.Drawing.Size(663, 15);
            this.label13.TabIndex = 3;
            this.label13.Text = "Open an empty browser when opening the application. A connection to the selected " +
    "bookmark is opened for a new browser.";
            // 
            // connectBookmarkCombobox
            // 
            this.connectBookmarkCombobox.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Left | System.Windows.Forms.AnchorStyles.Right)));
            this.connectBookmarkCombobox.DrawMode = System.Windows.Forms.DrawMode.OwnerDrawFixed;
            this.connectBookmarkCombobox.DropDownStyle = System.Windows.Forms.ComboBoxStyle.DropDownList;
            this.connectBookmarkCombobox.FormattingEnabled = true;
            this.connectBookmarkCombobox.IconMember = null;
            this.connectBookmarkCombobox.ItemHeight = 18;
            this.connectBookmarkCombobox.Location = new System.Drawing.Point(207, 38);
            this.connectBookmarkCombobox.Name = "connectBookmarkCombobox";
            this.connectBookmarkCombobox.Size = new System.Drawing.Size(533, 24);
            this.connectBookmarkCombobox.TabIndex = 1;
            this.connectBookmarkCombobox.SelectionChangeCommitted += new System.EventHandler(this.connectBookmarkCombobox_SelectionChangeCommitted);
            // 
            // label19
            // 
            this.label19.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Left | System.Windows.Forms.AnchorStyles.Right)));
            this.label19.AutoEllipsis = true;
            this.label19.AutoSize = true;
            this.label19.Location = new System.Drawing.Point(8, 42);
            this.label19.Name = "label19";
            this.label19.Size = new System.Drawing.Size(193, 15);
            this.label19.TabIndex = 5;
            this.label19.Text = "Connect to bookmark:";
            this.label19.TextAlign = System.Drawing.ContentAlignment.TopRight;
            // 
            // newBrowserOnStartupCheckbox
            // 
            this.newBrowserOnStartupCheckbox.AutoSize = true;
            this.tableLayoutPanel27.SetColumnSpan(this.newBrowserOnStartupCheckbox, 2);
            this.newBrowserOnStartupCheckbox.Enabled = true;
            this.newBrowserOnStartupCheckbox.Location = new System.Drawing.Point(8, 8);
            this.newBrowserOnStartupCheckbox.Name = "newBrowserOnStartupCheckbox";
            this.newBrowserOnStartupCheckbox.Padding = new System.Windows.Forms.Padding(0, 5, 0, 0);
            this.newBrowserOnStartupCheckbox.Size = new System.Drawing.Size(227, 24);
            this.newBrowserOnStartupCheckbox.TabIndex = 0;
            this.newBrowserOnStartupCheckbox.Text = "Open new browser window on startup";
            this.newBrowserOnStartupCheckbox.UseVisualStyleBackColor = true;
            this.newBrowserOnStartupCheckbox.CheckedChanged += new System.EventHandler(this.newBrowserOnStartupCheckbox_CheckedChanged);
            // 
            // groupBox14
            // 
            this.groupBox14.AutoSize = true;
            this.groupBox14.Controls.Add(this.tableLayoutPanel23);
            this.groupBox14.Dock = System.Windows.Forms.DockStyle.Fill;
            this.groupBox14.Location = new System.Drawing.Point(3, 116);
            this.groupBox14.Name = "groupBox14";
            this.groupBox14.Size = new System.Drawing.Size(754, 142);
            this.groupBox14.TabIndex = 0;
            this.groupBox14.TabStop = false;
            this.groupBox14.Text = "General";
            // 
            // tableLayoutPanel23
            // 
            this.tableLayoutPanel23.AutoSize = true;
            this.tableLayoutPanel23.ColumnCount = 2;
            this.tableLayoutPanel23.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle(System.Windows.Forms.SizeType.Absolute, 199F));
            this.tableLayoutPanel23.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle(System.Windows.Forms.SizeType.Absolute, 539F));
            this.tableLayoutPanel23.Controls.Add(this.infoWindowCheckbox, 0, 3);
            this.tableLayoutPanel23.Controls.Add(this.showHiddenFilesCheckbox, 0, 0);
            this.tableLayoutPanel23.Controls.Add(this.returnKeyCheckbox, 0, 2);
            this.tableLayoutPanel23.Controls.Add(this.doubleClickEditorCheckbox, 0, 1);
            this.tableLayoutPanel23.Dock = System.Windows.Forms.DockStyle.Fill;
            this.tableLayoutPanel23.Location = new System.Drawing.Point(3, 19);
            this.tableLayoutPanel23.Name = "tableLayoutPanel23";
            this.tableLayoutPanel23.Padding = new System.Windows.Forms.Padding(5);
            this.tableLayoutPanel23.RowCount = 4;
            this.tableLayoutPanel23.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel23.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel23.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel23.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel23.RowStyles.Add(new System.Windows.Forms.RowStyle(System.Windows.Forms.SizeType.Absolute, 20F));
            this.tableLayoutPanel23.Size = new System.Drawing.Size(748, 120);
            this.tableLayoutPanel23.TabIndex = 6;
            // 
            // infoWindowCheckbox
            // 
            this.infoWindowCheckbox.AutoSize = true;
            this.tableLayoutPanel23.SetColumnSpan(this.infoWindowCheckbox, 2);
            this.infoWindowCheckbox.Location = new System.Drawing.Point(8, 93);
            this.infoWindowCheckbox.Name = "infoWindowCheckbox";
            this.infoWindowCheckbox.Size = new System.Drawing.Size(257, 19);
            this.infoWindowCheckbox.TabIndex = 3;
            this.infoWindowCheckbox.Text = "Info window always shows current selection";
            this.infoWindowCheckbox.UseVisualStyleBackColor = true;
            this.infoWindowCheckbox.CheckedChanged += new System.EventHandler(this.infoWindowCheckbox_CheckedChanged);
            // 
            // showHiddenFilesCheckbox
            // 
            this.showHiddenFilesCheckbox.AutoSize = true;
            this.tableLayoutPanel23.SetColumnSpan(this.showHiddenFilesCheckbox, 2);
            this.showHiddenFilesCheckbox.Location = new System.Drawing.Point(8, 8);
            this.showHiddenFilesCheckbox.Name = "showHiddenFilesCheckbox";
            this.showHiddenFilesCheckbox.Padding = new System.Windows.Forms.Padding(0, 10, 0, 0);
            this.showHiddenFilesCheckbox.Size = new System.Drawing.Size(119, 29);
            this.showHiddenFilesCheckbox.TabIndex = 0;
            this.showHiddenFilesCheckbox.Text = "Show hidden files";
            this.showHiddenFilesCheckbox.UseVisualStyleBackColor = true;
            this.showHiddenFilesCheckbox.CheckedChanged += new System.EventHandler(this.showHiddenFilesCheckbox_CheckedChanged);
            // 
            // returnKeyCheckbox
            // 
            this.returnKeyCheckbox.AutoSize = true;
            this.tableLayoutPanel23.SetColumnSpan(this.returnKeyCheckbox, 2);
            this.returnKeyCheckbox.Location = new System.Drawing.Point(8, 68);
            this.returnKeyCheckbox.Name = "returnKeyCheckbox";
            this.returnKeyCheckbox.Size = new System.Drawing.Size(244, 19);
            this.returnKeyCheckbox.TabIndex = 2;
            this.returnKeyCheckbox.Text = "Return key selects folder or file to rename";
            this.returnKeyCheckbox.UseVisualStyleBackColor = true;
            this.returnKeyCheckbox.CheckedChanged += new System.EventHandler(this.returnKeyCheckbox_CheckedChanged);
            // 
            // doubleClickEditorCheckbox
            // 
            this.doubleClickEditorCheckbox.AutoSize = true;
            this.tableLayoutPanel23.SetColumnSpan(this.doubleClickEditorCheckbox, 2);
            this.doubleClickEditorCheckbox.Location = new System.Drawing.Point(8, 43);
            this.doubleClickEditorCheckbox.Name = "doubleClickEditorCheckbox";
            this.doubleClickEditorCheckbox.Size = new System.Drawing.Size(237, 19);
            this.doubleClickEditorCheckbox.TabIndex = 1;
            this.doubleClickEditorCheckbox.Text = "Double click opens file in external editor";
            this.doubleClickEditorCheckbox.UseVisualStyleBackColor = true;
            this.doubleClickEditorCheckbox.CheckedChanged += new System.EventHandler(this.doubleClickEditorCheckbox_CheckedChanged);
            // 
            // managedTransfersPanel
            // 
            this.managedTransfersPanel.Controls.Add(this.transfersTabControl);
            this.managedTransfersPanel.Location = new System.Drawing.Point(0, 0);
            this.managedTransfersPanel.Name = "managedTransfersPanel";
            this.managedTransfersPanel.Size = new System.Drawing.Size(0, 0);
            this.managedTransfersPanel.Text = "managedPanel1";
            // 
            // transfersTabControl
            // 
            this.transfersTabControl.Appearance = System.Windows.Forms.TabAppearance.FlatButtons;
            this.transfersTabControl.Controls.Add(this.tabPage1);
            this.transfersTabControl.Controls.Add(this.tabPage2);
            this.transfersTabControl.Controls.Add(this.tabPage3);
            this.transfersTabControl.Controls.Add(this.tabPage5);
            this.transfersTabControl.Controls.Add(this.tabPage4);
            this.transfersTabControl.Dock = System.Windows.Forms.DockStyle.Fill;
            this.transfersTabControl.Location = new System.Drawing.Point(0, 0);
            this.transfersTabControl.Name = "transfersTabControl";
            this.transfersTabControl.SelectedIndex = 0;
            this.transfersTabControl.Size = new System.Drawing.Size(0, 0);
            this.transfersTabControl.TabIndex = 0;
            // 
            // tabPage1
            // 
            this.tabPage1.Controls.Add(this.tableLayoutPanel5);
            this.tabPage1.Location = new System.Drawing.Point(4, 27);
            this.tabPage1.Name = "tabPage1";
            this.tabPage1.Size = new System.Drawing.Size(0, 0);
            this.tabPage1.TabIndex = 0;
            this.tabPage1.Text = "General";
            this.tabPage1.UseVisualStyleBackColor = true;
            // 
            // tableLayoutPanel5
            // 
            this.tableLayoutPanel5.AutoSize = true;
            this.tableLayoutPanel5.ColumnCount = 1;
            this.tableLayoutPanel5.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle(System.Windows.Forms.SizeType.Percent, 100F));
            this.tableLayoutPanel5.Controls.Add(this.groupBox6, 0, 2);
            this.tableLayoutPanel5.Controls.Add(this.groupBox5, 0, 1);
            this.tableLayoutPanel5.Controls.Add(this.groupBox29, 0, 0);
            this.tableLayoutPanel5.Dock = System.Windows.Forms.DockStyle.Fill;
            this.tableLayoutPanel5.Location = new System.Drawing.Point(0, 0);
            this.tableLayoutPanel5.Name = "tableLayoutPanel5";
            this.tableLayoutPanel5.RowCount = 4;
            this.tableLayoutPanel5.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel5.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel5.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel5.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel5.Size = new System.Drawing.Size(0, 0);
            this.tableLayoutPanel5.TabIndex = 7;
            // 
            // groupBox6
            // 
            this.groupBox6.AutoSize = true;
            this.groupBox6.Controls.Add(this.tableLayoutPanel8);
            this.groupBox6.Dock = System.Windows.Forms.DockStyle.Fill;
            this.groupBox6.Location = new System.Drawing.Point(3, 318);
            this.groupBox6.Name = "groupBox6";
            this.groupBox6.Size = new System.Drawing.Size(1, 115);
            this.groupBox6.TabIndex = 6;
            this.groupBox6.TabStop = false;
            this.groupBox6.Text = "Uploads";
            // 
            // tableLayoutPanel8
            // 
            this.tableLayoutPanel8.AutoSize = true;
            this.tableLayoutPanel8.ColumnCount = 2;
            this.tableLayoutPanel8.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle(System.Windows.Forms.SizeType.Absolute, 150F));
            this.tableLayoutPanel8.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle(System.Windows.Forms.SizeType.Absolute, 580F));
            this.tableLayoutPanel8.Controls.Add(this.label12, 0, 1);
            this.tableLayoutPanel8.Controls.Add(this.duplicateUploadOverwriteCheckbox, 1, 2);
            this.tableLayoutPanel8.Controls.Add(this.duplicateUploadCombobox, 1, 1);
            this.tableLayoutPanel8.Controls.Add(this.uploadTemporaryNameCheckBox, 0, 0);
            this.tableLayoutPanel8.Dock = System.Windows.Forms.DockStyle.Fill;
            this.tableLayoutPanel8.Location = new System.Drawing.Point(3, 19);
            this.tableLayoutPanel8.Name = "tableLayoutPanel8";
            this.tableLayoutPanel8.Padding = new System.Windows.Forms.Padding(5);
            this.tableLayoutPanel8.RowCount = 3;
            this.tableLayoutPanel8.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel8.RowStyles.Add(new System.Windows.Forms.RowStyle(System.Windows.Forms.SizeType.Percent, 50F));
            this.tableLayoutPanel8.RowStyles.Add(new System.Windows.Forms.RowStyle(System.Windows.Forms.SizeType.Percent, 50F));
            this.tableLayoutPanel8.Size = new System.Drawing.Size(0, 93);
            this.tableLayoutPanel8.TabIndex = 8;
            // 
            // label12
            // 
            this.label12.Anchor = System.Windows.Forms.AnchorStyles.Right;
            this.label12.AutoSize = true;
            this.label12.Location = new System.Drawing.Point(75, 37);
            this.label12.Name = "label12";
            this.label12.Size = new System.Drawing.Size(77, 15);
            this.label12.TabIndex = 5;
            this.label12.Text = "Existing Files:";
            this.label12.TextAlign = System.Drawing.ContentAlignment.TopRight;
            // 
            // duplicateUploadOverwriteCheckbox
            // 
            this.duplicateUploadOverwriteCheckbox.AutoSize = true;
            this.duplicateUploadOverwriteCheckbox.Location = new System.Drawing.Point(158, 62);
            this.duplicateUploadOverwriteCheckbox.Name = "duplicateUploadOverwriteCheckbox";
            this.duplicateUploadOverwriteCheckbox.Size = new System.Drawing.Size(200, 19);
            this.duplicateUploadOverwriteCheckbox.TabIndex = 7;
            this.duplicateUploadOverwriteCheckbox.Text = "Always overwrite when reloading";
            this.duplicateUploadOverwriteCheckbox.UseVisualStyleBackColor = true;
            this.duplicateUploadOverwriteCheckbox.CheckedChanged += new System.EventHandler(this.duplicateUploadOverwriteCheckbox_CheckedChanged);
            // 
            // duplicateUploadCombobox
            // 
            this.duplicateUploadCombobox.DropDownStyle = System.Windows.Forms.ComboBoxStyle.DropDownList;
            this.duplicateUploadCombobox.FormattingEnabled = true;
            this.duplicateUploadCombobox.Location = new System.Drawing.Point(158, 33);
            this.duplicateUploadCombobox.Name = "duplicateUploadCombobox";
            this.duplicateUploadCombobox.Size = new System.Drawing.Size(286, 23);
            this.duplicateUploadCombobox.TabIndex = 4;
            this.duplicateUploadCombobox.SelectionChangeCommitted += new System.EventHandler(this.duplicateUploadCombobox_SelectionChangeCommitted);
            // 
            // uploadTemporaryNameCheckBox
            // 
            this.uploadTemporaryNameCheckBox.AutoSize = true;
            this.tableLayoutPanel8.SetColumnSpan(this.uploadTemporaryNameCheckBox, 2);
            this.uploadTemporaryNameCheckBox.Location = new System.Drawing.Point(8, 8);
            this.uploadTemporaryNameCheckBox.Name = "uploadTemporaryNameCheckBox";
            this.uploadTemporaryNameCheckBox.Size = new System.Drawing.Size(197, 19);
            this.uploadTemporaryNameCheckBox.TabIndex = 8;
            this.uploadTemporaryNameCheckBox.Text = "Upload with temporary filename";
            this.uploadTemporaryNameCheckBox.UseVisualStyleBackColor = true;
            this.uploadTemporaryNameCheckBox.CheckedChanged += new System.EventHandler(this.uploadTemporaryNameCheckBox_CheckedChanged);
            // 
            // groupBox5
            // 
            this.groupBox5.AutoSize = true;
            this.groupBox5.Controls.Add(this.tableLayoutPanel7);
            this.groupBox5.Dock = System.Windows.Forms.DockStyle.Fill;
            this.groupBox5.Location = new System.Drawing.Point(3, 145);
            this.groupBox5.Name = "groupBox5";
            this.groupBox5.Size = new System.Drawing.Size(1, 167);
            this.groupBox5.TabIndex = 5;
            this.groupBox5.TabStop = false;
            this.groupBox5.Text = "Downloads";
            // 
            // tableLayoutPanel7
            // 
            this.tableLayoutPanel7.AutoSize = true;
            this.tableLayoutPanel7.ColumnCount = 3;
            this.tableLayoutPanel7.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle(System.Windows.Forms.SizeType.Absolute, 150F));
            this.tableLayoutPanel7.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle(System.Windows.Forms.SizeType.Percent, 100F));
            this.tableLayoutPanel7.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle());
            this.tableLayoutPanel7.Controls.Add(this.openAfterDownloadCheckbox, 0, 1);
            this.tableLayoutPanel7.Controls.Add(this.duplicateDownloadCombobox, 1, 3);
            this.tableLayoutPanel7.Controls.Add(this.label11, 0, 3);
            this.tableLayoutPanel7.Controls.Add(this.downloadFolderLabel, 1, 2);
            this.tableLayoutPanel7.Controls.Add(this.label10, 0, 2);
            this.tableLayoutPanel7.Controls.Add(this.duplicateDownloadOverwriteCheckbox, 1, 4);
            this.tableLayoutPanel7.Controls.Add(this.showDownloadFolderDialogButton, 2, 2);
            this.tableLayoutPanel7.Controls.Add(this.segmentedDownloadsCheckbox, 0, 0);
            this.tableLayoutPanel7.Dock = System.Windows.Forms.DockStyle.Fill;
            this.tableLayoutPanel7.Location = new System.Drawing.Point(3, 19);
            this.tableLayoutPanel7.Name = "tableLayoutPanel7";
            this.tableLayoutPanel7.Padding = new System.Windows.Forms.Padding(5);
            this.tableLayoutPanel7.RowCount = 5;
            this.tableLayoutPanel7.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel7.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel7.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel7.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel7.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel7.Size = new System.Drawing.Size(0, 145);
            this.tableLayoutPanel7.TabIndex = 10;
            // 
            // openAfterDownloadCheckbox
            // 
            this.openAfterDownloadCheckbox.AutoSize = true;
            this.tableLayoutPanel7.SetColumnSpan(this.openAfterDownloadCheckbox, 3);
            this.openAfterDownloadCheckbox.Location = new System.Drawing.Point(8, 33);
            this.openAfterDownloadCheckbox.Name = "openAfterDownloadCheckbox";
            this.openAfterDownloadCheckbox.Size = new System.Drawing.Size(1, 19);
            this.openAfterDownloadCheckbox.TabIndex = 0;
            this.openAfterDownloadCheckbox.Text = "Open downloaded files with default application";
            this.openAfterDownloadCheckbox.UseVisualStyleBackColor = true;
            this.openAfterDownloadCheckbox.CheckedChanged += new System.EventHandler(this.openAfterDownloadCheckbox_CheckedChanged);
            // 
            // duplicateDownloadCombobox
            // 
            this.duplicateDownloadCombobox.DropDownStyle = System.Windows.Forms.ComboBoxStyle.DropDownList;
            this.duplicateDownloadCombobox.FormattingEnabled = true;
            this.duplicateDownloadCombobox.Location = new System.Drawing.Point(158, 89);
            this.duplicateDownloadCombobox.Name = "duplicateDownloadCombobox";
            this.duplicateDownloadCombobox.Size = new System.Drawing.Size(286, 23);
            this.duplicateDownloadCombobox.TabIndex = 4;
            this.duplicateDownloadCombobox.SelectionChangeCommitted += new System.EventHandler(this.duplicateDownloadCombobox_SelectionChangeCommitted);
            // 
            // label11
            // 
            this.label11.Anchor = System.Windows.Forms.AnchorStyles.Right;
            this.label11.AutoSize = true;
            this.label11.Location = new System.Drawing.Point(75, 93);
            this.label11.Name = "label11";
            this.label11.Size = new System.Drawing.Size(77, 15);
            this.label11.TabIndex = 5;
            this.label11.Text = "Existing Files:";
            this.label11.TextAlign = System.Drawing.ContentAlignment.TopRight;
            // 
            // downloadFolderLabel
            // 
            this.downloadFolderLabel.Anchor = System.Windows.Forms.AnchorStyles.Left;
            this.downloadFolderLabel.AutoSize = true;
            this.downloadFolderLabel.Location = new System.Drawing.Point(158, 63);
            this.downloadFolderLabel.Name = "downloadFolderLabel";
            this.downloadFolderLabel.Size = new System.Drawing.Size(1, 15);
            this.downloadFolderLabel.TabIndex = 9;
            this.downloadFolderLabel.Text = "ellipsisLabel1";
            // 
            // label10
            // 
            this.label10.Anchor = System.Windows.Forms.AnchorStyles.Right;
            this.label10.AutoSize = true;
            this.label10.Location = new System.Drawing.Point(52, 63);
            this.label10.Name = "label10";
            this.label10.Size = new System.Drawing.Size(100, 15);
            this.label10.TabIndex = 3;
            this.label10.Text = "Download Folder:";
            this.label10.TextAlign = System.Drawing.ContentAlignment.TopRight;
            // 
            // duplicateDownloadOverwriteCheckbox
            // 
            this.duplicateDownloadOverwriteCheckbox.AutoSize = true;
            this.duplicateDownloadOverwriteCheckbox.Location = new System.Drawing.Point(158, 118);
            this.duplicateDownloadOverwriteCheckbox.Name = "duplicateDownloadOverwriteCheckbox";
            this.duplicateDownloadOverwriteCheckbox.Size = new System.Drawing.Size(1, 19);
            this.duplicateDownloadOverwriteCheckbox.TabIndex = 6;
            this.duplicateDownloadOverwriteCheckbox.Text = "Always overwrite when reloading";
            this.duplicateDownloadOverwriteCheckbox.UseVisualStyleBackColor = true;
            this.duplicateDownloadOverwriteCheckbox.CheckedChanged += new System.EventHandler(this.duplicateDownloadOverwriteCheckbox_CheckedChanged);
            // 
            // showDownloadFolderDialogButton
            // 
            this.showDownloadFolderDialogButton.Anchor = System.Windows.Forms.AnchorStyles.Right;
            this.showDownloadFolderDialogButton.AutoSize = true;
            this.showDownloadFolderDialogButton.AutoSizeMode = System.Windows.Forms.AutoSizeMode.GrowAndShrink;
            this.showDownloadFolderDialogButton.Location = new System.Drawing.Point(-63, 58);
            this.showDownloadFolderDialogButton.Name = "showDownloadFolderDialogButton";
            this.showDownloadFolderDialogButton.Size = new System.Drawing.Size(66, 25);
            this.showDownloadFolderDialogButton.TabIndex = 8;
            this.showDownloadFolderDialogButton.Text = "Choose…";
            this.showDownloadFolderDialogButton.UseVisualStyleBackColor = true;
            this.showDownloadFolderDialogButton.Click += new System.EventHandler(this.showDownloadFolderDialogButton_Click);
            // 
            // segmentedDownloadsCheckbox
            // 
            this.segmentedDownloadsCheckbox.AutoSize = true;
            this.tableLayoutPanel7.SetColumnSpan(this.segmentedDownloadsCheckbox, 3);
            this.segmentedDownloadsCheckbox.Location = new System.Drawing.Point(8, 8);
            this.segmentedDownloadsCheckbox.Name = "segmentedDownloadsCheckbox";
            this.segmentedDownloadsCheckbox.Size = new System.Drawing.Size(1, 19);
            this.segmentedDownloadsCheckbox.TabIndex = 4;
            this.segmentedDownloadsCheckbox.Text = "Segmented downloads with multiple connections per file";
            this.segmentedDownloadsCheckbox.UseVisualStyleBackColor = true;
            this.segmentedDownloadsCheckbox.CheckedChanged += new System.EventHandler(this.segmentedDownloadsCheckbox_CheckedChanged);
            // 
            // groupBox29
            // 
            this.groupBox29.AutoSize = true;
            this.groupBox29.Controls.Add(this.tableLayoutPanel6);
            this.groupBox29.Dock = System.Windows.Forms.DockStyle.Fill;
            this.groupBox29.Location = new System.Drawing.Point(3, 3);
            this.groupBox29.Name = "groupBox29";
            this.groupBox29.Size = new System.Drawing.Size(1, 136);
            this.groupBox29.TabIndex = 7;
            this.groupBox29.TabStop = false;
            this.groupBox29.Text = "Transfers";
            // 
            // tableLayoutPanel6
            // 
            this.tableLayoutPanel6.AutoSize = true;
            this.tableLayoutPanel6.ColumnCount = 2;
            this.tableLayoutPanel6.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle(System.Windows.Forms.SizeType.Absolute, 150F));
            this.tableLayoutPanel6.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle(System.Windows.Forms.SizeType.Absolute, 580F));
            this.tableLayoutPanel6.Controls.Add(this.transfersToFrontCheckbox, 0, 1);
            this.tableLayoutPanel6.Controls.Add(this.removeFromTransfersCheckbox, 0, 3);
            this.tableLayoutPanel6.Controls.Add(this.transferFilesCombobox, 1, 0);
            this.tableLayoutPanel6.Controls.Add(this.transfersToBackCheckbox, 0, 2);
            this.tableLayoutPanel6.Controls.Add(this.label1, 0, 0);
            this.tableLayoutPanel6.Dock = System.Windows.Forms.DockStyle.Fill;
            this.tableLayoutPanel6.Location = new System.Drawing.Point(3, 19);
            this.tableLayoutPanel6.Name = "tableLayoutPanel6";
            this.tableLayoutPanel6.Padding = new System.Windows.Forms.Padding(5);
            this.tableLayoutPanel6.RowCount = 4;
            this.tableLayoutPanel6.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel6.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel6.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel6.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel6.Size = new System.Drawing.Size(0, 114);
            this.tableLayoutPanel6.TabIndex = 0;
            // 
            // transfersToFrontCheckbox
            // 
            this.transfersToFrontCheckbox.AutoSize = true;
            this.tableLayoutPanel6.SetColumnSpan(this.transfersToFrontCheckbox, 2);
            this.transfersToFrontCheckbox.Location = new System.Drawing.Point(8, 37);
            this.transfersToFrontCheckbox.Name = "transfersToFrontCheckbox";
            this.transfersToFrontCheckbox.Size = new System.Drawing.Size(248, 19);
            this.transfersToFrontCheckbox.TabIndex = 2;
            this.transfersToFrontCheckbox.Text = "Bring window to front when transfer starts";
            this.transfersToFrontCheckbox.UseVisualStyleBackColor = true;
            this.transfersToFrontCheckbox.CheckedChanged += new System.EventHandler(this.transfersToFrontCheckbox_CheckedChanged);
            // 
            // removeFromTransfersCheckbox
            // 
            this.removeFromTransfersCheckbox.AutoSize = true;
            this.tableLayoutPanel6.SetColumnSpan(this.removeFromTransfersCheckbox, 2);
            this.removeFromTransfersCheckbox.Location = new System.Drawing.Point(8, 87);
            this.removeFromTransfersCheckbox.Name = "removeFromTransfersCheckbox";
            this.removeFromTransfersCheckbox.Size = new System.Drawing.Size(202, 19);
            this.removeFromTransfersCheckbox.TabIndex = 4;
            this.removeFromTransfersCheckbox.Text = "Remove when transfer completes";
            this.removeFromTransfersCheckbox.UseVisualStyleBackColor = true;
            this.removeFromTransfersCheckbox.CheckedChanged += new System.EventHandler(this.removeFromTransfersCheckbox_CheckedChanged);
            // 
            // transferFilesCombobox
            // 
            this.transferFilesCombobox.DropDownStyle = System.Windows.Forms.ComboBoxStyle.DropDownList;
            this.transferFilesCombobox.FormattingEnabled = true;
            this.transferFilesCombobox.Location = new System.Drawing.Point(158, 8);
            this.transferFilesCombobox.Name = "transferFilesCombobox";
            this.transferFilesCombobox.Size = new System.Drawing.Size(286, 23);
            this.transferFilesCombobox.TabIndex = 0;
            this.transferFilesCombobox.SelectionChangeCommitted += new System.EventHandler(this.transferFilesCombobox_SelectionChangeCommitted);
            // 
            // transfersToBackCheckbox
            // 
            this.transfersToBackCheckbox.AutoSize = true;
            this.tableLayoutPanel6.SetColumnSpan(this.transfersToBackCheckbox, 2);
            this.transfersToBackCheckbox.Location = new System.Drawing.Point(8, 62);
            this.transfersToBackCheckbox.Name = "transfersToBackCheckbox";
            this.transfersToBackCheckbox.Size = new System.Drawing.Size(233, 19);
            this.transfersToBackCheckbox.TabIndex = 3;
            this.transfersToBackCheckbox.Text = "Close window when transfer completes";
            this.transfersToBackCheckbox.UseVisualStyleBackColor = true;
            this.transfersToBackCheckbox.CheckedChanged += new System.EventHandler(this.transfersToBackCheckbox_CheckedChanged);
            // 
            // label1
            // 
            this.label1.Anchor = System.Windows.Forms.AnchorStyles.Right;
            this.label1.AutoSize = true;
            this.label1.Location = new System.Drawing.Point(75, 12);
            this.label1.Name = "label1";
            this.label1.Size = new System.Drawing.Size(77, 15);
            this.label1.TabIndex = 5;
            this.label1.Text = "Transfer Files:";
            // 
            // tabPage2
            // 
            this.tabPage2.Controls.Add(this.tableLayoutPanel9);
            this.tabPage2.Location = new System.Drawing.Point(4, 27);
            this.tabPage2.Name = "tabPage2";
            this.tabPage2.Size = new System.Drawing.Size(0, 0);
            this.tabPage2.TabIndex = 1;
            this.tabPage2.Text = "Permissions";
            this.tabPage2.UseVisualStyleBackColor = true;
            // 
            // tableLayoutPanel9
            // 
            this.tableLayoutPanel9.AutoSize = true;
            this.tableLayoutPanel9.ColumnCount = 1;
            this.tableLayoutPanel9.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle(System.Windows.Forms.SizeType.Percent, 100F));
            this.tableLayoutPanel9.Controls.Add(this.groupBox7, 0, 0);
            this.tableLayoutPanel9.Controls.Add(this.groupBox8, 0, 1);
            this.tableLayoutPanel9.Dock = System.Windows.Forms.DockStyle.Fill;
            this.tableLayoutPanel9.Location = new System.Drawing.Point(0, 0);
            this.tableLayoutPanel9.Name = "tableLayoutPanel9";
            this.tableLayoutPanel9.RowCount = 3;
            this.tableLayoutPanel9.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel9.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel9.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel9.Size = new System.Drawing.Size(0, 0);
            this.tableLayoutPanel9.TabIndex = 2;
            // 
            // groupBox7
            // 
            this.groupBox7.AutoSize = true;
            this.groupBox7.Controls.Add(this.tableLayoutPanel10);
            this.groupBox7.Dock = System.Windows.Forms.DockStyle.Fill;
            this.groupBox7.Enabled = false;
            this.groupBox7.Location = new System.Drawing.Point(3, 3);
            this.groupBox7.Name = "groupBox7";
            this.groupBox7.Size = new System.Drawing.Size(1, 182);
            this.groupBox7.TabIndex = 0;
            this.groupBox7.TabStop = false;
            this.groupBox7.Text = "Downloads";
            this.groupBox7.Visible = false;
            // 
            // tableLayoutPanel10
            // 
            this.tableLayoutPanel10.AutoSize = true;
            this.tableLayoutPanel10.ColumnCount = 2;
            this.tableLayoutPanel10.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle());
            this.tableLayoutPanel10.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle(System.Windows.Forms.SizeType.Percent, 100F));
            this.tableLayoutPanel10.Controls.Add(this.chmodDownloadCheckbox, 0, 0);
            this.tableLayoutPanel10.Controls.Add(this.chmodDownloadCustomRadioButton, 0, 1);
            this.tableLayoutPanel10.Controls.Add(this.chmodDownloadDefaultRadioButton, 0, 2);
            this.tableLayoutPanel10.Controls.Add(this.chmodDownloadTypeCombobox, 1, 2);
            this.tableLayoutPanel10.Controls.Add(this.tableLayoutPanel11, 0, 3);
            this.tableLayoutPanel10.Dock = System.Windows.Forms.DockStyle.Fill;
            this.tableLayoutPanel10.Location = new System.Drawing.Point(3, 19);
            this.tableLayoutPanel10.Name = "tableLayoutPanel10";
            this.tableLayoutPanel10.Padding = new System.Windows.Forms.Padding(5);
            this.tableLayoutPanel10.RowCount = 4;
            this.tableLayoutPanel10.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel10.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel10.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel10.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel10.Size = new System.Drawing.Size(0, 160);
            this.tableLayoutPanel10.TabIndex = 16;
            this.tableLayoutPanel10.Visible = false;
            // 
            // chmodDownloadCheckbox
            // 
            this.chmodDownloadCheckbox.AutoSize = true;
            this.tableLayoutPanel10.SetColumnSpan(this.chmodDownloadCheckbox, 2);
            this.chmodDownloadCheckbox.Location = new System.Drawing.Point(8, 8);
            this.chmodDownloadCheckbox.Name = "chmodDownloadCheckbox";
            this.chmodDownloadCheckbox.Size = new System.Drawing.Size(1, 19);
            this.chmodDownloadCheckbox.TabIndex = 0;
            this.chmodDownloadCheckbox.Text = "Change permissions";
            this.chmodDownloadCheckbox.UseVisualStyleBackColor = true;
            this.chmodDownloadCheckbox.CheckedChanged += new System.EventHandler(this.chmodDownloadCheckbox_CheckedChanged);
            // 
            // chmodDownloadCustomRadioButton
            // 
            this.chmodDownloadCustomRadioButton.AutoSize = true;
            this.tableLayoutPanel10.SetColumnSpan(this.chmodDownloadCustomRadioButton, 2);
            this.chmodDownloadCustomRadioButton.Font = new System.Drawing.Font("Microsoft Sans Serif", 7F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.chmodDownloadCustomRadioButton.Location = new System.Drawing.Point(8, 33);
            this.chmodDownloadCustomRadioButton.Name = "chmodDownloadCustomRadioButton";
            this.chmodDownloadCustomRadioButton.Padding = new System.Windows.Forms.Padding(20, 0, 0, 0);
            this.chmodDownloadCustomRadioButton.Size = new System.Drawing.Size(1, 17);
            this.chmodDownloadCustomRadioButton.TabIndex = 1;
            this.chmodDownloadCustomRadioButton.TabStop = true;
            this.chmodDownloadCustomRadioButton.Text = "to the permissions of the remote file or folder";
            this.chmodDownloadCustomRadioButton.UseVisualStyleBackColor = true;
            // 
            // chmodDownloadDefaultRadioButton
            // 
            this.chmodDownloadDefaultRadioButton.AutoSize = true;
            this.chmodDownloadDefaultRadioButton.Font = new System.Drawing.Font("Microsoft Sans Serif", 7F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.chmodDownloadDefaultRadioButton.Location = new System.Drawing.Point(8, 56);
            this.chmodDownloadDefaultRadioButton.Name = "chmodDownloadDefaultRadioButton";
            this.chmodDownloadDefaultRadioButton.Padding = new System.Windows.Forms.Padding(20, 0, 0, 0);
            this.chmodDownloadDefaultRadioButton.Size = new System.Drawing.Size(143, 17);
            this.chmodDownloadDefaultRadioButton.TabIndex = 2;
            this.chmodDownloadDefaultRadioButton.TabStop = true;
            this.chmodDownloadDefaultRadioButton.Text = "to these permissions:";
            this.chmodDownloadDefaultRadioButton.UseVisualStyleBackColor = true;
            this.chmodDownloadDefaultRadioButton.CheckedChanged += new System.EventHandler(this.chmodDownloadDefaultRadioButton_CheckedChanged);
            // 
            // chmodDownloadTypeCombobox
            // 
            this.chmodDownloadTypeCombobox.DropDownStyle = System.Windows.Forms.ComboBoxStyle.DropDownList;
            this.chmodDownloadTypeCombobox.Font = new System.Drawing.Font("Microsoft Sans Serif", 7F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.chmodDownloadTypeCombobox.FormattingEnabled = true;
            this.chmodDownloadTypeCombobox.Location = new System.Drawing.Point(157, 56);
            this.chmodDownloadTypeCombobox.MinimumSize = new System.Drawing.Size(150, 0);
            this.chmodDownloadTypeCombobox.Name = "chmodDownloadTypeCombobox";
            this.chmodDownloadTypeCombobox.Size = new System.Drawing.Size(150, 20);
            this.chmodDownloadTypeCombobox.TabIndex = 3;
            this.chmodDownloadTypeCombobox.SelectionChangeCommitted += new System.EventHandler(this.chmodDownloadTypeCombobox_SelectionChangeCommitted);
            // 
            // tableLayoutPanel11
            // 
            this.tableLayoutPanel11.AutoSize = true;
            this.tableLayoutPanel11.ColumnCount = 6;
            this.tableLayoutPanel10.SetColumnSpan(this.tableLayoutPanel11, 2);
            this.tableLayoutPanel11.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle(System.Windows.Forms.SizeType.Absolute, 50F));
            this.tableLayoutPanel11.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle());
            this.tableLayoutPanel11.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle());
            this.tableLayoutPanel11.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle());
            this.tableLayoutPanel11.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle());
            this.tableLayoutPanel11.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle(System.Windows.Forms.SizeType.Percent, 100F));
            this.tableLayoutPanel11.Controls.Add(this.ownerDownloadLabel, 1, 0);
            this.tableLayoutPanel11.Controls.Add(this.dotherxCheckbox, 4, 2);
            this.tableLayoutPanel11.Controls.Add(this.groupDownloadLabel, 1, 1);
            this.tableLayoutPanel11.Controls.Add(this.dotherwCheckbox, 3, 2);
            this.tableLayoutPanel11.Controls.Add(this.othersDownloadLabel, 1, 2);
            this.tableLayoutPanel11.Controls.Add(this.dgroupxCheckbox, 4, 1);
            this.tableLayoutPanel11.Controls.Add(this.dotherrCheckbox, 2, 2);
            this.tableLayoutPanel11.Controls.Add(this.dgroupwCheckbox, 3, 1);
            this.tableLayoutPanel11.Controls.Add(this.downerrCheckbox, 2, 0);
            this.tableLayoutPanel11.Controls.Add(this.downerwCheckbox, 3, 0);
            this.tableLayoutPanel11.Controls.Add(this.downerxCheckbox, 4, 0);
            this.tableLayoutPanel11.Controls.Add(this.dgrouprCheckbox, 2, 1);
            this.tableLayoutPanel11.Dock = System.Windows.Forms.DockStyle.Fill;
            this.tableLayoutPanel11.Location = new System.Drawing.Point(8, 83);
            this.tableLayoutPanel11.Name = "tableLayoutPanel11";
            this.tableLayoutPanel11.RowCount = 3;
            this.tableLayoutPanel11.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel11.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel11.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel11.Size = new System.Drawing.Size(1, 69);
            this.tableLayoutPanel11.TabIndex = 4;
            // 
            // ownerDownloadLabel
            // 
            this.ownerDownloadLabel.Anchor = System.Windows.Forms.AnchorStyles.Right;
            this.ownerDownloadLabel.AutoSize = true;
            this.ownerDownloadLabel.Font = new System.Drawing.Font("Microsoft Sans Serif", 7F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.ownerDownloadLabel.Location = new System.Drawing.Point(54, 5);
            this.ownerDownloadLabel.Name = "ownerDownloadLabel";
            this.ownerDownloadLabel.Size = new System.Drawing.Size(43, 13);
            this.ownerDownloadLabel.TabIndex = 4;
            this.ownerDownloadLabel.Text = "Owner";
            this.ownerDownloadLabel.TextAlign = System.Drawing.ContentAlignment.TopRight;
            // 
            // dotherxCheckbox
            // 
            this.dotherxCheckbox.AutoSize = true;
            this.dotherxCheckbox.Font = new System.Drawing.Font("Microsoft Sans Serif", 7F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.dotherxCheckbox.Location = new System.Drawing.Point(215, 49);
            this.dotherxCheckbox.Name = "dotherxCheckbox";
            this.dotherxCheckbox.Size = new System.Drawing.Size(64, 17);
            this.dotherxCheckbox.TabIndex = 15;
            this.dotherxCheckbox.Text = "Execute";
            this.dotherxCheckbox.UseVisualStyleBackColor = true;
            this.dotherxCheckbox.CheckedChanged += new System.EventHandler(this.dotherxCheckbox_CheckedChanged);
            // 
            // groupDownloadLabel
            // 
            this.groupDownloadLabel.Anchor = System.Windows.Forms.AnchorStyles.Right;
            this.groupDownloadLabel.AutoSize = true;
            this.groupDownloadLabel.Font = new System.Drawing.Font("Microsoft Sans Serif", 7F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.groupDownloadLabel.Location = new System.Drawing.Point(56, 28);
            this.groupDownloadLabel.Name = "groupDownloadLabel";
            this.groupDownloadLabel.Size = new System.Drawing.Size(41, 13);
            this.groupDownloadLabel.TabIndex = 8;
            this.groupDownloadLabel.Text = "Group";
            this.groupDownloadLabel.TextAlign = System.Drawing.ContentAlignment.TopRight;
            // 
            // dotherwCheckbox
            // 
            this.dotherwCheckbox.AutoSize = true;
            this.dotherwCheckbox.Font = new System.Drawing.Font("Microsoft Sans Serif", 7F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.dotherwCheckbox.Location = new System.Drawing.Point(160, 49);
            this.dotherwCheckbox.Name = "dotherwCheckbox";
            this.dotherwCheckbox.Size = new System.Drawing.Size(49, 17);
            this.dotherwCheckbox.TabIndex = 14;
            this.dotherwCheckbox.Text = "Write";
            this.dotherwCheckbox.UseVisualStyleBackColor = true;
            this.dotherwCheckbox.CheckedChanged += new System.EventHandler(this.dotherwCheckbox_CheckedChanged);
            // 
            // othersDownloadLabel
            // 
            this.othersDownloadLabel.Anchor = System.Windows.Forms.AnchorStyles.Right;
            this.othersDownloadLabel.AutoSize = true;
            this.othersDownloadLabel.Font = new System.Drawing.Font("Microsoft Sans Serif", 7F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.othersDownloadLabel.Location = new System.Drawing.Point(53, 51);
            this.othersDownloadLabel.Name = "othersDownloadLabel";
            this.othersDownloadLabel.Size = new System.Drawing.Size(44, 13);
            this.othersDownloadLabel.TabIndex = 12;
            this.othersDownloadLabel.Text = "Others";
            this.othersDownloadLabel.TextAlign = System.Drawing.ContentAlignment.TopRight;
            // 
            // dgroupxCheckbox
            // 
            this.dgroupxCheckbox.AutoSize = true;
            this.dgroupxCheckbox.Font = new System.Drawing.Font("Microsoft Sans Serif", 7F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.dgroupxCheckbox.Location = new System.Drawing.Point(215, 26);
            this.dgroupxCheckbox.Name = "dgroupxCheckbox";
            this.dgroupxCheckbox.Size = new System.Drawing.Size(64, 17);
            this.dgroupxCheckbox.TabIndex = 11;
            this.dgroupxCheckbox.Text = "Execute";
            this.dgroupxCheckbox.UseVisualStyleBackColor = true;
            this.dgroupxCheckbox.CheckedChanged += new System.EventHandler(this.dgroupxCheckbox_CheckedChanged);
            // 
            // dotherrCheckbox
            // 
            this.dotherrCheckbox.AutoSize = true;
            this.dotherrCheckbox.Font = new System.Drawing.Font("Microsoft Sans Serif", 7F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.dotherrCheckbox.Location = new System.Drawing.Point(103, 49);
            this.dotherrCheckbox.Name = "dotherrCheckbox";
            this.dotherrCheckbox.Size = new System.Drawing.Size(51, 17);
            this.dotherrCheckbox.TabIndex = 13;
            this.dotherrCheckbox.Text = "Read";
            this.dotherrCheckbox.UseVisualStyleBackColor = true;
            this.dotherrCheckbox.CheckedChanged += new System.EventHandler(this.dotherrCheckbox_CheckedChanged);
            // 
            // dgroupwCheckbox
            // 
            this.dgroupwCheckbox.AutoSize = true;
            this.dgroupwCheckbox.Font = new System.Drawing.Font("Microsoft Sans Serif", 7F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.dgroupwCheckbox.Location = new System.Drawing.Point(160, 26);
            this.dgroupwCheckbox.Name = "dgroupwCheckbox";
            this.dgroupwCheckbox.Size = new System.Drawing.Size(49, 17);
            this.dgroupwCheckbox.TabIndex = 10;
            this.dgroupwCheckbox.Text = "Write";
            this.dgroupwCheckbox.UseVisualStyleBackColor = true;
            this.dgroupwCheckbox.CheckedChanged += new System.EventHandler(this.dgroupwCheckbox_CheckedChanged);
            // 
            // downerrCheckbox
            // 
            this.downerrCheckbox.AutoSize = true;
            this.downerrCheckbox.Font = new System.Drawing.Font("Microsoft Sans Serif", 7F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.downerrCheckbox.Location = new System.Drawing.Point(103, 3);
            this.downerrCheckbox.Name = "downerrCheckbox";
            this.downerrCheckbox.Size = new System.Drawing.Size(51, 17);
            this.downerrCheckbox.TabIndex = 5;
            this.downerrCheckbox.Text = "Read";
            this.downerrCheckbox.UseVisualStyleBackColor = true;
            this.downerrCheckbox.CheckedChanged += new System.EventHandler(this.downerrCheckbox_CheckedChanged);
            // 
            // downerwCheckbox
            // 
            this.downerwCheckbox.AutoSize = true;
            this.downerwCheckbox.Font = new System.Drawing.Font("Microsoft Sans Serif", 7F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.downerwCheckbox.Location = new System.Drawing.Point(160, 3);
            this.downerwCheckbox.Name = "downerwCheckbox";
            this.downerwCheckbox.Size = new System.Drawing.Size(49, 17);
            this.downerwCheckbox.TabIndex = 6;
            this.downerwCheckbox.Text = "Write";
            this.downerwCheckbox.UseVisualStyleBackColor = true;
            this.downerwCheckbox.CheckedChanged += new System.EventHandler(this.downerwCheckbox_CheckedChanged);
            // 
            // downerxCheckbox
            // 
            this.downerxCheckbox.AutoSize = true;
            this.downerxCheckbox.Font = new System.Drawing.Font("Microsoft Sans Serif", 7F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.downerxCheckbox.Location = new System.Drawing.Point(215, 3);
            this.downerxCheckbox.Name = "downerxCheckbox";
            this.downerxCheckbox.Size = new System.Drawing.Size(64, 17);
            this.downerxCheckbox.TabIndex = 7;
            this.downerxCheckbox.Text = "Execute";
            this.downerxCheckbox.UseVisualStyleBackColor = true;
            this.downerxCheckbox.CheckedChanged += new System.EventHandler(this.downerxCheckbox_CheckedChanged);
            // 
            // dgrouprCheckbox
            // 
            this.dgrouprCheckbox.AutoSize = true;
            this.dgrouprCheckbox.Font = new System.Drawing.Font("Microsoft Sans Serif", 7F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.dgrouprCheckbox.Location = new System.Drawing.Point(103, 26);
            this.dgrouprCheckbox.Name = "dgrouprCheckbox";
            this.dgrouprCheckbox.Size = new System.Drawing.Size(51, 17);
            this.dgrouprCheckbox.TabIndex = 9;
            this.dgrouprCheckbox.Text = "Read";
            this.dgrouprCheckbox.UseVisualStyleBackColor = true;
            this.dgrouprCheckbox.CheckedChanged += new System.EventHandler(this.dgrouprCheckbox_CheckedChanged);
            // 
            // groupBox8
            // 
            this.groupBox8.AutoSize = true;
            this.groupBox8.Controls.Add(this.tableLayoutPanel12);
            this.groupBox8.Dock = System.Windows.Forms.DockStyle.Fill;
            this.groupBox8.Location = new System.Drawing.Point(3, 191);
            this.groupBox8.Name = "groupBox8";
            this.groupBox8.Size = new System.Drawing.Size(1, 182);
            this.groupBox8.TabIndex = 1;
            this.groupBox8.TabStop = false;
            this.groupBox8.Text = "Uploads";
            // 
            // tableLayoutPanel12
            // 
            this.tableLayoutPanel12.AutoSize = true;
            this.tableLayoutPanel12.ColumnCount = 2;
            this.tableLayoutPanel12.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle());
            this.tableLayoutPanel12.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle(System.Windows.Forms.SizeType.Percent, 100F));
            this.tableLayoutPanel12.Controls.Add(this.chmodUploadCheckbox, 0, 0);
            this.tableLayoutPanel12.Controls.Add(this.chmodUploadCustomRadioButton, 0, 1);
            this.tableLayoutPanel12.Controls.Add(this.chmodUploadDefaultRadioButton, 0, 2);
            this.tableLayoutPanel12.Controls.Add(this.chmodUploadTypeCombobox, 1, 2);
            this.tableLayoutPanel12.Controls.Add(this.tableLayoutPanel13, 0, 3);
            this.tableLayoutPanel12.Dock = System.Windows.Forms.DockStyle.Fill;
            this.tableLayoutPanel12.Location = new System.Drawing.Point(3, 19);
            this.tableLayoutPanel12.Name = "tableLayoutPanel12";
            this.tableLayoutPanel12.Padding = new System.Windows.Forms.Padding(5);
            this.tableLayoutPanel12.RowCount = 4;
            this.tableLayoutPanel12.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel12.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel12.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel12.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel12.Size = new System.Drawing.Size(0, 160);
            this.tableLayoutPanel12.TabIndex = 16;
            // 
            // chmodUploadCheckbox
            // 
            this.chmodUploadCheckbox.AutoSize = true;
            this.tableLayoutPanel12.SetColumnSpan(this.chmodUploadCheckbox, 2);
            this.chmodUploadCheckbox.Location = new System.Drawing.Point(8, 8);
            this.chmodUploadCheckbox.Name = "chmodUploadCheckbox";
            this.chmodUploadCheckbox.Size = new System.Drawing.Size(1, 19);
            this.chmodUploadCheckbox.TabIndex = 0;
            this.chmodUploadCheckbox.Text = "Change permissions";
            this.chmodUploadCheckbox.UseVisualStyleBackColor = true;
            this.chmodUploadCheckbox.CheckedChanged += new System.EventHandler(this.chmodUploadCheckbox_CheckedChanged);
            // 
            // chmodUploadCustomRadioButton
            // 
            this.chmodUploadCustomRadioButton.AutoSize = true;
            this.tableLayoutPanel12.SetColumnSpan(this.chmodUploadCustomRadioButton, 2);
            this.chmodUploadCustomRadioButton.Font = new System.Drawing.Font("Microsoft Sans Serif", 7F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.chmodUploadCustomRadioButton.Location = new System.Drawing.Point(8, 33);
            this.chmodUploadCustomRadioButton.Name = "chmodUploadCustomRadioButton";
            this.chmodUploadCustomRadioButton.Padding = new System.Windows.Forms.Padding(20, 0, 0, 0);
            this.chmodUploadCustomRadioButton.Size = new System.Drawing.Size(1, 17);
            this.chmodUploadCustomRadioButton.TabIndex = 1;
            this.chmodUploadCustomRadioButton.TabStop = true;
            this.chmodUploadCustomRadioButton.Text = "to the permissions of the local file or folder";
            this.chmodUploadCustomRadioButton.UseVisualStyleBackColor = true;
            this.chmodUploadCustomRadioButton.Visible = false;
            // 
            // chmodUploadDefaultRadioButton
            // 
            this.chmodUploadDefaultRadioButton.AutoSize = true;
            this.chmodUploadDefaultRadioButton.Font = new System.Drawing.Font("Microsoft Sans Serif", 7F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.chmodUploadDefaultRadioButton.Location = new System.Drawing.Point(8, 56);
            this.chmodUploadDefaultRadioButton.Name = "chmodUploadDefaultRadioButton";
            this.chmodUploadDefaultRadioButton.Padding = new System.Windows.Forms.Padding(20, 0, 0, 0);
            this.chmodUploadDefaultRadioButton.Size = new System.Drawing.Size(143, 17);
            this.chmodUploadDefaultRadioButton.TabIndex = 2;
            this.chmodUploadDefaultRadioButton.TabStop = true;
            this.chmodUploadDefaultRadioButton.Text = "to these permissions:";
            this.chmodUploadDefaultRadioButton.UseVisualStyleBackColor = true;
            this.chmodUploadDefaultRadioButton.CheckedChanged += new System.EventHandler(this.chmodUploadDefaultRadioButton_CheckedChanged);
            // 
            // chmodUploadTypeCombobox
            // 
            this.chmodUploadTypeCombobox.DropDownStyle = System.Windows.Forms.ComboBoxStyle.DropDownList;
            this.chmodUploadTypeCombobox.Font = new System.Drawing.Font("Microsoft Sans Serif", 7F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.chmodUploadTypeCombobox.FormattingEnabled = true;
            this.chmodUploadTypeCombobox.Location = new System.Drawing.Point(157, 56);
            this.chmodUploadTypeCombobox.MinimumSize = new System.Drawing.Size(150, 0);
            this.chmodUploadTypeCombobox.Name = "chmodUploadTypeCombobox";
            this.chmodUploadTypeCombobox.Size = new System.Drawing.Size(150, 20);
            this.chmodUploadTypeCombobox.TabIndex = 3;
            this.chmodUploadTypeCombobox.SelectionChangeCommitted += new System.EventHandler(this.chmodUploadTypeCombobox_SelectionChangeCommitted);
            // 
            // tableLayoutPanel13
            // 
            this.tableLayoutPanel13.AutoSize = true;
            this.tableLayoutPanel13.ColumnCount = 6;
            this.tableLayoutPanel12.SetColumnSpan(this.tableLayoutPanel13, 2);
            this.tableLayoutPanel13.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle(System.Windows.Forms.SizeType.Absolute, 50F));
            this.tableLayoutPanel13.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle());
            this.tableLayoutPanel13.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle());
            this.tableLayoutPanel13.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle());
            this.tableLayoutPanel13.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle());
            this.tableLayoutPanel13.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle(System.Windows.Forms.SizeType.Percent, 100F));
            this.tableLayoutPanel13.Controls.Add(this.ownerUploadLabel, 1, 0);
            this.tableLayoutPanel13.Controls.Add(this.uotherxCheckbox, 4, 2);
            this.tableLayoutPanel13.Controls.Add(this.uownerrCheckbox, 2, 0);
            this.tableLayoutPanel13.Controls.Add(this.uotherwCheckbox, 3, 2);
            this.tableLayoutPanel13.Controls.Add(this.uownerwCheckbox, 3, 0);
            this.tableLayoutPanel13.Controls.Add(this.uotherrCheckbox, 2, 2);
            this.tableLayoutPanel13.Controls.Add(this.uownerxCheckbox, 4, 0);
            this.tableLayoutPanel13.Controls.Add(this.othersUploadLabel, 1, 2);
            this.tableLayoutPanel13.Controls.Add(this.groupUploadLabel, 1, 1);
            this.tableLayoutPanel13.Controls.Add(this.ugroupxCheckbox, 4, 1);
            this.tableLayoutPanel13.Controls.Add(this.ugrouprCheckbox, 2, 1);
            this.tableLayoutPanel13.Controls.Add(this.ugroupwCheckbox, 3, 1);
            this.tableLayoutPanel13.Dock = System.Windows.Forms.DockStyle.Fill;
            this.tableLayoutPanel13.Location = new System.Drawing.Point(8, 83);
            this.tableLayoutPanel13.Name = "tableLayoutPanel13";
            this.tableLayoutPanel13.RowCount = 3;
            this.tableLayoutPanel13.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel13.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel13.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel13.Size = new System.Drawing.Size(1, 69);
            this.tableLayoutPanel13.TabIndex = 4;
            // 
            // ownerUploadLabel
            // 
            this.ownerUploadLabel.Anchor = System.Windows.Forms.AnchorStyles.Right;
            this.ownerUploadLabel.AutoSize = true;
            this.ownerUploadLabel.Font = new System.Drawing.Font("Microsoft Sans Serif", 7F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.ownerUploadLabel.Location = new System.Drawing.Point(54, 5);
            this.ownerUploadLabel.Name = "ownerUploadLabel";
            this.ownerUploadLabel.Size = new System.Drawing.Size(43, 13);
            this.ownerUploadLabel.TabIndex = 4;
            this.ownerUploadLabel.Text = "Owner";
            this.ownerUploadLabel.TextAlign = System.Drawing.ContentAlignment.TopRight;
            // 
            // uotherxCheckbox
            // 
            this.uotherxCheckbox.AutoSize = true;
            this.uotherxCheckbox.Font = new System.Drawing.Font("Microsoft Sans Serif", 7F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.uotherxCheckbox.Location = new System.Drawing.Point(215, 49);
            this.uotherxCheckbox.Name = "uotherxCheckbox";
            this.uotherxCheckbox.Size = new System.Drawing.Size(64, 17);
            this.uotherxCheckbox.TabIndex = 15;
            this.uotherxCheckbox.Text = "Execute";
            this.uotherxCheckbox.UseVisualStyleBackColor = true;
            this.uotherxCheckbox.CheckedChanged += new System.EventHandler(this.uotherxCheckbox_CheckedChanged);
            // 
            // uownerrCheckbox
            // 
            this.uownerrCheckbox.AutoSize = true;
            this.uownerrCheckbox.Font = new System.Drawing.Font("Microsoft Sans Serif", 7F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.uownerrCheckbox.Location = new System.Drawing.Point(103, 3);
            this.uownerrCheckbox.Name = "uownerrCheckbox";
            this.uownerrCheckbox.Size = new System.Drawing.Size(51, 17);
            this.uownerrCheckbox.TabIndex = 5;
            this.uownerrCheckbox.Text = "Read";
            this.uownerrCheckbox.UseVisualStyleBackColor = true;
            this.uownerrCheckbox.CheckedChanged += new System.EventHandler(this.uownerrCheckbox_CheckedChanged);
            // 
            // uotherwCheckbox
            // 
            this.uotherwCheckbox.AutoSize = true;
            this.uotherwCheckbox.Font = new System.Drawing.Font("Microsoft Sans Serif", 7F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.uotherwCheckbox.Location = new System.Drawing.Point(160, 49);
            this.uotherwCheckbox.Name = "uotherwCheckbox";
            this.uotherwCheckbox.Size = new System.Drawing.Size(49, 17);
            this.uotherwCheckbox.TabIndex = 14;
            this.uotherwCheckbox.Text = "Write";
            this.uotherwCheckbox.UseVisualStyleBackColor = true;
            this.uotherwCheckbox.CheckedChanged += new System.EventHandler(this.uotherwCheckbox_CheckedChanged);
            // 
            // uownerwCheckbox
            // 
            this.uownerwCheckbox.AutoSize = true;
            this.uownerwCheckbox.Font = new System.Drawing.Font("Microsoft Sans Serif", 7F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.uownerwCheckbox.Location = new System.Drawing.Point(160, 3);
            this.uownerwCheckbox.Name = "uownerwCheckbox";
            this.uownerwCheckbox.Size = new System.Drawing.Size(49, 17);
            this.uownerwCheckbox.TabIndex = 6;
            this.uownerwCheckbox.Text = "Write";
            this.uownerwCheckbox.UseVisualStyleBackColor = true;
            this.uownerwCheckbox.CheckedChanged += new System.EventHandler(this.uownerwCheckbox_CheckedChanged);
            // 
            // uotherrCheckbox
            // 
            this.uotherrCheckbox.AutoSize = true;
            this.uotherrCheckbox.Font = new System.Drawing.Font("Microsoft Sans Serif", 7F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.uotherrCheckbox.Location = new System.Drawing.Point(103, 49);
            this.uotherrCheckbox.Name = "uotherrCheckbox";
            this.uotherrCheckbox.Size = new System.Drawing.Size(51, 17);
            this.uotherrCheckbox.TabIndex = 13;
            this.uotherrCheckbox.Text = "Read";
            this.uotherrCheckbox.UseVisualStyleBackColor = true;
            this.uotherrCheckbox.CheckedChanged += new System.EventHandler(this.uotherrCheckbox_CheckedChanged);
            // 
            // uownerxCheckbox
            // 
            this.uownerxCheckbox.AutoSize = true;
            this.uownerxCheckbox.Font = new System.Drawing.Font("Microsoft Sans Serif", 7F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.uownerxCheckbox.Location = new System.Drawing.Point(215, 3);
            this.uownerxCheckbox.Name = "uownerxCheckbox";
            this.uownerxCheckbox.Size = new System.Drawing.Size(64, 17);
            this.uownerxCheckbox.TabIndex = 7;
            this.uownerxCheckbox.Text = "Execute";
            this.uownerxCheckbox.UseVisualStyleBackColor = true;
            this.uownerxCheckbox.CheckedChanged += new System.EventHandler(this.uownerxCheckbox_CheckedChanged);
            // 
            // othersUploadLabel
            // 
            this.othersUploadLabel.Anchor = System.Windows.Forms.AnchorStyles.Right;
            this.othersUploadLabel.AutoSize = true;
            this.othersUploadLabel.Font = new System.Drawing.Font("Microsoft Sans Serif", 7F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.othersUploadLabel.Location = new System.Drawing.Point(53, 51);
            this.othersUploadLabel.Name = "othersUploadLabel";
            this.othersUploadLabel.Size = new System.Drawing.Size(44, 13);
            this.othersUploadLabel.TabIndex = 12;
            this.othersUploadLabel.Text = "Others";
            this.othersUploadLabel.TextAlign = System.Drawing.ContentAlignment.TopRight;
            // 
            // groupUploadLabel
            // 
            this.groupUploadLabel.Anchor = System.Windows.Forms.AnchorStyles.Right;
            this.groupUploadLabel.AutoSize = true;
            this.groupUploadLabel.Font = new System.Drawing.Font("Microsoft Sans Serif", 7F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.groupUploadLabel.Location = new System.Drawing.Point(56, 28);
            this.groupUploadLabel.Name = "groupUploadLabel";
            this.groupUploadLabel.Size = new System.Drawing.Size(41, 13);
            this.groupUploadLabel.TabIndex = 8;
            this.groupUploadLabel.Text = "Group";
            this.groupUploadLabel.TextAlign = System.Drawing.ContentAlignment.TopRight;
            // 
            // ugroupxCheckbox
            // 
            this.ugroupxCheckbox.AutoSize = true;
            this.ugroupxCheckbox.Font = new System.Drawing.Font("Microsoft Sans Serif", 7F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.ugroupxCheckbox.Location = new System.Drawing.Point(215, 26);
            this.ugroupxCheckbox.Name = "ugroupxCheckbox";
            this.ugroupxCheckbox.Size = new System.Drawing.Size(64, 17);
            this.ugroupxCheckbox.TabIndex = 11;
            this.ugroupxCheckbox.Text = "Execute";
            this.ugroupxCheckbox.UseVisualStyleBackColor = true;
            this.ugroupxCheckbox.CheckedChanged += new System.EventHandler(this.ugroupxCheckbox_CheckedChanged);
            // 
            // ugrouprCheckbox
            // 
            this.ugrouprCheckbox.AutoSize = true;
            this.ugrouprCheckbox.Font = new System.Drawing.Font("Microsoft Sans Serif", 7F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.ugrouprCheckbox.Location = new System.Drawing.Point(103, 26);
            this.ugrouprCheckbox.Name = "ugrouprCheckbox";
            this.ugrouprCheckbox.Size = new System.Drawing.Size(51, 17);
            this.ugrouprCheckbox.TabIndex = 9;
            this.ugrouprCheckbox.Text = "Read";
            this.ugrouprCheckbox.UseVisualStyleBackColor = true;
            this.ugrouprCheckbox.CheckedChanged += new System.EventHandler(this.ugrouprCheckbox_CheckedChanged);
            // 
            // ugroupwCheckbox
            // 
            this.ugroupwCheckbox.AutoSize = true;
            this.ugroupwCheckbox.Font = new System.Drawing.Font("Microsoft Sans Serif", 7F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.ugroupwCheckbox.Location = new System.Drawing.Point(160, 26);
            this.ugroupwCheckbox.Name = "ugroupwCheckbox";
            this.ugroupwCheckbox.Size = new System.Drawing.Size(49, 17);
            this.ugroupwCheckbox.TabIndex = 10;
            this.ugroupwCheckbox.Text = "Write";
            this.ugroupwCheckbox.UseVisualStyleBackColor = true;
            this.ugroupwCheckbox.CheckedChanged += new System.EventHandler(this.ugroupwCheckbox_CheckedChanged);
            // 
            // tabPage3
            // 
            this.tabPage3.Controls.Add(this.tableLayoutPanel14);
            this.tabPage3.Location = new System.Drawing.Point(4, 27);
            this.tabPage3.Name = "tabPage3";
            this.tabPage3.Size = new System.Drawing.Size(0, 0);
            this.tabPage3.TabIndex = 2;
            this.tabPage3.Text = "Timestamps";
            this.tabPage3.UseVisualStyleBackColor = true;
            // 
            // tableLayoutPanel14
            // 
            this.tableLayoutPanel14.AutoSize = true;
            this.tableLayoutPanel14.ColumnCount = 1;
            this.tableLayoutPanel14.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle(System.Windows.Forms.SizeType.Percent, 100F));
            this.tableLayoutPanel14.Controls.Add(this.groupBox9, 0, 0);
            this.tableLayoutPanel14.Controls.Add(this.groupBox10, 0, 1);
            this.tableLayoutPanel14.Dock = System.Windows.Forms.DockStyle.Fill;
            this.tableLayoutPanel14.Location = new System.Drawing.Point(0, 0);
            this.tableLayoutPanel14.Name = "tableLayoutPanel14";
            this.tableLayoutPanel14.RowCount = 3;
            this.tableLayoutPanel14.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel14.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel14.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel14.Size = new System.Drawing.Size(0, 0);
            this.tableLayoutPanel14.TabIndex = 2;
            // 
            // groupBox9
            // 
            this.groupBox9.AutoSize = true;
            this.groupBox9.Controls.Add(this.tableLayoutPanel15);
            this.groupBox9.Dock = System.Windows.Forms.DockStyle.Fill;
            this.groupBox9.Location = new System.Drawing.Point(3, 3);
            this.groupBox9.Name = "groupBox9";
            this.groupBox9.Size = new System.Drawing.Size(1, 57);
            this.groupBox9.TabIndex = 0;
            this.groupBox9.TabStop = false;
            this.groupBox9.Text = "Downloads";
            // 
            // tableLayoutPanel15
            // 
            this.tableLayoutPanel15.AutoSize = true;
            this.tableLayoutPanel15.ColumnCount = 1;
            this.tableLayoutPanel15.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle(System.Windows.Forms.SizeType.Percent, 50F));
            this.tableLayoutPanel15.Controls.Add(this.preserveModificationDownloadCheckbox, 0, 0);
            this.tableLayoutPanel15.Dock = System.Windows.Forms.DockStyle.Fill;
            this.tableLayoutPanel15.Location = new System.Drawing.Point(3, 19);
            this.tableLayoutPanel15.Name = "tableLayoutPanel15";
            this.tableLayoutPanel15.Padding = new System.Windows.Forms.Padding(5);
            this.tableLayoutPanel15.RowCount = 1;
            this.tableLayoutPanel15.RowStyles.Add(new System.Windows.Forms.RowStyle(System.Windows.Forms.SizeType.Percent, 50F));
            this.tableLayoutPanel15.Size = new System.Drawing.Size(0, 35);
            this.tableLayoutPanel15.TabIndex = 1;
            // 
            // preserveModificationDownloadCheckbox
            // 
            this.preserveModificationDownloadCheckbox.AutoSize = true;
            this.preserveModificationDownloadCheckbox.Location = new System.Drawing.Point(8, 8);
            this.preserveModificationDownloadCheckbox.Name = "preserveModificationDownloadCheckbox";
            this.preserveModificationDownloadCheckbox.Size = new System.Drawing.Size(1, 19);
            this.preserveModificationDownloadCheckbox.TabIndex = 0;
            this.preserveModificationDownloadCheckbox.Text = "Preserve modification date";
            this.preserveModificationDownloadCheckbox.UseVisualStyleBackColor = true;
            this.preserveModificationDownloadCheckbox.CheckedChanged += new System.EventHandler(this.preserveModificationDownloadCheckbox_CheckedChanged);
            // 
            // groupBox10
            // 
            this.groupBox10.AutoSize = true;
            this.groupBox10.Controls.Add(this.tableLayoutPanel16);
            this.groupBox10.Dock = System.Windows.Forms.DockStyle.Fill;
            this.groupBox10.Location = new System.Drawing.Point(3, 66);
            this.groupBox10.Name = "groupBox10";
            this.groupBox10.Size = new System.Drawing.Size(1, 57);
            this.groupBox10.TabIndex = 1;
            this.groupBox10.TabStop = false;
            this.groupBox10.Text = "Uploads";
            // 
            // tableLayoutPanel16
            // 
            this.tableLayoutPanel16.AutoSize = true;
            this.tableLayoutPanel16.ColumnCount = 1;
            this.tableLayoutPanel16.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle(System.Windows.Forms.SizeType.Percent, 50F));
            this.tableLayoutPanel16.Controls.Add(this.preserveModificationUploadCheckbox, 0, 0);
            this.tableLayoutPanel16.Dock = System.Windows.Forms.DockStyle.Fill;
            this.tableLayoutPanel16.Location = new System.Drawing.Point(3, 19);
            this.tableLayoutPanel16.Name = "tableLayoutPanel16";
            this.tableLayoutPanel16.Padding = new System.Windows.Forms.Padding(5);
            this.tableLayoutPanel16.RowCount = 1;
            this.tableLayoutPanel16.RowStyles.Add(new System.Windows.Forms.RowStyle(System.Windows.Forms.SizeType.Percent, 50F));
            this.tableLayoutPanel16.Size = new System.Drawing.Size(0, 35);
            this.tableLayoutPanel16.TabIndex = 1;
            // 
            // preserveModificationUploadCheckbox
            // 
            this.preserveModificationUploadCheckbox.AutoSize = true;
            this.preserveModificationUploadCheckbox.Location = new System.Drawing.Point(8, 8);
            this.preserveModificationUploadCheckbox.Name = "preserveModificationUploadCheckbox";
            this.preserveModificationUploadCheckbox.Size = new System.Drawing.Size(1, 19);
            this.preserveModificationUploadCheckbox.TabIndex = 0;
            this.preserveModificationUploadCheckbox.Text = "Preserve modification date";
            this.preserveModificationUploadCheckbox.UseVisualStyleBackColor = true;
            this.preserveModificationUploadCheckbox.CheckedChanged += new System.EventHandler(this.preserveModificationUploadCheckbox_CheckedChanged);
            // 
            // tabPage5
            // 
            this.tabPage5.Controls.Add(this.tableLayoutPanel26);
            this.tabPage5.Location = new System.Drawing.Point(4, 27);
            this.tabPage5.Name = "tabPage5";
            this.tabPage5.Size = new System.Drawing.Size(0, 0);
            this.tabPage5.TabIndex = 4;
            this.tabPage5.Text = "Checksum";
            this.tabPage5.UseVisualStyleBackColor = true;
            // 
            // tableLayoutPanel26
            // 
            this.tableLayoutPanel26.AutoSize = true;
            this.tableLayoutPanel26.ColumnCount = 1;
            this.tableLayoutPanel26.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle(System.Windows.Forms.SizeType.Percent, 100F));
            this.tableLayoutPanel26.Controls.Add(this.groupBox16, 0, 0);
            this.tableLayoutPanel26.Controls.Add(this.groupBox17, 0, 1);
            this.tableLayoutPanel26.Dock = System.Windows.Forms.DockStyle.Fill;
            this.tableLayoutPanel26.Location = new System.Drawing.Point(0, 0);
            this.tableLayoutPanel26.Name = "tableLayoutPanel26";
            this.tableLayoutPanel26.RowCount = 3;
            this.tableLayoutPanel26.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel26.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel26.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel26.Size = new System.Drawing.Size(0, 0);
            this.tableLayoutPanel26.TabIndex = 3;
            // 
            // groupBox16
            // 
            this.groupBox16.AutoSize = true;
            this.groupBox16.Controls.Add(this.tableLayoutPanel31);
            this.groupBox16.Dock = System.Windows.Forms.DockStyle.Fill;
            this.groupBox16.Location = new System.Drawing.Point(3, 3);
            this.groupBox16.Name = "groupBox16";
            this.groupBox16.Size = new System.Drawing.Size(1, 57);
            this.groupBox16.TabIndex = 0;
            this.groupBox16.TabStop = false;
            this.groupBox16.Text = "Downloads";
            // 
            // tableLayoutPanel31
            // 
            this.tableLayoutPanel31.AutoSize = true;
            this.tableLayoutPanel31.ColumnCount = 1;
            this.tableLayoutPanel31.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle(System.Windows.Forms.SizeType.Percent, 50F));
            this.tableLayoutPanel31.Controls.Add(this.verifyChecksumDownloadCheckbox, 0, 0);
            this.tableLayoutPanel31.Dock = System.Windows.Forms.DockStyle.Fill;
            this.tableLayoutPanel31.Location = new System.Drawing.Point(3, 19);
            this.tableLayoutPanel31.Name = "tableLayoutPanel31";
            this.tableLayoutPanel31.Padding = new System.Windows.Forms.Padding(5);
            this.tableLayoutPanel31.RowCount = 1;
            this.tableLayoutPanel31.RowStyles.Add(new System.Windows.Forms.RowStyle(System.Windows.Forms.SizeType.Percent, 50F));
            this.tableLayoutPanel31.Size = new System.Drawing.Size(0, 35);
            this.tableLayoutPanel31.TabIndex = 1;
            // 
            // verifyChecksumDownloadCheckbox
            // 
            this.verifyChecksumDownloadCheckbox.AutoSize = true;
            this.verifyChecksumDownloadCheckbox.Location = new System.Drawing.Point(8, 8);
            this.verifyChecksumDownloadCheckbox.Name = "verifyChecksumDownloadCheckbox";
            this.verifyChecksumDownloadCheckbox.Size = new System.Drawing.Size(1, 19);
            this.verifyChecksumDownloadCheckbox.TabIndex = 0;
            this.verifyChecksumDownloadCheckbox.Text = "Verify checksum";
            this.verifyChecksumDownloadCheckbox.UseVisualStyleBackColor = true;
            this.verifyChecksumDownloadCheckbox.CheckedChanged += new System.EventHandler(this.verifyChecksumDownloadCheckbox_CheckedChanged);
            // 
            // groupBox17
            // 
            this.groupBox17.AutoSize = true;
            this.groupBox17.Controls.Add(this.tableLayoutPanel32);
            this.groupBox17.Dock = System.Windows.Forms.DockStyle.Fill;
            this.groupBox17.Location = new System.Drawing.Point(3, 66);
            this.groupBox17.Name = "groupBox17";
            this.groupBox17.Size = new System.Drawing.Size(1, 57);
            this.groupBox17.TabIndex = 1;
            this.groupBox17.TabStop = false;
            this.groupBox17.Text = "Uploads";
            // 
            // tableLayoutPanel32
            // 
            this.tableLayoutPanel32.AutoSize = true;
            this.tableLayoutPanel32.ColumnCount = 1;
            this.tableLayoutPanel32.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle(System.Windows.Forms.SizeType.Percent, 50F));
            this.tableLayoutPanel32.Controls.Add(this.verifyChecksumUploadCheckbox, 0, 0);
            this.tableLayoutPanel32.Dock = System.Windows.Forms.DockStyle.Fill;
            this.tableLayoutPanel32.Location = new System.Drawing.Point(3, 19);
            this.tableLayoutPanel32.Name = "tableLayoutPanel32";
            this.tableLayoutPanel32.Padding = new System.Windows.Forms.Padding(5);
            this.tableLayoutPanel32.RowCount = 1;
            this.tableLayoutPanel32.RowStyles.Add(new System.Windows.Forms.RowStyle(System.Windows.Forms.SizeType.Percent, 50F));
            this.tableLayoutPanel32.Size = new System.Drawing.Size(0, 35);
            this.tableLayoutPanel32.TabIndex = 1;
            // 
            // verifyChecksumUploadCheckbox
            // 
            this.verifyChecksumUploadCheckbox.AutoSize = true;
            this.verifyChecksumUploadCheckbox.Location = new System.Drawing.Point(8, 8);
            this.verifyChecksumUploadCheckbox.Name = "verifyChecksumUploadCheckbox";
            this.verifyChecksumUploadCheckbox.Size = new System.Drawing.Size(1, 19);
            this.verifyChecksumUploadCheckbox.TabIndex = 0;
            this.verifyChecksumUploadCheckbox.Text = "Verify checksum";
            this.verifyChecksumUploadCheckbox.UseVisualStyleBackColor = true;
            this.verifyChecksumUploadCheckbox.CheckedChanged += new System.EventHandler(this.verifyChecksumUploadCheckbox_CheckedChanged);
            // 
            // tabPage4
            // 
            this.tabPage4.Controls.Add(this.tableLayoutPanel17);
            this.tabPage4.Location = new System.Drawing.Point(4, 27);
            this.tabPage4.Name = "tabPage4";
            this.tabPage4.Size = new System.Drawing.Size(0, 0);
            this.tabPage4.TabIndex = 3;
            this.tabPage4.Text = "Filter";
            this.tabPage4.UseVisualStyleBackColor = true;
            // 
            // tableLayoutPanel17
            // 
            this.tableLayoutPanel17.AutoSize = true;
            this.tableLayoutPanel17.ColumnCount = 1;
            this.tableLayoutPanel17.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle(System.Windows.Forms.SizeType.Percent, 100F));
            this.tableLayoutPanel17.Controls.Add(this.groupBox11, 0, 0);
            this.tableLayoutPanel17.Controls.Add(this.groupBox12, 0, 1);
            this.tableLayoutPanel17.Dock = System.Windows.Forms.DockStyle.Fill;
            this.tableLayoutPanel17.Location = new System.Drawing.Point(0, 0);
            this.tableLayoutPanel17.Name = "tableLayoutPanel17";
            this.tableLayoutPanel17.RowCount = 3;
            this.tableLayoutPanel17.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel17.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel17.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel17.Size = new System.Drawing.Size(0, 0);
            this.tableLayoutPanel17.TabIndex = 3;
            // 
            // groupBox11
            // 
            this.groupBox11.AutoSize = true;
            this.groupBox11.Controls.Add(this.tableLayoutPanel18);
            this.groupBox11.Dock = System.Windows.Forms.DockStyle.Fill;
            this.groupBox11.Location = new System.Drawing.Point(3, 3);
            this.groupBox11.Name = "groupBox11";
            this.groupBox11.Size = new System.Drawing.Size(1, 163);
            this.groupBox11.TabIndex = 1;
            this.groupBox11.TabStop = false;
            this.groupBox11.Text = "Downloads";
            // 
            // tableLayoutPanel18
            // 
            this.tableLayoutPanel18.AutoSize = true;
            this.tableLayoutPanel18.ColumnCount = 2;
            this.tableLayoutPanel18.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle(System.Windows.Forms.SizeType.Percent, 100F));
            this.tableLayoutPanel18.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle());
            this.tableLayoutPanel18.Controls.Add(this.downloadSkipCheckbox, 0, 0);
            this.tableLayoutPanel18.Controls.Add(this.downloadSkipRegexRichTextbox, 0, 1);
            this.tableLayoutPanel18.Controls.Add(this.downloadSkipRegexDefaultButton, 1, 0);
            this.tableLayoutPanel18.Dock = System.Windows.Forms.DockStyle.Fill;
            this.tableLayoutPanel18.Location = new System.Drawing.Point(3, 19);
            this.tableLayoutPanel18.Name = "tableLayoutPanel18";
            this.tableLayoutPanel18.Padding = new System.Windows.Forms.Padding(5);
            this.tableLayoutPanel18.RowCount = 2;
            this.tableLayoutPanel18.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel18.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel18.Size = new System.Drawing.Size(0, 141);
            this.tableLayoutPanel18.TabIndex = 3;
            // 
            // downloadSkipCheckbox
            // 
            this.downloadSkipCheckbox.Anchor = System.Windows.Forms.AnchorStyles.Left;
            this.downloadSkipCheckbox.AutoSize = true;
            this.downloadSkipCheckbox.Location = new System.Drawing.Point(8, 10);
            this.downloadSkipCheckbox.Name = "downloadSkipCheckbox";
            this.downloadSkipCheckbox.Size = new System.Drawing.Size(1, 19);
            this.downloadSkipCheckbox.TabIndex = 0;
            this.downloadSkipCheckbox.Text = "Skip files matching the regular expression:";
            this.downloadSkipCheckbox.UseVisualStyleBackColor = true;
            this.downloadSkipCheckbox.CheckedChanged += new System.EventHandler(this.downloadSkipCheckbox_CheckedChanged);
            // 
            // downloadSkipRegexRichTextbox
            // 
            this.tableLayoutPanel18.SetColumnSpan(this.downloadSkipRegexRichTextbox, 2);
            this.downloadSkipRegexRichTextbox.Dock = System.Windows.Forms.DockStyle.Fill;
            this.downloadSkipRegexRichTextbox.Font = new System.Drawing.Font("Courier New", 8.25F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.downloadSkipRegexRichTextbox.Location = new System.Drawing.Point(8, 37);
            this.downloadSkipRegexRichTextbox.Name = "downloadSkipRegexRichTextbox";
            this.downloadSkipRegexRichTextbox.Size = new System.Drawing.Size(1, 96);
            this.downloadSkipRegexRichTextbox.TabIndex = 1;
            this.downloadSkipRegexRichTextbox.Text = "";
            this.downloadSkipRegexRichTextbox.TextChanged += new System.EventHandler(this.downloadSkipRegexRichTextbox_TextChanged);
            // 
            // downloadSkipRegexDefaultButton
            // 
            this.downloadSkipRegexDefaultButton.AutoSize = true;
            this.downloadSkipRegexDefaultButton.AutoSizeMode = System.Windows.Forms.AutoSizeMode.GrowAndShrink;
            this.downloadSkipRegexDefaultButton.Font = new System.Drawing.Font("Microsoft Sans Serif", 7F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.downloadSkipRegexDefaultButton.Location = new System.Drawing.Point(-48, 8);
            this.downloadSkipRegexDefaultButton.Name = "downloadSkipRegexDefaultButton";
            this.downloadSkipRegexDefaultButton.Size = new System.Drawing.Size(51, 23);
            this.downloadSkipRegexDefaultButton.TabIndex = 2;
            this.downloadSkipRegexDefaultButton.Text = "Default";
            this.downloadSkipRegexDefaultButton.UseVisualStyleBackColor = true;
            this.downloadSkipRegexDefaultButton.Click += new System.EventHandler(this.downloadSkipRegexDefaultButton_Click);
            // 
            // groupBox12
            // 
            this.groupBox12.AutoSize = true;
            this.groupBox12.Controls.Add(this.tableLayoutPanel19);
            this.groupBox12.Dock = System.Windows.Forms.DockStyle.Fill;
            this.groupBox12.Location = new System.Drawing.Point(3, 172);
            this.groupBox12.Name = "groupBox12";
            this.groupBox12.Size = new System.Drawing.Size(1, 163);
            this.groupBox12.TabIndex = 2;
            this.groupBox12.TabStop = false;
            this.groupBox12.Text = "Uploads";
            // 
            // tableLayoutPanel19
            // 
            this.tableLayoutPanel19.AutoSize = true;
            this.tableLayoutPanel19.ColumnCount = 2;
            this.tableLayoutPanel19.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle(System.Windows.Forms.SizeType.Percent, 100F));
            this.tableLayoutPanel19.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle());
            this.tableLayoutPanel19.Controls.Add(this.uploadSkipCheckbox, 0, 0);
            this.tableLayoutPanel19.Controls.Add(this.uploadSkipRegexRichTextbox, 0, 1);
            this.tableLayoutPanel19.Controls.Add(this.uploadSkipRegexDefaultButton, 1, 0);
            this.tableLayoutPanel19.Dock = System.Windows.Forms.DockStyle.Fill;
            this.tableLayoutPanel19.Location = new System.Drawing.Point(3, 19);
            this.tableLayoutPanel19.Name = "tableLayoutPanel19";
            this.tableLayoutPanel19.Padding = new System.Windows.Forms.Padding(5);
            this.tableLayoutPanel19.RowCount = 2;
            this.tableLayoutPanel19.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel19.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel19.Size = new System.Drawing.Size(0, 141);
            this.tableLayoutPanel19.TabIndex = 3;
            // 
            // uploadSkipCheckbox
            // 
            this.uploadSkipCheckbox.Anchor = System.Windows.Forms.AnchorStyles.Left;
            this.uploadSkipCheckbox.AutoSize = true;
            this.uploadSkipCheckbox.Location = new System.Drawing.Point(8, 10);
            this.uploadSkipCheckbox.Name = "uploadSkipCheckbox";
            this.uploadSkipCheckbox.Size = new System.Drawing.Size(1, 19);
            this.uploadSkipCheckbox.TabIndex = 0;
            this.uploadSkipCheckbox.Text = "Skip files matching the regular expression:";
            this.uploadSkipCheckbox.UseVisualStyleBackColor = true;
            this.uploadSkipCheckbox.CheckedChanged += new System.EventHandler(this.uploadSkipCheckbox_CheckedChanged);
            // 
            // uploadSkipRegexRichTextbox
            // 
            this.tableLayoutPanel19.SetColumnSpan(this.uploadSkipRegexRichTextbox, 2);
            this.uploadSkipRegexRichTextbox.Dock = System.Windows.Forms.DockStyle.Fill;
            this.uploadSkipRegexRichTextbox.Font = new System.Drawing.Font("Courier New", 8.25F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.uploadSkipRegexRichTextbox.Location = new System.Drawing.Point(8, 37);
            this.uploadSkipRegexRichTextbox.Name = "uploadSkipRegexRichTextbox";
            this.uploadSkipRegexRichTextbox.Size = new System.Drawing.Size(1, 96);
            this.uploadSkipRegexRichTextbox.TabIndex = 1;
            this.uploadSkipRegexRichTextbox.Text = "";
            this.uploadSkipRegexRichTextbox.TextChanged += new System.EventHandler(this.uploadSkipRegexRichTextbox_TextChanged);
            // 
            // uploadSkipRegexDefaultButton
            // 
            this.uploadSkipRegexDefaultButton.AutoSize = true;
            this.uploadSkipRegexDefaultButton.AutoSizeMode = System.Windows.Forms.AutoSizeMode.GrowAndShrink;
            this.uploadSkipRegexDefaultButton.Font = new System.Drawing.Font("Microsoft Sans Serif", 7F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.uploadSkipRegexDefaultButton.Location = new System.Drawing.Point(-48, 8);
            this.uploadSkipRegexDefaultButton.Name = "uploadSkipRegexDefaultButton";
            this.uploadSkipRegexDefaultButton.Size = new System.Drawing.Size(51, 23);
            this.uploadSkipRegexDefaultButton.TabIndex = 2;
            this.uploadSkipRegexDefaultButton.Text = "Default";
            this.uploadSkipRegexDefaultButton.UseVisualStyleBackColor = true;
            this.uploadSkipRegexDefaultButton.Click += new System.EventHandler(this.uploadSkipRegexDefaultButton_Click);
            // 
            // managedGoogleCloudPanel
            // 
            this.managedGoogleCloudPanel.Controls.Add(this.tableLayoutPanel45);
            this.managedGoogleCloudPanel.Location = new System.Drawing.Point(0, 0);
            this.managedGoogleCloudPanel.Name = "managedGoogleCloudPanel";
            this.managedGoogleCloudPanel.Size = new System.Drawing.Size(0, 0);
            this.managedGoogleCloudPanel.Text = "managedGoogleCloudPanel";
            // 
            // tableLayoutPanel45
            // 
            this.tableLayoutPanel45.AutoSize = true;
            this.tableLayoutPanel45.ColumnCount = 1;
            this.tableLayoutPanel45.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle(System.Windows.Forms.SizeType.Percent, 100F));
            this.tableLayoutPanel45.Controls.Add(this.groupBox24, 0, 0);
            this.tableLayoutPanel45.Controls.Add(this.groupBox30, 0, 1);
            this.tableLayoutPanel45.Controls.Add(this.groupBox32, 0, 4);
            this.tableLayoutPanel45.Dock = System.Windows.Forms.DockStyle.Fill;
            this.tableLayoutPanel45.Location = new System.Drawing.Point(0, 0);
            this.tableLayoutPanel45.Name = "tableLayoutPanel45";
            this.tableLayoutPanel45.RowCount = 5;
            this.tableLayoutPanel45.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel45.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel45.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel45.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel45.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel45.Size = new System.Drawing.Size(0, 0);
            this.tableLayoutPanel45.TabIndex = 1;
            // 
            // groupBox24
            // 
            this.groupBox24.AutoSize = true;
            this.groupBox24.Controls.Add(this.tableLayoutPanel46);
            this.groupBox24.Dock = System.Windows.Forms.DockStyle.Fill;
            this.groupBox24.Location = new System.Drawing.Point(3, 3);
            this.groupBox24.Name = "groupBox24";
            this.groupBox24.Size = new System.Drawing.Size(1, 81);
            this.groupBox24.TabIndex = 0;
            this.groupBox24.TabStop = false;
            this.groupBox24.Text = "Default Bucket Location";
            // 
            // tableLayoutPanel46
            // 
            this.tableLayoutPanel46.AutoSize = true;
            this.tableLayoutPanel46.ColumnCount = 1;
            this.tableLayoutPanel46.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle(System.Windows.Forms.SizeType.Absolute, 738F));
            this.tableLayoutPanel46.Controls.Add(this.label6, 0, 0);
            this.tableLayoutPanel46.Controls.Add(this.defaultGoogleLocationComboBox, 0, 1);
            this.tableLayoutPanel46.Dock = System.Windows.Forms.DockStyle.Fill;
            this.tableLayoutPanel46.Location = new System.Drawing.Point(3, 19);
            this.tableLayoutPanel46.Name = "tableLayoutPanel46";
            this.tableLayoutPanel46.Padding = new System.Windows.Forms.Padding(5);
            this.tableLayoutPanel46.RowCount = 2;
            this.tableLayoutPanel46.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel46.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel46.Size = new System.Drawing.Size(0, 59);
            this.tableLayoutPanel46.TabIndex = 2;
            // 
            // label6
            // 
            this.label6.AutoSize = true;
            this.label6.ForeColor = System.Drawing.SystemColors.GrayText;
            this.label6.Location = new System.Drawing.Point(8, 5);
            this.label6.Name = "label6";
            this.label6.Padding = new System.Windows.Forms.Padding(0, 0, 0, 5);
            this.label6.Size = new System.Drawing.Size(365, 20);
            this.label6.TabIndex = 0;
            this.label6.Text = "The geographic location in which buckets will be created by default.";
            // 
            // defaultGoogleLocationComboBox
            // 
            this.defaultGoogleLocationComboBox.DropDownStyle = System.Windows.Forms.ComboBoxStyle.DropDownList;
            this.defaultGoogleLocationComboBox.FormattingEnabled = true;
            this.defaultGoogleLocationComboBox.Location = new System.Drawing.Point(8, 28);
            this.defaultGoogleLocationComboBox.Name = "defaultGoogleLocationComboBox";
            this.defaultGoogleLocationComboBox.Size = new System.Drawing.Size(279, 23);
            this.defaultGoogleLocationComboBox.TabIndex = 1;
            this.defaultGoogleLocationComboBox.SelectionChangeCommitted += new System.EventHandler(this.defaultGoogleLocationComboBox_SelectionChangeCommitted);
            // 
            // groupBox30
            // 
            this.groupBox30.AutoSize = true;
            this.groupBox30.Controls.Add(this.tableLayoutPanel47);
            this.groupBox30.Dock = System.Windows.Forms.DockStyle.Fill;
            this.groupBox30.Location = new System.Drawing.Point(3, 90);
            this.groupBox30.Name = "groupBox30";
            this.groupBox30.Size = new System.Drawing.Size(1, 90);
            this.groupBox30.TabIndex = 1;
            this.groupBox30.TabStop = false;
            this.groupBox30.Text = "Default Storage Class";
            // 
            // tableLayoutPanel47
            // 
            this.tableLayoutPanel47.AutoSize = true;
            this.tableLayoutPanel47.ColumnCount = 1;
            this.tableLayoutPanel47.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle(System.Windows.Forms.SizeType.Absolute, 738F));
            this.tableLayoutPanel47.Controls.Add(this.defaultGoogleStorageClassComboBox, 0, 1);
            this.tableLayoutPanel47.Controls.Add(this.label9, 0, 0);
            this.tableLayoutPanel47.Dock = System.Windows.Forms.DockStyle.Fill;
            this.tableLayoutPanel47.Location = new System.Drawing.Point(3, 19);
            this.tableLayoutPanel47.Name = "tableLayoutPanel47";
            this.tableLayoutPanel47.Padding = new System.Windows.Forms.Padding(5);
            this.tableLayoutPanel47.RowCount = 2;
            this.tableLayoutPanel47.RowStyles.Add(new System.Windows.Forms.RowStyle(System.Windows.Forms.SizeType.Percent, 50F));
            this.tableLayoutPanel47.RowStyles.Add(new System.Windows.Forms.RowStyle(System.Windows.Forms.SizeType.Percent, 50F));
            this.tableLayoutPanel47.Size = new System.Drawing.Size(0, 68);
            this.tableLayoutPanel47.TabIndex = 0;
            // 
            // defaultGoogleStorageClassComboBox
            // 
            this.defaultGoogleStorageClassComboBox.DropDownStyle = System.Windows.Forms.ComboBoxStyle.DropDownList;
            this.defaultGoogleStorageClassComboBox.FormattingEnabled = true;
            this.defaultGoogleStorageClassComboBox.Location = new System.Drawing.Point(8, 37);
            this.defaultGoogleStorageClassComboBox.Name = "defaultGoogleStorageClassComboBox";
            this.defaultGoogleStorageClassComboBox.Size = new System.Drawing.Size(279, 23);
            this.defaultGoogleStorageClassComboBox.TabIndex = 2;
            this.defaultGoogleStorageClassComboBox.SelectionChangeCommitted += new System.EventHandler(this.defaultGoogleStorageClassComboBox_SelectionChangeCommitted);
            // 
            // label9
            // 
            this.label9.AutoSize = true;
            this.label9.ForeColor = System.Drawing.SystemColors.GrayText;
            this.label9.Location = new System.Drawing.Point(8, 5);
            this.label9.Name = "label9";
            this.label9.Padding = new System.Windows.Forms.Padding(0, 0, 0, 5);
            this.label9.Size = new System.Drawing.Size(700, 20);
            this.label9.TabIndex = 0;
            this.label9.Text = "Choose Reduced Reduncancy Storage (RRS) to reduce costs by storing non-critical, " +
    "reproducible data at lower levels of redundancy.";
            // 
            // groupBox32
            // 
            this.groupBox32.Controls.Add(this.tableLayoutPanel49);
            this.groupBox32.Dock = System.Windows.Forms.DockStyle.Fill;
            this.groupBox32.Location = new System.Drawing.Point(3, 186);
            this.groupBox32.Name = "groupBox32";
            this.groupBox32.Size = new System.Drawing.Size(1, 294);
            this.groupBox32.TabIndex = 3;
            this.groupBox32.TabStop = false;
            this.groupBox32.Text = "Default ACL";
            // 
            // tableLayoutPanel49
            // 
            this.tableLayoutPanel49.AutoSize = true;
            this.tableLayoutPanel49.ColumnCount = 1;
            this.tableLayoutPanel49.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle(System.Windows.Forms.SizeType.Absolute, 738F));
            this.tableLayoutPanel49.Controls.Add(this.defaultGoogleACLComboBox, 0, 1);
            this.tableLayoutPanel49.Controls.Add(this.label17, 0, 0);
            this.tableLayoutPanel49.Dock = System.Windows.Forms.DockStyle.Fill;
            this.tableLayoutPanel49.Location = new System.Drawing.Point(3, 19);
            this.tableLayoutPanel49.Name = "tableLayoutPanel49";
            this.tableLayoutPanel49.Padding = new System.Windows.Forms.Padding(5);
            this.tableLayoutPanel49.RowCount = 2;
            this.tableLayoutPanel49.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel49.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel49.Size = new System.Drawing.Size(0, 272);
            this.tableLayoutPanel49.TabIndex = 0;
            // 
            // defaultGoogleACLComboBox
            // 
            this.defaultGoogleACLComboBox.DropDownStyle = System.Windows.Forms.ComboBoxStyle.DropDownList;
            this.defaultGoogleACLComboBox.FormattingEnabled = true;
            this.defaultGoogleACLComboBox.Location = new System.Drawing.Point(8, 28);
            this.defaultGoogleACLComboBox.Name = "defaultGoogleACLComboBox";
            this.defaultGoogleACLComboBox.Size = new System.Drawing.Size(279, 23);
            this.defaultGoogleACLComboBox.TabIndex = 4;
            this.defaultGoogleACLComboBox.SelectionChangeCommitted += new System.EventHandler(this.defaultGoogleACLComboBox_SelectionChangeCommitted);
            // 
            // label17
            // 
            this.label17.AutoSize = true;
            this.label17.ForeColor = System.Drawing.SystemColors.GrayText;
            this.label17.Location = new System.Drawing.Point(8, 5);
            this.label17.Name = "label17";
            this.label17.Padding = new System.Windows.Forms.Padding(0, 0, 0, 5);
            this.label17.Size = new System.Drawing.Size(475, 20);
            this.label17.TabIndex = 2;
            this.label17.Text = "Choose the predefined set of grantees and permissions to apply to new buckets and" +
    " files.";
            // 
            // managedCryptomatorPanel
            // 
            this.managedCryptomatorPanel.Controls.Add(this.tableLayoutPanel24);
            this.managedCryptomatorPanel.Location = new System.Drawing.Point(0, 0);
            this.managedCryptomatorPanel.Name = "managedCryptomatorPanel";
            this.managedCryptomatorPanel.Size = new System.Drawing.Size(0, 0);
            this.managedCryptomatorPanel.Text = "managedPanel1";
            // 
            // tableLayoutPanel24
            // 
            this.tableLayoutPanel24.ColumnCount = 1;
            this.tableLayoutPanel24.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle(System.Windows.Forms.SizeType.Percent, 100F));
            this.tableLayoutPanel24.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle(System.Windows.Forms.SizeType.Absolute, 20F));
            this.tableLayoutPanel24.Controls.Add(this.groupBox15, 0, 0);
            this.tableLayoutPanel24.Dock = System.Windows.Forms.DockStyle.Fill;
            this.tableLayoutPanel24.Location = new System.Drawing.Point(0, 0);
            this.tableLayoutPanel24.Name = "tableLayoutPanel24";
            this.tableLayoutPanel24.RowCount = 2;
            this.tableLayoutPanel24.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel24.RowStyles.Add(new System.Windows.Forms.RowStyle(System.Windows.Forms.SizeType.Percent, 100F));
            this.tableLayoutPanel24.Size = new System.Drawing.Size(0, 0);
            this.tableLayoutPanel24.TabIndex = 0;
            // 
            // groupBox15
            // 
            this.groupBox15.AutoSize = true;
            this.groupBox15.AutoSizeMode = System.Windows.Forms.AutoSizeMode.GrowAndShrink;
            this.groupBox15.Controls.Add(this.flowLayoutPanel1);
            this.groupBox15.Dock = System.Windows.Forms.DockStyle.Fill;
            this.groupBox15.Location = new System.Drawing.Point(3, 3);
            this.groupBox15.Name = "groupBox15";
            this.groupBox15.Size = new System.Drawing.Size(1, 57);
            this.groupBox15.TabIndex = 0;
            this.groupBox15.TabStop = false;
            this.groupBox15.Text = "Cryptomator";
            // 
            // flowLayoutPanel1
            // 
            this.flowLayoutPanel1.AutoSize = true;
            this.flowLayoutPanel1.Controls.Add(this.cryptomatorAutoDetectCheckBox);
            this.flowLayoutPanel1.Controls.Add(this.cryptomatorUseKeychain);
            this.flowLayoutPanel1.Dock = System.Windows.Forms.DockStyle.Fill;
            this.flowLayoutPanel1.FlowDirection = System.Windows.Forms.FlowDirection.TopDown;
            this.flowLayoutPanel1.Location = new System.Drawing.Point(3, 19);
            this.flowLayoutPanel1.Name = "flowLayoutPanel1";
            this.flowLayoutPanel1.Padding = new System.Windows.Forms.Padding(5, 5, 0, 5);
            this.flowLayoutPanel1.Size = new System.Drawing.Size(0, 35);
            this.flowLayoutPanel1.TabIndex = 0;
            // 
            // cryptomatorAutoDetectCheckBox
            // 
            this.cryptomatorAutoDetectCheckBox.AutoSize = true;
            this.cryptomatorAutoDetectCheckBox.Location = new System.Drawing.Point(8, 8);
            this.cryptomatorAutoDetectCheckBox.Name = "cryptomatorAutoDetectCheckBox";
            this.cryptomatorAutoDetectCheckBox.Size = new System.Drawing.Size(228, 19);
            this.cryptomatorAutoDetectCheckBox.TabIndex = 0;
            this.cryptomatorAutoDetectCheckBox.Text = "Auto detect and open vault in browser";
            this.cryptomatorAutoDetectCheckBox.UseVisualStyleBackColor = true;
            this.cryptomatorAutoDetectCheckBox.CheckedChanged += new System.EventHandler(this.cryptomatorAutoDetectCheckBox_CheckedChanged);
            // 
            // cryptomatorUseKeychain
            // 
            this.cryptomatorUseKeychain.AutoSize = true;
            this.cryptomatorUseKeychain.Location = new System.Drawing.Point(8, 33);
            this.cryptomatorUseKeychain.Name = "cryptomatorUseKeychain";
            this.cryptomatorUseKeychain.Size = new System.Drawing.Size(96, 19);
            this.cryptomatorUseKeychain.TabIndex = 1;
            this.cryptomatorUseKeychain.Text = "Use Keychain";
            this.cryptomatorUseKeychain.UseVisualStyleBackColor = true;
            this.cryptomatorUseKeychain.CheckedChanged += new System.EventHandler(this.cryptomatorUseKeychain_CheckedChanged);
            // 
            // managedProfilesPanel
            // 
            this.managedProfilesPanel.Controls.Add(this.profilesPageHost);
            this.managedProfilesPanel.Location = new System.Drawing.Point(0, 0);
            this.managedProfilesPanel.Name = "managedProfilesPanel";
            this.managedProfilesPanel.Size = new System.Drawing.Size(0, 0);
            this.managedProfilesPanel.Text = "managedPanel1";
            // 
            // profilesPageHost
            // 
            this.profilesPageHost.BackColorTransparent = true;
            this.profilesPageHost.Dock = System.Windows.Forms.DockStyle.Fill;
            this.profilesPageHost.Location = new System.Drawing.Point(0, 0);
            this.profilesPageHost.Name = "profilesPageHost";
            this.profilesPageHost.Size = new System.Drawing.Size(0, 0);
            this.profilesPageHost.TabIndex = 0;
            this.profilesPageHost.Text = "elementHost1";
            this.profilesPageHost.Child = null;
            // 
            // toolStrip
            // 
            this.toolStrip.CanOverflow = false;
            this.toolStrip.GripStyle = System.Windows.Forms.ToolStripGripStyle.Hidden;
            this.toolStrip.ImageScalingSize = new System.Drawing.Size(32, 32);
            this.toolStrip.Items.AddRange(new System.Windows.Forms.ToolStripItem[] {
            this.generalButton,
            this.browserButton,
            this.transfersButton,
            this.editStripButton,
            this.profilesButton,
            this.ftpButton,
            this.s3Button,
            this.googleCloudButton,
            this.bandwidthButton,
            this.connectionButton,
            this.cryptomatorButton,
            this.updateButton,
            this.languageButton});
            this.toolStrip.LayoutStyle = System.Windows.Forms.ToolStripLayoutStyle.HorizontalStackWithOverflow;
            this.toolStrip.Location = new System.Drawing.Point(0, 0);
            this.toolStrip.MinimumSize = new System.Drawing.Size(0, 69);
            this.toolStrip.Name = "toolStrip";
            this.toolStrip.Size = new System.Drawing.Size(784, 69);
            this.toolStrip.TabIndex = 0;
            this.toolStrip.Text = "toolStrip1";
            // 
            // generalButton
            // 
            this.generalButton.AutoToolTip = false;
            this.generalButton.ImageAlign = System.Drawing.ContentAlignment.BottomCenter;
            this.generalButton.ImageTransparentColor = System.Drawing.Color.Magenta;
            this.generalButton.Margin = new System.Windows.Forms.Padding(5, 0, 5, 0);
            this.generalButton.Name = "generalButton";
            this.generalButton.Size = new System.Drawing.Size(51, 69);
            this.generalButton.Text = "General";
            this.generalButton.TextImageRelation = System.Windows.Forms.TextImageRelation.ImageAboveText;
            this.generalButton.Click += new System.EventHandler(this.generalButton_Click);
            // 
            // browserButton
            // 
            this.browserButton.ImageAlign = System.Drawing.ContentAlignment.BottomCenter;
            this.browserButton.ImageTransparentColor = System.Drawing.Color.Magenta;
            this.browserButton.Margin = new System.Windows.Forms.Padding(5, 0, 5, 0);
            this.browserButton.Name = "browserButton";
            this.browserButton.Size = new System.Drawing.Size(53, 69);
            this.browserButton.Text = "Browser";
            this.browserButton.TextImageRelation = System.Windows.Forms.TextImageRelation.ImageAboveText;
            this.browserButton.Click += new System.EventHandler(this.browserButton_Click);
            // 
            // transfersButton
            // 
            this.transfersButton.AutoToolTip = false;
            this.transfersButton.ImageAlign = System.Drawing.ContentAlignment.BottomCenter;
            this.transfersButton.ImageTransparentColor = System.Drawing.Color.Magenta;
            this.transfersButton.Margin = new System.Windows.Forms.Padding(5, 0, 5, 0);
            this.transfersButton.Name = "transfersButton";
            this.transfersButton.Size = new System.Drawing.Size(57, 69);
            this.transfersButton.Text = "Transfers";
            this.transfersButton.TextImageRelation = System.Windows.Forms.TextImageRelation.ImageAboveText;
            this.transfersButton.Click += new System.EventHandler(this.transfersButton_Click);
            // 
            // editStripButton
            // 
            this.editStripButton.AutoToolTip = false;
            this.editStripButton.ImageAlign = System.Drawing.ContentAlignment.BottomCenter;
            this.editStripButton.ImageTransparentColor = System.Drawing.Color.Magenta;
            this.editStripButton.Margin = new System.Windows.Forms.Padding(5, 0, 5, 0);
            this.editStripButton.Name = "editStripButton";
            this.editStripButton.Size = new System.Drawing.Size(42, 69);
            this.editStripButton.Text = "Editor";
            this.editStripButton.TextImageRelation = System.Windows.Forms.TextImageRelation.ImageAboveText;
            this.editStripButton.Click += new System.EventHandler(this.editStripButton_Click);
            // 
            // profilesButton
            // 
            this.profilesButton.AutoToolTip = false;
            this.profilesButton.ImageAlign = System.Drawing.ContentAlignment.BottomCenter;
            this.profilesButton.ImageTransparentColor = System.Drawing.Color.Magenta;
            this.profilesButton.Name = "profilesButton";
            this.profilesButton.Size = new System.Drawing.Size(50, 66);
            this.profilesButton.Text = "Profiles";
            this.profilesButton.TextImageRelation = System.Windows.Forms.TextImageRelation.ImageAboveText;
            this.profilesButton.Click += new System.EventHandler(this.profilesButton_Click);
            // 
            // ftpButton
            // 
            this.ftpButton.AutoToolTip = false;
            this.ftpButton.ImageAlign = System.Drawing.ContentAlignment.BottomCenter;
            this.ftpButton.ImageTransparentColor = System.Drawing.Color.Magenta;
            this.ftpButton.Margin = new System.Windows.Forms.Padding(5, 0, 5, 0);
            this.ftpButton.Name = "ftpButton";
            this.ftpButton.Size = new System.Drawing.Size(30, 69);
            this.ftpButton.Text = "FTP";
            this.ftpButton.TextImageRelation = System.Windows.Forms.TextImageRelation.ImageAboveText;
            this.ftpButton.Click += new System.EventHandler(this.ftpButton_Click);
            // 
            // s3Button
            // 
            this.s3Button.AutoToolTip = false;
            this.s3Button.ImageAlign = System.Drawing.ContentAlignment.BottomCenter;
            this.s3Button.ImageTransparentColor = System.Drawing.Color.Magenta;
            this.s3Button.Margin = new System.Windows.Forms.Padding(5, 0, 5, 0);
            this.s3Button.Name = "s3Button";
            this.s3Button.Size = new System.Drawing.Size(23, 69);
            this.s3Button.Text = "S3";
            this.s3Button.TextImageRelation = System.Windows.Forms.TextImageRelation.ImageAboveText;
            this.s3Button.Click += new System.EventHandler(this.s3Button_Click);
            // 
            // googleCloudButton
            // 
            this.googleCloudButton.ImageAlign = System.Drawing.ContentAlignment.BottomCenter;
            this.googleCloudButton.ImageTransparentColor = System.Drawing.Color.Magenta;
            this.googleCloudButton.Name = "googleCloudButton";
            this.googleCloudButton.Size = new System.Drawing.Size(127, 66);
            this.googleCloudButton.Text = "Google Cloud Storage";
            this.googleCloudButton.TextImageRelation = System.Windows.Forms.TextImageRelation.ImageAboveText;
            this.googleCloudButton.Click += new System.EventHandler(this.googleCloudButton_Click);
            // 
            // bandwidthButton
            // 
            this.bandwidthButton.AutoToolTip = false;
            this.bandwidthButton.ImageAlign = System.Drawing.ContentAlignment.BottomCenter;
            this.bandwidthButton.ImageTransparentColor = System.Drawing.Color.Magenta;
            this.bandwidthButton.Margin = new System.Windows.Forms.Padding(5, 0, 5, 0);
            this.bandwidthButton.Name = "bandwidthButton";
            this.bandwidthButton.Size = new System.Drawing.Size(68, 69);
            this.bandwidthButton.Text = "Bandwidth";
            this.bandwidthButton.TextImageRelation = System.Windows.Forms.TextImageRelation.ImageAboveText;
            this.bandwidthButton.Click += new System.EventHandler(this.bandwidthButton_Click);
            // 
            // connectionButton
            // 
            this.connectionButton.ImageAlign = System.Drawing.ContentAlignment.BottomCenter;
            this.connectionButton.ImageTransparentColor = System.Drawing.Color.Magenta;
            this.connectionButton.Margin = new System.Windows.Forms.Padding(5, 0, 5, 0);
            this.connectionButton.Name = "connectionButton";
            this.connectionButton.Size = new System.Drawing.Size(73, 69);
            this.connectionButton.Text = "Connection";
            this.connectionButton.TextImageRelation = System.Windows.Forms.TextImageRelation.ImageAboveText;
            this.connectionButton.Click += new System.EventHandler(this.connectionButton_Click);
            // 
            // cryptomatorButton
            // 
            this.cryptomatorButton.ImageAlign = System.Drawing.ContentAlignment.BottomCenter;
            this.cryptomatorButton.ImageTransparentColor = System.Drawing.Color.Magenta;
            this.cryptomatorButton.Margin = new System.Windows.Forms.Padding(5, 0, 5, 0);
            this.cryptomatorButton.Name = "cryptomatorButton";
            this.cryptomatorButton.Overflow = System.Windows.Forms.ToolStripItemOverflow.Never;
            this.cryptomatorButton.Size = new System.Drawing.Size(79, 69);
            this.cryptomatorButton.Text = "Cryptomator";
            this.cryptomatorButton.TextImageRelation = System.Windows.Forms.TextImageRelation.ImageAboveText;
            this.cryptomatorButton.Click += new System.EventHandler(this.cryptomatorButton_Click);
            // 
            // updateButton
            // 
            this.updateButton.ImageAlign = System.Drawing.ContentAlignment.BottomCenter;
            this.updateButton.ImageTransparentColor = System.Drawing.Color.Magenta;
            this.updateButton.Margin = new System.Windows.Forms.Padding(5, 0, 5, 0);
            this.updateButton.Name = "updateButton";
            this.updateButton.Overflow = System.Windows.Forms.ToolStripItemOverflow.Never;
            this.updateButton.Size = new System.Drawing.Size(49, 69);
            this.updateButton.Text = "Update";
            this.updateButton.TextImageRelation = System.Windows.Forms.TextImageRelation.ImageAboveText;
            this.updateButton.Click += new System.EventHandler(this.updateButton_Click);
            // 
            // languageButton
            // 
            this.languageButton.ImageAlign = System.Drawing.ContentAlignment.BottomCenter;
            this.languageButton.ImageTransparentColor = System.Drawing.Color.Magenta;
            this.languageButton.Margin = new System.Windows.Forms.Padding(5, 0, 5, 0);
            this.languageButton.Name = "languageButton";
            this.languageButton.Overflow = System.Windows.Forms.ToolStripItemOverflow.Never;
            this.languageButton.Size = new System.Drawing.Size(63, 69);
            this.languageButton.Text = "Language";
            this.languageButton.TextImageRelation = System.Windows.Forms.TextImageRelation.ImageAboveText;
            this.languageButton.Click += new System.EventHandler(this.languageButton_Click);
            // 
            // PreferencesForm
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(7F, 15F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.AutoSize = true;
            this.ClientSize = new System.Drawing.Size(784, 591);
            this.Controls.Add(this.panelManager);
            this.Controls.Add(this.toolStrip);
            this.FormBorderStyle = System.Windows.Forms.FormBorderStyle.FixedSingle;
            this.MaximizeBox = false;
            this.Name = "PreferencesForm";
            this.Text = "Preferences";
            this.panelManager.ResumeLayout(false);
            this.managedGeneralPanel.ResumeLayout(false);
            this.managedGeneralPanel.PerformLayout();
            this.tableLayoutPanelGeneral.ResumeLayout(false);
            this.tableLayoutPanelGeneral.PerformLayout();
            this.groupBox13.ResumeLayout(false);
            this.groupBox13.PerformLayout();
            this.tableLayoutPanel22.ResumeLayout(false);
            this.tableLayoutPanel22.PerformLayout();
            this.browserGroupbox.ResumeLayout(false);
            this.browserGroupbox.PerformLayout();
            this.tableLayoutPanel1.ResumeLayout(false);
            this.tableLayoutPanel1.PerformLayout();
            this.managedFtpPanel.ResumeLayout(false);
            this.managedFtpPanel.PerformLayout();
            this.tableLayoutPanel25.ResumeLayout(false);
            this.tableLayoutPanel25.PerformLayout();
            this.groupBox4.ResumeLayout(false);
            this.groupBox4.PerformLayout();
            this.tableLayoutPanel38.ResumeLayout(false);
            this.tableLayoutPanel38.PerformLayout();
            this.managedS3Panel.ResumeLayout(false);
            this.managedS3Panel.PerformLayout();
            this.tableLayoutPanel28.ResumeLayout(false);
            this.tableLayoutPanel28.PerformLayout();
            this.groupBox19.ResumeLayout(false);
            this.groupBox19.PerformLayout();
            this.tableLayoutPanel29.ResumeLayout(false);
            this.tableLayoutPanel29.PerformLayout();
            this.groupBox22.ResumeLayout(false);
            this.groupBox22.PerformLayout();
            this.tableLayoutPanel30.ResumeLayout(false);
            this.tableLayoutPanel30.PerformLayout();
            this.groupBox3.ResumeLayout(false);
            this.groupBox3.PerformLayout();
            this.tableLayoutPanel20.ResumeLayout(false);
            this.tableLayoutPanel20.PerformLayout();
            this.groupBox23.ResumeLayout(false);
            this.groupBox23.PerformLayout();
            this.tableLayoutPanel33.ResumeLayout(false);
            this.tableLayoutPanel33.PerformLayout();
            this.managedBandwidthPanel.ResumeLayout(false);
            this.managedBandwidthPanel.PerformLayout();
            this.tableLayoutPanel34.ResumeLayout(false);
            this.tableLayoutPanel34.PerformLayout();
            this.groupBox25.ResumeLayout(false);
            this.groupBox25.PerformLayout();
            this.tableLayoutPanel35.ResumeLayout(false);
            this.tableLayoutPanel35.PerformLayout();
            this.groupBox26.ResumeLayout(false);
            this.groupBox26.PerformLayout();
            this.tableLayoutPanel36.ResumeLayout(false);
            this.tableLayoutPanel36.PerformLayout();
            this.managedConnectionPanel.ResumeLayout(false);
            this.managedConnectionPanel.PerformLayout();
            this.tableLayoutPanel37.ResumeLayout(false);
            this.tableLayoutPanel37.PerformLayout();
            this.groupBox1.ResumeLayout(false);
            this.groupBox1.PerformLayout();
            this.tableLayoutPanel2.ResumeLayout(false);
            this.tableLayoutPanel2.PerformLayout();
            this.groupBox31.ResumeLayout(false);
            this.groupBox31.PerformLayout();
            this.tableLayoutPanel48.ResumeLayout(false);
            this.tableLayoutPanel48.PerformLayout();
            this.groupBox21.ResumeLayout(false);
            this.groupBox21.PerformLayout();
            this.tableLayoutPanel39.ResumeLayout(false);
            this.tableLayoutPanel39.PerformLayout();
            ((System.ComponentModel.ISupportInitialize)(this.connectionTimeoutUpDown)).EndInit();
            ((System.ComponentModel.ISupportInitialize)(this.retriesUpDown)).EndInit();
            ((System.ComponentModel.ISupportInitialize)(this.retryDelayUpDown)).EndInit();
            this.groupBox20.ResumeLayout(false);
            this.groupBox20.PerformLayout();
            this.tableLayoutPanel40.ResumeLayout(false);
            this.tableLayoutPanel40.PerformLayout();
            this.managedUpdatePanel.ResumeLayout(false);
            this.tableLayoutPanel41.ResumeLayout(false);
            this.tableLayoutPanel41.PerformLayout();
            this.groupBox27.ResumeLayout(false);
            this.groupBox27.PerformLayout();
            this.tableLayoutPanel42.ResumeLayout(false);
            this.tableLayoutPanel42.PerformLayout();
            this.managedLanguagePanel.ResumeLayout(false);
            this.managedLanguagePanel.PerformLayout();
            this.tableLayoutPanel43.ResumeLayout(false);
            this.tableLayoutPanel43.PerformLayout();
            this.groupBox28.ResumeLayout(false);
            this.groupBox28.PerformLayout();
            this.tableLayoutPanel44.ResumeLayout(false);
            this.tableLayoutPanel44.PerformLayout();
            this.managedEditorPanel.ResumeLayout(false);
            this.tableLayoutPanel3.ResumeLayout(false);
            this.groupBox2.ResumeLayout(false);
            this.tableLayoutPanel4.ResumeLayout(false);
            this.tableLayoutPanel4.PerformLayout();
            this.managedBrowserPanel.ResumeLayout(false);
            this.managedBrowserPanel.PerformLayout();
            this.tableLayoutPanel21.ResumeLayout(false);
            this.tableLayoutPanel21.PerformLayout();
            this.groupBox18.ResumeLayout(false);
            this.groupBox18.PerformLayout();
            this.tableLayoutPanel27.ResumeLayout(false);
            this.tableLayoutPanel27.PerformLayout();
            this.groupBox14.ResumeLayout(false);
            this.groupBox14.PerformLayout();
            this.tableLayoutPanel23.ResumeLayout(false);
            this.tableLayoutPanel23.PerformLayout();
            this.managedTransfersPanel.ResumeLayout(false);
            this.transfersTabControl.ResumeLayout(false);
            this.tabPage1.ResumeLayout(false);
            this.tabPage1.PerformLayout();
            this.tableLayoutPanel5.ResumeLayout(false);
            this.tableLayoutPanel5.PerformLayout();
            this.groupBox6.ResumeLayout(false);
            this.groupBox6.PerformLayout();
            this.tableLayoutPanel8.ResumeLayout(false);
            this.tableLayoutPanel8.PerformLayout();
            this.groupBox5.ResumeLayout(false);
            this.groupBox5.PerformLayout();
            this.tableLayoutPanel7.ResumeLayout(false);
            this.tableLayoutPanel7.PerformLayout();
            this.groupBox29.ResumeLayout(false);
            this.groupBox29.PerformLayout();
            this.tableLayoutPanel6.ResumeLayout(false);
            this.tableLayoutPanel6.PerformLayout();
            this.tabPage2.ResumeLayout(false);
            this.tabPage2.PerformLayout();
            this.tableLayoutPanel9.ResumeLayout(false);
            this.tableLayoutPanel9.PerformLayout();
            this.groupBox7.ResumeLayout(false);
            this.groupBox7.PerformLayout();
            this.tableLayoutPanel10.ResumeLayout(false);
            this.tableLayoutPanel10.PerformLayout();
            this.tableLayoutPanel11.ResumeLayout(false);
            this.tableLayoutPanel11.PerformLayout();
            this.groupBox8.ResumeLayout(false);
            this.groupBox8.PerformLayout();
            this.tableLayoutPanel12.ResumeLayout(false);
            this.tableLayoutPanel12.PerformLayout();
            this.tableLayoutPanel13.ResumeLayout(false);
            this.tableLayoutPanel13.PerformLayout();
            this.tabPage3.ResumeLayout(false);
            this.tabPage3.PerformLayout();
            this.tableLayoutPanel14.ResumeLayout(false);
            this.tableLayoutPanel14.PerformLayout();
            this.groupBox9.ResumeLayout(false);
            this.groupBox9.PerformLayout();
            this.tableLayoutPanel15.ResumeLayout(false);
            this.tableLayoutPanel15.PerformLayout();
            this.groupBox10.ResumeLayout(false);
            this.groupBox10.PerformLayout();
            this.tableLayoutPanel16.ResumeLayout(false);
            this.tableLayoutPanel16.PerformLayout();
            this.tabPage5.ResumeLayout(false);
            this.tabPage5.PerformLayout();
            this.tableLayoutPanel26.ResumeLayout(false);
            this.tableLayoutPanel26.PerformLayout();
            this.groupBox16.ResumeLayout(false);
            this.groupBox16.PerformLayout();
            this.tableLayoutPanel31.ResumeLayout(false);
            this.tableLayoutPanel31.PerformLayout();
            this.groupBox17.ResumeLayout(false);
            this.groupBox17.PerformLayout();
            this.tableLayoutPanel32.ResumeLayout(false);
            this.tableLayoutPanel32.PerformLayout();
            this.tabPage4.ResumeLayout(false);
            this.tabPage4.PerformLayout();
            this.tableLayoutPanel17.ResumeLayout(false);
            this.tableLayoutPanel17.PerformLayout();
            this.groupBox11.ResumeLayout(false);
            this.groupBox11.PerformLayout();
            this.tableLayoutPanel18.ResumeLayout(false);
            this.tableLayoutPanel18.PerformLayout();
            this.groupBox12.ResumeLayout(false);
            this.groupBox12.PerformLayout();
            this.tableLayoutPanel19.ResumeLayout(false);
            this.tableLayoutPanel19.PerformLayout();
            this.managedGoogleCloudPanel.ResumeLayout(false);
            this.managedGoogleCloudPanel.PerformLayout();
            this.tableLayoutPanel45.ResumeLayout(false);
            this.tableLayoutPanel45.PerformLayout();
            this.groupBox24.ResumeLayout(false);
            this.groupBox24.PerformLayout();
            this.tableLayoutPanel46.ResumeLayout(false);
            this.tableLayoutPanel46.PerformLayout();
            this.groupBox30.ResumeLayout(false);
            this.groupBox30.PerformLayout();
            this.tableLayoutPanel47.ResumeLayout(false);
            this.tableLayoutPanel47.PerformLayout();
            this.groupBox32.ResumeLayout(false);
            this.groupBox32.PerformLayout();
            this.tableLayoutPanel49.ResumeLayout(false);
            this.tableLayoutPanel49.PerformLayout();
            this.managedCryptomatorPanel.ResumeLayout(false);
            this.tableLayoutPanel24.ResumeLayout(false);
            this.tableLayoutPanel24.PerformLayout();
            this.groupBox15.ResumeLayout(false);
            this.groupBox15.PerformLayout();
            this.flowLayoutPanel1.ResumeLayout(false);
            this.flowLayoutPanel1.PerformLayout();
            this.managedProfilesPanel.ResumeLayout(false);
            this.toolStrip.ResumeLayout(false);
            this.toolStrip.PerformLayout();
            this.ResumeLayout(false);
            this.PerformLayout();

        }

        #endregion

        private System.Windows.Forms.ToolStripButton generalButton;
        private System.Windows.Forms.GroupBox browserGroupbox;
        private System.Windows.Forms.CheckBox saveWorkspaceCheckbox;
        private System.Windows.Forms.Label labelOpenEmtpyBrowser;
        private System.Windows.Forms.ToolStripButton transfersButton;
        private System.Windows.Forms.ToolStripButton ftpButton;
        private System.Windows.Forms.ToolStripButton s3Button;
        private System.Windows.Forms.ToolStripButton bandwidthButton;
        private System.Windows.Forms.ToolStripButton connectionButton;
        private System.Windows.Forms.ImageList iconList;
        private System.Windows.Forms.TabControl transfersTabControl;
        private System.Windows.Forms.TabPage tabPage1;
        private System.Windows.Forms.TabPage tabPage2;
        private System.Windows.Forms.TabPage tabPage3;
        private System.Windows.Forms.TabPage tabPage4;
        private System.Windows.Forms.ComboBox transferFilesCombobox;
        private System.Windows.Forms.CheckBox transfersToFrontCheckbox;
        private System.Windows.Forms.CheckBox transfersToBackCheckbox;
        private System.Windows.Forms.CheckBox removeFromTransfersCheckbox;
        private System.Windows.Forms.GroupBox groupBox5;
        private System.Windows.Forms.CheckBox openAfterDownloadCheckbox;
        private System.Windows.Forms.Label label11;
        private System.Windows.Forms.ComboBox duplicateDownloadCombobox;
        private System.Windows.Forms.Label label10;
        private System.Windows.Forms.CheckBox duplicateDownloadOverwriteCheckbox;
        private System.Windows.Forms.GroupBox groupBox6;
        private System.Windows.Forms.Label label12;
        private System.Windows.Forms.ComboBox duplicateUploadCombobox;
        private System.Windows.Forms.CheckBox duplicateUploadOverwriteCheckbox;
        private System.Windows.Forms.Button showDownloadFolderDialogButton;
        private System.Windows.Forms.FolderBrowserDialog downloadFolderBrowserDialog;
        private EllipsisLabel downloadFolderLabel;
        private System.Windows.Forms.GroupBox groupBox7;
        private System.Windows.Forms.CheckBox chmodDownloadCheckbox;
        private System.Windows.Forms.RadioButton chmodDownloadCustomRadioButton;
        private System.Windows.Forms.RadioButton chmodDownloadDefaultRadioButton;
        private System.Windows.Forms.Label ownerDownloadLabel;
        private System.Windows.Forms.ComboBox chmodDownloadTypeCombobox;
        private System.Windows.Forms.CheckBox dotherxCheckbox;
        private System.Windows.Forms.CheckBox dotherwCheckbox;
        private System.Windows.Forms.CheckBox dotherrCheckbox;
        private System.Windows.Forms.Label othersDownloadLabel;
        private System.Windows.Forms.CheckBox dgroupxCheckbox;
        private System.Windows.Forms.CheckBox dgroupwCheckbox;
        private System.Windows.Forms.CheckBox dgrouprCheckbox;
        private System.Windows.Forms.Label groupDownloadLabel;
        private System.Windows.Forms.CheckBox downerxCheckbox;
        private System.Windows.Forms.CheckBox downerwCheckbox;
        private System.Windows.Forms.CheckBox downerrCheckbox;
        private System.Windows.Forms.GroupBox groupBox8;
        private System.Windows.Forms.CheckBox uotherxCheckbox;
        private System.Windows.Forms.CheckBox uotherwCheckbox;
        private System.Windows.Forms.CheckBox uotherrCheckbox;
        private System.Windows.Forms.Label othersUploadLabel;
        private System.Windows.Forms.CheckBox ugroupxCheckbox;
        private System.Windows.Forms.CheckBox ugroupwCheckbox;
        private System.Windows.Forms.CheckBox ugrouprCheckbox;
        private System.Windows.Forms.Label groupUploadLabel;
        private System.Windows.Forms.CheckBox uownerxCheckbox;
        private System.Windows.Forms.CheckBox uownerwCheckbox;
        private System.Windows.Forms.CheckBox uownerrCheckbox;
        private System.Windows.Forms.Label ownerUploadLabel;
        private System.Windows.Forms.ComboBox chmodUploadTypeCombobox;
        private System.Windows.Forms.RadioButton chmodUploadDefaultRadioButton;
        private System.Windows.Forms.RadioButton chmodUploadCustomRadioButton;
        private System.Windows.Forms.CheckBox chmodUploadCheckbox;
        private System.Windows.Forms.GroupBox groupBox10;
        private System.Windows.Forms.CheckBox preserveModificationUploadCheckbox;
        private System.Windows.Forms.GroupBox groupBox9;
        private System.Windows.Forms.CheckBox preserveModificationDownloadCheckbox;
        private System.Windows.Forms.GroupBox groupBox11;
        private System.Windows.Forms.CheckBox downloadSkipCheckbox;
        private System.Windows.Forms.RichTextBox downloadSkipRegexRichTextbox;
        private System.Windows.Forms.Button downloadSkipRegexDefaultButton;
        private System.Windows.Forms.GroupBox groupBox12;
        private System.Windows.Forms.Button uploadSkipRegexDefaultButton;
        private System.Windows.Forms.RichTextBox uploadSkipRegexRichTextbox;
        private System.Windows.Forms.CheckBox uploadSkipCheckbox;
        private System.Windows.Forms.GroupBox groupBox19;
        private System.Windows.Forms.Label label29;
        private System.Windows.Forms.ComboBox defaultBucketLocationCombobox;
        private System.Windows.Forms.Label label31;
        private System.Windows.Forms.ComboBox defaultDownloadThrottleCombobox;
        private System.Windows.Forms.Label label32;
        private System.Windows.Forms.ComboBox defaultUploadThrottleCombobox;
        private System.Windows.Forms.GroupBox groupBox21;
        private System.Windows.Forms.NumericUpDown connectionTimeoutUpDown;
        private System.Windows.Forms.Label label36;
        private System.Windows.Forms.CheckBox retryCheckbox;
        private System.Windows.Forms.NumericUpDown retryDelayUpDown;
        private System.Windows.Forms.NumericUpDown retriesUpDown;
        private System.Windows.Forms.Label label34;
        private PanelManager panelManager;
        private ManagedPanel managedGeneralPanel;
        private ManagedPanel managedTransfersPanel;
        private ManagedPanel managedFtpPanel;
        private ManagedPanel managedS3Panel;
        private ManagedPanel managedBandwidthPanel;
        private ManagedPanel managedConnectionPanel;
        private System.Windows.Forms.TableLayoutPanel tableLayoutPanel1;
        private System.Windows.Forms.TableLayoutPanel tableLayoutPanelGeneral;
        private System.Windows.Forms.TableLayoutPanel tableLayoutPanel5;
        private System.Windows.Forms.TableLayoutPanel tableLayoutPanel6;
        private System.Windows.Forms.Label label1;
        private System.Windows.Forms.TableLayoutPanel tableLayoutPanel7;
        private System.Windows.Forms.TableLayoutPanel tableLayoutPanel8;
        private System.Windows.Forms.TableLayoutPanel tableLayoutPanel9;
        private System.Windows.Forms.TableLayoutPanel tableLayoutPanel10;
        private System.Windows.Forms.TableLayoutPanel tableLayoutPanel11;
        private System.Windows.Forms.TableLayoutPanel tableLayoutPanel12;
        private System.Windows.Forms.TableLayoutPanel tableLayoutPanel13;
        private System.Windows.Forms.TableLayoutPanel tableLayoutPanel14;
        private System.Windows.Forms.TableLayoutPanel tableLayoutPanel15;
        private System.Windows.Forms.TableLayoutPanel tableLayoutPanel16;
        private System.Windows.Forms.TableLayoutPanel tableLayoutPanel17;
        private System.Windows.Forms.TableLayoutPanel tableLayoutPanel18;
        private System.Windows.Forms.TableLayoutPanel tableLayoutPanel19;
        private System.Windows.Forms.TableLayoutPanel tableLayoutPanel25;
        private System.Windows.Forms.TableLayoutPanel tableLayoutPanel29;
        private System.Windows.Forms.TableLayoutPanel tableLayoutPanel28;
        private System.Windows.Forms.GroupBox groupBox22;
        private System.Windows.Forms.TableLayoutPanel tableLayoutPanel30;
        private System.Windows.Forms.ComboBox defaultStorageClassComboBox;
        private System.Windows.Forms.Label label3;
        private System.Windows.Forms.TableLayoutPanel tableLayoutPanel34;
        private System.Windows.Forms.GroupBox groupBox25;
        private System.Windows.Forms.TableLayoutPanel tableLayoutPanel35;
        private System.Windows.Forms.GroupBox groupBox26;
        private System.Windows.Forms.TableLayoutPanel tableLayoutPanel36;
        private System.Windows.Forms.TableLayoutPanel tableLayoutPanel39;
        private System.Windows.Forms.TableLayoutPanel tableLayoutPanel37;
        private System.Windows.Forms.Label label16;
        private System.Windows.Forms.GroupBox groupBox20;
        private System.Windows.Forms.TableLayoutPanel tableLayoutPanel40;
        private System.Windows.Forms.CheckBox systemProxyCheckBox;
        private System.Windows.Forms.ToolStripButton updateButton;
        private ManagedPanel managedUpdatePanel;
        private System.Windows.Forms.TableLayoutPanel tableLayoutPanel41;
        private System.Windows.Forms.GroupBox groupBox27;
        private System.Windows.Forms.TableLayoutPanel tableLayoutPanel42;
        private System.Windows.Forms.CheckBox updateCheckBox;
        private System.Windows.Forms.Button updateCheckButton;
        private System.Windows.Forms.Label lastUpdateLabel;
        private System.Windows.Forms.ToolStripButton languageButton;
        private ManagedPanel managedLanguagePanel;
        private System.Windows.Forms.TableLayoutPanel tableLayoutPanel43;
        private System.Windows.Forms.GroupBox groupBox28;
        private System.Windows.Forms.TableLayoutPanel tableLayoutPanel44;
        private System.Windows.Forms.ComboBox languageComboBox;
        private System.Windows.Forms.Label label18;
        private System.Windows.Forms.GroupBox groupBox29;
        private ClickThroughToolStrip toolStrip;
        private System.Windows.Forms.CheckBox uploadTemporaryNameCheckBox;
        private System.Windows.Forms.ComboBox updateFeedComboBox;
        private System.Windows.Forms.Button changeSystemProxyButton;
        private ManagedPanel managedEditorPanel;
        private System.Windows.Forms.TableLayoutPanel tableLayoutPanel3;
        private System.Windows.Forms.GroupBox groupBox2;
        private System.Windows.Forms.ToolStripButton editStripButton;
        private System.Windows.Forms.TableLayoutPanel tableLayoutPanel4;
        private System.Windows.Forms.Label label14;
        private ImageComboBox editorComboBox;
        private System.Windows.Forms.OpenFileDialog editorOpenFileDialog;
        private System.Windows.Forms.CheckBox alwaysUseDefaultEditorCheckBox;
        private System.Windows.Forms.GroupBox groupBox3;
        private System.Windows.Forms.TableLayoutPanel tableLayoutPanel20;
        private System.Windows.Forms.ComboBox defaultEncryptionComboBox;
        private System.Windows.Forms.Label label8;
        private ManagedPanel managedBrowserPanel;
        private System.Windows.Forms.ToolStripButton browserButton;
        private System.Windows.Forms.TableLayoutPanel tableLayoutPanel21;
        private System.Windows.Forms.GroupBox groupBox14;
        private System.Windows.Forms.TableLayoutPanel tableLayoutPanel23;
        private System.Windows.Forms.CheckBox infoWindowCheckbox;
        private System.Windows.Forms.CheckBox showHiddenFilesCheckbox;
        private System.Windows.Forms.CheckBox returnKeyCheckbox;
        private System.Windows.Forms.CheckBox doubleClickEditorCheckbox;
        private ManagedPanel managedCryptomatorPanel;
        private System.Windows.Forms.TableLayoutPanel tableLayoutPanel24;
        private System.Windows.Forms.GroupBox groupBox15;
        private System.Windows.Forms.FlowLayoutPanel flowLayoutPanel1;
        private System.Windows.Forms.CheckBox cryptomatorAutoDetectCheckBox;
        private System.Windows.Forms.ToolStripButton cryptomatorButton;
        private System.Windows.Forms.TabPage tabPage5;
        private System.Windows.Forms.TableLayoutPanel tableLayoutPanel26;
        private System.Windows.Forms.GroupBox groupBox16;
        private System.Windows.Forms.TableLayoutPanel tableLayoutPanel31;
        private System.Windows.Forms.CheckBox verifyChecksumDownloadCheckbox;
        private System.Windows.Forms.GroupBox groupBox17;
        private System.Windows.Forms.TableLayoutPanel tableLayoutPanel32;
        private System.Windows.Forms.CheckBox verifyChecksumUploadCheckbox;
        private System.Windows.Forms.CheckBox segmentedDownloadsCheckbox;
        private ManagedPanel managedGoogleCloudPanel;
        private System.Windows.Forms.ToolStripButton googleCloudButton;
        private System.Windows.Forms.GroupBox groupBox23;
        private System.Windows.Forms.TableLayoutPanel tableLayoutPanel33;
        private System.Windows.Forms.ComboBox defaultACLComboBox;
        private System.Windows.Forms.Label label5;
        private System.Windows.Forms.TableLayoutPanel tableLayoutPanel45;
        private System.Windows.Forms.GroupBox groupBox24;
        private System.Windows.Forms.TableLayoutPanel tableLayoutPanel46;
        private System.Windows.Forms.Label label6;
        private System.Windows.Forms.ComboBox defaultGoogleLocationComboBox;
        private System.Windows.Forms.GroupBox groupBox30;
        private System.Windows.Forms.TableLayoutPanel tableLayoutPanel47;
        private System.Windows.Forms.ComboBox defaultGoogleStorageClassComboBox;
        private System.Windows.Forms.Label label9;
        private System.Windows.Forms.GroupBox groupBox32;
        private System.Windows.Forms.TableLayoutPanel tableLayoutPanel49;
        private System.Windows.Forms.ComboBox defaultGoogleACLComboBox;
        private System.Windows.Forms.Label label17;
        private System.Windows.Forms.ToolStripButton profilesButton;
        private ManagedPanel managedProfilesPanel;
        private System.Windows.Forms.Integration.ElementHost profilesPageHost;
        private System.Windows.Forms.GroupBox groupBox4;
        private System.Windows.Forms.TableLayoutPanel tableLayoutPanel38;
        private System.Windows.Forms.Label label2;
        private System.Windows.Forms.ComboBox defaultEncodingCombobox;
        private System.Windows.Forms.GroupBox groupBox13;
        private System.Windows.Forms.TableLayoutPanel tableLayoutPanel22;
        private System.Windows.Forms.Label label15;
        private System.Windows.Forms.ComboBox bookmarkSizeComboBox;
        private System.Windows.Forms.GroupBox groupBox31;
        private System.Windows.Forms.TableLayoutPanel tableLayoutPanel48;
        private System.Windows.Forms.CheckBox debugLogCheckBox;
        private System.Windows.Forms.Button showDebugLogButton;
        private System.Windows.Forms.GroupBox groupBox1;
        private System.Windows.Forms.TableLayoutPanel tableLayoutPanel2;
        private System.Windows.Forms.CheckBox keychainCheckbox;
        private System.Windows.Forms.Label labelKeychain;
        private System.Windows.Forms.CheckBox confirmDisconnectCheckbox;
        private System.Windows.Forms.Label label7;
        private System.Windows.Forms.Label labelConfirmDisconnect;
        private ImageComboBox defaultProtocolCombobox;
        private System.Windows.Forms.GroupBox groupBox18;
        private System.Windows.Forms.TableLayoutPanel tableLayoutPanel27;
        private System.Windows.Forms.Label label13;
        private ImageComboBox connectBookmarkCombobox;
        private System.Windows.Forms.Label label19;
        private System.Windows.Forms.CheckBox newBrowserOnStartupCheckbox;
        private System.Windows.Forms.CheckBox cryptomatorUseKeychain;
    }
}
