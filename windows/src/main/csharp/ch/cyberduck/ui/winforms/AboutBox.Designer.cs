using Ch.Cyberduck.Ui.Winforms.Controls;

namespace Ch.Cyberduck.Ui.Winforms
{
    partial class AboutBox
    {
        /// <summary>
        /// Required designer variable.
        /// </summary>
        private System.ComponentModel.IContainer components = null;

        /// <summary>
        /// Clean up any resources being used.
        /// </summary>
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
            this.tableLayoutPanel = new System.Windows.Forms.TableLayoutPanel();
            this.logoPictureBox = new System.Windows.Forms.PictureBox();
            this.labelProductName = new System.Windows.Forms.Label();
            this.labelVersion = new System.Windows.Forms.Label();
            this.labelCopyright = new System.Windows.Forms.Label();
            this.creditsRichTextBox = new Ch.Cyberduck.Ui.Winforms.Controls.ReadOnlyRichTextBox();
            this.tableLayoutPanel1 = new System.Windows.Forms.TableLayoutPanel();
            this.ackButton = new System.Windows.Forms.Button();
            this.labelRegistered = new System.Windows.Forms.Label();
            this.tableLayoutPanel2 = new System.Windows.Forms.TableLayoutPanel();
            this.updateButton = new System.Windows.Forms.Button();
            this.tableLayoutPanel2 = new System.Windows.Forms.TableLayoutPanel();
            this.updateButton = new System.Windows.Forms.Button();
            this.tableLayoutPanel.SuspendLayout();
            ((System.ComponentModel.ISupportInitialize)(this.logoPictureBox)).BeginInit();
            this.tableLayoutPanel1.SuspendLayout();
            this.tableLayoutPanel2.SuspendLayout();
            this.SuspendLayout();
            //
            // tableLayoutPanel
            //
            this.tableLayoutPanel.ColumnCount = 1;
            this.tableLayoutPanel.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle(System.Windows.Forms.SizeType.Percent, 100F));
            this.tableLayoutPanel.Controls.Add(this.logoPictureBox, 0, 0);
            this.tableLayoutPanel.Controls.Add(this.labelProductName, 0, 1);
            this.tableLayoutPanel.Controls.Add(this.labelVersion, 0, 2);
            this.tableLayoutPanel.Controls.Add(this.labelCopyright, 0, 6);
            this.tableLayoutPanel.Controls.Add(this.creditsRichTextBox, 0, 5);
            this.tableLayoutPanel.Controls.Add(this.tableLayoutPanel1, 0, 8);
            this.tableLayoutPanel.Controls.Add(this.labelRegistered, 0, 3);
            this.tableLayoutPanel.Controls.Add(this.tableLayoutPanel2, 0, 4);
            this.tableLayoutPanel.Dock = System.Windows.Forms.DockStyle.Fill;
            this.tableLayoutPanel.Location = new System.Drawing.Point(0, 0);
            this.tableLayoutPanel.Name = "tableLayoutPanel";
            this.tableLayoutPanel.RowCount = 9;
            this.tableLayoutPanel.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel.RowStyles.Add(new System.Windows.Forms.RowStyle(System.Windows.Forms.SizeType.Percent, 100F));
            this.tableLayoutPanel.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel.Size = new System.Drawing.Size(348, 444);
            this.tableLayoutPanel.TabIndex = 0;
            //
            // logoPictureBox
            //
            this.logoPictureBox.Dock = System.Windows.Forms.DockStyle.Fill;
            this.logoPictureBox.Location = new System.Drawing.Point(3, 3);
            this.logoPictureBox.Name = "logoPictureBox";
            this.logoPictureBox.Size = new System.Drawing.Size(342, 160);
            this.logoPictureBox.SizeMode = System.Windows.Forms.PictureBoxSizeMode.CenterImage;
            this.logoPictureBox.TabIndex = 12;
            this.logoPictureBox.TabStop = false;
            //
            // labelProductName
            //
            this.labelProductName.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Left | System.Windows.Forms.AnchorStyles.Right)));
            this.labelProductName.AutoSize = true;
            this.labelProductName.Location = new System.Drawing.Point(7, 166);
            this.labelProductName.Margin = new System.Windows.Forms.Padding(7, 0, 3, 0);
            this.labelProductName.Name = "labelProductName";
            this.labelProductName.Size = new System.Drawing.Size(338, 15);
            this.labelProductName.TabIndex = 19;
            this.labelProductName.Text = "Product Name";
            this.labelProductName.TextAlign = System.Drawing.ContentAlignment.MiddleCenter;
            //
            // labelVersion
            //
            this.labelVersion.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Left | System.Windows.Forms.AnchorStyles.Right)));
            this.labelVersion.AutoSize = true;
            this.labelVersion.Location = new System.Drawing.Point(7, 184);
            this.labelVersion.Margin = new System.Windows.Forms.Padding(7, 3, 3, 10);
            this.labelVersion.MaximumSize = new System.Drawing.Size(0, 20);
            this.labelVersion.Name = "labelVersion";
            this.labelVersion.Size = new System.Drawing.Size(338, 15);
            this.labelVersion.TabIndex = 0;
            this.labelVersion.Text = "Version";
            this.labelVersion.TextAlign = System.Drawing.ContentAlignment.MiddleCenter;
            //
            // labelCopyright
            //
            this.labelCopyright.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Left | System.Windows.Forms.AnchorStyles.Right)));
            this.labelCopyright.AutoSize = true;
            this.labelCopyright.Location = new System.Drawing.Point(7, 382);
            this.labelCopyright.Margin = new System.Windows.Forms.Padding(7, 0, 3, 0);
            this.labelCopyright.MaximumSize = new System.Drawing.Size(0, 20);
            this.labelCopyright.Name = "labelCopyright";
            this.labelCopyright.Padding = new System.Windows.Forms.Padding(0, 2, 0, 0);
            this.labelCopyright.Size = new System.Drawing.Size(338, 17);
            this.labelCopyright.TabIndex = 21;
            this.labelCopyright.Text = "Copyright";
            this.labelCopyright.TextAlign = System.Drawing.ContentAlignment.MiddleCenter;
            //
            // creditsRichTextBox
            //
            this.creditsRichTextBox.BackColor = System.Drawing.Color.White;
            this.creditsRichTextBox.BorderStyle = System.Windows.Forms.BorderStyle.None;
            this.creditsRichTextBox.Dock = System.Windows.Forms.DockStyle.Fill;
            this.creditsRichTextBox.Location = new System.Drawing.Point(3, 227);
            this.creditsRichTextBox.Name = "creditsRichTextBox";
            this.creditsRichTextBox.ReadOnly = true;
            this.creditsRichTextBox.Size = new System.Drawing.Size(342, 152);
            this.creditsRichTextBox.TabIndex = 22;
            this.creditsRichTextBox.Text = "";
            //
            // tableLayoutPanel1
            //
            this.tableLayoutPanel1.AutoSize = true;
            this.tableLayoutPanel1.ColumnCount = 3;
            this.tableLayoutPanel1.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle(System.Windows.Forms.SizeType.Percent, 50F));
            this.tableLayoutPanel1.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle());
            this.tableLayoutPanel1.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle(System.Windows.Forms.SizeType.Percent, 50F));
            this.tableLayoutPanel1.Controls.Add(this.ackButton, 1, 0);
            this.tableLayoutPanel1.Dock = System.Windows.Forms.DockStyle.Fill;
            this.tableLayoutPanel1.Location = new System.Drawing.Point(3, 409);
            this.tableLayoutPanel1.Margin = new System.Windows.Forms.Padding(3, 10, 3, 10);
            this.tableLayoutPanel1.Name = "tableLayoutPanel1";
            this.tableLayoutPanel1.RowCount = 1;
            this.tableLayoutPanel1.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel1.Size = new System.Drawing.Size(342, 25);
            this.tableLayoutPanel1.TabIndex = 24;
            //
            // ackButton
            //
            this.ackButton.AutoSize = true;
            this.ackButton.Location = new System.Drawing.Point(110, 0);
            this.ackButton.Margin = new System.Windows.Forms.Padding(0);
            this.ackButton.Name = "ackButton";
            this.ackButton.Size = new System.Drawing.Size(122, 25);
            this.ackButton.TabIndex = 23;
            this.ackButton.Text = "Acknowledgments";
            this.ackButton.UseVisualStyleBackColor = true;
            //
            // labelRegistered
            //
            this.labelRegistered.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Left | System.Windows.Forms.AnchorStyles.Right)));
            this.labelRegistered.AutoSize = true;
            this.labelRegistered.Location = new System.Drawing.Point(3, 209);
            this.labelRegistered.Name = "labelRegistered";
            this.labelRegistered.Size = new System.Drawing.Size(342, 15);
            this.labelRegistered.TabIndex = 25;
            this.labelRegistered.Text = "Registered to";
            this.labelRegistered.TextAlign = System.Drawing.ContentAlignment.MiddleCenter;
            //
            // tableLayoutPanel2
            //
            this.tableLayoutPanel2.AutoSize = true;
            this.tableLayoutPanel2.ColumnCount = 3;
            this.tableLayoutPanel2.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle(System.Windows.Forms.SizeType.Percent, 50F));
            this.tableLayoutPanel2.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle());
            this.tableLayoutPanel2.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle(System.Windows.Forms.SizeType.Percent, 50F));
            this.tableLayoutPanel2.Controls.Add(this.updateButton, 1, 0);
            this.tableLayoutPanel2.Dock = System.Windows.Forms.DockStyle.Fill;
            this.tableLayoutPanel2.Location = new System.Drawing.Point(3, 227);
            this.tableLayoutPanel2.Name = "tableLayoutPanel2";
            this.tableLayoutPanel2.RowCount = 1;
            this.tableLayoutPanel2.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel2.Size = new System.Drawing.Size(359, 25);
            this.tableLayoutPanel2.TabIndex = 26;
            //
            // updateButton
            //
            this.updateButton.AutoSize = true;
            this.updateButton.Location = new System.Drawing.Point(120, 0);
            this.updateButton.Margin = new System.Windows.Forms.Padding(0);
            this.updateButton.Name = "updateButton";
            this.updateButton.Size = new System.Drawing.Size(118, 25);
            this.updateButton.TabIndex = 0;
            this.updateButton.Text = "Check for Update…";
            this.updateButton.UseVisualStyleBackColor = true;
            //
            // AboutBox
            //
            this.AutoScaleDimensions = new System.Drawing.SizeF(7F, 15F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.ClientSize = new System.Drawing.Size(348, 444);
            this.Controls.Add(this.tableLayoutPanel);
            this.FormBorderStyle = System.Windows.Forms.FormBorderStyle.FixedDialog;
            this.MaximizeBox = false;
            this.MinimizeBox = false;
            this.Name = "AboutBox";
            this.ShowIcon = false;
            this.ShowInTaskbar = false;
            this.StartPosition = System.Windows.Forms.FormStartPosition.CenterParent;
            this.Text = "About";
            this.tableLayoutPanel.ResumeLayout(false);
            this.tableLayoutPanel.PerformLayout();
            ((System.ComponentModel.ISupportInitialize)(this.logoPictureBox)).EndInit();
            this.tableLayoutPanel1.ResumeLayout(false);
            this.tableLayoutPanel1.PerformLayout();
            this.tableLayoutPanel2.ResumeLayout(false);
            this.tableLayoutPanel2.PerformLayout();
            this.ResumeLayout(false);

        }

        #endregion

        private System.Windows.Forms.TableLayoutPanel tableLayoutPanel;
        private System.Windows.Forms.PictureBox logoPictureBox;
        private System.Windows.Forms.Label labelProductName;
        private System.Windows.Forms.Label labelVersion;
        private System.Windows.Forms.Label labelCopyright;
        private ReadOnlyRichTextBox creditsRichTextBox;
        private System.Windows.Forms.Button ackButton;
        private System.Windows.Forms.TableLayoutPanel tableLayoutPanel1;
        private System.Windows.Forms.Label labelRegistered;
        private System.Windows.Forms.TableLayoutPanel tableLayoutPanel2;
        private System.Windows.Forms.Button updateButton;
    }
}
