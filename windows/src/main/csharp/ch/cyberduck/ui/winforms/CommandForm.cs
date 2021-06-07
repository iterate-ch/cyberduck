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
using Ch.Cyberduck.Ui.Core.Resources;
using System;
using System.Drawing;
using Ch.Cyberduck.Core.Microsoft.Windows.Sdk;
using static Ch.Cyberduck.Core.Microsoft.Windows.Sdk.PInvoke;
using static Ch.Cyberduck.Ui.Microsoft.Windows.Sdk.Constants;

namespace Ch.Cyberduck.Ui.Winforms
{
    public partial class CommandForm : BaseForm, ICommandView
    {
        private static readonly Font FixedFont = new Font("Courier New", 8, FontStyle.Regular);

        public CommandForm()
        {
            InitializeComponent();

            pictureBox.Image =
                IconCache.GetAppImage(
                    Environment.ExpandEnvironmentVariables(@"%windir%\system32\cmd.exe"), IconCache.IconSize.Large);
        }

        public event VoidHandler SendEvent = delegate { };

        public override string[] BundleNames
        {
            get { return new[] { "Command", "Localizable" }; }
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
            SendMessage(transcriptBox.Handle, WM_VSCROLL, SB_BOTTOM, default);
        }

        public void StartActivityAnimation()
        {
            animation.Visible = true;
        }

        public void StopActivityAnimation()
        {
            animation.Visible = false;
        }

        private void button1_Click(object sender, EventArgs e)
        {
            SendEvent();
        }
    }
}
