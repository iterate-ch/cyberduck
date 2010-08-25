namespace Ch.Cyberduck.Ui.Winforms.Controls
{
    partial class SearchTextBox
    {
        /// <summary> 
        /// Clean up any resources being used.
        /// </summary>
        /// <param name="disposing">true if managed resources should be disposed; otherwise, false.</param>
        protected override void Dispose(bool disposing)
        {
            if (disposing)
            {
                _inactiveFont.Dispose();
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
            this.searchOverlayLabel = new System.Windows.Forms.Label();
            this.searchText = new System.Windows.Forms.TextBox();
            this.searchImage = new System.Windows.Forms.PictureBox();
            ((System.ComponentModel.ISupportInitialize)(this.searchImage)).BeginInit();
            this.SuspendLayout();
            // 
            // searchOverlayLabel
            // 
            this.searchOverlayLabel.Anchor =
                ((System.Windows.Forms.AnchorStyles)
                 (((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Bottom)
                   | System.Windows.Forms.AnchorStyles.Left)));
            this.searchOverlayLabel.TextAlign = System.Drawing.ContentAlignment.MiddleLeft;
            this.searchOverlayLabel.AutoSize = false;
            this.searchOverlayLabel.Location = new System.Drawing.Point(0, 0);
            this.searchOverlayLabel.Margin = new System.Windows.Forms.Padding(0, 0, 22, 0);
            this.searchOverlayLabel.Name = "searchOverlayLabel";
            this.searchOverlayLabel.Size = new System.Drawing.Size(127, 21);
            this.searchOverlayLabel.TabIndex = 0;
            this.searchOverlayLabel.MouseLeave += new System.EventHandler(this.searchOverlayLabel_MouseLeave);
            this.searchOverlayLabel.Click += new System.EventHandler(this.searchOverlayLabel_Click);
            this.searchOverlayLabel.MouseEnter += new System.EventHandler(this.searchOverlayLabel_MouseEnter);
            // 
            // searchText
            // 
            this.searchText.Anchor =
                ((System.Windows.Forms.AnchorStyles)
                 ((((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Bottom)
                    | System.Windows.Forms.AnchorStyles.Left)
                   | System.Windows.Forms.AnchorStyles.Right)));
            this.searchText.BorderStyle = System.Windows.Forms.BorderStyle.None;
            this.searchText.Location = new System.Drawing.Point(3, 3);
            this.searchText.Margin = new System.Windows.Forms.Padding(0);
            this.searchText.Name = "searchText";
            this.searchText.Size = new System.Drawing.Size(125, 15);
            this.searchText.TabIndex = 0;
            this.searchText.TabStop = false;
            this.searchText.MouseLeave += new System.EventHandler(this.searchOverlayLabel_MouseLeave);
            this.searchText.MouseEnter += new System.EventHandler(this.searchOverlayLabel_MouseEnter);
            this.searchText.TextChanged += new System.EventHandler(this.searchText_TextChanged);
            this.searchText.GotFocus += new System.EventHandler(this.searchText_GotFocus);
            this.searchText.KeyDown += new System.Windows.Forms.KeyEventHandler(this.searchText_KeyUp);
            this.searchText.LostFocus += new System.EventHandler(this.searchText_LostFocus);
            // 
            // searchImage
            // 
            this.searchImage.AccessibleRole = System.Windows.Forms.AccessibleRole.PushButton;
            this.searchImage.Anchor =
                ((System.Windows.Forms.AnchorStyles)
                 (((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Bottom)
                   | System.Windows.Forms.AnchorStyles.Right)));
            this.searchImage.Cursor = System.Windows.Forms.Cursors.Arrow;
            this.searchImage.Location = new System.Drawing.Point(128, 0);
            this.searchImage.Padding = new System.Windows.Forms.Padding(0,1,0,0);
            this.searchImage.Margin = new System.Windows.Forms.Padding(0);
            this.searchImage.Name = "searchImage";
            this.searchImage.Size = new System.Drawing.Size(23, 21);
            this.searchImage.TabIndex = 1;
            this.searchImage.TabStop = false;
            this.searchImage.MouseLeave += new System.EventHandler(this.searchImage_MouseLeave);
            this.searchImage.Click += new System.EventHandler(this.searchImage_Click);
            this.searchImage.MouseEnter += new System.EventHandler(this.searchImage_MouseEnter);
            // 
            // SearchTextBox
            // 
            this.BackColor = System.Drawing.SystemColors.Window;
            this.Controls.Add(this.searchOverlayLabel);
            this.Controls.Add(this.searchText);
            this.Controls.Add(this.searchImage);
            this.Cursor = System.Windows.Forms.Cursors.IBeam;
            this.Size = new System.Drawing.Size(152, 21);
            ((System.ComponentModel.ISupportInitialize)(this.searchImage)).EndInit();
            this.ResumeLayout(false);
            this.PerformLayout();
        }

        #endregion

        private System.Windows.Forms.Label searchOverlayLabel;
        private System.Windows.Forms.TextBox searchText;
        private System.Windows.Forms.PictureBox searchImage;
    }
}
