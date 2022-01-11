using static InlineIL.FieldRef;
using static InlineIL.IL;
using static InlineIL.IL.Emit;
using static System.Runtime.CompilerServices.Unsafe;
using static Windows.Win32.CorePInvoke;

namespace Windows.Win32.UI.Shell.Common
{
    public unsafe ref struct PIDLIST_ABSOLUTEHandle
    {
        private ITEMIDLIST* ptr;

        public ref ITEMIDLIST Handle
        {
            get
            {
                // C# doesn't allow return ref ptr.
                // IL doesn't have a problem with it.
                Ldarg_0();
                Ldflda(Field(typeof(PIDLIST_ABSOLUTEHandle), nameof(ptr)));
                Ret(); // return ref *(ITEMIDLIST*)&ptr;
                throw Unreachable();
            }
        }

        public ref ITEMIDLIST* Pointer
        {
            get
            {
                // C# doesn't allow return ref ptr.
                // IL doesn't have a problem with it.
                Ldarg_0();
                Ldflda(Field(typeof(PIDLIST_ABSOLUTEHandle), nameof(ptr)));
                Ret(); // return ref ptr;
                throw Unreachable();
            }
        }

        public ref ITEMIDLIST Value => ref *ptr;

        public static implicit operator bool(in PIDLIST_ABSOLUTEHandle @this) => @this.ptr != null;

        public static implicit operator PIDLIST_ABSOLUTEHandle(in ITEMIDLIST pidl) => new() { ptr = (ITEMIDLIST*)AsPointer(ref AsRef(pidl)) };

        public void Dispose()
        {
            if (ptr != null)
            {
                CoTaskMemFree(ptr);
                ptr = null;
            }
        }
    }
}
