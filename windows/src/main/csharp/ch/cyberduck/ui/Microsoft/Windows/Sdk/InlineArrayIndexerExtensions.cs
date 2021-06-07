using System;

namespace Ch.Cyberduck.Ui.Microsoft.Windows.Sdk
{
    public static partial class InlineArrayIndexerExtensions
    {
        public unsafe static Span<ushort> AsSpan(this in DROPDESCRIPTION.__ushort_260 @this)
        {
            fixed (ushort* p0 = &@this._0)
                return new Span<ushort>(p0, @this.Length);
        }
    }
}
