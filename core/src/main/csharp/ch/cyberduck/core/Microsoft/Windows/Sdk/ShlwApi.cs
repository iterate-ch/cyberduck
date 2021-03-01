using System;

namespace Ch.Cyberduck.Core.Microsoft.Windows.Sdk
{
    partial class PInvoke
    {
        /// <inheritdoc cref = "AssocCreate(Guid, Guid*, void **)"/>
        public static unsafe HRESULT AssocCreate<T>(Guid clsid, out T* ppv)
            where T : unmanaged
        {
            var riid = typeof(T).GUID;
            HRESULT __result = AssocCreate(clsid, riid, out var ppvLocal);
            ppv = (T*)ppvLocal;
            return __result;
        }
    }
}
