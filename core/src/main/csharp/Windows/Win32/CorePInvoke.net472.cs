using System;
using System.Runtime.InteropServices;
using Windows.Win32.Storage.FileSystem;

namespace Windows.Win32;

partial class CorePInvoke
{
    public static unsafe partial uint GetFinalPathNameByHandle(SafeHandle hFile, Span<char> lpszFilePath, GETFINALPATHNAMEBYHANDLE_FLAGS dwFlags);
}
