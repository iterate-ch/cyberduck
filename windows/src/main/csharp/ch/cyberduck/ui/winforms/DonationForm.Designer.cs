namespace Ch.Cyberduck.Ui.Winforms
{
    partial class DonationForm
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
            this.tableLayoutPanel = new System.Windows.Forms.TableLayoutPanel();
            this.pictureBox = new System.Windows.Forms.PictureBox();
            this.label = new System.Windows.Forms.Label();
            this.label1 = new System.Windows.Forms.Label();
            this.label2 = new System.Windows.Forms.Label();
            this.label3 = new System.Windows.Forms.Label();
            this.label4 = new System.Windows.Forms.Label();
            this.laterButton = new System.Windows.Forms.Button();
            this.donateButton = new System.Windows.Forms.Button();
            this.neverShowDonationCheckBox = new System.Windows.Forms.CheckBox();
            this.tableLayoutPanel.SuspendLayout();
            ((System.ComponentModel.ISupportInitialize)(this.pictureBox)).BeginInit();
            this.SuspendLayout();
            // 
            // tableLayoutPanel
            // 
            this.tableLayoutPanel.ColumnCount = 4;
            this.tableLayoutPanel.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle());
            this.tableLayoutPanel.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle(System.Windows.Forms.SizeType.Percent, 100F));
            this.tableLayoutPanel.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle());
            this.tableLayoutPanel.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle());
            this.tableLayoutPanel.Controls.Add(this.pictureBox, 0, 0);
            this.tableLayoutPanel.Controls.Add(this.label, 1, 0);
            this.tableLayoutPanel.Controls.Add(this.label1, 1, 1);
            this.tableLayoutPanel.Controls.Add(this.label2, 1, 2);
            this.tableLayoutPanel.Controls.Add(this.label3, 1, 3);
            this.tableLayoutPanel.Controls.Add(this.label4, 1, 4);
            this.tableLayoutPanel.Controls.Add(this.laterButton, 2, 6);
            this.tableLayoutPanel.Controls.Add(this.donateButton, 3, 6);
            this.tableLayoutPanel.Controls.Add(this.neverShowDonationCheckBox, 1, 5);
            this.tableLayoutPanel.Location = new System.Drawing.Point(12, 12);
            this.tableLayoutPanel.Name = "tableLayoutPanel";
            this.tableLayoutPanel.RowCount = 7;
            this.tableLayoutPanel.RowStyles.Add(new System.Windows.Forms.RowStyle(System.Windows.Forms.SizeType.Absolute, 35F));
            this.tableLayoutPanel.RowStyles.Add(new System.Windows.Forms.RowStyle(System.Windows.Forms.SizeType.Absolute, 60F));
            this.tableLayoutPanel.RowStyles.Add(new System.Windows.Forms.RowStyle(System.Windows.Forms.SizeType.Absolute, 45F));
            this.tableLayoutPanel.RowStyles.Add(new System.Windows.Forms.RowStyle(System.Windows.Forms.SizeType.Absolute, 35F));
            this.tableLayoutPanel.RowStyles.Add(new System.Windows.Forms.RowStyle(System.Windows.Forms.SizeType.Absolute, 45F));
            this.tableLayoutPanel.RowStyles.Add(new System.Windows.Forms.RowStyle(System.Windows.Forms.SizeType.Absolute, 45F));
            this.tableLayoutPanel.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel.RowStyles.Add(new System.Windows.Forms.RowStyle(System.Windows.Forms.SizeType.Absolute, 20F));
            this.tableLayoutPanel.RowStyles.Add(new System.Windows.Forms.RowStyle(System.Windows.Forms.SizeType.Absolute, 20F));
            this.tableLayoutPanel.Size = new System.Drawing.Size(487, 305);
            this.tableLayoutPanel.TabIndex = 5;
            // 
            // pictureBox
            // 
            this.pictureBox.Location = new System.Drawing.Point(3, 3);
            this.pictureBox.Name = "pictureBox";
            this.tableLayoutPanel.SetRowSpan(this.pictureBox, 3);
            this.pictureBox.Size = new System.Drawing.Size(75, 74);
            this.pictureBox.TabIndex = 0;
            this.pictureBox.TabStop = false;
            // 
            // label
            // 
            this.label.AutoSize = true;
            this.tableLayoutPanel.SetColumnSpan(this.label, 3);
            this.label.Font = new System.Drawing.Font("Segoe UI", 10F, System.Drawing.FontStyle.Bold);
            this.label.Location = new System.Drawing.Point(84, 0);
            this.label.Name = "label";
            this.label.Size = new System.Drawing.Size(222, 19);
            this.label.TabIndex = 1;
            this.label.Text = "Thank you for using Cyberduck!";
            // 
            // label1
            // 
            this.label1.AutoSize = true;
            this.tableLayoutPanel.SetColumnSpan(this.label1, 3);
            this.label1.Location = new System.Drawing.Point(84, 35);
            this.label1.Name = "label1";
            this.label1.Size = new System.Drawing.Size(395, 45);
            this.label1.TabIndex = 2;
            this.label1.Text = "It has taken many nights to develop this application. If you enjoy using it, plea" +
                "se consider a donation to the author of this software. It will help to make Cybe" +
                "rduck even better!";
            // 
            // label2
            // 
            this.label2.AutoSize = true;
            this.tableLayoutPanel.SetColumnSpan(this.label2, 3);
            this.label2.Font = new System.Drawing.Font("Segoe UI", 8F);
            this.label2.Location = new System.Drawing.Point(84, 95);
            this.label2.Name = "label2";
            this.label2.Size = new System.Drawing.Size(400, 26);
            this.label2.TabIndex = 3;
            this.label2.Text = "The payment can be made simply and safely using Paypal. You don\'t need to open an" +
                " account.";
            // 
            // label3
            // 
            this.label3.AutoSize = true;
            this.label3.Font = new System.Drawing.Font("Segoe UI", 10F, System.Drawing.FontStyle.Bold);
            this.label3.Location = new System.Drawing.Point(84, 140);
            this.label3.Name = "label3";
            this.label3.Size = new System.Drawing.Size(99, 19);
            this.label3.TabIndex = 4;
            this.label3.Text = "Donation Key";
            // 
            // label4
            // 
            this.label4.AutoSize = true;
            this.tableLayoutPanel.SetColumnSpan(this.label4, 3);
            this.label4.Location = new System.Drawing.Point(84, 175);
            this.label4.Name = "label4";
            this.label4.Size = new System.Drawing.Size(385, 30);
            this.label4.TabIndex = 5;
            this.label4.Text = "As a contributor to Cyberduck, you receive a donation key that disables this prom" +
                "pt.";
            // 
            // laterButton
            // 
            this.laterButton.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Bottom | System.Windows.Forms.AnchorStyles.Left)));
            this.laterButton.DialogResult = System.Windows.Forms.DialogResult.Cancel;
            this.laterButton.Location = new System.Drawing.Point(304, 275);
            this.laterButton.Name = "laterButton";
            this.laterButton.Size = new System.Drawing.Size(87, 27);
            this.laterButton.TabIndex = 1;
            this.laterButton.Text = "Later";
            this.laterButton.UseVisualStyleBackColor = true;
            // 
            // donateButton
            // 
            this.donateButton.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Bottom | System.Windows.Forms.AnchorStyles.Left)));
            this.donateButton.DialogResult = System.Windows.Forms.DialogResult.OK;
            this.donateButton.Location = new System.Drawing.Point(397, 275);
            this.donateButton.Name = "donateButton";
            this.donateButton.Size = new System.Drawing.Size(87, 27);
            this.donateButton.TabIndex = 0;
            this.donateButton.Text = "Donate!";
            this.donateButton.UseVisualStyleBackColor = true;
            // 
            // neverShowDonationCheckBox
            // 
            this.neverShowDonationCheckBox.AutoSize = true;
            this.tableLayoutPanel.SetColumnSpan(this.neverShowDonationCheckBox, 3);
            this.neverShowDonationCheckBox.Location = new System.Drawing.Point(84, 223);
            this.neverShowDonationCheckBox.Name = "neverShowDonationCheckBox";
            this.neverShowDonationCheckBox.Padding = new System.Windows.Forms.Padding(2, 0, 0, 0);
            this.neverShowDonationCheckBox.Size = new System.Drawing.Size(204, 19);
            this.neverShowDonationCheckBox.TabIndex = 6;
            this.neverShowDonationCheckBox.Text = "Don\'t show again for this version.";
            this.neverShowDonationCheckBox.UseVisualStyleBackColor = true;
            // 
            // DonationForm
            // 
            this.AcceptButton = this.donateButton;
            this.AutoScaleDimensions = new System.Drawing.SizeF(7F, 15F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.ClientSize = new System.Drawing.Size(505, 324);
            this.Controls.Add(this.tableLayoutPanel);
            this.FormBorderStyle = System.Windows.Forms.FormBorderStyle.FixedDialog;
            this.Name = "DonationForm";
            this.StartPosition = System.Windows.Forms.FormStartPosition.CenterScreen;
            this.Text = "Please Donate";
            this.tableLayoutPanel.ResumeLayout(false);
            this.tableLayoutPanel.PerformLayout();
            ((System.ComponentModel.ISupportInitialize)(this.pictureBox)).EndInit();
            this.ResumeLayout(false);

        }

        #endregion

        protected System.Windows.Forms.TableLayoutPanel tableLayoutPanel;
        protected System.Windows.Forms.PictureBox pictureBox;
        protected System.Windows.Forms.Label label;
        protected System.Windows.Forms.Button donateButton;
        protected System.Windows.Forms.Button laterButton;
        private System.Windows.Forms.Label label1;
        private System.Windows.Forms.Label label2;
        private System.Windows.Forms.Label label3;
        private System.Windows.Forms.Label label4;
        private System.Windows.Forms.CheckBox neverShowDonationCheckBox;
    }
}