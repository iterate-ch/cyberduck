//
// Copyright (c) 2002-2022 iterate GmbH. All rights reserved.
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

using System.Drawing;
using Windows.Win32;
using Windows.Win32.Foundation;
using Windows.Win32.UI.Controls;
using Windows.Win32.UI.WindowsAndMessaging;
using static Windows.Win32.CorePInvoke;
using static Windows.Win32.UI.Controls.TASKDIALOG_ELEMENTS;
using static Windows.Win32.UI.Controls.TASKDIALOG_ICON_ELEMENTS;
using static Windows.Win32.UI.Controls.TASKDIALOG_MESSAGES;
using static Windows.Win32.UI.WindowsAndMessaging.SET_WINDOW_POS_FLAGS;

namespace Ch.Cyberduck.Core.TaskDialog
{
    public static class TaskDialogEventArgsExtensions
    {
        public static bool ClickButton(this TaskDialogEventArgs @this, uint buttonId)
            => SendMessage(@this, TDM_CLICK_BUTTON, (nuint)buttonId, 0).Value != 0;

        public static bool ClickRadioButton(this TaskDialogEventArgs @this, uint buttonId)
            => SendMessage(@this, TDM_CLICK_RADIO_BUTTON, (nuint)buttonId, 0).Value != 0;

        public static bool ClickVerification(this TaskDialogEventArgs @this, bool checkedState, bool setKeyboardFocusToCheckBox)
            => SendMessage(@this, TDM_CLICK_VERIFICATION, (nuint)(checkedState ? 1 : 0), (nint)(setKeyboardFocusToCheckBox ? 1 : 0)).Value != 0;

        public static bool MakeTopMost(this TaskDialogEventArgs @this)
            => SetWindowPos(@this, HWND_TOPMOST, 0, 0, 0, 0, SWP_NOSIZE | SWP_NOMOVE);

        public static void SetButtonElevationRequiredState(this TaskDialogEventArgs @this, uint buttonId, bool elevationRequired)
            => SendMessage(@this, TDM_SET_BUTTON_ELEVATION_REQUIRED_STATE, (nuint)buttonId, (nint)(elevationRequired ? 1 : 0));

        public static void SetButtonState(this TaskDialogEventArgs @this, uint buttonId, bool enabled)
            => SendMessage(@this, TDM_ENABLE_BUTTON, (nuint)buttonId, (nint)(enabled ? 1 : 0));

        public static bool SetContent(this TaskDialogEventArgs @this, string content)
            => SendMessage(@this, TDM_SET_ELEMENT_TEXT, (nuint)TDE_CONTENT, content).Value != 0;

        public static bool SetExpandedInformation(this TaskDialogEventArgs @this, string content)
            => SendMessage(@this, TDM_SET_ELEMENT_TEXT, (nuint)TDE_EXPANDED_INFORMATION, content).Value != 0;

        public static bool SetFooter(this TaskDialogEventArgs @this, string content)
            => SendMessage(@this, TDM_SET_ELEMENT_TEXT, (nuint)TDE_FOOTER, content).Value != 0;

        public static bool SetMainInstruction(this TaskDialogEventArgs @this, string content)
            => SendMessage(@this, TDM_SET_ELEMENT_TEXT, (nuint)TDE_MAIN_INSTRUCTION, content).Value != 0;

        public static bool SetMarqueeProgressBar(this TaskDialogEventArgs @this, bool marquee)
            => SendMessage(@this, TDM_SET_MARQUEE_PROGRESS_BAR, (nuint)(marquee ? 1 : 0), 0).Value != 0;

        public static bool SetProgressBarMarquee(this TaskDialogEventArgs @this, bool startMarquee, int speed)
            => SendMessage(@this, TDM_SET_PROGRESS_BAR_MARQUEE, (nuint)(startMarquee ? 1 : 0), (nint)speed).Value != 0;

        public static int SetProgressBarPosition(this TaskDialogEventArgs @this, uint newPosition)
            => (int)SendMessage(@this, TDM_SET_PROGRESS_BAR_POS, (nuint)newPosition, 0).Value;

