using System.Runtime.CompilerServices;

namespace Ch.Cyberduck.Core.Microsoft.Windows.Sdk
{
    partial class PInvoke
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
