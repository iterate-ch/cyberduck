using System;
using static System.Runtime.CompilerServices.Unsafe;
using static System.Runtime.InteropServices.MemoryMarshal;

namespace Windows.Win32
{
    namespace Foundation
    {
        public unsafe partial struct PCWSTR
        {
            public PCWSTR(ref char value)
            {
                Value = (char*)AsPointer(ref value);
            }

            public static implicit operator PCWSTR(in ReadOnlySpan<char> value) => new(ref GetReference(value));

            public static implicit operator PCWSTR(in string value) => new(ref GetReference(value.AsSpan()));
        }
    }
}
