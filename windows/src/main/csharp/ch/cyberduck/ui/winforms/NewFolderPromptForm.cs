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

using Ch.Cyberduck.Ui.Controller;
using System.Collections.Generic;
using System.Drawing;
using System.Windows.Forms;

namespace Ch.Cyberduck.Ui.Winforms
{
    public partial class NewFolderPromptForm : PromptForm, INewFolderPromptView
    {
        private readonly int regionRow;
        private ComboBox regionComboBox;

        public NewFolderPromptForm()
        {
            InitializeComponent();

            tableLayoutPanel.SuspendLayout();

            var newButtonRow = tableLayoutPanel.RowCount++;
            regionRow = newButtonRow - 1;
            tableLayoutPanel.RowStyles.Insert(regionRow, new());
            tableLayoutPanel.SetRow(buttonPanel, newButtonRow);

            SetupButtons();

            tableLayoutPanel.ResumeLayout();
        }

        public bool RegionsEnabled
        {
            set
            {
                if (value && regionComboBox == null)
                {
                    regionComboBox = new ComboBox();
                    regionComboBox.Anchor = (((AnchorStyles.Left | AnchorStyles.Right)));
                    regionComboBox.DropDownStyle = ComboBoxStyle.DropDownList;
                    regionComboBox.FormattingEnabled = true;
                    regionComboBox.Margin = new(6);
                    regionComboBox.Name = "regionComboBox";
                    regionComboBox.Size = new Size(121, 23);
                    regionComboBox.TabIndex = 1;

                    tableLayoutPanel.Controls.Add(regionComboBox, 1, regionRow);
                }
            }
            protected get { return regionComboBox != null; }
        }

        public string Region
        {
            get { return regionComboBox != null ? (string)regionComboBox.SelectedValue : null; }
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

        protected void SetupButtons()
        {
            buttonPanel.SuspendLayout();

            buttonPanel.ColumnCount = 3;
            buttonPanel.Controls.Add(CreateButton(new Button()
            {
                DialogResult = DialogResult.OK,
                TabIndex = 16,
                Text = "Create"
            }, out var createButton), 1, 0);
            AcceptButton = createButton;

            buttonPanel.Controls.Add(CreateButton(new Button()
            {
                DialogResult = DialogResult.Cancel,
                TabIndex = 17,
                Text = "Cancel",
            }, out var cancelButton), 2, 0);
            CancelButton = cancelButton;

            buttonPanel.ResumeLayout();
        }
    }
}
