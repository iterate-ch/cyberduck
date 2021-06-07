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
    }
}
