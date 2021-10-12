using static InlineIL.FieldRef;
using static InlineIL.IL;
using static InlineIL.IL.Emit;
using static Windows.Win32.CorePInvoke;

namespace Windows.Win32.Security.Credentials
{
    public unsafe ref struct CredHandle
    {
        private CREDENTIALW* ptr;

        public ref CREDENTIALW Handle
        {
            get
            {
                // C# doesn't allow return ref ptr.
                // IL doesn't have a problem with it.
                Ldarg_0();
                Ldflda(Field(typeof(CredHandle), nameof(ptr)));
                Ret(); // return ref *(CREDENTIALW*)&ptr;
                throw Unreachable();
            }
        }

        public ref CREDENTIALW* Pointer
        {
            get
            {
                // C# doesn't allow return ref ptr.
                // IL doesn't have a problem with it.
                Ldarg_0();
                Ldflda(Field(typeof(CredHandle), nameof(ptr)));
                Ret(); // return ref ptr;
                throw Unreachable();
            }
        }

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
