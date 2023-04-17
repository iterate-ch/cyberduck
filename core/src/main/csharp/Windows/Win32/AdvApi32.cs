#pragma warning disable IDE0001,IDE0002,IDE0005,CS1591,CS1573,CS0465,CS0649,CS8019,CS1570,CS1584,CS1658,CS0436

namespace Windows.Win32
{
    using global::System;
    using global::System.Diagnostics;
    using global::System.Runtime.CompilerServices;
    using global::System.Runtime.InteropServices;
    using winmdroot = global::Windows.Win32;

    public partial class CorePInvoke
    {
		/// <inheritdoc cref="CredRead(winmdroot.Foundation.PCWSTR, uint, uint, winmdroot.Security.Credentials.CREDENTIALW**)"/>
        public static unsafe winmdroot.Security.Credentials.CredHandle CredRead(string TargetName, winmdroot.Security.Credentials.CRED_TYPE type, winmdroot.Security.Credentials.CRED_FLAGS flags)
        {
            fixed(char* targetNameLocal = TargetName)
            {
                winmdroot.Security.Credentials.CREDENTIALW* credential = default;
                bool __result = CredRead(targetNameLocal, (uint)type, (uint)flags, &credential);
                return new winmdroot.Security.Credentials.CredHandle(credential);
            }
        }
    }
}
