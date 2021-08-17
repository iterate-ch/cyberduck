using System;
using System.Runtime.InteropServices;

namespace Ch.Cyberduck.Ui.Microsoft.Windows.Sdk.UI.Shell
{
    public static partial class FriendlyOverloadExtensions
    {
        public unsafe static T GetAt<T>(this IObjectArray @this, int index)
        {
            @this.GetAt((uint)index, typeof(T).GUID, out var temp);
            object unkn = Marshal.GetObjectForIUnknown((IntPtr)temp);
            return (T)unkn;
        }
    }
}
