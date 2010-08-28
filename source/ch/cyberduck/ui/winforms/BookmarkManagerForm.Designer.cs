using Ch.Cyberduck.Ui.Winforms.Controls;

namespace Ch.Cyberduck.Ui.Winforms
{
    partial class BookmarkManagerForm
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
            this.actionToolStrip = new System.Windows.Forms.ToolStrip();
            this.addToolStripButton = new System.Windows.Forms.ToolStripButton();
            this.editToolStripButton = new System.Windows.Forms.ToolStripButton();
            this.removeToolStripButton = new System.Windows.Forms.ToolStripButton();
            this.imageList1 = new System.Windows.Forms.ImageList(this.components);
            this.panelManager = new PanelManager();
            this.managedBookmarkPanel = new ManagedPanel();
            this.managedHistoryPanel = new ManagedPanel();
            this.historyListView = new BrightIdeasSoftware.ObjectListView();
            this.olvColumn1 = new BrightIdeasSoftware.OLVColumn();
            this.olvColumn2 = new BrightIdeasSoftware.OLVColumn();
            this.olvColumn3 = new BrightIdeasSoftware.OLVColumn();
            this.managedBonjourPanel = new ManagedPanel();
            this.bookmarkListView = new BrightIdeasSoftware.ObjectListView();
            this.bookmarkImageColumn = new BrightIdeasSoftware.OLVColumn();
            this.descriptionColumn = new BrightIdeasSoftware.OLVColumn();
            this.activeColumn = new BrightIdeasSoftware.OLVColumn();
            this.actionToolStrip.SuspendLayout();
            this.panelManager.SuspendLayout();
            this.managedBookmarkPanel.SuspendLayout();
            this.managedHistoryPanel.SuspendLayout();
            ((System.ComponentModel.ISupportInitialize)(this.historyListView)).BeginInit();
            ((System.ComponentModel.ISupportInitialize)(this.bookmarkListView)).BeginInit();
            this.SuspendLayout();
            // 
            // actionToolStrip
            // 
            this.actionToolStrip.AutoSize = false;
            this.actionToolStrip.Dock = System.Windows.Forms.DockStyle.Bottom;
            this.actionToolStrip.GripMargin = new System.Windows.Forms.Padding(0);
            this.actionToolStrip.GripStyle = System.Windows.Forms.ToolStripGripStyle.Hidden;
            this.actionToolStrip.ImageScalingSize = new System.Drawing.Size(30, 30);
            this.actionToolStrip.Items.AddRange(new System.Windows.Forms.ToolStripItem[] {
            this.addToolStripButton,
            this.editToolStripButton,
            this.removeToolStripButton});
            this.actionToolStrip.LayoutStyle = System.Windows.Forms.ToolStripLayoutStyle.HorizontalStackWithOverflow;
            this.actionToolStrip.Location = new System.Drawing.Point(0, 490);
            this.actionToolStrip.Name = "actionToolStrip";
            this.actionToolStrip.ShowItemToolTips = false;
            this.actionToolStrip.Size = new System.Drawing.Size(629, 34);
            this.actionToolStrip.Stretch = true;
            this.actionToolStrip.TabIndex = 4;
            this.actionToolStrip.Text = "toolStrip1";
            // 
            // addToolStripButton
            // 
            this.addToolStripButton.AutoSize = false;
            this.addToolStripButton.DisplayStyle = System.Windows.Forms.ToolStripItemDisplayStyle.Image;
            this.addToolStripButton.Image = global::Ch.Cyberduck.ResourcesBundle.add;
            this.addToolStripButton.ImageScaling = System.Windows.Forms.ToolStripItemImageScaling.None;
            this.addToolStripButton.ImageTransparentColor = System.Drawing.Color.Magenta;
            this.addToolStripButton.Margin = new System.Windows.Forms.Padding(16, 1, 0, 2);
            this.addToolStripButton.Name = "addToolStripButton";
            this.addToolStripButton.Size = new System.Drawing.Size(23, 22);
            this.addToolStripButton.Text = "toolStripButton1";
            // 
            // editToolStripButton
            // 
            this.editToolStripButton.AutoSize = false;
            this.editToolStripButton.DisplayStyle = System.Windows.Forms.ToolStripItemDisplayStyle.Image;
            this.editToolStripButton.Image = global::Ch.Cyberduck.ResourcesBundle.edit;
            this.editToolStripButton.ImageScaling = System.Windows.Forms.ToolStripItemImageScaling.None;
            this.editToolStripButton.ImageTransparentColor = System.Drawing.Color.Magenta;
            this.editToolStripButton.Name = "editToolStripButton";
            this.editToolStripButton.Size = new System.Drawing.Size(23, 22);
            this.editToolStripButton.Text = "toolStripButton2";
            // 
            // removeToolStripButton
            // 
            this.removeToolStripButton.AutoSize = false;
            this.removeToolStripButton.DisplayStyle = System.Windows.Forms.ToolStripItemDisplayStyle.Image;
            this.removeToolStripButton.Image = global::Ch.Cyberduck.ResourcesBundle.remove;
            this.removeToolStripButton.ImageScaling = System.Windows.Forms.ToolStripItemImageScaling.None;
            this.removeToolStripButton.ImageTransparentColor = System.Drawing.Color.Magenta;
            this.removeToolStripButton.Margin = new System.Windows.Forms.Padding(-1, 1, 0, 2);
            this.removeToolStripButton.Name = "removeToolStripButton";
            this.removeToolStripButton.Size = new System.Drawing.Size(22, 22);
            this.removeToolStripButton.Text = "toolStripButton3";
            // 
            // imageList1
            // 
            this.imageList1.ColorDepth = System.Windows.Forms.ColorDepth.Depth32Bit;
            this.imageList1.ImageSize = new System.Drawing.Size(16, 16);
            this.imageList1.TransparentColor = System.Drawing.Color.Transparent;
            // 
            // panelManager
            // 
            this.panelManager.Controls.Add(this.managedBookmarkPanel);
            this.panelManager.Controls.Add(this.managedHistoryPanel);
            this.panelManager.Controls.Add(this.managedBonjourPanel);
            this.panelManager.Dock = System.Windows.Forms.DockStyle.Fill;
            this.panelManager.Location = new System.Drawing.Point(0, 0);
            this.panelManager.Name = "panelManager";
            this.panelManager.SelectedIndex = 0;
            this.panelManager.SelectedPanel = this.managedBookmarkPanel;
            this.panelManager.Size = new System.Drawing.Size(629, 490);
            this.panelManager.TabIndex = 8;
            // 
            // managedBookmarkPanel
            // 
            this.managedBookmarkPanel.Controls.Add(this.bookmarkListView);
            this.managedBookmarkPanel.Location = new System.Drawing.Point(0, 0);
            this.managedBookmarkPanel.Name = "managedBookmarkPanel";
            this.managedBookmarkPanel.Size = new System.Drawing.Size(629, 490);
            this.managedBookmarkPanel.Text = "managedPanel1";
            // 
            // managedHistoryPanel
            // 
            this.managedHistoryPanel.Controls.Add(this.historyListView);
            this.managedHistoryPanel.Location = new System.Drawing.Point(0, 0);
            this.managedHistoryPanel.Name = "managedHistoryPanel";
            this.managedHistoryPanel.Size = new System.Drawing.Size(629, 490);
            this.managedHistoryPanel.Text = "managedPanel2";
            // 
            // historyListView
            // 
            this.historyListView.AllColumns.Add(this.olvColumn1);
            this.historyListView.AllColumns.Add(this.olvColumn2);
            this.historyListView.AllColumns.Add(this.olvColumn3);
            this.historyListView.BorderStyle = System.Windows.Forms.BorderStyle.None;
            this.historyListView.Columns.AddRange(new System.Windows.Forms.ColumnHeader[] {
            this.olvColumn1,
            this.olvColumn2,
            this.olvColumn3});
            this.historyListView.Cursor = System.Windows.Forms.Cursors.Default;
            this.historyListView.Dock = System.Windows.Forms.DockStyle.Fill;
            this.historyListView.FullRowSelect = true;
            this.historyListView.Location = new System.Drawing.Point(0, 0);
            this.historyListView.Name = "historyListView";
            this.historyListView.OwnerDraw = true;
            this.historyListView.RowHeight = 37;
            this.historyListView.ShowGroups = false;
            this.historyListView.Size = new System.Drawing.Size(629, 490);
            this.historyListView.TabIndex = 8;
            this.historyListView.UseCompatibleStateImageBehavior = false;
            this.historyListView.View = System.Windows.Forms.View.Details;
            // 
            // olvColumn1
            // 
            this.olvColumn1.AspectName = "";
            this.olvColumn1.IsEditable = false;
            this.olvColumn1.Text = "";
            this.olvColumn1.Width = 32;
            // 
            // olvColumn2
            // 
            this.olvColumn2.AspectName = "";
            this.olvColumn2.Text = "";
            this.olvColumn2.Width = 200;
            // 
            // olvColumn3
            // 
            this.olvColumn3.Text = "";
            // 
            // managedBonjourPanel
            // 
            this.managedBonjourPanel.Location = new System.Drawing.Point(0, 0);
            this.managedBonjourPanel.Name = "managedBonjourPanel";
            this.managedBonjourPanel.Size = new System.Drawing.Size(0, 0);
            this.managedBonjourPanel.Text = "managedPanel3";
            // 
            // bookmarkListView
            // 
            this.bookmarkListView.AllColumns.Add(this.bookmarkImageColumn);
            this.bookmarkListView.AllColumns.Add(this.descriptionColumn);
            this.bookmarkListView.AllColumns.Add(this.activeColumn);
            this.bookmarkListView.BorderStyle = System.Windows.Forms.BorderStyle.None;
            this.bookmarkListView.Columns.AddRange(new System.Windows.Forms.ColumnHeader[] {
            this.bookmarkImageColumn,
            this.descriptionColumn,
            this.activeColumn});
            this.bookmarkListView.Cursor = System.Windows.Forms.Cursors.Default;
            this.bookmarkListView.Dock = System.Windows.Forms.DockStyle.Fill;
            this.bookmarkListView.FullRowSelect = true;
            this.bookmarkListView.Location = new System.Drawing.Point(0, 0);
            this.bookmarkListView.Name = "bookmarkListView";
            this.bookmarkListView.OwnerDraw = true;
            this.bookmarkListView.RowHeight = 37;
            this.bookmarkListView.ShowGroups = false;
            this.bookmarkListView.Size = new System.Drawing.Size(629, 490);
            this.bookmarkListView.TabIndex = 8;
            this.bookmarkListView.UseCompatibleStateImageBehavior = false;
            this.bookmarkListView.View = System.Windows.Forms.View.Details;
            // 
            // bookmarkImageColumn
            // 
            this.bookmarkImageColumn.AspectName = "";
            this.bookmarkImageColumn.IsEditable = false;
            this.bookmarkImageColumn.Text = "";
            this.bookmarkImageColumn.Width = 32;
            // 
            // descriptionColumn
            // 
            this.descriptionColumn.AspectName = "";
            this.descriptionColumn.Text = "";
            this.descriptionColumn.Width = 200;
            // 
            // activeColumn
            // 
            this.activeColumn.Text = "";
            // 
            // BookmarkManagerForm
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(7F, 15F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.ClientSize = new System.Drawing.Size(629, 524);
            this.Controls.Add(this.panelManager);
            this.Controls.Add(this.actionToolStrip);
            this.Name = "BookmarkManagerForm";
            this.Text = "Bookmarks";
            this.actionToolStrip.ResumeLayout(false);
            this.actionToolStrip.PerformLayout();
            this.panelManager.ResumeLayout(false);
            this.managedBookmarkPanel.ResumeLayout(false);
            this.managedHistoryPanel.ResumeLayout(false);
            ((System.ComponentModel.ISupportInitialize)(this.historyListView)).EndInit();
            ((System.ComponentModel.ISupportInitialize)(this.bookmarkListView)).EndInit();
            this.ResumeLayout(false);

        }

        #endregion

        private System.Windows.Forms.ToolStrip actionToolStrip;
        private System.Windows.Forms.ToolStripButton addToolStripButton;
        private System.Windows.Forms.ToolStripButton editToolStripButton;
        private System.Windows.Forms.ToolStripButton removeToolStripButton;
        private System.Windows.Forms.ImageList imageList1;
        private PanelManager panelManager;
        private ManagedPanel managedBookmarkPanel;
        private ManagedPanel managedHistoryPanel;
        private ManagedPanel managedBonjourPanel;
        private BrightIdeasSoftware.ObjectListView historyListView;
        private BrightIdeasSoftware.OLVColumn olvColumn1;
        private BrightIdeasSoftware.OLVColumn olvColumn2;
        private BrightIdeasSoftware.OLVColumn olvColumn3;
        private BrightIdeasSoftware.ObjectListView bookmarkListView;
        private BrightIdeasSoftware.OLVColumn bookmarkImageColumn;
        private BrightIdeasSoftware.OLVColumn descriptionColumn;
        private BrightIdeasSoftware.OLVColumn activeColumn;



    }
}