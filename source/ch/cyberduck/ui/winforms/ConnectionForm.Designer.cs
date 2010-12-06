using System;
using Ch.Cyberduck.Ui.Controller;
using Ch.Cyberduck.Ui.Winforms.Controls;

namespace Ch.Cyberduck.Ui.Winforms
{
    partial class ConnectionForm
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
            System.ComponentModel.ComponentResourceManager resources = new System.ComponentModel.ComponentResourceManager(typeof(ConnectionForm));
            this.tableLayoutPanel1 = new System.Windows.Forms.TableLayoutPanel();
            this.label1 = new System.Windows.Forms.Label();
            this.protocol = new Ch.Cyberduck.Ui.Winforms.Controls.ImageComboBox();
            this.alertIcon = new System.Windows.Forms.PictureBox();
            this.labelURL = new System.Windows.Forms.Label();
            this.linkLabelURL = new System.Windows.Forms.LinkLabel();
            this.textBoxServer = new System.Windows.Forms.TextBox();
            this.labelServer = new System.Windows.Forms.Label();
            this.labelPort = new System.Windows.Forms.Label();
            this.textBoxPort = new System.Windows.Forms.TextBox();
            this.labelUsername = new System.Windows.Forms.Label();
            this.textBoxUsername = new System.Windows.Forms.TextBox();
            this.textBoxPassword = new System.Windows.Forms.TextBox();
            this.anonymousCheckBox = new System.Windows.Forms.CheckBox();
            this.savePasswordCheckBox = new System.Windows.Forms.CheckBox();
            this.separatorLine = new System.Windows.Forms.Label();
            this.toggleOptionsLabel = new System.Windows.Forms.Label();
            this.imageList = new System.Windows.Forms.ImageList(this.components);
            this.cancelButton = new System.Windows.Forms.Button();
            this.connectButton = new System.Windows.Forms.Button();
            this.optionsPanel = new System.Windows.Forms.TableLayoutPanel();
            this.label4 = new System.Windows.Forms.Label();
            this.label2 = new System.Windows.Forms.Label();
            this.textBoxPath = new System.Windows.Forms.TextBox();
            this.label3 = new System.Windows.Forms.Label();
            this.comboBoxConnectMode = new System.Windows.Forms.ComboBox();
            this.comboBoxEncoding = new System.Windows.Forms.ComboBox();
            this.checkBoxPKA = new System.Windows.Forms.CheckBox();
            this.pkLabel = new Ch.Cyberduck.Ui.Winforms.Controls.EllipsisLabel();
            this.tableLayoutPanel2 = new System.Windows.Forms.TableLayoutPanel();
            this.openFileDialog = new System.Windows.Forms.OpenFileDialog();
            this.tableLayoutPanel1.SuspendLayout();
            ((System.ComponentModel.ISupportInitialize)(this.alertIcon)).BeginInit();
            this.optionsPanel.SuspendLayout();
            this.tableLayoutPanel2.SuspendLayout();
            this.SuspendLayout();
            // 
            // tableLayoutPanel1
            // 
            this.tableLayoutPanel1.ColumnCount = 4;
            this.tableLayoutPanel1.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle(System.Windows.Forms.SizeType.Absolute, 120F));
            this.tableLayoutPanel1.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle(System.Windows.Forms.SizeType.Percent, 100F));
            this.tableLayoutPanel1.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle(System.Windows.Forms.SizeType.Absolute, 50F));
            this.tableLayoutPanel1.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle());
            this.tableLayoutPanel1.Controls.Add(this.label1, 0, 4);
            this.tableLayoutPanel1.Controls.Add(this.protocol, 0, 0);
            this.tableLayoutPanel1.Controls.Add(this.alertIcon, 3, 2);
            this.tableLayoutPanel1.Controls.Add(this.labelURL, 0, 2);
            this.tableLayoutPanel1.Controls.Add(this.linkLabelURL, 1, 2);
            this.tableLayoutPanel1.Controls.Add(this.textBoxServer, 1, 1);
            this.tableLayoutPanel1.Controls.Add(this.labelServer, 0, 1);
            this.tableLayoutPanel1.Controls.Add(this.labelPort, 2, 1);
            this.tableLayoutPanel1.Controls.Add(this.textBoxPort, 3, 1);
            this.tableLayoutPanel1.Controls.Add(this.labelUsername, 0, 3);
            this.tableLayoutPanel1.Controls.Add(this.textBoxUsername, 1, 3);
            this.tableLayoutPanel1.Controls.Add(this.textBoxPassword, 1, 4);
            this.tableLayoutPanel1.Controls.Add(this.anonymousCheckBox, 1, 5);
            this.tableLayoutPanel1.Controls.Add(this.savePasswordCheckBox, 1, 6);
            this.tableLayoutPanel1.Dock = System.Windows.Forms.DockStyle.Top;
            this.tableLayoutPanel1.Location = new System.Drawing.Point(0, 0);
            this.tableLayoutPanel1.Name = "tableLayoutPanel1";
            this.tableLayoutPanel1.Padding = new System.Windows.Forms.Padding(10, 10, 10, 0);
            this.tableLayoutPanel1.RowCount = 8;
            this.tableLayoutPanel1.RowStyles.Add(new System.Windows.Forms.RowStyle(System.Windows.Forms.SizeType.Absolute, 32F));
            this.tableLayoutPanel1.RowStyles.Add(new System.Windows.Forms.RowStyle(System.Windows.Forms.SizeType.Absolute, 28F));
            this.tableLayoutPanel1.RowStyles.Add(new System.Windows.Forms.RowStyle(System.Windows.Forms.SizeType.Absolute, 28F));
            this.tableLayoutPanel1.RowStyles.Add(new System.Windows.Forms.RowStyle(System.Windows.Forms.SizeType.Absolute, 28F));
            this.tableLayoutPanel1.RowStyles.Add(new System.Windows.Forms.RowStyle(System.Windows.Forms.SizeType.Absolute, 28F));
            this.tableLayoutPanel1.RowStyles.Add(new System.Windows.Forms.RowStyle(System.Windows.Forms.SizeType.Absolute, 25F));
            this.tableLayoutPanel1.RowStyles.Add(new System.Windows.Forms.RowStyle(System.Windows.Forms.SizeType.Absolute, 25F));
            this.tableLayoutPanel1.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel1.Size = new System.Drawing.Size(481, 206);
            this.tableLayoutPanel1.TabIndex = 37;
            // 
            // label1
            // 
            this.label1.Anchor = System.Windows.Forms.AnchorStyles.Right;
            this.label1.AutoSize = true;
            this.label1.Location = new System.Drawing.Point(67, 132);
            this.label1.Name = "label1";
            this.label1.Size = new System.Drawing.Size(60, 15);
            this.label1.TabIndex = 34;
            this.label1.Text = "Password:";
            this.label1.TextAlign = System.Drawing.ContentAlignment.TopRight;
            // 
            // protocol
            // 
            this.protocol.Anchor = ((System.Windows.Forms.AnchorStyles)(((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Left)
                        | System.Windows.Forms.AnchorStyles.Right)));
            this.tableLayoutPanel1.SetColumnSpan(this.protocol, 4);
            this.protocol.DrawMode = System.Windows.Forms.DrawMode.OwnerDrawFixed;
            this.protocol.DropDownStyle = System.Windows.Forms.ComboBoxStyle.DropDownList;
            this.protocol.FormattingEnabled = true;
            this.protocol.IconMember = null;
            this.protocol.ItemHeight = 20;
            this.protocol.Location = new System.Drawing.Point(13, 13);
            this.protocol.MaxDropDownItems = 9;
            this.protocol.Name = "protocol";
            this.protocol.Size = new System.Drawing.Size(455, 26);
            this.protocol.TabIndex = 0;
            this.protocol.SelectionChangeCommitted += new System.EventHandler(this.protocol_SelectionChangeCommitted);
            // 
            // alertIcon
            // 
            this.alertIcon.Anchor = System.Windows.Forms.AnchorStyles.Right;
            this.alertIcon.Image = global::Ch.Cyberduck.ResourcesBundle.alert;
            this.alertIcon.Location = new System.Drawing.Point(447, 73);
            this.alertIcon.Name = "alertIcon";
            this.alertIcon.Padding = new System.Windows.Forms.Padding(0, 2, 0, 0);
            this.alertIcon.Size = new System.Drawing.Size(21, 21);
            this.alertIcon.TabIndex = 33;
            this.alertIcon.TabStop = false;
            this.alertIcon.Visible = false;
            // 
            // labelURL
            // 
            this.labelURL.Anchor = System.Windows.Forms.AnchorStyles.Right;
            this.labelURL.AutoSize = true;
            this.labelURL.Location = new System.Drawing.Point(96, 76);
            this.labelURL.Name = "labelURL";
            this.labelURL.Size = new System.Drawing.Size(31, 15);
            this.labelURL.TabIndex = 3;
            this.labelURL.Text = "URL:";
            this.labelURL.TextAlign = System.Drawing.ContentAlignment.TopRight;
            // 
            // linkLabelURL
            // 
            this.linkLabelURL.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Left | System.Windows.Forms.AnchorStyles.Right)));
            this.linkLabelURL.AutoEllipsis = true;
            this.tableLayoutPanel1.SetColumnSpan(this.linkLabelURL, 2);
            this.linkLabelURL.Location = new System.Drawing.Point(133, 76);
            this.linkLabelURL.Margin = new System.Windows.Forms.Padding(3);
            this.linkLabelURL.Name = "linkLabelURL";
            this.linkLabelURL.Size = new System.Drawing.Size(259, 15);
            this.linkLabelURL.TabIndex = 4;
            this.linkLabelURL.TabStop = true;
            this.linkLabelURL.Text = "default";
            this.linkLabelURL.LinkClicked += new System.Windows.Forms.LinkLabelLinkClickedEventHandler(this.linkLabelURL_LinkClicked);
            // 
            // textBoxServer
            // 
            this.textBoxServer.Anchor = ((System.Windows.Forms.AnchorStyles)(((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Left)
                        | System.Windows.Forms.AnchorStyles.Right)));
            this.textBoxServer.Location = new System.Drawing.Point(133, 45);
            this.textBoxServer.Name = "textBoxServer";
            this.textBoxServer.Size = new System.Drawing.Size(209, 23);
            this.textBoxServer.TabIndex = 6;
            this.textBoxServer.TextChanged += new System.EventHandler(this.textBoxServer_TextChanged);
            // 
            // labelServer
            // 
            this.labelServer.Anchor = System.Windows.Forms.AnchorStyles.Right;
            this.labelServer.AutoSize = true;
            this.labelServer.Location = new System.Drawing.Point(85, 48);
            this.labelServer.Name = "labelServer";
            this.labelServer.Size = new System.Drawing.Size(42, 15);
            this.labelServer.TabIndex = 5;
            this.labelServer.Text = "Server:";
            this.labelServer.TextAlign = System.Drawing.ContentAlignment.TopRight;
            // 
            // labelPort
            // 
            this.labelPort.Anchor = System.Windows.Forms.AnchorStyles.Right;
            this.labelPort.AutoSize = true;
            this.labelPort.Location = new System.Drawing.Point(360, 48);
            this.labelPort.Name = "labelPort";
            this.labelPort.Size = new System.Drawing.Size(32, 15);
            this.labelPort.TabIndex = 7;
            this.labelPort.Text = "Port:";
            this.labelPort.TextAlign = System.Drawing.ContentAlignment.TopRight;
            // 
            // textBoxPort
            // 
            this.textBoxPort.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Right)));
            this.textBoxPort.Location = new System.Drawing.Point(398, 45);
            this.textBoxPort.Name = "textBoxPort";
            this.textBoxPort.Size = new System.Drawing.Size(70, 23);
            this.textBoxPort.TabIndex = 8;
            this.textBoxPort.TextChanged += new System.EventHandler(this.textBoxPort_TextChanged);
            // 
            // labelUsername
            // 
            this.labelUsername.Anchor = System.Windows.Forms.AnchorStyles.Right;
            this.labelUsername.AutoSize = true;
            this.labelUsername.Location = new System.Drawing.Point(64, 104);
            this.labelUsername.Name = "labelUsername";
            this.labelUsername.Size = new System.Drawing.Size(63, 15);
            this.labelUsername.TabIndex = 29;
            this.labelUsername.Text = "Username:";
            this.labelUsername.TextAlign = System.Drawing.ContentAlignment.TopRight;
            // 
            // textBoxUsername
            // 
            this.textBoxUsername.Anchor = ((System.Windows.Forms.AnchorStyles)(((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Left)
                        | System.Windows.Forms.AnchorStyles.Right)));
            this.tableLayoutPanel1.SetColumnSpan(this.textBoxUsername, 3);
            this.textBoxUsername.Location = new System.Drawing.Point(133, 101);
            this.textBoxUsername.Name = "textBoxUsername";
            this.textBoxUsername.Size = new System.Drawing.Size(335, 23);
            this.textBoxUsername.TabIndex = 30;
            this.textBoxUsername.TextChanged += new System.EventHandler(this.textBoxUsername_TextChanged);
            // 
            // textBoxPassword
            // 
            this.textBoxPassword.Anchor = ((System.Windows.Forms.AnchorStyles)(((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Left)
                        | System.Windows.Forms.AnchorStyles.Right)));
            this.tableLayoutPanel1.SetColumnSpan(this.textBoxPassword, 3);
            this.textBoxPassword.Location = new System.Drawing.Point(133, 129);
            this.textBoxPassword.Name = "textBoxPassword";
            this.textBoxPassword.Size = new System.Drawing.Size(335, 23);
            this.textBoxPassword.TabIndex = 35;
            this.textBoxPassword.UseSystemPasswordChar = true;
            // 
            // anonymousCheckBox
            // 
            this.anonymousCheckBox.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Left | System.Windows.Forms.AnchorStyles.Right)));
            this.tableLayoutPanel1.SetColumnSpan(this.anonymousCheckBox, 3);
            this.anonymousCheckBox.Location = new System.Drawing.Point(133, 157);
            this.anonymousCheckBox.Name = "anonymousCheckBox";
            this.anonymousCheckBox.Size = new System.Drawing.Size(335, 19);
            this.anonymousCheckBox.TabIndex = 36;
            this.anonymousCheckBox.Text = "Anonymous Login";
            this.anonymousCheckBox.UseVisualStyleBackColor = true;
            this.anonymousCheckBox.CheckedChanged += new System.EventHandler(this.anonymousCheckBox_CheckedChanged);
            // 
            // savePasswordCheckBox
            // 
            this.savePasswordCheckBox.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Left | System.Windows.Forms.AnchorStyles.Right)));
            this.tableLayoutPanel1.SetColumnSpan(this.savePasswordCheckBox, 3);
            this.savePasswordCheckBox.Location = new System.Drawing.Point(133, 182);
            this.savePasswordCheckBox.Name = "savePasswordCheckBox";
            this.savePasswordCheckBox.Size = new System.Drawing.Size(335, 19);
            this.savePasswordCheckBox.TabIndex = 37;
            this.savePasswordCheckBox.Text = "Save Password";
            this.savePasswordCheckBox.UseVisualStyleBackColor = true;
            this.savePasswordCheckBox.CheckedChanged += new System.EventHandler(this.savePasswordCheckBox_CheckedChanged);
            // 
            // separatorLine
            // 
            this.separatorLine.Anchor = ((System.Windows.Forms.AnchorStyles)(((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Left)
                        | System.Windows.Forms.AnchorStyles.Right)));
            this.separatorLine.BorderStyle = System.Windows.Forms.BorderStyle.Fixed3D;
            this.tableLayoutPanel2.SetColumnSpan(this.separatorLine, 3);
            this.separatorLine.Location = new System.Drawing.Point(16, 56);
            this.separatorLine.Margin = new System.Windows.Forms.Padding(6, 0, 3, 0);
            this.separatorLine.Name = "separatorLine";
            this.separatorLine.Size = new System.Drawing.Size(452, 2);
            this.separatorLine.TabIndex = 42;
            // 
            // toggleOptionsLabel
            // 
            this.toggleOptionsLabel.AutoSize = true;
            this.toggleOptionsLabel.ImageAlign = System.Drawing.ContentAlignment.MiddleLeft;
            this.toggleOptionsLabel.ImageIndex = 3;
            this.toggleOptionsLabel.ImageList = this.imageList;
            this.toggleOptionsLabel.Location = new System.Drawing.Point(13, 31);
            this.toggleOptionsLabel.MinimumSize = new System.Drawing.Size(0, 25);
            this.toggleOptionsLabel.Name = "toggleOptionsLabel";
            this.toggleOptionsLabel.Size = new System.Drawing.Size(124, 25);
            this.toggleOptionsLabel.TabIndex = 41;
            this.toggleOptionsLabel.Text = "        Toggle Transcript";
            this.toggleOptionsLabel.TextAlign = System.Drawing.ContentAlignment.MiddleLeft;
            // 
            // imageList
            // 
            this.imageList.ImageStream = ((System.Windows.Forms.ImageListStreamer)(resources.GetObject("imageList.ImageStream")));
            this.imageList.TransparentColor = System.Drawing.Color.Fuchsia;
            this.imageList.Images.SetKeyName(0, "arrow_up_bw.bmp");
            this.imageList.Images.SetKeyName(1, "arrow_up_color.bmp");
            this.imageList.Images.SetKeyName(2, "arrow_up_color_pressed.bmp");
            this.imageList.Images.SetKeyName(3, "arrow_down_bw.bmp");
            this.imageList.Images.SetKeyName(4, "arrow_down_color.bmp");
            this.imageList.Images.SetKeyName(5, "arrow_down_color_pressed.bmp");
            this.imageList.Images.SetKeyName(6, "green_arrow.bmp");
            // 
            // cancelButton
            // 
            this.cancelButton.AutoSize = true;
            this.cancelButton.AutoSizeMode = System.Windows.Forms.AutoSizeMode.GrowAndShrink;
            this.cancelButton.DialogResult = System.Windows.Forms.DialogResult.Cancel;
            this.cancelButton.Location = new System.Drawing.Point(415, 3);
            this.cancelButton.Name = "cancelButton";
            this.cancelButton.Size = new System.Drawing.Size(53, 25);
            this.cancelButton.TabIndex = 43;
            this.cancelButton.Text = "Cancel";
            this.cancelButton.UseVisualStyleBackColor = true;
            // 
            // connectButton
            // 
            this.connectButton.AutoSize = true;
            this.connectButton.AutoSizeMode = System.Windows.Forms.AutoSizeMode.GrowAndShrink;
            this.connectButton.DialogResult = System.Windows.Forms.DialogResult.OK;
            this.connectButton.Location = new System.Drawing.Point(347, 3);
            this.connectButton.Name = "connectButton";
            this.connectButton.Size = new System.Drawing.Size(62, 25);
            this.connectButton.TabIndex = 44;
            this.connectButton.Text = "Connect";
            this.connectButton.UseVisualStyleBackColor = true;
            // 
            // optionsPanel
            // 
            this.optionsPanel.ColumnCount = 4;
            this.optionsPanel.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle(System.Windows.Forms.SizeType.Absolute, 120F));
            this.optionsPanel.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle(System.Windows.Forms.SizeType.Percent, 100F));
            this.optionsPanel.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle());
            this.optionsPanel.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle());
            this.optionsPanel.Controls.Add(this.label4, 0, 2);
            this.optionsPanel.Controls.Add(this.label2, 0, 0);
            this.optionsPanel.Controls.Add(this.textBoxPath, 1, 0);
            this.optionsPanel.Controls.Add(this.label3, 0, 1);
            this.optionsPanel.Controls.Add(this.comboBoxConnectMode, 1, 1);
            this.optionsPanel.Controls.Add(this.comboBoxEncoding, 1, 2);
            this.optionsPanel.Controls.Add(this.checkBoxPKA, 1, 3);
            this.optionsPanel.Controls.Add(this.pkLabel, 1, 4);
            this.optionsPanel.Dock = System.Windows.Forms.DockStyle.Top;
            this.optionsPanel.Location = new System.Drawing.Point(0, 269);
            this.optionsPanel.Name = "optionsPanel";
            this.optionsPanel.Padding = new System.Windows.Forms.Padding(10, 0, 10, 0);
            this.optionsPanel.RowCount = 5;
            this.optionsPanel.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.optionsPanel.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.optionsPanel.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.optionsPanel.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.optionsPanel.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.optionsPanel.Size = new System.Drawing.Size(481, 133);
            this.optionsPanel.TabIndex = 45;
            // 
            // label4
            // 
            this.label4.Anchor = System.Windows.Forms.AnchorStyles.Right;
            this.label4.AutoSize = true;
            this.label4.Location = new System.Drawing.Point(67, 65);
            this.label4.Name = "label4";
            this.label4.Size = new System.Drawing.Size(60, 15);
            this.label4.TabIndex = 49;
            this.label4.Text = "Encoding:";
            this.label4.TextAlign = System.Drawing.ContentAlignment.TopRight;
            // 
            // label2
            // 
            this.label2.Anchor = System.Windows.Forms.AnchorStyles.Right;
            this.label2.AutoSize = true;
            this.label2.Location = new System.Drawing.Point(93, 7);
            this.label2.Name = "label2";
            this.label2.Size = new System.Drawing.Size(34, 15);
            this.label2.TabIndex = 45;
            this.label2.Text = "Path:";
            this.label2.TextAlign = System.Drawing.ContentAlignment.TopRight;
            // 
            // textBoxPath
            // 
            this.textBoxPath.Anchor = ((System.Windows.Forms.AnchorStyles)(((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Left)
                        | System.Windows.Forms.AnchorStyles.Right)));
            this.optionsPanel.SetColumnSpan(this.textBoxPath, 3);
            this.textBoxPath.Location = new System.Drawing.Point(133, 3);
            this.textBoxPath.Name = "textBoxPath";
            this.textBoxPath.Size = new System.Drawing.Size(335, 23);
            this.textBoxPath.TabIndex = 46;
            this.textBoxPath.TextChanged += new System.EventHandler(this.textBoxPath_TextChanged);
            // 
            // label3
            // 
            this.label3.Anchor = System.Windows.Forms.AnchorStyles.Right;
            this.label3.AutoSize = true;
            this.label3.Location = new System.Drawing.Point(38, 36);
            this.label3.Name = "label3";
            this.label3.Size = new System.Drawing.Size(89, 15);
            this.label3.TabIndex = 47;
            this.label3.Text = "Connect Mode:";
            this.label3.TextAlign = System.Drawing.ContentAlignment.TopRight;
            // 
            // comboBoxConnectMode
            // 
            this.comboBoxConnectMode.Anchor = ((System.Windows.Forms.AnchorStyles)(((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Left)
                        | System.Windows.Forms.AnchorStyles.Right)));
            this.optionsPanel.SetColumnSpan(this.comboBoxConnectMode, 3);
            this.comboBoxConnectMode.DropDownStyle = System.Windows.Forms.ComboBoxStyle.DropDownList;
            this.comboBoxConnectMode.FormattingEnabled = true;
            this.comboBoxConnectMode.Location = new System.Drawing.Point(133, 32);
            this.comboBoxConnectMode.Name = "comboBoxConnectMode";
            this.comboBoxConnectMode.Size = new System.Drawing.Size(335, 23);
            this.comboBoxConnectMode.TabIndex = 48;
            this.comboBoxConnectMode.SelectionChangeCommitted += new System.EventHandler(this.comboBoxConnectMode_SelectionChangeCommitted);
            // 
            // comboBoxEncoding
            // 
            this.comboBoxEncoding.Anchor = ((System.Windows.Forms.AnchorStyles)(((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Left)
                        | System.Windows.Forms.AnchorStyles.Right)));
            this.optionsPanel.SetColumnSpan(this.comboBoxEncoding, 3);
            this.comboBoxEncoding.DropDownStyle = System.Windows.Forms.ComboBoxStyle.DropDownList;
            this.comboBoxEncoding.FormattingEnabled = true;
            this.comboBoxEncoding.Location = new System.Drawing.Point(133, 61);
            this.comboBoxEncoding.Name = "comboBoxEncoding";
            this.comboBoxEncoding.Size = new System.Drawing.Size(335, 23);
            this.comboBoxEncoding.TabIndex = 50;
            this.comboBoxEncoding.SelectionChangeCommitted += new System.EventHandler(this.comboBoxEncoding_SelectionChangeCommitted);
            // 
            // checkBoxPKA
            // 
            this.checkBoxPKA.Anchor = System.Windows.Forms.AnchorStyles.Left;
            this.checkBoxPKA.AutoSize = true;
            this.optionsPanel.SetColumnSpan(this.checkBoxPKA, 3);
            this.checkBoxPKA.Location = new System.Drawing.Point(133, 90);
            this.checkBoxPKA.Name = "checkBoxPKA";
            this.checkBoxPKA.Size = new System.Drawing.Size(185, 19);
            this.checkBoxPKA.TabIndex = 51;
            this.checkBoxPKA.Text = "Use Public Key Authentication";
            this.checkBoxPKA.UseVisualStyleBackColor = true;
            this.checkBoxPKA.CheckedChanged += new System.EventHandler(this.checkBoxPKA_CheckedChanged);
            // 
            // pkLabel
            // 
            this.pkLabel.Anchor = ((System.Windows.Forms.AnchorStyles)(((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Left)
                        | System.Windows.Forms.AnchorStyles.Right)));
            this.pkLabel.AutoSize = true;
            this.optionsPanel.SetColumnSpan(this.pkLabel, 3);
            this.pkLabel.Location = new System.Drawing.Point(150, 112);
            this.pkLabel.Margin = new System.Windows.Forms.Padding(20, 0, 3, 0);
            this.pkLabel.MinimumSize = new System.Drawing.Size(0, 25);
            this.pkLabel.Name = "pkLabel";
            this.pkLabel.Size = new System.Drawing.Size(318, 25);
            this.pkLabel.TabIndex = 52;
            this.pkLabel.Text = "No private key selected";
            // 
            // tableLayoutPanel2
            // 
            this.tableLayoutPanel2.ColumnCount = 3;
            this.tableLayoutPanel2.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle(System.Windows.Forms.SizeType.Percent, 100F));
            this.tableLayoutPanel2.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle());
            this.tableLayoutPanel2.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle());
            this.tableLayoutPanel2.Controls.Add(this.separatorLine, 0, 2);
            this.tableLayoutPanel2.Controls.Add(this.toggleOptionsLabel, 0, 1);
            this.tableLayoutPanel2.Controls.Add(this.cancelButton, 2, 0);
            this.tableLayoutPanel2.Controls.Add(this.connectButton, 1, 0);
            this.tableLayoutPanel2.Dock = System.Windows.Forms.DockStyle.Top;
            this.tableLayoutPanel2.Location = new System.Drawing.Point(0, 206);
            this.tableLayoutPanel2.Name = "tableLayoutPanel2";
            this.tableLayoutPanel2.Padding = new System.Windows.Forms.Padding(10, 0, 10, 0);
            this.tableLayoutPanel2.RowCount = 3;
            this.tableLayoutPanel2.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel2.RowStyles.Add(new System.Windows.Forms.RowStyle(System.Windows.Forms.SizeType.Absolute, 25F));
            this.tableLayoutPanel2.RowStyles.Add(new System.Windows.Forms.RowStyle(System.Windows.Forms.SizeType.Absolute, 6F));
            this.tableLayoutPanel2.Size = new System.Drawing.Size(481, 63);
            this.tableLayoutPanel2.TabIndex = 46;
            // 
            // ConnectionForm
            // 
            this.AcceptButton = this.connectButton;
            this.AutoScaleDimensions = new System.Drawing.SizeF(7F, 15F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.CancelButton = this.cancelButton;
            this.ClientSize = new System.Drawing.Size(481, 410);
            this.Controls.Add(this.optionsPanel);
            this.Controls.Add(this.tableLayoutPanel2);
            this.Controls.Add(this.tableLayoutPanel1);
            this.FormBorderStyle = System.Windows.Forms.FormBorderStyle.SizableToolWindow;
            this.Name = "ConnectionForm";
            this.StartPosition = System.Windows.Forms.FormStartPosition.CenterParent;
            this.Text = "Open Connection";
            this.tableLayoutPanel1.ResumeLayout(false);
            this.tableLayoutPanel1.PerformLayout();
            ((System.ComponentModel.ISupportInitialize)(this.alertIcon)).EndInit();
            this.optionsPanel.ResumeLayout(false);
            this.optionsPanel.PerformLayout();
            this.tableLayoutPanel2.ResumeLayout(false);
            this.tableLayoutPanel2.PerformLayout();
            this.ResumeLayout(false);

        }

        #endregion

        private System.Windows.Forms.TableLayoutPanel tableLayoutPanel1;
        private System.Windows.Forms.Label label1;
        private ImageComboBox protocol;
        private System.Windows.Forms.PictureBox alertIcon;
        private System.Windows.Forms.Label labelURL;
        private System.Windows.Forms.LinkLabel linkLabelURL;
        private System.Windows.Forms.TextBox textBoxServer;
        private System.Windows.Forms.Label labelServer;
        private System.Windows.Forms.Label labelPort;
        private System.Windows.Forms.TextBox textBoxPort;
        private System.Windows.Forms.Label labelUsername;
        private System.Windows.Forms.TextBox textBoxUsername;
        private System.Windows.Forms.TextBox textBoxPassword;
        private System.Windows.Forms.CheckBox anonymousCheckBox;
        private System.Windows.Forms.CheckBox savePasswordCheckBox;
        private System.Windows.Forms.Label separatorLine;
        private System.Windows.Forms.Label toggleOptionsLabel;
        private System.Windows.Forms.ImageList imageList;
        private System.Windows.Forms.Button cancelButton;
        private System.Windows.Forms.Button connectButton;
        private System.Windows.Forms.TableLayoutPanel optionsPanel;
        private System.Windows.Forms.Label label2;
        private System.Windows.Forms.TextBox textBoxPath;
        private System.Windows.Forms.Label label3;
        private System.Windows.Forms.ComboBox comboBoxConnectMode;
        private System.Windows.Forms.Label label4;
        private System.Windows.Forms.ComboBox comboBoxEncoding;
        private System.Windows.Forms.CheckBox checkBoxPKA;
        private EllipsisLabel pkLabel;
        private System.Windows.Forms.TableLayoutPanel tableLayoutPanel2;
        private System.Windows.Forms.OpenFileDialog openFileDialog;
        public event EventHandler<PrivateKeyArgs> ChangedPrivateKey;
    }
}