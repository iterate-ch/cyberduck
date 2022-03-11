#pragma warning disable CS1591,CS1573,CS0465,CS0649,CS8019,CS1570,CS1584,CS1658,CS0436
namespace Windows.Win32
{
    using global::System;
    using global::System.Diagnostics;
    using global::System.Runtime.CompilerServices;
    using global::System.Runtime.InteropServices;
    using winmdroot = global::Windows.Win32;

    namespace System.Com
    {
        public struct STGMEDIUM
        {
            public uint tymed;
            public winmdroot.System.Com.STGMEDIUM._Anonymous_e__Union Anonymous;
            public object pUnkForRelease;

            [StructLayout(LayoutKind.Explicit)]
            public partial struct _Anonymous_e__Union
            {
                [FieldOffset(0)]
                public winmdroot.Graphics.Gdi.HBITMAP hBitmap;
                [FieldOffset(0)]
                public nint hGlobal;
                [FieldOffset(0)]
                public winmdroot.Foundation.PWSTR lpszFileName;
            }
        }
    }
}
