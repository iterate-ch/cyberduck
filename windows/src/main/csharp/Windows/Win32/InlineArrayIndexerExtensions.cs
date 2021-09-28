using System;

namespace Windows.Win32.UI.Shell
{
    public static partial class FriendlyOverloadExtensions
    {
        public unsafe static Span<char> AsSpan(this in DROPDESCRIPTION.__char_260 @this)
        {
            fixed (char* p0 = &@this._0)
                return new Span<char>(p0, @this.Length);
        }
    }
}
