using Ch.Cyberduck.Ui.Winforms.Controls;

namespace Ch.Cyberduck.Ui.Winforms
{
    partial class UpdateForm
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
            this.laterButton = new System.Windows.Forms.Button();
            this.tableLayoutPanel = new System.Windows.Forms.TableLayoutPanel();
            this.newVersionAvailableLabel = new System.Windows.Forms.Label();
            this.versionLabel = new System.Windows.Forms.Label();
            this.releaseNotesLabel = new System.Windows.Forms.Label();
            this.installButton = new System.Windows.Forms.Button();
            this.changesTextBox = new Ch.Cyberduck.Ui.Winforms.Controls.ReadOnlyRichTextBox();
            this.updater = new wyDay.Controls.AutomaticUpdater();
            this.statusLabel = new System.Windows.Forms.Label();
            this.progressBar = new System.Windows.Forms.ProgressBar();
            this.pictureBox = new System.Windows.Forms.PictureBox();
            this.tableLayoutPanel.SuspendLayout();
            ((System.ComponentModel.ISupportInitialize)(this.updater)).BeginInit();
            ((System.ComponentModel.ISupportInitialize)(this.pictureBox)).BeginInit();
            this.SuspendLayout();
            // 
            // laterButton
            // 
            this.laterButton.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Bottom | System.Windows.Forms.AnchorStyles.Left)));
            this.laterButton.AutoSize = true;
            this.laterButton.DialogResult = System.Windows.Forms.DialogResult.Cancel;
            this.laterButton.Location = new System.Drawing.Point(271, 312);
            this.laterButton.Name = "laterButton";
            this.laterButton.Size = new System.Drawing.Size(107, 27);
            this.laterButton.TabIndex = 1;
            this.laterButton.Text = "Remind Me Later";
            this.laterButton.UseVisualStyleBackColor = true;
            this.laterButton.Click += new System.EventHandler(this.laterButton_Click);
            // 
            // tableLayoutPanel
            // 
            this.tableLayoutPanel.Anchor = ((System.Windows.Forms.AnchorStyles)((((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Bottom)
                        | System.Windows.Forms.AnchorStyles.Left)
                        | System.Windows.Forms.AnchorStyles.Right)));
            this.tableLayoutPanel.ColumnCount = 3;
            this.tableLayoutPanel.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle(System.Windows.Forms.SizeType.Percent, 100F));
            this.tableLayoutPanel.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle());
            this.tableLayoutPanel.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle());
            this.tableLayoutPanel.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle(System.Windows.Forms.SizeType.Absolute, 20F));
            this.tableLayoutPanel.Controls.Add(this.newVersionAvailableLabel, 0, 2);
            this.tableLayoutPanel.Controls.Add(this.versionLabel, 0, 4);
            this.tableLayoutPanel.Controls.Add(this.releaseNotesLabel, 0, 6);
            this.tableLayoutPanel.Controls.Add(this.laterButton, 1, 10);
            this.tableLayoutPanel.Controls.Add(this.installButton, 2, 10);
            this.tableLayoutPanel.Controls.Add(this.changesTextBox, 0, 7);
            this.tableLayoutPanel.Controls.Add(this.updater, 0, 10);
            this.tableLayoutPanel.Controls.Add(this.statusLabel, 0, 8);
            this.tableLayoutPanel.Controls.Add(this.progressBar, 0, 9);
            this.tableLayoutPanel.Location = new System.Drawing.Point(96, 0);
            this.tableLayoutPanel.Name = "tableLayoutPanel";
            this.tableLayoutPanel.Padding = new System.Windows.Forms.Padding(0, 10, 10, 10);
            this.tableLayoutPanel.RowCount = 11;
            this.tableLayoutPanel.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel.RowStyles.Add(new System.Windows.Forms.RowStyle(System.Windows.Forms.SizeType.Absolute, 6F));
            this.tableLayoutPanel.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel.RowStyles.Add(new System.Windows.Forms.RowStyle(System.Windows.Forms.SizeType.Absolute, 6F));
            this.tableLayoutPanel.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel.Size = new System.Drawing.Size(486, 352);
            this.tableLayoutPanel.TabIndex = 6;
            // 
            // newVersionAvailableLabel
            // 
            this.newVersionAvailableLabel.AutoSize = true;
            this.tableLayoutPanel.SetColumnSpan(this.newVersionAvailableLabel, 3);
            this.newVersionAvailableLabel.Font = new System.Drawing.Font("Segoe UI", 10F, System.Drawing.FontStyle.Bold);
            this.newVersionAvailableLabel.Location = new System.Drawing.Point(3, 10);
            this.newVersionAvailableLabel.Name = "newVersionAvailableLabel";
            this.newVersionAvailableLabel.Size = new System.Drawing.Size(281, 19);
            this.newVersionAvailableLabel.TabIndex = 1;
            this.newVersionAvailableLabel.Text = "A new version of Cyberduck is available!";
            // 
            // versionLabel
            // 
            this.versionLabel.AutoSize = true;
            this.tableLayoutPanel.SetColumnSpan(this.versionLabel, 3);
            this.versionLabel.Location = new System.Drawing.Point(3, 35);
            this.versionLabel.Name = "versionLabel";
            this.versionLabel.Size = new System.Drawing.Size(452, 30);
            this.versionLabel.TabIndex = 2;
            this.versionLabel.Text = "Cyberduck /newversion/ is now available (you have /oldversion/). Would you like t" +
                "o download it now?";
            // 
            // releaseNotesLabel
            // 
            this.releaseNotesLabel.AutoSize = true;
            this.tableLayoutPanel.SetColumnSpan(this.releaseNotesLabel, 3);
            this.releaseNotesLabel.Font = new System.Drawing.Font("Segoe UI", 8F);
            this.releaseNotesLabel.Location = new System.Drawing.Point(3, 71);
            this.releaseNotesLabel.Name = "releaseNotesLabel";
            this.releaseNotesLabel.Size = new System.Drawing.Size(82, 13);
            this.releaseNotesLabel.TabIndex = 3;
            this.releaseNotesLabel.Text = "Release Notes:";
            // 
            // installButton
            // 
            this.installButton.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Bottom | System.Windows.Forms.AnchorStyles.Left)));
            this.installButton.AutoSize = true;
            this.installButton.Location = new System.Drawing.Point(384, 312);
            this.installButton.Name = "installButton";
            this.installButton.Size = new System.Drawing.Size(89, 27);
            this.installButton.TabIndex = 0;
            this.installButton.Text = "Install Update";
            this.installButton.UseVisualStyleBackColor = true;
            this.installButton.Click += new System.EventHandler(this.donateButton_Click);
            // 
            // changesTextBox
            // 
            this.changesTextBox.BackColor = System.Drawing.Color.White;
            this.changesTextBox.BorderStyle = System.Windows.Forms.BorderStyle.FixedSingle;
            this.tableLayoutPanel.SetColumnSpan(this.changesTextBox, 3);
            this.changesTextBox.Dock = System.Windows.Forms.DockStyle.Fill;
            this.changesTextBox.Location = new System.Drawing.Point(3, 87);
            this.changesTextBox.Name = "changesTextBox";
            this.changesTextBox.ReadOnly = true;
            this.changesTextBox.Size = new System.Drawing.Size(470, 178);
            this.changesTextBox.TabIndex = 4;
            this.changesTextBox.Text = "";
            // 
            // updater
            // 
            this.updater.ContainerForm = this;
            this.updater.Location = new System.Drawing.Point(3, 307);
            this.updater.Name = "updater";
            this.updater.Size = new System.Drawing.Size(16, 16);
            this.updater.TabIndex = 9;
            this.updater.wyUpdateCommandline = null;
            // 
            // statusLabel
            // 
            this.statusLabel.AutoSize = true;
            this.tableLayoutPanel.SetColumnSpan(this.statusLabel, 3);
            this.statusLabel.Location = new System.Drawing.Point(3, 268);
            this.statusLabel.Name = "statusLabel";
            this.statusLabel.Size = new System.Drawing.Size(38, 15);
            this.statusLabel.TabIndex = 7;
            this.statusLabel.Text = "status";
            // 
            // progressBar
            // 
            this.tableLayoutPanel.SetColumnSpan(this.progressBar, 3);
            this.progressBar.Dock = System.Windows.Forms.DockStyle.Fill;
            this.progressBar.Location = new System.Drawing.Point(3, 286);
            this.progressBar.MaximumSize = new System.Drawing.Size(0, 15);
            this.progressBar.MinimumSize = new System.Drawing.Size(0, 15);
            this.progressBar.Name = "progressBar";
            this.progressBar.Size = new System.Drawing.Size(470, 15);
            this.progressBar.TabIndex = 8;
            // 
            // pictureBox
            // 
            this.pictureBox.Location = new System.Drawing.Point(12, 15);
            this.pictureBox.Name = "pictureBox";
            this.pictureBox.Size = new System.Drawing.Size(74, 74);
            this.pictureBox.TabIndex = 0;
            this.pictureBox.TabStop = false;
            // 
            // UpdateForm
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(7F, 15F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.CancelButton = this.laterButton;
            this.ClientSize = new System.Drawing.Size(582, 352);
            this.Controls.Add(this.tableLayoutPanel);
            this.Controls.Add(this.pictureBox);
            this.MinimumSize = new System.Drawing.Size(350, 350);
            this.Name = "UpdateForm";
            this.StartPosition = System.Windows.Forms.FormStartPosition.CenterScreen;
            this.Text = "Cyberduck Update";
            this.tableLayoutPanel.ResumeLayout(false);
            this.tableLayoutPanel.PerformLayout();
            ((System.ComponentModel.ISupportInitialize)(this.updater)).EndInit();
            ((System.ComponentModel.ISupportInitialize)(this.pictureBox)).EndInit();
            this.ResumeLayout(false);

        }

        #endregion

        protected System.Windows.Forms.TableLayoutPanel tableLayoutPanel;
        protected System.Windows.Forms.PictureBox pictureBox;
        protected System.Windows.Forms.Label newVersionAvailableLabel;
        private System.Windows.Forms.Label versionLabel;
        private System.Windows.Forms.Label releaseNotesLabel;
        protected System.Windows.Forms.Button laterButton;
        protected System.Windows.Forms.Button installButton;
        private ReadOnlyRichTextBox changesTextBox;
        private System.Windows.Forms.Label statusLabel;
        private System.Windows.Forms.ProgressBar progressBar;
        private wyDay.Controls.AutomaticUpdater updater;
    }
}