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
using System.Windows.Forms;
using ch.cyberduck.core;
using ch.cyberduck.core.ftp;
using Ch.Cyberduck.Ui.Controller;
using Ch.Cyberduck.Ui.Winforms.Controls;

namespace Ch.Cyberduck.Ui.Winforms
{
    public partial class ConnectionForm : BaseForm, IConnectionView
    {
        private bool _expanded = true;

        public ConnectionForm()
        {
            InitializeComponent();

            //focus nickname
            Load += (sender, args) => textBoxServer.Focus();

            protocol.ICImageList = ProtocolIconsImageList();

            toggleOptionsLabel.Text = "        " + LocaleFactory.localizedString("More Options", "Bookmark");
            toggleOptionsLabel.ImageIndex = (_expanded ? 1 : 4);

            openFileDialog.Title = LocaleFactory.localizedString("Select the private key in PEM or PuTTY format",
                "Credentials");

            openFileDialog.Filter = "Private Key Files (*.pem;*.crt;*.ppk)|*.pem;*.crt;*.ppk|All Files (*.*)|*.*";
            openFileDialog.FilterIndex = 1;

            SetMinMaxSize(Height);
            ConfigureToggleOptions();

            numericUpDownPort.GotFocus += delegate { numericUpDownPort.Select(0, numericUpDownPort.Text.Length); };
        }

        public override string[] BundleNames
        {
            get { return new[] {"Connection", "Keychain", "Localizable"}; }
        }

        public Image Favicon
        {
            set { ; }
        }

        public bool ConnectModeFieldEnabled
        {
            set { comboBoxConnectMode.Enabled = value; }
        }

        public bool EncodingFieldEnabled
        {
            set { comboBoxEncoding.Enabled = value; }
        }

        public bool WebUrlFieldEnabled
        {
            set { ; }
        }

        public bool TimezoneFieldEnabled
        {
            set { ; }
        }

        public bool AlertIconEnabled
        {
            set { alertIcon.Visible = value; }
        }

        public bool OptionsVisible
        {
            get { return _expanded; }
            set
            {
                if (_expanded != value)
                {
                    _expanded = value;

                    if (_expanded)
                    {
                        SetMinMaxSize(Height + optionsPanel.Height);
                        Height += optionsPanel.Height;
                    }
                    else
                    {
                        SetMinMaxSize(Height - optionsPanel.Height);
                        Height -= optionsPanel.Height;
                    }
                    optionsPanel.Visible = _expanded;
                    toggleOptionsLabel.ImageIndex = (_expanded ? 1 : 4);
                }
            }
        }

        public string Nickname
        {
            get { return string.Empty; }
            set { ; }
        }

        public string URL
        {
            set { linkLabelURL.Text = value; }
            get { return linkLabelURL.Text; }
        }

        public string DownloadFolder
        {
            set { ; }
        }

        public FTPConnectMode SelectedConnectMode
        {
            get { return (FTPConnectMode) comboBoxConnectMode.SelectedValue; }
            set { comboBoxConnectMode.SelectedValue = value; }
        }

        public string SelectedEncoding
        {
            get { return comboBoxEncoding.Text; }
            set { comboBoxEncoding.Text = value; }
        }

        public Host.TransferType SelectedTransferMode
        {
            get { return null; }
            set { ; }
        }

        public string SelectedDownloadFolder
        {
            get { return string.Empty; }
        }

        public string WebURL
        {
            get { return string.Empty; }
            set { ; }
        }

        public string WebUrlButtonToolTip
        {
            set { ; }
        }

        public string Notes
        {
            get { return string.Empty; }
            set { ; }
        }

        public string SelectedTimezone
        {
            get { return string.Empty; }
            set { ; }
        }

        public string WindowTitle
        {
            set { ; }
        }

        public bool PkCheckboxState
        {
            get { return checkBoxPKA.Checked; }
            set { checkBoxPKA.Checked = value; }
        }

        public string PkLabel
        {
            set
            {
                pkLabel.Text = value;
                pkLabel.ForeColor = checkBoxPKA.Checked ? Color.FromKnownColor(KnownColor.ControlText) : Color.Gray;
            }
            get { return pkLabel.Text; }
        }

        public string UsernameLabel
        {
            set { labelUsername.Text = value; }
        }

        public string PasswordLabel
        {
            set { labelPassword.Text = value; }
        }

        public bool HostFieldEnabled
        {
            set { textBoxServer.Enabled = value; }
            get { return textBoxServer.Enabled; }
        }

        public bool PortFieldEnabled
        {
            set { numericUpDownPort.Enabled = value; }
        }

        public bool PkCheckboxEnabled
        {
            set { checkBoxPKA.Enabled = value; }
        }

        public Protocol SelectedProtocol
        {
            get { return (Protocol) protocol.SelectedValue; }
            set { protocol.SelectedValue = value; }
        }

        public string Hostname
        {
            get { return textBoxServer.Text; }
            set { textBoxServer.Text = value; }
        }

        public string Port
        {
            get { return numericUpDownPort.Text; }
            set { numericUpDownPort.Text = value; }
        }

        public string Username
        {
            get { return textBoxUsername.Text; }
            set { textBoxUsername.Text = value; }
        }

        public bool UsernameEnabled
        {
            set { textBoxUsername.Enabled = value; }
        }

        public string Password
        {
            get { return textBoxPassword.Text; }
            set { textBoxPassword.Text = value; }
        }

        public bool PasswordEnabled
        {
            set { textBoxPassword.Enabled = value; }
        }

        public string Path
        {
            get { return textBoxPath.Text; }
            set { textBoxPath.Text = value; }
        }

