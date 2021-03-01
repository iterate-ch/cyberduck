using Ch.Cyberduck.Core.Microsoft.Windows.Sdk;
using static Ch.Cyberduck.Core.Microsoft.Windows.Sdk.PInvoke;

namespace Ch.Cyberduck.Ui.Microsoft.Windows.Sdk
{
    using static Constants;

    partial class PInvoke
    {
        public static unsafe LRESULT Edit_SetCueBannerText(nint hwnd, string text)
        {
            return SendMessage(hwnd, EM_SETCUEBANNER, 0, text);
        }
    }
}
