using System.Drawing;
using System.Runtime.InteropServices.ComTypes;
using Windows.Win32.Foundation;
using Windows.Win32.System.Ole;
using Windows.Win32.UI.Shell;

namespace Windows.Win32;

public unsafe static partial class UI_Shell_IDropTargetHelper_Extensions
{
    /// <inheritdoc cref="IDropTargetHelper.DragEnter(HWND, IDataObject, Point*, DROPEFFECT)" />
    public static void DragEnter(this IDropTargetHelper @this, HWND hwndTarget, IDataObject pDataObject, in Point ppt, DROPEFFECT dwEffect)
    {
        fixed (Point* pptLocal = &ppt)
        {
            @this.DragEnter(hwndTarget, pDataObject, pptLocal, dwEffect);
        }
    }

    /// <inheritdoc cref="IDropTargetHelper.DragOver(Point*, DROPEFFECT)" />
    public static void DragOver(this IDropTargetHelper @this, in Point ppt, DROPEFFECT dwEffect)
    {
        fixed (Point* pptLocal = &ppt)
        {
            @this.DragOver(pptLocal, dwEffect);
        }
    }

    /// <inheritdoc cref="IDropTargetHelper.Drop(IDataObject, Point*, DROPEFFECT)" />
    public static void Drop(this IDropTargetHelper @this, IDataObject pDataObject, in Point ppt, DROPEFFECT dwEffect)
    {
        fixed (Point* pptLocal = &ppt)
        {
            @this.Drop(pDataObject, pptLocal, dwEffect);
        }
    }
}
