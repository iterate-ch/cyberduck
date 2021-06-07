using System;

namespace Ch.Cyberduck.Ui.Microsoft.Windows.Sdk
{
    unsafe partial class PInvoke
    {
        /// <inheritdoc cref="CoCreateInstance(System.Guid*, IUnknown*, uint, System.Guid*, void**)"/>
        /// <seealso href="https://github.com/microsoft/CsWin32/issues/103" />
        /// <seealso href="https://github.com/microsoft/CsWin32/pull/201" />
        public static unsafe HRESULT CoCreateInstance<T>(in Guid rclsid, object pUnkOuter, uint dwClsContext, out T ppv)
        {
            HRESULT hr = CoCreateInstance(rclsid, pUnkOuter, dwClsContext, typeof(T).GUID, out object o);
            ppv = (T)o;
            return hr;
        }
    }
}