        public static bool SetProgressBarRange(this TaskDialogEventArgs @this, short minRange, short maxRange)
            => (int)SendMessage(@this, TDM_SET_PROGRESS_BAR_RANGE, 0, (nint)((int)minRange | maxRange << 16)).Value != 0;

        public static bool SetProgressBarState(this TaskDialogEventArgs @this, uint newState)
            => SendMessage(@this, TDM_SET_PROGRESS_BAR_STATE, (nuint)newState, 0).Value != 0;

        public static void SetRadioButtonState(this TaskDialogEventArgs @this, uint buttonId, bool enabled)
            => SendMessage(@this, TDM_ENABLE_RADIO_BUTTON, (nuint)buttonId, (nint)(enabled ? 1 : 0));

        public static bool SetWindowTitle(this TaskDialogEventArgs @this, string title)
            => SetWindowText(@this, title);

        public static bool UpdateContent(this TaskDialogEventArgs @this, string content)
            => SendMessage(@this, TDM_UPDATE_ELEMENT_TEXT, (nuint)TDE_CONTENT, content).Value != 0;

        public static bool UpdateExpandedInformation(this TaskDialogEventArgs @this, string content)
            => SendMessage(@this, TDM_UPDATE_ELEMENT_TEXT, (nuint)TDE_EXPANDED_INFORMATION, content).Value != 0;

        public static bool UpdateFooter(this TaskDialogEventArgs @this, string content)
            => SendMessage(@this, TDM_UPDATE_ELEMENT_TEXT, (nuint)TDE_FOOTER, content).Value != 0;

        public static void UpdateFooterIcon(this TaskDialogEventArgs @this, PCWSTR icon)
            => SendMessage(@this, TDM_UPDATE_ICON, (nuint)TDIE_ICON_FOOTER, icon);

        public static void UpdateFooterIcon(this TaskDialogEventArgs @this, Icon icon)
            => SendMessage(@this, TDM_UPDATE_ICON, (nuint)TDIE_ICON_FOOTER, icon.Handle);

        public static void UpdateMainIcon(this TaskDialogEventArgs @this, PCWSTR icon)
            => SendMessage(@this, TDM_UPDATE_ICON, (nuint)TDIE_ICON_MAIN, icon);

        public static void UpdateMainIcon(this TaskDialogEventArgs @this, Icon icon)
            => SendMessage(@this, TDM_UPDATE_ICON, (nuint)TDIE_ICON_MAIN, icon.Handle);

        public static bool UpdateMainInstruction(this TaskDialogEventArgs @this, string content)
            => SendMessage(@this, TDM_UPDATE_ELEMENT_TEXT, (nuint)TDE_MAIN_INSTRUCTION, content).Value != 0;

        private static LRESULT SendMessage(TaskDialog.ITaskDialog @this, TASKDIALOG_MESSAGES msg, WPARAM wParam, LPARAM lParam)
            => CorePInvoke.SendMessage(@this.HWND, (uint)msg, wParam, lParam);

        private static LRESULT SendMessage(TaskDialog.ITaskDialog @this, TASKDIALOG_MESSAGES msg, WPARAM wParam, PCWSTR lParam)
            => CorePInvoke.SendMessage(@this.HWND, (uint)msg, wParam, lParam);

        private static LRESULT SendMessage(TaskDialog.ITaskDialog @this, TASKDIALOG_MESSAGES msg, WPARAM wParam, string lParam)
            => CorePInvoke.SendMessage(@this.HWND, (uint)msg, wParam, lParam);

        private static BOOL SetWindowPos(TaskDialog.ITaskDialog @this, HWND hWndInsertAfter, int X, int Y, int cx, int cy, SET_WINDOW_POS_FLAGS uFlags)
            => CorePInvoke.SetWindowPos(@this.HWND, hWndInsertAfter, X, Y, cx, cy, uFlags);

        private static BOOL SetWindowText(TaskDialog.ITaskDialog @this, string lpString)
            => CorePInvoke.SetWindowText(@this.HWND, lpString);
    }
}
