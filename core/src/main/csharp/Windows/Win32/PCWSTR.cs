using System;
using System.Runtime.CompilerServices;
using System.Runtime.InteropServices;

namespace Windows.Win32.Foundation;

public unsafe partial struct PCWSTR
{
    public static PCWSTR DangerousFromString(in ReadOnlySpan<char> value)
    {
        return (char*)Unsafe.AsPointer(ref MemoryMarshal.GetReference(value));
    }

    public static PCWSTR DangerousFromString(string value) => DangerousFromString(value.AsSpan());

    public string ToString(int length) => Value is null ? new string(Value, 0, length) : null;
}
