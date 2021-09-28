using System;

namespace Windows.Win32.UI.WindowsAndMessaging
{
    using static CyberduckCorePInvoke;

    public class HIconHandle : IDisposable
    {
        private HICON hIcon;

        public HIconHandle(HICON hIcon)
        {
            this.hIcon = hIcon;
        }

        public static implicit operator HICON(HIconHandle handle) => handle.hIcon;

        public static implicit operator IntPtr(HIconHandle handle) => handle.hIcon.Value;

        public void Dispose()
        {
            if (!hIcon.IsNull)
            {
                DestroyIcon(hIcon);
                hIcon = default;
            }
        }
    }
}
