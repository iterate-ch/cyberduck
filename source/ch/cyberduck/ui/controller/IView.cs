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
using System.ComponentModel;
using System.Windows.Forms;
using ch.cyberduck.ui.controller;
using Ch.Cyberduck.Ui.Winforms.Taskdialog;

namespace Ch.Cyberduck.Ui.Controller
{
    public interface IView : ISynchronizeInvoke
    {
        bool Visible { get; set; }
        bool ReleaseWhenClose { set; }
        bool IsDisposed { get; }
        void Close();
        void Dispose();
        void Show();
        void Show(IWin32Window owner);
        void Show(IView owner);
        void BringToFront();
        DialogResult ModalResult { get; }

        DialogResult ShowDialog();
        DialogResult ShowDialog(IWin32Window owner);
        DialogResult ShowDialog(IView owner);

        DialogResult MessageBox(
            string title,
            string message,
            string content,
            eTaskDialogButtons buttons,
            eSysIcons icon);

        int CommandBox(
            string title,
            string mainInstruction,
            string content,
            string expandedInfo,
            string footer,
            string verificationText,
            string commandButtons,
            bool showCancelButton,
            eSysIcons mainIcon,
            eSysIcons footerIcon);

        //todo evtl. extend form that implements these events
        event VoidHandler ViewShownEvent;
        event VoidHandler ViewClosedEvent;
        event FormClosingEventHandler ViewClosingEvent;
        event VoidHandler ViewDisposedEvent;

        void ValidateCommands();
    }
}