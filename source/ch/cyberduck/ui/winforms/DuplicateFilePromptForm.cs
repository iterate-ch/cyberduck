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
using System.Windows.Forms;
using ch.cyberduck.core.i18n;
using Ch.Cyberduck.Ui.Controller;

namespace Ch.Cyberduck.Ui.Winforms
{
    public partial class DuplicateFilePromptForm : PromptForm, IDuplicateFilePromptView
    {
        public DuplicateFilePromptForm()
        {
            InitializeComponent();

            Text = Locale.localizedString("Duplicate File", "Duplicate");
            Button cancelBtn = new Button
                                   {
                                       AutoSize = true,
                                       Size = cancelButton.Size,
                                       TabIndex = 5,
                                       Text = Locale.localizedString("Cancel", "Duplicate"),
                                       UseVisualStyleBackColor = true,
                                       Anchor = cancelButton.Anchor
                                   };
            tableLayoutPanel.Controls.Add(cancelBtn, 1, 2);
            tableLayoutPanel.RowStyles[0].Height = 20F;

            pictureBox.Width = 32;
            label.Text = Locale.localizedString("Enter the name for the new file:", "Duplicate");
            okButton.Text = Locale.localizedString("Duplicate", "Duplicate");

            // cancelButton is the 'Edit' button now
            cancelButton.DialogResult = DialogResult.Yes;
            cancelButton.Text = Locale.localizedString("Edit", "Duplicate");

            CancelButton = cancelBtn;
        }
    }
}