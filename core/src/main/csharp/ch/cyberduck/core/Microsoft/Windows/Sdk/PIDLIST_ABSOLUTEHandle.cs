using System;
using System.Runtime.CompilerServices;

namespace Ch.Cyberduck.Core.Microsoft.Windows.Sdk.UI.Shell
{
    using static PInvoke;

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
