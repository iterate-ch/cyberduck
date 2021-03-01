using System;

namespace Ch.Cyberduck.Core.Microsoft.Windows.Sdk
{
    public partial struct HICON : IDisposable
    {
        public void Dispose()
        {
            PInvoke.DestroyIcon(this);
        }
    }

    partial class PInvoke
    {
        public static LRESULT SendMessage(nint hWnd, uint Msg, WPARAM wParam, LPARAM lParam) => SendMessage((HWND)hWnd, Msg, wParam, lParam);

        public static LRESULT SendMessage(nint hWnd, uint Msg, nuint wParam, nint lParam)
        {
            return SendMessage(hWnd, Msg, (WPARAM)wParam, (LPARAM)lParam);
        }

        public static LRESULT SendMessage(HWND hWnd, uint Msg, nuint wParam, nint lParam)
        {
            return SendMessage(hWnd, Msg, (WPARAM)wParam, (LPARAM)lParam);
        }

        public static LRESULT SendMessage(nint hWnd, uint Msg, nuint wParam, string lParam)
        {
            return SendMessage(hWnd, Msg, (WPARAM)wParam, lParam);
        }

        public static LRESULT SendMessage(HWND hWnd, uint Msg, nuint wParam, string lParam)
        {
            return SendMessage(hWnd, Msg, (WPARAM)wParam, lParam);
        }

        public static unsafe LRESULT SendMessage(nint hWnd, uint Msg, WPARAM wParam, string lParam)
        {
            fixed (char* lParamLocal = lParam)
            {
                return SendMessage(hWnd, Msg, wParam, (nint)lParamLocal);
            }
        }
        public static unsafe LRESULT SendMessage(HWND hWnd, uint Msg, WPARAM wParam, string lParam)
        {
            fixed (char* lParamLocal = lParam)
            {
                return SendMessage(hWnd, Msg, wParam, (nint)lParamLocal);
            }
        }
    }
}
