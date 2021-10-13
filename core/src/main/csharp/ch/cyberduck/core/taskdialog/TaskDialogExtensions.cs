using Windows.Win32;
using Windows.Win32.Foundation;
using Windows.Win32.UI.Controls;
using Windows.Win32.UI.WindowsAndMessaging;
using System;
using System.Drawing;
using static Windows.Win32.Constants;
using static Windows.Win32.CorePInvoke;
using static Windows.Win32.UI.WindowsAndMessaging.SET_WINDOW_POS_FLAGS;
using static Windows.Win32.UI.Controls.TASKDIALOG_ELEMENTS;
using static Windows.Win32.UI.Controls.TASKDIALOG_ICON_ELEMENTS;
using static Windows.Win32.UI.Controls.TASKDIALOG_MESSAGES;
namespace Ch.Cyberduck.Core.TaskDialog
{
    partial class TaskDialog
    {
        /// <summary>
        /// Simulate the action of a button click in the TaskDialog. This can be a DialogResult value
        /// or the ButtonID set on a TaskDialogButton set on TaskDialog.Buttons.
        /// </summary>
        /// <param name="buttonId">Indicates the button ID to be selected.</param>
        /// <returns>If the function succeeds the return value is true.</returns>
        public static bool ClickButton(this TaskDialogNotificationArgs args, nuint buttonId)
        {
            var handle = args.GetHWND();

            if (buttonId >= RadioButtonIDOffset && buttonId < CommandButtonIDOffset)
            {
                // TDM_CLICK_RADIO_BUTTON = WM_USER+110, // wParam = Radio Button ID
                return SendMessage(handle, TDM_CLICK_RADIO_BUTTON, buttonId, 0).Value != 0;
            }
            else
            {
                // TDM_CLICK_BUTTON = WM_USER+102, // wParam = Button ID
                return SendMessage(handle, TDM_CLICK_BUTTON, buttonId, 0).Value != 0;
            }
        }

        /// <summary>
        /// Simulate the action of a command link button click in the TaskDialog.
        /// </summary>
        /// <param name="index">The zero-based index into the button set.</param>
        /// <returns>
        /// If the function succeeds the return value is true.
        /// </returns>
        public static bool ClickCommandButton(this TaskDialogNotificationArgs args, uint index) => args.ClickButton(GetButtonIdForCommandButton(index));

        /// <summary>
        /// Simulate the action of a common button click in the TaskDialog.
        /// </summary>
        /// <param name="index">The zero-based index into the button set.</param>
        /// <returns>
        /// If the function succeeds the return value is true.
        /// </returns>
        public static bool ClickCommonButton(this TaskDialogNotificationArgs args, uint index) => args.ClickButton(index);

        /// <summary>
        /// Simulate the action of a custom button click in the TaskDialog.
        /// </summary>
        /// <param name="index">The zero-based index into the button set.</param>
        /// <returns>
        /// If the function succeeds the return value is true.
        /// </returns>
        public static bool ClickCustomButton(this TaskDialogNotificationArgs args, uint index) => args.ClickButton(GetButtonIdForCustomButton(index));

        /// <summary>
        /// Simulate the action of a radio button click in the TaskDialog.
        /// </summary>
        /// <param name="index">The zero-based index into the button set.</param>
        /// <returns>
        /// If the function succeeds the return value is true.
        /// </returns>
        public static bool ClickRadioButton(this TaskDialogNotificationArgs args, uint index) => args.ClickButton(GetButtonIdForRadioButton(index));

        /// <summary>
        /// Check or uncheck the verification checkbox in the TaskDialog.
        /// </summary>
        /// <param name="checkedState">The checked state to set the verification checkbox.</param>
        /// <param name="setKeyboardFocusToCheckBox">True to set the keyboard focus to the checkbox, and fasle otherwise.</param>
        public static void ClickVerification(this TaskDialogNotificationArgs args, bool checkedState, bool setKeyboardFocusToCheckBox)
        {
            var handle = args.GetHWND();
            // TDM_CLICK_VERIFICATION = WM_USER+113, // wParam = 0 (unchecked), 1 (checked), lParam = 1 (set key focus)
            SendMessage(handle, TDM_CLICK_VERIFICATION,
                checkedState ? 1u : 0,
                setKeyboardFocusToCheckBox ? 1 : 0);
        }

