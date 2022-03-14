#pragma warning disable IDE0001,IDE0005,CS1591,CS1573,CS0465,CS0649,CS8019,CS1570,CS1584,CS1658,CS0436

namespace Windows.Win32
{
    using global::System;
    using global::System.Diagnostics;
    using global::System.Runtime.CompilerServices;
    using global::System.Runtime.InteropServices;
    using winmdroot = global::Windows.Win32;

    public static partial class FriendlyOverloadExtensions
    {
        /// <inheritdoc cref="winmdroot.UI.Shell.IDropTargetHelper.DragEnter(Foundation.HWND, System.Runtime.InteropServices.ComTypes.IDataObject, Foundation.POINT*, uint)"/>
        public static unsafe void DragEnter(this winmdroot.UI.Shell.IDropTargetHelper @this, winmdroot.Foundation.HWND hwndTarget, global::System.Runtime.InteropServices.ComTypes.IDataObject pDataObject, in winmdroot.Foundation.POINT ppt, winmdroot.UI.Shell.DROPIMAGETYPE dwEffect)
        {
            fixed (winmdroot.Foundation.POINT* pptLocal = &ppt)
            {
                @this.DragEnter(hwndTarget, pDataObject, pptLocal, (uint)dwEffect);
            }
        }

        /// <inheritdoc cref="winmdroot.UI.Shell.IDropTargetHelper.DragOver(Foundation.POINT*, uint)"
        public static unsafe void DragOver(this winmdroot.UI.Shell.IDropTargetHelper @this, in winmdroot.Foundation.POINT ppt, winmdroot.UI.Shell.DROPIMAGETYPE dwEffect)
        {
            fixed (winmdroot.Foundation.POINT* pptLocal = &ppt)
            {
                @this.DragOver(pptLocal, (uint)dwEffect);
            }
        }

        /// <inheritdoc cref="winmdroot.UI.Shell.IDropTargetHelper.Drop(System.Runtime.InteropServices.ComTypes.IDataObject, Foundation.POINT*, uint)"
        public static unsafe void Drop(this winmdroot.UI.Shell.IDropTargetHelper @this, global::System.Runtime.InteropServices.ComTypes.IDataObject pDataObject, in winmdroot.Foundation.POINT ppt, winmdroot.UI.Shell.DROPIMAGETYPE dwEffect)
        {
            fixed (winmdroot.Foundation.POINT* pptLocal = &ppt)
            {
                @this.Drop(pDataObject, pptLocal, (uint)dwEffect);
            }
        }

        public static void GetAt<T>(this winmdroot.UI.Shell.Common.IObjectArray @this, uint uiIndex, out T ppv)
        {
            @this.GetAt(uiIndex, typeof(T).GUID, out var temp);
            ppv = (T)temp;
        }

        /// <inheritdoc cref="winmdroot.UI.Shell.IVirtualDesktopManager.GetWindowDesktopId(winmdroot.Foundation.HWND, global::System.Guid*)"/>
		public static unsafe global::System.Guid GetWindowDesktopId(this winmdroot.UI.Shell.IVirtualDesktopManager @this, winmdroot.Foundation.HWND topLevelWindow)
        {
            global::System.Guid desktopIdLocal = default;
            @this.GetWindowDesktopId(topLevelWindow, &desktopIdLocal);
            return desktopIdLocal;
        }

        /// <inheritdoc cref="winmdroot.UI.Shell.IVirtualDesktopManager.IsWindowOnCurrentVirtualDesktop(winmdroot.Foundation.HWND, winmdroot.Foundation.BOOL*)"/>
		public static unsafe winmdroot.Foundation.BOOL IsWindowOnCurrentVirtualDesktop(this winmdroot.UI.Shell.IVirtualDesktopManager @this, winmdroot.Foundation.HWND topLevelWindow)
        {
            winmdroot.Foundation.BOOL onCurrentDesktopLocal = default;
            @this.IsWindowOnCurrentVirtualDesktop(topLevelWindow, &onCurrentDesktopLocal);
            return onCurrentDesktopLocal;
        }
    }
}
