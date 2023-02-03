using Windows.Win32.Foundation;

namespace Windows.Win32
{
    public partial class CorePInvoke
    {
        /// <inheritdoc cref="SendMessage(HWND, uint, WPARAM, LPARAM)"/>
        public static unsafe LRESULT SendMessage(HWND hWnd, uint Msg, WPARAM wParam, in string lParam)
            => SendMessage(hWnd, Msg, wParam, (PCWSTR)lParam);

        /// <inheritdoc cref="SendMessage(HWND, uint, WPARAM, LPARAM)"/>
        public static unsafe LRESULT SendMessage(HWND hWnd, uint Msg, WPARAM wParam, PCWSTR lParam)
            => SendMessage(hWnd, Msg, wParam, (nint)lParam.Value);
    }
}