        public static void MakeTopMost(this TaskDialogNotificationArgs taskDialog)
        {
            var handle = taskDialog.GetHWND();
            // https://docs.microsoft.com/en-us/windows/win32/api/winuser/nf-winuser-setwindowpos
            // HWND_TOPMOST (HWND) - 1
            // Places the window above all non-topmost windows.The window maintains its topmost position even when it is deactivated.

            SetWindowPos(
                 handle, HWND_TOPMOST,
                 0, 0, 0, 0,
                 SWP_NOSIZE | SWP_NOMOVE);
        }

        /// <summary>
        /// Designate whether a given Task Dialog button or command link should have a User Account Control (UAC) shield icon.
        /// </summary>
        /// <param name="buttonId">ID of the push button or command link to be updated.</param>
        /// <param name="elevationRequired">False to designate that the action invoked by the button does not require elevation;
        /// true to designate that the action does require elevation.</param>
        public static void SetButtonElevationRequiredState(this TaskDialogNotificationArgs args, nuint buttonId, bool elevationRequired)
        {
            var handle = args.GetHWND();
            // TDM_SET_BUTTON_ELEVATION_REQUIRED_STATE = WM_USER+115, // wParam = Button ID, lParam = 0 (elevation not required), lParam != 0 (elevation required)
            SendMessage(handle, TDM_SET_BUTTON_ELEVATION_REQUIRED_STATE,
                buttonId, elevationRequired ? 1 : 0);
        }

        /// <summary>
        /// Enable or disable a button in the TaskDialog.
        /// The passed buttonID is the ButtonID set on a TaskDialogButton set on TaskDialog.Buttons
        /// or a common button ID.
        /// </summary>
        /// <param name="buttonId">Indicates the button ID to be enabled or diabled.</param>
        /// <param name="enabled">Enambe the button if true. Disable the button if false.</param>
        public static void SetButtonEnabledState(this TaskDialogNotificationArgs args, nuint buttonId, bool enabled)
        {
            var handle = args.GetHWND();

            if (buttonId >= RadioButtonIDOffset && buttonId < CommandButtonIDOffset)
            {
                // TDM_ENABLE_RADIO_BUTTON = WM_USER+112, // lParam = 0 (disable), lParam != 0 (enable), wParam = Radio Button ID
                SendMessage(handle, TDM_ENABLE_RADIO_BUTTON, buttonId, enabled ? 1 : 0);
            }
            else
            {
                // TDM_ENABLE_BUTTON = WM_USER+111, // lParam = 0 (disable), lParam != 0 (enable), wParam = Button ID
                SendMessage(handle, TDM_ENABLE_BUTTON, buttonId, enabled ? 1 : 0);
            }
        }

        /// <summary>
        /// Sets the elevation required state of a command link button, adding a shield icon.
        /// </summary>
        /// <param name="index">The zero-based index of the button to set.</param>
        /// <param name="elevationRequired"><c>true</c> to show a shield icon; <c>false</c> to remove</param>
        /// <remarks>
        /// Note that this is purely for visual effect. You will still need to perform
        /// the necessary code to trigger a UAC prompt for the user.
        /// </remarks>
        public static void SetCommandButtonElevationRequiredState(this TaskDialogNotificationArgs args, uint index, bool elevationRequired)
        {
            args.SetButtonElevationRequiredState(GetButtonIdForCommandButton(index), elevationRequired);
        }

        /// <summary>
        /// Sets the state of a command link button to enabled or disabled.
        /// </summary>
        /// <param name="index">The zero-based index of the button to set.</param>
        /// <param name="enabled"><c>true</c> to enable the button; <c>false</c> to disable</param>
        public static void SetCommandButtonEnabledState(this TaskDialogNotificationArgs args, uint index, bool enabled)
        {
            args.SetButtonEnabledState(GetButtonIdForCommandButton(index), enabled);
        }

