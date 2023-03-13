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

using ch.cyberduck.core;
using Ch.Cyberduck.Ui.Controller;
using System.Windows.Forms;

namespace Ch.Cyberduck.Ui.Winforms
{
    public partial class CreateFilePromptForm : PromptForm, ICreateFilePromptView
    {
        public CreateFilePromptForm()
        {
            InitializeComponent();

            Text = LocaleFactory.localizedString("Create new file", "File");
            pictureBox.Padding = new Padding(0, 0, 0, 5);
            label.Text = LocaleFactory.localizedString("Enter the name for the new file", "File");

            SetupButtons();
        }
        
        private void SetupButtons()
        {
            buttonPanel.SuspendLayout();

            buttonPanel.ColumnCount = 4;

            buttonPanel.Controls.Add(CreateButton(new Button()
            {
                DialogResult = DialogResult.OK,
                TabIndex = 0,
                Text = LocaleFactory.localizedString("Create", "File"),
            }, out var createButton), 1, 0);
            AcceptButton = createButton;

            buttonPanel.Controls.Add(CreateButton(new Button()
            {
                DialogResult = DialogResult.Yes,
                TabIndex = 1,
                Text = LocaleFactory.localizedString("Edit", "File"),
            }, out _), 2, 0);

            buttonPanel.Controls.Add(CreateButton(new Button()
            {
                DialogResult = DialogResult.Cancel,
                TabIndex = 5,
                Text = "Cancel",
            }, out var cancelButton), 3, 0);
            CancelButton = cancelButton;

            buttonPanel.ResumeLayout();
        }
    }
}
