#pragma warning disable IDE0001,IDE0002,IDE0005,CS1591,CS1573,CS0465,CS0649,CS8019,CS1570,CS1584,CS1658,CS0436

namespace Windows.Win32
{
    using global::System;
    using global::System.Diagnostics;
    using global::System.Runtime.CompilerServices;
    using global::System.Runtime.InteropServices;
    using winmdroot = global::Windows.Win32;

    public partial class CorePInvoke
    {
        /// <inheritdoc cref="ExtractIconEx(winmdroot.Foundation.PCWSTR, int, winmdroot.UI.WindowsAndMessaging.HICON*, winmdroot.UI.WindowsAndMessaging.HICON*, uint)"/>
		public static unsafe uint ExtractIconEx(string lpszFile, int nIconIndex, in winmdroot.UI.WindowsAndMessaging.HICON phiconLarge, in winmdroot.UI.WindowsAndMessaging.HICON phiconSmall, uint nIcons)
        {
            fixed (char* lpszFileLocal = lpszFile)
            fixed (winmdroot.UI.WindowsAndMessaging.HICON* phiconLargeLocal = &phiconLarge)
            fixed (winmdroot.UI.WindowsAndMessaging.HICON* phiconSmallLocal = &phiconSmall)
            {
                uint __result = ExtractIconEx(lpszFileLocal, nIconIndex, phiconLargeLocal, phiconSmallLocal, nIcons);
                return __result;
            }
        }

        /// <inheritdoc cref="SHCreateAssociationRegistration(global::System.Guid*, void**)"/>
        public static unsafe winmdroot.Foundation.HRESULT SHCreateAssociationRegistration<T>(out T ppv)
        {
            Guid riid = typeof(T).GUID;
            void* ppvLocal;
            winmdroot.Foundation.HRESULT __result = SHCreateAssociationRegistration(&riid, &ppvLocal);
            ppv = (T)Marshal.GetObjectForIUnknown((IntPtr)ppvLocal);
            return __result;
        }

        /// <inheritdoc cref="SHCreateFileExtractIcon(winmdroot.Foundation.PCWSTR, uint, global::System.Guid*, void**)"/>
		public static unsafe winmdroot.Foundation.HRESULT SHCreateFileExtractIcon<T>(string pszFile, uint dwFileAttributes, out T ppv)
        {
            Guid riid = typeof(T).GUID;
            void* ppvLocal;
            fixed (char* pszFileLocal = pszFile)
            {
                winmdroot.Foundation.HRESULT __result = SHCreateFileExtractIcon(pszFileLocal, dwFileAttributes, &riid, &ppvLocal);
                ppv = (T)Marshal.GetObjectForIUnknown((IntPtr)ppvLocal);
                return __result;
            }
        }

        /// <inheritdoc cref="SHCreateItemFromParsingName(winmdroot.Foundation.PCWSTR, winmdroot.System.Com.IBindCtx, global::System.Guid*, out object)">
        public static unsafe winmdroot.Foundation.HRESULT SHCreateItemFromParsingName<T>(string pszFile, winmdroot.System.Com.IBindCtx pbc, out T ppv)
        {
            Guid riid = typeof(T).GUID;
            fixed (char* pszFileLocal = pszFile)
            {
                winmdroot.Foundation.HRESULT __result = SHCreateItemFromParsingName(pszFileLocal, pbc, &riid, out var ppvLocal);
                ppv = (T)ppvLocal;
                return __result;
            }
        }

        /// <inheritdoc cref="SHCreateItemFromIDList(winmdroot.UI.Shell.Common.ITEMIDLIST*, global::System.Guid*, void**)"/>
        public static unsafe winmdroot.Foundation.HRESULT SHCreateItemFromIDList<T>(in winmdroot.UI.Shell.Common.ITEMIDLIST pidl, out T ppv)
        {
            Guid riid = typeof(T).GUID;
            void* ppvLocal;
            fixed (winmdroot.UI.Shell.Common.ITEMIDLIST* pidlLocal = &pidl)
            {
                winmdroot.Foundation.HRESULT __result = SHCreateItemFromIDList(pidlLocal, &riid, &ppvLocal);
                ppv = (T)Marshal.GetObjectForIUnknown((IntPtr)ppvLocal);
                return __result;
            }
        }

        /// <inheritdoc cref="SHGetFileInfo(winmdroot.Foundation.PCWSTR, winmdroot.Storage.FileSystem.FILE_FLAGS_AND_ATTRIBUTES, winmdroot.UI.Shell.SHFILEINFOW*, uint, winmdroot.UI.Shell.SHGFI_FLAGS)"/>
        public static unsafe nuint SHGetFileInfo(string pszPath, winmdroot.Storage.FileSystem.FILE_FLAGS_AND_ATTRIBUTES dwFileAttributes, in winmdroot.UI.Shell.SHFILEINFOW sfi, winmdroot.UI.Shell.SHGFI_FLAGS uFlags)
        {
            fixed (winmdroot.UI.Shell.SHFILEINFOW* psfiLocal = &sfi)
            fixed (char* pszPathLocal = pszPath)
            {
                return SHGetFileInfo(pszPathLocal, dwFileAttributes, psfiLocal, (uint)Marshal.SizeOf<winmdroot.UI.Shell.SHFILEINFOW>(), uFlags);
            }
        }

        /// <inheritdoc cref="SHGetKnownFolderPath(Guid*, uint, winmdroot.Foundation.HANDLE, winmdroot.Foundation.PWSTR*)"/>
        public static unsafe string SHGetKnownFolderPath(in Guid rfid, winmdroot.UI.Shell.KNOWN_FOLDER_FLAG dwFlags, winmdroot.Foundation.HANDLE hToken)
        {
            fixed (Guid* rfidLocal = &rfid)
            {
                winmdroot.Foundation.PWSTR pszPath = default;
                try
                {
                    winmdroot.Foundation.HRESULT __result = SHGetKnownFolderPath(rfidLocal, (uint)dwFlags, hToken, &pszPath);
                    __result.ThrowOnFailure();
                    return pszPath.ToString();
                }
                finally
                {
                    CoTaskMemFree(pszPath);
                }
            }
        }
    }
}
