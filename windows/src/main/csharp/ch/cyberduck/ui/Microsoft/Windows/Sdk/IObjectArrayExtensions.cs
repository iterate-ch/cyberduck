using System;
using System.Runtime.InteropServices;

namespace Ch.Cyberduck.Ui.Microsoft.Windows.Sdk
{
    public static partial class FriendlyOverloadExtensions
    {
        public unsafe static T GetAt<T>(this IObjectArray @this, int index)
        {
            void* temp;
            @this.GetAt((uint)index, typeof(T).GUID, &temp);
            object unkn = Marshal.GetObjectForIUnknown((IntPtr)temp);
            return (T)unkn;
        }
    }
}
