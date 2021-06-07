// Author: Markus Scholtes, 2019
// Version 1.3
// Version for Windows 10 1809
// Compile with:
// C:\Windows\Microsoft.NET\Framework\v4.0.30319\csc.exe VirtualDesktop.cs

using System;
using System.ComponentModel;
using System.Runtime.InteropServices;
using System.Windows.Forms;
using Ch.Cyberduck.Ui.Microsoft.Windows.Sdk;
using static Ch.Cyberduck.Ui.Microsoft.Windows.Sdk.PInvoke;

// Based on http://stackoverflow.com/a/32417530, Windows 10 SDK and github project VirtualDesktop

namespace Ch.Cyberduck.Ui.Core.VirtualDesktop.Windows1809
{
    [ComImport]
    [InterfaceType(ComInterfaceType.InterfaceIsIUnknown)]
    [Guid("372E1D3B-38D3-42E4-A15B-8AB2B178F513")]
    internal interface IApplicationView
    {
        int SetFocus();

        int SwitchTo();

        int TryInvokeBack(IntPtr /* IAsyncCallback* */ callback);

        int GetThumbnailWindow(out IntPtr hwnd);

        int GetMonitor(out IntPtr /* IImmersiveMonitor */ immersiveMonitor);

        int GetVisibility(out int visibility);

        int SetCloak(APPLICATION_VIEW_CLOAK_TYPE cloakType, int unknown);

        int GetPosition(ref Guid guid /* GUID for IApplicationViewPosition */, out IntPtr /* IApplicationViewPosition** */ position);

        int SetPosition(ref IntPtr /* IApplicationViewPosition* */ position);

        int InsertAfterWindow(IntPtr hwnd);

        int GetExtendedFramePosition(out Rect rect);

        int GetAppUserModelId([MarshalAs(UnmanagedType.LPWStr)] out string id);

        int SetAppUserModelId(string id);

        int IsEqualByAppUserModelId(string id, out int result);

        int GetViewState(out uint state);

        int SetViewState(uint state);

        int GetNeediness(out int neediness);

        int GetLastActivationTimestamp(out ulong timestamp);

        int SetLastActivationTimestamp(ulong timestamp);

        int GetVirtualDesktopId(out Guid guid);

        int SetVirtualDesktopId(ref Guid guid);

        int GetShowInSwitchers(out int flag);

        int SetShowInSwitchers(int flag);

        int GetScaleFactor(out int factor);

        int CanReceiveInput(out bool canReceiveInput);

        int GetCompatibilityPolicyType(out APPLICATION_VIEW_COMPATIBILITY_POLICY flags);

        int SetCompatibilityPolicyType(APPLICATION_VIEW_COMPATIBILITY_POLICY flags);

        int GetSizeConstraints(IntPtr /* IImmersiveMonitor* */ monitor, out Size size1, out Size size2);

        int GetSizeConstraintsForDpi(uint uint1, out Size size1, out Size size2);

        int SetSizeConstraintsForDpi(ref uint uint1, ref Size size1, ref Size size2);

        int OnMinSizePreferencesUpdated(IntPtr hwnd);

        int ApplyOperation(IntPtr /* IApplicationViewOperation* */ operation);

        int IsTray(out bool isTray);

        int IsInHighZOrderBand(out bool isInHighZOrderBand);

        int IsSplashScreenPresented(out bool isSplashScreenPresented);

        int Flash();

        int GetRootSwitchableOwner(out IApplicationView rootSwitchableOwner);

        int EnumerateOwnershipTree(out IObjectArray ownershipTree);

        int GetEnterpriseId([MarshalAs(UnmanagedType.LPWStr)] out string enterpriseId);

        int IsMirrored(out bool isMirrored);

        int Unknown1(out int unknown);

        int Unknown2(out int unknown);

        int Unknown3(out int unknown);

        int Unknown4(out int unknown);

        int Unknown5(out int unknown);

        int Unknown6(int unknown);

        int Unknown7();

        int Unknown8(out int unknown);

        int Unknown9(int unknown);

        int Unknown10(int unknownX, int unknownY);

        int Unknown11(int unknown);

        int Unknown12(out Size size1);
    }

    [ComImport]
    [InterfaceType(ComInterfaceType.InterfaceIsIUnknown)]
    [Guid("1841C6D7-4F9D-42C0-AF41-8747538F10E5")]
    internal interface IApplicationViewCollection
    {
        int GetViews(out IObjectArray array);

        int GetViewsByZOrder(out IObjectArray array);

        int GetViewsByAppUserModelId(string id, out IObjectArray array);

