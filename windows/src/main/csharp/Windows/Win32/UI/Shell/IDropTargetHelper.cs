#pragma warning disable IDE0001,IDE0005,CS1591,CS1573,CS0465,CS0649,CS8019,CS1570,CS1584,CS1658,CS0436

namespace Windows.Win32
{
    using global::System;
    using global::System.Diagnostics;
    using global::System.Runtime.CompilerServices;
    using global::System.Runtime.InteropServices;
    using winmdroot = global::Windows.Win32;

    namespace UI.Shell
    {
        [Guid("4657278B-411B-11D2-839A-00C04FD918D0"), InterfaceType(ComInterfaceType.InterfaceIsIUnknown), ComImport()]
        public interface IDropTargetHelper
        {
            /// <summary>Notifies the drag-image manager that the drop target's IDropTarget::DragEnter method has been called.</summary>
            /// <param name="hwndTarget">
            /// <para>Type: <b>HWND</b> The target's window handle.</para>
            /// <para><see href="https://docs.microsoft.com/windows/win32/api//shobjidl_core/nf-shobjidl_core-idroptargethelper-dragenter#parameters">Read more on docs.microsoft.com</see>.</para>
            /// </param>
            /// <param name="pDataObject">
            /// <para>Type: <b><a href="https://docs.microsoft.com/windows/desktop/api/objidl/nn-objidl-idataobject">IDataObject</a>*</b> A pointer to the data object's <a href="https://docs.microsoft.com/windows/desktop/api/objidl/nn-objidl-idataobject">IDataObject</a> interface.</para>
            /// <para><see href="https://docs.microsoft.com/windows/win32/api//shobjidl_core/nf-shobjidl_core-idroptargethelper-dragenter#parameters">Read more on docs.microsoft.com</see>.</para>
            /// </param>
            /// <param name="ppt">
            /// <para>Type: <b><a href="https://docs.microsoft.com/previous-versions/dd162805(v=vs.85)">POINT</a>*</b> The <a href="https://docs.microsoft.com/previous-versions/dd162805(v=vs.85)">POINT</a> structure pointer that was received in the <a href="https://docs.microsoft.com/windows/desktop/api/oleidl/nf-oleidl-idroptarget-dragenter">IDropTarget::DragEnter</a> method's <i>pt</i> parameter.</para>
            /// <para><see href="https://docs.microsoft.com/windows/win32/api//shobjidl_core/nf-shobjidl_core-idroptargethelper-dragenter#parameters">Read more on docs.microsoft.com</see>.</para>
            /// </param>
            /// <param name="dwEffect">
            /// <para>Type: <b>DWORD</b> The value pointed to by the <a href="https://docs.microsoft.com/windows/desktop/api/oleidl/nf-oleidl-idroptarget-dragenter">IDropTarget::DragEnter</a> method's <i>pdwEffect</i> parameter.</para>
            /// <para><see href="https://docs.microsoft.com/windows/win32/api//shobjidl_core/nf-shobjidl_core-idroptargethelper-dragenter#parameters">Read more on docs.microsoft.com</see>.</para>
            /// </param>
            /// <returns>
            /// <para>Type: <b>HRESULT</b> Returns S_OK if successful, or a COM error value otherwise.</para>
            /// </returns>
            /// <remarks>
            /// <para><see href="https://docs.microsoft.com/windows/win32/api//shobjidl_core/nf-shobjidl_core-idroptargethelper-dragenter">Learn more about this API from docs.microsoft.com</see>.</para>
            /// </remarks>
            unsafe void DragEnter(winmdroot.Foundation.HWND hwndTarget, global::System.Runtime.InteropServices.ComTypes.IDataObject pDataObject, winmdroot.Foundation.POINT* ppt, uint dwEffect);

            /// <summary>Notifies the drag-image manager that the drop target's IDropTarget::DragLeave method has been called.</summary>
            /// <returns>
            /// <para>Type: <b>HRESULT</b> Returns S_OK if successful, or a COM error value otherwise.</para>
            /// </returns>
            /// <remarks>
            /// <para><see href="https://docs.microsoft.com/windows/win32/api//shobjidl_core/nf-shobjidl_core-idroptargethelper-dragleave">Learn more about this API from docs.microsoft.com</see>.</para>
            /// </remarks>
            void DragLeave();

