using System;

namespace Ch.Cyberduck.Ui.Microsoft.Windows.Sdk
{
    partial class Constants
    {
        public static readonly Guid CLSID_DropTargetHelper = new Guid(0x4657278a, 0x411b, 0x11d2, 0x83, 0x9a, 0x0, 0xc0, 0x4f, 0xd9, 0x18, 0xd0);
    }

    unsafe partial class PInvoke
    {
        /// <inheritdoc cref="CoCreateInstance(System.Guid*, IUnknown*, uint, System.Guid*, void**)"/>
        /// <seealso href="https://github.com/microsoft/CsWin32/issues/103" />
        public static unsafe HRESULT CoCreateInstance<T>(in Guid rclsid, IUnknown* pUnkOuter, uint dwClsContext, out T* ppv)
            where T : unmanaged
        {
            HRESULT hr = CoCreateInstance(rclsid, pUnkOuter, dwClsContext, typeof(T).GUID, out void* o);
            ppv = (T*)o;
            return hr;
        }
    }
}