        int GetViewForHwnd(IntPtr hwnd, out IApplicationView view);

        int GetViewForApplication(object application, out IApplicationView view);

        int GetViewForAppUserModelId(string id, out IApplicationView view);

        int GetViewInFocus(out IntPtr view);

        void outreshCollection();

        int RegisterForApplicationViewChanges(object listener, out int cookie);

        int UnregisterForApplicationViewChanges(int cookie);
    }

    [ComImport]
    [InterfaceType(ComInterfaceType.InterfaceIsIUnknown)]
    [Guid("FF72FFDD-BE7E-43FC-9C03-AD81681E88E4")]
    internal interface IVirtualDesktop
    {
        bool IsViewVisible(IApplicationView view);

        Guid GetId();
    }

    [ComImport]
    [InterfaceType(ComInterfaceType.InterfaceIsIUnknown)]
    [Guid("F31574D6-B682-4CDC-BD56-1827860ABEC6")]
    internal interface IVirtualDesktopManagerInternal
    {
        int GetCount();

        void MoveViewToDesktop(IApplicationView view, IVirtualDesktop desktop);

        bool CanViewMoveDesktops(IApplicationView view);

        IVirtualDesktop GetCurrentDesktop();

        void GetDesktops(out IObjectArray desktops);

        [PreserveSig]
        int GetAdjacentDesktop(IVirtualDesktop from, int direction, out IVirtualDesktop desktop);

        void SwitchDesktop(IVirtualDesktop desktop);

        IVirtualDesktop CreateDesktop();

        void RemoveDesktop(IVirtualDesktop desktop, IVirtualDesktop fallback);

        IVirtualDesktop FindDesktop(ref Guid desktopid);
    }

    [ComImport]
    [InterfaceType(ComInterfaceType.InterfaceIsIUnknown)]
    [Guid("4CE81583-1E4C-4632-A621-07A53543148F")]
    internal interface IVirtualDesktopPinnedApps
    {
        bool IsAppIdPinned(string appId);

        void PinAppID(string appId);

        void UnpinAppID(string appId);

        bool IsViewPinned(IApplicationView applicationView);

        void PinView(IApplicationView applicationView);

        void UnpinView(IApplicationView applicationView);
    }

    public class Desktop
    {
        private IVirtualDesktop ivd;

        private Desktop(IVirtualDesktop desktop)
        {
            this.ivd = desktop;
        }

        public static int Count
        { // Returns the number of desktops
            get { return DesktopManager.VirtualDesktopManagerInternal.GetCount(); }
        }

        public static Desktop Current
        { // Returns current desktop
            get { return new Desktop(DesktopManager.VirtualDesktopManagerInternal.GetCurrentDesktop()); }
        }

        public bool IsVisible
        { // Returns <true> if this desktop is the current displayed one
            get { return object.ReferenceEquals(ivd, DesktopManager.VirtualDesktopManagerInternal.GetCurrentDesktop()); }
        }

        public Desktop Left
        { // Returns desktop at the left of this one, null if none
            get
            {
                IVirtualDesktop desktop;
                int hr = DesktopManager.VirtualDesktopManagerInternal.GetAdjacentDesktop(ivd, 3, out desktop); // 3 = LeftDirection
                if (hr == 0)
                    return new Desktop(desktop);
                else
                    return null;
            }
        }

        public Desktop Right
        { // Returns desktop at the right of this one, null if none
            get
            {
                IVirtualDesktop desktop;
                int hr = DesktopManager.VirtualDesktopManagerInternal.GetAdjacentDesktop(ivd, 4, out desktop); // 4 = RightDirection
                if (hr == 0)
                    return new Desktop(desktop);
                else
                    return null;
            }
        }

        public static Desktop Create()
        { // Create a new desktop
            return new Desktop(DesktopManager.VirtualDesktopManagerInternal.CreateDesktop());
        }

        public static int FromDesktop(Desktop desktop)
        { // Returns index of desktop object or -1 if not found
            return DesktopManager.GetDesktopIndex(desktop.ivd);
        }

        public static Desktop FromIndex(int index)
        { // Create desktop object from index 0..Count-1
            return new Desktop(DesktopManager.GetDesktop(index));
        }

        public static Desktop FromWindow(IntPtr hWnd)
        { // Creates desktop object on which window <hWnd> is displayed
            if (hWnd == IntPtr.Zero) throw new ArgumentNullException();
            Guid id = DesktopManager.VirtualDesktopManager.GetWindowDesktopId(hWnd);
            return new Desktop(DesktopManager.VirtualDesktopManagerInternal.FindDesktop(ref id));
        }

