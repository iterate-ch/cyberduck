#include "stdafx.h"
#include <vector>
#include <VersionHelpers.h>

BOOL GetKernelVersion(VS_FIXEDFILEINFO& vInfo)
{
    static const wchar_t kernel32[] = L"\\kernel32.dll";
    wchar_t path[MAX_PATH];

    unsigned int n = GetSystemDirectory(path, MAX_PATH);
    memcpy_s(path + n, MAX_PATH, kernel32, sizeof(kernel32));

    unsigned int size = GetFileVersionInfoSize(path, NULL);
    if (size == 0)
    {
        return false;
    }

    std::vector<char> verionInfo;
    verionInfo.resize(size);
    BOOL result = GetFileVersionInfo(path, 0, size, verionInfo.data());
    if (!result || GetLastError() != S_OK)
    {
        return false;
    }

    VS_FIXEDFILEINFO *vinfo;
    result = VerQueryValue(verionInfo.data(), L"\\", (LPVOID *)&vinfo, &size);
    vInfo = *vinfo;

    return result;
}

OSVERSIONINFO GetOSVersionInfo()
{
    OSVERSIONINFO osvi;

    VS_FIXEDFILEINFO vinfo;
    if (GetKernelVersion(vinfo))
    {
        osvi.dwMajorVersion = HIWORD(vinfo.dwProductVersionMS);
        osvi.dwMinorVersion = LOWORD(vinfo.dwProductVersionMS);
        osvi.dwBuildNumber = HIWORD(vinfo.dwProductVersionLS);
    }

    return osvi;
}

bool windows10orGreater()
{
    static const wchar_t kernel32[] = L"\\kernel32.dll";
    wchar_t path[MAX_PATH];

    unsigned int n = GetSystemDirectory(path, MAX_PATH);
    memcpy_s(path + n, MAX_PATH, kernel32, sizeof(kernel32));

    unsigned int size = GetFileVersionInfoSize(path, NULL);
    if (size == 0)
    {
        return false;
    }

    std::vector<char> verionInfo;
    verionInfo.resize(size);
    BOOL result = GetFileVersionInfo(path, 0, size, verionInfo.data());
    if (!result || GetLastError() != S_OK)
    {
        return false;
    }

    VS_FIXEDFILEINFO *vinfo;
    result = VerQueryValue(verionInfo.data(), L"\\", (LPVOID *)&vinfo, &size);
    if (!result || size < sizeof(VS_FIXEDFILEINFO))
    {
        return false;
    }

    return HIWORD(vinfo->dwProductVersionMS) >= 10;
}

UINT __stdcall CustomAction1(
    MSIHANDLE hInstall
)
{
    HRESULT hr = S_OK;
    UINT er = ERROR_SUCCESS;

    hr = WcaInitialize(hInstall, "CustomAction1");
    ExitOnFailure(hr, "Failed to initialize");

    WcaLog(LOGMSG_STANDARD, "Initialized.");

    // TODO: Add your custom action code here.
    auto eightOrGreater = IsWindows8OrGreater();
    if (eightOrGreater)
    {
        auto osVersion = GetOSVersionInfo();
        hr = WcaSetIntProperty(L"VersionNT", osVersion.dwMajorVersion * 100 + osVersion.dwMinorVersion);
        ExitOnFailure(hr, "Failed on set VersionNT");
        hr = WcaSetIntProperty(L"WindowsBuild", osVersion.dwBuildNumber);
        ExitOnFailure(hr, "Failed on set WindowsBuild");
    }

LExit:
    er = SUCCEEDED(hr) ? ERROR_SUCCESS : ERROR_INSTALL_FAILURE;
    return WcaFinalize(er);
}


// DllMain - Initialize and cleanup WiX custom action utils.
extern "C" BOOL WINAPI DllMain(
    __in HINSTANCE hInst,
    __in ULONG ulReason,
    __in LPVOID
)
{
    switch (ulReason)
    {
    case DLL_PROCESS_ATTACH:
        WcaGlobalInitialize(hInst);
        break;

    case DLL_PROCESS_DETACH:
        WcaGlobalFinalize();
        break;
    }

    return TRUE;
}
