using System;
using System.Runtime.CompilerServices;
using System.Runtime.InteropServices;

namespace Windows.Win32.Foundation
{
    public partial struct PWSTR
    {
        public unsafe static implicit operator PWSTR(in string value) => (char*)Unsafe.AsPointer(ref MemoryMarshal.GetReference(value.AsSpan()));
    }
}
