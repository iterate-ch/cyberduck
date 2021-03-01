using System;

namespace Ch.Cyberduck.Core.Microsoft.Windows.Sdk
{
    [Flags]
    public enum CRED_FLAGS : uint
    {
        CRED_FLAGS_PROMPT_NOW = 0x2,
        CRED_FLAGS_USERNAME_TARGET = 0x4
    }

    public enum CRED_PERSIST : uint
    {
        CRED_PERSIST_SESSION = 1,
        CRED_PERSIST_LOCAL_MACHINE = 2,
        CRED_PERSIST_ENTERPRISE = 3
    }

    public enum CRED_TYPE : uint
    {
        CRED_TYPE_GENERIC = 1,
        CRED_TYPE_DOMAIN_PASSWORD = 2,
        CRED_TYPE_DOMAIN_CERTIFICATE = 3,
        CRED_TYPE_DOMAIN_VISIBLE_PASSWORD = 4,
        CRED_TYPE_GENERIC_CERTIFICATE = 5,
        CRED_TYPE_DOMAIN_EXTENDED = 6,
        CRED_TYPE_MAXIMUM = 7,
        CRED_TYPE_MAXIMUM_EX = (CRED_TYPE_MAXIMUM + 1000)
    }

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
