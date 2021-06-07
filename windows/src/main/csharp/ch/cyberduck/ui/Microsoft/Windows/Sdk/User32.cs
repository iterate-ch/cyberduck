using System.Runtime.CompilerServices;

namespace Ch.Cyberduck.Ui.Microsoft.Windows.Sdk
{
    partial class PInvoke
    {
        public unsafe static void GetWindowThreadProcessId(nint hwnd, out int processId)
        {
            uint result;
            GetWindowThreadProcessId((HWND)hwnd, &result);
            processId = (int)result;
        }

        public static bool HideCaret(nint hWnd) => HideCaret((HWND)hWnd);

        public static unsafe bool SetMenuInfo(HMENU param0, in MENUINFO param1)
            => SetMenuInfo(param0, (MENUINFO*)Unsafe.AsPointer(ref Unsafe.AsRef(param1)));

        public static unsafe bool SetMenuItemInfo(HMENU hmenu, int item, bool fByPositon, in MENUITEMINFOW lpmii)
           => SetMenuItemInfo(hmenu, (uint)item, fByPositon, (MENUITEMINFOW*)Unsafe.AsPointer(ref Unsafe.AsRef(lpmii)));
    }
}
