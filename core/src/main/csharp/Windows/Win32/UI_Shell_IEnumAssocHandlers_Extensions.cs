using Windows.Win32.UI.Shell;

namespace Windows.Win32;

public static partial class UI_Shell_IEnumAssocHandlers_Extensions
{
    /// <inheritdoc cref="IEnumAssocHandlers.Next(uint, IAssocHandler[], uint*)"/>
    public static unsafe int Next(this IEnumAssocHandlers @this, IAssocHandler[] rgelt)
    {
        uint pceltFetched = 0;
        @this.Next((uint)rgelt.Length, rgelt, &pceltFetched);
        return (int)pceltFetched;
    }
}
