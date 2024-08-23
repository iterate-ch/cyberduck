using System;
using Windows.Win32.Foundation;
using Windows.Win32.UI.Shell;

namespace Windows.Win32;

public partial class UI_Shell_IVirtualDesktopManager_Extensions
{
    /// <inheritdoc cref="IVirtualDesktopManager.GetWindowDesktopId(HWND, Guid*)"/>
    public static unsafe Guid GetWindowDesktopId(this IVirtualDesktopManager @this, HWND topLevelWindow)
    {
        Guid desktopIdLocal = default;
        @this.GetWindowDesktopId(topLevelWindow, &desktopIdLocal);
        return desktopIdLocal;
    }

    /// <inheritdoc cref="IVirtualDesktopManager.IsWindowOnCurrentVirtualDesktop(HWND, BOOL*)"/>
    public static unsafe BOOL IsWindowOnCurrentVirtualDesktop(this IVirtualDesktopManager @this, HWND topLevelWindow)
    {
        BOOL onCurrentDesktopLocal = default;
        @this.IsWindowOnCurrentVirtualDesktop(topLevelWindow, &onCurrentDesktopLocal);
        return onCurrentDesktopLocal;
    }
}
