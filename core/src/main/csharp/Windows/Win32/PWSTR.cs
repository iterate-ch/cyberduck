using System;
using static System.Runtime.CompilerServices.Unsafe;
using static System.Runtime.InteropServices.MemoryMarshal;

namespace Windows.Win32
{
    namespace Foundation
    {
        public unsafe partial struct PWSTR
        {
            public PWSTR(ref char value)
            {
                Value = (char*)AsPointer(ref value);
            }

            public static implicit operator PWSTR(in Span<char> value) => new(ref GetReference(value));

            public static implicit operator PWSTR(in string value) => new(ref GetReference(value.AsSpan()));
        }
    }
}
