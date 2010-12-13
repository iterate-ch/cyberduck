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
using System.Collections.Generic;
using System.Drawing;
using System.Windows.Forms;
using ch.cyberduck.core;
using ch.cyberduck.core.i18n;
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

            protocol.ICImageList = IconCache.Instance.GetProtocolIcons();

            toggleOptionsLabel.Text = "        " + Locale.localizedString("More Options", "Bookmark");
            toggleOptionsLabel.ImageIndex = (_expanded ? 1 : 4);

            openFileDialog.Title = Locale.localizedString("Select the private key in PEM or PuTTY format", "Credentials");

            //todo localization
            openFileDialog.Filter = "Private Key Files (*.pem;*.crt;*.ppk)|*.pem;*.crt;*.ppk|All Files (*.*)|*.*";
            openFileDialog.FilterIndex = 1;

            SetMinMaxSize(Height);
            ConfigureToggleOptions();
        }

        public bool HostFieldEnabled
        {
            set { textBoxServer.Enabled = value; }
        }

        public override string[] BundleNames
        {
            get { return new[] {"Bookmark"}; }
        }

        public bool SavePasswordEnabled
        {
            set { ; }
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

        public void PopulateConnectModes(List<string> connectModes)
        {
            comboBoxConnectMode.DataSource = connectModes;
        }

        public void PopulateEncodings(List<string> encodings)
        {
            comboBoxEncoding.DataSource = encodings;
        }

        public void PopulateTransferModes(List<string> transferModes)
        {
            comboBoxTransferFiles.DataSource = transferModes;
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
                ChangedPrivateKey(this, new PrivateKeyArgs(openFileDialog.FileName));
            }
            else
            {
                ChangedPrivateKey(this, new PrivateKeyArgs(null));
            }
        }

        public void PopulateTimezones(List<string> timezones)
        {
            comboBoxTimezone.DataSource = timezones;
        }

        public string UsernameLabel
        {
            set { labelUsername.Text = value; }
        }

        public string PasswordLabel
        {
            set { }
        }

        bool IBookmarkView.HostFieldEnabled { get; set; }

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
        public event VoidHandler ChangedTimezoneEvent = delegate { };
        public event VoidHandler ChangedConnectModeEvent = delegate { };
        public event VoidHandler ChangedTransferEvent = delegate { };
        public event VoidHandler ChangedPublicKeyCheckboxEvent = delegate { };
        public event VoidHandler ChangedWebURLEvent = delegate { };
        public event VoidHandler ChangedCommentEvent = delegate { };
        public event VoidHandler ChangedBrowserDownloadPathEvent = delegate { };
        public event VoidHandler OpenWebUrl = delegate { };
        public event VoidHandler OpenDownloadFolderBrowserEvent = delegate { };
        public event VoidHandler OpenDownloadFolderEvent = delegate { };
        public event VoidHandler LaunchNetworkAssistantEvent = delegate { };
        public event VoidHandler OpenUrl = delegate { };
        public event VoidHandler ChangedSavePasswordCheckboxEvent = delegate { };
        public event EventHandler<PrivateKeyArgs> ChangedPrivateKey = delegate { };

        public bool PortFieldEnabled
        {
            set { textBoxPort.Enabled = value; }
        }

        public bool ConnectModeFieldEnabled
        {
            set { comboBoxConnectMode.Enabled = value; }
        }

        public bool EncodingFieldEnabled
        {
            set { comboBoxEncoding.Enabled = value; }
        }

        public bool PkCheckboxEnabled
        {
            set { checkBoxPKA.Enabled = value; }
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
            set { textBoxNickname.Text = value; }
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
            get { return textBoxPort.Text; }
            set { textBoxPort.Text = value; }
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
            get { return string.Empty; }
            set { ; }
        }

        public bool PasswordEnabled
        {
            set { ; }
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
            get { return true; }
            set { ; }
        }

        public string SelectedConnectMode
        {
            get { return comboBoxConnectMode.Text; }
            set { comboBoxConnectMode.Text = value; }
        }

        public string SelectedEncoding
        {
            get { return comboBoxEncoding.Text; }
            set { comboBoxEncoding.Text = value; }
        }

        public string SelectedTransferMode
        {
            get { return comboBoxTransferFiles.Text; }
            set { comboBoxTransferFiles.Text = value; }
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

        public string WindowTitle
        {
            set { Text = value; }
        }

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

        private void textBoxPort_TextChanged(object sender, EventArgs e)
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

        private void checkBoxPKA_CheckedChanged(object sender, EventArgs e)
        {
            ChangedPublicKeyCheckboxEvent();
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
    }
}