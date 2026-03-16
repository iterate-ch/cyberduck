// Copyright (c) 2010-2026 Yves Langisch. All rights reserved.
// http://cyberduck.io/
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
// feedback@cyberduck.io

using System;
using System.Collections.Generic;
using Ch.Cyberduck.Ui.Controller;
using com.amazonaws.regions;
using static Ch.Cyberduck.ImageHelper;

namespace Ch.Cyberduck.Ui.Winforms
{
    public partial class LocationForm : BaseForm, ILocationPromptView
    {
        public LocationForm()
        {
            InitializeComponent();
            pictureBox.Image = Images.CyberduckApplication;
        }

        protected override bool EnableAutoSizePosition => false;

        public override string[] BundleNames => new[] { "Keychain" };

        public string Title
        {
            set { Text = value; }
        }

        public string Message
        {
            set { label.Text = value; }
        }

        public string Location
        {
            get => (string)comboBox1.SelectedValue;
            set => comboBox1.SelectedValue = value;
        }

        public void PopulateLocations(IList<KeyValuePair<string, string>> locations)
        {
            comboBox1.DataSource = locations;
            comboBox1.ValueMember = "Key";
            comboBox1.DisplayMember = "Value";

        }
    }
}
