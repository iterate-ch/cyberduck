#pragma warning disable IDE0001,IDE0002,IDE0005,CS1591,CS1573,CS0465,CS0649,CS8019,CS1570,CS1584,CS1658,CS0436

namespace Windows.Win32
{
    using global::System;
    using global::System.Diagnostics;
    using global::System.Runtime.CompilerServices;
    using global::System.Runtime.InteropServices;
    using winmdroot = global::Windows.Win32;

    namespace UI.Shell
    {
        [Guid("92218CAB-ECAA-4335-8133-807FD234C2EE"), InterfaceType(ComInterfaceType.InterfaceIsIUnknown), ComImport()]
        public interface IAssocHandlerInvoker
        {
            /// <summary>Determines whether an invoker supports its selection.</summary>
            /// <returns>
            /// <para>Type: <b>HRESULT</b> Returns <b>S_OK</b> if this instance supports its selection, or <b>S_FALSE</b> otherwise.</para>
            /// </returns>
            /// <remarks>
            /// <para><see href="https://docs.microsoft.com/windows/win32/api//shobjidl_core/nf-shobjidl_core-iassochandlerinvoker-supportsselection">Learn more about this API from docs.microsoft.com</see>.</para>
            /// </remarks>
            [PreserveSig]
            winmdroot.Foundation.HRESULT SupportsSelection();

            /// <summary>Invokes an associated application handler.</summary>
            /// <returns>
            /// <para>Type: <b>HRESULT</b> If this method succeeds, it returns <b xmlns:loc="http://microsoft.com/wdcml/l10n">S_OK</b>. Otherwise, it returns an <b xmlns:loc="http://microsoft.com/wdcml/l10n">HRESULT</b> error code.</para>
            /// </returns>
            /// <remarks>
            /// <para><see href="https://docs.microsoft.com/windows/win32/api//shobjidl_core/nf-shobjidl_core-iassochandlerinvoker-invoke">Learn more about this API from docs.microsoft.com</see>.</para>
            /// </remarks>
            void Invoke();
        }
    }
}
