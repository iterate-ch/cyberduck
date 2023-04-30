using System.Diagnostics.CodeAnalysis;
using static Windows.Win32.CorePInvoke;

namespace Windows.Win32.Security.Credentials
{
    public unsafe ref struct CredHandle
    {
        private CREDENTIALW* ptr;

        public CredHandle(in CREDENTIALW* pointer)
        {
            ptr = pointer;
        }

        [UnscopedRef]
        public ref CREDENTIALW* Pointer => ref ptr;

        public ref CREDENTIALW Value => ref *ptr;

        public static implicit operator bool(in CredHandle @this) => @this.ptr != null;

        public void Dispose()
        {
            if (ptr != null)
            {
                CredFree(ptr);
                ptr = null;
            }
        }
    }
}
