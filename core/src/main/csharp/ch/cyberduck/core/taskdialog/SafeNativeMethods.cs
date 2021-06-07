using Ch.Cyberduck.Core.Microsoft.Windows.Sdk;
using static Ch.Cyberduck.Core.Microsoft.Windows.Sdk.GetWindowLongPtr_nIndex;
using static Ch.Cyberduck.Core.Microsoft.Windows.Sdk.PInvoke;
using static Ch.Cyberduck.Core.Microsoft.Windows.Sdk.WINDOWS_EX_STYLE;
using static Ch.Cyberduck.Core.Microsoft.Windows.Sdk.WINDOWS_STYLE;
using static Ch.Cyberduck.Core.Microsoft.Windows.Sdk.SetWindowPos_uFlags;

namespace Ch.Cyberduck.Core.TaskDialog
{
    /// <summary>
    /// Provides safe Win32 API wrapper calls for various actions not directly
    /// supported by WPF classes out of the box.
    /// </summary>
    internal class SafeNativeMethods
    {
        /// <summary>
        /// Sets the window's close button visibility.
        /// </summary>
        /// <param name="handle">The window to set.</param>
        /// <param name="showCloseButton"><c>true</c> to show the close button; otherwise, <c>false</c></param>
        public static void SetWindowCloseButtonVisibility(HWND handle, bool showCloseButton)
        {
            int style = GetWindowLong(handle, GWL_STYLE);

            if (showCloseButton)
                SetWindowLong(handle, GWL_STYLE, style & (int)WS_SYSMENU);
            else
                SetWindowLong(handle, GWL_STYLE, style & ~(int)WS_SYSMENU);
        }

        /// <summary>
        /// Sets the window's icon visibility.
        /// </summary>
        /// <param name="window">The window to set.</param>
        /// <param name="showIcon"><c>true</c> to show the icon in the caption; otherwise, <c>false</c></param>
        public static void SetWindowIconVisibility(HWND handle, bool showIcon)
        {
            // Change the extended window style
            int extendedStyle = GetWindowLong(handle, GWL_EXSTYLE);
            if (showIcon)
            {
                SetWindowLong(handle, GWL_EXSTYLE, extendedStyle | ~(int)WS_EX_DLGMODALFRAME);
            }
            else
            {
                SetWindowLong(handle, GWL_EXSTYLE, extendedStyle | (int)WS_EX_DLGMODALFRAME);
            }

            // Update the window's non-client area to reflect the changes
            SetWindowPos(handle, default, 0, 0, 0, 0,
                SWP_NOMOVE | SWP_NOSIZE | SWP_NOZORDER | SWP_FRAMECHANGED);
        }
    }
}
