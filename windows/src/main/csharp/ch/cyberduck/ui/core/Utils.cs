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

using System.Collections.Generic;
using System.Drawing;
using System.Windows.Forms;
using ch.cyberduck.core;
using ch.cyberduck.core.local;
using Ch.Cyberduck.Core.Resources;
using Ch.Cyberduck.Ui.Controller;
using Ch.Cyberduck.Ui.Winforms.Taskdialog;

namespace Ch.Cyberduck.Ui.Core
{
    public class Utils
    {
        public static DialogResult CommandBox(IWin32Window owner, string title, string mainInstruction, string content,
            string expandedInfo, string help, string verificationText, string commandButtons, bool showCancelButton,
            SysIcons mainIcon, SysIcons footerIcon, DialogResponseHandler handler)
        {
            TaskDialog dialog = new TaskDialog();
            dialog.HelpDelegate = delegate(string url) { BrowserLauncherFactory.get().open(url); };
            DialogResult result = dialog.ShowCommandBox(owner, title, mainInstruction, content, expandedInfo,
                FormatHelp(help), verificationText, commandButtons, showCancelButton, mainIcon, footerIcon);
            handler(dialog.CommandButtonResult, dialog.VerificationChecked);
            return result;
        }

        public static DialogResult CommandBox(string title, string mainInstruction, string content, string expandedInfo,
            string help, string verificationText, string commandButtons, bool showCancelButton, SysIcons mainIcon,
            SysIcons footerIcon, DialogResponseHandler handler)
        {
            return CommandBox(null, title, mainInstruction, content, expandedInfo, help, verificationText,
                commandButtons, showCancelButton, mainIcon, footerIcon, handler);
        }

        public static DialogResult CommandBox(string title, string message, string detail, string commandButtons,
            bool showCancelButton, string verificationText, SysIcons mainIcon, DialogResponseHandler handler)
        {
            return CommandBox(title, message, detail, commandButtons, showCancelButton, verificationText, mainIcon, null,
                handler);
        }

        public static DialogResult CommandBox(string title, string message, string detail, string commandButtons,
            bool showCancelButton, string verificationText, SysIcons mainIcon, string help,
            DialogResponseHandler handler)
        {
            return CommandBox(title, message, detail, null, help, verificationText, commandButtons, showCancelButton,
                mainIcon, SysIcons.Information, handler);
        }

        public static DialogResult MessageBox(IWin32Window owner, string title, string message, string content,
            string expandedInfo, string help, string verificationText, DialogResponseHandler handler)
        {
            TaskDialog dialog = new TaskDialog();
            dialog.HelpDelegate = delegate(string url) { BrowserLauncherFactory.get().open(url); };
            DialogResult result = dialog.MessageBox(owner, title, message, content, expandedInfo, FormatHelp(help),
                verificationText, TaskDialogButtons.OK, SysIcons.Information, SysIcons.Information);
            handler(-1, dialog.VerificationChecked);
            return result;
        }

        private static string FormatHelp(string help)
        {
            if (string.IsNullOrEmpty(help))
            {
                return null;
            }
            return "<A HREF=\"" + help + "\">" + LocaleFactory.localizedString("Help", "Main") + "</A>";
        }

        public static ImageList ToImageList(IDictionary<string, Bitmap> dict)
        {
            ImageList images = new ImageList();
            images.ImageSize = new Size(16, 16);
            images.ColorDepth = ColorDepth.Depth32Bit;
            foreach (var icon in IconCache.Instance.GetProtocolIcons())
            {
                images.Images.Add(icon.Key, icon.Value);
            }
            return images;
        }
    }
}