using System;

namespace Ch.Cyberduck.Core.Microsoft.Windows.Sdk
{
    using Security.Credentials;

    partial class PInvoke
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
