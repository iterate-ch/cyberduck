namespace Ch.Cyberduck.Ui.Winforms
{
    partial class ErrorForm
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
            System.ComponentModel.ComponentResourceManager resources = new System.ComponentModel.ComponentResourceManager(typeof(ErrorForm));
            this.imageList = new System.Windows.Forms.ImageList(this.components);
            this.detailPanel = new System.Windows.Forms.Panel();
            this.detailTextBox = new System.Windows.Forms.TextBox();
            this.tableLayoutPanel1 = new System.Windows.Forms.TableLayoutPanel();
            this.separatorLine = new System.Windows.Forms.Label();
            this.toggleTranscriptLabel = new System.Windows.Forms.Label();
            this.introLabel = new System.Windows.Forms.Label();
            this.okButton = new System.Windows.Forms.Button();
            this.errorListView = new BrightIdeasSoftware.ObjectListView();
            this.errorColumn = new BrightIdeasSoftware.OLVColumn();
            this.cancelButton = new System.Windows.Forms.Button();
            this.detailPanel.SuspendLayout();
            this.tableLayoutPanel1.SuspendLayout();
            ((System.ComponentModel.ISupportInitialize)(this.errorListView)).BeginInit();
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
            // detailPanel
            // 
            this.detailPanel.Controls.Add(this.detailTextBox);
            this.detailPanel.Dock = System.Windows.Forms.DockStyle.Bottom;
            this.detailPanel.Location = new System.Drawing.Point(0, 214);
            this.detailPanel.Name = "detailPanel";
            this.detailPanel.Padding = new System.Windows.Forms.Padding(13, 0, 13, 13);
            this.detailPanel.Size = new System.Drawing.Size(570, 148);
            this.detailPanel.TabIndex = 7;
            // 
            // detailTextBox
            // 
            this.detailTextBox.Dock = System.Windows.Forms.DockStyle.Fill;
            this.detailTextBox.Location = new System.Drawing.Point(13, 0);
            this.detailTextBox.Multiline = true;
            this.detailTextBox.Name = "detailTextBox";
            this.detailTextBox.ReadOnly = true;
            this.detailTextBox.ScrollBars = System.Windows.Forms.ScrollBars.Vertical;
            this.detailTextBox.Size = new System.Drawing.Size(544, 135);
            this.detailTextBox.TabIndex = 0;
            // 
            // tableLayoutPanel1
            // 
            this.tableLayoutPanel1.ColumnCount = 3;
            this.tableLayoutPanel1.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle(System.Windows.Forms.SizeType.Percent, 100F));
            this.tableLayoutPanel1.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle());
            this.tableLayoutPanel1.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle());
            this.tableLayoutPanel1.Controls.Add(this.separatorLine, 0, 3);
            this.tableLayoutPanel1.Controls.Add(this.toggleTranscriptLabel, 0, 2);
            this.tableLayoutPanel1.Controls.Add(this.introLabel, 0, 0);
            this.tableLayoutPanel1.Controls.Add(this.okButton, 2, 2);
            this.tableLayoutPanel1.Controls.Add(this.errorListView, 0, 1);
            this.tableLayoutPanel1.Controls.Add(this.cancelButton, 1, 2);
            this.tableLayoutPanel1.Dock = System.Windows.Forms.DockStyle.Fill;
            this.tableLayoutPanel1.Location = new System.Drawing.Point(0, 0);
            this.tableLayoutPanel1.Name = "tableLayoutPanel1";
            this.tableLayoutPanel1.Padding = new System.Windows.Forms.Padding(10, 10, 10, 0);
            this.tableLayoutPanel1.RowCount = 4;
            this.tableLayoutPanel1.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel1.RowStyles.Add(new System.Windows.Forms.RowStyle(System.Windows.Forms.SizeType.Percent, 100F));
            this.tableLayoutPanel1.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel1.RowStyles.Add(new System.Windows.Forms.RowStyle(System.Windows.Forms.SizeType.Absolute, 10F));
            this.tableLayoutPanel1.Size = new System.Drawing.Size(570, 214);
            this.tableLayoutPanel1.TabIndex = 12;
            // 
            // separatorLine
            // 
            this.separatorLine.Anchor = ((System.Windows.Forms.AnchorStyles)(((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Left)
                        | System.Windows.Forms.AnchorStyles.Right)));
            this.separatorLine.BorderStyle = System.Windows.Forms.BorderStyle.Fixed3D;
            this.tableLayoutPanel1.SetColumnSpan(this.separatorLine, 3);
            this.separatorLine.Location = new System.Drawing.Point(16, 204);
            this.separatorLine.Margin = new System.Windows.Forms.Padding(6, 0, 3, 0);
            this.separatorLine.Name = "separatorLine";
            this.separatorLine.Size = new System.Drawing.Size(541, 2);
            this.separatorLine.TabIndex = 43;
            // 
            // toggleTranscriptLabel
            // 
            this.toggleTranscriptLabel.Anchor = System.Windows.Forms.AnchorStyles.Left;
            this.toggleTranscriptLabel.AutoSize = true;
            this.toggleTranscriptLabel.ImageAlign = System.Drawing.ContentAlignment.MiddleLeft;
            this.toggleTranscriptLabel.ImageIndex = 3;
            this.toggleTranscriptLabel.ImageList = this.imageList;
            this.toggleTranscriptLabel.Location = new System.Drawing.Point(13, 176);
            this.toggleTranscriptLabel.MinimumSize = new System.Drawing.Size(0, 25);
            this.toggleTranscriptLabel.Name = "toggleTranscriptLabel";
            this.toggleTranscriptLabel.Size = new System.Drawing.Size(124, 25);
            this.toggleTranscriptLabel.TabIndex = 7;
            this.toggleTranscriptLabel.Text = "        Toggle Transcript";
            this.toggleTranscriptLabel.TextAlign = System.Drawing.ContentAlignment.MiddleLeft;
            // 
            // introLabel
            // 
            this.introLabel.Anchor = ((System.Windows.Forms.AnchorStyles)(((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Left)
                        | System.Windows.Forms.AnchorStyles.Right)));
            this.tableLayoutPanel1.SetColumnSpan(this.introLabel, 3);
            this.introLabel.Location = new System.Drawing.Point(13, 10);
            this.introLabel.Name = "introLabel";
            this.introLabel.Size = new System.Drawing.Size(544, 17);
            this.introLabel.TabIndex = 8;
            this.introLabel.Text = "The last action could not be completed due to the following errors:";
            // 
            // okButton
            // 
            this.okButton.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Bottom | System.Windows.Forms.AnchorStyles.Right)));
            this.okButton.AutoSize = true;
            this.okButton.DialogResult = System.Windows.Forms.DialogResult.OK;
            this.okButton.Location = new System.Drawing.Point(470, 176);
            this.okButton.Name = "okButton";
            this.okButton.Size = new System.Drawing.Size(87, 25);
            this.okButton.TabIndex = 10;
            this.okButton.Text = "Try Again";
            this.okButton.UseVisualStyleBackColor = true;
            // 
            // errorListView
            // 
            this.errorListView.AllColumns.Add(this.errorColumn);
            this.errorListView.Anchor = ((System.Windows.Forms.AnchorStyles)((((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Bottom)
                        | System.Windows.Forms.AnchorStyles.Left)
                        | System.Windows.Forms.AnchorStyles.Right)));
            this.errorListView.Columns.AddRange(new System.Windows.Forms.ColumnHeader[] {
            this.errorColumn});
            this.tableLayoutPanel1.SetColumnSpan(this.errorListView, 3);
            this.errorListView.Location = new System.Drawing.Point(13, 30);
            this.errorListView.Name = "errorListView";
            this.errorListView.Size = new System.Drawing.Size(544, 140);
            this.errorListView.TabIndex = 6;
            this.errorListView.UseCompatibleStateImageBehavior = false;
            this.errorListView.View = System.Windows.Forms.View.Details;
            // 
            // cancelButton
            // 
            this.cancelButton.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Bottom | System.Windows.Forms.AnchorStyles.Right)));
            this.cancelButton.AutoSize = true;
            this.cancelButton.DialogResult = System.Windows.Forms.DialogResult.Cancel;
            this.cancelButton.Location = new System.Drawing.Point(377, 176);
            this.cancelButton.Name = "cancelButton";
            this.cancelButton.Size = new System.Drawing.Size(87, 25);
            this.cancelButton.TabIndex = 9;
            this.cancelButton.Text = "Cancel";
            this.cancelButton.UseVisualStyleBackColor = true;
            // 
            // ErrorForm
            // 
            this.AcceptButton = this.okButton;
            this.AutoScaleDimensions = new System.Drawing.SizeF(7F, 15F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.CancelButton = this.cancelButton;
            this.ClientSize = new System.Drawing.Size(570, 362);
            this.Controls.Add(this.tableLayoutPanel1);
            this.Controls.Add(this.detailPanel);
            this.FormBorderStyle = System.Windows.Forms.FormBorderStyle.SizableToolWindow;
            this.Name = "ErrorForm";
            this.StartPosition = System.Windows.Forms.FormStartPosition.CenterParent;
            this.Text = "Failures";
            this.detailPanel.ResumeLayout(false);
            this.detailPanel.PerformLayout();
            this.tableLayoutPanel1.ResumeLayout(false);
            this.tableLayoutPanel1.PerformLayout();
            ((System.ComponentModel.ISupportInitialize)(this.errorListView)).EndInit();
            this.ResumeLayout(false);

        }

        #endregion

        private System.Windows.Forms.ImageList imageList;
        private System.Windows.Forms.Panel detailPanel;
        private System.Windows.Forms.TextBox detailTextBox;
        private System.Windows.Forms.TableLayoutPanel tableLayoutPanel1;
        private System.Windows.Forms.Label separatorLine;
        private System.Windows.Forms.Label toggleTranscriptLabel;
        private System.Windows.Forms.Label introLabel;
        private System.Windows.Forms.Button okButton;
        private BrightIdeasSoftware.ObjectListView errorListView;
        private BrightIdeasSoftware.OLVColumn errorColumn;
        private System.Windows.Forms.Button cancelButton;
    }
}