using System;
using System.Runtime.InteropServices;
using Windows.Win32.Security.Credentials;
using static Windows.Win32.CorePInvoke;

namespace Windows.Win32;

public class SafeCredentialHandle(bool ownsHandle) : SafeHandle(IntPtr.Zero, ownsHandle)
{
    public override bool IsInvalid => handle == IntPtr.Zero;

    public unsafe ref readonly CREDENTIALW Credential => ref *(CREDENTIALW*)handle;

    public SafeCredentialHandle(nint handle, bool ownsHandle) : this(ownsHandle)
    {
        SetHandle(handle);
    }

    protected override unsafe bool ReleaseHandle()
    {
        CredFree((void*)handle);
        return true;
    }
}