        /// <summary>
        /// Sets the elevation required state of a common button, adding a shield icon.
        /// </summary>
        /// <param name="index">The zero-based index of the button to set.</param>
        /// <param name="elevationRequired"><c>true</c> to show a shield icon; <c>false</c> to remove</param>
        /// <remarks>
        /// Note that this is purely for visual effect. You will still need to perform
        /// the necessary code to trigger a UAC prompt for the user.
        /// </remarks>
        public static void SetCommonButtonElevationRequiredState(this TaskDialogNotificationArgs args, uint index, bool elevationRequired)
        {
            args.SetButtonElevationRequiredState(index, elevationRequired);
        }

        /// <summary>
        /// Sets the state of a common button to enabled or disabled.
        /// </summary>
        /// <param name="index">The zero-based index of the button to set.</param>
        /// <param name="enabled"><c>true</c> to enable the button; <c>false</c> to disable</param>
        public static void SetCommonButtonEnabledState(this TaskDialogNotificationArgs args, uint index, bool enabled)
        {
            args.SetButtonEnabledState(index, enabled);
        }

        /// <summary>
        /// Updates the content text.
        /// </summary>
        /// <param name="content">The new value.</param>
        /// <returns>If the function succeeds the return value is true.</returns>
        public static bool SetContent(this TaskDialogNotificationArgs args, string content)
        {
            var handle = args.GetHWND();
            // TDE_CONTENT,
            // TDM_SET_ELEMENT_TEXT                = WM_USER+108  // wParam = element (TASKDIALOG_ELEMENTS), lParam = new element text (LPCWSTR)
            return SendMessage(handle, TDM_SET_ELEMENT_TEXT,
                TDE_CONTENT, content).Value != 0;
        }

        /// <summary>
        /// Sets the elevation required state of a custom button, adding a shield icon.
        /// </summary>
        /// <param name="index">The zero-based index of the button to set.</param>
        /// <param name="elevationRequired"><c>true</c> to enable the button; <c>false</c> to disable</param>
        /// <remarks>
        /// Note that this is purely for visual effect. You will still need to perform
        /// the necessary code to trigger a UAC prompt for the user.
        /// </remarks>
        public static void SetCustomButtonElevationRequiredState(this TaskDialogNotificationArgs args, uint index, bool elevationRequired)
        {
            args.SetButtonElevationRequiredState(TaskDialog.GetButtonIdForCustomButton(index), elevationRequired);
        }

        /// <summary>
        /// Sets the state of a custom button to enabled or disabled.
        /// </summary>
        /// <param name="index">The zero-based index of the button to set.</param>
        /// <param name="enabled"><c>true</c> to enable the button; <c>false</c> to disable</param>
        public static void SetCustomButtonEnabledState(this TaskDialogNotificationArgs args, uint index, bool enabled)
        {
            args.SetButtonEnabledState(GetButtonIdForCustomButton(index), enabled);
        }

        /// <summary>
        /// Updates the Expanded Information text.
        /// </summary>
        /// <param name="expandedInformation">The new value.</param>
        /// <returns>If the function succeeds the return value is true.</returns>
        public static bool SetExpandedInformation(this TaskDialogNotificationArgs args, string expandedInformation)
        {
            var handle = args.GetHWND();

            // TDE_EXPANDED_INFORMATION,
            // TDM_SET_ELEMENT_TEXT                = WM_USER+108  // wParam = element (TASKDIALOG_ELEMENTS), lParam = new element text (LPCWSTR)
            return SendMessage(handle, TDM_SET_ELEMENT_TEXT,
                TDE_EXPANDED_INFORMATION, expandedInformation).Value != 0;
        }

        /// <summary>
        /// Updates the Footer text.
        /// </summary>
        /// <param name="footer">The new value.</param>
        /// <returns>If the function succeeds the return value is true.</returns>
        public static bool SetFooter(this TaskDialogNotificationArgs args, string footer)
        {
            var handle = args.GetHWND();

            // TDE_FOOTER,
            // TDM_SET_ELEMENT_TEXT                = WM_USER+108  // wParam = element (TASKDIALOG_ELEMENTS), lParam = new element text (LPCWSTR)
            return SendMessage(handle, TDM_SET_ELEMENT_TEXT,
                TDE_FOOTER, footer).Value != 0;
        }

