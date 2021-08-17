using System;

namespace Ch.Cyberduck.Core.Microsoft.Windows.Sdk.UI.WindowsAndMessaging
{
    using static PInvoke;

    public partial struct HICON : IDisposable
    {
        public void Dispose()
        {
            DestroyIcon(this);
        }
    }
}
