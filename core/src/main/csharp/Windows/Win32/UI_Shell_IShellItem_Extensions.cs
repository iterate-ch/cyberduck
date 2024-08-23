using System;
using Windows.Win32.System.Com;
using Windows.Win32.UI.Shell;

namespace Windows.Win32;

public static partial class UI_Shell_IShellItem_Extensions
{
    /// <inheritdoc cref="IShellItem.BindToHandler(IBindCtx, Guid*, Guid*, out object)"/>
    public static unsafe void BindToHandler<T>(this IShellItem @this, IBindCtx pbc, in Guid bhid, out T ppv) where T : class
    {
        Guid riid = typeof(T).GUID;
        fixed (Guid* bhidLocal = &bhid)
        {
            @this.BindToHandler(pbc, bhidLocal, &riid, out var ppvLocal);
            ppv = (T)ppvLocal;
        }
    }

}