        /// <summary>
        /// Updates the Main Instruction.
        /// </summary>
        /// <param name="mainInstruction">The new value.</param>
        /// <returns>If the function succeeds the return value is true.</returns>
        public static bool SetMainInstruction(this TaskDialogNotificationArgs args, string mainInstruction)
        {
            var handle = args.GetHWND();

            // TDE_MAIN_INSTRUCTION
            // TDM_SET_ELEMENT_TEXT                = WM_USER+108  // wParam = element (TASKDIALOG_ELEMENTS), lParam = new element text (LPCWSTR)
            return SendMessage(handle, TDM_SET_ELEMENT_TEXT,
                TDE_MAIN_INSTRUCTION, mainInstruction).Value != 0;
        }

        /// <summary>
        /// Used to indicate whether the hosted progress bar should be displayed in marquee mode or not.
        /// </summary>
        /// <param name="marquee">Specifies whether the progress bar sbould be shown in Marquee mode.
        /// A value of true turns on Marquee mode.</param>
        /// <returns>If the function succeeds the return value is true.</returns>
        public static bool SetMarqueeProgressBar(this TaskDialogNotificationArgs args, bool marquee)
        {
            var handle = args.GetHWND();

            // TDM_SET_MARQUEE_PROGRESS_BAR        = WM_USER+103, // wParam = 0 (nonMarque) wParam != 0 (Marquee)
            return SendMessage(handle, TDM_SET_MARQUEE_PROGRESS_BAR,
                marquee ? 1u : 0, 0).Value != 0;

            // Future: get more detailed error from and throw.
        }

        /// <summary>
        /// Sets the animation state of the Marquee Progress Bar.
        /// </summary>
        /// <param name="startMarquee">true starts the marquee animation and false stops it.</param>
        /// <param name="speed">The time in milliseconds between refreshes.</param>
        public static void SetProgressBarMarquee(this TaskDialogNotificationArgs args, bool startMarquee, nint speed)
        {
            var handle = args.GetHWND();

            // TDM_SET_PROGRESS_BAR_MARQUEE        = WM_USER+107, // wParam = 0 (stop marquee), wParam != 0 (start marquee), lparam = speed (milliseconds between repaints)
            SendMessage(handle, TDM_SET_PROGRESS_BAR_MARQUEE,
                startMarquee ? 1u : 0, speed);
        }

        /// <summary>
        /// Set the current position for a progress bar.
        /// </summary>
        /// <param name="newPosition">The new position.</param>
        /// <returns>Returns the previous value if successful, or zero otherwise.</returns>
        public static int SetProgressBarPosition(this TaskDialogNotificationArgs args, uint newPosition)
        {
            var handle = args.GetHWND();

            // TDM_SET_PROGRESS_BAR_POS            = WM_USER+106, // wParam = new position
            return (int)SendMessage(handle, TDM_SET_PROGRESS_BAR_POS,
                newPosition, 0).Value;
        }

        /// <summary>
        /// Set the minimum and maximum values for the hosted progress bar.
        /// </summary>
        /// <param name="minRange">Minimum range value. By default, the minimum value is zero.</param>
        /// <param name="maxRange">Maximum range value.  By default, the maximum value is 100.</param>
        /// <returns>If the function succeeds the return value is true.</returns>
        public static bool SetProgressBarRange(this TaskDialogNotificationArgs args, short minRange, short maxRange)
        {
            var handle = args.GetHWND();

            // TDM_SET_PROGRESS_BAR_RANGE          = WM_USER+105, // lParam = MAKELPARAM(nMinRange, nMaxRange)
            // #define MAKELPARAM(l, h)      ((LPARAM)(DWORD)MAKELONG(l, h))
            // #define MAKELONG(a, b)      ((LONG)(((WORD)(((DWORD_PTR)(a)) & 0xffff)) | ((DWORD)((WORD)(((DWORD_PTR)(b)) & 0xffff))) << 16))
            return SendMessage(handle, TDM_SET_PROGRESS_BAR_RANGE,
                default(nuint), (int)minRange | maxRange << 16).Value != 0;

            // Return value is actually prior range.
        }

