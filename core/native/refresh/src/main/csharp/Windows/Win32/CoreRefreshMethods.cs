using System;
using System.Buffers;
using Windows.Win32.Foundation;

namespace Windows.Win32;

public partial class CoreRefreshMethods
{
    /// <inheritdoc cref="SHGetImageList(int, Guid*, void**)"/>
    public static unsafe HRESULT SHGetImageList<T>(int iImageList, out T ppvObj)
    {
        Guid riid = typeof(T).GUID;
        HRESULT __result = SHGetImageList(iImageList, &riid, out var ppvObjLocal);
        ppvObj = (T)ppvObjLocal;
        return __result;
    }

    /// <inheritdoc cref="SHLoadIndirectString(PCWSTR, PWSTR, uint, void**)"/>
    public static string SHLoadIndirectString(string pszSource)
    {
        return SHLoadIndirectString(PCWSTR.DangerousFromString(pszSource));
    }

    /// <inheritdoc cref="SHLoadIndirectString(PCWSTR, PWSTR, uint, void**)"/>
    public static unsafe string SHLoadIndirectString(PCWSTR pszSource)
    {
        var pool = ArrayPool<char>.Shared;

        char[] pszOutBuf;
        // include trailing zero-byte.
        int cchOutBuf = pszSource.Length + 1;
        HRESULT __result;

    jump:
        pszOutBuf = pool.Rent(cchOutBuf);
        try
        {
            fixed (char* pszOutBufLocal = pszOutBuf)
            {
                __result = SHLoadIndirectString(pszSource, pszOutBufLocal, (uint)pszOutBuf.Length, null);
                if (__result.Succeeded)
                {
                    return new PWSTR(pszOutBufLocal).ToString();
                }
            }
        }
        finally
        {
            pool.Return(pszOutBuf);
        }

        if (__result == HRESULT.STRSAFE_E_INSUFFICIENT_BUFFER)
        {
            cchOutBuf *= 2;
            goto jump;
        }

        __result.ThrowOnFailure();
        return default;
    }

    /// <inheritdoc cref="SHCreateFileExtractIcon(PCWSTR, uint, Guid*, void**)"/>
    public static unsafe HRESULT SHCreateFileExtractIcon<T>(string pszFile, uint dwFileAttributes, out T ppv)
    {
        Guid riid = typeof(T).GUID;
        fixed (char* pszFileLocal = pszFile)
        {
            HRESULT __result = SHCreateFileExtractIcon(pszFileLocal, dwFileAttributes, &riid, out var ppvLocal);
            ppv = (T)ppvLocal;
            return __result;
        }
    }
}
