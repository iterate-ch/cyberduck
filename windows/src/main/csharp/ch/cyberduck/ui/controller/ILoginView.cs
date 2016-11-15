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
        string SelectedPrivateKey { get; set; }
        bool PrivateKeyFieldEnabled { set; }

        string PasswordLabel { set; }
        string UsernameLabel { set; }

        void PopulatePrivateKeys(List<string> keys);
        void ShowPrivateKeyBrowser(string path);

        #region Events

        event VoidHandler ChangedUsernameEvent;
        event VoidHandler ChangedPasswordEvent;
        event VoidHandler ChangedSavePasswordCheckboxEvent;
        event VoidHandler ChangedAnonymousCheckboxEvent;
        event VoidHandler ChangedPkCheckboxEvent;
        event EventHandler<PrivateKeyArgs> ChangedPrivateKey;
        event ValidateInputHandler ValidateInput;
        event VoidHandler OpenPrivateKeyBrowserEvent;

        // event EventHandler<PrivateKeyArgs> ChangedPrivateKeyEvent;

        #endregion
    }
}