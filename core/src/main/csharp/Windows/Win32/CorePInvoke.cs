using System;
using System.Runtime.InteropServices;
using Microsoft.Win32.SafeHandles;
using Windows.Win32.Foundation;
using Windows.Win32.Security;
using Windows.Win32.Security.Credentials;
using Windows.Win32.Storage.FileSystem;
using Windows.Win32.System.Com;
using Windows.Win32.System.SystemServices;
using Windows.Win32.UI.Shell;
using Windows.Win32.UI.Shell.Common;

namespace Windows.Win32;

public unsafe partial class CorePInvoke
{
    public static readonly PCWSTR TD_QUESTION_ICON = (char*)104;

    /// <inheritdoc cref="LoadString(HINSTANCE, uint, PWSTR, int)"/>
    public static unsafe int LoadString(SafeHandle hInstance, uint uID, out PCWSTR lpBuffer)
    {
        fixed (PCWSTR* lpBufferLocal = &lpBuffer)
        {
            int __result = LoadString(hInstance, uID, (char*)lpBufferLocal, 0);
            return __result;
        }
    }

    /// <inheritdoc cref="CreateFile(PCWSTR, uint, FILE_SHARE_MODE, SECURITY_ATTRIBUTES*, FILE_CREATION_DISPOSITION, FILE_FLAGS_AND_ATTRIBUTES, HANDLE)"/>
    public static unsafe SafeFileHandle CreateFile(
        in ReadOnlySpan<char> lpFileName,
        uint dwDesiredAccess,
        FILE_SHARE_MODE dwShareMode,
        SECURITY_ATTRIBUTES? lpSecurityAttributes,
        FILE_CREATION_DISPOSITION dwCreationDisposition,
        FILE_FLAGS_AND_ATTRIBUTES dwFlagsAndAttributes,
        SafeHandle hTemplateFile)
    {
        bool hTemplateFileAddRef = false;
        try
        {
            fixed (char* lpFileNameLocal = lpFileName)
            {
                SECURITY_ATTRIBUTES lpSecurityAttributesLocal = lpSecurityAttributes ?? default(SECURITY_ATTRIBUTES);
                HANDLE hTemplateFileLocal;
                if (hTemplateFile is object)
                {
                    hTemplateFile.DangerousAddRef(ref hTemplateFileAddRef);
                    hTemplateFileLocal = (HANDLE)hTemplateFile.DangerousGetHandle();
                }
                else
                    hTemplateFileLocal = (HANDLE)new IntPtr(0L);
                HANDLE __result = CorePInvoke.CreateFile(
                    lpFileName: lpFileNameLocal,
                    dwDesiredAccess: dwDesiredAccess,
                    dwShareMode: dwShareMode,
                    lpSecurityAttributes: lpSecurityAttributes.HasValue ? &lpSecurityAttributesLocal : null,
                    dwCreationDisposition: dwCreationDisposition,
                    dwFlagsAndAttributes: dwFlagsAndAttributes,
                    hTemplateFile: hTemplateFileLocal);
                return new SafeFileHandle(__result, ownsHandle: true);
            }
        }
        finally
        {
            if (hTemplateFileAddRef)
                hTemplateFile.DangerousRelease();
        }
    }

    /// <inheritdoc cref="CredDelete(PCWSTR, CRED_TYPE, uint)" />
    public static unsafe bool CredDelete(string TargetName, CRED_TYPE type, CRED_FLAGS flags)
    {
        fixed (char* targetNameLocal = TargetName)
        {
            BOOL __result = CredDelete(targetNameLocal, type, (uint)flags);
            return __result;
        }
    }

    /// <inheritdoc cref="CredRead(PCWSTR, CRED_TYPE, uint, CREDENTIALW**)"/>
    public static unsafe SafeCredentialHandle CredRead(string TargetName, CRED_TYPE type, CRED_FLAGS flags)
    {
        CREDENTIALW* credential = default;
        fixed (char* targetNameLocal = TargetName)
        {
            _ = CredRead(targetNameLocal, type, (uint)flags, &credential);
        }
        return new((nint)credential, true);
    }

    /// <inheritdoc cref="GetFileInformationByHandleEx(HANDLE, FILE_INFO_BY_HANDLE_CLASS, void*, uint)"/>
    public static unsafe BOOL GetFileInformationByHandleEx<T>(SafeHandle hFile, FILE_INFO_BY_HANDLE_CLASS FileInformationClass, out T value) where T : unmanaged
    {
        fixed (T* valueLocal = &value)
        {
            return GetFileInformationByHandleEx(hFile, FileInformationClass, valueLocal, (uint)Marshal.SizeOf<T>());
        }
    }

