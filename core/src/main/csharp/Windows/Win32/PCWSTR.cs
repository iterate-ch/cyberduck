using System;
using System.Runtime.CompilerServices;
using System.Runtime.InteropServices;

namespace Windows.Win32.Foundation
{
    public partial struct PCWSTR
    {
        public unsafe static implicit operator PCWSTR(in string value) => (char*)Unsafe.AsPointer(ref MemoryMarshal.GetReference(value.AsSpan()));
    }
}