            /// <summary>Notifies the drag-image manager that the drop target's IDropTarget::DragOver method has been called.</summary>
            /// <param name="ppt">
            /// <para>Type: <b><a href="https://docs.microsoft.com/previous-versions/dd162805(v=vs.85)">POINT</a>*</b> The <a href="https://docs.microsoft.com/previous-versions/dd162805(v=vs.85)">POINT</a> structure pointer that was received in the <a href="https://docs.microsoft.com/windows/desktop/api/oleidl/nf-oleidl-idroptarget-dragover">IDropTarget::DragOver</a> method's <i>pt</i> parameter.</para>
            /// <para><see href="https://docs.microsoft.com/windows/win32/api//shobjidl_core/nf-shobjidl_core-idroptargethelper-dragover#parameters">Read more on docs.microsoft.com</see>.</para>
            /// </param>
            /// <param name="dwEffect">
            /// <para>Type: <b>DWORD</b> The value pointed to by the <a href="https://docs.microsoft.com/windows/desktop/api/oleidl/nf-oleidl-idroptarget-dragover">IDropTarget::DragOver</a> method's <i>pdwEffect</i> parameter.</para>
            /// <para><see href="https://docs.microsoft.com/windows/win32/api//shobjidl_core/nf-shobjidl_core-idroptargethelper-dragover#parameters">Read more on docs.microsoft.com</see>.</para>
            /// </param>
            /// <returns>
            /// <para>Type: <b>HRESULT</b> Returns S_OK if successful, or a COM error value otherwise.</para>
            /// </returns>
            /// <remarks>
            /// <para><see href="https://docs.microsoft.com/windows/win32/api//shobjidl_core/nf-shobjidl_core-idroptargethelper-dragover">Learn more about this API from docs.microsoft.com</see>.</para>
            /// </remarks>
            unsafe void DragOver(winmdroot.Foundation.POINT* ppt, uint dwEffect);

            /// <summary>Notifies the drag-image manager that the drop target's IDropTarget::Drop method has been called.</summary>
            /// <param name="pDataObject">
            /// <para>Type: <b><a href="https://docs.microsoft.com/windows/desktop/api/objidl/nn-objidl-idataobject">IDataObject</a>*</b> A pointer to the data object's <a href="https://docs.microsoft.com/windows/desktop/api/objidl/nn-objidl-idataobject">IDataObject</a> interface.</para>
            /// <para><see href="https://docs.microsoft.com/windows/win32/api//shobjidl_core/nf-shobjidl_core-idroptargethelper-drop#parameters">Read more on docs.microsoft.com</see>.</para>
            /// </param>
            /// <param name="ppt">
            /// <para>Type: <b><a href="https://docs.microsoft.com/previous-versions/dd162805(v=vs.85)">POINT</a>*</b> A <a href="https://docs.microsoft.com/previous-versions/dd162805(v=vs.85)">POINT</a> structure pointer that was received in the <a href="https://docs.microsoft.com/windows/desktop/api/oleidl/nf-oleidl-idroptarget-drop">IDropTarget::Drop</a> method's <i>pt</i> parameter.</para>
            /// <para><see href="https://docs.microsoft.com/windows/win32/api//shobjidl_core/nf-shobjidl_core-idroptargethelper-drop#parameters">Read more on docs.microsoft.com</see>.</para>
            /// </param>
            /// <param name="dwEffect">
            /// <para>Type: <b>DWORD</b> The value pointed to by the <a href="https://docs.microsoft.com/windows/desktop/api/oleidl/nf-oleidl-idroptarget-drop">IDropTarget::Drop</a> method's <i>pdwEffect</i> parameter.</para>
            /// <para><see href="https://docs.microsoft.com/windows/win32/api//shobjidl_core/nf-shobjidl_core-idroptargethelper-drop#parameters">Read more on docs.microsoft.com</see>.</para>
            /// </param>
            /// <returns>
            /// <para>Type: <b>HRESULT</b> Returns S_OK if successful, or a COM error value otherwise.</para>
            /// </returns>
            /// <remarks>
            /// <para><see href="https://docs.microsoft.com/windows/win32/api//shobjidl_core/nf-shobjidl_core-idroptargethelper-drop">Learn more about this API from docs.microsoft.com</see>.</para>
            /// </remarks>
            unsafe void Drop(global::System.Runtime.InteropServices.ComTypes.IDataObject pDataObject, winmdroot.Foundation.POINT* ppt, uint dwEffect);

            /// <summary>Notifies the drag-image manager to show or hide the drag image.</summary>
            /// <param name="fShow">
            /// <para>Type: <b>BOOL</b> A boolean value that is set to <b>TRUE</b> to show the drag image, and <b>FALSE</b> to hide it.</para>
            /// <para><see href="https://docs.microsoft.com/windows/win32/api//shobjidl_core/nf-shobjidl_core-idroptargethelper-show#parameters">Read more on docs.microsoft.com</see>.</para>
            /// </param>
            /// <returns>
            /// <para>Type: <b>HRESULT</b> Returns S_OK if successful, or a COM error value otherwise.</para>
            /// </returns>
            /// <remarks>
            /// <para><see href="https://docs.microsoft.com/windows/win32/api//shobjidl_core/nf-shobjidl_core-idroptargethelper-show">Learn more about this API from docs.microsoft.com</see>.</para>
            /// </remarks>
            void Show(winmdroot.Foundation.BOOL fShow);
        }
    }
}
