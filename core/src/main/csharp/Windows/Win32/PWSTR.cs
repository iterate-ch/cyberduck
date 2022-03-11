using System;
using static System.Runtime.CompilerServices.Unsafe;
using static System.Runtime.InteropServices.MemoryMarshal;

namespace Windows.Win32
{
    namespace Foundation
    {
        public unsafe partial struct PWSTR
        {
            public static unsafe implicit operator PWSTR(in string value) => (char*)AsPointer(ref GetReference(value.AsSpan()));
        }
    }
}
