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
using System.Drawing;
using System.Runtime.InteropServices;
using System.Windows.Forms;
using Windows.Win32.Foundation;
using static Windows.Win32.CorePInvoke;
using static Windows.Win32.PInvoke;

namespace Ch.Cyberduck.Ui.Winforms.Controls
{
    public class ReadOnlyRichTextBox : RichTextBox
    {
        public ReadOnlyRichTextBox()
        {
            ReadOnly = true;
            //set the BackColor back to White since ReadOnly=true makes the background grey
            BackColor = Color.White;
            SendMessage((HWND)Handle, EM_SETTYPOGRAPHYOPTIONS,
                                      (nuint)TO_ADVANCEDTYPOGRAPHY,
                                      (nint)TO_ADVANCEDTYPOGRAPHY);
        }

        protected override CreateParams CreateParams
        {
            get
            {
                CreateParams p = base.CreateParams;
                if (!LoadLibrary("msftedit.dll").IsInvalid)
                {
                    p.ClassName = "RICHEDIT50W";
                }
                return p;
            }
        }

        protected override void WndProc(ref Message m)
        {
            base.WndProc(ref m);
            HideCaret((HWND)Handle);
        }
    }
}
