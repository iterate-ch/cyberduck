using System;
using System.Drawing;
using Ch.Cyberduck.Ui.Winforms.Controls;

namespace Ch.Cyberduck.Ui.Winforms
{
    partial class LoginForm
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
            this.openFileDialog = new System.Windows.Forms.OpenFileDialog();
            this.loginButton = new System.Windows.Forms.Button();
            this.cancelButton = new System.Windows.Forms.Button();
            this.tableLayoutPanel1 = new System.Windows.Forms.TableLayoutPanel();
            this.labelUsername = new System.Windows.Forms.Label();
            this.textBoxUsername = new System.Windows.Forms.TextBox();
            this.checkBoxPkAuthentication = new System.Windows.Forms.CheckBox();
            this.labelPassword = new System.Windows.Forms.Label();
            this.checkBoxAnonymous = new System.Windows.Forms.CheckBox();
            this.textBoxPassword = new System.Windows.Forms.TextBox();
            this.pkLabel = new Ch.Cyberduck.Ui.Winforms.Controls.EllipsisLabel();
            this.diskPictureBox = new System.Windows.Forms.PictureBox();
            this.checkBoxSavePassword = new System.Windows.Forms.CheckBox();
            this.labelMessageLabel = new System.Windows.Forms.Label();
            this.labelMessageLink = new Ch.Cyberduck.Ui.Winforms.Controls.ClickLinkLabel();
            this.tableLayoutPanel1.SuspendLayout();
            ((System.ComponentModel.ISupportInitialize)(this.diskPictureBox)).BeginInit();
            this.SuspendLayout();
            // 
            // loginButton
            // 
            this.loginButton.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Bottom | System.Windows.Forms.AnchorStyles.Right)));
            this.loginButton.AutoSize = true;
            this.loginButton.DialogResult = System.Windows.Forms.DialogResult.OK;
            this.loginButton.Location = new System.Drawing.Point(371, 198);
            this.loginButton.Name = "loginButton";
            this.loginButton.Size = new System.Drawing.Size(50, 27);
            this.loginButton.TabIndex = 7;
            this.loginButton.Text = "Login";
            this.loginButton.UseVisualStyleBackColor = true;
            // 
            // cancelButton
            // 
            this.cancelButton.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Bottom | System.Windows.Forms.AnchorStyles.Right)));
            this.cancelButton.AutoSize = true;
            this.cancelButton.DialogResult = System.Windows.Forms.DialogResult.Cancel;
            this.cancelButton.Location = new System.Drawing.Point(427, 198);
            this.cancelButton.Name = "cancelButton";
            this.cancelButton.Size = new System.Drawing.Size(58, 27);
            this.cancelButton.TabIndex = 8;
            this.cancelButton.Text = "Cancel";
            this.cancelButton.UseVisualStyleBackColor = true;
            // 
            // tableLayoutPanel1
            // 
            this.tableLayoutPanel1.AutoSize = true;
            this.tableLayoutPanel1.ColumnCount = 5;
            this.tableLayoutPanel1.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle(System.Windows.Forms.SizeType.Absolute, 82F));
            this.tableLayoutPanel1.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle());
            this.tableLayoutPanel1.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle(System.Windows.Forms.SizeType.Percent, 100F));
            this.tableLayoutPanel1.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle());
            this.tableLayoutPanel1.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle());
            this.tableLayoutPanel1.Controls.Add(this.cancelButton, 4, 6);
            this.tableLayoutPanel1.Controls.Add(this.labelUsername, 1, 1);
            this.tableLayoutPanel1.Controls.Add(this.textBoxUsername, 2, 1);
            this.tableLayoutPanel1.Controls.Add(this.checkBoxPkAuthentication, 2, 4);
            this.tableLayoutPanel1.Controls.Add(this.labelPassword, 1, 2);
            this.tableLayoutPanel1.Controls.Add(this.checkBoxAnonymous, 2, 3);
            this.tableLayoutPanel1.Controls.Add(this.textBoxPassword, 2, 2);
            this.tableLayoutPanel1.Controls.Add(this.pkLabel, 2, 5);
            this.tableLayoutPanel1.Controls.Add(this.loginButton, 3, 6);
            this.tableLayoutPanel1.Controls.Add(this.diskPictureBox, 0, 0);
            this.tableLayoutPanel1.Controls.Add(this.checkBoxSavePassword, 0, 6);
            this.tableLayoutPanel1.Controls.Add(this.labelMessageLabel, 1, 3);
            this.tableLayoutPanel1.Dock = System.Windows.Forms.DockStyle.Fill;
            this.tableLayoutPanel1.Location = new System.Drawing.Point(0, 0);
            this.tableLayoutPanel1.Name = "tableLayoutPanel1";
            this.tableLayoutPanel1.Padding = new System.Windows.Forms.Padding(10);
            this.tableLayoutPanel1.RowCount = 7;
            this.tableLayoutPanel1.RowStyles.Add(new System.Windows.Forms.RowStyle(System.Windows.Forms.SizeType.Absolute, 35F));
            this.tableLayoutPanel1.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel1.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel1.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel1.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel1.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel1.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.tableLayoutPanel1.RowStyles.Add(new System.Windows.Forms.RowStyle(System.Windows.Forms.SizeType.Absolute, 23F));
            this.tableLayoutPanel1.Size = new System.Drawing.Size(498, 238);
            this.tableLayoutPanel1.TabIndex = 10;
            // 
            // labelUsername
            // 
            this.labelUsername.Anchor = System.Windows.Forms.AnchorStyles.Right;
            this.labelUsername.AutoSize = true;
            this.labelUsername.Location = new System.Drawing.Point(100, 52);
            this.labelUsername.Name = "labelUsername";
            this.labelUsername.Size = new System.Drawing.Size(63, 15);
            this.labelUsername.TabIndex = 0;
            this.labelUsername.Text = "Username:";
            this.labelUsername.TextAlign = System.Drawing.ContentAlignment.TopRight;
            // 
            // textBoxUsername
            // 
            this.textBoxUsername.Anchor = ((System.Windows.Forms.AnchorStyles)(((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Left) 
            | System.Windows.Forms.AnchorStyles.Right)));
            this.tableLayoutPanel1.SetColumnSpan(this.textBoxUsername, 3);
            this.textBoxUsername.Location = new System.Drawing.Point(169, 48);
            this.textBoxUsername.Name = "textBoxUsername";
            this.textBoxUsername.Size = new System.Drawing.Size(316, 23);
            this.textBoxUsername.TabIndex = 1;
            this.textBoxUsername.TextChanged += new System.EventHandler(this.textBoxUsername_TextChanged);
            // 
            // checkBoxPkAuthentication
            // 
            this.checkBoxPkAuthentication.AutoSize = true;
            this.tableLayoutPanel1.SetColumnSpan(this.checkBoxPkAuthentication, 3);
            this.checkBoxPkAuthentication.Location = new System.Drawing.Point(169, 131);
            this.checkBoxPkAuthentication.Name = "checkBoxPkAuthentication";
            this.checkBoxPkAuthentication.Size = new System.Drawing.Size(185, 19);
            this.checkBoxPkAuthentication.TabIndex = 6;
            this.checkBoxPkAuthentication.Text = "Use Public Key Authentication";
            this.checkBoxPkAuthentication.UseVisualStyleBackColor = true;
            this.checkBoxPkAuthentication.CheckedChanged += new System.EventHandler(this.checkBoxPkAuthentication_CheckedChanged);
            // 
            // labelPassword
            // 
            this.labelPassword.Anchor = System.Windows.Forms.AnchorStyles.Right;
            this.labelPassword.AutoSize = true;
            this.labelPassword.Location = new System.Drawing.Point(103, 81);
            this.labelPassword.Name = "labelPassword";
            this.labelPassword.Size = new System.Drawing.Size(60, 15);
            this.labelPassword.TabIndex = 2;
            this.labelPassword.Text = "Password:";
            this.labelPassword.TextAlign = System.Drawing.ContentAlignment.TopRight;
            // 
            // checkBoxAnonymous
            // 
            this.checkBoxAnonymous.AutoSize = true;
            this.tableLayoutPanel1.SetColumnSpan(this.checkBoxAnonymous, 3);
            this.checkBoxAnonymous.Location = new System.Drawing.Point(169, 106);
            this.checkBoxAnonymous.Name = "checkBoxAnonymous";
            this.checkBoxAnonymous.Size = new System.Drawing.Size(124, 19);
            this.checkBoxAnonymous.TabIndex = 5;
            this.checkBoxAnonymous.Text = "Anonymous Login";
            this.checkBoxAnonymous.UseVisualStyleBackColor = true;
            this.checkBoxAnonymous.CheckedChanged += new System.EventHandler(this.checkBoxAnonymous_CheckedChanged);
            // 
            // textBoxPassword
            // 
            this.textBoxPassword.Anchor = ((System.Windows.Forms.AnchorStyles)(((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Left) 
            | System.Windows.Forms.AnchorStyles.Right)));
            this.tableLayoutPanel1.SetColumnSpan(this.textBoxPassword, 3);
            this.textBoxPassword.Location = new System.Drawing.Point(169, 77);
            this.textBoxPassword.Name = "textBoxPassword";
            this.textBoxPassword.Size = new System.Drawing.Size(316, 23);
            this.textBoxPassword.TabIndex = 3;
            this.textBoxPassword.UseSystemPasswordChar = true;
            this.textBoxPassword.TextChanged += new System.EventHandler(this.textBoxPassword_TextChanged);
            // 
            // pkLabel
            // 
            this.pkLabel.Anchor = ((System.Windows.Forms.AnchorStyles)(((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Left) 
            | System.Windows.Forms.AnchorStyles.Right)));
            this.pkLabel.AutoSize = true;
            this.tableLayoutPanel1.SetColumnSpan(this.pkLabel, 3);
            this.pkLabel.Location = new System.Drawing.Point(186, 153);
            this.pkLabel.Margin = new System.Windows.Forms.Padding(20, 0, 3, 0);
            this.pkLabel.MinimumSize = new System.Drawing.Size(0, 30);
            this.pkLabel.Name = "pkLabel";
            this.pkLabel.Size = new System.Drawing.Size(299, 30);
            this.pkLabel.TabIndex = 10;
            this.pkLabel.Text = "No private key selected";
            // 
            // diskPictureBox
            // 
            this.diskPictureBox.Location = new System.Drawing.Point(13, 13);
            this.diskPictureBox.MinimumSize = new System.Drawing.Size(55, 55);
            this.diskPictureBox.Name = "diskPictureBox";
            this.tableLayoutPanel1.SetRowSpan(this.diskPictureBox, 5);
            this.diskPictureBox.Size = new System.Drawing.Size(55, 55);
            this.diskPictureBox.SizeMode = System.Windows.Forms.PictureBoxSizeMode.AutoSize;
            this.diskPictureBox.TabIndex = 11;
            this.diskPictureBox.TabStop = false;
            // 
            // checkBoxSavePassword
            // 
            this.checkBoxSavePassword.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Bottom | System.Windows.Forms.AnchorStyles.Left)));
            this.checkBoxSavePassword.AutoSize = true;
            this.tableLayoutPanel1.SetColumnSpan(this.checkBoxSavePassword, 3);
            this.checkBoxSavePassword.Location = new System.Drawing.Point(13, 206);
            this.checkBoxSavePassword.Name = "checkBoxSavePassword";
            this.checkBoxSavePassword.Size = new System.Drawing.Size(103, 19);
            this.checkBoxSavePassword.TabIndex = 4;
            this.checkBoxSavePassword.Text = "Save password";
            this.checkBoxSavePassword.UseVisualStyleBackColor = true;
            this.checkBoxSavePassword.CheckedChanged += new System.EventHandler(this.checkBoxSavePassword_CheckedChanged);
            // 
            // labelMessageLabel
            // 
            this.labelMessageLabel.Dock = System.Windows.Forms.DockStyle.Fill;
            this.labelMessageLabel.Location = new System.Drawing.Point(95, 103);
            this.labelMessageLabel.Name = "labelMessageLabel";
            this.labelMessageLabel.Size = new System.Drawing.Size(68, 25);
            this.labelMessageLabel.TabIndex = 12;
            this.labelMessageLabel.Text = "label1";
            // 
            // labelMessageLink
            // 
            this.labelMessageLink.AutoEllipsis = true;
            this.labelMessageLink.Dock = System.Windows.Forms.DockStyle.Fill;
            this.labelMessageLink.Location = new System.Drawing.Point(92, 10);
            this.labelMessageLink.Margin = new System.Windows.Forms.Padding(0);
            this.labelMessageLink.Name = "labelMessageLink";
            this.labelMessageLink.Size = new System.Drawing.Size(396, 35);
            this.labelMessageLink.TabIndex = 9;
            this.labelMessageLink.TabStop = true;
            this.labelMessageLink.Text = "label1";
            // 
            // LoginForm
            // 
            this.AcceptButton = this.loginButton;
            this.AutoScaleDimensions = new System.Drawing.SizeF(7F, 15F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.AutoSize = true;
            this.CancelButton = this.cancelButton;
            this.ClientSize = new System.Drawing.Size(498, 238);
            this.Controls.Add(this.tableLayoutPanel1);
            this.FormBorderStyle = System.Windows.Forms.FormBorderStyle.FixedDialog;
            this.Name = "LoginForm";
            this.StartPosition = System.Windows.Forms.FormStartPosition.CenterParent;
            this.Text = "Login";
            this.tableLayoutPanel1.ResumeLayout(false);
            this.tableLayoutPanel1.PerformLayout();
            ((System.ComponentModel.ISupportInitialize)(this.diskPictureBox)).EndInit();
            this.ResumeLayout(false);
            this.PerformLayout();

        }

        #endregion

        private System.Windows.Forms.Label labelUsername;
        private System.Windows.Forms.TextBox textBoxUsername;
        private System.Windows.Forms.Label labelPassword;
        private System.Windows.Forms.TextBox textBoxPassword;
        private System.Windows.Forms.CheckBox checkBoxSavePassword;
        private System.Windows.Forms.CheckBox checkBoxAnonymous;
        private System.Windows.Forms.CheckBox checkBoxPkAuthentication;
        private System.Windows.Forms.Button loginButton;
        private System.Windows.Forms.Button cancelButton;
        private ClickLinkLabel labelMessageLink;
        private System.Windows.Forms.TableLayoutPanel tableLayoutPanel1;
        private EllipsisLabel pkLabel;
        private System.Windows.Forms.OpenFileDialog openFileDialog;
        private System.Windows.Forms.PictureBox diskPictureBox;
        private System.Windows.Forms.Label labelMessageLabel;
    }
}