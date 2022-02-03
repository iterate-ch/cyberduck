using Ch.Cyberduck.Ui.Winforms.Controls;

namespace Ch.Cyberduck.Ui.Winforms
{
    partial class TransferForm
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
            this.toolbarMenuStrip = new System.Windows.Forms.ContextMenuStrip(this.components);
            this.resumeToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.reloadToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.stopToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.removeToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.cleanUpToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.logToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.trashToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.toolStripSeparator1 = new System.Windows.Forms.ToolStripSeparator();
            this.openToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.showToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.bandwidthMenuStrip = new System.Windows.Forms.ContextMenuStrip(this.components);
            this.connectionsMenuStrip = new System.Windows.Forms.ContextMenuStrip(this.components);
            this.cancelButton = new System.Windows.Forms.Button();
            this.splitContainer = new System.Windows.Forms.SplitContainer();
            this.panel2 = new System.Windows.Forms.Panel();
            this.transferListView = new Ch.Cyberduck.Ui.Winforms.Controls.ListViewControls();
            this.dummyColumn = ((BrightIdeasSoftware.OLVColumn)(new BrightIdeasSoftware.OLVColumn()));
            this.transferColumn = ((BrightIdeasSoftware.OLVColumn)(new BrightIdeasSoftware.OLVColumn()));
            this.tableLayoutPanel1 = new System.Windows.Forms.TableLayoutPanel();
            this.bandwidthSplitButton = new Ch.Cyberduck.Ui.Winforms.Controls.SplitButton();
            this.fileIcon = new System.Windows.Forms.PictureBox();
            this.connectionsSplitButton = new Ch.Cyberduck.Ui.Winforms.Controls.SplitButton();
            this.label1 = new System.Windows.Forms.Label();
            this.localLabel = new Ch.Cyberduck.Ui.Winforms.Controls.EllipsisLabel();
            this.label2 = new System.Windows.Forms.Label();
            this.urlLabel = new Ch.Cyberduck.Ui.Winforms.Controls.EllipsisLabel();
            this.transcriptBox = new System.Windows.Forms.RichTextBox();
            this.toolStrip = new Ch.Cyberduck.Ui.Winforms.Controls.ClickThroughToolStrip();
            this.resumeToolStripButton = new System.Windows.Forms.ToolStripButton();
            this.reloadToolStripButton = new System.Windows.Forms.ToolStripButton();
            this.stopToolStripButton = new System.Windows.Forms.ToolStripButton();
            this.removeToolStripButton = new System.Windows.Forms.ToolStripButton();
            this.cleanUptoolStripButton = new System.Windows.Forms.ToolStripButton();
            this.showToolStripButton = new System.Windows.Forms.ToolStripButton();
            this.openToolStripButton = new System.Windows.Forms.ToolStripButton();
            this.logToolStripButton = new System.Windows.Forms.ToolStripButton();
            this.trashToolStripButton = new System.Windows.Forms.ToolStripButton();
            this.toolbarMenuStrip.SuspendLayout();
            ((System.ComponentModel.ISupportInitialize)(this.splitContainer)).BeginInit();
            this.splitContainer.Panel1.SuspendLayout();
            this.splitContainer.Panel2.SuspendLayout();
            this.splitContainer.SuspendLayout();
            this.panel2.SuspendLayout();
            ((System.ComponentModel.ISupportInitialize)(this.transferListView)).BeginInit();
            this.tableLayoutPanel1.SuspendLayout();
            ((System.ComponentModel.ISupportInitialize)(this.fileIcon)).BeginInit();
            this.toolStrip.SuspendLayout();
            this.SuspendLayout();
            // 
            // toolbarMenuStrip
            // 
            this.toolbarMenuStrip.ImageScalingSize = new System.Drawing.Size(32, 32);
            this.toolbarMenuStrip.Items.AddRange(new System.Windows.Forms.ToolStripItem[] {
            this.resumeToolStripMenuItem,
            this.reloadToolStripMenuItem,
            this.stopToolStripMenuItem,
            this.removeToolStripMenuItem,
            this.cleanUpToolStripMenuItem,
            this.logToolStripMenuItem,
            this.trashToolStripMenuItem,
            this.toolStripSeparator1,
            this.openToolStripMenuItem,
            this.showToolStripMenuItem});
            this.toolbarMenuStrip.Name = "toolbarMenuStrip";
            this.toolbarMenuStrip.Size = new System.Drawing.Size(188, 352);
            this.toolbarMenuStrip.Closing += new System.Windows.Forms.ToolStripDropDownClosingEventHandler(this.toolbarMenuStrip_Closing);
            this.toolbarMenuStrip.ItemClicked += new System.Windows.Forms.ToolStripItemClickedEventHandler(this.toolbarMenuStrip_ItemClicked);
            // 
            // resumeToolStripMenuItem
            // 
            this.resumeToolStripMenuItem.Name = "resumeToolStripMenuItem";
            this.resumeToolStripMenuItem.Size = new System.Drawing.Size(187, 38);
            this.resumeToolStripMenuItem.Text = "Resume";
            // 
            // reloadToolStripMenuItem
            // 
            this.reloadToolStripMenuItem.Name = "reloadToolStripMenuItem";
            this.reloadToolStripMenuItem.Size = new System.Drawing.Size(187, 38);
            this.reloadToolStripMenuItem.Text = "Reload";
            // 
            // stopToolStripMenuItem
            // 
            this.stopToolStripMenuItem.Name = "stopToolStripMenuItem";
            this.stopToolStripMenuItem.Size = new System.Drawing.Size(187, 38);
            this.stopToolStripMenuItem.Text = "Stop";
            // 
            // removeToolStripMenuItem
            // 
            this.removeToolStripMenuItem.Name = "removeToolStripMenuItem";
            this.removeToolStripMenuItem.Size = new System.Drawing.Size(187, 38);
            this.removeToolStripMenuItem.Text = "Remove";
            // 
            // cleanUpToolStripMenuItem
            // 
            this.cleanUpToolStripMenuItem.Name = "cleanUpToolStripMenuItem";
            this.cleanUpToolStripMenuItem.Size = new System.Drawing.Size(187, 38);
            this.cleanUpToolStripMenuItem.Text = "Clean Up";
            // 
            // logToolStripMenuItem
            // 
            this.logToolStripMenuItem.Name = "logToolStripMenuItem";
            this.logToolStripMenuItem.Size = new System.Drawing.Size(187, 38);
            this.logToolStripMenuItem.Text = "Log";
            // 
            // trashToolStripMenuItem
            // 
            this.trashToolStripMenuItem.Name = "trashToolStripMenuItem";
            this.trashToolStripMenuItem.Size = new System.Drawing.Size(187, 38);
            this.trashToolStripMenuItem.Text = "Trash";
            // 
            // toolStripSeparator1
            // 
            this.toolStripSeparator1.Name = "toolStripSeparator1";
            this.toolStripSeparator1.Size = new System.Drawing.Size(184, 6);
            // 
            // openToolStripMenuItem
            // 
            this.openToolStripMenuItem.Name = "openToolStripMenuItem";
            this.openToolStripMenuItem.Size = new System.Drawing.Size(187, 38);
            this.openToolStripMenuItem.Text = "Open";
            // 
            // showToolStripMenuItem
            // 
            this.showToolStripMenuItem.Name = "showToolStripMenuItem";
            this.showToolStripMenuItem.Size = new System.Drawing.Size(187, 38);
            this.showToolStripMenuItem.Text = "Show";
            // 
            // bandwidthMenuStrip
            // 
            this.bandwidthMenuStrip.ImageScalingSize = new System.Drawing.Size(32, 32);
            this.bandwidthMenuStrip.Name = "bandwidthMenuStrip";
            this.bandwidthMenuStrip.Size = new System.Drawing.Size(61, 4);
            // 
            // connectionsMenuStrip
            // 
            this.connectionsMenuStrip.ImageScalingSize = new System.Drawing.Size(32, 32);
            this.connectionsMenuStrip.Name = "connectionsMenuStrip";
            this.connectionsMenuStrip.Size = new System.Drawing.Size(61, 4);
            // 
            // cancelButton
            // 
            this.cancelButton.DialogResult = System.Windows.Forms.DialogResult.Cancel;
            this.cancelButton.Location = new System.Drawing.Point(561, 26);
            this.cancelButton.Margin = new System.Windows.Forms.Padding(6, 6, 6, 6);
            this.cancelButton.Name = "cancelButton";
            this.cancelButton.Size = new System.Drawing.Size(0, 0);
            this.cancelButton.TabIndex = 7;
            this.cancelButton.TabStop = false;
            this.cancelButton.Text = "cancelButton";
            this.cancelButton.UseVisualStyleBackColor = true;
            this.cancelButton.Click += new System.EventHandler(this.cancelButton_Click);
            // 
            // splitContainer
            // 
            this.splitContainer.Dock = System.Windows.Forms.DockStyle.Fill;
            this.splitContainer.FixedPanel = System.Windows.Forms.FixedPanel.Panel2;
            this.splitContainer.Location = new System.Drawing.Point(0, 74);
            this.splitContainer.Margin = new System.Windows.Forms.Padding(6, 6, 6, 6);
            this.splitContainer.Name = "splitContainer";
            this.splitContainer.Orientation = System.Windows.Forms.Orientation.Horizontal;
            // 
            // splitContainer.Panel1
            // 
            this.splitContainer.Panel1.Controls.Add(this.panel2);
            // 
            // splitContainer.Panel2
            // 
            this.splitContainer.Panel2.Controls.Add(this.transcriptBox);
            this.splitContainer.Size = new System.Drawing.Size(1005, 1061);
            this.splitContainer.SplitterDistance = 900;
            this.splitContainer.SplitterWidth = 11;
            this.splitContainer.TabIndex = 6;
            this.splitContainer.SplitterMoved += new System.Windows.Forms.SplitterEventHandler(this.splitContainer_SplitterMoved);
            // 
            // panel2
            // 
            this.panel2.Controls.Add(this.transferListView);
            this.panel2.Controls.Add(this.tableLayoutPanel1);
            this.panel2.Dock = System.Windows.Forms.DockStyle.Fill;
            this.panel2.Location = new System.Drawing.Point(0, 0);
            this.panel2.Margin = new System.Windows.Forms.Padding(6, 6, 6, 6);
            this.panel2.Name = "panel2";
            this.panel2.Size = new System.Drawing.Size(1005, 900);
            this.panel2.TabIndex = 7;
            // 
            // transferListView
            // 
            this.transferListView.AllColumns.Add(this.dummyColumn);
            this.transferListView.AllColumns.Add(this.transferColumn);
            this.transferListView.AlternateRowBackColor = System.Drawing.SystemColors.Menu;
            this.transferListView.BorderStyle = System.Windows.Forms.BorderStyle.None;
            this.transferListView.Columns.AddRange(new System.Windows.Forms.ColumnHeader[] {
            this.dummyColumn,
            this.transferColumn});
            this.transferListView.Cursor = System.Windows.Forms.Cursors.Default;
            this.transferListView.Dock = System.Windows.Forms.DockStyle.Fill;
            this.transferListView.GridLines = true;
            this.transferListView.HeaderStyle = System.Windows.Forms.ColumnHeaderStyle.None;
            this.transferListView.HideSelection = false;
            this.transferListView.Location = new System.Drawing.Point(0, 0);
            this.transferListView.Margin = new System.Windows.Forms.Padding(6, 6, 6, 6);
            this.transferListView.Name = "transferListView";
            this.transferListView.RowHeight = 85;
            this.transferListView.Size = new System.Drawing.Size(1005, 806);
            this.transferListView.TabIndex = 6;
            this.transferListView.UseCompatibleStateImageBehavior = false;
            this.transferListView.View = System.Windows.Forms.View.Details;
            this.transferListView.KeyDown += new System.Windows.Forms.KeyEventHandler(this.transferListView_KeyDown);
            this.transferListView.KeyPress += new System.Windows.Forms.KeyPressEventHandler(this.transferListView_KeyPress);
            // 
            // dummyColumn
            // 
            this.dummyColumn.CellPadding = null;
            // 
            // transferColumn
            // 
            this.transferColumn.CellPadding = null;
            this.transferColumn.Width = 400;
            // 
            // tableLayoutPanel1
            // 
            this.tableLayoutPanel1.ColumnCount = 5;
            this.tableLayoutPanel1.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle(System.Windows.Forms.SizeType.Absolute, 78F));
            this.tableLayoutPanel1.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle(System.Windows.Forms.SizeType.Absolute, 186F));
            this.tableLayoutPanel1.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle(System.Windows.Forms.SizeType.Percent, 100F));
            this.tableLayoutPanel1.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle(System.Windows.Forms.SizeType.Absolute, 102F));
            this.tableLayoutPanel1.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle(System.Windows.Forms.SizeType.Absolute, 102F));
            this.tableLayoutPanel1.Controls.Add(this.bandwidthSplitButton, 4, 0);
            this.tableLayoutPanel1.Controls.Add(this.fileIcon, 0, 0);
            this.tableLayoutPanel1.Controls.Add(this.connectionsSplitButton, 3, 0);
            this.tableLayoutPanel1.Controls.Add(this.label1, 1, 0);
            this.tableLayoutPanel1.Controls.Add(this.localLabel, 2, 1);
            this.tableLayoutPanel1.Controls.Add(this.label2, 1, 1);
            this.tableLayoutPanel1.Controls.Add(this.urlLabel, 2, 0);
            this.tableLayoutPanel1.Dock = System.Windows.Forms.DockStyle.Bottom;
            this.tableLayoutPanel1.Location = new System.Drawing.Point(0, 806);
            this.tableLayoutPanel1.Margin = new System.Windows.Forms.Padding(6, 6, 6, 6);
            this.tableLayoutPanel1.Name = "tableLayoutPanel1";
            this.tableLayoutPanel1.Padding = new System.Windows.Forms.Padding(9, 11, 9, 0);
            this.tableLayoutPanel1.RowCount = 2;
            this.tableLayoutPanel1.RowStyles.Add(new System.Windows.Forms.RowStyle(System.Windows.Forms.SizeType.Percent, 50F));
            this.tableLayoutPanel1.RowStyles.Add(new System.Windows.Forms.RowStyle(System.Windows.Forms.SizeType.Percent, 50F));
            this.tableLayoutPanel1.Size = new System.Drawing.Size(1005, 94);
            this.tableLayoutPanel1.TabIndex = 8;
            // 
            // bandwidthSplitButton
            // 
            this.bandwidthSplitButton.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Left | System.Windows.Forms.AnchorStyles.Right)));
            this.bandwidthSplitButton.ContextMenuStrip = this.bandwidthMenuStrip;
            this.bandwidthSplitButton.Location = new System.Drawing.Point(900, 28);
            this.bandwidthSplitButton.Margin = new System.Windows.Forms.Padding(6, 6, 6, 6);
            this.bandwidthSplitButton.Name = "bandwidthSplitButton";
            this.tableLayoutPanel1.SetRowSpan(this.bandwidthSplitButton, 2);
            this.bandwidthSplitButton.Size = new System.Drawing.Size(90, 49);
            this.bandwidthSplitButton.SplitMenuStrip = this.bandwidthMenuStrip;
            this.bandwidthSplitButton.TabIndex = 7;
            this.bandwidthSplitButton.UseVisualStyleBackColor = true;
            this.bandwidthSplitButton.Click += new System.EventHandler(this.bandwidthSplitButton_Click);
            // 
            // fileIcon
            // 
            this.fileIcon.Dock = System.Windows.Forms.DockStyle.Fill;
            this.fileIcon.Location = new System.Drawing.Point(15, 11);
            this.fileIcon.Margin = new System.Windows.Forms.Padding(6, 0, 6, 6);
            this.fileIcon.Name = "fileIcon";
            this.tableLayoutPanel1.SetRowSpan(this.fileIcon, 2);
            this.fileIcon.Size = new System.Drawing.Size(66, 77);
            this.fileIcon.TabIndex = 2;
            this.fileIcon.TabStop = false;
            // 
            // connectionsSplitButton
            // 
            this.connectionsSplitButton.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Left | System.Windows.Forms.AnchorStyles.Right)));
            this.connectionsSplitButton.ContextMenuStrip = this.connectionsMenuStrip;
            this.connectionsSplitButton.Location = new System.Drawing.Point(798, 28);
            this.connectionsSplitButton.Margin = new System.Windows.Forms.Padding(6, 6, 6, 6);
            this.connectionsSplitButton.Name = "connectionsSplitButton";
            this.tableLayoutPanel1.SetRowSpan(this.connectionsSplitButton, 2);
            this.connectionsSplitButton.Size = new System.Drawing.Size(90, 49);
            this.connectionsSplitButton.SplitMenuStrip = this.connectionsMenuStrip;
            this.connectionsSplitButton.TabIndex = 6;
            this.connectionsSplitButton.UseVisualStyleBackColor = true;
            this.connectionsSplitButton.Click += new System.EventHandler(this.queueSizeUpDown_ValueChanged);
            // 
            // label1
            // 
            this.label1.Dock = System.Windows.Forms.DockStyle.Fill;
            this.label1.Location = new System.Drawing.Point(93, 11);
            this.label1.Margin = new System.Windows.Forms.Padding(6, 0, 6, 0);
            this.label1.Name = "label1";
            this.label1.Size = new System.Drawing.Size(174, 41);
            this.label1.TabIndex = 0;
            this.label1.Text = "URL:";
            this.label1.TextAlign = System.Drawing.ContentAlignment.TopRight;
            // 
            // localLabel
            // 
            this.localLabel.AutoSize = true;
            this.localLabel.Dock = System.Windows.Forms.DockStyle.Fill;
            this.localLabel.Location = new System.Drawing.Point(279, 52);
            this.localLabel.Margin = new System.Windows.Forms.Padding(6, 0, 6, 0);
            this.localLabel.Name = "localLabel";
            this.localLabel.Size = new System.Drawing.Size(507, 42);
            this.localLabel.TabIndex = 4;
            // 
            // label2
            // 
            this.label2.Dock = System.Windows.Forms.DockStyle.Fill;
            this.label2.Location = new System.Drawing.Point(93, 52);
            this.label2.Margin = new System.Windows.Forms.Padding(6, 0, 6, 0);
            this.label2.Name = "label2";
            this.label2.Size = new System.Drawing.Size(174, 42);
            this.label2.TabIndex = 1;
            this.label2.Text = "Local File:";
            this.label2.TextAlign = System.Drawing.ContentAlignment.TopRight;
            // 
            // urlLabel
            // 
            this.urlLabel.Dock = System.Windows.Forms.DockStyle.Fill;
            this.urlLabel.Location = new System.Drawing.Point(279, 11);
            this.urlLabel.Margin = new System.Windows.Forms.Padding(6, 0, 6, 0);
            this.urlLabel.Name = "urlLabel";
            this.urlLabel.Size = new System.Drawing.Size(507, 41);
            this.urlLabel.TabIndex = 3;
            // 
            // transcriptBox
            // 
            this.transcriptBox.Dock = System.Windows.Forms.DockStyle.Fill;
            this.transcriptBox.Location = new System.Drawing.Point(0, 0);
            this.transcriptBox.Margin = new System.Windows.Forms.Padding(11, 13, 11, 13);
            this.transcriptBox.Name = "transcriptBox";
            this.transcriptBox.ReadOnly = true;
            this.transcriptBox.Size = new System.Drawing.Size(1005, 150);
            this.transcriptBox.TabIndex = 1;
            this.transcriptBox.Text = "";
            // 
            // toolStrip
            // 
            this.toolStrip.ContextMenuStrip = this.toolbarMenuStrip;
            this.toolStrip.GripStyle = System.Windows.Forms.ToolStripGripStyle.Hidden;
            this.toolStrip.ImageScalingSize = new System.Drawing.Size(32, 32);
            this.toolStrip.Items.AddRange(new System.Windows.Forms.ToolStripItem[] {
            this.resumeToolStripButton,
            this.reloadToolStripButton,
            this.stopToolStripButton,
            this.removeToolStripButton,
            this.cleanUptoolStripButton,
            this.showToolStripButton,
            this.openToolStripButton,
            this.logToolStripButton,
            this.trashToolStripButton});
            this.toolStrip.Location = new System.Drawing.Point(0, 0);
            this.toolStrip.Name = "toolStrip";
            this.toolStrip.Padding = new System.Windows.Forms.Padding(0, 0, 4, 0);
            this.toolStrip.Size = new System.Drawing.Size(1005, 74);
            this.toolStrip.TabIndex = 5;
            this.toolStrip.Text = "toolStrip1";
            // 
            // resumeToolStripButton
            // 
            this.resumeToolStripButton.AutoToolTip = false;
            this.resumeToolStripButton.Image = global::Ch.Cyberduck.ImageHelper.Resume;
            this.resumeToolStripButton.ImageTransparentColor = System.Drawing.Color.Magenta;
            this.resumeToolStripButton.Name = "resumeToolStripButton";
            this.resumeToolStripButton.Size = new System.Drawing.Size(103, 68);
            this.resumeToolStripButton.Text = "Resume";
            this.resumeToolStripButton.TextImageRelation = System.Windows.Forms.TextImageRelation.ImageAboveText;
            // 
            // reloadToolStripButton
            // 
            this.reloadToolStripButton.AutoToolTip = false;
            this.reloadToolStripButton.Image = global::Ch.Cyberduck.ImageHelper.Reload;
            this.reloadToolStripButton.ImageTransparentColor = System.Drawing.Color.Magenta;
            this.reloadToolStripButton.Name = "reloadToolStripButton";
            this.reloadToolStripButton.Size = new System.Drawing.Size(91, 68);
            this.reloadToolStripButton.Text = "Reload";
            this.reloadToolStripButton.TextImageRelation = System.Windows.Forms.TextImageRelation.ImageAboveText;
            // 
            // stopToolStripButton
            // 
            this.stopToolStripButton.AutoToolTip = false;
            this.stopToolStripButton.Image = global::Ch.Cyberduck.ImageHelper.Stop;
            this.stopToolStripButton.ImageTransparentColor = System.Drawing.Color.Magenta;
            this.stopToolStripButton.Name = "stopToolStripButton";
            this.stopToolStripButton.Size = new System.Drawing.Size(67, 68);
            this.stopToolStripButton.Text = "Stop";
            this.stopToolStripButton.TextImageRelation = System.Windows.Forms.TextImageRelation.ImageAboveText;
            // 
            // removeToolStripButton
            // 
            this.removeToolStripButton.AutoToolTip = false;
            this.removeToolStripButton.Image = global::Ch.Cyberduck.ImageHelper.Clean;
            this.removeToolStripButton.ImageTransparentColor = System.Drawing.Color.Magenta;
            this.removeToolStripButton.Name = "removeToolStripButton";
            this.removeToolStripButton.Size = new System.Drawing.Size(105, 68);
            this.removeToolStripButton.Text = "Remove";
            this.removeToolStripButton.TextImageRelation = System.Windows.Forms.TextImageRelation.ImageAboveText;
            // 
            // cleanUptoolStripButton
            // 
            this.cleanUptoolStripButton.AutoToolTip = false;
            this.cleanUptoolStripButton.Image = global::Ch.Cyberduck.ImageHelper.CleanAll;
            this.cleanUptoolStripButton.ImageTransparentColor = System.Drawing.Color.Magenta;
            this.cleanUptoolStripButton.Name = "cleanUptoolStripButton";
            this.cleanUptoolStripButton.Size = new System.Drawing.Size(116, 68);
            this.cleanUptoolStripButton.Text = "Clean Up";
            this.cleanUptoolStripButton.TextImageRelation = System.Windows.Forms.TextImageRelation.ImageAboveText;
            // 
            // showToolStripButton
            // 
            this.showToolStripButton.Alignment = System.Windows.Forms.ToolStripItemAlignment.Right;
            this.showToolStripButton.AutoToolTip = false;
            this.showToolStripButton.Image = global::Ch.Cyberduck.ImageHelper.Reveal;
            this.showToolStripButton.ImageTransparentColor = System.Drawing.Color.Magenta;
            this.showToolStripButton.Name = "showToolStripButton";
            this.showToolStripButton.Size = new System.Drawing.Size(77, 68);
            this.showToolStripButton.Text = "Show";
            this.showToolStripButton.TextImageRelation = System.Windows.Forms.TextImageRelation.ImageAboveText;
            // 
            // openToolStripButton
            // 
            this.openToolStripButton.Alignment = System.Windows.Forms.ToolStripItemAlignment.Right;
            this.openToolStripButton.AutoToolTip = false;
            this.openToolStripButton.Image = global::Ch.Cyberduck.ImageHelper.Open;
            this.openToolStripButton.ImageTransparentColor = System.Drawing.Color.Magenta;
            this.openToolStripButton.Name = "openToolStripButton";
            this.openToolStripButton.Size = new System.Drawing.Size(78, 68);
            this.openToolStripButton.Text = "Open";
            this.openToolStripButton.TextImageRelation = System.Windows.Forms.TextImageRelation.ImageAboveText;
            // 
            // logToolStripButton
            // 
            this.logToolStripButton.AutoToolTip = false;
            this.logToolStripButton.Image = global::Ch.Cyberduck.ImageHelper.Log;
            this.logToolStripButton.ImageTransparentColor = System.Drawing.Color.Magenta;
            this.logToolStripButton.Name = "logToolStripButton";
            this.logToolStripButton.Size = new System.Drawing.Size(58, 68);
            this.logToolStripButton.Text = "Log";
            this.logToolStripButton.TextImageRelation = System.Windows.Forms.TextImageRelation.ImageAboveText;
            // 
            // trashToolStripButton
            // 
            this.trashToolStripButton.AutoToolTip = false;
            this.trashToolStripButton.Image = global::Ch.Cyberduck.ImageHelper.Trash;
            this.trashToolStripButton.ImageTransparentColor = System.Drawing.Color.Magenta;
            this.trashToolStripButton.Name = "trashToolStripButton";
            this.trashToolStripButton.Size = new System.Drawing.Size(74, 68);
            this.trashToolStripButton.Text = "Trash";
            this.trashToolStripButton.TextImageRelation = System.Windows.Forms.TextImageRelation.ImageAboveText;
            // 
            // TransferForm
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(13F, 32F);
            this.CancelButton = this.cancelButton;
            this.ClientSize = new System.Drawing.Size(1005, 1135);
            this.Controls.Add(this.splitContainer);
            this.Controls.Add(this.toolStrip);
            this.Controls.Add(this.cancelButton);
            this.Margin = new System.Windows.Forms.Padding(13, 15, 13, 15);
            this.Name = "TransferForm";
            this.Text = "Transfers";
            this.toolbarMenuStrip.ResumeLayout(false);
            this.splitContainer.Panel1.ResumeLayout(false);
            this.splitContainer.Panel2.ResumeLayout(false);
            ((System.ComponentModel.ISupportInitialize)(this.splitContainer)).EndInit();
            this.splitContainer.ResumeLayout(false);
            this.panel2.ResumeLayout(false);
            ((System.ComponentModel.ISupportInitialize)(this.transferListView)).EndInit();
            this.tableLayoutPanel1.ResumeLayout(false);
            this.tableLayoutPanel1.PerformLayout();
            ((System.ComponentModel.ISupportInitialize)(this.fileIcon)).EndInit();
            this.toolStrip.ResumeLayout(false);
            this.toolStrip.PerformLayout();
            this.ResumeLayout(false);
            this.PerformLayout();

        }

        #endregion

        private System.Windows.Forms.ToolStripButton resumeToolStripButton;
        private System.Windows.Forms.ToolStripButton reloadToolStripButton;
        private System.Windows.Forms.ToolStripButton stopToolStripButton;
        private System.Windows.Forms.ToolStripButton removeToolStripButton;
        private System.Windows.Forms.ToolStripButton showToolStripButton;
        private System.Windows.Forms.ToolStripButton openToolStripButton;
        private System.Windows.Forms.ToolStripButton trashToolStripButton;
        private System.Windows.Forms.SplitContainer splitContainer;
        private System.Windows.Forms.Label label2;
        private System.Windows.Forms.Label label1;
        private System.Windows.Forms.Panel panel2;
        private ListViewControls transferListView;
        private BrightIdeasSoftware.OLVColumn dummyColumn;
        private BrightIdeasSoftware.OLVColumn transferColumn;
        private System.Windows.Forms.RichTextBox transcriptBox;
        private System.Windows.Forms.PictureBox fileIcon;
        private EllipsisLabel localLabel;
        private EllipsisLabel urlLabel;
        private SplitButton connectionsSplitButton;
        private System.Windows.Forms.ContextMenuStrip toolbarMenuStrip;
        private System.Windows.Forms.ToolStripMenuItem resumeToolStripMenuItem;
        private System.Windows.Forms.ToolStripMenuItem reloadToolStripMenuItem;
        private System.Windows.Forms.ToolStripMenuItem stopToolStripMenuItem;
        private System.Windows.Forms.ToolStripMenuItem removeToolStripMenuItem;
        private System.Windows.Forms.ToolStripMenuItem cleanUpToolStripMenuItem;
        private System.Windows.Forms.ToolStripMenuItem logToolStripMenuItem;
        private System.Windows.Forms.ToolStripSeparator toolStripSeparator1;
        private System.Windows.Forms.ToolStripMenuItem openToolStripMenuItem;
        private System.Windows.Forms.ToolStripMenuItem showToolStripMenuItem;
        private System.Windows.Forms.ToolStripButton cleanUptoolStripButton;
        private SplitButton bandwidthSplitButton;
        private System.Windows.Forms.ContextMenuStrip bandwidthMenuStrip;
        private System.Windows.Forms.ContextMenuStrip connectionsMenuStrip;
        private System.Windows.Forms.TableLayoutPanel tableLayoutPanel1;
        private ClickThroughToolStrip toolStrip;
        private System.Windows.Forms.Button cancelButton;
        private System.Windows.Forms.ToolStripButton logToolStripButton;
        private System.Windows.Forms.ToolStripMenuItem trashToolStripMenuItem;
    }
}
