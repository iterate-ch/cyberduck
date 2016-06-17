// 
// Copyright (c) 2010-2016 Yves Langisch. All rights reserved.
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
// 

using Ch.Cyberduck.Core.Resources;
using Ch.Cyberduck.Ui.Controller;

namespace Ch.Cyberduck.Ui.Winforms
{
    public partial class DonationForm : BaseForm, IDonationView
    {
        public DonationForm()
        {
            InitializeComponent();

            pictureBox.Image = IconCache.Instance.IconForName("cyberduck", 64);
        }

        public override string[] BundleNames
        {
            get { return new[] {"Donate"}; }
        }

        public bool NeverShowDonation
        {
            get { return neverShowDonationCheckBox.Checked; }
            set { neverShowDonationCheckBox.Checked = value; }
        }

        public string Title
        {
            set { Text = value; }
        }
    }
}