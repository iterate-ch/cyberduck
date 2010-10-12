// 
// Copyright (c) 2010 Yves Langisch. All rights reserved.
// http://cyberduck.ch/
// 
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
// 
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
// 
// Bug fixes, suggestions and comments should be sent to:
// yves@cyberduck.ch
// 
using System;
using System.Collections;
using System.Collections.Generic;
using System.Drawing;
using System.Windows.Forms;
using BrightIdeasSoftware;
using ch.cyberduck.core;
using ch.cyberduck.ui.controller;
using Ch.Cyberduck.Ui.Controller;
using Ch.Cyberduck.Ui.Winforms.Controls;

namespace Ch.Cyberduck.Ui.Winforms
{
    public partial class TransferPromptForm : BaseForm, ITransferPromptView
    {
        private static readonly int MaxHeight = 800;
        private static readonly int MaxWidth = 800;
        private static readonly int MinHeight = 250;
        private static readonly int MinWidth = 450;
        private bool _expanded = true;

        public TransferPromptForm()
        {
            InitializeComponent();

            DoubleBuffered = true;
            MaximumSize = new Size(MaxWidth, MaxHeight + detailsTableLayoutPanel.Height);
            MinimumSize = new Size(MinWidth, MinHeight + detailsTableLayoutPanel.Height);

            browser.UseExplorerTheme = true;
            browser.UseTranslucentSelection = true;
            browser.OwnerDraw = true;
            browser.UseOverlays = false;
            browser.HeaderStyle = ColumnHeaderStyle.None;
            browser.ShowGroups = false;
            browser.ShowImagesOnSubItems = true;
            browser.TreeColumnRenderer = new BrowserRenderer();
            browser.SelectedRowDecoration = new ExplorerRowBorderDecoration();
            browser.MultiSelect = false;
            browser.FullRowSelect = true;            
            //browser.ItemsChanged += (sender, args) => ItemsChanged();

            //due to the checkbox feature the highlight bar is not being redrawn properly -> redraw the entire control instead
            browser.SelectedIndexChanged += (sender, args) => browser.Refresh();

            ScaledImageRenderer sir = new ScaledImageRenderer();

            treeColumnWarning.Renderer = sir;
            treeColumnCreate.Renderer = sir;
            treeColumnSync.Renderer = sir;
            
            treeColumnName.FillsFreeSpace = true;            

            toggleDetailsLabel.Click += delegate { ToggleDetailsEvent(); };
            toggleDetailsLabel.MouseDown += delegate { toggleDetailsLabel.ImageIndex = (_expanded ? 2 : 5); };
            toggleDetailsLabel.MouseEnter += delegate { toggleDetailsLabel.ImageIndex = (_expanded ? 1 : 4); };
            toggleDetailsLabel.MouseLeave += delegate { toggleDetailsLabel.ImageIndex = (_expanded ? 0 : 3); };
            toggleDetailsLabel.MouseUp += delegate { toggleDetailsLabel.ImageIndex = (_expanded ? 1 : 4); };
        }

        private class ScaledImageRenderer : BaseRenderer
        {
            protected override int DrawImage(Graphics g, Rectangle r, object imageSelector)
            {
                if (imageSelector is Image)
                {
                    Image image = imageSelector as Image;
                    int top = r.Y;
                    if (image.Size.Height < r.Height)
                        top += ((r.Height - image.Size.Height) / 2);

                    //make sure that 72dpi images are being scaled correctly
                    g.DrawImage(image, new Rectangle(r.X, top, image.Width, image.Height));
                    return image.Width;
                }
                return base.DrawImage(g, r, imageSelector);
            }
        }

        public void SetModel(IEnumerable<TreePathReference> model)
        {
            //browser.ClearObjects();
            browser.Roots = model;            
        }

        public void RefreshBrowserObject(TreePathReference reference)
        {
            if (reference != null){
            browser.RefreshObject(reference);
            } else
            {
                //browser.ReloadTree();
            }
        }

