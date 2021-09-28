using System;
using System.Runtime.CompilerServices;
using System.Runtime.InteropServices;

namespace Windows.Win32
{
    using Foundation;

    partial class CyberduckCorePInvoke
    {
        public static LRESULT SendMessage(nint hWnd, uint Msg, nuint wParam, nint lParam)
            => SendMessage(hWnd, Msg, (WPARAM)wParam, (LPARAM)lParam);

        public static LRESULT SendMessage(nint hWnd, uint Msg, WPARAM wParam, LPARAM lParam)
            => SendMessage((HWND)hWnd, Msg, wParam, lParam);

        public static LRESULT SendMessage(nint hWnd, uint Msg, WPARAM wParam, string lParam)
            => SendMessage((HWND)hWnd, Msg, wParam, lParam);

        public static unsafe LRESULT SendMessage(HWND hWnd, uint Msg, WPARAM wParam, string lParam)
            => SendMessage(hWnd, Msg, wParam, (LPARAM)(nint)Unsafe.AsPointer(ref MemoryMarshal.GetReference(lParam.AsSpan())));
    }
}
