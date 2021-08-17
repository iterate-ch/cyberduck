using System;
using System.Runtime.CompilerServices;
using System.Runtime.InteropServices;

namespace Ch.Cyberduck.Core.Microsoft.Windows.Sdk.Foundation
{
    public partial struct PCWSTR
    {
        public unsafe static implicit operator PCWSTR(in string value) => (char*)Unsafe.AsPointer(ref MemoryMarshal.GetReference(value.AsSpan()));
    }
}
