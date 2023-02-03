#pragma warning disable IDE0001,IDE0002,IDE0005,CS1591,CS1573,CS0465,CS0649,CS8019,CS1570,CS1584,CS1658,CS0436

namespace Windows.Win32
{
    using global::System;
    using global::System.Diagnostics;
    using global::System.Runtime.CompilerServices;
    using global::System.Runtime.InteropServices;
    using winmdroot = global::Windows.Win32;

    public unsafe partial class CorePInvoke
    {
        public static readonly winmdroot.Foundation.PCWSTR TD_ERROR_ICON = MAKEINTRESOURCE(-2);
        public static readonly winmdroot.Foundation.PCWSTR TD_INFORMATION_ICON = MAKEINTRESOURCE(-3);
        public static readonly winmdroot.Foundation.PCWSTR TD_QUESTION_ICON = MAKEINTRESOURCE(104);
        public static readonly winmdroot.Foundation.PCWSTR TD_SHIELD_ICON = MAKEINTRESOURCE(-4);
        public static readonly winmdroot.Foundation.PCWSTR TD_WARNING_ICON = MAKEINTRESOURCE(-1);

        private static winmdroot.Foundation.PCWSTR MAKEINTRESOURCE(int value) => MAKEINTRESOURCE(&value);

        private static winmdroot.Foundation.PCWSTR MAKEINTRESOURCE(short value) => MAKEINTRESOURCE(&value);

        private static winmdroot.Foundation.PCWSTR MAKEINTRESOURCE(ushort value) => MAKEINTRESOURCE(&value);

        private static winmdroot.Foundation.PCWSTR MAKEINTRESOURCE(void* ptr) => (char*)*(ushort*)ptr;

        /// <inheritdoc cref="LoadString(winmdroot.Foundation.HINSTANCE, uint, winmdroot.Foundation.PWSTR, int)"/>
		public static unsafe int LoadString(SafeHandle hInstance, uint uID, out winmdroot.Foundation.PWSTR lpBuffer)
        {
            fixed (winmdroot.Foundation.PWSTR* lpBufferLocal = &lpBuffer)
            {
                int __result = CorePInvoke.LoadString(hInstance, uID, new winmdroot.Foundation.PWSTR((char*)lpBufferLocal), 0);
                return __result;
            }
        }
    }
}
