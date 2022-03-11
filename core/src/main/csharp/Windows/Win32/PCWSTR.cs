using System;
using static System.Runtime.CompilerServices.Unsafe;
using static System.Runtime.InteropServices.MemoryMarshal;

namespace Windows.Win32
{
    namespace Foundation
    {
        public unsafe partial struct PCWSTR
        {
            public static unsafe implicit operator PCWSTR(in string value) => (char*)AsPointer(ref GetReference(value.AsSpan()));
        }
    }
}
