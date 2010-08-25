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

namespace Ch.Cyberduck.Ui.Controller
{
    public interface ILoginView
    {
        // Properties
        string Title { set; }
        string Message { set; }
        string Username { get; set; }
        string Password { get; set; }

        bool SavePasswordChecked { get; set; }
        bool SavePasswordEnabled { set; }
        bool UsernameEnabled { set; }
        bool PasswordEnabled { set; }
        bool AnonymousChecked { get; set; }
        bool PkCheckboxChecked { get; set; }
        bool PkCheckboxEnabled { set; }

        string PasswordLabel { set; }
        string UsernameLabel { set; }

        // Delegates
        event EventHandler ChangedUsernameEvent;
        event EventHandler ChangedPasswordEvent;
        event EventHandler ChangedSavePasswordCheckboxEvent;
        event EventHandler ChangedAnonymousCheckboxEvent;
        event EventHandler ChangedPkCheckboxEvent;
    }
}