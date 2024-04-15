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

using ch.cyberduck.core;
using ch.cyberduck.core.local;
using Ch.Cyberduck.Core.TaskDialog;
using Ch.Cyberduck.Ui.Controller;
using System.Windows.Forms;
using Windows.Win32.Foundation;
using Windows.Win32.UI.Controls;
using Windows.Win32.UI.WindowsAndMessaging;
using static Windows.Win32.UI.Controls.TASKDIALOG_COMMON_BUTTON_FLAGS;

namespace Ch.Cyberduck.Ui.Core
{
    public class Utils
    {
        private const MESSAGEBOX_RESULT BUTTON_ID = (MESSAGEBOX_RESULT)0b1000_0000;
        private const MESSAGEBOX_RESULT BUTTON_MASK = (MESSAGEBOX_RESULT)0b0111_1111;

        public static TaskDialogResult Show(
            IWin32Window owner = default,
            bool allowDialogCancellation = false,
            string title = null,
            TaskDialogIcon? mainIcon = default,
            string mainInstruction = null,
            string content = null,
            string expandedInfo = null,
            bool expandedByDefault = false,
            TASKDIALOG_COMMON_BUTTON_FLAGS commonButtons = 0,
            string[] customButtons = null,
            string[] radioButtons = null,
            string[] commandLinks = null,
            bool showProgressBar = false,
            bool showMarqueeProgressBar = false,
            TaskDialogIcon? footerIcon = default,
            string footerText = null,
            bool expandToFooter = false,
            string verificationText = null,
            bool verificationByDefault = false,
            DialogResponseHandler callback = null,
            bool enableCallbackTimer = false)
        {
            var dialog = TaskDialog.Create()
                .CommonButtons(commonButtons)
                .Content(content)
                .ExpandedInformation(expandedInfo)
                .FooterText(footerText)
                .Instruction(mainInstruction)
                .Parent((HWND)(owner?.Handle ?? default))
                .Title(title)
                .UseHyperlinks()
                .VerificationText(verificationText, verificationByDefault)
                .Callback((s, e) =>
                {
                    if (e is TaskDialogHyperlinkClickedEventArgs hyperlinkClickedEventArgs)
                    {
                        BrowserLauncherFactory.get().open(hyperlinkClickedEventArgs.Url);
                        return true;
                    }

                    return false;
                });

            if (allowDialogCancellation)
            {
                dialog.AllowCancellation();
            }

            if (enableCallbackTimer)
            {
                dialog.EnableCallbackTimer();
            }

            if (expandedByDefault)
            {
                dialog.ExpandedByDefault();
            }

            if (expandToFooter)
            {
                dialog.ExpandToFooter();
            }

            if (footerIcon is { } footerIconValue)
            {
                dialog.FooterIcon(footerIconValue);
            }

            if (mainIcon is { } mainIconValue)
            {
                dialog.MainIcon(mainIconValue);
            }

            if (showProgressBar)
            {
                dialog.ShowProgressbar(showMarqueeProgressBar);
            }

            if (radioButtons != null)
            {
                dialog.RadioButtons(c =>
                {
                    for (int i = 0, end = radioButtons.Length; i < end; i++)
                    {
                        var value = radioButtons[i];
                        c((MESSAGEBOX_RESULT)i, value, false);
                    }
                });
            }

            switch (customButtons, commandLinks)
            {
                case (not null, null) v:
                    dialog.CustomButtons(c =>
                    {
                        for (int i = 0, end = v.customButtons.Length; i < end; i++)
                        {
                            var value = v.customButtons[i];
                            c((MESSAGEBOX_RESULT)i | BUTTON_ID, value, false);
                        }
                    });
                    break;

                case (null, not null) v:
                    dialog.CommandLinks(c =>
                    {
                        for (int i = 0, end = v.commandLinks.Length; i < end; i++)
                        {
                            var value = v.commandLinks[i];
                            c((MESSAGEBOX_RESULT)i | BUTTON_ID, value, false);
                        }
                    });
                    break;
            }

            var result = dialog.Show();
            if ((result.Button & BUTTON_ID) > 0)
            {
                result = new(result.Button & BUTTON_MASK,
                    result.RadioButton,
                    result.VerificationChecked);
            }

            callback?.Invoke((int)result.Button, result.VerificationChecked.GetValueOrDefault());
            return result;
        }

        public static TaskDialogResult CommandBox(IWin32Window owner, string title, string mainInstruction,
            string content, string expandedInfo, string help, string verificationText, string commandButtons,
            bool showCancelButton, TaskDialogIcon mainIcon, TaskDialogIcon footerIcon, DialogResponseHandler handler)
        {
            var dialog = TaskDialog.Create()
                .CommonButtons(showCancelButton ? TDCBF_CANCEL_BUTTON : 0)
                .Content(content)
                .ExpandedInformation(expandedInfo)
                .FooterIcon(footerIcon)
                .FooterText(FormatHelp(help))
                .Instruction(mainInstruction)
                .MainIcon(mainIcon)
                .Parent((HWND)(owner?.Handle ?? default))
                .Title(title)
                .UseHyperlinks()
                .VerificationText(verificationText, false)
                .CommandLinks(add =>
                {
                    var split = commandButtons.Split('|');
                    for (int i = 0; i < split.Length; i++)
                    {
                        add((MESSAGEBOX_RESULT)i | BUTTON_ID, split[i], false);
                    }
                })
                .Callback((s, e) =>
                {
                    switch (e)
                    {
                        case TaskDialogHyperlinkClickedEventArgs hyperlinkClickedEventArgs:
                            BrowserLauncherFactory.get().open(hyperlinkClickedEventArgs.Url);
                            return true;
                    }
                    return false;
                });

            var result = dialog.Show();
            if ((result.Button & BUTTON_ID) > 0)
            {
                result = new(result.Button & BUTTON_MASK,
                    result.RadioButton,
                    result.VerificationChecked);
            }
            handler((int)result.Button, result.VerificationChecked.GetValueOrDefault());
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
            var dialog = TaskDialog.Create()
                .CommonButtons(TDCBF_OK_BUTTON)
                .Content(content)
                .ExpandedInformation(expandedInfo)
                .FooterText(FormatHelp(help))
                .Instruction(message)
                .Parent((HWND)owner?.Handle)
                .Title(title)
                .UseHyperlinks()
                .VerificationText(verificationText, false)
                .Callback((s, e) =>
                {
                    switch (e)
                    {
                        case TaskDialogHyperlinkClickedEventArgs hyperlinkClickedEventArgs:
                            BrowserLauncherFactory.get().open(hyperlinkClickedEventArgs.Url);
                            return true;
                    }
                    return false;
                });

            var result = dialog.Show();
            handler((int)result.Button, result.VerificationChecked.GetValueOrDefault());
            return result;
        }

        public static string FormatHelp(string help)
        {
            if (string.IsNullOrEmpty(help))
            {
                return null;
            }
            return "<A HREF=\"" + help + "\">" + LocaleFactory.localizedString("Help", "Main") + "</A>";
        }
    }
}