        public static bool IsApplicationPinned(IntPtr hWnd)
        { // Returns true if application for window <hWnd> is pinned to all desktops
            if (hWnd == IntPtr.Zero) throw new ArgumentNullException();
            return DesktopManager.VirtualDesktopPinnedApps.IsAppIdPinned(DesktopManager.GetAppId(hWnd));
        }

        public static bool IsWindowPinned(IntPtr hWnd)
        { // Returns true if window <hWnd> is pinned to all desktops
            if (hWnd == IntPtr.Zero) throw new ArgumentNullException();
            return DesktopManager.VirtualDesktopPinnedApps.IsViewPinned(hWnd.GetApplicationView());
        }

        public static void PinApplication(IntPtr hWnd)
        { // pin application for window <hWnd> to all desktops
            if (hWnd == IntPtr.Zero) throw new ArgumentNullException();
            string appId = DesktopManager.GetAppId(hWnd);
            if (!DesktopManager.VirtualDesktopPinnedApps.IsAppIdPinned(appId))
            { // pin only if not already pinned
                DesktopManager.VirtualDesktopPinnedApps.PinAppID(appId);
            }
        }

        public static void PinWindow(IntPtr hWnd)
        { // pin window <hWnd> to all desktops
            if (hWnd == IntPtr.Zero) throw new ArgumentNullException();
            var view = hWnd.GetApplicationView();
            if (!DesktopManager.VirtualDesktopPinnedApps.IsViewPinned(view))
            { // pin only if not already pinned
                DesktopManager.VirtualDesktopPinnedApps.PinView(view);
            }
        }

        public static void UnpinApplication(IntPtr hWnd)
        { // unpin application for window <hWnd> from all desktops
            if (hWnd == IntPtr.Zero) throw new ArgumentNullException();
            var view = hWnd.GetApplicationView();
            string appId = DesktopManager.GetAppId(hWnd);
            if (DesktopManager.VirtualDesktopPinnedApps.IsAppIdPinned(appId))
            { // unpin only if already pinned
                DesktopManager.VirtualDesktopPinnedApps.UnpinAppID(appId);
            }
        }

        public static void UnpinWindow(IntPtr hWnd)
        { // unpin window <hWnd> from all desktops
            if (hWnd == IntPtr.Zero) throw new ArgumentNullException();
            var view = hWnd.GetApplicationView();
            if (DesktopManager.VirtualDesktopPinnedApps.IsViewPinned(view))
            { // unpin only if not already unpinned
                DesktopManager.VirtualDesktopPinnedApps.UnpinView(view);
            }
        }

        public override bool Equals(object obj)
        { // Compares with object
            var desk = obj as Desktop;
            return desk != null && object.ReferenceEquals(this.ivd, desk.ivd);
        }

        public override int GetHashCode()
        { // Get hash
            return ivd.GetHashCode();
        }

        public bool HasWindow(IntPtr hWnd)
        { // Returns true if window <hWnd> is on this desktop
            if (hWnd == IntPtr.Zero) throw new ArgumentNullException();
            return ivd.GetId() == DesktopManager.VirtualDesktopManager.GetWindowDesktopId(hWnd);
        }

        public void MakeVisible()
        { // Make this desktop visible
            DesktopManager.VirtualDesktopManagerInternal.SwitchDesktop(ivd);
        }

        public void MoveActiveWindow()
        {
            MoveWindow(GetForegroundWindow());
        }

        public void MoveWindow(IntPtr hWnd)
        { // Move window <hWnd> to this desktop
            int processId;
            if (hWnd == IntPtr.Zero) throw new ArgumentNullException();
            GetWindowThreadProcessId(hWnd, out processId);

            if (System.Diagnostics.Process.GetCurrentProcess().Id == processId)
            { // window of process
                try // the easy way (if we are owner)
                {
                    DesktopManager.VirtualDesktopManager.MoveWindowToDesktop(hWnd, ivd.GetId());
                }
                catch // window of process, but we are not the owner
                {
                    IApplicationView view;
                    DesktopManager.ApplicationViewCollection.GetViewForHwnd(hWnd, out view);
                    DesktopManager.VirtualDesktopManagerInternal.MoveViewToDesktop(view, ivd);
                }
            }
            else
            { // window of other process
                IApplicationView view;
                DesktopManager.ApplicationViewCollection.GetViewForHwnd(hWnd, out view);
                DesktopManager.VirtualDesktopManagerInternal.MoveViewToDesktop(view, ivd);
            }
        }

