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
using System;
using Ch.Cyberduck.Ui.Controller;

namespace Ch.Cyberduck.Ui.Winforms
{
    public partial class LoginForm : BaseForm, ILoginView
    {
        public LoginForm()
        {
            InitializeComponent();
        }

        public string Title
        {
            set { Text = value; }
        }

        public string Message
        {
            set { labelMessage.Text = value; }
        }

        public string Username
        {
            get { return textBoxUsername.Text; }
            set { textBoxUsername.Text = value; }
        }

        public string Password
        {
            get { return textBoxPassword.Text; }
            set { textBoxPassword.Text = value; }
        }

        public bool SavePasswordChecked
        {
            get { return checkBoxSavePassword.Checked; }
            set { checkBoxSavePassword.Checked = value; }
        }

        public bool SavePasswordEnabled
        {
            set { checkBoxSavePassword.Enabled = value; }
        }

        public bool UsernameEnabled
        {
            set { textBoxUsername.Enabled = value; }
        }

        public bool PasswordEnabled
        {
            set { textBoxPassword.Enabled = value; }
        }

        public bool AnonymousChecked
        {
            get { return checkBoxAnonymous.Checked; }
            set { checkBoxAnonymous.Checked = value; }
        }

        public bool PkCheckboxChecked
        {
            get { return checkBoxPkAuthentication.Checked; }
            set { checkBoxPkAuthentication.Checked = value; }
        }

        public bool PkCheckboxEnabled
        {
            set { checkBoxPkAuthentication.Enabled = value; }
        }

        public string PasswordLabel
        {
            set { labelPassword.Text = value; }
        }

        public string UsernameLabel
        {
            set { labelUsername.Text = value; }
        }

        public event EventHandler ChangedUsernameEvent;
        public event EventHandler ChangedPasswordEvent;
        public event EventHandler ChangedSavePasswordCheckboxEvent;
        public event EventHandler ChangedAnonymousCheckboxEvent;
        public event EventHandler ChangedPkCheckboxEvent;

        private void textBoxUsername_TextChanged(object sender, EventArgs e)
        {
            ChangedUsernameEvent(this, EventArgs.Empty);
        }

        private void textBoxPassword_TextChanged(object sender, EventArgs e)
        {
            ChangedPasswordEvent(this, EventArgs.Empty);
        }

        private void checkBoxSavePassword_CheckedChanged(object sender, EventArgs e)
        {
            ChangedSavePasswordCheckboxEvent(this, EventArgs.Empty);
        }

        private void checkBoxAnonymous_CheckedChanged(object sender, EventArgs e)
        {
            ChangedAnonymousCheckboxEvent(this, EventArgs.Empty);
        }

        private void checkBoxPkAuthentication_CheckedChanged(object sender, EventArgs e)
        {
            ChangedPkCheckboxEvent(this, EventArgs.Empty);
        }
    }
}