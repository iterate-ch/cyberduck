using Windows.Win32.Security.Credentials;

namespace Windows.Win32
{
    public partial class CorePInvoke
    {
        /// <inheritdoc cref="CredRead(Foundation.PCWSTR, uint, uint, CREDENTIALW**)"/>
        public static unsafe bool CredRead(string TargetName, CRED_TYPE type, CRED_FLAGS flags, out CREDENTIALW credential)
        {
            fixed (CREDENTIALW* credentialLocal = &credential)
            fixed (char* targetNameLocal = TargetName)
            {
                return CredRead(targetNameLocal, (uint)type, (uint)flags, (CREDENTIALW**)credentialLocal);
            }
        }
    }
}
