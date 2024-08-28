using System;
using System.Runtime.CompilerServices;
using System.Runtime.InteropServices;

namespace Windows.Win32.Foundation;

public unsafe partial struct PCWSTR
{
    public static PCWSTR DangerousFromSpan(in ReadOnlySpan<char> value)
    {
        return (char*)Unsafe.AsPointer(ref MemoryMarshal.GetReference(value));
    }

    public static PCWSTR DangerousFromString(string value) => DangerousFromSpan(value.AsSpan());

    public PWSTR DangerousAsPWSTR() => Value;

    public string ToString(int length) => Value is null ? new string(Value, 0, length) : null;
}