    /// <inheritdoc cref="GetFinalPathNameByHandle(HANDLE, PWSTR, uint, GETFINALPATHNAMEBYHANDLE_FLAGS)"/>
    public static unsafe partial uint GetFinalPathNameByHandle(SafeHandle hFile, Span<char> lpszFilePath, GETFINALPATHNAMEBYHANDLE_FLAGS dwFlags)
    {
        fixed (char* lpszFilePathLocal = lpszFilePath)
        {
            return GetFinalPathNameByHandle(hFile, lpszFilePathLocal, (uint)lpszFilePath.Length, dwFlags);
        }
    }

    /// <inheritdoc cref="ILClone(ITEMIDLIST*)" />
    public static unsafe SafeITEMIDLISTHandle ILClone(in SafeITEMIDLISTHandle pidl, bool leaveOpen = false)
    {
        bool pidlAddRef = false;
        try
        {
            ITEMIDLIST* pidlLocal;
            if (pidl is not null)
            {
                pidl.DangerousAddRef(ref pidlAddRef);
                pidlLocal = (ITEMIDLIST*)pidl.DangerousGetHandle();
            }
            else
            {
                throw new ArgumentNullException(nameof(pidl));
            }

            return new((nint)ILClone(pidlLocal), true);
        }
        finally
        {
            if (pidlAddRef)
            {
                pidl.DangerousRelease();

                if (!leaveOpen)
                {
                    pidl.Dispose();
                }
            }
        }
    }

    /// <inheritdoc cref="ILCreateFromPath(PCWSTR)"/>
    public static unsafe SafeITEMIDLISTHandle ILCreateFromPathSafe(string pszPath)
    {
        fixed (char* pszPathLocal = pszPath)
        {
#pragma warning disable RS0030
            ITEMIDLIST* __result = ILCreateFromPath(pszPathLocal);
#pragma warning restore RS0030
            return new((nint)__result, true);
        }
    }

    /// <inheritdoc cref="ILFindLastID(ITEMIDLIST*)" />
    public static unsafe SafeITEMIDLISTHandle ILFindLastID(in SafeITEMIDLISTHandle pidl)
    {
        bool pidlAddRef = false;
        try
        {
            ITEMIDLIST* pidlLocal;
            if (pidl is not null)
            {
                pidl.DangerousAddRef(ref pidlAddRef);
                pidlLocal = (ITEMIDLIST*)pidl.DangerousGetHandle();
            }
            else
            {
                throw new ArgumentNullException(nameof(pidl));
            }

            return new((nint)ILFindLastID(pidlLocal), false);
        }
        finally
        {
            if (pidlAddRef)
            {
                pidl.DangerousRelease();
            }
        }
    }

    /// <inheritdoc cref="ILRemoveLastID(ITEMIDLIST*)" />
    public static unsafe BOOL ILRemoveLastID(SafeITEMIDLISTHandle pidl)
    {
        bool pidlAddRef = false;
        try
        {
            ITEMIDLIST* pidlLocal;
            if (pidl is not null)
            {
                pidl.DangerousAddRef(ref pidlAddRef);
                pidlLocal = (ITEMIDLIST*)pidl.DangerousGetHandle();
            }
            else
            {
                throw new ArgumentNullException(nameof(pidl));
            }

            return ILRemoveLastID(pidlLocal);
        }
        finally
        {
            if (pidlAddRef)
            {
                pidl.DangerousRelease();
            }
        }
    }

    /// <inheritdoc cref="PathCchCanonicalizeEx(PWSTR, nuint, PCWSTR, PATHCCH_OPTIONS)"/>
    public static unsafe HRESULT PathCchCanonicalizeEx(ref Span<char> pszPathOut, string pszPathIn, PATHCCH_OPTIONS dwFlags)
    {
        fixed (char* ppszPathOut = pszPathOut)
        {
            PWSTR wstrpszPathOut = ppszPathOut;
#pragma warning disable RS0030
            HRESULT __result = CorePInvoke.PathCchCanonicalizeEx(wstrpszPathOut, (nuint)pszPathOut.Length, pszPathIn, dwFlags);
#pragma warning restore RS0030
            pszPathOut = pszPathOut.Slice(0, wstrpszPathOut.Length);
            return __result;
        }
    }

