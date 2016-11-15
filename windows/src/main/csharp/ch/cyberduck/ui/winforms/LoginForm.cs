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

using System;
using System.Collections.Generic;
using System.Drawing;
using System.Media;
using System.Windows.Forms;
using ch.cyberduck.core;
using Ch.Cyberduck.Ui.Controller;

namespace Ch.Cyberduck.Ui.Winforms
{
    public partial class LoginForm : BaseForm, ILoginView
    {
        public LoginForm()
        {
            InitializeComponent();

            openFileDialog.Title = LocaleFactory.localizedString("Select the private key in PEM or PuTTY format",
                "Credentials");
            labelMessageLink.Font = DefaultFontBold;

            openFileDialog.Filter = "Private Key Files (*.pem;*.crt;*.ppk)|*.pem;*.crt;*.ppk|All Files (*.*)|*.*";
            openFileDialog.FilterIndex = 1;

            FormClosing += delegate(object sender, FormClosingEventArgs args)
            {
                bool cancel = DialogResult != DialogResult.Cancel && !ValidateInput();
                if (cancel)
                {
                    args.Cancel = true;
                    SystemSounds.Beep.Play();
                }
            };
        }

        public string Title
        {
            set { Text = value; }
        }

        public string Message
        {
            set
            {
                labelMessageLabel.Visible = false;
                labelMessageLink.Visible = false;
                tableLayoutPanel1.Controls.Remove(labelMessageLink);
                tableLayoutPanel1.Controls.Remove(labelMessageLabel);
                if (value.StartsWith(Scheme.http.name()))
                {
                    try
                    {
                        new Uri(value);
                        tableLayoutPanel1.Controls.Add(labelMessageLink, 1, 0);
                        labelMessageLink.Visible = true;
                        labelMessageLink.Text = value;
                        tableLayoutPanel1.SetColumnSpan(labelMessageLink, 4);
                    }
                    catch (UriFormatException)
                    {
                        tableLayoutPanel1.Controls.Add(labelMessageLabel, 1, 0);
                        tableLayoutPanel1.SetColumnSpan(labelMessageLabel, 4);
                        labelMessageLabel.Visible = true;
                        labelMessageLabel.Text = value;
                    }
                }
                else
                {
                    tableLayoutPanel1.Controls.Add(labelMessageLabel, 1, 0);
                    tableLayoutPanel1.SetColumnSpan(labelMessageLabel, 4);
                    labelMessageLabel.Visible = true;
                    labelMessageLabel.Text = value;
                }
            }
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

        public bool PrivateKeyFieldEnabled
        {
            set
            {
                comboBoxPrivateKey.Enabled = value;
                choosePkButton.Enabled = value;
            }
        }

        public string PasswordLabel
        {
            set { labelPassword.Text = value; }
        }

        public string UsernameLabel
        {
            set { labelUsername.Text = value; }
        }

        public void PopulatePrivateKeys(List<string> keys)
        {
            comboBoxPrivateKey.DataSource = null;
            comboBoxPrivateKey.DataSource = keys;
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
        }

        public Image DiskIcon
        {
            set { diskPictureBox.Image = value; }
        }

        public bool AnonymousEnabled
        {
            set { checkBoxAnonymous.Enabled = value; }
        }

        public string SelectedPrivateKey
        {
            get { return comboBoxPrivateKey.Text; }
            set { comboBoxPrivateKey.Text = value; }
        }

        public event VoidHandler ChangedUsernameEvent;
        public event VoidHandler ChangedPasswordEvent;
        public event VoidHandler ChangedSavePasswordCheckboxEvent;
        public event VoidHandler ChangedAnonymousCheckboxEvent;
        public event VoidHandler ChangedPkCheckboxEvent;
        public event EventHandler<PrivateKeyArgs> ChangedPrivateKey;
        public event ValidateInputHandler ValidateInput;
        public event VoidHandler OpenPrivateKeyBrowserEvent;

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

        private void choosePkButton_Click(object sender, EventArgs e)
        {
            OpenPrivateKeyBrowserEvent();
        }
    }
}