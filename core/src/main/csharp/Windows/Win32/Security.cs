using System;

namespace Windows.Win32
{
    using Security.Credentials;

    partial class CyberduckCorePInvoke
    {
        public static unsafe bool CredRead(string TargetName, CRED_TYPE type, CRED_FLAGS flags, out CREDENTIALW* credential)
        {
            fixed (CREDENTIALW** credentialLocal = &credential)
            fixed (char* targetNameLocal = TargetName)
            {
                return CredRead(targetNameLocal, (uint)type, (uint)flags, credentialLocal);
            }
        }
    }
}