        public bool PathEnabled
        {
            get { return textBoxPath.Enabled; }
            set { textBoxPath.Enabled = value; }
        }

        public bool SavePasswordChecked
        {
            get { return savePasswordCheckBox.Checked; }
            set { savePasswordCheckBox.Checked = value; }
        }

        public bool SavePasswordEnabled
        {
            set { savePasswordCheckBox.Enabled = value; }
        }

        public bool AnonymousChecked
        {
            get { return anonymousCheckBox.Checked; }
            set { anonymousCheckBox.Checked = value; }
        }

        public bool AnonymousEnabled
        {
            get { return anonymousCheckBox.Enabled; }
            set { anonymousCheckBox.Enabled = value; }
        }

        public void PopulateProtocols(List<KeyValueIconTriple<Protocol, string>> protocols)
        {
            protocol.DataSource = protocols;
            protocol.ValueMember = "Key";
            protocol.DisplayMember = "Value";
            protocol.IconMember = "IconKey";
        }

        public void PopulateEncodings(List<string> encodings)
        {
            comboBoxEncoding.DataSource = encodings;
        }

        public void PopulateTimezones(List<string> timezones)
        {
            //
        }

        public void PopulateConnectModes(List<KeyValuePair<string, FTPConnectMode>> modes)
        {
            comboBoxConnectMode.DataSource = null;
            comboBoxConnectMode.DataSource = modes;
            comboBoxConnectMode.ValueMember = "Value";
            comboBoxConnectMode.DisplayMember = "Key";
        }

        public void PopulateTransferModes(List<KeyValuePair<string, Host.TransferType>> modes)
        {
            //
        }

        public void ShowDownloadFolderBrowser(string path)
        {
            //
        }

        public void ShowPrivateKeyBrowser(string path)
        {
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

        public event VoidHandler ChangedEncodingEvent = delegate { };
        public event VoidHandler ChangedNicknameEvent = delegate { };
        public event VoidHandler ChangedTimezoneEvent = delegate { };
        public event VoidHandler ChangedConnectModeEvent = delegate { };
        public event VoidHandler ChangedTransferEvent = delegate { };
        public event VoidHandler ChangedWebURLEvent = delegate { };
        public event VoidHandler ChangedCommentEvent = delegate { };
        public event VoidHandler ChangedBrowserDownloadPathEvent = delegate { };
        public event VoidHandler ChangedProtocolEvent = delegate { };
        public event VoidHandler ChangedPortEvent = delegate { };
        public event VoidHandler ChangedServerEvent = delegate { };
        public event VoidHandler ChangedUsernameEvent = delegate { };
        public event VoidHandler ChangedPathEvent = delegate { };
        public event VoidHandler ChangedAnonymousCheckboxEvent = delegate { };
        public event VoidHandler ChangedSavePasswordCheckboxEvent = delegate { };
        public event VoidHandler ChangedPublicKeyCheckboxEvent = delegate { };
        public event VoidHandler OpenUrl = delegate { };
        public event VoidHandler OpenWebUrl = delegate { };
        public event VoidHandler OpenDownloadFolderBrowserEvent = delegate { };
        public event VoidHandler OpenDownloadFolderEvent = delegate { };
        public event VoidHandler LaunchNetworkAssistantEvent = delegate { };
        public event VoidHandler ToggleOptions = delegate { };

        private void ConfigureToggleOptions()
        {
            toggleOptionsLabel.Click += (sender, args) => ToggleOptions();
            toggleOptionsLabel.MouseDown += delegate { toggleOptionsLabel.ImageIndex = (_expanded ? 2 : 5); };
            toggleOptionsLabel.MouseEnter += delegate { toggleOptionsLabel.ImageIndex = (_expanded ? 1 : 4); };
            toggleOptionsLabel.MouseLeave += delegate { toggleOptionsLabel.ImageIndex = (_expanded ? 0 : 3); };
            toggleOptionsLabel.MouseUp += delegate { toggleOptionsLabel.ImageIndex = (_expanded ? 1 : 4); };
        }

        private void SetMinMaxSize(int height)
        {
            MinimumSize = new Size(450, height);
            MaximumSize = new Size(800, height);
        }

        private void protocol_SelectionChangeCommitted(object sender, EventArgs e)
        {
            ChangedProtocolEvent();
        }

        private void textBoxServer_TextChanged(object sender, EventArgs e)
        {
            ChangedServerEvent();
        }

        private void linkLabelURL_LinkClicked(object sender, LinkLabelLinkClickedEventArgs e)
        {
            OpenUrl();
        }

        private void textBoxUsername_TextChanged(object sender, EventArgs e)
        {
            ChangedUsernameEvent();
        }

        private void anonymousCheckBox_CheckedChanged(object sender, EventArgs e)
        {
            ChangedAnonymousCheckboxEvent();
        }

        private void savePasswordCheckBox_CheckedChanged(object sender, EventArgs e)
        {
            ChangedSavePasswordCheckboxEvent();
        }

        private void textBoxPath_TextChanged(object sender, EventArgs e)
        {
            ChangedPathEvent();
        }

        private void comboBoxConnectMode_SelectionChangeCommitted(object sender, EventArgs e)
        {
            ChangedConnectModeEvent();
        }

        private void comboBoxEncoding_SelectionChangeCommitted(object sender, EventArgs e)
        {
            ChangedEncodingEvent();
        }

        private void checkBoxPKA_CheckedChanged(object sender, EventArgs e)
        {
            ChangedPublicKeyCheckboxEvent();
        }

        private void numericUpDownPort_TextChanged(object sender, EventArgs e)
        {
            ChangedPortEvent();
        }
    }
}