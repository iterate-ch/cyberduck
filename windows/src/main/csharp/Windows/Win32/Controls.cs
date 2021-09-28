using Windows.Win32.Foundation;

namespace Windows.Win32
{
    using static Constants;
    using static CyberduckCorePInvoke;

    partial class PInvoke
    {
        public static unsafe LRESULT Edit_SetCueBannerText(nint hwnd, string text)
           => SendMessage(hwnd, EM_SETCUEBANNER, default, text);
    }
}
