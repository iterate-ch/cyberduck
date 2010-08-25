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
using System.Drawing;
using System.Windows.Forms;
using BrightIdeasSoftware;
using ch.cyberduck.core.threading;
using ch.cyberduck.ui.controller;
using Ch.Cyberduck.Ui.Controller;

namespace Ch.Cyberduck.Ui.Winforms
{
    public partial class ActivityForm : BaseForm, IActivityView
    {
        private readonly int _initialDescriptionColumnWidth;
        private readonly int _initialFormWidth;

        public ActivityForm()
        {
            InitializeComponent();

            _initialFormWidth = Width;
            _initialDescriptionColumnWidth = descriptionColumn.Width;

            // ImageList doesn't work with animated GIFs. At least not in conjuction with our 
            // custom renderer.
            /*
            ImageList images = new ImageList {ImageSize = new Size(32, 32), ColorDepth = ColorDepth.Depth32Bit};            
            images.Images.Add("stop", ResourcesBundle.stop);
            images.Images.Add("throbber", ResourcesBundle.throbber);
            */
            
            //todo config gemäss ErrorForm (vor allem resizing)
            activitiyListView.RowHeight = 45;
            activitiyListView.ShowGroups = false;
            activitiyListView.OwnerDraw = true;
            activitiyListView.UseOverlays = false;
            activitiyListView.FullRowSelect = true;
            activitiyListView.MultiSelect = false;
            activitiyListView.HeaderStyle = ColumnHeaderStyle.None;

            // Activity name (task description)
            TaskRenderer taskRenderer = new TaskRenderer();
            taskRenderer.TitleFont = new Font(activitiyListView.Font.Name, activitiyListView.Font.Size + 1);
            taskRenderer.CellPadding = new Size(2, 5);
            descriptionColumn.Renderer = taskRenderer;

            // Throbber
            AnimatedImageRenderer throbberRenderer = new AnimatedImageRenderer(true);
            throbberColumn.AspectGetter = delegate { return "throbber"; };
            throbberColumn.Renderer = throbberRenderer;

            // Stop image (button)
            AnimatedImageRenderer stopRenderer = new AnimatedImageRenderer();
            stopColumn.AspectGetter = delegate { return "stop"; };
            stopColumn.Renderer = stopRenderer;
        }

        public void SetModel(IEnumerable model)
        {
            activitiyListView.SetObjects(model);
        }

        public void RefreshTask(BackgroundAction action)
        {
            activitiyListView.RefreshObject(action);
        }

        public void AddTask(BackgroundAction action)
        {
            activitiyListView.AddObject(action);
        }

        public void RemoveTask(BackgroundAction action)
        {
            activitiyListView.RemoveObject(action);
        }

        public BackgroundAction SelectedTask
        {
            get { return (BackgroundAction) activitiyListView.SelectedObject; }
        }

        public AspectGetterDelegate ModelTitleGetter
        {
            set { descriptionColumn.AspectGetter = value; }
        }

        public AspectGetterDelegate ModelDescriptionGetter
        {
            set { ((TaskRenderer) descriptionColumn.Renderer).DescriptionAspectGetter = value; }
        }

        public AspectGetterDelegate ModelIsRunningGetter
        {
            set { ((AnimatedImageRenderer) throbberColumn.Renderer).AnimationRunningAspectGetter = value; }
        }

        public event VoidHandler StopActionEvent = delegate { };

        private void ActivityForm_SizeChanged(object sender, EventArgs e)
        {
            // resize description column
            int diff = Width - _initialFormWidth;
            if (diff > 0)
                descriptionColumn.Width = _initialDescriptionColumnWidth + diff;
        }

        private void activitiyListView_CellClick(object sender, CellClickEventArgs e)
        {
            // stop button clicked
            if (e.Column == stopColumn && e.HitTest.HitTestLocation == HitTestLocation.Image)
            {
                StopActionEvent();
            }
        }
    }
}