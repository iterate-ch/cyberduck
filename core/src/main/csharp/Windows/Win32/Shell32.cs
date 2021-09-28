using System.Runtime.CompilerServices;

namespace Windows.Win32
{
    using Storage.FileSystem;
    using UI.Shell;

    partial class CyberduckCorePInvoke
    {
        public static unsafe nuint SHGetFileInfo(string pszPath, FILE_FLAGS_AND_ATTRIBUTES dwFileAttributes, in SHFILEINFOW sfi, SHGFI_FLAGS uFlags)
        {
            var psfi = (SHFILEINFOW*)Unsafe.AsPointer(ref Unsafe.AsRef(sfi));
            fixed (char* pszPathLocal = pszPath)
            {
                return SHGetFileInfo(pszPathLocal, dwFileAttributes, psfi, (uint)Unsafe.SizeOf<SHFILEINFOW>(), uFlags);
            }
        }
    }
}
