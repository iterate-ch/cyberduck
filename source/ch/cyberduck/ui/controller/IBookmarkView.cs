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
using System.Collections.Generic;
using System.Drawing;
using ch.cyberduck.core;
using Ch.Cyberduck.Core;
using ch.cyberduck.ui.controller;
using Ch.Cyberduck.Ui.Winforms.Controls;

namespace Ch.Cyberduck.Ui.Controller
{
    public interface IBookmarkView : IView
    {
        // Properties
        Image Favicon { set; }        

        bool ConnectModeFieldEnabled { set; }
        bool EncodingFieldEnabled { set; }
        bool WebUrlFieldEnabled { set; }
        bool TimezoneFieldEnabled { set; }
        bool AlertIconEnabled { set; }

        event VoidHandler ToggleOptions;
        bool OptionsVisible { get; set; }

        string Nickname { get; set; }
        string URL { set; }
        string UsernamePlaceholder { set; }
        string DownloadFolder { set; }
        string SelectedConnectMode { get; set; }
        string SelectedEncoding { get; set; }
        string SelectedTransferMode { get; set; }
        string SelectedDownloadFolder { get; }
        string WebURL { get; set; }
        string WebUrlButtonToolTip { set; }
        string Notes { get; set; }
        string SelectedTimezone { get; set; }
        string WindowTitle { set; }
        // Public Key Checkbox
        bool PkCheckboxState { get; set; }
        string PkLabel { get;  set; }
        string UsernameLabel { set; }
        bool HostFieldEnabled { get;  set; }
        bool PortFieldEnabled { set; }
        bool PublicKeyFieldEnabled { set; }
        Protocol SelectedProtocol { get; set; }
        string Hostname { get; set; }
        string Port { get; set; }
        string Username { get; set; }
        bool UsernameEnabled { set; }
        string Password { get; set; }
        bool PasswordEnabled { set; }
        string Path { get; set; }
        bool PathEnabled { get; set; }
        bool SavePasswordChecked { get; set; }
        bool SavePasswordEnabled { set; }
        bool AnonymousChecked { get; set; }

        void PopulateProtocols(List<KeyValueIconTriple<Protocol, string>> protocols);
        void PopulateEncodings(List<string> encodings);
        void PopulateTimezones(List<string> timezones);
        void PopulateConnectModes(List<string> connectModes);
        void PopulateTransferModes(List<string> transferModes);

        void ShowDownloadFolderBrowser(string path);

        // Delegates
        event VoidHandler ChangedEncodingEvent;
        event VoidHandler ChangedNicknameEvent;
        event VoidHandler ChangedTimezoneEvent;
        event VoidHandler ChangedConnectModeEvent;
        event VoidHandler ChangedTransferEvent;
        event VoidHandler ChangedWebURLEvent;
        event VoidHandler ChangedCommentEvent;
        event VoidHandler ChangedBrowserDownloadPathEvent;
        event VoidHandler ChangedProtocolEvent;
        event VoidHandler ChangedPortEvent;
        event VoidHandler ChangedServerEvent;
        event VoidHandler ChangedUsernameEvent;
        event VoidHandler ChangedPathEvent;
        event VoidHandler ChangedAnonymousCheckboxEvent;
        event VoidHandler ChangedPublicKeyCheckboxEvent;
        event VoidHandler ChangedSavePasswordCheckboxEvent;
        event VoidHandler OpenUrl;
        event VoidHandler OpenWebUrl;
        event VoidHandler OpenDownloadFolderBrowserEvent;
        event VoidHandler OpenDownloadFolderEvent;

        //todo
        event VoidHandler LaunchNetworkAssistantEvent;
    }
}