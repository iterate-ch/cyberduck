using System;
using System.Runtime.CompilerServices;
using System.Runtime.InteropServices;

namespace Windows.Win32.Foundation;

public unsafe partial struct PWSTR
{
    public static PWSTR DangerousFromString(in ReadOnlySpan<char> value)
    {
        return (char*)Unsafe.AsPointer(ref MemoryMarshal.GetReference(value));
    }

    public static PWSTR DangerousFromString(string value) => DangerousFromString(value.AsSpan());

    public string ToString(int length) => Value is null ? null : new(Value, 0, length);
}
