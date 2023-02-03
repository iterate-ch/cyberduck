using System.Windows.Forms;
using Ch.Cyberduck.Ui.Winforms.Controls;

namespace Ch.Cyberduck.Ui.Winforms
{
    partial class TransferPromptForm
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
            System.ComponentModel.ComponentResourceManager resources = new System.ComponentModel.ComponentResourceManager(typeof(TransferPromptForm));
            this.comboBoxAction = new System.Windows.Forms.ComboBox();
            this.browser = new Ch.Cyberduck.Ui.Winforms.Controls.MulticolorTreeListView();
            this.treeColumnName = ((BrightIdeasSoftware.OLVColumn)(new BrightIdeasSoftware.OLVColumn()));
            this.treeColumnSize = ((BrightIdeasSoftware.OLVColumn)(new BrightIdeasSoftware.OLVColumn()));
            this.treeColumnWarning = ((BrightIdeasSoftware.OLVColumn)(new BrightIdeasSoftware.OLVColumn()));
            this.treeColumnSync = ((BrightIdeasSoftware.OLVColumn)(new BrightIdeasSoftware.OLVColumn()));
            this.treeColumnCreate = ((BrightIdeasSoftware.OLVColumn)(new BrightIdeasSoftware.OLVColumn()));
            this.localFileUrl = new Ch.Cyberduck.Ui.Winforms.Controls.EllipsisLabel();
            this.localFileModificationDate = new Ch.Cyberduck.Ui.Winforms.Controls.EllipsisLabel();
            this.localFileSize = new Ch.Cyberduck.Ui.Winforms.Controls.EllipsisLabel();
            this.remoteFileModificationDate = new Ch.Cyberduck.Ui.Winforms.Controls.EllipsisLabel();
            this.remoteFileSize = new Ch.Cyberduck.Ui.Winforms.Controls.EllipsisLabel();
            this.remoteFileUrl = new Ch.Cyberduck.Ui.Winforms.Controls.EllipsisLabel();
            this.label1 = new System.Windows.Forms.Label();
            this.labelRemoteFile = new System.Windows.Forms.Label();
            this.cancelButton = new System.Windows.Forms.Button();
            this.continueButton = new System.Windows.Forms.Button();
            this.mainTableLayoutPanel = new System.Windows.Forms.TableLayoutPanel();
            this.animation = new System.Windows.Forms.PictureBox();
            this.separatorLine = new System.Windows.Forms.Label();
            this.statusLabel = new System.Windows.Forms.Label();
            this.toggleDetailsLabel = new System.Windows.Forms.Label();
            this.imageList = new System.Windows.Forms.ImageList(this.components);
            this.detailsTableLayoutPanel = new System.Windows.Forms.TableLayoutPanel();
            ((System.ComponentModel.ISupportInitialize)(this.browser)).BeginInit();
            this.mainTableLayoutPanel.SuspendLayout();
            ((System.ComponentModel.ISupportInitialize)(this.animation)).BeginInit();
            this.detailsTableLayoutPanel.SuspendLayout();
            this.SuspendLayout();
            // 
            // comboBoxAction
            // 
            this.comboBoxAction.Anchor = ((System.Windows.Forms.AnchorStyles)(((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Left)
                        | System.Windows.Forms.AnchorStyles.Right)));
            this.mainTableLayoutPanel.SetColumnSpan(this.comboBoxAction, 4);
            this.comboBoxAction.DropDownStyle = System.Windows.Forms.ComboBoxStyle.DropDownList;
            this.comboBoxAction.FormattingEnabled = true;
            this.comboBoxAction.Location = new System.Drawing.Point(13, 13);
            this.comboBoxAction.Name = "comboBoxAction";
            this.comboBoxAction.Size = new System.Drawing.Size(689, 23);
            this.comboBoxAction.TabIndex = 0;
            this.comboBoxAction.SelectionChangeCommitted += new System.EventHandler(this.comboBoxAction_SelectionChangeCommitted);
            // 
            // browser
            // 
            this.browser.ActiveForegroudColor = System.Drawing.SystemColors.ControlText;
            this.browser.ActiveGetterTransferItem = null;
            this.browser.ActiveGetterPath = null;
            this.browser.AllColumns.Add(this.treeColumnName);
            this.browser.AllColumns.Add(this.treeColumnSize);
            this.browser.AllColumns.Add(this.treeColumnWarning);
            this.browser.AllColumns.Add(this.treeColumnSync);
            this.browser.AllColumns.Add(this.treeColumnCreate);
            this.browser.CheckBoxes = true;
            this.browser.Columns.AddRange(new System.Windows.Forms.ColumnHeader[] {
            this.treeColumnName,
            this.treeColumnSize,
            this.treeColumnWarning,
            this.treeColumnSync,
            this.treeColumnCreate});
            this.mainTableLayoutPanel.SetColumnSpan(this.browser, 4);
            this.browser.Cursor = System.Windows.Forms.Cursors.Default;
            this.browser.Dock = System.Windows.Forms.DockStyle.Fill;
            this.browser.InactiveForegroudColor = System.Drawing.Color.Gray;
            this.browser.Location = new System.Drawing.Point(13, 42);
            this.browser.Name = "browser";
            this.browser.OwnerDraw = true;
            this.browser.ShowGroups = false;
            this.browser.ShowImagesOnSubItems = true;
            this.browser.Size = new System.Drawing.Size(689, 234);
            this.browser.TabIndex = 2;
            this.browser.UseCompatibleStateImageBehavior = false;
            this.browser.View = System.Windows.Forms.View.Details;
            this.browser.VirtualMode = true;
            this.browser.SelectionChanged += new System.EventHandler(this.browser_SelectionChanged);
            // 
            // treeColumnName
            // 
            this.treeColumnName.AspectName = "Name";
            this.treeColumnName.Text = "Name";
            this.treeColumnName.UseInitialLetterForGroup = true;
            this.treeColumnName.Width = 350;
            // 
            // treeColumnSize
            // 
            this.treeColumnSize.AspectName = "Size";
            this.treeColumnSize.HeaderTextAlign = System.Windows.Forms.HorizontalAlignment.Right;
            this.treeColumnSize.Text = "Size";
            this.treeColumnSize.TextAlign = System.Windows.Forms.HorizontalAlignment.Right;
            this.treeColumnSize.Width = 120;
            // 
            // treeColumnWarning
            // 
            this.treeColumnWarning.AspectName = "";
            this.treeColumnWarning.IsEditable = false;
            this.treeColumnWarning.Text = "";
            this.treeColumnWarning.Width = 20;
            // 
            // treeColumnSync
            // 
            this.treeColumnSync.Width = 20;
            // 
            // treeColumnCreate
            // 
            this.treeColumnCreate.Width = 20;
            // 
            // localFileUrl
            // 
            this.localFileUrl.Anchor = ((System.Windows.Forms.AnchorStyles)(((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Left)
                        | System.Windows.Forms.AnchorStyles.Right)));
            this.localFileUrl.Location = new System.Drawing.Point(91, 51);
            this.localFileUrl.Name = "localFileUrl";
            this.localFileUrl.Size = new System.Drawing.Size(611, 15);
            this.localFileUrl.TabIndex = 9;
            // 
            // localFileModificationDate
            // 
            this.localFileModificationDate.Anchor = ((System.Windows.Forms.AnchorStyles)(((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Left)
                        | System.Windows.Forms.AnchorStyles.Right)));
            this.localFileModificationDate.Location = new System.Drawing.Point(91, 81);
            this.localFileModificationDate.Name = "localFileModificationDate";
            this.localFileModificationDate.Size = new System.Drawing.Size(611, 17);
            this.localFileModificationDate.TabIndex = 8;
            // 
            // localFileSize
            // 
            this.localFileSize.Anchor = ((System.Windows.Forms.AnchorStyles)(((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Left)
                        | System.Windows.Forms.AnchorStyles.Right)));
            this.localFileSize.Location = new System.Drawing.Point(91, 66);
            this.localFileSize.Name = "localFileSize";
            this.localFileSize.Size = new System.Drawing.Size(611, 15);
            this.localFileSize.TabIndex = 7;
            // 
            // remoteFileModificationDate
            // 
            this.remoteFileModificationDate.Anchor = ((System.Windows.Forms.AnchorStyles)(((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Left)
                        | System.Windows.Forms.AnchorStyles.Right)));
            this.remoteFileModificationDate.Location = new System.Drawing.Point(91, 31);
            this.remoteFileModificationDate.Name = "remoteFileModificationDate";
            this.remoteFileModificationDate.Size = new System.Drawing.Size(611, 16);
            this.remoteFileModificationDate.TabIndex = 6;
            // 
            // remoteFileSize
            // 
            this.remoteFileSize.Anchor = ((System.Windows.Forms.AnchorStyles)(((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Left)
                        | System.Windows.Forms.AnchorStyles.Right)));
            this.remoteFileSize.Location = new System.Drawing.Point(91, 16);
            this.remoteFileSize.Name = "remoteFileSize";
            this.remoteFileSize.Size = new System.Drawing.Size(611, 15);
            this.remoteFileSize.TabIndex = 5;
            // 
            // remoteFileUrl
            // 
            this.remoteFileUrl.Anchor = ((System.Windows.Forms.AnchorStyles)(((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Left)
                        | System.Windows.Forms.AnchorStyles.Right)));
            this.remoteFileUrl.Location = new System.Drawing.Point(91, 0);
            this.remoteFileUrl.Name = "remoteFileUrl";
            this.remoteFileUrl.Size = new System.Drawing.Size(611, 16);
            this.remoteFileUrl.TabIndex = 4;
            // 
            // label1
            // 
            this.label1.AutoSize = true;
            this.label1.Location = new System.Drawing.Point(13, 51);
            this.label1.Name = "label1";
            this.label1.Size = new System.Drawing.Size(59, 15);
            this.label1.TabIndex = 3;
            this.label1.Text = "Local File:";
            this.label1.TextAlign = System.Drawing.ContentAlignment.TopRight;
            // 
            // labelRemoteFile
            // 
            this.labelRemoteFile.AutoSize = true;
            this.labelRemoteFile.Location = new System.Drawing.Point(13, 0);
            this.labelRemoteFile.Name = "labelRemoteFile";
            this.labelRemoteFile.Size = new System.Drawing.Size(72, 15);
            this.labelRemoteFile.TabIndex = 2;
            this.labelRemoteFile.Text = "Remote File:";
            this.labelRemoteFile.TextAlign = System.Drawing.ContentAlignment.TopRight;
            // 
            // cancelButton
            // 
            this.cancelButton.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Right)));
            this.cancelButton.AutoSize = true;
            this.cancelButton.DialogResult = System.Windows.Forms.DialogResult.Cancel;
            this.cancelButton.Location = new System.Drawing.Point(617, 282);
            this.cancelButton.Name = "cancelButton";
            this.cancelButton.Size = new System.Drawing.Size(85, 25);
            this.cancelButton.TabIndex = 1;
            this.cancelButton.Text = "Cancel";
            this.cancelButton.UseVisualStyleBackColor = true;
            // 
            // continueButton
            // 
            this.continueButton.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Right)));
            this.continueButton.AutoSize = true;
            this.continueButton.DialogResult = System.Windows.Forms.DialogResult.OK;
            this.continueButton.Location = new System.Drawing.Point(526, 282);
            this.continueButton.Name = "continueButton";
            this.continueButton.Size = new System.Drawing.Size(85, 25);
            this.continueButton.TabIndex = 0;
            this.continueButton.Text = "Continue";
            this.continueButton.UseVisualStyleBackColor = true;
            // 
            // mainTableLayoutPanel
            // 
            this.mainTableLayoutPanel.AutoSize = true;
            this.mainTableLayoutPanel.ColumnCount = 4;
            this.mainTableLayoutPanel.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle(System.Windows.Forms.SizeType.Absolute, 30F));
            this.mainTableLayoutPanel.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle(System.Windows.Forms.SizeType.Percent, 100F));
            this.mainTableLayoutPanel.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle());
            this.mainTableLayoutPanel.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle());
            this.mainTableLayoutPanel.Controls.Add(this.animation, 0, 2);
            this.mainTableLayoutPanel.Controls.Add(this.separatorLine, 0, 4);
            this.mainTableLayoutPanel.Controls.Add(this.statusLabel, 1, 2);
            this.mainTableLayoutPanel.Controls.Add(this.continueButton, 2, 2);
            this.mainTableLayoutPanel.Controls.Add(this.cancelButton, 3, 2);
            this.mainTableLayoutPanel.Controls.Add(this.comboBoxAction, 0, 0);
            this.mainTableLayoutPanel.Controls.Add(this.browser, 0, 1);
            this.mainTableLayoutPanel.Controls.Add(this.toggleDetailsLabel, 0, 3);
            this.mainTableLayoutPanel.Dock = System.Windows.Forms.DockStyle.Fill;
            this.mainTableLayoutPanel.Location = new System.Drawing.Point(0, 0);
            this.mainTableLayoutPanel.Name = "mainTableLayoutPanel";
            this.mainTableLayoutPanel.Padding = new System.Windows.Forms.Padding(10);
            this.mainTableLayoutPanel.RowCount = 5;
            this.mainTableLayoutPanel.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.mainTableLayoutPanel.RowStyles.Add(new System.Windows.Forms.RowStyle(System.Windows.Forms.SizeType.Percent, 100F));
            this.mainTableLayoutPanel.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.mainTableLayoutPanel.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.mainTableLayoutPanel.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.mainTableLayoutPanel.RowStyles.Add(new System.Windows.Forms.RowStyle(System.Windows.Forms.SizeType.Absolute, 20F));
            this.mainTableLayoutPanel.Size = new System.Drawing.Size(715, 347);
            this.mainTableLayoutPanel.TabIndex = 3;
            // 
            // animation
            // 
            this.animation.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Right)));
            this.animation.Image = global::Ch.Cyberduck.ImageHelper.ThrobberSmall;
            this.animation.Location = new System.Drawing.Point(13, 282);
            this.animation.Name = "animation";
            this.animation.Size = new System.Drawing.Size(24, 20);
            this.animation.SizeMode = System.Windows.Forms.PictureBoxSizeMode.CenterImage;
            this.animation.TabIndex = 45;
            this.animation.TabStop = false;
            this.animation.Visible = false;
            // 
            // separatorLine
            // 
            this.separatorLine.Anchor = ((System.Windows.Forms.AnchorStyles)(((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Left)
                        | System.Windows.Forms.AnchorStyles.Right)));
            this.separatorLine.BorderStyle = System.Windows.Forms.BorderStyle.Fixed3D;
            this.mainTableLayoutPanel.SetColumnSpan(this.separatorLine, 4);
            this.separatorLine.Location = new System.Drawing.Point(16, 335);
            this.separatorLine.Margin = new System.Windows.Forms.Padding(6, 0, 3, 0);
            this.separatorLine.Name = "separatorLine";
            this.separatorLine.Size = new System.Drawing.Size(686, 2);
            this.separatorLine.TabIndex = 44;
            // 
            // statusLabel
            // 
            this.statusLabel.Anchor = ((System.Windows.Forms.AnchorStyles)(((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Left)
                        | System.Windows.Forms.AnchorStyles.Right)));
            this.statusLabel.AutoSize = true;
            this.statusLabel.Location = new System.Drawing.Point(43, 279);
            this.statusLabel.Name = "statusLabel";
            this.statusLabel.Padding = new System.Windows.Forms.Padding(0, 4, 0, 0);
            this.statusLabel.Size = new System.Drawing.Size(477, 19);
            this.statusLabel.TabIndex = 3;
            this.statusLabel.Text = "Anzahl Files";
            // 
            // toggleDetailsLabel
            // 
            this.toggleDetailsLabel.Anchor = System.Windows.Forms.AnchorStyles.Left;
            this.toggleDetailsLabel.AutoSize = true;
            this.mainTableLayoutPanel.SetColumnSpan(this.toggleDetailsLabel, 4);
            this.toggleDetailsLabel.ImageAlign = System.Drawing.ContentAlignment.MiddleLeft;
            this.toggleDetailsLabel.ImageIndex = 0;
            this.toggleDetailsLabel.ImageList = this.imageList;
            this.toggleDetailsLabel.Location = new System.Drawing.Point(13, 310);
            this.toggleDetailsLabel.MinimumSize = new System.Drawing.Size(0, 25);
            this.toggleDetailsLabel.Name = "toggleDetailsLabel";
            this.toggleDetailsLabel.Size = new System.Drawing.Size(124, 25);
            this.toggleDetailsLabel.TabIndex = 8;
            this.toggleDetailsLabel.Text = "        Toggle Transcript";
            this.toggleDetailsLabel.TextAlign = System.Drawing.ContentAlignment.MiddleLeft;
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
            // detailsTableLayoutPanel
            // 
            this.detailsTableLayoutPanel.AutoSize = true;
            this.detailsTableLayoutPanel.ColumnCount = 2;
            this.detailsTableLayoutPanel.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle());
            this.detailsTableLayoutPanel.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle(System.Windows.Forms.SizeType.Percent, 100F));
            this.detailsTableLayoutPanel.Controls.Add(this.localFileModificationDate, 1, 6);
            this.detailsTableLayoutPanel.Controls.Add(this.localFileUrl, 1, 4);
            this.detailsTableLayoutPanel.Controls.Add(this.localFileSize, 1, 5);
            this.detailsTableLayoutPanel.Controls.Add(this.labelRemoteFile, 0, 0);
            this.detailsTableLayoutPanel.Controls.Add(this.remoteFileUrl, 1, 0);
            this.detailsTableLayoutPanel.Controls.Add(this.remoteFileSize, 1, 1);
            this.detailsTableLayoutPanel.Controls.Add(this.label1, 0, 4);
            this.detailsTableLayoutPanel.Controls.Add(this.remoteFileModificationDate, 1, 2);
            this.detailsTableLayoutPanel.Dock = System.Windows.Forms.DockStyle.Bottom;
            this.detailsTableLayoutPanel.Location = new System.Drawing.Point(0, 347);
            this.detailsTableLayoutPanel.Name = "detailsTableLayoutPanel";
            this.detailsTableLayoutPanel.Padding = new System.Windows.Forms.Padding(10, 0, 10, 10);
            this.detailsTableLayoutPanel.RowCount = 7;
            this.detailsTableLayoutPanel.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.detailsTableLayoutPanel.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.detailsTableLayoutPanel.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.detailsTableLayoutPanel.RowStyles.Add(new System.Windows.Forms.RowStyle(System.Windows.Forms.SizeType.Absolute, 4F));
            this.detailsTableLayoutPanel.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.detailsTableLayoutPanel.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.detailsTableLayoutPanel.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.detailsTableLayoutPanel.Size = new System.Drawing.Size(715, 108);
            this.detailsTableLayoutPanel.TabIndex = 4;
            // 
            // TransferPromptForm
            // 
            this.AcceptButton = this.continueButton;
            this.AutoScaleDimensions = new System.Drawing.SizeF(7F, 15F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.CancelButton = this.cancelButton;
            this.ClientSize = new System.Drawing.Size(715, 455);
            this.Controls.Add(this.mainTableLayoutPanel);
            this.Controls.Add(this.detailsTableLayoutPanel);
            this.Name = "TransferPromptForm";
            this.StartPosition = System.Windows.Forms.FormStartPosition.CenterParent;
            this.Text = "TransferControl Prompt";
            ((System.ComponentModel.ISupportInitialize)(this.browser)).EndInit();
            this.mainTableLayoutPanel.ResumeLayout(false);
            this.mainTableLayoutPanel.PerformLayout();
            ((System.ComponentModel.ISupportInitialize)(this.animation)).EndInit();
            this.detailsTableLayoutPanel.ResumeLayout(false);
            this.detailsTableLayoutPanel.PerformLayout();
            this.ResumeLayout(false);
            this.PerformLayout();

        }

        #endregion

        private System.Windows.Forms.ComboBox comboBoxAction;
        private System.Windows.Forms.Button cancelButton;
        private System.Windows.Forms.Button continueButton;
        private MulticolorTreeListView browser;
        private BrightIdeasSoftware.OLVColumn treeColumnName;
        private BrightIdeasSoftware.OLVColumn treeColumnSize;
        private System.Windows.Forms.Label label1;
        private System.Windows.Forms.Label labelRemoteFile;
        private EllipsisLabel remoteFileUrl;
        private EllipsisLabel remoteFileModificationDate;
        private EllipsisLabel remoteFileSize;
        private EllipsisLabel localFileUrl;
        private EllipsisLabel localFileModificationDate;
        private EllipsisLabel localFileSize;
        private TableLayoutPanel mainTableLayoutPanel;
        private System.Windows.Forms.Label statusLabel;
        private System.Windows.Forms.Label toggleDetailsLabel;
        private System.Windows.Forms.Label separatorLine;
        private TableLayoutPanel detailsTableLayoutPanel;
        private BrightIdeasSoftware.OLVColumn treeColumnWarning;
        private BrightIdeasSoftware.OLVColumn treeColumnSync;
        private BrightIdeasSoftware.OLVColumn treeColumnCreate;
        private ImageList imageList;
        private PictureBox animation;
    }
}
