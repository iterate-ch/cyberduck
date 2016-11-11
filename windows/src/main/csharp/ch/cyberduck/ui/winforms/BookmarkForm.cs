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
    public partial class BookmarkForm : BaseForm, IBookmarkView
    {
        private bool _expanded = true;

        public BookmarkForm()
        {
            InitializeComponent();

            //focus nickname
            Load += (sender, args) => textBoxNickname.Focus();

            protocol.ICImageList = ProtocolIconsImageList();

            toggleOptionsLabel.Text = "        " + LocaleFactory.localizedString("More Options", "Bookmark");
            toggleOptionsLabel.ImageIndex = (_expanded ? 1 : 4);

            openFileDialog.Title = LocaleFactory.localizedString("Select the private key in PEM or PuTTY format",
                "Credentials");

            openFileDialog.Filter = "Private Key Files (*.pem;*.crt;*.ppk;*)|*.pem;*.crt;*.ppk|All Files (*.*)|*.*";
            openFileDialog.FilterIndex = 1;

            SetMinMaxSize(Height);
            ConfigureToggleOptions();


            numericUpDownPort.GotFocus += delegate { numericUpDownPort.Select(0, numericUpDownPort.Text.Length); };
        }

        public override string[] BundleNames
        {
            get { return new[] {"Bookmark"}; }
        }

        public bool SavePasswordEnabled
        {
            set
            {
                ;
            }
        }

        public string PasswordLabel
        {
            set { }
        }

        public string Password
        {
            get { return string.Empty; }
            set
            {
                ;
            }
        }

        public bool PasswordEnabled
        {
            set
            {
                ;
            }
        }

        public bool SavePasswordChecked
        {
            get { return true; }
            set
            {
                ;
            }
        }

        public bool HostFieldEnabled
        {
            get { return textBoxServer.Enabled; }
            set { textBoxServer.Enabled = value; }
        }

        public bool AnonymousChecked
        {
            get { return checkBoxAnonymous.Checked; }
            set { checkBoxAnonymous.Checked = value; }
        }

        public bool AnonymousEnabled
        {
            get { return checkBoxAnonymous.Enabled; }
            set { checkBoxAnonymous.Enabled = value; }
        }

        public void PopulateProtocols(List<KeyValueIconTriple<Protocol, string>> protocols)
        {
            protocol.DataSource = protocols;
            protocol.ValueMember = "Key";
            protocol.DisplayMember = "Value";
            protocol.IconMember = "IconKey";
        }

        public void PopulatePrivateKeys(List<string> keys)
        {
            comboBoxPrivateKey.DataSource = null;
            comboBoxPrivateKey.DataSource = keys;
        }

        public void PopulateClientCertificates(List<string> certificates)
        {
            comboBoxClientCertificate.DataSource = certificates;
        }

        public void PopulateConnectModes(List<KeyValuePair<string, FTPConnectMode>> modes)
        {
            comboBoxConnectMode.DataSource = null;
            comboBoxConnectMode.DataSource = modes;
            comboBoxConnectMode.DisplayMember = "Key";
            comboBoxConnectMode.ValueMember = "Value";
        }

        public void PopulateEncodings(List<string> encodings)
        {
            comboBoxEncoding.DataSource = encodings;
        }

        public void PopulateTransferModes(List<KeyValuePair<string, Host.TransferType>> modes)
        {
            comboBoxTransferFiles.DataSource = null;
            comboBoxTransferFiles.DataSource = modes;
            comboBoxTransferFiles.DisplayMember = "Key";
            comboBoxTransferFiles.ValueMember = "Value";
        }

        public void ShowDownloadFolderBrowser(string path)
        {
            folderBrowserDialog.SelectedPath = path;
            if (DialogResult.OK == folderBrowserDialog.ShowDialog())
            {
                ChangedBrowserDownloadPathEvent();
            }
        }

        public void ShowPrivateKeyBrowser(string path)
        {
            openFileDialog.InitialDirectory = path;
            openFileDialog.FileName = String.Empty;
            if (DialogResult.OK == openFileDialog.ShowDialog())
            {
                ChangedPrivateKeyEvent(this, new PrivateKeyArgs(openFileDialog.FileName));
            }
        }

        public void PopulateTimezones(List<string> timezones)
        {
            comboBoxTimezone.DataSource = timezones;
        }

        public bool ClientCertificateFieldEnabled
        {
            set { comboBoxClientCertificate.Enabled = value; }
        }

        public string UsernameLabel
        {
            set { labelUsername.Text = value; }
        }

        public Image Favicon
        {
            set { buttonWebURL.Image = value; }
        }

        public event VoidHandler ChangedProtocolEvent = delegate { };
        public event VoidHandler ChangedPortEvent = delegate { };
        public event VoidHandler ChangedEncodingEvent = delegate { };
        public event VoidHandler ChangedServerEvent = delegate { };
        public event VoidHandler ChangedUsernameEvent = delegate { };
        public event VoidHandler ChangedNicknameEvent = delegate { };
        public event VoidHandler ChangedPathEvent = delegate { };
        public event VoidHandler ChangedAnonymousCheckboxEvent = delegate { };
        public event VoidHandler ChangedClientCertificateEvent = delegate { };
        public event VoidHandler ChangedTimezoneEvent = delegate { };
        public event VoidHandler ChangedConnectModeEvent = delegate { };
        public event VoidHandler ChangedTransferEvent = delegate { };
        public event VoidHandler ChangedWebURLEvent = delegate { };
        public event VoidHandler ChangedCommentEvent = delegate { };
        public event VoidHandler ChangedBrowserDownloadPathEvent = delegate { };
        public event VoidHandler OpenWebUrl = delegate { };
        public event VoidHandler OpenPrivateKeyBrowserEvent = delegate { };
        public event VoidHandler OpenDownloadFolderBrowserEvent = delegate { };
        public event VoidHandler OpenDownloadFolderEvent = delegate { };
        public event VoidHandler LaunchNetworkAssistantEvent = delegate { };
        public event VoidHandler OpenUrl = delegate { };
        public event EventHandler<PrivateKeyArgs> ChangedPrivateKeyEvent = delegate { };

        public bool PortFieldEnabled
        {
            set { numericUpDownPort.Enabled = value; }
        }

        public bool ConnectModeFieldEnabled
        {
            set { comboBoxConnectMode.Enabled = value; }
        }

        public bool EncodingFieldEnabled
        {
            set { comboBoxEncoding.Enabled = value; }
        }

        public string SelectedClientCertificate
        {
            get { return comboBoxClientCertificate.Text; }
            set { comboBoxClientCertificate.Text = value; }
        }

        public bool PrivateKeyFieldEnabled
        {
            set
            {
                comboBoxPrivateKey.Enabled = value;
                choosePkButton.Enabled = value;
            }
        }

        public bool WebUrlFieldEnabled
        {
            set { textBoxWebUrl.Enabled = value; }
        }

        public string WebUrlButtonToolTip
        {
            set { toolTip.SetToolTip(buttonWebURL, value); }
        }

        public bool TimezoneFieldEnabled
        {
            set { comboBoxTimezone.Enabled = value; }
        }

        public bool AlertIconEnabled
        {
            set { alertIcon.Visible = value; }
        }

        public event VoidHandler ToggleOptions;

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

        public string DownloadFolder
        {
            set { linkLabelDownloadFolder.Text = value; }
        }

        public Protocol SelectedProtocol
        {
            get { return (Protocol) protocol.SelectedValue; }
            set { protocol.SelectedValue = value; }
        }

        public string Nickname
        {
            get { return textBoxNickname.Text; }
            set
            {
                textBoxNickname.TextChanged -= textBoxNickname_TextChanged;
                textBoxNickname.Text = value;
                textBoxNickname.TextChanged += textBoxNickname_TextChanged;
            }
        }

        public string URL
        {
            set { linkLabelURL.Text = value; }
            get { return linkLabelURL.Text; }
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
            get { return (Host.TransferType) comboBoxTransferFiles.SelectedValue; }
            set { comboBoxTransferFiles.SelectedValue = value; }
        }

        public string SelectedDownloadFolder
        {
            get { return folderBrowserDialog.SelectedPath; }
        }

        public string WebURL
        {
            get { return textBoxWebUrl.Text; }
            set { textBoxWebUrl.Text = value; }
        }

        public string Notes
        {
            get { return richTextBoxNotes.Text; }
            set { richTextBoxNotes.Text = value; }
        }

        public string SelectedTimezone
        {
            get { return comboBoxTimezone.Text; }
            set { comboBoxTimezone.Text = value; }
        }

        public string SelectedPrivateKey
        {
            get { return comboBoxPrivateKey.Text; }
            set { comboBoxPrivateKey.Text = value; }
        }

        public string WindowTitle
        {
            set { Text = value; }
        }

        public event VoidHandler ChangedSavePasswordCheckboxEvent = delegate { };

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

        private void textBoxNickname_TextChanged(object sender, EventArgs e)
        {
            ChangedNicknameEvent();
        }

        private void textBoxServer_TextChanged(object sender, EventArgs e)
        {
            ChangedServerEvent();
        }

        private void numericUpDownPort_TextChanged(object sender, EventArgs e)
        {
            ChangedPortEvent();
        }

        private void textBoxUsername_TextChanged(object sender, EventArgs e)
        {
            ChangedUsernameEvent();
        }

        private void textBoxPath_TextChanged(object sender, EventArgs e)
        {
            //prevent cursor from being positioned at the beginning after trimming trailing spaces
            //see Host.setDefaultPath
            int sel = textBoxPath.SelectionStart;
            String trimmed = textBoxPath.Text.Trim();
            if (!trimmed.Equals(textBoxPath))
            {
                textBoxPath.Text = trimmed;
                textBoxPath.SelectionStart = sel;
            }
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

        private void comboBoxTransferFiles_SelectionChangeCommitted(object sender, EventArgs e)
        {
            ChangedTransferEvent();
        }

        private void textBoxWebUrl_TextChanged(object sender, EventArgs e)
        {
            ChangedWebURLEvent();
        }

        private void richTextBoxNotes_TextChanged(object sender, EventArgs e)
        {
            ChangedCommentEvent();
        }

        private void downloadFolderButton_Click(object sender, EventArgs e)
        {
            OpenDownloadFolderBrowserEvent();
        }

        private void comboBoxTimezone_SelectionChangeCommitted(object sender, EventArgs e)
        {
            ChangedTimezoneEvent();
        }

        private void linkLabelDownloadFolder_LinkClicked(object sender, LinkLabelLinkClickedEventArgs e)
        {
            OpenDownloadFolderEvent();
        }

        private void buttonWebURL_Click(object sender, EventArgs e)
        {
            OpenWebUrl();
        }

        private void linkLabelURL_Click(object sender, EventArgs e)
        {
            OpenUrl();
        }

        private void button1_Click(object sender, EventArgs e)
        {
            Close();
        }

        private void checkBoxAnonymous_CheckedChanged(object sender, EventArgs e)
        {
            ChangedAnonymousCheckboxEvent();
        }

        private void comboBoxPrivateKey_SelectionChangeCommitted(object sender, EventArgs e)
        {
            ChangedPrivateKeyEvent(sender, new PrivateKeyArgs(SelectedPrivateKey));
        }

        private void choosePkButton_Click(object sender, EventArgs e)
        {
            OpenPrivateKeyBrowserEvent();
        }

        private void comboBoxClientCertificate_SelectionChangeCommitted(object sender, EventArgs e)
        {
            ChangedClientCertificateEvent();
        }
    }
}