        /// <summary>
        /// Sets the state of the progress bar.
        /// </summary>
        /// <param name="newState">The state to set the progress bar.</param>
        /// <returns>If the function succeeds the return value is true.</returns>
        public static bool SetProgressBarState(this TaskDialogNotificationArgs args, TaskDialogProgressBarState newState)
        {
            var handle = args.GetHWND();

            // TDM_SET_PROGRESS_BAR_STATE          = WM_USER+104, // wParam = new progress state
            return SendMessage(handle, TDM_SET_PROGRESS_BAR_STATE, newState, default).Value != 0;

            // Future: get more detailed error from and throw.
        }

        /// <summary>
        /// Sets the state of a radio button to enabled or disabled.
        /// </summary>
        /// <param name="index">The zero-based index of the button to set.</param>
        /// <param name="enabled"><c>true</c> to enable the button; <c>false</c> to disable</param>
        public static void SetRadioButtonEnabledState(this TaskDialogNotificationArgs args, uint index, bool enabled)
        {
            args.SetButtonEnabledState(GetButtonIdForRadioButton(index), enabled);
        }

        /// <summary>
        /// Updates the window title text.
        /// </summary>
        /// <param name="title">The new value.</param>
        /// <returns>If the function succeeds the return value is true.</returns>
        public static bool SetWindowTitle(this TaskDialogNotificationArgs args, string title)
        {
            var handle = args.GetHWND();

            return SetWindowText(handle, title);
        }

        /// <summary>
        /// Updates the content text.
        /// </summary>
        /// <param name="content">The new value.</param>
        public static void UpdateContent(this TaskDialogNotificationArgs args, string content)
        {
            var handle = args.GetHWND();

            // TDE_CONTENT,
            // TDM_UPDATE_ELEMENT_TEXT             = WM_USER+114, // wParam = element (TASKDIALOG_ELEMENTS), lParam = new element text (LPCWSTR)
            SendMessage(handle, TDM_UPDATE_ELEMENT_TEXT,
                TDE_CONTENT, content);
        }

        /// <summary>
        /// Updates the Expanded Information text. No effect if it was previously set to null.
        /// </summary>
        /// <param name="expandedInformation">The new value.</param>
        public static void UpdateExpandedInformation(this TaskDialogNotificationArgs args, string expandedInformation)
        {
            var handle = args.GetHWND();

            // TDE_EXPANDED_INFORMATION,
            // TDM_UPDATE_ELEMENT_TEXT             = WM_USER+114, // wParam = element (TASKDIALOG_ELEMENTS), lParam = new element text (LPCWSTR)
            SendMessage(handle, TDM_UPDATE_ELEMENT_TEXT,
                TDE_EXPANDED_INFORMATION, expandedInformation);
        }

        /// <summary>
        /// Updates the Footer text. No Effect if it was perviously set to null.
        /// </summary>
        /// <param name="footer">The new value.</param>
        public static void UpdateFooter(this TaskDialogNotificationArgs args, string footer)
        {
            var handle = args.GetHWND();

            // TDE_FOOTER,
            // TDM_UPDATE_ELEMENT_TEXT             = WM_USER+114, // wParam = element (TASKDIALOG_ELEMENTS), lParam = new element text (LPCWSTR)
            SendMessage(handle, TDM_UPDATE_ELEMENT_TEXT,
                TDE_FOOTER, footer);
        }

        /// <summary>
        /// Updates the footer icon. Note the type (standard via enum or
        /// custom via Icon type) must be used when upating the icon.
        /// </summary>
        /// <param name="icon">Task Dialog standard icon.</param>
        public static void UpdateFooterIcon(this TaskDialogNotificationArgs args, TaskDialogIcon icon)
        {
            var handle = args.GetHWND();

            // TDM_UPDATE_ICON = WM_USER+116  // wParam = icon element (TASKDIALOG_ICON_ELEMENTS), lParam = new icon (hIcon if TDF_USE_HICON_* was set, PCWSTR otherwise)
            SendMessage(handle, TDM_UPDATE_ICON,
                TDIE_ICON_FOOTER, icon);
        }

