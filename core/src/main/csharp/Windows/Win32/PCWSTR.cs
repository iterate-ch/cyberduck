#pragma warning disable IDE0001,IDE0002,IDE0003,IDE0005,CS1591,CS1573,CS0465,CS0649,CS8019,CS1570,CS1584,CS1658,CS0436

namespace Windows.Win32
{
    using global::System;
    using global::System.Diagnostics;
    using global::System.Runtime.CompilerServices;
    using global::System.Runtime.InteropServices;
    using winmdroot = global::Windows.Win32;

    namespace Foundation
    {
        public unsafe partial struct PCWSTR
        {
            public PCWSTR(ref char value)
            {
                Value = (char*)Unsafe.AsPointer(ref value);
            }

            public static implicit operator PCWSTR(in ReadOnlySpan<char> value) => new(ref MemoryMarshal.GetReference(value));

            public static implicit operator PCWSTR(in string value) => new(ref MemoryMarshal.GetReference(value.AsSpan()));

            public string ToString(int length) => this.Value == null ? null : new string(this.Value, 0, length);
        }
    }
}
