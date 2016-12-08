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

using System.Collections.Generic;
using System.Drawing;
using System.Windows.Forms;
using Ch.Cyberduck.Ui.Controller;

namespace Ch.Cyberduck.Ui.Winforms
{
    public partial class NewFolderPromptForm : PromptForm, INewFolderPromptView
    {
        private ComboBox regionComboBox;

        public NewFolderPromptForm()
        {
            InitializeComponent();
        }

        public bool RegionsEnabled
        {
            set
            {
                if (value && regionComboBox == null)
                {
                    regionComboBox = new ComboBox();
                    regionComboBox.FormattingEnabled = true;
                    regionComboBox.Location = new Point(84, 70);
                    regionComboBox.Name = "regionComboBox";
                    regionComboBox.Size = new Size(121, 23);
                    regionComboBox.DropDownStyle = ComboBoxStyle.DropDownList;
                    regionComboBox.TabIndex = 3;
                    regionComboBox.Anchor = (((AnchorStyles.Left | AnchorStyles.Right)));
                    tableLayoutPanel.RowCount++;
                    tableLayoutPanel.RowStyles.Insert(2, new RowStyle(SizeType.AutoSize));
                    tableLayoutPanel.SetRow(okButton, 3);
                    tableLayoutPanel.SetRow(cancelButton, 3);
                    tableLayoutPanel.Controls.Add(regionComboBox, 1, 2);
                    tableLayoutPanel.SetColumnSpan(regionComboBox, 3);
                }
            }
            protected get { return regionComboBox != null; }
        }

        public string Region
        {
            get { return regionComboBox != null ? (string) regionComboBox.SelectedValue : null; }
            set
            {
                if (regionComboBox != null)
                {
                    regionComboBox.SelectedValue = value;
                }
            }
        }

        public void PopulateRegions(IList<KeyValuePair<string, string>> regions)
        {
            if (regionComboBox != null)
            {
                regionComboBox.DataSource = regions;
                regionComboBox.ValueMember = "Key";
                regionComboBox.DisplayMember = "Value";
            }
        }
    }
}