    /// <inheritdoc cref="SHCreateAssociationRegistration(Guid*, object)"/>
    public static unsafe HRESULT SHCreateAssociationRegistration<T>(out T ppv) where T : class
    {
        Guid riid = typeof(T).GUID;
        HRESULT __result = SHCreateAssociationRegistration(&riid, out var ppvLocal);
        ppv = ppvLocal as T;
        return __result;
    }

    /// <inheritdoc cref="SendMessage(HWND, uint, WPARAM, LPARAM)"/>
    public static unsafe LRESULT SendMessage(HWND hWnd, uint Msg, WPARAM wParam, in string lParam)
        => SendMessage(hWnd, Msg, wParam, PCWSTR.DangerousFromString(lParam));

    /// <inheritdoc cref="SendMessage(HWND, uint, WPARAM, LPARAM)"/>
    public static unsafe LRESULT SendMessage(HWND hWnd, uint Msg, WPARAM wParam, PCWSTR lParam)
        => SendMessage(hWnd, Msg, wParam, (nint)lParam.Value);

    /// <inheritdoc cref="SHParseDisplayName(PCWSTR, IBindCtx, ITEMIDLIST**, uint, uint*)" />
    public static unsafe HRESULT SHParseDisplayName(string pszName, IBindCtx pbc, SFGAO_FLAGS sfgaoIn, out SafeITEMIDLISTHandle ppidl, out SFGAO_FLAGS sfgaoOut)
    {
        fixed (SFGAO_FLAGS* sfgaoOutLocal = &sfgaoOut)
        {
            var __result = SHParseDisplayName(pszName, pbc, out var ppidlLocal, (uint)sfgaoIn, (uint*)sfgaoOutLocal);
            ppidl = new((nint)ppidlLocal, true);
            return __result;
        }
    }

    /// <inheritdoc cref="SHOpenFolderAndSelectItems(ITEMIDLIST*, uint, ITEMIDLIST**, uint)"/>
    public static unsafe HRESULT SHOpenFolderAndSelectItems(SafeITEMIDLISTHandle pidlFolder, SafeITEMIDLISTHandle apidl)
    {
        bool pidlFolderAddRef = false;
        bool apidlAddRef = false;
        try
        {
            ITEMIDLIST* pidlFolderLocal;
            if (pidlFolder is not null)
            {
                pidlFolder.DangerousAddRef(ref pidlFolderAddRef);
                pidlFolderLocal = (ITEMIDLIST*)pidlFolder.DangerousGetHandle();
            }
            else
            {
                throw new ArgumentNullException(nameof(pidlFolder));
            }

            ITEMIDLIST* apidlLocal;
            if (apidl is not null)
            {
                apidl.DangerousAddRef(ref apidlAddRef);
                apidlLocal = (ITEMIDLIST*)apidl.DangerousGetHandle();
            }
            else
            {
                throw new ArgumentNullException(nameof(apidl));
            }

            return SHOpenFolderAndSelectItems(pidlFolderLocal, 1, &apidlLocal, 0);
        }
        finally
        {
            if (apidlAddRef)
            {
                apidl.DangerousRelease();
            }

            if (pidlFolderAddRef)
            {
                pidlFolder.DangerousRelease();
            }
        }
    }

    /// <inheritdoc cref="SHGetKnownFolderPath(Guid*, uint, HANDLE, PWSTR*)"/>
    public static unsafe string SHGetKnownFolderPath(in Guid rfid, KNOWN_FOLDER_FLAG dwFlags, HANDLE hToken)
    {
        fixed (Guid* rfidLocal = &rfid)
        {
            PWSTR pszPath = default;
            try
            {
                HRESULT __result = SHGetKnownFolderPath(rfidLocal, dwFlags, hToken, &pszPath);
                __result.ThrowOnFailure();
                return pszPath.ToString();
            }
            finally
            {
                CoTaskMemFree(pszPath);
            }
        }
    }

    /// <inheritdoc cref="SHGetFileInfo(PCWSTR, FILE_FLAGS_AND_ATTRIBUTES, SHFILEINFOW*, uint, SHGFI_FLAGS)"/>
    public static unsafe nuint SHGetFileInfo(string pszPath, FILE_FLAGS_AND_ATTRIBUTES dwFileAttributes, in SHFILEINFOW sfi, SHGFI_FLAGS uFlags)
    {
        fixed (SHFILEINFOW* psfiLocal = &sfi)
        fixed (char* pszPathLocal = pszPath)
        {
            return SHGetFileInfo(pszPathLocal, dwFileAttributes, psfiLocal, (uint)Marshal.SizeOf<SHFILEINFOW>(), uFlags);
        }
    }

