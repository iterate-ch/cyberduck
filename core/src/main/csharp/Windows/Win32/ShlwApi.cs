using System;
using System.Runtime.InteropServices;
using Windows.Win32.Foundation;

namespace Windows.Win32
{
    partial class CorePInvoke
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
