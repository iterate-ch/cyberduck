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
using System.Windows.Forms;
using static Windows.Win32.PInvoke;

namespace Ch.Cyberduck.Ui.Winforms.Controls
{
    /// <summary>
    /// Menustrip component with Click Through support
    /// </summary>
    /// <see cref="http://blogs.msdn.com/b/rickbrew/archive/2006/01/09/511003.aspx"/>
    public class ClickThroughMenuStrip : MenuStrip
    {
        protected override void WndProc(ref Message m)
        {
            base.WndProc(ref m);            
            if (m.Msg == WM_MOUSEACTIVATE &&
                m.Result == (IntPtr) MA_ACTIVATEANDEAT)
            {
                m.Result = (IntPtr) MA_ACTIVATE;
            }
        }
    }
}
