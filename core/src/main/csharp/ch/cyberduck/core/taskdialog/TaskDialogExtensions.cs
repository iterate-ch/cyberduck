using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Ch.Cyberduck.Core.TaskDialog
{
    public static class TaskDialogExtensions
    {
        private static readonly IntPtr HWND_TOPMOST = new IntPtr(-1);

        public static void MakeTopMost(this IActiveTaskDialog taskDialog)
        {
            if (!(taskDialog is ActiveTaskDialog activeTaskDialog)) return;
            // https://docs.microsoft.com/en-us/windows/win32/api/winuser/nf-winuser-setwindowpos
            // HWND_TOPMOST (HWND) - 1
            // Places the window above all non-topmost windows.The window maintains its topmost position even when it is deactivated.

           NativeMethods.SetWindowPos(
                activeTaskDialog.Handle, HWND_TOPMOST,
                0, 0, 0, 0,
                Win32Constants.SWP_NOSIZE | Win32Constants.SWP_NOMOVE);
        }
    }
}
