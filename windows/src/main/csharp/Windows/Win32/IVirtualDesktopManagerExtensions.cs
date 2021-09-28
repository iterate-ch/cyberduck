using System;

namespace Windows.Win32.UI.Shell
{
    using Foundation;

    public static partial class FriendlyOverloadExtensions
    {
        public static Guid GetWindowDesktopId(this IVirtualDesktopManager @this, nint hwnd)
        {
            @this.GetWindowDesktopId((HWND)hwnd, out var desktopId);
            return desktopId;
        }

        public static bool IsWindowOnCurrentVirtualDesktop(this IVirtualDesktopManager @this, nint hwnd)
        {
            @this.IsWindowOnCurrentVirtualDesktop((HWND)hwnd, out var onCurrentDesktop);
            return onCurrentDesktop;
        }

        public static void MoveWindowToDesktop(this IVirtualDesktopManager @this, nint hwnd, in Guid target) => @this.MoveWindowToDesktop((HWND)hwnd, target);
    }
}
