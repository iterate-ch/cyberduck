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
using System.Windows.Forms;
using ch.cyberduck.core;
using ch.cyberduck.core.local;
using Ch.Cyberduck.Core.TaskDialog;
using Ch.Cyberduck.Ui.Controller;

namespace Ch.Cyberduck.Ui.Core
{
    public class Utils
    {
        public static TaskDialogResult CommandBox(IWin32Window owner, string title, string mainInstruction,
            string content, string expandedInfo, string help, string verificationText, string commandButtons,
            bool showCancelButton, TaskDialogIcon mainIcon, TaskDialogIcon footerIcon, DialogResponseHandler handler)
        {
            TaskDialogResult result = TaskDialog.Show(
                owner: owner?.Handle ?? IntPtr.Zero,
                title: title,
                mainInstruction: mainInstruction,
                content: content,
                footerText: FormatHelp(help),
                expandedInfo: expandedInfo,
                verificationText: verificationText,
                commandLinks: commandButtons.Split(new char[] {'|'}),
                commonButtons: showCancelButton ? TaskDialogCommonButtons.Cancel : TaskDialogCommonButtons.None,
                mainIcon: mainIcon,
                footerIcon: footerIcon,
                callback: (dialog, args, callbackData) =>
                {
                    switch (args.Notification)
                    {
                        case TaskDialogNotification.HyperlinkClicked:
                            BrowserLauncherFactory.get().open(args.Hyperlink);
                            return true;
                    }
                    return false;
                });
            handler(result.CommandButtonResult ?? -1, result.VerificationChecked ?? false);
            return result;
        }

        public static TaskDialogResult CommandBox(string title, string mainInstruction, string content,
            string expandedInfo, string help, string verificationText, string commandButtons, bool showCancelButton,
            TaskDialogIcon mainIcon, TaskDialogIcon footerIcon, DialogResponseHandler handler)
        {
            return CommandBox(null, title, mainInstruction, content, expandedInfo, help, verificationText,
                commandButtons, showCancelButton, mainIcon, footerIcon, handler);
        }

        public static TaskDialogResult CommandBox(string title, string message, string detail, string commandButtons,
            bool showCancelButton, string verificationText, TaskDialogIcon mainIcon, DialogResponseHandler handler)
        {
            return CommandBox(title, message, detail, commandButtons, showCancelButton, verificationText, mainIcon, null,
                handler);
        }

        public static TaskDialogResult CommandBox(string title, string message, string detail, string commandButtons,
            bool showCancelButton, string verificationText, TaskDialogIcon mainIcon, string help,
            DialogResponseHandler handler)
        {
            return CommandBox(title, message, detail, null, help, verificationText, commandButtons, showCancelButton,
                mainIcon, TaskDialogIcon.Information, handler);
        }

        public static TaskDialogResult MessageBox(IWin32Window owner, string title, string message, string content,
            string expandedInfo, string help, string verificationText, DialogResponseHandler handler)
        {
            TaskDialogResult result = TaskDialog.Show(
                owner: owner?.Handle ?? IntPtr.Zero,
                title: title,
                mainInstruction: message,
                content: content,
                footerText: FormatHelp(help),
                expandedInfo: expandedInfo,
                verificationText: verificationText,
                commonButtons: TaskDialogCommonButtons.OK,
                mainIcon: TaskDialogIcon.Information,
                callback: (dialog, args, callbackData) =>
                {
                    switch (args.Notification)
                    {
                        case TaskDialogNotification.HyperlinkClicked:
                            BrowserLauncherFactory.get().open(args.Hyperlink);
                            return true;
                    }
                    return false;
                });
            handler(result.CommandButtonResult ?? -1, result.VerificationChecked ?? false);
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
    }
}