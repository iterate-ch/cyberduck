using System;
using System.Runtime.CompilerServices;
using System.Runtime.InteropServices;

namespace Windows.Win32.Foundation;

public unsafe partial struct PWSTR
{
    public static PWSTR DangerousFromString(in Span<char> value)
    {
        return (char*)Unsafe.AsPointer(ref MemoryMarshal.GetReference(value));
    }

    public string ToString(int length) => Value is null ? null : new(Value, 0, length);
}
