#pragma warning disable IDE0001,IDE0002,IDE0005,CS1591,CS1573,CS0465,CS0649,CS8019,CS1570,CS1584,CS1658,CS0436

namespace Windows.Win32
{
    using global::System;
    using global::System.Diagnostics;
    using global::System.Runtime.CompilerServices;
    using global::System.Runtime.InteropServices;
    using winmdroot = global::Windows.Win32;

    public partial class FriendlyOverloadExtensions
    {
        /// <inheritdoc cref="winmdroot.UI.Controls.IImageList.GetIcon(int, uint, winmdroot.UI.WindowsAndMessaging.HICON*)"/>
        public static unsafe void GetIcon(this winmdroot.UI.Controls.IImageList @this, int i, uint flags, out winmdroot.UI.WindowsAndMessaging.HICON picon)
        {
            fixed (winmdroot.UI.WindowsAndMessaging.HICON* piconLocal = &picon)
            {
                @this.GetIcon(i, flags, piconLocal);
            }
        }
    }
}
