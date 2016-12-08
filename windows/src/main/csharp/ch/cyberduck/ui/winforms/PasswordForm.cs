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

using System.Windows.Forms;
using Ch.Cyberduck.Core;
using Ch.Cyberduck.Ui.Controller;

namespace Ch.Cyberduck.Ui.Winforms
{
    public partial class PasswordForm : PromptForm, IPasswordPromptView
    {
        private readonly CheckBox saveCheckBox;

        public PasswordForm()
        {
            InitializeComponent();

            saveCheckBox = new CheckBox
            {
                Name = "saveCheckBox",
                TabIndex = 3,
                Anchor = (((AnchorStyles.Left | AnchorStyles.Right)))
            };
            tableLayoutPanel.RowCount++;
            tableLayoutPanel.RowStyles.Insert(2, new RowStyle(SizeType.AutoSize));
            tableLayoutPanel.SetRow(okButton, 3);
            tableLayoutPanel.SetRow(cancelButton, 3);
            tableLayoutPanel.Controls.Add(saveCheckBox, 1, 2);
            tableLayoutPanel.SetColumnSpan(saveCheckBox, 3);
        }

        public string Title
        {
            set { Text = value; }
        }

        public string Reason
        {
            set { label.Text = value; }
        }

        public string OkButtonText
        {
            set { okButton.Text = value; }
        }

        public string Placeholder
        {
            set { NativeMethods.SendMessage(saveCheckBox.Handle, NativeConstants.EM_SETCUEBANNER, 0, value); }
        }

        public bool SavePassword
        {
            get { return saveCheckBox.Checked; }
            set { saveCheckBox.Checked = value; }
        }
    }
}