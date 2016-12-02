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

using System.Drawing;

namespace Ch.Cyberduck.Ui.Controller
{
    interface IPasswordView : IView
    {
        Image PwdIcon { set; }
        bool SavePasswordState { get; set; }
        bool SavePasswordEnabled { get; set; }

        string Title { set; }
        string Reason { set; }
        string PasswordLabel { set; }

        string Password { get; set; }
    }
}