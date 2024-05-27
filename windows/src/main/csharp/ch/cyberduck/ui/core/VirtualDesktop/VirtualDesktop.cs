using System;
using System.Windows.Forms;
using Windows.Win32;
using Windows.Win32.Foundation;
using Windows.Win32.UI.Shell;

namespace Ch.Cyberduck.Ui.Core.VirtualDesktop
{
    internal static class DesktopManager
    {
        private static readonly IVirtualDesktopManager desktopmanager;

        static DesktopManager()
        {
            desktopmanager = (IVirtualDesktopManager)new VirtualDesktopManager();
        }

        public static Guid GetWindowDesktopId(IWin32Window window) => desktopmanager.GetWindowDesktopId((HWND)window.Handle);

        public static bool IsWindowOnCurrentVirtualDesktop(IWin32Window window) => desktopmanager.IsWindowOnCurrentVirtualDesktop((HWND)window.Handle);

        public static void MoveWindowToDesktop(IWin32Window window, in Guid desktop) => desktopmanager.MoveWindowToDesktop((HWND)window.Handle, desktop);
    }
}