        public bool DetailsVisible
        {
            get { return _expanded; }
            set
            {
                if (_expanded != value)
                {                    
                    _expanded = value;
                    //todo make it flickerfree
                    //try http://stackoverflow.com/questions/487661/how-do-i-suspend-painting-for-a-control-and-its-children
                    SuspendLayout();                    
                    if (_expanded)
                    {
                        MaximumSize = new Size(MaxWidth, MaxHeight + detailsTableLayoutPanel.Height);
                        Height += detailsTableLayoutPanel.Height;
                        MinimumSize = new Size(MinWidth, MinHeight + detailsTableLayoutPanel.Height);
                        ResumeLayout();
                    }
                    else
                    {
                        MinimumSize = new Size(MinWidth, MinHeight);
                        Height -= detailsTableLayoutPanel.Height;
                        MaximumSize = new Size(MaxWidth, MaxHeight);
                    }

                    detailsTableLayoutPanel.Visible = _expanded;
                    ResumeLayout();
                    toggleDetailsLabel.ImageIndex = (_expanded ? 1 : 4);                   
                }
            }
        }

        public int NumberOfFiles
        {
            get { return browser.GetItemCount(); }
        }

        public void PopulateActions(IDictionary<TransferAction, string> actions)
        {
            comboBoxAction.Items.Clear();
            comboBoxAction.DataSource = new BindingSource(actions, null);
            comboBoxAction.DisplayMember = "Value";
            comboBoxAction.ValueMember = "Key";
        }

        public TransferAction SelectedAction
        {
            get { return (TransferAction) comboBoxAction.SelectedValue; }
            set { comboBoxAction.SelectedValue = value; }
        }

        public TreePathReference SelectedPath
        {
            get { return (TreePathReference)browser.SelectedObject; }
        }

        public string LocalFileUrl
        {
            set { localFileUrl.Text = value; }
        }

        public string LocalFileSize
        {
            set { localFileSize.Text = value; }
        }

        public string LocalFileModificationDate
        {
            set { localFileModificationDate.Text = value; }
        }

        public string RemoteFileUrl
        {
            set { remoteFileUrl.Text = value; }
        }

        public string RemoteFileSize
        {
            set { remoteFileSize.Text = value; }
        }

        public string RemoteFileModificationDate
        {
            set { remoteFileModificationDate.Text = value; }
        }

        public void ModelCanExpandDelegate(TreeListView.CanExpandGetterDelegate canExpandDelegate)
        {
            browser.CanExpandGetter = canExpandDelegate;
        }

        public void ModelChildrenGetterDelegate(TreeListView.ChildrenGetterDelegate childrenGetterDelegate)
        {
            browser.ChildrenGetter = childrenGetterDelegate;
        }

        public CheckStateGetterDelegate ModelCheckStateGetter
        {
            set { browser.CheckStateGetter = value; }
        }

        public CheckStatePutterDelegate ModelCheckStateSetter
        {
            set { browser.CheckStatePutter = value; }
        }

        public AspectGetterDelegate ModelFilenameGetter
        {
            set { treeColumnName.AspectGetter = value; }
        }

        public ImageGetterDelegate ModelIconGetter
        {
            set { treeColumnName.ImageGetter = value; }
        }

        public AspectGetterDelegate ModelSizeGetter
        {
            set { treeColumnSize.AspectGetter = value; }
        }

        public AspectToStringConverterDelegate ModelSizeAsStringGetter
        {
            set { treeColumnSize.AspectToStringConverter = value; }
        }

        public ImageGetterDelegate ModelWarningGetter
        {
            set { treeColumnWarning.ImageGetter = value; }
        }

        public ImageGetterDelegate ModelCreateGetter
        {
            set { treeColumnCreate.ImageGetter = value; }
        }

        public ImageGetterDelegate ModelSyncGetter
        {
            set { treeColumnSync.ImageGetter = value; }
        }

        public MulticolorTreeListView.ActiveGetterDelegate ModelActiveGetter
        {
            set { browser.ActiveGetter = value; }
        }

        public event VoidHandler ChangedActionEvent = delegate { };
        public event VoidHandler ChangedSelectionEvent = delegate { };
        public event VoidHandler ToggleDetailsEvent = delegate { };
        public event VoidHandler ItemsChanged;

        public void StartActivityAnimation()
        {
            animation.Visible = true;
        }

        public void StopActivityAnimation()
        {
            animation.Visible = false;
        }

        public string StatusLabel
        {
            set { statusLabel.Text = value; }
        }

        private void comboBoxAction_SelectionChangeCommitted(object sender, EventArgs e)
        {
            ChangedActionEvent();
        }

        private void browser_SelectionChanged(object sender, EventArgs e)
        {
            ChangedSelectionEvent();
        }

        public override string[] BundleNames
        {
            get { return new[]{"Prompt"}; }
        }
    }
}