using Windows.Win32.UI.Shell;

namespace Windows.Win32;

public static partial class UI_Shell_IApplicationAssociationRegistration_Extensions
{
    /// <inheritdoc cref="winmdroot.UI.Shell.IQueryAssociations.GetString(uint, ASSOCSTR, Foundation.PCWSTR, Foundation.PWSTR, ref uint)"
    public static unsafe bool GetString(this IQueryAssociations @this, ASSOCSTR str, string pszExtra, out string pszOut)
    {
        var pool = global::System.Buffers.ArrayPool<char>.Shared;
        uint length = 0;
        try
        {
            @this.GetString(ASSOCF.ASSOCF_NOTRUNCATE, str, pszExtra, default, ref length);
            char[] buffer = null;
            try
            {
                buffer = pool.Rent((int)length);
                length = (uint)buffer.Length;
                fixed (char* bufferLocal = buffer)
                {
                    @this.GetString(ASSOCF.ASSOCF_NOTRUNCATE, str, pszExtra, bufferLocal, ref length);

                    pszOut = new(bufferLocal, 0, (int)length);
                    return true;
                }
            }
            finally
            {
                pool.Return(buffer);
            }
        }
        catch { }
        pszOut = default;
        return false;
    }
}
