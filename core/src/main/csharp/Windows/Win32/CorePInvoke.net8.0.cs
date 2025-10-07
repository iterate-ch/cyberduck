using System;
using System.Runtime.InteropServices;
using System.Runtime.Versioning;
using Windows.Win32.Storage.FileSystem;

namespace Windows.Win32;

partial class CorePInvoke
{
    [SupportedOSPlatform("windows6.0.6000")]
    public static unsafe partial uint GetFinalPathNameByHandle(SafeHandle hFile, Span<char> lpszFilePath, GETFINALPATHNAMEBYHANDLE_FLAGS dwFlags);
}
