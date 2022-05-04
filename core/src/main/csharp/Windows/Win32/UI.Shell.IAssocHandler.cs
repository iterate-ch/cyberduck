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
        [Guid("F04061AC-1659-4A3F-A954-775AA57FC083"), InterfaceType(ComInterfaceType.InterfaceIsIUnknown), ComImport()]
        public interface IAssocHandler
        {
            /// <summary>Retrieves the full path and file name of the executable file associated with the file type.</summary>
            /// <param name="ppsz">
            /// <para>Type: <b>LPWSTR*</b> When this method returns, contains the address of a pointer to a null-terminated, Unicode string that contains the full path of the file, including the file name.</para>
            /// <para><see href="https://docs.microsoft.com/windows/win32/api//shobjidl_core/nf-shobjidl_core-iassochandler-getname#parameters">Read more on docs.microsoft.com</see>.</para>
            /// </param>
            /// <returns>
            /// <para>Type: <b>HRESULT</b> If this method succeeds, it returns <b xmlns:loc="http://microsoft.com/wdcml/l10n">S_OK</b>. Otherwise, it returns an <b xmlns:loc="http://microsoft.com/wdcml/l10n">HRESULT</b> error code.</para>
            /// </returns>
            /// <remarks>
            /// <para><see href="https://docs.microsoft.com/windows/win32/api//shobjidl_core/nf-shobjidl_core-iassochandler-getname">Learn more about this API from docs.microsoft.com</see>.</para>
            /// </remarks>
            unsafe void GetName(winmdroot.Foundation.PWSTR* ppsz);

            /// <summary>Retrieves the display name of an application.</summary>
            /// <param name="ppsz">
            /// <para>Type: <b>LPWSTR*</b> When this method returns, contains the address of a pointer to a null-terminated, Unicode string that contains the display name of the application.</para>
            /// <para><see href="https://docs.microsoft.com/windows/win32/api//shobjidl_core/nf-shobjidl_core-iassochandler-getuiname#parameters">Read more on docs.microsoft.com</see>.</para>
            /// </param>
            /// <returns>
            /// <para>Type: <b>HRESULT</b> If this method succeeds, it returns <b xmlns:loc="http://microsoft.com/wdcml/l10n">S_OK</b>. Otherwise, it returns an <b xmlns:loc="http://microsoft.com/wdcml/l10n">HRESULT</b> error code.</para>
            /// </returns>
            /// <remarks>
            /// <para><see href="https://docs.microsoft.com/windows/win32/api//shobjidl_core/nf-shobjidl_core-iassochandler-getuiname">Learn more about this API from docs.microsoft.com</see>.</para>
            /// </remarks>
            unsafe void GetUIName(winmdroot.Foundation.PWSTR* ppsz);

            /// <summary>Retrieves the location of the icon associated with the application.</summary>
            /// <param name="ppszPath">
            /// <para>Type: <b>LPWSTR*</b> When this method returns, contains the address of a pointer to a null-terminated, Unicode string that contains the path to the application's icon.</para>
            /// <para><see href="https://docs.microsoft.com/windows/win32/api//shobjidl_core/nf-shobjidl_core-iassochandler-geticonlocation#parameters">Read more on docs.microsoft.com</see>.</para>
            /// </param>
            /// <param name="pIndex">
            /// <para>Type: <b>int*</b> When this method returns, contains a pointer to the index of the icon within the resource named in <i>ppszPath</i>.</para>
            /// <para><see href="https://docs.microsoft.com/windows/win32/api//shobjidl_core/nf-shobjidl_core-iassochandler-geticonlocation#parameters">Read more on docs.microsoft.com</see>.</para>
            /// </param>
            /// <returns>
            /// <para>Type: <b>HRESULT</b> If this method succeeds, it returns <b xmlns:loc="http://microsoft.com/wdcml/l10n">S_OK</b>. Otherwise, it returns an <b xmlns:loc="http://microsoft.com/wdcml/l10n">HRESULT</b> error code.</para>
            /// </returns>
            /// <remarks>
            /// <para><see href="https://docs.microsoft.com/windows/win32/api//shobjidl_core/nf-shobjidl_core-iassochandler-geticonlocation">Learn more about this API from docs.microsoft.com</see>.</para>
            /// </remarks>
            unsafe void GetIconLocation(winmdroot.Foundation.PWSTR* ppszPath, int* pIndex);

            /// <summary>Indicates whether the application is registered as a recommended handler for the queried file type.</summary>
            /// <returns>
            /// <para>Type: <b>HRESULT</b> Returns S_OK if the program is recommended; otherwise, S_FALSE.</para>
            /// </returns>
            /// <remarks>
            /// <para><see href="https://docs.microsoft.com/windows/win32/api//shobjidl_core/nf-shobjidl_core-iassochandler-isrecommended">Learn more about this API from docs.microsoft.com</see>.</para>
            /// </remarks>
            [PreserveSig]
            winmdroot.Foundation.HRESULT IsRecommended();

            /// <summary>Sets an application as the default application for this file type.</summary>
            /// <param name="pszDescription">
            /// <para>Type: <b>LPCWSTR</b> A pointer to a null-terminated, Unicode string that contains the display name of the application.</para>
            /// <para><see href="https://docs.microsoft.com/windows/win32/api//shobjidl_core/nf-shobjidl_core-iassochandler-makedefault#parameters">Read more on docs.microsoft.com</see>.</para>
            /// </param>
            /// <returns>
            /// <para>Type: <b>HRESULT</b> If this method succeeds, it returns <b xmlns:loc="http://microsoft.com/wdcml/l10n">S_OK</b>. Otherwise, it returns an <b xmlns:loc="http://microsoft.com/wdcml/l10n">HRESULT</b> error code.</para>
            /// </returns>
            /// <remarks>
            /// <para><see href="https://docs.microsoft.com/windows/win32/api//shobjidl_core/nf-shobjidl_core-iassochandler-makedefault">Learn more about this API from docs.microsoft.com</see>.</para>
            /// </remarks>
            void MakeDefault(winmdroot.Foundation.PCWSTR pszDescription);

            /// <summary>Directly invokes the associated handler.</summary>
            /// <param name="pdo">
            /// <para>Type: <b><a href="https://docs.microsoft.com/windows/desktop/api/objidl/nn-objidl-idataobject">IDataObject</a>*</b> A pointer to an <a href="https://docs.microsoft.com/windows/desktop/api/objidl/nn-objidl-idataobject">IDataObject</a> that represents the selected item on which to invoke the handler. Note that you should not call <b>IAssocHandler::Invoke</b> with a selection of multiple items. If you have multiple items, call <a href="https://docs.microsoft.com/windows/desktop/api/shobjidl_core/nf-shobjidl_core-iassochandler-createinvoker">IAssocHandler::CreateInvoker</a> instead. See Remarks for more details.</para>
            /// <para><see href="https://docs.microsoft.com/windows/win32/api//shobjidl_core/nf-shobjidl_core-iassochandler-invoke#parameters">Read more on docs.microsoft.com</see>.</para>
            /// </param>
            /// <returns>
            /// <para>Type: <b>HRESULT</b> If this method succeeds, it returns <b xmlns:loc="http://microsoft.com/wdcml/l10n">S_OK</b>. Otherwise, it returns an <b xmlns:loc="http://microsoft.com/wdcml/l10n">HRESULT</b> error code.</para>
            /// </returns>
            /// <remarks>
            /// <para><see href="https://docs.microsoft.com/windows/win32/api//shobjidl_core/nf-shobjidl_core-iassochandler-invoke">Learn more about this API from docs.microsoft.com</see>.</para>
            /// </remarks>
            void Invoke(winmdroot.System.Com.IDataObject pdo);

            /// <summary>Retrieves an object that enables the invocation of the associated handler on the current selection. The invoker includes the ability to verify whether the current selection is supported.</summary>
            /// <param name="pdo">
            /// <para>Type: <b><a href="https://docs.microsoft.com/windows/desktop/api/objidl/nn-objidl-idataobject">IDataObject</a>*</b> A pointer to an <a href="https://docs.microsoft.com/windows/desktop/api/objidl/nn-objidl-idataobject">IDataObject</a> that represents the selected item or items on which to invoke the handler. Note that if you have only a single item, <a href="https://docs.microsoft.com/windows/desktop/api/shobjidl_core/nf-shobjidl_core-iassochandler-invoke">IAssocHandler::Invoke</a> could be the better choice. See Remarks for more details.</para>
            /// <para><see href="https://docs.microsoft.com/windows/win32/api//shobjidl_core/nf-shobjidl_core-iassochandler-createinvoker#parameters">Read more on docs.microsoft.com</see>.</para>
            /// </param>
            /// <param name="ppInvoker">
            /// <para>Type: <b><a href="https://docs.microsoft.com/windows/desktop/api/shobjidl_core/nn-shobjidl_core-iassochandlerinvoker">IAssocHandlerInvoker</a>**</b> When this method returns, contains the address of a pointer to an <a href="https://docs.microsoft.com/windows/desktop/api/shobjidl_core/nn-shobjidl_core-iassochandlerinvoker">IAssocHandlerInvoker</a> object. This object is used to invoke the menu item after ensuring that the selected items are supported by the associated handler.</para>
            /// <para><see href="https://docs.microsoft.com/windows/win32/api//shobjidl_core/nf-shobjidl_core-iassochandler-createinvoker#parameters">Read more on docs.microsoft.com</see>.</para>
            /// </param>
            /// <returns>
            /// <para>Type: <b>HRESULT</b> If this method succeeds, it returns <b xmlns:loc="http://microsoft.com/wdcml/l10n">S_OK</b>. Otherwise, it returns an <b xmlns:loc="http://microsoft.com/wdcml/l10n">HRESULT</b> error code.</para>
            /// </returns>
            /// <remarks>
            /// <para><see href="https://docs.microsoft.com/windows/win32/api//shobjidl_core/nf-shobjidl_core-iassochandler-createinvoker">Learn more about this API from docs.microsoft.com</see>.</para>
            /// </remarks>
            void CreateInvoker(winmdroot.System.Com.IDataObject pdo, out winmdroot.UI.Shell.IAssocHandlerInvoker ppInvoker);
        }
    }
}