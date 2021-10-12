using System;
using static System.Runtime.CompilerServices.Unsafe;
using static System.Runtime.InteropServices.MemoryMarshal;

namespace Windows.Win32.Foundation
{
    public partial struct PCWSTR
    {
        public unsafe static implicit operator PCWSTR(in string value) => (char*)AsPointer(ref GetReference(value.AsSpan()));
    }
}
