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
using System.Windows.Forms;
using Ch.Cyberduck.Ui.Winforms.Taskdialog;

namespace Ch.Cyberduck.Ui.Controller
{
    //todo wird glaube ich nirgends mehr gebraucht -> entfernen
    internal interface IMessageBoxView
    {
        DialogResult MessageBox(IView Owner,
                                string Title,
                                string MainInstruction,
                                string Content,
                                eTaskDialogButtons Buttons,
                                eSysIcons MainIcon);
    }
}