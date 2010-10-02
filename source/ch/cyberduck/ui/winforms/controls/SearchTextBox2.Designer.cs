namespace Ch.Cyberduck.ui.winforms.controls
{
    partial class SearchTextBox2
    {
        System.Windows.Forms.TextBox textBox = new System.Windows.Forms.TextBox();
        System.Windows.Forms.PictureBox xPictureBox = new System.Windows.Forms.PictureBox();
        private System.Windows.Forms.Timer keyStrokeTimer = new System.Windows.Forms.Timer();

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
            this.SuspendLayout();
            // 
            // SearchTextBox
            // 
            this.Name = "SearchTextBox";
            this.ResumeLayout(false);

        }
        #endregion
    }
}
