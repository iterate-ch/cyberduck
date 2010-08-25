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
using ch.cyberduck.ui.controller;
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

            protocol.ICImageList = IconCache.Instance.GetProtocolIcons();

            toggleOptionsLabel.Text = "        " + Locale.localizedString("More Options");
            toggleOptionsLabel.ImageIndex = (_expanded ? 1 : 4);

            SetMinMaxSize(Height);
            ConfigureToggleOptions();
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
        }

        public string UsernamePlaceholder
        {
            set { ; }
        }

        public string DownloadFolder
        {
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
            get { return string.Empty; }
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
            get { return pkLabel.Text; }
            set { pkLabel.Text = value; }
        }

        public string UsernameLabel
        {
            set { labelUsername.Text = value; }
        }

        public bool HostFieldEnabled
        {
            set { textBoxServer.Enabled = value; }
            get { return textBoxServer.Enabled; }
        }

        public bool PortFieldEnabled
        {
            set { textBoxPort.Enabled = value; }
        }

        public bool PublicKeyFieldEnabled
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
            ;
        }

        public void PopulateConnectModes(List<string> connectModes)
        {
            comboBoxConnectMode.DataSource = connectModes;
        }

        public void PopulateTransferModes(List<string> transferModes)
        {
            ;
        }

        public void ShowDownloadFolderBrowser(string path)
        {
            ;
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

        private void textBoxPort_TextChanged(object sender, EventArgs e)
        {
            ChangedPortEvent();
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

        public override string[] BundleNames
        {
            get { return new[]{"Connection"}; }
        }
    }
}