using System;
using System.Runtime.InteropServices;

namespace Windows.Win32;

public class CoTaskMemFreeHandle(bool ownsHandle) : SafeHandle(IntPtr.Zero, ownsHandle)
{
    public override bool IsInvalid => handle == IntPtr.Zero;

    public CoTaskMemFreeHandle(nint handle, bool ownsHandle) : this(ownsHandle)
    {
        SetHandle(handle);
    }

    protected override bool ReleaseHandle()
    {
        Marshal.FreeCoTaskMem(handle);
        return true;
    }
}
