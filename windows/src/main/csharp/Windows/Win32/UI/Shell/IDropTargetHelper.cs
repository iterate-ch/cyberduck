using System.Drawing;
using System.Runtime.InteropServices;
using System.Runtime.InteropServices.ComTypes;
using Windows.Win32.Foundation;
using Windows.Win32.System.Ole;

namespace Windows.Win32.UI.Shell;

[Guid("4657278B-411B-11D2-839A-00C04FD918D0"), InterfaceType(ComInterfaceType.InterfaceIsIUnknown), ComImport()]
public interface IDropTargetHelper
{
    /// <summary>Notifies the drag-image manager that the drop target's IDropTarget::DragEnter method has been called.</summary>
    /// <param name="hwndTarget">
    /// <para>Type: <b>HWND</b> The target's window handle.</para>
    /// <para><see href="https://learn.microsoft.com/windows/win32/api/shobjidl_core/nf-shobjidl_core-idroptargethelper-dragenter#parameters">Read more on docs.microsoft.com</see>.</para>
    /// </param>
    /// <param name="pDataObject">
    /// <para>Type: <b><a href="https://docs.microsoft.com/windows/desktop/api/objidl/nn-objidl-idataobject">IDataObject</a>*</b> A pointer to the data object's <a href="https://docs.microsoft.com/windows/desktop/api/objidl/nn-objidl-idataobject">IDataObject</a> interface.</para>
    /// <para><see href="https://learn.microsoft.com/windows/win32/api/shobjidl_core/nf-shobjidl_core-idroptargethelper-dragenter#parameters">Read more on docs.microsoft.com</see>.</para>
    /// </param>
    /// <param name="ppt">
    /// <para>Type: <b><a href="https://docs.microsoft.com/windows/win32/api/windef/ns-windef-point">POINT</a>*</b> The <a href="https://docs.microsoft.com/windows/win32/api/windef/ns-windef-point">POINT</a> structure pointer that was received in the <a href="https://docs.microsoft.com/windows/desktop/api/oleidl/nf-oleidl-idroptarget-dragenter">IDropTarget::DragEnter</a> method's <i>pt</i> parameter.</para>
    /// <para><see href="https://learn.microsoft.com/windows/win32/api/shobjidl_core/nf-shobjidl_core-idroptargethelper-dragenter#parameters">Read more on docs.microsoft.com</see>.</para>
    /// </param>
    /// <param name="dwEffect">
    /// <para>Type: <b>DWORD</b> The value pointed to by the <a href="https://docs.microsoft.com/windows/desktop/api/oleidl/nf-oleidl-idroptarget-dragenter">IDropTarget::DragEnter</a> method's <i>pdwEffect</i> parameter.</para>
    /// <para><see href="https://learn.microsoft.com/windows/win32/api/shobjidl_core/nf-shobjidl_core-idroptargethelper-dragenter#parameters">Read more on docs.microsoft.com</see>.</para>
    /// </param>
    /// <returns>
    /// <para>Type: <b>HRESULT</b> Returns S_OK if successful, or a COM error value otherwise.</para>
    /// </returns>
    /// <remarks>This method is called by a drop target when its <a href="https://docs.microsoft.com/windows/desktop/api/oleidl/nf-oleidl-idroptarget-dragenter">IDropTarget::DragEnter</a> method is called. It notifies the drag-image manager that the drop target has been entered, and provides it with the information needed to display the drag image.</remarks>
    unsafe void DragEnter(HWND hwndTarget, IDataObject pDataObject, Point* ppt, DROPEFFECT dwEffect);

    /// <summary>Notifies the drag-image manager that the drop target's IDropTarget::DragLeave method has been called.</summary>
    /// <returns>
    /// <para>Type: <b>HRESULT</b> Returns S_OK if successful, or a COM error value otherwise.</para>
    /// </returns>
    /// <remarks>This method is called by a drop target when its <a href="https://docs.microsoft.com/windows/desktop/api/oleidl/nf-oleidl-idroptarget-dragleave">IDropTarget::DragLeave</a> method is called. It notifies the drag-image manager that the cursor has left the drop target.</remarks>
    void DragLeave();

