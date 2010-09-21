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
using System.Drawing;
using System.Windows.Forms;
using ch.cyberduck.core.i18n;
using ch.cyberduck.ui.controller;
using Ch.Cyberduck.Ui.Controller;

namespace Ch.Cyberduck.Ui.Winforms
{
    public partial class LoginForm : BaseForm, ILoginView
    {
        public LoginForm()
        {
            InitializeComponent();

            openFileDialog.Title = Locale.localizedString("Select the private key in PEM or PuTTY format", "Credentials");

            //todo localization
            openFileDialog.Filter = "Private Key Files (*.pem;*.crt;*.ppk)|*.pem;*.crt;*.ppk|All Files (*.*)|*.*";
            openFileDialog.FilterIndex = 1;
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

        public bool SavePasswordState
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

        public bool AnonymousState
        {
            get { return checkBoxAnonymous.Checked; }
            set { checkBoxAnonymous.Checked = value; }
        }

        public bool PkCheckboxState
        {
            get { return checkBoxPkAuthentication.Checked; }
            set { checkBoxPkAuthentication.Checked = value; }
        }

        public bool PkCheckboxEnabled
        {
            set { checkBoxPkAuthentication.Enabled = value; }
        }

        public string PkLabel
        {
            set
            {
                pkLabel.Text = value;
                pkLabel.ForeColor = checkBoxPkAuthentication.Checked
                                        ? Color.FromKnownColor(KnownColor.ControlText)
                                        : Color.Gray;
            }
            get { return pkLabel.Text; }
        }

        public string PasswordLabel
        {
            set { labelPassword.Text = value; }
        }

        public string UsernameLabel
        {
            set { labelUsername.Text = value; }
        }

        public void ShowPrivateKeyBrowser(string path)
        {
            if (!Visible) return;

            openFileDialog.InitialDirectory = path;
            openFileDialog.FileName = String.Empty;
            if (DialogResult.OK == openFileDialog.ShowDialog())
            {
                ChangedPrivateKey(this, new PrivateKeyArgs(openFileDialog.FileName));
            }
            else
            {
                ChangedPrivateKey(this, new PrivateKeyArgs(null));
            }
        }

        public Image DiskIcon
        {
            set { diskPictureBox.Image = value; }
        }

        public bool AnonymousEnabled
        {
            set { checkBoxAnonymous.Enabled = value; }
        }

        public event VoidHandler ChangedUsernameEvent;
        public event VoidHandler ChangedPasswordEvent;
        public event VoidHandler ChangedSavePasswordCheckboxEvent;
        public event VoidHandler ChangedAnonymousCheckboxEvent;
        public event VoidHandler ChangedPkCheckboxEvent;
        public event EventHandler<PrivateKeyArgs> ChangedPrivateKey;

        private void textBoxUsername_TextChanged(object sender, EventArgs e)
        {
            ChangedUsernameEvent();
        }

        private void textBoxPassword_TextChanged(object sender, EventArgs e)
        {
            ChangedPasswordEvent();
        }

        private void checkBoxSavePassword_CheckedChanged(object sender, EventArgs e)
        {
            ChangedSavePasswordCheckboxEvent();
        }

        private void checkBoxAnonymous_CheckedChanged(object sender, EventArgs e)
        {
            ChangedAnonymousCheckboxEvent();
        }

        private void checkBoxPkAuthentication_CheckedChanged(object sender, EventArgs e)
        {
            ChangedPkCheckboxEvent();
        }
    }
}