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
            this.toolStrip = new System.Windows.Forms.ToolStrip();
            this.toolbarMenuStrip = new System.Windows.Forms.ContextMenuStrip(this.components);
            this.resumeToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.reloadToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.stopToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.removeToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.cleanUpToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.logToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.toolStripSeparator1 = new System.Windows.Forms.ToolStripSeparator();
            this.openToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.showToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.resumeToolStripButton = new System.Windows.Forms.ToolStripButton();
            this.reloadToolStripButton = new System.Windows.Forms.ToolStripButton();
            this.stopToolStripButton = new System.Windows.Forms.ToolStripButton();
            this.removeToolStripButton = new System.Windows.Forms.ToolStripButton();
            this.cleanUptoolStripButton = new System.Windows.Forms.ToolStripButton();
            this.showToolStripButton = new System.Windows.Forms.ToolStripButton();
            this.openToolStripButton = new System.Windows.Forms.ToolStripButton();
            this.logToolStripButton = new System.Windows.Forms.ToolStripButton();
            this.splitContainer = new System.Windows.Forms.SplitContainer();
            this.panel2 = new System.Windows.Forms.Panel();
            this.transferListView = new Ch.Cyberduck.Ui.Winforms.Controls.ListViewControls();
            this.dummyColumn = ((BrightIdeasSoftware.OLVColumn)(new BrightIdeasSoftware.OLVColumn()));
            this.transferColumn = ((BrightIdeasSoftware.OLVColumn)(new BrightIdeasSoftware.OLVColumn()));
            this.tableLayoutPanel1 = new System.Windows.Forms.TableLayoutPanel();
            this.bandwithSplitButton = new ch.cyberduck.ui.winforms.controls.SplitButton();
            this.bandwidthMenuStrip = new System.Windows.Forms.ContextMenuStrip(this.components);
            this.fileIcon = new System.Windows.Forms.PictureBox();
            this.queueSizeUpDown = new System.Windows.Forms.NumericUpDown();
            this.label1 = new System.Windows.Forms.Label();
            this.localLabel = new Ch.Cyberduck.Ui.Winforms.Controls.EllipsisLabel();
            this.label2 = new System.Windows.Forms.Label();
            this.urlLabel = new Ch.Cyberduck.Ui.Winforms.Controls.EllipsisLabel();
            this.transcriptBox = new System.Windows.Forms.RichTextBox();
            this.toolStrip.SuspendLayout();
            this.toolbarMenuStrip.SuspendLayout();
            this.splitContainer.Panel1.SuspendLayout();
            this.splitContainer.Panel2.SuspendLayout();
            this.splitContainer.SuspendLayout();
            this.panel2.SuspendLayout();
            ((System.ComponentModel.ISupportInitialize)(this.transferListView)).BeginInit();
            this.tableLayoutPanel1.SuspendLayout();
            ((System.ComponentModel.ISupportInitialize)(this.fileIcon)).BeginInit();
            ((System.ComponentModel.ISupportInitialize)(this.queueSizeUpDown)).BeginInit();
            this.SuspendLayout();
            // 
            // toolStrip
            // 
            this.toolStrip.ContextMenuStrip = this.toolbarMenuStrip;
            this.toolStrip.GripStyle = System.Windows.Forms.ToolStripGripStyle.Hidden;
            this.toolStrip.ImageScalingSize = new System.Drawing.Size(32, 32);
            this.toolStrip.Location = new System.Drawing.Point(0, 0);
            this.toolStrip.Name = "toolStrip";
            this.toolStrip.Size = new System.Drawing.Size(541, 54);
            this.toolStrip.TabIndex = 5;
            this.toolStrip.Text = "toolStrip1";
            // 
            // toolbarMenuStrip
            // 
            this.toolbarMenuStrip.Items.AddRange(new System.Windows.Forms.ToolStripItem[] {
            this.resumeToolStripMenuItem,
            this.reloadToolStripMenuItem,
            this.stopToolStripMenuItem,
            this.removeToolStripMenuItem,
            this.cleanUpToolStripMenuItem,
            this.logToolStripMenuItem,
            this.toolStripSeparator1,
            this.openToolStripMenuItem,
            this.showToolStripMenuItem});
            this.toolbarMenuStrip.Name = "toolbarMenuStrip";
            this.toolbarMenuStrip.Size = new System.Drawing.Size(123, 186);
            this.toolbarMenuStrip.Closing += new System.Windows.Forms.ToolStripDropDownClosingEventHandler(this.toolbarMenuStrip_Closing);
            this.toolbarMenuStrip.ItemClicked += new System.Windows.Forms.ToolStripItemClickedEventHandler(this.toolbarMenuStrip_ItemClicked);
            // 
            // resumeToolStripMenuItem
            // 
            this.resumeToolStripMenuItem.Name = "resumeToolStripMenuItem";
            this.resumeToolStripMenuItem.Size = new System.Drawing.Size(122, 22);
            this.resumeToolStripMenuItem.Text = "Resume";
            // 
            // reloadToolStripMenuItem
            // 
            this.reloadToolStripMenuItem.Name = "reloadToolStripMenuItem";
            this.reloadToolStripMenuItem.Size = new System.Drawing.Size(122, 22);
            this.reloadToolStripMenuItem.Text = "Reload";
            // 
            // stopToolStripMenuItem
            // 
            this.stopToolStripMenuItem.Name = "stopToolStripMenuItem";
            this.stopToolStripMenuItem.Size = new System.Drawing.Size(122, 22);
            this.stopToolStripMenuItem.Text = "Stop";
            // 
            // removeToolStripMenuItem
            // 
            this.removeToolStripMenuItem.Name = "removeToolStripMenuItem";
            this.removeToolStripMenuItem.Size = new System.Drawing.Size(122, 22);
            this.removeToolStripMenuItem.Text = "Remove";
            // 
            // cleanUpToolStripMenuItem
            // 
            this.cleanUpToolStripMenuItem.Name = "cleanUpToolStripMenuItem";
            this.cleanUpToolStripMenuItem.Size = new System.Drawing.Size(122, 22);
            this.cleanUpToolStripMenuItem.Text = "Clean Up";
            // 
            // logToolStripMenuItem
            // 
            this.logToolStripMenuItem.Name = "logToolStripMenuItem";
            this.logToolStripMenuItem.Size = new System.Drawing.Size(122, 22);
            this.logToolStripMenuItem.Text = "Log";
            // 
            // toolStripSeparator1
            // 
            this.toolStripSeparator1.Name = "toolStripSeparator1";
            this.toolStripSeparator1.Size = new System.Drawing.Size(119, 6);
            // 
            // openToolStripMenuItem
            // 
            this.openToolStripMenuItem.Name = "openToolStripMenuItem";
            this.openToolStripMenuItem.Size = new System.Drawing.Size(122, 22);
            this.openToolStripMenuItem.Text = "Open";
            // 
            // showToolStripMenuItem
            // 
            this.showToolStripMenuItem.Name = "showToolStripMenuItem";
            this.showToolStripMenuItem.Size = new System.Drawing.Size(122, 22);
            this.showToolStripMenuItem.Text = "Show";
            // 
            // resumeToolStripButton
            // 
            this.resumeToolStripButton.AutoToolTip = false;
            this.resumeToolStripButton.Image = global::Ch.Cyberduck.ResourcesBundle.resume;
            this.resumeToolStripButton.ImageTransparentColor = System.Drawing.Color.Magenta;
            this.resumeToolStripButton.Name = "resumeToolStripButton";
            this.resumeToolStripButton.Size = new System.Drawing.Size(53, 51);
            this.resumeToolStripButton.Text = "Resume";
            this.resumeToolStripButton.TextImageRelation = System.Windows.Forms.TextImageRelation.ImageAboveText;
            // 
            // reloadToolStripButton
            // 
            this.reloadToolStripButton.AutoToolTip = false;
            this.reloadToolStripButton.Image = global::Ch.Cyberduck.ResourcesBundle.reload;
            this.reloadToolStripButton.ImageTransparentColor = System.Drawing.Color.Magenta;
            this.reloadToolStripButton.Name = "reloadToolStripButton";
            this.reloadToolStripButton.Size = new System.Drawing.Size(47, 51);
            this.reloadToolStripButton.Text = "Reload";
            this.reloadToolStripButton.TextImageRelation = System.Windows.Forms.TextImageRelation.ImageAboveText;
            // 
            // stopToolStripButton
            // 
            this.stopToolStripButton.AutoToolTip = false;
            this.stopToolStripButton.Image = global::Ch.Cyberduck.ResourcesBundle.stop;
            this.stopToolStripButton.ImageTransparentColor = System.Drawing.Color.Magenta;
            this.stopToolStripButton.Name = "stopToolStripButton";
            this.stopToolStripButton.Size = new System.Drawing.Size(36, 51);
            this.stopToolStripButton.Text = "Stop";
            this.stopToolStripButton.TextImageRelation = System.Windows.Forms.TextImageRelation.ImageAboveText;
            // 
            // removeToolStripButton
            // 
            this.removeToolStripButton.AutoToolTip = false;
            this.removeToolStripButton.Image = global::Ch.Cyberduck.ResourcesBundle.clean;
            this.removeToolStripButton.ImageTransparentColor = System.Drawing.Color.Magenta;
            this.removeToolStripButton.Name = "removeToolStripButton";
            this.removeToolStripButton.Size = new System.Drawing.Size(54, 51);
            this.removeToolStripButton.Text = "Remove";
            this.removeToolStripButton.TextImageRelation = System.Windows.Forms.TextImageRelation.ImageAboveText;
            // 
            // cleanUptoolStripButton
            // 
            this.cleanUptoolStripButton.AutoToolTip = false;
            this.cleanUptoolStripButton.Image = global::Ch.Cyberduck.ResourcesBundle.cleanAll;
            this.cleanUptoolStripButton.ImageTransparentColor = System.Drawing.Color.Magenta;
            this.cleanUptoolStripButton.Name = "cleanUptoolStripButton";
            this.cleanUptoolStripButton.Size = new System.Drawing.Size(59, 51);
            this.cleanUptoolStripButton.Text = "Clean Up";
            this.cleanUptoolStripButton.TextImageRelation = System.Windows.Forms.TextImageRelation.ImageAboveText;
            // 
            // showToolStripButton
            // 
            this.showToolStripButton.Alignment = System.Windows.Forms.ToolStripItemAlignment.Right;
            this.showToolStripButton.AutoToolTip = false;
            this.showToolStripButton.ImageTransparentColor = System.Drawing.Color.Magenta;
            this.showToolStripButton.Name = "showToolStripButton";
            this.showToolStripButton.Size = new System.Drawing.Size(40, 51);
            this.showToolStripButton.Text = "Show";
            this.showToolStripButton.TextImageRelation = System.Windows.Forms.TextImageRelation.ImageAboveText;
            // 
            // openToolStripButton
            // 
            this.openToolStripButton.Alignment = System.Windows.Forms.ToolStripItemAlignment.Right;
            this.openToolStripButton.AutoToolTip = false;
            this.openToolStripButton.Image = global::Ch.Cyberduck.ResourcesBundle.open;
            this.openToolStripButton.ImageTransparentColor = System.Drawing.Color.Magenta;
            this.openToolStripButton.Name = "openToolStripButton";
            this.openToolStripButton.Size = new System.Drawing.Size(40, 51);
            this.openToolStripButton.Text = "Open";
            this.openToolStripButton.TextImageRelation = System.Windows.Forms.TextImageRelation.ImageAboveText;
            // 
            // logToolStripButton
            // 
            this.logToolStripButton.AutoToolTip = false;
            this.logToolStripButton.Image = global::Ch.Cyberduck.ResourcesBundle.log;
            this.logToolStripButton.ImageTransparentColor = System.Drawing.Color.Magenta;
            this.logToolStripButton.Name = "logToolStripButton";
            this.logToolStripButton.Size = new System.Drawing.Size(36, 51);
            this.logToolStripButton.Text = "Log";
            this.logToolStripButton.TextImageRelation = System.Windows.Forms.TextImageRelation.ImageAboveText;
            // 
            // splitContainer
            // 
            this.splitContainer.Dock = System.Windows.Forms.DockStyle.Fill;
            this.splitContainer.FixedPanel = System.Windows.Forms.FixedPanel.Panel2;
            this.splitContainer.Location = new System.Drawing.Point(0, 54);
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
            this.splitContainer.Size = new System.Drawing.Size(541, 478);
            this.splitContainer.SplitterDistance = 343;
            this.splitContainer.SplitterWidth = 5;
            this.splitContainer.TabIndex = 6;
            this.splitContainer.SplitterMoved += new System.Windows.Forms.SplitterEventHandler(this.splitContainer_SplitterMoved);
            // 
            // panel2
            // 
            this.panel2.Controls.Add(this.transferListView);
            this.panel2.Controls.Add(this.tableLayoutPanel1);
            this.panel2.Dock = System.Windows.Forms.DockStyle.Fill;
            this.panel2.Location = new System.Drawing.Point(0, 0);
            this.panel2.Name = "panel2";
            this.panel2.Size = new System.Drawing.Size(541, 343);
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
            this.transferListView.Location = new System.Drawing.Point(0, 0);
            this.transferListView.Name = "transferListView";
            this.transferListView.RowHeight = 85;
            this.transferListView.Size = new System.Drawing.Size(541, 299);
            this.transferListView.TabIndex = 6;
            this.transferListView.UseCompatibleStateImageBehavior = false;
            this.transferListView.View = System.Windows.Forms.View.Details;
            this.transferListView.DoubleClick += new System.EventHandler(this.transferListView_DoubleClick);
            this.transferListView.KeyDown += new System.Windows.Forms.KeyEventHandler(this.transferListView_KeyDown);
            // 
            // transferColumn
            // 
            this.transferColumn.Width = 400;
            // 
            // tableLayoutPanel1
            // 
            this.tableLayoutPanel1.ColumnCount = 5;
            this.tableLayoutPanel1.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle(System.Windows.Forms.SizeType.Absolute, 40F));
            this.tableLayoutPanel1.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle(System.Windows.Forms.SizeType.Absolute, 100F));
            this.tableLayoutPanel1.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle(System.Windows.Forms.SizeType.Percent, 100F));
            this.tableLayoutPanel1.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle(System.Windows.Forms.SizeType.Absolute, 40F));
            this.tableLayoutPanel1.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle(System.Windows.Forms.SizeType.Absolute, 55F));
            this.tableLayoutPanel1.Controls.Add(this.bandwithSplitButton, 4, 0);
            this.tableLayoutPanel1.Controls.Add(this.fileIcon, 0, 0);
            this.tableLayoutPanel1.Controls.Add(this.queueSizeUpDown, 3, 0);
            this.tableLayoutPanel1.Controls.Add(this.label1, 1, 0);
            this.tableLayoutPanel1.Controls.Add(this.localLabel, 2, 1);
            this.tableLayoutPanel1.Controls.Add(this.label2, 1, 1);
            this.tableLayoutPanel1.Controls.Add(this.urlLabel, 2, 0);
            this.tableLayoutPanel1.Dock = System.Windows.Forms.DockStyle.Bottom;
            this.tableLayoutPanel1.Location = new System.Drawing.Point(0, 299);
            this.tableLayoutPanel1.Name = "tableLayoutPanel1";
            this.tableLayoutPanel1.Padding = new System.Windows.Forms.Padding(0, 3, 0, 0);
            this.tableLayoutPanel1.RowCount = 2;
            this.tableLayoutPanel1.RowStyles.Add(new System.Windows.Forms.RowStyle(System.Windows.Forms.SizeType.Percent, 50F));
            this.tableLayoutPanel1.RowStyles.Add(new System.Windows.Forms.RowStyle(System.Windows.Forms.SizeType.Percent, 50F));
            this.tableLayoutPanel1.Size = new System.Drawing.Size(541, 44);
            this.tableLayoutPanel1.TabIndex = 8;
            // 
            // bandwithSplitButton
            // 
            this.bandwithSplitButton.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Left | System.Windows.Forms.AnchorStyles.Right)));
            this.bandwithSplitButton.ContextMenuStrip = this.bandwidthMenuStrip;
            this.bandwithSplitButton.Location = new System.Drawing.Point(489, 12);
            this.bandwithSplitButton.Name = "bandwithSplitButton";
            this.tableLayoutPanel1.SetRowSpan(this.bandwithSplitButton, 2);
            this.bandwithSplitButton.Size = new System.Drawing.Size(49, 23);
            this.bandwithSplitButton.SplitMenuStrip = this.bandwidthMenuStrip;
            this.bandwithSplitButton.TabIndex = 7;
            this.bandwithSplitButton.UseVisualStyleBackColor = true;
            this.bandwithSplitButton.Click += new System.EventHandler(this.bandwithSplitButton_Click);
            // 
            // bandwidthMenuStrip
            // 
            this.bandwidthMenuStrip.Name = "bandwidthMenuStrip";
            this.bandwidthMenuStrip.Size = new System.Drawing.Size(61, 4);
            // 
            // fileIcon
            // 
            this.fileIcon.Location = new System.Drawing.Point(3, 6);
            this.fileIcon.Name = "fileIcon";
            this.tableLayoutPanel1.SetRowSpan(this.fileIcon, 2);
            this.fileIcon.Size = new System.Drawing.Size(32, 32);
            this.fileIcon.TabIndex = 2;
            this.fileIcon.TabStop = false;
            // 
            // queueSizeUpDown
            // 
            this.queueSizeUpDown.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Left | System.Windows.Forms.AnchorStyles.Right)));
            this.queueSizeUpDown.Location = new System.Drawing.Point(449, 12);
            this.queueSizeUpDown.Maximum = new decimal(new int[] {
            9,
            0,
            0,
            0});
            this.queueSizeUpDown.Minimum = new decimal(new int[] {
            1,
            0,
            0,
            0});
            this.queueSizeUpDown.Name = "queueSizeUpDown";
            this.tableLayoutPanel1.SetRowSpan(this.queueSizeUpDown, 2);
            this.queueSizeUpDown.Size = new System.Drawing.Size(34, 23);
            this.queueSizeUpDown.TabIndex = 6;
            this.queueSizeUpDown.Value = new decimal(new int[] {
            5,
            0,
            0,
            0});
            this.queueSizeUpDown.ValueChanged += new System.EventHandler(this.queueSizeUpDown_ValueChanged);
            // 
            // label1
            // 
            this.label1.Dock = System.Windows.Forms.DockStyle.Fill;
            this.label1.Location = new System.Drawing.Point(43, 3);
            this.label1.Name = "label1";
            this.label1.Size = new System.Drawing.Size(94, 20);
            this.label1.TabIndex = 0;
            this.label1.Text = "URL:";
            this.label1.TextAlign = System.Drawing.ContentAlignment.TopRight;
            // 
            // localLabel
            // 
            this.localLabel.AutoSize = true;
            this.localLabel.Dock = System.Windows.Forms.DockStyle.Fill;
            this.localLabel.Location = new System.Drawing.Point(143, 23);
            this.localLabel.Name = "localLabel";
            this.localLabel.Size = new System.Drawing.Size(300, 21);
            this.localLabel.TabIndex = 4;
            // 
            // label2
            // 
            this.label2.Dock = System.Windows.Forms.DockStyle.Fill;
            this.label2.Location = new System.Drawing.Point(43, 23);
            this.label2.Name = "label2";
            this.label2.Size = new System.Drawing.Size(94, 21);
            this.label2.TabIndex = 1;
            this.label2.Text = "Local File:";
            this.label2.TextAlign = System.Drawing.ContentAlignment.TopRight;
            // 
            // urlLabel
            // 
            this.urlLabel.Dock = System.Windows.Forms.DockStyle.Fill;
            this.urlLabel.Location = new System.Drawing.Point(143, 3);
            this.urlLabel.Name = "urlLabel";
            this.urlLabel.Size = new System.Drawing.Size(300, 20);
            this.urlLabel.TabIndex = 3;
            // 
            // transcriptBox
            // 
            this.transcriptBox.Dock = System.Windows.Forms.DockStyle.Fill;
            this.transcriptBox.Location = new System.Drawing.Point(0, 0);
            this.transcriptBox.Name = "transcriptBox";
            this.transcriptBox.ReadOnly = true;
            this.transcriptBox.Size = new System.Drawing.Size(541, 130);
            this.transcriptBox.TabIndex = 1;
            this.transcriptBox.Text = "";
            // 
            // TransferForm
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(7F, 15F);
            this.ClientSize = new System.Drawing.Size(541, 532);
            this.Controls.Add(this.splitContainer);
            this.Controls.Add(this.toolStrip);
            this.Name = "TransferForm";
            this.Text = "Transfers";
            this.toolStrip.ResumeLayout(false);
            this.toolStrip.PerformLayout();
            this.toolbarMenuStrip.ResumeLayout(false);
            this.splitContainer.Panel1.ResumeLayout(false);
            this.splitContainer.Panel2.ResumeLayout(false);
            this.splitContainer.ResumeLayout(false);
            this.panel2.ResumeLayout(false);
            ((System.ComponentModel.ISupportInitialize)(this.transferListView)).EndInit();
            this.tableLayoutPanel1.ResumeLayout(false);
            this.tableLayoutPanel1.PerformLayout();
            ((System.ComponentModel.ISupportInitialize)(this.fileIcon)).EndInit();
            ((System.ComponentModel.ISupportInitialize)(this.queueSizeUpDown)).EndInit();
            this.ResumeLayout(false);
            this.PerformLayout();

        }

        #endregion

        private System.Windows.Forms.ToolStrip toolStrip;
        private System.Windows.Forms.ToolStripButton resumeToolStripButton;
        private System.Windows.Forms.ToolStripButton reloadToolStripButton;
        private System.Windows.Forms.ToolStripButton stopToolStripButton;
        private System.Windows.Forms.ToolStripButton removeToolStripButton;
        private System.Windows.Forms.ToolStripButton showToolStripButton;
        private System.Windows.Forms.ToolStripButton openToolStripButton;
        private System.Windows.Forms.ToolStripButton logToolStripButton;
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
        private System.Windows.Forms.NumericUpDown queueSizeUpDown;
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
        private ch.cyberduck.ui.winforms.controls.SplitButton bandwithSplitButton;
        private System.Windows.Forms.ContextMenuStrip bandwidthMenuStrip;
        private System.Windows.Forms.TableLayoutPanel tableLayoutPanel1;
    }
}
