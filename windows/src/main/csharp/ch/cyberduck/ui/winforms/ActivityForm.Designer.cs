namespace Ch.Cyberduck.Ui.Winforms
{
    partial class ActivityForm
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
            System.ComponentModel.ComponentResourceManager resources = new System.ComponentModel.ComponentResourceManager(typeof(ActivityForm));
            this.activitiyListView = new BrightIdeasSoftware.ObjectListView();
            this.descriptionColumn = new BrightIdeasSoftware.OLVColumn();
            this.throbberColumn = new BrightIdeasSoftware.OLVColumn();
            this.stopColumn = new BrightIdeasSoftware.OLVColumn();
            this.imageList1 = new System.Windows.Forms.ImageList(this.components);
            ((System.ComponentModel.ISupportInitialize)(this.activitiyListView)).BeginInit();
            this.SuspendLayout();
            // 
            // activitiyListView
            // 
            this.activitiyListView.AllColumns.Add(this.descriptionColumn);
            this.activitiyListView.AllColumns.Add(this.throbberColumn);
            this.activitiyListView.AllColumns.Add(this.stopColumn);
            this.activitiyListView.AlternateRowBackColor = System.Drawing.SystemColors.Menu;
            this.activitiyListView.Columns.AddRange(new System.Windows.Forms.ColumnHeader[] {
            this.descriptionColumn,
            this.throbberColumn,
            this.stopColumn});
            this.activitiyListView.Cursor = System.Windows.Forms.Cursors.Default;
            this.activitiyListView.Dock = System.Windows.Forms.DockStyle.Fill;
            this.activitiyListView.Location = new System.Drawing.Point(0, 0);
            this.activitiyListView.MultiSelect = false;
            this.activitiyListView.Name = "activitiyListView";
            this.activitiyListView.Size = new System.Drawing.Size(427, 303);
            this.activitiyListView.TabIndex = 3;
            this.activitiyListView.UseAlternatingBackColors = true;
            this.activitiyListView.UseCompatibleStateImageBehavior = false;
            this.activitiyListView.View = System.Windows.Forms.View.Details;
            this.activitiyListView.CellClick += new System.EventHandler<BrightIdeasSoftware.CellClickEventArgs>(this.activitiyListView_CellClick);
            // 
            // descriptionColumn
            // 
            this.descriptionColumn.HeaderFont = null;
            this.descriptionColumn.Width = 250;
            // 
            // throbberColumn
            // 
            this.throbberColumn.HeaderFont = null;
            this.throbberColumn.Width = 90;
            // 
            // stopColumn
            // 
            this.stopColumn.HeaderFont = null;
            this.stopColumn.Width = 80;
            // 
            // imageList1
            // 
            this.imageList1.ImageStream = ((System.Windows.Forms.ImageListStreamer)(resources.GetObject("imageList1.ImageStream")));
            this.imageList1.TransparentColor = System.Drawing.Color.Transparent;
            this.imageList1.Images.SetKeyName(0, "throbber.gif");
            // 
            // ActivityForm
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 13F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.ClientSize = new System.Drawing.Size(427, 303);
            this.Controls.Add(this.activitiyListView);
            this.FormBorderStyle = System.Windows.Forms.FormBorderStyle.SizableToolWindow;
            this.Name = "ActivityForm";
            this.Text = "Activity";
            this.SizeChanged += new System.EventHandler(this.ActivityForm_SizeChanged);
            ((System.ComponentModel.ISupportInitialize)(this.activitiyListView)).EndInit();
            this.ResumeLayout(false);

        }

        #endregion

        private BrightIdeasSoftware.ObjectListView activitiyListView;
        private BrightIdeasSoftware.OLVColumn descriptionColumn;
        private BrightIdeasSoftware.OLVColumn throbberColumn;
        private BrightIdeasSoftware.OLVColumn stopColumn;
        private System.Windows.Forms.ImageList imageList1;

    }
}