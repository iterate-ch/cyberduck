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

using System.Drawing;
using Ch.Cyberduck.Ui.Controller;

namespace Ch.Cyberduck.Ui.Winforms
{
    public partial class PasswordForm : BaseForm, IPasswordView
    {
        public PasswordForm()
        {
            InitializeComponent();
        }

        public Image DiskIcon
        {
            set { pwdPictureBox.Image = value; }
        }

        public string Title
        {
            set { Text = value; }
        }

        public string Reason
        {
            set { }
        }

        public string PasswordLabel
        {
            set { labelPassword.Text = value; }
        }

        public string Password
        {
            get { return textBoxPassword.Text; }
            set { textBoxPassword.Text = value; }
        }

        public bool SavePasswordState
        {
            get { return checkBoxSavePassword.Checked; }
            set { checkBoxSavePassword.Checked = value; }
        }

        public bool SavePasswordEnabled
        {
            get { return checkBoxSavePassword.Enabled; }
            set { checkBoxSavePassword.Enabled = value; }
        }

        public Image PwdIcon
        {
            set { pwdPictureBox.Image = value; }
        }

        public event ValidateInputHandler ValidateInput;
    }
}