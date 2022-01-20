using Windows.Win32.Foundation;

namespace Windows.Win32
{
    public unsafe partial class CorePInvoke
    {
        public static readonly PCWSTR TD_ERROR_ICON = MAKEINTRESOURCE(-2);
        public static readonly PCWSTR TD_INFORMATION_ICON = MAKEINTRESOURCE(-3);
        public static readonly PCWSTR TD_QUESTION_ICON = MAKEINTRESOURCE(104);
        public static readonly PCWSTR TD_SHIELD_ICON = MAKEINTRESOURCE(-4);
        public static readonly PCWSTR TD_WARNING_ICON = MAKEINTRESOURCE(-1);

        private static PCWSTR MAKEINTRESOURCE(int value) => MAKEINTRESOURCE(&value);

        private static PCWSTR MAKEINTRESOURCE(short value) => MAKEINTRESOURCE(&value);

        private static PCWSTR MAKEINTRESOURCE(ushort value) => MAKEINTRESOURCE(&value);

        private static PCWSTR MAKEINTRESOURCE(void* ptr) => (char*)*(ushort*)ptr;
    }
}
