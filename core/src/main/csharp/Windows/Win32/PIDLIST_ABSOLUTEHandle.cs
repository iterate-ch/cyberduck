using System.Diagnostics.CodeAnalysis;
using static System.Runtime.CompilerServices.Unsafe;
using static Windows.Win32.CorePInvoke;

namespace Windows.Win32.UI.Shell.Common
{
    public unsafe ref struct PIDLIST_ABSOLUTEHandle
    {
        private ITEMIDLIST* ptr;

        public PIDLIST_ABSOLUTEHandle(in ITEMIDLIST ptr)
        {
            this.ptr = (ITEMIDLIST*)AsPointer(ref AsRef(ptr));
        }

        [UnscopedRef]
        public ref ITEMIDLIST* Pointer => ref ptr;

        [UnscopedRef]
        public ref ITEMIDLIST Value => ref *ptr;

        public static implicit operator bool(in PIDLIST_ABSOLUTEHandle @this) => @this.ptr != null;

        public static implicit operator PIDLIST_ABSOLUTEHandle(in ITEMIDLIST pidl) => new(pidl);

        public void Dispose()
        {
            if (ptr != null)
            {
                CoTaskMemFree(ptr);
                ptr = null;
            }
        }
    }
}