    /// <inheritdoc cref="SHCreateDataObject(ITEMIDLIST*, uint, ITEMIDLIST**, IDataObject, Guid*, out object)" />
    public static unsafe HRESULT SHCreateDataObject(SafeITEMIDLISTHandle pidlFolder, SafeITEMIDLISTHandle apidl, out IDataObject ppv)
    {
        bool pidlFolderAddRef = false;
        bool apidlAddRef = false;
        try
        {
            ITEMIDLIST* pidlFolderLocal;
            if (pidlFolder is not null)
            {
                pidlFolder.DangerousAddRef(ref pidlFolderAddRef);
                pidlFolderLocal = (ITEMIDLIST*)pidlFolder.DangerousGetHandle();
            }
            else
            {
                throw new ArgumentNullException(nameof(pidlFolder));
            }

            ITEMIDLIST* apidlLocal;
            if (apidl is not null)
            {
                apidl.DangerousAddRef(ref apidlAddRef);
                apidlLocal = (ITEMIDLIST*)apidl.DangerousGetHandle();
            }
            else
            {
                throw new ArgumentNullException(nameof(apidl));
            }

            return LocalExternFunction(pidlFolderLocal, 1, &apidlLocal, null, typeof(IDataObject).GUID, out ppv);
        }
        finally
        {
            if (apidlAddRef)
            {
                apidl.DangerousRelease();
            }

            if (pidlFolderAddRef)
            {
                pidlFolder.DangerousRelease();
            }
        }

        [DllImport("SHELL32.dll", ExactSpelling = true, EntryPoint = "SHCreateDataObject")]
        [DefaultDllImportSearchPaths(DllImportSearchPath.System32)]
        static extern unsafe HRESULT LocalExternFunction(
            [Optional] ITEMIDLIST* pidlFolder,
            uint cidl,
            [Optional] ITEMIDLIST** apidl,
            IDataObject pdtInner,
            in Guid riid,
            [MarshalAs(UnmanagedType.Interface)] out IDataObject ppv);
    }

    /// <inheritdoc cref="SHCreateItemFromIDList(ITEMIDLIST*, Guid*, void**)"/>
    public static unsafe HRESULT SHCreateItemFromIDList<T>(in SafeITEMIDLISTHandle pidl, out T ppv) where T : class
    {
        bool hPidlAddRef = false;
        try
        {
            ITEMIDLIST* pidlLocal;
            if (pidl is not null)
            {
                pidl.DangerousAddRef(ref hPidlAddRef);
                pidlLocal = (ITEMIDLIST*)pidl.DangerousGetHandle();
            }
            else
            {
                pidlLocal = (ITEMIDLIST*)IntPtr.Zero;
            }

            Guid riid = typeof(T).GUID;
            HRESULT __result = SHCreateItemFromIDList(pidlLocal, &riid, out var ppvLocal);
            ppv = (T)ppvLocal;
            return __result;
        }
        finally
        {
            if (hPidlAddRef)
            {
                pidl.DangerousRelease();
            }
        }
    }

    /// <inheritdoc cref="SHOpenFolderAndSelectItems(ITEMIDLIST*, uint, ITEMIDLIST**, uint)"/>
    public static unsafe HRESULT SHOpenFolderAndSelectItems(SafeITEMIDLISTHandle pidlFolder, in ReadOnlySpan<PITEMIDLIST> apidl, uint dwFlags)
    {
        bool hPidlFolderAddRef = false;
        try
        {
            fixed (PITEMIDLIST* apidlLocal = apidl)
            {
                ITEMIDLIST* pidlFolderLocal;
                if (pidlFolder is not null)
                {
                    pidlFolder.DangerousAddRef(ref hPidlFolderAddRef);
                    pidlFolderLocal = (ITEMIDLIST*)pidlFolder.DangerousGetHandle();
                }
                else
                {
                    pidlFolderLocal = (ITEMIDLIST*)IntPtr.Zero;
                }

                HRESULT __result = SHOpenFolderAndSelectItems(pidlFolderLocal, (uint)apidl.Length, (ITEMIDLIST**)apidlLocal, dwFlags);
                return __result;
            }
        }
        finally
        {
            if (hPidlFolderAddRef)
            {
                pidlFolder.DangerousRelease();
            }
        }
    }
}
