using System;
using System.Runtime.InteropServices;
using Windows.Win32.Foundation;
using Windows.Win32.Storage.FileSystem;
using Windows.Win32.System.Com;
using Windows.Win32.UI.Shell;
using Windows.Win32.UI.Shell.Common;
using Windows.Win32.UI.WindowsAndMessaging;
using static System.Runtime.CompilerServices.Unsafe;

namespace Windows.Win32
{
    public partial class CorePInvoke
    {
        /// <inheritdoc cref="ExtractIconEx(winmdroot.Foundation.PCWSTR, int, winmdroot.UI.WindowsAndMessaging.HICON*, winmdroot.UI.WindowsAndMessaging.HICON*, uint)"/>
		public static unsafe uint ExtractIconEx(string lpszFile, int nIconIndex, in HICON phiconLarge, in HICON phiconSmall, uint nIcons)
        {
            fixed (char* lpszFileLocal = lpszFile)
            {
                uint __result = ExtractIconEx(lpszFileLocal, nIconIndex, (HICON*)phiconLarge.Value, (HICON*)phiconSmall.Value, nIcons);
                return __result;
            }
        }

        /// <inheritdoc cref="ILCreateFromPath(PCWSTR)"/>
        public static unsafe ref ITEMIDLIST ILCreateFromPath2(string pszPath)
        {
            fixed (char* pszPathLocal = pszPath)
            {
                ITEMIDLIST* __result = ILCreateFromPath(pszPathLocal);
                return ref *__result;
            }
        }

        /// <inheritdoc cref="SHCreateAssociationRegistration(global::System.Guid*, void**)"/>
        public static unsafe HRESULT SHCreateAssociationRegistration<T>(out T ppv)
        {
            Guid riid = typeof(T).GUID;
            void* ppvLocal;
            HRESULT __result = SHCreateAssociationRegistration(&riid, &ppvLocal);
            ppv = (T)Marshal.GetObjectForIUnknown((IntPtr)ppvLocal);
            return __result;
        }

        /// <inheritdoc cref="SHCreateFileExtractIcon(winmdroot.Foundation.PCWSTR, uint, global::System.Guid*, void**)"/>
		public static unsafe HRESULT SHCreateFileExtractIcon<T>(string pszFile, uint dwFileAttributes, out T ppv)
        {
            Guid riid = typeof(T).GUID;
            void* ppvLocal;
            fixed (char* pszFileLocal = pszFile)
            {
                HRESULT __result = SHCreateFileExtractIcon(pszFileLocal, dwFileAttributes, &riid, &ppvLocal);
                ppv = (T)Marshal.GetObjectForIUnknown((IntPtr)ppvLocal);
                return __result;
            }
        }

        /// <inheritdoc cref="SHCreateItemFromIDList(winmdroot.UI.Shell.Common.ITEMIDLIST*, global::System.Guid*, void**)"/>
        public static unsafe HRESULT SHCreateItemFromIDList<T>(in ITEMIDLIST pidl, out T ppv)
        {
            Guid riid = typeof(T).GUID;
            void* ppvLocal;
            fixed (ITEMIDLIST* pidlLocal = &pidl)
            {
                HRESULT __result = SHCreateItemFromIDList(pidlLocal, &riid, &ppvLocal);
                ppv = (T)Marshal.GetObjectForIUnknown((IntPtr)ppvLocal);
                return __result;
            }
        }

        /// <inheritdoc cref="SHGetFileInfo(PCWSTR, FILE_FLAGS_AND_ATTRIBUTES, SHFILEINFOW*, uint, SHGFI_FLAGS)"/>
        public static unsafe nuint SHGetFileInfo(string pszPath, FILE_FLAGS_AND_ATTRIBUTES dwFileAttributes, in SHFILEINFOW sfi, SHGFI_FLAGS uFlags)
        {
            fixed (SHFILEINFOW* psfiLocal = &sfi)
            fixed (char* pszPathLocal = pszPath)
            {
                return SHGetFileInfo(pszPathLocal, dwFileAttributes, psfiLocal, (uint)SizeOf<SHFILEINFOW>(), uFlags);
            }
        }

        /// <inheritdoc cref="SHParseDisplayName(string, IBindCtx, out ITEMIDLIST*, uint, uint*)"/>
        public static unsafe HRESULT SHParseDisplayName(string pszName, IBindCtx pbc, out ITEMIDLIST ppidl, uint sfgaoIn, out uint psfgaOut)
        {
            fixed (ITEMIDLIST* ppidlLocal = &ppidl)
            fixed (uint* psfgaOutLocal = &psfgaOut)
            fixed (char* pszNameLocal = pszName)
            {
                return SHParseDisplayName(pszNameLocal, pbc, (ITEMIDLIST**)ppidlLocal, sfgaoIn, psfgaOutLocal);
            }
        }
    }
}
