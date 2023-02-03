using System;

namespace Ch.Cyberduck.Ui.Winforms.Controls
{
    partial class TransferControl
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

        #region Component Designer generated code

        /// <summary> 
        /// Required method for Designer support - do not modify 
        /// the contents of this method with the code editor.
        /// </summary>
        private void InitializeComponent()
        {
            this.progressLabel = new System.Windows.Forms.Label();
            this.statusLabel = new System.Windows.Forms.Label();
            this.filesComboBox = new System.Windows.Forms.ComboBox();
            this.messageLabel = new System.Windows.Forms.Label();
            this.progressBar = new System.Windows.Forms.ProgressBar();
            this.statusPictureBox = new System.Windows.Forms.PictureBox();
            this.directionPictureBox = new System.Windows.Forms.PictureBox();
            ((System.ComponentModel.ISupportInitialize)(this.statusPictureBox)).BeginInit();
            ((System.ComponentModel.ISupportInitialize)(this.directionPictureBox)).BeginInit();
            this.SuspendLayout();
            // 
            // progressLabel
            // 
            this.progressLabel.AutoSize = true;
            this.progressLabel.Location = new System.Drawing.Point(77, 53);
            this.progressLabel.Name = "progressLabel";
            this.progressLabel.Size = new System.Drawing.Size(109, 15);
            this.progressLabel.TabIndex = 1;
            this.progressLabel.Text = "56.1 KB of 56.1 KB";
            // 
            // statusLabel
            // 
            this.statusLabel.AutoSize = true;
            this.statusLabel.Location = new System.Drawing.Point(51, 33);
            this.statusLabel.Name = "statusLabel";
            this.statusLabel.Size = new System.Drawing.Size(117, 15);
            this.statusLabel.TabIndex = 2;
            this.statusLabel.Text = "Download complete";
            // 
            // filesComboBox
            // 
            this.filesComboBox.Anchor = ((System.Windows.Forms.AnchorStyles)(((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Left)
                        | System.Windows.Forms.AnchorStyles.Right)));
            this.filesComboBox.DropDownStyle = System.Windows.Forms.ComboBoxStyle.DropDownList;
            this.filesComboBox.FormattingEnabled = true;
            this.filesComboBox.Location = new System.Drawing.Point(54, 6);
            this.filesComboBox.Name = "filesComboBox";
            this.filesComboBox.Size = new System.Drawing.Size(402, 23);
            this.filesComboBox.TabIndex = 3;
            // 
            // messageLabel
            // 
            this.messageLabel.AutoSize = true;
            this.messageLabel.Location = new System.Drawing.Point(77, 71);
            this.messageLabel.Name = "messageLabel";
            this.messageLabel.Size = new System.Drawing.Size(223, 15);
            this.messageLabel.TabIndex = 5;
            this.messageLabel.Text = "19. Februar 2010 13:21:45 GMT +01:00";
            // 
            // progressBar
            // 
            this.progressBar.Anchor = ((System.Windows.Forms.AnchorStyles)(((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Left)
                        | System.Windows.Forms.AnchorStyles.Right)));
            this.progressBar.Location = new System.Drawing.Point(54, 32);
            this.progressBar.Name = "progressBar";
            this.progressBar.Size = new System.Drawing.Size(402, 15);
            this.progressBar.TabIndex = 6;
            // 
            // statusPictureBox
            // 
            this.statusPictureBox.Image = global::Ch.Cyberduck.ImageHelper.StatusGreen;
            this.statusPictureBox.Location = new System.Drawing.Point(51, 50);
            this.statusPictureBox.Name = "statusPictureBox";
            this.statusPictureBox.Size = new System.Drawing.Size(20, 20);
            this.statusPictureBox.SizeMode = System.Windows.Forms.PictureBoxSizeMode.StretchImage;
            this.statusPictureBox.TabIndex = 4;
            this.statusPictureBox.TabStop = false;
            // 
            // directionPictureBox
            // 
            this.directionPictureBox.BackColor = System.Drawing.SystemColors.Control;
            this.directionPictureBox.Image = global::Ch.Cyberduck.ImageHelper.TransferDownload;
            this.directionPictureBox.Location = new System.Drawing.Point(3, 3);
            this.directionPictureBox.Name = "directionPictureBox";
            this.directionPictureBox.Size = new System.Drawing.Size(32, 32);
            this.directionPictureBox.SizeMode = System.Windows.Forms.PictureBoxSizeMode.AutoSize;
            this.directionPictureBox.TabIndex = 0;
            this.directionPictureBox.TabStop = false;
            // 
            // TransferControl
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(7F, 15F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.Controls.Add(this.messageLabel);
            this.Controls.Add(this.statusPictureBox);
            this.Controls.Add(this.filesComboBox);
            this.Controls.Add(this.statusLabel);
            this.Controls.Add(this.progressLabel);
            this.Controls.Add(this.directionPictureBox);
            this.Controls.Add(this.progressBar);
            this.Name = "TransferControl";
            this.Size = new System.Drawing.Size(464, 90);
            ((System.ComponentModel.ISupportInitialize)(this.statusPictureBox)).EndInit();
            ((System.ComponentModel.ISupportInitialize)(this.directionPictureBox)).EndInit();
            this.ResumeLayout(false);
            this.PerformLayout();

        }

        #endregion

        private System.Windows.Forms.PictureBox directionPictureBox;
        private System.Windows.Forms.Label progressLabel;
        private System.Windows.Forms.Label statusLabel;
        private System.Windows.Forms.ComboBox filesComboBox;
        private System.Windows.Forms.PictureBox statusPictureBox;
        private System.Windows.Forms.Label messageLabel;
        private System.Windows.Forms.ProgressBar progressBar;

        public void ValidateCommands()
        {
            throw new NotImplementedException();
        }
    }
}
