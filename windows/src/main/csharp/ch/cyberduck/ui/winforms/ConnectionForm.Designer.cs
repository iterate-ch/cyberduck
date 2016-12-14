﻿using System;
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
            this.imageList = new System.Windows.Forms.ImageList(this.components);
            this.openFileDialog = new System.Windows.Forms.OpenFileDialog();
            this.connectButton = new System.Windows.Forms.Button();
            this.cancelButton = new System.Windows.Forms.Button();
            this.optionsPanel = new System.Windows.Forms.TableLayoutPanel();
            this.labelEncoding = new System.Windows.Forms.Label();
            this.labelPath = new System.Windows.Forms.Label();
            this.textBoxPath = new System.Windows.Forms.TextBox();
            this.labelConnectMode = new System.Windows.Forms.Label();
            this.comboBoxConnectMode = new System.Windows.Forms.ComboBox();
            this.comboBoxEncoding = new System.Windows.Forms.ComboBox();
            this.tableLayoutPanel2 = new System.Windows.Forms.TableLayoutPanel();
            this.separatorLine = new System.Windows.Forms.Label();
            this.toggleOptionsLabel = new System.Windows.Forms.Label();
            this.tableLayoutPanel1 = new System.Windows.Forms.TableLayoutPanel();
            this.labelPassword = new System.Windows.Forms.Label();
            this.protocol = new Ch.Cyberduck.Ui.Winforms.Controls.ImageComboBox();
            this.alertIcon = new System.Windows.Forms.PictureBox();
            this.labelURL = new System.Windows.Forms.Label();
            this.linkLabelURL = new System.Windows.Forms.LinkLabel();
            this.textBoxServer = new System.Windows.Forms.TextBox();
            this.labelServer = new System.Windows.Forms.Label();
            this.labelPort = new System.Windows.Forms.Label();
            this.labelUsername = new System.Windows.Forms.Label();
            this.textBoxUsername = new System.Windows.Forms.TextBox();
            this.textBoxPassword = new System.Windows.Forms.TextBox();
            this.anonymousCheckBox = new System.Windows.Forms.CheckBox();
            this.numericUpDownPort = new System.Windows.Forms.NumericUpDown();
            this.labelPrivateKey = new System.Windows.Forms.Label();
            this.choosePkButton = new System.Windows.Forms.Button();
            this.savePasswordCheckBox = new System.Windows.Forms.CheckBox();
            this.comboBoxPrivateKey = new System.Windows.Forms.ComboBox();
            this.optionsPanel.SuspendLayout();
            this.tableLayoutPanel2.SuspendLayout();
            this.tableLayoutPanel1.SuspendLayout();
            ((System.ComponentModel.ISupportInitialize)(this.alertIcon)).BeginInit();
            ((System.ComponentModel.ISupportInitialize)(this.numericUpDownPort)).BeginInit();
            this.SuspendLayout();
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
            // optionsPanel
            // 
            this.optionsPanel.ColumnCount = 4;
            this.optionsPanel.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle());
            this.optionsPanel.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle(System.Windows.Forms.SizeType.Percent, 100F));
            this.optionsPanel.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle());
            this.optionsPanel.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle());
            this.optionsPanel.Controls.Add(this.labelEncoding, 0, 2);
            this.optionsPanel.Controls.Add(this.labelPath, 0, 0);
            this.optionsPanel.Controls.Add(this.textBoxPath, 1, 0);
            this.optionsPanel.Controls.Add(this.labelConnectMode, 0, 1);
            this.optionsPanel.Controls.Add(this.comboBoxConnectMode, 1, 1);
            this.optionsPanel.Controls.Add(this.comboBoxEncoding, 1, 2);
            this.optionsPanel.Dock = System.Windows.Forms.DockStyle.Top;
            this.optionsPanel.Location = new System.Drawing.Point(0, 297);
            this.optionsPanel.Name = "optionsPanel";
            this.optionsPanel.Padding = new System.Windows.Forms.Padding(10, 0, 10, 0);
            this.optionsPanel.RowCount = 5;
            this.optionsPanel.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.optionsPanel.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.optionsPanel.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.optionsPanel.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.optionsPanel.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.optionsPanel.Size = new System.Drawing.Size(481, 93);
            this.optionsPanel.TabIndex = 45;
            // 
            // labelEncoding
            // 
            this.labelEncoding.Anchor = System.Windows.Forms.AnchorStyles.Right;
            this.labelEncoding.AutoSize = true;
            this.labelEncoding.Location = new System.Drawing.Point(42, 65);
            this.labelEncoding.Name = "labelEncoding";
            this.labelEncoding.Size = new System.Drawing.Size(60, 15);
            this.labelEncoding.TabIndex = 49;
            this.labelEncoding.Text = "Encoding:";
            this.labelEncoding.TextAlign = System.Drawing.ContentAlignment.TopRight;
            // 
            // labelPath
            // 
            this.labelPath.Anchor = System.Windows.Forms.AnchorStyles.Right;
            this.labelPath.AutoSize = true;
            this.labelPath.Location = new System.Drawing.Point(68, 7);
            this.labelPath.Name = "labelPath";
            this.labelPath.Size = new System.Drawing.Size(34, 15);
            this.labelPath.TabIndex = 45;
            this.labelPath.Text = "Path:";
            this.labelPath.TextAlign = System.Drawing.ContentAlignment.TopRight;
            // 
            // textBoxPath
            // 
            this.textBoxPath.Anchor = ((System.Windows.Forms.AnchorStyles)(((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Left) 
            | System.Windows.Forms.AnchorStyles.Right)));
            this.optionsPanel.SetColumnSpan(this.textBoxPath, 3);
            this.textBoxPath.Location = new System.Drawing.Point(108, 3);
            this.textBoxPath.Name = "textBoxPath";
            this.textBoxPath.Size = new System.Drawing.Size(360, 23);
            this.textBoxPath.TabIndex = 46;
            this.textBoxPath.TextChanged += new System.EventHandler(this.textBoxPath_TextChanged);
            // 
            // labelConnectMode
            // 
            this.labelConnectMode.Anchor = System.Windows.Forms.AnchorStyles.Right;
            this.labelConnectMode.AutoSize = true;
            this.labelConnectMode.Location = new System.Drawing.Point(13, 36);
            this.labelConnectMode.Name = "labelConnectMode";
            this.labelConnectMode.Size = new System.Drawing.Size(89, 15);
            this.labelConnectMode.TabIndex = 47;
            this.labelConnectMode.Text = "Connect Mode:";
            this.labelConnectMode.TextAlign = System.Drawing.ContentAlignment.TopRight;
            // 
            // comboBoxConnectMode
            // 
            this.comboBoxConnectMode.Anchor = ((System.Windows.Forms.AnchorStyles)(((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Left) 
            | System.Windows.Forms.AnchorStyles.Right)));
            this.optionsPanel.SetColumnSpan(this.comboBoxConnectMode, 3);
            this.comboBoxConnectMode.DropDownStyle = System.Windows.Forms.ComboBoxStyle.DropDownList;
            this.comboBoxConnectMode.FormattingEnabled = true;
            this.comboBoxConnectMode.Location = new System.Drawing.Point(108, 32);
            this.comboBoxConnectMode.Name = "comboBoxConnectMode";
            this.comboBoxConnectMode.Size = new System.Drawing.Size(360, 23);
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
            this.comboBoxEncoding.Location = new System.Drawing.Point(108, 61);
            this.comboBoxEncoding.Name = "comboBoxEncoding";
            this.comboBoxEncoding.Size = new System.Drawing.Size(360, 23);
            this.comboBoxEncoding.TabIndex = 50;
            this.comboBoxEncoding.SelectionChangeCommitted += new System.EventHandler(this.comboBoxEncoding_SelectionChangeCommitted);
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
            this.tableLayoutPanel2.Location = new System.Drawing.Point(0, 234);
            this.tableLayoutPanel2.Name = "tableLayoutPanel2";
            this.tableLayoutPanel2.Padding = new System.Windows.Forms.Padding(10, 0, 10, 0);
            this.tableLayoutPanel2.RowCount = 3;
            this.tableLayoutPanel2.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel2.RowStyles.Add(new System.Windows.Forms.RowStyle(System.Windows.Forms.SizeType.Absolute, 25F));
            this.tableLayoutPanel2.RowStyles.Add(new System.Windows.Forms.RowStyle(System.Windows.Forms.SizeType.Absolute, 6F));
            this.tableLayoutPanel2.Size = new System.Drawing.Size(481, 63);
            this.tableLayoutPanel2.TabIndex = 46;
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
            this.toggleOptionsLabel.Size = new System.Drawing.Size(122, 25);
            this.toggleOptionsLabel.TabIndex = 41;
            this.toggleOptionsLabel.Text = "        Toggle Transcript";
            this.toggleOptionsLabel.TextAlign = System.Drawing.ContentAlignment.MiddleLeft;
            // 
            // tableLayoutPanel1
            // 
            this.tableLayoutPanel1.ColumnCount = 4;
            this.tableLayoutPanel1.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle());
            this.tableLayoutPanel1.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle(System.Windows.Forms.SizeType.Percent, 100F));
            this.tableLayoutPanel1.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle(System.Windows.Forms.SizeType.Absolute, 50F));
            this.tableLayoutPanel1.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle());
            this.tableLayoutPanel1.Controls.Add(this.labelPassword, 0, 4);
            this.tableLayoutPanel1.Controls.Add(this.protocol, 0, 0);
            this.tableLayoutPanel1.Controls.Add(this.alertIcon, 3, 2);
            this.tableLayoutPanel1.Controls.Add(this.labelURL, 0, 2);
            this.tableLayoutPanel1.Controls.Add(this.linkLabelURL, 1, 2);
            this.tableLayoutPanel1.Controls.Add(this.textBoxServer, 1, 1);
            this.tableLayoutPanel1.Controls.Add(this.labelServer, 0, 1);
            this.tableLayoutPanel1.Controls.Add(this.labelPort, 2, 1);
            this.tableLayoutPanel1.Controls.Add(this.labelUsername, 0, 3);
            this.tableLayoutPanel1.Controls.Add(this.textBoxUsername, 1, 3);
            this.tableLayoutPanel1.Controls.Add(this.textBoxPassword, 1, 4);
            this.tableLayoutPanel1.Controls.Add(this.anonymousCheckBox, 1, 5);
            this.tableLayoutPanel1.Controls.Add(this.numericUpDownPort, 3, 1);
            this.tableLayoutPanel1.Controls.Add(this.labelPrivateKey, 0, 6);
            this.tableLayoutPanel1.Controls.Add(this.choosePkButton, 3, 6);
            this.tableLayoutPanel1.Controls.Add(this.savePasswordCheckBox, 1, 7);
            this.tableLayoutPanel1.Controls.Add(this.comboBoxPrivateKey, 1, 6);
            this.tableLayoutPanel1.Dock = System.Windows.Forms.DockStyle.Top;
            this.tableLayoutPanel1.Location = new System.Drawing.Point(0, 0);
            this.tableLayoutPanel1.Name = "tableLayoutPanel1";
            this.tableLayoutPanel1.Padding = new System.Windows.Forms.Padding(10, 10, 10, 0);
            this.tableLayoutPanel1.RowCount = 9;
            this.tableLayoutPanel1.RowStyles.Add(new System.Windows.Forms.RowStyle(System.Windows.Forms.SizeType.Absolute, 32F));
            this.tableLayoutPanel1.RowStyles.Add(new System.Windows.Forms.RowStyle(System.Windows.Forms.SizeType.Absolute, 28F));
            this.tableLayoutPanel1.RowStyles.Add(new System.Windows.Forms.RowStyle(System.Windows.Forms.SizeType.Absolute, 28F));
            this.tableLayoutPanel1.RowStyles.Add(new System.Windows.Forms.RowStyle(System.Windows.Forms.SizeType.Absolute, 28F));
            this.tableLayoutPanel1.RowStyles.Add(new System.Windows.Forms.RowStyle(System.Windows.Forms.SizeType.Absolute, 28F));
            this.tableLayoutPanel1.RowStyles.Add(new System.Windows.Forms.RowStyle(System.Windows.Forms.SizeType.Absolute, 25F));
            this.tableLayoutPanel1.RowStyles.Add(new System.Windows.Forms.RowStyle(System.Windows.Forms.SizeType.Absolute, 28F));
            this.tableLayoutPanel1.RowStyles.Add(new System.Windows.Forms.RowStyle(System.Windows.Forms.SizeType.Absolute, 28F));
            this.tableLayoutPanel1.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel1.Size = new System.Drawing.Size(481, 234);
            this.tableLayoutPanel1.TabIndex = 37;
            // 
            // labelPassword
            // 
            this.labelPassword.Anchor = System.Windows.Forms.AnchorStyles.Right;
            this.labelPassword.AutoSize = true;
            this.labelPassword.Location = new System.Drawing.Point(45, 132);
            this.labelPassword.Name = "labelPassword";
            this.labelPassword.Size = new System.Drawing.Size(60, 15);
            this.labelPassword.TabIndex = 34;
            this.labelPassword.Text = "Password:";
            this.labelPassword.TextAlign = System.Drawing.ContentAlignment.TopRight;
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
            this.alertIcon.Image = ((System.Drawing.Image)(resources.GetObject("alertIcon.Image")));
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
            this.labelURL.Location = new System.Drawing.Point(74, 76);
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
            this.linkLabelURL.Location = new System.Drawing.Point(111, 76);
            this.linkLabelURL.Margin = new System.Windows.Forms.Padding(3);
            this.linkLabelURL.Name = "linkLabelURL";
            this.linkLabelURL.Size = new System.Drawing.Size(277, 15);
            this.linkLabelURL.TabIndex = 4;
            this.linkLabelURL.TabStop = true;
            this.linkLabelURL.Text = "default";
            this.linkLabelURL.LinkClicked += new System.Windows.Forms.LinkLabelLinkClickedEventHandler(this.linkLabelURL_LinkClicked);
            // 
            // textBoxServer
            // 
            this.textBoxServer.Anchor = ((System.Windows.Forms.AnchorStyles)(((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Left) 
            | System.Windows.Forms.AnchorStyles.Right)));
            this.textBoxServer.Location = new System.Drawing.Point(111, 45);
            this.textBoxServer.Name = "textBoxServer";
            this.textBoxServer.Size = new System.Drawing.Size(227, 23);
            this.textBoxServer.TabIndex = 6;
            this.textBoxServer.TextChanged += new System.EventHandler(this.textBoxServer_TextChanged);
            // 
            // labelServer
            // 
            this.labelServer.Anchor = System.Windows.Forms.AnchorStyles.Right;
            this.labelServer.AutoSize = true;
            this.labelServer.Location = new System.Drawing.Point(63, 48);
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
            this.labelPort.Location = new System.Drawing.Point(356, 48);
            this.labelPort.Name = "labelPort";
            this.labelPort.Size = new System.Drawing.Size(32, 15);
            this.labelPort.TabIndex = 7;
            this.labelPort.Text = "Port:";
            this.labelPort.TextAlign = System.Drawing.ContentAlignment.TopRight;
            // 
            // labelUsername
            // 
            this.labelUsername.Anchor = System.Windows.Forms.AnchorStyles.Right;
            this.labelUsername.AutoSize = true;
            this.labelUsername.Location = new System.Drawing.Point(42, 104);
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
            this.textBoxUsername.Location = new System.Drawing.Point(111, 101);
            this.textBoxUsername.Name = "textBoxUsername";
            this.textBoxUsername.Size = new System.Drawing.Size(357, 23);
            this.textBoxUsername.TabIndex = 30;
            this.textBoxUsername.TextChanged += new System.EventHandler(this.textBoxUsername_TextChanged);
            // 
            // textBoxPassword
            // 
            this.textBoxPassword.Anchor = ((System.Windows.Forms.AnchorStyles)(((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Left) 
            | System.Windows.Forms.AnchorStyles.Right)));
            this.tableLayoutPanel1.SetColumnSpan(this.textBoxPassword, 3);
            this.textBoxPassword.Location = new System.Drawing.Point(111, 129);
            this.textBoxPassword.Name = "textBoxPassword";
            this.textBoxPassword.Size = new System.Drawing.Size(357, 23);
            this.textBoxPassword.TabIndex = 35;
            this.textBoxPassword.UseSystemPasswordChar = true;
            // 
            // anonymousCheckBox
            // 
            this.anonymousCheckBox.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Left | System.Windows.Forms.AnchorStyles.Right)));
            this.tableLayoutPanel1.SetColumnSpan(this.anonymousCheckBox, 3);
            this.anonymousCheckBox.Location = new System.Drawing.Point(111, 157);
            this.anonymousCheckBox.Name = "anonymousCheckBox";
            this.anonymousCheckBox.Size = new System.Drawing.Size(357, 19);
            this.anonymousCheckBox.TabIndex = 36;
            this.anonymousCheckBox.Text = "Anonymous Login";
            this.anonymousCheckBox.UseVisualStyleBackColor = true;
            this.anonymousCheckBox.CheckedChanged += new System.EventHandler(this.anonymousCheckBox_CheckedChanged);
            // 
            // numericUpDownPort
            // 
            this.numericUpDownPort.Location = new System.Drawing.Point(394, 45);
            this.numericUpDownPort.Maximum = new decimal(new int[] {
            65535,
            0,
            0,
            0});
            this.numericUpDownPort.Name = "numericUpDownPort";
            this.numericUpDownPort.Size = new System.Drawing.Size(70, 23);
            this.numericUpDownPort.TabIndex = 7;
            this.numericUpDownPort.TextAlign = System.Windows.Forms.HorizontalAlignment.Right;
            this.numericUpDownPort.TextChanged += new System.EventHandler(this.numericUpDownPort_TextChanged);
            // 
            // labelPrivateKey
            // 
            this.labelPrivateKey.Anchor = System.Windows.Forms.AnchorStyles.Right;
            this.labelPrivateKey.AutoSize = true;
            this.labelPrivateKey.Location = new System.Drawing.Point(13, 185);
            this.labelPrivateKey.Name = "labelPrivateKey";
            this.labelPrivateKey.Size = new System.Drawing.Size(92, 15);
            this.labelPrivateKey.TabIndex = 39;
            this.labelPrivateKey.Text = "SSH Private Key:";
            this.labelPrivateKey.TextAlign = System.Drawing.ContentAlignment.TopRight;
            // 
            // choosePkButton
            // 
            this.choosePkButton.AutoSize = true;
            this.choosePkButton.Location = new System.Drawing.Point(394, 181);
            this.choosePkButton.Margin = new System.Windows.Forms.Padding(3, 2, 2, 1);
            this.choosePkButton.Name = "choosePkButton";
            this.choosePkButton.Size = new System.Drawing.Size(75, 25);
            this.choosePkButton.TabIndex = 38;
            this.choosePkButton.Text = "Choose…";
            this.choosePkButton.UseVisualStyleBackColor = true;
            this.choosePkButton.Click += new System.EventHandler(this.choosePkButton_Click);
            // 
            // savePasswordCheckBox
            // 
            this.savePasswordCheckBox.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Left | System.Windows.Forms.AnchorStyles.Right)));
            this.savePasswordCheckBox.Location = new System.Drawing.Point(111, 211);
            this.savePasswordCheckBox.Name = "savePasswordCheckBox";
            this.savePasswordCheckBox.Size = new System.Drawing.Size(227, 19);
            this.savePasswordCheckBox.TabIndex = 37;
            this.savePasswordCheckBox.Text = "Save Password";
            this.savePasswordCheckBox.UseVisualStyleBackColor = true;
            this.savePasswordCheckBox.CheckedChanged += new System.EventHandler(this.savePasswordCheckBox_CheckedChanged);
            // 
            // comboBoxPrivateKey
            // 
            this.comboBoxPrivateKey.Anchor = ((System.Windows.Forms.AnchorStyles)(((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Left) 
            | System.Windows.Forms.AnchorStyles.Right)));
            this.tableLayoutPanel1.SetColumnSpan(this.comboBoxPrivateKey, 2);
            this.comboBoxPrivateKey.DropDownStyle = System.Windows.Forms.ComboBoxStyle.DropDownList;
            this.comboBoxPrivateKey.FormattingEnabled = true;
            this.comboBoxPrivateKey.Location = new System.Drawing.Point(111, 182);
            this.comboBoxPrivateKey.Name = "comboBoxPrivateKey";
            this.comboBoxPrivateKey.Size = new System.Drawing.Size(277, 23);
            this.comboBoxPrivateKey.TabIndex = 37;
            // 
            // ConnectionForm
            // 
            this.AcceptButton = this.connectButton;
            this.AutoScaleDimensions = new System.Drawing.SizeF(7F, 15F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.CancelButton = this.cancelButton;
            this.ClientSize = new System.Drawing.Size(481, 392);
            this.Controls.Add(this.optionsPanel);
            this.Controls.Add(this.tableLayoutPanel2);
            this.Controls.Add(this.tableLayoutPanel1);
            this.FormBorderStyle = System.Windows.Forms.FormBorderStyle.SizableToolWindow;
            this.Name = "ConnectionForm";
            this.StartPosition = System.Windows.Forms.FormStartPosition.CenterParent;
            this.Text = "New Connection";
            this.optionsPanel.ResumeLayout(false);
            this.optionsPanel.PerformLayout();
            this.tableLayoutPanel2.ResumeLayout(false);
            this.tableLayoutPanel2.PerformLayout();
            this.tableLayoutPanel1.ResumeLayout(false);
            this.tableLayoutPanel1.PerformLayout();
            ((System.ComponentModel.ISupportInitialize)(this.alertIcon)).EndInit();
            ((System.ComponentModel.ISupportInitialize)(this.numericUpDownPort)).EndInit();
            this.ResumeLayout(false);

        }

        #endregion

        private System.Windows.Forms.TableLayoutPanel tableLayoutPanel1;
        private System.Windows.Forms.Label labelPassword;
        private ImageComboBox protocol;
        private System.Windows.Forms.PictureBox alertIcon;
        private System.Windows.Forms.Label labelURL;
        private System.Windows.Forms.LinkLabel linkLabelURL;
        private System.Windows.Forms.TextBox textBoxServer;
        private System.Windows.Forms.Label labelServer;
        private System.Windows.Forms.Label labelPort;
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
        private System.Windows.Forms.Label labelPath;
        private System.Windows.Forms.TextBox textBoxPath;
        private System.Windows.Forms.Label labelConnectMode;
        private System.Windows.Forms.ComboBox comboBoxConnectMode;
        private System.Windows.Forms.Label labelEncoding;
        private System.Windows.Forms.ComboBox comboBoxEncoding;
        private System.Windows.Forms.TableLayoutPanel tableLayoutPanel2;
        private System.Windows.Forms.OpenFileDialog openFileDialog;
        private System.Windows.Forms.NumericUpDown numericUpDownPort;
        private System.Windows.Forms.ComboBox comboBoxPrivateKey;
        private System.Windows.Forms.Label labelPrivateKey;
        private System.Windows.Forms.Button choosePkButton;
    }
}