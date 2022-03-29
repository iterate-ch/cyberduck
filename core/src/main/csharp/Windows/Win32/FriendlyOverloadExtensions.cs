#pragma warning disable IDE0001,IDE0002,IDE0005,CS1591,CS1573,CS0465,CS0649,CS8019,CS1570,CS1584,CS1658,CS0436

namespace Windows.Win32
{
    using global::System;
    using global::System.Diagnostics;
    using global::System.Runtime.CompilerServices;
    using global::System.Runtime.InteropServices;
    using winmdroot = global::Windows.Win32;

    public static partial class FriendlyOverloadExtensions
    {
        /// <inheritdoc cref="winmdroot.UI.Shell.IAssocHandler.GetName(winmdroot.Foundation.PWSTR*)"/>
		public static unsafe string GetName(this winmdroot.UI.Shell.IAssocHandler @this)
        {
            winmdroot.Foundation.PWSTR ppszLocal = new();
            @this.GetName(&ppszLocal);
            return ppszLocal.ToString();
        }

        /// <inheritdoc cref="winmdroot.UI.Shell.IAssocHandler.GetUIName(winmdroot.Foundation.PWSTR*)"/>
		public static unsafe string GetUIName(this winmdroot.UI.Shell.IAssocHandler @this)
        {
            winmdroot.Foundation.PWSTR ppszLocal = new();
            @this.GetUIName(&ppszLocal);
            return ppszLocal.ToString();
        }

        /// <inheritdoc cref="winmdroot.UI.Shell.IEnumAssocHandlers.Next(uint, winmdroot.UI.Shell.IAssocHandler[], uint*)"/>
		public static unsafe int Next(this winmdroot.UI.Shell.IEnumAssocHandlers @this, winmdroot.UI.Shell.IAssocHandler[] rgelt)
        {
            uint pceltFetched = 0;
            @this.Next((uint)rgelt.Length, rgelt, &pceltFetched);
            return (int)pceltFetched;
        }
    }
}
