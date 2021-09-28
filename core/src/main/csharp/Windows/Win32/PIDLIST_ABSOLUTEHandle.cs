using System;
using System.Runtime.CompilerServices;

namespace Windows.Win32.UI.Shell
{
    using static CyberduckCorePInvoke;

    public unsafe class PIDLIST_ABSOLUTEHandle : IDisposable
    {
        private ITEMIDLIST* ptr;

        public ITEMIDLIST* Pointer => ptr;

        public ref ITEMIDLIST Value => ref Unsafe.AsRef<ITEMIDLIST>(ptr);

        public static implicit operator bool(PIDLIST_ABSOLUTEHandle handle) => handle.ptr != default;

        public void Dispose()
        {
            if (ptr != default)
            {
                CoTaskMemFree(ptr);
                ptr = default;
            }
        }

        public ref ITEMIDLIST* Put() => ref ptr;
    }
}
