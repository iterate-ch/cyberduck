using Windows.Win32.Foundation;
using Windows.Win32.UI.WindowsAndMessaging;

namespace Windows.Win32;

public partial class PInvoke
{
    /// <inheritdoc cref="SetMenuInfo(HMENU, MENUINFO*)"/>
    public static unsafe BOOL SetMenuInfo(HMENU param0, in MENUINFO param1)
    {
        fixed (MENUINFO* param1Local = &param1)
        {
            BOOL __result = SetMenuInfo(param0, param1Local);
            return __result;
        }
    }

    /// <inheritdoc cref="SetMenuItemInfo(HMENU, uint, BOOL, MENUITEMINFOW*)"/>
    public static unsafe BOOL SetMenuItemInfo(HMENU hmenu, uint item, BOOL fByPositon, in MENUITEMINFOW lpmii)
    {
        fixed (MENUITEMINFOW* lpmiiLocal = &lpmii)
        {
            BOOL __result = SetMenuItemInfo(hmenu, item, fByPositon, lpmiiLocal);
            return __result;
        }
    }
}
