using System.Diagnostics.CodeAnalysis;
using static Windows.Win32.CorePInvoke;

namespace Windows.Win32.UI.WindowsAndMessaging
{
    public unsafe ref struct HICON_Handle
    {
        private HICON ptr;

        public HICON_Handle(in HICON handle)
        {
            ptr = handle;
        }

        [UnscopedRef]
        public ref HICON Handle => ref ptr;

        public static implicit operator bool(in HICON_Handle @this) => !@this.ptr.IsNull;

        public static implicit operator nint(in HICON_Handle @this) => @this.ptr;

        public void Dispose()
        {
            if (!ptr.IsNull)
            {
                DestroyIcon(ptr);
                ptr = default;
            }
        }
    }
}
