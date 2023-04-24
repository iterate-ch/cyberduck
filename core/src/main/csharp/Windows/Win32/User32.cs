#pragma warning disable IDE0001,IDE0002,IDE0005,CS1591,CS1573,CS0465,CS0649,CS8019,CS1570,CS1584,CS1658,CS0436

namespace Windows.Win32
{
    using global::System;
    using global::System.Diagnostics;
    using global::System.Runtime.CompilerServices;
    using global::System.Runtime.InteropServices;
    using winmdroot = global::Windows.Win32;

    public partial class CorePInvoke
    {
        /// <inheritdoc cref="SendMessage(winmdroot.Foundation.HWND, uint, winmdroot.Foundation.WPARAM, winmdroot.Foundation.LPARAM)"/>
        public static unsafe winmdroot.Foundation.LRESULT SendMessage(winmdroot.Foundation.HWND hWnd, uint Msg, winmdroot.Foundation.WPARAM wParam, in string lParam)
            => SendMessage(hWnd, Msg, wParam, (winmdroot.Foundation.PCWSTR)lParam);

        /// <inheritdoc cref="SendMessage(winmdroot.Foundation.HWND, uint, winmdroot.Foundation.WPARAM, winmdroot.Foundation.LPARAM)"/>
        public static unsafe winmdroot.Foundation.LRESULT SendMessage(winmdroot.Foundation.HWND hWnd, uint Msg, winmdroot.Foundation.WPARAM wParam, winmdroot.Foundation.PCWSTR lParam)
            => SendMessage(hWnd, Msg, wParam, (nint)lParam.Value);
    }
}