        /// <summary>
        /// Updates the footer icon. Note the type (standard via enum or
        /// custom via Icon type) must be used when upating the icon.
        /// </summary>
        /// <param name="icon">The icon to set.</param>
        public static void UpdateFooterIcon(this TaskDialogNotificationArgs args, Icon icon)
        {
            var handle = args.GetHWND();

            // TDM_UPDATE_ICON = WM_USER+116  // wParam = icon element (TASKDIALOG_ICON_ELEMENTS), lParam = new icon (hIcon if TDF_USE_HICON_* was set, PCWSTR otherwise)
            SendMessage(handle, TDM_UPDATE_ICON,
                TDIE_ICON_FOOTER, icon?.Handle ?? IntPtr.Zero);
        }

        /// <summary>
        /// Updates the main instruction icon. Note the type (standard via enum or
        /// custom via Icon type) must be used when upating the icon.
        /// </summary>
        /// <param name="icon">Task Dialog standard icon.</param>
        public static void UpdateMainIcon(this TaskDialogNotificationArgs args, TaskDialogIcon icon)
        {
            var handle = args.GetHWND();

            // TDM_UPDATE_ICON = WM_USER+116  // wParam = icon element (TASKDIALOG_ICON_ELEMENTS), lParam = new icon (hIcon if TDF_USE_HICON_* was set, PCWSTR otherwise)
            SendMessage(handle, TDM_UPDATE_ICON,
                TDIE_ICON_MAIN, (int)icon);
        }

        /// <summary>
        /// Updates the main instruction icon. Note the type (standard via enum or
        /// custom via Icon type) must be used when upating the icon.
        /// </summary>
        /// <param name="icon">The icon to set.</param>
        public static void UpdateMainIcon(this TaskDialogNotificationArgs args, Icon icon)
        {
            var handle = args.GetHWND();

            // TDM_UPDATE_ICON = WM_USER+116  // wParam = icon element (TASKDIALOG_ICON_ELEMENTS), lParam = new icon (hIcon if TDF_USE_HICON_* was set, PCWSTR otherwise)
            SendMessage(handle, TDM_UPDATE_ICON,
                TDIE_ICON_MAIN, icon?.Handle ?? IntPtr.Zero);
        }

        /// <summary>
        /// Updates the Main Instruction.
        /// </summary>
        /// <param name="mainInstruction">The new value.</param>
        public static void UpdateMainInstruction(this TaskDialogNotificationArgs args, string mainInstruction)
        {
            var handle = args.GetHWND();

            // TDE_MAIN_INSTRUCTION
            // TDM_UPDATE_ELEMENT_TEXT             = WM_USER+114, // wParam = element (TASKDIALOG_ELEMENTS), lParam = new element text (LPCWSTR)
            SendMessage(handle, TDM_UPDATE_ELEMENT_TEXT,
                TDE_MAIN_INSTRUCTION, mainInstruction);
        }

        private static HWND GetHWND(this TaskDialogNotificationArgs args) => args.TaskDialog is TaskDialogData data ? data.HWND : throw new ArgumentException(nameof(args));

        private unsafe static LRESULT SendMessage(HWND handle, TASKDIALOG_MESSAGES Msg, nuint wParam, nint lParam)
            => PInvoke.SendMessage(handle, (uint)Msg, (WPARAM)wParam, (LPARAM)lParam);

        private unsafe static LRESULT SendMessage(HWND handle, TASKDIALOG_MESSAGES Msg, TASKDIALOG_ELEMENTS wParam, string lParam)
            => PInvoke.SendMessage(handle, (uint)Msg, (WPARAM)(nuint)wParam, lParam);

        private unsafe static LRESULT SendMessage(HWND handle, TASKDIALOG_MESSAGES Msg, TaskDialogProgressBarState wParam, LPARAM lParam)
            => PInvoke.SendMessage(handle, (uint)Msg, (WPARAM)(nuint)wParam, lParam);

        private unsafe static LRESULT SendMessage(HWND handle, TASKDIALOG_MESSAGES Msg, TASKDIALOG_ICON_ELEMENTS wParam, nint lParam)
            => PInvoke.SendMessage(handle, (uint)Msg, (WPARAM)(nuint)wParam, (LPARAM)lParam);

        private unsafe static LRESULT SendMessage(HWND handle, TASKDIALOG_MESSAGES Msg, TASKDIALOG_ICON_ELEMENTS wParam, TaskDialogIcon lParam)
            => SendMessage(handle, Msg, wParam, (LPARAM)(nint)lParam);
    }
}
