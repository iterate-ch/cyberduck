using static InlineIL.FieldRef;
using static InlineIL.IL;
using static InlineIL.IL.Emit;
using static Windows.Win32.CorePInvoke;

namespace Windows.Win32.UI.WindowsAndMessaging
{
    public unsafe ref struct HICON_Handle
    {
        private HICON ptr;

        public HICON Ref
        {
            get
            {
                // C# doesn't allow return ref ptr.
                // IL doesn't have a problem with it.
                Ldarg_0();
                Ldflda(Field(typeof(HICON_Handle), nameof(ptr)));
                Ret();
                throw Unreachable();
            }
        }

        public ref HICON Value
        {
            get
            {
                // C# doesn't allow return ref ptr.
                // IL doesn't have a problem with it.
                Ldarg_0();
                Ldflda(Field(typeof(HICON_Handle), nameof(ptr)));
                Ret(); // return ref *(HICON*)&ptr;
                throw Unreachable();
            }
        }

        public static implicit operator bool(in HICON_Handle @this) => @this.ptr != null;

        public static implicit operator HICON_Handle(in HICON hicon) => new() { ptr = hicon };

        public void Dispose()
        {
            if (ptr != null)
            {
                DestroyIcon(ptr);
                ptr = default;
            }
        }
    }
}
