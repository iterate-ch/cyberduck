using System;
using System.Runtime.InteropServices;

namespace Windows.Win32
{
    using Foundation;

    partial class CyberduckCorePInvoke
    {
        /// <inheritdoc cref = "AssocCreate(Guid, Guid*, void **)"/>
        public static unsafe HRESULT AssocCreate<T>(Guid clsid, out T ppv)
        {
            var riid = typeof(T).GUID;
            HRESULT __result = AssocCreate(clsid, riid, out var ppvLocal);
            ppv = (T)Marshal.GetObjectForIUnknown((IntPtr)ppvLocal);
            return __result;
        }
    }
}