    /// <summary>Notifies the drag-image manager that the drop target's IDropTarget::DragOver method has been called.</summary>
    /// <param name="ppt">
    /// <para>Type: <b><a href="https://docs.microsoft.com/windows/win32/api/windef/ns-windef-point">POINT</a>*</b> The <a href="https://docs.microsoft.com/windows/win32/api/windef/ns-windef-point">POINT</a> structure pointer that was received in the <a href="https://docs.microsoft.com/windows/desktop/api/oleidl/nf-oleidl-idroptarget-dragover">IDropTarget::DragOver</a> method's <i>pt</i> parameter.</para>
    /// <para><see href="https://learn.microsoft.com/windows/win32/api/shobjidl_core/nf-shobjidl_core-idroptargethelper-dragover#parameters">Read more on docs.microsoft.com</see>.</para>
    /// </param>
    /// <param name="dwEffect">
    /// <para>Type: <b>DWORD</b> The value pointed to by the <a href="https://docs.microsoft.com/windows/desktop/api/oleidl/nf-oleidl-idroptarget-dragover">IDropTarget::DragOver</a> method's <i>pdwEffect</i> parameter.</para>
    /// <para><see href="https://learn.microsoft.com/windows/win32/api/shobjidl_core/nf-shobjidl_core-idroptargethelper-dragover#parameters">Read more on docs.microsoft.com</see>.</para>
    /// </param>
    /// <returns>
    /// <para>Type: <b>HRESULT</b> Returns S_OK if successful, or a COM error value otherwise.</para>
    /// </returns>
    /// <remarks>This method is called by a drop target when its <a href="https://docs.microsoft.com/windows/desktop/api/oleidl/nf-oleidl-idroptarget-dragover">IDropTarget::DragOver</a> method is called. It notifies the drag-image manager that the cursor position has changed and provides it with the information needed to display the drag image.</remarks>
    unsafe void DragOver(Point* ppt, DROPEFFECT dwEffect);

    /// <summary>Notifies the drag-image manager that the drop target's IDropTarget::Drop method has been called.</summary>
    /// <param name="pDataObject">
    /// <para>Type: <b><a href="https://docs.microsoft.com/windows/desktop/api/objidl/nn-objidl-idataobject">IDataObject</a>*</b> A pointer to the data object's <a href="https://docs.microsoft.com/windows/desktop/api/objidl/nn-objidl-idataobject">IDataObject</a> interface.</para>
    /// <para><see href="https://learn.microsoft.com/windows/win32/api/shobjidl_core/nf-shobjidl_core-idroptargethelper-drop#parameters">Read more on docs.microsoft.com</see>.</para>
    /// </param>
    /// <param name="ppt">
    /// <para>Type: <b><a href="https://docs.microsoft.com/windows/win32/api/windef/ns-windef-point">POINT</a>*</b> A <a href="https://docs.microsoft.com/windows/win32/api/windef/ns-windef-point">POINT</a> structure pointer that was received in the <a href="https://docs.microsoft.com/windows/desktop/api/oleidl/nf-oleidl-idroptarget-drop">IDropTarget::Drop</a> method's <i>pt</i> parameter.</para>
    /// <para><see href="https://learn.microsoft.com/windows/win32/api/shobjidl_core/nf-shobjidl_core-idroptargethelper-drop#parameters">Read more on docs.microsoft.com</see>.</para>
    /// </param>
    /// <param name="dwEffect">
    /// <para>Type: <b>DWORD</b> The value pointed to by the <a href="https://docs.microsoft.com/windows/desktop/api/oleidl/nf-oleidl-idroptarget-drop">IDropTarget::Drop</a> method's <i>pdwEffect</i> parameter.</para>
    /// <para><see href="https://learn.microsoft.com/windows/win32/api/shobjidl_core/nf-shobjidl_core-idroptargethelper-drop#parameters">Read more on docs.microsoft.com</see>.</para>
    /// </param>
    /// <returns>
    /// <para>Type: <b>HRESULT</b> Returns S_OK if successful, or a COM error value otherwise.</para>
    /// </returns>
    /// <remarks>This method is called by a drop target when its <a href="https://docs.microsoft.com/windows/desktop/api/oleidl/nf-oleidl-idroptarget-drop">IDropTarget::Drop</a> method is called. It notifies the drag-image manager that the object has been dropped, and provides it with the information needed to display the drag image.</remarks>
    unsafe void Drop(IDataObject pDataObject, Point* ppt, DROPEFFECT dwEffect);

    /// <summary>Notifies the drag-image manager to show or hide the drag image.</summary>
    /// <param name="fShow">
    /// <para>Type: <b>BOOL</b> A boolean value that is set to <b>TRUE</b> to show the drag image, and <b>FALSE</b> to hide it.</para>
    /// <para><see href="https://learn.microsoft.com/windows/win32/api/shobjidl_core/nf-shobjidl_core-idroptargethelper-show#parameters">Read more on docs.microsoft.com</see>.</para>
    /// </param>
    /// <returns>
    /// <para>Type: <b>HRESULT</b> Returns S_OK if successful, or a COM error value otherwise.</para>
    /// </returns>
    /// <remarks>This method is used when dragging over a target window in a low color-depth video mode. It allows the target to notify the drag-image manager to hide the drag image while it is painting the window. While you are painting a window that is currently being dragged over, hide the drag image by calling <b>Show</b> with <i>fShow</i> set to <b>FALSE</b>. Once the window has been painted, display the drag image again by calling <b>Show</b> with <i>fShow</i> set to <b>TRUE</b>.</remarks>
    void Show(BOOL fShow);
}
