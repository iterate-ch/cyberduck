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

using System.ComponentModel;
using System.Windows.Forms;
using Ch.Cyberduck.Core.TaskDialog;

namespace Ch.Cyberduck.Ui.Controller
{
    public delegate void DialogResponseHandler(int option, bool verificationChecked);

    public interface IView : ISynchronizeInvoke
    {
        bool Visible { get; set; }
        bool ReleaseWhenClose { set; }
        bool IsHandleCreated { get; }
        bool IsDisposed { get; }
        bool Disposing { get; }
        DialogResult ModalResult { get; }
        void Close();
        void Dispose();
        void Show();
        void Show(IWin32Window owner);
        void Show(IView owner);
        void Activate();
        void BringToFront();

        DialogResult ShowDialog();
        DialogResult ShowDialog(IWin32Window owner);
        DialogResult ShowDialog(IView owner);

        TaskDialogResult MessageBox(string title, string message, string content, string expandedInfo, string help,
            string verificationText, DialogResponseHandler handler);

        TaskDialogResult MessageBox(string title, string message, string content, TaskDialogCommonButtons buttons,
            TaskDialogIcon icons);

        TaskDialogResult CommandBox(string title, string mainInstruction, string content, string expandedInfo, string help,
            string verificationText, string commandButtons, bool showCancelButton, TaskDialogIcon mainIcon,
            TaskDialogIcon footerIcon, DialogResponseHandler handler);

        //todo evtl. extend form that implements these events
        event VoidHandler PositionSizeRestoredEvent;
        event VoidHandler ViewShownEvent;
        event VoidHandler ViewClosedEvent;
        event FormClosingEventHandler ViewClosingEvent;
        event VoidHandler ViewDisposedEvent;

        void ValidateCommands();
    }
}