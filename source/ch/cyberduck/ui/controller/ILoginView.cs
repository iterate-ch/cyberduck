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
// yves@langisch.ch
// 
using System;
using System.Drawing;
using ch.cyberduck.ui.controller;

namespace Ch.Cyberduck.Ui.Controller
{
    public interface ILoginView : IView
    {
        // Properties
        string Title { set; }
        string Message { set; }
        string Username { get; set; }
        string Password { get; set; }        
        Image DiskIcon { set; }

        bool SavePasswordState { get; set; }
        bool SavePasswordEnabled { set; }
        bool UsernameEnabled { set; }
        bool PasswordEnabled { set; }
        bool AnonymousState { get; set; }
        bool AnonymousEnabled { set; }
        bool PkCheckboxState { get; set; }
        bool PkCheckboxEnabled { set; }
        string PkLabel { get; set; }

        string PasswordLabel { set; }
        string UsernameLabel { set; }

        void ShowPrivateKeyBrowser(string path);

        #region Events

        event VoidHandler ChangedUsernameEvent;
        event VoidHandler ChangedPasswordEvent;
        event VoidHandler ChangedSavePasswordCheckboxEvent;
        event VoidHandler ChangedAnonymousCheckboxEvent;
        event VoidHandler ChangedPkCheckboxEvent;
        event EventHandler<PrivateKeyArgs> ChangedPrivateKey;
        event ValidateInputHandler ValidateInput;

        #endregion
    }
}