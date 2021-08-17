using System;
using System.Runtime.CompilerServices;

namespace Ch.Cyberduck.Core.Microsoft.Windows.Sdk.Security.Credentials
{
    using static PInvoke;

    public unsafe class CredHandle : IDisposable
    {
        private CREDENTIALW* ptr;

        public ref CREDENTIALW Value => ref Unsafe.AsRef<CREDENTIALW>(ptr);

        public void Dispose()
        {
            if (ptr != default)
            {
                CredFree(ptr);
                ptr = default;
            }
        }

        public ref CREDENTIALW* Put() => ref ptr;
    }
}
