using System.Windows.Interop;

namespace System.Windows;

public static class WindowExtensions
{
    public static bool? ShowWithOwnerDialog(this Window window, in nint? owner)
    {
        if (owner is nint ownerLocal)
        {
            _ = new WindowInteropHelper(window)
            {
                Owner = ownerLocal
            };
            window.WindowStartupLocation = WindowStartupLocation.CenterOwner;
        }

        return window.ShowDialog();
    }
}
