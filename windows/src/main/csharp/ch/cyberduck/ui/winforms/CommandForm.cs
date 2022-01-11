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

using Ch.Cyberduck.Ui.Controller;
using System;
using System.Drawing;
using Windows.Win32.Foundation;
using static Ch.Cyberduck.ImageHelper;
using static Windows.Win32.CorePInvoke;
using static Windows.Win32.PInvoke;

namespace Ch.Cyberduck.Ui.Winforms
{
    public partial class CommandForm : BaseForm, ICommandView
    {
        private static readonly Font FixedFont = new Font("Courier New", 8, FontStyle.Regular);

        public CommandForm()
        {
            InitializeComponent();

            pictureBox.Image = IconProvider.GetFileIcon(
                Environment.ExpandEnvironmentVariables(@"%windir%\system32\cmd.exe"), false, true, true);
        }

        public override string[] BundleNames
        {
            get { return new[] {"Command", "Localizable"}; }
        }

        public string Command
        {
            get { return commandBox.Text; }
        }

        public void AddTranscriptEntry(string message)
        {
            transcriptBox.SelectionFont = FixedFont;
            transcriptBox.SelectionColor = Color.Black;
            transcriptBox.SelectedText = message + Environment.NewLine;
            transcriptBox.Select(transcriptBox.TextLength, transcriptBox.TextLength);
            SendMessage((HWND)transcriptBox.Handle, WM_VSCROLL, (nuint)SB_BOTTOM, 0);
        }

        public void StartActivityAnimation()
        {
            animation.Visible = true;
        }

        public void StopActivityAnimation()
        {
            animation.Visible = false;
        }

        public event VoidHandler SendEvent = delegate { };

        private void button1_Click(object sender, EventArgs e)
        {
            SendEvent();
        }
    }
}
