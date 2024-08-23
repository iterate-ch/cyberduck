using System;
using System.Runtime.InteropServices;
using Windows.Win32.Foundation;
using Windows.Win32.Security.Credentials;
using Windows.Win32.Storage.FileSystem;
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

    /// <inheritdoc cref="ILCreateFromPath(PCWSTR)"/>
    public static unsafe SafeITEMIDLISTHandle ILCreateFromPathSafe(string pszPath)
    {
        fixed (char* pszPathLocal = pszPath)
        {
            ITEMIDLIST* __result = ILCreateFromPath(pszPathLocal);
            return new((nint)__result, true);
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

    /// <inheritdoc cref="SHGetFileInfo(PCWSTR, FILE_FLAGS_AND_ATTRIBUTES, SHFILEINFOW*, uint, SHGFI_FLAGS)"/>
    public static unsafe nuint SHGetFileInfo(string pszPath, FILE_FLAGS_AND_ATTRIBUTES dwFileAttributes, in SHFILEINFOW sfi, SHGFI_FLAGS uFlags)
    {
        fixed (SHFILEINFOW* psfiLocal = &sfi)
        fixed (char* pszPathLocal = pszPath)
        {
            return SHGetFileInfo(pszPathLocal, dwFileAttributes, psfiLocal, (uint)Marshal.SizeOf<SHFILEINFOW>(), uFlags);
        }
    }
}
