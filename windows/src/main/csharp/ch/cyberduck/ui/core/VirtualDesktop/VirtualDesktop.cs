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

        public static Guid GetWindowDesktopId(Form form) => desktopmanager.GetWindowDesktopId((HWND)form.Handle);

        public static bool IsWindowOnCurrentVirtualDesktop(Form form) => desktopmanager.IsWindowOnCurrentVirtualDesktop((HWND)form.Handle);

        public static void MoveWindowToDesktop(Form form, in Guid desktop) => desktopmanager.MoveWindowToDesktop((HWND)form.Handle, desktop);
    }
}