        public void Remove(Desktop fallback = null)
        { // Destroy desktop and switch to <fallback>
            IVirtualDesktop fallbackdesktop;
            if (fallback == null)
            { // if no fallback is given use desktop to the left except for desktop 0.
                Desktop dtToCheck = new Desktop(DesktopManager.GetDesktop(0));
                if (this.Equals(dtToCheck))
                { // desktop 0: set fallback to second desktop (= "right" desktop)
                    DesktopManager.VirtualDesktopManagerInternal.GetAdjacentDesktop(ivd, 4, out fallbackdesktop); // 4 = RightDirection
                }
                else
                { // set fallback to "left" desktop
                    DesktopManager.VirtualDesktopManagerInternal.GetAdjacentDesktop(ivd, 3, out fallbackdesktop); // 3 = LeftDirection
                }
            }
            else
                // set fallback desktop
                fallbackdesktop = fallback.ivd;

            DesktopManager.VirtualDesktopManagerInternal.RemoveDesktop(ivd, fallbackdesktop);
        }
    }

    public class VirtualDesktopManager : Contracts.IVirtualDesktopManager
    {
        public void BringToCurrentDesktop(Form form)
        {
            var handle = form.Handle;
            if (DesktopManager.VirtualDesktopManager.IsWindowOnCurrentVirtualDesktop(handle))
            {
                return;
            }

            var currentDesktop = DesktopManager.VirtualDesktopManagerInternal.GetCurrentDesktop();
            var id = currentDesktop.GetId();

            DesktopManager.VirtualDesktopManager.MoveWindowToDesktop(handle, id);
        }
    }

    internal static class DesktopManager
    {
        internal static IApplicationViewCollection ApplicationViewCollection;

        internal static IVirtualDesktopManager VirtualDesktopManager;

        internal static IVirtualDesktopManagerInternal VirtualDesktopManagerInternal;

        internal static IVirtualDesktopPinnedApps VirtualDesktopPinnedApps;

        static DesktopManager()
        {
            var shell = (IServiceProvider10)Activator.CreateInstance(Type.GetTypeFromCLSID(Guids.CLSID_ImmersiveShell));
            VirtualDesktopManagerInternal = (IVirtualDesktopManagerInternal)shell.QueryService(Guids.CLSID_VirtualDesktopManagerInternal, typeof(IVirtualDesktopManagerInternal).GUID);
            VirtualDesktopManager = (IVirtualDesktopManager)Activator.CreateInstance(Type.GetTypeFromCLSID(Guids.CLSID_VirtualDesktopManager));
            ApplicationViewCollection = (IApplicationViewCollection)shell.QueryService(typeof(IApplicationViewCollection).GUID, typeof(IApplicationViewCollection).GUID);
            VirtualDesktopPinnedApps = (IVirtualDesktopPinnedApps)shell.QueryService(Guids.CLSID_VirtualDesktopPinnedApps, typeof(IVirtualDesktopPinnedApps).GUID);
        }

        internal static string GetAppId(IntPtr hWnd)
        { // get Application ID to window handle
            string appId;
            hWnd.GetApplicationView().GetAppUserModelId(out appId);
            return appId;
        }

        internal static IApplicationView GetApplicationView(this IntPtr hWnd)
        { // get application view to window handle
            IApplicationView view;
            ApplicationViewCollection.GetViewForHwnd(hWnd, out view);
            return view;
        }

        internal static IVirtualDesktop GetDesktop(int index)
        {   // get desktop with index
            int count = VirtualDesktopManagerInternal.GetCount();
            if (index < 0 || index >= count) throw new ArgumentOutOfRangeException("index");
            IObjectArray desktops;
            VirtualDesktopManagerInternal.GetDesktops(out desktops);
            IVirtualDesktop objdesktop = desktops.GetAt<IVirtualDesktop>(index);
            Marshal.ReleaseComObject(desktops);
            return objdesktop;
        }

        internal static int GetDesktopIndex(IVirtualDesktop desktop)
        { // get index of desktop
            int index = -1;
            Guid IdSearch = desktop.GetId();
            IObjectArray desktops;
            VirtualDesktopManagerInternal.GetDesktops(out desktops);
            IVirtualDesktop objdesktop;
            for (int i = 0; i < VirtualDesktopManagerInternal.GetCount(); i++)
            {
                objdesktop = desktops.GetAt<IVirtualDesktop>(i);
                if (IdSearch.CompareTo(objdesktop.GetId()) == 0)
                {
                    index = i;
                    break;
                }
            }
            Marshal.ReleaseComObject(desktops);
            return index;
        }
    }
}
