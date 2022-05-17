#pragma warning disable IDE0001,IDE0002,IDE0005,CS1591,CS1573,CS0465,CS0649,CS8019,CS1570,CS1584,CS1658,CS0436

namespace Windows.Win32
{
    using global::System;
    using global::System.Buffers;
    using global::System.Diagnostics;
    using global::System.Runtime.CompilerServices;
    using global::System.Runtime.InteropServices;
    using winmdroot = global::Windows.Win32;

    /// <content>
    /// Contains extern methods from "ShlwApi.dll".
    /// </content>
    public static partial class CorePInvoke
    {
        /// <inheritdoc cref="SHLoadIndirectString(winmdroot.Foundation.PCWSTR, winmdroot.Foundation.PWSTR, uint, void**)"/>
		public static unsafe string SHLoadIndirectString(winmdroot.Foundation.PCWSTR pszSource)
        {
            var pool = ArrayPool<char>.Shared;

            char[] pszOutBuf;
            // include trailing zero-byte.
            int cchOutBuf = pszSource.Length + 1;
            winmdroot.Foundation.HRESULT __result;

        jump:
            pszOutBuf = pool.Rent(cchOutBuf);
            try
            {
                fixed (char* pszOutBufLocal = pszOutBuf)
                {
                    __result = CorePInvoke.SHLoadIndirectString(pszSource, pszOutBufLocal, (uint)pszOutBuf.Length, null);
                    if (__result.Succeeded)
                    {
                        return new winmdroot.Foundation.PWSTR(pszOutBufLocal).ToString();
                    }
                }
            }
            finally
            {
                pool.Return(pszOutBuf);
            }
            if (__result == STRSAFE_E_INSUFFICIENT_BUFFER)
            {
                cchOutBuf *= 2;
                goto jump;
            }
            __result.ThrowOnFailure();
            return default;
        }
    }
}
