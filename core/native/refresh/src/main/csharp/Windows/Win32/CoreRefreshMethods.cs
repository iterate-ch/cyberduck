#pragma warning disable CS1591,CS1573,CS0465,CS0649,CS8019,CS1570,CS1584,CS1658,CS0436
namespace Windows.Win32
{
    using global::System;
    using global::System.Diagnostics;
    using global::System.Runtime.CompilerServices;
    using global::System.Runtime.InteropServices;
    using winmdroot = global::Windows.Win32;

    public partial class CoreRefreshMethods
    {
        /// <inheritdoc cref="SHGetImageList(int, global::System.Guid*, void**)"/>
        public static unsafe Foundation.HRESULT SHGetImageList<T>(int iImageList, out T ppvObj)
        {
            Guid riid = typeof(T).GUID;
            void* ppvObjLocal;
            winmdroot.Foundation.HRESULT __result = SHGetImageList(iImageList, &riid, &ppvObjLocal);
            ppvObj = (T)Marshal.GetObjectForIUnknown((IntPtr)ppvObjLocal);
            return __result;
        }
    }
}
