using Windows.Win32.Foundation;
using Windows.Win32.UI.Shell;

namespace Windows.Win32
{
    public static partial class UI_Shell_IAssocHandler_Extensions
    {
        /// <inheritdoc cref="IAssocHandler.GetIconLocation(Foundation.PWSTR*, out int)"/>
        public static unsafe string GetIconLocation(this IAssocHandler @this, out int pIndex)
        {
            PWSTR ppszLocal;
            @this.GetIconLocation(&ppszLocal, out pIndex);
            return ppszLocal.ToString();
        }

        /// <inheritdoc cref="IAssocHandler.GetName(PWSTR*)"/>
        public static unsafe string GetName(this IAssocHandler @this)
        {
            PWSTR ppszLocal;
            @this.GetName(&ppszLocal);
            return ppszLocal.ToString();
        }

        /// <inheritdoc cref="IAssocHandler.GetUIName(PWSTR*)"/>
        public static unsafe string GetUIName(this IAssocHandler @this)
        {
            PWSTR ppszLocal;
            @this.GetUIName(&ppszLocal);
            return ppszLocal.ToString();
        }
    }
}
