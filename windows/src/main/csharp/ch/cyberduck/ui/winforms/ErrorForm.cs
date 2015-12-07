// 
// Copyright (c) 2010-2013 Yves Langisch. All rights reserved.
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

using System.Collections.Generic;
using System.Drawing;
using System.Windows.Forms;
using BrightIdeasSoftware;
using Ch.Cyberduck.Ui.Controller;
using ch.cyberduck.core;
using ch.cyberduck.core.exception;

namespace Ch.Cyberduck.Ui.Winforms
{
    public partial class ErrorForm : BaseForm, IErrorView
    {
        private static readonly int MaxHeight = 800;
        private static readonly int MaxWidth = 800;
        private static readonly int MinHeight = 250;
        private static readonly int MinWidth = 450;
        private readonly ErrorRenderer _renderer = new ErrorRenderer();
        private bool _expanded = true;

        public ErrorForm()
        {
            InitializeComponent();

            Load += delegate { okButton.Focus(); };

            MaximumSize = new Size(MaxWidth, MaxHeight + detailPanel.Height);
            MinimumSize = new Size(MinWidth, MinHeight + detailPanel.Height);

            detailTextBox.Font = new Font(FontFamily.GenericMonospace, 8);

            toggleTranscriptLabel.Text = "        " + LocaleFactory.localizedString("Toggle Transcript");
            toggleTranscriptLabel.ImageIndex = (_expanded ? 1 : 4);

            // configure error listview
            errorListView.OwnerDraw = true;
            errorListView.UseOverlays = false;
            errorListView.FullRowSelect = true;
            errorListView.RowHeight = 60;
            errorListView.UseAlternatingBackColors = true;
            errorListView.AlternateRowBackColor = Color.WhiteSmoke;
            errorListView.MultiSelect = false;
            errorListView.HeaderStyle = ColumnHeaderStyle.None;
            errorListView.ShowGroups = false;

            errorColumn.Renderer = _renderer;
            errorColumn.FillsFreeSpace = true;
            _renderer.CellPadding = new Size(2, 4);
            _renderer.ErrorFont = new Font(errorListView.Font, FontStyle.Bold);
            _renderer.DescriptionFont = new Font(errorListView.Font, FontStyle.Bold);
            _renderer.HostFont = new Font(FontFamily.GenericMonospace, 8);
            _renderer.ErrorHostSpace = 1;
            _renderer.HostDescriptionSpace = 6;

            toggleTranscriptLabel.Click += delegate { ToggleTranscriptEvent(); };
            toggleTranscriptLabel.MouseDown += delegate { toggleTranscriptLabel.ImageIndex = (_expanded ? 2 : 5); };
            toggleTranscriptLabel.MouseEnter += delegate { toggleTranscriptLabel.ImageIndex = (_expanded ? 1 : 4); };
            toggleTranscriptLabel.MouseLeave += delegate { toggleTranscriptLabel.ImageIndex = (_expanded ? 0 : 3); };
            toggleTranscriptLabel.MouseUp += delegate { toggleTranscriptLabel.ImageIndex = (_expanded ? 1 : 4); };
        }

        public override string[] BundleNames
        {
            get { return new[] {"Error"}; }
        }

        public AspectGetterDelegate ModelHostGetter
        {
            set { errorColumn.AspectGetter = value; }
        }

        public AspectGetterDelegate ModelDescriptionGetter
        {
            set { _renderer.DescriptionAspectGetter = value; }
        }

        public AspectGetterDelegate ModelErrorMessageGetter
        {
            set { _renderer.ErrorAspectGetter = value; }
        }

        public void SetModel(IEnumerable<BackgroundException> model)
        {
            errorListView.SetObjects(model);
        }

        public string Transcript
        {
            set { detailTextBox.Text = value; }
        }

        public bool TranscriptEnabled
        {
            set { toggleTranscriptLabel.Enabled = value; }
        }

        public bool TranscriptVisible
        {
            get { return _expanded; }
            set
            {
                if (_expanded != value)
                {
                    _expanded = value;

                    if (_expanded)
                    {
                        MaximumSize = new Size(MaxWidth, MaxHeight + detailPanel.Height);
                        Height += detailPanel.Height;
                        MinimumSize = new Size(MinWidth, MinHeight + detailPanel.Height);
                    }
                    else
                    {
                        MinimumSize = new Size(MinWidth, MinHeight);
                        Height -= detailPanel.Height;
                        MaximumSize = new Size(MaxWidth, MaxHeight);
                    }
                    detailPanel.Visible = _expanded;
                    toggleTranscriptLabel.ImageIndex = (_expanded ? 1 : 4);
                }
            }
        }

        public event VoidHandler ToggleTranscriptEvent;
    }
}