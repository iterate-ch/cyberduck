using System;

namespace Ch.Cyberduck.Core.Microsoft.Windows.Sdk.UI.WindowsAndMessaging
{
    using static PInvoke;

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
