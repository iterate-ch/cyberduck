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
using System.Windows.Forms;

namespace Ch.Cyberduck.Ui.Winforms
{
    public partial class PasswordForm : PromptForm, IPasswordPromptView
    {
        private readonly CheckBox saveCheckBox;
        private readonly Button okButton;
        private readonly Button skipButton;

        public PasswordForm()
        {
            InitializeComponent();

            inputTextBox.UseSystemPasswordChar = true;
            
            tableLayoutPanel.SuspendLayout();

            var lastRow = tableLayoutPanel.RowCount++;
            var layoutRow = lastRow - 1;
            tableLayoutPanel.RowStyles.Insert(layoutRow, new(SizeType.AutoSize));
            tableLayoutPanel.SetRow(buttonPanel, lastRow);

            saveCheckBox = new CheckBox
            {
                Name = "saveCheckBox",
                TabIndex = 1,
                Anchor = (((AnchorStyles.Left | AnchorStyles.Right))),
                Text = "Save Password"
            };
            tableLayoutPanel.Controls.Add(saveCheckBox, 1, layoutRow);

            SetupButtons(out okButton, out skipButton);

            tableLayoutPanel.ResumeLayout();
        }

        public bool CanSkip
        {
            set => skipButton.Visible = value;
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

        public bool SavePasswordState
        {
            get { return saveCheckBox.Checked; }
            set { saveCheckBox.Checked = value; }
        }

        public bool SavePasswordEnabled
        {
            set { saveCheckBox.Enabled = value; }
        }

        public string SkipButtonText
        {
            set => skipButton.Text = value;
        }

        private void SetupButtons(out Button createButton, out Button skipButton)
        {
            buttonPanel.SuspendLayout();

            buttonPanel.ColumnCount = 4;
            buttonPanel.Controls.Add(CreateButton(new Button()
            {
                DialogResult = DialogResult.OK,
                TabIndex = 16,
                Text = "Create",
            }, out createButton), 1, 0);
            AcceptButton = createButton;

            buttonPanel.Controls.Add(CreateButton(new Button()
            {
                DialogResult = DialogResult.Yes,
                TabIndex = 17,
                Text = "Skip",
            }, out skipButton), 2, 0);

            buttonPanel.Controls.Add(CreateButton(new Button()
            {
                DialogResult = DialogResult.Cancel,
                TabIndex = 18,
                Text = "Cancel",
            }, out var cancelButton), 3, 0);
            CancelButton = cancelButton;

            buttonPanel.ResumeLayout();
        }
    }
}
